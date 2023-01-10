package java.lang.process

import java.lang.ProcessBuilder.Redirect
import java.io.{File, IOException, InputStream, OutputStream}
import java.io.FileDescriptor
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
        val nsec = unit.toNanos(timeout) + TimeUnit.MICROSECONDS.toNanos(tv._2)
        val sec = TimeUnit.NANOSECONDS.toSeconds(nsec)
        ts._1 = tv._1 + sec
        ts._2 = if (sec > 0) nsec - TimeUnit.SECONDS.toNanos(sec) else nsec
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
  def apply(builder: ProcessBuilder): Process = {
    val useGen2 = if (LinktimeInfo.is32BitPlatform) {
      false
    } else if (LinktimeInfo.isLinux) {
      LinuxOsSpecific.hasPidfdOpen()
    } else if ((LinktimeInfo.isMac) || (LinktimeInfo.isFreeBSD)) {
      // Other BSDs should work but have not been exercised.
      true
    } else {
      rc
    }
  }

  @inline private def nullTerminate(
      list: java.util.List[String]
  )(implicit z: Zone) = {
    val res: Ptr[CString] = alloc[CString]((list.size() + 1).toUInt)
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
