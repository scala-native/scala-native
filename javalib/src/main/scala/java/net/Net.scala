package java.net

import java.io.FileDescriptor
import scala.scalanative.meta.LinktimeInfo.isWindows

trait Net {
  def socket(family: ProtocolFamily, stream: Boolean): FileDescriptor
  def close(fd: FileDescriptor): Unit
  def setSocketBlocking(fd: FileDescriptor, blocking: Boolean): Unit



}

object Net extends Net {

  private val netImpl: Net = if (isWindows) WindowsNet else UnixNet
  @inline override def socket(family: ProtocolFamily, stream: Boolean): FileDescriptor =
    netImpl.socket(family, stream)
  @inline override def close(fd: FileDescriptor): Unit =
    netImpl.close(fd)
  @inline override def setSocketBlocking(fd: FileDescriptor, blocking: Boolean): Unit =
    netImpl.setSocketBlocking(fd, blocking)
}
