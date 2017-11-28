package java.net

import scala.collection.mutable.MutableList
import scala.collection.mutable.ArrayBuffer
import collection.JavaConverters._

import scalanative.native._
import scalanative.posix.net._if._
import scalanative.posix.net.ifOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.socketOps._
import scalanative.posix.sys.ioctl._
import scalanative.posix.unistd._
import scalanative.native.ifaddrs._
import scalanative.native.ifaddrsOps.ifaddrsOps
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._

import java.util.Enumeration

// Ported from Apache Harmony

final class NetworkInterface private (name: String,
                                      displayName: String,
                                      interfaceIndex: Int)
    extends Object {

  private val addresses: MutableList[InetAddress]     = new MutableList()
  private var parent: NetworkInterface                = null
  private val children: MutableList[NetworkInterface] = new MutableList()
  private val hash                                    = name.hashCode()

  def this(name: String,
           displayName: String,
           addresses: Array[InetAddress],
           interfaceIndex: Int) = {
    this(name, displayName, interfaceIndex)

    if (addresses != null) {
      this.addresses ++= addresses
    }
  }

  private[net] def getIndex(): Int = interfaceIndex

  private[net] def getFirstAddress(): InetAddress = {
    if (addresses.size >= 1)
      addresses.head
    else
      null
  }

  private[net] def getName(): String = name

  def getInetAddresses(): Enumeration[InetAddress] =
    addresses.iterator.asJavaEnumeration

  private def getFullFormOfCompressesIPV6Address(compressedArg: String) = {
    var compressed         = compressedArg
    val fullForm           = new StringBuilder(39)
    val NUM_OF_IPV6_FIELDS = 8

    var fields = compressed.split(":")

    var numOfCompressedFields: Int = 0

    if (compressed.startsWith("::")) {
      compressed = compressed.replace("::", "")
      fields = compressed.split(":")
      numOfCompressedFields = NUM_OF_IPV6_FIELDS - fields.size
      for (i <- 0 until numOfCompressedFields) { fullForm ++= "0:" }
      fields.foreach(fullForm ++= _ + ":")
    } else if (compressed.endsWith(":")) {
      compressed = compressed.replace("::", "")
      fields = compressed.split(":")
      numOfCompressedFields = NUM_OF_IPV6_FIELDS - fields.size
      fields.foreach(fullForm ++= _ + ":")
      for (i <- 0 until numOfCompressedFields) { fullForm ++= "0:" }
    } else {
      numOfCompressedFields = NUM_OF_IPV6_FIELDS - fields.size + 1
      fields.foreach((field: String) =>
        field match {
          case "" =>
            for (i <- 0 until numOfCompressedFields) { fullForm ++= "0:" }
          case _ =>
            fullForm ++= field + ":"
      })
    }

    fullForm.dropRight(1).toString()
  }

  def getDisplayName(): String = displayName match {
    case "" => name
    case _  => displayName
  }

  override def equals(obj: Any): Boolean = obj match {
    case obj: AnyRef if this eq obj => true
    case netif: NetworkInterface => {
      ((name != "" && netif.getName() == name) ||
      (name == "" && netif.getName() == displayName)) &&
      netif.addresses == addresses
    }
    case _ => false
  }

  override def hashCode(): Int = hash

  override def toString(): String = {
    val string = new StringBuilder(25)
    string += '[' ++= name ++= "][" ++= displayName += ']'
    addresses.foreach(addr => string += '[' ++= addr.toString() += ']')

    string.toString()
  }

  def getInterfaceAddresses(): java.util.List[InterfaceAddress] = {
    val ptr                 = stackalloc[Ptr[ifaddrs]]
    val ret                 = ifaddrs.getifaddrs(ptr)
    val first: Ptr[ifaddrs] = !ptr
    val toReturn            = getInterfaceAddressesImpl(first, List()).asJava
    freeifaddrs(first)
    toReturn
  }

  private def getInterfaceAddressesImpl(
      netif: Ptr[ifaddrs.ifaddrs],
      acc: List[InterfaceAddress]): List[InterfaceAddress] = {
    if (netif != null) {
      val netifName = fromCString(netif.ifa_name)
      if (netifName == name && netif.ifa_addr != null) {
        val addr: Ptr[sockaddr] = netif.ifa_addr
        if (addr.sa_family == AF_INET.toUInt) {
          val addr4     = addr.cast[Ptr[sockaddr_in]]
          val addr4in   = addr4.sin_addr.in_addr
          val addrBytes = Array.fill[Byte](4)(0)
          for (i <- 3 to 0 by -1) {
            addrBytes(i) = (addr4in >> i * 8).toByte
          }
          val mask    = netif.ifa_netmask
          val mask4   = mask.cast[Ptr[sockaddr_in]]
          val mask4in = mask4.sin_addr.in_addr
          val prefixLength =
            mask4in.toBinaryString.groupBy(identity).mapValues(_.size)('1')

          getInterfaceAddressesImpl(
            (netif.ifa_next).cast[Ptr[ifaddrs.ifaddrs]],
            new InterfaceAddress(new Inet4Address(addrBytes),
                                 prefixLength.toShort) :: acc)
        } else {
          val addr6     = addr.cast[Ptr[sockaddr_in6]]
          val addr6in   = addr6.sin6_addr
          val addrBytes = Array.fill[Byte](16)(0)
          for (i <- 0 until 16) {
            addrBytes(i) = (!((addr6in._1)._1 + i)).toByte
          }
          val mask      = netif.ifa_netmask
          val mask6     = mask.cast[Ptr[sockaddr_in6]]
          val mask6in   = mask6.sin6_addr
          val maskBytes = Array.fill[Long](16)(0)
          for (i <- 0 until 16) {
            maskBytes(i) = (!((mask6in._1)._1 + i)).toLong
          }
          val prefixLength = maskBytes
            .map(
              _.toBinaryString
                .groupBy(identity)
                .mapValues(_.size)
                .getOrElse('1', 0))
            .reduceLeft(_ + _)

          getInterfaceAddressesImpl(
            netif.ifa_next.cast[Ptr[ifaddrs.ifaddrs]],
            new InterfaceAddress(new Inet6Address(addrBytes),
                                 prefixLength.toShort) :: acc)
        }
      } else {
        getInterfaceAddressesImpl(netif.ifa_next.cast[Ptr[ifaddrs.ifaddrs]],
                                  acc)
      }
    } else {
      acc
    }
  }

  def getSubInterfaces(): Enumeration[NetworkInterface] =
    children.iterator.asJavaEnumeration

  def getParent(): NetworkInterface = parent

  def isUp(): Boolean = {
    if (addresses.size == 0)
      false
    else
      getNetworkInterfaceAttribute(IFF_RUNNING)
  }

  def isLoopback(): Boolean = {
    if (addresses.size == 0) {
      false
    } else {
      getNetworkInterfaceAttribute(IFF_LOOPBACK)
    }
  }

  def isPointToPoint(): Boolean = {
    if (addresses.size == 0) {
      false
    } else {
      getNetworkInterfaceAttribute(IFF_POINTOPOINT)
    }
  }

  def supportsMulticast(): Boolean = {
    if (addresses.size == 0) {
      false
    } else {
      getNetworkInterfaceAttribute(IFF_MULTICAST)
    }
  }

  def getHardwareAddress(): Array[Byte] = {
    if (addresses.size == 0) {
      Array[Byte]()
    } else {
      val ifreqSize   = 40
      val ifnameSize  = 16
      val macAddrSize = 6
      val toReturn    = Array.fill[Byte](macAddrSize)(0)
      val fd          = socket(AF_INET, SOCK_STREAM, 0)
      if (fd < 0) {
        throw new SocketException(
          "The operation failed with no recovery possible")
      }
      val ifc = stackalloc[ifconf]
      val buf = stackalloc[Byte](2048)
      ifc.ifc_len = 2048
      ifc.ifc_buf = buf
      if (ioctl(fd, SIOCGIFCONF, ifc.cast[Ptr[Byte]]) != 0) {
        throw new SocketException(
          "The operation failed with no recovery possible")
      }

      val ifrs = ifc.ifc_buf
      val n    = ifc.ifc_len / ifreqSize

      for (i <- 0 until n.toInt) {
        val ifr    = (ifrs + (i * ifreqSize)).cast[Ptr[ifreq]]
        val ifname = ifr.ifr_name.cast[CString]
        if (fromCString(ifname) == name) {
          if (ioctl(fd, SIOCGIFHWADDR, ifr.cast[Ptr[Byte]]) != 0) {
            close(fd)
            throw new SocketException(
              "Could not retrieve information about socket.")
          }
          var allZero = true
          for (j <- 0 until macAddrSize) {
            toReturn(j) = !((ifr.ifr_hwaddr._2)._1 + j)
            if (toReturn(j) != 0) {
              allZero = false
            }
          }
          close(fd)
          if (allZero) {
            return null
          }
          return toReturn
        }
      }
      close(fd)
      Array[Byte]()
    }
  }

  def getMTU(): Int = {
    if (addresses.size == 0) {
      0
    } else {
      val ifreqSize  = 40
      val ifnameSize = 16
      val fd         = socket(AF_INET, SOCK_STREAM, 0)
      if (fd < 0) {
        throw new SocketException(
          "The operation failed with no recovery possible")
      }
      val ifc = stackalloc[ifconf]
      val buf = stackalloc[Byte](2048)
      ifc.ifc_len = 2048
      ifc.ifc_buf = buf
      if (ioctl(fd, SIOCGIFCONF, ifc.cast[Ptr[Byte]]) != 0) {
        throw new SocketException(
          "The operation failed with no recovery possible")
      }

      val ifrs = ifc.ifc_buf
      val n    = ifc.ifc_len / ifreqSize

      for (i <- 0 until n.toInt) {
        val ifr    = (ifrs + (i * ifreqSize)).cast[Ptr[ifreq]]
        val ifname = ifr.ifr_name.cast[CString]
        if (fromCString(ifname) == name) {
          if (ioctl(fd, SIOCGIFMTU, ifr.cast[Ptr[Byte]]) != 0) {
            close(fd)
            throw new SocketException(
              "Could not retrieve information about socket.")
          }
          val mtu = ifr.ifr_mtu
          close(fd)
          return mtu
        }
      }
      close(fd)
      0
    }
  }

  def isVirtual(): Boolean = parent != null

  private def getNetworkInterfaceAttribute(iiFlag: Long): Boolean = {
    val ifreqSize  = 40
    val ifnameSize = 16
    val fd         = socket(AF_INET, SOCK_STREAM, 0)
    if (fd < 0) {
      throw new SocketException(
        "The operation failed with no recovery possible")
    }
    val ifc = stackalloc[ifconf]
    val buf = stackalloc[Byte](2048)
    ifc.ifc_len = 2048
    ifc.ifc_buf = buf
    if (ioctl(fd, SIOCGIFCONF, ifc.cast[Ptr[Byte]]) != 0) {
      throw new SocketException(
        "The operation failed with no recovery possible")
    }

    val ifrs = ifc.ifc_buf
    val n    = ifc.ifc_len / ifreqSize

    for (i <- 0 until n.toInt) {
      val ifr    = (ifrs + (i * ifreqSize)).cast[Ptr[ifreq]]
      val ifname = ifr.ifr_name.cast[CString]
      if (fromCString(ifname) == name) {
        if (ioctl(fd, SIOCGIFFLAGS, ifr.cast[Ptr[Byte]]) != 0) {
          close(fd)
          throw new SocketException(
            "Could not retrieve information about socket.")
        }
        val flags = ifr.ifr_flags
        close(fd)
        return (flags & iiFlag) == iiFlag
      }
    }
    close(fd)
    false
  }
}

object NetworkInterface {
  private def getNetworkInterfacesImpl(): Array[NetworkInterface] = {
    val ptr                 = stackalloc[Ptr[ifaddrs]]
    val ret                 = ifaddrs.getifaddrs(ptr)
    val first: Ptr[ifaddrs] = !ptr
    val toReturn =
      traverseInterfaces(first, ArrayBuffer.empty[NetworkInterface])
    freeifaddrs(first)
    toReturn
  }

  private def traverseInterfaces(
      netif: Ptr[ifaddrs],
      acc: ArrayBuffer[NetworkInterface]): Array[NetworkInterface] = {
    if (netif != null) {
      val netifName = fromCString(netif.ifa_name)
      val currNetif = acc.find(_.getName() == netifName) match {
        case Some(someNetif) => someNetif
        case None =>
          new NetworkInterface(netifName,
                               netifName,
                               if_nametoindex(netif.ifa_name).toInt)
      }
      if (netif.ifa_addr != null) {
        val addr: Ptr[sockaddr] = netif.ifa_addr
        if (addr.sa_family == AF_INET.toUInt) {
          val addr4     = addr.cast[Ptr[sockaddr_in]]
          val addr4in   = addr4.sin_addr.in_addr
          val addrBytes = Array.fill[Byte](4)(0)

          for (i <- 3 to 0 by -1) {
            addrBytes(i) = (addr4in >> i * 8).toByte
          }
          val inet = new Inet4Address(addrBytes)
          currNetif.addresses += inet
          acc.find(_.getName() == netifName) match {
            case Some(someNetif) =>
              traverseInterfaces(netif.ifa_next.cast[Ptr[ifaddrs]], acc)
            case None =>
              traverseInterfaces(netif.ifa_next.cast[Ptr[ifaddrs]],
                                 acc += currNetif)
          }
        } else {
          val addr6     = addr.cast[Ptr[sockaddr_in6]]
          val addr6in   = addr6.sin6_addr
          val addrBytes = Array.fill[Byte](16)(0)
          for (i <- 0 until 16) {
            addrBytes(i) = (!((addr6in._1)._1 + i)).toByte
          }
          val inet = new Inet6Address(addrBytes)
          currNetif.addresses += inet
          acc.find(_.getName() == netifName) match {
            case Some(someNetif) =>
              traverseInterfaces(netif.ifa_next.cast[Ptr[ifaddrs]], acc)
            case None =>
              traverseInterfaces(netif.ifa_next.cast[Ptr[ifaddrs]],
                                 acc += currNetif)
          }
        }
      } else {
        traverseInterfaces(netif.ifa_next.cast[Ptr[ifaddrs]], acc += currNetif)
      }
    } else {
      acc.toArray
    }
  }

  def getByName(interfaceName: String): NetworkInterface = {
    if (interfaceName == null) {
      throw new NullPointerException("interface name is null")
    }

    val interfaces = getNetworkInterfaces().asScala
    if (interfaces != null)
      interfaces.find(x => x.getName() == interfaceName) match {
        case Some(interface) => interface
        case None            => null
      } else
      null
  }

  def getByInetAddress(address: InetAddress): NetworkInterface = {
    if (address == null) {
      throw new NullPointerException("address is null")
    }

    val interfaces = getNetworkInterfaces().asScala
    if (interfaces != null)
      interfaces.find(x => x.addresses.exists(y => y == address)) match {
        case Some(interface) => interface
        case None            => null
      } else
      null
  }

  def getNetworkInterfaces(): Enumeration[NetworkInterface] = {
    val interfaces = getNetworkInterfacesImpl()
    if (interfaces == null) {
      null
    } else {
      interfaces.foreach(netif =>
        netif.addresses.foreach { addr =>
          if (addr != null || addr.isLinkLocalAddress() || addr
                .isSiteLocalAddress()) {
            addr.asInstanceOf[Inet6Address].scopedIf = netif
            addr.asInstanceOf[Inet6Address].ifname = netif.getName()
            addr.asInstanceOf[Inet6Address].scope_ifname_set = true
          }
      })

      val netifMap = interfaces.map(netif => (netif.getName(), netif)).toMap
      interfaces.foreach(netif =>
        netif.getName().split(":") match {
          case Array(x, _, _*) if netifMap.contains(x) => {
            netifMap(x).children += netif
            netif.parent = netifMap(x)
            netifMap(x).addresses ++= netif.addresses
          }
          case _ => // Do nothing
      })

      interfaces.iterator.asJavaEnumeration
    }
  }
}
