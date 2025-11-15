package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.linux
import scala.scalanative.posix._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[process] object ProcessExitCheckerLinux
    extends ProcessExitChecker.Factory {

  import linux.pidfd._
  import linux.ppoll._

  override def createSingle(pid: Int)(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker =
    getPidFd(pid).fold {
      reapWhenNotRunning(pid)
      ProcessExitCheckerWaitpid.createSingle(pid)
    }(fd => new Single(pid, UnixFileDescriptorAtomic(fd)))

  private def getPidFd(pid: Int): Option[CInt] = {
    val pidfd = UnixProcess.throwOnError(_ != errno.ESRCH)(
      pidfd_open(pid, 0.toUInt),
      s"pidfd_open($pid) failed"
    )
    if (pidfd < 0) None else Some(pidfd)
  }

  /* We use ppoll here as an epoll descriptor may not be persisted; if multiple
   * threads called `waitFor` simultaneously, there would be multiple concurrent
   * calls to `epoll_wait` on the same descriptor, and the kernel would not
   * necessarily unblock all of them. */
  private class Single(pid: Int, pidfd: UnixFileDescriptorAtomic)(implicit
      pr: ProcessRegistry
  ) extends ProcessExitChecker {

    override def close(): Unit = pidfd.close()

    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean = {
      import poll._
      import pollOps._

      val curpidfd = pidfd.get()
      curpidfd != -1 && {
        val ppollfd = stackalloc[struct_pollfd](1)
        ppollfd.fd = curpidfd
        ppollfd.events = (pollEvents.POLLIN | pollEvents.POLLRDNORM).toShort

        val ts = stackalloc[time.timespec]()
        val tsOrNull = unitOpt.map { unit =>
          UnixProcess.toTimespec(timeout, unit, ts)
          ts
        }.orNull

        val res = UnixProcess.throwOnErrorRetryEINTR(_ != errno.EBADF)(
          ppoll(ppollfd, 1.toUSize, tsOrNull, null),
          s"ppoll failed for pid=$pid"
        )

        if (res > 0) {
          reapWhenNotRunning(pid)
          close()
        }

        res != 0
      }
    }
  }

  @alwaysinline
  private def reapWhenNotRunning(pid: Int)(implicit pr: ProcessRegistry) =
    UnixProcess.waitpidAndComplete(pid, hang = false)

}
