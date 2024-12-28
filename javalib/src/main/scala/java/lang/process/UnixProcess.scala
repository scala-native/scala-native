package java.lang.process

import scala.scalanative.meta.LinktimeInfo

private[lang] abstract class UnixProcess extends GenericProcess {}

object UnixProcess {
  def apply(builder: ProcessBuilder): Process = {
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

    if (useGen2) UnixProcessGen2(builder)
    else UnixProcessGen1(builder)
  }
}
