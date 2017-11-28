package java.net

import java.io.FileDescriptor
import scala.scalanative.native._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.inOps._

// Ported from Apache Harmony

abstract class DatagramSocketImpl extends SocketOptions {
  protected[net] var fd: FileDescriptor = null
  protected[net] var localPort: Int     = -1

  protected[net] def bind(port: Int, addr: InetAddress): Unit

  protected[net] def close(): Unit

  protected[net] def create(): Unit

  protected[net] def getFileDescriptor(): FileDescriptor = fd

  private[net] def getLocalAddress(): InetAddress = {
    val len = stackalloc[socket.socklen_t]
    !len = sizeof[in.sockaddr_in6].toUInt
    val sin = stackalloc[socket.sockaddr]
    if (socket.getsockname(fd.fd, sin, len) != 0) {
      return null
    }

    if (!sin._1 == socket.AF_INET.toUShort) {
      val addr4     = sin.cast[Ptr[in.sockaddr_in]]
      val addr4in   = addr4.sin_addr.in_addr
      val addrBytes = Array.fill[Byte](4)(0)
      for (i <- 3 to 0 by -1) {
        addrBytes(i) = (addr4in >> i * 8).toByte
      }
      new Inet4Address(addrBytes)
    } else {
      val addr6     = sin.cast[Ptr[in.sockaddr_in6]]
      val addr6in   = addr6.sin6_addr
      val addrBytes = Array.fill[Byte](16)(0)
      for (i <- 0 until 16) {
        addrBytes(i) = (!((addr6in._1)._1 + i)).toByte
      }
      new Inet6Address(addrBytes)
    }
  }

  protected[net] def getLocalPort(): Int = localPort

  @deprecated
  protected[net] def getTTL(): Byte

  protected[net] def getTimeToLive(): Int

  protected[net] def join(addr: InetAddress): Unit

  protected[net] def joinGroup(addr: SocketAddress,
                               netInterface: NetworkInterface): Unit

  protected[net] def leave(addr: InetAddress): Unit

  protected[net] def leaveGroup(addr: SocketAddress,
                                netInterface: NetworkInterface): Unit

  protected[net] def peek(sender: InetAddress): Int

  protected[net] def receive(pack: DatagramPacket): Unit

  protected[net] def send(pack: DatagramPacket): Unit

  protected[net] def setTimeToLive(ttl: Int): Unit

  @deprecated
  protected[net] def setTTL(ttl: Byte): Unit

  protected[net] def connect(inetAddr: InetAddress, port: Int): Unit = {
    /* Do nothing */
  }

  protected[net] def disconnect(): Unit = { /* Do nothing */ }

  protected[net] def peekData(pack: DatagramPacket): Int
}
