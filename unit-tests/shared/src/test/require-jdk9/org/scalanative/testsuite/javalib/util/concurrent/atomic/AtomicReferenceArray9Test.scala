/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicReferenceArray

import org.junit.Assert._
import org.junit.Test

class AtomicReferenceArray9Test extends JSR166Test {
  import JSR166Test._

  /** get and set for out of bound indices throw IndexOutOfBoundsException
   */
  @Test def testIndexing(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (index <- Array[Int](-1, SIZE)) {
      val j = index
      assertEachThrows(
        classOf[IndexOutOfBoundsException],
        () => aa.getPlain(j),
        () => aa.getOpaque(j),
        () => aa.getAcquire(j),
        () => aa.setPlain(j, null),
        () => aa.setOpaque(j, null),
        () => aa.setRelease(j, null),
        () => aa.compareAndExchange(j, null, null),
        () => aa.compareAndExchangeAcquire(j, null, null),
        () => aa.compareAndExchangeRelease(j, null, null),
        () => aa.weakCompareAndSetPlain(j, null, null),
        () => aa.weakCompareAndSetVolatile(j, null, null),
        () => aa.weakCompareAndSetAcquire(j, null, null),
        () => aa.weakCompareAndSetRelease(j, null, null)
      )
    }
  }

  /** getPlain returns the last value set
   */
  @Test def testGetPlainSet(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertEquals(one, aa.getPlain(i))
      aa.set(i, two)
      assertEquals(two, aa.getPlain(i))
      aa.set(i, m3)
      assertEquals(m3, aa.getPlain(i))
    }
  }

  /** getOpaque returns the last value set
   */
  @Test def testGetOpaqueSet(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertEquals(one, aa.getOpaque(i))
      aa.set(i, two)
      assertEquals(two, aa.getOpaque(i))
      aa.set(i, m3)
      assertEquals(m3, aa.getOpaque(i))
    }
  }

  /** getAcquire returns the last value set
   */
  @Test def testGetAcquireSet(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertEquals(one, aa.getAcquire(i))
      aa.set(i, two)
      assertEquals(two, aa.getAcquire(i))
      aa.set(i, m3)
      assertEquals(m3, aa.getAcquire(i))
    }
  }

  /** get returns the last value setPlain
   */
  @Test def testGetSetPlain(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.setPlain(i, one)
      assertEquals(one, aa.get(i))
      aa.setPlain(i, two)
      assertEquals(two, aa.get(i))
      aa.setPlain(i, m3)
      assertEquals(m3, aa.get(i))
    }
  }

  /** get returns the last value setOpaque
   */
  @Test def testGetSetOpaque(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.setOpaque(i, one)
      assertEquals(one, aa.get(i))
      aa.setOpaque(i, two)
      assertEquals(two, aa.get(i))
      aa.setOpaque(i, m3)
      assertEquals(m3, aa.get(i))
    }
  }

  /** get returns the last value setRelease
   */
  @Test def testGetSetRelease(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.setRelease(i, one)
      assertEquals(one, aa.get(i))
      aa.setRelease(i, two)
      assertEquals(two, aa.get(i))
      aa.setRelease(i, m3)
      assertEquals(m3, aa.get(i))
    }
  }

  /** compareAndExchange succeeds in changing value if equal to expected else
   *  fails
   */
  @Test def testCompareAndExchange(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertEquals(one, aa.compareAndExchange(i, one, two))
      assertEquals(two, aa.compareAndExchange(i, two, m4))
      assertEquals(m4, aa.get(i))
      assertEquals(m4, aa.compareAndExchange(i, m5, seven))
      assertEquals(m4, aa.get(i))
      assertEquals(m4, aa.compareAndExchange(i, m4, seven))
      assertEquals(seven, aa.get(i))
    }
  }

  /** compareAndExchangeAcquire succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeAcquire(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertEquals(one, aa.compareAndExchangeAcquire(i, one, two))
      assertEquals(two, aa.compareAndExchangeAcquire(i, two, m4))
      assertEquals(m4, aa.get(i))
      assertEquals(m4, aa.compareAndExchangeAcquire(i, m5, seven))
      assertEquals(m4, aa.get(i))
      assertEquals(m4, aa.compareAndExchangeAcquire(i, m4, seven))
      assertEquals(seven, aa.get(i))
    }
  }

  /** compareAndExchangeRelease succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeRelease(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      assertEquals(one, aa.compareAndExchangeRelease(i, one, two))
      assertEquals(two, aa.compareAndExchangeRelease(i, two, m4))
      assertEquals(m4, aa.get(i))
      assertEquals(m4, aa.compareAndExchangeRelease(i, m5, seven))
      assertEquals(m4, aa.get(i))
      assertEquals(m4, aa.compareAndExchangeRelease(i, m4, seven))
      assertEquals(seven, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetPlain succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetPlain(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      while (!aa.weakCompareAndSetPlain(i, one, two)) ()
      while (!aa.weakCompareAndSetPlain(i, two, m4)) ()
      assertEquals(m4, aa.get(i))
      while (!aa.weakCompareAndSetPlain(i, m4, seven)) ()
      assertEquals(seven, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetVolatile succeeds in changing value when equal
   *  to expected
   */
  @Test def testWeakCompareAndSetVolatile(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      while (!aa.weakCompareAndSetVolatile(i, one, two)) ()
      while (!aa.weakCompareAndSetVolatile(i, two, m4)) ()
      assertEquals(m4, aa.get(i))
      while (!aa.weakCompareAndSetVolatile(i, m4, seven)) ()
      assertEquals(seven, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetAcquire succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetAcquire(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      while (!aa.weakCompareAndSetAcquire(i, one, two)) ()
      while (!aa.weakCompareAndSetAcquire(i, two, m4)) ()
      assertEquals(m4, aa.get(i))
      while (!aa.weakCompareAndSetAcquire(i, m4, seven)) ()
      assertEquals(seven, aa.get(i))
    }
  }

  /** repeated weakCompareAndSetRelease succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetRelease(): Unit = {
    val aa =
      new AtomicReferenceArray[Integer](SIZE)
    for (i <- 0 until SIZE) {
      aa.set(i, one)
      while (!aa.weakCompareAndSetRelease(i, one, two)) ()
      while (!aa.weakCompareAndSetRelease(i, two, m4)) ()
      assertEquals(m4, aa.get(i))
      while (!aa.weakCompareAndSetRelease(i, m4, seven)) ()
      assertEquals(seven, aa.get(i))
    }
  }
}
