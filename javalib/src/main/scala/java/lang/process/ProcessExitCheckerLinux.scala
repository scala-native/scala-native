package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.linux
import scala.scalanative.posix._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[process] object ProcessExitCheckerLinux
    extends ProcessExitChecker.Factory {

  import linux.epoll._
  import linux.pidfd._
  import linux.ppoll._

  override def createSingle(pid: Int)(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker =
    getPidFd(pid).fold {
      reapWhenNotRunning(pid)
      ProcessExitCheckerWaitpid.createSingle(pid)
    }(fd => new Single(pid, UnixFileDescriptorAtomic(fd)))

  // XXX: this watcher is NOT thread-safe, must be used from single thread
  override def createMulti(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker.Multi = new Multi

  private def getPidFd(pid: Int): Option[CInt] = {
    val pidfd = UnixProcess.throwOnError(_ != errno.ESRCH)(
      pidfd_open(pid, 0.toUInt),
      s"pidfd_open($pid) failed"
    )
    if (pidfd < 0) None else Some(pidfd)
  }

  /* We use ppoll here as an epoll descriptor may not be persisted; if multiple
   * threads called `waitFor` simultaneously, there would be multiple concurrent
   * calls to `epoll_wait` on the same descriptor, and the kernel would not
   * necessarily unblock all of them. */
  private class Single(pid: Int, pidfd: UnixFileDescriptorAtomic)(implicit
      pr: ProcessRegistry
  ) extends ProcessExitChecker {

    override def close(): Unit = pidfd.close()

    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean = {
      import poll._
      import pollOps._

      val curpidfd = pidfd.get()
      curpidfd != -1 && {
        val ppollfd = stackalloc[struct_pollfd](1)
        ppollfd.fd = curpidfd
        ppollfd.events = (pollEvents.POLLIN | pollEvents.POLLRDNORM).toShort

        val ts = stackalloc[time.timespec]()
        val tsOrNull = unitOpt.map { unit =>
          UnixProcess.toTimespec(timeout, unit, ts)
          ts
        }.orNull

        val res = UnixProcess.throwOnErrorRetryEINTR(_ != errno.EBADF)(
          ppoll(ppollfd, 1.toUSize, tsOrNull, null),
          s"ppoll failed for pid=$pid"
        )

        if (res > 0) {
          reapWhenNotRunning(pid)
          close()
        }

        res != 0
      }
    }
  }

  private val maxEvents = 1024
  private val epollEventSize: CSize = scalanative_epoll_event_size()

  private class Multi(implicit
      pr: ProcessRegistry
  ) extends ProcessExitChecker.Multi {

    private val _epollfd = UnixFileDescriptorAtomic(
      UnixProcess.throwOnError(
        epoll_create1(EPOLL_CLOEXEC),
        "epoll_create failed"
      )
    )

    @alwaysinline private def epollfd: Int = _epollfd.get()

    @alwaysinline private def closeWithEpollFD(): Unit = _epollfd.close()

    private val eventsBuf: Ptr[Byte] = {
      val buf = stdlib.calloc(maxEvents.toCSize, epollEventSize)
      if (buf eq null) {
        closeWithEpollFD()
        UnixProcess.throwWith(buf, "failed to allocate epoll read buffer")
      }
      buf
    }

    override def close(): Unit = {
      closeWithEpollFD()
      stdlib.free(eventsBuf)
    }

    /** very important to delete from the epoll set the structure which is
     *  currently referred to by pidfd; if we had closed pidfd first, the
     *  structure would have remained registered forever.
     *
     *  on the other hand, it's also very important to close pidfd only once as
     *  otherwise the same file descriptor is likely to be re-used, and closing
     *  it leads to closing an unrelated resource. therefore, call this method
     *  immediately after epoll_wait returns it.
     */
    private def closePidFd(pidfd: CInt): Unit = {
      val res = epoll_ctl(epollfd, EPOLL_CTL_DEL, pidfd, null)
      unistd.close(pidfd)
      UnixProcess.throwOnError { e =>
        e != errno.EBADF && e != errno.ENOENT
      }(res, s"epoll_ctl failed to delete: $pidfd")
    }

    override def addOrReap(handle: GenericProcessHandle): Boolean = {
      val pid = handle.pid().toInt
      getPidFd(pid).exists { pidfd =>
        0 <= UnixProcess.throwOnError(
          add(pidfd = pidfd, events = EPOLLIN | EPOLLHUP, pid = pid),
          s"epoll_ctl register [fd=$pidfd] failed"
        ) || { unistd.close(pidfd); false }
      } || { reapWhenNotRunning(pid); false }
    }

    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean = {
      val timeoutMillis = unitOpt.fold(-1) { unit =>
        Math.clamp(unit.toMillis(timeout), 0, Int.MaxValue)
      }
      val res = UnixProcess.retryEINTR(
        epoll_wait(epollfd, eventsBuf, maxEvents, timeoutMillis)
      )

      var idx = 0
      val ptrEvent = stackalloc[stdint.uint32_t]()
      val ptrData = stackalloc[stdint.uint64_t]()
      while (idx < res) {
        scalanative_epoll_event_get(eventsBuf, idx, ptrEvent, ptrData)
        idx += 1
        val (fd, pid) = from64((!ptrData).toLong)
        reapWhenNotRunning(pid)
        closePidFd(fd)
      }

      res > 0
    }

    private def add(
        pidfd: CInt,
        events: Int,
        pid: Int
    ): CInt = {
      val kev = stackalloc[Byte](epollEventSize)
      scalanative_epoll_event_set(
        kev,
        0,
        events.toUInt,
        to64(fd = pidfd, pid = pid).toULong
      )
      epoll_ctl(epollfd, EPOLL_CTL_ADD, pidfd, kev)
    }

  }

  // fd high 32, pid low 32
  @alwaysinline private def to64(fd: CInt, pid: Int): Long =
    (Integer.toUnsignedLong(fd) << 32) | Integer.toUnsignedLong(pid)

  // fd first, pid second
  @alwaysinline private def from64(i64: Long): (CInt, Int) =
    ((i64 >>> 32).toInt, i64.toInt)

  @alwaysinline
  private def reapWhenNotRunning(pid: Int)(implicit pr: ProcessRegistry) =
    UnixProcess.waitpidAndComplete(pid, hang = false)

}
