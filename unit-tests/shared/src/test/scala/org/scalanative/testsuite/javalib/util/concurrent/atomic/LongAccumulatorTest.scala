/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 * file: src/test/tck/LongAccumulatorTest.java
 * revision 1.10, dated: 2019-09-08
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent.atomic

import java.util.concurrent.atomic.LongAccumulator
import java.util.concurrent.{
  ExecutorService, Executors, Phaser, ThreadLocalRandom
}
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test

class LongAccumulatorTest extends JSR166Test {
  import JSR166Test._

  @Test def testConstructor(): Unit = {
    for (identity <- List(jl.Long.MIN_VALUE, 0L, jl.Long.MAX_VALUE))
      assertEquals(identity, new LongAccumulator(jl.Long.max, identity).get())
  }

  /* accumulate accumulates given value to current, and get returns current
   *  value
   */
  @Test def testAccumulateAndGet(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    acc.accumulate(2)
    assertEquals("a1", 2, acc.get())
    acc.accumulate(-4)
    assertEquals("a2", 2, acc.get())
    acc.accumulate(4)
    assertEquals("a3", 4, acc.get())
  }

  /* reset() causes subsequent get() to return zero
   */
  @Test def testReset(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    acc.accumulate(2)
    assertEquals("a1", 2, acc.get())
    acc.reset()
    assertEquals("a2", 0, acc.get())
  }

  /* getThenReset() returns current value; subsequent get() returns zero
   */
  @Test def testGetThenReset(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    acc.accumulate(2)
    assertEquals("a1", 2, acc.get())
    assertEquals("a2", 2, acc.getThenReset())
    assertEquals("a3", 0, acc.get())
  }

  /* toString returns current value.
   */
  @Test def testToString(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    assertEquals("a1", "0", acc.toString())
    acc.accumulate(1)
    assertEquals("a2", jl.Long.toString(1), acc.toString())
  }

  /* intValue returns current value.
   */
  @Test def testIntValue(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    assertEquals("a1", 0, acc.intValue())
    acc.accumulate(1)
    assertEquals("a2", 1, acc.intValue())
  }

  /** longValue returns current value.
   */
  @Test def testLongValue(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    assertEquals("a1", 0, acc.longValue())
    acc.accumulate(1)
    assertEquals("a2", 1, acc.longValue())
  }

  /** floatValue returns current value.
   */
  @Test def testFloatValue(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    assertEquals("a1", 0.0f, acc.floatValue(), 0.0f)
    acc.accumulate(1)
    assertEquals("a2", 1.0f, acc.floatValue(), 0.0f)
  }

  /** doubleValue returns current value.
   */
  @Test def testDoubleValue(): Unit = {
    val acc = new LongAccumulator(jl.Long.max, 0L)
    assertEquals("a1", 0.0, acc.doubleValue(), 0.0)
    acc.accumulate(1)
    assertEquals("a2", 1.0, acc.doubleValue(), 0.0)
  }

  /** accumulates by multiple threads produce correct result
   */
  @Test def testAccumulateAndGetMT(): Unit = {
    val acc = new LongAccumulator((x, y) => x + y, 0L)
    val nThreads = ThreadLocalRandom.current().nextInt(1, 5)
    val phaser = new Phaser(nThreads + 1)
    val incs = if (expensiveTests) 1000000 else 100000
    val total: scala.Long = nThreads * incs / 2L * (incs - 1) // Gauss
    val task: Runnable = () => {
      phaser.arriveAndAwaitAdvance()
      for (i <- 0 until incs) {
        acc.accumulate(i)
        assertTrue("index: $i", acc.get() <= total)
      }
      phaser.arrive()
    }

    val p = Executors.newCachedThreadPool()

    usingPoolCleaner(p) { _ =>
      for (i <- nThreads until 0 by -1)
        p.execute(task)

      phaser.arriveAndAwaitAdvance()
      phaser.arriveAndAwaitAdvance()
      assertEquals(total, acc.get())
    }
  }
}
