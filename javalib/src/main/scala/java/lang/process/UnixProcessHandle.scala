package java.lang.process

import java.util.concurrent.{TimeUnit, TimeoutException}

import scala.scalanative.libc.{signal => csig}
import scala.scalanative.posix
import scala.scalanative.posix.{signal => psig}
import scala.scalanative.unsafe._

private[process] class UnixProcessHandle(
    _pid: CInt,
    override val builder: ProcessBuilder
) extends GenericProcessHandle {

  override final def pid(): Long = _pid.toLong
  override final def supportsNormalTermination(): Boolean = true

  override protected final def destroyImpl(force: Boolean): Boolean =
    psig.kill(_pid, if (force) psig.SIGKILL else csig.SIGTERM) == 0

  override protected final def close(): Unit = {}

  override protected def getExitCodeImpl: Option[Int] =
    UnixProcess.waitpidNowNoECHILD(_pid)

  override def waitFor(): Boolean = {
    completion.get()
    true
  }

  override def waitFor(timeout: scala.Long, unit: TimeUnit): Boolean =
    timeout > 0L && {
      try {
        completion.get(timeout, unit)
        true
      } catch {
        case _: TimeoutException => false
      }
    }

  override protected def waitForImpl(): Boolean = false

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    false

}

private[process] object UnixProcessHandle {

  /** Reap a child and, if it's one of ours, mark it as completed in the
   *  watcher.
   *
   *  Alas, if the child is not one of ours, we can't avoid reaping it, without
   *  iterating over the heavy single-pid waitpid.
   *
   *  The only alternative was waitid(P_ALL, WNOWAIT) but if the pid returned by
   *  it is not reaped, the next iteration of waitid will produce the same one
   *  again... and again.
   */
  def waitpidAny(pr: ProcessRegistry): Boolean = {
    // no choice but to reap a child, whether it's ours or not
    val wstatus = stackalloc[Int]()
    val pid = posix.sys.wait.wait(wstatus)
    pid != -1 && pr.completeWith(pid) {
      UnixProcess.getExitCodeFromWaitStatus(!wstatus)
    }
  }

}
