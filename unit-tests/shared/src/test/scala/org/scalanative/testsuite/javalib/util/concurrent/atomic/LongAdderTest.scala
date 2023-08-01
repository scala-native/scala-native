/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent.atomic

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test
import JSR166Test._

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.LongAdder

class LongAdderTest extends JSR166Test {
  import JSR166Test._

  @Test def testConstructor(): Unit = {
    val ai = new LongAdder
    assertEquals(0, ai.sum)
  }

  /** add adds given value to current, and sum returns current value
   */
  @Test def testAddAndSum(): Unit = {
    val ai = new LongAdder
    ai.add(2)
    assertEquals(2, ai.sum)
    ai.add(-4)
    assertEquals(-2, ai.sum)
  }

  /** decrement decrements and sum returns current value
   */
  @Test def testDecrementAndSum(): Unit = {
    val ai = new LongAdder
    ai.decrement()
    assertEquals(-1, ai.sum)
    ai.decrement()
    assertEquals(-2, ai.sum)
  }

  /** incrementAndGet increments and returns current value
   */
  @Test def testIncrementAndSum(): Unit = {
    val ai = new LongAdder
    ai.increment()
    assertEquals(1, ai.sum)
    ai.increment()
    assertEquals(2, ai.sum)
  }

  /** reset() causes subsequent sum() to return zero
   */
  @Test def testReset(): Unit = {
    val ai = new LongAdder
    ai.add(2)
    assertEquals(2, ai.sum)
    ai.reset()
    assertEquals(0, ai.sum)
  }

  /** sumThenReset() returns sum; subsequent sum() returns zero
   */
  @Test def testSumThenReset(): Unit = {
    val ai = new LongAdder
    ai.add(2)
    assertEquals(2, ai.sum)
    assertEquals(2, ai.sumThenReset)
    assertEquals(0, ai.sum)
  }

  /** toString returns current value.
   */
  @Test def testToString(): Unit = {
    val ai = new LongAdder
    assertEquals("0", ai.toString)
    ai.increment()
    assertEquals(1L.toString(), ai.toString)
  }

  /** intValue returns current value.
   */
  @Test def testIntValue(): Unit = {
    val ai = new LongAdder
    assertEquals(0, ai.intValue)
    ai.increment()
    assertEquals(1, ai.intValue)
  }

  /** longValue returns current value.
   */
  @Test def testLongValue(): Unit = {
    val ai = new LongAdder
    assertEquals(0, ai.longValue)
    ai.increment()
    assertEquals(1, ai.longValue)
  }

  /** floatValue returns current value.
   */
  @Test def testFloatValue(): Unit = {
    val ai = new LongAdder
    assertEquals(0.0f, ai.floatValue, 0.0f)
    ai.increment()
    assertEquals(1.0f, ai.floatValue, 0.0f)
  }

  /** doubleValue returns current value.
   */
  @Test def testDoubleValue(): Unit = {
    val ai = new LongAdder
    assertEquals(0.0, ai.doubleValue, 0.0)
    ai.increment()
    assertEquals(1.0, ai.doubleValue, 0.0)
  }

  /** adds by multiple threads produce correct sum
   */
  @throws[Throwable]
  def testAddAndSumMT(): Unit = {
    val incs = 1000000
    val nthreads = 4
    val pool = Executors.newCachedThreadPool()
    val a = new LongAdder
    val barrier = new CyclicBarrier(nthreads + 1)
    for (i <- 0 until nthreads) {
      pool.execute(new AdderTask(a, barrier, incs))
    }
    barrier.await
    barrier.await
    val total = nthreads.toLong * incs
    val sum = a.sum
    assertEquals(sum, total)
    pool.shutdown()
  }
  final class AdderTask(
      val adder: LongAdder,
      val barrier: CyclicBarrier,
      val incs: Int
  ) extends Runnable {
    var result = 0L

    override def run(): Unit = {
      try {
        barrier.await
        val a = adder
        for (i <- 0 until incs) {
          a.add(1L)
        }
        result = a.sum
        barrier.await
      } catch {
        case t: Throwable =>
          throw new Error(t)
      }
    }
  }
}
