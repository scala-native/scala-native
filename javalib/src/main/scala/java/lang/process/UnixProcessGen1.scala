package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.posix.{errno => e, sys, time}
import scala.scalanative.unsafe._

import sys.time._
import time._

private[process] class UnixProcessHandleGen1(
    override protected val _pid: CInt,
    override val builder: ProcessBuilder
) extends UnixProcessHandle {

  override protected def waitForImpl(): Boolean =
    osWaitForImpl(null)

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean = {
    val ts = stackalloc[timespec]()
    val tv = stackalloc[timeval]()
    UnixProcessGen2.throwOnError(
      gettimeofday(tv, null),
      "Failed to set time of day."
    )
    val nsec =
      unit.toNanos(timeout) + TimeUnit.MICROSECONDS.toNanos(tv._2.toLong)
    val sec = TimeUnit.NANOSECONDS.toSeconds(nsec)
    ts._1 = tv._1 + sec.toSize
    ts._2 = (if (sec > 0) nsec - TimeUnit.SECONDS.toNanos(sec) else nsec).toSize
    osWaitForImpl(ts)
  }

  override protected def getExitCodeImpl: Option[Int] = {
    val res = UnixProcessGen1.ProcessMonitor.checkResult(_pid)
    if (res == -1) None else Some(res)
  }

  private def osWaitForImpl(ts: Ptr[timespec]): Boolean = {
    val exitCode = stackalloc[CInt]()
    while (true) {
      !exitCode = -1
      val res = UnixProcessGen1.ProcessMonitor.waitForPid(_pid, ts, exitCode)
      if (res == 0) {
        setCachedExitCode(!exitCode)
        return true
      } else if (res == e.ETIMEDOUT) return false
    }
    false
  }
}

private[process] object UnixProcessGen1 {
  @link("pthread")
  @extern
  @define("__SCALANATIVE_JAVALIB_PROCESS_MONITOR")
  object ProcessMonitor {
    @name("scalanative_process_monitor_notify")
    def notifyMonitor(): Unit = extern
    @name("scalanative_process_monitor_check_result")
    def checkResult(pid: Int): CInt = extern
    @name("scalanative_process_monitor_init")
    def init(): Unit = extern
    @name("scalanative_process_monitor_wait_for_pid")
    def waitForPid(pid: Int, ts: Ptr[timespec], res: Ptr[CInt]): CInt = extern
  }
  ProcessMonitor.init()

  def apply(
      builder: ProcessBuilder
  ): GenericProcess = Zone.acquire { implicit z =>
    try {
      UnixProcessGen2.forkChild(builder)(new UnixProcessHandleGen1(_, _))
    } finally {
      /* Being here, we know that a child process exists, or existed.
       * ProcessMonitor needs to know about it. It is _far_ better
       * to do the notification in this parent.
       *
       * Implementations of 'fork' can be very restrictive about what
       * can run in the child before it calls one of the 'exec*' methods.
       * 'notifyMonitor' may or may not follow those rules. Even if it
       * currently does, that could easily change with future maintenance
       * make it no longer compliant, leading to shrapnel & wasted
       * developer time.
       */

      ProcessMonitor.notifyMonitor()
    }
  }

}
