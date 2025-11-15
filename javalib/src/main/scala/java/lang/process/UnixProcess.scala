package java.lang.process

import java.io.{FileDescriptor, IOException}
import java.util.concurrent.TimeUnit

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.LibcExt
import scala.scalanative.posix
import scala.scalanative.posix.time.timespec
import scala.scalanative.unsafe._

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

  def getExitCodeFromWaitStatus(wstatus: Int): Int = {
    // https://tldp.org/LDP/abs/html/exitcodes.html
    if (WIFEXITED(wstatus)) WEXITSTATUS(wstatus)
    else if (WIFSIGNALED(wstatus)) 128 + WTERMSIG(wstatus)
    else 1 // Catchall for general errors
  }

  def waitpidNoECHILD(
      pid: pid_t,
      hang: Boolean = false
  ): Either[Int, (pid_t, Int)] = {
    val wstatus = stackalloc[Int]()
    val options = if (hang) 0 else WNOHANG
    val res = throwOnErrorRetryEINTR { e =>
      e != ECHILD // see SN issues #4208 and #4348
    }(waitpid(pid, wstatus, options), "waitpid failed")
    if (res == 0) Left(0) // no error
    else if (res == -1) Left(errno) // ECHILD
    else Right(res -> getExitCodeFromWaitStatus(!wstatus))
  }

  def waitpidAndComplete(pid: pid_t, timeout: Long, unit: TimeUnit)(implicit
      pr: ProcessRegistry
  ): Boolean = {
    // busy-wait, waitpid has no timeout
    val deadline = System.nanoTime() + Math.max(0, unit.toNanos(timeout))

    @tailrec
    def iter(): Boolean = UnixProcess.waitpidNoECHILD(pid) match {
      case Left(x) =>
        x != 0 || {
          val remaining = deadline - System.nanoTime()
          remaining > 0 && { Thread.sleep(Math.min(remaining, 100)); iter() }
        }
      case Right((pid, x)) => pr.completeWith(pid)(x); true
    }

    iter()
  }

  def waitpidAndComplete(pid: pid_t, hang: Boolean)(implicit
      pr: ProcessRegistry
  ): Boolean =
    waitpidNoECHILD(pid, hang = hang) match {
      case Left(x)          => x != 0
      case Right((pid, ec)) => pr.completeWith(pid)(ec); true
    }

  def throwWith[A](rc: A, errnum: Int, msg: => String): Nothing =
    throw new IOException(
      s"$msg [res=$rc, errno=$errnum]: ${LibcExt.strError(errnum)}"
    )

  @alwaysinline
  def throwWith[A](rc: A, msg: => String): Nothing =
    throwWith(rc, errno, msg)

  def throwIf[A](rc: A, errnum: => Int, msg: => String)(f: A => Boolean): A = {
    if (f(rc)) throwWith(rc, errnum, msg)
    rc
  }

  @alwaysinline
  def throwIf[A](rc: A, msg: => String)(f: A => Boolean): A =
    throwIf(rc, errno, msg)(f)

  @alwaysinline
  def throwIfNull[A <: AnyRef](obj: A, msg: => String): A =
    throwIf(obj, msg)(_ eq null)

  @alwaysinline
  def throwOnError(f: Int => Boolean)(rc: CInt, msg: => String): CInt =
    throwIf(rc, msg)(_ == -1 && f(errno))

  @alwaysinline
  def throwOnError(rc: CInt, msg: => String): CInt =
    throwOnError(_ => true)(rc, msg)

  @alwaysinline
  def throwOnErrnum(rc: CInt, msg: => String): CInt =
    throwIf(rc, rc, msg)(_ != 0)

  @tailrec
  def retryEINTR(rc: => CInt): CInt = {
    val res = rc
    if (res == -1 && errno == EINTR) {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedException()
      retryEINTR(rc)
    } else res
  }

  @alwaysinline
  def throwOnErrorRetryEINTR(rc: => CInt, msg: => String): CInt = {
    val res = retryEINTR(rc)
    if (res == -1) throwWith(res, msg)
    res
  }

  def throwOnErrorRetryEINTR(
      f: Int => Boolean
  )(rc: => CInt, msg: => String): CInt = {
    val res = retryEINTR(rc)
    if (res == -1 && f(errno)) throwWith(res, msg)
    res
  }

  def nanosToTimespec(timeoutNanos: Long, ts: Ptr[timespec]): Unit = {
    import posix.timeOps._
    val timeoutNanosNonNeg = Math.max(0, timeoutNanos)
    val nanosPerSecond = 1000000000L
    val seconds = Math.floorDiv(timeoutNanosNonNeg, nanosPerSecond)
    ts.tv_sec = seconds.toSize
    ts.tv_nsec = (timeoutNanosNonNeg - nanosPerSecond * seconds).toSize
  }

  @alwaysinline
  def toTimespec(timeout: Long, unit: TimeUnit, ts: Ptr[timespec]): Unit =
    nanosToTimespec(unit.toNanos(timeout), ts)

}
