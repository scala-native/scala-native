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
import JSR166Test.*

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util.*
import java.util.concurrent.*

class PriorityBlockingQueueGenericTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] =
    new PriorityBlockingQueue[Any]
}
class PriorityBlockingQueueInitialCapacityTest extends BlockingQueueTest {
  override protected def emptyCollection(): BlockingQueue[Any] = {
    val rnd: ThreadLocalRandom = ThreadLocalRandom.current
    val initialCapacity: Int = rnd.nextInt(1, SIZE)
    new PriorityBlockingQueue[Any](initialCapacity)
  }
}

object PriorityBlockingQueueTest {

  class MyReverseComparator extends Comparator[Any] with Serializable {
    override def compare(x: Any, y: Any): Int = {
      return (y.asInstanceOf[Comparable[Any]]).compareTo(x)
    }
  }

  private def populatedQueue(n: Int): PriorityBlockingQueue[Integer] = {
    val q: PriorityBlockingQueue[Integer] =
      new PriorityBlockingQueue[Integer](n)
    assertTrue(q.isEmpty)
    var i: Int = n - 1
    while ({ i >= 0 }) {
      assertTrue(q.offer(Integer.valueOf(i)))
      i -= 2
    }
    i = (n & 1)
    while ({ i < n }) {
      assertTrue(q.offer(Integer.valueOf(i)))
      i += 2
    }
    assertFalse(q.isEmpty)
    assertEquals(Integer.MAX_VALUE, q.remainingCapacity)
    assertEquals(n, q.size)
    assertEquals(0.asInstanceOf[Integer], q.peek)
    return q
  }
}
class PriorityBlockingQueueTest extends JSR166Test {

  @Test def testConstructor1(): Unit = {
    assertEquals(
      Integer.MAX_VALUE,
      new PriorityBlockingQueue[Integer](SIZE).remainingCapacity
    )
  }

  @Test def testConstructor2(): Unit = {
    try {
      new PriorityBlockingQueue[Integer](0)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  @Test def testConstructor3(): Unit = {
    try {
      new PriorityBlockingQueue[Integer](null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  @Test def testConstructor4(): Unit = {
    val elements: Collection[Integer] =
      Arrays.asList(new Array[Integer](SIZE)*)
    try {
      new PriorityBlockingQueue[Integer](elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  @Test def testConstructor5(): Unit = {
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE - 1) { ints(i) = i }
    val elements: Collection[Integer] = Arrays.asList(ints*)
    try {
      new PriorityBlockingQueue[Integer](elements)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  @Test def testConstructor6(): Unit = {
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE) { ints(i) = i }
    val q =
      new PriorityBlockingQueue[Integer](Arrays.asList(ints*))
    for (i <- 0 until SIZE) { assertEquals(ints(i), q.poll) }
  }

  @Test def testConstructor7(): Unit = {
    val cmp: PriorityBlockingQueueTest.MyReverseComparator =
      new PriorityBlockingQueueTest.MyReverseComparator
    val q = new PriorityBlockingQueue[Integer](SIZE, cmp)
    assertEquals(cmp, q.comparator)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE) { ints(i) = Integer.valueOf(i) }
    q.addAll(Arrays.asList(ints*))
    for (i <- SIZE - 1 to 0 by -1) { assertEquals(ints(i), q.poll) }
  }

  @Test def testEmpty(): Unit = {
    val q = new PriorityBlockingQueue[Integer](2)
    assertTrue(q.isEmpty)
    assertEquals(Integer.MAX_VALUE, q.remainingCapacity)
    q.add(one)
    assertFalse(q.isEmpty)
    q.add(two)
    q.remove()
    q.remove()
    assertTrue(q.isEmpty)
  }

  @Test def testRemainingCapacity(): Unit = {
    val q = PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertEquals(Integer.MAX_VALUE, q.remainingCapacity)
      assertEquals(SIZE - i, q.size)
      assertEquals(i, q.remove())
    }
    for (i <- 0 until SIZE) {
      assertEquals(Integer.MAX_VALUE, q.remainingCapacity)
      assertEquals(i, q.size)
      assertTrue(q.add(i))
    }
  }

  @Test def testOffer(): Unit = {
    val q = new PriorityBlockingQueue[Integer](1)
    assertTrue(q.offer(zero))
    assertTrue(q.offer(one))
  }

  @Test def testOfferNonComparable(): Unit = {
    val q = new PriorityBlockingQueue[Any](1)
    try {
      q.offer(new Object {})
      shouldThrow()
    } catch {
      case success: ClassCastException =>
        assertTrue(q.isEmpty)
        assertEquals(0, q.size)
        assertNull(q.poll)
    }
  }

  @Test def testAdd(): Unit = {
    val q = new PriorityBlockingQueue[Integer](SIZE)
    for (i <- 0 until SIZE) {
      assertEquals(i, q.size)
      assertTrue(q.add(Integer.valueOf(i)))
    }
  }

  @Test def testAddAllSelf(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  @Test def testAddAll3(): Unit = {
    val q = new PriorityBlockingQueue[Integer](SIZE)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- 0 until SIZE - 1) { ints(i) = Integer.valueOf(i) }
    try {
      q.addAll(Arrays.asList(ints*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  @Test def testAddAll5(): Unit = {
    val empty: Array[Integer] = new Array[Integer](0)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    for (i <- SIZE - 1 to 0 by -1) { ints(i) = Integer.valueOf(i) }
    val q = new PriorityBlockingQueue[Integer](SIZE)
    assertFalse(q.addAll(Arrays.asList(empty*)))
    assertTrue(q.addAll(Arrays.asList(ints*)))
    for (i <- 0 until SIZE) { assertEquals(ints(i), q.poll) }
  }

  @Test def testPut(): Unit = {
    val q = new PriorityBlockingQueue[Integer](SIZE)
    for (i <- 0 until SIZE) {
      val x: Integer = Integer.valueOf(i)
      q.put(x)
      assertTrue(q.contains(x))
    }
    assertEquals(SIZE, q.size)
  }

  @throws[InterruptedException]
  @Test def testPutWithTake(): Unit = {
    val q = new PriorityBlockingQueue[Integer](2)
    val size: Int = 4
    val t: Thread = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        for (i <- 0 until size) { q.put(Integer.valueOf(0)) }
      }
    })
    awaitTermination(t)
    assertEquals(size, q.size)
    q.take
  }

  @Test def testTimedOffer(): Unit = {
    val q = new PriorityBlockingQueue[Integer](2)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        q.put(Integer.valueOf(0))
        q.put(Integer.valueOf(0))
        assertTrue(q.offer(Integer.valueOf(0), SHORT_DELAY_MS, MILLISECONDS))
        assertTrue(q.offer(Integer.valueOf(0), LONG_DELAY_MS, MILLISECONDS))
      }
    })
    awaitTermination(t)
  }

  @throws[InterruptedException]
  @Test def testTake(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.take) }
  }

  @throws[InterruptedException]
  @Test def testBlockingTake(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    val pleaseInterrupt: CountDownLatch = new CountDownLatch(1)
    val t: Thread = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) { assertEquals(i, q.take) }
        Thread.currentThread.interrupt()
        assertThrows(classOf[InterruptedException], () => q.take())
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        assertThrows(classOf[InterruptedException], () => q.take())
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean()) { assertThreadBlocks(t, Thread.State.WAITING) }
    t.interrupt()
    awaitTermination(t)
  }

  @Test def testPoll(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.poll) }
    assertNull(q.poll)
  }

  @throws[InterruptedException]
  @Test def testTimedPoll0(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.poll(0, MILLISECONDS)) }
    assertNull(q.poll(0, MILLISECONDS))
  }

  @throws[InterruptedException]
  @Test def testTimedPoll(): Unit = {
    val q: PriorityBlockingQueue[Integer] =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
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

  @throws[InterruptedException]
  @Test def testInterruptedTimedPoll(): Unit = {
    val q: BlockingQueue[Integer] =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
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
  }

  @Test def testPeek(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertEquals(i, q.peek)
      assertEquals(i, q.poll)
      assertTrue(q.peek == null || !(q.peek == i))
    }
    assertNull(q.peek)
  }

  @Test def testElement(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
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

  @Test def testRemove(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) { assertEquals(i, q.remove()) }
    try {
      q.remove()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  @Test def testContains(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertTrue(q.contains(Integer.valueOf(i)))
      q.poll
      assertFalse(q.contains(Integer.valueOf(i)))
    }
  }

  @Test def testClear(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    q.clear()
    assertTrue(q.isEmpty)
    assertEquals(0, q.size)
    q.add(one)
    assertFalse(q.isEmpty)
    assertTrue(q.contains(one))
    q.clear()
    assertTrue(q.isEmpty)
  }

  @Test def testContainsAll(): Unit = {
    val q = PriorityBlockingQueueTest.populatedQueue(SIZE)
    val p = new PriorityBlockingQueue[Integer](SIZE)
    for (i <- 0 until SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      p.add(Integer.valueOf(i))
    }
    assertTrue(p.containsAll(q))
  }

  @Test def testRetainAll(): Unit = {
    val q = PriorityBlockingQueueTest.populatedQueue(SIZE)
    val p = PriorityBlockingQueueTest.populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      val changed: Boolean = q.retainAll(p)
      if (i == 0) { assertFalse(changed) }
      else { assertTrue(changed) }
      assertTrue(q.containsAll(p))
      assertEquals(SIZE - i, q.size)
      p.remove()
    }
  }

  @Test def testRemoveAll(): Unit = {
    for (i <- 1 until SIZE) {
      val q = PriorityBlockingQueueTest.populatedQueue(SIZE)
      val p = PriorityBlockingQueueTest.populatedQueue(i)
      assertTrue(q.removeAll(p))
      assertEquals(SIZE - i, q.size)
      for (j <- 0 until i) {
        val x: Integer = (p.remove()).asInstanceOf[Integer]
        assertFalse(q.contains(x))
      }
    }
  }

  @throws[InterruptedException]
  @Test def testToArray(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    val a: Array[AnyRef] = q.toArray
    assertSame(classOf[Array[AnyRef]], a.getClass)
    Arrays.sort(a)
    for (o <- a) { assertSame(o, q.take) }
    assertTrue(q.isEmpty)
  }

  @throws[InterruptedException]
  @Test def testToArray2(): Unit = {
    val q: PriorityBlockingQueue[Integer] =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    val ints: Array[Integer] = new Array[Integer](SIZE)
    val array: Array[Integer] = q.toArray(ints)
    assertSame(ints, array)
    Arrays.sort(ints.asInstanceOf[Array[Object]])
    for (o <- ints) { assertSame(o, q.take) }
    assertTrue(q.isEmpty)
  }

  @Ignore("Runtime limitation - issue #209")
  @Test def testToArray1_BadArg(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case success: ArrayStoreException =>

    }
  }

  @Test def testIterator(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    val it: Iterator[?] = q.iterator
    var i: Int = 0
    i = 0
    while ({ it.hasNext }) {
      assertTrue(q.contains(it.next))
      i += 1
    }
    assertEquals(i, SIZE)
    assertIteratorExhausted(it)
  }

  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(new PriorityBlockingQueue[Integer]().iterator)
  }

  @Test def testIteratorRemove(): Unit = {
    val q = new PriorityBlockingQueue[Integer](3)
    q.add(Integer.valueOf(2))
    q.add(Integer.valueOf(1))
    q.add(Integer.valueOf(3))
    var it: Iterator[?] = q.iterator
    it.next
    it.remove()
    it = q.iterator
    assertEquals(it.next, Integer.valueOf(2))
    assertEquals(it.next, Integer.valueOf(3))
    assertFalse(it.hasNext)
  }

  @Test def testToString(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    val s: String = q.toString
    for (i <- 0 until SIZE) { assertTrue(s.contains(String.valueOf(i))) }
  }

  @Test def testPollInExecutor(): Unit = {
    val q = new PriorityBlockingQueue[Integer](2)
    val threadsStarted: CheckedBarrier = new CheckedBarrier(2)
    usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
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
  }

  @throws[Exception]
  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = {
    // val x: Queue[_] = PriorityBlockingQueueTest.populatedQueue(SIZE)
    // val y: Queue[_] = serialClone(x)
    // assertNotSame(x, y)
    // assertEquals(x.size, y.size)
    // while ({ !(x.isEmpty) }) {
    //   assertFalse(y.isEmpty)
    //   assertEquals(x.remove, y.remove())
    // }
    // assertTrue(y.isEmpty)
  }

  @Test def testDrainTo(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
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

  @throws[InterruptedException]
  @Test def testDrainToWithActivePut(): Unit = {
    val q =
      PriorityBlockingQueueTest.populatedQueue(SIZE)
    val t: Thread = new Thread(new CheckedRunnable() {
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

  @Test def testDrainToN(): Unit = {
    val q = new PriorityBlockingQueue[Integer](SIZE * 2)
    for (i <- 0 until SIZE + 2) {
      for (j <- 0 until SIZE) { assertTrue(q.offer(Integer.valueOf(j))) }
      val l = new ArrayList[Any]
      q.drainTo(l, i)
      val k: Int = if ((i < SIZE)) { i }
      else { SIZE }
      assertEquals(k, l.size)
      assertEquals(SIZE - k, q.size)
      for (j <- 0 until k) { assertEquals(l.get(j), Integer.valueOf(j)) }
      while (q.poll != null) ()
    }
  }

  @Test def testNeverContainsNull(): Unit = {
    val qs: Array[Collection[?]] = Array(
      new PriorityBlockingQueue[AnyRef],
      PriorityBlockingQueueTest.populatedQueue(2)
    )
    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
    }
  }
}
