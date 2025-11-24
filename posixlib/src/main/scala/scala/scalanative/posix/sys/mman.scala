package scala.scalanative
package posix
package sys

import scala.scalanative.posix.sys.types._
import scala.scalanative.unsafe._

/** Partial implementation of POSIX mman.h for Scala
 *
 *  @see
 *    The Open Group Base Specifications
 *    [[https://pubs.opengroup.org/onlinepubs/9799919799 Issue 8, 2024]]
 *    edition.
 *
 *  A method with an ""XSI|SIO"" comment indicates it is defined in extended
 *  POSIX X/Open System Interfaces or Synchronized Input and Output
 *  specifications, not base POSIX.
 *
 *  The ADV, ML, MLR, SHM, TYM extensions are not yet implemented.
 */
@extern
@define("__SCALANATIVE_POSIX_SYS_MMAN")
object mman {
  def mmap(
      addr: CVoidPtr,
      length: size_t,
      prot: CInt,
      flags: CInt,
      fd: CInt,
      offset: off_t
  ): Ptr[Byte] = extern

  def munmap(addr: CVoidPtr, length: size_t): CInt = extern

  /** XSI|SIO
   */
  @blocking
  def msync(addr: CVoidPtr, length: size_t, flags: CInt): CInt = extern

  // Return value
  @name("scalanative_map_failed")
  def MAP_FAILED: CVoidPtr = extern

  // Symbolic "constants", roughly in POSIX declaration order

  @name("scalanative_prot_exec")
  def PROT_EXEC: CInt = extern

  @name("scalanative_prot_read")
  def PROT_READ: CInt = extern

  @name("scalanative_prot_write")
  def PROT_WRITE: CInt = extern

  @name("scalanative_prot_none")
  def PROT_NONE: CInt = extern

  @name("scalanative_map_anon")
  def MAP_ANON: CInt = extern

  @name("scalanative_map_anonymous")
  def MAP_ANONYMOUS: CInt = extern

  @name("scalanative_map_shared")
  def MAP_SHARED: CInt = extern

  @name("scalanative_map_private")
  def MAP_PRIVATE: CInt = extern

  @name("scalanative_map_fixed")
  def MAP_FIXED: CInt = extern

  // Symbolic 'constants' for the msync() function

  /** XSI|SIO
   */
  @name("scalanative_ms_async")
  def MS_ASYNC: CInt = extern

  /** XSI|SIO
   */
  @name("scalanative_ms_invalidate")
  def MS_SYNC: CInt = extern

  /** XSI|SIO
   */
  @name("scalanative_ms_invalidate")
  def MS_INVALIDATE: CInt = extern

}
