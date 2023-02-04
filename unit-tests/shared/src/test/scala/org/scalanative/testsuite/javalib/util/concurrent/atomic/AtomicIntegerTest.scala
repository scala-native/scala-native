/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicInteger

import org.junit.{Test, Ignore}
import org.junit.Assert._

class AtomicIntegerTest extends JSR166Test {
  final val VALUES =
    Array(Integer.MIN_VALUE, -1, 0, 1, 42, Integer.MAX_VALUE)

  /** constructor initializes to given value
   */
  @Test def testConstructor(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.get)
  }

  /** default constructed initializes to zero
   */
  @Test def testConstructor2(): Unit = {
    val ai = new AtomicInteger
    assertEquals(0, ai.get)
  }

  /** get returns the last value set
   */
  @Test def testGetSet(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.get)
    ai.set(2)
    assertEquals(2, ai.get)
    ai.set(-3)
    assertEquals(-3, ai.get)
  }

  /** get returns the last value lazySet in same thread
   */
  @Test def testGetLazySet(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.get)
    ai.lazySet(2)
    assertEquals(2, ai.get)
    ai.lazySet(-3)
    assertEquals(-3, ai.get)
  }

  /** compareAndSet succeeds in changing value if equal to expected else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val ai = new AtomicInteger(1)
    assertTrue(ai.compareAndSet(1, 2))
    assertTrue(ai.compareAndSet(2, -4))
    assertEquals(-4, ai.get)
    assertFalse(ai.compareAndSet(-5, 7))
    assertEquals(-4, ai.get)
    assertTrue(ai.compareAndSet(-4, 7))
    assertEquals(7, ai.get)
  }

  /** compareAndSet in one thread enables another waiting for value to succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val ai = new AtomicInteger(1)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({ !ai.compareAndSet(2, 3) }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(ai.compareAndSet(1, 2))
    t.join(JSR166Test.LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertEquals(3, ai.get)
  }

  /** repeated weakCompareAndSet succeeds in changing value when equal to
   *  expected
   */
  @deprecated @Test def testWeakCompareAndSet(): Unit = {
    val ai = new AtomicInteger(1)
    while (!ai.weakCompareAndSet(1, 2)) ()
    while (!ai.weakCompareAndSet(2, -(4))) ()
    assertEquals(-4, ai.get)
    while (!ai.weakCompareAndSet(-(4), 7)) ()
    assertEquals(7, ai.get)
  }

  /** getAndSet returns previous value and sets to given value
   */
  @Test def testGetAndSet(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.getAndSet(0))
    assertEquals(0, ai.getAndSet(-10))
    assertEquals(-10, ai.getAndSet(1))
  }

  /** getAndAdd returns previous value and adds given value
   */
  @Test def testGetAndAdd(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.getAndAdd(2))
    assertEquals(3, ai.get)
    assertEquals(3, ai.getAndAdd(-4))
    assertEquals(-1, ai.get)
  }

  /** getAndDecrement returns previous value and decrements
   */
  @Test def testGetAndDecrement(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.getAndDecrement)
    assertEquals(0, ai.getAndDecrement)
    assertEquals(-1, ai.getAndDecrement)
  }

  /** getAndIncrement returns previous value and increments
   */
  @Test def testGetAndIncrement(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(1, ai.getAndIncrement)
    assertEquals(2, ai.get)
    ai.set(-2)
    assertEquals(-2, ai.getAndIncrement)
    assertEquals(-1, ai.getAndIncrement)
    assertEquals(0, ai.getAndIncrement)
    assertEquals(1, ai.get)
  }

  /** addAndGet adds given value to current, and returns current value
   */
  @Test def testAddAndGet(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(3, ai.addAndGet(2))
    assertEquals(3, ai.get)
    assertEquals(-1, ai.addAndGet(-4))
    assertEquals(-1, ai.get)
  }

  /** decrementAndGet decrements and returns current value
   */
  @Test def testDecrementAndGet(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(0, ai.decrementAndGet)
    assertEquals(-1, ai.decrementAndGet)
    assertEquals(-2, ai.decrementAndGet)
    assertEquals(-2, ai.get)
  }

  /** incrementAndGet increments and returns current value
   */
  @Test def testIncrementAndGet(): Unit = {
    val ai = new AtomicInteger(1)
    assertEquals(2, ai.incrementAndGet)
    assertEquals(2, ai.get)
    ai.set(-2)
    assertEquals(-1, ai.incrementAndGet)
    assertEquals(0, ai.incrementAndGet)
    assertEquals(1, ai.incrementAndGet)
    assertEquals(1, ai.get)
  }

  /** a deserialized/reserialized atomic holds same value
   */
  @throws[Exception]
  @Ignore("No ObjectInputStreams in Scala Native")
  @Test def testSerialization(): Unit = {
    //   val x = new AtomicInteger
    //   val y = serialClone(x)
    //   assertNotSame(x, y)
    //   x.set(22)
    //   val z = serialClone(x)
    //   assertEquals(22, x.get)
    //   assertEquals(0, y.get)
    //   assertEquals(22, z.get)
  }

  /** toString returns current value.
   */
  @Test def testToString(): Unit = {
    val ai = new AtomicInteger
    assertEquals("0", ai.toString)
    for (x <- VALUES) {
      ai.set(x)
      assertEquals(Integer.toString(x), ai.toString)
    }
  }

  /** intValue returns current value.
   */
  @Test def testIntValue(): Unit = {
    val ai = new AtomicInteger
    assertEquals(0, ai.intValue)
    for (x <- VALUES) {
      ai.set(x)
      assertEquals(x, ai.intValue)
    }
  }

  /** longValue returns current value.
   */
  @Test def testLongValue(): Unit = {
    val ai = new AtomicInteger
    assertEquals(0L, ai.longValue)
    for (x <- VALUES) {
      ai.set(x)
      assertEquals(x.toLong, ai.longValue)
    }
  }

  /** floatValue returns current value.
   */
  @Test def testFloatValue(): Unit = {
    val ai = new AtomicInteger
    assertEquals(0.0f, ai.floatValue, 0.00000001)
    for (x <- VALUES) {
      ai.set(x)
      assertEquals(x.toFloat, ai.floatValue, 0.00000001)
    }
  }

  /** doubleValue returns current value.
   */
  @Test def testDoubleValue(): Unit = {
    val ai = new AtomicInteger
    assertEquals(0.0d, ai.doubleValue, 0.0000001)
    for (x <- VALUES) {
      ai.set(x)
      assertEquals(x.toDouble, ai.doubleValue, 0.0000001)
    }
  }
}
