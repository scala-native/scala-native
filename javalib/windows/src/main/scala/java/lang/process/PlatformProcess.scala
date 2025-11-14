package java.lang.process

import scala.scalanative.windows

private[process] object PlatformProcess {

  def apply(pb: ProcessBuilder): GenericProcess = WindowsProcess(pb)

  type PidType = windows.DWord
  type PlatformProcessHandle = WindowsProcessHandle

  // return true if something has been reaped
  val reapSomeProcesses: Option[() => Boolean] = None

}
