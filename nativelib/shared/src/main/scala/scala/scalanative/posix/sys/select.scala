package scala.scalanative.posix.sys

import scalanative.native._, Nat._
import scalanative.posix.inttypes._
import scalanative.posix.time._

@extern
object select {
  type suseconds_t = CLongInt

  type fd_set = CStruct1[Ptr[CLongInt]]

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

object selectOps {
  import select._

  implicit class timevalOps(val ptr: Ptr[time.timeval]) extends AnyVal {
    def tv_sec: time_t       = !(ptr._1)
    def tv_usec: suseconds_t = !(ptr._2)

    def tv_sec_=(v: time_t): Unit       = !ptr._1 = v
    def tv_usec_=(v: suseconds_t): Unit = !ptr._2 = v
  }

}
