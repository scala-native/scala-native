/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.{CountDownLatch, Exchanger, TimeoutException}

import org.junit.Assert._
import org.junit.Test

class ExchangerTest extends JSR166Test {
  import JSR166Test._

  private val itemOne = itemFor(1)
  private val itemTwo = itemFor(2)
  private val itemThree = itemFor(3)

  /** exchange exchanges objects across two threads */
  @Test def testExchange(): Unit = {
    val e = new Exchanger[Item]()
    val t1 = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        assertSame(itemOne, e.exchange(itemTwo))
        assertSame(itemTwo, e.exchange(itemOne))
      }
    })
    val t2 = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        assertSame(itemTwo, e.exchange(itemOne))
        assertSame(itemOne, e.exchange(itemTwo))
      }
    })

    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** timed exchange exchanges objects across two threads */
  @Test def testTimedExchange(): Unit = {
    val e = new Exchanger[Item]()
    val t1 = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        assertSame(itemOne, e.exchange(itemTwo, LONG_DELAY_MS, MILLISECONDS))
        assertSame(itemTwo, e.exchange(itemOne, LONG_DELAY_MS, MILLISECONDS))
      }
    })
    val t2 = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        assertSame(itemTwo, e.exchange(itemOne, LONG_DELAY_MS, MILLISECONDS))
        assertSame(itemOne, e.exchange(itemTwo, LONG_DELAY_MS, MILLISECONDS))
      }
    })

    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** interrupt during wait for exchange throws InterruptedException */
  @Test def testExchange_InterruptedException(): Unit = {
    val e = new Exchanger[Item]()
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable {
      override def realRun(): Unit = {
        threadStarted.countDown()
        e.exchange(itemOne)
        ()
      }
    })

    await(threadStarted)
    t.interrupt()
    awaitTermination(t)
  }

  /** interrupt during wait for timed exchange throws InterruptedException */
  @Test def testTimedExchange_InterruptedException(): Unit = {
    val e = new Exchanger[Item]()
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable {
      override def realRun(): Unit = {
        threadStarted.countDown()
        e.exchange(null, LONG_DELAY_MS, MILLISECONDS)
        ()
      }
    })

    await(threadStarted)
    t.interrupt()
    awaitTermination(t)
  }

  /** timeout during wait for timed exchange throws TimeoutException */
  @Test def testExchange_TimeoutException(): Unit = {
    val e = new Exchanger[Item]()
    val t = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        val startTime = System.nanoTime()
        try {
          e.exchange(null, timeoutMillis(), MILLISECONDS)
          shouldThrow()
        } catch {
          case _: TimeoutException =>
        }
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    })

    awaitTermination(t)
  }

  /** If one exchanging thread is interrupted, another succeeds. */
  @Test def testReplacementAfterExchange(): Unit = {
    val e = new Exchanger[Item]()
    val exchanged = new CountDownLatch(2)
    val interrupted = new CountDownLatch(1)
    val t1 = newStartedThread(new CheckedInterruptedRunnable {
      override def realRun(): Unit = {
        assertSame(itemTwo, e.exchange(itemOne))
        exchanged.countDown()
        e.exchange(itemTwo)
        ()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        assertSame(itemOne, e.exchange(itemTwo))
        exchanged.countDown()
        await(interrupted)
        assertSame(itemThree, e.exchange(itemOne))
      }
    })
    val t3 = newStartedThread(new CheckedRunnable {
      override def realRun(): Unit = {
        await(interrupted)
        assertSame(itemOne, e.exchange(itemThree))
      }
    })

    await(exchanged)
    t1.interrupt()
    awaitTermination(t1)
    interrupted.countDown()
    awaitTermination(t2)
    awaitTermination(t3)
  }
}
