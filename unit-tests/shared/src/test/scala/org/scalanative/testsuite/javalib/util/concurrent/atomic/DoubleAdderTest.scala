/* Ported from JSR 166 revision 1.8, dated: 2017-08-04
 *   https://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166
 *     /src/test/tck/DoubleAdderTest.java?revision=1.8&pathrev=MAIN&view=markup
 */

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

import java.{lang => jl}

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.DoubleAdder

class DoubleAdderTest extends JSR166Test {
  import JSR166Test._

  /** default constructed initializes to zero
   */
  @Test def testConstructor(): Unit = {
    val ai = new DoubleAdder
    assertEquals(0.0, ai.sum, 0.0)
  }

  /** add adds given value to current, and sum returns current value
   */
  @Test def testAddAndSum() = {
    val ai = new DoubleAdder()
    ai.add(2.0)
    assertEquals(2.0, ai.sum(), 0.0)
    ai.add(-4.0)
    assertEquals(-2.0, ai.sum(), 0.0)
  }

  /** reset() causes subsequent sum() to return zero
   */
  @Test def testReset() = {
    val ai = new DoubleAdder()
    ai.add(2.0)
    assertEquals(2.0, ai.sum(), 0.0)
    ai.reset()
    assertEquals(0.0, ai.sum(), 0.0)
  }

  /** sumThenReset() returns sum; subsequent sum() returns zero
   */
  @Test def testSumThenReset() = {
    val ai = new DoubleAdder()
    ai.add(2.0)
    assertEquals(2.0, ai.sum(), 0.0)
    assertEquals(2.0, ai.sumThenReset(), 0.0)
    assertEquals(0.0, ai.sum(), 0.0)
  }

  // Serialization is not implemented on Scala Native
  //
  // /**
  //  * a deserialized/reserialized adder holds same value
  //  */
  // public void testSerialization() throws Exception

  /** toString returns current value.
   */
  @Test def testToString() = {
    val ai = new DoubleAdder()
    assertEquals(jl.Double.toString(0.0), ai.toString())
    ai.add(1.0)
    assertEquals(jl.Double.toString(1.0), ai.toString())
  }

  /** intValue returns current value.
   */
  @Test def testIntValue() = {
    val ai = new DoubleAdder()
    assertEquals(0, ai.intValue())
    ai.add(1.0)
    assertEquals(1, ai.intValue())
  }

  /** longValue returns current value.
   */
  @Test def testLongValue() = {
    val ai = new DoubleAdder()
    assertEquals(0L, ai.longValue())
    ai.add(1.0)
    assertEquals(1L, ai.longValue())
  }

  /** floatValue returns current value.
   */
  @Test def testFloatValue() = {
    val ai = new DoubleAdder()
    assertEquals(0.0f, ai.floatValue(), 0.0f)
    ai.add(1.0)
    assertEquals(1.0f, ai.floatValue(), 0.0f)
  }

  /** doubleValue returns current value.
   */
  @Test def testDoubleValue() = {
    val ai = new DoubleAdder()
    assertEquals(0.0, ai.doubleValue(), 0.0)
    ai.add(1.0)
    assertEquals(1.0, ai.doubleValue(), 0.0)
  }

  @throws[Throwable]
  @Test def testAddAndSumMT: Unit = {

    val incs = 1000000
    val nthreads = 4

    val pool = Executors.newCachedThreadPool()
    usingPoolCleaner(pool) { _ =>
      val a = new DoubleAdder
      val barrier = new CyclicBarrier(nthreads + 1)
      for (i <- 0 until nthreads) {
        pool.execute(new AdderTask(a, barrier, incs))
      }
      barrier.await
      barrier.await
      val total = nthreads * incs
      val sum = a.sum
      assertEquals(total.toDouble, sum, 0.0)
    }
    pool.shutdown()
  }

  final class AdderTask(
      val adder: DoubleAdder,
      val barrier: CyclicBarrier,
      val incs: Int
  ) extends Runnable {
    var result = 0.0 // Appears unnecessary; _may_ be here to trick optimizer.

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
