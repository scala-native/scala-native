package java.util.random

/* See top of java.util.random.RandomGenerator for Intellectual Property
 * attributions and algorithm hints.
 */

import java.nio.ByteBuffer
import java.util.Spliterators.AbstractSpliterator
import java.util.function.{Consumer, Supplier}
import java.util.stream.{Stream, StreamSupport}
import java.util.{Spliterator, Spliterators, stream => jus}
import java.{lang => jl}

private final class L32X64MixRandom private[random] (
    private var a: Int, // Per-instance additive parameter (must be odd)
    private var s: Int, // Per-instance LCG state (must be odd)
    private var x0: Int, // Per-instance XBG state
    private var x1: Int //    (x0 and x1 are never both zero)
) extends AbstractSplittableRandomGenerator {

  /* By contract, callers have validated array sizes for constructors.
   * This requirement avoids the nastiness resulting from throwing an
   * Exception in a constructor.
   *
   * Method is 'private' and all callers are in java.util.random.
   * Bad constructor callers can only come from Scala Native itself.
   *
   * Even then, to ensure algorithm parameter requirements are met, the primary
   * constructor forces a & s to be odd and x0 & x1 to be "both not zero"
   */

  final val M = 0xadb4a92d; // Fixed multiplier

  def this(buf: Array[Int]) =
    this(
      buf(0), // additive parameter
      buf(1), // LCG initial state
      buf(2), // XBG initial state
      buf(3)
    )

  def this(seed: Array[Byte]) =
    this({
      val rngArgs = new Array[Int](4)
      val bbSeed = ByteBuffer.wrap(seed)
      rngArgs(0) = bbSeed.getInt()
      rngArgs(1) = bbSeed.getInt()
      rngArgs(2) = bbSeed.getInt()
      rngArgs(3) = bbSeed.getInt()

      rngArgs
    })

  def this(seed: Long) =
    this({
      val buf = RandomSupport.getEntropy4x32()
      buf(1) = seed.toInt
      buf
    })

  def this() =
    this(RandomSupport.getEntropy4x32())

  if ((a & 1) == 0) // additive parameter, forced odd
    a |= 1

  if ((s & 1) == 0) // LCG parameter, forced odd
    s |= 1

  if ((x0 == 0L) && (x1 == 0L)) {
    x0 = a // 'a' is known to be odd here, so half of XBG state is now not zero
    x1 = a ^ 0x5a5a5a5a // shake things up a bit
  }

  // JVM fills upper 32 bits, not a nextInt().toLong which clears them.
  def nextLong(): scala.Long =
    (nextInt().toLong << 32) | nextInt().toLong

  /* Since most callers will want 32 bit results, implement nextLong()
   * in terms of nextInt(). The other way around is the usual practice
   * in other LXM algorithms. Try to eke out a few CPU cycles with
   * a guess about eventual usage patterns.
   */

  override def nextInt(): scala.Int = {

    // Combining operation
    var z = s.toLong + x0

    // Mixing constants from G. Steele & S. Vigna LXM paper, 2021 page 148:6
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L
    z = z >>> 32
    val result = z.toInt

    // Update the LCG subgenerator
    s = M * s + a

    // Vigna xoroshiro** 1.0
    // https://prng.di.unimi.it/xoroshiro64starstar.c

    // Update the XBG subgenerator (xoroshiro64v1_0)
    var q0: scala.Int = x0
    var q1: scala.Int = x1

    q1 ^= q0
    q0 = jl.Integer.rotateLeft(q0, 26) ^ q1 ^ (q1 << 9)
    q0 = q0 ^ q1 ^ (q1 << 9)
    q1 = jl.Integer.rotateLeft(q1, 13)
    x0 = q0
    x1 = q1

    result
  }

  // Members declared in RandomGenerator

  override def isDeprecated() =
    L32X64MixRandomFactory.isDeprecated()

  // Members declared in java.util.random.RandomGenerator.SplittableGenerator

  override def split(): RandomGenerator.SplittableGenerator = {
    new L32X64MixRandom(
      nextInt(),
      nextInt(),
      nextInt(),
      nextInt()
    )
  }

}
