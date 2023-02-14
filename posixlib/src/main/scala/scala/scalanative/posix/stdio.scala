package scala.scalanative
package posix

import scalanative.unsafe, unsafe._
import scalanative.posix.sys.types, types.{off_t, size_t}
import scalanative.libc.stdio._

@extern object stdio {
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

  def ctermid(s: CString): CString = extern

  def dprintf(fd: Int, format: CString, valist: va_list): Int = extern

  def fdopen(fd: Int, mode: CString): Ptr[FILE] = extern
  def fileno(stream: Ptr[FILE]): Int = extern
  def flockfile(filehandle: Ptr[FILE]): Unit = extern

  def fmemopen(
      buf: Ptr[Byte],
      size: size_t,
      mode: CString
  ): Ptr[FILE] = extern

  def fseeko(stream: Ptr[FILE], offset: off_t, whence: Int): Int =
    extern

  def ftello(stream: Ptr[FILE]): off_t = extern

  // Can not block; see "try" part of "ftry*"
  def ftrylockfile(filehandle: Ptr[FILE]): Int = extern

  def funlockfile(filehandle: Ptr[FILE]): Unit = extern

  def getc_unlocked(stream: Ptr[CString]): Int = extern
  def getchar_unlocked(): Int = extern

  def getdelim(
      lineptr: Ptr[CString],
      n: Ptr[size_t],
      delim: Int,
      stream: Ptr[FILE]
  ): ssize_t = extern

  def getline(
      lineptr: Ptr[CString],
      n: Ptr[size_t],
      stream: Ptr[FILE]
  ): ssize_t = extern

  def open_memstream(
      ptr: Ptr[CString],
      sizeloc: Ptr[size_t]
  ): Ptr[FILE] =
    extern

  def pclose(stream: Ptr[FILE]): Int = extern
  def popen(command: CString, typ: CString): Ptr[FILE] = extern
  def putc_unlocked(c: Int, stream: Ptr[FILE]): Int = extern
  def putchar_unlocked(c: Int): Int = extern

  def renameat(
      olddirfd: Int,
      oldpath: CString,
      newdirdf: Int,
      newpath: CString
  ): Int = extern

  def vdprintf(fd: Int, format: CString, ap: va_list): Int = extern
}
