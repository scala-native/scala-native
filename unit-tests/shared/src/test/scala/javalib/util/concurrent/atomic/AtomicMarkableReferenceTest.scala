/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicMarkableReference

import org.junit.Test
import org.junit.Assert._

class AtomicMarkableReferenceTest extends JSR166Test {
  import JSR166Test._

  /** constructor initializes to given reference and mark
   */
  @Test def testConstructor(): Unit = {
    val ai = new AtomicMarkableReference[Any](one, false)
    assertSame(one, ai.getReference)
    assertFalse(ai.isMarked)
    val a2 = new AtomicMarkableReference[Any](null, true)
    assertNull(a2.getReference)
    assertTrue(a2.isMarked)
  }

  /** get returns the last values of reference and mark set
   */
  @Test def testGetSet(): Unit = {
    val mark = new Array[Boolean](1)
    val ai = new AtomicMarkableReference[Any](one, false)
    assertSame(one, ai.getReference)
    assertFalse(ai.isMarked)
    assertSame(one, ai.get(mark))
    assertFalse(mark(0))
    ai.set(two, false)
    assertSame(two, ai.getReference)
    assertFalse(ai.isMarked)
    assertSame(two, ai.get(mark))
    assertFalse(mark(0))
    ai.set(one, true)
    assertSame(one, ai.getReference)
    assertTrue(ai.isMarked)
    assertSame(one, ai.get(mark))
    assertTrue(mark(0))
  }

  /** attemptMark succeeds in single thread
   */
  @Test def testAttemptMark(): Unit = {
    val mark = new Array[Boolean](1)
    val ai = new AtomicMarkableReference[Any](one, false)
    assertFalse(ai.isMarked)
    assertTrue(ai.attemptMark(one, true))
    assertTrue(ai.isMarked)
    assertSame(one, ai.get(mark))
    assertTrue(mark(0))
  }

  /** compareAndSet succeeds in changing values if equal to expected reference
   *  and mark else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val mark = new Array[Boolean](1)
    val ai = new AtomicMarkableReference[Any](one, false)
    assertSame(one, ai.get(mark))
    assertFalse(ai.isMarked)
    assertFalse(mark(0))
    assertTrue(ai.compareAndSet(one, two, false, false))
    assertSame(two, ai.get(mark))
    assertFalse(mark(0))
    assertTrue(ai.compareAndSet(two, m3, false, true))
    assertSame(m3, ai.get(mark))
    assertTrue(mark(0))
    assertFalse(ai.compareAndSet(two, m3, true, true))
    assertSame(m3, ai.get(mark))
    assertTrue(mark(0))
  }

  /** compareAndSet in one thread enables another waiting for reference value to
   *  succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val ai = new AtomicMarkableReference[Any](one, false)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({
          !ai.compareAndSet(two, three, false, false)
        }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(ai.compareAndSet(one, two, false, false))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertSame(three, ai.getReference)
    assertFalse(ai.isMarked)
  }

  /** compareAndSet in one thread enables another waiting for mark value to
   *  succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads2(): Unit = {
    val ai = new AtomicMarkableReference[Any](one, false)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while ({
          !ai.compareAndSet(one, one, true, false)
        }) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(ai.compareAndSet(one, one, false, true))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertSame(one, ai.getReference)
    assertFalse(ai.isMarked)
  }

  /** repeated weakCompareAndSet succeeds in changing values when equal to
   *  expected
   */
  @Test def testWeakCompareAndSet(): Unit = {
    val mark = new Array[Boolean](1)
    val ai = new AtomicMarkableReference[Any](one, false)
    assertSame(one, ai.get(mark))
    assertFalse(ai.isMarked())
    assertFalse(mark(0))
    while ({
      !ai.weakCompareAndSet(one, two, false, false)
    }) ()
    assertSame(two, ai.get(mark))
    assertFalse(mark(0))
    while ({
      !ai.weakCompareAndSet(two, m3, false, true)
    }) ()
    assertSame(m3, ai.get(mark))
    assertTrue(mark(0))
  }
}
