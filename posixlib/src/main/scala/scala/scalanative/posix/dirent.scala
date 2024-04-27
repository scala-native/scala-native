package scala.scalanative
package posix

import scalanative.posix.sys.types.ino_t

import scala.scalanative.unsafe._, Nat._

object DirentTypes {
  type _256 = Digit3[_2, _5, _6] // see comment above 'type dirent' below.

  type DIR = CStruct0 // An opaque structure from os. Deconstruct a your peril.

  /* Use "glue" is here because Direct call through to the operating
   * system is beyond the scope of time available. Linux, FreeBSD and,
   * macOS appear to use different layouts, possibly containing padding.
   */

  /* The second field, d_name, is suspect and should be changed to CString
   * as soon as breaking changes are allowed after 0.5.0.
   *
   * The handling of d_name across operating systems is complex. All, so far,
   * agree that the field is a null-terminated array of characters, a.k.a
   * CString.
   *
   * Guarantees of the allocated size of that CString are stated but
   * unreliable.  Some operating systems do not specify NAME_MAX.
   * Some specify it as 255 but can return a string longer that that.
   * macOS specifies 255 UTF-8 characters. Since each UTF-8 character
   * can be up to, currently, 5 bytes long, the total size can
   * exceed the _256 here.
   *
   * Using a CString here, follows the Open Group 'char   d_name[]'.
   * By C rules, that is equivalent to 'char *', a.k.a CString.
   */

  /* Code in dirent.c manually checks that the sizes & declarations
   * of this structure are compatible with the operating system definitions.
   * Devo: if you change anything here, please examine the  dirent.c checks.
   */

  type dirent =
    CStruct3[ino_t, CArray[CChar, _256], CShort]
}

// not __SCALANATIVE_POSIX_DIRENTIMPL for historical/version_compatibility.
@define("__SCALANATIVE_POSIX_DIRENT")
@extern
private[scalanative] object DirentImpl {
  import DirentTypes._

  type DIR = DirentTypes.DIR

  def scalanative_closedir(dirp: Ptr[DIR]): CInt = extern

  def scalanative_opendir(name: CString): Ptr[DIR] = extern

  def scalanative_readdir(dirp: Ptr[DIR]): CInt = extern

  // Not POSIX, javalib internal use only.
  def scalanative_readdirImpl(dirp: Ptr[DIR], buf: Ptr[dirent]): CInt = extern

  def scalanative_dt_unknown: CInt = extern
  def scalanative_dt_fifo: CInt = extern
  def scalanative_dt_chr: CInt = extern
  def scalanative_dt_dir: CInt = extern
  def scalanative_dt_blk: CInt = extern
  def scalanative_dt_reg: CInt = extern
  def scalanative_dt_lnk: CInt = extern
  def scalanative_dt_sock: CInt = extern
  def scalanative_dt_wht: CInt = extern
}

object dirent {
  import DirentTypes._
  import DirentImpl._

  type DIR = DirentTypes.DIR
  type dirent = DirentTypes.dirent

  def closedir(dirp: Ptr[DIR]): CInt = scalanative_closedir(dirp)

  def opendir(name: CString): Ptr[DIR] = scalanative_opendir(name)

  def readdir(dirp: Ptr[DIR]): Ptr[dirent] = {
    val entry = new Array[Byte](sizeOf[dirent]).at(0).asInstanceOf[Ptr[dirent]]

    if (DirentImpl.scalanative_readdirImpl(dirp, entry) != 0) null
    else entry
  }

  @deprecated(
    "Not POSIX, subject to removal in the future. Use readdir(dirp) instead.",
    since = "posixlib 0.5.2"
  )
  // Not to be confused with POSIX readdir_r().
  def readdir(dirp: Ptr[DIR], buf: Ptr[dirent]): CInt =
    DirentImpl.scalanative_readdirImpl(dirp, buf)

  /* readdir_r() is specified in Open Group 2018 version, and probably earlier.
   *
   * macOS allows the function but Linux & FreeBSD describe it a "deprecated"
   * and recommend using a current thread-safe version of 'readdir()'.
   * For one discussion, see:
   *  https://man7.org/linux/man-pages/man3/readdir_r.3.html
   *
   * Not implemented to avoid spending time on complexity to no good end.
   */
  // def readdir_r(/* three arguments */)

  def DT_UNKNOWN: CInt = scalanative_dt_unknown

  def DT_FIFO: CInt = scalanative_dt_fifo

  def DT_CHR: CInt = scalanative_dt_chr

  def DT_DIR: CInt = scalanative_dt_dir

  def DT_BLK: CInt = scalanative_dt_blk

  def DT_REG: CInt = scalanative_dt_reg

  def DT_LNK: CInt = scalanative_dt_lnk

  def DT_SOCK: CInt = scalanative_dt_sock

  def DT_WHT: CInt = scalanative_dt_wht
}
