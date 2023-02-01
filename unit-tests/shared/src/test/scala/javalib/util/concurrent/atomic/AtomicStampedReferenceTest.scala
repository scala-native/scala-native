/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicStampedReference

import org.junit.Test
import org.junit.Assert._

class AtomicStampedReferenceTest extends JSR166Test {
  import JSR166Test._

  /** constructor initializes to given reference and stamp
   */
  @Test def testConstructor(): Unit = {
    val ai = new AtomicStampedReference[Any](one, 0)
    assertSame(one, ai.getReference)
    assertEquals(0, ai.getStamp)
    val a2 = new AtomicStampedReference[Any](null, 1)
    assertNull(a2.getReference)
    assertEquals(1, a2.getStamp)
  }

  /** get returns the last values of reference and stamp set
   */
  @Test def testGetSet(): Unit = {
    val mark = new Array[Int](1)
    val ai = new AtomicStampedReference[Any](one, 0)
    assertSame(one, ai.getReference)
    assertEquals(0, ai.getStamp)
    assertSame(one, ai.get(mark))
    assertEquals(0, mark(0))
    ai.set(two, 0)
    assertSame(two, ai.getReference)
    assertEquals(0, ai.getStamp)
    assertSame(two, ai.get(mark))
    assertEquals(0, mark(0))
    ai.set(one, 1)
    assertSame(one, ai.getReference)
    assertEquals(1, ai.getStamp)
    assertSame(one, ai.get(mark))
    assertEquals(1, mark(0))
  }

  /** attemptStamp succeeds in single thread
   */
  @Test def testAttemptStamp(): Unit = {
    val mark = new Array[Int](1)
    val ai = new AtomicStampedReference[Any](one, 0)
    assertEquals(0, ai.getStamp)
    assertTrue(ai.attemptStamp(one, 1))
    assertEquals(1, ai.getStamp)
    assertSame(one, ai.get(mark))
    assertEquals(1, mark(0))
  }

  /** compareAndSet succeeds in changing values if equal to expected reference
   *  and stamp else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val mark = new Array[Int](1)
    val ai = new AtomicStampedReference[Any](one, 0)
    assertSame(one, ai.get(mark))
    assertEquals(0, ai.getStamp)
    assertEquals(0, mark(0))
    assertTrue(ai.compareAndSet(one, two, 0, 0))
    assertSame(two, ai.get(mark))
    assertEquals(0, mark(0))
    assertTrue(ai.compareAndSet(two, m3, 0, 1))
    assertSame(m3, ai.get(mark))
    assertEquals(1, mark(0))
    assertFalse(ai.compareAndSet(two, m3, 1, 1))
    assertSame(m3, ai.get(mark))
    assertEquals(1, mark(0))
  }

  /** compareAndSet in one thread enables another waiting for reference value to
   *  succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val ai = new AtomicStampedReference[Any](one, 0)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({ !ai.compareAndSet(two, three, 0, 0) }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(ai.compareAndSet(one, two, 0, 0))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertSame(three, ai.getReference)
    assertEquals(0, ai.getStamp)
  }

  /** compareAndSet in one thread enables another waiting for stamp value to
   *  succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads2(): Unit = {
    val ai = new AtomicStampedReference[Any](one, 0)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({ !ai.compareAndSet(one, one, 1, 2) }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(ai.compareAndSet(one, one, 0, 1))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertSame(one, ai.getReference)
    assertEquals(2, ai.getStamp)
  }

  /** repeated weakCompareAndSet succeeds in changing values when equal to
   *  expected
   */
  @Test def testWeakCompareAndSet(): Unit = {
    val mark = new Array[Int](1)
    val ai = new AtomicStampedReference[Any](one, 0)
    assertSame(one, ai.get(mark))
    assertEquals(0, ai.getStamp)
    assertEquals(0, mark(0))
    while (!ai.weakCompareAndSet(one, two, 0, 0)) ()
    assertSame(two, ai.get(mark))
    assertEquals(0, mark(0))
    while (!ai.weakCompareAndSet(two, m3, 0, 1)) ()
    assertSame(m3, ai.get(mark))
    assertEquals(1, mark(0))
  }
}
