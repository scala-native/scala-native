package java.util.regex

import scala.scalanative._
import native._
import native.stdlib._

import java.nio.charset.Charset

// Syntax guide: https://github.com/google/re2/wiki/Syntax
// re2 c wrapper: https://github.com/marcomaggi/cre2
@link("re2")
@extern
object cre2 {
  type regexp_t     = CStruct0
  type options_t    = CStruct0
  type string_t     = CStruct2[CString, CInt]
  type error_code_t = CInt
  type anchor_t     = CInt
  type encoding_t   = CInt

  @name("scalanative_cre2_new")
  def compile(
      pattern: CString,
      pattern_len: CInt,
      options: Ptr[options_t]
  ): Ptr[regexp_t] = extern

  @name("scalanative_cre2_delete")
  def delete(
      regex: Ptr[regexp_t]
  ): Unit = extern

  @name("scalanative_cre2_quote_meta")
  def quoteMeta(
      quoted: Ptr[string_t],
      original: Ptr[string_t]
  ): CInt = extern

  @name("scalanative_cre2_num_capturing_groups")
  def numCapturingGroups(
      regex: Ptr[regexp_t]
  ): CInt = extern

  @name("scalanative_cre2_find_named_capturing_groups")
  def findNamedCapturingGroups(
      regex: Ptr[regexp_t],
      name: CString
  ): CInt = extern

  @name("scalanative_cre2_match")
  def matches(
      regex: Ptr[regexp_t],
      text: CString,
      textlen: CInt,
      startpos: CInt,
      endpos: CInt,
      anchor: anchor_t,
      matches: Ptr[string_t],
      nMatches: CInt
  ): CInt = extern

  @name("scalanative_cre2_replace_re")
  def replace(
      regex: Ptr[regexp_t],
      text_and_target: Ptr[string_t],
      rewrite: Ptr[string_t]
  ): CInt = extern

  @name("scalanative_cre2_global_replace_re")
  def globalReplace(
      regex: Ptr[regexp_t],
      text_and_target: Ptr[string_t],
      rewrite: Ptr[string_t]
  ): CInt = extern

  @name("scalanative_cre2_error_code")
  def errorCode(regex: Ptr[regexp_t]): error_code_t = extern

  @name("scalanative_cre2_error_string")
  def errorString(regex: Ptr[regexp_t]): CString = extern

  @name("scalanative_cre2_error_arg")
  def errorArg(regex: Ptr[regexp_t], arg: Ptr[string_t]): Unit = extern

  @name("scalanative_cre2_opt_new")
  def optNew(): Ptr[options_t] = extern

  @name("scalanative_cre2_opt_delete")
  def optDelete(options: Ptr[options_t]): Unit = extern

  @name("scalanative_cre2_opt_set_posix_syntax")
  def setPosixSyntax(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_longest_match")
  def setLongestMatch(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_log_errors")
  def setLogErrors(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_literal")
  def setLiteral(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_nl")
  def setNeverNl(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_dot_nl")
  def setDotNl(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_capture")
  def setNeverCapture(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_capture")
  def setCaseSensitive(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_never_capture")
  def setPerlClasses(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_word_boundary")
  def setWordBoundary(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_one_line")
  def setOneLine(
      options: Ptr[options_t],
      flag: CInt
  ): Unit = extern

  @name("scalanative_cre2_opt_set_max_mem")
  def setMaxMem(
      options: Ptr[options_t],
      m: CLong
  ): Unit = extern

  @name("scalanative_cre2_opt_set_encoding")
  def setEncoding(
      options: Ptr[options_t],
      enc: encoding_t
  ): Unit = extern
}
