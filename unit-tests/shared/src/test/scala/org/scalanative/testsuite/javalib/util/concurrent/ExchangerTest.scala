/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: jsr166/src/test/tck/ExchangerTest.java
 *  revision 1.27, dated: 2021-01-26
 */

/* Scala Native Notes
 *
 *   1. Shield your eyes: This code is intended to be Scala which stays very
 *      close to JSR-166 .java original. This aids detecting porting errors.
 *      It is _Not_Intended_ to be idiomatic Scala.
 *
 *   2. The JSR-166 code used 'Arrays.asList(ar)' in a number of tests.
 *      For Scala, the varargs slice required by a direct translation
 *      differs on Scala 2 & 3.  For Scala 2 it is 'Arrays.asList(ar: _*).
 *      On Scala 3 it is 'Arrays.asList(ar*)'. Early Scala 3 versions
 *      tolerate the former ': _*' but announce that such tolerance
 *      will cease in some future version.
 *
 *      This code uses a non-obvious and somewhat runtime expression to
 *      void the need for creating and maintaining variants for Scala version:
 *      'Arrays.stream(ar).collect(Collectors.toList())'.
 *
 *  3.  Extra is taken for so that assertion and other failures in Runnables
 *      cause the CI run for this class to fail. This eliminates
 *      an entire class of false positive results.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, Exchanger, TimeoutException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ExchangerTest extends JSR166Test {
  import JSR166Test._

  /*  SN: JSR-166 code used [Item] but SN defines Item different to
   *      JSR-166, so use SN definition of Integer.
   *      This is OK, because Integer is still an Object/AnyRef and
   *      not a primitive.
   */

  type SnItemType = Integer

  /* SN: Ensure that failures in Runnables get reported to overall CI.
   */

  private def ciAwaitTermination(
      thread: Thread,
      threadSuccess: AtomicBoolean,
      timeoutMillis: Long = LONG_DELAY_MS
  ) = {
    awaitTermination(thread)
    if (!threadSuccess.get())
      fail(s"Failure in $thread, check log for cause or stacktrace.")
  }

  /** exchange exchanges objects across two threads
   */
  @Test def testExchange(): Unit = {
    val t1Success = new AtomicBoolean()

    val e = new Exchanger[SnItemType]()
    val t1 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertSame(one, e.exchange(two))
        assertSame(two, e.exchange(one))
        t1Success.set(true)
      }
    })

    val t2Success = new AtomicBoolean()

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertSame(two, e.exchange(one))
        assertSame(one, e.exchange(two))
        t2Success.set(true)
      }
    })

    ciAwaitTermination(t1, t1Success)
    ciAwaitTermination(t2, t2Success)
  }

  /** timed exchange exchanges objects across two threads
   */
  @Test def testTimedExchange(): Unit = {
    val t1Success = new AtomicBoolean()

    val e = new Exchanger[SnItemType]()
    val t1 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertSame(one, e.exchange(two, LONG_DELAY_MS, MILLISECONDS))
        assertSame(two, e.exchange(one, LONG_DELAY_MS, MILLISECONDS))
        t1Success.set(true)
      }
    })

    val t2Success = new AtomicBoolean()

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertSame(two, e.exchange(one, LONG_DELAY_MS, MILLISECONDS))
        assertSame(one, e.exchange(two, LONG_DELAY_MS, MILLISECONDS))
        t2Success.set(true)
      }
    })

    ciAwaitTermination(t1, t1Success)
    ciAwaitTermination(t2, t2Success)
  }

  /** interrupt during wait for exchange throws InterruptedException
   */
  @Test def testExchange_InterruptedException(): Unit = {
    /* This test gives CI a clear CountDownLatch timeout failure, so no
     * special CI handling is needed.
     */

    val e = new Exchanger[SnItemType]()
    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        threadStarted.countDown()
        e.exchange(one)
      }
    })

    await(threadStarted)
    t.interrupt()
    awaitTermination(t)
  }

  /** interrupt during wait for timed exchange throws InterruptedException
   */
  @Test def testTimedExchange_InterruptedException(): Unit = {
    /* SN: This test differs from others in the class in that it
     *     does not need ciAwaitTermination(). If the tested condition,
     *     'interrupt', does not cause the exchange() to return, a
     *     normal awaitTermination() will report a timeout error and
     *     visibly fail CI for this class.
     */

    val e = new Exchanger[SnItemType]()

    val threadStarted = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        threadStarted.countDown()
        e.exchange(null, LONG_DELAY_MS, MILLISECONDS)
      }
    })

    await(threadStarted)
    t.interrupt()
    awaitTermination(t)
  }

  /** timeout during wait for timed exchange throws TimeoutException
   */
  @Test def testExchange_TimeoutException(): Unit = {
    val tSuccess = new AtomicBoolean()

    val e = new Exchanger[SnItemType]()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        val startTime = System.nanoTime()

        assertThrows(
          "a1",
          classOf[TimeoutException],
          e.exchange(null, timeoutMillis(), MILLISECONDS)
        )

        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        tSuccess.set(true)
      }
    })

    ciAwaitTermination(t, tSuccess)
  }

  /** If one exchanging thread is interrupted, another succeeds.
   */
  @Test def testReplacementAfterExchange(): Unit = {
    // t1 failures are evident in CI as CountDownLatch or other timeouts.
    val t2Success = new AtomicBoolean()
    val t3Success = new AtomicBoolean()

    val e = new Exchanger[SnItemType]()

    val exchanged = new CountDownLatch(2)
    val interrupted = new CountDownLatch(1)
    val t1 = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        assertSame(two, e.exchange(one))
        exchanged.countDown()
        e.exchange(two)
      }
    })

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertSame(one, e.exchange(two))
        exchanged.countDown()
        await(interrupted)
        assertSame(three, e.exchange(one))
        t2Success.set(true)
      }
    })

    val t3 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        await(interrupted)
        assertSame(one, e.exchange(three))
        t3Success.set(true)
      }
    })

    await(exchanged)
    t1.interrupt()
    awaitTermination(t1)

    interrupted.countDown()

    ciAwaitTermination(t2, t2Success)
    ciAwaitTermination(t3, t3Success)
  }
}
