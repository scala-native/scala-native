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

  trait Factory {

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
