/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.Arrays
import java.util.concurrent.atomic.AtomicLongArray

import org.junit.Assert._
import org.junit.Test

class AtomicLongArray9Test extends JSR166Test {
  import JSR166Test._

  /** get and set for out of bound indices throw IndexOutOfBoundsException
   */
  @Test def testIndexing(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (index <- Array[Int](-1, SIZE)) {
      val j = index
      assertEachThrows(
        classOf[IndexOutOfBoundsException],
        () => aa.getPlain(j),
        () => aa.getOpaque(j),
        () => aa.getAcquire(j),
        () => aa.setPlain(j, 1),
        () => aa.setOpaque(j, 1),
        () => aa.setRelease(j, 1),
        () => aa.compareAndExchange(j, 1, 2),
        () => aa.compareAndExchangeAcquire(j, 1, 2),
        () => aa.compareAndExchangeRelease(j, 1, 2),
        () => aa.weakCompareAndSetPlain(j, 1, 2),
        () => aa.weakCompareAndSetVolatile(j, 1, 2),
        () => aa.weakCompareAndSetAcquire(j, 1, 2),
        () => aa.weakCompareAndSetRelease(j, 1, 2)
      )
    }
  }

  /** getPlain returns the last value set
   */
  @Test def testGetPlainSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getPlain(i))
      aa.set(i, 2)
      assertEquals(2, aa.getPlain(i))
      aa.set(i, -3)
      assertEquals(-3, aa.getPlain(i))
    }
  }

  /** getOpaque returns the last value set
   */
  @Test def testGetOpaqueSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getOpaque(i))
      aa.set(i, 2)
      assertEquals(2, aa.getOpaque(i))
      aa.set(i, -3)
      assertEquals(-3, aa.getOpaque(i))
    }
  }

  /** getAcquire returns the last value set
   */
  @Test def testGetAcquireSet(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.getAcquire(i))
      aa.set(i, 2)
      assertEquals(2, aa.getAcquire(i))
      aa.set(i, -3)
      assertEquals(-3, aa.getAcquire(i))
    }
  }

  /** get returns the last value setPlain
   */
  @Test def testGetSetPlain(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.setPlain(i, 1)
      assertEquals(1, aa.get(i))
      aa.setPlain(i, 2)
      assertEquals(2, aa.get(i))
      aa.setPlain(i, -3)
      assertEquals(-3, aa.get(i))
    }
  }

  /** get returns the last value setOpaque
   */
  @Test def testGetSetOpaque(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.setOpaque(i, 1)
      assertEquals(1, aa.get(i))
      aa.setOpaque(i, 2)
      assertEquals(2, aa.get(i))
      aa.setOpaque(i, -3)
      assertEquals(-3, aa.get(i))
    }
  }

  /** get returns the last value setRelease
   */
  @Test def testGetSetRelease(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.setRelease(i, 1)
      assertEquals(1, aa.get(i))
      aa.setRelease(i, 2)
      assertEquals(2, aa.get(i))
      aa.setRelease(i, -3)
      assertEquals(-3, aa.get(i))
    }
  }

  /** compareAndExchange succeeds in changing value if equal to expected else
   *  fails
   */
  @Test def testCompareAndExchange(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.compareAndExchange(i, 1, 2))
      assertEquals(2, aa.compareAndExchange(i, 2, -4))
      assertEquals(-4, aa.get(i))
      assertEquals(-4, aa.compareAndExchange(i, -5, 7))
      assertEquals(-4, aa.get(i))
      assertEquals(-4, aa.compareAndExchange(i, -4, 7))
      assertEquals(7, aa.get(i))
    }
  }

  /** compareAndExchangeAcquire succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeAcquire(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.compareAndExchangeAcquire(i, 1, 2))
      assertEquals(2, aa.compareAndExchangeAcquire(i, 2, -4))
      assertEquals(-4, aa.get(i))
      assertEquals(-4, aa.compareAndExchangeAcquire(i, -5, 7))
      assertEquals(-4, aa.get(i))
      assertEquals(-4, aa.compareAndExchangeAcquire(i, -4, 7))
      assertEquals(7, aa.get(i))
    }
  }

  /** compareAndExchangeRelease succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeRelease(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      assertEquals(1, aa.compareAndExchangeRelease(i, 1, 2))
      assertEquals(2, aa.compareAndExchangeRelease(i, 2, -4))
      assertEquals(-4, aa.get(i))
      assertEquals(-4, aa.compareAndExchangeRelease(i, -5, 7))
      assertEquals(-4, aa.get(i))
      assertEquals(-4, aa.compareAndExchangeRelease(i, -4, 7))
      assertEquals(7, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetPlain succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetPlain(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      while (!aa.weakCompareAndSetPlain(i, 1, 2)) ()
      while (!aa.weakCompareAndSetPlain(i, 2, -(4))) ()
      assertEquals(-4, aa.get(i))
      while (!aa.weakCompareAndSetPlain(i, -(4), 7)) ()
      assertEquals(7, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetVolatile succeeds in changing value when equal
   *  to expected
   */
  @Test def testWeakCompareAndSetVolatile(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      while (!aa.weakCompareAndSetVolatile(i, 1, 2)) ()
      while (!aa.weakCompareAndSetVolatile(i, 2, -(4))) ()
      assertEquals(-4, aa.get(i))
      while (!aa.weakCompareAndSetVolatile(i, -(4), 7)) ()
      assertEquals(7, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetAcquire succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetAcquire(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      while (!aa.weakCompareAndSetAcquire(i, 1, 2)) ()
      while (!aa.weakCompareAndSetAcquire(i, 2, -(4))) ()
      assertEquals(-4, aa.get(i))
      while (!aa.weakCompareAndSetAcquire(i, -(4), 7)) ()
      assertEquals(7, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetRelease succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetRelease(): Unit = {
    val aa = new AtomicLongArray(SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, 1)
      while (!aa.weakCompareAndSetRelease(i, 1, 2)) ()
      while (!aa.weakCompareAndSetRelease(i, 2, -(4))) ()
      assertEquals(-4, aa.get(i))
      while (!aa.weakCompareAndSetRelease(i, -(4), 7)) ()
      assertEquals(7, aa.get(i))
    }
  }
}
