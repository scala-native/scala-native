package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix
import java.io.{FileDescriptor, IOException}
import scala.annotation.tailrec
import scala.scalanative.posix.unistd

private[net] class UnixPlainDatagramSocketImpl
    extends AbstractPlainDatagramSocketImpl {

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
}
