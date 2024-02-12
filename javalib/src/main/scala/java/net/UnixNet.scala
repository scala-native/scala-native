package java.net

import java.io.{FileDescriptor, IOException}
import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.sys.{socket => unixsocket}
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe.CInt

object UnixNet extends Net {
  override def socket(family: ProtocolFamily, stream: Boolean): FileDescriptor = {
    val af = family match {
      case StandardProtocolFamily.INET  => unixsocket.AF_INET
      case StandardProtocolFamily.INET6 => unixsocket.AF_INET6
      case _ =>
        throw new UnsupportedOperationException("Protocol family not supported")
    }
    val socketType = if (stream) unixsocket.SOCK_STREAM else unixsocket.SOCK_DGRAM

    val sock = unixsocket.socket(af, socketType, 0)
    if (sock < 0) {
      throw new IOException(s"Could not create socket in address family: ${family.name()}")
    }
    val fileHandle = FileDescriptor.FileHandle(sock)
    new FileDescriptor(fileHandle, readOnly = false)
  }

  override def close(fd: FileDescriptor): Unit = unistd.close(fd.fd)

  override def setSocketBlocking(fd: FileDescriptor, blocking: Boolean): Unit = {
    updateSocketFdOpts(fd.fd) { oldOpts =>
      if (blocking) oldOpts & ~O_NONBLOCK
      else oldOpts | O_NONBLOCK
    }
  }

  @inline
  private def getSocketFdOpts(fdFd: Int): CInt = {
    val opts = fcntl(fdFd, F_GETFL, 0)
    if (opts == -1) throw new ConnectException(s"connect failed, fcntl F_GETFL, errno: $errno")
    opts
  }

  @inline
  private def setSocketFdOpts(fdFd: Int, opts: Int): Unit = {
    val ret = fcntl(fdFd, F_SETFL, opts)
    if (ret == -1) throw new ConnectException(s"connect failed, fcntl F_SETFL for opts: $opts, errno: $errno")
  }

  @inline
  private def updateSocketFdOpts(fdFd: Int)(mapping: CInt => CInt): Int = {
    val oldOpts = getSocketFdOpts(fdFd)
    setSocketFdOpts(fdFd, mapping(oldOpts))
    oldOpts
  }




}
