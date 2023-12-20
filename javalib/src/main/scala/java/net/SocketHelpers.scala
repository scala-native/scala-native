package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._

import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.{netdb, netdbOps}, netdb._, netdbOps._
import scala.scalanative.posix.netinet.{in, inOps}, in._, inOps._
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.string.memcpy

import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows.WinSocketApiOps

object SocketHelpers {
  if (isWindows) {
    // WinSockets needs to be initialized before usage
    WinSocketApiOps.init()
  }

  // scripted-tests/run/java-net-socket.scala uses this method.
  def isReachableByEcho(ip: String, timeout: Int, port: Int): Boolean = {
    val s = new java.net.Socket()
    val isReachable =
      try {
        s.connect(new InetSocketAddress(ip, port), timeout)
        true
      } finally {
        s.close()
      }
    isReachable
  }

  private[net] def getGaiHintsAddressFamily(): Int = {
    getPreferIPv6Addresses() match {
      // let getaddrinfo() decide what is returned and its order.
      case None                  => AF_UNSPEC
      case Some(preferIPv6Addrs) => if (preferIPv6Addrs) AF_INET6 else AF_INET
    }
  }

  // True if at least one non-loopback interface has an IPv6 address.
  private def isIPv6Configured(): Boolean = {
    if (isWindows) {
      false // Support for IPv6 is neither implemented nor tested.
    } else {
      /* The lookup can not be a local address. This one of two IPv6
       * addresses for the famous, in the IPv6 world, www.kame.net
       * IPv6 dancing kame (turtle). The url from Ipv6 for fun some time
       */
      val kameIPv6Addr = c"2001:2F0:0:8800:0:0:1:1"

      val hints = stackalloc[addrinfo]() // stackalloc clears its memory
      val ret = stackalloc[Ptr[addrinfo]]()

      hints.ai_family = AF_INET6
      hints.ai_flags = AI_NUMERICHOST | AI_ADDRCONFIG | AI_PASSIVE
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = in.IPPROTO_TCP

      val gaiStatus = getaddrinfo(kameIPv6Addr, null, hints, ret)
      val result =
        if (gaiStatus != 0) {
          false
        } else {
          try {
            val ai = !ret
            if ((ai == null) || (ai.ai_addr == null)) {
              false
            } else {
              ai.ai_addr.sa_family == AF_INET6
            }
          } finally {
            freeaddrinfo(!ret)
          }
        }

      result
    }
  }

  // A Single Point of Truth to toggle IPv4/IPv6 underlying transport protocol.
  private lazy val useIPv4Stack: Boolean = {
    // Java defaults to "false"
    val systemPropertyForcesIPv4 =
      java.lang.Boolean.parseBoolean(
        System.getProperty("java.net.preferIPv4Stack", "false")
      )

    // Do the expensive test last.
    systemPropertyForcesIPv4 || !isIPv6Configured()
  }

  private[net] def getUseIPv4Stack(): Boolean = useIPv4Stack

  private lazy val preferIPv6Addresses: Option[Boolean] = {
    if (getUseIPv4Stack()) {
      Some(false)
    } else {
      val prop = System.getProperty("java.net.preferIPv6Addresses", "false")

      // Java 9 and above allow "system" or Boolean: true/false.
      if (prop.toLowerCase() == "system") None
      else Some(java.lang.Boolean.parseBoolean(prop))
    }
  }

  private[net] def getPreferIPv6Addresses(): Option[Boolean] =
    preferIPv6Addresses

  // Protocol used to set IP layer socket options must match active net stack.
  private lazy val stackIpproto: Int =
    if (getUseIPv4Stack()) in.IPPROTO_IP else in.IPPROTO_IPV6

  private[net] def getIPPROTO(): Int = stackIpproto

  private lazy val trafficClassSocketOption: Int =
    if (getUseIPv4Stack()) in.IP_TOS else ip6.IPV6_TCLASS

  private[net] def getTrafficClassSocketOption(): Int =
    trafficClassSocketOption

  // Return text translation of getaddrinfo (gai) error code.
  private[net] def getGaiErrorMessage(gaiErrorCode: CInt): String = {
    if (isWindows) {
      "getAddrInfo error code: ${gaiErrorCode}"
    } else {
      fromCString(gai_strerror(gaiErrorCode))
    }
  }

  private[net] def isIPv4MappedAddress(pb: Ptr[Byte]): Boolean = {
    val ptrInt = pb.asInstanceOf[Ptr[Int]]
    val ptrLong = pb.asInstanceOf[Ptr[Long]]
    (ptrInt(2) == 0xffff0000) && (ptrLong(0) == 0x0L)
  }

  private[net] def sockaddrToByteArray(sockAddr: Ptr[sockaddr]): Array[Byte] = {
    val af = sockAddr.sa_family.toInt
    val (src, size) = if (af == AF_INET6) {
      val v6addr = sockAddr.asInstanceOf[Ptr[in.sockaddr_in6]]
      val sin6Addr = v6addr.sin6_addr.at1.asInstanceOf[Ptr[Byte]]
      // Scala JVM down-converts even when preferIPv6Addresses is "true"
      if (isIPv4MappedAddress(sin6Addr)) {
        (sin6Addr + 12, 4)
      } else {
        (sin6Addr, 16)
      }
    } else if (af == AF_INET) {
      val v4addr = sockAddr.asInstanceOf[Ptr[in.sockaddr_in]]
      val sin4Addr = v4addr.sin_addr.at1.asInstanceOf[Ptr[Byte]]
      (sin4Addr, 4)
    } else {
      throw new SocketException(s"Unsupported address family: ${af}")
    }

    val byteArray = new Array[Byte](size)
    memcpy(byteArray.at(0), src, size.toUInt)

    byteArray
  }

  private def sockddrToPort(sockAddr: Ptr[sockaddr]): Int = {
    val af = sockAddr.sa_family.toInt
    val inPort = if (af == AF_INET6) {
      sockAddr.asInstanceOf[Ptr[in.sockaddr_in6]].sin6_port
    } else if (af == AF_INET) {
      sockAddr.asInstanceOf[Ptr[in.sockaddr_in]].sin_port
    } else {
      throw new SocketException(s"Unsupported address family: ${af}")
    }
    inet.ntohs(inPort).toInt
  }

  private[net] def sockaddrStorageToInetSocketAddress(
      sockAddr: Ptr[sockaddr]
  ): InetSocketAddress = {
    val addr = InetAddress.getByAddress(sockaddrToByteArray(sockAddr))
    val port = sockddrToPort(sockAddr)
    new InetSocketAddress(addr, port)
  }

  // Create copies of loopback & wildcard, so that originals never get changed

  // ScalaJVM shows loopbacks with null host, wildcards with numeric host.
  private[net] def loopbackIPv4(): InetAddress =
    InetAddress.getByAddress(Array[Byte](127, 0, 0, 1))

  private[net] def loopbackIPv6(): InetAddress = InetAddress.getByAddress(
    Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
  )

  private def wildcardIPv4(): InetAddress =
    InetAddress.getByAddress("0.0.0.0", Array[Byte](0, 0, 0, 0))

  private def wildcardIPv6(): InetAddress = InetAddress.getByAddress(
    "0:0:0:0:0:0:0:0",
    Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  private lazy val useLoopbackIPv6: Boolean = {
    getPreferIPv6Addresses() match {
      case Some(useIPv6) => useIPv6
      case None =>
        try {
          // "system" case relies on local nameserver having "localhost" defined.
          InetAddress.getByName("localhost").isInstanceOf[Inet6Address]
        } catch {
          /* Make a best guess. On an IPv4 system, getPreferIPv6Addresses()
           * would have been Some(false), so this is a known IPv6 system.
           * Make loopback match IPv6 implementation socket.
           * Time will tell if this heuristic works.
           */
          case e: UnknownHostException => true
        }
    }
  }

  private[net] def getLoopbackAddress(): InetAddress = {
    if (useLoopbackIPv6) loopbackIPv6()
    else loopbackIPv4()
  }

  private lazy val useWildcardIPv6: Boolean = {
    getPreferIPv6Addresses() match {
      case Some(useIPv6) => useIPv6
      // For "system" case assume wildcard & loopback both use same protocol.
      case None => useLoopbackIPv6
    }
  }

  private[net] def getWildcardAddress(): InetAddress = {
    if (useWildcardIPv6) wildcardIPv6()
    else wildcardIPv4()
  }

}

/* Normally objects 'ip' and 'ip6' would be in a separate file.
 * The way that Scala Native javalib gets built means that can not be
 * easily done here.
 */


/* As of this writing, there is no good home for this object in Scala Native.
 * Those definitions are not POSIX
 */
@extern
private[net] object ip {
  type ip_mreq = CStruct2[
    in_addr, // imr_multiaddr
    in_addr // imr_address
  ]

  // Linux only
  type ip_mreqn = CStruct3[
    in_addr, // imr_multiaddr
    in_addr, // imr_address
    CInt // imr_ifindex
  ]

  @name("scalanative_ip_multicast_ttl")
  def IP_MULTICAST_TTL: CInt = extern

  @name("scalanative_ip_add_membership")
  def IP_ADD_MEMBERSHIP: CInt = extern

  @name("scalanative_ip_drop_membership")
  def IP_DROP_MEMBERSHIP: CInt = extern
}

private[net] object ipOps {
  import ip._
  implicit class ip_mreqOps(val ptr: Ptr[ip_mreq]) extends AnyVal {
    def imr_multiaddr: in_addr = ptr._1
    def imr_address: in_addr = ptr._2
    def imr_multiaddr_=(v: in_addr): Unit = ptr._1 = v
    def imr_address_=(v: in_addr): Unit = ptr._2 = v
  }

  implicit class mip_mreqnOps(val ptr: Ptr[ip_mreqn]) extends AnyVal {
    def imr_multiaddr: in_addr = ptr._1
    def imr_address: in_addr = ptr._2
    def imr_ifindex: CInt = ptr._3
    def imr_multiaddr_=(v: in_addr): Unit = ptr._1 = v
    def imr_address_=(v: in_addr): Unit = ptr._2 = v
    def imr_ifindex_=(v: CInt): Unit = ptr._3 = v
  }
}


/* As of this writing, there is no good home for this object in Scala Native.
 * This is and its matching C code are the Scala Native rendition of
 * ip6.h described in RFC 2553 and follow-ons.
 *
 * It is IETF (Internet Engineering Task Force) and neither POSIX nor
 * ISO C. The value it describes varies by operating system. Linux, macOS,
 * and FreeBSD each us a different one. The RFC suggests that it be
 * accessed by including netinet/in.h.
 *
 * This object implements only the IPV6_TCLASS needed by java.net. The
 * full implementation is complex and does not belong in javalib.
 *
 * When creativity strikes someone and a good home is found, this code
 * can and should be moved there.
 */
@extern
private[net] object ip6 {
  @name("scalanative_ipv6_tclass")
  def IPV6_TCLASS: CInt = extern

  @name("scalanative_ipv6_multicast_hops")
  def IPV6_MULTICAST_HOPS: CInt = extern
}
