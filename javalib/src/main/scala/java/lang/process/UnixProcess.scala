package java.lang.process

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe.CInt

private[process] abstract class UnixProcess extends GenericProcess {
  protected val _pid: CInt

  override final def pid(): Long = _pid.toLong
}

private[process] object UnixProcess {
  def apply(pb: ProcessBuilder): GenericProcess = {
    val useGen2 = if (LinktimeInfo.is32BitPlatform) {
      false
    } else if (LinktimeInfo.isLinux) {
      LinuxOsSpecific.hasPidfdOpen()
    } else if ((LinktimeInfo.isMac) || (LinktimeInfo.isFreeBSD)) {
      // Other BSDs should work but have not been exercised.
      true
    } else {
      false
    }

    if (useGen2) UnixProcessGen2(pb) else UnixProcessGen1(pb)
  }
}
