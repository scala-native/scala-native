package java
package lang

import java.io.{File, IOException, InputStream, OutputStream}
import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.scalanative.native.{errno => err, signal => sig, _}
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
    _inputStream: PipeIO.Stream,
    _errorStream: PipeIO.Stream,
    _outputStream: OutputStream
) extends Process {
  override def destroy(): Unit = kill(pid, 9)

  override def destroyForcibly(): Process = {
    destroy()
    this
  }

  override def exitValue(): scala.Int = locked { _ =>
    _exitValue match {
      case -1 =>
        throw new IllegalThreadStateException(
          s"Process $pid has not exited yet")
      case v => v
    }
  }

  override def getErrorStream(): InputStream = _errorStream

  override def getInputStream(): InputStream = _inputStream

  override def getOutputStream(): OutputStream = _outputStream

  override def isAlive(): scala.Boolean = locked(_ => _exitValue == -1)

  override def toString = s"UnixProcess($pid)"

  override def waitFor(): scala.Int = locked { m =>
    _exitValue match {
      case -1 =>
        waitImpl(() => waitFor(m, null))
        _exitValue
      case v => v
    }
  }
  override def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean =
    locked {
      case m =>
        _exitValue match {
          case -1 =>
            val ts = stackalloc[timespec]
            val tv = stackalloc[timeval]
            throwOnError(gettimeofday(tv, null), "Failed to set time of day.")
            val nsec = unit.toNanos(timeout) + TimeUnit.MICROSECONDS.toNanos(
              !tv._2.cast[Ptr[CInt]])
            val sec = TimeUnit.NANOSECONDS.toSeconds(nsec)
            !ts._1 = !tv._1 + sec
            !ts._2 = if (sec > 0) nsec - TimeUnit.SECONDS.toNanos(sec) else nsec
            waitImpl(() => waitFor(m, ts)) == 0
          case _ => true
        }
    }

  @inline private def waitImpl(f: () => Int) = {
    var res = 1
    do res = f() while (if (res == 0) _exitValue == -1 else res != ETIMEDOUT)
    res
  }

  private[this] var _condId    = 0
  private[this] var _exitValue = -1
  private[this] val _conds     = mutable.Map.empty[Int, PtrWrapper[pthread_cond_t]]
  private def alertWaitingThreads(): Unit = {
    _conds.values.foreach(c => pthread_cond_broadcast(c.value))
  }
  private def setExitValue(value: CInt): Unit = {
    _exitValue = value
    _inputStream.drain()
    _errorStream.drain()
    _outputStream.close()
  }
  private[this] def waitFor(mutex: PtrWrapper[pthread_mutex_t],
                            ts: Ptr[timespec]): Int = Zone { implicit z =>
    val id   = { _condId += 1; _condId }
    val cond = alloc[scala.Byte](pthread_cond_t_size).cast[Ptr[pthread_cond_t]]
    pthread_cond_init(cond, null)
    _conds += id -> PtrWrapper(cond)
    val res =
      if (ts != null) pthread_cond_timedwait(cond, mutex.value, ts)
      else pthread_cond_wait(cond, mutex.value)
    _conds -= id
    pthread_cond_destroy(cond)
    res
  }
}

object UnixProcess {
  @link("pthread")
  @extern
  private[this] object ProcessMonitor {
    type proc_info = CStruct2[CInt, CInt]
    @name("scalanative_process_monitor_init")
    def init(): Unit = extern

    @name("scalanative_process_monitor_last_proc_info")
    def lastProcInfo(info: Ptr[proc_info]): Unit = extern

    @name("scalanative_process_monitor_shared_mutex")
    def sharedMutex(): Ptr[pthread_mutex_t] = extern

    @name("scalanative_process_monitor_wakeup")
    def wakeup(): CInt = extern
  }
  ProcessMonitor.init()

  case class PtrWrapper[T](value: Ptr[T])
  def locked[R](f: PtrWrapper[pthread_mutex_t] => R): R = {
    val mutex = ProcessMonitor.sharedMutex()
    try {
      throwOnError(pthread_mutex_lock(mutex), "Couldn't lock shared mutex.")
      f(PtrWrapper(mutex))
    } finally {
      throwOnError(pthread_mutex_unlock(mutex), "Couldn't unlock shared mutex.")
    }
  }
  def apply(builder: ProcessBuilder): Process = Zone { implicit z =>
    import scala.collection.JavaConverters._
    val infds  = stackalloc[CInt](2)
    val outfds = stackalloc[CInt](2)
    val errfds =
      if (builder.redirectErrorStream) outfds else stackalloc[CInt](2)

    throwOnError(unistd.pipe(infds), s"Couldn't create pipe.")
    throwOnError(unistd.pipe(outfds), s"Couldn't create pipe.")
    if (!builder.redirectErrorStream)
      throwOnError(unistd.pipe(errfds), s"Couldn't create pipe.")

    val cmd  = builder.command.asScala
    val dir  = builder.directory
    val argv = nullTerminate(cmd)
    val envp = nullTerminate(builder.environment.asScala.map {
      case (k, v) => s"$k=$v"
    }.toSeq)
    val binaries = binaryPaths(builder.environment, cmd.head)

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
          unistd.execve(toCString(b), argv, envp)
        }
        // The spec of vfork requires calling _exit if the child process fails to execve.
        unistd._exit(1)
        throw new IOException(s"Failed to create process for command: $cmd")
      case pid =>
        Seq(!(outfds + 1), !(errfds + 1), !infds) foreach unistd.close
        val res = new UnixProcess(
          pid,
          PipeIO[PipeIO.Stream](!outfds, builder.redirectOutput),
          PipeIO[PipeIO.Stream](!errfds, builder.redirectError),
          PipeIO[OutputStream](!(infds + 1), builder.redirectInput)
        )
        processes += pid -> res
        res
    }
  }

  private val processes =
    scala.collection.mutable.HashMap.empty[CInt, UnixProcess]
  private def signalHandler(sig: CInt): Unit = {
    val procInfo = stackalloc[ProcessMonitor.proc_info]
    ProcessMonitor.lastProcInfo(procInfo)
    val pid = !procInfo._1
    processes get pid match {
      case Some(process) =>
        process.setExitValue(!procInfo._2)
        processes -= pid
        process.alertWaitingThreads()
      case _ =>
    }
    throwOnError(ProcessMonitor.wakeup(), "Couldn't wake up process monitor.")
  }
  signal(SIGUSR1, CFunctionPtr.fromFunction1(signalHandler))

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
