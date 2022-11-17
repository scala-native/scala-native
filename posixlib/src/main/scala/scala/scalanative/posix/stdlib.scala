package scala.scalanative
package posix

import scala.scalanative.unsafe.{CInt, CString, extern}

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
 */
@extern object stdlib extends stdlib

@extern trait stdlib extends libc.stdlib {
  def setenv(name: CString, value: CString, overwrite: CInt): CInt = extern
  def unsetenv(name: CString): CInt = extern
}
