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
import scala.util.Using

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
    )
    assertTrue(
      Arrays.equals(
        q.toArray(new Array[Object](SIZE)),
        intList.toArray(new Array[Object](SIZE))
      )
    )
    for (i <- 0 until SIZE) {
      mustEqual(items(i), q.poll())
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
    val items = new Array[Item](2)
    items(0) = new Item(zero)
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
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
    val q = populatedQueue(SIZE)
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
    })

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
    val q = new LinkedTransferQueue[Item]()
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

  /** offer transfers elements across Executor tasks
   */
  @Test def testOfferInExecutor(): Unit = {
    val q = new LinkedTransferQueue[Item]()
    val threadsStarted = new CheckedBarrier(2)
    val executor = Executors.newFixedThreadPool(2)
    Using(cleaner(executor)) { cleaner =>

      executor.execute(new CheckedRunnable() {
        override def realRun() = {
          threadsStarted.await()
          val startTime = System.nanoTime()
          assertTrue(q.offer(itemFor(one), LONG_DELAY_MS, MILLISECONDS))
          assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
        }
      })

      executor.execute(new CheckedRunnable() {
        override def realRun() = {
          threadsStarted.await()
          assertSame(itemFor(one), q.take())
          checkEmpty(q)
        }
      })
    }
  }

  /** timed poll retrieves elements across Executor threads
   */
  @Test def testPollInExecutor(): Unit = {
    val q = new LinkedTransferQueue[Item]()
    val threadsStarted = new CheckedBarrier(2)
    val executor = Executors.newFixedThreadPool(2)
    Using(cleaner(executor)) { cleaner =>
      executor.execute(new CheckedRunnable() {
        override def realRun() = {
          assertNull(q.poll())
          threadsStarted.await()
          val startTime = System.nanoTime()
          assertSame(itemFor(one), q.poll(LONG_DELAY_MS, MILLISECONDS))
          assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
          checkEmpty(q)
        }
      })

      executor.execute(new CheckedRunnable() {
        override def realRun() = {
          threadsStarted.await()
          q.put(itemFor(one))
        }
      })
    }
  }

  /** A deserialized/reserialized queue has same elements in same order
   *
   *  We don't have `serialClone`, since ObjectInputStream is not in Scala
   *  Native.
   */
  // @Test def testSerialization() = {
  //   val x: Queue[Item] = populatedQueue(SIZE)
  //   val y: Queue[Item] = serialClone(x)
  //   assertNotSame(y, x)
  //   mustEqual(x.size(), y.size())
  //   mustEqual(x.toString(), y.toString())
  //   assertTrue(Arrays.equals(x.toArray(), y.toArray()))
  //   while (!x.isEmpty()) {
  //     assertFalse(y.isEmpty())
  //     mustEqual(x.remove(), y.remove())
  //   }
  //   assertTrue(y.isEmpty())
  // }

  /** drainTo(c) empties queue into another collection c
   */
  @Test def testDrainTo() = {
    val q = populatedQueue(SIZE)
    val l = new ArrayList[Item]()
    q.drainTo(l)
    mustEqual(0, q.size())
    mustEqual(SIZE, l.size())
    for (i <- 0 until SIZE) {
      mustEqual(i, l.get(i))
    }
    q.add(itemFor(zero))
    q.add(itemFor(one))
    assertFalse(q.isEmpty())
    mustContain(q, itemFor(zero))
    mustContain(q, itemFor(one))
    l.clear()
    q.drainTo(l)
    mustEqual(0, q.size())
    mustEqual(2, l.size())
    for (i <- 0 until 2) {
      mustEqual(i, l.get(i))
    }
  }

  /** drainTo(c) empties full queue, unblocking a waiting put.
   */
  @Test def testDrainToWithActivePut() = {
    val q = populatedQueue(SIZE)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        q.put(new Item(SIZE + 1))
      }
    })
    val l = new ArrayList[Item]()
    q.drainTo(l)
    assertTrue(l.size() >= SIZE)
    for (i <- 0 until SIZE)
      mustEqual(i, l.get(i))
    awaitTermination(t)
    assertTrue(q.size() + l.size() >= SIZE)
  }

  /** drainTo(c, n) empties first min(n, size) elements of queue into c
   */
  @Test def testDrainToN() = {
    val q = new LinkedTransferQueue[Item]()
    for (i <- 0 until SIZE + 2) {
      for (j <- 0 until SIZE) {
        mustOffer(q, j)
      }
      val l = new ArrayList[Item]()
      q.drainTo(l, i)
      val k = if (i < SIZE) i else SIZE
      mustEqual(k, l.size())
      mustEqual(SIZE - k, q.size())
      for (j <- 0 until k)
        mustEqual(j, l.get(j))
      while (q.poll() != null) {}
    }
  }

  /** timed poll() or take() increments the waiting consumer count offer(e)
   *  decrements the waiting consumer count
   */
  @Test def testWaitingConsumer() = {
    val q = new LinkedTransferQueue[Item]()
    mustEqual(0, q.getWaitingConsumerCount())
    assertFalse(q.hasWaitingConsumer())
    val threadStarted = new CountDownLatch(1)

    val t = newStartedThread(new CheckedRunnable() {
      def realRun() = {
        threadStarted.countDown()
        val startTime = System.nanoTime()
        assertSame(itemFor(one), q.poll(LONG_DELAY_MS, MILLISECONDS))
        mustEqual(0, q.getWaitingConsumerCount())
        assertFalse(q.hasWaitingConsumer())
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      }
    })

    threadStarted.await()
    val oneConsumer = new Callable[Boolean]() {
      override def call() =
        q.hasWaitingConsumer() && q.getWaitingConsumerCount() == 1

    }
    waitForThreadToEnterWaitState(t, oneConsumer)

    assertTrue(q.offer(itemFor(one)))
    mustEqual(0, q.getWaitingConsumerCount())
    assertFalse(q.hasWaitingConsumer())

    awaitTermination(t)
  }

  /** transfer(null) throws NullPointerException
   */
  @Test def testTransfer1() = {
    try {
      val q = new LinkedTransferQueue[Item]()
      q.transfer(null)
      shouldThrow()
    } catch {
      case _: NullPointerException => {}
    }
  }

  /** transfer waits until a poll occurs. The transferred element is returned by
   *  the associated poll.
   */
  @Test def testTransfer2() = {
    val q = new LinkedTransferQueue[Item]()
    val threadStarted = new CountDownLatch(1)

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        threadStarted.countDown()
        q.transfer(itemFor(five))
        checkEmpty(q)
      }
    })

    threadStarted.await()
    val oneElement = new Callable[Boolean]() {
      override def call() =
        !q.isEmpty() && q.size() == 1
    }
    waitForThreadToEnterWaitState(t, oneElement)

    assertSame(itemFor(five), q.poll())
    checkEmpty(q)
    awaitTermination(t)
  }

  /** transfer waits until a poll occurs, and then transfers in fifo order
   */
  @Test def testTransfer3() = {
    val q = new LinkedTransferQueue[Item]()

    val first = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        q.transfer(itemFor(four))
        mustNotContain(q, four)
        mustEqual(1, q.size())
      }
    })

    val interruptedThread = newStartedThread(new CheckedInterruptedRunnable() {
      override def realRun() = {
        while (q.isEmpty())
          Thread.`yield`()
        q.transfer(itemFor(five))
      }
    })

    while (q.size() < 2)
      Thread.`yield`()
    mustEqual(2, q.size())
    assertSame(itemFor(four), q.poll())
    first.join()
    mustEqual(1, q.size())
    interruptedThread.interrupt()
    interruptedThread.join()
    checkEmpty(q)
  }

  /** transfer waits until a poll occurs, at which point the polling thread
   *  returns the element
   */
  @Test def testTransfer4() = {
    val q = new LinkedTransferQueue[Item]()

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        q.transfer(itemFor(four))
        mustNotContain(q, itemFor(four))
        assertSame(itemFor(three), q.poll())
      }
    })

    while (q.isEmpty())
      Thread.`yield`()
    assertFalse(q.isEmpty())
    mustEqual(1, q.size())
    assertTrue(q.offer(itemFor(three)))
    assertSame(itemFor(four), q.poll())
    awaitTermination(t)
  }

  /** transfer waits until a take occurs. The transferred element is returned by
   *  the associated take.
   */
  @Test def testTransfer5() = {
    val q = new LinkedTransferQueue[Item]()

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        q.transfer(itemFor(four))
        checkEmpty(q)
      }
    })

    while (q.isEmpty())
      Thread.`yield`()
    assertFalse(q.isEmpty())
    mustEqual(1, q.size())
    assertSame(itemFor(four), q.take())
    checkEmpty(q)
    awaitTermination(t)
  }

  /** tryTransfer(null) throws NullPointerException
   */
  @Test def testTryTransfer1() = {
    val q = new LinkedTransferQueue[Item]()
    try {
      q.tryTransfer(null)
      shouldThrow()
    } catch {
      case _: NullPointerException => {}
    }
  }

  /** tryTransfer returns false and does not enqueue if there are no consumers
   *  waiting to poll or take.
   */
  @Test def testTryTransfer2() = {
    val q = new LinkedTransferQueue[Object]()
    assertFalse(q.tryTransfer(new Object()))
    assertFalse(q.hasWaitingConsumer())
    checkEmpty(q)
  }

  /** If there is a consumer waiting in timed poll, tryTransfer returns true
   *  while successfully transfering object.
   */
  @Test def testTryTransfer3() = {
    val hotPotato = new Object()
    val q = new LinkedTransferQueue[Object]()

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        while (!q.hasWaitingConsumer())
          Thread.`yield`()
        assertTrue(q.hasWaitingConsumer())
        checkEmpty(q)
        assertTrue(q.tryTransfer(hotPotato))
      }
    })

    val startTime = System.nanoTime()
    assertSame(hotPotato, q.poll(LONG_DELAY_MS, MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    checkEmpty(q)
    awaitTermination(t)
  }

  /** If there is a consumer waiting in take, tryTransfer returns true while
   *  successfully transfering object.
   */
  @Test def testTryTransfer4() = {
    val hotPotato = new Object()
    val q = new LinkedTransferQueue[Object]()

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        while (!q.hasWaitingConsumer())
          Thread.`yield`()
        assertTrue(q.hasWaitingConsumer())
        checkEmpty(q)
        assertTrue(q.tryTransfer(hotPotato))
      }
    })

    assertSame(q.take(), hotPotato)
    checkEmpty(q)
    awaitTermination(t)
  }

  /** tryTransfer blocks interruptibly if no takers
   */
  @Test def testTryTransfer5() = {
    val q = new LinkedTransferQueue[Object]()
    val pleaseInterrupt = new CountDownLatch(1)
    assertTrue(q.isEmpty())

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        Thread.currentThread().interrupt()
        try {
          q.tryTransfer(new Object(), randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException => {}
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.tryTransfer(new Object(), LONGER_DELAY_MS, MILLISECONDS)
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

  /** tryTransfer gives up after the timeout and returns false
   */
  @Test def testTryTransfer6() = {
    val q = new LinkedTransferQueue[Object]()

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        val startTime = System.nanoTime()
        assertFalse(q.tryTransfer(new Object(), timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        checkEmpty(q)
      }
    })

    awaitTermination(t)
    checkEmpty(q)
  }

  /** tryTransfer waits for any elements previously in to be removed before
   *  transfering to a poll or take
   */
  @Test def testTryTransfer7() = {
    val q = new LinkedTransferQueue[Item]()
    assertTrue(q.offer(itemFor(four)))

    val t = newStartedThread(new CheckedRunnable() {
      override def realRun() = {
        val startTime = System.nanoTime()
        assertTrue(q.tryTransfer(itemFor(five), LONG_DELAY_MS, MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
        checkEmpty(q)
      }
    })

    while (q.size() != 2)
      Thread.`yield`()
    mustEqual(2, q.size())
    assertSame(itemFor(four), q.poll())
    assertSame(itemFor(five), q.poll())
    checkEmpty(q)
    awaitTermination(t)
  }

  /** tryTransfer attempts to enqueue into the queue and fails returning false
   *  not enqueueing and the successive poll is null
   */
  @Test def testTryTransfer8() = {
    val q = new LinkedTransferQueue[Item]()
    assertTrue(q.offer(itemFor(four)))
    mustEqual(1, q.size())
    val startTime = System.nanoTime()
    assertFalse(q.tryTransfer(itemFor(five), timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    mustEqual(1, q.size())
    assertSame(itemFor(four), q.poll())
    assertNull(q.poll())
    checkEmpty(q)
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

  /** remove(null), contains(null) always return false
   */
  @Test def testNeverContainsNull() = {
    val qs = Seq(
      new LinkedTransferQueue[Item](),
      populatedQueue(2)
    )

    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
    }
  }

  /* ==== Not from JSR166 ==== */
  @Test def testForEach() = {
    val q = new LinkedTransferQueue[Item]()
    q.add(itemFor(one))
    q.add(itemFor(two))

    q.forEach { x =>
      assertTrue(x == itemFor(one) || x == itemFor(two))
    }
  }
}

object LinkedTransferQueueTest {
  class Generic extends BlockingQueueTest {
    protected def emptyCollection() = new LinkedTransferQueue()
  }
}
