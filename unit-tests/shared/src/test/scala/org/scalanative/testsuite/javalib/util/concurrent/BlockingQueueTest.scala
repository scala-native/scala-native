/*
 * Written by Doug Lea and Martin Buchholz with assistance from members
 * of JCP JSR-166 Expert Group and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit._
import org.junit.Assert._

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util._
import java.util.concurrent._

/** Contains "contract" tests applicable to all BlockingQueue implementations.
 */
abstract class BlockingQueueTest extends JSR166Test {
  import JSR166Test._

  /** Returns an empty instance of the implementation class. */
  protected def emptyCollection(): BlockingQueue[Any]

  /** Returns an element suitable for insertion in the collection. Override for
   *  collections with unusual element types.
   */
  protected def makeElement(i: Int) = Integer.valueOf(i)

  /** offer(null) throws NullPointerException
   */
  @Test def testOfferNull(): Unit = {
    val q = emptyCollection()
    try {
      q.offer(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** add(null) throws NullPointerException
   */
  @Test def testAddNull(): Unit = {
    val q = emptyCollection()
    try {
      q.add(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** timed offer(null) throws NullPointerException
   */
  @throws[InterruptedException]
  @Test def testTimedOfferNull(): Unit = {
    val q = emptyCollection()
    val startTime = System.nanoTime
    try {
      q.offer(null, LONG_DELAY_MS, MILLISECONDS)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
  }

  /** put(null) throws NullPointerException
   */
  @throws[InterruptedException]
  @Test def testPutNull(): Unit = {
    val q = emptyCollection()
    try {
      q.put(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** addAll(null) throws NullPointerException
   */
  @throws[InterruptedException]
  @Test def testAddAllNull(): Unit = {
    val q = emptyCollection()
    try {
      q.addAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** addAll of a collection with null elements throws NullPointerException
   */
  @Test def testAddAllNullElements(): Unit = {
    val q = emptyCollection()
    val elements = Arrays.asList(new Array[Integer](SIZE): _*)
    try {
      q.addAll(elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** toArray(null) throws NullPointerException
   */
  @Test def testToArray_NullArray(): Unit = {
    val q = emptyCollection()
    try {
      q.toArray(null.asInstanceOf[Array[AnyRef]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** drainTo(null) throws NullPointerException
   */
  @Test def testDrainToNull(): Unit = {
    val q = emptyCollection()
    try {
      q.drainTo(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** drainTo(this) throws IllegalArgumentException
   */
  @Test def testDrainToSelf(): Unit = {
    val q = emptyCollection()
    try {
      q.drainTo(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** drainTo(null, n) throws NullPointerException
   */
  @Test def testDrainToNullN(): Unit = {
    val q = emptyCollection()
    try {
      q.drainTo(null, 0)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** drainTo(this, n) throws IllegalArgumentException
   */
  @Test def testDrainToSelfN(): Unit = {
    val q = emptyCollection()
    try {
      q.drainTo(q, 0)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** drainTo(c, n) returns 0 and does nothing when n <= 0
   */
  @Test def testDrainToNonPositiveMaxElements(): Unit = {
    val q = emptyCollection()
    val ns = Array(0, -1, -42, Integer.MIN_VALUE)
    val sink = new ArrayList[Any]
    for (n <- ns) {
      assertEquals(0, q.drainTo(sink, n))
      assertTrue(sink.isEmpty)
    }
    if (q.remainingCapacity > 0) { // Not SynchronousQueue, that is
      val one = makeElement(1)
      q.add(one)
      for (n <- ns) { assertEquals(0, q.drainTo(sink, n)) }
      assertEquals(1, q.size)
      assertSame(one, q.poll())
      assertTrue(sink.isEmpty)
    }
  }

  /** timed poll before a delayed offer times out; after offer succeeds; on
   *  interruption throws
   */
  @throws[InterruptedException]
  @Test def testTimedPollWithOffer(): Unit = {
    val q = emptyCollection()
    val barrier = new CheckedBarrier(2)
    val zero = makeElement(0)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
        assertNull(q.poll(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        barrier.await
        assertSame(zero, q.poll(LONG_DELAY_MS, MILLISECONDS))
        Thread.currentThread.interrupt()
        try {
          q.poll(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        barrier.await
        try {
          q.poll(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      }
    })
    barrier.await
    val startTime = System.nanoTime
    assertTrue(q.offer(zero, LONG_DELAY_MS, MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    barrier.await
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** take() blocks interruptibly when empty
   */
  @Ignore("needs ForkJoinPool")
  @Test def testTakeFromEmptyBlocksInterruptibly(): Unit = {
    val q = emptyCollection()
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        threadStarted.countDown()
        try {
          q.take
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(threadStarted)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** take() throws InterruptedException immediately if interrupted before
   *  waiting
   */
  @Test def testTakeFromEmptyAfterInterrupt(): Unit = {
    val q = emptyCollection()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        Thread.currentThread.interrupt()
        try {
          q.take
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    awaitTermination(t)
  }

  /** timed poll() blocks interruptibly when empty
   */
  @Test def testTimedPollFromEmptyBlocksInterruptibly(): Unit = {
    val q = emptyCollection()
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        pleaseInterrupt.countDown()
        try {
          q.poll(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch { case success: InterruptedException => () }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed poll() throws InterruptedException immediately if interrupted before
   *  waiting
   */
  @Test def testTimedPollFromEmptyAfterInterrupt(): Unit = {
    val q = emptyCollection()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        Thread.currentThread.interrupt()
        try {
          q.poll(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    awaitTermination(t)
  }

  /** remove(x) removes x and returns true if present TODO: move to superclass
   *  CollectionTest.java
   */
  @Test def testRemoveElement(): Unit = {
    val q = emptyCollection()
    val size = Math.min(q.remainingCapacity, SIZE)
    val elts = new Array[AnyRef](size)
    assertFalse(q.contains(makeElement(99)))
    assertFalse(q.remove(makeElement(99)))
    checkEmpty(q)
    for (i <- 0 until size) {
      val elem = makeElement(i)
      elts(i) = elem
      q.add(elem)
    }
    var i = 1
    while (i < size) {
      for (pass <- 0 until 2) {
        assertEquals(pass == 0, q.contains(elts(i)))
        assertEquals(pass == 0, q.remove(elts(i)))
        assertFalse(q.contains(elts(i)))
        assertTrue(q.contains(elts(i - 1)))
        if (i < size - 1) assertTrue(q.contains(elts(i + 1)))
      }
      i += 2
    }
    if (size > 0) assertTrue(q.contains(elts(0)))
    i = size - 2
    while ({ i >= 0 }) {
      assertTrue(q.contains(elts(i)))
      assertFalse(q.contains(elts(i + 1)))
      assertTrue(q.remove(elts(i)))
      assertFalse(q.contains(elts(i)))
      assertFalse(q.remove(elts(i + 1)))
      assertFalse(q.contains(elts(i + 1)))

      i -= 2
    }
    checkEmpty(q)
  }

  /** For debugging. */
  def XXXXtestFails(): Unit = { fail(emptyCollection().getClass.toString) }
}
