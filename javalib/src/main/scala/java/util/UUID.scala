// Ported from Scala.js commit: dbca410 dated: 2026-01-17

/* That Scala.js commit contains the Scala.js change which motivated
 * re-porting:
 *   "Use two fields of type Long in ju.UUID."
 *   commit: 8d6664a dated: 2025-12-15
 *
 * 2026-07-28
 *  - Implement Java 26 UUID#ofEpochMillis which implements
 *    IETF RFC 9562 UUID version 7.
 */

package java.util

import java.security.SecureRandom
import java.{lang => jl}

final class UUID(private val mostSigBits: Long, private val leastSigBits: Long)
    extends AnyRef
    with java.io.Serializable
    with Comparable[UUID] {

  import UUID._

  /* Most significant long:
   *
   *  0xFFFFFFFF00000000 time_low
   *  0x00000000FFFF0000 time_mid
   *  0x000000000000F000 version
   *  0x0000000000000FFF time_hi
   *
   * Least significant long:
   *
   *  0xC000000000000000 variant
   *  0x3FFF000000000000 clock_seq
   *  0x0000FFFFFFFFFFFF node
   */

  @inline
  def getLeastSignificantBits(): Long =
    leastSigBits

  @inline
  def getMostSignificantBits(): Long =
    mostSigBits

  def version(): Int =
    (mostSigBits.toInt & 0xf000) >> 12

  def variant(): Int = {
    val i3 = (leastSigBits >>> 32).toInt // 3rd most significant Int
    if ((i3 & 0x80000000) == 0) {
      // MSB0 not set: NCS backwards compatibility variant
      0
    } else if ((i3 & 0x40000000) != 0) {
      // MSB1 set: either MS reserved or future reserved
      (i3 & 0xe0000000) >>> 29
    } else {
      // MSB1 not set: RFC 4122 variant
      2
    }
  }

  def timestamp(): Long = {
    if (version() != TimeBased)
      throw new UnsupportedOperationException("Not a time-based UUID")
    val mostSigBits = this.mostSigBits // local copy
    val lo = mostSigBits.toInt
    val resHi = (lo >>> 16) | ((lo & 0x0fff) << 16)
    (resHi.toLong << 32) | (mostSigBits >>> 32)
  }

  def clockSequence(): Int = {
    if (version() != TimeBased)
      throw new UnsupportedOperationException("Not a time-based UUID")
    (leastSigBits >>> 48).toInt & 0x3fff
  }

  def node(): Long = {
    if (version() != TimeBased)
      throw new UnsupportedOperationException("Not a time-based UUID")
    leastSigBits & 0x0000ffffffffffffL
  }

  override def toString(): String = {
    @inline def paddedHex8(x: Long, offset: Int): String = {
      val s = Integer.toHexString((x >>> offset).toInt)
      "00000000".substring(s.length) + s
    }

    @inline def paddedHex4(x: Long, offset: Int): String = {
      val s = Integer.toHexString((x >>> offset).toInt & 0xffff)
      "0000".substring(s.length) + s
    }

    // local copies
    val mostSigBits = this.mostSigBits
    val leastSigBits = this.leastSigBits

    paddedHex8(mostSigBits, 32) + "-" + paddedHex4(
      mostSigBits,
      16
    ) + "-" + paddedHex4(mostSigBits, 0) + "-" +
      paddedHex4(leastSigBits, 48) + "-" + paddedHex4(
        leastSigBits,
        32
      ) + paddedHex8(leastSigBits, 0)
  }

  override def hashCode(): Int =
    java.lang.Long.hashCode(mostSigBits) ^ java.lang.Long.hashCode(leastSigBits)

  override def equals(that: Any): Boolean = that match {
    case that: UUID =>
      this.mostSigBits == that.getMostSignificantBits() &&
        this.leastSigBits == that.getLeastSignificantBits()
    case _ =>
      false
  }

  def compareTo(that: UUID): Int = {
    // See #4882 and the test `UUIDTest.compareTo()` for context
    val thisHi = this.getMostSignificantBits()
    val thatHi = that.getMostSignificantBits()
    if (thisHi != thatHi) {
      if (thisHi < thatHi) -1
      else 1
    } else {
      val thisLo = this.getLeastSignificantBits()
      val thatLo = that.getLeastSignificantBits()
      if (thisLo != thatLo) {
        if (thisLo < thatLo) -1
        else 1
      } else {
        0
      }
    }
  }
}

object UUID {
  private final val TimeBased = 1
  private final val DCESecurity = 2
  private final val NameBased = 3
  private final val Random = 4
  private final val UnixEpochTimeBased = 7

  // Typed as `Random` so that the IR typechecks when SecureRandom is not available
  private lazy val csprng: Random = new java.security.SecureRandom()
  private lazy val randomUUIDBuffer: Array[Byte] = new Array[Byte](16)

  def randomUUID(): UUID = {
    val buffer = randomUUIDBuffer // local copy

    /* We use nextBytes() because that is the primitive of most secure RNGs,
     * and therefore it allows to perform a unique call to the underlying
     * secure RNG.
     */
    csprng.nextBytes(randomUUIDBuffer)

    @inline def longFromBuffer(i: Int): Long = {
      @inline def b(j: Int): Long = (buffer(i + j).toLong & 0xffL) << (8 * j)
      b(0) | b(1) | b(2) | b(3) | b(4) | b(5) | b(6) | b(7)
    }

    val mostSigBits =
      (longFromBuffer(0) & ~0x000000000000f000L) | 0x0000000000004000L
    val leastSigBits =
      (longFromBuffer(8) & ~0xc000000000000000L) | 0x8000000000000000L
    new UUID(mostSigBits, leastSigBits)
  }

  // Not implemented (requires messing with MD5 or SHA-1):
  // def nameUUIDFromBytes(name: Array[Byte]): UUID = ???

  def fromString(name: String): UUID = {
    import Integer.parseInt

    def fail(): Nothing =
      throw new IllegalArgumentException("Invalid UUID string: " + name)

    @inline def parseHex8(his: String, los: String): Int =
      (parseInt(his, 16) << 16) | parseInt(los, 16)

    if (name.length != 36 || name.charAt(8) != '-' ||
        name.charAt(13) != '-' || name
          .charAt(18) != '-' || name.charAt(23) != '-') {
      fail()
    }

    try {
      val i1 = parseHex8(name.substring(0, 4), name.substring(4, 8))
      val i2 = parseHex8(name.substring(9, 13), name.substring(14, 18))
      val i3 = parseHex8(name.substring(19, 23), name.substring(24, 28))
      val i4 = parseHex8(name.substring(28, 32), name.substring(32, 36))
      val mostSigBits = (i1.toLong << 32) | Integer.toUnsignedLong(i2)
      val leastSigBits = (i3.toLong << 32) | Integer.toUnsignedLong(i4)
      new UUID(mostSigBits, leastSigBits)
    } catch {
      case _: NumberFormatException => fail()
    }
  }

// Scala Native additions -----------------------------------------------

  /** @since 26 */
  def ofEpochMillis(timestamp: scala.Long): UUID = {
    /* Return an IETF RFC 9562 version 7 UUID as described
     * by https://www.rfc-editor.org/rfc/rfc9562.html and
     * the JDK 26 documentation for this method.
     */

    if ((timestamp < 0) ||
        (timestamp > ((1L << 48) - 1)))
      // JVM 26 message has typo of no blank before "does"; corrected it here.
      throw new IllegalArgumentException(
        s"Supplied timestamp: ${timestamp} does not fit within 48 bits"
      )

    /* On the magic number 10:
     *
     *  First assume that it is more efficient to ask the underlying
     *  RNG for only the number of bytes used, even if that is not
     *  a power of two.
     *
     *  The RFC describe using 74 bytes, which is not an even power of
     *  8 bits to the byte.
     *
     *    * One of the random fields is 12 bits. It is convenient
     *      for internal manipulation to round this up to 2 full byte2.
     *
     *    * One of the random fields is 62 bits. It is convenient
     *      for internal manipulation to round this up to 8 full bytes.

     *  This gives the math: 10 = (74 + (12 + 4) + (62 + 2)) / 8
     */

    val buffer = new Array[Byte](10)

    csprng.nextBytes(buffer)

    @inline def twelveBitsOfRandomnessAsMask(): scala.Long = {
      /* Return a bitmask containing twelve bits of randomness
       * as the least significant bits of an otherwise zero filled Long.
       *
       * With a sufficient rng, it should not matter which 12 are used
       * as long as they are used only at most once.
       *
       * It is convenient for development and developing to have the
       * first byte of the buffer be the higher of the two low bytes.
       * This aids matching the position immediately to the right of the
       * UUID version (7) when examining .toString() output. In turn,
       * this aids ensuring that any given byte is once.
       */

      @inline def b(i: Int): scala.Long =
        buffer(i).toLong & 0xffL

      ((b(0) << 8) | b(1)) >> 4
    }

    /* Re-using the Scala.js original internal method is useful if one
     * stays aware of the byte order. For its reasons, Scala.js does
     * not consider the first (lowest indexed) byte in the buffer to
     * be RFC network order most significant byte first (MSB), but
     * Java least significant byte first (LSB).
     */
    @inline def longFromBuffer(i: Int): scala.Long = {
      @inline def b(j: Int): scala.Long =
        (buffer(i + j).toLong & 0xffL) << (8 * j)
      b(0) | b(1) | b(2) | b(3) | b(4) | b(5) | b(6) | b(7)
    }

    val mostSigBits = (
      (timestamp << 16) |
        0x0000000000007000L | // UUID version 7
        twelveBitsOfRandomnessAsMask()
    )

    // Recall: IETF RFC 9562/4122 variant here is always 0x0b10
    val leastSigBits =
      (longFromBuffer(2) & 0x0fffffffffffffffL) |
        0x8000000000000000L // UUID variant

    new UUID(mostSigBits, leastSigBits)
  }
}
