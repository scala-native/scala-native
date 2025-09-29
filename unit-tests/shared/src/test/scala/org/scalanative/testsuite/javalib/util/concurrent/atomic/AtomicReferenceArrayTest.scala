/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.Arrays
import java.util.concurrent.atomic.AtomicReferenceArray

import org.junit.Assert._
import org.junit.{Ignore, Test}

class AtomicReferenceArrayTest extends JSR166Test {
  import JSR166Test._

  /** constructor creates array of given size with all elements null
   */
  @Test def testConstructor(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) { assertNull(i.toString, aa.get(i)) }
  }

  /** constructor with null array throws NPE
   */
  @Test def testConstructor2NPE(): Unit = {
    try {
      val a = null
      new AtomicReferenceArray[Integer](a)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** constructor with array is of same size and has all elements
   */
  @Test def testConstructor2(): Unit = {
    val a = Array(two, one, three, four, seven)
    val aa = new AtomicReferenceArray[Integer](a)
    assertEquals(a.length, aa.length)
    for (i <- 0 until a.length) { assertEquals(a(i), aa.get(i)) }
  }

  /** Initialize AtomicReferenceArray<Class> with SubClass[]
   */
  @Test def testConstructorSubClassArray(): Unit = {
    val a = Array[Number](two, one, three, four, seven)
    val aa = new AtomicReferenceArray[Number](a)
    assertEquals(a.length, aa.length)
    for (i <- 0 until a.length) {
      assertSame(a(i), aa.get(i))
      val x = java.lang.Long.valueOf(i)
      aa.set(i, x)
      assertSame(x, aa.get(i))
    }
  }

  /** get and set for out of bound indices throw IndexOutOfBoundsException
   */
  @deprecated @Test def testIndexing(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (index <- Array[Int](-1, SIZE)) {
      try {
        aa.get(index)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.set(index, null)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.lazySet(index, null)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.compareAndSet(index, null, null)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
      try {
        aa.weakCompareAndSet(index, null, null)
        shouldThrow()
      } catch {
        case success: IndexOutOfBoundsException =>

      }
    }
  }

  /** get returns the last value set at index
   */
  @Test def testGetSet(): Unit = {
    val aa = new AtomicReferenceArray[Any](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertSame(one, aa.get(i))
      aa.set(i, two)
      assertSame(two, aa.get(i))
      aa.set(i, m3)
      assertSame(m3, aa.get(i))
    }
  }

  /** get returns the last value lazySet at index by same thread
   */
  @Test def testGetLazySet(): Unit = {
    val aa = new AtomicReferenceArray[Any](SIZE)
    for (i <- 0 until SIZE) {
      aa.lazySet(i, one)
      assertSame(one, aa.get(i))
      aa.lazySet(i, two)
      assertSame(two, aa.get(i))
      aa.lazySet(i, m3)
      assertSame(m3, aa.get(i))
    }
  }

  /** compareAndSet succeeds in changing value if equal to expected else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val aa = new AtomicReferenceArray[Any](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertTrue(aa.compareAndSet(i, one, two))
      assertTrue(aa.compareAndSet(i, two, m4))
      assertSame(m4, aa.get(i))
      assertFalse(aa.compareAndSet(i, m5, seven))
      assertSame(m4, aa.get(i))
      assertTrue(aa.compareAndSet(i, m4, seven))
      assertSame(seven, aa.get(i))
    }
  }

  /** compareAndSet in one thread enables another waiting for value to succeed
   */
  @throws[InterruptedException]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val a = new AtomicReferenceArray[Any](1)
    a.set(0, one)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while (!a.compareAndSet(0, two, three)) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(a.compareAndSet(0, one, two))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertSame(three, a.get(0))
  }

  /** repeated weakCompareAndSet succeeds in changing value when equal to
   *  expected
   */
  @deprecated @Test def testWeakCompareAndSet(): Unit = {
    val aa = new AtomicReferenceArray[Any](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      while (!aa.weakCompareAndSet(i, one, two)) ()
      while (!aa.weakCompareAndSet(i, two, m4)) ()
      assertSame(m4, aa.get(i))
      while (!aa.weakCompareAndSet(i, m4, seven)) ()
      assertSame(seven, aa.get(i))
    }
  }

  /** getAndSet returns previous value and sets to given value at given index
   */
  @Test def testGetAndSet(): Unit = {
    val aa = new AtomicReferenceArray[Any](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertSame(one, aa.getAndSet(i, zero))
      assertSame(zero, aa.getAndSet(i, m10))
      assertSame(m10, aa.getAndSet(i, one))
    }
  }

  /** a deserialized/reserialized array holds same values in same order
   */
  @throws[Exception]
  @Ignore("No ObjectInputStreams in Scala Native")
  @Test def testSerialization(): Unit = {
    //   val x = new AtomicReferenceArray[Any](SIZE)
    //   for (i <- 0 until SIZE) { x.set(i, new Integer(-i)) }
    //   val y = serialClone(x)
    //   assertNotSame(x, y)
    //   assertEquals(x.length, y.length)
    //   for (i <- 0 until SIZE) { assertEquals(x.get(i), y.get(i)) }
  }

  /** toString returns current value.
   */
  @Test def testToString(): Unit = {
    val a = Array[Int](two, one, three, four, seven)
    val aRef: Array[Integer] = a.map(v => v: Integer)
    val aa = new AtomicReferenceArray[Integer](aRef)
    assertEquals(java.util.Arrays.toString(a), aa.toString)
  }
}
