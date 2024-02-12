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
}
