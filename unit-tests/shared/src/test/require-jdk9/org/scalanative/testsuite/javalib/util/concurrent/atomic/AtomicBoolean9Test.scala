/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicBoolean

import org.junit.Test
import org.junit.Assert._

class AtomicBoolean9Test extends JSR166Test {

  /** getPlain returns the last value set
   */
  @Test def testGetPlainSet(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.getPlain)
    ai.set(false)
    assertEquals(false, ai.getPlain)
    ai.set(true)
    assertEquals(true, ai.getPlain)
  }

  /** getOpaque returns the last value set
   */
  @Test def testGetOpaqueSet(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.getOpaque)
    ai.set(false)
    assertEquals(false, ai.getOpaque)
    ai.set(true)
    assertEquals(true, ai.getOpaque)
  }

  /** getAcquire returns the last value set
   */
  @Test def testGetAcquireSet(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.getAcquire)
    ai.set(false)
    assertEquals(false, ai.getAcquire)
    ai.set(true)
    assertEquals(true, ai.getAcquire)
  }

  /** get returns the last value setPlain
   */
  @Test def testGetSetPlain(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.get)
    ai.setPlain(false)
    assertEquals(false, ai.get)
    ai.setPlain(true)
    assertEquals(true, ai.get)
  }

  /** get returns the last value setOpaque
   */
  @Test def testGetSetOpaque(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.get)
    ai.setOpaque(false)
    assertEquals(false, ai.get)
    ai.setOpaque(true)
    assertEquals(true, ai.get)
  }

  /** get returns the last value setRelease
   */
  @Test def testGetSetRelease(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.get)
    ai.setRelease(false)
    assertEquals(false, ai.get)
    ai.setRelease(true)
    assertEquals(true, ai.get)
  }

  /** compareAndExchange succeeds in changing value if equal to expected else
   *  fails
   */
  @Test def testCompareAndExchange(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.compareAndExchange(true, false))
    assertEquals(false, ai.compareAndExchange(false, false))
    assertEquals(false, ai.get)
    assertEquals(false, ai.compareAndExchange(true, true))
    assertEquals(false, ai.get)
    assertEquals(false, ai.compareAndExchange(false, true))
    assertEquals(true, ai.get)
  }

  /** compareAndExchangeAcquire succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeAcquire(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.compareAndExchangeAcquire(true, false))
    assertEquals(false, ai.compareAndExchangeAcquire(false, false))
    assertEquals(false, ai.get)
    assertEquals(false, ai.compareAndExchangeAcquire(true, true))
    assertEquals(false, ai.get)
    assertEquals(false, ai.compareAndExchangeAcquire(false, true))
    assertEquals(true, ai.get)
  }

  /** compareAndExchangeRelease succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeRelease(): Unit = {
    val ai = new AtomicBoolean(true)
    assertEquals(true, ai.compareAndExchangeRelease(true, false))
    assertEquals(false, ai.compareAndExchangeRelease(false, false))
    assertEquals(false, ai.get)
    assertEquals(false, ai.compareAndExchangeRelease(true, true))
    assertEquals(false, ai.get)
    assertEquals(false, ai.compareAndExchangeRelease(false, true))
    assertEquals(true, ai.get)
  }

  /** repeated weakCompareAndSetPlain succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetPlain(): Unit = {
    val ai = new AtomicBoolean(true)
    while (!ai.weakCompareAndSetPlain(true, false)) ()
    while (!ai.weakCompareAndSetPlain(false, false)) ()
    assertFalse(ai.get)
    while (!ai.weakCompareAndSetPlain(false, true)) ()
    assertTrue(ai.get)
  }

  /** repeated weakCompareAndSetVolatile succeeds in changing value when equal
   *  to expected
   */
  @Test def testWeakCompareAndSetVolatile(): Unit = {
    val ai = new AtomicBoolean(true)
    while (!ai.weakCompareAndSetVolatile(true, false)) ()
    while (!ai.weakCompareAndSetVolatile(false, false)) ()
    assertEquals(false, ai.get)
    while (!ai.weakCompareAndSetVolatile(false, true)) ()
    assertEquals(true, ai.get)
  }

  /** repeated weakCompareAndSetAcquire succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetAcquire(): Unit = {
    val ai = new AtomicBoolean(true)
    while (!ai.weakCompareAndSetAcquire(true, false)) ()
    while (!ai.weakCompareAndSetAcquire(false, false)) ()
    assertEquals(false, ai.get)
    while (!ai.weakCompareAndSetAcquire(false, true)) ()
    assertEquals(true, ai.get)
  }

  /** repeated weakCompareAndSetRelease succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetRelease(): Unit = {
    val ai = new AtomicBoolean(true)
    while (!ai.weakCompareAndSetRelease(true, false)) ()
    while (!ai.weakCompareAndSetRelease(false, false)) ()
    assertEquals(false, ai.get)
    while (!ai.weakCompareAndSetRelease(false, true)) ()
    assertEquals(true, ai.get)
  }
}
