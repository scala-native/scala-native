package java.lang.process

import java.io.{FileDescriptor, IOException}

import scala.annotation.tailrec

import scala.scalanative.libc.{LibcExt, signal => csig}
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix
import scala.scalanative.posix.{signal => psig}
import scala.scalanative.unsafe._

private[process] abstract class UnixProcessHandle extends GenericProcessHandle {
  protected val _pid: CInt

  override final def pid(): Long = _pid.toLong
  override final def supportsNormalTermination(): Boolean = true

  override protected final def destroyImpl(force: Boolean): Boolean =
    psig.kill(_pid, if (force) psig.SIGKILL else csig.SIGTERM) == 0
}

private[process] object UnixProcess {
  import posix.errno._
  import posix.sys.wait._

  def apply(
      stdin: FileDescriptor,
      stdout: FileDescriptor,
      stderr: FileDescriptor
  )(handle: UnixProcessHandle): GenericProcess = new GenericProcess(handle) {
    override protected def fdIn: FileDescriptor = stdin
    override protected def fdOut: FileDescriptor = stdout
    override protected def fdErr: FileDescriptor = stderr
  }

  private def getFileDescriptor(fds: Ptr[CInt], read: Boolean): FileDescriptor =
    if (null == fds) FileDescriptor.none
    else {
      val idx = if (read) 0 else 1
      posix.unistd.close(!(fds + 1 - idx)) // close the other one
      new FileDescriptor(!(fds + idx), readOnly = read)
    }

  def apply(
      handle: UnixProcessHandle,
      infds: Ptr[CInt],
      outfds: Ptr[CInt],
      errfds: Ptr[CInt]
  ): GenericProcess = apply(
    getFileDescriptor(infds, read = false),
    getFileDescriptor(outfds, read = true),
    getFileDescriptor(errfds, read = true)
  )(handle)

  private[process] val useGen2 =
    if (LinktimeInfo.is32BitPlatform) {
      false
    } else if (LinktimeInfo.isLinux) {
      LinuxOsSpecific.hasPidfdOpen()
    } else if ((LinktimeInfo.isMac) || (LinktimeInfo.isFreeBSD)) {
      // Other BSDs should work but have not been exercised.
      true
    } else {
      false
    }

  def apply(pb: ProcessBuilder): GenericProcess = {
    if (useGen2) UnixProcessGen2(pb) else UnixProcessGen1(pb)
  }

  def getExitCodeFromWaitStatus(wstatus: Int): Int = {
    // https://tldp.org/LDP/abs/html/exitcodes.html
    if (WIFEXITED(wstatus)) WEXITSTATUS(wstatus)
    else if (WIFSIGNALED(wstatus)) 128 + WTERMSIG(wstatus)
    else 1 // Catchall for general errors
  }

  def waitpidNoECHILD(pid: pid_t, options: Int): Option[Int] = {
    val wstatus = stackalloc[Int]()
    val res = throwOnErrorRetryEINTR { e =>
      e != ECHILD // see SN issues #4208 and #4348
    }(waitpid(pid, wstatus, options), "waitpid failed")
    if (res == 0 || res == -1) None
    else Some(getExitCodeFromWaitStatus(!wstatus))
  }

  def waitpidNowNoECHILD(pid: pid_t): Option[Int] =
    waitpidNoECHILD(pid, WNOHANG)

  def throwWith[A](rc: A, msg: => String): Nothing =
    throw new IOException(
      s"$msg [res=$rc, errno=$errno]: ${LibcExt.strError()}"
    )

  def throwIf[A](rc: A, msg: => String)(f: A => Boolean): A = {
    if (f(rc)) throwWith(rc, msg)
    rc
  }

  @inline
  def throwOnError(rc: CInt, msg: => String): CInt =
    throwIf(rc, msg)(_ == -1)

  @inline
  def throwOnNonZero(rc: CInt, msg: => String): CInt =
    throwIf(rc, msg)(_ != 0)

  @inline
  def throwOnErrorRetryEINTR(rc: => CInt, msg: => String): CInt = {
    throwOnErrorRetryEINTR(_ => true)(rc, msg)
  }

  @tailrec
  def throwOnErrorRetryEINTR(
      f: Int => Boolean
  )(rc: => CInt, msg: => String): CInt = {
    val res = rc
    if (res != -1) res
    else if (errno == EINTR) {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedException()
      throwOnErrorRetryEINTR(f)(rc, msg)
    } else {
      if (f(errno)) throwWith(res, msg)
      res
    }
  }

}
