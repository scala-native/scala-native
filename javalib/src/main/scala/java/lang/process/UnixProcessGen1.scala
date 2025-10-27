package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.posix
import scala.scalanative.unsafe._

private[process] class UnixProcessHandleGen1(
    override protected val _pid: CInt,
    override val builder: ProcessBuilder
) extends UnixProcessHandle {

  override protected final def close(): Unit = {}

  override def waitFor(): Boolean =
    try {
      completion.get()
      true
    } catch {
      case _: Throwable => false
    }

  override def waitFor(timeout: scala.Long, unit: TimeUnit): Boolean =
    try {
      if (timeout > 0L) completion.get(timeout, unit)
      true
    } catch {
      case _: Throwable => false
    }

  override protected def waitForImpl(): Boolean = false

  override protected def waitForImpl(timeout: Long, unit: TimeUnit): Boolean =
    false

  override protected def getExitCodeImpl: Option[Int] =
    UnixProcess.waitpidNowNoECHILD(_pid)

}

private[process] object UnixProcessGen1 {

  def apply(
      builder: ProcessBuilder
  ): GenericProcess = Zone.acquire { implicit z =>
    UnixProcessGen2.forkChild(builder)(new UnixProcessHandleGen1(_, _))
  }

  def waitpidAny(): Boolean = {
    // no choice but to reap a child, whether it's ours or not
    val wstatus = stackalloc[Int]()
    val pid = posix.sys.wait.wait(wstatus)
    pid != -1 && GenericProcessWatcher.completeWith(pid) {
      UnixProcess.getExitCodeFromWaitStatus(!wstatus)
    }
  }

}
