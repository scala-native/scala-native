/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent._

import org.junit.Assert._
import org.junit.{Ignore, Test}

object LinkedBlockingDequeTest {
  import JSR166Test._

  def populatedDeque(n: Int): LinkedBlockingDeque[Item] = {
    val q = new LinkedBlockingDeque[Item](n)
    assertTrue(q.isEmpty())
    for (i <- 0 until n) mustOffer(q, i)
    assertFalse(q.isEmpty())
    mustEqual(0, q.remainingCapacity())
    mustEqual(n, q.size())
    mustEqual(0, q.peekFirst())
    mustEqual(n - 1, q.peekLast())
    q
  }
}

class LinkedBlockingDequeUnboundedTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    new LinkedBlockingDeque[Any]()
}

class LinkedBlockingDequeBoundedTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    new LinkedBlockingDeque[Any](JSR166Test.SIZE)
}

class LinkedBlockingDequeTest extends JSR166Test {
  import JSR166Test._
  import LinkedBlockingDequeTest._

  private def it(i: Int): Item = itemFor(i)
  private val eightySix = itemFor(86)

  /** isEmpty is true before add, false after */
  @Test def testEmpty(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    assertTrue(q.isEmpty())
    q.add(it(1))
    assertFalse(q.isEmpty())
    q.add(it(2))
    q.removeFirst()
    q.removeFirst()
    assertTrue(q.isEmpty())
  }

  /** size changes when elements added and removed */
  @Test def testSize(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(SIZE - i, q.size())
      q.removeFirst()
    }
    for (i <- 0 until SIZE) {
      mustEqual(i, q.size())
      mustAdd(q, it(1))
    }
  }

  /** offerFirst(null) throws NullPointerException */
  @Test def testOfferFirstNull(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    try {
      q.offerFirst(null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** offerLast(null) throws NullPointerException */
  @Test def testOfferLastNull(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    try {
      q.offerLast(null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** OfferFirst succeeds */
  @Test def testOfferFirst(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    assertTrue(q.offerFirst(it(0)))
    assertTrue(q.offerFirst(it(2)))
  }

  /** OfferLast succeeds */
  @Test def testOfferLast(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    assertTrue(q.offerLast(it(0)))
    assertTrue(q.offerLast(it(1)))
  }

  /** pollFirst succeeds unless empty */
  @Test def testPollFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.pollFirst())
    assertNull(q.pollFirst())
  }

  /** pollLast succeeds unless empty */
  @Test def testPollLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) mustEqual(i, q.pollLast())
    assertNull(q.pollLast())
  }

  /** peekFirst returns next element, or null if empty */
  @Test def testPeekFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.peekFirst())
      mustEqual(i, q.pollFirst())
      assertTrue(q.peekFirst() == null || q.peekFirst() != itemFor(i))
    }
    assertNull(q.peekFirst())
  }

  /** peek returns next element, or null if empty */
  @Test def testPeek(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.peek())
      mustEqual(i, q.pollFirst())
      assertTrue(q.peek() == null || q.peek() != itemFor(i))
    }
    assertNull(q.peek())
  }

  /** peekLast returns next element, or null if empty */
  @Test def testPeekLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) {
      mustEqual(i, q.peekLast())
      mustEqual(i, q.pollLast())
      assertTrue(q.peekLast() == null || q.peekLast() != itemFor(i))
    }
    assertNull(q.peekLast())
  }

  /** getFirst() returns first element, or throws NSEE if empty */
  @Test def testFirstElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.getFirst())
      mustEqual(i, q.pollFirst())
    }
    try {
      q.getFirst()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
    assertNull(q.peekFirst())
  }

  /** getLast() returns last element, or throws NSEE if empty */
  @Test def testLastElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) {
      mustEqual(i, q.getLast())
      mustEqual(i, q.pollLast())
    }
    try {
      q.getLast()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
    assertNull(q.peekLast())
  }

  /** removeFirst() removes first element, or throws NSEE if empty */
  @Test def testRemoveFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.removeFirst())
    try {
      q.removeFirst()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
    assertNull(q.peekFirst())
  }

  /** removeLast() removes last element, or throws NSEE if empty */
  @Test def testRemoveLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) mustEqual(i, q.removeLast())
    try {
      q.removeLast()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
    assertNull(q.peekLast())
  }

  /** remove removes next element, or throws NSEE if empty */
  @Test def testRemove(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.remove())
    try {
      q.remove()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }

  /** removeFirstOccurrence(x) removes x and returns true if present */
  @Test def testRemoveFirstOccurrence(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 1 until SIZE by 2)
      assertTrue(q.removeFirstOccurrence(itemFor(i)))
    for (i <- 0 until SIZE by 2) {
      assertTrue(q.removeFirstOccurrence(itemFor(i)))
      assertFalse(q.removeFirstOccurrence(itemFor(i + 1)))
    }
    assertTrue(q.isEmpty())
  }

  /** removeLastOccurrence(x) removes x and returns true if present */
  @Test def testRemoveLastOccurrence(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 1 until SIZE by 2)
      assertTrue(q.removeLastOccurrence(itemFor(i)))
    for (i <- 0 until SIZE by 2) {
      assertTrue(q.removeLastOccurrence(itemFor(i)))
      assertFalse(q.removeLastOccurrence(itemFor(i + 1)))
    }
    assertTrue(q.isEmpty())
  }

  /** peekFirst returns element inserted with addFirst */
  @Test def testAddFirst(): Unit = {
    val q = populatedDeque(3)
    q.pollLast()
    q.addFirst(it(4))
    assertSame(it(4), q.peekFirst())
  }

  /** peekLast returns element inserted with addLast */
  @Test def testAddLast(): Unit = {
    val q = populatedDeque(3)
    q.pollLast()
    q.addLast(it(4))
    assertSame(it(4), q.peekLast())
  }

  /** A new deque has indicated capacity, or Integer.MAX_VALUE if none given */
  @Test def testConstructor1(): Unit = {
    mustEqual(SIZE, new LinkedBlockingDeque[Item](SIZE).remainingCapacity())
    mustEqual(Int.MaxValue, new LinkedBlockingDeque[Item]().remainingCapacity())
  }

  /** Constructor throws IllegalArgumentException if capacity nonpositive */
  @Test def testConstructor2(): Unit = {
    try {
      new LinkedBlockingDeque[Item](0)
      shouldThrow()
    } catch {
      case _: IllegalArgumentException =>
    }
  }

  /** Initializing from null Collection throws NullPointerException */
  @Test def testConstructor3(): Unit = {
    try {
      new LinkedBlockingDeque[Item](null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Initializing from Collection of null elements throws NPE */
  @Test def testConstructor4(): Unit = {
    val elements: Collection[Item] = Arrays.asList(new Array[Item](SIZE): _*)
    try {
      new LinkedBlockingDeque[Item](elements)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Initializing from Collection with some null elements throws NPE */
  @Test def testConstructor5(): Unit = {
    val items = new Array[Item](2)
    items(0) = it(0)
    val elements: Collection[Item] = Arrays.asList(items: _*)
    try {
      new LinkedBlockingDeque[Item](elements)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Deque contains all elements of collection used to initialize */
  @Test def testConstructor6(): Unit = {
    val items = defaultItems
    val q = new LinkedBlockingDeque[Item](Arrays.asList(items: _*))
    for (i <- 0 until SIZE) mustEqual(items(i), q.poll())
  }

  /** Deque transitions from empty to full when elements added */
  @Test def testEmptyFull(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    assertTrue(q.isEmpty())
    mustEqual(2, q.remainingCapacity())
    q.add(it(1))
    assertFalse(q.isEmpty())
    q.add(it(2))
    assertFalse(q.isEmpty())
    mustEqual(0, q.remainingCapacity())
    assertFalse(q.offer(it(3)))
  }

  /** remainingCapacity decreases on add, increases on remove */
  @Test def testRemainingCapacity(): Unit = {
    val q: BlockingQueue[Item] = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.remainingCapacity())
      mustEqual(SIZE, q.size() + q.remainingCapacity())
      mustEqual(i, q.remove())
    }
    for (i <- 0 until SIZE) {
      mustEqual(SIZE - i, q.remainingCapacity())
      mustEqual(SIZE, q.size() + q.remainingCapacity())
      mustAdd(q, i)
    }
  }

  /** push(null) throws NPE */
  @Test def testPushNull(): Unit = {
    val q = new LinkedBlockingDeque[Item](1)
    try {
      q.push(null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** push succeeds if not full; throws IllegalStateException if full */
  @Test def testPush(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    for (i <- 0 until SIZE) {
      val x = itemFor(i)
      q.push(x)
      mustEqual(x, q.peek())
    }
    mustEqual(0, q.remainingCapacity())
    try {
      q.push(itemFor(SIZE))
      shouldThrow()
    } catch {
      case _: IllegalStateException =>
    }
  }

  /** peekFirst returns element inserted with push */
  @Test def testPushWithPeek(): Unit = {
    val q = populatedDeque(3)
    q.pollLast()
    q.push(it(4))
    assertSame(it(4), q.peekFirst())
  }

  /** pop removes next element, or throws NSEE if empty */
  @Test def testPop(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.pop())
    try {
      q.pop()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }

  /** Offer succeeds if not full; fails if full */
  @Test def testOffer(): Unit = {
    val q = new LinkedBlockingDeque[Item](1)
    assertTrue(q.offer(it(0)))
    assertFalse(q.offer(it(1)))
  }

  /** add succeeds if not full; throws IllegalStateException if full */
  @Test def testAdd(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    for (i <- 0 until SIZE) mustAdd(q, i)
    mustEqual(0, q.remainingCapacity())
    try {
      q.add(itemFor(SIZE))
      shouldThrow()
    } catch {
      case _: IllegalStateException =>
    }
  }

  /** addAll(this) throws IllegalArgumentException */
  @Test def testAddAllSelf(): Unit = {
    val q = populatedDeque(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case _: IllegalArgumentException =>
    }
  }

  /** addAll of a collection with any null elements throws NPE */
  @Test def testAddAll3(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    val items = new Array[Item](2)
    items(0) = it(0)
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** addAll throws IllegalStateException if not enough room */
  @Test def testAddAll4(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE - 1)
    try {
      q.addAll(Arrays.asList(defaultItems: _*))
      shouldThrow()
    } catch {
      case _: IllegalStateException =>
    }
  }

  /** Deque contains all elements, in traversal order, of successful addAll */
  @Test def testAddAll5(): Unit = {
    val empty = new Array[Item](0)
    val items = defaultItems
    val q = new LinkedBlockingDeque[Item](SIZE)
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    for (i <- 0 until SIZE) mustEqual(items(i), q.poll())
  }

  /** all elements successfully put are contained */
  @Test def testPut(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    for (i <- 0 until SIZE) {
      val x = itemFor(i)
      q.put(x)
      mustContain(q, x)
    }
    mustEqual(0, q.remainingCapacity())
  }

  /** put blocks interruptibly if full */
  @Test def testBlockingPut(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) q.put(itemFor(i))
        mustEqual(SIZE, q.size())
        mustEqual(0, q.remainingCapacity())

        Thread.currentThread().interrupt()
        try {
          q.put(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.put(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    mustEqual(SIZE, q.size())
    mustEqual(0, q.remainingCapacity())
  }

  /** put blocks interruptibly waiting for take when full */
  @Test def testPutWithTake(): Unit = {
    val capacity = 2
    val q = new LinkedBlockingDeque[Item](capacity)
    val pleaseTake = new CountDownLatch(1)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until capacity) q.put(itemFor(i))
        pleaseTake.countDown()
        q.put(eightySix)

        Thread.currentThread().interrupt()
        try {
          q.put(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.put(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseTake)
    mustEqual(0, q.remainingCapacity())
    mustEqual(0, q.take())

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    mustEqual(0, q.remainingCapacity())
  }

  /** timed offer times out if full and elements not taken */
  @Test def testTimedOffer(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        q.put(it(0))
        q.put(it(1))
        val startTime = System.nanoTime()

        assertFalse(q.offer(it(2), timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())

        Thread.currentThread().interrupt()
        try {
          q.offer(it(3), randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.offer(it(4), LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** take retrieves elements in FIFO order */
  @Test def testTake(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.take())
  }

  /** take removes existing elements until empty, then blocks interruptibly */
  @Test def testBlockingTake(): Unit = {
    val q = populatedDeque(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) mustEqual(i, q.take())

        Thread.currentThread().interrupt()
        try {
          q.take()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.take()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** poll succeeds unless empty */
  @Test def testPoll(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.poll())
    assertNull(q.poll())
  }

  /** timed poll with zero timeout succeeds when non-empty, else times out */
  @Test def testTimedPoll0(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.poll(0, MILLISECONDS))
    assertNull(q.poll(0, MILLISECONDS))
  }

  /** timed poll with nonzero timeout succeeds when non-empty, else times out */
  @Test def testTimedPoll(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      val startTime = System.nanoTime()
      mustEqual(i, q.poll(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
    val startTime = System.nanoTime()
    assertNull(q.poll(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    checkEmpty(q)
  }

  /** Interrupted timed poll throws InterruptedException */
  @Test def testInterruptedTimedPoll(): Unit = {
    val q: BlockingQueue[Item] = populatedDeque(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE)
          mustEqual(i, q.poll(LONG_DELAY_MS, MILLISECONDS))

        Thread.currentThread().interrupt()
        try {
          q.poll(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.poll(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
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

  /** putFirst(null) throws NPE */
  @Test def testPutFirstNull(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    try {
      q.putFirst(null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** all elements successfully putFirst are contained */
  @Test def testPutFirst(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    for (i <- 0 until SIZE) {
      val x = itemFor(i)
      q.putFirst(x)
      mustContain(q, x)
    }
    mustEqual(0, q.remainingCapacity())
  }

  /** putFirst blocks interruptibly if full */
  @Test def testBlockingPutFirst(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) q.putFirst(itemFor(i))
        mustEqual(SIZE, q.size())
        mustEqual(0, q.remainingCapacity())

        Thread.currentThread().interrupt()
        try {
          q.putFirst(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.putFirst(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    mustEqual(SIZE, q.size())
    mustEqual(0, q.remainingCapacity())
  }

  /** putFirst blocks interruptibly waiting for take when full */
  @Test def testPutFirstWithTake(): Unit = {
    val capacity = 2
    val q = new LinkedBlockingDeque[Item](capacity)
    val pleaseTake = new CountDownLatch(1)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until capacity) q.putFirst(itemFor(i))
        pleaseTake.countDown()
        q.putFirst(eightySix)

        pleaseInterrupt.countDown()
        try {
          q.putFirst(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseTake)
    mustEqual(0, q.remainingCapacity())
    mustEqual(capacity - 1, q.take())

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    mustEqual(0, q.remainingCapacity())
  }

  /** timed offerFirst times out if full and elements not taken */
  @Test def testTimedOfferFirst(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        q.putFirst(it(0))
        q.putFirst(it(1))
        val startTime = System.nanoTime()

        assertFalse(q.offerFirst(it(2), timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())

        Thread.currentThread().interrupt()
        try {
          q.offerFirst(it(3), randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.offerFirst(it(4), LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** takeFirst retrieves elements in FIFO order */
  @Test def testTakeFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.takeFirst())
  }

  /** takeFirst() blocks interruptibly when empty */
  @Test def testTakeFirstFromEmptyBlocksInterruptibly(): Unit = {
    val q: BlockingDeque[Item] = new LinkedBlockingDeque[Item]()
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        threadStarted.countDown()
        try {
          q.takeFirst()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(threadStarted)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** takeFirst() throws InterruptedException immediately if interrupted */
  @Test def testTakeFirstFromEmptyAfterInterrupt(): Unit = {
    val q: BlockingDeque[Item] = new LinkedBlockingDeque[Item]()
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        Thread.currentThread().interrupt()
        try {
          q.takeFirst()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    awaitTermination(t)
  }

  /** takeLast() blocks interruptibly when empty */
  @Test def testTakeLastFromEmptyBlocksInterruptibly(): Unit = {
    val q: BlockingDeque[Item] = new LinkedBlockingDeque[Item]()
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        threadStarted.countDown()
        try {
          q.takeLast()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(threadStarted)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** takeLast() throws InterruptedException immediately if interrupted */
  @Test def testTakeLastFromEmptyAfterInterrupt(): Unit = {
    val q: BlockingDeque[Item] = new LinkedBlockingDeque[Item]()
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        Thread.currentThread().interrupt()
        try {
          q.takeLast()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    awaitTermination(t)
  }

  /** takeFirst removes existing elements until empty, then blocks */
  @Test def testBlockingTakeFirst(): Unit = {
    val q = populatedDeque(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) mustEqual(i, q.takeFirst())

        Thread.currentThread().interrupt()
        try {
          q.takeFirst()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.takeFirst()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed pollFirst with zero timeout succeeds when non-empty */
  @Test def testTimedPollFirst0(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(i, q.pollFirst(0, MILLISECONDS))
    assertNull(q.pollFirst(0, MILLISECONDS))
  }

  /** timed pollFirst with nonzero timeout succeeds when non-empty */
  @Test def testTimedPollFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      val startTime = System.nanoTime()
      mustEqual(i, q.pollFirst(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
    val startTime = System.nanoTime()
    assertNull(q.pollFirst(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    checkEmpty(q)
  }

  /** Interrupted timed pollFirst throws InterruptedException */
  @Test def testInterruptedTimedPollFirst(): Unit = {
    val q = populatedDeque(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE)
          mustEqual(i, q.pollFirst(LONG_DELAY_MS, MILLISECONDS))

        Thread.currentThread().interrupt()
        try {
          q.pollFirst(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.pollFirst(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed pollFirst before and after offerFirst */
  @Test def testTimedPollFirstWithOfferFirst(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    val barrier = new CheckedBarrier(2)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertNull(q.pollFirst(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())

        barrier.await()
        assertSame(it(0), q.pollFirst(LONG_DELAY_MS, MILLISECONDS))

        Thread.currentThread().interrupt()
        try {
          q.pollFirst(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }

        barrier.await()
        try {
          q.pollFirst(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      }
    })

    barrier.await()
    val startTime = System.nanoTime()
    assertTrue(q.offerFirst(it(0), LONG_DELAY_MS, MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    barrier.await()
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** putLast(null) throws NPE */
  @Test def testPutLastNull(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    try {
      q.putLast(null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** all elements successfully putLast are contained */
  @Test def testPutLast(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    for (i <- 0 until SIZE) {
      val x = itemFor(i)
      q.putLast(x)
      mustContain(q, x)
    }
    mustEqual(0, q.remainingCapacity())
  }

  /** putLast blocks interruptibly if full */
  @Test def testBlockingPutLast(): Unit = {
    val q = new LinkedBlockingDeque[Item](SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) q.putLast(itemFor(i))
        mustEqual(SIZE, q.size())
        mustEqual(0, q.remainingCapacity())

        Thread.currentThread().interrupt()
        try {
          q.putLast(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.putLast(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    mustEqual(SIZE, q.size())
    mustEqual(0, q.remainingCapacity())
  }

  /** putLast blocks interruptibly waiting for take when full */
  @Test def testPutLastWithTake(): Unit = {
    val capacity = 2
    val q = new LinkedBlockingDeque[Item](capacity)
    val pleaseTake = new CountDownLatch(1)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until capacity) q.putLast(itemFor(i))
        pleaseTake.countDown()
        q.putLast(eightySix)

        Thread.currentThread().interrupt()
        try {
          q.putLast(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.putLast(ninetynine)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseTake)
    mustEqual(0, q.remainingCapacity())
    mustEqual(0, q.take())

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    mustEqual(0, q.remainingCapacity())
  }

  /** timed offerLast times out if full and elements not taken */
  @Test def testTimedOfferLast(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        q.putLast(it(0))
        q.putLast(it(1))
        val startTime = System.nanoTime()

        assertFalse(q.offerLast(it(2), timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())

        Thread.currentThread().interrupt()
        try {
          q.offerLast(it(3), randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }

        pleaseInterrupt.countDown()
        try {
          q.offerLast(it(4), LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** takeLast retrieves elements in reverse FIFO order */
  @Test def testTakeLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) mustEqual(SIZE - i - 1, q.takeLast())
  }

  /** takeLast removes existing elements until empty, then blocks */
  @Test def testBlockingTakeLast(): Unit = {
    val q = populatedDeque(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) mustEqual(SIZE - i - 1, q.takeLast())

        Thread.currentThread().interrupt()
        try {
          q.takeLast()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.takeLast()
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed pollLast with zero timeout succeeds when non-empty */
  @Test def testTimedPollLast0(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE)
      mustEqual(SIZE - i - 1, q.pollLast(0, MILLISECONDS))
    assertNull(q.pollLast(0, MILLISECONDS))
  }

  /** timed pollLast with nonzero timeout succeeds when non-empty */
  @Test def testTimedPollLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      val startTime = System.nanoTime()
      mustEqual(SIZE - i - 1, q.pollLast(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
    val startTime = System.nanoTime()
    assertNull(q.pollLast(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    checkEmpty(q)
  }

  /** Interrupted timed pollLast throws InterruptedException */
  @Test def testInterruptedTimedPollLast(): Unit = {
    val q = populatedDeque(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE)
          mustEqual(SIZE - i - 1, q.pollLast(LONG_DELAY_MS, MILLISECONDS))

        Thread.currentThread().interrupt()
        try {
          q.pollLast(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.pollLast(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
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

  /** timed poll before and after offerLast */
  @Test def testTimedPollWithOfferLast(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    val barrier = new CheckedBarrier(2)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertNull(q.poll(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())

        barrier.await()
        assertSame(it(0), q.poll(LONG_DELAY_MS, MILLISECONDS))

        Thread.currentThread().interrupt()
        try {
          q.poll(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())

        barrier.await()
        try {
          q.poll(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case _: InterruptedException =>
        }
        assertFalse(Thread.interrupted())
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      }
    })

    barrier.await()
    val startTime = System.nanoTime()
    assertTrue(q.offerLast(it(0), LONG_DELAY_MS, MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)

    barrier.await()
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** element returns next element, or throws NSEE if empty */
  @Test def testElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.element())
      q.poll()
    }
    try {
      q.element()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }

  /** contains(x) reports true when elements added but not yet removed */
  @Test def testContains(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustContain(q, i)
      q.poll()
      mustNotContain(q, i)
    }
  }

  /** clear removes all elements */
  @Test def testClear(): Unit = {
    val q = populatedDeque(SIZE)
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    mustEqual(SIZE, q.remainingCapacity())
    q.add(it(1))
    assertFalse(q.isEmpty())
    mustContain(q, it(1))
    q.clear()
    assertTrue(q.isEmpty())
  }

  /** containsAll(c) is true when c contains a subset of elements */
  @Test def testContainsAll(): Unit = {
    val q = populatedDeque(SIZE)
    val p = new LinkedBlockingDeque[Item](SIZE)
    for (i <- 0 until SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c */
  @Test def testRetainAll(): Unit = {
    val q = populatedDeque(SIZE)
    val p = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)

      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size())
      p.remove()
    }
  }

  /** removeAll(c) removes only those elements of c */
  @Test def testRemoveAll(): Unit = {
    for (i <- 1 until SIZE) {
      val q = populatedDeque(SIZE)
      val p = populatedDeque(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size())
      for (_ <- 0 until i)
        mustNotContain(q, p.remove())
    }
  }

  /** toArray contains all elements in FIFO order */
  @Test def testToArray(): Unit = {
    val q = populatedDeque(SIZE)
    val a = q.toArray()
    assertSame(classOf[Array[AnyRef]], a.getClass())
    a.foreach(o => assertSame(o, q.poll()))
    assertTrue(q.isEmpty())
  }

  /** toArray(a) contains all elements in FIFO order */
  @Test def testToArray2(): Unit = {
    val q = populatedDeque(SIZE)
    val items = new Array[Item](SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    items.foreach(o => assertSame(o, q.remove()))
    assertTrue(q.isEmpty())
  }

  /** toArray(incompatible array type) throws ArrayStoreException */
  @Ignore(
    "Scala Native reference arrays do not preserve runtime component types"
  )
  @Test def testToArray_incompatibleArrayType(): Unit = {
    val q = populatedDeque(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case _: ArrayStoreException =>
    }
  }

  /** iterator iterates through all elements */
  @Test def testIterator(): Unit = {
    val q = populatedDeque(SIZE)
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

  /** iterator of empty collection has no elements */
  @Test def testEmptyIterator(): Unit = {
    val c: Deque[Item] = new LinkedBlockingDeque[Item]()
    assertIteratorExhausted(c.iterator())
    assertIteratorExhausted(c.descendingIterator())
  }

  /** iterator.remove removes current element */
  @Test def testIteratorRemove(): Unit = {
    val q = new LinkedBlockingDeque[Item](3)
    q.add(it(2))
    q.add(it(1))
    q.add(it(3))

    var iter = q.iterator()
    iter.next()
    iter.remove()

    iter = q.iterator()
    assertSame(iter.next(), it(1))
    assertSame(iter.next(), it(3))
    assertFalse(iter.hasNext())
  }

  /** iterator ordering is FIFO */
  @Test def testIteratorOrdering(): Unit = {
    val q = new LinkedBlockingDeque[Item](3)
    q.add(it(1))
    q.add(it(2))
    q.add(it(3))
    mustEqual(0, q.remainingCapacity())
    var k = 0
    val iter = q.iterator()
    while (iter.hasNext()) {
      k += 1
      mustEqual(k, iter.next())
    }
    mustEqual(3, k)
  }

  /** Modifications do not cause iterators to fail */
  @Test def testWeaklyConsistentIteration(): Unit = {
    val q = new LinkedBlockingDeque[Item](3)
    q.add(it(1))
    q.add(it(2))
    q.add(it(3))
    val iter = q.iterator()
    while (iter.hasNext()) {
      q.remove()
      iter.next()
    }
    mustEqual(0, q.size())
  }

  /** Descending iterator iterates through all elements */
  @Test def testDescendingIterator(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    val iter = q.descendingIterator()
    while (iter.hasNext()) {
      mustContain(q, iter.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertFalse(iter.hasNext())
    try {
      iter.next()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }

  /** Descending iterator ordering is reverse FIFO */
  @Test def testDescendingIteratorOrdering(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    for (_ <- 0 until 100) {
      mustAdd(q, it(3))
      mustAdd(q, it(2))
      mustAdd(q, it(1))

      var k = 0
      val iter = q.descendingIterator()
      while (iter.hasNext()) {
        k += 1
        mustEqual(k, iter.next())
      }

      mustEqual(3, k)
      q.remove()
      q.remove()
      q.remove()
    }
  }

  /** descendingIterator.remove removes current element */
  @Test def testDescendingIteratorRemove(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    for (_ <- 0 until 100) {
      mustAdd(q, it(3))
      mustAdd(q, it(2))
      mustAdd(q, it(1))
      var iter = q.descendingIterator()
      mustEqual(iter.next(), it(1))
      iter.remove()
      mustEqual(iter.next(), it(2))
      iter = q.descendingIterator()
      mustEqual(iter.next(), it(2))
      mustEqual(iter.next(), it(3))
      iter.remove()
      assertFalse(iter.hasNext())
      q.remove()
    }
  }

  /** toString contains toStrings of elements */
  @Test def testToString(): Unit = {
    val q = populatedDeque(SIZE)
    val s = q.toString()
    for (i <- 0 until SIZE)
      assertTrue(s.contains(String.valueOf(i)))
  }

  /** offer transfers elements across Executor tasks */
  @Test def testOfferInExecutor(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    q.add(it(1))
    q.add(it(2))
    val threadsStarted = new CheckedBarrier(2)
    val executor = Executors.newFixedThreadPool(2)
    usingPoolCleaner(executor) { executor =>
      executor.execute(new CheckedRunnable {
        override def realRun(): Unit = {
          assertFalse(q.offer(it(3)))
          threadsStarted.await()
          assertTrue(q.offer(it(3), LONG_DELAY_MS, MILLISECONDS))
          mustEqual(0, q.remainingCapacity())
        }
      })

      executor.execute(new CheckedRunnable {
        override def realRun(): Unit = {
          threadsStarted.await()
          assertSame(it(1), q.take())
        }
      })
    }
  }

  /** timed poll retrieves elements across Executor threads */
  @Test def testPollInExecutor(): Unit = {
    val q = new LinkedBlockingDeque[Item](2)
    val threadsStarted = new CheckedBarrier(2)
    val executor = Executors.newFixedThreadPool(2)
    usingPoolCleaner(executor) { executor =>
      executor.execute(new CheckedRunnable {
        override def realRun(): Unit = {
          assertNull(q.poll())
          threadsStarted.await()
          assertSame(it(1), q.poll(LONG_DELAY_MS, MILLISECONDS))
          checkEmpty(q)
        }
      })

      executor.execute(new CheckedRunnable {
        override def realRun(): Unit = {
          threadsStarted.await()
          q.put(it(1))
        }
      })
    }
  }

  /** A deserialized/reserialized deque has same elements in same order */
  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = ()

  /** drainTo(c) empties deque into another collection c */
  @Test def testDrainTo(): Unit = {
    val q = populatedDeque(SIZE)
    val l = new ArrayList[Item]()
    q.drainTo(l)
    mustEqual(0, q.size())
    mustEqual(SIZE, l.size())
    for (i <- 0 until SIZE) mustEqual(l.get(i), i)
    q.add(it(0))
    q.add(it(1))
    assertFalse(q.isEmpty())
    mustContain(q, it(0))
    mustContain(q, it(1))
    l.clear()
    q.drainTo(l)
    mustEqual(0, q.size())
    mustEqual(2, l.size())
    for (i <- 0 until 2) mustEqual(l.get(i), i)
  }

  /** drainTo empties full deque, unblocking a waiting put. */
  @Test def testDrainToWithActivePut(): Unit = {
    val q = populatedDeque(SIZE)
    val t = new Thread(new CheckedRunnable {
      override def realRun(): Unit = q.put(new Item(SIZE + 1))
    })

    t.start()
    val l = new ArrayList[Item]()
    q.drainTo(l)
    assertTrue(l.size() >= SIZE)
    for (i <- 0 until SIZE) mustEqual(l.get(i), i)
    t.join()
    assertTrue(q.size() + l.size() >= SIZE)
  }

  /** drainTo(c, n) empties first min(n, size) elements into c */
  @Test def testDrainToN(): Unit = {
    val q = new LinkedBlockingDeque[Item]()
    for (i <- 0 until SIZE + 2) {
      for (j <- 0 until SIZE) mustOffer(q, j)
      val l = new ArrayList[Item]()
      q.drainTo(l, i)
      val k = if (i < SIZE) i else SIZE
      mustEqual(k, l.size())
      mustEqual(SIZE - k, q.size())
      for (j <- 0 until k) mustEqual(l.get(j), j)
      while (q.poll() != null) ()
    }
  }

  /** remove(null), contains(null) always return false */
  @Test def testNeverContainsNull(): Unit = {
    val qs: Array[Deque[_]] =
      Array(new LinkedBlockingDeque[Item](), populatedDeque(2))

    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
      assertFalse(q.removeFirstOccurrence(null))
      assertFalse(q.removeLastOccurrence(null))
    }
  }
}
