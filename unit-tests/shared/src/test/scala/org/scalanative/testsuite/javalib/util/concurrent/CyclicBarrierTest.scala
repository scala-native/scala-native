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
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

class CyclicBarrierTest extends JSR166Test {

  /** Spin-waits till the number of waiters == numberOfWaiters.
   */
  def awaitNumberWaiting(barrier: CyclicBarrier, numberOfWaiters: Int): Unit = {
    val startTime = System.nanoTime
    while ({ barrier.getNumberWaiting != numberOfWaiters }) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
      Thread.`yield`()
    }
  }

  /** Creating with negative parties throws IllegalArgumentException
   */
  @Test def testConstructor1(): Unit = {
    try {
      new CyclicBarrier(-1, null.asInstanceOf[Runnable])
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Creating with negative parties and no action throws
   *  IllegalArgumentException
   */
  @Test def testConstructor2(): Unit = {
    try {
      new CyclicBarrier(-1)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** getParties returns the number of parties given in constructor
   */
  @Test def testGetParties(): Unit = {
    val b = new CyclicBarrier(2)
    assertEquals(2, b.getParties)
    assertEquals(0, b.getNumberWaiting)
  }

  /** A 1-party barrier triggers after single await
   */
  @throws[Exception]
  @Test def testSingleParty(): Unit = {
    val b = new CyclicBarrier(1)
    assertEquals(1, b.getParties)
    assertEquals(0, b.getNumberWaiting)
    b.await
    b.await
    assertEquals(0, b.getNumberWaiting)
  }

  /** The supplied barrier action is run at barrier
   */
  @throws[Exception]
  @Test def testBarrierAction(): Unit = {
    val count = new AtomicInteger(0)
    val incCount = new Runnable() {
      override def run(): Unit = { count.getAndIncrement }
    }
    val b = new CyclicBarrier(1, incCount)
    assertEquals(1, b.getParties)
    assertEquals(0, b.getNumberWaiting)
    b.await
    b.await
    assertEquals(0, b.getNumberWaiting)
    assertEquals(2, count.get)
  }

  /** A 2-party/thread barrier triggers after both threads invoke await
   */
  @throws[Exception]
  @Test def testTwoParties(): Unit = {
    val b = new CyclicBarrier(2)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        b.await
        b.await
        b.await
        b.await
      }
    })
    b.await
    b.await
    b.await
    b.await
    awaitTermination(t)
  }

  /** An interruption in one party causes others waiting in await to throw
   *  BrokenBarrierException
   */
  @Test def testAwait1_Interrupted_BrokenBarrier(): Unit = {
    val c = new CyclicBarrier(3)
    val pleaseInterrupt = new CountDownLatch(2)
    val t1 =
      new ThreadShouldThrow(classOf[InterruptedException]) {
        @throws[Exception]
        override def realRun(): Unit = {
          pleaseInterrupt.countDown()
          c.await
        }
      }
    val t2 =
      new ThreadShouldThrow(classOf[BrokenBarrierException]) {
        @throws[Exception]
        override def realRun(): Unit = {
          pleaseInterrupt.countDown()
          c.await
        }
      }
    t1.start()
    t2.start()
    await(pleaseInterrupt)
    t1.interrupt()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** An interruption in one party causes others waiting in timed await to throw
   *  BrokenBarrierException
   */
  @throws[Exception]
  @Test def testAwait2_Interrupted_BrokenBarrier(): Unit = {
    val c = new CyclicBarrier(3)
    val pleaseInterrupt = new CountDownLatch(2)
    val t1 =
      new ThreadShouldThrow(classOf[InterruptedException]) {
        @throws[Exception]
        override def realRun(): Unit = {
          pleaseInterrupt.countDown()
          c.await(LONG_DELAY_MS, MILLISECONDS)
        }
      }
    val t2 =
      new ThreadShouldThrow(classOf[BrokenBarrierException]) {
        @throws[Exception]
        override def realRun(): Unit = {
          pleaseInterrupt.countDown()
          c.await(LONG_DELAY_MS, MILLISECONDS)
        }
      }
    t1.start()
    t2.start()
    await(pleaseInterrupt)
    t1.interrupt()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** A timeout in timed await throws TimeoutException
   */
  @throws[InterruptedException]
  @Test def testAwait3_TimeoutException(): Unit = {
    val c = new CyclicBarrier(2)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
        try {
          c.await(timeoutMillis(), MILLISECONDS)
          shouldThrow()
        } catch {
          case success: TimeoutException =>

        }
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    })
    awaitTermination(t)
  }

  /** A timeout in one party causes others waiting in timed await to throw
   *  BrokenBarrierException
   */
  @throws[InterruptedException]
  @Test def testAwait4_Timeout_BrokenBarrier(): Unit = {
    val c = new CyclicBarrier(3)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        try {
          c.await(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: BrokenBarrierException =>

        }
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        awaitNumberWaiting(c, 1)
        val startTime = System.nanoTime
        try {
          c.await(timeoutMillis(), MILLISECONDS)
          shouldThrow()
        } catch {
          case success: TimeoutException =>

        }
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    })
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** A timeout in one party causes others waiting in await to throw
   *  BrokenBarrierException
   */
  @throws[InterruptedException]
  @Test def testAwait5_Timeout_BrokenBarrier(): Unit = {
    val c = new CyclicBarrier(3)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        try {
          c.await
          shouldThrow()
        } catch {
          case success: BrokenBarrierException =>

        }
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        awaitNumberWaiting(c, 1)
        val startTime = System.nanoTime
        try {
          c.await(timeoutMillis(), MILLISECONDS)
          shouldThrow()
        } catch {
          case success: TimeoutException =>

        }
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    })
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** A reset of an active barrier causes waiting threads to throw
   *  BrokenBarrierException
   */
  @throws[InterruptedException]
  @Test def testReset_BrokenBarrier(): Unit = {
    val c = new CyclicBarrier(3)
    val pleaseReset = new CountDownLatch(2)
    val t1 =
      new ThreadShouldThrow(classOf[BrokenBarrierException]) {
        @throws[Exception]
        override def realRun(): Unit = {
          pleaseReset.countDown()
          c.await
        }
      }
    val t2 =
      new ThreadShouldThrow(classOf[BrokenBarrierException]) {
        @throws[Exception]
        override def realRun(): Unit = {
          pleaseReset.countDown()
          c.await
        }
      }
    t1.start()
    t2.start()
    await(pleaseReset)
    awaitNumberWaiting(c, 2)
    c.reset()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** A reset before threads enter barrier does not throw BrokenBarrierException
   */
  @throws[Exception]
  @Test def testReset_NoBrokenBarrier(): Unit = {
    val c = new CyclicBarrier(3)
    c.reset()
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = { c.await }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = { c.await }
    })
    c.await
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Reset of a non-broken barrier does not break barrier
   */
  @throws[Exception]
  @Test def testResetWithoutBreakage(): Unit = {
    val barrier = new CyclicBarrier(3)
    for (i <- 0 until 3) {
      val start = new CyclicBarrier(3)
      val t1 = newStartedThread(new CheckedRunnable() {
        @throws[Exception]
        override def realRun(): Unit = {
          start.await
          barrier.await
        }
      })
      val t2 = newStartedThread(new CheckedRunnable() {
        @throws[Exception]
        override def realRun(): Unit = {
          start.await
          barrier.await
        }
      })
      start.await
      barrier.await
      awaitTermination(t1)
      awaitTermination(t2)
      assertFalse(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
      if (i == 1) barrier.reset()
      assertFalse(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
    }
  }

  /** Reset of a barrier after interruption reinitializes it.
   */
  @throws[Exception]
  @Test def testResetAfterInterrupt(): Unit = {
    val barrier = new CyclicBarrier(3)
    for (i <- 0 until 2) {
      val startBarrier = new CyclicBarrier(3)
      val t1 =
        new ThreadShouldThrow(classOf[InterruptedException]) {
          @throws[Exception]
          override def realRun(): Unit = {
            startBarrier.await
            barrier.await
          }
        }
      val t2 =
        new ThreadShouldThrow(classOf[BrokenBarrierException]) {
          @throws[Exception]
          override def realRun(): Unit = {
            startBarrier.await
            barrier.await
          }
        }
      t1.start()
      t2.start()
      startBarrier.await
      t1.interrupt()
      awaitTermination(t1)
      awaitTermination(t2)
      assertTrue(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
      barrier.reset()
      assertFalse(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
    }
  }

  /** Reset of a barrier after timeout reinitializes it.
   */
  @throws[Exception]
  @Test def testResetAfterTimeout(): Unit = {
    val barrier = new CyclicBarrier(3)
    for (i <- 0 until 2) {
      assertEquals(0, barrier.getNumberWaiting)
      val t1 = newStartedThread(new CheckedRunnable() {
        @throws[Exception]
        override def realRun(): Unit = {
          try {
            barrier.await
            shouldThrow()
          } catch {
            case success: BrokenBarrierException =>

          }
        }
      })
      val t2 = newStartedThread(new CheckedRunnable() {
        @throws[Exception]
        override def realRun(): Unit = {
          awaitNumberWaiting(barrier, 1)
          val startTime = System.nanoTime
          try {
            barrier.await(timeoutMillis(), MILLISECONDS)
            shouldThrow()
          } catch {
            case success: TimeoutException =>

          }
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        }
      })
      awaitTermination(t1)
      awaitTermination(t2)
      assertEquals(0, barrier.getNumberWaiting)
      assertTrue(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
      barrier.reset()
      assertFalse(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
    }
  }

  /** Reset of a barrier after a failed command reinitializes it.
   */
  @throws[Exception]
  @Test def testResetAfterCommandException(): Unit = {
    val barrier = new CyclicBarrier(
      3,
      new Runnable() {
        override def run(): Unit = { throw new NullPointerException }
      }
    )
    for (i <- 0 until 2) {
      val startBarrier = new CyclicBarrier(3)
      val t1 =
        new ThreadShouldThrow(classOf[BrokenBarrierException]) {
          @throws[Exception]
          override def realRun(): Unit = {
            startBarrier.await
            barrier.await
          }
        }
      val t2 =
        new ThreadShouldThrow(classOf[BrokenBarrierException]) {
          @throws[Exception]
          override def realRun(): Unit = {
            startBarrier.await
            barrier.await
          }
        }
      t1.start()
      t2.start()
      startBarrier.await
      awaitNumberWaiting(barrier, 2)
      try {
        barrier.await
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
      awaitTermination(t1)
      awaitTermination(t2)
      assertTrue(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
      barrier.reset()
      assertFalse(barrier.isBroken)
      assertEquals(0, barrier.getNumberWaiting)
    }
  }

  // TODO: ThreadPoolExecutor
  // /** There can be more threads calling await() than parties, as long as each
  //  *  task only calls await once and the task count is a multiple of parties.
  //  */
  // @throws[Exception]
  // @Test def testMoreTasksThanParties(): Unit = {
  //   val rnd = ThreadLocalRandom.current
  //   val parties = rnd.nextInt(1, 5)
  //   val nTasks = rnd.nextInt(1, 5) * parties
  //   val tripCount = new AtomicInteger(0)
  //   val awaitCount = new AtomicInteger(0)
  //   val barrier = new CyclicBarrier(parties, () => tripCount.getAndIncrement)
  //   val awaiter: Runnable = () => {
  //     def foo() =
  //       try {
  //         if (randomBoolean()) barrier.await
  //         else barrier.await(LONG_DELAY_MS, MILLISECONDS)
  //         awaitCount.getAndIncrement
  //       } catch {
  //         case fail: Throwable =>
  //           threadUnexpectedException(fail)
  //       }
  //     foo()
  //   }
  //   usingPoolCleaner(Executors.newFixedThreadPool(nTasks)) { e =>
  //     var i = nTasks
  //     while ({ i -= 1; i + 1 > 0 }) e.execute(awaiter)
  //   }
  //   assertEquals(nTasks / parties, tripCount.get)
  //   assertEquals(nTasks, awaitCount.get)
  //   assertEquals(0, barrier.getNumberWaiting)
  // }
}
