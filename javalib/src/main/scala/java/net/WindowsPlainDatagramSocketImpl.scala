package java.net

import java.io.{FileDescriptor, IOException}
import scala.scalanative.posix.sys.{socket => unixSocket}
import scala.scalanative.posix.errno._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._
import scala.annotation.tailrec
private[net] class WindowsPlainDatagramSocketImpl
    extends AbstractPlainDatagramSocketImpl {
  import WinSocketApi._
  import WinSocketApiExt._
  import WinSocketApiOps._

  override def create(): Unit = {
    WinSocketApiOps.init()
    val socket = WSASocketW(
      addressFamily = unixSocket.AF_INET,
      socketType = unixSocket.SOCK_DGRAM,
      protocol = 0, // choosed by provider
      protocolInfo = null,
      group = 0.toUInt,
      flags = WSA_FLAG_OVERLAPPED
    )
    if (socket == InvalidSocket) {
      throw new IOException(s"Couldn't create a socket: ${WSAGetLastError()}")
    }

    val fileHandle = FileDescriptor.FileHandle(socket)

    // enable broadcast by default
    val broadcastPrt = stackalloc[CInt]()
    !broadcastPrt = 1
    if (unixSocket.setsockopt(
          fileHandle.toInt,
          unixSocket.SOL_SOCKET,
          unixSocket.SO_BROADCAST,
          broadcastPrt.asInstanceOf[Ptr[Byte]],
          sizeof[CInt].toUInt
        ) < 0) {
      closeSocket(socket)
      throw new IOException(s"Could not set SO_BROADCAST on socket: $errno")
    }

    fd = new FileDescriptor(fileHandle, readOnly = false)
  }

  protected def tryPoll(op: String): Unit = {
    val nAlloc = 1.toUInt
    val pollFd: Ptr[WSAPollFd] = stackalloc[WSAPollFd](nAlloc)

    pollFd.socket = fd.handle
    pollFd.revents = 0.toShort
    pollFd.events = POLLIN.toShort

    val pollRes = WSAPoll(pollFd, nAlloc, timeout)
    val revents = pollFd.revents

    pollRes match {
      case err if err < 0 =>
        throw new SocketException(
          s"${op} failed, poll errno: ${WSAGetLastError()}"
        )

      case 0 =>
        throw new SocketTimeoutException(
          s"${op} timed out, SO_TIMEOUT: ${timeout}"
        )

      case _ => // success, carry on
    }

    if (((revents & POLLERR) | (revents & POLLHUP)) != 0) {
      throw new SocketException(s"${op} poll failed, POLLERR or POLLHUP")
    } else if ((revents & POLLNVAL) != 0) {
      throw new SocketException(
        s"${op} failed, invalid poll request: ${revents}"
      )
    } else if (((revents & POLLIN) | (revents & POLLOUT)) == 0) {
      throw new SocketException(
        s"${op} failed, neither POLLIN nor POLLOUT set, revents, ${revents}"
      )
    }
  }

  protected def setSocketFdBlocking(
      fd: FileDescriptor,
      blocking: Boolean
  ): Unit = {
    val mode = stackalloc[Int]()
    if (blocking)
      !mode = 0
    else
      !mode = 1
    if (ioctlSocket(fd.handle, FIONBIO, mode) != 0)
      throw new SocketException(
        s"Failed to set socket ${if (!blocking) "non-" else ""}blocking"
      )
  }
}
