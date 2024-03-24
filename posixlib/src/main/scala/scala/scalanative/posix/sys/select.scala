package scala.scalanative
package posix
package sys

import scalanative.unsafe._
import scalanative.unsafe.Nat._

/** POSIX select.h for Scala
 *
 *  @see
 *    The Open Group Base Specifications
 *    [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]]
 *    edition.
 */
@extern
@define("__SCALANATIVE_POSIX_SYS_SELECT")
object select {

  // Use single points of truth for types required by POSIX specification.

  type time_t = types.time_t
  type suseconds_t = types.suseconds_t

  type sigset_t = posix.signal.sigset_t

  type timespec = posix.time.timespec
  type timeval = sys.time.timeval

  // The declaration of type fd_set closely follows the Linux C declaration.
  // glibc, circa March 2019 and many years prior, is documented as using a
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
  private type _16 = Digit2[_1, _6]

  type fd_set = CStruct1[CArray[CLongInt, _16]]

  /* Allocation & usage example:
   *
   * An fd_set is arguably too large to allocate on the stack, so use a Zone.
   *
   *	import scalanative.unsafe.{Zone, alloc}
   *
   *	Zone {
   *	    // Zone.alloc is documented as returning zeroed memory.
   *	    val fdsetPtr = alloc[fd_set] //  No need to FD_ZERO.
   * 	    FD_SET(sock, fdsetPtr)
   *
   * 	    // If used, allocate writefds and/or exceptfds the same way.
   *
   * 	    val result = select(nfds, fdsetPtr, writefds, exceptfds, timeout)
   * 	    // check result.
   * 	    // do work implied by result.
   *
   * 	} // fdsetPtr and memory it points to are not valid outsize of Zone.
   */

  /* Declare pselect() as a direct call through to C. There no
   * @name("scalanative_pselect") is needed.
   * Guard code exists to ensure match with operating system at compile time.
   *   fd_set is guarded by code in select.c
   *   timespec is guarded by code in time.c (distinct from sys/time.c)
   */

  def pselect(
      nfds: CInt,
      readfds: Ptr[fd_set],
      writefds: Ptr[fd_set],
      exceptfds: Ptr[fd_set],
      timeout: Ptr[timespec],
      sigmask: sigset_t
  ): CInt = extern

  // select() is a excellent candidate to be changed to use direct call-thru.
  @name("scalanative_select")
  def select(
      nfds: CInt,
      readfds: Ptr[fd_set],
      writefds: Ptr[fd_set],
      exceptfds: Ptr[fd_set],
      timeout: Ptr[time.timeval]
  ): CInt = extern

  @name("scalanative_fd_setsize")
  def FD_SETSIZE: CInt = extern

  @name("scalanative_fd_clr")
  def FD_CLR(fd: CInt, set: Ptr[fd_set]): Unit = extern

  @name("scalanative_fd_isset")
  def FD_ISSET(fd: CInt, set: Ptr[fd_set]): CInt = extern

  @name("scalanative_fd_set")
  def FD_SET(fd: CInt, set: Ptr[fd_set]): Unit = extern

  @name("scalanative_fd_zero")
  def FD_ZERO(set: Ptr[fd_set]): Unit = extern

}
