package java.net

import scala.scalanative.native._
import scala.scalanative.posix.{netdb, netdbOps}, netdb._, netdbOps._
import scala.scalanative.posix.arpa.inet._
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.sys.select._
import scala.scalanative.posix.unistd.close
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.sys.selectOps._
import scala.scalanative.posix.netinet.{in, inOps}, in._, inOps._

private[net] object SocketHelpers {

  def isReachableByEcho(ip: String, timeout: Int, port: Int): Boolean = {
    Zone { implicit z =>
      val cIP   = toCString(ip)
      var hints = stackalloc[addrinfo]
      var ret   = stackalloc[Ptr[addrinfo]]

      string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
      hints.ai_family = AF_UNSPEC
      hints.ai_protocol = 0
      hints.ai_addr = null
      hints.ai_flags = 4 // AI_NUMERICHOST
      hints.ai_socktype = SOCK_STREAM
      hints.ai_next = null

      if (getaddrinfo(cIP, toCString(port.toString), hints, ret) != 0) {
        return false
      }

      val sock = socket((!ret).ai_family, SOCK_STREAM, (!ret).ai_protocol)
      try {
        if (sock < 0) {
          return false
        }
        fcntl(sock, F_SETFL, O_NONBLOCK)

        val fdset = stackalloc[fd_set]
        !fdset._1 = stackalloc[CLongInt](FD_SETSIZE / sizeof[CLongInt])
        FD_ZERO(fdset)
        FD_SET(sock, fdset)

        val time = stackalloc[timeval]
        time.tv_sec = timeout / 1000
        time.tv_usec = (timeout % 1000) * 1000

        connect(sock, (!ret).ai_addr, (!ret).ai_addrlen)

        if (select(sock + 1, null, fdset, null, time) == 1) {
          val so_error = stackalloc[CInt].cast[Ptr[Byte]]
          val len      = stackalloc[socklen_t]
          !len = sizeof[CInt].toUInt
          getsockopt(sock, SOL_SOCKET, SO_ERROR, so_error, len)
          if (!(so_error.cast[Ptr[CInt]]) != 0) {
            return false
          }
        }

        val sentBytes = send(sock, toCString("echo"), 4, 0)
        if (sentBytes < 4) {
          return false
        }

        if (select(sock + 1, fdset, null, null, time) != 1) {
          return false
        } else {
          val buf      = stackalloc[CChar](5)
          val recBytes = recv(sock, buf, 5, 0)
          if (recBytes < 4) {
            return false
          }
        }
      } catch {
        case e: Throwable => e
      } finally {
        close(sock)
        freeaddrinfo(!ret)
      }
    }
    true
  }

  def hostToIp(host: String): Option[String] = {
    Zone { implicit z =>
      var hints = stackalloc[addrinfo]
      var ret   = stackalloc[Ptr[addrinfo]]

      var ipstr = stackalloc[CChar](INET6_ADDRSTRLEN + 1)
      string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = 0
      hints.ai_next = null

      val status = getaddrinfo(toCString(host), null, hints, ret)
      if (status != 0)
        return None

      var addr = stackalloc[Byte]
      if ((!ret).ai_family == AF_INET) {
        addr = (!ret).ai_addr.cast[Ptr[sockaddr_in]].sin_addr.cast[Ptr[Byte]]
      } else {
        addr = (!ret).ai_addr.cast[Ptr[sockaddr_in6]].sin6_addr.cast[Ptr[Byte]]
      }
      inet_ntop((!ret).ai_family, addr, ipstr, INET6_ADDRSTRLEN.toUInt)
      freeaddrinfo(!ret)
      Some(fromCString(ipstr))
    }
  }

  def hostToIpArray(host: String): scala.Array[String] = {
    Zone { implicit z =>
      var hints = stackalloc[addrinfo]
      var ret   = stackalloc[Ptr[addrinfo]]

      string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = 0
      hints.ai_canonname = null
      hints.ai_addr = null
      hints.ai_next = null

      val retArray = scala.collection.mutable.ArrayBuffer[String]()
      val status   = getaddrinfo(toCString(host), null, hints, ret)
      if (status != 0)
        return scala.Array.empty[String]

      var p = !ret
      while (p != null) {
        var ipstr = stackalloc[CChar](INET6_ADDRSTRLEN + 1)
        var addr  = stackalloc[Byte]
        if (p.ai_family == AF_INET) {
          addr = p.ai_addr.cast[Ptr[sockaddr_in]].sin_addr.cast[Ptr[Byte]]
        } else {
          addr = p.ai_addr.cast[Ptr[sockaddr_in6]].sin6_addr.cast[Ptr[Byte]]
        }
        inet_ntop(p.ai_family, addr, ipstr, INET6_ADDRSTRLEN.toUInt)
        retArray += fromCString(ipstr)
        p = p.ai_next.cast[Ptr[addrinfo]]
      }
      freeaddrinfo(!ret)
      retArray.toArray
    }
  }

  def ipToHost(ip: String, isV6: Boolean): Option[String] = {
    Zone { implicit z =>
      var status  = 0
      val host    = stackalloc[CChar](1024)
      val service = stackalloc[CChar](20)
      if (isV6) {
        val addr6 = stackalloc[sockaddr_in6]
        addr6.sin6_family = AF_INET6.toUShort
        inet_pton(AF_INET6, toCString(ip), addr6.sin6_addr.cast[Ptr[Byte]])
        status = getnameinfo(addr6.cast[Ptr[sockaddr]],
                             sizeof[sockaddr_in6].toUInt,
                             host,
                             1024.toUInt,
                             service,
                             20.toUInt,
                             0)
      } else {
        val addr4 = stackalloc[sockaddr_in]
        addr4.sin_family = AF_INET.toUShort
        inet_pton(AF_INET, toCString(ip), addr4.sin_addr.cast[Ptr[Byte]])
        status = getnameinfo(addr4.cast[Ptr[sockaddr]],
                             sizeof[sockaddr_in].toUInt,
                             host,
                             1024.toUInt,
                             service,
                             20.toUInt,
                             0)
      }

      if (status == 0) Some(fromCString(host)) else None
    }
  }

}
