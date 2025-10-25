package org.scalanative.testsuite.javalib.util.random

/* JEP356 - Enhanced Pseudo-Random Number Generators
 *
 * Introduced in Java 17.
 */

/* This file tests only that each JEP 356 method can be called and it
 * executes without throwing an exception. It supplements and does not
 * replace the Java 8 ThreadLocalRandomTest.
 *
 * Statistical properties of output and concurrent use with another Thread
 * or Threads are not checked. Arguments to JEP 356 default methods are
 * extensively checked in other files, not here.
 */

// Credit & thanks to https://www.random.org/ for the arbitrary seeds.

import java.util.concurrent.ThreadLocalRandom
import java.util.{Arrays, Spliterator}
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* "seeds" are arbitrary values, not crafted.
 * Some were taken from SplittablerandomTest.scala and others created ab ovo.
 */

class JEP356_ThreadLocalRandomTestOnJDK17 {

  final val expectedStreamCharacteristics =
    Spliterator.SIZED | Spliterator.IMMUTABLE |
      Spliterator.NONNULL | Spliterator.SUBSIZED //  0x4540, decimal 17728

  @Test def create_Constructor_ZeroArg(): Unit = {
    /* Does the constructor link, execute without Exception, and
     * return a usable method?
     */

    val rng = ThreadLocalRandom.current()

    val upperBoundExclusive = 10

    val result = rng.nextInt(upperBoundExclusive)

    assertTrue(
      s"result ${result} is not in range [0, ${upperBoundExclusive})",
      (result >= 0) && (result < upperBoundExclusive)
    )
  }

  /* Methods specified in trait RandomGenerator
   */

  @Test def doubles(): Unit = {
    val max = 10
    var count = 0

    val rng = ThreadLocalRandom.current()

    val doubles = rng
      .doubles()
      .takeWhile(e => { count += 1; count <= max })
      .forEach(dbl =>
        // Does rng have a usable & believable method?
        assertNotEquals("doubles", jl.Double.NaN, dbl)
      )

    assertEquals("count", max + 1, count)

    val doublesSpliterator = rng
      .doubles()
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      doublesSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      doublesSpliterator.estimateSize()
    )
  }

  @Test def doubles_OriginUntilBound(): Unit = {
    val origin = -2.0
    val bound = Math.E

    val maxCount = 10
    var count = 0

    val rng = ThreadLocalRandom.current()

    val doublesInBound = rng
      .doubles(origin, bound)
      .takeWhile(e => { count += 1; count <= maxCount })
      .forEach(dbl =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${dbl}",
          (dbl >= origin) && (dbl < bound)
        )
      )

    assertEquals("count", maxCount + 1, count)

    val doublesSpliterator = rng
      .doubles(origin, bound)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      doublesSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      doublesSpliterator.estimateSize()
    )
  }

  @Test def doubles_StreamSize(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 0.0
    val bound = 1.0

    val rng = ThreadLocalRandom.current()

    val doubles = rng
      .doubles(streamSize)
      .takeWhile(e => { count += 1; count <= streamSize })
      .forEach(dbl =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${dbl}",
          (dbl >= origin) && (dbl < bound)
        )
      )

    assertEquals("count", streamSize, count)

    val doublesSpliterator = rng
      .doubles(streamSize)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      doublesSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      streamSize,
      doublesSpliterator.estimateSize()
    )
  }

  @Test def doubles_StreamSizeOriginUntilBound(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = -2.0
    val bound = Math.E

    val rng = ThreadLocalRandom.current()

    val doubles = rng
      .doubles(streamSize, origin, bound)
      .takeWhile(e => { count += 1; count <= streamSize })
      .forEach(dbl =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${dbl}",
          (dbl >= origin) && (dbl < bound)
        )
      )

    assertEquals("count", streamSize, count)

    val doublesSpliterator = rng
      .doubles(streamSize, origin, bound)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      doublesSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      streamSize,
      doublesSpliterator.estimateSize()
    )
  }

  @Test def ints(): Unit = {
    val max = 10
    var count = 0

    val rng = ThreadLocalRandom.current()

    val ints = rng
      .ints()
      .takeWhile(e => { count += 1; count <= max })
      .forEach(i => assertTrue("ints", i.isInstanceOf[scala.Int]))

    assertEquals("count", max + 1, count)

    val intsSpliterator = rng
      .ints()
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      intsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      intsSpliterator.estimateSize()
    )
  }

  @Test def ints_OriginUntilBound(): Unit = {
    val origin = -2
    val bound = 4

    val maxCount = 10
    var count = 0

    val rng = ThreadLocalRandom.current()

    val intsInBound = rng
      .ints(origin, bound)
      .takeWhile(e => { count += 1; count <= maxCount })
      .forEach(i =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${i}",
          (i >= origin) && (i < bound)
        )
      )

    assertEquals("count", maxCount + 1, count)

    val intsSpliterator = rng
      .ints(origin, bound)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      intsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      intsSpliterator.estimateSize()
    )
  }

  @Test def ints_StreamSize(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 0
    val bound = 11

    val rng = ThreadLocalRandom.current()

    val ints = rng
      .ints(streamSize)
      .takeWhile(e => { count += 1; count <= streamSize })
      .forEach(i => assertTrue("ints", i.isInstanceOf[scala.Int]))

    assertEquals("count", streamSize, count)

    val intsSpliterator = rng
      .ints(streamSize)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      intsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      streamSize,
      intsSpliterator.estimateSize()
    )
  }

  @Test def ints_StreamSizeOriginUntilBound(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 4
    val bound = 12

    val rng = ThreadLocalRandom.current()

    val ints = rng
      .ints(streamSize, origin, bound)
      .takeWhile(e => { count += 1; count <= streamSize })
      .forEach(i =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${i}",
          (i >= origin) && (i < bound)
        )
      )

    assertEquals("count", streamSize, count)

    val intsSpliterator = rng
      .ints(streamSize, origin, bound)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      intsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      streamSize,
      intsSpliterator.estimateSize()
    )
  }

  @Test def isDeprecated(): Unit = {
    val rng = ThreadLocalRandom.current()

    assertFalse(
      s"ThreadLocalRandom should not be marked deprecated",
      rng.isDeprecated()
    )
  }

  @Test def longs(): Unit = {
    val max = 10
    var count = 0

    val rng = ThreadLocalRandom.current()

    val longs = rng
      .longs()
      .takeWhile(e => { count += 1; count <= max })
      .forEach(lng => assertTrue("ints", lng.isInstanceOf[scala.Long]))

    assertEquals("count", max + 1, count)

    val longsSpliterator = rng
      .longs()
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      longsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      longsSpliterator.estimateSize()
    )
  }

  @Test def longs_OriginUntilBound(): Unit = {
    val origin = -2L
    val bound = 4L

    val maxCount = 10
    var count = 0

    val rng = ThreadLocalRandom.current()

    val longsInBound = rng
      .longs(origin, bound)
      .takeWhile(e => { count += 1; count <= maxCount })
      .forEach(lng =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${lng}",
          (lng >= origin) && (lng < bound)
        )
      )

    assertEquals("count", maxCount + 1, count)

    val longsSpliterator = rng
      .longs(origin, bound)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      longsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      longsSpliterator.estimateSize()
    )
  }

  @Test def longs_StreamSize(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 0L
    val bound = 11L

    val rng = ThreadLocalRandom.current()

    val longs = rng
      .longs(streamSize)
      .takeWhile(e => { count += 1; count <= streamSize })
      .forEach(lng => assertTrue("longs", lng.isInstanceOf[scala.Long]))

    assertEquals("count", streamSize, count)

    val longsSpliterator = rng
      .longs(streamSize)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      longsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      streamSize,
      longsSpliterator.estimateSize()
    )
  }

  @Test def longs_StreamSizeOriginUntilBound(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 4L
    val bound = 12L

    val rng = ThreadLocalRandom.current()

    val longs = rng
      .longs(streamSize, origin, bound)
      .takeWhile(e => { count += 1; count <= streamSize })
      .forEach(lng =>
        assertTrue(
          s"expected a value >= ${origin} && < ${bound} got: ${lng}",
          (lng >= origin) && (lng < bound)
        )
      )

    assertEquals("count", streamSize, count)

    val longsSpliterator = rng
      .longs(streamSize, origin, bound)
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      longsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      streamSize,
      longsSpliterator.estimateSize()
    )
  }

  @Test def nextBoolean(): Unit = {
    val rng = ThreadLocalRandom.current()

    val result = rng.nextBoolean()
    assertTrue("nextBoolean", ((result == false) || (result == true)))
  }

  @Test def nextBytes(): Unit = {
    val rng = ThreadLocalRandom.current()

    val nBytes = 37 // try to trick up algorithm, use odd non-power-of-2.
    val bytes = new Array[Byte](nBytes)

    /* With no control over the seed and only 256 possible output
     * possibilities, test only that SN link finds symbol & executes it
     * without throwing.
     */
    rng.nextBytes(bytes)

    // Nonsense test to get optimizer to believe we are using the value
    assertTrue(s"second value differs", bytes(0) != jl.Integer.MAX_VALUE)
  }

  @Test def nextDouble(): Unit = {
    val rng = ThreadLocalRandom.current()

    val d1: scala.Double = rng.nextDouble()
    val d2: scala.Double = rng.nextDouble()

    // They _could_ match by chance, but that is highly unlikely.
    assertNotEquals("unlikely match", d1, d2, 0.0)
  }

  @Test def nextDouble_Bound(): Unit = {
    val rng = ThreadLocalRandom.current()

    val bound = Math.PI
    val nTries = 30

    for (j <- 1 to nTries) {
      val nd: scala.Double = rng.nextDouble(bound)
      assertTrue(
        s"j: ${j}, expected a value >= 0 && < ${bound} got: ${nd}",
        (nd >= 0) && (nd < bound)
      )
    }
  }

  @Test def nextDouble_OriginUntilBound(): Unit = {
    val rng = ThreadLocalRandom.current()

    val origin = -1.0
    val bound = Math.E
    val nTries = 30

    for (j <- 1 to nTries) {
      val nd: scala.Double = rng.nextDouble(origin, bound)
      assertTrue(
        s"j: ${j}, expected a value >= ${origin} && < ${bound} got: ${nd}",
        (nd >= origin) && (nd < bound)
      )
    }
  }

  @Test def nextExponential(): Unit = {
    val rng = ThreadLocalRandom.current()

    val result = rng.nextExponential()

    /* Check only that the entry point links, executes, and returns an
     * acceptable result.
     */
    assertTrue(
      s"result ${result} must be non-negative",
      result >= 0.0
    )
  }

  @Test def nextFloat(): Unit = {
    val rng = ThreadLocalRandom.current()

    val f1: scala.Float = rng.nextFloat()
    val f2: scala.Float = rng.nextFloat()

    // They _could_ match by chance, but that is highly unlikely.
    assertNotEquals("unlikely match", f1, f2, 0.0)
  }

  @Test def nextFloat_Bound(): Unit = {
    val rng = ThreadLocalRandom.current()

    val bound = Math.PI.toFloat
    val nTries = 30

    for (j <- 1 to nTries) {
      val nf: scala.Float = rng.nextFloat(bound)
      assertTrue(
        s"j: ${j}, expected a value >= 0 && < ${bound} got: ${nf}",
        (nf >= 0) && (nf < bound)
      )
    }
  }

  @Test def nextGaussian_ZeroArgs(): Unit = {
    val rng = ThreadLocalRandom.current()

    val result = rng.nextGaussian()

    /* Check only that the entry point links, executes, and returns an
     * acceptable result.
     */
    assertTrue(
      s"result ${result} is more than 10 standard deviations out",
      Math.abs(result) <= 10.0
    )
  }

  @Test def nextGaussian_MeanStddev(): Unit = {
    val rng = ThreadLocalRandom.current()

    val result = rng.nextGaussian(-1.0, 2.0)

    /* Check only that the entry point links, executes, and returns an
     * acceptable result.
     */
    assertTrue(
      s"result ${result} is more than 10 standard deviations out",
      Math.abs(result) <= 20.0
    )
  }

  @Test def nextFloat_OriginToBound(): Unit = {
    val rng = ThreadLocalRandom.current()

    val origin = -10.0f / 3.0f
    val bound = -Math.E.toFloat
    val nTries = 30

    for (j <- 1 to nTries) {
      val nf: scala.Float = rng.nextFloat(origin, bound)
      assertTrue(
        s"j: ${j}, expected a value >= ${origin} && < ${bound} got: ${nf}",
        (nf >= origin) && (nf < bound)
      )
    }
  }

  @Test def nextInt(): Unit = {
    val rng = ThreadLocalRandom.current()

    val i1: scala.Int = rng.nextInt()
    val i2: scala.Int = rng.nextInt()

    // They _could_ match by chance, but that is highly unlikely.
    assertNotEquals("unlikely match", i1, i2)
  }

  @Test def nextInt_Bound(): Unit = {
    val rng = ThreadLocalRandom.current()

    val bound = 10
    val nTries = 30

    for (j <- 1 to nTries) {
      val ni = rng.nextInt(bound)
      assertTrue(
        s"j: ${j}, expected a value >= 0 && < ${bound} got: ${ni}",
        (ni >= 0) && (ni < bound)
      )
    }
  }

  @Test def nextLong(): Unit = {
    val rng = ThreadLocalRandom.current()

    val L1: scala.Long = rng.nextLong()
    val L2: scala.Long = rng.nextLong()

    // They _could_ match by chance, but that is highly unlikely.
    assertNotEquals("unlikely match", L1, L2)
  }

  @Test def nextLong_Bound(): Unit = {
    val rng = ThreadLocalRandom.current()

    val bound = 10
    val nTries = 30

    for (j <- 1 to nTries) {
      val nl = rng.nextLong(bound)
      assertTrue(
        s"j: ${j}, expected a value >= 0 && < ${bound} got: ${nl}",
        (nl >= 0) && (nl < bound)
      )
    }
  }

}
