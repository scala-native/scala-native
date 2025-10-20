/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert.*
import org.junit.{Test, Ignore}

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util.*
import java.util.concurrent.*
import JSR166Test.*

class ArrayBlockingQueueFairTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    ArrayBlockingQueueTest
      .populatedQueue(0, SIZE, 2 * SIZE, true)
      .asInstanceOf[BlockingQueue[Any]]
}
class ArrayBlockingQueueNonFairTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    ArrayBlockingQueueTest
      .populatedQueue(0, SIZE, 2 * SIZE, false)
      .asInstanceOf[BlockingQueue[Any]]
}

object ArrayBlockingQueueTest {
  import JSR166Test.*

  /** Returns a new queue of given size containing consecutive Integers 0 ... n
   *  \- 1.
   */
  def populatedQueue(n: Int): ArrayBlockingQueue[Integer] =
    populatedQueue(n, n, n, false)

  /** Returns a new queue of given size containing consecutive Integers 0 ... n
   *  \- 1, with given capacity range and fairness.
   */
  def populatedQueue(
      size: Int,
      minCapacity: Int,
      maxCapacity: Int,
      fair: Boolean
  ): ArrayBlockingQueue[Integer] = {
    val rnd: ThreadLocalRandom = ThreadLocalRandom.current
    val capacity: Int = rnd.nextInt(minCapacity, maxCapacity + 1)
    val q: ArrayBlockingQueue[Integer] =
      new ArrayBlockingQueue[Integer](capacity)
    assertTrue(q.isEmpty)
// shuffle circular array elements so they wrap
    val n: Int = rnd.nextInt(capacity)
    for (i <- 0 until n) { q.add(42) }
    for (i <- 0 until n) { q.remove() }

    for (i <- 0 until size) { assertTrue(q.offer(i.asInstanceOf[Integer])) }
    assertEquals(size == 0, q.isEmpty)
    assertEquals(capacity - size, q.remainingCapacity)
    assertEquals(size, q.size)
    if (size > 0) { assertEquals(0.asInstanceOf[Integer], q.peek) }
    return q
  }
}

class ArrayBlockingQueueTest extends JSR166Test {
  import JSR166Test.*

  /** A new queue has the indicated capacity
   */
  @Test def testConstructor1(): Unit = {
    assertEquals(SIZE, new ArrayBlockingQueue[Any](SIZE).remainingCapacity)
  }

  /** Constructor throws IllegalArgumentException if capacity argument
   *  nonpositive
   */
  @Test def testConstructor_nonPositiveCapacity(): Unit = {
    for (i <- Array[Int](0, -(1), Integer.MIN_VALUE)) {
      try {
        new ArrayBlockingQueue[Any](i)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
      for (fair <- Array[Boolean](true, false)) {
        try {
          new ArrayBlockingQueue[Any](i, fair)
          shouldThrow()
        } catch {
          case success: IllegalArgumentException =>

        }
      }
    }
  }

  /** Initializing from null Collection throws NPE
   */
  @Test def testConstructor_nullCollection(): Unit = {
    try {
      new ArrayBlockingQueue[Any](1, true, null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Initializing from Collection of null elements throws NPE
   */
  @Test def testConstructor4(): Unit = {
    val elements: Collection[Integer] =
      Arrays.asList(new Array[Integer](SIZE)*)
    try {
      new ArrayBlockingQueue[Any](SIZE, false, elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Initializing from Collection with some null elements throws NPE
   */
  @Test def testConstructor5(): Unit = {
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE - 1) { ints(i) = i }
    val elements: Collection[Integer] = Arrays.asList(ints*)
    try {
      new ArrayBlockingQueue[Any](SIZE, false, elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Initializing from too large collection throws IllegalArgumentException
   */
  @Test def testConstructor_collectionTooLarge()
      : Unit = { // just barely fits - succeeds
    new ArrayBlockingQueue[Any](SIZE, false, Collections.nCopies(SIZE, ""))
    try {
      new ArrayBlockingQueue[Any](
        SIZE - 1,
        false,
        Collections.nCopies(SIZE, "")
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Queue contains all elements of collection used to initialize
   */
  @Test def testConstructor7(): Unit = {
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE) { ints(i) = i }
    val elements: Collection[Integer] = Arrays.asList(ints*)
    val q: ArrayBlockingQueue[Any] =
      new ArrayBlockingQueue[Any](SIZE, true, elements)
    for (i <- 0 until SIZE) { assertEquals(ints(i), q.poll) }
  }

  /** Queue transitions from empty to full when elements added
   */
  @Test def testEmptyFull(): Unit = {
    val q: BlockingQueue[Integer] =
      ArrayBlockingQueueTest.populatedQueue(0, 2, 2, false)
    assertTrue(q.isEmpty)
    assertEquals(2, q.remainingCapacity)
    q.add(one)
    assertFalse(q.isEmpty)
    assertTrue(q.offer(two))
    assertFalse(q.isEmpty)
    assertEquals(0, q.remainingCapacity)
    assertFalse(q.offer(three))
  }

  /** remainingCapacity decreases on add, increases on remove
   */
  @Test def testRemainingCapacity(): Unit = {
    val size: Int = ThreadLocalRandom.current.nextInt(1, SIZE)
    val q =
      ArrayBlockingQueueTest.populatedQueue(size, size, 2 * size, false)
    val spare: Int = q.remainingCapacity
    val capacity: Int = spare + size
    for (i <- 0 until size) {
      assertEquals(spare + i, q.remainingCapacity)
      assertEquals(capacity, q.size + q.remainingCapacity)
      assertEquals(i, q.remove())
    }
    for (i <- 0 until size) {
      assertEquals(capacity - i, q.remainingCapacity)
      assertEquals(capacity, q.size + q.remainingCapacity)
      assertTrue(q.add(i))
    }
  }

  /** Offer succeeds if not full; fails if full
   */
  @Test def testOffer(): Unit = {
    val q = new ArrayBlockingQueue[Any](1)
    assertTrue(q.offer(zero))
    assertFalse(q.offer(one))
  }

  /** add succeeds if not full; throws IllegalStateException if full
   */
  @Test def testAdd(): Unit = {
    val q = new ArrayBlockingQueue[Any](SIZE)
    for (i <- 0 until SIZE) { assertTrue(q.add(i.asInstanceOf[Integer])) }
    assertEquals(0, q.remainingCapacity)
    try {
      q.add(SIZE.asInstanceOf[Integer])
      shouldThrow()
    } catch {
      case success: IllegalStateException =>

    }
  }

  /** addAll(this) throws IllegalArgumentException
   */
  @Test def testAddAllSelf(): Unit = {
    val q: ArrayBlockingQueue[Integer] =
      ArrayBlockingQueueTest.populatedQueue(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException => ()

    }
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testAddAll3(): Unit = {
    val q = new ArrayBlockingQueue[Any](SIZE)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE - 1) { ints(i) = Integer.valueOf(i) }
    try {
      q.addAll(Arrays.asList(ints*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** addAll throws IllegalStateException if not enough room
   */
  @Test def testAddAll_insufficientSpace(): Unit = {
    val size: Int = ThreadLocalRandom.current.nextInt(1, SIZE)
    var q: ArrayBlockingQueue[Integer] =
      ArrayBlockingQueueTest.populatedQueue(0, size, size, false)
// Just fits:
    q.addAll(ArrayBlockingQueueTest.populatedQueue(size, size, 2 * size, false))
    assertEquals(0, q.remainingCapacity)
    assertEquals(size, q.size)
    assertEquals(0, q.peek)
    try {
      q = ArrayBlockingQueueTest.populatedQueue(0, size, size, false)
      q.addAll(Collections.nCopies(size + 1, 42))
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
    val q = new ArrayBlockingQueue[Any](SIZE)
    assertFalse(q.addAll(Arrays.asList(empty*)))
    assertTrue(q.addAll(Arrays.asList(ints*)))
    for (i <- 0 until SIZE) { assertEquals(ints(i), q.poll) }
  }

  /** all elements successfully put are contained
   */
  @throws[InterruptedException]
  @Test def testPut(): Unit = {
    val q = new ArrayBlockingQueue[Any](SIZE)
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
    val q = new ArrayBlockingQueue[Any](SIZE)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) { q.put(i) }
        assertEquals(SIZE, q.size)
        assertEquals(0, q.remainingCapacity)
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
    val q = new ArrayBlockingQueue[Any](capacity)
    val pleaseTake: CountDownLatch = new CountDownLatch(1)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until capacity) q.put(i)
        pleaseTake.countDown()
        q.put(86)

        Thread.currentThread.interrupt()
        try {
          q.put(99)
          shouldThrow()
        } catch {
          case success: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          q.put(99)
          shouldThrow()
        } catch {
          case success: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseTake)
    assertEquals(0, q.remainingCapacity())
    assertEquals(0, q.take())

    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.WAITING) }
    t.interrupt()
    awaitTermination(t)
    assertEquals(0, q.remainingCapacity)
  }

  /** timed offer times out if full and elements not taken
   */
  @Test def testTimedOffer(): Unit = {
    val q = new ArrayBlockingQueue[Any](2)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        q.put(new Object {})
        q.put(new Object {})

        val startTime: Long = System.nanoTime()
        assertFalse(q.offer(new Object {}, timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())

        Thread.currentThread.interrupt()
        try {
          q.offer(new Object {}, randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case success: InterruptedException => ()
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.take) }
  }

  /** Take removes existing elements until empty, then blocks interruptibly
   */
  @throws[InterruptedException]
  @Test def testBlockingTake(): Unit = {
    val q: ArrayBlockingQueue[Integer] =
      ArrayBlockingQueueTest.populatedQueue(SIZE)
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.poll) }
    assertNull(q.poll)
  }

  /** timed poll with zero timeout succeeds when non-empty, else times out
   */
  @throws[InterruptedException]
  @Test def testTimedPoll0(): Unit = {
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.poll(0, MILLISECONDS)) }
    assertNull(q.poll(0, MILLISECONDS))
    checkEmpty(q)
  }

  /** timed poll with nonzero timeout succeeds when non-empty, else times out
   */
  @throws[InterruptedException]
  @Test def testTimedPoll(): Unit = {
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      val startTime: Long = System.nanoTime
      assertEquals(i, q.poll(LONG_DELAY_MS, MILLISECONDS))
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
    val q: BlockingQueue[Integer] = ArrayBlockingQueueTest.populatedQueue(SIZE)
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.remove()) }
    try {
      q.remove()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains(): Unit = {
    val size: Int = ThreadLocalRandom.current.nextInt(1, SIZE)
    val q = ArrayBlockingQueueTest.populatedQueue(size, size, 2 * size, false)
    assertFalse(q.contains(null))
    for (i <- 0 until size) {
      assertTrue(q.contains(Integer.valueOf(i)))
      assertEquals(i, q.poll)
      assertFalse(q.contains(Integer.valueOf(i)))
    }
  }

  /** clear removes all elements
   */
  @Test def testClear(): Unit = {
    val size: Int = ThreadLocalRandom.current.nextInt(1, 5)
    val q = ArrayBlockingQueueTest.populatedQueue(size, size, 2 * size, false)
    val capacity: Int = size + q.remainingCapacity
    q.clear()
    assertTrue(q.isEmpty)
    assertEquals(0, q.size)
    assertEquals(capacity, q.remainingCapacity)
    q.add(one)
    assertFalse(q.isEmpty)
    assertTrue(q.contains(one))
    q.clear()
    assertTrue(q.isEmpty)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    val p: ArrayBlockingQueue[Any] = new ArrayBlockingQueue[Any](SIZE)
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    val p = ArrayBlockingQueueTest.populatedQueue(SIZE)
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
      val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
      val p = ArrayBlockingQueueTest.populatedQueue(i)
      assertTrue(q.removeAll(p))
      assertEquals(SIZE - i, q.size)
      for (j <- 0 until i) {
        val x: Integer = (p.remove()).asInstanceOf[Integer]
        assertFalse(q.contains(x))
      }
    }
  }

  def checkToArray(q: ArrayBlockingQueue[Integer]): Unit = {
    val size: Int = q.size
    val a1 = q.toArray().asInstanceOf[Array[Object]]
    assertEquals(size, a1.length)
    val a2 = q.toArray(new Array[Integer](0))
    assertEquals(size, a2.length)
    val a3 = q.toArray(new Array[Integer](Math.max(0, size - 1)))
    assertEquals(size, a3.length)
    val a4 = new Array[Integer](size)
    assertSame(a4, q.toArray(a4))
    val a5 = Array.fill(size + 1)(42: Integer)
    assertSame(a5, q.toArray(a5))
    val a6 = Array.fill(size + 2)(42: Integer)
    assertSame(a6, q.toArray(a6))
    val as = Seq(a1, a2, a3, a4, a5, a6)
    for (a <- as) {
      if (a.length > size) { assertNull(a(size)) }
      if (a.length > size + 1) { assertEquals(42, a(size + 1)) }
    }
    val it: Iterator[?] = q.iterator
    val s: Integer = q.peek
    for (i <- 0 until size) {
      val x: Integer = it.next.asInstanceOf[Integer]
      assertEquals(s + i, x.asInstanceOf[Int])
      for (a <- as) { assertSame(a(i), x) }
    }
  }

  /** toArray() and toArray(a) contain all elements in FIFO order
   */
  @Test def testToArray(): Unit = {
    val rnd = ThreadLocalRandom.current
    val size: Int = rnd.nextInt(6)
    val capacity: Int = Math.max(1, size + rnd.nextInt(size + 1))
    val q = new ArrayBlockingQueue[Integer](capacity)
    for (i <- 0 until size) {
      checkToArray(q)
      q.add(i)
    }
// Provoke wraparound
    val added: Int = size * 2
    for (i <- 0 until added) {
      checkToArray(q)
      assertEquals(i.asInstanceOf[Integer], q.poll())
      q.add(size + i)
    }
    for (i <- 0 until size) {
      checkToArray(q)
      assertEquals((added + i).asInstanceOf[Integer], q.poll())
    }
  }

  /** toArray(incompatible array type) throws ArrayStoreException
   */
  @Ignore("No support for Array component type checks in SN")
  @Test def testToArray_incompatibleArrayType(): Unit = {
    val q: BlockingQueue[Integer] = ArrayBlockingQueueTest.populatedQueue(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case success: ArrayStoreException =>

    }
    try {
      q.toArray(new Array[String](0))
      shouldThrow()
    } catch {
      case success: ArrayStoreException => ()

    }
  }

  /** iterator iterates through all elements
   */
  @throws[InterruptedException]
  @Test def testIterator(): Unit = {
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    var it: Iterator[?] = q.iterator
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
    assertIteratorExhausted(new ArrayBlockingQueue[Any](SIZE).iterator)
  }

  /** iterator.remove removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q = new ArrayBlockingQueue[Any](3)
    q.add(two)
    q.add(one)
    q.add(three)
    var it: Iterator[?] = q.iterator
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
    val q = new ArrayBlockingQueue[Any](3)
    q.add(one)
    q.add(two)
    q.add(three)
    assertEquals("queue should be full", 0, q.remainingCapacity)
    var k: Int = 0
    val it: Iterator[?] = q.iterator
    while ({ it.hasNext }) { assertEquals({ k += 1; k }, it.next) }
    assertEquals(3, k)
  }

  /** Modifications do not cause iterators to fail
   */
  @Test def testWeaklyConsistentIteration(): Unit = {
    val q = new ArrayBlockingQueue[Any](3)
    q.add(one)
    q.add(two)
    q.add(three)
    val it: Iterator[?] = q.iterator
    while ({ it.hasNext }) {
      q.remove()
      it.next
    }
    assertEquals(0, q.size)
  }

  /** toString contains toStrings of elements
   */
  @Test def testToString(): Unit = {
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
    val s: String = q.toString
    for (i <- 0 until SIZE) { assertTrue(s.contains(String.valueOf(i))) }
  }

  /** offer transfers elements across Executor tasks
   */
  @Test def testOfferInExecutor(): Unit =
    usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
      val q = new ArrayBlockingQueue[Any](2)
      q.add(one)
      q.add(two)
      val threadsStarted: CheckedBarrier = new CheckedBarrier(2)
      executor.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          assertFalse(q.offer(three))
          threadsStarted.await
          assertTrue(q.offer(three, LONG_DELAY_MS, MILLISECONDS))
          assertEquals(0, q.remainingCapacity)
        }
      })
      executor.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadsStarted.await
          assertEquals(0, q.remainingCapacity)
          assertSame(one, q.take)
        }
      })
    }

  /** timed poll retrieves elements across Executor threads
   */
  @Test def testPollInExecutor(): Unit =
    usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
      val q = new ArrayBlockingQueue[Any](2)
      val threadsStarted = new CheckedBarrier(2)
      executor.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          assertNull(q.poll)
          threadsStarted.await
          assertSame(one, q.poll(LONG_DELAY_MS, MILLISECONDS))
          checkEmpty(q)
        }
      })
      executor.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadsStarted.await
          q.put(one)
        }
      })
    }

  /** A deserialized/reserialized queue has same elements in same order
   */
  @throws[Exception]
  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = {}

  /** drainTo(c) empties queue into another collection c
   */
  @Test def testDrainTo(): Unit = {
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
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
    val q = ArrayBlockingQueueTest.populatedQueue(SIZE)
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
    val q = new ArrayBlockingQueue[Any](SIZE * 2)
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
    val qs: Array[Collection[?]] = Array(
      ArrayBlockingQueueTest.populatedQueue(0, 1, 10, false),
      ArrayBlockingQueueTest.populatedQueue(2, 2, 10, true)
    )
    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
    }
  }
}
