package java.util.random

/* See top of java.util.random.RandomGenerator for Intellectual Property
 * attributions and algorithm hints.
 */

import java.lang as jl

import java.nio.ByteBuffer

import java.util.{Spliterator, Spliterators}
import java.util.Spliterators.AbstractSpliterator

import java.util.function.{Consumer, Supplier}

import java.util.stream.{Stream, StreamSupport}

private final class Xoshiro256PlusPlus private[random] (
    // XBG state, Blacknam & Vigna: "not everwhere zero"
    private var state0: scala.Long,
    private var state1: scala.Long,
    private var state2: scala.Long,
    private var state3: scala.Long
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
    this(buf(0), buf(1), buf(2), buf(3))

  def this(seed: Array[scala.Byte]) =
    this({
      val buf = new Array[Long](4)
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
    this(RandomSupport.getVignaInitialState4x64(seed))

  def this() =
    this(RandomSupport.getEntropy4x64())

  /* This algorithm is directly from 2019 D. Blackman & S. Vigna code.
   *   URL: https://prng.di.unimi.it/xoshiro256plusplus.c
   */

  if ((state0 + state1 + state2 + state3) == 0L)
    // if all zeros Xoshiro256 never gets out of initial state
    throw new IllegalArgumentException(
      "Xoshiro256PlusPlus \"state must be seeded so that" +
        " it is not everywhere zero\""
    )

// https://github.com/vigna/dsiutils/blob/master/src/it/unimi/
//    dsi/util/XoShiRo256PlusPlusRandom.java#L108

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

    val XOSHIRO256_FIRST_CONSTANT = 17
    val XOSHIRO256_SECOND_CONSTANT = 45

    val t0 = state0
    val result = jl.Long.rotateLeft(t0 + state3, 23) + t0

    val t = state1 << XOSHIRO256_FIRST_CONSTANT

    state2 ^= t0
    state3 ^= state1
    state1 ^= state2
    state0 ^= state3

    state2 ^= t

    state3 = jl.Long.rotateLeft(state3, XOSHIRO256_SECOND_CONSTANT)

    result
  }

  // Members declared in RandomGenerator

  override def isDeprecated() =
    Xoshiro256PlusPlusRandomFactory.isDeprecated()

  // Members declared or overridden in RandomGenerator.LeapableGenerator

  def copy(): RandomGenerator.LeapableGenerator =
    new Xoshiro256PlusPlus(state0, state1, state2, state3)

  private def jumpOrLeapImpl(table: Array[Long]): Unit = {
    var s0 = 0L
    var s1 = 0L
    var s2 = 0L
    var s3 = 0L

    for (i <- 0 until table.length) {
      for (b <- 0 until 64) {
        if ((table(i) & (1L << b)) != 0) {
          s0 ^= state0
          s1 ^= state1
          s2 ^= state2
          s3 ^= state3
        }

        nextLong();
      }
    }

    state0 = s0
    state1 = s1
    state2 = s2
    state3 = s3
  }

  def jump(): Unit =
    jumpOrLeapImpl(Xoshiro256PlusPlus.JUMP_TABLE)

  def jumpDistance(): Double =
    3.402823669209385e38 // algorithm magic value from JVM.

  def leap(): Unit =
    jumpOrLeapImpl(Xoshiro256PlusPlus.LEAP_TABLE)

  def leapDistance(): Double =
    6.277101735386681e57 // algorithm magic value from JVM.
}

private object Xoshiro256PlusPlus {

  final val JUMP_TABLE = Array[Long](
    0x180ec6d33cfd0abaL,
    0xd5a61266f0c9392cL,
    0xa9582618e03fc9aaL,
    0x39abdc4529b1661cL
  )

  final val LEAP_TABLE = Array[Long](
    0x76e15d3efefdcbbfL,
    0xc5004e441c522fb3L,
    0x77710069854ee241L,
    0x39109bb02acbe635L
  )
}
