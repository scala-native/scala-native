package java.net

/* Originally ported from Apache Harmony.
 * Extensively re-written for Scala Native.
 * Some code ported under license from or influenced by Arman Bilge. See:
 *   https://github.com/armanbilge/epollcat (and other repositories).
 */

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.annotation.tailrec

import java.io.IOException
import java.net.SocketHelpers.sockaddrToByteArray
import java.{util => ju}

import scala.scalanative.posix.arpa.inet._
import scala.scalanative.posix.errno.errno
import scala.scalanative.posix.netinet.in._
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.netdb._
import scala.scalanative.posix.netdbOps._
import scala.scalanative.posix.string.{memcpy, strerror}
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.time.{time_t, time, difftime}
import scala.scalanative.posix.unistd

import scala.scalanative.meta.LinktimeInfo.{isLinux, isMac}

/* Design note:
 *    Much of java.net, both in JVM and Scala Native defines or assumes
 *    the ipAddress field to have either 4 or 16 bytes.
 *
 *    One might guess from the output of 'toString() that the
 *    the IPv6 scope_id/zone_id/interface_id (e.g. "%en0") is handled
 *    by extending this ipAddress field beyond 16. That is not the case.
 *    That information is handled separately.
 */

class InetAddress protected (ipAddress: Array[Byte], originalHost: String)
    extends Serializable {
  import InetAddress._

  private def this(ipAddress: Array[Byte]) = this(ipAddress, null)

  private var hostLastUpdated: time_t = 0
  private var cachedHost: String = null
  private var lastLookupFailed = true

  override def equals(obj: Any): Boolean = {
    if (obj == null || obj.getClass != this.getClass) {
      false
    } else {
      val objIPAddress = obj.asInstanceOf[InetAddress].getAddress()
      objIPAddress.indices.forall(i => objIPAddress(i) == ipAddress(i))
    }
  }

  def getAddress() = ipAddress.clone

  def getCanonicalHostName(): String = {
    // reverse name lookup with cache

    def hostTimeoutExpired(timeNow: time_t): Boolean = {
      val timeout = if (lastLookupFailed) NegativeHostTimeout else HostTimeout
      difftime(timeNow, hostLastUpdated) > timeout
    }

    val timeNow = time(null)
    if (cachedHost == null || hostTimeoutExpired(timeNow)) {
      hostLastUpdated = timeNow

      getFullyQualifiedDomainName(ipAddress) match {
        case None =>
          lastLookupFailed = true
          cachedHost = getHostAddress()
        case Some(hostName) =>
          lastLookupFailed = false
          cachedHost = hostName
      }
    }
    cachedHost
  }

  def getHostAddress(): String = {
    val bytes = ipAddress.at(0)
    if (ipAddress.length == 4) {
      formatIn4Addr(bytes)
    } else if (ipAddress.length == 16) {
      if (isIPv4MappedAddress(bytes)) {
        formatIn4Addr(extractIP4Bytes(bytes).at(0))
      } else {
        Inet6Address.formatInet6Address(this.asInstanceOf[Inet6Address])
      }
    } else {
      "<unknown>"
    }
  }

  def getHostName(): String = {
    if (originalHost != null) {
      // remember the host given to the constructor
      originalHost
    } else {
      getCanonicalHostName()
    }
  }

  // Method used historically by Scala Native for IPv4 addresses.
  protected def bytesToInt(bytes: Array[Byte], start: Int): Int = {
    // First mask the byte with 255, as when a negative
    // signed byte converts to an integer, it has bits
    // on in the first 3 bytes, we are only concerned
    // about the right-most 8 bits.
    // Then shift the rightmost byte to align with its
    // position in the integer.
    return (((bytes(start + 3) & 255)) | ((bytes(start + 2) & 255) << 8)
      | ((bytes(start + 1) & 255) << 16)
      | ((bytes(start) & 255) << 24))
  }

  protected def getZoneIdent(): String = "" // Ease Inet6Address declaration

  override def hashCode(): Int =
    if (ipAddress.length == 4) bytesToInt(ipAddress, 0) // too scared to change
    else ju.Arrays.hashCode(ipAddress)

  def isLinkLocalAddress(): Boolean = false

  def isAnyLocalAddress(): Boolean = false

  def isLoopbackAddress(): Boolean = false

  def isMCGlobal(): Boolean = false

  def isMCLinkLocal(): Boolean = false

  def isMCNodeLocal(): Boolean = false

  def isMCOrgLocal(): Boolean = false

  def isMCSiteLocal(): Boolean = false

  def isMulticastAddress(): Boolean = false

  /* Editorial Comment: isReachable() is in the Java 8 specification and
   * must be implemented for completeness. It has severely limited utility
   * in the 21st century.  Many, if not most, systems now block the
   * echo port (7). ICMP is not used here because it requires elevated
   * privileges and is also often blocked.
   */

  def isReachable(timeout: Int): Boolean = {
    if (timeout < 0) {
      throw new IllegalArgumentException(
        "Argument 'timeout' in method 'isReachable' is negative"
      )
    } else {
      val s = new Socket()
      val echoPort = 7 // Port from Java spec, almost _always_ disbled.
      val isReachable =
        try {
          s.connect(new InetSocketAddress(this, echoPort), timeout)
          /* Most likely outcome: java.net.ConnectException: Connection refused
           * Could also be a TimeoutException. Let them bubble up.
           */
          true
        } finally {
          s.close()
        }
      isReachable
    }
  }

  // Not implemented: isReachable(NetworkInterface netif, int ttl, int timeout)

  def isSiteLocalAddress(): Boolean = false

  override def toString(): String = {
    val hostName =
      if (originalHost != null) originalHost
      else if (!lastLookupFailed) cachedHost
      else ""

    hostName + "/" + getHostAddress()
  }

}

object InetAddress {

  // cached host values are discarded after this amount of time (seconds)
  private val HostTimeout: Int =
    sys.props
      .get("networkaddress.cache.ttl")
      .map(_.toInt)
      .getOrElse(30)

  // failed lookups are retried after this amount of time (seconds)
  private val NegativeHostTimeout: Int =
    sys.props
      .get("networkaddress.cache.negative.ttl")
      .map(_.toInt)
      .getOrElse(10)

  private def apply(
      addrinfoP: Ptr[addrinfo],
      host: String,
      isNumeric: Boolean
  ): InetAddress = {
    /* if an address parses as numeric, some JVM implementations are said
     * to fill the host field in the resultant InetAddress with the
     * numeric representation.
     * The Scastie JVM and those used for Linux/macOS manual testing seem
     * to leave the host field blank/empty.
     */
    val effectiveHost = if (isNumeric) null else host

    if (addrinfoP.ai_family == AF_INET) {
      new Inet4Address(addrinfoToByteArray(addrinfoP), effectiveHost)
    } else if (addrinfoP.ai_family == AF_INET6) {
      val addr = addrinfoP.ai_addr.asInstanceOf[Ptr[sockaddr_in6]]
      val addrBytes = addr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]

      // Scala JVM down-converts even when preferIPv6Addresses is "true"
      if (isIPv4MappedAddress(addrBytes)) {
        new Inet4Address(extractIP4Bytes(addrBytes), effectiveHost)
      } else {
        /* Yes, Java specifies Int for scope_id in a way which disallows
         * some values POSIX/IEEE/IETF allows.
         */

        val scope_id = addr.sin6_scope_id.toInt

        val zoneIdent = {
          val ifIndex = host.indexOf('%')
          val ifNameStart = ifIndex + 1
          if ((ifIndex < 0) || (ifNameStart >= host.length)) ""
          else host.substring(ifNameStart)
        }

        Inet6Address(
          addrinfoToByteArray(addrinfoP),
          effectiveHost,
          scope_id,
          zoneIdent
        )
      }
    } else {
      val af = addrinfoP.ai_family
      throw new IOException(
        s"The requested address family is not supported: ${af}."
      )
    }
  }

  private def addrinfoToByteArray(
      addrinfoP: Ptr[addrinfo]
  ): Array[Byte] = {
    sockaddrToByteArray(addrinfoP.ai_addr)
  }

  private def extractIP4Bytes(pb: Ptr[Byte]): Array[Byte] = {
    val buf = new Array[Byte](4)
    buf(0) = pb(12)
    buf(1) = pb(13)
    buf(2) = pb(14)
    buf(3) = pb(15)
    buf
  }

  private def formatIn4Addr(pb: Ptr[Byte]): String = {
    // By contract, pb isInstanceOf[Ptr[in_addr]]
    val dstSize = INET_ADDRSTRLEN
    val dst = stackalloc[Byte](dstSize)

    val result = inet_ntop(AF_INET, pb, dst, dstSize.toUInt)

    if (result == null)
      throw new IOException(
        s"inet_ntop IPv4 failed,${fromCString(strerror(errno))}"
      )

    fromCString(dst)
  }

  private def getByNumericName(host: String): Option[InetAddress] = Zone {
    implicit z =>
      val hints = stackalloc[addrinfo]() // stackalloc clears its memory
      val addrinfo = stackalloc[Ptr[addrinfo]]()

      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = IPPROTO_TCP
      hints.ai_flags = AI_NUMERICHOST

      val gaiStatus = getaddrinfo(toCString(host), null, hints, addrinfo)

      if (gaiStatus != 0) {
        val mappedStatus = mapGaiStatus(gaiStatus)
        if (mappedStatus == EAI_NONAME) {
          val ifIndex = host.indexOf('%')
          val hasInterface = (ifIndex >= 0)
          if (!hasInterface) {
            None
          } else {
            /* If execution gets here, we know that we are dealing with one
             * of a large number of corner cases where interface/scope
             * id suppplied us not valid for host supplied.
             * ScalaJVM reports some cases early, such as an unknown
             * non-numeric interface name, and some later, probably at the
             * point of use, such as an invalid numeric interface id.
             *
             * It is simply not economic to try to match the timing and
             * mesage of all those cases. They all boil down to the
             * interface being invalid.
             */
            throw new UnknownHostException(
              s"something rotten with host and/or interface: '${host}'"
            )
          }
        } else {
          val gaiMsg = SocketHelpers.getGaiErrorMessage(mappedStatus)
          throw new UnknownHostException(host + ": " + gaiMsg)
        }
      } else
        try {
          // should never happen, but check anyways
          java.util.Objects.requireNonNull(!addrinfo)

          /* At this point, there is at least one addrinfo. Use the first
           * one unconditionally because here is a vanishingly small chance
           * it will have an af_family other than AF_INET or AF_INET6. Other
           * protocols should caused getaddrinfo() to return EAI_NONAME.
           *
           * InetAddress() will catch the case of an af_family which is
           * neither IPv4 nor IPv6.
           */

          Some(InetAddress(!addrinfo, host, isNumeric = true))
        } finally {
          freeaddrinfo(!addrinfo)
        }
  }

  private def getByNonNumericName(host: String): InetAddress = Zone {
    implicit z =>
      /* To prevent circular dependencies, javalib is not supposed to use
       * the quite powerful Scala Collections library.
       *
       * Use tail recursion to avoid an even nastier while loop. Let
       * the Scala compiler do the work.
       */

      @tailrec
      def findPreferrredAddrinfo(
          preference: Option[Boolean],
          ai: Ptr[addrinfo]
      ): Option[Ptr[addrinfo]] = {

        if (ai == null) {
          None
        } else {
          val result =
            if (ai.ai_family == AF_INET) {
              if ((preference == None) || (preference.get == false)) {
                Some(ai)
              } else {
                None
              }
            } else if (ai.ai_family == AF_INET6) {
              if ((preference == None) || (preference.get == true)) {
                Some(ai)
              } else {
                None
              }
            } else { // skip AF_UNSPEC & other unknown families
              None
            }

          if (result != None) {
            result
          } else {
            val aiNext = ai.ai_next.asInstanceOf[Ptr[addrinfo]]
            findPreferrredAddrinfo(preference, aiNext)
          }
        }
      }

      val hints = stackalloc[addrinfo]() // stackalloc clears its memory
      val addrinfo = stackalloc[Ptr[addrinfo]]()

      hints.ai_family = SocketHelpers.getGaiHintsAddressFamily()
      hints.ai_socktype = SOCK_STREAM
      hints.ai_protocol = IPPROTO_TCP
      if (hints.ai_family == AF_INET6) {
        hints.ai_flags |= (AI_V4MAPPED | AI_ADDRCONFIG)
      }

      val gaiStatus = getaddrinfo(toCString(host), null, hints, addrinfo)

      if (gaiStatus != 0) {
        val gaiMsg = SocketHelpers.getGaiErrorMessage(gaiStatus)
        throw new UnknownHostException(host + ": " + gaiMsg)
      } else
        try {
          val preferIPv6 = SocketHelpers.getPreferIPv6Addresses()
          findPreferrredAddrinfo(preferIPv6, !addrinfo) match {
            case None =>
              throw new UnknownHostException(s"${host}: Name does not resolve")
            case Some(ai) => InetAddress(ai, host, isNumeric = false)
          }
        } finally {
          freeaddrinfo(!addrinfo)
        }
  }

  /* Fully Qualified Domain Name which may or may not be the same as the
   * canonical name.
   */
  private def getFullyQualifiedDomainName(
      ipByteArray: Array[Byte]
  ): Option[String] = {
    /* MAXDNAME is the largest size of a Fully Qualified Domain Name.
     * It is defined in:
     *   https://github.com/openbsd/src/blob/master/include/arpa/nameser.h
     *
     * That URL says: "Define constants based on rfc883".
     * These are direct name (bind) server definitions.
     *
     * This is larger than the length of individual segments because there
     * can be multiple segments of 256. Two56.Two56.Two56.com
     *
     * On many BSD derived systems, this value is defined as (non-POSIX)
     * NI_MAXHOST.
     *   https://man7.org/linux/man-pages/man3/getnameinfo.3.html
     *
     * RFC 2181, section "Name syntax" states:
     *   The length of any one label is limited to between 1 and 63 octets.
     *   A full domain name is limited to 255 octets (including the
     *   separators).
     *
     * A CString needs one more space for its terminal NUL.
     *
     * Use the larger MAXDNAME here, the extra space is not _all_ that
     * expensive, and it is not used for long.
     */

    val MAXDNAME = 1025.toUInt /* maximum presentation domain name */

    def tailorSockaddr(ipBA: Array[Byte], addr: Ptr[sockaddr]): Unit = {
      val from = ipBA.at(0)

      // By contract the 'sockaddr' argument passed in is cleared/all_zeros.
      if (ipBA.length == 16) {
        val v6addr = addr.asInstanceOf[Ptr[sockaddr_in6]]
        v6addr.sin6_family = AF_INET6.toUShort
        // because the FQDN scope is Global, no need to set sin6_scope_id
        val dst = v6addr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]
        memcpy(dst, from, 16.toUInt)
      } else if (ipBA.length == 4) {
        val v4addr = addr.asInstanceOf[Ptr[sockaddr_in]]
        v4addr.sin_family = AF_INET.toUShort
        v4addr.sin_addr = !(from.asInstanceOf[Ptr[in_addr]]) // Structure copy
      } else {
        throw new IOException(s"Invalid ipAddress length: ${ipBA.length}")
      }
    }

    def ipToHost(ipBA: Array[Byte]): Option[String] =
      Zone { implicit z =>
        // Reserve extra space for NUL terminator.
        val hostSize = MAXDNAME + 1.toUInt
        val host: Ptr[CChar] = alloc[CChar](hostSize)
        // will clear/zero all memory
        val addr = stackalloc[sockaddr_in6]().asInstanceOf[Ptr[sockaddr]]

        // By contract 'sockaddr' passed into tailor method is all zeros.
        tailorSockaddr(ipBA, addr)
        val status =
          getnameinfo(
            addr,
            if (ipBA.length == 16) sizeof[sockaddr_in6].toUInt
            else sizeof[sockaddr_in].toUInt,
            host,
            hostSize,
            null, // 'service' is not used; do not retrieve
            0.toUInt,
            0
          )

        if (status != 0) None
        else Some(fromCString(host))
      }

    ipToHost(ipByteArray)
  }

  private def hostToInetAddressArray(host: String): Array[InetAddress] =
    Zone { implicit z =>
      /* The JVM implementations in both the manual testing &
       * Continuous Integration environments have the "feature" of
       * not filling in the host field of an InetAddress if the name
       * is strictly numeric.
       *
       * See the getByName() method and those it calls for a discussion
       * about difficulties determining if a given string is a numeric
       * hostname or not.
       *
       * The "double getadderfo" here is unfortunate (expensive) but
       * handles corner cases. Room for improvement here.
       *
       * Host name should already be in name server cache, since the
       * caller of this code just looked it up and found it.
       */

      lazy val hostIsNumeric: Boolean = {
        val leadingCh = Character.toUpperCase(host(0))

        val lookupRequired =
          Character.isDigit(leadingCh) || "ABCDEF".contains(leadingCh)

        if (!lookupRequired) {
          false
        } else if (host.contains(":")) {
          true
        } else {
          InetAddress.getByNumericName(host).isDefined
        }
      }

      @tailrec
      def addAddresses(
          addIPv4: Boolean,
          addIPv6: Boolean,
          ai: Ptr[addrinfo],
          host: String,
          iaBuf: scala.collection.mutable.ArrayBuffer[InetAddress]
      ): Unit = {
        if (ai != null) {
          if ((ai.ai_family == AF_INET) && addIPv4) {
            iaBuf += InetAddress(ai, host, hostIsNumeric)
          } else if ((ai.ai_family == AF_INET6) && addIPv6) {
            iaBuf += InetAddress(ai, host, hostIsNumeric)
          }
          // else skip AF_UNSPEC & other unknown families

          val aiNext = ai.ai_next.asInstanceOf[Ptr[addrinfo]]
          addAddresses(addIPv4, addIPv6, aiNext, host, iaBuf)
        }
      }

      def fillAddressBuffer(
          preference: Option[Boolean],
          ai: Ptr[addrinfo],
          host: String,
          iaBuf: scala.collection.mutable.ArrayBuffer[InetAddress]
      ): Unit = {

        preference match {
          case None =>
            addAddresses(addIPv4 = true, addIPv6 = true, ai, host, iaBuf)

          case Some(preferIPv6) if (preferIPv6) => // AddIPv6 first, then IPv4
            addAddresses(addIPv4 = false, addIPv6 = true, ai, host, iaBuf)
            addAddresses(addIPv4 = true, addIPv6 = false, ai, host, iaBuf)

          case Some(_) => // AddIPv4 first, then IPv6
            addAddresses(addIPv4 = true, addIPv6 = false, ai, host, iaBuf)
            addAddresses(addIPv4 = false, addIPv6 = true, ai, host, iaBuf)
        }
      } // def fillAddressBuffer

      val retArray = scala.collection.mutable.ArrayBuffer[InetAddress]()

      val hints = stackalloc[addrinfo]()
      val ret = stackalloc[Ptr[addrinfo]]()

      hints.ai_family = AF_UNSPEC
      hints.ai_socktype = SOCK_STREAM // ignore SOCK_DGRAM only
      hints.ai_protocol = IPPROTO_TCP

      val gaiStatus = getaddrinfo(toCString(host), null, hints, ret)

      if (gaiStatus != 0) {
        val mappedStatus = mapGaiStatus(gaiStatus)
        if (mappedStatus != EAI_NONAME) {
          val gaiMsg = SocketHelpers.getGaiErrorMessage(mappedStatus)
          throw new UnknownHostException(host + ": " + gaiMsg)
        }
      } else
        try {
          val preferIPv6 = SocketHelpers.getPreferIPv6Addresses()
          fillAddressBuffer(preferIPv6, !ret, host, retArray)
        } finally {
          freeaddrinfo(!ret)
        }

      retArray.toArray
    }

  private def isIPv4MappedAddress(pb: Ptr[Byte]): Boolean = {
    val ptrInt = pb.asInstanceOf[Ptr[Int]]
    val ptrLong = pb.asInstanceOf[Ptr[Long]]
    (ptrInt(2) == 0xffff0000) && (ptrLong(0) == 0x0L)
  }

  private def mapGaiStatus(gaiStatus: Int): Int = {
    /* This is where some arcane Operating System specific behavior
     * comes to puddle and pool. This method is not for small children
     * or maintainers with good taste & practice.
     *
     * EAI_NODATA was removed from RFC3493 "Basic Socket Interface Extensions
     * for IPv6" in February 2003. EAI_NONAME was introduced and is the
     * contemporary idiom. Although it is remove (i.e. well past deprecated),
     * EAI_NODATA can be returned by Linux & macOS in some poorly defined
     * circumstances.
     *
     * The magic integer values for Linux & macOS are hardcoded
     * because they are extremely unlikely to change after all this time.
     *
     * For consistency of the reported message, map EAI_NODATA to EAI_NONAME.
     * Both will return "UnknownHostException".
     */

    // EAI_NODATA was removed from FreeBSD a decade or more ago.
    val EAI_NODATA =
      if (isLinux) -5
      else if (isMac) 7
      else Integer.MAX_VALUE // placeholder, will never match

    if (gaiStatus == EAI_NONAME) gaiStatus
    else if (gaiStatus == EAI_NODATA) EAI_NONAME
    else gaiStatus
  }

  def getAllByName(host: String): Array[InetAddress] = {
    if ((host == null) || (host.length == 0)) {
      /* The obvious recursive call to getAllByName("localhost") does not
       * work here.
       *
       * ScalaJVM, on both Linux & macOS, returns a 1 element array
       * with the host field filled in. The InetAddress type and address
       * field are controlled by the System property
       * "java.net.preferIPv6Addresses"
       */

      val lbBytes = SocketHelpers.getLoopbackAddress().getAddress()

      // use a subclass so that isLoopback method is effective & truthful.
      val ia = if (lbBytes.length == 4) {
        new Inet4Address(lbBytes, "localhost")
      } else {
        new Inet6Address(lbBytes, "localhost")
      }
      Array[InetAddress](ia)
    } else {
      val ips = InetAddress.hostToInetAddressArray(host)
      if (ips.isEmpty) {
        throw new UnknownHostException(host + ": Name or service not known")
      }
      ips
    }
  }

  def getByAddress(addr: Array[Byte]): InetAddress =
    getByAddress(null, addr)

  def getByAddress(host: String, addr: Array[Byte]): InetAddress = {
    /* Java 8 spec say adddress must be 4 or 16 bytes long, so no IPv6
     * scope_id complexity required here.
     */
    if (addr.length == 4) {
      new Inet4Address(addr.clone, host)
    } else if (addr.length == 16) {
      new Inet6Address(addr.clone, host)
    } else {
      throw new UnknownHostException(
        s"addr is of illegal length: ${addr.length}"
      )
    }
  }

  def getByName(host: String): InetAddress = {
    /* Design Note:
     *     A long comment because someone is going to have to maintain this
     * and will appreciate the clues. 18 lines of comments for 3 lines of code.
     *
     * The double lookup below, first to check if the host is a numeric
     * IPv4 or IPv6 address and then to look the host up as a non-numeric
     * name, may look somewhere between passing strange and straight out
     * dumb.
     *
     * It is because ScalaJVM creates the InetAddress with a null host name
     * if the host resolves as numeric.  If the host resolves to non-numeric
     * then the InetAddress is created using that String.
     *
     * There is not good way to test after a single omnibus lookup to tell
     * if the host resolved as numeric or non-numeric.  inet_pton() for
     * IPv4 addresses requires full dotted decimal: ddd.ddd.ddd.ddd.
     * ScalaJVM parses and passes some more obscure but valid IPv4 addresses.
     * There have long been test cases in InetAddressTest.scala for such.
     *
     * The less preferred inet_aton() handles these obscure cases but
     * misses more modern usages. inet_aton() is not POSIX, so it's portability
     * is an issue.
     *
     * Hence, the double lookup. Better solutions are welcome.
     */

    if (host == null || host.length == 0) {
      getLoopbackAddress()
    } else {
      InetAddress
        .getByNumericName(host)
        .getOrElse(InetAddress.getByNonNumericName(host))
    }
  }

  def getLocalHost(): InetAddress = {
    val MAXHOSTNAMELEN = 256.toUInt // SUSv2 255 + 1 for terminal NUL
    val hostName = stackalloc[Byte](MAXHOSTNAMELEN)

    val ghnStatus = unistd.gethostname(hostName, MAXHOSTNAMELEN);
    if (ghnStatus != 0) {
      throw new UnknownHostException(fromCString(strerror(errno)))
    } else {
      /* OS library routine should have NUL terminated 'hostName'.
       * If not, hostName(MAXHOSTNAMELEN) should be NUL from stackalloc.
       */
      InetAddress.getByName(fromCString(hostName))
    }
  }

  def getLoopbackAddress(): InetAddress = SocketHelpers.getLoopbackAddress()

}
