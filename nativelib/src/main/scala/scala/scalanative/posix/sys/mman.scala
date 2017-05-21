package scala.scalanative
package posix.sys

import native._
import types.{mode_t, off_t}

// http://pubs.opengroup.org/onlinepubs/9699919799/

@extern
object mman {

  def mlock(addr: Ptr[Byte], len: CSize): CInt = extern

  def mlockall(flags: CInt): CInt = extern

  def mmap(addr: Ptr[Byte],
           len: CSize,
           prot: CInt,
           flags: CInt,
           fd: CInt,
           offset: off_t): Ptr[Byte] = extern

  def mprotect(addr: Ptr[Byte], len: CSize, prot: CInt): CInt = extern

  def msync(addr: Ptr[Byte], len: CSize, flags: CInt): CInt = extern

  def munlock(addr: Ptr[Byte], len: CSize): CInt = extern

  def munlockall(): CInt = extern

  def munmap(addr: Ptr[Byte], len: CSize): CInt = extern

  def posix_madvise(addr: Ptr[Byte], len: CSize, advice: CInt): CInt = extern

  def posix_mem_offset(addr: Ptr[Byte],
                       len: CSize,
                       off: Ptr[off_t],
                       contig_len: Ptr[CSize],
                       fildes: Ptr[CInt]): CInt = extern

  def posix_typed_mem_get_info(fildes: CInt,
                               info: Ptr[posix_typed_mem_info]): CInt = extern

  def posix_typed_mem_open(name: CString, oflag: CInt, tflag: CInt): CInt =
    extern

  def shm_open(name: CString, oflag: CInt, mode: mode_t): CInt = extern

  def shm_unlink(name: CString): CInt = extern

  // Types

  type posix_typed_mem_info = CStruct1[CSize]

  // Macros

  @name("scalanative_prot_exec")
  def PROT_EXEC: CInt = extern

  @name("scalanative_prot_read")
  def PROT_READ: CInt = extern

  @name("scalanative_prot_write")
  def PROT_WRITE: CInt = extern

  @name("scalanative_prot_none")
  def PROT_NONE: CInt = extern

  @name("scalanative_map_fixed")
  def MAP_FIXED: CInt = extern

  @name("scalanative_map_shared")
  def MAP_SHARED: CInt = extern

  @name("scalanative_map_private")
  def MAP_PRIVATE: CInt = extern

  @name("scalanative_ms_async")
  def MS_ASYNC: CInt = extern

  @name("scalanative_ms_invalidate")
  def MS_INVALIDATE: CInt = extern

  @name("scalanative_ms_sync")
  def MS_SYNC: CInt = extern

  @name("scalanative_mcl_current")
  def MCL_CURRENT: CInt = extern

  @name("scalanative_mcl_future")
  def MCL_FUTURE: CInt = extern

  @name("scalanative_posix_madv_dontneed")
  def POSIX_MADV_DONTNEED: CInt = extern

  @name("scalanative_posix_madv_normal")
  def POSIX_MADV_NORMAL: CInt = extern

  @name("scalanative_posix_madv_random")
  def POSIX_MADV_RANDOM: CInt = extern

  @name("scalanative_posix_madv_sequential")
  def POSIX_MADV_SEQUENTIAL: CInt = extern

  @name("scalanative_posix_madv_willneed")
  def POSIX_MADV_WILLNEED: CInt = extern

  @name("scalanative_posix_typed_mem_allocate")
  def POSIX_TYPED_MEM_ALLOCATE: CInt = extern

  @name("scalanative_posix_typed_mem_allocate_contig")
  def POSIX_TYPED_MEM_ALLOCATE_CONTIG: CInt = extern

  @name("scalanative_posix_typed_mem_map_allocatable")
  def POSIX_TYPED_MEM_MAP_ALLOCATABLE: CInt = extern
}
