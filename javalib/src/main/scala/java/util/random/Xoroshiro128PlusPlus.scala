package java.util.random

/* See top of java.util.random.RandomGenerator for Intellectual Property
 * attributions and algorithm hints.
 */

import java.{lang => jl}

import java.nio.ByteBuffer

import java.util.{Spliterator, Spliterators}
import java.util.Spliterators.AbstractSpliterator

import java.util.function.{Consumer, Supplier}

import java.util.stream.{Stream, StreamSupport}

private final class Xoroshiro128PlusPlus private[random] (
    // XBG state, Blacknam & Vigna: "not everwhere zero"
    private var state0: scala.Long,
    private var state1: scala.Long
) extends AbstractJumpableRandomGenerator
    with RandomGenerator.LeapableGenerator {

  /* By contract, callers have validated array sizes for constructors.
   * This requirement avoids the nastiness resulting from throwing an
   * Exception in a constructor.
   *
   * Method is 'private' and all callers are in java.util.random.
   * Bad constructor callers can only come from Scala Native itself.
   */

  def this(buf: Array[Long]) =
    this(buf(0), buf(1))

  def this(seed: Array[scala.Byte]) =
    this({
      val buf = new Array[Long](2)
      val bb = ByteBuffer.wrap(seed)
      for (j <- 0 until buf.length)
        buf(j) = bb.getLong()
      buf
    })

  /* For Xoroshiro/Xoshiro group (XBG) generators, two instances created
   * with same seed, in same or separate runs, should return the same
   * sequence of values under the same sequence of operations.
   */

  def this(seed: Long) =
    this(RandomSupport.getVignaInitialState2x64(seed))

  def this() =
    this(RandomSupport.getEntropy2x64())

  /* This algorithm is directly from S. Vigna DSI Utilities code.
   *   URL: https://github.com/vigna/dsiutils/blob/master/src/it/unimi/dsi
   *        /util/XoRoShiRo128PlusPlusRandomGenerator.java
   */

  if ((state0 + state1) == 0L)
    // if all zeros Xoroshiro128 never gets out of initial state
    throw new IllegalArgumentException(
      "Xoshiro128PlusPlus \"state must be seeded so that" +
        " it is not everywhere zero\""
    )

  def nextLong(): scala.Long = {
    /* The Blackman & Vigna Java code uses two constants, 17 & 45.
     * These are the same as described in a table in the Java 22
     * "package java.util.random" documentation.
     *
     * They are given apparently unnecessary names here to help future
     * maintainers locate them in case JVM changes. They are obvious
     * if you know where they are and hard to find if you have to trace
     * from zero knowledge of the algorithm.
     */

    val XOROSHIRO128_FIRST_CONSTANT = 17
    val XOROSHIRO128_SECOND_CONSTANT = 49
    val XOROSHIRO128_THIRD_CONSTANT = 28

    val s0 = state0
    var s1 = state1

    val result = jl.Long.rotateLeft(s0 + s1, 17) + s0

    s1 ^= s0
    state0 = jl.Long.rotateLeft(s0, 49) ^ s1 ^ (s1 << 21)
    state1 = jl.Long.rotateLeft(s1, 28)

    result
  }

  // Members declared in RandomGenerator

  override def isDeprecated() =
    Xoroshiro128PlusPlusRandomFactory.isDeprecated()

  // Members declared or overridden in RandomGenerator.LeapableGenerator

  def copy(): RandomGenerator.LeapableGenerator =
    new Xoroshiro128PlusPlus(state0, state1)

  private def jumpOrLeapImpl(table: Array[Long]): Unit = {
    var s0 = 0L
    var s1 = 0L

    for (i <- 0 until table.length) {
      for (b <- 0 until 64) {
        if ((table(i) & (1L << b)) != 0) {
          s0 ^= state0
          s1 ^= state1
        }

        nextLong();
      }
    }

    state0 = s0
    state1 = s1
  }

  def jump(): Unit =
    jumpOrLeapImpl(Xoroshiro128PlusPlus.JUMP_TABLE)

  def jumpDistance(): Double =
    1.8446744073709552e19 // algorithm magic value from JVM.

  def leap(): Unit =
    jumpOrLeapImpl(Xoroshiro128PlusPlus.LEAP_TABLE)

  def leapDistance(): Double =
    7.922816251426434e28 // algorithm magic value from JVM.
}

private object Xoroshiro128PlusPlus {

  final val JUMP_TABLE = Array[Long](
    0x2bd7a6a6e99c2ddcL,
    0x0992ccaf6a6fca05L
  )

  final val LEAP_TABLE = Array[Long](
    0x360fd5f2cf8d5d99L,
    0x9c6e6877736c46e3L
  )
}
