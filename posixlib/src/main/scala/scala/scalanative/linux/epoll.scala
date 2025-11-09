package scala.scalanative
package linux

/*
 * https://man7.org/linux/man-pages/man2/epoll_wait.2.html
 * https://man7.org/linux/man-pages/man2/epoll_create1.2.html
 */

import posix._
import unsafe._

@extern
@define("__SCALANATIVE_POSIX_EPOLL")
object epoll {

  import stdint._

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

  def epoll_create(size: CInt): CInt = extern
  def epoll_create1(flags: CInt): CInt = extern

  def epoll_ctl(
      epfd: CInt,
      op: CInt,
      fd: CInt,
      event: CVoidPtr
  ): CInt = extern

  @blocking
  def epoll_wait(
      epfd: CInt,
      events: CVoidPtr,
      maxevents: CInt,
      timeoutMillis: CInt
  ): CInt = extern

  def scalanative_epoll_event_size(): CSize = extern

  def scalanative_epoll_event_set(
      ev: CVoidPtr,
      idx: CInt,
      events: uint32_t,
      data: uint64_t
  ): Unit = extern

  def scalanative_epoll_event_get(
      ev: CVoidPtr,
      idx: CInt,
      events: Ptr[uint32_t],
      data: Ptr[uint64_t]
  ): Unit = extern

}
