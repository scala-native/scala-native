/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.{Test, Ignore}

import JSR166Test._

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util._
import java.util.concurrent._
import java.util.concurrent.LinkedTransferQueue
import scala.collection.mutable.ArrayBuffer

class LinkedTransferQueueTest extends JSR166Test {
  @Test def testConstructor1() = {
    mustEqual(0, new LinkedTransferQueue().size())
    assertTrue(new LinkedTransferQueue().isEmpty())
  }

  /** Initializing constructor with null collection throws NullPointerException
   */
  @Test def testConstructor2() = {
    try {
      new LinkedTransferQueue(null)
      shouldThrow()
    } catch {
      case _: NullPointerException => {}
    }
  }

  @Test def testConstructor3() = {
    val elements: java.util.Collection[Item] =
      Arrays.asList(null)

    try {
      new LinkedTransferQueue(elements)
      shouldThrow()
    } catch {
      case _: NullPointerException => {}
    }
  }

  @Test def testConstructor4() = {
    val elements = Arrays.asList(new Item(zero), null)
    try {
      new LinkedTransferQueue(elements)
      shouldThrow()
    } catch {
      case _: NullPointerException => {}
    }
  }

  @Test def testConstructor5() = {
    val items = defaultItems
    val intList = Arrays.asList(items: _*)
    val q = new LinkedTransferQueue(intList)
    mustEqual(q.size(), intList.size())
    mustEqual(q.toString(), intList.toString())
    assertTrue(Arrays.equals(q.toArray(), intList.toArray()))
    assertTrue(
      Arrays.equals(
        q.toArray(new Array[Object](0)),
        intList.toArray(new Array[Object](0))
      )
    );
    assertTrue(
      Arrays.equals(
        q.toArray(new Array[Object](SIZE)),
        intList.toArray(new Array[Object](SIZE))
      )
    );
    for (i <- 0 until SIZE) {
      mustEqual(items(i), q.poll());
    }
  }

  @Test def testRemainingCapacity() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(Integer.MAX_VALUE, q.remainingCapacity())
      mustEqual(SIZE - i, q.size())
      mustEqual(i, q.remove())
    }
    for (i <- 0 until SIZE) {
      mustEqual(Integer.MAX_VALUE, q.remainingCapacity())
      mustEqual(i, q.size())
      mustAdd(q, i)
    }
  }

  /** addAll(this) throws IllegalArgumentException
   */
  @Test def testAddAllSelf() = {
    val q = populatedQueue(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case _: IllegalArgumentException => {}
    }
  }

  /** addAll of a collection with any null elements throws NullPointerException
   *  after possibly adding some elements
   */
  @Test def testAddAll3() = {
    val q = new LinkedTransferQueue[Item]()
    val items = new Array[Item](2); items(0) = new Item(zero)
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow();
    } catch {
      case _: NullPointerException => {}
    }
  }

  /** Queue contains all elements, in traversal order, of successful addAll
   */
  @Test def testAddAll5() = {
    val empty = new Array[Item](0)
    val items = defaultItems
    val q = new LinkedTransferQueue[Item]()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    for (i <- 0 until SIZE) {
      mustEqual(items(i), q.poll())
    }
  }

  /** all elements successfully put are contained
   */
  @Test def testPut() = {
    val q = new LinkedTransferQueue[Item]()
    val items = defaultItems
    for (i <- 0 until SIZE) {
      mustEqual(i, q.size())
      q.put(items(i))
      mustContain(q, items(i))
    }
  }

  /** take retrieves elements in FIFO order
   */
  @Test def testTake() = {
    val q = populatedQueue(SIZE);
    for (i <- 0 until SIZE) {
      mustEqual(i, q.take())
    }
  }

  /** take removes existing elements until empty, then blocks interruptibly
   */
  @Test def testBlockingTake() = {
    val q = populatedQueue(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        for (i <- 0 until SIZE) mustEqual(i, q.take())

        Thread.currentThread().interrupt()
        try {
          q.take()
          shouldThrow()
        } catch {
          case _: InterruptedException => {}
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.take()
          shouldThrow()
        } catch {
          case _: InterruptedException => {}
        }
        assertFalse(Thread.interrupted())
      }
    });

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** poll succeeds unless empty
   */
  @Test def testPoll() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.poll())
    }
    assertNull(q.poll())
    checkEmpty(q)
  }

  /** timed poll with zero timeout succeeds when non-empty, else times out
   */
  @Test def testTimedPoll0() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.poll(0, MILLISECONDS))
    }
    assertNull(q.poll(0, MILLISECONDS))
    checkEmpty(q)
  }

  /** timed poll with nonzero timeout succeeds when non-empty, else times out
   */
  @Test def testTimedPoll() = {
    val q = populatedQueue(SIZE)
    var startTime = System.nanoTime()
    for (i <- 0 until SIZE)
      mustEqual(i, q.poll(LONG_DELAY_MS, MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)

    startTime = System.nanoTime()
    assertNull(q.poll(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    checkEmpty(q)
  }

  /** Interrupted timed poll throws InterruptedException instead of returning
   *  timeout status
   */
  @Test def testInterruptedTimedPoll() = {
    val q = populatedQueue(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        for (i <- 0 until SIZE)
          mustEqual(i, q.poll(LONG_DELAY_MS, MILLISECONDS))

        Thread.currentThread().interrupt()
        try {
          q.poll(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException => {}
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.poll(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException => {}
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
    checkEmpty(q)
  }

  /** timed poll after thread interrupted throws InterruptedException instead of
   *  returning timeout status
   */
  @Test def testTimedPollAfterInterrupt() = {
    val q = populatedQueue(SIZE)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        Thread.currentThread().interrupt()
        for (i <- 0 until SIZE)
          mustEqual(i, q.poll(randomTimeout(), randomTimeUnit()))
        try {
          q.poll(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException => {}
        }
        assertFalse(Thread.interrupted())
      }
    })

    awaitTermination(t)
    checkEmpty(q)
  }

  /** peek returns next element, or null if empty
   */
  @Test def testPeek() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.peek())
      mustEqual(i, q.poll())
      assertTrue(
        q.peek() == null ||
        i != q.peek().value
      )
    }
    assertNull(q.peek())
    checkEmpty(q)
  }

  /** element returns next element, or throws NoSuchElementException if empty
   */
  @Test def testElement() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.element())
      mustEqual(i, q.poll())
    }
    try {
      q.element()
      shouldThrow()
    } catch {
      case _: NoSuchElementException => {}
    }
    checkEmpty(q)
  }

  /** remove removes next element, or throws NoSuchElementException if empty
   */
  @Test def testRemove() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.remove())
    }
    try {
      q.remove()
      shouldThrow()
    } catch {
      case _: NoSuchElementException => {}
    }
    checkEmpty(q)
  }

  /** An add following remove(x) succeeds
   */
  @Test def testRemoveElementAndAdd() = {
    val q = LinkedTransferQueue[Item]()
    mustAdd(q, one)
    mustAdd(q, two)
    mustRemove(q, one)
    mustRemove(q, two)
    mustAdd(q, three)
    mustEqual(q.take(), itemFor(three))
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains() = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustContain(q, i)
      mustEqual(i, q.poll())
      mustNotContain(q, i)
    }
  }

  /** clear removes all elements
   */
  @Test def testClear() = {
    val q = populatedQueue(SIZE)
    q.clear()
    checkEmpty(q)
    mustEqual(Integer.MAX_VALUE, q.remainingCapacity())
    q.add(itemFor(one))
    assertFalse(q.isEmpty())
    mustEqual(1, q.size())
    mustContain(q, one)
    q.clear()
    checkEmpty(q)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll() = {
    val q = populatedQueue(SIZE)
    val p = new LinkedTransferQueue[Item]()
    for (i <- 0 until SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if changed
   */
  @Test def testRetainAll() = {
    val q = populatedQueue(SIZE)
    val p = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) {
        assertFalse(changed)
      } else {
        assertTrue(changed)
      }
      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size())
      p.remove()
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testRemoveAll() = {
    for (i <- 1 until SIZE) {
      val q = populatedQueue(SIZE)
      val p = populatedQueue(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size())
      for (j <- 0 until i) {
        mustNotContain(q, p.remove())
      }
    }
  }

  /** toArray() contains all elements in FIFO order
   */
  @Test def testToArray() = {
    val q = populatedQueue(SIZE)
    val a = q.toArray()
    assertSame(classOf[Array[Object]], a.getClass)
    for (o <- a)
      assertSame(o, q.poll())
    assertTrue(q.isEmpty())
  }

  /** toArray(a) contains all elements in FIFO order //
   */
  @Test def testToArray2() = {
    val q = populatedQueue(SIZE)
    val items = new Array[Item](SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    for (o <- items)
      assertSame(o, q.poll())
    assertTrue(q.isEmpty())
  }

  /** toArray(incompatible array type) throws ArrayStoreException
   *
   *  We don't have this, because SN doesn't yet do runtime variance check.
   */
  // @Test def testToArray_incompatibleArrayType() = {
  //   val q = populatedQueue(SIZE)
  //   try {
  //     val ss: Array[String] = q.toArray(new Array[String](10))
  //     shouldThrow()
  //   } catch {
  //     case _: ArrayStoreException => {}
  //   }
  // }

  /** iterator iterates through all elements
   */
  @Test def testIterator() = {
    val q = populatedQueue(SIZE)
    var it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)

    it = q.iterator()
    i = 0
    while (it.hasNext()) {
      mustEqual(it.next(), q.take())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements
   */
  @Test def testEmptyIterator() = {
    assertIteratorExhausted(new LinkedTransferQueue[Item]().iterator())
  }

  /** iterator.remove() removes current element
   */
  @Test def testIteratorRemove() = {
    val q = new LinkedTransferQueue[Item]()
    q.add(itemFor(two))
    q.add(itemFor(one))
    q.add(itemFor(three))

    var it = q.iterator()
    it.next()
    it.remove()

    it = q.iterator()
    assertSame(it.next(), itemFor(one))
    assertSame(it.next(), itemFor(three))
    assertFalse(it.hasNext())
  }

  /** iterator ordering is FIFO
   */
  @Test def testIteratorOrdering() = {
    val q = new LinkedTransferQueue[Item]()
    mustEqual(Integer.MAX_VALUE, q.remainingCapacity())
    q.add(itemFor(one))
    q.add(itemFor(two))
    q.add(itemFor(three))
    mustEqual(Integer.MAX_VALUE, q.remainingCapacity())
    var k = 0
    val it = q.iterator()
    while (it.hasNext()) {
      k += 1
      mustEqual(k, it.next())
    }
    mustEqual(3, k)
  }

  /** Modifications do not cause iterators to fail
   */
  @Test def testWeaklyConsistentIteration() = {
    val q = new LinkedTransferQueue[Item]()
    q.add(itemFor(one))
    q.add(itemFor(two))
    q.add(itemFor(three))
    val it = q.iterator()
    while (it.hasNext()) {
      q.remove()
      it.next()
    }
    mustEqual(0, q.size())
  }

  private def populatedQueue(n: Int) = {
    val q = new LinkedTransferQueue[Item]()
    // checkEmpty(q)
    for (i <- 0 until n) {
      mustEqual(i, q.size())
      mustOffer(q, i)
      mustEqual(Integer.MAX_VALUE, q.remainingCapacity())
    }
    assertFalse(q.isEmpty())
    q
  }
}

object LinkedTransferQueueTest {
  class Generic extends BlockingQueueTest {
    protected def emptyCollection() = new LinkedTransferQueue()
  }
}
