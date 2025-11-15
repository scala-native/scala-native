package java.lang.process

import java.{util => ju}

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.bsd
import scala.scalanative.posix._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import ju.concurrent.TimeUnit

private[process] object ProcessExitCheckerFreeBSD
    extends ProcessExitChecker.Factory {

  import bsd.kevent._

  private val keventSize = scalanative_kevent_size()

  override def createSingle(pid: CInt)(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker =
    new Single(pid)

  // XXX: this watcher is NOT thread-safe, must be used from single thread
  override def createMulti(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker.Multi = new Multi

  /* kqueue descriptor may not be persisted; if multiple threads called
   * `waitFor` simultaneously, there would be multiple concurrent calls to
   * `kevent` on the same descriptor, and the kernel would not necessarily
   * unblock all of them. */
  private class Single(pid: Int)(implicit pr: ProcessRegistry)
      extends ProcessExitChecker {
    override def close(): Unit = {}

    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean = {
      val kq = UnixProcess.throwOnErrorRetryEINTR(kqueue(), "kqueue failed")

      try {
        val addBuf = stackalloc[Byte](keventSize)
        keventAddPid(pid, addBuf)

        val getNum = 8 // 1 should be enough, really
        val getBuf = stackalloc[Byte](getNum.toCSize * keventSize)

        keventWith(kq, addBuf, 1, getBuf, getNum, timeout, unitOpt)(
          "kevent failed"
        )
      } finally {
        unistd.close(kq)
      }
    }

  }

  private class Multi(implicit
      pr: ProcessRegistry
  ) extends ProcessExitChecker.Multi {

    private val _kq = UnixFileDescriptorAtomic(
      UnixProcess.throwOnErrorRetryEINTR(kqueue(), "kqueue failed")
    )

    @alwaysinline private def kq: Int = _kq.get()

    private val (_rnotify, _wnotify) = {
      val pipe = stackalloc[CInt](2)
      UnixProcess.throwOnError(unistd.pipe(pipe), "notify pipe failed")

      val rnotify = !(pipe + 0)
      val wnotify = !(pipe + 1)
      UnixProcess.throwOnErrorRetryEINTR(
        fcntl.fcntl(rnotify, fcntl.F_SETFL, fcntl.O_NONBLOCK),
        "failed to set NONBLOCK on notify pull pipe"
      )
      UnixProcess.throwOnErrorRetryEINTR(
        fcntl.fcntl(wnotify, fcntl.F_SETFL, fcntl.O_NONBLOCK),
        "failed to set NONBLOCK on notify push pipe"
      )

      val kev = stackalloc[Byte](keventSize)
      keventAdd(fd = rnotify, filter = EVFILT_READ, flags = 0, fflags = 0)(kev)
      UnixProcess.throwOnErrorRetryEINTR(
        kevent(kq, kev, 1, null, 0, null),
        s"kevent register rnotify=$rnotify failed"
      )

      (UnixFileDescriptorAtomic(rnotify), UnixFileDescriptorAtomic(wnotify))
    }

    private val maxEvents = 1024
    private val eventsBuf: Ptr[Byte] = UnixProcess.throwIfNull(
      stdlib.calloc(maxEvents.toCSize, keventSize),
      "failed to allocate kevent read buffer"
    )

    override def close(): Unit = {
      _kq.close()
      _wnotify.close()
      _rnotify.close()
      stdlib.free(eventsBuf)
    }

    override def addOrReap(handle: GenericProcessHandle): Boolean = {
      val pid = handle.pid()
      val kev = stackalloc[Byte](keventSize)
      keventAddPid(pid, kev)
      val res =
        UnixProcess.throwOnErrorRetryEINTR { e =>
          if (e == errno.ESRCH) {
            reapWhenNotRunning(pid.toInt)
            false
          } else
            e != errno.EBADF
        }(
          kevent(kq, kev, 1, null, 0, null),
          s"kevent register pid=$pid failed"
        )

      res >= 0 && {
        // wake up kevent to wait on updated queue
        pushNotifyEvent(pid, _wnotify.get())
        true
      }
    }

    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean =
      keventWith(kq, null, 0, eventsBuf, maxEvents, timeout, unitOpt)(
        "kevent wait failed"
      )

  }

  private def keventAdd(fd: Long, filter: CInt, flags: CInt, fflags: CInt)(
      kev: Ptr[Byte]
  ): Unit = scalanative_kevent_set(
    kev,
    idx = 0,
    ident = fd.toUSize,
    filter = filter.toShort,
    flags = (EV_ADD | flags).toUShort,
    fflags = fflags.toUInt,
    data = 0,
    udata = null
  )

  private def keventAddPid(pid: Long, kev: Ptr[Byte]): Unit = {
    /* event will eventually be deleted when child pid closes.
     * EV_DISPATCH hints that the event can be deleted immediately after
     * delivery. */
    keventAdd(
      fd = pid,
      filter = EVFILT_PROC,
      flags = EV_DISPATCH, // deregisters upon delivery
      fflags = NOTE_EXIT | NOTE_EXITSTATUS
    )(kev)
  }

  private def keventWith(
      kq: Int,
      eventsAdd: Ptr[Byte],
      eventsAddNum: Int,
      eventsGet: Ptr[Byte],
      eventsGetMax: Int,
      timeout: Long,
      unitOpt: Option[TimeUnit]
  )(errmsg: => String)(implicit pr: ProcessRegistry): Boolean = {
    val deadlineNanos = unitOpt.map { unit =>
      System.nanoTime() + Math.max(0, unit.toNanos(timeout))
    }
    keventWith(kq, eventsAdd, eventsAddNum, eventsGet, eventsGetMax)(
      errmsg,
      deadlineNanos
    )
  }

  @tailrec
  private def keventWith(
      kq: Int,
      eventsAdd: Ptr[Byte],
      eventsAddNum: Int,
      eventsGet: Ptr[Byte],
      eventsGetMax: Int
  )(errmsg: => String, deadlineNanos: Option[Long])(implicit
      pr: ProcessRegistry
  ): Boolean = {
    val ts = stackalloc[time.timespec]()
    val tsOrNull = deadlineNanos.map { nanos =>
      UnixProcess.nanosToTimespec(nanos - System.nanoTime(), ts)
      ts
    }.orNull

    val eventsGetNum = UnixProcess.throwOnErrorRetryEINTR(_ != errno.ESRCH)(
      kevent(kq, eventsAdd, eventsAddNum, eventsGet, eventsGetMax, tsOrNull),
      errmsg
    )

    var reaped = false
    var idx = 0
    val ident = stackalloc[stdint.uintptr_t]()
    val filter = stackalloc[stdint.int16_t]()
    val fflags = stackalloc[stdint.uint32_t]()
    while (idx < eventsGetNum) {
      scalanative_kevent_get(
        eventsGet,
        idx,
        ident = ident,
        filter = filter,
        flags = null,
        fflags = fflags,
        data = null,
        udata = null
      )
      if (!filter == EVFILT_PROC) {
        // stored elem.data for NOTE_EXITSTATUS is unreliable on the Mac
        if (((!fflags).toInt & (NOTE_EXIT | NOTE_EXITSTATUS)) != 0)
          if (reapWhenNotRunning((!ident).toInt)) reaped = true
      } else if (!filter == EVFILT_READ)
        pullNotifyEvents((!ident).toInt) // numBytes: elem.data.toInt
      idx += 1
    }

    reaped || eventsGetNum > 0 &&
      keventWith(kq, eventsAdd, eventsAddNum, eventsGet, eventsGetMax)(
        errmsg,
        deadlineNanos
      )
  }

  private def pullNotifyEvents(rnotify: CInt): Unit = {
    // handle notifications on the pipe: drain the pipe
    val buf = stackalloc[Byte](64)
    while ({
      0 < UnixProcess.throwOnErrorRetryEINTR(_ != errno.EWOULDBLOCK)(
        unistd.read(rnotify, buf, 64.toUSize),
        "kevent notify pull failed"
      )
    }) {} // rnotify is non-blocking
  }

  private def pushNotifyEvent(pid: Long, wnotify: CInt): Unit = {
    val buf = stackalloc[Byte]()
    !buf = 1.toByte
    UnixProcess.throwOnErrorRetryEINTR(_ != errno.EWOULDBLOCK)(
      unistd.write(wnotify, buf, 1.toUSize),
      s"kevent notify push [pid=$pid] failed"
    )
  }

  /* Explicitly allow HANG in "options".
   * macOS appears to allow a tiny (millisecond?) delay between when
   * kevent reports a child exit transition and when waitpid() on that
   * process reports the child as exited.  This delay is not seen on Linux.
   *
     * The alternative to allowing HANG on a process which kevent/ppoll has
   * just reported as having exited is a fussy busy-wait timing loop.
   */
  private def reapWhenNotRunning(pid: Int)(implicit pr: ProcessRegistry) =
    UnixProcess.waitpidAndComplete(pid, hang = true)

}
