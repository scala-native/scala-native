package java.net

import java.io.{FileDescriptor, IOException}
import scala.scalanative.posix.sys.{socket => unixsocket}
import scala.scalanative.unsafe.stackalloc
import scala.scalanative.unsigned._
import scala.scalanative.windows._

object WindowsNet extends Net {
  import WinSocketApi._
  import WinSocketApiExt._
  import WinSocketApiOps._

  WinSocketApiOps.init()

  override def socket(family: ProtocolFamily, stream: Boolean): FileDescriptor = {
    val addressFamily = family match {
      case StandardProtocolFamily.INET  => unixsocket.AF_INET
      // case StandardProtocolFamily.INET6 => unixSocket.AF_INET6
      case _ =>
        throw new UnsupportedOperationException("Protocol family not supported")
    }
    val socketType = if (stream) unixsocket.SOCK_STREAM else unixsocket.SOCK_DGRAM

    val socket = WSASocketW(
      addressFamily = addressFamily,
      socketType =socketType,
      protocol = 0, // chosen by provider
      protocolInfo = null,
      group = 0.toUInt,
      flags = WSA_FLAG_OVERLAPPED
    )
    if (socket == InvalidSocket) {
      throw new IOException(s"Couldn't create socket: ${WSAGetLastError()}")
    }

    new FileDescriptor(FileDescriptor.FileHandle(socket), readOnly = false)
  }

  override def close(fd: FileDescriptor): Unit = closeSocket(fd.handle)

  def setSocketBlocking(fd: FileDescriptor, blocking: Boolean): Unit = {
    val mode = stackalloc[Int]()
    !mode = if (blocking) 0 else 1
    if (ioctlSocket(fd.handle, FIONBIO, mode) != 0) {
      throw new SocketException(
        s"Failed to set socket ${if (!blocking) "non-" else ""}blocking"
      )
    }
  }
}
