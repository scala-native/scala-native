package java.lang.process

import java.util.concurrent.TimeUnit

import scala.util.Try

import scala.scalanative.posix.errno._
import scala.scalanative.posix.{fcntl, stdlib, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[process] object EventWatcherLinux extends EventWatcher.Factory {
  override def create(): EventWatcher = new EventWatcherLinux
}

private[process] class EventWatcherLinux extends EventWatcher {

  import LinuxOsSpecific.Extern._
  import LinuxOsSpecific._

  private val _epollfd = UnixProcess.throwOnError(
    epoll_create1(fcntl.O_CLOEXEC),
    "epoll_create failed"
  )

  private val _eventfd = {
    val fd = UnixProcess.throwOnError(
      eventfd(0.toUInt, fcntl.O_CLOEXEC | fcntl.O_NONBLOCK),
      "eventfd failed"
    )
    add(fd, EPOLLIN.toUInt, 0)
    fd
  }

  private val maxEvents = 1024
  private val eventsBuf: Ptr[epoll_event] = UnixProcess.throwIfNull(
    stdlib
      .malloc(sizeOf[epoll_event] * maxEvents)
      .asInstanceOf[Ptr[epoll_event]],
    "failed to allocate epoll read buffer"
  )

  override def close(): Unit = {
    unistd.close(_eventfd)
    unistd.close(_epollfd)
  }

  override def add(pid: Long): Unit = {
    /* event will eventually be deleted when child pid closes.
     * EV_DISPATCH hints that the event can be deleted immediately after
     * delivery.
     */
    val fd = UnixProcess.throwOnError(
      pidfd_open(pid.toInt, 0.toUInt),
      s"pidfd_open($pid) failed"
    )

    add(fd, EPOLLIN.toUInt, pid)

    // wake up epoll to wait on updated queue
    pushNotifyEvent(pid)
  }

  override def waitAndReapSome(
      timeout: Long,
      unitOpt: Option[TimeUnit]
  )(pr: ProcessRegistry): Boolean = {
    val timeoutMillis = unitOpt.fold(-1) { unit =>
      Math.clamp(unit.toMillis(timeout), 0, Int.MaxValue)
    }

    val res = UnixProcess.throwOnErrorRetryEINTR(
      epoll_wait(_epollfd, eventsBuf, maxEvents, timeoutMillis),
      s"wait epoll_wait failed"
    )

    var idx = 0
    while (idx < res) {
      val elem = eventsBuf + idx
      idx += 1
      val u64 = elem.data.u64.toLong
      val fd = (u64 & 0xffffffffL).toInt
      def reap(): Unit = {
        val pid = (u64 >> 32) & 0xffffffffL
        pr.complete(pid)
        del(fd)
        unistd.close(fd)
      }
      Try {
        if (fd == _eventfd)
          pullNotifyEvents()
        else
          reap()
      }
    }

    res > 0
  }

  private def add(
      fd: CInt,
      events: UInt,
      pid: Long
  ): Unit = {
    val u64 = ((pid & 0xffffffffL) << 32) | (fd & 0xffffffff)

    val kev = stackalloc[epoll_event]()
    kev.events = events
    kev.data.u64 = u64.toULong

    UnixProcess.throwOnError(
      epoll_ctl(_epollfd, EPOLL_CTL_ADD, fd, kev),
      s"epoll_ctl register [fd=$fd] failed"
    )
  }

  private def del(fd: CInt): Unit = {
    val kev = stackalloc[epoll_event]()
    epoll_ctl(_epollfd, EPOLL_CTL_DEL, fd, kev)
  }

  private def pullNotifyEvents(): Unit = {
    val buf = stackalloc[Byte](8)
    UnixProcess.throwOnErrorRetryEINTR(e => e != EAGAIN && e != EWOULDBLOCK)(
      unistd.read(_eventfd, buf, 8.toUSize),
      "epoll notify pull failed"
    )
  }

  private def pushNotifyEvent(pid: Long): Unit = {
    val buf = stackalloc[Long]() // XXX: assumes Long is 8 bytes
    !buf = 1L
    UnixProcess.throwOnErrorRetryEINTR(e => e != EAGAIN && e != EWOULDBLOCK)(
      unistd.write(_eventfd, buf, 8.toUSize),
      s"epoll notify push [pid=$pid] failed"
    )
  }

}
