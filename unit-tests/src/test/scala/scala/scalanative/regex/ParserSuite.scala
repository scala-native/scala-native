package scala.scalanative
package regex

import java.util
import java.util.regex.PatternSyntaxException

import ScalaTestCompat.fail

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

object ParserSuite extends tests.Suite {

  private trait RunePredicate {
    def applies(rune: Int): Boolean
  }

  private val IS_LOWER = new RunePredicate() {
    override def applies(r: Int): Boolean = Unicode.isLower(r)
  }

  private val IS_LOWER_FOLD = new RunePredicate() {
    override def applies(r: Int): Boolean = {
      if (Unicode.isLower(r)) return true
      var c = Unicode.simpleFold(r)
      while (c != r) {
        if (Unicode.isLower(c)) return true

        c = Unicode.simpleFold(c)
      }
      false
    }
  }

  private val IS_TITLE = new RunePredicate() {
    override def applies(r: Int): Boolean = Unicode.isTitle(r)
  }

  private val IS_UPPER = new RunePredicate() {
    override def applies(r: Int): Boolean = Unicode.isUpper(r)
  }

  private val IS_UPPER_FOLD = new RunePredicate() {
    override def applies(r: Int): Boolean = {
      if (Unicode.isUpper(r)) return true
      var c = Unicode.simpleFold(r)
      while (c != r) {
        if (Unicode.isUpper(c)) return true

        c = Unicode.simpleFold(c)
      }
      false
    }
  }

  private val OP_NAMES: util.HashMap[Regexp.Op, String] = {
    val temp = new util.HashMap[Regexp.Op, String]()
    temp.put(Regexp.Op.NO_MATCH, "no")
    temp.put(Regexp.Op.EMPTY_MATCH, "emp")
    temp.put(Regexp.Op.LITERAL, "lit")
    temp.put(Regexp.Op.CHAR_CLASS, "cc")
    temp.put(Regexp.Op.ANY_CHAR_NOT_NL, "dnl")
    temp.put(Regexp.Op.ANY_CHAR, "dot")
    temp.put(Regexp.Op.BEGIN_LINE, "bol")
    temp.put(Regexp.Op.END_LINE, "eol")
    temp.put(Regexp.Op.BEGIN_TEXT, "bot")
    temp.put(Regexp.Op.END_TEXT, "eot")
    temp.put(Regexp.Op.WORD_BOUNDARY, "wb")
    temp.put(Regexp.Op.NO_WORD_BOUNDARY, "nwb")
    temp.put(Regexp.Op.CAPTURE, "cap")
    temp.put(Regexp.Op.STAR, "star")
    temp.put(Regexp.Op.PLUS, "plus")
    temp.put(Regexp.Op.QUEST, "que")
    temp.put(Regexp.Op.REPEAT, "rep")
    temp.put(Regexp.Op.CONCAT, "cat")
    temp.put(Regexp.Op.ALTERNATE, "alt")
    temp
  }

  private val TEST_FLAGS = MATCH_NL | PERL_X | UNICODE_GROUPS

  private val PARSE_TESTS = Array(
    // Base cases
    Array("a", "lit{a}"),
    Array("a.", "cat{lit{a}dot{}}"),
    Array("a.b", "cat{lit{a}dot{}lit{b}}"),
    Array("ab", "str{ab}"),
    Array("a.b.c", "cat{lit{a}dot{}lit{b}dot{}lit{c}}"),
    Array("abc", "str{abc}"),
    Array("a|^", "alt{lit{a}bol{}}"),
    Array("a|b", "cc{0x61-0x62}"),
    Array("(a)", "cap{lit{a}}"),
    Array("(a)|b", "alt{cap{lit{a}}lit{b}}"),
    Array("a*", "star{lit{a}}"),
    Array("a+", "plus{lit{a}}"),
    Array("a?", "que{lit{a}}"),
    Array("a{2}", "rep{2,2 lit{a}}"),
    Array("a{2,3}", "rep{2,3 lit{a}}"),
    Array("a{2,}", "rep{2,-1 lit{a}}"),
    Array("a*?", "nstar{lit{a}}"),
    Array("a+?", "nplus{lit{a}}"),
    Array("a??", "nque{lit{a}}"),
    Array("a{2}?", "nrep{2,2 lit{a}}"),
    Array("a{2,3}?", "nrep{2,3 lit{a}}"),
    Array("a{2,}?", "nrep{2,-1 lit{a}}"),
    // Malformed { } are treated as literals.
    Array("x{1001", "str{x{1001}"),
    Array("x{9876543210", "str{x{9876543210}"),
    Array("x{9876543210,", "str{x{9876543210,}"),
    Array("x{2,1", "str{x{2,1}"),
    Array("x{1,9876543210", "str{x{1,9876543210}"),
    Array("", "emp{}"),
    Array("|", "emp{}"),
    // alt{emp{}emp{}} but got factored
    Array("|x|", "alt{emp{}lit{x}emp{}}"),
    Array(".", "dot{}"),
    Array("^", "bol{}"),
    Array("$", "eol{}"),
    Array("\\|", "lit{|}"),
    Array("\\(", "lit{(}"),
    Array("\\)", "lit{)}"),
    Array("\\*", "lit{*}"),
    Array("\\+", "lit{+}"),
    Array("\\?", "lit{?}"),
    Array("{", "lit{{}"),
    Array("}", "lit{}}"),
    Array("\\.", "lit{.}"),
    Array("\\^", "lit{^}"),
    Array("\\$", "lit{$}"),
    Array("\\\\", "lit{\\}"),
    Array("[ace]", "cc{0x61 0x63 0x65}"),
    Array("[abc]", "cc{0x61-0x63}"),
    Array("[a-z]", "cc{0x61-0x7a}"),
    Array("[a]", "lit{a}"),
    Array("\\-", "lit{-}"),
    Array("-", "lit{-}"),
    Array("\\_", "lit{_}"),
    Array("abc", "str{abc}"),
    Array("abc|def", "alt{str{abc}str{def}}"),
    Array("abc|def|ghi", "alt{str{abc}str{def}str{ghi}}"),
    // Posix and Perl extensions
    Array("\\p{Lower}", "cc{0x61-0x7a}"),
    Array("[a-z]", "cc{0x61-0x7a}"),
    Array("(?i)\\p{Lower}", "cc{0x41-0x5a 0x61-0x7a 0x17f 0x212a}"),
    Array("(?i)[a-z]", "cc{0x41-0x5a 0x61-0x7a 0x17f 0x212a}"),
    Array("\\d", "cc{0x30-0x39}"),
    Array("\\D", "cc{0x0-0x2f 0x3a-0x10ffff}"),
    Array("\\s", "cc{0x9-0xa 0xc-0xd 0x20}"),
    Array("\\S", "cc{0x0-0x8 0xb 0xe-0x1f 0x21-0x10ffff}"),
    Array("\\w", "cc{0x30-0x39 0x41-0x5a 0x5f 0x61-0x7a}"),
    Array("\\W", "cc{0x0-0x2f 0x3a-0x40 0x5b-0x5e 0x60 0x7b-0x10ffff}"),
    Array("(?i)\\w", "cc{0x30-0x39 0x41-0x5a 0x5f 0x61-0x7a 0x17f 0x212a}"),
    Array(
      "(?i)\\W",
      "cc{0x0-0x2f 0x3a-0x40 0x5b-0x5e 0x60 0x7b-0x17e 0x180-0x2129 0x212b-0x10ffff}"),
    Array("[^\\\\]", "cc{0x0-0x5b 0x5d-0x10ffff}"),
    //  { "\\C", "byte{}" },  // probably never
    // Unicode, negatives, and a double negative.
    Array("\\p{Braille}", "cc{0x2800-0x28ff}"),
    Array("\\P{Braille}", "cc{0x0-0x27ff 0x2900-0x10ffff}"),
    Array("\\p{^Braille}", "cc{0x0-0x27ff 0x2900-0x10ffff}"),
    Array("\\P{^Braille}", "cc{0x2800-0x28ff}"),
    Array(
      "\\pZ",
      "cc{0x20 0xa0 0x1680 0x180e 0x2000-0x200a 0x2028-0x2029 0x202f 0x205f 0x3000}"),
    Array("[\\p{Braille}]", "cc{0x2800-0x28ff}"),
    Array("[\\P{Braille}]", "cc{0x0-0x27ff 0x2900-0x10ffff}"),
    Array("[\\p{^Braille}]", "cc{0x0-0x27ff 0x2900-0x10ffff}"),
    Array("[\\P{^Braille}]", "cc{0x2800-0x28ff}"),
    Array(
      "[\\pZ]",
      "cc{0x20 0xa0 0x1680 0x180e 0x2000-0x200a 0x2028-0x2029 0x202f 0x205f 0x3000}"),
    Array("\\p{Ll}", mkCharClass(IS_LOWER)),
    Array("[\\p{Ll}]", mkCharClass(IS_LOWER)),
    Array("(?i)[\\p{Ll}]", mkCharClass(IS_LOWER_FOLD)),
    Array("\\p{Lt}", mkCharClass(IS_TITLE)),
    Array("[\\p{Lt}]", mkCharClass(IS_TITLE)),
    Array("\\p{Lu}", mkCharClass(IS_UPPER)),
    Array("[\\p{Lu}]", mkCharClass(IS_UPPER)),
    Array("(?i)[\\p{Lu}]", mkCharClass(IS_UPPER_FOLD)),
    Array("\\p{Any}", "dot{}"),
    Array("\\p{^Any}", "cc{}"),
    // Hex, octal.
    Array("[\\012-\\234]\\141", "cat{cc{0xa-0x9c}lit{a}}"),
    Array("[\\x{41}-\\x7a]\\x61", "cat{cc{0x41-0x7a}lit{a}}"),
    // More interesting regular expressions.
    Array("a{,2}", "str{a{,2}}"),
    Array("\\.\\^\\$\\\\", "str{.^$\\}"),
    Array("[a-zABC]", "cc{0x41-0x43 0x61-0x7a}"),
    Array("[^a]", "cc{0x0-0x60 0x62-0x10ffff}"),
    Array("[α-ε☺]", "cc{0x3b1-0x3b5 0x263a}"),
    // utf-8
    Array("a*{", "cat{star{lit{a}}lit{{}}"),
    // Test precedences
    Array("(?:ab)*", "star{str{ab}}"),
    Array("(ab)*", "star{cap{str{ab}}}"),
    Array("ab|cd", "alt{str{ab}str{cd}}"),
    Array("a(b|c)d", "cat{lit{a}cap{cc{0x62-0x63}}lit{d}}"),
    // Test flattening.
    Array("(?:a)", "lit{a}"),
    Array("(?:ab)(?:cd)", "str{abcd}"),
    Array("(?:a+b+)(?:c+d+)",
          "cat{plus{lit{a}}plus{lit{b}}plus{lit{c}}plus{lit{d}}}"),
    Array("(?:a+|b+)|(?:c+|d+)",
          "alt{plus{lit{a}}plus{lit{b}}plus{lit{c}}plus{lit{d}}}"),
    Array("(?:a|b)|(?:c|d)", "cc{0x61-0x64}"),
    Array("a|.", "dot{}"),
    Array(".|a", "dot{}"),
    Array("(?:[abc]|A|Z|hello|world)",
          "alt{cc{0x41 0x5a 0x61-0x63}str{hello}str{world}}"),
    Array("(?:[abc]|A|Z)", "cc{0x41 0x5a 0x61-0x63}"),
    // Test Perl quoted literals
    Array("\\Q+|*?{[\\E", "str{+|*?{[}"),
    Array("\\Q+\\E+", "plus{lit{+}}"),
    Array("\\Q\\\\E", "lit{\\}"),
    Array("\\Q\\\\\\E", "str{\\\\}"),
    // Test Perl \A and \z
    Array("(?m)^", "bol{}"),
    Array("(?m)$", "eol{}"),
    Array("(?-m)^", "bot{}"),
    Array("(?-m)$", "eot{}"),
    Array("(?m)\\A", "bot{}"),
    Array("(?m)\\z", "eot{\\z}"),
    Array("(?-m)\\A", "bot{}"),
    Array("(?-m)\\z", "eot{\\z}"),
    // Test named captures
    Array("(?<name>a)", "cap{name:lit{a}}"),
    // Case-folded literals
    Array("[Aa]", "litfold{A}"),
    Array("[\\x{100}\\x{101}]", "litfold{Ā}"),
    Array("[Δδ]", "litfold{Δ}"),
    // Strings
    Array("abcde", "str{abcde}"),
    Array("[Aa][Bb]cd", "cat{strfold{AB}str{cd}}"),
    // Factoring.
    Array(
      "abc|abd|aef|bcx|bcy",
      "alt{cat{lit{a}alt{cat{lit{b}cc{0x63-0x64}}str{ef}}}cat{str{bc}cc{0x78-0x79}}}"),
//    Array("ax+y|ax+z|ay+w", "cat{lit{a}alt{cat{plus{lit{x}}cc{0x79-0x7a}}cat{plus{lit{y}}lit{w}}}}"), // TODO: fails because of equals  if (first != null && first.equals(ifirst)) {
    // Bug fixes.
    Array("(?:.)", "dot{}"),
    Array("(?:x|(?:xa))", "cat{lit{x}alt{emp{}lit{a}}}"),
//    Array("(?:.|(?:.a))", "cat{dot{}alt{emp{}lit{a}}}"),
    Array("(?:A(?:A|a))", "cat{lit{A}litfold{A}}"),
    Array("(?:A|a)", "litfold{A}"),
    Array("A|(?:A|a)", "litfold{A}"),
    Array("(?s).", "dot{}"),
    Array("(?-s).", "dnl{}"),
    Array("(?:(?:^).)", "cat{bol{}dot{}}"),
    Array("(?-s)(?:(?:^).)", "cat{bol{}dnl{}}"),
    Array("[\\x00-\\x{10FFFF}]", "dot{}"),
    Array("[^\\x00-\\x{10FFFF}]", "cc{}"),
    Array("(?:[a][a-])", "cat{lit{a}cc{0x2d 0x61}}"),
    // RE2 prefix_tests
    Array("abc|abd", "cat{str{ab}cc{0x63-0x64}}"),
    Array("a(?:b)c|abd", "cat{str{ab}cc{0x63-0x64}}"),
    Array(
      "abc|abd|aef|bcx|bcy",
      "alt{cat{lit{a}alt{cat{lit{b}cc{0x63-0x64}}str{ef}}}" + "cat{str{bc}cc{0x78-0x79}}}"),
    Array("abc|x|abd", "alt{str{abc}lit{x}str{abd}}"),
    Array("(?i)abc|ABD", "cat{strfold{AB}cc{0x43-0x44 0x63-0x64}}")
//    Array("[ab]c|[ab]d", "cat{cc{0x61-0x62}cc{0x63-0x64}}")
//    Array("(?:xx|yy)c|(?:xx|yy)d", "cat{alt{str{xx}str{yy}}cc{0x63-0x64}}"),
//    Array("x{2}|x{2}[0-9]", "cat{rep{2,2 lit{x}}alt{emp{}cc{0x30-0x39}}}"),
//    Array("x{2}y|x{2}[0-9]y", "cat{rep{2,2 lit{x}}alt{lit{y}cat{cc{0x30-0x39}lit{y}}}}")
  )

  // TODO(adonovan): add some tests for:
  // - ending a regexp with "\\"
  // - Java UTF-16 things.

  test("ParseSimple") {
    testParseDump(PARSE_TESTS, TEST_FLAGS)
  }

  private val FOLDCASE_TESTS = Array(
    Array("AbCdE", "strfold{ABCDE}"),
    Array("[Aa]", "litfold{A}"),
    // 0x17F is an old English long s (looks like an f) and folds to s.
    Array("a", "litfold{A}"),
    // 0x212A is the Kelvin symbol and folds to k.
    Array("A[F-g]", "cat{litfold{A}cc{0x41-0x7a 0x17f 0x212a}}") // [Aa][A-z...]
  )

  test("ParseFoldCase") {
    testParseDump(FOLDCASE_TESTS, FOLD_CASE)
  }

  private val LITERAL_TESTS = Array(
    Array("(|)^$.[*+?]{5,10},\\", "str{(|)^$.[*+?]{5,10},\\}"))

  test("ParseLiteral") {
    testParseDump(LITERAL_TESTS, LITERAL)
  }

  private val MATCHNL_TESTS = Array(Array(".", "dot{}"),
                                    Array("\n", "lit{\n}"),
                                    Array("[^a]", "cc{0x0-0x60 0x62-0x10ffff}"),
                                    Array("[a\\n]", "cc{0xa 0x61}"))

  test("ParseMatchNL") {
    testParseDump(MATCHNL_TESTS, MATCH_NL)
  }

  private val NOMATCHNL_TESTS = Array(
    Array(".", "dnl{}"),
    Array("\n", "lit{\n}"),
    Array("[^a]", "cc{0x0-0x9 0xb-0x60 0x62-0x10ffff}"),
    Array("[a\\n]", "cc{0xa 0x61}"))

  test("ParseNoMatchNL") {
    testParseDump(NOMATCHNL_TESTS, 0)
  }

  // Test Parse -> Dump.
  private def testParseDump(tests: Array[Array[String]], flags: Int): Unit = {
    for (test <- tests) {
      try {
        val re = Parser.parse(test(0), flags)
        val d  = dump(re)
        if (!(test(1) == d)) {
          fail(
            String.format("parse/dump of " + test(0) + " expected " + test(1) +
              ", got " + d))
        }
      } catch {
        case e: PatternSyntaxException =>
          throw new RuntimeException("Parsing failed: " + test(0), e)
      }
    }
  }

  // dump prints a string representation of the regexp showing
  // the structure explicitly.
  private def dump(re: Regexp) = {
    val b = new StringBuffer()
    dumpRegexp(b, re)
    b.toString
  }

  // dumpRegexp writes an encoding of the syntax tree for the regexp |re|
  // to |b|.  It is used during testing to distinguish between parses that
  // might print the same using re's toString() method.
  private def dumpRegexp(b: StringBuffer, re: Regexp): Unit = {
    val name = OP_NAMES.get(re.op)
    if (name == null) b.append("op").append(re.op)
    else
      re.op match {
        case STAR | PLUS | QUEST | REPEAT =>
          if ((re.flags & NON_GREEDY) != 0) b.append('n')
          b.append(name)
        case LITERAL =>
          if (re.runes.length > 1) b.append("str")
          else b.append("lit")
          if ((re.flags & FOLD_CASE) != 0) {
            var break = false
            for (r <- re.runes if !break) {
              if (Unicode.simpleFold(r) != r) {
                b.append("fold")
                break = true
              }
            }
          }
        case _ =>
          b.append(name)
      }
    b.append('{')
    re.op match {
      case END_TEXT =>
        if ((re.flags & WAS_DOLLAR) == 0) b.append("\\z")
      case LITERAL =>
        for (r <- re.runes) {
          b.appendCodePoint(r)
        }
      case CONCAT | ALTERNATE =>
        for (sub <- re.subs) {
          dumpRegexp(b, sub)
        }
      case STAR | PLUS | QUEST =>
        dumpRegexp(b, re.subs(0))
      case REPEAT =>
        b.append(re.min).append(',').append(re.max).append(' ')
        dumpRegexp(b, re.subs(0))
      case CAPTURE =>
        if (re.name != null && !re.name.isEmpty) {
          b.append(re.name)
          b.append(':')
        }
        dumpRegexp(b, re.subs(0))
      case CHAR_CLASS =>
        var sep = ""
        var i   = 0
        while (i < re.runes.length) {
          b.append(sep)
          sep = " "
          val lo = re.runes(i)
          val hi = re.runes(i + 1)
          if (lo == hi) b.append("%#x".format(lo))
          else b.append("%#x-%#x".format(lo, hi))

          i += 2
        }
      case _ =>
    }
    b.append('}')
  }

  private def mkCharClass(f: RunePredicate): String = {
    val re    = new Regexp(Regexp.Op.CHAR_CLASS)
    val runes = new util.ArrayList[Integer]
    var lo    = -1
    var i     = 0
    while (i <= Unicode.MAX_RUNE) {

      if (f.applies(i)) {
        if (lo < 0) lo = i
      } else if (lo >= 0) {
        runes.add(lo)
        runes.add(i - 1)
        lo = -1
      }
      i += 1
    }
    if (lo >= 0) {
      runes.add(lo)
      runes.add(Unicode.MAX_RUNE)
    }
    re.runes = new Array[Int](runes.size)
    var j = 0
    import scala.collection.JavaConverters._
    for (i <- runes.asScala) {
      re.runes(j) = i
      j += 1
    }
    dump(re)
  }

  test("AppendRangeCollapse") { // AppendRange should collapse each of the new ranges
    // into the earlier ones (it looks back two ranges), so that
    // the slice never grows very large.
    // Note that we are not calling cleanClass.
    val cc = new CharClass
    // Add 'A', 'a', 'B', 'b', etc.
    for (i <- 'A' to 'Z') {
      cc.appendRange(i, i)
      cc.appendRange(i + 'a' - 'A', i + 'a' - 'A')
    }
    assert("AZaz" == runesToString(cc.toArray))
  }

  // Converts an array of Unicode runes to a Java UTF-16 string.
  private def runesToString(runes: Array[Int]) = {
    val out = new StringBuffer()
    for (rune <- runes) {
      out.appendCodePoint(rune)
    }
    out.toString
  }

  private val INVALID_REGEXPS = Array(
    "(",
    ")",
    "(a",
    "(a|b|",
    "(a|b",
    "[a-z",
    "([a-z)",
    "x{1001}",
    "x{9876543210}",
    "x{2,1}",
    "x{1,9876543210}", // Java string literals can't contain Invalid UTF-8.
    // "\\xff",
    // "[\xff]",
    // "[\\\xff]",
    // "\\\xff",
    "(?<name>a",
    "(?<name>",
    "(?<name",
    "(?<x y>a)",
    "(?<>a)",
    "[a-Z]",
    "(?i)[a-Z]",
    "a{100000}",
    "a{100000,}"
  )

  private val ONLY_PERL = Array("[a-b-c]",
                                "\\Qabc\\E",
                                "\\Q*+?{[\\E",
                                "\\Q\\\\E",
                                "\\Q\\\\\\E",
                                "\\Q\\\\\\\\E",
                                "\\Q\\\\\\\\\\E",
                                "(?:a)",
                                "(?<name>a)")

  private val ONLY_POSIX =
    Array("a++", "a**", "a?*", "a+*", "a{1}*", ".{1}{2}.{3}")

  test("ParseInvalidRegexps") {
    for (regexp <- INVALID_REGEXPS) {
      try {
        val re = Parser.parse(regexp, PERL)
        fail(
          "Parsing (PERL) " + regexp + " should have failed, instead got " +
            dump(re))
      } catch {
        case e: PatternSyntaxException =>
        /* ok */
      }
      try {
        val re = Parser.parse(regexp, POSIX)
        fail(
          "parsing (POSIX) " + regexp + " should have failed, instead got " +
            dump(re))
      } catch {
        case e: PatternSyntaxException =>
      }
    }
    for (regexp <- ONLY_PERL) {
      Parser.parse(regexp, PERL)
      try {
        val re = Parser.parse(regexp, POSIX)
        fail(
          "parsing (POSIX) " + regexp + " should have failed, instead got " +
            dump(re))
      } catch {
        case _: PatternSyntaxException =>
      }
    }
    for (regexp <- ONLY_POSIX) {
      try {
        val re = Parser.parse(regexp, PERL)
        fail(
          "parsing (PERL) " + regexp + " should have failed, instead got " +
            dump(re))
      } catch {
        case _: PatternSyntaxException =>
      }
      Parser.parse(regexp, POSIX)
    }
  }

  test("ToStringEquivalentParse") {
    for (tt <- PARSE_TESTS) {
      val re = Parser.parse(tt(0), TEST_FLAGS)
      val d  = dump(re)

      // (already ensured by testParseSimple)
      assert(d == tt(1), "ParseSimple failure")

      val s = re.toString
      if (!(s == tt(0))) { // If toString didn't return the original regexp,
        // it must have found one with fewer parens.
        // Unfortunately we can't check the length here, because
        // toString produces "\\{" for a literal brace,
        // but "{" is a shorter equivalent in some contexts.
        val nre = Parser.parse(s, TEST_FLAGS)
        val nd  = dump(nre)
        assert(d == nd, "parse(%s) -> %s".format(tt(0), s))
        val ns = nre.toString
        assert(s == ns, "parse(%s) -> %s".format(tt(0), s))
      }
    }
  }

}
