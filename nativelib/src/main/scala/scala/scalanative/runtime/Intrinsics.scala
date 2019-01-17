package scala.scalanative
package runtime

object Intrinsics {

  /** Intrinsified unsigned devision on ints. */
  def divUInt(l: Int, r: Int): Int = intrinsic

  /** Intrinsified unsigned devision on longs. */
  def divULong(l: Long, r: Long): Long = intrinsic

  /** Intrinsified unsigned remainder on ints. */
  def remUInt(l: Int, r: Int): Int = intrinsic

  /** Intrinsified unsigned remainder on longs. */
  def remULong(l: Long, r: Long): Long = intrinsic

  /** Intrinsified byte to unsigned int converstion. */
  def byteToUInt(b: Byte): Int = intrinsic

  /** Intrinsified byte to unsigned long conversion. */
  def byteToULong(b: Byte): Long = intrinsic

  /** Intrinsified short to unsigned int conversion. */
  def shortToUInt(v: Short): Int = intrinsic

  /** Intrinsified short to unsigned long conversion. */
  def shortToULong(v: Short): Long = intrinsic

  /** Intrinsified int to unsigned long conversion. */
  def intToULong(v: Int): Long = intrinsic
}
