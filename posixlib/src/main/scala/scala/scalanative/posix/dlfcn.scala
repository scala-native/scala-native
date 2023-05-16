package scala.scalanative
package posix

import scala.scalanative.unsafe._

/** POSIX dlfcn.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@link("dl")
@extern object dlfcn {

// Symbolic constants

  @name("scalanative_rtld_lazy")
  def RTLD_LAZY: CInt = extern

  @name("scalanative_rtld_now")
  def RTLD_NOW: CInt = extern

  @name("scalanative_rtld_global")
  def RTLD_GLOBAL: CInt = extern

  @name("scalanative_rtld_local")
  def RTLD_LOCAL: CInt = extern

// Methods

  // Convention: A C "void *" is represented in Scala Native as a "Ptr[Byte]".

  def dlclose(handle: Ptr[Byte]): Int = extern

  def dlerror(): CString = extern

  def dlopen(filename: CString, flags: Int): Ptr[Byte] = extern

  def dlsym(handle: Ptr[Byte], symbol: CString): Ptr[Byte] = extern
}
