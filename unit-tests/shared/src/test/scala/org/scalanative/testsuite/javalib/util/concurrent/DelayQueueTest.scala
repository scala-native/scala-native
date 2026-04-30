/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent._

import org.junit.Assert._
import org.junit.{Ignore, Test}

object DelayQueueTest {
  import JSR166Test._

  final class PDelay(val pseudodelay: Int) extends Delayed {
    override def compareTo(y: Delayed): Int =
      Integer.compare(this.pseudodelay, y.asInstanceOf[PDelay].pseudodelay)

    override def equals(other: Any): Boolean =
      other.isInstanceOf[PDelay] &&
        this.pseudodelay == other.asInstanceOf[PDelay].pseudodelay

    override def hashCode(): Int = pseudodelay

    override def getDelay(unit: TimeUnit): Long =
      Int.MinValue.toLong + pseudodelay

    override def toString(): String = String.valueOf(pseudodelay)
  }

  final class NanoDelay(delayNanos: Long) extends Delayed {
    private val trigger = System.nanoTime() + delayNanos

    override def compareTo(y: Delayed): Int =
      java.lang.Long.compare(trigger, y.asInstanceOf[NanoDelay].trigger)

    override def equals(other: Any): Boolean =
      other.isInstanceOf[NanoDelay] &&
        this.trigger == other.asInstanceOf[NanoDelay].trigger

    override def hashCode(): Int = trigger.toInt

    override def getDelay(unit: TimeUnit): Long =
      unit.convert(trigger - System.nanoTime(), TimeUnit.NANOSECONDS)

    def getTriggerTime(): Long = trigger

    override def toString(): String = String.valueOf(trigger)
  }

  def populatedQueue(n: Int): DelayQueue[PDelay] = {
    val q = new DelayQueue[PDelay]()
    assertTrue(q.isEmpty())
    var i = n - 1
    while (i >= 0) {
      assertTrue(q.offer(new PDelay(i)))
      i -= 2
    }
    i = n & 1
    while (i < n) {
      assertTrue(q.offer(new PDelay(i)))
      i += 2
    }
    assertFalse(q.isEmpty())
    mustEqual(Int.MaxValue, q.remainingCapacity())
    mustEqual(n, q.size())
    mustEqual(new PDelay(0), q.peek())
    q
  }
}

class DelayQueueTest extends JSR166Test {
  import DelayQueueTest._
  import JSR166Test._

  /** A new queue has unbounded capacity */
  @Test def testConstructor1(): Unit = {
    mustEqual(Int.MaxValue, new DelayQueue[PDelay]().remainingCapacity())
  }

  /** Initializing from null Collection throws NPE */
  @Test def testConstructor3(): Unit = {
    try {
      new DelayQueue[PDelay](null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Initializing from Collection of null elements throws NPE */
  @Test def testConstructor4(): Unit = {
    try {
      new DelayQueue[PDelay](Arrays.asList(new Array[PDelay](SIZE): _*))
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Initializing from Collection with some null elements throws NPE */
  @Test def testConstructor5(): Unit = {
    val a = new Array[PDelay](SIZE)
    for (i <- 0 until SIZE - 1) a(i) = new PDelay(i)
    try {
      new DelayQueue[PDelay](Arrays.asList(a: _*))
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Queue contains all elements of collection used to initialize */
  @Test def testConstructor6(): Unit = {
    val items = new Array[PDelay](SIZE)
    for (i <- 0 until SIZE) items(i) = new PDelay(i)
    val q = new DelayQueue[PDelay](Arrays.asList(items: _*))
    for (i <- 0 until SIZE) mustEqual(items(i), q.poll())
  }

  /** isEmpty is true before add, false after */
  @Test def testEmpty(): Unit = {
    val q = new DelayQueue[PDelay]()
    assertTrue(q.isEmpty())
    mustEqual(Int.MaxValue, q.remainingCapacity())
    q.add(new PDelay(1))
    assertFalse(q.isEmpty())
    q.add(new PDelay(2))
    q.remove()
    q.remove()
    assertTrue(q.isEmpty())
  }

  /** remainingCapacity() always returns Integer.MAX_VALUE */
  @Test def testRemainingCapacity(): Unit = {
    val q: BlockingQueue[PDelay] = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(Int.MaxValue, q.remainingCapacity())
      mustEqual(SIZE - i, q.size())
      assertTrue(q.remove().isInstanceOf[PDelay])
    }
    for (i <- 0 until SIZE) {
      mustEqual(Int.MaxValue, q.remainingCapacity())
      mustEqual(i, q.size())
      assertTrue(q.add(new PDelay(i)))
    }
  }

  /** offer non-null succeeds */
  @Test def testOffer(): Unit = {
    val q = new DelayQueue[PDelay]()
    assertTrue(q.offer(new PDelay(0)))
    assertTrue(q.offer(new PDelay(1)))
  }

  /** add succeeds */
  @Test def testAdd(): Unit = {
    val q = new DelayQueue[PDelay]()
    for (i <- 0 until SIZE) {
      mustEqual(i, q.size())
      assertTrue(q.add(new PDelay(i)))
    }
  }

  /** addAll(this) throws IllegalArgumentException */
  @Test def testAddAllSelf(): Unit = {
    val q = populatedQueue(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case _: IllegalArgumentException =>
    }
  }

  /** addAll of a collection with any null elements throws NPE */
  @Test def testAddAll3(): Unit = {
    val q = new DelayQueue[PDelay]()
    val a = new Array[PDelay](SIZE)
    for (i <- 0 until SIZE - 1) a(i) = new PDelay(i)
    try {
      q.addAll(Arrays.asList(a: _*))
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** Queue contains all elements of successful addAll */
  @Test def testAddAll5(): Unit = {
    val empty = new Array[PDelay](0)
    val items = new Array[PDelay](SIZE)
    for (i <- SIZE - 1 to 0 by -1) items(i) = new PDelay(i)
    val q = new DelayQueue[PDelay]()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    for (i <- 0 until SIZE) mustEqual(items(i), q.poll())
  }

  /** all elements successfully put are contained */
  @Test def testPut(): Unit = {
    val q = new DelayQueue[PDelay]()
    for (i <- 0 until SIZE) {
      val x = new PDelay(i)
      q.put(x)
      assertTrue(q.contains(x))
    }
    mustEqual(SIZE, q.size())
  }

  /** put doesn't block waiting for take */
  @Test def testPutWithTake(): Unit = {
    val q = new DelayQueue[PDelay]()
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        q.put(new PDelay(0))
        q.put(new PDelay(0))
        q.put(new PDelay(0))
        q.put(new PDelay(0))
      }
    })

    awaitTermination(t)
    mustEqual(4, q.size())
  }

  /** Queue is unbounded, so timed offer never times out */
  @Test def testTimedOffer(): Unit = {
    val q = new DelayQueue[PDelay]()
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        q.put(new PDelay(0))
        q.put(new PDelay(0))
        assertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, MILLISECONDS))
        assertTrue(q.offer(new PDelay(0), LONG_DELAY_MS, MILLISECONDS))
      }
    })

    awaitTermination(t)
  }

  /** take retrieves elements in priority order */
  @Test def testTake(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE)
      mustEqual(new PDelay(i), q.take())
  }

  /** take signals another waiting taker when more expired elements remain */
  @Test def testTakeSignalsNextExpiredElement(): Unit = {
    val q = new DelayQueue[PDelay]()
    val threadsStarted = new CountDownLatch(2)
    val done = new CountDownLatch(2)
    val taken = Collections.synchronizedList(new ArrayList[PDelay]())

    val taker = new CheckedRunnable {
      override def realRun(): Unit = {
        threadsStarted.countDown()
        taken.add(q.take())
        done.countDown()
      }
    }

    val t1 = newStartedThread(taker)
    val t2 = newStartedThread(taker)
    try {
      await(threadsStarted)
      waitForThreadToEnterWaitState(t1)
      waitForThreadToEnterWaitState(t2)

      q.put(new PDelay(0))
      q.put(new PDelay(1))

      await(done)
      awaitTermination(t1)
      awaitTermination(t2)
      mustEqual(2, taken.size())
      assertTrue(q.isEmpty())
    } finally {
      t1.interrupt()
      t2.interrupt()
    }
  }

  /** Take removes existing elements until empty, then blocks interruptibly */
  @Test def testBlockingTake(): Unit = {
    val q = populatedQueue(SIZE)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE) mustEqual(new PDelay(i), q.take())

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
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) mustEqual(new PDelay(i), q.poll())
    assertNull(q.poll())
  }

  /** timed poll with zero timeout succeeds when non-empty, else times out */
  @Test def testTimedPoll0(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE)
      mustEqual(new PDelay(i), q.poll(0, MILLISECONDS))
    assertNull(q.poll(0, MILLISECONDS))
  }

  /** timed poll with nonzero timeout succeeds when non-empty, else times out */
  @Test def testTimedPoll(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      val startTime = System.nanoTime()
      mustEqual(new PDelay(i), q.poll(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
    val startTime = System.nanoTime()
    assertNull(q.poll(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    checkEmpty(q)
  }

  /** Interrupted timed poll throws InterruptedException */
  @Test def testInterruptedTimedPoll(): Unit = {
    val pleaseInterrupt = new CountDownLatch(1)
    val q = populatedQueue(SIZE)
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        for (i <- 0 until SIZE)
          mustEqual(new PDelay(i), q.poll(LONG_DELAY_MS, MILLISECONDS))

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

  /** peek returns next element, or null if empty */
  @Test def testPeek(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(new PDelay(i), q.peek())
      mustEqual(new PDelay(i), q.poll())
      if (q.isEmpty()) assertNull(q.peek())
      else assertFalse(new PDelay(i).equals(q.peek()))
    }
    assertNull(q.peek())
  }

  /** element returns next element, or throws NSEE if empty */
  @Test def testElement(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(new PDelay(i), q.element())
      q.poll()
    }
    try {
      q.element()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }

  /** remove removes next element, or throws NSEE if empty */
  @Test def testRemove(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE)
      mustEqual(new PDelay(i), q.remove())
    try {
      q.remove()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }
  }

  /** contains(x) reports true when elements added but not yet removed */
  @Test def testContains(): Unit = {
    val q = populatedQueue(SIZE)
    for (i <- 0 until SIZE) {
      assertTrue(q.contains(new PDelay(i)))
      q.poll()
      assertFalse(q.contains(new PDelay(i)))
    }
  }

  /** clear removes all elements */
  @Test def testClear(): Unit = {
    val q = populatedQueue(SIZE)
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    mustEqual(Int.MaxValue, q.remainingCapacity())
    val x = new PDelay(1)
    q.add(x)
    assertFalse(q.isEmpty())
    assertTrue(q.contains(x))
    q.clear()
    assertTrue(q.isEmpty())
  }

  /** containsAll(c) is true when c contains a subset of elements */
  @Test def testContainsAll(): Unit = {
    val q = populatedQueue(SIZE)
    val p = new DelayQueue[PDelay]()
    for (i <- 0 until SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      p.add(new PDelay(i))
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c */
  @Test def testRetainAll(): Unit = {
    val q = populatedQueue(SIZE)
    val p = populatedQueue(SIZE)
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
      val q = populatedQueue(SIZE)
      val p = populatedQueue(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size())
      for (_ <- 0 until i)
        assertFalse(q.contains(p.remove()))
    }
  }

  /** toArray contains all elements */
  @Test def testToArray(): Unit = {
    val q = populatedQueue(SIZE)
    val a = q.toArray()
    assertSame(classOf[Array[AnyRef]], a.getClass())
    Arrays.sort(a)
    a.foreach(o => assertSame(o, q.take()))
    assertTrue(q.isEmpty())
  }

  /** toArray(a) contains all elements */
  @Test def testToArray2(): Unit = {
    val q = populatedQueue(SIZE)
    val items = new Array[PDelay](SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    Arrays.sort(items.asInstanceOf[Array[AnyRef]])
    items.foreach(o => assertSame(o, q.remove()))
    assertTrue(q.isEmpty())
  }

  /** toArray(incompatible array type) throws ArrayStoreException */
  @Ignore(
    "Scala Native reference arrays do not preserve runtime component types"
  )
  @Test def testToArray_incompatibleArrayType(): Unit = {
    val q = populatedQueue(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case _: ArrayStoreException =>
    }
  }

  /** iterator iterates through all elements */
  @Test def testIterator(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    val it = q.iterator()
    while (it.hasNext()) {
      assertTrue(q.contains(it.next()))
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements */
  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(new DelayQueue[PDelay]().iterator())
  }

  /** iterator.remove removes current element */
  @Test def testIteratorRemove(): Unit = {
    val q = new DelayQueue[PDelay]()
    q.add(new PDelay(2))
    q.add(new PDelay(1))
    q.add(new PDelay(3))
    var it = q.iterator()
    it.next()
    it.remove()
    it = q.iterator()
    mustEqual(new PDelay(2), it.next())
    mustEqual(new PDelay(3), it.next())
    assertFalse(it.hasNext())
  }

  /** toString contains toStrings of elements */
  @Test def testToString(): Unit = {
    val q = populatedQueue(SIZE)
    val s = q.toString()
    val it = q.iterator()
    while (it.hasNext())
      assertTrue(s.contains(it.next().toString()))
  }

  /** timed poll transfers elements across Executor tasks */
  @Test def testPollInExecutor(): Unit = {
    val q = new DelayQueue[PDelay]()
    val threadsStarted = new CheckedBarrier(2)
    val executor = Executors.newFixedThreadPool(2)
    usingPoolCleaner(executor) { executor =>
      executor.execute(new CheckedRunnable {
        override def realRun(): Unit = {
          assertNull(q.poll())
          threadsStarted.await()
          assertNotNull(q.poll(LONG_DELAY_MS, MILLISECONDS))
          checkEmpty(q)
        }
      })

      executor.execute(new CheckedRunnable {
        override def realRun(): Unit = {
          threadsStarted.await()
          q.put(new PDelay(1))
        }
      })
    }
  }

  /** Delayed actions do not occur until their delay elapses */
  @Test def testDelay(): Unit = {
    val q = new DelayQueue[NanoDelay]()
    for (i <- 0 until SIZE)
      q.add(new NanoDelay(1000000L * (SIZE - i)))

    var last = 0L
    for (i <- 0 until SIZE) {
      val e = q.take()
      val tt = e.getTriggerTime()
      assertTrue(System.nanoTime() - tt >= 0)
      if (i != 0) assertTrue(tt >= last)
      last = tt
    }
    assertTrue(q.isEmpty())
  }

  /** peek of a non-empty queue returns non-null even if not expired */
  @Test def testPeekDelayed(): Unit = {
    val q = new DelayQueue[NanoDelay]()
    q.add(new NanoDelay(Long.MaxValue))
    assertNotNull(q.peek())
  }

  /** poll of a non-empty queue returns null if no expired elements. */
  @Test def testPollDelayed(): Unit = {
    val q = new DelayQueue[NanoDelay]()
    q.add(new NanoDelay(Long.MaxValue))
    assertNull(q.poll())
  }

  /** timed poll of a non-empty queue returns null if no expired elements. */
  @Test def testTimedPollDelayed(): Unit = {
    val q = new DelayQueue[NanoDelay]()
    q.add(new NanoDelay(LONG_DELAY_MS * 1000000L))
    val startTime = System.nanoTime()
    assertNull(q.poll(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
  }

  /** drainTo(c) empties queue into another collection c */
  @Test def testDrainTo(): Unit = {
    val q = new DelayQueue[PDelay]()
    val elems = new Array[PDelay](SIZE)
    for (i <- 0 until SIZE) {
      elems(i) = new PDelay(i)
      q.add(elems(i))
    }
    val l = new ArrayList[PDelay]()
    q.drainTo(l)
    mustEqual(0, q.size())
    for (i <- 0 until SIZE) mustEqual(elems(i), l.get(i))
    q.add(elems(0))
    q.add(elems(1))
    assertFalse(q.isEmpty())
    assertTrue(q.contains(elems(0)))
    assertTrue(q.contains(elems(1)))
    l.clear()
    q.drainTo(l)
    mustEqual(0, q.size())
    mustEqual(2, l.size())
    for (i <- 0 until 2) mustEqual(elems(i), l.get(i))
  }

  /** drainTo empties queue */
  @Test def testDrainToWithActivePut(): Unit = {
    val q = populatedQueue(SIZE)
    val t = new Thread(new CheckedRunnable {
      override def realRun(): Unit = q.put(new PDelay(SIZE + 1))
    })

    t.start()
    val l = new ArrayList[PDelay]()
    q.drainTo(l)
    assertTrue(l.size() >= SIZE)
    t.join()
    assertTrue(q.size() + l.size() >= SIZE)
  }

  /** drainTo(c, n) empties first min(n, size) elements of queue into c */
  @Test def testDrainToN(): Unit = {
    for (i <- 0 until SIZE + 2) {
      val q = populatedQueue(SIZE)
      val l = new ArrayList[PDelay]()
      q.drainTo(l, i)
      val k = if (i < SIZE) i else SIZE
      mustEqual(SIZE - k, q.size())
      mustEqual(k, l.size())
    }
  }

  /** remove(null), contains(null) always return false */
  @Test def testNeverContainsNull(): Unit = {
    val q: Collection[_] = populatedQueue(SIZE)
    assertFalse(q.contains(null))
    assertFalse(q.remove(null))
  }
}
