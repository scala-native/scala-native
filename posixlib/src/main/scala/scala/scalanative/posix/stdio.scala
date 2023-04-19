package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.posix.sys.types, types.{off_t, size_t}

@extern object stdio extends stdio

@extern trait stdio extends libc.stdio {
  /* Open Group 2018 extensions to ISO/IEC C.
   * Reference:
   *   https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/stdio.h.html
   *
   * These definitions are annotated CX (ISO/IEC C extension)
   * in the above specification.
   */

  type ssize_t = types.ssize_t

  type va_list = unsafe.CVarArgList

// Macros

  /* Open Group POSIX defines this as a C macro.
   * To provide the value in a portable manner, it is implemented here as
   * an external method.  A slight but necessary deviation from the
   * specification. The same idiom is used in an number of other posixlib
   * files.
   */
  @name("scalanative_l_ctermid")
  def L_ctermid: CUnsignedInt = extern

// Methods

  @blocking def ctermid(s: CString): CString = extern

  @blocking def dprintf(fd: Int, format: CString, valist: va_list): Int = extern

  @blocking def fdopen(fd: Int, mode: CString): Ptr[FILE] = extern
  @blocking def fileno(stream: Ptr[FILE]): Int = extern
  @blocking def flockfile(filehandle: Ptr[FILE]): Unit = extern

  @blocking def fmemopen(
      buf: Ptr[Byte],
      size: size_t,
      mode: CString
  ): Ptr[FILE] = extern

  @blocking def fseeko(stream: Ptr[FILE], offset: off_t, whence: Int): Int =
    extern

  @blocking def ftello(stream: Ptr[FILE]): off_t = extern

  // Can not block; see "try" part of "ftry*"
  def ftrylockfile(filehandle: Ptr[FILE]): Int = extern

  @blocking def funlockfile(filehandle: Ptr[FILE]): Unit = extern

  @blocking def getc_unlocked(stream: Ptr[CString]): Int = extern
  @blocking def getchar_unlocked(): Int = extern

  @blocking def getdelim(
      lineptr: Ptr[CString],
      n: Ptr[size_t],
      delim: Int,
      stream: Ptr[FILE]
  ): ssize_t = extern

  @blocking def getline(
      lineptr: Ptr[CString],
      n: Ptr[size_t],
      stream: Ptr[FILE]
  ): ssize_t = extern

  @blocking def open_memstream(
      ptr: Ptr[CString],
      sizeloc: Ptr[size_t]
  ): Ptr[FILE] =
    extern

  @blocking def pclose(stream: Ptr[FILE]): Int = extern
  @blocking def popen(command: CString, typ: CString): Ptr[FILE] = extern
  @blocking def putc_unlocked(c: Int, stream: Ptr[FILE]): Int = extern
  @blocking def putchar_unlocked(c: Int): Int = extern

  @blocking def renameat(
      olddirfd: Int,
      oldpath: CString,
      newdirdf: Int,
      newpath: CString
  ): Int = extern

  @blocking def vdprintf(fd: Int, format: CString, ap: va_list): Int = extern
}
