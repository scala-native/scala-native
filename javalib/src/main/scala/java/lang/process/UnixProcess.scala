package java.lang.process

import java.lang.ProcessBuilder.Redirect
import java.io.{File, IOException, InputStream, OutputStream}
import java.io.FileDescriptor
import java.util.concurrent.TimeUnit
import java.util.ScalaOps._

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc.{errno => err, signal => sig}
import err.errno
import scala.scalanative.posix.{fcntl, signal, sys, time, unistd, errno => e}
import signal.{kill, SIGKILL}
import time._
import sys.time._

private[lang] class UnixProcess private (
    pid: CInt,
    builder: ProcessBuilder,
    infds: Ptr[CInt],
    outfds: Ptr[CInt],
    errfds: Ptr[CInt]
) extends GenericProcess {
  override def destroy(): Unit = kill(pid, sig.SIGTERM)

  override def destroyForcibly(): Process = {
    kill(pid, SIGKILL)
    this
  }

  override def exitValue(): scala.Int = {
    checkResult() match {
      case -1 =>
        throw new IllegalThreadStateException(
          s"Process $pid has not exited yet"
        )
      case v => v
    }
  }

  override def getErrorStream(): InputStream = _errorStream

  override def getInputStream(): InputStream = _inputStream

  override def getOutputStream(): OutputStream = _outputStream

  override def isAlive(): scala.Boolean = checkResult() == -1

  override def toString = s"UnixProcess($pid)"

  override def waitFor(): scala.Int = {
    checkResult() match {
      case -1 =>
        waitImpl(() => waitFor(null))
        _exitValue
      case v => v
    }
  }
  override def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean =
    checkResult() match {
      case -1 =>
        val ts = stackalloc[timespec]()
        val tv = stackalloc[timeval]()
        UnixProcess.throwOnError(
          gettimeofday(tv, null),
          "Failed to set time of day."
        )
        val nsec =
          unit.toNanos(timeout) + TimeUnit.MICROSECONDS.toNanos(tv._2.toLong)
        val sec = TimeUnit.NANOSECONDS.toSeconds(nsec)
        ts._1 = tv._1 + sec.toSize
        ts._2 =
          (if (sec > 0) nsec - TimeUnit.SECONDS.toNanos(sec) else nsec).toSize
        waitImpl(() => waitFor(ts)) == 0
      case _ => true
    }

  @inline private def waitImpl(f: () => Int): Int = {
    var res = 1
    while ({
      res = f()
      res match {
        case 0   => _exitValue == -1
        case res => res != e.ETIMEDOUT
      }
    }) ()
    res
  }

  private[this] val _inputStream =
    PipeIO[PipeIO.Stream](
      this,
      new FileDescriptor(!outfds),
      builder.redirectOutput()
    )
  private[this] val _errorStream =
    PipeIO[PipeIO.Stream](
      this,
      new FileDescriptor(!errfds),
      builder.redirectError()
    )
  private[this] val _outputStream =
    PipeIO[OutputStream](
      this,
      new FileDescriptor(!(infds + 1)),
      builder.redirectInput()
    )

  private[this] var _exitValue = -1
  private[lang] def checkResult(): CInt = {
    if (_exitValue == -1) setExitValue(UnixProcess.checkResult(pid))
    _exitValue
  }
  private[this] def setExitValue(value: CInt): Unit = {
    if (_exitValue == -1 && value != -1) {
      _exitValue = value
      _inputStream.drain()
      _errorStream.drain()
      _outputStream.close()
    }
  }
  private[this] def waitFor(ts: Ptr[timespec]): Int = {
    val res = stackalloc[CInt]()
    !res = -1
    val result = UnixProcess.waitForPid(pid, ts, res)
    setExitValue(!res)
    result
  }
}

object UnixProcess {
  @link("pthread")
  @extern
  private[this] object ProcessMonitor {
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

  private def checkResult(pid: Int): CInt = ProcessMonitor.checkResult(pid)
  private def waitForPid(pid: Int, ts: Ptr[timespec], res: Ptr[CInt]): CInt =
    ProcessMonitor.waitForPid(pid, ts, res)
  def apply(builder: ProcessBuilder): Process = Zone { implicit z =>
    val infds: Ptr[CInt] = stackalloc[CInt](2.toUInt)
    val outfds: Ptr[CInt] = stackalloc[CInt](2.toUInt)
    val errfds =
      if (builder.redirectErrorStream()) outfds else stackalloc[CInt](2.toUInt)

    throwOnError(unistd.pipe(infds), s"Couldn't create pipe.")
    throwOnError(unistd.pipe(outfds), s"Couldn't create pipe.")
    if (!builder.redirectErrorStream())
      throwOnError(unistd.pipe(errfds), s"Couldn't create pipe.")
    val cmd = builder.command().scalaOps.toSeq
    val binaries = binaryPaths(builder.environment(), cmd.head)
    val dir = builder.directory()
    val argv = nullTerminate(cmd)
    val envp = nullTerminate {
      builder
        .environment()
        .entrySet()
        .scalaOps
        .toSeq
        .map(e => s"${e.getKey()}=${e.getValue()}")
    }

    unistd.fork() match {
      case -1 =>
        throw new IOException("Unable to fork process")

      case 0 =>
        if (dir != null)
          unistd.chdir(toCString(dir.toString))

        setupChildFDS(!infds, builder.redirectInput(), unistd.STDIN_FILENO)
        setupChildFDS(
          !(outfds + 1),
          builder.redirectOutput(),
          unistd.STDOUT_FILENO
        )
        setupChildFDS(
          !(errfds + 1),
          if (builder.redirectErrorStream()) Redirect.PIPE
          else builder.redirectError(),
          unistd.STDERR_FILENO
        )
        unistd.close(!infds)
        unistd.close(!(infds + 1))
        unistd.close(!outfds)
        unistd.close(!(outfds + 1))
        unistd.close(!errfds)
        unistd.close(!(errfds + 1))

        binaries.foreach { b =>
          val bin = toCString(b)
          if (unistd.execve(bin, argv, envp) == -1 && errno == e.ENOEXEC) {
            val newArgv = nullTerminate(Seq("/bin/sh", "-c", b))
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
        Seq(!(outfds + 1), !(errfds + 1), !infds) foreach unistd.close
        new UnixProcess(pid, builder, infds, outfds, errfds)
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
      seq: collection.Seq[String]
  )(implicit z: Zone) = {
    val res: Ptr[CString] = alloc[CString]((seq.size + 1).toUInt)
    seq.zipWithIndex foreach { case (s, i) => !(res + i) = toCString(s) }
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
      case ProcessBuilder.Redirect.Type.PIPE =>
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

  @inline def open(f: File, flags: CInt) = Zone { implicit z =>
    fcntl.open(toCString(f.getAbsolutePath()), flags, 0.toUInt) match {
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
    if ((bin startsWith "/") || (bin startsWith ".")) {
      Seq(bin)
    } else {
      val path = environment get "PATH" match {
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
