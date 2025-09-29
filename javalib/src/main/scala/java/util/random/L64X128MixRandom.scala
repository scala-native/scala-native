package java.util.random

/* See top of java.util.random.RandomGenerator for Intellectual Property
 * attributions and algorithm hints.
 */

import java.nio.ByteBuffer
import java.util.Spliterators.AbstractSpliterator
import java.util.function.{Consumer, Supplier}
import java.util.stream.{Stream, StreamSupport}
import java.util.{Spliterator, Spliterators}
import java.{lang => jl}

private final class L64X128MixRandom private[random] (
    private var a: Long, // Per-instance additive parameter (must be odd)
    private var s: Long, // Per-instance LCG state (must be odd)
    private var x0: Long, // Per-instance XBG state
    private var x1: Long //    (x0 and x1 are never both zero)
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

  final val M = 0xd1342543de82ef95L; // Fixed multiplier

  def this(buf: Array[Long]) =
    this(
      buf(0),
      buf(1),
      buf(2),
      buf(3)
    )

  def this(seed: Array[Byte]) =
    this({
      val buf = RandomSupport.getEntropy4x64()
      if (seed.size >= 8) {
        val seedLong = ByteBuffer.wrap(seed).getLong()
        buf(1) = seedLong
      } // Do not throw in constructor, silently fallback to entropy.
      buf
    })

  def this(seed: Long) =
    this({ val buf = RandomSupport.getEntropy4x64(); buf(1) = seed; buf })

  def this() =
    this(RandomSupport.getEntropy4x64())

  if ((a & 1) == 0) // additive parameter, forced odd
    a |= 1

  if ((s & 1) == 0) // LCG parameter, forced odd
    s |= 1

  if ((x0 == 0L) && (x1 == 0L)) {
    x0 = a // 'a' is known to be odd here, so half of XBG state is now not zero
    x1 = a ^ 0xa5a5a5a5a5a5a5a5L // shake things up a bit
  }

  /* This the algorithm directly from the Steele & Vigna LXM paper, 2021
   * given in Java as Figure 1.
   * It corresponds to the name "L64X128MixRandom" in JVM documentation for
   * "package java.util.random".
   * Keep track of the shells moving around & about.
   */

  def nextLong(): scala.Long = {
    // Combining operation
    var z = s + x0

    // Mixing function (lea64)
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L
    z = (z ^ (z >>> 32))

    // Update the LCG subgenerator
    s = M * s + a

    // Update the XBG subgenerator (xoroshiro128v1_0)
    var q0 = x0
    var q1 = x1

    q1 ^= q0
    q0 = jl.Long.rotateLeft(q0, 24)
    q0 = q0 ^ q1 ^ (q1 << 16)
    q1 = jl.Long.rotateLeft(q1, 37)
    x0 = q0
    x1 = q1

    z
  }

  // Members declared in RandomGenerator

  override def isDeprecated() =
    L64X128MixRandomFactory.isDeprecated()

  // Members declared in java.util.random.RandomGenerator.SplittableGenerator

  override def split(): RandomGenerator.SplittableGenerator = {
    /* Translated to Scala from Java code in paper:
     *   Guy L. Steele Jr. and Sebastiano Vigna. 2021.
     */
    new L64X128MixRandom(
      nextLong(),
      nextLong(),
      nextLong(),
      nextLong()
    )
  }

}
