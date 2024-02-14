package scala.scalanative
package posix

import scala.scalanative.unsafe._

import scalanative.posix.sys.types.size_t

/** POSIX stdlib.h for Scala
 *
 *  Some of the functionality described on this reference page extends the ISO C
 *  standard. Applications shall define the appropriate feature test macro (see
 *  XSH The Compilation Environment ) to enable the visibility of these symbols
 *  in this header.
 *
 *  Extension to the ISO C standard: The functionality described is an extension
 *  to the ISO C standard. Application developers may make use of an extension
 *  as it is supported on all POSIX.1-2017-conforming systems.
 *
 *  All the methods declared in this file and not libc.stdlib are Open Group
 *  extensions to the ISO/IEC C standard.
 *
 *  A method with an XSI comment indicates it is defined in extended POSIX
 *  X/Open System Interfaces, not base POSIX.
 *
 *  A method with an ADV comment indicates it Open Group 2018 "Advisory
 *  Information" meaning, from the specification: "The functionality described
 *  is optional. The functionality described is also an extension to the ISO C
 *  standard."
 */
@extern object stdlib extends stdlib

/** posixlib stdlib is known to be incomplete. It contains the methods from the
 *  Open Group 2028 specification but, not yet, all of the declarations. For an
 *  incomplete example, he data types div_t, ldiv_t, lldiv_t returned by div() &
 *  ldiv and the constants described in sys/wait.h are not defined.
 */
@extern trait stdlib extends libc.stdlib {

  /** XSI */
  def a64l(str64: CString): CLong = extern

  /** XSI */
  def drand48(): Double = extern

  /** XSI */
  def erand48(xsubi: Ptr[CUnsignedShort]): Double = extern

  def getsubopt(
      optionp: Ptr[CString],
      tokens: Ptr[CString],
      valuep: Ptr[CString]
  ): CInt = extern

  /** XSI */
  def grantpt(fd: CInt): CInt = extern

  /** XSI */
  def initstate(
      seed: CUnsignedInt,
      state: Ptr[CChar],
      size: size_t
  ): Ptr[CChar] =
    extern

  /** XSI */
  def jrand48(xsubi: Ptr[CUnsignedShort]): CLong = extern

  /** XSI */
  def l64a(value: CLong): CString = extern

  /** XSI */
  def lcong48(param: Ptr[CUnsignedShort]): Unit = extern

  /** XSI */
  def lrand48(): CLong = extern

  def mkdtemp(template: CString): CString = extern

  def mkstemp(template: CString): CInt = extern

  /** XSI */
  def mrand48(): CLong = extern

  /** XSI */
  def nrand48(xsubi: Ptr[CUnsignedShort]): CLong = extern

  /** ADV */
  def posix_memalign(
      memptr: Ptr[Ptr[_]],
      alignment: size_t,
      size: size_t
  ): CInt = extern

  /** XSI */
  def posix_openpt(flags: CInt): CInt = extern

  /** XSI */
  def ptsname(fd: CInt): CString = extern

  /** XSI */
  def putenv(string: CString): CInt = extern

  // OB CX - not implemented
  // int rand_r(unsigned *);

  /** XSI */
  def random(): CLong = extern

  /** XSI */
  def realpath(path: CString, resolved_path: CString): CString = extern

  /** XSI */
  def seed48(seed16v: Ptr[CUnsignedShort]): Ptr[CUnsignedShort] = extern

  def setenv(name: CString, value: CString, overwrite: CInt): CInt = extern

  /** XSI */
  def setkey(key: CString): Unit = extern

  /** XSI */
  def setstate(state: Ptr[CChar]): Ptr[CChar] = extern

  /** XSI */
  def srand48(seedval: CLong): Unit = extern

  /** XSI */
  def srandom(seed: CUnsignedInt): Unit = extern

  /** XSI */
  def unlockpt(fd: CInt): CInt = extern

  def unsetenv(name: CString): CInt = extern
}
