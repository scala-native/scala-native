package java.net

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
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiOps

import scala.scalanative.posix.netinet.{in, inOps}, in._, inOps._

object SocketHelpers {
  if (isWindows) {
    // WinSockets needs to be initialized before usage
    WinSocketApiOps.init()
  }

  /*
   * The following should be long enough and constant exists on macOS.
   * https://www.gnu.org/software/libc/manual/html_node/Host-Identification.html
   * https://man7.org/linux/man-pages/man2/gethostname.2.html
   */
  val MAXHOSTNAMELEN = 256.toUInt

  private def setSocketNonBlocking(socket: CInt)(implicit z: Zone): CInt = {
    if (isWindows) {
      val mode = alloc[CInt]()
      !mode = 0
      ioctlSocket(socket.toPtr[Byte], FIONBIO, mode)
    } else {
      fcntl(socket, F_SETFL, O_NONBLOCK)
    }
  }

  def isReachableByEcho(ip: String, timeout: Int, port: Int): Boolean =
    Zone { implicit z =>
      val cIP = toCString(ip)
      val hints = stackalloc[addrinfo]()
      val ret = stackalloc[Ptr[addrinfo]]()

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
        setSocketNonBlocking(sock)
        // stackalloc is documented as returning zeroed memory
        val fdsetPtr = stackalloc[fd_set]() // No need to FD_ZERO
        FD_SET(sock, fdsetPtr)

        // calculate once and use a second time below.
        val tv_sec = timeout / 1000
        val tv_usec = (timeout % 1000) * 1000

        val time = stackalloc[timeval]()
        time.tv_sec = tv_sec
        time.tv_usec = tv_usec

        if (connect(sock, ai.ai_addr, ai.ai_addrlen) != 0) {
          return false
        }

        if (select(sock + 1, null, fdsetPtr, null, time) == 1) {
          val so_error = stackalloc[CInt]().asInstanceOf[Ptr[Byte]]
          val len = stackalloc[socklen_t]()
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
          val buf: Ptr[CChar] = stackalloc[CChar](5.toUSize)
          val recBytes = recv(sock, buf, 5.toUInt, 0)
          if (recBytes < 4) {
            return false
          }
        }
      } catch {
        case e: Throwable => e
      } finally {
        if (isWindows) closeSocket(sock.toPtr[Byte])
        else close(sock)
        freeaddrinfo(ai)
      }
      true
    }

  private def getGaiHintsAddressFamily(): Int = {
    if (preferIPv4Stack) {
      AF_INET
    } else if (preferIPv6Addresses) {
      AF_INET6
    } else {
      AF_UNSPEC // let getaddrinfo() decide what is returned and its order.
    }
  }

  def hostToIp(host: String): Option[String] =
    Zone { implicit z =>
      val ret = stackalloc[Ptr[addrinfo]]()
      val ipstr: Ptr[CChar] = stackalloc[CChar]((INET6_ADDRSTRLEN + 1).toUInt)

      val hints = stackalloc[addrinfo]()
      hints.ai_family = getGaiHintsAddressFamily()
      hints.ai_socktype = SOCK_STREAM
      hints.ai_flags = AI_ADDRCONFIG

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
      val hints = stackalloc[addrinfo]()
      val ret = stackalloc[Ptr[addrinfo]]()

      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = 0
      hints.ai_next = null

      val retArray = scala.collection.mutable.ArrayBuffer[String]()
      val status = getaddrinfo(toCString(host), null, hints, ret)
      if (status != 0)
        return scala.Array.empty[String]

      var ai = !ret
      while (ai != null) {
        val ipstr: Ptr[CChar] = stackalloc[CChar]((INET6_ADDRSTRLEN + 1).toUInt)
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

  private def tailorSockaddr(ip: String, isV6: Boolean, addr: Ptr[sockaddr])(
      implicit z: Zone
  ): Boolean = {
    // By contract the 'sockaddr' argument is cleared/all_zeros.
    val src = toCString(ip)
    val dst =
      if (isV6) {
        val v6addr = addr.asInstanceOf[Ptr[sockaddr_in6]]
        v6addr.sin6_family = AF_INET6.toUShort
        v6addr.sin6_addr.toPtr
          .asInstanceOf[Ptr[Byte]]
      } else {
        val v4addr = addr.asInstanceOf[Ptr[sockaddr_in]]
        v4addr.sin_family = AF_INET.toUShort
        v4addr.sin_addr.toPtr
          .asInstanceOf[Ptr[Byte]]
      }

    // Return true iff output argument addr is now fit for use by intended
    // sole caller, ipToHost().
    inet_pton(addr.sa_family.toInt, src, dst) == 1
  }

  def ipToHost(ip: String, isV6: Boolean): Option[String] =
    Zone { implicit z =>
      // Sole caller, Java 8 InetAddress#getHostName(),
      // does not allow/specify Exceptions, so better error reporting
      // of C function failures here and in tailorSockaddr() is not feasible.

      val host: Ptr[CChar] = stackalloc[CChar](MAXHOSTNAMELEN)
      val addr = stackalloc[sockaddr]() // will clear/zero all memory returned

      // By contract 'sockaddr' passed in is cleared/all_zeros.
      if (!tailorSockaddr(ip, isV6, addr)) {
        None
      } else {
        val status =
          getnameinfo(
            addr,
            sizeof[sockaddr].toUInt,
            host,
            MAXHOSTNAMELEN,
            null, // 'service' is not used; do not retrieve
            0.toUInt,
            0
          )

        if (status == 0) Some(fromCString(host)) else None
      }
    }

  // True if at least one non-loopback interface has an IPv6 address.
  private def isIPv6Configured(): Boolean = {
    if (isWindows) {
      false // Support for IPv6 is neither implemented nor tested.
    } else
      Zone { implicit z =>
        /* The lookup can not be a local address. This one of two IPv6
         * addresses for the famous, in the IPv6 world, www.kame.net
         * IPv6 dancing kame (turtle). The url from Ipv6 for fun some time
         */
        val kameIPv6Addr = c"2001:2F0:0:8800:0:0:1:1"

        val hints = stackalloc[addrinfo]() // stackalloc clears its memory
        val ret = stackalloc[Ptr[addrinfo]]()

        hints.ai_family = AF_INET6
        hints.ai_flags = AI_NUMERICHOST | AI_ADDRCONFIG
        hints.ai_socktype = SOCK_STREAM

        val gaiStatus = getaddrinfo(kameIPv6Addr, null, hints, ret)
        val result =
          try {
            if (gaiStatus != 0) {
              val msg = fromCString(gai_strerror(gaiStatus))
              throw new java.io.IOException(
                s"Could not determine if IPv6 is configured: ${msg}"
              )
            }

            val ai = !ret
            if ((ai == null) || (ai.ai_addr == null)) {
              false
            } else {
              ai.ai_addr.sa_family == AF_INET6.toUShort
            }
          } finally {
            freeaddrinfo(!ret)
          }

        result
      }
  }

  // A Single Point of Truth to toggle IPv4/IPv6 underlying transport protocol.
  private lazy val preferIPv4Stack: Boolean = {
    val prop = System.getProperty("java.net.preferIPv4Stack")
    // Java defaults to "false", did System properties override?
    val forceIPv4 = ((prop != null) && (prop == "true"))
    forceIPv4 || !isIPv6Configured() // Do the expensive test last.
  }

  /* Leave preferIPv4 stack at its default while this code is a
   * Work In Progress (WIP).  Toggle to "true" before code is accepted
   * as a final Pull Request (PR). This default is the opposite of the
   * long standing Java practice, but reduces the chance of hosing people.
   * If people have to opt-in, they are more likely to be prepared.
   */
//  private[net] def getPreferIPv4Stack(): Boolean = true
  private[net] def getPreferIPv4Stack(): Boolean = preferIPv4Stack

  private lazy val preferIPv6Addresses: Boolean = {
    val prop = System.getProperty("java.net.preferIPv6Addresses")
    (prop != null) && (prop == "true") // Java default of "false"
  }

  private[net] def getPreferIPv6Addresses(): Boolean = preferIPv6Addresses

}
