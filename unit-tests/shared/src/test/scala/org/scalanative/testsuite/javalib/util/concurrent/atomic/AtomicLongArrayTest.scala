/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicLongArray
import java.util.Arrays

import org.junit.{Test, Ignore}
import org.junit.Assert.*

class AtomicLongArrayTest extends JSR166Test {
  import JSR166Test.*

  /** constructor creates array of given size with all elements zero
   */
  @Test def testConstructor(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) { assertEquals(0, aa.get(i)) }
  }

  /** constructor with null array throws NPE
   */
  @Test def testConstructor2NPE(): Unit = {
    try {
      val a = null
      new AtomicLongArray(a)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** constructor with array is of same size and has all elements
   */
  @Test def testConstructor2(): Unit = {
    val a = Array(17L, 3L, -42L, 99L, -7L)
    val aa = new AtomicLongArray(a)
    assertEquals(a.length, aa.length)
    for (i <- 0 until a.length) { assertEquals(a(i), aa.get(i)) }
  }

  /** get and set for out of bound indices throw IndexOutOfBoundsException
   */
  @deprecated @Test def testIndexing(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (index <- Array[Int](-1, SIZE)) {
      try {
        aa.get(index)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.set(index, 1)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.lazySet(index, 1)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.compareAndSet(index, 1, 2)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.weakCompareAndSet(index, 1, 2)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.getAndAdd(index, 1)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.addAndGet(index, 1)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
    }
  }

  /** get returns the last value set at index
   */
  @Test def testGetSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.get(i))
      aa.set(i, 2)
      assertEquals(2, aa.get(i))
      aa.set(i, -3)
      assertEquals(-3, aa.get(i))
    }
  }

  /** get returns the last value lazySet at index by same thread
   */
  @Test def testGetLazySet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.lazySet(i, 1)
      assertEquals(1, aa.get(i))
      aa.lazySet(i, 2)
      assertEquals(2, aa.get(i))
      aa.lazySet(i, -3)
      assertEquals(-3, aa.get(i))
    }
  }

  /** compareAndSet succeeds in changing value if equal to expected else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertTrue(aa.compareAndSet(i, 1, 2))
      assertTrue(aa.compareAndSet(i, 2, -4))
      assertEquals(-4, aa.get(i))
      assertFalse(aa.compareAndSet(i, -5, 7))
      assertEquals(-4, aa.get(i))
      assertTrue(aa.compareAndSet(i, -4, 7))
      assertEquals(7, aa.get(i))
    }
  }

  /** compareAndSet in one thread enables another waiting for value to succeed
   */
  @throws[InterruptedException]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val a = new AtomicLongArray(1)
    a.set(0, 1)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({ !a.compareAndSet(0, 2, 3) }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(a.compareAndSet(0, 1, 2))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertEquals(3, a.get(0))
  }

  /** repeated weakCompareAndSet succeeds in changing value when equal to
   *  expected
   */
  @deprecated @Test def testWeakCompareAndSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      while (!aa.weakCompareAndSet(i, 1, 2)) ()
      while (!aa.weakCompareAndSet(i, 2, -(4))) ()
      assertEquals(-4, aa.get(i))
      while (!aa.weakCompareAndSet(i, -(4), 7)) ()
      assertEquals(7, aa.get(i))
    }
  }

  /** getAndSet returns previous value and sets to given value at given index
   */
  @Test def testGetAndSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getAndSet(i, 0))
      assertEquals(0, aa.getAndSet(i, -10))
      assertEquals(-10, aa.getAndSet(i, 1))
    }
  }

  /** getAndAdd returns previous value and adds given value
   */
  @Test def testGetAndAdd(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getAndAdd(i, 2))
      assertEquals(3, aa.get(i))
      assertEquals(3, aa.getAndAdd(i, -4))
      assertEquals(-1, aa.get(i))
    }
  }

  /** getAndDecrement returns previous value and decrements
   */
  @Test def testGetAndDecrement(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getAndDecrement(i))
      assertEquals(0, aa.getAndDecrement(i))
      assertEquals(-1, aa.getAndDecrement(i))
    }
  }

  /** getAndIncrement returns previous value and increments
   */
  @Test def testGetAndIncrement(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getAndIncrement(i))
      assertEquals(2, aa.get(i))
      aa.set(i, -2)
      assertEquals(-2, aa.getAndIncrement(i))
      assertEquals(-1, aa.getAndIncrement(i))
      assertEquals(0, aa.getAndIncrement(i))
      assertEquals(1, aa.get(i))
    }
  }

  /** addAndGet adds given value to current, and returns current value
   */
  @Test def testAddAndGet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(3, aa.addAndGet(i, 2))
      assertEquals(3, aa.get(i))
      assertEquals(-1, aa.addAndGet(i, -4))
      assertEquals(-1, aa.get(i))
    }
  }

  /** decrementAndGet decrements and returns current value
   */
  @Test def testDecrementAndGet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(0, aa.decrementAndGet(i))
      assertEquals(-1, aa.decrementAndGet(i))
      assertEquals(-2, aa.decrementAndGet(i))
      assertEquals(-2, aa.get(i))
    }
  }

  /** incrementAndGet increments and returns current value
   */
  @Test def testIncrementAndGet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(2, aa.incrementAndGet(i))
      assertEquals(2, aa.get(i))
      aa.set(i, -2)
      assertEquals(-1, aa.incrementAndGet(i))
      assertEquals(0, aa.incrementAndGet(i))
      assertEquals(1, aa.incrementAndGet(i))
      assertEquals(1, aa.get(i))
    }
  }
  class Counter(val aa: AtomicLongArray) extends CheckedRunnable {
    var decs = 0
    override def realRun(): Unit = {
      @annotation.tailrec
      def loop(): Unit = {
        var done = true
        for (i <- 0 until aa.length) {
          val v = aa.get(i)
          assertTrue(v >= 0)
          if (v != 0) {
            done = false
            if (aa.compareAndSet(i, v, v - 1)) decs += 1
          }
        }
        if (!done) loop()
      }
      loop()
    }
  }

  /** Multiple threads using same array of counters successfully update a number
   *  of times equal to total count
   */
  @throws[InterruptedException]
  @Test def testCountingInMultipleThreads(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    val countdown = 10000
    for (i <- 0 until SIZE) { aa.set(i, countdown) }
    val c1 = new Counter(aa)
    val c2 = new Counter(aa)
    val t1 = newStartedThread(c1)
    val t2 = newStartedThread(c2)
    t1.join()
    t2.join()
    assertEquals(c1.decs + c2.decs, SIZE * countdown)
  }

  /** a deserialized/reserialized array holds same values in same order
   */
  @throws[Exception]
  @Ignore("No ObjectInputStreams in Scala Native")
  @Test def testSerialization(): Unit = {
    //   val x = new AtomicLongArray(SIZE)
    //   for (i <- 0 until SIZE) { x.set(i, -i) }
    //   val y = serialClone(x)
    //   assertNotSame(x, y)
    //   assertEquals(x.length, y.length)
    //   for (i <- 0 until SIZE) { assertEquals(x.get(i), y.get(i)) }
  }

  /** toString returns current value.
   */
  @Test def testToString(): Unit = {
    val a = Array[Long](17, 3, -42, 99, -7)
    val aa = new AtomicLongArray(a)
    assertEquals(Arrays.toString(a), aa.toString)
  }
}
