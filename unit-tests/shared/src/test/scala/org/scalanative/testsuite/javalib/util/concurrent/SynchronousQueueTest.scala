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

class SynchronousQueueFairTest extends BlockingQueueTest {
  override protected def emptyCollection() = new SynchronousQueue[Any](true)
}
class SynchronousQueueNonFairTest extends BlockingQueueTest {
  override protected def emptyCollection() = new SynchronousQueue[Any](false)
}

class SynchronousQueueTest extends JSR166Test {

  @Test def testEmptyFull(): Unit = testEmptyFull(false)
  @Test def testEmptyFull_fair(): Unit = testEmptyFull(true)
  def testEmptyFull(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    assertTrue(q.isEmpty)
    assertEquals(0, q.size)
    assertEquals(0, q.remainingCapacity)
    assertFalse(q.offer(zero))
  }

  @Test def testOffer(): Unit = testOffer(false)
  @Test def testOffer_fair(): Unit = testOffer(true)
  def testOffer(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    assertFalse(q.offer(one))
  }

  @Test def testAdd(): Unit = testAdd(false)
  @Test def testAdd_fair(): Unit = testAdd(true)
  def testAdd(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    assertEquals(0, q.remainingCapacity)
    try {
      q.add(one)
      shouldThrow()
    } catch {
      case success: IllegalStateException =>

    }
  }

  @Test def testAddAll_self(): Unit = testAddAll_self(false)
  @Test def testAddAll_self_fair(): Unit = testAddAll_self(true)
  def testAddAll_self(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  @Test def testAddAll_ISE(): Unit = testAddAll_ISE(false)
  @Test def testAddAll_ISE_fair(): Unit = testAddAll_ISE(true)
  def testAddAll_ISE(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val ints = new Array[Integer](1)
    for (i <- 0 until ints.length) { ints(i) = i }
    val coll = Arrays.asList(ints: _*)
    try {
      q.addAll(coll)
      shouldThrow()
    } catch {
      case success: IllegalStateException =>

    }
  }

  @Ignore("Needs ForkJoinPool")
  @Test def testBlockingPut(): Unit = testBlockingPut(false)
  @Ignore("Needs ForkJoinPool")
  @Test def testBlockingPut_fair(): Unit = testBlockingPut(true)
  def testBlockingPut(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
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
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    assertEquals(0, q.remainingCapacity)
  }

  @Ignore("Needs ForkJoinPool")
  @Test def testPutWithTake(): Unit = testPutWithTake(false)
  @Ignore("Needs ForkJoinPool")
  @Test def testPutWithTake_fair(): Unit = testPutWithTake(true)
  def testPutWithTake(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val pleaseTake = new CountDownLatch(1)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        pleaseTake.countDown()
        q.put(one)
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
    try assertSame(one, q.take)
    catch {
      case e: InterruptedException =>
        threadUnexpectedException(e)
    }
    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    assertEquals(0, q.remainingCapacity)
  }

  @Test def testTimedOffer(): Unit = {
    val fair = randomBoolean()
    val q = new SynchronousQueue[Any](fair)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
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
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  @Test def testPoll(): Unit = testPoll(false)
  @Test def testPoll_fair(): Unit = testPoll(true)
  def testPoll(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    assertNull(q.poll)
  }

  @Test def testTimedPoll0(): Unit = testTimedPoll0(false)
  @Test def testTimedPoll0_fair(): Unit = testTimedPoll0(true)
  def testTimedPoll0(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    try assertNull(q.poll(0, MILLISECONDS))
    catch {
      case e: InterruptedException =>
        threadUnexpectedException(e)
    }
  }

  @Test def testTimedPoll(): Unit = {
    val fair = randomBoolean()
    val q = new SynchronousQueue[Any](fair)
    val startTime = System.nanoTime
    try assertNull(q.poll(timeoutMillis(), MILLISECONDS))
    catch {
      case e: InterruptedException =>
        threadUnexpectedException(e)
    }
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
  }

  @Test def testTimedPollWithOffer(): Unit = {
    val fair = randomBoolean()
    val q = new SynchronousQueue[Any](fair)
    val pleaseOffer = new CountDownLatch(1)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        var startTime = System.nanoTime
        assertNull(q.poll(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        pleaseOffer.countDown()
        startTime = System.nanoTime
        assertSame(zero, q.poll(LONG_DELAY_MS, MILLISECONDS))
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
          q.poll(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      }
    })
    await(pleaseOffer)
    val startTime = System.nanoTime
    try assertTrue(q.offer(zero, LONG_DELAY_MS, MILLISECONDS))
    catch {
      case e: InterruptedException =>
        threadUnexpectedException(e)
    }
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  @Test def testPeek(): Unit = testPeek(false)
  @Test def testPeek_fair(): Unit = testPeek(true)
  def testPeek(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    assertNull(q.peek)
  }

  @Test def testElement(): Unit = testElement(false)
  @Test def testElement_fair(): Unit = testElement(true)
  def testElement(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    try {
      q.element
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  @Test def testRemove(): Unit = testRemove(false)
  @Test def testRemove_fair(): Unit = testRemove(true)
  def testRemove(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    try {
      q.remove()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  @Test def testContains(): Unit = testContains(false)
  @Test def testContains_fair(): Unit = testContains(true)
  def testContains(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    assertFalse(q.contains(zero))
  }

  @Test def testClear(): Unit = testClear(false)
  @Test def testClear_fair(): Unit = testClear(true)
  def testClear(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    q.clear()
    assertTrue(q.isEmpty)
  }

  @Test def testContainsAll(): Unit = testContainsAll(false)
  @Test def testContainsAll_fair(): Unit = testContainsAll(true)
  def testContainsAll(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val empty = new Array[Integer](0)
    assertTrue(q.containsAll(Arrays.asList(empty: _*)))
    val ints = new Array[Integer](1)
    ints(0) = zero
    assertFalse(q.containsAll(Arrays.asList(ints: _*)))
  }

  @Test def testRetainAll(): Unit = testRetainAll(false)
  @Test def testRetainAll_fair(): Unit = testRetainAll(true)
  def testRetainAll(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val empty = new Array[Integer](0)
    assertFalse(q.retainAll(Arrays.asList(empty)))
    val ints = new Array[Integer](1)
    ints(0) = zero
    assertFalse(q.retainAll(Arrays.asList(ints)))
  }

  @Test def testRemoveAll(): Unit = testRemoveAll(false)
  @Test def testRemoveAll_fair(): Unit = testRemoveAll(true)
  def testRemoveAll(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val empty = new Array[Integer](0)
    assertFalse(q.removeAll(Arrays.asList(empty)))
    val ints = new Array[Integer](1)
    ints(0) = zero
    assertFalse(q.containsAll(Arrays.asList(ints)))
  }

  @Test def testToArray(): Unit = testToArray(false)
  @Test def testToArray_fair(): Unit = testToArray(true)
  def testToArray(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val o = q.toArray
    assertEquals(0, o.length)
  }

  @Test def testToArray2(): Unit = testToArray2(false)
  @Test def testToArray2_fair(): Unit = testToArray2(true)
  def testToArray2(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Integer](fair)
    var a = new Array[Integer](0)
    assertSame(a, q.toArray(a))
    a = Array.fill(3)(42: Integer)
    assertSame(a, q.toArray(a))
    assertNull(a(0))
    for (i <- 1 until a.length) { assertEquals(42, a(i).asInstanceOf[Int]) }
  }

  @Test def testToArray_null(): Unit = testToArray_null(false)
  @Test def testToArray_null_fair(): Unit = testToArray_null(true)
  def testToArray_null(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    try {
      val unused = q.toArray(null.asInstanceOf[Array[AnyRef]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  @Test def testIterator(): Unit = testIterator(false)
  @Test def testIterator_fair(): Unit = testIterator(true)
  def testIterator(fair: Boolean): Unit = {
    assertIteratorExhausted(new SynchronousQueue[Any](fair).iterator)
  }

  @Test def testIteratorRemove(): Unit = testIteratorRemove(false)
  @Test def testIteratorRemove_fair(): Unit = testIteratorRemove(true)
  def testIteratorRemove(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val it = q.iterator
    try {
      it.remove()
      shouldThrow()
    } catch {
      case success: IllegalStateException =>

    }
  }

  @Test def testToString(): Unit = testToString(false)
  @Test def testToString_fair(): Unit = testToString(true)
  def testToString(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val s = q.toString
    assertNotNull(s)
  }

  // TODO: ThreadPoolExecutor
  //
  // @Test def testOfferInExecutor(): Unit = testOfferInExecutor(false)
  // @Test def testOfferInExecutor_fair(): Unit = testOfferInExecutor(true)
  // def testOfferInExecutor(fair: Boolean): Unit = {
  //   val q = new SynchronousQueue[Any](fair)
  //   val threadsStarted = new CheckedBarrier(2)
  //   usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
  //     executor.execute(new CheckedRunnable() {
  //       @throws[InterruptedException]
  //       override def realRun(): Unit = {
  //         assertFalse(q.offer(one))
  //         threadsStarted.await
  //         assertTrue(q.offer(one, LONG_DELAY_MS, MILLISECONDS))
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

  //
  // @Test def testPollInExecutor(): Unit = testPollInExecutor(false)
  // @Test def testPollInExecutor_fair(): Unit = testPollInExecutor(true)
  // def testPollInExecutor(fair: Boolean): Unit = {
  //   val q = new SynchronousQueue[Any](fair)
  //   val threadsStarted = new CheckedBarrier(2)
  //   usingPoolCleaner(Executors.newFixedThreadPool(2)) { executor =>
  //     executor.execute(new CheckedRunnable() {
  //       @throws[InterruptedException]
  //       override def realRun(): Unit = {
  //         assertNull(q.poll)
  //         threadsStarted.await
  //         assertSame(one, q.poll(LONG_DELAY_MS, MILLISECONDS))
  //         assertTrue(q.isEmpty)
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

  @Ignore("No Object Input Streams in Scala Native")
  @Test def testSerialization(): Unit = {
    // val x = new SynchronousQueue[Any]
    // val y = new SynchronousQueue[Any](false)
    // val z = new SynchronousQueue[Any](true)
    // assertSerialEquals(x, y)
    // assertNotSerialEquals(x, z)
    // val qs = Array(x, y, z)
    // for (q <- qs) {
    //   val clone = serialClone(q)
    //   assertNotSame(q, clone)
    //   assertSerialEquals(q, clone)
    //   assertTrue(clone.isEmpty)
    //   assertEquals(0, clone.size)
    //   assertEquals(0, clone.remainingCapacity)
    //   assertFalse(clone.offer(zero))
    // }
  }

  @Test def testDrainTo(): Unit = testDrainTo(false)
  @Test def testDrainTo_fair(): Unit = testDrainTo(true)
  def testDrainTo(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val l = new ArrayList[Any]()
    q.drainTo(l)
    assertEquals(0, q.size)
    assertEquals(0, l.size)
  }

  @Test def testDrainToWithActivePut(): Unit = {
    testDrainToWithActivePut(false)
  }
  @Test def testDrainToWithActivePut_fair(): Unit = {
    testDrainToWithActivePut(true)
  }
  def testDrainToWithActivePut(fair: Boolean): Unit = {
    val q = new SynchronousQueue[Any](fair)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { q.put(one) }
    })
    val l = new ArrayList[Any]()
    val startTime = System.nanoTime
    while ({ l.isEmpty }) {
      q.drainTo(l)
      if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
      Thread.`yield`()
    }
    assertEquals(1, l.size)
    assertSame(one, l.get(0))
    awaitTermination(t)
  }

  @throws[InterruptedException]
  @Test def testDrainToN(): Unit = {
    val q = new SynchronousQueue[Any]
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { q.put(one) }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { q.put(two) }
    })
    val l = new ArrayList[Any]()
    var drained = 0
    while ({ drained = q.drainTo(l, 1); drained == 0 }) Thread.`yield`()
    assertEquals(1, drained)
    assertEquals(1, l.size)
    while ({ drained = q.drainTo(l, 1); drained == 0 }) Thread.`yield`()
    assertEquals(1, drained)
    assertEquals(2, l.size)
    assertTrue(l.contains(one))
    assertTrue(l.contains(two))
    awaitTermination(t1)
    awaitTermination(t2)
  }

  @Test def testNeverContainsNull(): Unit = {
    val q = new SynchronousQueue[Any]
    assertFalse(q.contains(null))
    assertFalse(q.remove(null))
  }
}
