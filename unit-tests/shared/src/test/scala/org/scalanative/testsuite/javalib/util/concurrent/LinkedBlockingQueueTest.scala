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
import java.util
import java.util._
import java.util.concurrent._

class LinkedBlockingQueueUnboundedTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    new LinkedBlockingQueue[Any]
}
class LinkedBlockingQueueBoundedTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    new LinkedBlockingQueue[Any](SIZE)
}

object LinkedBlockingQueueTest {

  /** Returns a new queue of given size containing consecutive Integers 0 ... n
   *  \- 1.
   */
  private def populatedQueue(n: Int): LinkedBlockingQueue[Integer] = {
    val q: LinkedBlockingQueue[Integer] = new LinkedBlockingQueue[Integer](n)
    assertTrue(q.isEmpty)
    for (i <- 0 until n) { assertTrue(q.offer(Integer.valueOf(i))) }
    assertFalse(q.isEmpty)
    assertEquals(0, q.remainingCapacity)
    assertEquals(n, q.size)
    assertEquals(0.asInstanceOf[Integer], q.peek)
    return q
  }
}
class LinkedBlockingQueueTest extends JSR166Test {

  /** A new queue has the indicated capacity, or Integer.MAX_VALUE if none given
   */
  @Test def testConstructor1(): Unit = {
    assertEquals(SIZE, new LinkedBlockingQueue[Integer](SIZE).remainingCapacity)
    assertEquals(
      Integer.MAX_VALUE,
      new LinkedBlockingQueue[Integer]().remainingCapacity
    )
  }

  /** Constructor throws IllegalArgumentException if capacity argument
   *  nonpositive
   */
  @Test def testConstructor2(): Unit = {
    try {
      new LinkedBlockingQueue[Integer](0)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Initializing from null Collection throws NullPointerException
   */
  @Test def testConstructor3(): Unit = {
    try {
      new LinkedBlockingQueue[Integer](null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Initializing from Collection of null elements throws NullPointerException
   */
  @Test def testConstructor4(): Unit = {
    val elements: Collection[Integer] =
      Arrays.asList(new Array[Integer](SIZE): _*)
    try {
      new LinkedBlockingQueue[Integer](elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Initializing from Collection with some null elements throws
   *  NullPointerException
   */
  @Test def testConstructor5(): Unit = {
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE - 1) { ints(i) = Integer.valueOf(i) }
    val elements: Collection[Integer] = Arrays.asList(ints: _*)
    try {
      new LinkedBlockingQueue[Integer](elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Queue contains all elements of collection used to initialize
   */
  @Test def testConstructor6(): Unit = {
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE) { ints(i) = Integer.valueOf(i) }
    val q: LinkedBlockingQueue[_] =
      new LinkedBlockingQueue[Integer](Arrays.asList(ints: _*))
    for (i <- 0 until SIZE) { assertEquals(ints(i), q.poll) }
  }

  /** Queue transitions from empty to full when elements added
   */
  @Test def testEmptyFull(): Unit = {
    val q = new LinkedBlockingQueue[Integer](2)
    assertTrue(q.isEmpty)
    assertEquals("should have room for 2", 2, q.remainingCapacity)
    q.add(one)
    assertFalse(q.isEmpty)
    q.add(two)
    assertFalse(q.isEmpty)
    assertEquals(0, q.remainingCapacity)
    assertFalse(q.offer(three))
  }

  /** remainingCapacity decreases on add, increases on remove
   */
  @Test def testRemainingCapacity(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertEquals(i, q.remainingCapacity)
      assertEquals(SIZE, q.size + q.remainingCapacity)
      assertEquals(i, q.remove())
    }
    for (i <- 0 until SIZE) {
      assertEquals(SIZE - i, q.remainingCapacity)
      assertEquals(SIZE, q.size + q.remainingCapacity)
      assertTrue(q.add(i))
    }
  }

  /** Offer succeeds if not full; fails if full
   */
  @Test def testOffer(): Unit = {
    val q = new LinkedBlockingQueue[Integer](1)
    assertTrue(q.offer(zero))
    assertFalse(q.offer(one))
  }

  /** add succeeds if not full; throws IllegalStateException if full
   */
  @Test def testAdd(): Unit = {
    val q = new LinkedBlockingQueue[Integer](SIZE)
    for (i <- 0 until SIZE) { assertTrue(q.add(Integer.valueOf(i))) }
    assertEquals(0, q.remainingCapacity)
    try {
      q.add(Integer.valueOf(SIZE))
      shouldThrow()
    } catch {
      case success: IllegalStateException =>

    }
  }

  /** addAll(this) throws IllegalArgumentException
   */
  @Test def testAddAllSelf(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testAddAll3(): Unit = {
    val q = new LinkedBlockingQueue[Integer](SIZE)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE - 1) { ints(i) = Integer.valueOf(i) }
    val elements: Collection[Integer] = Arrays.asList(ints: _*)
    try {
      q.addAll(elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** addAll throws IllegalStateException if not enough room
   */
  @Test def testAddAll4(): Unit = {
    val q = new LinkedBlockingQueue[Integer](SIZE - 1)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE) { ints(i) = Integer.valueOf(i) }
    val elements: Collection[Integer] = Arrays.asList(ints: _*)
    try {
      q.addAll(elements)
      shouldThrow()
    } catch {
      case success: IllegalStateException =>

    }
  }

  /** Queue contains all elements, in traversal order, of successful addAll
   */
  @Test def testAddAll5(): Unit = {
    val empty: Array[Integer] = new Array[Integer](0)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE) { ints(i) = Integer.valueOf(i) }
    val q = new LinkedBlockingQueue[Integer](SIZE)
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(ints: _*)))
    for (i <- 0 until SIZE) { assertEquals(ints(i), q.poll) }
  }

  /** all elements successfully put are contained
   */
  @throws[InterruptedException]
  @Test def testPut(): Unit = {
    val q = new LinkedBlockingQueue[Integer](SIZE)
    for (i <- 0 until SIZE) {
      val x: Integer = Integer.valueOf(i)
      q.put(x)
      assertTrue(q.contains(x))
    }
    assertEquals(0, q.remainingCapacity)
  }

  /** put blocks interruptibly if full
   */
  @throws[InterruptedException]
  @Test def testBlockingPut(): Unit = {
    val q = new LinkedBlockingQueue[Integer](SIZE)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) { q.put(i) }
        assertEquals(SIZE, q.size)
        assertEquals(0, q.remainingCapacity)
        Thread.currentThread.interrupt()
        assertThrows(classOf[InterruptedException], () => q.put(99))
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        assertThrows(classOf[InterruptedException], () => q.put(99))
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.WAITING) }
    t.interrupt()
    awaitTermination(t)
    assertEquals(SIZE, q.size)
    assertEquals(0, q.remainingCapacity)
  }

  /** put blocks interruptibly waiting for take when full
   */
  @throws[InterruptedException]
  @Test def testPutWithTake(): Unit = {
    val capacity: Int = 2
    val q = new LinkedBlockingQueue[Integer](2)
    val pleaseTake: CountDownLatch = new CountDownLatch(1)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until capacity) { q.put(i) }
        pleaseTake.countDown()
        q.put(86)
        Thread.currentThread.interrupt()
        try {
          q.put(99)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        try {
          q.put(99)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseTake)
    assertEquals(0, q.remainingCapacity)
    assertEquals(0, q.take)
    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.WAITING) }
    t.interrupt()
    awaitTermination(t)
    assertEquals(0, q.remainingCapacity)
  }

  /** timed offer times out if full and elements not taken
   */
  @Test def testTimedOffer(): Unit = {
    val q = new LinkedBlockingQueue[Any](2)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        q.put(new Object {})
        q.put(new Object {})
        val startTime: Long = System.nanoTime
        assertFalse(q.offer(new Object {}, timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        Thread.currentThread.interrupt()
        try {
          q.offer(new Object {}, randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        try {
          q.offer(new Object {}, LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.TIMED_WAITING) }
    t.interrupt()
    awaitTermination(t)
  }

  /** take retrieves elements in FIFO order
   */
  @throws[InterruptedException]
  @Test def testTake(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.take) }
  }

  /** Take removes existing elements until empty, then blocks interruptibly
   */
  @throws[InterruptedException]
  @Test def testBlockingTake(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) { assertEquals(i, q.take) }
        Thread.currentThread.interrupt()
        try {
          q.take
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        try {
          q.take
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.WAITING) }
    t.interrupt()
    awaitTermination(t)
  }

  /** poll succeeds unless empty
   */
  @Test def testPoll(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.poll) }
    assertNull(q.poll)
  }

  /** timed poll with zero timeout succeeds when non-empty, else times out
   */
  @throws[InterruptedException]
  @Test def testTimedPoll0(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.poll(0, MILLISECONDS)) }
    assertNull(q.poll(0, MILLISECONDS))
  }

  /** timed poll with nonzero timeout succeeds when non-empty, else times out
   */
  @throws[InterruptedException]
  @Test def testTimedPoll(): Unit = {
    val q: LinkedBlockingQueue[Integer] =
      LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      val startTime: Long = System.nanoTime
      assertEquals(i, q.poll(LONG_DELAY_MS, MILLISECONDS).asInstanceOf[Int])
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
    val startTime: Long = System.nanoTime
    assertNull(q.poll(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    checkEmpty(q)
  }

  /** Interrupted timed poll throws InterruptedException instead of returning
   *  timeout status
   */
  @throws[InterruptedException]
  @Test def testInterruptedTimedPoll(): Unit = {
    val q: BlockingQueue[Integer] = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) {
          assertEquals(i, q.poll(LONG_DELAY_MS, MILLISECONDS).asInstanceOf[Int])
        }
        Thread.currentThread.interrupt()
        try {
          q.poll(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        try {
          q.poll(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.TIMED_WAITING) }
    t.interrupt()
    awaitTermination(t)
    checkEmpty(q)
  }

  /** peek returns next element, or null if empty
   */
  @Test def testPeek(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertEquals(i, q.peek)
      assertEquals(i, q.poll)
      assertTrue(q.peek == null || !(q.peek == i))
    }
    assertNull(q.peek)
  }

  /** element returns next element, or throws NSEE if empty
   */
  @Test def testElement(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertEquals(i, q.element)
      assertEquals(i, q.poll)
    }
    try {
      q.element
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  /** remove removes next element, or throws NSEE if empty
   */
  @Test def testRemove(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.remove()) }
    try {
      q.remove()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  /** An add following remove(x) succeeds
   */
  @throws[InterruptedException]
  @Test def testRemoveElementAndAdd(): Unit = {
    val q = new LinkedBlockingQueue[Integer]
    assertTrue(q.add(Integer.valueOf(1)))
    assertTrue(q.add(Integer.valueOf(2)))
    assertTrue(q.remove(Integer.valueOf(1)))
    assertTrue(q.remove(Integer.valueOf(2)))
    assertTrue(q.add(Integer.valueOf(3)))
    assertNotNull(q.take)
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertTrue(q.contains(Integer.valueOf(i)))
      q.poll
      assertFalse(q.contains(Integer.valueOf(i)))
    }
  }

  /** clear removes all elements
   */
  @Test def testClear(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    q.clear()
    assertTrue(q.isEmpty)
    assertEquals(0, q.size)
    assertEquals(SIZE, q.remainingCapacity)
    q.add(one)
    assertFalse(q.isEmpty)
    assertTrue(q.contains(one))
    q.clear()
    assertTrue(q.isEmpty)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val p = new LinkedBlockingQueue[Integer](SIZE)
    for (i <- 0 until SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      p.add(Integer.valueOf(i))
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if changed
   */
  @Test def testRetainAll(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val p: LinkedBlockingQueue[_] = LinkedBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      val changed: Boolean = q.retainAll(p)
      if (i == 0) { assertFalse(changed) }
      else { assertTrue(changed) }
      assertTrue(q.containsAll(p))
      assertEquals(SIZE - i, q.size)
      p.remove()
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testRemoveAll(): Unit = {
    for (i <- 1 until SIZE) {
      val q: LinkedBlockingQueue[_] =
        LinkedBlockingQueueTest.populatedQueue(SIZE)
      val p: LinkedBlockingQueue[_] = LinkedBlockingQueueTest.populatedQueue(i)
      assertTrue(q.removeAll(p))
      assertEquals(SIZE - i, q.size)
      for (j <- 0 until i) {
        val x: Integer = (p.remove()).asInstanceOf[Integer]
        assertFalse(q.contains(x))
      }
    }
  }

  /** toArray contains all elements in FIFO order
   */
  @Test def testToArray(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val a: Array[AnyRef] = q.toArray
    assertSame(classOf[Array[AnyRef]], a.getClass)
    for (o <- a) { assertSame(o, q.poll) }
    assertTrue(q.isEmpty)
  }

  /** toArray(a) contains all elements in FIFO order
   */
  @throws[InterruptedException]
  @Test def testToArray2(): Unit = {
    val q: LinkedBlockingQueue[Integer] =
      LinkedBlockingQueueTest.populatedQueue(SIZE)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    val array: Array[Integer] = q.toArray(ints)
    assertSame(ints, array)
    for (o <- ints) { assertSame(o, q.poll) }
    assertTrue(q.isEmpty)
  }

  /** toArray(incompatible array type) throws ArrayStoreException
   */
  @Ignore("No distinguishment in Array component types in Scala Native")
  @Test def testToArray1_BadArg(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case success: ArrayStoreException =>

    }
  }

  /** iterator iterates through all elements
   */
  @throws[InterruptedException]
  @Test def testIterator(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    var it: Iterator[_] = q.iterator
    var i: Int = 0
    i = 0
    while ({ it.hasNext }) {
      assertTrue(q.contains(it.next))
      i += 1
    }
    assertEquals(i, SIZE)
    assertIteratorExhausted(it)
    it = q.iterator
    i = 0
    while ({ it.hasNext }) {
      assertEquals(it.next, q.take)
      i += 1
    }
    assertEquals(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(new LinkedBlockingQueue[Integer]().iterator)
  }

  /** iterator.remove removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q = new LinkedBlockingQueue[Integer](3)
    q.add(two)
    q.add(one)
    q.add(three)
    var it: Iterator[_] = q.iterator
    it.next
    it.remove()
    it = q.iterator
    assertSame(it.next, one)
    assertSame(it.next, three)
    assertFalse(it.hasNext)
  }

  /** iterator ordering is FIFO
   */
  @Test def testIteratorOrdering(): Unit = {
    val q = new LinkedBlockingQueue[Integer](3)
    q.add(one)
    q.add(two)
    q.add(three)
    assertEquals(0, q.remainingCapacity)
    var k: Int = 0
    val it: Iterator[_] = q.iterator
    while ({ it.hasNext }) { assertEquals({ k += 1; k }, it.next) }
    assertEquals(3, k)
  }

  /** Modifications do not cause iterators to fail
   */
  @Test def testWeaklyConsistentIteration(): Unit = {
    val q = new LinkedBlockingQueue[Integer](3)
    q.add(one)
    q.add(two)
    q.add(three)
    val it: Iterator[_] = q.iterator
    while ({ it.hasNext }) {
      q.remove()
      it.next
    }
    assertEquals(0, q.size)
  }

  /** toString contains toStrings of elements
   */
  @Test def testToString(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val s: String = q.toString
    for (i <- 0 until SIZE) { assertTrue(s.contains(String.valueOf(i))) }
  }

  // TODO: ThreadPoolExecutor
  // /** offer transfers elements across Executor tasks
  //  */
  // @Test def testOfferInExecutor(): Unit = {
  //   val q = new LinkedBlockingQueue[Integer](2)
  //   q.add(one)
  //   q.add(two)
  //   val threadsStarted = new CheckedBarrier(2)
  //   usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
  //     executor.execute(new CheckedRunnable() {
  //       @throws[InterruptedException]
  //       override def realRun(): Unit = {
  //         assertFalse(q.offer(three))
  //         threadsStarted.await
  //         assertTrue(q.offer(three, LONG_DELAY_MS, MILLISECONDS))
  //         assertEquals(0, q.remainingCapacity)
  //       }
  //     })
  //     executor.execute(new CheckedRunnable() {
  //       @throws[InterruptedException]
  //       override def realRun(): Unit = {
  //         threadsStarted.await
  //         assertSame(one, q.take)
  //       }
  //     })
  //   }
  // }

  // /** timed poll retrieves elements across Executor threads
  //  */
  // @Test def testPollInExecutor(): Unit = {
  //   val q = new LinkedBlockingQueue[Integer](2)
  //   val threadsStarted = new CheckedBarrier(2)
  //   usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
  //     executor.execute(new CheckedRunnable() {
  //       @throws[InterruptedException]
  //       override def realRun(): Unit = {
  //         assertNull(q.poll)
  //         threadsStarted.await
  //         assertSame(one, q.poll(LONG_DELAY_MS, MILLISECONDS))
  //         checkEmpty(q)
  //       }
  //     })
  //     executor.execute(new CheckedRunnable() {
  //       @throws[InterruptedException]
  //       override def realRun(): Unit = {
  //         threadsStarted.await
  //         q.put(one)
  //       }
  //     })
  //   }
  // }

  /** A deserialized/reserialized queue has same elements in same order
   */
  @throws[Exception]
  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = {}

  /** drainTo(c) empties queue into another collection c
   */
  @Test def testDrainTo(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val l = new ArrayList[Any]
    q.drainTo(l)
    assertEquals(0, q.size)
    assertEquals(SIZE, l.size)
    for (i <- 0 until SIZE) { assertEquals(l.get(i), Integer.valueOf(i)) }
    q.add(zero)
    q.add(one)
    assertFalse(q.isEmpty)
    assertTrue(q.contains(zero))
    assertTrue(q.contains(one))
    l.clear()
    q.drainTo(l)
    assertEquals(0, q.size)
    assertEquals(2, l.size)
    for (i <- 0 until 2) { assertEquals(l.get(i), Integer.valueOf(i)) }
  }

  /** drainTo empties full queue, unblocking a waiting put.
   */
  @throws[InterruptedException]
  @Test def testDrainToWithActivePut(): Unit = {
    val q = LinkedBlockingQueueTest.populatedQueue(SIZE)
    val t: Thread = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { q.put(Integer.valueOf(SIZE + 1)) }
    })
    t.start()
    val l = new ArrayList[Any]
    q.drainTo(l)
    assertTrue(l.size >= SIZE)
    for (i <- 0 until SIZE) { assertEquals(l.get(i), Integer.valueOf(i)) }
    t.join()
    assertTrue(q.size + l.size >= SIZE)
  }

  /** drainTo(c, n) empties first min(n, size) elements of queue into c
   */
  @Test def testDrainToN(): Unit = {
    val q = new LinkedBlockingQueue[Integer]
    for (i <- 0 until SIZE + 2) {
      for (j <- 0 until SIZE) { assertTrue(q.offer(Integer.valueOf(j))) }
      val l = new ArrayList[Any]
      q.drainTo(l, i)
      val k: Int = if ((i < SIZE)) { i }
      else { SIZE }
      assertEquals(k, l.size)
      assertEquals(SIZE - k, q.size)
      for (j <- 0 until k) { assertEquals(l.get(j), Integer.valueOf(j)) }
      while (q.poll() != null) ()
    }
  }

  /** remove(null), contains(null) always return false
   */
  @Test def testNeverContainsNull(): Unit = {
    val qs: Array[Collection[_]] = Array(
      new LinkedBlockingQueue[AnyRef],
      LinkedBlockingQueueTest.populatedQueue(2)
    )
    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
    }
  }
}
