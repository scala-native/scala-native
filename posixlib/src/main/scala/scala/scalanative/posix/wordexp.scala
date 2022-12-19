package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.unsafe.Nat._

import scalanative.posix.sys.types.size_t

/** POSIX wordexp.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
object wordexp {

  type wordexp_t = CStruct3[
    size_t, //  we_wordc  Count of words matched by 'words'.
    Ptr[CString], // we_wordv  Pointer to list of expanded words.
    size_t, // we_offs   Slots to reserve at the beginning of we_wordv.
  ]

  /// Symbolic constants
  // flags

  @name("scalanative_wrde_append")
  def WRDE_APPEND: CInt = extern

  @name("scalanative_wrde_dooffs")
  def WRDE_DOOFFS: CInt = extern

  @name("scalanative_wrde_nocmd")
  def WRDE_NOCMD: CInt = extern

  @name("scalanative_wrde_reuse")
  def WRDE_REUSE: CInt = extern

  @name("scalanative_wrde_showerr")
  def WRDE_SHOWERR: CInt = extern

  @name("scalanative_wrde_undef")
  def WRDE_UNDEF: CInt = extern

  // error returns
  @name("scalanative_wrde_badchar")
  def WRDE_BADCHAR: CInt = extern

  @name("scalanative_wrde_badval")
  def WRDE_BADVAL: CInt = extern

  @name("scalanative_wrde_cmdsub")
  def WRDE_CMDSUB: CInt = extern

  @name("scalanative_wrde_nospace")
  def WRDE_NOSPACE: CInt = extern

  @name("scalanative_wrde_syntax")
  def WRDE_SYNTAX: CInt = extern

  /// Methods

  def wordexp(
      pattern: CString,
      expansion: Ptr[wordexp_t],
      flags: CInt
  ): CInt = extern

  def wordfree(wordexpP: Ptr[wordexp_t]): CInt = extern
}

object wordexpOps {
  import wordexp.wordexp_t

  implicit class wordexp_tOps(val ptr: Ptr[wordexp_t]) extends AnyVal {
    def we_wordc: size_t = ptr._1
    def we_wordv: Ptr[CString] = ptr._2
    def we_offs: size_t = ptr._3

    def we_wordc_=(v: size_t): Unit = ptr._1 = v
    def we_wordv_=(v: Ptr[CString]): Unit = ptr._2 = v
    def we_offs_=(v: size_t): Unit = ptr._3 = v
  }
}
