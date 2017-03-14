package scala.scalanative.posix.sys

import scala.scalanative.native._
import scala.scalanative.posix.sys.types.{mode_t, off_t}

/**
  * Created by remi on 14/03/17.
  */
@extern
object mman {
  def mmap (addr: Ptr[Byte], len: CSize, prot: CInt,
    flags: CInt, fd: CInt, offset: off_t): Ptr[Byte] = extern

  /* Deallocate any mapping for the region starting at ADDR and extending LEN
   bytes.  Returns 0 if successful, -1 for errors (and sets errno).  */
  def munmap (addr: Ptr[Byte], len: CSize): CInt = extern

  /* Change the memory protection of the region starting at ADDR and
     extending LEN bytes to PROT.  Returns 0 if successful, -1 for errors
     (and sets errno).  */
  def mprotect (addr: Ptr[Byte], len: CSize, prot: CInt): CInt = extern

  /* Synchronize the region starting at ADDR and extending LEN bytes with the
     file it maps.*/
  def msync (addr: Ptr[Byte], len: CSize, flags: CInt): CInt = extern

    /* Advise the system about particular usage patterns the program follows
       for the region starting at ADDR and extending LEN bytes.  */
    def madvise (addr: Ptr[Byte], len: CSize, advice: CInt): CInt = extern

  /* Guarantee all whole pages mapped by the range [ADDR,ADDR+LEN) to
     be memory resident.  */
  def mlock (addr: Ptr[Byte], len: CSize): CInt = extern

  /* Unlock whole pages previously mapped by the range [ADDR,ADDR+LEN).  */
  def  munlock (addr: Ptr[Byte], len: CSize): CInt = extern

  /* Cause all currently mapped pages of the process to be memory resident
     until unlocked by a call to the `munlockall', until the process exits,
     or until the process calls `execve'.  */
  def mlockall (flags: CInt): CInt = extern

  /* All currently mapped pages of the process' address space become
     unlocked.  */
  def munlockall(): CInt = extern

  /* mincore returns the memory residency status of the pages in the
       current process's address space specified by [start, start + len).
       The status is returned in a vector of bytes.  The least significant
       bit of each byte is 1 if the referenced page is in memory, otherwise
       it is zero.  */
    def mincore (start: Ptr[Byte], len: CSize, vec: Ptr[Byte]): CInt = extern

    /* Remap pages mapped by the range [ADDR,ADDR+OLD_LEN) to new length
       NEW_LEN.  If MREMAP_MAYMOVE is set in FLAGS the returned address
       may differ from ADDR.  If MREMAP_FIXED is set in FLAGS the function
       takes another parameter which is a fixed address at which the block
       resides after a successful call.  */
    def mremap (addr: Ptr[Byte], old_len: CSize, new_len: CSize,
    flags: CInt): Ptr[Byte] = extern

  /* Remap arbitrary pages of a shared backing store within an existing
     VMA.  */
  def remap_file_pages (start: Ptr[Byte], ize: CSize, prot: CInt,
    pgoff: CSize, flags: CInt): CInt = extern

  /* Open shared memory segment.  */
 def shm_open (name: CString, oflag: CInt, mode: mode_t): CInt = extern

  /* Remove shared memory segment.  */
  def shm_unlink (name: CString): CInt = extern
}
