package scala.scalanative
package native

@extern
object stdlib {

  // Memory management

  def malloc(size: CSize): Ptr[Byte]                     = extern
  def calloc(num: CSize, size: CSize): Ptr[Byte]         = extern
  def realloc(ptr: Ptr[Byte], newSize: CSize): Ptr[Byte] = extern
  def free(ptr: Ptr[Byte]): Unit                         = extern
  def aligned_alloc(alignment: CSize, size: CSize): Unit = extern

  // Program utilities

  def abort(): Unit                                  = extern
  def exit(exitCode: CInt): Unit                     = extern
  def quick_exit(exitCode: CInt): Unit               = extern
  def _Exit(exitCode: CInt): Unit                    = extern
  def atexit(func: CFunctionPtr0[Unit]): CInt        = extern
  def at_quick_exit(func: CFunctionPtr0[Unit]): CInt = extern

  // Communicating with the environment

  def system(command: CString): CInt = extern
  def getenv(name: CString): CString = extern

  // Pseudo-random number generation

  def rand(): CInt                    = extern
  def srand(seed: CUnsignedInt): Unit = extern

  // Conversions to numeric formats

  def atof(str: CString): CDouble                                    = extern
  def atoi(str: CString): CInt                                       = extern
  def atol(str: CString): CLong                                      = extern
  def atoll(str: CString): CLongLong                                 = extern
  def strtol(str: CString, str_end: Ptr[CString], base: CInt): CLong = extern
  def strtoll(str: CString, str_end: Ptr[CString], base: CInt): CLongLong =
    extern
  def strtoul(str: CString, str_end: Ptr[CString], base: CInt): CUnsignedLong =
    extern
  def strtoull(str: CString,
               str_end: Ptr[CString],
               base: CInt): CUnsignedLongLong =
    extern
  def strtof(str: CString, str_end: Ptr[CString]): CFloat  = extern
  def strtod(str: CString, str_end: Ptr[CString]): CDouble = extern

  // Macros

  @name("scalanative_libc_exit_success")
  def EXIT_SUCCESS: CInt = extern
  @name("scalanative_libc_exit_failure")
  def EXIT_FAILURE: CInt = extern
  @name("scalanative_libc_rand_max")
  def RAND_MAX: CInt = extern
}
