package java.lang.process

import java.{util => ju}

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
    val ts = stackalloc[time.timespec]()
    val tsOrNull = unitOpt.map { unit =>
      UnixProcess.toTimespec(timeout, unit, ts)
      ts
    }.orNull

    val eventsGetNum = UnixProcess.throwOnErrorRetryEINTR(_ != errno.ESRCH)(
      kevent(kq, eventsAdd, eventsAddNum, eventsGet, eventsGetMax, tsOrNull),
      errmsg
    )

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
          reapWhenNotRunning((!ident).toInt)
      }
      idx += 1
    }

    eventsGetNum > 0
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
