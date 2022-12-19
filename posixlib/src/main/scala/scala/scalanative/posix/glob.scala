package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.unsafe.Nat._
import scalanative.meta.LinktimeInfo.isLinux

import scalanative.posix.sys.types

/** POSIX glob.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
object glob {

  type size_t = types.size_t

  /* POSIX specification names the minimally required fields.
   * It allows re-ordering and additional fields.
   *
   * Linux orders the fields in the same way as POSIX. macOS uses
   * a different order. Use the macOS field order to correspond with the
   * macOS size (below). globOps below handles the differing field orders.
   *
   * macOS sizeof(glob_t) is 88 bytes. Linux is 72. Declare Scala Native glob_t
   * as the former size to cover both cases. glob.c has _Static_assert code
   * to check Scala Native glob_t against operating system size & field order.
   */
  type glob_t = CStruct6[
    size_t, //  gl_pathc, count of total paths so far
    CInt, // gl_matchc, count of paths matching pattern
    size_t, // gl_offs, reserved at beginning of gl_pathv
    CInt, // gl_flags, returned flags
    Ptr[CString], // gl_pathv, list of paths matching pattern
    CArray[CUnsignedChar, Nat.Digit2[_5, _6]] // macOS non-POSIX fields
  ]

  type unixGlob_t = CStruct4[
    size_t, //  gl_pathc, count of total paths so far
    Ptr[CString], // gl_pathv, list of paths matching pattern
    size_t, // gl_offs, reserved at beginning of gl_pathv
    CArray[CUnsignedChar, Nat.Digit2[_6, _4]] // macOS non-POSIX fields
  ]

  /// Symbolic constants
  // flags

  @name("scalanative_glob_append")
  def GLOB_APPEND: CInt = extern

  @name("scalanative_glob_dooffs")
  def GLOB_DOOFFS: CInt = extern

  @name("scalanative_glob_err")
  def GLOB_ERR: CInt = extern

  @name("scalanative_glob_mark")
  def GLOB_MARK: CInt = extern

  @name("scalanative_glob_nocheck")
  def GLOB_NOCHECK: CInt = extern

  @name("scalanative_glob_noescape")
  def GLOB_NOESCAPE: CInt = extern

  @name("scalanative_glob_nosort")
  def GLOB_NOSORT: CInt = extern

  // error returns
  @name("scalanative_glob_aborted")
  def GLOB_ABORTED: CInt = extern

  @name("scalanative_glob_nomatch")
  def GLOB_NOMATCH: CInt = extern

  @name("scalanative_glob_nospace")
  def GLOB_NOSPACE: CInt = extern

  /// Methods

  def glob(
      pattern: CString,
      flags: CInt,
      errfunc: CFuncPtr2[CString, CInt, CInt],
      pglob: Ptr[glob_t]
  ): CInt = extern

  def globfree(pglob: Ptr[glob_t]): CInt = extern
}

object globOps {
  import glob.{glob_t, unixGlob_t, size_t}

  implicit class glob_tOps(val ptr: Ptr[glob_t]) extends AnyVal {
    def gl_pathc: size_t = ptr._1 // Count of paths matched by pattern.

    // Pointer to a list of matched pathnames.
    def gl_pathv: Ptr[CString] =
      if (isLinux) ptr.asInstanceOf[Ptr[unixGlob_t]]._2
      else ptr._5

    // Slots to reserve at the beginning of gl_pathv.
    def gl_offs: size_t = ptr._3

    // gl_pathc & gl_pathv are usually read-only; gl_offs get used for write.
    def gl_pathc_=(v: size_t): Unit = ptr._1 = v

    def gl_pathv_=(v: Ptr[CString]): Unit =
      if (isLinux)
        ptr.asInstanceOf[Ptr[unixGlob_t]]._2 = v
      else
        ptr._5 = v

    def gl_offs_=(v: size_t): Unit = ptr._3 = v
  }
}
