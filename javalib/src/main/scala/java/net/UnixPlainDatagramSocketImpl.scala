package java.net

import java.io.{FileDescriptor, IOException}

import scala.annotation.tailrec

import scala.scalanative.posix
import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[net] class UnixPlainDatagramSocketImpl
    extends AbstractPlainDatagramSocketImpl {

  override def create(): Unit = {
    val af =
      if (SocketHelpers.getUseIPv4Stack()) posix.sys.socket.AF_INET
      else posix.sys.socket.AF_INET6
    val sock = posix.sys.socket.socket(af, posix.sys.socket.SOCK_DGRAM, 0)
    if (sock < 0)
      throw new IOException(
        s"Could not create a socket in address family: ${af}"
      )

    // enable broadcast by default
    val broadcastPrt = stackalloc[CInt]()
    !broadcastPrt = 1
    if (posix.sys.socket.setsockopt(
          sock,
          posix.sys.socket.SOL_SOCKET,
          posix.sys.socket.SO_BROADCAST,
          broadcastPrt.asInstanceOf[Ptr[Byte]],
          sizeof[CInt].toUInt
        ) < 0) {
      unistd.close(sock)
      throw new IOException(s"Could not set SO_BROADCAST on socket: $errno")
    }

    fd = new FileDescriptor(sock)
  }
  protected def tryPoll(op: String): Unit = {
    val nAlloc = 1.toUInt
    val pollFd: Ptr[struct_pollfd] = stackalloc[struct_pollfd](nAlloc)

    pollFd.fd = fd.fd
    pollFd.revents = 0
    pollFd.events = POLLIN

    val pollRes = poll(pollFd, nAlloc, timeout)
    val revents = pollFd.revents

    pollRes match {
      case err if err < 0 =>
        throw new SocketException(s"${op} failed, poll errno: $errno")

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
        s"${op} poll failed, invalid poll request: ${revents}"
      )
    } else if (((revents & POLLIN) | (revents & POLLOUT)) == 0) {
      throw new SocketException(
        s"${op} poll failed, neither POLLIN nor POLLOUT set, " +
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
        s"connect failed, fcntl F_GETFL, errno: $errno"
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
          s"fcntl F_SETFL for opts: $opts, errno: $errno"
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
