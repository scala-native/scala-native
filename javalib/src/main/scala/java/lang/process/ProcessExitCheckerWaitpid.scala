package java.lang.process

import java.util.concurrent.TimeUnit

private[process] object ProcessExitCheckerWaitpid
    extends ProcessExitChecker.Factory {

  override def createSingle(pid: Int)(implicit
      pr: ProcessRegistry
  ): ProcessExitChecker =
    new Single(pid)

  private class Single(pid: Int)(implicit pr: ProcessRegistry)
      extends ProcessExitChecker {
    override def close(): Unit = {}

    // return positive if something has been reaped, 0 if timed out
    def waitAndReapSome(timeout: Long, unitOpt: Option[TimeUnit]): Boolean =
      waitAndReap(pid, timeout, unitOpt)
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
