package org.scalanative.testsuite.javalib.util

import java.util.random.RandomGenerator
import java.util.{Random => JuRandom}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class RandomTestOnJDK19 {

  @Test def fromRejectsNullGenerator(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      JuRandom.from(null.asInstanceOf[RandomGenerator])
    )
  }

  @Test def fromReturnsRandomGeneratorUnchanged(): Unit = {
    val random = new JuRandom(123456789L)

    assertSame(random, JuRandom.from(random))
  }

  @Test def fromDelegatesGeneratorMethods(): Unit = {
    val generator = new TrackingGenerator
    val random = JuRandom.from(generator)

    assertEquals(0x13579bdf, random.nextInt())
    assertEquals(17, random.nextInt(23))
    assertEquals(7, random.nextInt(5, 11))

    assertEquals(0x123456789abcdef0L, random.nextLong())
    assertEquals(37L, random.nextLong(41L))
    assertEquals(103L, random.nextLong(101L, 109L))

    assertTrue(random.nextBoolean())
    assertEquals(0.25f, random.nextFloat(), 0.0f)
    assertEquals(3.5f, random.nextFloat(7.0f), 0.0f)
    assertEquals(2.5f, random.nextFloat(2.0f, 3.0f), 0.0f)

    assertEquals(0.5, random.nextDouble(), 0.0)
    assertEquals(4.5, random.nextDouble(9.0), 0.0)
    assertEquals(12.25, random.nextDouble(12.0, 13.0), 0.0)

    assertEquals(-0.75, random.nextGaussian(), 0.0)
    assertEquals(42.0, random.nextGaussian(40.0, 2.0), 0.0)
    assertEquals(0.125, random.nextExponential(), 0.0)

    val bytes = new Array[Byte](4)
    random.nextBytes(bytes)
    assertArrayEquals(Array[Byte](1, 2, 3, 4), bytes)
  }

  @Test def fromDelegatingRandomDoesNotSupportSetSeed(): Unit = {
    val random = JuRandom.from(new TrackingGenerator)

    assertThrows(classOf[UnsupportedOperationException], random.setSeed(1L))
  }

  private final class TrackingGenerator extends RandomGenerator {
    override def nextBoolean(): Boolean = true

    override def nextBytes(bytes: Array[Byte]): Unit = {
      var i = 0
      while (i < bytes.length) {
        bytes(i) = (i + 1).toByte
        i += 1
      }
    }

    override def nextDouble(): Double = 0.5

    override def nextDouble(bound: Double): Double = bound / 2.0

    override def nextDouble(origin: Double, bound: Double): Double =
      origin + ((bound - origin) / 4.0)

    override def nextExponential(): Double = 0.125

    override def nextFloat(): Float = 0.25f

    override def nextFloat(bound: Float): Float = bound / 2.0f

    override def nextFloat(origin: Float, bound: Float): Float =
      origin + ((bound - origin) / 2.0f)

    override def nextGaussian(): Double = -0.75

    override def nextGaussian(mean: Double, stddev: Double): Double =
      mean + stddev

    override def nextInt(): Int = 0x13579bdf

    override def nextInt(bound: Int): Int = bound - 6

    override def nextInt(origin: Int, bound: Int): Int = origin + 2

    override def nextLong(): Long = 0x123456789abcdef0L

    override def nextLong(bound: Long): Long = bound - 4L

    override def nextLong(origin: Long, bound: Long): Long = origin + 2L

    override def isDeprecated(): Boolean = false
  }
}
