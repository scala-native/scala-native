package java.net

import java.io.{FileDescriptor, IOException}
import scala.scalanative.libc._
import scala.scalanative.posix.sys.{socket => unixSocket}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._

private[net] class WindowsPlainSocketImpl extends AbstractPlainSocketImpl {
  import WinSocketApi._
  import WinSocketApiExt._
  import WinSocketApiOps._

  override def create(streaming: Boolean): Unit = {
    WinSocketApiOps.init()
    val socket = WSASocketW(
      addressFamily = unixSocket.AF_INET,
      socketType = unixSocket.SOCK_STREAM,
      protocol = 0, // choosed by provider
      protocolInfo = null,
      group = 0.toUInt,
      flags = WSA_FLAG_OVERLAPPED
    )
    if (socket == InvalidSocket) {
      throw new IOException(s"Couldn't create a socket: ${WSAGetLastError()}")
    }
    fd = new FileDescriptor(
      FileDescriptor.FileHandle(socket),
      readOnly = false
    )
  }

  protected def tryPollOnConnect(timeout: Int): Unit = {
    val nAlloc = 1.toUInt
    val pollFd = stackalloc[WSAPollFd](nAlloc)

    pollFd.socket = fd.handle
    pollFd.revents = 0.toShort
    pollFd.events = (POLLIN | POLLOUT).toShort

    val pollRes = WSAPoll(pollFd, nAlloc, timeout)
    val revents = pollFd.revents

    setSocketFdBlocking(fd, blocking = true)

    pollRes match {
      case err if err < 0 =>
        throw new SocketException(s"connect failed, poll errno: ${errno.errno}")

      case 0 =>
        throw new SocketTimeoutException(
          s"connect timed out, SO_TIMEOUT: ${timeout}"
        )

      case _ =>
        if ((revents & POLLNVAL) != 0) {
          throw new ConnectException(
            s"connect failed, invalid poll request: ${revents}"
          )
        } else if ((revents & (POLLERR | POLLHUP)) != 0) {
          throw new ConnectException(
            s"connect failed, POLLERR or POLLHUP set: ${revents}"
          )
        }
    }
  }

  protected def tryPollOnAccept(): Unit = {
    val nAlloc = 1.toUInt
    val pollFd = stackalloc[WSAPollFd](nAlloc)

    pollFd.socket = fd.handle
    pollFd.revents = 0.toShort
    pollFd.events = POLLIN.toShort

    val pollRes = WSAPoll(pollFd, nAlloc, timeout)
    val revents = pollFd.revents

    pollRes match {
      case err if err < 0 =>
        throw new SocketException(s"accept failed, poll errno: ${errno.errno}")

      case 0 =>
        throw new SocketTimeoutException(
          s"accept timed out, SO_TIMEOUT: ${timeout}"
        )

      case _ => // success, carry on
    }

    if (((revents & POLLERR) | (revents & POLLHUP)) != 0) {
      throw new SocketException("Accept poll failed, POLLERR or POLLHUP")
    } else if ((revents & POLLNVAL) != 0) {
      throw new SocketException(
        s"accept failed, invalid poll request: ${revents}"
      )
    } else if (((revents & POLLIN) | (revents & POLLOUT)) == 0) {
      throw new SocketException(
        "accept failed, neither POLLIN nor POLLOUT set, " +
          s"revents, ${revents}"
      )
    }
  }

  protected def setSocketFdBlocking(
      fd: FileDescriptor,
      blocking: Boolean
  ): Unit = {
    val mode = stackalloc[Int]
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
