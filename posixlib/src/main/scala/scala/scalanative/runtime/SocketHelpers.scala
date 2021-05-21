package scala.scalanative
package runtime

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.posix.{netdb, netdbOps}, netdb._, netdbOps._
import scala.scalanative.posix.arpa.inet._
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.sys.select._
import scala.scalanative.posix.unistd.close
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.sys.time.timeval
import scala.scalanative.posix.sys.timeOps._

import scala.scalanative.posix.netinet.{in, inOps}, in._, inOps._

object SocketHelpers {
  /*
   * The following should be long enough and constant exists on macOS.
   * https://www.gnu.org/software/libc/manual/html_node/Host-Identification.html
   * https://man7.org/linux/man-pages/man2/gethostname.2.html
   */
  val MAXHOSTNAMELEN = 256.toUInt

  def isReachableByEcho(ip: String, timeout: Int, port: Int): Boolean =
    Zone { implicit z =>
      val cIP   = toCString(ip)
      val hints = stackalloc[addrinfo]
      val ret   = stackalloc[Ptr[addrinfo]]

      hints.ai_family = AF_UNSPEC
      hints.ai_protocol = 0
      hints.ai_addr = null
      hints.ai_flags = 4 // AI_NUMERICHOST
      hints.ai_socktype = SOCK_STREAM
      hints.ai_next = null

      if (getaddrinfo(cIP, toCString(port.toString), hints, ret) != 0) {
        return false
      }

      val ai = !ret

      val sock = socket(ai.ai_family, SOCK_STREAM, ai.ai_protocol)

      try {

        if (sock < 0) {
          return false
        }

        fcntl(sock, F_SETFL, O_NONBLOCK)

        // stackalloc is documented as returning zeroed memory
        val fdsetPtr = stackalloc[fd_set] //  No need to FD_ZERO
        FD_SET(sock, fdsetPtr)

        // calculate once and use a second time below.
        val tv_sec  = timeout / 1000
        val tv_usec = (timeout % 1000) * 1000

        val time = stackalloc[timeval]
        time.tv_sec = tv_sec
        time.tv_usec = tv_usec

        if (connect(sock, ai.ai_addr, ai.ai_addrlen) != 0) {
          return false
        }

        if (select(sock + 1, null, fdsetPtr, null, time) == 1) {
          val so_error = stackalloc[CInt].asInstanceOf[Ptr[Byte]]
          val len      = stackalloc[socklen_t]
          !len = sizeof[CInt].toUInt
          getsockopt(sock, SOL_SOCKET, SO_ERROR, so_error, len)
          if (!(so_error.asInstanceOf[Ptr[CInt]]) != 0) {
            return false
          }
        } else {
          return false
        }

        val sentBytes = send(sock, toCString("echo"), 4.toUInt, 0)
        if (sentBytes < 4) {
          return false
        }

        // Reset timeout before using it again.
        // Linux 'man select' recommends that the value of timeout argument
        // be considered as undefined for OS interoperability.
        time.tv_sec = tv_sec
        time.tv_usec = tv_usec

        if (select(sock + 1, fdsetPtr, null, null, time) != 1) {
          return false
        } else {
          val buf      = stackalloc[CChar](5.toUInt)
          val recBytes = recv(sock, buf, 5.toUInt, 0)
          if (recBytes < 4) {
            return false
          }
        }
      } catch {
        case e: Throwable => e
      } finally {
        close(sock)
        freeaddrinfo(ai)
      }
      true
    }

  def hostToIp(host: String): Option[String] =
    Zone { implicit z =>
      val hints = stackalloc[addrinfo]
      val ret   = stackalloc[Ptr[addrinfo]]

      val ipstr = stackalloc[CChar]((INET6_ADDRSTRLEN + 1).toUInt)
      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = 0
      hints.ai_next = null

      val status = getaddrinfo(toCString(host), null, hints, ret)
      if (status != 0)
        return None

      val ai = !ret
      val addr =
        if (ai.ai_family == AF_INET) {
          ai.ai_addr
            .asInstanceOf[Ptr[sockaddr_in]]
            .sin_addr
            .toPtr
            .asInstanceOf[Ptr[Byte]]
        } else {
          ai.ai_addr
            .asInstanceOf[Ptr[sockaddr_in6]]
            .sin6_addr
            .toPtr
            .asInstanceOf[Ptr[Byte]]
        }
      inet_ntop(ai.ai_family, addr, ipstr, INET6_ADDRSTRLEN.toUInt)
      freeaddrinfo(ai)
      Some(fromCString(ipstr))
    }

  def hostToIpArray(host: String): scala.Array[String] =
    Zone { implicit z =>
      val hints = stackalloc[addrinfo]
      val ret   = stackalloc[Ptr[addrinfo]]

      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = 0
      hints.ai_next = null

      val retArray = scala.collection.mutable.ArrayBuffer[String]()
      val status   = getaddrinfo(toCString(host), null, hints, ret)
      if (status != 0)
        return scala.Array.empty[String]

      var ai = !ret
      while (ai != null) {
        val ipstr = stackalloc[CChar]((INET6_ADDRSTRLEN + 1).toUInt)
        val addr =
          if (ai.ai_family == AF_INET) {
            ai.ai_addr
              .asInstanceOf[Ptr[sockaddr_in]]
              .sin_addr
              .toPtr
              .asInstanceOf[Ptr[Byte]]
          } else {
            ai.ai_addr
              .asInstanceOf[Ptr[sockaddr_in6]]
              .sin6_addr
              .toPtr
              .asInstanceOf[Ptr[Byte]]
          }
        inet_ntop(ai.ai_family, addr, ipstr, INET6_ADDRSTRLEN.toUInt)
        retArray += fromCString(ipstr)
        ai = ai.ai_next.asInstanceOf[Ptr[addrinfo]]
      }
      freeaddrinfo(!ret) // start from first addrinfo
      retArray.toArray
    }

  def ipToHost(ip: String, isV6: Boolean): Option[String] =
    Zone { implicit z =>
      val host    = stackalloc[CChar](MAXHOSTNAMELEN)
      val service = stackalloc[CChar](20.toUInt)
      val status =
        if (isV6) {
          val addr6 = stackalloc[sockaddr_in6]
          addr6.sin6_family = AF_INET6.toUShort
          inet_pton(AF_INET6,
                    toCString(ip),
                    addr6.sin6_addr.toPtr.asInstanceOf[Ptr[Byte]])
          getnameinfo(addr6.asInstanceOf[Ptr[sockaddr]],
                      sizeof[sockaddr_in6].toUInt,
                      host,
                      MAXHOSTNAMELEN,
                      service,
                      20.toUInt,
                      0)
        } else {
          val addr4 = stackalloc[sockaddr_in]
          addr4.sin_family = AF_INET.toUShort
          inet_pton(AF_INET,
                    toCString(ip),
                    addr4.sin_addr.toPtr.asInstanceOf[Ptr[Byte]])
          getnameinfo(addr4.asInstanceOf[Ptr[sockaddr]],
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
