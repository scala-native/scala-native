package java.lang.process

import scala.scalanative.posix.sys.types

private[process] object PlatformProcess {

  def apply(pb: ProcessBuilder): GenericProcess = UnixProcess(pb)

  type PidType = types.pid_t
  type PlatformProcessHandle = UnixProcessHandle

  // return true if something has been reaped
  val reapSomeProcesses: Option[() => Boolean] =
    if (UnixProcess.useGen2) None
    else Some(UnixProcessGen1.waitpidAny)

}
