package scala.scalanative.misc
package regex

import scala.scalanative.native._

@extern
object CRegex {
  type CRegexStruct      = Ptr[CStruct0]
  type CRegexMatchStruct = Ptr[CStruct0]

  // Bindings

  @name("scalanative_misc_regex_create")
  def create(pattern: CString,
             flags: Regex.Flags,
             locale: CString,
             out: CString,
             max_out: CInt): CRegexStruct = extern

  @name("scalanative_misc_regex_delete")
  def delete(regex: CRegexStruct): Unit = extern

  @name("scalanative_misc_regex_match_delete")
  def deleteMatchResult(result: CRegexMatchStruct): Unit = extern

  @name("scalanative_misc_regex_search")
  def search(regex: CRegexStruct, text: CString, flags: Match.Flags): Boolean =
    extern

  @name("scalanative_misc_regex_search_with_result")
  def searchWithResult(regex: CRegexStruct,
                       text: CString,
                       flags: Match.Flags,
                       result: CRegexMatchStruct): CRegexMatchStruct = extern

  @name("scalanative_misc_regex_match")
  def matchAll(regex: CRegexStruct,
               text: CString,
               flags: Match.Flags): Boolean = extern

  @name("scalanative_misc_regex_match_with_result")
  def matchAllWithResult(regex: CRegexStruct,
                         text: CString,
                         flags: Match.Flags,
                         result: CRegexMatchStruct): CRegexMatchStruct = extern

  @name("scalanative_misc_regex_match_iterator_first")
  def matchIteratorFirst(regex: CRegexStruct,
                         text: CString,
                         flags: Match.Flags,
                         result: CRegexMatchStruct): CRegexMatchStruct = extern

  @name("scalanative_misc_regex_match_iterator_next")
  def matchIteratorNext(result: CRegexMatchStruct): CRegexMatchStruct = extern

  @name("scalanative_misc_regex_match_token_iterator_first")
  def matchTokenIteratorFirst(regex: CRegexStruct,
                              text: CString,
                              tokenCount: CInt,
                              tokens: Ptr[CInt],
                              flags: Match.Flags,
                              result: CRegexMatchStruct): CRegexMatchStruct =
    extern

  @name("scalanative_misc_regex_match_token_iterator_next")
  def matchTokenIteratorNext(result: CRegexMatchStruct): CRegexMatchStruct =
    extern

  @name("scalanative_misc_regex_match_submatch_count")
  def getMatchSubmatchCount(result: CRegexMatchStruct): CInt = extern

  @name("scalanative_misc_regex_match_submatch_string")
  def getMatchSubmatchString(result: CRegexMatchStruct,
                             index: CInt,
                             out: Ptr[CString],
                             max_out: Ptr[CInt]): Boolean = extern

  @name("scalanative_misc_regex_match_submatch_range")
  def getMatchSubmatchRange(result: CRegexMatchStruct,
                            index: CInt,
                            range: Ptr[CInt]): Boolean = extern

  @name("scalanative_misc_regex_match_format")
  def matchFormat(result: CRegexMatchStruct,
                  format: CString,
                  flags: Match.FormatFlags,
                  out: Ptr[CString],
                  max_out: Ptr[CInt]): Boolean = extern

  @name("scalanative_misc_regex_match_token_string")
  def getMatchTokenString(result: CRegexMatchStruct,
                          out: Ptr[CString],
                          max_out: Ptr[CInt]): Boolean = extern

  @name("scalanative_misc_regex_match_token_range")
  def getMatchTokenRange(result: CRegexMatchStruct,
                         range: Ptr[CInt]): Boolean = extern

  @name("scalanative_misc_regex_match_replace")
  def replaceAll(regex: CRegexStruct,
                 text: CString,
                 format: CString,
                 flags: Match.Flags,
                 out: Ptr[CString],
                 max_out: Ptr[CInt]): Boolean = extern
}
