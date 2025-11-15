package java.lang.process

import java.io.{File, IOException}
import java.{util => ju}

import scala.annotation.tailrec

import scalanative.posix.errno._
import scalanative.posix.spawn._
import scalanative.posix.{fcntl, unistd}
import scalanative.unsafe._
import scalanative.unsigned._

import ju.ArrayList
import ju.ScalaOps._

private[process] object UnixProcessFactory {

  def apply(pb: ProcessBuilder): GenericProcess = Zone.acquire { implicit z =>
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

    val needSpawn = pb.isCwd && ProcessExitChecker.unixFactoryOpt.isDefined
    if (needSpawn) spawnChild(pb) else forkChild(pb)
  }

  def forkChild(builder: ProcessBuilder)(implicit z: Zone): GenericProcess = {
    var success = false
    val (infds, outfds, errfds) = createPipes(builder)

    def closePipe(pipe: Ptr[CInt]): Unit =
      if (pipe ne null) {
        unistd.close(!(pipe + 0))
        unistd.close(!(pipe + 1))
      }

    def closePipes(): Unit = {
      closePipe(infds)
      closePipe(outfds)
      closePipe(errfds)
    }

    def runChild(): Nothing = {
      val cmd = builder.command()
      val binaries = binaryPaths(builder.environment(), cmd.get(0))
      val argv = nullTerminate(cmd)
      val envp = nullTerminate(builder.getEnvironmentAsList())

      if (!builder.isCwd)
        unistd.chdir(toCString(builder.directory().toString()))

      setupChildFDS(!infds, builder.redirectInput(), unistd.STDIN_FILENO)
      setupChildFDS(
        !(outfds + 1),
        builder.redirectOutput(),
        unistd.STDOUT_FILENO
      )
      if (builder.redirectErrorStream())
        dup2(unistd.STDOUT_FILENO, unistd.STDERR_FILENO, "pipe")
      else
        setupChildFDS(
          !(errfds + 1),
          builder.redirectError(),
          unistd.STDERR_FILENO
        )

      closePipes()

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
    }

    @tailrec
    def fork(attempt: Int = 0): Int = {
      val pid = unistd.fork()
      if (pid == -1 && errno == EAGAIN && attempt < 4) {
        // start with 16ms, end with 128ms
        Thread.sleep(2 << (attempt + 4))
        fork(attempt + 1)
      } else pid
    }

    try {
      val pid = UnixProcess.throwOnError(fork(0), "Unable to fork process")
      if (pid == 0) runChild()
      else {
        success = true
        val handle = new UnixProcessHandle(pid)(builder)
        UnixProcess(handle, infds, outfds, errfds)
      }
    } finally {
      if (!success) closePipes()
    }
  }

  private def spawnChild(
      builder: ProcessBuilder
  )(implicit z: Zone): GenericProcess = {
    val pidPtr = stackalloc[pid_t]()

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
    UnixProcess.throwOnErrnum(
      posix_spawn_file_actions_init(fileActions),
      "posix_spawn_file_actions_init"
    )

    var success = false
    val (infds, outfds, errfds) = createPipes(builder)

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

      if (builder.redirectErrorStream())
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

      def closeSpawn(fd: CInt): Unit = UnixProcess.throwOnErrnum(
        posix_spawn_file_actions_addclose(fileActions, fd),
        s"posix_spawn_file_actions_addclose fd: $fd"
      )

      def closePipe(pipe: Ptr[CInt]): Unit =
        if (pipe ne null) {
          closeSpawn(!(pipe + 0))
          closeSpawn(!(pipe + 1))
        }

      closePipe(infds)
      closePipe(outfds)
      closePipe(errfds)

      /* This will exec binary executables.
       * Some shells (bash, ???) will also execute scripts with initial
       * shebang (#!).
       */
      @tailrec
      def spawn(exec: String, argv: Ptr[CString], attempt: Int = 0): CInt = {
        val status = posix_spawn(
          pidPtr,
          toCString(exec),
          fileActions,
          null, // attrp
          argv,
          envp
        )
        if (status == EAGAIN && attempt < 4) {
          // start with 16ms, end with 128ms
          Thread.sleep(2 << (attempt + 4))
          spawn(exec, argv, attempt + 1)
        } else status
      }

      var status = ENOEXEC
      val execIter = binaryPaths(builder.environment(), cmd.get(0))

      while (status == ENOEXEC && execIter.hasNext)
        status = spawn(execIter.next(), argv)

      if (status == ENOEXEC) { // try falling back to shell script
        val shCmd = "/bin/sh"
        val fallbackCmd = Array(shCmd, "-c", cmd.scalaOps.mkString(sep = " "))
        status = spawn(shCmd, nullTerminate(ju.Arrays.asList(fallbackCmd)))
      }

      UnixProcess.throwOnErrnum(status, "Unable to posix_spawn process")

      success = true
      val handle = new UnixProcessHandle(!pidPtr)(builder)
      UnixProcess(handle, infds, outfds, errfds)
    } finally {
      if (!success) {
        def closePipe(pipe: Ptr[CInt]): Unit =
          if (pipe ne null) {
            unistd.close(!(pipe + 0))
            unistd.close(!(pipe + 1))
          }
        closePipe(infds)
        closePipe(outfds)
        closePipe(errfds)
      }
      UnixProcess.throwOnErrnum(
        posix_spawn_file_actions_destroy(fileActions),
        "posix_spawn_file_actions_destroy"
      )
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

  private def createPipe(what: String, redirect: ProcessBuilder.Redirect)(
      implicit z: Zone
  ): Ptr[CInt] =
    if (redirect.`type`() != ProcessBuilder.Redirect.Type.PIPE) null
    else {
      val fds = alloc[CInt](2)
      UnixProcess.throwOnError(
        unistd.pipe(fds),
        s"Couldn't create $what pipe."
      )
      fds
    }

  private def createPipes(
      pb: ProcessBuilder
  )(implicit z: Zone): (Ptr[CInt], Ptr[CInt], Ptr[CInt]) = {
    val infds: Ptr[CInt] = createPipe("input", pb.redirectInput())
    val outfds: Ptr[CInt] = createPipe("output", pb.redirectOutput())
    val errfds =
      if (pb.redirectErrorStream()) null
      else createPipe("error", pb.redirectError())
    (infds, outfds, errfds)
  }

  private def dup2(
      oldfd: CInt,
      newfd: CInt,
      what: String
  ): Unit = UnixProcess.throwOnErrorRetryEINTR(
    unistd.dup2(oldfd, newfd),
    s"Couldn't duplicate $what file descriptor"
  )

  private def setupChildFDS(
      childFd: => CInt,
      redirect: ProcessBuilder.Redirect,
      procFd: CInt
  )(implicit z: Zone): Unit = {
    def openWith(flags: CInt, what: String): Unit = {
      val file = redirect.file()
      val mode = getModeFromOpenFlags(flags)
      val fd = UnixProcess.throwOnErrorRetryEINTR(
        fcntl.open(getFileNameCString(file), flags, mode),
        s"Unable to open $what file $file"
      )
      dup2(fd, procFd, what)
      unistd.close(fd)
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
  ): Unit = UnixProcess.throwOnErrnum(
    posix_spawn_file_actions_adddup2(fileActions, oldfd, newfd),
    s"Could not adddup2 $what file descriptor $newfd"
  )

  private def setupSpawnFDS(
      fileActions: Ptr[posix_spawn_file_actions_t],
      childFd: => CInt,
      redirect: ProcessBuilder.Redirect,
      procFd: CInt
  )(implicit z: Zone): Unit = {
    import fcntl.{open => _, _}
    @inline def addopen(flags: CInt, what: String): Unit = {
      val file = redirect.file()
      val f = getFileNameCString(file)
      val mode = getModeFromOpenFlags(flags)
      UnixProcess.throwOnErrnum(
        posix_spawn_file_actions_addopen(fileActions, procFd, f, flags, mode),
        s"Could not addopen $what fd=$procFd file=$file"
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
