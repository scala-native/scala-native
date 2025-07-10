package java.lang

import scalanative.meta.LinktimeInfo._
import scalanative.unsafe._

private[java] object MemmemImpl {
  /* The central idea is to use a memmem() provided by the operating system
   * where possible or else fall back to a less efficient local implementation.
   *
   * Unix-like systems, listed below, are known to have implemented memmem()
   * in libc for at least a few decades. The POSIX version may be less than 8
   * 
   * ScalaNativeMemmem.memmem() will use posix.string.memmem on unspecified
   * systems which implement Open Group Issue 8 or above. Otherwise,
   * such as on Windows, it will use the local implementation.
   */

  /** CX - The Open Group Base Specifications Issue 8 */
  def memmem(
      haystack: CVoidPtr,
      haystacklen: CInt,
      needle: CVoidPtr,
      needlelen: CInt
  ): CVoidPtr = {
    val impl =
      if (isLinux || isMac || isFreeBSD || isOpenBSD || isNetBSD)
        scalanative.posix.string.memmem(_, _, _, _)
      else
        ScalaNativeMemmem.memmem(_, _, _, _)

    impl(haystack, haystacklen, needle, needlelen)
  }
}

@define("__SCALANATIVE_JAVALIB_MEMMEM")
@extern
private[java] object ScalaNativeMemmem {

  @name("scalanative_memmem")
  def memmem(
      haystack: CVoidPtr,
      haystacklen: CInt,
      needle: CVoidPtr,
      needlelen: CInt
  ): CVoidPtr = extern
}
