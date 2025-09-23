package java.lang.process

import java.io.{File, IOException}
import java.util.concurrent.TimeUnit
import java.util.ScalaOps._
import java.util.ArrayList

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc.{errno => err, signal => sig}
import err.errno
import scala.scalanative.posix.{fcntl, signal, sys, time, unistd, errno => e}
import signal.{kill, SIGKILL}
import time._
import sys.time._

private[process] class UnixProcessGen1 private (
    override protected val _pid: CInt,
    override protected val builder: ProcessBuilder,
    override protected val infds: Ptr[CInt],
    override protected val outfds: Ptr[CInt],
    override protected val errfds: Ptr[CInt]
) extends UnixProcess() {
  override def destroy(): Unit = kill(_pid, sig.SIGTERM)

  override def destroyForcibly(): Process = {
    kill(_pid, SIGKILL)
    this
  }

  override def toString = s"UnixProcess(${_pid})"

  override def waitFor(): Int = {
    hasExited || osWaitForImpl(null)
    exitValue()
  }

  override def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean =
    hasExited || {
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
      ts._2 =
        (if (sec > 0) nsec - TimeUnit.SECONDS.toNanos(sec) else nsec).toSize
      osWaitForImpl(ts)
    }

  override protected def getExitCodeImpl: Option[Int] = {
    val res = UnixProcessGen1.ProcessMonitor.checkResult(_pid)
    if (res == -1) None else Some(res)
  }

  private def osWaitForImpl(ts: Ptr[timespec]): Boolean = synchronized {
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
  private object ProcessMonitor {
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
    val infds: Ptr[CInt] = stackalloc[CInt](2)
    val outfds: Ptr[CInt] = stackalloc[CInt](2)
    val errfds =
      if (builder.redirectErrorStream()) outfds else stackalloc[CInt](2)

    throwOnError(unistd.pipe(infds), s"Couldn't create pipe.")
    throwOnError(unistd.pipe(outfds), s"Couldn't create pipe.")
    if (!builder.redirectErrorStream())
      throwOnError(unistd.pipe(errfds), s"Couldn't create pipe.")
    val cmd = builder.command()
    val binaries = binaryPaths(builder.environment(), cmd.get(0))
    val dir = builder.directory()
    val argv = nullTerminate(cmd)
    val envp = nullTerminate {
      val list = new ArrayList[String]
      builder
        .environment()
        .entrySet()
        .iterator()
        .scalaOps
        .foreach(e => list.add(s"${e.getKey()}=${e.getValue()}"))
      list
    }

    unistd.fork() match {
      case -1 =>
        throw new IOException("Unable to fork process")

      case 0 =>
        if ((dir != null) && (dir.toString != "."))
          unistd.chdir(toCString(dir.toString))

        setupChildFDS(!infds, builder.redirectInput(), unistd.STDIN_FILENO)
        setupChildFDS(
          !(outfds + 1),
          builder.redirectOutput(),
          unistd.STDOUT_FILENO
        )
        setupChildFDS(
          !(errfds + 1),
          if (builder.redirectErrorStream()) ProcessBuilder.Redirect.PIPE
          else builder.redirectError(),
          unistd.STDERR_FILENO
        )

        val parentFds = new ArrayList[CInt] // No Scala Collections in javalib
        parentFds.add(!(infds + 1)) // parent's stdout - write, in child
        parentFds.add(!outfds) // parent's stdin - read, in child
        if (!builder.redirectErrorStream())
          parentFds.add(!errfds) // parent's stderr - read, in child

        parentFds.forEach { fd => unistd.close(fd) }

        binaries.foreach { b =>
          val bin = toCString(b)
          if (unistd.execve(bin, argv, envp) == -1 && errno == e.ENOEXEC) {
            val al = new ArrayList[String](3)
            al.add("/bin/sh"); al.add("-c");
            al.add(cmd.scalaOps.mkString(sep = " "))
            val newArgv = nullTerminate(al)
            unistd.execve(c"/bin/sh", newArgv, envp)
          }
        }

        /* execve failed. FreeBSD "man" recommends fast exit.
         * Linux says nada.
         * Code 127 is "Command not found", the convention for exec failure.
         */
        unistd._exit(127)
        throw new IOException(s"Failed to create process for command: $cmd")

      case pid =>
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

        val childFds = new ArrayList[CInt] // No Scala Collections in javalib
        childFds.add(!infds) // child's stdin read, in parent
        childFds.add(!(outfds + 1)) // child's stdout write, in parent
        if (!builder.redirectErrorStream())
          childFds.add(!(errfds + 1)) // child's stderr write, in parent

        childFds.forEach { fd => unistd.close(fd) }

        new UnixProcessGen1(pid, builder, infds, outfds, errfds)
    }
  }

  @inline
  private def throwOnError(rc: CInt, msg: => String): CInt = {
    if (rc != 0) {
      throw new IOException(s"$msg Error code: $rc, Error number: $errno")
    } else {
      rc
    }
  }

  @inline private def nullTerminate(
      list: java.util.List[String]
  )(implicit z: Zone) = {
    val res: Ptr[CString] = alloc[CString]((list.size() + 1))
    val li = list.listIterator()
    while (li.hasNext()) {
      !(res + li.nextIndex()) = toCString(li.next())
    }
    res
  }

  @inline private def setupChildFDS(
      childFd: CInt,
      redirect: ProcessBuilder.Redirect,
      procFd: CInt
  ): Unit = {
    import fcntl.{open => _, _}
    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.INHERIT =>
      case ProcessBuilder.Redirect.Type.PIPE    =>
        if (unistd.dup2(childFd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate pipe file descriptor $errno"
          )
        }
      case r @ ProcessBuilder.Redirect.Type.READ =>
        val fd = open(redirect.file(), O_RDONLY)
        if (unistd.dup2(fd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate read file descriptor $errno"
          )
        }
      case r @ ProcessBuilder.Redirect.Type.WRITE =>
        val fd = open(redirect.file(), O_CREAT | O_WRONLY | O_TRUNC)
        if (unistd.dup2(fd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate write file descriptor $errno"
          )
        }
      case r @ ProcessBuilder.Redirect.Type.APPEND =>
        val fd = open(redirect.file(), O_CREAT | O_WRONLY | O_APPEND)
        if (unistd.dup2(fd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate append file descriptor $errno"
          )
        }
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
