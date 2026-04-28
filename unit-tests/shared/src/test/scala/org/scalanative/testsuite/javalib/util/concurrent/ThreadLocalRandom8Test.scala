/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.{AtomicInteger, LongAdder}
import java.util.function.{DoubleConsumer, IntConsumer, LongConsumer}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ThreadLocalRandom8Test extends JSR166Test {
  import ThreadLocalRandom8Test._

  @Test def testBadStreamSize(): Unit = {
    val r = ThreadLocalRandom.current()
    assertThrows(classOf[IllegalArgumentException], r.ints(-1L))
    assertThrows(classOf[IllegalArgumentException], r.ints(-1L, 2, 3))
    assertThrows(classOf[IllegalArgumentException], r.longs(-1L))
    assertThrows(classOf[IllegalArgumentException], r.longs(-1L, -1L, 1L))
    assertThrows(classOf[IllegalArgumentException], r.doubles(-1L))
    assertThrows(classOf[IllegalArgumentException], r.doubles(-1L, .5, .6))
  }

  @Test def testBadStreamBounds(): Unit = {
    val r = ThreadLocalRandom.current()
    assertThrows(classOf[IllegalArgumentException], r.ints(2, 1))
    assertThrows(classOf[IllegalArgumentException], r.ints(10, 42, 42))
    assertThrows(classOf[IllegalArgumentException], r.longs(-1L, -1L))
    assertThrows(classOf[IllegalArgumentException], r.longs(10, 1L, -2L))
    assertThrows(classOf[IllegalArgumentException], r.doubles(0.0, 0.0))
    assertThrows(classOf[IllegalArgumentException], r.doubles(10, .5, .4))
  }

  @Test def testIntsCount(): Unit = {
    val counter = new LongAdder()
    val r = ThreadLocalRandom.current()
    var size = 0L
    var reps = 0
    while (reps < REPS) {
      counter.reset()
      r.ints(size).parallel().forEach(new IntConsumer {
        override def accept(x: Int): Unit = counter.increment()
      })
      assertEquals(size, counter.sum())
      size += 524959L
      reps += 1
    }
  }

  @Test def testLongsCount(): Unit = {
    val counter = new LongAdder()
    val r = ThreadLocalRandom.current()
    var size = 0L
    var reps = 0
    while (reps < REPS) {
      counter.reset()
      r.longs(size).parallel().forEach(new LongConsumer {
        override def accept(x: Long): Unit = counter.increment()
      })
      assertEquals(size, counter.sum())
      size += 524959L
      reps += 1
    }
  }

  @Test def testDoublesCount(): Unit = {
    val counter = new LongAdder()
    val r = ThreadLocalRandom.current()
    var size = 0L
    var reps = 0
    while (reps < REPS) {
      counter.reset()
      r.doubles(size).parallel().forEach(new DoubleConsumer {
        override def accept(x: Double): Unit = counter.increment()
      })
      assertEquals(size, counter.sum())
      size += 524959L
      reps += 1
    }
  }

  @Test def testBoundedInts(): Unit = {
    val fails = new AtomicInteger(0)
    val r = ThreadLocalRandom.current()
    val size = 12345L
    var least = -15485867
    while (least < MAX_INT_BOUND) {
      var bound = least + 2
      while (bound > least && bound < MAX_INT_BOUND) {
        val lo = least
        val hi = bound
        r.ints(size, lo, hi).parallel().forEach(new IntConsumer {
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
    val r = ThreadLocalRandom.current()
    val size = 123L
    var least = -86028121L
    while (least < MAX_LONG_BOUND) {
      var bound = least + 2L
      while (bound > least && bound < MAX_LONG_BOUND) {
        val lo = least
        val hi = bound
        r.longs(size, lo, hi).parallel().forEach(new LongConsumer {
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
    val r = ThreadLocalRandom.current()
    val size = 456L
    var least = 0.00011
    while (least < 1.0e20) {
      var bound = least * 1.0011
      while (bound < 1.0e20) {
        val lo = least
        val hi = bound
        r.doubles(size, lo, hi).parallel().forEach(new DoubleConsumer {
          override def accept(x: Double): Unit =
            if (x < lo || x >= hi) fails.getAndIncrement()
        })
        bound *= 17.0
      }
      least *= 9.0
    }
    assertEquals(0, fails.get())
  }

  @Test def testUnsizedIntsCount(): Unit = {
    val counter = new LongAdder()
    ThreadLocalRandom
      .current()
      .ints()
      .limit(100L)
      .parallel()
      .forEach(new IntConsumer {
        override def accept(x: Int): Unit = counter.increment()
      })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedLongsCount(): Unit = {
    val counter = new LongAdder()
    ThreadLocalRandom
      .current()
      .longs()
      .limit(100L)
      .parallel()
      .forEach(new LongConsumer {
        override def accept(x: Long): Unit = counter.increment()
      })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedDoublesCount(): Unit = {
    val counter = new LongAdder()
    ThreadLocalRandom
      .current()
      .doubles()
      .limit(100L)
      .parallel()
      .forEach(new DoubleConsumer {
        override def accept(x: Double): Unit = counter.increment()
      })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedIntsCountSeq(): Unit = {
    val counter = new LongAdder()
    ThreadLocalRandom.current().ints().limit(100L).forEach(new IntConsumer {
      override def accept(x: Int): Unit = counter.increment()
    })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedLongsCountSeq(): Unit = {
    val counter = new LongAdder()
    ThreadLocalRandom.current().longs().limit(100L).forEach(new LongConsumer {
      override def accept(x: Long): Unit = counter.increment()
    })
    assertEquals(100L, counter.sum())
  }

  @Test def testUnsizedDoublesCountSeq(): Unit = {
    val counter = new LongAdder()
    ThreadLocalRandom.current().doubles().limit(100L).forEach(new DoubleConsumer {
      override def accept(x: Double): Unit = counter.increment()
    })
    assertEquals(100L, counter.sum())
  }

  @Ignore("No ObjectInputStream/ObjectOutputStream in Scala Native")
  @Test def testSerialization(): Unit = ()
}

object ThreadLocalRandom8Test {
  private final val MAX_INT_BOUND = 1 << 26
  private final val MAX_LONG_BOUND = 1L << 42
  private final val REPS =
    Integer.getInteger("ThreadLocalRandom8Test.reps", 4).intValue()
}
