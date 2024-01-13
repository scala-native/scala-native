package java.net

// Ported from Apache Harmony & extensively re-worked for Scala Native.

import scalanative.unsafe._
import scalanative.unsigned._

import java.{util => ju}

import scala.scalanative.posix.net.`if`._
import scala.scalanative.posix.stddef

final class Inet6Address private (
    ipAddress: Array[Byte],
    host: String,
    useScopeId: Boolean, // true when this created with explicit, valid scopeId
    scopeId: Int,
    nif: NetworkInterface
) extends InetAddress(ipAddress, host) {

  /* Note on scopeId constructor argument:
   *
   *   For most intents and purposes a Java scopeId is a NetworkInterface index
   *   equivalent to a C or IETF (Internet Engineering Task Force) sin6_scope_id.
   *   IETF discussions sometimes call it a 'zoneId'.
   *
   *   See the "Design Note_2" in NetworkInterface.scala for a description of
   *   a case where Java and C practice differ.
   */

  override def equals(that: Any): Boolean = that match {
    case that: Inet6Address => this.hashCode() == that.hashCode()
    case _                  => false
  }

  def getScopedInterface(): NetworkInterface = nif

  def getScopeId(): Int =
    if (useScopeId) scopeId
    else if (nif == null) 0
    else nif.getIndex()

  override def hashCode(): Int = {
    val base = ju.Arrays.hashCode(ipAddress)
      + host.hashCode()
      + useScopeId.hashCode()
      + scopeId.hashCode()

    val hash = if (nif == null) base else (base + nif.hashCode())
    hash
  }

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

  private def formatScopeId(): String = {
    if (nif != null)
      nif.getDisplayName()
    else if (useScopeId) {
      val netIf = NetworkInterface.getByIndex(scopeId)
      if (netIf == null) String.valueOf(scopeId)
      else netIf.getDisplayName()
    } else ""
  }

}

object Inet6Address {

  def getByAddress(
      host: String,
      addr: Array[Byte],
      scopeId: Int
  ): Inet6Address = {
    if (addr == null || addr.length != 16)
      throw new UnknownHostException("Illegal IPv6 address")

    /* JVM treats negative scopeId as having being not supplied.
     * Explicitly specified 0 scopeIds are considered supplied.
     * Elsewhere implicit 0 scopeIds, say from a sin6_scope_id, are not.
     */
    if (scopeId < 0)
      Inet6Address(addr, host)
    else
      new Inet6Address(addr, host, true, scopeId, null)
  }

  def getByAddress(
      host: String,
      addr: Array[Byte],
      nif: NetworkInterface
  ): Inet6Address = {
    if (addr == null || addr.length != 16)
      throw new UnknownHostException("Illegal IPv6 address")

    /* match JVM
     * Do not throw on null nif but fail late with obscure/wrong/unexpected
     * scopeId of 0.
     */

    new Inet6Address(addr, host, false, 0, nif)
  }

  private[net] def apply(
      addr: Array[Byte],
      host: String
  ): Inet6Address = {
    new Inet6Address(addr, host, false, 0, null)
  }

  private final val hexCharacters = "0123456789abcdef"

  private[net] def formatInet6Address(in6Addr: Inet6Address): String = {
    /* ScalaJVM expects the long form of, say "0:0:0:0:0:0:0:1"
     * inet_pton() and getnameinfo() both return the short form "::1".
     *
     * Translate by hand as before but avoid non-local returns.
     */

    val ia6ByteArray = in6Addr.getAddress()

    /* The magic number 64 is used to construct the StringBuffer with a large
     * enough size that it should not have pay the cost of expanding.
     * The largest IPv6 address, proper, is 39 ((sizeof("fe80:") * 7) plus 4).
     * Memory blocks tend to be allocated in powers of two.
     * Round up to the next higher power of two which will also cover a
     * possible interface identifier ("%bridge0").
     * The math need not be exact, the buffer will grow if we guess wrong.
     */

    val buffer = new StringBuilder(64)
    var isFirst = true

    // IPv6 binary addresses are defined as 16 bytes, so loop count is known.
    for (i <- 0 until 16) {
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

    val suffix = in6Addr.formatScopeId()

    if (!suffix.isEmpty())
      buffer.append('%').append(suffix)

    buffer.toString
  }

}
