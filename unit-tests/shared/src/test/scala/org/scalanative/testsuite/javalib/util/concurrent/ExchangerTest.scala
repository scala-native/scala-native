/*
 * Ported from OpenJDK JSR-166 TCK test.
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.{CountDownLatch, Exchanger}

import org.junit.Assert._
import org.junit.Test

import JSR166Test._

class ExchangerTest extends JSR166Test {

  /**
   * exchange exchanges objects across two threads
   */
  @Test def testExchange(): Unit = {
    val e = new Exchanger[Item]
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        assertSame(one, e.exchange(two))
        assertSame(two, e.exchange(one))
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        assertSame(two, e.exchange(one))
        assertSame(one, e.exchange(two))
      }
    })

    awaitTermination(t1)
    awaitTermination(t2)
  }

  /**
   * timed exchange exchanges objects across two threads
   */
  @Test def testTimedExchange(): Unit = {
    val e = new Exchanger[Item]
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        assertSame(one, e.exchange(two, LONG_DELAY_MS, MILLISECONDS))
        assertSame(two, e.exchange(one, LONG_DELAY_MS, MILLISECONDS))
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        assertSame(two, e.exchange(one, LONG_DELAY_MS, MILLISECONDS))
        assertSame(one, e.exchange(two, LONG_DELAY_MS, MILLISECONDS))
      }
    })

    awaitTermination(t1)
    awaitTermination(t2)
  }

  /**
   * interrupt during wait for exchange throws InterruptedException
   */
  @Test def testExchange_InterruptedException(): Unit = {
    val e = new Exchanger[Item]
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        threadStarted.countDown()
        e.exchange(one)
      }
    })

    await(threadStarted)
    t.interrupt()
    awaitTermination(t)
  }

  /**
   * interrupt during wait for timed exchange throws InterruptedException
   */
  @Test def testTimedExchange_InterruptedException(): Unit = {
    val e = new Exchanger[Item]
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        threadStarted.countDown()
        e.exchange(null, LONG_DELAY_MS, MILLISECONDS)
      }
    })

    await(threadStarted)
    t.interrupt()
    awaitTermination(t)
  }

  /**
   * timeout during wait for timed exchange throws TimeoutException
   */
  @Test def testExchange_TimeoutException(): Unit = {
    val e = new Exchanger[Item]
    val t = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
        try {
          e.exchange(null, timeoutMillis(), MILLISECONDS)
          shouldThrow()
        } catch {
          case success: java.util.concurrent.TimeoutException =>
        }
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    })

    awaitTermination(t)
  }

  /**
   * If one exchanging thread is interrupted, another succeeds.
   */
  @Test def testReplacementAfterExchange(): Unit = {
    val e = new Exchanger[Item]
    val exchanged = new CountDownLatch(2)
    val interrupted = new CountDownLatch(1)
    val t1 = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        assertSame(two, e.exchange(one))
        exchanged.countDown()
        e.exchange(two)
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        assertSame(one, e.exchange(two))
        exchanged.countDown()
        await(interrupted)
        assertSame(three, e.exchange(one))
      }
    })
    val t3 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        await(interrupted)
        assertSame(one, e.exchange(three))
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
