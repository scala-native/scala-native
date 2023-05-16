package java.util
package regex

import scalanative.{regex => snRegex}

import java.util.Arrays
import java.util.function.Predicate
import java.util.stream.Stream

// Inspired & informed by:
// https://github.com/google/re2j/blob/master/java/com/google/re2j/Pattern.java

object Pattern {

  def CANON_EQ: Int = 128
  def CASE_INSENSITIVE: Int = 2
  def COMMENTS: Int = 4
  def DOTALL: Int = 32
  def LITERAL: Int = 16
  def MULTILINE: Int = 8
  def UNICODE_CASE: Int = 64
  def UNICODE_CHARACTER_CLASS: Int = 256
  def UNIX_LINES: Int = 1

  private def validateJavaFlags(flags: Int): Unit = {

    def notSupported(flag: Int, flagName: String): Unit = {
      if ((flags & flag) == flag) {
        throw new UnsupportedOperationException(
          s"regex flag $flagName is not supported."
        )
      }
    }

    val unsupportedOptions = Array[(Int, String)](
      (CANON_EQ, "CANON_EQ(canonical equivalences)"),
      (COMMENTS, "COMMENTS"),
      (UNICODE_CASE, "UNICODE_CASE"),
      (UNICODE_CHARACTER_CLASS, "UNICODE_CHARACTER_CLASS"),
      (UNIX_LINES, "UNIX_LINES")
    )

    for (i <- 0 until unsupportedOptions.length) {
      notSupported(unsupportedOptions(i)._1, unsupportedOptions(i)._2)
    }

    // Any bit set other than given set throws.
    if ((flags & ~(CASE_INSENSITIVE | DOTALL | LITERAL | MULTILINE)) != 0) {
      throw new IllegalArgumentException(s"Unknown flag ${flags}")
    }
  }

  private def toRe2Flags(flags: Int): Int = {

    // Pass snRegex only the flags it knows about and clear the rest.
    //
    // j.u.regex LITERAL is handled in j.u.regex.Pattern#compile
    // so OK to clear that bit. java bits not known to snRegex will cause
    // it to throw, so clear them also.
    //
    // snRegex supports only CASE_INSENSITIVE | DOTALL | MULTILINE |
    //                    DISABLE_UNICODE_GROUPS))
    //
    // DISABLE_UNICODE_GROUPS causes rejectUnsupportedOptions()
    // to be thrown, so only CASE_INSENSITIVE, DOTALL, and MULTILINE are
    // left when execution reaches this point.
    //
    // The constants for these three definitely differ between j.u.regex
    // and snRegex and must be be translated.

    val optionTranslations = Array[(Int, Int)](
      (CASE_INSENSITIVE, snRegex.Pattern.CASE_INSENSITIVE),
      (DOTALL, snRegex.Pattern.DOTALL),
      (MULTILINE, snRegex.Pattern.MULTILINE)
    )

    var re2Flags = 0

    // Optimize for most common case of no flags. Skip loop if no work to do.
    if (flags != 0) {
      // use for(range) used to get loop optimized.
      for (i <- 0 until optionTranslations.length) {
        if ((flags & optionTranslations(i)._1) != 0) {
          re2Flags = re2Flags | optionTranslations(i)._2
        }
      }
    }

    re2Flags
  }

  def compile(regex: String): Pattern = compile(regex, 0)

  def compile(regex: String, flags: Int): Pattern = {

    validateJavaFlags(flags)

    val r = if ((flags & LITERAL) == 0) regex else quote(regex)

    new Pattern(r, flags)
  }

  def matches(regex: String, input: CharSequence): Boolean =
    compile(regex).matcher(input).matches()

  def quote(s: String): String = s"\\Q${s}\\E"
}

final class Pattern private[regex] (_regex: String, _flags: Int) {

  private[regex] val compiled = {
    val re2Flags = Pattern.toRe2Flags(_flags)
    snRegex.Pattern.compile(_regex, re2Flags)
  }

  def asPredicate(): Predicate[String] = {
    new Predicate[String] {
      override def test(t: String): Boolean =
        matcher(t).matches()
    }
  }

  def flags: Int = _flags

  def matcher(input: CharSequence): Matcher = new Matcher(this, input)

  def pattern: String = _regex

  def split(input: CharSequence): Array[String] = split(input, 0)

  def split(input: CharSequence, limit: Int): Array[String] =
    compiled.split(input, limit)

  def splitAsStream(input: CharSequence): Stream[String] =
    Arrays
      .stream(split(input))
      .asInstanceOf[Stream[String]]

  override def toString: String = _regex
}
