package java.lang.process

import java.io.FileDescriptor
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe.{CInt, Ptr}

private[process] abstract class UnixProcess extends GenericProcess {
  protected val _pid: CInt
  protected val infds: Ptr[CInt]
  protected val outfds: Ptr[CInt]
  protected val errfds: Ptr[CInt]

  override protected def fdIn = new FileDescriptor(!(infds + 1))
  override protected def fdOut = new FileDescriptor(!outfds, readOnly = true)
  override protected def fdErr = new FileDescriptor(!errfds, readOnly = true)

  override final def pid(): Long = _pid.toLong

  override final protected def close(): Unit = {}
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
