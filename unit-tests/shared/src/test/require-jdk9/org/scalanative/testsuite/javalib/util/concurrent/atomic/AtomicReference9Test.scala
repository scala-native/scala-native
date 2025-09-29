/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Test

class AtomicReference9Test extends JSR166Test {
  import JSR166Test._

  /** getPlain returns the last value set
   */
  @Test def testGetPlainSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.getPlain)
    ai.set(two)
    assertEquals(two, ai.getPlain)
    ai.set(m3)
    assertEquals(m3, ai.getPlain)
  }

  /** getOpaque returns the last value set
   */
  @Test def testGetOpaqueSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.getOpaque)
    ai.set(two)
    assertEquals(two, ai.getOpaque)
    ai.set(m3)
    assertEquals(m3, ai.getOpaque)
  }

  /** getAcquire returns the last value set
   */
  @Test def testGetAcquireSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.getAcquire)
    ai.set(two)
    assertEquals(two, ai.getAcquire)
    ai.set(m3)
    assertEquals(m3, ai.getAcquire)
  }

  /** get returns the last value setPlain
   */
  @Test def testGetSetPlain(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.get)
    ai.setPlain(two)
    assertEquals(two, ai.get)
    ai.setPlain(m3)
    assertEquals(m3, ai.get)
  }

  /** get returns the last value setOpaque
   */
  @Test def testGetSetOpaque(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.get)
    ai.setOpaque(two)
    assertEquals(two, ai.get)
    ai.setOpaque(m3)
    assertEquals(m3, ai.get)
  }

  /** get returns the last value setRelease
   */
  @Test def testGetSetRelease(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.get)
    ai.setRelease(two)
    assertEquals(two, ai.get)
    ai.setRelease(m3)
    assertEquals(m3, ai.get)
  }

  /** compareAndExchange succeeds in changing value if equal to expected else
   *  fails
   */
  @Test def testCompareAndExchange(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.compareAndExchange(one, two))
    assertEquals(two, ai.compareAndExchange(two, m4))
    assertEquals(m4, ai.get)
    assertEquals(m4, ai.compareAndExchange(m5, seven))
    assertEquals(m4, ai.get)
    assertEquals(m4, ai.compareAndExchange(m4, seven))
    assertEquals(seven, ai.get)
  }

  /** compareAndExchangeAcquire succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeAcquire(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.compareAndExchangeAcquire(one, two))
    assertEquals(two, ai.compareAndExchangeAcquire(two, m4))
    assertEquals(m4, ai.get)
    assertEquals(m4, ai.compareAndExchangeAcquire(m5, seven))
    assertEquals(m4, ai.get)
    assertEquals(m4, ai.compareAndExchangeAcquire(m4, seven))
    assertEquals(seven, ai.get)
  }

  /** compareAndExchangeRelease succeeds in changing value if equal to expected
   *  else fails
   */
  @Test def testCompareAndExchangeRelease(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one, ai.compareAndExchangeRelease(one, two))
    assertEquals(two, ai.compareAndExchangeRelease(two, m4))
    assertEquals(m4, ai.get)
    assertEquals(m4, ai.compareAndExchangeRelease(m5, seven))
    assertEquals(m4, ai.get)
    assertEquals(m4, ai.compareAndExchangeRelease(m4, seven))
    assertEquals(seven, ai.get)
  }

  /** repeated weakCompareAndSetPlain succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetPlain(): Unit = {
    val ai = new AtomicReference[Integer](one)
    while (!ai.weakCompareAndSetPlain(one, two)) ()
    while (!ai.weakCompareAndSetPlain(two, m4)) ()
    assertEquals(m4, ai.get)
    while (!ai.weakCompareAndSetPlain(m4, seven)) ()
    assertEquals(seven, ai.get)
  }

  /** repeated weakCompareAndSetVolatile succeeds in changing value when equal
   *  to expected
   */
  @Test def testWeakCompareAndSetVolatile(): Unit = {
    val ai = new AtomicReference[Integer](one)
    while (!ai.weakCompareAndSetVolatile(one, two)) ()
    while (!ai.weakCompareAndSetVolatile(two, m4)) ()
    assertEquals(m4, ai.get)
    while (!ai.weakCompareAndSetVolatile(m4, seven)) ()
    assertEquals(seven, ai.get)
  }

  /** repeated weakCompareAndSetAcquire succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetAcquire(): Unit = {
    val ai = new AtomicReference[Integer](one)
    while (!ai.weakCompareAndSetAcquire(one, two)) ()
    while (!ai.weakCompareAndSetAcquire(two, m4)) ()
    assertEquals(m4, ai.get)
    while (!ai.weakCompareAndSetAcquire(m4, seven)) ()
    assertEquals(seven, ai.get)
  }

  /** repeated weakCompareAndSetRelease succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSetRelease(): Unit = {
    val ai = new AtomicReference[Integer](one)
    while (!ai.weakCompareAndSetRelease(one, two)) ()
    while (!ai.weakCompareAndSetRelease(two, m4)) ()
    assertEquals(m4, ai.get)
    while (!ai.weakCompareAndSetRelease(m4, seven)) ()
    assertEquals(seven, ai.get)
  }
}
