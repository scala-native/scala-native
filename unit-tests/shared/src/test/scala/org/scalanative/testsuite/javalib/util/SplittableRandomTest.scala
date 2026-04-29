/*
 * Includes tests ported from JSR-166 TCK tests and released to the public
 * domain, as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

// Ported from Scala.js, revision c473689, dated 3 May 2021

/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalanative.testsuite.javalib.util

import java.util.SplittableRandom
import java.util.concurrent.atomic.{AtomicInteger, LongAdder}
import java.util.function.{DoubleConsumer, IntConsumer, LongConsumer}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class SplittableRandomTest extends JSR166Test {
  import SplittableRandomTest._

  @Test def testNextInt(): Unit = {
    val sr = new SplittableRandom()
    val f = sr.nextInt()
    var i = 0
    while (i < NCALLS && sr.nextInt() == f) {
      i += 1
    }
    assertTrue(i < NCALLS)
  }

  @Test def testNextLong(): Unit = {
    val sr = new SplittableRandom()
    val f = sr.nextLong()
    var i = 0
    while (i < NCALLS && sr.nextLong() == f) {
      i += 1
    }
    assertTrue(i < NCALLS)
  }

  @Test def testNextDouble(): Unit = {
    val sr = new SplittableRandom()
    val f = sr.nextDouble()
    var i = 0
    while (i < NCALLS && sr.nextDouble() == f) {
      i += 1
    }
    assertTrue(i < NCALLS)
  }

  @Test def testSeedConstructor(): Unit = {
    var seed = 2L
    while (seed < MAX_LONG_BOUND) {
      val sr1 = new SplittableRandom(seed)
      val sr2 = new SplittableRandom(seed)
      var i = 0
      while (i < REPS) {
        assertEquals(sr1.nextLong(), sr2.nextLong())
        i += 1
      }
      seed += 15485863L
    }
  }

  @Test def testSplit1(): Unit = {
    val sr = new SplittableRandom()
    var reps = 0
    while (reps < REPS) {
      val sc = sr.split()
      var i = 0
      while (i < NCALLS && sr.nextLong() == sc.nextLong()) {
        i += 1
      }
      assertTrue(i < NCALLS)
      reps += 1
    }
  }

  @Test def testSplit2(): Unit = {
    val sr = new SplittableRandom(12345L)
    var reps = 0
    while (reps < REPS) {
      val sc = sr.split()
      var i = 0
      while (i < NCALLS && sr.nextLong() == sc.nextLong()) {
        i += 1
      }
      assertTrue(i < NCALLS)
      reps += 1
    }
  }

  @Test def testNextIntBoundNonPositive(): Unit = {
    val sr = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], sr.nextInt(-17))
    assertThrows(classOf[IllegalArgumentException], sr.nextInt(0))
    assertThrows(classOf[IllegalArgumentException], sr.nextInt(Int.MinValue))
  }

  @Test def testNextIntBadBounds(): Unit = {
    val sr = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], sr.nextInt(17, 2))
    assertThrows(classOf[IllegalArgumentException], sr.nextInt(-42, -42))
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextInt(Int.MaxValue, Int.MinValue)
    )
  }

  @Test def testNextIntBounded(): Unit = {
    val sr = new SplittableRandom()
    var i0 = 0
    while (i0 < 2) {
      assertEquals(0, sr.nextInt(1))
      i0 += 1
    }

    var bound = 2
    while (bound < MAX_INT_BOUND) {
      val f = sr.nextInt(bound)
      assertTrue(0 <= f && f < bound)
      var i = 0
      var j = 0
      while (i < NCALLS && {
            j = sr.nextInt(bound)
            j == f
          }) {
        assertTrue(0 <= j && j < bound)
        i += 1
      }
      assertTrue(i < NCALLS)
      bound += 524959
    }
  }

  @Test def testNextIntBounded2(): Unit = {
    val sr = new SplittableRandom()
    var least = -15485863
    while (least < MAX_INT_BOUND) {
      var bound = least + 2
      while (bound > least && bound < MAX_INT_BOUND) {
        val f = sr.nextInt(least, bound)
        assertTrue(least <= f && f < bound)
        var i = 0
        var j = 0
        while (i < NCALLS && {
              j = sr.nextInt(least, bound)
              j == f
            }) {
          assertTrue(least <= j && j < bound)
          i += 1
        }
        assertTrue(i < NCALLS)
        bound += 49979687
      }
      least += 524959
    }
  }

  @Test def testNextLongBoundNonPositive(): Unit = {
    val sr = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], sr.nextLong(-17L))
    assertThrows(classOf[IllegalArgumentException], sr.nextLong(0L))
    assertThrows(classOf[IllegalArgumentException], sr.nextLong(Long.MinValue))
  }

  @Test def testNextLongBadBounds(): Unit = {
    val sr = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], sr.nextLong(17L, 2L))
    assertThrows(classOf[IllegalArgumentException], sr.nextLong(-42L, -42L))
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextLong(Long.MaxValue, Long.MinValue)
    )
  }

  @Test def testNextLongBounded(): Unit = {
    val sr = new SplittableRandom()
    var i0 = 0
    while (i0 < 2) {
      assertEquals(0L, sr.nextLong(1L))
      i0 += 1
    }

    var bound = 2L
    while (bound < MAX_LONG_BOUND) {
      val f = sr.nextLong(bound)
      assertTrue(0L <= f && f < bound)
      var i = 0
      var j = 0L
      while (i < NCALLS && {
            j = sr.nextLong(bound)
            j == f
          }) {
        assertTrue(0L <= j && j < bound)
        i += 1
      }
      assertTrue(i < NCALLS)
      bound += 15485863L
    }
  }

  @Test def testNextLongBounded2(): Unit = {
    val sr = new SplittableRandom()
    var least = -86028121L
    while (least < MAX_LONG_BOUND) {
      var bound = least + 2L
      while (bound > least && bound < MAX_LONG_BOUND) {
        val f = sr.nextLong(least, bound)
        assertTrue(least <= f && f < bound)
        var i = 0
        var j = 0L
        while (i < NCALLS && {
              j = sr.nextLong(least, bound)
              j == f
            }) {
          assertTrue(least <= j && j < bound)
          i += 1
        }
        assertTrue(i < NCALLS)
        bound += Math.abs(bound * 7919L)
      }
      least += 982451653L
    }
  }

  @Test def testNextDoubleBoundNonPositive(): Unit = {
    val sr = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], sr.nextDouble(-17.0d))
    assertThrows(classOf[IllegalArgumentException], sr.nextDouble(0.0d))
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(-java.lang.Double.MIN_VALUE)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(java.lang.Double.NEGATIVE_INFINITY)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(java.lang.Double.NaN)
    )
  }

  @Test def testNextDoubleBadBounds(): Unit = {
    val sr = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], sr.nextDouble(17.0d, 2.0d))
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(-42.0d, -42.0d)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(java.lang.Double.MAX_VALUE, java.lang.Double.MIN_VALUE)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(java.lang.Double.NaN, 0.0d)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      sr.nextDouble(0.0d, java.lang.Double.NaN)
    )
  }

  @Test def testNextDoubleBounded2(): Unit = {
    val sr = new SplittableRandom()
    var least = 0.0001d
    while (least < 1.0e20) {
      var bound = least * 1.001d
      while (bound < 1.0e20) {
        val f = sr.nextDouble(least, bound)
        assertTrue(least <= f && f < bound)
        var i = 0
        var j = 0.0d
        while (i < NCALLS && {
              j = sr.nextDouble(least, bound)
              j == f
            }) {
          assertTrue(least <= j && j < bound)
          i += 1
        }
        assertTrue(i < NCALLS)
        bound *= 16.0d
      }
      least *= 8.0d
    }
  }

  @Test def testBadStreamSize(): Unit = {
    val r = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], r.ints(-1L))
    assertThrows(classOf[IllegalArgumentException], r.ints(-1L, 2, 3))
    assertThrows(classOf[IllegalArgumentException], r.longs(-1L))
    assertThrows(classOf[IllegalArgumentException], r.longs(-1L, -1L, 1L))
    assertThrows(classOf[IllegalArgumentException], r.doubles(-1L))
    assertThrows(classOf[IllegalArgumentException], r.doubles(-1L, .5, .6))
  }

  @Test def testBadStreamBounds(): Unit = {
    val r = new SplittableRandom()
    assertThrows(classOf[IllegalArgumentException], r.ints(2, 1))
    assertThrows(classOf[IllegalArgumentException], r.ints(10, 42, 42))
    assertThrows(classOf[IllegalArgumentException], r.longs(-1L, -1L))
    assertThrows(classOf[IllegalArgumentException], r.longs(10, 1L, -2L))
    assertThrows(classOf[IllegalArgumentException], r.doubles(0.0d, 0.0d))
    assertThrows(classOf[IllegalArgumentException], r.doubles(10L, .5, .4))
  }

  @Test def testIntsCount(): Unit = {
    val counter = new LongAdder()
    val r = new SplittableRandom()
    var size = 0L
    var reps = 0
    while (reps < REPS) {
      counter.reset()
      r.ints(size)
        .parallel()
        .forEach(new IntConsumer {
          override def accept(x: Int): Unit = counter.increment()
        })
      assertEquals(size, counter.sum())
      size += 524959L
      reps += 1
    }
  }

  @Test def testLongsCount(): Unit = {
    val counter = new LongAdder()
    val r = new SplittableRandom()
    var size = 0L
    var reps = 0
    while (reps < REPS) {
      counter.reset()
      r.longs(size)
        .parallel()
        .forEach(new LongConsumer {
          override def accept(x: Long): Unit = counter.increment()
        })
      assertEquals(size, counter.sum())
      size += 524959L
      reps += 1
    }
  }

  @Test def testDoublesCount(): Unit = {
    val counter = new LongAdder()
    val r = new SplittableRandom()
    var size = 0L
    var reps = 0
    while (reps < REPS) {
      counter.reset()
      r.doubles(size)
        .parallel()
        .forEach(new DoubleConsumer {
          override def accept(x: Double): Unit = counter.increment()
        })
      assertEquals(size, counter.sum())
      size += 524959L
      reps += 1
    }
  }

  @Test def testBoundedInts(): Unit = {
    val fails = new AtomicInteger(0)
    val r = new SplittableRandom()
    val size = 12345L
    var least = -15485867
    while (least < MAX_INT_BOUND) {
      var bound = least + 2
      while (bound > least && bound < MAX_INT_BOUND) {
        val lo = least
        val hi = bound
        r.ints(size, lo, hi)
          .parallel()
          .forEach(new IntConsumer {
            override def accept(x: Int): Unit =
              if (x < lo || x >= hi) fails.getAndIncrement()
          })
        bound += 67867967
      }
      least += 524959
    }
    assertEquals(0, fails.get())
  }

  @Test def testBoundedLongs(): Unit = {
    val fails = new AtomicInteger(0)
    val r = new SplittableRandom()
    val size = 123L
    var least = -86028121L
    while (least < MAX_LONG_BOUND) {
      var bound = least + 2L
      while (bound > least && bound < MAX_LONG_BOUND) {
        val lo = least
        val hi = bound
        r.longs(size, lo, hi)
          .parallel()
          .forEach(new LongConsumer {
            override def accept(x: Long): Unit =
              if (x < lo || x >= hi) fails.getAndIncrement()
          })
        bound += Math.abs(bound * 7919L)
      }
      least += 1982451653L
    }
    assertEquals(0, fails.get())
  }

  @Test def testBoundedDoubles(): Unit = {
    val fails = new AtomicInteger(0)
    val r = new SplittableRandom()
    val size = 456L
    var least = 0.00011d
    while (least < 1.0e20) {
      var bound = least * 1.0011d
      while (bound < 1.0e20) {
        val lo = least
        val hi = bound
        r.doubles(size, lo, hi)
          .parallel()
          .forEach(new DoubleConsumer {
            override def accept(x: Double): Unit =
              if (x < lo || x >= hi) fails.getAndIncrement()
          })
        bound *= 17.0d
      }
      least *= 9.0d
    }
    assertEquals(0, fails.get())
  }

  @Test def testUnsizedIntsCount(): Unit = {
    val counter = new LongAdder()
    new SplittableRandom()
      .ints()
      .limit(100L)
      .parallel()
      .forEach(
        new IntConsumer {
          override def accept(x: Int): Unit = counter.increment()
        }
      )
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedLongsCount(): Unit = {
    val counter = new LongAdder()
    new SplittableRandom()
      .longs()
      .limit(100L)
      .parallel()
      .forEach(
        new LongConsumer {
          override def accept(x: Long): Unit = counter.increment()
        }
      )
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedDoublesCount(): Unit = {
    val counter = new LongAdder()
    new SplittableRandom()
      .doubles()
      .limit(100L)
      .parallel()
      .forEach(
        new DoubleConsumer {
          override def accept(x: Double): Unit = counter.increment()
        }
      )
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedIntsCountSeq(): Unit = {
    val counter = new LongAdder()
    new SplittableRandom()
      .ints()
      .limit(100L)
      .forEach(new IntConsumer {
        override def accept(x: Int): Unit = counter.increment()
      })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedLongsCountSeq(): Unit = {
    val counter = new LongAdder()
    new SplittableRandom()
      .longs()
      .limit(100L)
      .forEach(new LongConsumer {
        override def accept(x: Long): Unit = counter.increment()
      })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedDoublesCountSeq(): Unit = {
    val counter = new LongAdder()
    new SplittableRandom()
      .doubles()
      .limit(100L)
      .forEach(new DoubleConsumer {
        override def accept(x: Double): Unit = counter.increment()
      })
    assertEquals(100L, counter.sum())
  }

  @Test def testShouldImplementMostRandomMethods(): Unit = {
    val r = new SplittableRandom(123L)
    val bytes = new Array[Byte](1)

    r.nextBytes(bytes)
    r.nextBoolean()
    r.nextInt()
    r.nextInt(2)
    r.nextLong()
    r.nextDouble()

    r.ints()
    r.ints(1L)
    r.ints(0, 1)
    r.ints(1L, 0, 1)
    r.longs()
    r.longs(1L)
    r.longs(0L, 1L)
    r.longs(1L, 0L, 1L)
    r.doubles()
    r.doubles(1L)
    r.doubles(0.0d, 1.0d)
    r.doubles(1L, 0.0d, 1.0d)
  }

  @Test def testNextBytes(): Unit = {
    val sr = new SplittableRandom()
    val n = sr.nextInt(1, 20)
    val bytes = new Array[Byte](n)
    var i = 0
    while (i < n) {
      var tries = NCALLS
      var varied = false
      while (tries > 0 && !varied) {
        val before = bytes(i)
        sr.nextBytes(bytes)
        val after = bytes(i)
        if (after * before < 0) {
          varied = true
        }
        tries -= 1
      }
      if (!varied) {
        fail("not enough variation in random bytes")
      }
      i += 1
    }
  }

  @Test def testNextBytes_emptyArray(): Unit = {
    new SplittableRandom().nextBytes(new Array[Byte](0))
  }

  @Test def testNextBytes_nullArray(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new SplittableRandom().nextBytes(null)
    )
  }

  @Test def nextLong(): Unit = {
    val sr1 = new SplittableRandom(205620432625028L)
    assertEquals(-546649510716590878L, sr1.nextLong())
    assertEquals(5574037117696891406L, sr1.nextLong())
    assertEquals(-2877648745898596966L, sr1.nextLong())
    assertEquals(5734720902145206190L, sr1.nextLong())
    assertEquals(1684781725002208217L, sr1.nextLong())
    assertEquals(687902890032948154L, sr1.nextLong())
    assertEquals(176280366443457561L, sr1.nextLong())
    assertEquals(-2944062288620903198L, sr1.nextLong())
    assertEquals(6872063775710978746L, sr1.nextLong())
    assertEquals(-7374959378916621341L, sr1.nextLong())

    val sr2 = new SplittableRandom(-7374959378916621341L)
    assertEquals(3241340805431811560L, sr2.nextLong())
    assertEquals(-2124831722811234979L, sr2.nextLong())
    assertEquals(7339249063279462363L, sr2.nextLong())
    assertEquals(1969867631102365324L, sr2.nextLong())
    assertEquals(81632902222022867L, sr2.nextLong())
    assertEquals(3451014011249622471L, sr2.nextLong())
    assertEquals(-1727223780574897556L, sr2.nextLong())
    assertEquals(-5128686556801302975L, sr2.nextLong())
    assertEquals(-6412221907719417908L, sr2.nextLong())
    assertEquals(-110482401893286265L, sr2.nextLong())
  }

  @Test def nextInt(): Unit = {
    val sr1 = new SplittableRandom(-84638)
    assertEquals(962946964, sr1.nextInt())
    assertEquals(1723227640, sr1.nextInt())
    assertEquals(-621790539, sr1.nextInt())
    assertEquals(-1848500421, sr1.nextInt())
    assertEquals(-614898617, sr1.nextInt())
    assertEquals(-628601850, sr1.nextInt())
    assertEquals(-463597391, sr1.nextInt())
    assertEquals(1874082924, sr1.nextInt())
    assertEquals(-1206032701, sr1.nextInt())
    assertEquals(1549874426, sr1.nextInt())

    val sr2 = new SplittableRandom(1549874426)
    assertEquals(-495782737, sr2.nextInt())
    assertEquals(-1487672352, sr2.nextInt())
    assertEquals(-538628223, sr2.nextInt())
    assertEquals(1117712970, sr2.nextInt())
    assertEquals(2081437683, sr2.nextInt())
    assertEquals(2134440938, sr2.nextInt())
    assertEquals(-2102672277, sr2.nextInt())
    assertEquals(832521577, sr2.nextInt())
    assertEquals(518494223, sr2.nextInt())
    assertEquals(-42114979, sr2.nextInt())
  }

  @Test def nextDouble(): Unit = {
    val sr1 = new SplittableRandom(-45)
    assertEquals(0.8229662358649753, sr1.nextDouble(), 0.0)
    assertEquals(0.43324117570991283, sr1.nextDouble(), 0.0)
    assertEquals(0.2639712712295723, sr1.nextDouble(), 0.0)
    assertEquals(0.5576376282289696, sr1.nextDouble(), 0.0)
    assertEquals(0.5505810186639037, sr1.nextDouble(), 0.0)
    assertEquals(0.3944509738261206, sr1.nextDouble(), 0.0)
    assertEquals(0.3108138671457821, sr1.nextDouble(), 0.0)
    assertEquals(0.585951421265481, sr1.nextDouble(), 0.0)
    assertEquals(0.2009547438834305, sr1.nextDouble(), 0.0)
    assertEquals(0.8317691736686829, sr1.nextDouble(), 0.0)

    val sr2 = new SplittableRandom(45)
    assertEquals(0.9684135896502549, sr2.nextDouble(), 0.0)
    assertEquals(0.9819686323309464, sr2.nextDouble(), 0.0)
    assertEquals(0.5311927268453047, sr2.nextDouble(), 0.0)
    assertEquals(0.8521356026917833, sr2.nextDouble(), 0.0)
    assertEquals(0.01880601374789126, sr2.nextDouble(), 0.0)
    assertEquals(0.37792881248018584, sr2.nextDouble(), 0.0)
    assertEquals(0.7179744490511354, sr2.nextDouble(), 0.0)
    assertEquals(0.3448879713662756, sr2.nextDouble(), 0.0)
    assertEquals(0.023020123407108684, sr2.nextDouble(), 0.0)
    assertEquals(0.6454709437764473, sr2.nextDouble(), 0.0)
  }

  @Test def nextBoolean(): Unit = {
    val sr1 = new SplittableRandom(4782934)
    assertFalse(sr1.nextBoolean())
    assertFalse(sr1.nextBoolean())
    assertTrue(sr1.nextBoolean())
    assertTrue(sr1.nextBoolean())
    assertTrue(sr1.nextBoolean())
    assertFalse(sr1.nextBoolean())
    assertFalse(sr1.nextBoolean())
    assertTrue(sr1.nextBoolean())
    assertTrue(sr1.nextBoolean())
    assertTrue(sr1.nextBoolean())

    val sr2 = new SplittableRandom(-4782934)
    assertFalse(sr2.nextBoolean())
    assertFalse(sr2.nextBoolean())
    assertTrue(sr2.nextBoolean())
    assertTrue(sr2.nextBoolean())
    assertTrue(sr2.nextBoolean())
    assertFalse(sr2.nextBoolean())
    assertFalse(sr2.nextBoolean())
    assertTrue(sr2.nextBoolean())
    assertTrue(sr2.nextBoolean())
    assertTrue(sr2.nextBoolean())
  }

  @Test def split(): Unit = {
    val sr1 = new SplittableRandom(205620432625028L).split()
    assertEquals(-2051870635339219700L, sr1.nextLong())
    assertEquals(-4512002368431042276L, sr1.nextLong())

    val sr2 = new SplittableRandom(-4512002368431042276L).split()
    assertEquals(7607532382842316154L, sr2.nextLong())
    assertEquals(-1011899051174066375L, sr2.nextLong())

    val sr3 = new SplittableRandom(7607532382842316154L).split()
    assertEquals(-1531465968943756660L, sr3.nextLong())
    assertEquals(948449286892387518L, sr3.nextLong())

    val sr4 = new SplittableRandom(948449286892387518L).split()
    assertEquals(2486448889230464769L, sr4.nextLong())
    assertEquals(4550542803092639410L, sr4.nextLong())

    val sr5 = sr4.split()
    assertEquals(8668601242423591169L, sr5.nextLong())
    assertEquals(-986244092642826172L, sr5.nextLong())

    val sr6 = sr4.split()
    assertEquals(274792684182118046L, sr6.nextLong())
    assertEquals(683259797650761389L, sr6.nextLong())

    val sr7 = sr6.split()
    assertEquals(1682793527903105269L, sr7.nextLong())
    assertEquals(2140483520539013019L, sr7.nextLong())

    val sr8 = sr6.split()
    assertEquals(-7468768144124082123L, sr8.nextLong())
    assertEquals(6163667569279435512L, sr8.nextLong())
  }

}

object SplittableRandomTest {
  private final val NCALLS = 10000
  private final val MAX_INT_BOUND = 1 << 26
  private final val MAX_LONG_BOUND = 1L << 40
  private final val REPS =
    Integer.getInteger("SplittableRandomTest.reps", 4).intValue()
}
