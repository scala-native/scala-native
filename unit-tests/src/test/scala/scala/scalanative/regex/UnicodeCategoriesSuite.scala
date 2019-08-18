package scala.scalanative
package regex

import java.util
import java.util.regex.PatternSyntaxException

import scala.collection.immutable.TreeSet

// import ScalaTestCompat.fail
import ScalaTestCompat.{fail, ignore}

import RE2.{
  FOLD_CASE,
  LITERAL,
  MATCH_NL,
  NON_GREEDY,
  PERL,
  PERL_X,
  POSIX,
  UNICODE_GROUPS,
  WAS_DOLLAR
}

import Regexp.Op._

import regex.ParserSuite.{mkCharClass, RunePredicate, dump}

//
//   Design Notes:
//
//   * UnicodeCategoriesSuite.scala is derived from ParserSuite.scala
//     and expanded in order to more extensively exercise the data tables
//     related to Unicode general categories.
//
//     The careful reader will notice that Script and Property tables
//     are not validated here. Simple_Parse in ParserSuite validates Braille
//     and touches Greek but coverage of others is spotty to non-extant.
//     Writing this Suite revealed that the current nativelib.regex
//     implementation does not handle Properties at all!  One day, both
//     Scripts and Properties should be validated in Suites of their own.
//
//   * Tests and their supporting data are enclosed in blocks so that
//     the associated memory can be released and garbage collected after
//     each test.  The Scala Native test runner currently dies silently
//     and mysteriously when handling large amounts of memory.
//
//   * Tests are meant be capable of being executed independent of what has
//     come before. This means some extra time is spent generating tables but
//     makes the Suite more robust to change.
//

// The trait Paragon and its derivatives are used to hold what
// biologists call a "specimen type" or craftsman a "benchmark".
// That is an ideal case.
//
// In all but two cases, a Unicode code point translates into a CharClass.
// In those two case, the result is a literal. The Paragon trait
// encapsulates that complexity.

sealed trait Paragon {
  def value(): String
}

private class RunePredicateParagon(f: RunePredicate) extends Paragon {
  lazy val _value     = mkCharClass(f)
  def value(): String = _value
}

private class StringParagon(_value: String) extends Paragon {
  def value(): String = _value
}

private object Paragon {
  def apply(f: RunePredicate): Paragon = new RunePredicateParagon(f)
  def apply(s: String): Paragon        = new StringParagon(s)
}

object UnicodeCategoriesSuite extends tests.Suite {

  // This testParseDump method is derived from testParseDump() in
  // ParserSuite.scala and modified to the requirements of this suite.
  // For consistency and to reduce code duplication it uses ParserSuite
  // methods where feasible.

  private def testParseDump(
      tests: Array[Tuple2[String, Paragon]],
      flags: Int
  ): Unit = {
    for (test <- tests) {
      val (pattern, paragon) = test
      try {
        val re = Parser.parse(pattern, flags)
        val d  = dump(re)
        if (!(paragon.value == d)) {
          fail(
            String.format(
              "parse/dump of " + pattern + " expected " +
                paragon.value + ", got " + d
            )
          )
        }
      } catch {
        case e: PatternSyntaxException =>
          throw new RuntimeException("Parsing failed: " + pattern, e)
      }
    }
  }

  // BEWARE: Prior art retained, but INCONSISTENT with JVM default
  //         These are perl flags and set DOT_ALL (. matches \n) true.

  private val TEST_FLAGS = ParserSuite.TEST_FLAGS

  test("Validate code point counts-by-category versus JVM Java 8 actual") {

    // This test started life checking that the number of re2s code points
    // matched the numbers given in Unicode 6.3 DerivedGeneralCategory.txt.
    //
    // Once that was true, warts were added to the re2s data tables
    // to make them follow JVM 8 practice. This test then morphed
    // to check that the number of code points in each category
    // for re2s and JVM matched.
    //
    // Taken together, the two steps were intended to, and did, discover
    // cases where a code point was totally missing in both re2s and JVM
    // but present in Unicode or visa versa.

    // This tests checks  gross numbers in each category.  Test below ensure
    // that the actual code points in each category match.

    // This section depends upon some knowledge of the scala JVM values and
    // assumes the Scala Native Character class follows them.
    // In particular, that the values are contiguous.

    val maxType = 30 // Character.FINAL_QUOTE_PUNCTUATION in JVM

// Format: off

    // The first column in the table below is the number of elements
    // in each category as actually implemented in JVM Java 8.
    // That Java version is the current reference for ScalaNative.
    //
    // The second column is the number for the category given in Unicode 6.3
    // DerivedGeneralCategory.txt. These numbers are retained as a
    // reference for future developers. It identifies & highlights differences.
    //
    // The third column gives the Java Enum name.
    //
    // The trailing comment gives the Unicode table identifier/short_name
    // and, on 0 boundaries, the index number. The latter saves time &
    // frustration when developing & debugging.

    // Both the Java Character class and Unicode.org present categories
    // in the order below.

    // Be careful with index 17. For some reason,  it is skipped by JVM.

      val expected = Array(
          //        JVM  Unicode
	  Tuple3(864414, 864409, "UNASSIGNED"),		       // Cn, index 0
	  Tuple3(  1441,   1441, "UPPERCASE_LETTER"),	       // Lu
	  Tuple3(  1751,   1751, "LOWERCASE_LETTER"),	       // Ll
	  Tuple3(    31,     31, "TITLECASE_LETTER"),	       // Lt
	  Tuple3(   237,    237, "MODIFIER_LETTER"),	       // Lm
	  Tuple3( 97553,  97553, "OTHER_LETTER"),	       // Lo
	  Tuple3(  1280,   1281, "NON_SPACING_MARK"),	       // Mn
	  Tuple3(    12,     12, "ENCLOSING_MARK"),	       // Me
	  Tuple3(   353,    352, "COMBINING_SPACING_MARK"),    // Mc
	  Tuple3(   460,    460, "DECIMAL_DIGIT_NUMBER"),      // Nd
	  Tuple3(   224,    224, "LETTER_NUMBER"),	       // Nl, index 10
	  Tuple3(   464,    464, "OTHER_NUMBER"),	       // No
	  Tuple3(    18,     17, "SPACE_SEPARATOR"),	       // Zs
	  Tuple3(     1,      1, "LINE_SEPARATOR"),	       // Zl
	  Tuple3(     1,      1, "PARAGRAPH_SEPARATOR"),       // Zp
	  Tuple3(    65,     65, "CONTROL"),		       // Cc
	  Tuple3(   139,    145, "FORMAT"),		       // Cf
	  Tuple3(     0,      0, "HACK, NOT USED IN JVM"),    // BEWARE! UNUSED
	  Tuple3(137468, 137468, "PRIVATE_USE"),	       // Co
	  Tuple3(  2048,   2048, "SURROGATE"),		       // Cs
	  Tuple3(    23,     23, "DASH_PUNCTUATION"),	       // Pd, index 20
	  Tuple3(    72,     74, "START_PUNCTUATION"),	       // Ps
	  Tuple3(    71,     73, "END_PUNCTUATION"),	       // Pe
	  Tuple3(    10,     10, "CONNECTOR_PUNCTUATION"),     // Pc
	  Tuple3(   434,    434, "OTHER_PUNCTUATION"),	       // Po
	  Tuple3(   952,    948, "MATH_SYMBOL"),	       // Sm
	  Tuple3(    49,     49, "CURRENCY_SYMBOL"),	       // Sc
	  Tuple3(   115,    115, "MODIFIER_SYMBOL"),	       // Sk
	  Tuple3(  4404,   4404, "OTHER_SYMBOL"),	       // So
	  Tuple3(    12,     12, "INITIAL_QUOTE_PUNCTUATION"), // Pi
	  Tuple3(    10,     10, "FINAL_QUOTE_PUNCTUATION")    // Pf, index 30
	)
// Format: on

    val counts = new Array[Int](maxType + 1) // all 0 guaranteed by scala spec.
    assert(expected.length == (maxType + 1), s"a1")
    assert(counts.length == expected.length, s"a2")

    for (r <- 0 to Unicode.MAX_RUNE) {
      val t = Character.getType(r)

      assert(
        (t >= 0) && (t <= maxType),
        s"Character.getType(${r}) result: ${t} not in range" +
          s" 0 to ${maxType}"
      )

      counts(t) += 1
    }

    var success = true

    for (i <- 0 to maxType) {
      val count                            = counts(i)
      val (expectedJVMCount, _, tableName) = expected(i)

      // Do not fail at first error, print out all failing
      // cases. Failures tend to be related. A codepoint in wrong bin
      // causes two errors: one where it is and one where it should be.

      if (count != expectedJVMCount) {
        success = false
        printf(
          s"${tableName} count: ${count} != " +
            s"expected: ${expectedJVMCount}\n"
        )
      }
    }

    assert(success, s"Test failed, see messages")
  }

  test("Validate category tables before using them; slow! (deca-seconds):") {
    // Empty but provides visual header for group of Unicode Tables tests.
  }

  // Tests, data, & predicates are enclosed in a block for each Category
  // so that the large amounts of memory used are released when
  // the scope of the block is exited.

  { // Tests, data, & predicates for Unicode General Category L (Letter).

    val IS_LETTER = new RunePredicate() {
      override def applies(r: Int): Boolean =
        Character.isLetter(r)
    }

    val IS_LETTER_LOWER = new RunePredicate() {
      override def applies(r: Int): Boolean =
        Character.getType(r) == Character.LOWERCASE_LETTER
    }

    val IS_LETTER_CASEBLIND = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        val t = Character.getType(r)

        (t == Character.LOWERCASE_LETTER) ||
        (t == Character.UPPERCASE_LETTER) ||
        (t == Character.TITLECASE_LETTER)
      }
    }

    val IS_LETTER_MODIFIER = new RunePredicate() {
      override def applies(r: Int): Boolean =
        Character.getType(r) == Character.MODIFIER_LETTER
    }

    val IS_LETTER_OTHER = new RunePredicate() {
      override def applies(r: Int): Boolean =
        Character.getType(r) == Character.OTHER_LETTER
    }

    val IS_LETTER_TITLE = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.TITLECASE_LETTER
      }
    }

    val IS_LETTER_UPPER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.UPPERCASE_LETTER
      }
    }

    // Derived from Unicode 6.3 CaseFolding.txt.
    //
    // The rightmost, commented, column is the Titlecase codepoint.
    // The leftmost column is its lowercase fold.
    // If a middle column exists it is the codepoint folded to uppercase.
    // Otherwise, the uppercase for the codepoint is the codepoint itself.

    // format: off

   val LETTER_TITLE_CASE_FOLDS = TreeSet(0x1c4, 0x1c6, // 1c5
						0x1c7, 0x1c9, // 1c8
						0x1ca, 0x1cc, // 1cb
						0x1f1, 0x1f3, // 1f2
						0x1f80,	     // 1f88
						0x1f81,	     // 1f89
						0x1f82,	     // 1f8a
						0x1f83,	     // 1f8b
						0x1f84,	     // 1f8c
						0x1f85,	     // 1f8d
						0x1f86,	     // 1f8e
						0x1f87,	     // 1f8f
						0x1f90,	     // 1f98
						0x1f91,	     // 1f99
						0x1f92,	     // 1f9a
						0x1f93,	     // 1f9b
						0x1f94,	     // 1f9c
						0x1f95,	     // 1f9d
						0x1f96,	     // 1f9e
						0x1f97,	     // 1f9f
						0x1fa0,	     // 1fa8
						0x1fa1,	     // 1fa9
						0x1fa2,	     // 1faa
						0x1fa3,	     // 1fab
						0x1fa4,	     // 1fac
						0x1fa5,	     // 1fad
						0x1fa6,	     // 1fae
						0x1fa7,	     // 1faf
						0x1fb3,	     // 1fbc
						0x1fc3,	     // 1fcc
						0x1ff3	     // 1ffc
					)
  // format: on

    val IS_LETTER_TITLE_FOLD = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        (Character.getType(r) == Character.TITLECASE_LETTER) ||
        (LETTER_TITLE_CASE_FOLDS.contains(r))
      }
    }

    val UNICODE_LETTER_TABLE_TESTS = Array(
      // Unicode tends to use static date. j.l.Character tends to use
      // dynamically generated data.  The goal is to have Unicode &
      // Character use the same underlying code, and enjoy all the
      // goodness that brings.
      //
      // For now,run sanity checks that the two agree on data before
      // running the extensive & expensive tests after this one.

      // The full L letter table is now dynamically generated from its
      // constitents. Check those parts before using them to create
      // the letter table. Then check the letter table itsef.
      // All nice and cozy before a long testing Nantucket sleigh ride.
      Tuple2("\\p{Ll}", Paragon(IS_LETTER_LOWER)),
      Tuple2("[\\p{Ll}]", Paragon(IS_LETTER_LOWER)),
      Tuple2("\\p{Lm}", Paragon(IS_LETTER_MODIFIER)),
      Tuple2("\\p{Lo}", Paragon(IS_LETTER_OTHER)),
      Tuple2("\\p{Lt}", Paragon(IS_LETTER_TITLE)),
      Tuple2("[\\p{Lt}]", Paragon(IS_LETTER_TITLE)),
      Tuple2("\\p{Lu}", Paragon(IS_LETTER_UPPER)),
      Tuple2("[\\p{Lu}]", Paragon(IS_LETTER_UPPER)),
      Tuple2("\\p{L}", Paragon(IS_LETTER)),
      // Look for inconsistencies in case blind tables.
      // Some of those tables are generated. The case blind
      // tests are pretty good at catching bugs in not only their tables
      // but also in the underlying generating tables.
      Tuple2("(?i)[\\p{Ll}]", Paragon(IS_LETTER_CASEBLIND)),
      Tuple2("(?i)[\\p{Lt}]", Paragon(IS_LETTER_TITLE_FOLD)),
      Tuple2("(?i)[\\p{Lu}]", Paragon(IS_LETTER_CASEBLIND))
    )

    test("  - Letter (L*)") {
      testParseDump(UNICODE_LETTER_TABLE_TESTS, TEST_FLAGS)
    }
  }

  { // Tests, data, & predicates for Unicode General Category M (Mark).

    val IS_MARK = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        val t = Character.getType(r)
        (t == Character.COMBINING_SPACING_MARK) ||
        (t == Character.ENCLOSING_MARK) ||
        (t == Character.NON_SPACING_MARK)
      }
    }

    val IS_MARK_COMBINING_SPACING = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.COMBINING_SPACING_MARK
      }
    }

    val IS_MARK_ENCLOSING = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.ENCLOSING_MARK
      }
    }

    val IS_MARK_NON_SPACING = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.NON_SPACING_MARK
      }
    }

    val UNICODE_MARK_TABLE_TESTS = Array(
      Tuple2("\\p{Mc}", Paragon(IS_MARK_COMBINING_SPACING)),
      Tuple2("\\p{Me}", Paragon(IS_MARK_ENCLOSING)),
      Tuple2("\\p{Mn}", Paragon(IS_MARK_NON_SPACING)),
      Tuple2("\\p{M}", Paragon(IS_MARK))
    )

    test("  - Mark (M*)") {
      testParseDump(UNICODE_MARK_TABLE_TESTS, TEST_FLAGS)
    }

  }

  { // Tests, data, & predicates for Unicode General Category N (Number).

    val IS_NUMBER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        val t = Character.getType(r)

        (t == Character.DECIMAL_DIGIT_NUMBER) ||
        (t == Character.LETTER_NUMBER) ||
        (t == Character.OTHER_NUMBER)
      }
    }

    val IS_NUMBER_DECIMAL_DIGIT = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.DECIMAL_DIGIT_NUMBER
      }
    }

    val IS_NUMBER_LETTER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.LETTER_NUMBER
      }
    }

    val IS_NUMBER_OTHER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.OTHER_NUMBER
      }
    }

    val UNICODE_NUMBER_TABLE_TESTS = Array(
      Tuple2("\\p{Nd}", Paragon(IS_NUMBER_DECIMAL_DIGIT)),
      Tuple2("\\p{Nl}", Paragon(IS_NUMBER_LETTER)),
      Tuple2("\\p{No}", Paragon(IS_NUMBER_OTHER)),
      Tuple2("\\p{N}", Paragon(IS_NUMBER))
    )

    test("  - Number (N*)") {
      testParseDump(UNICODE_NUMBER_TABLE_TESTS, TEST_FLAGS)
    }
  }

  { // Predicates for Unicode General Category C (Other) & constituents.

    val IS_OTHER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        val t = Character.getType(r)

        (t == Character.CONTROL) ||
        (t == Character.FORMAT) ||
        (t == Character.UNASSIGNED) ||
        (t == Character.PRIVATE_USE) ||
        (t == Character.SURROGATE)
      }
    }

    val IS_OTHER_CONTROL = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.CONTROL
      }
    }

    val IS_OTHER_FORMAT = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.FORMAT
      }
    }

    val IS_OTHER_NOT_ASSIGNED = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.UNASSIGNED
      }
    }

    val IS_OTHER_PRIVATE_USE = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.PRIVATE_USE
      }
    }

    val IS_OTHER_SURROGATE = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.SURROGATE
      }
    }

    val UNICODE_OTHER_TABLE_TESTS = Array(
      Tuple2("\\p{Cc}", Paragon(IS_OTHER_CONTROL)),
      Tuple2("\\p{Cf}", Paragon(IS_OTHER_FORMAT)),
      Tuple2("\\p{Cn}", Paragon(IS_OTHER_NOT_ASSIGNED)),
      Tuple2("\\p{Co}", Paragon(IS_OTHER_PRIVATE_USE)),
      Tuple2("\\p{Cs}", Paragon(IS_OTHER_SURROGATE)),
      Tuple2("\\p{C}", Paragon(IS_OTHER))
    )

    test(s"  - Other (C*)") {
      testParseDump(UNICODE_OTHER_TABLE_TESTS, TEST_FLAGS)
    }
  }

  { // Tests, data, & predicates for Unicode General Category P (Punctuation).

    val IS_PUNCTUATION = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        val t = Character.getType(r)

        (t == Character.CONNECTOR_PUNCTUATION) ||
        (t == Character.DASH_PUNCTUATION) ||
        (t == Character.END_PUNCTUATION) ||
        (t == Character.FINAL_QUOTE_PUNCTUATION) ||
        (t == Character.INITIAL_QUOTE_PUNCTUATION) ||
        (t == Character.OTHER_PUNCTUATION) ||
        (t == Character.START_PUNCTUATION)
      }
    }

    val IS_PUNCTUATION_CONNECTOR = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.CONNECTOR_PUNCTUATION
      }
    }

    val IS_PUNCTUATION_DASH = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.DASH_PUNCTUATION
      }
    }

    val IS_PUNCTUATION_END = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.END_PUNCTUATION
      }
    }

    val IS_PUNCTUATION_FINAL_QUOTE = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.FINAL_QUOTE_PUNCTUATION
      }
    }

    val IS_PUNCTUATION_INITIAL_QUOTE = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.INITIAL_QUOTE_PUNCTUATION
      }
    }

    val IS_PUNCTUATION_OTHER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.OTHER_PUNCTUATION
      }
    }

    val IS_PUNCTUATION_START = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.START_PUNCTUATION
      }
    }

    val UNICODE_PUNCTUATION_TABLE_TESTS = Array(
      Tuple2("\\p{Pc}", Paragon(IS_PUNCTUATION_CONNECTOR)),
      Tuple2("\\p{Pd}", Paragon(IS_PUNCTUATION_DASH)),
      Tuple2("\\p{Pe}", Paragon(IS_PUNCTUATION_END)),
      Tuple2("\\p{Pf}", Paragon(IS_PUNCTUATION_FINAL_QUOTE)),
      Tuple2("\\p{Pi}", Paragon(IS_PUNCTUATION_INITIAL_QUOTE)),
      Tuple2("\\p{Po}", Paragon(IS_PUNCTUATION_OTHER)),
      Tuple2("\\p{Ps}", Paragon(IS_PUNCTUATION_START)),
      Tuple2("\\p{P}", Paragon(IS_PUNCTUATION))
    )

    test("  - Punctuation (P*)") {
      testParseDump(UNICODE_PUNCTUATION_TABLE_TESTS, TEST_FLAGS)
    }
  }

  { // Tests, data, & predicates for Unicode General Category S (Symbol).

    val IS_SYMBOL = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        val t = Character.getType(r)
        (t == Character.CURRENCY_SYMBOL) ||
        (t == Character.MODIFIER_SYMBOL) ||
        (t == Character.MATH_SYMBOL) ||
        (t == Character.OTHER_SYMBOL)
      }
    }

    val IS_SYMBOL_CURRENCY = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.CURRENCY_SYMBOL
      }
    }

    val IS_SYMBOL_MODIFIER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.MODIFIER_SYMBOL
      }
    }

    val IS_SYMBOL_MATH = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.MATH_SYMBOL
      }
    }

    val IS_SYMBOL_OTHER = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.OTHER_SYMBOL
      }
    }

    val UNICODE_SYMBOL_TABLE_TESTS = Array(
      Tuple2("\\p{Sc}", Paragon(IS_SYMBOL_CURRENCY)),
      Tuple2("\\p{Sk}", Paragon(IS_SYMBOL_MODIFIER)),
      Tuple2("\\p{Sm}", Paragon(IS_SYMBOL_MATH)),
      Tuple2("\\p{So}", Paragon(IS_SYMBOL_OTHER)),
      Tuple2("\\p{S}", Paragon(IS_SYMBOL))
    )

    test("  - Symbol (S*)") {
      testParseDump(UNICODE_SYMBOL_TABLE_TESTS, TEST_FLAGS)
    }
  }

  { // Tests, data, & predicates for Unicode General Category Z (Separator).

    val IS_SEPARATOR = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        (r == '\u2028') || (r == '\u2029') ||
        Character.getType(r) == Character.SPACE_SEPARATOR
      }
    }

    // No IS_SEPARATOR_LINE or IS_SEPARATOR_PARAGRAPH here, where consistency
    // would lead one to expect them. For details, ee comment in
    // UNICODE_SEPARATOR_TABLE_TEST below.

    val IS_SEPARATOR_SPACE = new RunePredicate() {
      override def applies(r: Int): Boolean = {
        Character.getType(r) == Character.SPACE_SEPARATOR
      }
    }

    val UNICODE_SEPARATOR_TABLE_TESTS = Array(
      // "\\p{Zl}" & "\\p{Zp}" return character classes of size 1, which
      // gets simplified to a literal.
      // As of UC 12.1, hence in 6.3, these are the only two codepoints
      // which behave that way.
      Tuple2("\\p{Zl}", Paragon("lit{\u2028}")),
      Tuple2("\\p{Zp}", Paragon("lit{\u2029}")),
      Tuple2("\\p{Zs}", Paragon(IS_SEPARATOR_SPACE)),
      Tuple2("\\p{Z}", Paragon(IS_SEPARATOR))
    )

    test("  - Separator (Z*)") {
      testParseDump(UNICODE_SEPARATOR_TABLE_TESTS, TEST_FLAGS)
    }
  }

}
