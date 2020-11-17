package java.net

// Ported from Apache Harmony
final class Inet4Address private[net] (ipAddress: Array[Byte], host: String)
    extends InetAddress(ipAddress, host) {

  private[net] def this(ipAddress: Array[Byte]) = this(ipAddress, null)

  override def isMulticastAddress(): Boolean =
    (ipAddress(0) & 0xF0) == 0xE0

  override def isAnyLocalAddress(): Boolean =
    ipAddress.forall(_ == 0)

  override def isLoopbackAddress(): Boolean =
    (ipAddress(0) & 255) == 127

  override def isLinkLocalAddress(): Boolean =
    ((ipAddress(0) & 255) == 169) && ((ipAddress(1) & 255) == 254)

  override def isSiteLocalAddress(): Boolean = {
    ((ipAddress(0) & 255) == 10) || ((ipAddress(0) & 255) == 172) &&
    (((ipAddress(1) & 255) > 15) && (ipAddress(1) & 255) < 32) ||
    ((ipAddress(0) & 255) == 192) && ((ipAddress(1) & 255) == 168)
  }

  override def isMCGlobal(): Boolean = {
    if (!isMulticastAddress()) return false

    val address = InetAddress.bytesToInt(ipAddress, 0)

    if (address >>> 8 < 0xE00001) return false

    if (address >>> 24 > 0xEE) return false

    true
  }

  override def isMCNodeLocal(): Boolean = false

  override def isMCLinkLocal(): Boolean =
    InetAddress.bytesToInt(ipAddress, 0) >>> 8 == 0xE00000

  override def isMCSiteLocal(): Boolean =
    (InetAddress.bytesToInt(ipAddress, 0) >>> 16) == 0xEFFF

  override def isMCOrgLocal(): Boolean = {
    val prefix = InetAddress.bytesToInt(ipAddress, 0) >>> 16
    prefix >= 0xEFC0 && prefix <= 0xEFC3
  }

}

object Inet4Address extends InetAddressBase {}
