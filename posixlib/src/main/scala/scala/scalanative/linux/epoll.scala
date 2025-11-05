package scala.scalanative
package linux

/*
 * https://man7.org/linux/man-pages/man2/epoll_wait.2.html
 * https://man7.org/linux/man-pages/man2/epoll_create1.2.html
 */

import unsafe._
import unsigned._

@extern
@define("__SCALANATIVE_POSIX_EPOLL")
object epoll {

  @name("scalanative_epoll_cloexec")
  def EPOLL_CLOEXEC: CInt = extern

  @name("scalanative_epoll_ctl_add")
  def EPOLL_CTL_ADD: CInt = extern
  @name("scalanative_epoll_ctl_mod")
  def EPOLL_CTL_MOD: CInt = extern
  @name("scalanative_epoll_ctl_del")
  def EPOLL_CTL_DEL: CInt = extern

  @name("scalanative_epollin")
  def EPOLLIN: CInt = extern
  @name("scalanative_epollout")
  def EPOLLOUT: CInt = extern
  @name("scalanative_epollrdhup")
  def EPOLLRDHUP: CInt = extern
  @name("scalanative_epollpri")
  def EPOLLPRI: CInt = extern
  @name("scalanative_epollerr")
  def EPOLLERR: CInt = extern
  @name("scalanative_epollhup")
  def EPOLLHUP: CInt = extern

  @name("scalanative_epollet")
  def EPOLLET: CInt = extern
  @name("scalanative_epolloneshot")
  def EPOLLONESHOT: CInt = extern

  type epoll_data_t = CStruct1[CLongLong] // 64-bit
  type epoll_event = CStruct2[UInt, epoll_data_t]

  def epoll_create(size: CInt): CInt = extern
  def epoll_create1(flags: CInt): CInt = extern

  def epoll_ctl(
      epfd: CInt,
      op: CInt,
      fd: CInt,
      event: Ptr[epoll_event]
  ): CInt = extern

  @blocking
  def epoll_wait(
      epfd: CInt,
      events: Ptr[epoll_event],
      maxevents: CInt,
      timeoutMillis: CInt
  ): CInt = extern

  // scalafmt: { align.preset = more }

  implicit class dataOps(val ref: epoll_data_t) extends AnyVal {
    def ptr = ref._1.toPtr[Byte]
    def fd  = ref._1.toInt
    def u32 = ref._1.toUInt
    def u64 = ref._1.toULong

    def ptr_=(v: CVoidPtr): Unit = ref._1 = v.toLong
    def fd_=(v: CInt): Unit      = ref._1 = v.toLong
    def u32_=(v: UInt): Unit     = ref._1 = v.toLong
    def u64_=(v: ULong): Unit    = ref._1 = v.toLong
  }

  implicit class eventOps(val ptr: Ptr[epoll_event]) extends AnyVal {
    def events = ptr._1
    def data   = ptr._2

    def events_=(v: UInt): Unit       = ptr._1 = v
    def data_=(v: epoll_data_t): Unit = ptr._2 = v
  }

  // scalafmt: { align.preset = none }

}
