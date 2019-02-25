package java
package lang

import java.io.{File, IOException, InputStream, OutputStream}
import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.scalanative.native._
import scala.scalanative.libc.{errno => err, signal => sig, _}
import sig._
import err.errno
import scala.scalanative.posix.{fcntl, pthread, sys, unistd, errno => e, time}
import time._
import sys.time._
import e.ETIMEDOUT
import UnixProcess._
import java.lang.ProcessBuilder.Redirect

import pthread._
import scala.collection.mutable
import scala.scalanative.posix.sys.types.{pthread_cond_t, pthread_mutex_t}

private[lang] class UnixProcess private (
    pid: CInt,
    builder: ProcessBuilder,
    infds: Ptr[CInt],
    outfds: Ptr[CInt],
    errfds: Ptr[CInt]
) extends Process {
  override def destroy(): Unit = kill(pid, 9)

  override def destroyForcibly(): Process = {
    destroy()
    this
  }

  override def exitValue(): scala.Int = {
    checkResult() match {
      case -1 =>
        throw new IllegalThreadStateException(
          s"Process $pid has not exited yet")
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
        val ts = stackalloc[timespec]
        val tv = stackalloc[timeval]
        throwOnError(gettimeofday(tv, null), "Failed to set time of day.")
        val nsec = unit.toNanos(timeout) + TimeUnit.MICROSECONDS.toNanos(
          !tv._2.cast[Ptr[CInt]])
        val sec = TimeUnit.NANOSECONDS.toSeconds(nsec)
        !ts._1 = !tv._1 + sec
        !ts._2 = if (sec > 0) nsec - TimeUnit.SECONDS.toNanos(sec) else nsec
        waitImpl(() => waitFor(ts)) == 0
      case _ => true
    }

  @inline private def waitImpl(f: () => Int) = {
    var res = 1
    do res = f() while (if (res == 0) _exitValue == -1 else res != ETIMEDOUT)
    res
  }

  private[this] val _inputStream =
    PipeIO[PipeIO.Stream](this, !outfds, builder.redirectOutput)
  private[this] val _errorStream =
    PipeIO[PipeIO.Stream](this, !errfds, builder.redirectError)
  private[this] val _outputStream =
    PipeIO[OutputStream](this, !(infds + 1), builder.redirectInput)

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
    val res = stackalloc[CInt]
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
    val infds  = stackalloc[CInt](2)
    val outfds = stackalloc[CInt](2)
    val errfds =
      if (builder.redirectErrorStream) outfds else stackalloc[CInt](2)

    throwOnError(unistd.pipe(infds), s"Couldn't create pipe.")
    throwOnError(unistd.pipe(outfds), s"Couldn't create pipe.")
    if (!builder.redirectErrorStream)
      throwOnError(unistd.pipe(errfds), s"Couldn't create pipe.")
    val cmd      = builder.command.asScala
    val binaries = binaryPaths(builder.environment, cmd.head)
    val dir      = builder.directory
    val argv     = nullTerminate(cmd)
    val envp = nullTerminate(builder.environment.asScala.map {
      case (k, v) => s"$k=$v"
    }.toSeq)

    /*
     * Use vfork rather than fork to avoid copying the parent process memory to the child. It also
     * ensures that the parent won't try to read or write to the child file descriptors before the
     * child process has called execve. In an ideal world, we'd use posix_spawn, but it doesn't
     * support changing the working directory of the child process or closing all of the unused
     * parent file descriptors. Using posix_spawn would require adding an additional step in which
     * we spawned a new process that called execve with a helper binary. This may be necessary
     * eventually to increase portability but, for now, just use vfork, which is suppported on
     * OSX and Linux (despite warnings about vfork's future, it seems somewhat unlikely that support
     * will be dropped soon.
     */
    unistd.vfork() match {
      case -1 =>
        throw new IOException("Unable to fork process")
      case 0 =>
        /*
         * It is unsafe to directly run any code in vfork2 on top of the parent's stack without
         * creating a new stack frame on the child. To fix this, put all of the code that needs
         * to run on the child before execve inside of a method.
         */
        def invokeChildProcess(): Process = {
          if (dir != null) unistd.chdir(toCString(dir.toString))
          setupChildFDS(!infds, builder.redirectInput, unistd.STDIN_FILENO)
          setupChildFDS(!(outfds + 1),
                        builder.redirectOutput,
                        unistd.STDOUT_FILENO)
          setupChildFDS(!(errfds + 1),
                        if (builder.redirectErrorStream) Redirect.PIPE
                        else builder.redirectError,
                        unistd.STDERR_FILENO)
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
          // The spec of vfork requires calling _exit if the child process fails to execve.
          unistd._exit(1)
          throw new IOException(s"Failed to create process for command: $cmd")
        }
        invokeChildProcess()
      case pid =>
        Seq(!(outfds + 1), !(errfds + 1), !infds) foreach unistd.close
        new UnixProcess(pid, builder, infds, outfds, errfds)
    }
  }

  @inline
  private[lang] def throwOnError(rc: CInt, msg: => String): CInt = {
    if (rc != 0) {
      throw new IOException(s"$msg Error code: $rc, Error number: $errno")
    } else {
      rc
    }
  }

  @inline private def nullTerminate(seq: Seq[String])(implicit z: Zone) = {
    val res = alloc[CString](seq.length + 1)
    seq.zipWithIndex foreach { case (s, i) => !(res + i) = toCString(s) }
    res
  }

  @inline private def setupChildFDS(childFd: CInt,
                                    redirect: ProcessBuilder.Redirect,
                                    procFd: CInt): Unit = {
    import fcntl.{open => _, _}
    redirect.`type` match {
      case ProcessBuilder.Redirect.Type.INHERIT =>
      case ProcessBuilder.Redirect.Type.PIPE =>
        if (unistd.dup2(childFd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate pipe file descriptor $errno")
        }
      case r @ ProcessBuilder.Redirect.Type.READ =>
        val fd = open(redirect.file, O_RDONLY)
        if (unistd.dup2(fd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate read file descriptor $errno")
        }
      case r @ ProcessBuilder.Redirect.Type.WRITE =>
        val fd = open(redirect.file, O_CREAT | O_WRONLY | O_TRUNC)
        if (unistd.dup2(fd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate write file descriptor $errno")
        }
      case r @ ProcessBuilder.Redirect.Type.APPEND =>
        val fd = open(redirect.file, O_CREAT | O_WRONLY | O_APPEND)
        if (unistd.dup2(fd, procFd) == -1) {
          throw new IOException(
            s"Couldn't duplicate append file descriptor $errno")
        }
    }
  }

  @inline def open(f: File, flags: CInt) = Zone { implicit z =>
    fcntl.open(toCString(f.getAbsolutePath), flags) match {
      case -1 => throw new IOException(s"Unable to open file $f ($errno)")
      case fd => fd
    }
  }

  // The execvpe function isn't available on all platforms so find the possible binaries to exec.
  private def binaryPaths(environment: java.util.Map[String, String],
                          bin: String): Seq[String] = {
    if ((bin startsWith "/") || (bin startsWith ".")) {
      Seq(bin)
    } else {
      val path = environment get "PATH" match {
        case null => "/bin:/usr/bin:/usr/local/bin"
        case p    => p
      }
      path split ":" map { absPath =>
        new File(s"$absPath/$bin")
      } collect {
        case f if f.canExecute => f.toString
      }
    }
  }
}
