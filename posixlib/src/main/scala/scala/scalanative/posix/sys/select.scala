package scala.scalanative.posix.sys

import scalanative.unsafe._
import scalanative.unsafe.Nat._
import scalanative.posix.inttypes._
import scalanative.posix.time._

@extern
object select {

  // posix requires this file declares suseconds_t. Use single point of truth.

  type suseconds_t = types.suseconds_t

  // The declaration of type fd_set closely follows the Linux C declaration.
  // glibc circa March 2019 and many years prior is documented as using a
  // fixed buffer of 1024 bits.
  //
  // Since "extern object may only contain extern fields and methods"
  // a runtime check can not be done here. Instead, a compile time check
  // is done in posix/sys/select.c. That detects mismatches on the
  // compilation system, but may miss mismatches on the executing system.
  //
  // The more recent posix poll() avoids issues with FD_SETSIZE and offers
  // performance advantages. However, sometimes ya just gotta select().

  // Linux specifies an array of 64 bit longs.
  // 16 * 64 == 1024 == FD_SETSIZE.
  private[this] type _16 = Digit2[_1, _6]

  type fd_set = CStruct1[CArray[CLongInt, _16]]

  // Allocation & usage example:
  //
  // An fd_set is arguably too large to allocate on the stack, so use a Zone.
  //
  //    import scalanative.unsafe.{Zone, alloc}
  //
  //    Zone {
  //
  //        // Zone.alloc is documented as returning zeroed memory.
  //        val fdsetPtr = alloc[fd_set] //  No need to FD_ZERO.
  //        FD_SET(sock, fdsetPtr)
  //
  //        // If used, allocate writefds and/or exceptfds the same way.
  //
  //        val result = select(nfds, fdsetPtr, writefds, exceptfds)
  //        // check result.
  //        // do work implied by result.
  //
  //    } // fdsetPtr and memory it points to are not valid outsize of Zone.

  @name("scalanative_select")
  def select(nfds: CInt,
             readfds: Ptr[fd_set],
             writefds: Ptr[fd_set],
             exceptfds: Ptr[fd_set],
             timeout: Ptr[time.timeval]): CInt = extern

  @name("scalanative_FD_SETSIZE")
  def FD_SETSIZE: CInt = extern

  @name("scalanative_FD_CLR")
  def FD_CLR(fd: CInt, set: Ptr[fd_set]): Unit = extern

  @name("scalanative_FD_ISSET")
  def FD_ISSET(fd: CInt, set: Ptr[fd_set]): CInt = extern

  @name("scalanative_FD_SET")
  def FD_SET(fd: CInt, set: Ptr[fd_set]): Unit = extern

  @name("scalanative_FD_ZERO")
  def FD_ZERO(set: Ptr[fd_set]): Unit = extern

}
