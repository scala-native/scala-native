package java.net

// Ported from Apache Harmony
final class Inet6Address private[net] (ipAddress: Array[Byte],
                                       host: String,
                                       scopeId: Int)
    extends InetAddress(ipAddress, host) {

  private[net] def this(ipAddress: Array[Byte]) = this(ipAddress, null, 0)

  private[net] def this(ipAddress: Array[Byte], host: String) =
    this(ipAddress, host, 0)

  def getScopeId(): Int = scopeId

  override def isLinkLocalAddress(): Boolean =
    (ipAddress(0) == -2) && ((ipAddress(1) & 255) >>> 6) == 2

  override def isAnyLocalAddress(): Boolean = ipAddress.forall(_ == 0)

  override def isLoopbackAddress(): Boolean = {
    if (ipAddress(15) != 1)
      return false

    ipAddress.dropRight(1).forall(_ == 0)
  }

  @transient
  private[net] var scopedIf: NetworkInterface = null
  private[net] var ifname: String             = null
  private[net] var scope_ifname_set: Boolean  = false

  override def isMCGlobal(): Boolean =
    (ipAddress(0) == -1) && (ipAddress(1) & 15) == 14

  override def isMCLinkLocal(): Boolean =
    (ipAddress(0) == -1) && (ipAddress(1) & 15) == 2

  override def isMCNodeLocal(): Boolean =
    (ipAddress(0) == -1) && (ipAddress(1) & 15) == 1

  override def isMCOrgLocal(): Boolean =
    (ipAddress(0) == -1) && (ipAddress(1) & 15) == 8

  override def isMCSiteLocal(): Boolean =
    (ipAddress(0) == -1) && (ipAddress(1) & 15) == 5

  override def isMulticastAddress(): Boolean = ipAddress(0) == -1

  override def isSiteLocalAddress(): Boolean =
    (ipAddress(0) == -2) && ((ipAddress(1) & 255) >>> 6) == 3

  def isIPv4CompatibleAddress(): Boolean = ipAddress.take(12).forall(_ == 0)

}

object Inet6Address extends InetAddressBase {

  def getByAddress(host: String,
                   addr: Array[Byte],
                   scope_id: Int): Inet6Address = {
    if (addr == null || addr.length != 16) {
      throw new UnknownHostException("Illegal IPv6 address")
    }
    if (scope_id < 0) {
      new Inet6Address(addr, host, 0)
    } else {
      new Inet6Address(addr, host, scope_id)
    }
  }

  // def getByAddress(host: String, addr: Array[Byte], nif: NetworkInterface): Inet6Addres
}
