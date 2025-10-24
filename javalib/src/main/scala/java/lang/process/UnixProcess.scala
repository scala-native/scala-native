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

private[process] trait UnixProcessHandleFactory {
  def create(pid: CInt, builder: ProcessBuilder): UnixProcessHandle
}

private[process] object UnixProcess {
  def apply(
      stdin: FileDescriptor,
      stdout: FileDescriptor,
      stderr: FileDescriptor
  )(handle: UnixProcessHandle): GenericProcess = new GenericProcess(handle) {
    override protected def fdIn: FileDescriptor = stdin
    override protected def fdOut: FileDescriptor = stdout
    override protected def fdErr: FileDescriptor = stderr
  }

  def apply(
      infds: Ptr[CInt],
      outfds: Ptr[CInt],
      errfds: Ptr[CInt]
  )(pid: CInt, pb: ProcessBuilder)(implicit
      factory: UnixProcessHandleFactory
  ): GenericProcess = apply(
    new FileDescriptor(!(infds + 1)),
    new FileDescriptor(!outfds, readOnly = true),
    if (null == errfds) new FileDescriptor()
    else new FileDescriptor(!errfds, readOnly = true)
  )(factory.create(pid, pb))

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
