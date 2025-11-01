package java.lang.process

import java.util.concurrent.TimeUnit

import scala.scalanative.meta.LinktimeInfo

private[process] trait EventWatcher {
  def add(pid: Long): Unit
  def close(): Unit
  // return positive if something has been reaped, 0 if timed out
  def waitAndReapSome(timeout: Long, unitOpt: Option[TimeUnit])(
      pr: ProcessRegistry
  ): Boolean
}

private[process] object EventWatcher {

  trait Factory {
    def create(): EventWatcher
  }

  val factoryOpt: Option[Factory] =
    if (LinktimeInfo.isWindows) None
    else if (LinktimeInfo.is32BitPlatform) None // TODO: wonder why
    else if (LinktimeInfo.isLinux)
      if (!LinuxOsSpecific.hasPidfdOpen()) None
      else Some(EventWatcherLinux)
    else if (LinktimeInfo.isMac || LinktimeInfo.isFreeBSD)
      Some(EventWatcherFreeBSD) // Other BSDs should work but untested
    else None

}
