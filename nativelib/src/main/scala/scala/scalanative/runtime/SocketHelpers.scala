package scala.scalanative.runtime

import scalanative.native._
import scalanative.posix.{netdb, netdbOps}, netdb._, netdbOps._
import scalanative.posix.arpa.inet._
import scalanative.posix.sys.socketOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.select._
import scalanative.posix.sys.selectOps._
import scalanative.posix.netinet.{in, inOps}, in._, inOps._

object SocketHelpers {

  def isReachableByICMP(ip: String, time: Int, v6: Boolean): Boolean = {
    // TODO

    /*Zone { implicit z =>
      val cIP = toCString(ip)
      var addr = stackalloc[sockaddr]
      var family = AF_INET
      if(v6) {
        val addr6 = stackalloc[sockaddr_in6]
        inet_pton(AF_INET6, cIP, addr6.sin6_addr.cast[Ptr[Byte]])
        addr6.sin6_family = AF_INET6.toUShort
        family = AF_INET6
        addr = addr6.cast[Ptr[sockaddr]]
      }
      else {
        val addr4 = stackalloc[sockaddr_in]
        inet_pton(AF_INET, cIP, addr4.sin_addr.cast[Ptr[Byte]])
        addr4.sin_family = AF_INET.toUShort
        family = AF_INET
        addr = addr4.cast[Ptr[sockaddr]]
      }

      var icmpHdr = stackalloc[icmphdr]
      var sequence = 0
      val sock = socket(family, SOCK_DGRAM, IPPROTO_ICMP)
      if(sock < 0) {
        return false
      }
      string.memset(icmpHdr, 0, sizeof[icmphdr])
      icmp_hdr.`type` = ICMP_ECHO
      icmp_hdr.un.echo.id = 1337 // arbitrary id
    }*/
    false
  }

  def isReachableByEcho(ip: String, timeout: Int, isV6: Boolean): Boolean = {
    Zone { implicit z =>
      val cIP         = toCString(ip)
      var addr        = stackalloc[sockaddr]
      var family: Int = 0
      if (isV6) {
        val addr6 = stackalloc[sockaddr_in6]
        inet_pton(AF_INET6, cIP, addr6.sin6_addr.cast[Ptr[Byte]])
        addr6.sin6_family = AF_INET6.toUShort
        addr6.sin6_port = htons(7.toUShort)
        family = AF_INET6
        addr = addr6.cast[Ptr[sockaddr]]
      } else {
        val addr4 = stackalloc[sockaddr_in]
        inet_pton(AF_INET, cIP, addr4.sin_addr.cast[Ptr[Byte]])
        addr4.sin_family = AF_INET.toUShort
        addr4.sin_port = htons(7.toUShort)
        family = AF_INET
        addr = addr4.cast[Ptr[sockaddr]]
      }
      val sock = socket(family, SOCK_STREAM, 0);
      if (sock < 0) {
        return false
      }
      if (connect(sock, addr, sizeof[sockaddr].toUInt) < 0) {
        return false
      }

      val sentBytes = send(sock, toCString("echo"), 4, 0)
      if (sentBytes < 4) {
        return false
      }

      val rfds = stackalloc[fd_set](FD_SETSIZE / sizeof[CInt])
      FD_ZERO(rfds)
      FD_SET(sock, rfds)

      val time = stackalloc[timeval]
      time.tv_sec = timeout / 1000
      time.tv_usec = (timeout % 1000) * 1000

      // name conflict
      var res =
        scalanative.posix.sys.select.select(sock + 1, rfds, null, null, time)
      if (res <= 0) {
        return false
      }

      val buf      = stackalloc[CChar](5)
      val recBytes = recv(sock, buf, 5, 0)
      if (recBytes < 4) {
        return false
      }
    }
    true
  }

  def hostToIp(host: String): Option[String] = {
    Zone { implicit z =>
      var status = 0
      var hints  = stackalloc[addrinfo]
      var ret    = stackalloc[addrinfo]

      var ipstr = stackalloc[CString]
      string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = 0
      hints.ai_next = null

      status = getaddrinfo(toCString(host), null, hints, ret)
      if (status != 0)
        return None

      var addr = stackalloc[Byte]
      if (ret.ai_family == AF_INET) {
        addr = ret.ai_addr.cast[Ptr[sockaddr_in]].sin_addr.cast[Ptr[Byte]]
      } else {
        addr = ret.ai_addr.cast[Ptr[sockaddr_in6]].sin6_addr.cast[Ptr[Byte]]
      }
      inet_ntop(ret.ai_family, addr, !ipstr, INET6_ADDRSTRLEN.toUInt)
      return Some(fromCString(!ipstr))
    }
  }

  def hostToIpArray(host: String): scala.Array[String] = {
    Zone { implicit z =>
      var status = 0
      var hints  = stackalloc[addrinfo]
      var ret    = stackalloc[addrinfo]

      string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = 0
      hints.ai_canonname = null
      hints.ai_addr = null
      hints.ai_next = null

      val retArray = scala.collection.mutable.ArrayBuffer[String]()
      status = getaddrinfo(toCString(host), null, hints, ret)

      if (status != 0)
        return scala.Array.empty[String]

      while (ret != null) {
        var ipstr = stackalloc[CChar](INET6_ADDRSTRLEN + 1)
        var addr  = stackalloc[Byte]
        if (ret.ai_family == AF_INET) {
          addr = ret.ai_addr.cast[Ptr[sockaddr_in]].sin_addr.cast[Ptr[Byte]]
        } else {
          addr = ret.ai_addr.cast[Ptr[sockaddr_in6]].sin6_addr.cast[Ptr[Byte]]
        }
        inet_ntop(ret.ai_family, addr, ipstr, INET6_ADDRSTRLEN.toUInt)
        retArray += fromCString(ipstr)
        ret = ret.ai_next.cast[Ptr[addrinfo]]
      }
      return retArray.toArray
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
      if (status == 0) {
        return Some(fromCString(host))
      }
      return None
    }
  }

}
