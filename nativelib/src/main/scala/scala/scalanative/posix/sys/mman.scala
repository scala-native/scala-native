package scala.scalanative
package posix.sys

import native._
import types.{mode_t, off_t}

@extern
object mman {
  def mmap(addr: Ptr[Byte],
           len: CSize,
           prot: CInt,
           flags: CInt,
           fd: CInt,
           offset: off_t): Ptr[Byte] = extern

  /* Deallocate any mapping for the region starting at ADDR and extending LEN
   bytes.  Returns 0 if successful, -1 for errors (and sets errno).  */
  def munmap(addr: Ptr[Byte], len: CSize): CInt = extern

  /* Change the memory protection of the region starting at ADDR and
     extending LEN bytes to PROT.  Returns 0 if successful, -1 for errors
     (and sets errno).  */
  def mprotect(addr: Ptr[Byte], len: CSize, prot: CInt): CInt = extern

  /* Synchronize the region starting at ADDR and extending LEN bytes with the
     file it maps.*/
  def msync(addr: Ptr[Byte], len: CSize, flags: CInt): CInt = extern

  /* Advise the system about particular usage patterns the program follows
       for the region starting at ADDR and extending LEN bytes.  */
  def madvise(addr: Ptr[Byte], len: CSize, advice: CInt): CInt = extern

  /* Guarantee all whole pages mapped by the range [ADDR,ADDR+LEN) to
     be memory resident.  */
  def mlock(addr: Ptr[Byte], len: CSize): CInt = extern

  /* Unlock whole pages previously mapped by the range [ADDR,ADDR+LEN).  */
  def munlock(addr: Ptr[Byte], len: CSize): CInt = extern

  /* Cause all currently mapped pages of the process to be memory resident
     until unlocked by a call to the `munlockall', until the process exits,
     or until the process calls `execve'.  */
  def mlockall(flags: CInt): CInt = extern

  /* All currently mapped pages of the process' address space become
     unlocked.  */
  def munlockall(): CInt = extern

  /* mincore returns the memory residency status of the pages in the
       current process's address space specified by [start, start + len).
       The status is returned in a vector of bytes.  The least significant
       bit of each byte is 1 if the referenced page is in memory, otherwise
       it is zero.  */
  def mincore(start: Ptr[Byte], len: CSize, vec: Ptr[Byte]): CInt = extern

  /* Remap pages mapped by the range [ADDR,ADDR+OLD_LEN) to new length
       NEW_LEN.  If MREMAP_MAYMOVE is set in FLAGS the returned address
       may differ from ADDR.  If MREMAP_FIXED is set in FLAGS the function
       takes another parameter which is a fixed address at which the block
       resides after a successful call.  */
  def mremap(addr: Ptr[Byte],
             old_len: CSize,
             new_len: CSize,
             flags: CInt): Ptr[Byte] = extern

  /* Remap arbitrary pages of a shared backing store within an existing
     VMA.  */
  def remap_file_pages(start: Ptr[Byte],
                       ize: CSize,
                       prot: CInt,
                       pgoff: CSize,
                       flags: CInt): CInt = extern

  /* Open shared memory segment.  */
  def shm_open(name: CString, oflag: CInt, mode: mode_t): CInt = extern

  /* Remove shared memory segment.  */
  def shm_unlink(name: CString): CInt = extern

  // Macros
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
  @name("scalanative_map_32bit")
  def MAP_32BIT: CInt = extern
  @name("scalanative_map_anon")
  def MAP_ANON: CInt = extern
  @name("scalanative_map_anonymous")
  def MAP_ANONYMOUS: CInt = extern
  @name("scalanative_map_denywrite")
  def MAP_DENYWRITE: CInt = extern
  @name("scalanative_map_executable")
  def MAP_EXECUTABLE: CInt = extern
  @name("scalanative_map_file")
  def MAP_FILE: CInt = extern
  @name("scalanative_map_fixed")
  def MAP_FIXED: CInt = extern
  @name("scalanative_map_growsdown")
  def MAP_GROWSDOWN: CInt = extern
  @name("scalanative_map_hugetlb")
  def MAP_HUGETLB: CInt = extern
  @name("scalanative_map_huge_2mb")
  def MAP_HUGE_2M: CInt = extern
  @name("scalanative_map_huge_1gb")
  def MAP_HUGE_1GB: CInt = extern
  @name("scalanative_map_huge_shift")
  def MAP_HUGE_SHIFT: CInt = extern
  @name("scalanative_map_locked")
  def MAP_LOCKED: CInt = extern
  @name("scalanative_map_nonblock")
  def MAP_NONBLOCK: CInt = extern
  @name("scalanative_map_noreserve")
  def MAP_NORESERVE: CInt = extern
  @name("scalanative_map_populate")
  def MAP_POPULATE: CInt = extern
  @name("scalanative_map_stack")
  def MAP_STACK: CInt = extern
  @name("scalanative_map_uninitialized")
  def MAP_UNINITIALIZED: CInt = extern
  @name("scalanative_config_mmap_allow_uninitialized")
  def CONFIG_MMAP_ALLOW_UNINITIALIZED: CInt = extern

}
