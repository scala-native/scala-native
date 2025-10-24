package java.lang.process

import java.io.{File, IOException}
import java.util.concurrent.TimeUnit

import scala.scalanative.libc.errno.errno
import scala.scalanative.posix.{errno => e, fcntl, sys, time}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

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
    UnixProcessGen1.throwOnError(
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

  private implicit object HandleFactory extends UnixProcessHandleFactory {
    override def create(pid: CInt, builder: ProcessBuilder): UnixProcessHandle =
      new UnixProcessHandleGen1(pid, builder)
  }

  def apply(
      builder: ProcessBuilder
  ): GenericProcess = Zone.acquire { implicit z =>
    try {
      UnixProcessGen2.forkChild(builder)
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

  @inline
  def throwOnError(rc: CInt, msg: => String): CInt = {
    if (rc != 0) {
      throw new IOException(s"$msg Error code: $rc, Error number: $errno")
    } else {
      rc
    }
  }

  @inline def open(f: File, flags: CInt) = Zone.acquire { implicit z =>
    def defaultCreateMode = 0x1a4.toUInt // 0644, no octal literal in Scala
    val mode: CUnsignedInt =
      if ((flags & fcntl.O_CREAT) != 0) defaultCreateMode
      else 0.toUInt
    fcntl.open(toCString(f.getAbsolutePath()), flags, mode) match {
      case -1 => throw new IOException(s"Unable to open file $f ($errno)")
      case fd => fd
    }
  }

  // The execvpe function isn't available on all platforms so find the
  // possible binaries to exec.
  private def binaryPaths(
      environment: java.util.Map[String, String],
      bin: String
  ): Seq[String] = {
    if ((bin.startsWith("/")) || (bin.startsWith("."))) {
      Seq(bin)
    } else {
      val path = environment.get("PATH") match {
        case null => "/bin:/usr/bin:/usr/local/bin"
        case p    => p
      }

      path
        .split(':')
        .toIndexedSeq
        .map { absPath => new File(s"$absPath/$bin") }
        .collect {
          case f if f.canExecute() => f.toString
        }
    }
  }
}
