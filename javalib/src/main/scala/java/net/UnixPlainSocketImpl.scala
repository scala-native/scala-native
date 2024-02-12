package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.posix.errno._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.posix.pollOps._

import scala.annotation.tailrec

private[net] class UnixPlainSocketImpl extends AbstractPlainSocketImpl {

  final protected def tryPollOnConnect(timeout: Int): Unit = {
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
    finally UnixNet.setSocketBlocking(fd, blocking = true)

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
}
