package java.net

// Ported from Apache Harmony

import scalanative.unsafe._
import scalanative.unsigned._

import scala.scalanative.posix.net.`if`._
import scala.scalanative.posix.stddef

final class Inet6Address private (
    val ipAddress: Array[Byte],
    host: String,
    scopeId: Int,
    val zoneIdent: String
) extends InetAddress(ipAddress, host) {

  def this(ipAddress: Array[Byte], host: String, scope: Int) =
    this(ipAddress, host, scope, "")

  def this(ipAddress: Array[Byte], host: String) =
    this(ipAddress, host, 0)

  def this(ipAddress: Array[Byte]) = this(ipAddress, null)

  def getScopeId(): Int = scopeId

  override def isLinkLocalAddress(): Boolean =
    (ipAddress(0) == -2) && ((ipAddress(1) & 255) >>> 6) == 2

  override def isAnyLocalAddress(): Boolean = ipAddress.forall(_ == 0)

  override def isLoopbackAddress(): Boolean = {
    if (ipAddress(15) != 1)
      return false

    ipAddress.dropRight(1).forall(_ == 0)
  }

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

object Inet6Address {

  def getByAddress(
      host: String,
      addr: Array[Byte],
      scope_id: Int
  ): Inet6Address = {
    if (addr == null || addr.length != 16)
      throw new UnknownHostException("Illegal IPv6 address")

    new Inet6Address(addr, host, Math.max(0, scope_id))
  }

  private[net] def apply(
      ipAddress: Array[Byte],
      host: String,
      scopeId: Int,
      zone: String
  ): Inet6Address = {
    new Inet6Address(ipAddress, host, scopeId, zone)
  }

  private val hexCharacters = "0123456789abcdef"

  private[net] def formatInet6Address(in6Addr: Inet6Address): String = {
    /* ScalaJVM expects the long form of, say "0:0:0:0:0:0:0:1"
     * inet_pton() and getnameinfo() both return the short form "::1".
     *
     * Translate by hand as before but avoid non-local returns.
     */

    val ia6ByteArray = in6Addr.ipAddress

    val buffer = new StringBuilder()
    var isFirst = true
    for (i <- 0 until ia6ByteArray.length) {
      if ((i & 1) == 0)
        isFirst = true

      var j = (ia6ByteArray(i) & 0xf0) >>> 4
      if (j != 0 || !isFirst) {
        buffer.append(hexCharacters.charAt(j))
        isFirst = false
      }
      j = ia6ByteArray(i) & 0x0f
      if (j != 0 || !isFirst) {
        buffer.append(hexCharacters.charAt(j))
        isFirst = false
      }
      if ((i & 1) != 0 && (i + 1) < ia6ByteArray.length) {
        if (isFirst)
          buffer.append('0')
        buffer.append(':')
      }
      if ((i & 1) != 0 && (i + 1) == ia6ByteArray.length && isFirst) {
        buffer.append('0')
      }
    }

    if (!in6Addr.zoneIdent.isEmpty) {
      val zi = in6Addr.zoneIdent
      val suffix =
        try {
          val ifIndex = Integer.parseInt(zi)
          val ifName = stackalloc[Byte](IF_NAMESIZE.toUSize)
          if (if_indextoname(ifIndex.toUInt, ifName) == stddef.NULL) zi
          else fromCString(ifName)
        } catch {
          case e: NumberFormatException => zi
        }

      buffer.append('%').append(suffix)
    }

    buffer.toString
  }

}
