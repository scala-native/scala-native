package java.lang.process

import java.io.{File, IOException, InputStream, OutputStream}
import java.io.FileDescriptor

import java.lang.ProcessBuilder.Redirect

import java.lang.process.BsdOsSpecific._
import java.lang.process.BsdOsSpecific.Extern.{kevent64, kqueue}

import java.lang.process.LinuxOsSpecific._
import java.lang.process.LinuxOsSpecific.Extern.{pidfd_open, ppoll}

import java.{util => ju}
import ju.concurrent.TimeUnit
import ju.ArrayList
import ju.ScalaOps._

import scalanative.meta.LinktimeInfo

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.posix.{errno => pe}, pe.errno, pe.ENOEXEC
import scalanative.posix.fcntl

import scalanative.posix.poll._
import scalanative.posix.pollOps._
import scalanative.posix.pollEvents

import scalanative.posix.signal.{kill, SIGKILL, SIGTERM}
import scalanative.posix.string.strerror
import scalanative.posix.sys.wait._

import scalanative.posix.time.timespec
import scalanative.posix.timeOps.timespecOps
import scalanative.posix.unistd

private[lang] class UnixProcessGen2 private (
    pid: CInt,
    builder: ProcessBuilder,
    infds: Ptr[CInt],
    outfds: Ptr[CInt],
    errfds: Ptr[CInt]
) extends UnixProcess(pid, builder, infds, outfds, errfds) {

  private[this] var _exitValue: Option[Int] = None

  override def destroy(): Unit = kill(pid, SIGTERM)

  override def destroyForcibly(): Process = {
    kill(pid, SIGKILL)
    this
  }

  override def exitValue(): scala.Int = {
    if (_exitValue.isDefined) { // previous waitFor() discovered _exitValue
      _exitValue.head
    } else { // have to find out for ourselves.
      val waitStatus = waitpidImpl(pid, options = WNOHANG)

      if (waitStatus == 0) {
        throw new IllegalThreadStateException()
      } else {
        _exitValue.getOrElse(-1) // -1 should never happen
      }
    }
  }

  override def getErrorStream(): InputStream = _errorStream

  override def getInputStream(): InputStream = _inputStream

  override def getOutputStream(): OutputStream = _outputStream

  override def isAlive(): scala.Boolean = {
    waitpidImpl(pid, options = WNOHANG) == 0
  }

  override def toString = { // Match JVM output
    val ev = _exitValue.fold("not exited")(_.toString())
    s"Process[pid=${pid}, exitValue=${ev}]"
  }

  override def waitFor(): scala.Int = {
    // wait until process exits or forever, whichever comes first.
    _exitValue // avoid wait-after-wait complexity
      .orElse(osWaitForImpl(None))
      .getOrElse(1) // 1 == EXIT_FAILURE, unknown cause
  }

  override def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean = {
    // avoid wait-after-wait complexity
    _exitValue // avoid wait-after-wait complexity
      .orElse {
        // wait until process exits or times out.
        val tv = stackalloc[timespec]()
        fillTimeval(timeout, unit, tv)
        osWaitForImpl(Some(tv))
      }.isDefined
  }

  private[lang] def checkResult(): CInt = {
    /* checkResult() is a no-op on UnixProcessGen2 but can not be easily deleted.
     * PipeIO.scala calls it and neither knows nor cares if it is calling into
     * a UnixProcessGen1 or UnixProcessGen2.
     * When/if UnixProcessGen1 is no longer in the mix, this method and its callers
     * in PipeIO can be deleted to save a few machine cycles.
     */

    0 // Sole caller, PipeIO, never checks value. Just no-op & match signature.
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

  private def waitpidImpl(pid: pid_t, options: Int): Int = {
    val wstatus = stackalloc[Int]()

    val waitStatus = waitpid(pid, wstatus, options)

    if (waitStatus == -1) {
      val msg = s"waitpid failed: ${fromCString(strerror(errno))}"
      throw new IOException(msg)
    } else if (waitStatus > 0) {
      // Cache exitStatus as long as we already have it in hand.
      val decoded =
        if (WIFEXITED(!wstatus)) WEXITSTATUS(!wstatus)
        else if (WIFSIGNALED(!wstatus)) 128 + WTERMSIG(!wstatus)
        else {
          1 // Catchall for general errors
          // https://tldp.org/LDP/abs/html/exitcodes.html
        }

      _exitValue = Some(decoded)
    }

    waitStatus
  }

  private def askZombiesForTheirExitStatus(): Int = {
    /* This method is simple, but the __long__ explanation it requires
     * belongs in one place, not in each of its callers.
     *
     * USE THIS METHOD __ONLY_IMMEDIATELY_AFTER_ kevent64/ppoll says
     * the child process has exited.  Otherwise it can hang/block indefinitely,
     * causing much sadness and rending of garments.
     *
     *  Explicitly allow HANG in "options".
     *  macOS appears to allow a tiny (millisecond?) delay between when
     *  kevent64 reports a child exit transition and when waitpid() on that
     *  process reports the child as exited.  This delay is not seen on Linux.
     *
     *  The alternative to allowing HANG on a process which kevent64/ppoll has
     *  just reported as having exited to a fussy busy-wait timing loop.
     */

    waitpidImpl(pid, options = 0)
  }

  private def closeProcessStreams(): Unit = {
    // drain() on a stream will close() it.
    _inputStream.drain()
    _errorStream.drain()
    _outputStream.close()
  }

  // corral handling timevalue conversion details, fill tv.
  private def fillTimeval(
      timeout: scala.Long,
      unit: TimeUnit,
      tv: Ptr[timespec]
  ): Unit = {
    if (timeout < 0) {
      throw new Exception(
        s"invalid negative timeout: value: ${timeout} unit: ${unit}"
      )
    }

    /* The longest representation the C structure will accommodate is
     * java.lang.Long.MAX_VALUE seconds and 999,999 nanos.
     *
     * Certain combinations of the timeout & unit arguments and specified
     * conversion will result in saturation and Java returning
     * java.lang.Long.MAX_VALUE.
     *
     * The math below will only accommodate java.lang.Long.MAX_VALUE seconds
     * and 0 nanos. Perhaps during that time a better solution will be found.
     */

    val seconds = unit.toSeconds(timeout)

    tv.tv_sec = seconds.toSize
    tv.tv_nsec = (unit.toNanos(timeout) - unit.toNanos(seconds)).toSize
  }

  // Returns: Some(exitCode) if process has exited, None if timeout.
  private def osWaitForImpl(timeout: Option[Ptr[timespec]]): Option[Int] = {
    // caller should have returned before here if _exitValue.isDefined == true
    if (LinktimeInfo.isLinux) {
      linuxWaitForImpl(timeout)
    } else if (LinktimeInfo.isMac || LinktimeInfo.isFreeBSD) {
      bsdWaitForImpl(timeout)
    } else {
      // Should never get here. Earlier dispatch should have called UnixProcessGen1.
      throw new IOException("unsuported Platform")
    }
  }

  /* Linux - ppoll()
   *     Returns: Some(exitCode) if process has exited, None if timeout.
   */
  private def linuxWaitForImpl(timeout: Option[Ptr[timespec]]): Option[Int] = {
    /* Design Note:
     *     This first implementation uses ppoll() because it gets the job
     *     done and there are fewer SN ecosystem changes to implement.
     *
     *     A future evolution could use epoll(). Since only one fd is involved
     *     I doubt that there is any execution speedup. It would be sweet
     *     though.
     */

    // close-on-exec is automatically set on the pidFd.
    val pidFd = pidfd_open(pid, 0.toUInt)

    if (pidFd == -1) {
      val msg = s"pidfd_open failed: ${fromCString(strerror(errno))}"
      throw new IOException(msg)
    }

    val fds = stackalloc[struct_pollfd](1.toUInt)
    (fds + 0).fd = pidFd
    (fds + 0).events = pollEvents.POLLIN | pollEvents.POLLRDNORM

    val tmo = timeout.getOrElse(null)

    // 'null' sigmask will retain all current signals.
    val ppollStatus = ppoll(fds, 1.toUSize, tmo, null);

    unistd.close(pidFd) // ensure fd does not leak away.

    if (ppollStatus < 0) {
      val msg = s"ppoll failed: ${errno}"
      throw new IOException(msg)
    } else if (ppollStatus == 0) {
      None
    } else {
      /* Minimize potential blocking wait in waitpid() by doing some necessary work
       * before asking for an exit status rather than after.
       * This gives the pid process time to exit fully.
       */
      closeProcessStreams()
      Some(askZombiesForTheirExitStatus())
    }
  }

  /* macOS -- kevent64
   *     Returns: Some(exitCode) if process has exited, None if timeout.
   */
  private def bsdWaitForImpl(timeout: Option[Ptr[timespec]]): Option[Int] = {

    /* Design Note:
     *     This first implementation creates a kqueue() on each & every
     *     waitFor() invocation.  An obvious evolution is to create one
     *     kqueue per class instance and reuse it. The trick would be to
     *     ensure that it gets closed when the instance is no longer used.
     *     Things would have to be set up so that Linux systems would stay
     *     happy.
     */

    val kq = kqueue()

    if (kq == -1) {
      val msg = s"kqueue failed: ${fromCString(strerror(errno))}"
      throw new IOException(msg)
    }

    val childExitEvent = stackalloc[kevent64_s]()
    val eventResult = stackalloc[kevent64_s]()

    /* event will eventually be deleted when child pid closes.
     * EV_DISPATCH hints that the event can be deleted immediately after delivery.
     */

    childExitEvent._1 = pid.toUSize
    childExitEvent._2 = EVFILT_PROC.toShort
    childExitEvent._3 = (EV_ADD | EV_DISPATCH).toUShort
    childExitEvent._4 = (NOTE_EXIT | NOTE_EXITSTATUS).toUInt

    val tmo = timeout.getOrElse(null)

    val status =
      kevent64(kq, childExitEvent, 1, eventResult, 1, 0.toUInt, tmo)

    unistd.close(kq) // Do not leak kq.

    if (status < 0) {
      val msg = s"kevent64 failed: ${fromCString(strerror(errno))}"
      throw new IOException(msg)
    } else if (status == 0) {
      None
    } else {
      /* Minimize potential blocking wait in waitpid() by doing some necessary work
       * before asking for an exit status rather than after.
       * This gives the pid process time to exit fully.
       *
       * macOS may have a millisecond or more delay between kevent64
       * reporting a process as having exited and waitpid() seeing it.
       */
      closeProcessStreams()
      Some(askZombiesForTheirExitStatus())
    }
  } // macOS -- kevent64

} // class

object UnixProcessGen2 {

  def apply(builder: ProcessBuilder): Process = Zone { implicit z =>
    val infds: Ptr[CInt] = stackalloc[CInt](2.toUInt)
    val outfds: Ptr[CInt] = stackalloc[CInt](2.toUInt)
    val errfds =
      if (builder.redirectErrorStream()) outfds
      else stackalloc[CInt](2.toUInt)

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
          if (unistd.execve(bin, argv, envp) == -1 && errno == ENOEXEC) {
            val al = new ArrayList[String](3)
            al.add("/bin/sh"); al.add("-c"); al.add(b)
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

        Seq(!(outfds + 1), !(errfds + 1), !infds) foreach unistd.close

        new UnixProcessGen2(pid, builder, infds, outfds, errfds)
    }
  }

  private def throwOnError(rc: CInt, msg: => String): CInt = {
    if (rc != 0) {
      throw new IOException(s"$msg Error code: $rc, Error number: $errno")
    } else {
      rc
    }
  }

  private def nullTerminate(
      list: java.util.List[String]
  )(implicit z: Zone) = {
    val res: Ptr[CString] = alloc[CString]((list.size() + 1).toUInt)
    val li = list.listIterator()
    while (li.hasNext()) {
      !(res + li.nextIndex()) = toCString(li.next())
    }
    res
  }

  private def setupChildFDS(
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

  def open(f: File, flags: CInt) = Zone { implicit z =>
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
