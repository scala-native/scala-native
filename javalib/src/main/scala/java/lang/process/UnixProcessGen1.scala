package java.lang.process

import java.util.concurrent.{TimeUnit, TimeoutException}

import scala.scalanative.unsafe._

private[process] class UnixProcessHandleGen1(
    override protected val _pid: CInt,
    override val builder: ProcessBuilder
) extends UnixProcessHandle {

  override protected final def close(): Unit = {}

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

private[process] object UnixProcessGen1 {

  def apply(
      builder: ProcessBuilder
  ): GenericProcess = Zone.acquire { implicit z =>
    UnixProcessGen2.forkChild(builder)(new UnixProcessHandleGen1(_, _))
  }

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
  def waitpidAny(): Boolean =
    // no choice but to reap a child, whether it's ours or not
    UnixProcess.waitpidAndComplete(-1, hang = true)(
      GenericProcessWatcher.processRegistry
    )

}
