package java.net

import java.io.{FileDescriptor, IOException}

import scala.annotation.tailrec

import scala.scalanative.libc.LibcExt
import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.unistd
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

  // Write end of the pipe used by close() to wake up a thread polling in connect
  private val connectWakeupLock = new Object
  private var connectWakeupFd: CInt = -1

  protected def useNonBlockingConnect(timeout: Int): Boolean = true

  protected def wakeUpBlockedConnect(): Unit =
    connectWakeupLock.synchronized {
      if (connectWakeupFd >= 0) {
        val buf = stackalloc[Byte]()
        unistd.write(connectWakeupFd, buf, 1.toCSize)
      }
    }

  protected final def tryPollOnConnect(
      sockFd: FileDescriptor,
      timeout: Int
  ): Unit = {
    val pipeFds = stackalloc[CInt](2)
    if (unistd.pipe(pipeFds) != 0)
      throw new SocketException(s"connect failed, pipe errno: $errno")

    connectWakeupLock.synchronized { connectWakeupFd = pipeFds(1) }
    try {
      // close() sets isClosed before signalling, so it cannot be missed here
      if (!isClosed) pollOnConnect(sockFd, pipeFds(0), timeout)
    } finally {
      connectWakeupLock.synchronized { connectWakeupFd = -1 }
      unistd.close(pipeFds(0))
      unistd.close(pipeFds(1))
    }
  }

  private def pollOnConnect(
      sockFd: FileDescriptor,
      wakeupFd: CInt,
      timeout: Int
  ): Unit = {
    val hasTimeout = timeout > 0
    val deadline = if (hasTimeout) System.currentTimeMillis() + timeout else 0L
    val nAlloc = 2.toUInt
    val pollFds: Ptr[struct_pollfd] = stackalloc[struct_pollfd](nAlloc)
    val sockPollFd = pollFds
    val wakeupPollFd = pollFds + 1

    sockPollFd.fd = sockFd.fd
    sockPollFd.events = (POLLIN | POLLOUT).toShort
    wakeupPollFd.fd = wakeupFd
    wakeupPollFd.events = POLLIN.toShort

    def failWithTimeout() = throw new SocketTimeoutException(
      s"connect timed out, SO_TIMEOUT: ${timeout}"
    )

    @tailrec def loop(remainingTimeout: Int): Unit = {
      sockPollFd.revents = 0
      wakeupPollFd.revents = 0
      val pollRes = poll(pollFds, nAlloc, remainingTimeout)
      val revents = sockPollFd.revents

      if (isClosed || wakeupPollFd.revents != 0)
        throw new SocketException("connect: Socket is closed")

      pollRes match {
        case err if err < 0 =>
          val errCode = errno
          if (errCode != EINTR)
            throw new SocketException(s"connect failed, poll errno: $errCode")
          else if (!hasTimeout) loop(-1)
          else {
            val remaining = deadline - System.currentTimeMillis()
            if (remaining > 0) loop(remaining.toInt)
            else failWithTimeout()
          }

        case 0 => failWithTimeout()

        case _ =>
          if ((revents & POLLNVAL) != 0) {
            val msg = s"connect failed, invalid poll request: ${revents}"
            throw new ConnectException(msg)
          }
          val soError = pendingSocketError(sockFd)
          if (soError == ECONNREFUSED)
            throw new ConnectException("Connection refused")
          else if (soError != 0)
            throw new ConnectException(
              s"connect failed: ${LibcExt.strError(soError)}"
            )
          else if ((revents & POLLOUT) == 0) {
            // Writable with no pending error means connected, even if the
            // peer has already sent data (POLLIN) or closed (POLLHUP).
            if ((revents & POLLERR) != 0)
              throw new ConnectException(
                s"connect failed, poll POLLERR: ${revents}"
              )
            // Not enough information at this point to report remote host:port.
            throw new ConnectException("Connection refused")
          }
      }
    }

    loop(if (hasTimeout) timeout else -1)
  }

  private def pendingSocketError(sockFd: FileDescriptor): CInt = {
    val soError = stackalloc[CInt]()
    val len = stackalloc[socket.socklen_t]()
    !len = sizeof[CInt].toUInt
    val res = socket.getsockopt(
      sockFd.fd,
      socket.SOL_SOCKET,
      socket.SO_ERROR,
      soError.asInstanceOf[Ptr[Byte]],
      len
    )
    if (res != 0) errno else !soError
  }

  protected def tryPollOnAccept(): Unit = {
    val nAlloc = 1.toUInt
    val pollFd: Ptr[struct_pollfd] = stackalloc[struct_pollfd](nAlloc)

    pollFd.fd = fd.fd
    pollFd.revents = 0
    pollFd.events = POLLIN.toShort

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
