package java.lang.process

import java.io.{File, IOException, InputStream, OutputStream}
import java.io.FileDescriptor

import java.{lang => jl}
import java.lang.ProcessBuilder.Redirect

import java.lang.process.BsdOsSpecific._
import java.lang.process.BsdOsSpecific.Extern.{kevent, kqueue}

import java.lang.process.LinuxOsSpecific._
import java.lang.process.LinuxOsSpecific.Extern.{pidfd_open, ppoll}

import java.{util => ju}
import ju.concurrent.TimeUnit
import ju.ArrayList
import ju.ScalaOps._

import java.util.{concurrent => juc}

import scala.annotation.tailrec

import scalanative.meta.LinktimeInfo

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.posix.errno._
import scalanative.posix.fcntl

import scalanative.posix.poll._
import scalanative.posix.pollOps._
import scalanative.posix.pollEvents

import scalanative.posix.signal.{kill, SIGKILL, SIGTERM}
import scalanative.posix.spawn._
import scalanative.posix.string.strerror
import scalanative.posix.sys.wait._

import scalanative.posix.time.timespec
import scalanative.posix.timeOps.timespecOps
import scalanative.posix.unistd

/* Design Note:
 * 
 * the java.lang.Process class was introduced in JDK 1.
 * Even the most recent JDK 24 documentation of the class says nothing
 * about the class being thread-safe. That means that one must treat
 * it as concurrent access requiring external synchronization.
 *
 * So much for "de jure". "De facto" applications such as Li Haoyi's os-lib
 * test cases, seem to work better, meaning succeed, where they also succeed on
 * JVM, when these methods are internally synchronized.
 *
 * Best current guess/hypothesis is that such applications are using an
 * unsynchronized 'isAlive()', to poll or watch "watch" if another process
 * in a 'waitFor()' has completed.
 * 
 * This code now uses a shared Java AtomicInteger in many places for greater
 * concurrency than the previous "synchronized{}" blocks.
 * 
 * This is a developing story, stay tuned.
 */

private[lang] class UnixProcessGen2 private (
    pid: CInt,
    builder: ProcessBuilder,
    infds: Ptr[CInt],
    outfds: Ptr[CInt],
    errfds: Ptr[CInt]
) extends UnixProcess() {

  private class CachedExitValue() {
    final val EXIT_VALUE_UNKNOWN = jl.Integer.MIN_VALUE

    // Non-negative values indicate exit status is known to be that value.
    val cached = new juc.atomic.AtomicInteger(EXIT_VALUE_UNKNOWN)

    def get(): scala.Int =
      cached.get()

    def getOrDefault(default: scala.Int): scala.Int = {
      val cev = get()
      if (cev >= 0) cev
      else default
    }

    def setOnce(known: scala.Int): Unit = {
      // Save only the value first presented. Be idempotent.
      cached.compareAndExchangeRelease(EXIT_VALUE_UNKNOWN, known)
    }
  }

  private val cachedExitValue = new CachedExitValue()

  override private[process] val processInfo =
    GenericProcess.Info.create(builder, pid = pid.toLong)

  override def destroy(): Unit = kill(pid, SIGTERM)

  override def destroyForcibly(): Process = {
    kill(pid, SIGKILL)
    this
  }

  override def exitValue(): scala.Int = {
    val cev = cachedExitValue.getOrDefault(-1)

    if (cev >= 0) cev // use cachedExitValue discovered by previous waitFor()
    else { // have to find out for ourselves.
      val waitStatus = waitpidImplNoECHILD(pid, options = WNOHANG)

      if (waitStatus == 0)
        throw new IllegalThreadStateException()
      else
        cachedExitValue.getOrDefault(1) // default 1 should never happen
    }
  }

  override def getErrorStream(): InputStream = _errorStream

  override def getInputStream(): InputStream = _inputStream

  override def getOutputStream(): OutputStream = _outputStream

  override def isAlive(): scala.Boolean = {
    if (cachedExitValue.get() >= 0) false
    else waitpidImplNoECHILD(pid, options = WNOHANG) == 0
  }

  override def toString = { // Match JVM output
    val cev = cachedExitValue.getOrDefault(-1)

    if (cev >= 0) s"Process[pid=${pid}, exitValue=${cev}]"
    else "not exited"
  }

  override def waitFor(): scala.Int = {
    // wait until process exits or forever, whichever comes first.
    var done = false

    while (!done) {
      try {
        osWaitForImpl(None)
        done = true
      } catch {
        case _: InterruptedException
            if !Thread.currentThread().isInterrupted() => // loop again
      }
    }

    cachedExitValue.getOrDefault(1) // default 1 should never happen
  }

  override def waitFor(
      timeoutArg: scala.Long,
      unit: TimeUnit
  ): scala.Boolean = {
    val cev = cachedExitValue.getOrDefault(-1)

    if (cev >= 0) true
    else {
      // Java allows negative timeouts. Simplify timeout math; treat as 0.
      val timeout = Math.max(timeoutArg, 0L)

      val deadline = System.nanoTime() + unit.toNanos(timeout)
      // wait until process exits or times out.
      val ts = stackalloc[timespec]()
      fillTimespec(timeout, unit, ts)
      def waitWithRepeat(): Unit = {
        try osWaitForImpl(Some(ts))
        catch {
          case _: InterruptedException
              if !Thread.currentThread().isInterrupted() =>
            deadline - System.nanoTime() match {
              case remaining if remaining < 0 => None
              case remainingNanos             =>
                fillTimespec(remainingNanos, TimeUnit.NANOSECONDS, ts)
                waitWithRepeat()
            }
        }
      }
      waitWithRepeat()
    }

    cachedExitValue.get() >= 0
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

  private val _inputStream =
    PipeIO[PipeIO.Stream](
      this,
      new FileDescriptor(!outfds),
      builder.redirectOutput()
    )

  private val _errorStream =
    PipeIO[PipeIO.Stream](
      this,
      new FileDescriptor(!errfds),
      builder.redirectError()
    )

  private val _outputStream =
    PipeIO[OutputStream](
      this,
      new FileDescriptor(!(infds + 1)),
      builder.redirectInput()
    )

  private def waitpidImplNoECHILD(pid: pid_t, options: Int): Int = {
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
        cachedExitValue.setOnce(1)
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
      cachedExitValue.setOnce(decoded)
    }

    waitStatus
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

    waitpidImplNoECHILD(pid, options = 0)
    cachedExitValue.setOnce(1)
  }

  private def closeProcessStreams(): Unit = synchronized {
    // drain() on a stream will close() it.
    _inputStream.drain()
    _errorStream.drain()
    _outputStream.close()
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

  private def osWaitForImpl(timeout: Option[Ptr[timespec]]): Unit = {
    // caller should have returned before here if cachedExitValue is known.
    if (cachedExitValue.get() >= 0) {
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
  }

  /* Linux - ppoll()
   */
  private def linuxWaitForImpl(timeout: Option[Ptr[timespec]]): Unit = {
    // epoll() is not used in this method since only one fd is involved.

    // close-on-exec is automatically set on the pidFd.
    val pidFd = pidfd_open(pid, 0.toUInt)

    if (pidFd == -1) {
      val msg = s"pidfd_open failed: ${fromCString(strerror(errno))}"
      throw new IOException(msg)
    }

    val fds = stackalloc[struct_pollfd](1)
    (fds + 0).fd = pidFd
    (fds + 0).events = (pollEvents.POLLIN | pollEvents.POLLRDNORM).toShort

    val tmo = timeout.getOrElse(null)

    // 'null' sigmask will retain all current signals.
    val ppollStatus = ppoll(fds, 1.toUSize, tmo, null);

    unistd.close(pidFd) // ensure fd does not leak away.

    if (ppollStatus < 0) {
      // handled in the caller
      if (errno == EINTR) throw new InterruptedException()
      throw new IOException(
        s"waitFor pid=${pid}, ppoll failed: ${fromCString(strerror(errno))}"
      )
    } else if (ppollStatus == 0) {
      None
    } else {
      /* Minimize potential blocking wait in waitpid() by doing some
       * necessary work before asking for an exit status rather than after.
       * This gives the pid process time to exit fully.
       */
      closeProcessStreams()
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
        s"waitFor pid=${pid} kqueue failed: ${fromCString(strerror(errno))}"
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

    childExitEvent._1 = pid.toUSize
    childExitEvent._2 = EVFILT_PROC.toShort
    childExitEvent._3 = (EV_ADD | EV_DISPATCH).toUShort
    childExitEvent._4 = (NOTE_EXIT | NOTE_EXITSTATUS).toUInt

    val tmo = timeout.getOrElse(null)

    val status =
      kevent(
        kq,
        childExitEvent.asInstanceOf[Ptr[kevent]],
        1,
        eventResult.asInstanceOf[Ptr[kevent]],
        1,
        tmo
      )

    unistd.close(kq) // Do not leak kq.

    if (status < 0) {
      if (errno == EINTR) throw new InterruptedException()
      throw new IOException(
        s"wait pid=${pid}, kevent failed: ${fromCString(strerror(errno))}"
      )
    } else if (status == 0) {
      None
    } else {
      /* Minimize potential blocking wait in waitpid() by doing some
       * necessary work before asking for an exit status rather than after.
       * This gives the pid process time to exit fully.
       *
       * macOS may have a millisecond or more delay between kevent
       * reporting a process as having exited and waitpid() seeing it.
       */
      closeProcessStreams()
      askZombiesForTheirExitStatus()
    }
  }
}

object UnixProcessGen2 {

  def apply(builder: ProcessBuilder): Process = Zone.acquire { implicit z =>
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
      forkChild(builder)
    } else {
      spawnChild(builder)
    }
  }

  private def forkChild(builder: ProcessBuilder)(implicit z: Zone): Process = {
    val infds: Ptr[CInt] = stackalloc[CInt](2)
    val outfds: Ptr[CInt] = stackalloc[CInt](2)
    val errfds =
      if (builder.redirectErrorStream()) outfds
      else stackalloc[CInt](2)

    throwOnError(unistd.pipe(infds), s"Couldn't create infds pipe.")
    throwOnError(unistd.pipe(outfds), s"Couldn't create outfds pipe.")
    if (!builder.redirectErrorStream())
      throwOnError(unistd.pipe(errfds), s"Couldn't create errfds pipe.")

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
          if (builder.redirectErrorStream()) Redirect.PIPE
          else builder.redirectError(),
          unistd.STDERR_FILENO
        )

        // No sense closing stuff either active or already closed!
        // dup2() will close() what is not INHERITed.
        val parentFds = new ArrayList[CInt] // No Scala Collections in javalib
        parentFds.add(!(infds + 1)) // parent's stdout - write, in child
        parentFds.add(!outfds) // parent's stdin - read, in child
        if (!builder.redirectErrorStream())
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
        if (!builder.redirectErrorStream())
          childFds.add(!(errfds + 1)) // child's stderr write, in parent

        childFds.forEach { fd => unistd.close(fd) }

        new UnixProcessGen2(pid, builder, infds, outfds, errfds)
    }
  }

  private def spawnChild(builder: ProcessBuilder)(implicit z: Zone): Process = {
    val cmd = builder.command()
    if (cmd.get(0).indexOf('/') >= 0) {
      spawnCommand(builder, cmd, attempt = 1)
    } else {
      spawnFollowPath(builder)
    }
  }

  private def spawnCommand(
      builder: ProcessBuilder,
      localCmd: ju.List[String],
      attempt: Int
  )(implicit z: Zone): Process = {
    val pidPtr = stackalloc[pid_t]()

    val infds: Ptr[CInt] = stackalloc[CInt](2)
    val outfds: Ptr[CInt] = stackalloc[CInt](2)
    val errfds =
      if (builder.redirectErrorStream()) outfds
      else stackalloc[CInt](2)

    throwOnError(unistd.pipe(infds), s"Couldn't create infds pipe.")
    throwOnError(unistd.pipe(outfds), s"Couldn't create outfds pipe.")
    if (!builder.redirectErrorStream())
      throwOnError(unistd.pipe(errfds), s"Couldn't create errfds pipe.")

    val exec = localCmd.get(0)
    val argv = nullTerminate(localCmd)
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

        setupSpawnFDS(
          fileActions,
          !(errfds + 1),
          if (builder.redirectErrorStream()) Redirect.PIPE
          else builder.redirectError(),
          unistd.STDERR_FILENO
        )

        // No Scala Collections in javalib
        val parentFds = new ArrayList[CInt](3)
        parentFds.add(!(infds + 1)) // parent's stdout - write, in child
        parentFds.add(!outfds) // parent's stdin - read, in child
        if (!builder.redirectErrorStream())
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
        val status = posix_spawn(
          pidPtr,
          toCString(exec),
          fileActions,
          null, // attrp
          argv,
          envp
        )

        if (status == 0) {
          new UnixProcessGen2(!pidPtr, builder, infds, outfds, errfds)
        } else if (!(status == ENOEXEC) && (attempt == 1)) {
          val msg = fromCString(strerror(status))
          throw new IOException(s"Unable to posix_spawn process: ${msg}")
        } else { // try falling back to shell script
          val fallbackCmd = new ArrayList[String](3)
          fallbackCmd.add("/bin/sh")
          fallbackCmd.add("-c")
          fallbackCmd.add(localCmd.scalaOps.mkString(sep = " "))

          spawnCommand(builder, fallbackCmd, attempt = 2)
        }
      } finally {
        val childFds = new ArrayList[CInt] // No Scala Collections in javalib
        childFds.add(!infds) // child's stdin read, in parent
        childFds.add(!(outfds + 1)) // child's stdout write, in parent
        if (!builder.redirectErrorStream())
          childFds.add(!(errfds + 1)) // child's stderr write, in parent

        childFds.forEach(unistd.close(_))

        throwOnError(
          posix_spawn_file_actions_destroy(fileActions),
          "posix_spawn_file_actions_destroy"
        )
      }

    unixProcess
  }

  private def spawnFollowPath(
      builder: ProcessBuilder
  )(implicit z: Zone): Process = {

    @tailrec
    def walkPath(iter: UnixPathIterator): Process = {
      val cmd = builder.command()
      val cmd0 = cmd.get(0)

      if (!iter.hasNext()) {
        val errnoText = fromCString(strerror(errno))
        val msg = s"Cannot run program '${cmd0}': error=${errno}, ${errnoText}"
        throw new IOException(msg)
      } else {
        /* Maintainers:
         *     Please see corresponding note in method spawnCommand().
         *
         *     Checking that the fully qualified file exists and is
         *     executable a performance optimization.
         *
         *     posix_spawn() is the ultimate arbiter of which files
         *     the child can and can not execute. posix_spawn() is
         *     relatively expensive to be called on files "known" to
         *     either not exist or not be executable.
         *
         *    The "canExecute()" test required/assumes that the parent
         *    can see and execute the same set of files.  This is a
         *    reasonable precondition. Java 19 has no way to change child
         *    id or group. spawnCommand() takes care to not specify
         *    posix_spawn() options for changes which Java 19 does not
         *    specify.
         */

        val fName = s"${iter.next()}/${cmd0}"
        val f = new File(fName)
        if (!f.canExecute()) {
          walkPath(iter)
        } else {
          val newCmdList = new ArrayList[String](cmd)
          newCmdList.set(0, fName)

          spawnCommand(builder, newCmdList, attempt = 1)
        }
      }
    }

    walkPath(new UnixPathIterator(builder.environment()))
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

  private def setupChildFDS(
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

  private def setupSpawnFDS(
      fileActions: Ptr[posix_spawn_file_actions_t],
      childFd: CInt,
      redirect: ProcessBuilder.Redirect,
      procFd: CInt
  ): Unit = {
    import fcntl.{open => _, _}
    redirect.`type`() match {
      case ProcessBuilder.Redirect.Type.INHERIT =>

      case ProcessBuilder.Redirect.Type.PIPE =>
        val status =
          posix_spawn_file_actions_adddup2(fileActions, childFd, procFd)
        if (status != 0) {
          throw new IOException(
            s"Could not adddup2 pipe file descriptor ${procFd}: ${status}"
          )
        }

      case r @ ProcessBuilder.Redirect.Type.READ =>
        val fd = open(redirect.file(), O_RDONLY)
        // result is error checked in inline open() below.

        val status = posix_spawn_file_actions_adddup2(fileActions, fd, procFd)
        if (status != 0) {
          throw new IOException(
            s"Could not adddup2 read file ${redirect.file()}: ${status}"
          )
        }

      case r @ ProcessBuilder.Redirect.Type.WRITE =>
        val fd = open(redirect.file(), O_CREAT | O_WRONLY | O_TRUNC)
        // result is error checked in inline open() below.

        val status = posix_spawn_file_actions_adddup2(fileActions, fd, procFd)
        if (status != 0) {
          throw new IOException(
            s"Could not adddup2 write file ${redirect.file()}: ${status}"
          )
        }

      case r @ ProcessBuilder.Redirect.Type.APPEND =>
        val fd = open(redirect.file(), O_CREAT | O_WRONLY | O_APPEND)
        // result is error checked in inline open() below.

        val status = posix_spawn_file_actions_adddup2(fileActions, fd, procFd)
        if (status != 0) {
          throw new IOException(
            s"Could not adddup2 append file ${redirect.file()}: ${status}"
          )
        }
    }
  }

  def open(f: File, flags: CInt) = Zone.acquire { implicit z =>
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
  private class UnixPathIterator(
      environment: java.util.Map[String, String]
  ) extends ju.Iterator[String] {
    /* The default path here is passing strange Scala Native prior art.
     * It is preserved to keep compatability  with UnixProcessGen1 and prior
     * versions of Scala Native.
     *
     * For example, Ubuntu Linux bash compiles in:
     *   PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
     *   // Note that "/usr/local/" comes before (left of) "/usr/".
     */
    val path = environment.getOrDefault("PATH", "/bin:/usr/bin:/usr/local/bin")

    val pathElements = path.split(':')
    val nElements = pathElements.length
    var lookingAt = 0

    override def hasNext(): Boolean = (lookingAt < nElements)

    override def next(): String = {
      if (lookingAt >= nElements) {
        throw new NoSuchElementException()
      } else {
        val d = pathElements(lookingAt)
        lookingAt += 1
        // "" == "." is a poorly documented Unix PATH quirk/corner_case.
        if (d.length == 0) "." else d
      }
    }
  }
}
