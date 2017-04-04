package java.util.regex

import scala.scalanative._
import native._
import native.stdlib._
import runtime.GC

import java.nio.charset.Charset

// Syntax guide: https://github.com/google/re2/wiki/Syntax
// re2 c wrapper: https://github.com/marcomaggi/cre2
@link("re2")
@extern
private[regex] object cre2 {
  import cre2h._

  @name("scalanative_cre2_new")
  def compile(
      pattern: CString,
      pattern_len: CInt,
      options: Ptr[Options]
  ): Ptr[Regex] = extern

  @name("scalanative_cre2_delete")
  def delete(
      regex: Ptr[Regex]
  ): Unit = extern

  @name("scalanative_cre2_quote_meta")
  def quoteMeta(
      quoted: StringPart,
      original: StringPart
  ): CInt = extern

  @name("scalanative_cre2_num_capturing_groups")
  def numCapturingGroups(
      regex: Ptr[Regex]
  ): CInt = extern

  @name("scalanative_cre2_find_named_capturing_groups")
  def findNamedCapturingGroups(
      regex: Ptr[Regex],
      name: CString
  ): CInt = extern

  @name("scalanative_cre2_match")
  def matches(
      regex: Ptr[Regex],
      text: CString,
      textlen: CInt,
      startpos: CInt,
      endpos: CInt,
      anchor: Anchor,
      matches: StringPart,
      nMatches: CInt
  ): CInt = extern

  @name("scalanative_cre2_replace_re")
  def replace(
      regex: Ptr[Regex],
      text_and_target: StringPart,
      rewrite: StringPart
  ): CInt = extern

  @name("scalanative_cre2_global_replace_re")
  def globalReplace(
      regex: Ptr[Regex],
      text_and_target: StringPart,
      rewrite: StringPart
  ): CInt = extern

  @name("scalanative_cre2_error_code")
  def errorCode(regex: Ptr[Regex]): CInt = extern

  @name("scalanative_cre2_error_string")
  def errorString(regex: Ptr[Regex]): CString = extern

  @name("scalanative_cre2_error_arg")
  def errorArg(regex: Ptr[Regex], arg: StringPart): Unit = extern

  /* Options */
  @name("scalanative_cre2_opt_new")
  def optNew(): Ptr[Options] = extern

  @name("scalanative_cre2_opt_delete")
  def optDelete(options: Ptr[Options]): Unit = extern

  @name("scalanative_cre2_opt_set_posix_syntax")
  def setPosixSyntax(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_longest_match")
  def setLongestMatch(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_log_errors")
  def setLogErrors(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_literal")
  def setLiteral(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_nl")
  def setNeverNl(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_dot_nl")
  def setDotNl(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_capture")
  def setNeverCapture(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_capture")
  def setCaseSensitive(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_capture")
  def setPerlClasses(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_word_boundary")
  def setWordBoundary(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_one_line")
  def setOneLine(
      options: Ptr[Options],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_max_mem")
  def setMaxMem(
      options: Ptr[Options],
      m: CLong
  ): Unit = extern

  @name("scalanative_cre2_opt_set_encoding")
  def setEncoding(
      options: Ptr[Options],
      enc: Encoding
  ): Unit = extern
}
