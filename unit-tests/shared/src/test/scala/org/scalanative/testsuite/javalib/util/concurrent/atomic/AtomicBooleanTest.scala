/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicBoolean
import org.junit.{Test, Ignore}
import org.junit.Assert._

class AtomicBooleanTest extends JSR166Test {
  import JSR166Test._

  /** constructor initializes to given value
   */
  @Test def testConstructor(): Unit = {
    assertTrue(new AtomicBoolean(true).get)
    assertFalse(new AtomicBoolean(false).get)
  }

  /** default constructed initializes to false
   */
  @Test def testConstructor2(): Unit = {
    val ai = new AtomicBoolean
    assertFalse(ai.get)
  }

  /** get returns the last value set
   */
  @Test def testGetSet(): Unit = {
    val ai = new AtomicBoolean(true)
    assertTrue(ai.get)
    ai.set(false)
    assertFalse(ai.get)
    ai.set(true)
    assertTrue(ai.get)
  }

  /** get returns the last value lazySet in same thread
   */
  @Test def testGetLazySet(): Unit = {
    val ai = new AtomicBoolean(true)
    assertTrue(ai.get)
    ai.lazySet(false)
    assertFalse(ai.get)
    ai.lazySet(true)
    assertTrue(ai.get)
  }

  /** compareAndSet succeeds in changing value if equal to expected else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val ai = new AtomicBoolean(true)
    assertTrue(ai.compareAndSet(true, false))
    assertFalse(ai.get)
    assertTrue(ai.compareAndSet(false, false))
    assertFalse(ai.get)
    assertFalse(ai.compareAndSet(true, false))
    assertFalse(ai.get)
    assertTrue(ai.compareAndSet(false, true))
    assertTrue(ai.get)
  }

  /** compareAndSet in one thread enables another waiting for value to succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val ai = new AtomicBoolean(true)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({ !ai.compareAndSet(false, true) }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(ai.compareAndSet(true, false))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
  }

  /** repeated weakCompareAndSet succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSet(): Unit = {
    val ai = new AtomicBoolean(true)
    while (!ai.weakCompareAndSet(true, false)) ()
    assertFalse(ai.get)
    while (!ai.weakCompareAndSet(false, false)) ()
    assertFalse(ai.get)
    while (!ai.weakCompareAndSet(false, true)) ()
    assertTrue(ai.get)
  }

  /** getAndSet returns previous value and sets to given value
   */
  @Test def testGetAndSet(): Unit = {
    val ai = new AtomicBoolean
    val booleans = Array(false, true)
    for (before <- booleans) {
      for (after <- booleans) {
        ai.set(before)
        assertEquals(before, ai.getAndSet(after))
        assertEquals(after, ai.get)
      }
    }
  }

  /** a deserialized/reserialized atomic holds same value
   */
  @throws[Exception]
  @Ignore("No ObjectInputStreams in Scala Native")
  @Test def testSerialization(): Unit = {
    //   val x = new AtomicBoolean
    //   val y = serialClone(x)
    //   x.set(true)
    //   val z = serialClone(x)
    //   assertTrue(x.get)
    //   assertFalse(y.get)
    //   assertTrue(z.get)
  }

  /** toString returns current value.
   */
  @Test def testToString(): Unit = {
    val ai = new AtomicBoolean
    assertEquals(java.lang.Boolean.toString(false), ai.toString)
    ai.set(true)
    assertEquals(java.lang.Boolean.toString(true), ai.toString)
  }
}
