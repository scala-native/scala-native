package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix.sys.socket

import java.io.{FileDescriptor, IOException}

private[net] class UnixPlainSocketImpl extends AbstractPlainSocketImpl {

  override def create(streaming: Boolean): Unit = {
    val af =
      if (SocketHelpers.getUseIPv4Stack()) socket.AF_INET
      else socket.AF_INET6

    val sockType =
      if (streaming) socket.SOCK_STREAM
      else socket.SOCK_DGRAM

    val sock = socket.socket(af, sockType, 0)

    if (sock < 0)
      throw new IOException(
        s"Could not create a socket in address family: ${af}" +
          " streaming: ${streaming}"
      )

    fd = new FileDescriptor(sock)
  }

  protected def tryPollOnConnect(timeout: Int): Unit = {
    val nAlloc = 1.toUInt
    val pollFd: Ptr[struct_pollfd] = stackalloc[struct_pollfd](nAlloc)

    pollFd.fd = fd.fd
    pollFd.revents = 0
    pollFd.events = (POLLIN | POLLOUT).toShort

    val pollRes = poll(pollFd, nAlloc, timeout)
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
    val pollFd: Ptr[struct_pollfd] = stackalloc[struct_pollfd](nAlloc)

    pollFd.fd = fd.fd
    pollFd.revents = 0
    pollFd.events = POLLIN

    val pollRes = poll(pollFd, nAlloc, timeout)
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
    updateSocketFdOpts(fd.fd) { oldOpts =>
      if (blocking) oldOpts & ~O_NONBLOCK
      else oldOpts | O_NONBLOCK
    }
  }

  @inline
  private def getSocketFdOpts(fdFd: Int): CInt = {
    val opts = fcntl(fdFd, F_GETFL, 0)

    if (opts == -1) {
      throw new ConnectException(
        "connect failed, fcntl F_GETFL" +
          s", errno: ${errno.errno}"
      )
    }

    opts
  }

  @inline
  private def setSocketFdOpts(fdFd: Int, opts: Int): Unit = {
    val ret = fcntl(fdFd, F_SETFL, opts)

    if (ret == -1) {
      throw new ConnectException(
        "connect failed, " +
          s"fcntl F_SETFL for opts: ${opts}" +
          s", errno: ${errno.errno}"
      )
    }
  }

  @inline
  private def updateSocketFdOpts(fdFd: Int)(mapping: CInt => CInt): Int = {
    val oldOpts = getSocketFdOpts(fdFd)
    setSocketFdOpts(fdFd, mapping(oldOpts))
    oldOpts
  }
}
