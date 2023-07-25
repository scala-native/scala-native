package java.net

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.annotation.tailrec

import java.net.SocketHelpers.sockaddrToByteArray

import java.{util => ju}
import ju.Objects
import ju.stream.Stream

import scala.scalanative.posix.errno.{errno, ENXIO}
import scala.scalanative.posix.net.`if`._
import scala.scalanative.posix.net.ifOps._
import scala.scalanative.posix.netinet.in._
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.sys.ioctl.ioctl
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.string._
import scala.scalanative.posix.unistd

import scala.scalanative.meta.LinktimeInfo

import macOsIf._
import macOsIfDl._

/* Design Notes:
 *   1) This code is Unix only. On Windows, "empty" values are returned.
 *      A Windows implementation is left as an exercise for the reader.
 *
 *   2) The Unix implementation often splits into a Linux path and a
 *      macOS/BSD path.  The former uses ioctl() calls and lets the
 *      operating system search for the named interface.  Such a kernel
 *      search should be marginally faster and less error prone than
 *      the user land search of getifaddrs() results done on the
 *      macOS/BSD path.
 *
 *   3) Virtual and/or sub-interface methods rely on the convention that such
 *      interfaces have a colon (:) in the name. Improvements are welcome.
 *
 *   4) For future reference:
 *      GetAdaptersAddresses() function exist on Windows Vista and later.
 *      Function returns a linked list of detailed adapter information
 *      (much more than just addresses).
 *      C examples are provided in the documentation on MSDN.
 */

class NetworkInterface private (ifName: String) {

  override def equals(that: Any): Boolean = that match {
    case that: NetworkInterface => this.hashCode() == that.hashCode()
    case _                      => false
  }

  def getDisplayName(): String = getName()

  def getHardwareAddress(): Array[Byte] = {
    if (LinktimeInfo.isWindows) new Array[Byte](0) // No Windows support
    else {
      NetworkInterface.unixImplGetHardwareAddress(ifName)
    }
  }

  def getIndex(): Int = {
    if (LinktimeInfo.isWindows) 0 // No Windows support
    else {
      NetworkInterface.unixImplGetIndex(ifName)
    }
  }

  def getInetAddresses(): ju.Enumeration[InetAddress] = {
    if (LinktimeInfo.isWindows) { // No Windows support
      ju.Collections.enumeration[InetAddress](new ju.ArrayList[InetAddress])
    } else {
      NetworkInterface.unixImplGetInetAddresses(ifName)
    }
  }

  def getInterfaceAddresses(): ju.List[InterfaceAddress] = {
    if (LinktimeInfo.isWindows) { // No Windows support
      ju.Collections.emptyList[InterfaceAddress]()
    } else {
      NetworkInterface.unixImplGetInterfaceAddresses(ifName)
    }
  }

  def getMTU(): Int = {
    if (LinktimeInfo.isWindows) 0 // No Windows support
    else {
      NetworkInterface.unixImplGetIfMTU(ifName)
    }
  }

  def getName(): String = ifName

  def getParent(): NetworkInterface = {
    if (LinktimeInfo.isWindows) null // No Windows support
    else if (!this.isVirtual()) null
    else {
      val parentName = ifName.split(":")(0)
      NetworkInterface.getByName(parentName)
    }
  }

  def getSubInterfaces(): ju.Enumeration[NetworkInterface] = {
    val ifList = new ju.ArrayList[NetworkInterface]()

    // No Windows support, so empty Enumeration will be returned.
    if (!LinktimeInfo.isWindows) {
      val allIfs = NetworkInterface.getNetworkInterfaces()
      val matchMe = s"${ifName}:"
      while (allIfs.hasMoreElements()) {
        val elem = allIfs.nextElement()
        val elemName = elem.getName()
        if (elemName.startsWith(matchMe))
          ifList.add(elem)
      }
    }
    ju.Collections.enumeration[NetworkInterface](ifList)
  }

  def inetAddresses(): Stream[InetAddress] = {
    if (LinktimeInfo.isWindows)
      Stream.empty[InetAddress]() // No Windows support
    else {
      NetworkInterface.unixImplInetAddresses(ifName)
    }
  }

  def isLoopback(): Boolean = {
    if (LinktimeInfo.isWindows) false // No Windows support
    else {
      val ifFlags = NetworkInterface.unixImplGetIfFlags(ifName)
      (ifFlags & unixIf.IFF_LOOPBACK) == unixIf.IFF_LOOPBACK
    }
  }

  def isPointToPoint(): Boolean = {
    if (LinktimeInfo.isWindows) false // No Windows support
    else {
      val ifFlags = NetworkInterface.unixImplGetIfFlags(ifName)
      (ifFlags & unixIf.IFF_POINTOPOINT) == unixIf.IFF_POINTOPOINT
    }
  }

  def isUp(): Boolean = {
    if (LinktimeInfo.isWindows) false // No Windows support
    else {
      val ifFlags = NetworkInterface.unixImplGetIfFlags(ifName)
      (ifFlags & unixIf.IFF_UP) == unixIf.IFF_UP
    }
  }

  // relies upon convention that Virtual or sub-interfaces have colon in name.
  def isVirtual(): Boolean = ifName.indexOf(':') >= 0 // a best guess

  override def hashCode(): Int = ifName.hashCode()

  def subInterfaces(): Stream[NetworkInterface] = {
    val allIfs = NetworkInterface.networkInterfaces()
    val matchMe = s"${ifName}:"
    allIfs.filter(_.getName().startsWith(matchMe))
  }

  def supportsMulticast(): Boolean = {
    if (LinktimeInfo.isWindows) false // No Windows support
    else {
      val ifFlags = NetworkInterface.unixImplGetIfFlags(ifName)
      (ifFlags & unixIf.IFF_MULTICAST) == unixIf.IFF_MULTICAST
    }
  }

  override def toString(): String = s"name:${ifName} (${ifName})"

}

object NetworkInterface {
  import unixIfaddrs._
  import unixIfaddrsOps._

  def getByIndex(index: Int): NetworkInterface = {
    if (index < 0)
      throw new IllegalArgumentException("Interface index can't be negative")

    if (LinktimeInfo.isWindows) {
      null
    } else {
      unixGetByIndex(index)
    }
  }

  def getByInetAddress(addr: InetAddress): NetworkInterface = {
    Objects.requireNonNull(addr)
    if (LinktimeInfo.isWindows) {
      null
    } else {
      unixGetByInetAddress(addr)
    }
  }

  def getByName(name: String): NetworkInterface = {
    Objects.requireNonNull(name)
    if (LinktimeInfo.isWindows) {
      null
    } else {
      unixGetByName(name)
    }
  }

  def getNetworkInterfaces(): ju.Enumeration[NetworkInterface] = {
    if (LinktimeInfo.isWindows) {
      null
    } else {
      unixGetNetworkInterfaces()
    }
  }

  /** networkInterfaces() method is Java 9. It is provided because Streams are
   *  less clumsy than Enumerations.
   */
  def networkInterfaces(): Stream[NetworkInterface] = {
    if (LinktimeInfo.isWindows) {
      null
    } else {
      unixNetworkInterfaces()
    }
  }

  private def createInetAddress(
      ifa: Ptr[ifaddrs],
      ifName: String
  ): Option[InetAddress] = {
    val sa = ifa.ifa_addr
    val af = sa.sa_family.toInt

    if (!((af == AF_INET) || (af == AF_INET6))) None
    else {
      val bytes = sockaddrToByteArray(sa)
      if (af == AF_INET) {
        Some(InetAddress.getByAddress(bytes))
      } else {
        val scopeId = sa.asInstanceOf[Ptr[sockaddr_in6]].sin6_scope_id.toInt
        Some(Inet6Address(bytes, "", scopeId, ifName))
      }
    }
  }

  private def createInterfaceAddress(
      ifa: Ptr[ifaddrs],
      interfaceName: String
  ): Option[InterfaceAddress] = {

    def decodePrefixLength(sa: Ptr[sockaddr]): Short = {
      val result =
        if (sa.sa_family.toInt == AF_INET) {
          val sin4 = sa.asInstanceOf[Ptr[sockaddr_in]]
          val mask = sin4.sin_addr.s_addr.toInt
          Integer.bitCount(mask)
        } else if (sa.sa_family.toInt == AF_INET6) {
          val sin6 = sa.asInstanceOf[Ptr[sockaddr_in6]]
          val longs =
            sin6.sin6_addr.at1.at(0).asInstanceOf[Ptr[scala.Long]]
          java.lang.Long.bitCount(longs(0)) + java.lang.Long.bitCount(longs(1))
        } else {
          0 // Blivet! Unknown address family, assume zero length prefix.
        }
      result.toShort
    }

    val sa = ifa.ifa_addr
    val af = sa.sa_family.toInt
    if (!((af == AF_INET) || (af == AF_INET6))) {
      None // Silently skip AF_PACKET (17) and such.
    } else {
      val bytes = sockaddrToByteArray(sa)
      val inetAddress = if (af == AF_INET) {
        InetAddress.getByAddress(bytes)
      } else {
        val scopeId = sa.asInstanceOf[Ptr[sockaddr_in6]].sin6_scope_id
        Inet6Address(bytes, "", scopeId.toInt, interfaceName)
      }

      val broadcastAddress: Option[Array[Byte]] =
        if (sa.sa_family.toInt == AF_INET6) None
        else if ((ifa.ifa_flags & unixIf.IFF_LOOPBACK.toUInt) != 0.toUInt) None
        else Some(sockaddrToByteArray(ifa.ifa_broadaddr))

      val prefixLen = decodePrefixLength(ifa.ifa_netmask)

      val ifAddress =
        new InterfaceAddress(inetAddress, broadcastAddress, prefixLen)

      Some(ifAddress)
    }
  }

  private def createNetworkInterface(ifa: Ptr[ifaddrs]): NetworkInterface = {
    val ifName = fromCString(ifa.ifa_name)
    new NetworkInterface(ifName)
  }

  private def unixGetByIndex(index: Int): NetworkInterface = {
    val buf = stackalloc[Byte](IF_NAMESIZE)

    val ret = if_indextoname(index.toUInt, buf)

    if (ret != null) unixGetByName(fromCString(ret))
    else if (errno == ENXIO) null // no interface has that index
    else
      throw new SocketException(fromCString(strerror(errno)))
  }

  private def unixGetByInetAddress(addr: InetAddress): NetworkInterface = {

    def found(addr: Array[Byte], addrLen: Int, sa: Ptr[sockaddr]): Boolean = {
      val sa_family = sa.sa_family.toInt
      if (sa_family == AF_INET6) {
        if (addrLen != 16) false
        else {
          val sa6 = sa.asInstanceOf[Ptr[sockaddr_in6]]
          val sin6Addr = sa6.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]
          memcmp(addr.at(0), sin6Addr, addrLen.toUInt) == 0
        }
      } else if (sa_family == AF_INET) {
        val sa4 = sa.asInstanceOf[Ptr[sockaddr_in]]
        val sin4Addr = sa4.sin_addr.at1.asInstanceOf[Ptr[Byte]]
        memcmp(addr.at(0), sin4Addr, addrLen.toUInt) == 0
      } else false
    }

    @tailrec
    def findIfInetAddress(
        ipAddress: Array[Byte],
        addrLen: Int,
        ifa: Ptr[ifaddrs]
    ): NetworkInterface = {
      if (ifa == null) null
      else if (found(ipAddress, addrLen, ifa.ifa_addr))
        createNetworkInterface(ifa)
      else
        findIfInetAddress(
          ipAddress,
          addrLen,
          ifa.ifa_next.asInstanceOf[Ptr[ifaddrs]]
        )
    }

    val addrBytes = addr.getAddress()
    val len = addrBytes.length // check this once, not N times

    if (!((len == 4) || (len == 16)))
      throw new SocketException(
        s"unixGetByInetAddress: wrong Array[Byte] length: ${len}"
      )
    else {
      val ifap = stackalloc[Ptr[ifaddrs]]()

      val gifStatus = getifaddrs(ifap)
      if (gifStatus == -1)
        throw new SocketException(
          s"getifaddrs failed: ${fromCString(strerror(errno))}"
        )

      val result =
        try {
          findIfInetAddress(addrBytes, len, !ifap)
        } finally {
          freeifaddrs(!ifap)
        }

      result
    }
  }

  private def unixGetByName(name: String): NetworkInterface = Zone {
    implicit z =>
      @tailrec
      def findIfName(
          cName: CString,
          ifa: Ptr[ifaddrs]
      ): NetworkInterface = {
        if (ifa == null) null
        else if (strcmp(ifa.ifa_name, cName) == 0)
          createNetworkInterface(ifa)
        else findIfName(cName, ifa.ifa_next.asInstanceOf[Ptr[ifaddrs]])
      }

      val cName = toCString(name)
      val ifap = stackalloc[Ptr[ifaddrs]]()

      val gifStatus = getifaddrs(ifap)
      if (gifStatus == -1)
        throw new SocketException(
          s"getifaddrs failed: ${fromCString(strerror(errno))}"
        )

      val result =
        try {
          findIfName(cName, !ifap)
        } finally {
          freeifaddrs(!ifap)
        }

      result
  }

  private def unixAccumulateNetworkInterfaces(
      accumulator: (NetworkInterface) => Unit
  ): Unit = {

    @tailrec
    def accumulateNetIfs(
        ni: Ptr[if_nameindex],
        addOne: (NetworkInterface) => Unit
    ): Unit = {
      if ((ni.if_index.toInt != 0) || (ni.if_name != null)) {
        val ifName =
          if (ni.if_name == null) ""
          else fromCString(ni.if_name)

        addOne(new NetworkInterface(ifName))

        accumulateNetIfs(
          ni + 1, // + 1 should skip entire structure
          accumulator
        )
      }
    }

    val nameIndex = if_nameindex()

    if (nameIndex == null)
      throw new SocketException(
        s"if_nameindex() failed: ${fromCString(strerror(errno))}"
      )

    try {
      accumulateNetIfs(nameIndex, accumulator)
    } finally {
      if_freenameindex(nameIndex)
    }
  }

  private def unixGetNetworkInterfaces(): ju.Enumeration[NetworkInterface] = {
    val ifList = new ju.ArrayList[NetworkInterface]()
    unixAccumulateNetworkInterfaces((netIf: NetworkInterface) => {
      ifList.add(netIf); ()
    })
    ju.Collections.enumeration[NetworkInterface](ifList)
  }

  private def unixNetworkInterfaces(): Stream[NetworkInterface] = {
    val builder = Stream.builder[NetworkInterface]()
    unixAccumulateNetworkInterfaces((netIf: NetworkInterface) => {
      builder.add(netIf); ()
    })
    builder.build()
  }

  /* Implement OS specific class & helper methods
   */

  private def linuxImplGetIoctlFd(): Int = {
    val fd = socket(AF_INET, SOCK_DGRAM, 0)

    if (fd == -1) {
      val msg = fromCString(strerror(errno))
      throw new SocketException(s"socket(AF_INET, SOCK_DGRAM) failed: ${msg}\n")
    }

    fd
  }

  private def macOsImplExecCallback(
      ifName: String,
      callback: Ptr[ifaddrs] => Tuple2[Int, Array[Byte]]
  ): Tuple2[Int, Array[Byte]] = {
    @tailrec
    def findAfLinkIfName(
        ifNameC: CString,
        ifa: Ptr[ifaddrs]
    ): Ptr[ifaddrs] = {
      if (ifa == null) null
      else if ((strcmp(ifNameC, ifa.ifa_name) == 0)
          && (ifa.ifa_addr.sa_family.toInt == 18 /* AF_LINK */ ))
        ifa
      else
        findAfLinkIfName(ifNameC, ifa.ifa_next)
    }

    val ifap = stackalloc[Ptr[ifaddrs]]()

    val gifStatus = getifaddrs(ifap)
    if (gifStatus == -1)
      throw new SocketException(
        s"getifaddrs failed: ${fromCString(strerror(errno))}"
      )

    try
      Zone { implicit z =>
        val foundIfa = findAfLinkIfName(toCString(ifName), !ifap)
        callback(foundIfa)
      }
    finally {
      freeifaddrs(!ifap)
    }
  }

  private def unixImplGetIndex(ifName: String): Int = Zone { implicit z =>
    // toInt truncation OK, since index will never be larger than MAX_INT
    if_nametoindex(toCString(ifName)).toInt
      // Return 0 on error. Do not give errno error message.
  }

  private def unixImplGetHardwareAddress(ifName: String): Array[Byte] = {
    if (LinktimeInfo.isLinux)
      linuxImplGetHardwareAddress(ifName)
    else
      macOsImplGetHardwareAddress(ifName)
  }

  private def macOsImplGetHardwareAddress(ifName: String): Array[Byte] = {
    def decodeSocketDl(sockaddrDl: Ptr[macOsIfDl.sockaddr_dl]): Array[Byte] = {

      val nBytes = if (sockaddrDl == null) 0 else sockaddrDl.sdl_alen.toInt
      val bytes = new Array[Byte](nBytes)

      if (nBytes > 0) { // skip name
        val src = sockaddrDl.sdl_data.at(sockaddrDl.sdl_nlen.toInt)
        val dst = bytes.at(0)
        memcpy(dst, src, nBytes.toUInt)
      }
      bytes
    }

    def cb(ifa: Ptr[ifaddrs]): Tuple2[Int, Array[Byte]] = {
      val arr =
        if (ifa == null) new Array[Byte](0)
        else
          decodeSocketDl(ifa.ifa_addr.asInstanceOf[Ptr[sockaddr_dl]])

      (0, arr)
    }

    macOsImplExecCallback(ifName, cb)._2
  }

  private def linuxImplGetHardwareAddress(ifName: String): Array[Byte] = Zone {
    implicit z =>
      // acknowledge:
      //   https://www.geekpage.jp/en/programming/linux-network/get-macaddr.php

      val request = stackalloc[unixIf.ifreq_hwaddress]()

      strncpy(
        request.at1.asInstanceOf[CString],
        toCString(ifName),
        (unixIf.IFNAMSIZ - 1).toUSize
      )

      val saP = request.at2.asInstanceOf[Ptr[sockaddr]]
      saP.sa_family = AF_INET.toUShort

      val fd = linuxImplGetIoctlFd()

      try {
        val status =
          ioctl(fd, unixIf.SIOCGIFHWADDR, request.asInstanceOf[Ptr[Byte]]);
        if (status != 0) {
          val msg = fromCString(strerror(errno))
          throw new SocketException(s"ioctl SIOCGIFHWADDR failed: ${msg}\n")
        }
      } finally {
        unistd.close(fd)
      }

      val hwAddress = new Array[Byte](6)
      val hwAddrBytes = request.at2.sa_data

      for (j <- 0 until 6)
        hwAddress(j) = hwAddrBytes(j)

      hwAddress
  }

  private def unixImplGetIfMTU(ifName: String): Int = {
    if (LinktimeInfo.isLinux)
      linuxImplGetIfMTU(ifName)
    else
      macOsImplGetIfMTU(ifName)
  }

  private def macOsImplGetIfMTU(ifName: String): Int = {
    def cb(ifa: Ptr[ifaddrs]): Tuple2[Int, Array[Byte]] = {
      val result =
        if (ifa == null) 0
        else
          ifa.ifa_data.asInstanceOf[Ptr[macOsIf.if_data]].ifi_mtu.toInt

      (result, null)
    }

    macOsImplExecCallback(ifName, cb)._1
  }

  private def linuxImplGetIfMTU(ifName: String): Int = Zone { implicit z =>
    val request = stackalloc[unixIf.ifreq_mtu]()

    strncpy(
      request.at1.asInstanceOf[CString],
      toCString(ifName),
      (unixIf.IFNAMSIZ - 1).toUSize
    )

    val saP = request.at2.asInstanceOf[Ptr[sockaddr]]
    saP.sa_family = AF_INET.toUShort

    val fd = linuxImplGetIoctlFd()

    try {
      val status =
        ioctl(fd, unixIf.SIOCGIFMTU, request.asInstanceOf[Ptr[Byte]]);
      if (status != 0)
        throw new SocketException(
          s"ioctl SIOCGIFMTU failed: ${fromCString(strerror(errno))}"
        )

    } finally {
      unistd.close(fd)
    }

    request._2 // ifr_mtu
  }

  private def unixImplGetIfFlags(ifName: String): Short = {
    if (LinktimeInfo.isLinux)
      linuxImplGetIfFlags(ifName)
    else
      macOsImplGetIfFlags(ifName)
  }

  private def macOsImplGetIfFlags(ifName: String): Short = {
    def cb(ifa: Ptr[ifaddrs]): Tuple2[Int, Array[Byte]] = {
      val result =
        if (ifa == null) 0
        else ifa.ifa_flags.toInt

      (result, null)
    }

    macOsImplExecCallback(ifName, cb)._1.toShort
  }

  private def linuxImplGetIfFlags(ifName: String): Short = Zone { implicit z =>
    val request = stackalloc[unixIf.ifreq_flags]()

    strncpy(
      request.at1.asInstanceOf[CString],
      toCString(ifName),
      (unixIf.IFNAMSIZ - 1).toUSize
    )

    val saP = request.at2.asInstanceOf[Ptr[sockaddr]]
    saP.sa_family = AF_INET.toUShort

    val fd = linuxImplGetIoctlFd()

    try {
      val status =
        ioctl(fd, unixIf.SIOCGIFFLAGS, request.asInstanceOf[Ptr[Byte]]);

      if (status != 0) {
        val msg = fromCString(strerror(errno))
        throw new SocketException(s"ioctl SIOCGIFFLAGS failed: ${msg}\n")
      }
    } finally {
      unistd.close(fd)
    }

    request._2 // ifr_flags
  }

  private def unixAccumulateInetAddresses(
      ifNameJ: String,
      accumulator: (InetAddress) => Unit
  ): Unit = Zone { implicit z =>
    @tailrec
    def accumulateInetAddresses(
        ifNameC: CString,
        addOne: (InetAddress) => Unit,
        ifa: Ptr[ifaddrs]
    ): Unit = {
      if (ifa != null) {
        if (strcmp(ifNameC, ifa.ifa_name) == 0) {
          createInetAddress(ifa, ifNameJ).map(ia => addOne(ia))
        }
        accumulateInetAddresses(
          ifNameC,
          addOne,
          ifa.ifa_next.asInstanceOf[Ptr[ifaddrs]]
        )
      }
    }

    val ifap = stackalloc[Ptr[ifaddrs]]()

    val gifStatus = getifaddrs(ifap)

    if (gifStatus == -1)
      throw new SocketException(
        s"getifaddrs failed: ${fromCString(strerror(errno))}"
      )

    try {
      accumulateInetAddresses(toCString(ifNameJ), accumulator, !ifap)
    } finally {
      freeifaddrs(!ifap)
    }
  }

  private def unixAccumulateInterfaceAddresses(
      ifName: String,
      accumulator: (InterfaceAddress) => Unit
  ): Unit = Zone { implicit z =>
    @tailrec
    def accumulateInterfaceAddresses(
        ifNameJ: String,
        ifNameC: CString,
        addOne: (InterfaceAddress) => Unit,
        ifa: Ptr[ifaddrs]
    ): Unit = {
      if (ifa != null) {
        if (strcmp(ifNameC, ifa.ifa_name) == 0) {
          createInterfaceAddress(ifa, ifNameJ).map(ia => addOne(ia))
        }
        accumulateInterfaceAddresses(
          ifNameJ,
          ifNameC,
          addOne,
          ifa.ifa_next.asInstanceOf[Ptr[ifaddrs]]
        )
      }
    }

    val ifap = stackalloc[Ptr[ifaddrs]]()

    val gifStatus = getifaddrs(ifap)

    if (gifStatus == -1)
      throw new SocketException(
        s"getifaddrs failed: ${fromCString(strerror(errno))}"
      )

    try {
      accumulateInterfaceAddresses(
        ifName,
        toCString(ifName),
        accumulator,
        !ifap
      )
    } finally {
      freeifaddrs(!ifap)
    }
  }

  private def unixImplGetInterfaceAddresses(
      ifName: String
  ): ju.List[InterfaceAddress] = {
    val ifaList = new ju.ArrayList[InterfaceAddress]()
    unixAccumulateInterfaceAddresses(
      ifName,
      (ifa: InterfaceAddress) => { ifaList.add(ifa); () }
    )
    ifaList
  }

  private def unixImplGetInetAddresses(
      ifName: String
  ): ju.Enumeration[InetAddress] = {
    val ifList = new ju.ArrayList[InetAddress]()
    unixAccumulateInetAddresses(
      ifName,
      (ia: InetAddress) => { ifList.add(ia); () }
    )
    ju.Collections.enumeration[InetAddress](ifList)
  }

  private def unixImplInetAddresses(ifName: String): Stream[InetAddress] = {
    val builder = Stream.builder[InetAddress]()
    unixAccumulateInetAddresses(
      ifName,
      (ia: InetAddress) => { builder.add(ia); () }
    )
    builder.build()
  }

}

@extern
private object unixIfaddrs {
  /* Reference: man getifaddrs
   *            #include <ifaddrs.h>
   */

  // format: off
  type ifaddrs = CStruct7[
    Ptr[Byte], /* Ptr[ifaddrs] */ // ifa_next: Next item in list
    CString, // ifa_name: Name of interface
    CUnsignedInt, // ifa_flags: Flags from SIOCGIFFLAGS
    Ptr[sockaddr], // ifa_addr: Address of interface
    Ptr[sockaddr], // ifa_netmask: Netmask of interface
    // ifu_broadaddr: Broadcast address of interface
    // ifu_dstaddr: Point-to-point destination address
    Ptr[sockaddr], // union: ifu_broadaddr, ifu_dstaddr
    Ptr[Byte] // ifa_data: Address-specific data
  ]
  // format: on

  def getifaddrs(ifap: Ptr[Ptr[ifaddrs]]): CInt = extern

  def freeifaddrs(ifa: Ptr[ifaddrs]): Unit = extern
}

private object unixIfaddrsOps {
  import unixIfaddrs._

  implicit class unixIfaddrOps(val ptr: Ptr[ifaddrs]) extends AnyVal {
    def ifa_next: Ptr[ifaddrs] = ptr._1.asInstanceOf[Ptr[ifaddrs]]
    def ifa_name: CString = ptr._2
    def ifa_flags: CUnsignedInt = ptr._3
    def ifa_addr: Ptr[sockaddr] = ptr._4
    def ifa_netmask: Ptr[sockaddr] = ptr._5
    def ifa_broadaddr: Ptr[sockaddr] = ptr._6
    def ifa_dstaddr: Ptr[sockaddr] = ptr._6
    def ifa_data: Ptr[Byte] = ptr._7

    // ifa fields are read-only in use, so no Ops here to set them.
  }
}

@extern
private object unixIf {
  /* Reference: man 7 netdevice
   *            #include <net/if.h>
   */

  // Three SN-only types used to facilitate retrieving specific types of data.
  type ifreq_hwaddress = CStruct2[
    CArray[CChar, Nat.Digit2[Nat._1, Nat._6]],
    sockaddr
  ]

  type ifreq_mtu = CStruct2[
    CArray[CChar, Nat.Digit2[Nat._1, Nat._6]],
    CInt
  ]

  type ifreq_flags = CStruct2[
    CArray[CChar, Nat.Digit2[Nat._1, Nat._6]],
    CShort
  ]

  @name("scalanative_ifnamesiz")
  def IFNAMSIZ: CInt = extern

  @name("scalanative_iff_broadcast")
  def IFF_BROADCAST: CInt = extern

  @name("scalanative_iff_loopback")
  def IFF_LOOPBACK: CInt = extern

  @name("scalanative_iff_multicast")
  def IFF_MULTICAST: CInt = extern

  @name("scalanative_iff_pointopoint")
  def IFF_POINTOPOINT: CInt = extern

  @name("scalanative_iff_running")
  def IFF_RUNNING: CInt = extern

  @name("scalanative_siocgifflags")
  def SIOCGIFFLAGS: CInt = extern

  @name("scalanative_siocgifhwaddr")
  def SIOCGIFHWADDR: CInt = extern

  @name("scalanative_siocgifmtu")
  def SIOCGIFMTU: CInt = extern

  @name("scalanative_iff_up")
  def IFF_UP: CInt = extern
}

private object macOsIf {

  /*  Scala if_data & corresponding ifDataOps definitions are not complete.
   *  Only items used in NetworkInterface are declared.
   */

  /* Reference: macOS
   *  /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include
   *      /net/if_var.h
   *
   * struct if_data {
   *   // generic interface information
   *   u_char          ifi_type;       // ethernet, tokenring, etc
   *   u_char          ifi_typelen;    // Length of frame type id
   *   u_char          ifi_physical;   // e.g., AUI, Thinnet, 10base-T, etc
   *   u_char          ifi_addrlen;    // media address length
   *   u_char          ifi_hdrlen;     // media header length
   *   u_char          ifi_recvquota;  // polling quota for receive intrs
   *   u_char          ifi_xmitquota;  // polling quota for xmit intrs
   *   u_char          ifi_unused1;    // for future use
   *   u_int32_t       ifi_mtu;        // maximum transmission unit
   */

  // Incomplete
  type if_data = CStruct2[
    CLongLong, // Placeholder, consolidate & skip fields of no interest.
    CUnsignedInt // ifi_mtu
  ]

  // Incomplete, corresponding to incomplete if_data just above.
  implicit class ifDataOps(val ptr: Ptr[if_data]) extends AnyVal {
    def ifi_mtu: CUnsignedInt = ptr._2
  }
  // ifi fields read-only fields in use, so no Ops here to set them.
}

private object macOsIfDl {
  /*  Scala sockaddr_dl & corresponding sockaddrDlOps definitions are not
   *  complete. They are only what NetworkInterface uses.
   */

  /* Reference: FreeBSD man sockaddr_dl
   *    #include <net/if_dl.h>
   *
   * For sdl_data field, use the larger of macOS defined 12 and
   * FreeBSD defined 46.
   *
   * struct sockaddr_dl
   *  The sockaddr_dl structure is used to describe a layer 2 link-level
   *  address. The structure has the following members:
   *
   *      ushort_t sdl_family;     // address family
   *      ushort_t sdl_index;      // if != 0, system interface index
   *      uchar_t  sdl_type;       // interface type
   *      uchar_t  sdl_nlen;       // interface name length
   *      uchar_t  sdl_alen;       // link level address length
   *      uchar_t  sdl_slen;       // link layer selector length
   *      char     sdl_data[46];   // contains both if name and ll address
   */

  // sdl_data, max(macOs == 12, FreeBsd == 46)
  type _46 = Nat.Digit2[Nat._4, Nat._6]
  type sdl_data_t = CArray[CChar, _46]

  type sockaddr_dl = CStruct8[
    Byte, // sdl_len;    //  Total length of sockaddr
    Byte, // sdl_family; // address family
    CShort, // sdl_index
    Byte, // sdl_type
    Byte, // sdl_nlen
    Byte, // sdl_alen
    Byte, // sdl_slen
    sdl_data_t
  ]

  implicit class sockaddrDlOps(val ptr: Ptr[sockaddr_dl]) extends AnyVal {
    def sdl_len: UByte = ptr._1.toUByte
    def sdl_family: UByte = ptr._2.toUByte
    def sdl_index: UShort = ptr._3.toUShort
    def sdl_type: UByte = ptr._4.toUByte
    def sdl_nlen: UByte = ptr._5.toUByte
    def sdl_alen: UByte = ptr._6.toUByte
    def sdl_slen: UByte = ptr._7.toUByte
    def sdl_data: sdl_data_t = ptr._8
  }
}
