package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.meta.LinktimeInfo

/** Provides ability to query a process or processes for their exit. Doesn't
 *  actively do anything unless asked.
 */
private[process] trait ProcessExitChecker {
  def close(): Unit
  // return positive if something has been reaped, 0 if timed out
  def waitAndReapSome(timeout: Long, unitOpt: Option[TimeUnit]): Boolean
}

private[process] object ProcessExitChecker {

  trait Multi extends ProcessExitChecker {

    /** If the process is running, register it and return true.
     *
     *  If the process isn't running, reap the process and return false.
     *
     *  Make sure to add it to the process registry before checker can reap this
     *  process and call `complete` on the registry.
     */
    def addOrReap(handle: GenericProcessHandle): Boolean
  }

  trait Factory {
    def createMulti(implicit pr: ProcessRegistry): Multi

    /** @return None if process has exited */
    def createSingle(pid: Int)(implicit
        pr: ProcessRegistry
    ): ProcessExitChecker
  }

  val unixFactoryOpt: Option[Factory] =
    if (LinktimeInfo.is32BitPlatform) None // TODO: wonder why
    else if (LinktimeInfo.isLinux)
      if (!scala.scalanative.linux.pidfd.has_pidfd_open()) None
      else Some(ProcessExitCheckerLinux)
    else if (LinktimeInfo.isMac || LinktimeInfo.isFreeBSD)
      Some(ProcessExitCheckerFreeBSD) // Other BSDs should work but untested
    else None

  val factoryOpt: Option[Factory] =
    if (LinktimeInfo.isWindows) None
    else unixFactoryOpt.orElse(Some(ProcessExitCheckerWaitpid))

}
