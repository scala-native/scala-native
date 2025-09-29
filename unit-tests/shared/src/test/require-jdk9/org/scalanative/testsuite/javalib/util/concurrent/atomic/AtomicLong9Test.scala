/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicLong

import org.junit.Assert._
import org.junit.Test

class AtomicLong9Test extends JSR166Test {
  import JSR166Test._

  /** getPlain returns the last value set
   */
  @Test def testGetPlainSet(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.getPlain)
    ai.set(2)
    assertEquals(2, ai.getPlain)
    ai.set(-3)
    assertEquals(-3, ai.getPlain)
  }

  /** getOpaque returns the last value set
   */
  @Test def testGetOpaqueSet(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.getOpaque)
    ai.set(2)
    assertEquals(2, ai.getOpaque)
    ai.set(-3)
    assertEquals(-3, ai.getOpaque)
  }

  /** getAcquire returns the last value set
   */
  @Test def testGetAcquireSet(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.getAcquire)
    ai.set(2)
    assertEquals(2, ai.getAcquire)
    ai.set(-3)
    assertEquals(-3, ai.getAcquire)
  }

  /** get returns the last value setPlain
   */
  @Test def testGetSetPlain(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.get)
    ai.setPlain(2)
    assertEquals(2, ai.get)
    ai.setPlain(-3)
    assertEquals(-3, ai.get)
  }

  /** get returns the last value setOpaque
   */
  @Test def testGetSetOpaque(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.get)
    ai.setOpaque(2)
    assertEquals(2, ai.get)
    ai.setOpaque(-3)
    assertEquals(-3, ai.get)
  }

  /** get returns the last value setRelease
   */
  @Test def testGetSetRelease(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.get)
    ai.setRelease(2)
    assertEquals(2, ai.get)
    ai.setRelease(-3)
    assertEquals(-3, ai.get)
  }

  /** compareAndExchange succeeds in changing value if equal to expected else
   *  fails
   */
  @Test def testCompareAndExchange(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.compareAndExchange(1, 2))
    assertEquals(2, ai.compareAndExchange(2, -4))
    assertEquals(-4, ai.get)
    assertEquals(-4, ai.compareAndExchange(-5, 7))
    assertEquals(-4, ai.get)
    assertEquals(-4, ai.compareAndExchange(-4, 7))
    assertEquals(7, ai.get)
  }

  /** compareAndExchangeAcquire succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeAcquire(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.compareAndExchangeAcquire(1, 2))
    assertEquals(2, ai.compareAndExchangeAcquire(2, -4))
    assertEquals(-4, ai.get)
    assertEquals(-4, ai.compareAndExchangeAcquire(-5, 7))
    assertEquals(-4, ai.get)
    assertEquals(-4, ai.compareAndExchangeAcquire(-4, 7))
    assertEquals(7, ai.get)
  }

  /** compareAndExchangeRelease succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeRelease(): Unit = {
    val ai = new AtomicLong(1)
    assertEquals(1, ai.compareAndExchangeRelease(1, 2))
    assertEquals(2, ai.compareAndExchangeRelease(2, -4))
    assertEquals(-4, ai.get)
    assertEquals(-4, ai.compareAndExchangeRelease(-5, 7))
    assertEquals(-4, ai.get)
    assertEquals(-4, ai.compareAndExchangeRelease(-4, 7))
    assertEquals(7, ai.get)
  }

  /** repeated weakCompareAndSetPlain succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetPlain(): Unit = {
    val ai = new AtomicLong(1)
    while (!ai.weakCompareAndSetPlain(1, 2)) ()
    while (!ai.weakCompareAndSetPlain(2, -(4))) ()
    assertEquals(-4, ai.get)
    while (!ai.weakCompareAndSetPlain(-(4), 7)) ()
    assertEquals(7, ai.get)
  }

  /** repeated weakCompareAndSetVolatile succeeds in changing value when equal
   *  to expected
   */
  @Test def testWeakCompareAndSetVolatile(): Unit = {
    val ai = new AtomicLong(1)
    while (!ai.weakCompareAndSetVolatile(1, 2)) ()
    while (!ai.weakCompareAndSetVolatile(2, -(4))) ()
    assertEquals(-4, ai.get)
    while (!ai.weakCompareAndSetVolatile(-(4), 7)) ()
    assertEquals(7, ai.get)
  }

  /** repeated weakCompareAndSetAcquire succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetAcquire(): Unit = {
    val ai = new AtomicLong(1)
    while (!ai.weakCompareAndSetAcquire(1, 2)) ()
    while (!ai.weakCompareAndSetAcquire(2, -(4))) ()
    assertEquals(-4, ai.get)
    while (!ai.weakCompareAndSetAcquire(-(4), 7)) ()
    assertEquals(7, ai.get)
  }

  /** repeated weakCompareAndSetRelease succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetRelease(): Unit = {
    val ai = new AtomicLong(1)
    while (!ai.weakCompareAndSetRelease(1, 2)) ()
    while (!ai.weakCompareAndSetRelease(2, -(4))) ()
    assertEquals(-4, ai.get)
    while (!ai.weakCompareAndSetRelease(-(4), 7)) ()
    assertEquals(7, ai.get)
  }
}
