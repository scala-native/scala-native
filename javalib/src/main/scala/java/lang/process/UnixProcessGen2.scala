package java.lang.process

import java.io.{File, IOException}
import java.lang.process.BsdOsSpecific.Extern.{kevent, kqueue}
import java.lang.process.BsdOsSpecific._
import java.lang.process.LinuxOsSpecific.Extern.{pidfd_open, ppoll}
import java.{util => ju}

import scala.annotation.tailrec

import scalanative.meta.LinktimeInfo
import scalanative.posix.errno._
import scalanative.posix.poll._
import scalanative.posix.pollOps._
import scalanative.posix.spawn._
import scalanative.posix.string.strerror
import scalanative.posix.sys.wait._
import scalanative.posix.time.timespec
import scalanative.posix.timeOps.timespecOps
import scalanative.posix.{fcntl, pollEvents, unistd}
import scalanative.unsafe._
import scalanative.unsigned._

import ju.ArrayList
import ju.ScalaOps._
import ju.concurrent.TimeUnit

/* Design Note:
 * 
 * This implementation is actively "Under Heavy Construction".
 * If you are thinking of making changes, please co-ordinate in
 * SN Issue #4508 or elsewhere. Thank you.
 * 
 * The comments in this file, especially block or design comments
 * have become out of sync with the implementation are almost certainly
 * a waste of time.
 *
 * If you attempt to trace code paths and say "this does not make sense",
 * "this can not be", "this is not thread-safe", or "this is neither
 * efficient nor a short code path" you are probably right.
 */

private[process] class UnixProcessHandleGen2(
    override protected val _pid: CInt,
    override val builder: ProcessBuilder
) extends UnixProcessHandle {

  override protected def getExitCodeImpl: Option[Int] =
    waitpidImplNoECHILD(_pid, options = WNOHANG)

  override protected def waitForImpl(): Boolean = {
    /* wait until process exits, is interrupted in OS wait,  or forever,
     * whichever comes first.
     */
    while (!osWaitForImpl(None) && !hasExited) {}
    true
  }

  override protected def waitForImpl(
      timeoutArg: Long,
      unit: TimeUnit
  ): Boolean = {
    // Java allows negative timeouts. Simplify timeout math; treat as 0.
    val timeout = Math.max(timeoutArg, 0L)

    val deadline = System.nanoTime() + unit.toNanos(timeout)
    // wait until process exits or times out.
    val ts = stackalloc[timespec]()
    fillTimespec(timeout, unit, ts)
    @tailrec
    def waitWithRepeat(): Boolean = {
      val ok = osWaitForImpl(Some(ts))
      hasExited || !ok && {
        val remainingNanos = deadline - System.nanoTime()
        remainingNanos >= 0 && {
          fillTimespec(remainingNanos, TimeUnit.NANOSECONDS, ts)
          waitWithRepeat()
        }
      }
    }
    waitWithRepeat()
  }

  private def waitpidImplNoECHILD(pid: pid_t, options: Int): Option[Int] = {
    val wstatus = stackalloc[Int]()

    val waitStatus = waitpid(pid, wstatus, options)

    if (waitStatus == -1) {
      if (errno == EINTR) {
        throw new InterruptedException()
      } else if (errno == ECHILD) {
        /* See extensive discussion in SN Issue #4208 and identical
         * closely related #4208.
         */

        // OK if no exchange, someone else already reaped the process.
        Some(1)
      } else {
        val msg = s"waitpid failed: ${fromCString(strerror(errno))}"
        throw new IOException(msg)
      }
    } else if (waitStatus > 0) {
      // Cache exitStatus as long as we already have it in hand.
      val decoded =
        if (WIFEXITED(!wstatus)) WEXITSTATUS(!wstatus)
        else if (WIFSIGNALED(!wstatus)) 128 + WTERMSIG(!wstatus)
        else {
          1 // Catchall for general errors
          // https://tldp.org/LDP/abs/html/exitcodes.html
        }

      // OK if no exchange, someone else already reaped the process.
      Some(decoded)
    } else None
  }

  private def askZombiesForTheirExitStatus(): Unit = {
    /* This method is simple, but the __long__ explanation it requires
     * belongs in one place, not in each of its callers.
     *
     * USE THIS METHOD __ONLY_IMMEDIATELY_AFTER_ kevent/ppoll says
     * the child process has exited.  Otherwise it can hang/block indefinitely,
     * causing much sadness and rending of garments.
     *
     *  Explicitly allow HANG in "options".
     *  macOS appears to allow a tiny (millisecond?) delay between when
     *  kevent reports a child exit transition and when waitpid() on that
     *  process reports the child as exited.  This delay is not seen on Linux.
     *
     *  The alternative to allowing HANG on a process which kevent/ppoll has
     *  just reported as having exited is a fussy busy-wait timing loop.
     */

    val ec = waitpidImplNoECHILD(_pid, options = 0)
    setCachedExitCode(ec.getOrElse(1))
  }

  // corral handling timevalue conversion details, fill ts.
  private def fillTimespec(
      timeout: scala.Long,
      unit: TimeUnit,
      ts: Ptr[timespec]
  ): Unit = {
    // Precondition: caller has ensured that timeout >= 0.

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

    /* Arguments 'timeout: scala.Long' and 'unit: TimeUnit' allow a greater
     * range of values than the underlying operating data structure:
     * C struct timespec. 'timespec' allows only java.lang.Long.MAX_VALUE
     * seconds and 999,999,999 nanos. Note the restriction on the range
     * of nanoseconds.
     *
     * TimeUnits of TimeUnit.SECOND or larger will always have zero
     * nanoseconds. Some combinations of timeout and unit will
     * saturate (overflow) the timspec.tv_sec field.
     * Consider: java.lang.Long.MAX_VALUE and TimeUnit.DAYS.
     *
     * TimeUnits smaller than TimeUnit.SECOND may have  effective
     * tv nanoseconds. Consider: 1999 and TimeUnit.MILLISECONDS.
     * The operating system(s) require the timespec to be normalized.
     * That is, the tv_nsec field must be between 0 and 999,999,999.
     * That is, represent less than a second, full seconds go into the
     * tv_sec field.
     *
     * The math below is more complicated that the 'usual' algorithm
     * one might expect because it accounts for saturation and normalization.
     * NOT:
     *  tv.tv_nsec =
     *      (unit.toNanos(timeout) - TimeUnit.SECONDS.toNanos(seconds)).toSize
     */

    ts.tv_sec = unit.toSeconds(timeout).toSize

    /* To the devo or reviewer reading the code down the line and asking
     * "These are known compile time constants, why not use 1_000 and such?".
     *
     * SN currently supports Scala 2.12, which does not allow underscores
     * in numeric literals: 1_000. Scala versions 2.13 and above do.
     * The complier should optimize the math of the constants at compile time
     * but the code looks strange.
     *
     * If there is a reason to touch this code once Scala 2.12 is no longer
     * supported, the literals with underbars expected by contemporary
     * eyes can be introduced.
     */

    ts.tv_nsec = {
      val modulus = unit match {
        case _ if (unit == TimeUnit.MILLISECONDS) =>
          1000L
        case _ if (unit == TimeUnit.MICROSECONDS) =>
          1000L * 1000
        case _ if (unit == TimeUnit.NANOSECONDS) =>
          1000L * 1000 * 1000
        case _ => 1L // For all i: Int, (i % 1) == 0, which propagates through.
      }

      unit.toNanos(timeout % modulus).toSize
    }
  }

  private def osWaitForImpl(timeout: Option[Ptr[timespec]]): Boolean = try {
    // caller should have returned before here if cachedExitValue is known.
    if (hasExited) {
      // Another waitFor() has reaped exitValue; nothing to do here.
    } else if (LinktimeInfo.isLinux) {
      linuxWaitForImpl(timeout)
    } else if (LinktimeInfo.isMac || LinktimeInfo.isFreeBSD) {
      bsdWaitForImpl(timeout)
    } else {
      /* Should never get here. Earlier dispatch should have called
       * UnixProcessGen1.
       */
      throw new IOException("unsuported Platform")
    }
    true
  } catch {
    case _: InterruptedException if !Thread.currentThread().isInterrupted() =>
      false
  }

  /* Linux - ppoll()
   */
  private def linuxWaitForImpl(timeout: Option[Ptr[timespec]]): Unit = {
    // epoll() is not used in this method since only one fd is involved.

    // close-on-exec is automatically set on the pidFd.
    val pidFd = pidfd_open(_pid, 0.toUInt)

    if (pidFd == -1) {
      if (errno == EINTR) throw new InterruptedException()
      else if (errno == ECHILD || errno == ESRCH || errno == EINVAL)
        setCachedExitCode(1)
      else
        throw new IOException(
          s"pidfd_open(${_pid}) failed: ${fromCString(strerror(errno))}"
        )
      return
    }

    val fds = stackalloc[struct_pollfd](1)
    (fds + 0).fd = pidFd
    (fds + 0).events = (pollEvents.POLLIN | pollEvents.POLLRDNORM).toShort

    // 'null' sigmask will retain all current signals.
    val ppollStatus = ppoll(fds, 1.toUSize, timeout.orNull, null)

    unistd.close(pidFd) // ensure fd does not leak away.

    if (ppollStatus < 0) {
      // handled in the caller
      if (errno == EINTR) throw new InterruptedException()
      throw new IOException(
        s"waitFor pid=${_pid}, ppoll failed: ${fromCString(strerror(errno))}"
      )
    } else if (ppollStatus > 0) {
      askZombiesForTheirExitStatus()
    }
  }

  /* macOS & FreeBSD -- kevent
   */
  private def bsdWaitForImpl(timeout: Option[Ptr[timespec]]): Unit = {

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
      if (errno == EINTR) throw new InterruptedException()
      throw new IOException(
        s"waitFor pid=${_pid} kqueue failed: ${fromCString(strerror(errno))}"
      )
    }

    /* Some Scala non-idiomatic sleight of hand is going on here to
     * ease implementation. Scala 3 has union types, but other versions
     * do not.  "struct kevent" and "struct kevent64_s" overlay exactly in
     * the fields of interest here. In C and Scala 3 they could be a union.
     * Here the former is declared as the latter and later cast because
     * it is easier to access the field names of the latter; fewer casts
     * and contortions.
     */
    val childExitEvent = stackalloc[kevent64_s]()
    val eventResult = stackalloc[kevent64_s]()

    /* event will eventually be deleted when child pid closes.
     * EV_DISPATCH hints that the event can be deleted immediately after
     * delivery.
     */

    childExitEvent._1 = _pid.toUSize
    childExitEvent._2 = EVFILT_PROC.toShort
    childExitEvent._3 = (EV_ADD | EV_DISPATCH).toUShort
    childExitEvent._4 = (NOTE_EXIT | NOTE_EXITSTATUS).toUInt

    val status =
      kevent(
        kq,
        childExitEvent.asInstanceOf[Ptr[kevent]],
        1,
        eventResult.asInstanceOf[Ptr[kevent]],
        1,
        timeout.orNull
      )

    unistd.close(kq) // Do not leak kq.

    if (status < 0) {
      if (errno == EINTR) throw new InterruptedException()
      throw new IOException(
        s"wait pid=${_pid}, kevent failed: ${fromCString(strerror(errno))}"
      )
    } else if (status > 0) {
      askZombiesForTheirExitStatus()
    }
  }
}

private[process] object UnixProcessGen2 {

  def apply(
      builder: ProcessBuilder
  ): GenericProcess = Zone.acquire { implicit z =>
    /* If builder.directory is not null, it specifies a new working
     * directory for the process (chdir()).
     *
     * POSIX 2018 gives no way to change the working directory in
     * file_actions, so the legacy fork() path must be taken.
     * POXIX 2023 should allow changing the working directory.
     *
     * Checking for ".", which callers tend to specify, is an optimization
     * to elide changing directory to what is already the working
     * directory.
     */

    val dir = builder.directory()
    if ((dir != null) && (dir.toString != ".")) {
      forkChild(builder)(new UnixProcessHandleGen2(_, _))
    } else {
      spawnChild(builder)
    }
  }

  def forkChild(builder: ProcessBuilder)(
      f: (Int, ProcessBuilder) => UnixProcessHandle
  )(implicit z: Zone): GenericProcess = {
    val infds: Ptr[CInt] = createPipe(stackalloc[CInt](2), "input")
    val outfds: Ptr[CInt] = createPipe(stackalloc[CInt](2), "output")
    val errfds =
      if (builder.redirectErrorStream()) null
      else createPipe(stackalloc[CInt](2), "error")

    val cmd = builder.command()
    val binaries = binaryPaths(builder.environment(), cmd.get(0))
    val dir = builder.directory()
    val argv = nullTerminate(cmd)
    val envp = nullTerminate(builder.getEnvironmentAsList())

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
        if (null eq errfds)
          dup2(unistd.STDOUT_FILENO, unistd.STDERR_FILENO, "pipe")
        else
          setupChildFDS(
            !(errfds + 1),
            builder.redirectError(),
            unistd.STDERR_FILENO
          )

        // No sense closing stuff either active or already closed!
        // dup2() will close() what is not INHERITed.
        val parentFds = new ArrayList[CInt] // No Scala Collections in javalib
        parentFds.add(!(infds + 1)) // parent's stdout - write, in child
        parentFds.add(!outfds) // parent's stdin - read, in child
        if (null ne errfds)
          parentFds.add(!errfds) // parent's stderr - read, in child

        parentFds.forEach { fd => unistd.close(fd) }

        binaries.foreach { b =>
          val bin = toCString(b)
          if (unistd.execve(bin, argv, envp) == -1 && errno == ENOEXEC) {
            val al = new ArrayList[String](3)
            al.add("/bin/sh"); al.add("-c")
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
        val childFds = new ArrayList[CInt] // No Scala Collections in javalib
        childFds.add(!infds) // child's stdin read, in parent
        childFds.add(!(outfds + 1)) // child's stdout write, in parent
        if (null ne errfds)
          childFds.add(!(errfds + 1)) // child's stderr write, in parent

        childFds.forEach { fd => unistd.close(fd) }

        UnixProcess(f(pid, builder), infds, outfds, errfds)
    }
  }

  private def spawnChild(
      builder: ProcessBuilder
  )(implicit z: Zone): GenericProcess = {
    val pidPtr = stackalloc[pid_t]()

    val infds: Ptr[CInt] = createPipe(stackalloc[CInt](2), "input")
    val outfds: Ptr[CInt] = createPipe(stackalloc[CInt](2), "output")
    val errfds =
      if (builder.redirectErrorStream()) null
      else createPipe(stackalloc[CInt](2), "error")

    val cmd = builder.command()
    val argv = nullTerminate(cmd)
    val envp = nullTerminate(builder.getEnvironmentAsList())

    /* Maintainers:
     *     There is a performance optimization in the walkPath() method
     *     of spawnFollowPath() which relies upon this parent being able
     *     to "see" the same set of PATH files and their attributes
     *     as the child. This is a valid assumption through Java 19
     *     as ProceesBuilder specifies no way to change process ids
     *     or groups.
     *
     *     If Java develops this capability in the future,
     *     please consider that optimization when changing fileActions,
     *     particularly POSIX_SPAWN_RESETIDS and POSIX_SPAWN_SETPGROUP.
     */

    // posix_spawn_file_actions_t takes 80 bytes, so do not stackalloc.
    val fileActions = alloc[posix_spawn_file_actions_t]()
    throwOnError(
      posix_spawn_file_actions_init(fileActions),
      "posix_spawn_file_actions_init"
    )

    val unixProcess =
      try {
        setupSpawnFDS(
          fileActions,
          !infds,
          builder.redirectInput(),
          unistd.STDIN_FILENO
        )

        setupSpawnFDS(
          fileActions,
          !(outfds + 1),
          builder.redirectOutput(),
          unistd.STDOUT_FILENO
        )

        if (null eq errfds)
          dup2Spawn(
            fileActions,
            unistd.STDOUT_FILENO,
            unistd.STDERR_FILENO,
            "pipe"
          )
        else
          setupSpawnFDS(
            fileActions,
            !(errfds + 1),
            builder.redirectError(),
            unistd.STDERR_FILENO
          )

        // No Scala Collections in javalib
        val parentFds = new ArrayList[CInt](3)
        parentFds.add(!(infds + 1)) // parent's stdout - write, in child
        parentFds.add(!outfds) // parent's stdin - read, in child
        if (null ne errfds)
          parentFds.add(!errfds) // parent's stderr - read, in child

        parentFds.forEach { fd =>
          throwOnError(
            posix_spawn_file_actions_addclose(fileActions, fd),
            s"posix_spawn_file_actions_addclose fd: ${fd}"
          )
        }

        /* This will exec binary executables.
         * Some shells (bash, ???) will also execute scripts with initial
         * shebang (#!).
         */
        def spawn(exec: String, argv: Ptr[CString]): CInt = posix_spawn(
          pidPtr,
          toCString(exec),
          fileActions,
          null, // attrp
          argv,
          envp
        )

        var status = ENOEXEC
        val execIter = binaryPaths(builder.environment(), cmd.get(0))

        while (status == ENOEXEC && execIter.hasNext)
          status = spawn(execIter.next(), argv)

        if (status == ENOEXEC) { // try falling back to shell script
          val shCmd = "/bin/sh"
          val fallbackCmd = Array(shCmd, "-c", cmd.scalaOps.mkString(sep = " "))
          status = spawn(shCmd, nullTerminate(ju.Arrays.asList(fallbackCmd)))
        }

        if (status != 0) {
          val msg = fromCString(strerror(status))
          throw new IOException(s"Unable to posix_spawn process: $msg")
        }

        val handle = new UnixProcessHandleGen2(!pidPtr, builder)
        UnixProcess(handle, infds, outfds, errfds)
      } finally {
        val childFds = new ArrayList[CInt] // No Scala Collections in javalib
        childFds.add(!infds) // child's stdin read, in parent
        childFds.add(!(outfds + 1)) // child's stdout write, in parent
        if (null ne errfds)
          childFds.add(!(errfds + 1)) // child's stderr write, in parent

        childFds.forEach(unistd.close(_))

        throwOnError(
          posix_spawn_file_actions_destroy(fileActions),
          "posix_spawn_file_actions_destroy"
        )
      }

    unixProcess
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
    val res: Ptr[CString] = alloc[CString]((list.size() + 1))
    val li = list.listIterator()
    while (li.hasNext()) {
      !(res + li.nextIndex()) = toCString(li.next())
    }
    res
  }

  private def createPipe(fds: Ptr[CInt], what: String): Ptr[CInt] = {
    throwOnError(unistd.pipe(fds), s"Couldn't create $what pipe.")
    fds
  }

  private def dup2(
      oldfd: CInt,
      newfd: CInt,
      what: String
  ): Unit = {
    if (unistd.dup2(oldfd, newfd) == -1)
      throw new IOException(s"Couldn't duplicate $what file descriptor $errno")
  }

  private def setupChildFDS(
      childFd: CInt,
      redirect: ProcessBuilder.Redirect,
      procFd: CInt
  )(implicit z: Zone): Unit = {
    def openWith(flags: CInt, what: String): Unit = {
      val file = redirect.file()
      val mode = getModeFromOpenFlags(flags)
      val fd = fcntl.open(getFileNameCString(file), flags, mode)
      if (fd < 0)
        throw new IOException(s"Unable to open $what file $file ($errno)")
      dup2(fd, procFd, what)
    }
    import fcntl.{open => _, _}
    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.INHERIT =>
      case ProcessBuilder.Redirect.Type.PIPE    =>
        dup2(childFd, procFd, "pipe")
      case ProcessBuilder.Redirect.Type.READ =>
        openWith(O_RDONLY, "read")
      case ProcessBuilder.Redirect.Type.WRITE =>
        openWith(O_CREAT | O_WRONLY | O_TRUNC, "write")
      case ProcessBuilder.Redirect.Type.APPEND =>
        openWith(O_CREAT | O_WRONLY | O_APPEND, "append")
    }
  }

  private def dup2Spawn(
      fileActions: Ptr[posix_spawn_file_actions_t],
      oldfd: CInt,
      newfd: CInt,
      what: String
  ): Unit = {
    val status = posix_spawn_file_actions_adddup2(fileActions, oldfd, newfd)
    if (status != 0)
      throw new IOException(
        s"Could not adddup2 $what file descriptor $newfd: $status"
      )
  }

  private def setupSpawnFDS(
      fileActions: Ptr[posix_spawn_file_actions_t],
      childFd: CInt,
      redirect: ProcessBuilder.Redirect,
      procFd: CInt
  )(implicit z: Zone): Unit = {
    import fcntl.{open => _, _}
    @inline def addopen(flags: CInt, what: String): Unit = {
      val file = redirect.file()
      val f = getFileNameCString(file)
      val mode = getModeFromOpenFlags(flags)
      val status =
        posix_spawn_file_actions_addopen(fileActions, procFd, f, flags, mode)
      if (status != 0)
        throw new IOException(
          s"Could not addopen $what fd=$procFd file=$file: $status"
        )
    }

    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.INHERIT =>

      case ProcessBuilder.Redirect.Type.PIPE =>
        dup2Spawn(fileActions, childFd, procFd, "pipe")

      case ProcessBuilder.Redirect.Type.READ =>
        addopen(O_RDONLY, "read")

      case ProcessBuilder.Redirect.Type.WRITE =>
        addopen(O_CREAT | O_WRONLY | O_TRUNC, "write")

      case ProcessBuilder.Redirect.Type.APPEND =>
        addopen(O_CREAT | O_WRONLY | O_APPEND, "append")
    }
  }

  private def getModeFromOpenFlags(flags: CInt): CUnsignedInt = {
    def defaultCreateMode = 0x1a4 // 0644, no octal literal in Scala
    (if ((flags & fcntl.O_CREAT) == 0) 0 else defaultCreateMode).toUInt
  }

  private def getFileNameCString(f: File)(implicit z: Zone) =
    toCString(f.getAbsolutePath())

  // The execvpe function isn't available on all platforms so find the
  // possible binaries to exec.
  private def binaryPaths(
      environment: java.util.Map[String, String],
      bin: String
  ): Iterator[String] = {
    if (bin.indexOf('/') >= 0 || bin.startsWith(".")) {
      Iterator(bin)
    } else {
      val path = environment.get("PATH") match {
        case null => "/bin:/usr/bin:/usr/local/bin"
        case p    => p
      }
      path.split(File.pathSeparator).iterator.flatMap { absPath =>
        val f = if (absPath.isEmpty) new File(bin) else new File(absPath, bin)
        if (f.canExecute()) Some(f.toString()) else None
      }
    }
  }

}
