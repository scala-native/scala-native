package org.scalanative.testsuite.javalib.util.random

/* JEP356 - Enhanced Pseudo-Random Number Generators
 *
 * Introduced in Java 17.
 */

/* "seeds" are arbitrary values, not crafted.
 * Some were taken from SplittableRandomTest.scala and others created ab ovo.
 * Credit & thanks to https://www.random.org/ for the latter.
 */

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{lang => jl}

import java.util.{Arrays, Spliterator}

import java.util.random.{RandomGenerator, RandomGeneratorFactory}

class JEP356_Xoshiro256PlusPlusRandomTestOnJDK17 {
  final val algorithmName = "Xoshiro256PlusPlus"

  final val factory = RandomGeneratorFactory.of[RandomGenerator](algorithmName)

  /* Recall well, JVM (23) clears all characteristics for Jumpable classes
   * when a streamSize is given to methods returning a Stream.
   */
  final val expectedStreamCharacteristicsZero = 0

  // doubles(), etc. when a streamSize is not given.
  final val expectedStreamCharacteristicsImmutable =
    Spliterator.IMMUTABLE //  0x400, decimal 1024

  final val epsilon = 0.0 // tolerance for Floating point comparisons.

  @Test def create_Constructor_ZeroArg(): Unit = {
    /* Does the constructor link, execute without Exception, and
     * return a useable method?
     */

    val rng = factory.create()

    val upperBoundExclusive = 10

    val result = rng.nextInt(upperBoundExclusive)

    assertTrue(
      s"result ${result} is not in range [0, ${upperBoundExclusive})",
      (result >= 0) && (result < upperBoundExclusive)
    )
  }

  @Test def create_Constructor_ByteSeed(): Unit = {
    val seedSize = 4 * 8 // 1 * sizeof[Long]
    val byteSeed = new Array[Byte](seedSize)

    for (j <- 0 until seedSize)
      byteSeed(j) = j.toByte // Silly for real PRNG seed, but useful here.

    val rng = factory.create(byteSeed)

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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsImmutable,
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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsImmutable,
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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsZero,
      doublesSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      doublesSpliterator.estimateSize()
    )
  }

  @Test def doubles_StreamSizeOriginUntilBound(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = -2.0
    val bound = Math.E

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsZero,
      doublesSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      doublesSpliterator.estimateSize()
    )
  }

  @Test def ints(): Unit = {
    val max = 10
    var count = 0

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsImmutable,
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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsImmutable,
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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsZero,
      intsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      intsSpliterator.estimateSize()
    )
  }

  @Test def ints_StreamSizeOriginUntilBound(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 4
    val bound = 12

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsZero,
      intsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      intsSpliterator.estimateSize()
    )
  }

  @Test def isDeprecated(): Unit = {
    val rng = RandomGenerator.of(algorithmName)
    assertFalse("algorithm should not be marked deprecated", rng.isDeprecated())
  }

  @Test def longs(): Unit = {
    val max = 10
    var count = 0

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsImmutable,
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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsImmutable,
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

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsZero,
      longsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      longsSpliterator.estimateSize()
    )
  }

  @Test def longs_StreamSizeOriginUntilBound(): Unit = {
    val streamSize = 10
    var count = 0

    val origin = 4L
    val bound = 12L

    val rng = RandomGenerator.of(algorithmName)

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
      expectedStreamCharacteristicsZero,
      longsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      longsSpliterator.estimateSize()
    )
  }

  @Test def nextBoolean(): Unit = {
    val rng = factory.create(4782934L)

    /* Cannot check against JVM because result sequence is not specified
     * to be the same. Check if the method throws.
     * Use "result"" to lessen the chances of the nextBoolean() call itself
     * being optimized away.
     */
    val result = rng.nextBoolean()
    assertTrue("nextBoolean", ((result == false) || (result == true)))
  }

  @Test def nextBytes(): Unit = {
    val rng = factory.create(115979080L)

    val nBytes = 107 // try to trick up algorithm, use odd non-power-of-2.
    val bytes = new Array[Byte](nBytes)

    /* average appears to be stable across repeated calls on different
     * JVM instance. The SN implementation returns different values across
     * invocations-from-scratch. It is not required to follow JVM
     * undocumented behavior.
     */
    rng.nextBytes(bytes)

    var sum = 0
    for (j <- 0 until nBytes)
      sum += Math.abs(bytes(j))

    assertTrue(s"sum", sum != 0) // coherence test, == 0 highly unlikely
  }

  @Test def nextDouble(): Unit = {
    val rng1 = factory.create(-45L)

    val r1d1: scala.Double = rng1.nextDouble()
    val r1d2: scala.Double = rng1.nextDouble()

    assertNotEquals("rng1", r1d1, r1d2, 0.0)

    val rng2 = factory.create(45L)

    val r2d1: scala.Double = rng1.nextDouble()
    val r2d2: scala.Double = rng1.nextDouble()

    assertNotEquals("rng2", r2d1, r2d2, 0.0)

    assertNotEquals("cross-rng d1", r1d1, r2d1, 0.0)
    assertNotEquals("cross-rng d2", r1d2, r2d2, 0.0)
  }

  @Test def nextDouble_Bound(): Unit = {
    val rng = factory.create(-129358683L)

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
    val rng = factory.create(889940749L)

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
    val rng = factory.create(-210364010L)

    val result = rng.nextExponential()

    assertTrue(
      s"result ${result} must be non-negative",
      result >= 0.0
    )
  }

  @Test def nextFloat(): Unit = {
    val rng1 = factory.create(-60318L)

    val r1f1: scala.Float = rng1.nextFloat()
    val r1f2: scala.Float = rng1.nextFloat()

    assertNotEquals("rng1", r1f1, r1f2, 0.0f)

    val rng2 = factory.create(60318L)

    val r2f1: scala.Float = rng2.nextFloat()
    val r2f2: scala.Float = rng2.nextFloat()

    assertNotEquals("rng2", r2f1, r2f2, 0.0f)

    assertNotEquals("cross-rng f1", r1f1, r2f1, 0.0f)
    assertNotEquals("cross-rng f2", r1f2, r2f2, 0.0f)
  }

  @Test def nextFloat_Bound(): Unit = {
    val rng = factory.create(533632458L)

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
    val rng = factory.create(-942141823L)
    val bound = 10.0

    val result = rng.nextGaussian()

    assertTrue(
      s"result ${result} is more than ${bound} standard deviations out",
      Math.abs(result) <= bound
    )
  }

  @Test def nextGaussian_MeanStddev(): Unit = {
    val rng = factory.create(-942141823L)
    val bound = 20.0

    val result = rng.nextGaussian(-1.0, 2.0)

    assertTrue(
      s"result ${result} is more than 10 standard deviations out",
      Math.abs(result) <= 20.0
    )
  }

  @Test def nextFloat_OriginToBound(): Unit = {
    val rng = factory.create(706770784L)

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
    val rng1 = factory.create(-84638L)

    val r1i1: scala.Int = rng1.nextInt()
    val r1i2: scala.Int = rng1.nextInt()

    assertNotEquals("rng1", r1i1, r1i2)

    val rng2 = factory.create(84638L)

    val r2i1: scala.Int = rng2.nextInt()
    val r2i2: scala.Int = rng2.nextInt()

    assertNotEquals("rng2", r2i1, r2i2)

    assertNotEquals("cross-rng i1", r1i1, r2i1)
    assertNotEquals("cross-rng i2", r1i2, r2i2)
  }

  @Test def nextInt_Bound(): Unit = {
    val rng = factory.create(-21713650L)

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
    val rng1 = factory.create(205620432625028L)

    val r1L1: scala.Int = rng1.nextInt()
    val r1L2: scala.Int = rng1.nextInt()

    assertNotEquals("rng1", r1L1, r1L2)

    val rng2 = factory.create(-205620432625028L)

    val r2L1: scala.Long = rng2.nextLong()
    val r2L2: scala.Long = rng2.nextLong()

    assertNotEquals("rng2", r2L1, r2L2)

    assertNotEquals("cross-rng L1", r1L1, r2L1)
    assertNotEquals("cross-rng L2", r1L2, r2L2)
  }

  @Test def nextLong_RepeatedCallsDiffer(): Unit = {
    val rng = factory.create(304449391L)

    /* Check for the obvious case of a development stub returning a fixed value
     * or short cycle of fixed values.
     *
     *  Test only nextLong() since it is the abstract method which gets
     *  implemented in each RNG and upon which everything else is built.
     */

    // If nextLong() repeats within this period, something is _way_ broken.
    val nTries = 30

    val firstLong = rng.nextLong()

    for (j <- 1 to nTries) {
      val nl = rng.nextLong()
      assertTrue(
        s"j: ${j}, nextLong() returned repeat value: ${nl}",
        nl != firstLong
      )
    }
  }

  @Test def nextLong_Bound(): Unit = {
    val rng = factory.create(481582459L)

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

  @Test def of_RandomGenerator(): Unit = {
    val rng = RandomGenerator.of(algorithmName)

    // rng is not _required_ to be, but likely is, a LeapableGenerator
    assertTrue(rng.isInstanceOf[RandomGenerator])

    // Does rng have a usable & believable method?
    assertNotEquals("rng.nextLong", -3, rng.nextLong(10))
  }

  /* Methods specified in trait RandomGenerator.LeapableGenerator or
   * specified in RandomGenerator.StreamableGenerator or
   * RandomGenerator.JumpableGenerator and overridden in this class.
   */

  @Test def of_RandomGeneratorLeapable(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)

    assertTrue(rng.isInstanceOf[RandomGenerator])
    assertTrue(rng.isInstanceOf[RandomGenerator.LeapableGenerator])

    // Does rng have a usable & believable method?
    assertNotEquals("rng.nextLong", -3, rng.nextLong(10))
  }

  @Test def of_RandomGeneratorJumpable(): Unit = {
    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)

    assertTrue(rng.isInstanceOf[RandomGenerator])
    assertTrue(rng.isInstanceOf[RandomGenerator.JumpableGenerator])

    // Does rng have a usable & believable method?
    assertNotEquals("rng.nextLong", -3, rng.nextLong(10))
  }

  @Test def rngs(): Unit = {
    val max = 10
    var count = 0

    val originRng = RandomGenerator.LeapableGenerator.of(algorithmName)

    // Logically a Stream.limit(); exercise a different path just for fun

    val rngs = originRng
      .rngs()
      .takeWhile(e => { count += 1; count <= max })
      .forEach(rng =>
        // Does rng have a usable & believable method?
        assertNotEquals("rng.nextLong", -1, rng.nextLong(10))
      )

    assertEquals("count", max + 1, count)

    val rngsSpliterator = originRng
      .rngs()
      .spliterator()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristicsImmutable,
      rngsSpliterator.characteristics()
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      rngsSpliterator.estimateSize()
    )
  }

  // Jumpable Abstract methods

  @Test def copy_Jumpable(): Unit = {
    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)
    val copyOfRng = rng.copy()

    assertTrue(
      "copyOfRng is Jumpable",
      copyOfRng.isInstanceOf[RandomGenerator.JumpableGenerator]
    )

    assertFalse(
      "copyOfRng is Splittable",
      copyOfRng.isInstanceOf[RandomGenerator.SplittableGenerator]
    )

    assertFalse("same reference, not a copy", rng.eq(copyOfRng))

    assertEquals("next values match", rng.nextLong(), copyOfRng.nextLong())
  }

  @Test def jump(): Unit = {
    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)
    val rngReference = rng
    val copyOfRng = rng.copy()

    rng.jump()

    assertTrue("lept rng reference", rngReference.eq(rng))

    val nlBefore = copyOfRng.nextLong()
    val nlAfter = rng.nextLong()

    assertTrue(
      s"nextLong values, before jump: ${nlBefore} after:${nlAfter} ",
      nlBefore != nlAfter
    )
  }

  @Test def jumpDistance(): Unit = {
    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)

    assertEquals(
      "jumpDistance",
      3.402823669209385e38,
      rng.jumpDistance(),
      epsilon
    )
  }

  // Jumpable Default methods

  @Test def copyAndJump(): Unit = {
    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)
    val rngReference = rng

    val copyOfRng = rng.copyAndJump()

    assertFalse("lept rng reference", rngReference.eq(copyOfRng))

    val nlBefore = copyOfRng.nextLong()
    val nlAfter = rng.nextLong()

    assertTrue(
      s"nextLong values, before jump: ${nlBefore} after:${nlAfter} ",
      nlBefore != nlAfter
    )
  }

  @Test def jumps(): Unit = {
    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)
    val maxRngs = 5

    val jumps = rng.jumps()

    def verify[T <: RandomGenerator](r: T): Unit = {
      assertTrue(r.isInstanceOf[RandomGenerator.JumpableGenerator])
    }

    jumps.limit(maxRngs).forEach(verify)
  }

  @Test def jumps_StreamSize(): Unit = {

    val rng = RandomGenerator.JumpableGenerator.of(algorithmName)

    val maxRngs = 6
    val maxCount = maxRngs + 10
    var count = 0

    val jumps = rng.jumps()

    def verify[T <: RandomGenerator](r: T): Unit = {
      assertTrue(r.isInstanceOf[RandomGenerator.JumpableGenerator])
      assertTrue(
        s"exceeded expected number of rngs: ${maxRngs}",
        count < maxCount
      )
      count += 1
    }

    jumps.limit(maxRngs).forEach(verify)

  }

  // Leapable Abstract methods

  @Test def copy_Leapable(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)
    val copyOfRng = rng.copy()

    // copyOfRng is at least Jumpable and may even be Leapable
    assertTrue(
      "copyOfRng is Jumpable",
      copyOfRng.isInstanceOf[RandomGenerator.JumpableGenerator]
    )

    assertFalse(
      "copyOfRng is Splittable",
      copyOfRng.isInstanceOf[RandomGenerator.SplittableGenerator]
    )

    assertFalse("same reference, not a copy", rng.eq(copyOfRng))

    assertEquals("next values match", rng.nextLong(), copyOfRng.nextLong())
  }

  @Test def leap(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)
    val rngReference = rng
    val copyOfRng = rng.copy()

    rng.leap()

    assertTrue("lept rng reference", rngReference.eq(rng))

    val nlBefore = copyOfRng.nextLong()
    val nlAfter = rng.nextLong()

    assertTrue(
      s"nextLong values, before leap: ${nlBefore} after:${nlAfter} ",
      nlBefore != nlAfter
    )
  }

  @Test def leapDistance(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)

    assertEquals(
      "leapDistance",
      6.277101735386681e57,
      rng.leapDistance(),
      epsilon
    )
  }

  // Leapable Default methods

  @Test def copyAndLeap(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)
    val rngReference = rng

    val copyOfRng = rng.copyAndLeap()

    assertFalse("lept rng reference", rngReference.eq(copyOfRng))

    val nlBefore = copyOfRng.nextLong()
    val nlAfter = rng.nextLong()

    assertTrue(
      s"nextLong values, before leap: ${nlBefore} after:${nlAfter} ",
      nlBefore != nlAfter
    )
  }

  @Test def leaps(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)
    val maxRngs = 5

    val leaps = rng.leaps()

    def verify[T <: RandomGenerator](r: T): Unit = {
      assertTrue(r.isInstanceOf[RandomGenerator.JumpableGenerator])
    }

    leaps.limit(maxRngs).forEach(verify)
  }

  @Test def leaps_StreamSize(): Unit = {
    val rng = RandomGenerator.LeapableGenerator.of(algorithmName)

    val maxRngs = 6
    val maxCount = maxRngs + 10
    var count = 0

    val leaps = rng.leaps()

    def verify[T <: RandomGenerator](r: T): Unit = {
      assertTrue(r.isInstanceOf[RandomGenerator.JumpableGenerator])
      assertTrue(
        s"exceeded expected number of rngs: ${maxRngs}",
        count < maxCount
      )
      count += 1
    }

    leaps.limit(maxRngs).forEach(verify)
  }

  /* Methods unique to class under test
   */

  @Test def algorithmAttributes(): Unit = {
    // Spot check, did we get the right Factory?

    assertEquals("equidistribution()", 3, factory.equidistribution())
    assertEquals("group()", "Xoshiro", factory.group())
    assertFalse("isArbitrarilyJumpable()", factory.isArbitrarilyJumpable())
    assertFalse("isDeprecated()", factory.isDeprecated())
    assertTrue("isJumpable()", factory.isJumpable())
    assertTrue("isLeapable()", factory.isLeapable())
    assertFalse("isSplittable()", factory.isSplittable())
    assertEquals("name()", algorithmName, factory.name())
    assertEquals("stateBits()", 256, factory.stateBits())
  }

}
