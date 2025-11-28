package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.javalib.io.ObjectHandle

private[process] object ProcessExitCheckerWaitpid
    extends ProcessExitChecker.MultiFactory {

  override def createMulti(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker.Multi = new Multi

  override def createSingle(procesId: ObjectHandle)(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker =
    new Single(procesId.asInt)

  private class Single(pid: Int)(implicit pr: ProcessRegistry)
      extends ProcessExitChecker {
    override def close(): Unit = {}

    // return positive if something has been reaped, 0 if timed out
    def waitAndReapSome(timeout: Long, unitOpt: Option[TimeUnit]): Boolean =
      waitAndReap(pid, timeout, unitOpt)
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
   *
   *  Note: -1 passed to Single stands for "any process"
   */
  private class Multi(implicit pr: ProcessRegistry)
      extends Single(-1)
      with ProcessExitChecker.Multi {
    override def addOrReap(handle: GenericProcessHandle): Boolean = true
  }

  def waitAndReap(pid: Int, timeout: Long, unitOpt: Option[TimeUnit])(implicit
      pr: ProcessRegistry
  ): Boolean =
    unitOpt.fold {
      UnixProcess.waitpidAndComplete(pid, hang = true)
    } {
      UnixProcess.waitpidAndComplete(pid, timeout, _)
    }

}
