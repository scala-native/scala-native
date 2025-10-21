package java.net

import java.io.{FileDescriptor, IOException}

import scala.annotation.tailrec

import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix.sys.socket
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

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
          s" streaming: ${streaming}"
      )

    fd = new FileDescriptor(sock)
  }

  protected final def tryPollOnConnect(timeout: Int): Unit = {
    val hasTimeout = timeout > 0
    val deadline = if (hasTimeout) System.currentTimeMillis() + timeout else 0L
    val nAlloc = 1.toUInt
    val pollFd: Ptr[struct_pollfd] = stackalloc[struct_pollfd](nAlloc)

    pollFd.fd = fd.fd
    pollFd.revents = 0
    pollFd.events = (POLLIN | POLLOUT).toShort

    def failWithTimeout() = throw new SocketTimeoutException(
      s"connect timed out, SO_TIMEOUT: ${timeout}"
    )

    @tailrec def loop(remainingTimeout: Int): Unit = {
      val pollRes = poll(pollFd, nAlloc, remainingTimeout)
      val revents = pollFd.revents

      pollRes match {
        case err if err < 0 =>
          val errCode = errno
          if (errCode == EINTR && hasTimeout) {
            val remaining = deadline - System.currentTimeMillis()
            if (remaining > 0) loop(remaining.toInt)
            else failWithTimeout()
          } else
            throw new SocketException(s"connect failed, poll errno: $errCode")

        case 0 => failWithTimeout()

        case _ =>
          if ((revents & POLLNVAL) != 0) {
            val msg = s"connect failed, invalid poll request: ${revents}"
            throw new ConnectException(msg)
          } else if ((revents & (POLLIN | POLLHUP)) != 0) {
            // Not enough information at this point to report remote host:port.
            val msg = "Connection refused"
            throw new ConnectException(msg)
          } else if ((revents & POLLERR) != 0) { // an error was recognized.
            val msg = s"connect failed, poll POLLERR: ${revents}"
            throw new ConnectException(msg)
          } // else should be POLLOUT - Open for Business, ignore XSI bits if set
      }
    }

    try loop(timeout)
    finally setSocketFdBlocking(fd, blocking = true)

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
        throw new SocketException(s"accept failed, poll errno: $errno")

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
