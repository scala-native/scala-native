package java.lang.process

import java.{util => ju}

import scalanative.libc.stdlib
import scalanative.posix.errno._
import scalanative.posix.time.timespec
import scalanative.posix.timeOps.timespecOps
import scalanative.posix.{fcntl, unistd}
import scalanative.unsafe._
import scalanative.unsigned._

import ju.concurrent.TimeUnit

private[process] object EventWatcherFreeBSD extends EventWatcher.Factory {
  override def create(): EventWatcher = new EventWatcherFreeBSD
}

private[process] class EventWatcherFreeBSD extends EventWatcher {

  import BsdOsSpecific.Extern._
  import BsdOsSpecific._

  private val _kq =
    UnixProcess.throwOnErrorRetryEINTR(kqueue(), "kqueue failed")

  private val maxEvents = 1024
  private val eventsBuf: Ptr[kevent] = UnixProcess.throwIfNull(
    stdlib.malloc(sizeOf[kevent] * maxEvents).asInstanceOf[Ptr[kevent]],
    "failed to allocate kevent read buffer"
  )

  override def close(): Unit = {
    unistd.close(_kq)
    stdlib.free(eventsBuf)
  }

  override def add(pid: Long): Unit = {
    /* event will eventually be deleted when child pid closes.
     * EV_DISPATCH hints that the event can be deleted immediately after
     * delivery.
     */
    add(
      pid.toUSize,
      EVFILT_PROC,
      EV_DISPATCH, // deregisters upon delivery
      NOTE_EXIT | NOTE_EXITSTATUS
    )
  }

  override def waitAndReapSome(
      timeout: Long,
      unitOpt: Option[TimeUnit]
  )(pr: ProcessRegistry): Boolean = {
    val ts = stackalloc[timespec]()
    val tsOpt = unitOpt.map { unit =>
      val timeoutNanos = Math.max(0, unit.toNanos(timeout))
      val nanosPerSecond = 1000000000L
      val seconds = Math.floorDiv(timeoutNanos, nanosPerSecond)
      ts.tv_sec = seconds.toSize
      ts.tv_nsec = (timeoutNanos - nanosPerSecond * seconds).toSize
      ts
    }

    val res = UnixProcess.throwOnErrorRetryEINTR(
      kevent(_kq, null, 0, eventsBuf, maxEvents, tsOpt.orNull),
      "wait kevent failed"
    )

    var idx = 0
    while (idx < res) {
      val elem = eventsBuf + idx
      idx += 1
      elem.filter.toInt match {
        case EVFILT_PROC =>
          val pid = elem.ident.toInt
          if ((elem.fflags.toInt & NOTE_EXITSTATUS) != 0) {
            val ec = UnixProcess.getExitCodeFromWaitStatus(elem.data.toInt)
            pr.completeWith(pid)(ec)
          }
          if ((elem.fflags.toInt & NOTE_EXIT) != 0)
            pr.complete(pid)
        case _ =>
      }
    }

    res > 0
  }

  private def add(
      fd: USize,
      filter: CInt,
      flags: CInt,
      fflags: CInt
  ): Unit = {
    val kev = stackalloc[kevent]()
    kev.ident = fd
    kev.filter = filter.toShort
    kev.flags = (EV_ADD | flags).toUShort
    kev.fflags = fflags.toUInt

    UnixProcess.throwOnErrorRetryEINTR(
      kevent(_kq, kev, 1, null, 0, null),
      s"kevent register [fd=$fd] failed"
    )
  }

}
