package scala.scalanative.posix.sys

import scalanative.native._
import scalanative.native.Nat.{Digit, _1, _6}
import scalanative.posix.inttypes._
import scalanative.posix.time._

@extern
object select {

  // posix requires this file declares suseconds_t. Use single point of truth.

  type suseconds_t = types.suseconds_t

  // The declaration of type fd_set closely follows the Linux C declaration.

  // 16 * 64 == 1024 == FD_SETSIZE.
  // Assured by assertion() in nativelib select.c. See comments there.

  private[this] type _16 = Digit[_1, _6]

  type fd_set = CStruct1[CArray[CLongInt, _16]]

  // Allocation & usage example:
  //     val fdsetPtr = stackalloc[fd_set].asInstanceOf[Ptr[fd_set]]
  //     FD_ZERO(fdsetPtr)

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
