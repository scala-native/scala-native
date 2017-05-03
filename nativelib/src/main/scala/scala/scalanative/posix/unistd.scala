package scala.scalanative.posix

import scala.scalanative.native.{
  CUnsignedInt,
  CString,
  CLongLong,
  Ptr,
  extern,
  name,
  CSize,
  CInt
}

@extern
object unistd {

  type off_t = CLongLong

  def sleep(seconds: CUnsignedInt): CUnsignedInt                  = extern
  def usleep(usecs: CUnsignedInt): CInt                           = extern
  def unlink(path: CString): CInt                                 = extern
  def access(pathname: CString, mode: CInt): CInt                 = extern
  def readlink(path: CString, buf: CString, bufsize: CSize): CInt = extern
  def getcwd(buf: CString, size: CSize): CString                  = extern
  def write(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt        = extern
  def read(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt         = extern
  def close(fildes: CInt): CInt                                   = extern
  def fsync(fildes: CInt): CInt                                   = extern
  def lseek(fildes: CInt, offset: off_t, whence: CInt): off_t     = extern
  def ftruncate(fildes: CInt, length: off_t): CInt                = extern
  def truncate(path: CString, length: off_t): CInt                = extern

  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  // Macros

  @name("scalanative_environ")
  def environ: Ptr[CString] = extern
}
