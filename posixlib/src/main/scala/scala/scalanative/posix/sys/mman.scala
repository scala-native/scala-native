package scala.scalanative
package posix
package sys

import scala.scalanative.unsafe._
import scala.scalanative.unsafe.extern
import scala.scalanative.posix.sys.types._

@extern
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

  @blocking
  def msync(addr: CVoidPtr, length: size_t, flags: CInt): CInt = extern

  @name("scalanative_prot_exec")
  def PROT_EXEC: CInt = extern

  @name("scalanative_prot_read")
  def PROT_READ: CInt = extern

  @name("scalanative_prot_write")
  def PROT_WRITE: CInt = extern

  @name("scalanative_prot_none")
  def PROT_NONE: CInt = extern

  @name("scalanative_map_shared")
  def MAP_SHARED: CInt = extern

  @name("scalanative_map_private")
  def MAP_PRIVATE: CInt = extern

  @name("scalanative_map_fixed")
  def MAP_FIXED: CInt = extern

  @name("scalanative_ms_async")
  def MS_ASYNC: CInt = extern

  @name("scalanative_ms_invalidate")
  def MS_SYNC: CInt = extern

  @name("scalanative_ms_invalidate")
  def MS_INVALIDATE: CInt = extern

}
