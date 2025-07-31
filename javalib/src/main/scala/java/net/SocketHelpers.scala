package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._

import java.io.IOException
import java.io.FileDescriptor

import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.{netdb, netdbOps}, netdb._, netdbOps._
import scala.scalanative.posix.netinet.{in, inOps}, in._, inOps._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.string.memcpy

import scala.scalanative.meta.LinktimeInfo
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

  private[net] def prepareSockaddrIn4(
      inetAddress: InetAddress,
      port: Int,
      sa4: Ptr[in.sockaddr_in]
  ): Unit = {
    require(inetAddress.isInstanceOf[Inet4Address])

    sa4.sin_family = AF_INET.toUShort
    sa4.sin_port = inet.htons(port.toUShort)
    val src = inetAddress.getAddress()
    val from = src.asInstanceOf[scala.scalanative.runtime.Array[Byte]].at(0)
    val dst = sa4.sin_addr.at1.asInstanceOf[Ptr[Byte]]
    memcpy(dst, from, 4.toUInt)
  }

  /* Fill in the given sockaddr_in6 with the given InetAddress, either
   * Inet4Address or Inet6Address, and the given port.
   * Set the af_family for IPv6.  On return, the sockaddr_in6 should
   * be ready to use in bind() or connect().
   *
   * By contract, all the bytes in sa6 are zero coming in.
   */
  private[net] def prepareSockaddrIn6(
      inetAddress: InetAddress,
      port: Int,
      sa6: Ptr[in.sockaddr_in6]
  ): Unit = {

    /* BEWARE: This is Unix-only code.
     *   Currently (2022-08-27) execution on Windows never get here. IPv4Only
     *   is forced on.  If that ever changes, this method may need
     *   Windows code.
     *
     *   Having the complexity in one place, it should make adding
     *   Windows support easier.
     */

    sa6.sin6_family = AF_INET6.toUShort
    sa6.sin6_port = inet.htons(port.toUShort)

    val src = inetAddress.getAddress()

    if (inetAddress.isInstanceOf[Inet6Address]) {
      val from = src.asInstanceOf[scala.scalanative.runtime.Array[Byte]].at(0)
      val dst = sa6.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]
      memcpy(dst, from, 16.toUInt)

      sa6.sin6_scope_id = inetAddress
        .asInstanceOf[Inet6Address]
        .getScopeId()
        .toUShort
    } else { // Use IPv4mappedIPv6 address
      // IPv4 addresses do not have a scope_id, so leave at current value 0

      val dst = sa6.sin6_addr.toPtr.s6_addr

      // By contract, the leading bytes are already zero.
      val FF = 255.toUByte
      dst(10) = FF // set the IPv4mappedIPv6 indicator bytes
      dst(11) = FF

      // add the IPv4 trailing bytes, unrolling small loop
      dst(12) = src(0).toUByte
      dst(13) = src(1).toUByte
      dst(14) = src(2).toUByte
      dst(15) = src(3).toUByte
    }
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

  private def extractIP4Bytes(pb: Ptr[Byte]): Array[Byte] = {
    val buf = new Array[Byte](4)
    buf(0) = pb(12)
    buf(1) = pb(13)
    buf(2) = pb(14)
    buf(3) = pb(15)
    buf
  }

  /* The goal is to have a single implementation of InetAddress class &
   * subclass creation that can be used by InetAddress.scala and
   * NetworkInterface.scala, by way of sockaddrStorageToInetSocketAddress().
   *
   * One would expect such a routine to be in InetAddress.scala
   * to make the creation of Inet4Address & Inet6Address instances
   * simpler and have better performance.
   *
   * test-runtime compiles & executes across many versions using that
   * scheme.  Unfortunately, test-scripted on Scala 2.12 (and possibly
   * other versions) fails to compile the java-net-socket test.
   * Good design wrecked upon the rocks of hard reality.
   */

  private[net] def sockaddrToInetAddress(
      sin: Ptr[sockaddr],
      host: String
  ): InetAddress = {

    if (sin.sa_family == AF_INET) {
      InetAddress.getByAddress(host, SocketHelpers.sockaddrToByteArray(sin))
    } else if (sin.sa_family == AF_INET6) {
      val sin6 = sin.asInstanceOf[Ptr[sockaddr_in6]]
      val addrBytes = sin6.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]

      // Scala JVM down-converts even when preferIPv6Addresses is "true"
      /* 2024-01-21 10:16 -0500
       * Yes this is still astonishing but true. Not just a trick of
       * output formatting.
       *
       * Using scala.cli
       *
       * scala> import
       * scala> val ia1 = InetAddress.getByName("::FFFF:127.0.0.1")
       * val ia1: java.net.InetAddress = /127.0.0.1
       * scala> ia1.isInstanceOf[Inet4Address]
       * val res0: Boolean = true
       */
      if (isIPv4MappedAddress(addrBytes)) {
        InetAddress.getByAddress(host, extractIP4Bytes(addrBytes))
      } else {
        /* Yes, Java specifies Int for scope_id in a way which disallows
         * some values POSIX/IEEE/IETF allows.
         */

        val scope_id = sin6.sin6_scope_id.toInt

        /* Be aware some trickiness here.
         * Java treats a 0 scope_id (qua NetworkInterface index)
         * as having been not supplied.
         * Exactly the same 0 scope_id explicitly passed to
         * Inet6Address.getByAddress() is considered supplied and
         * displayed as such.
         */

        // Keep address bytes passed in immutable, get new Array.
        val clonedBytes = SocketHelpers.sockaddrToByteArray(sin)
        if (scope_id == 0)
          InetAddress.getByAddress(host, clonedBytes)
        else
          Inet6Address.getByAddress(
            host,
            clonedBytes,
            scope_id
          )
      }
    } else {
      throw new IOException(
        s"The requested address family is not supported: ${sin.sa_family}."
      )
    }
  }

  private[net] def sockaddrStorageToInetSocketAddress(
      sockAddr: Ptr[sockaddr]
  ): InetSocketAddress = {
    val addr = sockaddrToInetAddress(sockAddr, "")
    val port = sockddrToPort(sockAddr)
    new InetSocketAddress(addr, port)
  }

  /* InetAddress() & Inet6Address() make defensive copies of the Array[Byte].
   * As a result, these originals can never get changed.
   */

  // ScalaJVM shows loopbacks with null host, wildcards with numeric host.
  private[net] lazy val loopbackIPv4: InetAddress =
    InetAddress.getByAddress(Array[Byte](127, 0, 0, 1))

  private[net] lazy val loopbackIPv6: InetAddress = InetAddress.getByAddress(
    Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
  )

  private lazy val wildcardIPv4: InetAddress =
    InetAddress.getByAddress("0.0.0.0", Array[Byte](0, 0, 0, 0))

  private lazy val wildcardIPv6: InetAddress = InetAddress.getByAddress(
    "0:0:0:0:0:0:0:0",
    Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  private lazy val useLoopbackIPv6: Boolean = {
    getPreferIPv6Addresses() match {
      case Some(useIPv6) => useIPv6
      case None          =>
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
    if (useLoopbackIPv6) loopbackIPv6
    else loopbackIPv4
  }

  private lazy val useWildcardIPv6: Boolean = {
    getPreferIPv6Addresses() match {
      case Some(useIPv6) => useIPv6
      // For "system" case assume wildcard & loopback both use same protocol.
      case None => useLoopbackIPv6
    }
  }

  private[net] def getWildcardAddress(): InetAddress = {
    if (useWildcardIPv6) wildcardIPv6
    else wildcardIPv4
  }

  /* Return the wildcard address corresponding directly to the IP stack in use.
   * This address has not been selected by getPreferIPv6Addresses().
   *
   * This section will need to be revisited as more robust FreeBSD support
   * is added.  The assumption here is that FreeBSD always returns the
   * IPv4 wildcard. That assumption/guess needs to be verified.
   * FreeBSD & NetBSD are reported to separate IPv4 & IPv6 stacks.
   */

  private[net] def getWildcardAddressForBind(): InetAddress = {
    if (LinktimeInfo.isFreeBSD) wildcardIPv4
    else if (useIPv4Stack) wildcardIPv4
    else wildcardIPv6
  }

  private[net] def fetchFdLocalAddress(osFd: Int): InetAddress = {
    // allocate largest possible buffer, then pass generic overlay 'sin' to C.
    val storage = stackalloc[socket.sockaddr_storage]()
    val sin = storage.asInstanceOf[Ptr[socket.sockaddr]]
    val addressLen = stackalloc[socket.socklen_t]()
    !addressLen = sizeof[in.sockaddr_in6].toUInt

    if (socket.getsockname(
          osFd,
          sin,
          addressLen
        ) == -1) {
      throw new SocketException("getsockname failed")
    }

    SocketHelpers.sockaddrToInetAddress(sin, "")
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
  @define("__SCALANATIVE_JAVALIB_NETINET_IN6")
  @name("scalanative_ipv6_tclass")
  def IPV6_TCLASS: CInt = extern

  implicit class ip6Extension(self: ip6.type) {
    def IPV6_MULTICAST_HOPS: CInt = in.IPV6_MULTICAST_HOPS
  }
}
