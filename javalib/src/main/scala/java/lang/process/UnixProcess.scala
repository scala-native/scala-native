package java.lang.process

import java.io.FileDescriptor
import scala.scalanative.libc.{signal => sig}
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.signal
import scala.scalanative.unsafe.{CInt, Ptr}

private[process] abstract class UnixProcessHandle extends GenericProcessHandle {
  protected val _pid: CInt

  override final def pid(): Long = _pid.toLong
  override final def supportsNormalTermination(): Boolean = true

  override protected final def close(): Unit = {}
  override protected final def destroyImpl(force: Boolean): Boolean =
    signal.kill(_pid, if (force) signal.SIGKILL else sig.SIGTERM) == 0
}

private[process] object UnixProcess {
  def apply(
      handle: UnixProcessHandle,
      infds: Ptr[CInt],
      outfds: Ptr[CInt],
      errfds: Ptr[CInt]
  ): GenericProcess = new GenericProcess(handle) {
    override protected def fdIn = new FileDescriptor(!(infds + 1))
    override protected def fdOut = new FileDescriptor(!outfds, readOnly = true)
    override protected def fdErr = new FileDescriptor(!errfds, readOnly = true)
  }

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
