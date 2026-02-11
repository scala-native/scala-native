/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import org.junit.Assert._
import org.junit.Test

import JSR166Test.*

class SubmissionPublisherTest extends JSR166Test {

  class TestSubscriber extends Flow.Subscriber[Integer] {
    @volatile var sn: Flow.Subscription = null
    var last = 0 // Requires that onNexts are in numeric order

    @volatile var nexts = 0
    @volatile var errors = 0
    @volatile var completes = 0
    @volatile var throwOnCall = false
    @volatile var request = true
    @volatile var lastError: Throwable = null

    override def onSubscribe(s: Flow.Subscription): Unit = synchronized {
      threadAssertTrue(sn == null)
      sn = s
      notifyAll()
      if (throwOnCall) throw new SPException()
      if (request) sn.request(1L)
    }

    override def onNext(t: Integer): Unit = synchronized {
      nexts += 1
      notifyAll()
      val current = t.intValue()
      threadAssertTrue(current >= last)
      last = current
      if (request) sn.request(1L)
      if (throwOnCall) throw new SPException()
    }

    override def onError(t: Throwable): Unit = synchronized {
      threadAssertTrue(completes == 0)
      threadAssertTrue(errors == 0)
      lastError = t
      errors += 1
      notifyAll()
    }

    override def onComplete(): Unit = synchronized {
      threadAssertTrue(completes == 0)
      completes += 1
      notifyAll()
    }

    def awaitSubscribe(): Unit = {
      if (sn == null) {
        while ({
          try wait(1L)
          catch {
            case ex: Exception => threadUnexpectedException(ex)
          }

          sn == null
        }) {}
      }
    }

    def awaitNext(n: Int): Unit = {
      if (nexts < n) {
        while ({
          try wait(1L)
          catch {
            case ex: Exception => threadUnexpectedException(ex)
          }

          nexts < n
        }) {}
      }
    }

    def awaitComplete(): Unit = {
      if (completes == 0 && errors == 0) {
        while ({
          try wait(1L)
          catch {
            case ex: Exception => threadUnexpectedException(ex)
          }

          completes == 0 && errors == 0
        }) {}
      }
    }

    def awaitError(): Unit = {
      if (errors == 0) {
        while ({
          try wait(1L)
          catch {
            case ex: Exception => threadUnexpectedException(ex)
          }

          (errors == 0)
        }) {}
      }
    }
  }

  def noopHandle(count: AtomicInteger): Boolean = {
    count.getAndIncrement()
    false
  }

  def reqHandle(count: AtomicInteger, sub: Flow.Subscriber[_]): Boolean = {
    count.getAndIncrement()
    sub.asInstanceOf[TestSubscriber].sn.request(Long.MaxValue)
    true
  }

  def basicPublisher() = new SubmissionPublisher[Integer]()

  class SPException extends RuntimeException {}

  final val basicExecutor = basicPublisher().getExecutor()

  /** A new SubmissionPublisher has no subscribers, a non-null executor, a
   *  power-of-two capacity, is not closed, and reports zero demand and lag
   */
  def checkInitialState(p: SubmissionPublisher[_]): Unit = {
    assertFalse(p.hasSubscribers())
    assertEquals(0, p.getNumberOfSubscribers())
    assertTrue(p.getSubscribers().isEmpty())
    assertFalse(p.isClosed())
    assertNull(p.getClosedException())
    val n = p.getMaxBufferCapacity()
    assertTrue((n & (n - 1)) == 0) // power of two

    assertNotNull(p.getExecutor())
    assertEquals(0L, p.estimateMinimumDemand())
    assertEquals(0, p.estimateMaximumLag())
  }

  /* End of helper classes and methods */

  /** A default-constructed SubmissionPublisher has no subscribers, is not
   *  closed, has default buffer size, and uses the defaultExecutor
   */
  @Test def testConstructor1(): Unit = {
    val p = new SubmissionPublisher[Integer]
    checkInitialState(p)
    assertEquals(p.getMaxBufferCapacity(), Flow.defaultBufferSize())
    val e = p.getExecutor()
    val c = ForkJoinPool.commonPool()
    if (ForkJoinPool.getCommonPoolParallelism() > 1) assertSame(e, c)
    else assertNotSame(e, c)
  }

  /** A new SubmissionPublisher has no subscribers, is not closed, has the given
   *  buffer size, and uses the given executor
   */
  @Test def testConstructor2(): Unit = {
    val e = Executors.newFixedThreadPool(1)
    val p = new SubmissionPublisher[Integer](e, 8)
    checkInitialState(p)
    assertSame(p.getExecutor(), e)
    assertEquals(8, p.getMaxBufferCapacity())
  }

  /** A null Executor argument to SubmissionPublisher constructor throws
   *  NullPointerException
   */
  @Test def testConstructor3(): Unit = {
    try {
      new SubmissionPublisher[Integer](null, 8)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** A negative capacity argument to SubmissionPublisher constructor throws
   *  IllegalArgumentException
   */
  @Test def testConstructor4(): Unit = {
    val e = Executors.newFixedThreadPool(1)
    try {
      new SubmissionPublisher[Integer](e, -1)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException => ()
    }
  }

  /** A closed publisher reports isClosed with no closedException and throws
   *  IllegalStateException upon attempted submission; a subsequent close or
   *  closeExceptionally has no additional effect.
   */
  @Test def testClose(): Unit = {
    val p = basicPublisher()
    checkInitialState(p)
    p.close()
    assertTrue(p.isClosed())
    assertNull(p.getClosedException())
    try {
      p.submit(1)
      shouldThrow()
    } catch {
      case success: IllegalStateException => ()
    }
    val ex = new SPException()
    p.closeExceptionally(ex)
    assertTrue(p.isClosed())
    assertNull(p.getClosedException())
  }

  /** A publisher closedExceptionally reports isClosed with the closedException
   *  and throws IllegalStateException upon attempted submission; a subsequent
   *  close or closeExceptionally has no additional effect.
   */
  @Test def testCloseExceptionally(): Unit = {
    val p = basicPublisher()
    checkInitialState(p)
    val ex = new SPException()
    p.closeExceptionally(ex)
    assertTrue(p.isClosed())
    assertSame(p.getClosedException(), ex)
    try {
      p.submit(1)
      shouldThrow()
    } catch {
      case success: IllegalStateException => ()
    }
    p.close()
    assertTrue(p.isClosed())
    assertSame(p.getClosedException(), ex)
  }

  /** Upon subscription, the subscriber's onSubscribe is called, no other
   *  Subscriber methods are invoked, the publisher hasSubscribers, isSubscribed
   *  is true, and existing subscriptions are unaffected.
   */
  @Test def testSubscribe1(): Unit = {
    val s = new TestSubscriber()
    val p = basicPublisher()
    p.subscribe(s)
    assertTrue(p.hasSubscribers())
    assertEquals(1, p.getNumberOfSubscribers())
    assertTrue(p.getSubscribers().contains(s))
    assertTrue(p.isSubscribed(s))
    s.awaitSubscribe()
    assertNotNull(s.sn)
    assertEquals(0, s.nexts)
    assertEquals(0, s.errors)
    assertEquals(0, s.completes)
    val s2 = new TestSubscriber()
    p.subscribe(s2)
    assertTrue(p.hasSubscribers())
    assertEquals(2, p.getNumberOfSubscribers())
    assertTrue(p.getSubscribers().contains(s))
    assertTrue(p.getSubscribers().contains(s2))
    assertTrue(p.isSubscribed(s))
    assertTrue(p.isSubscribed(s2))
    s2.awaitSubscribe()
    assertNotNull(s2.sn)
    assertEquals(0, s2.nexts)
    assertEquals(0, s2.errors)
    assertEquals(0, s2.completes)
    p.close()
  }

  /** If closed, upon subscription, the subscriber's onComplete method is
   *  invoked
   */
  @Test def testSubscribe2(): Unit = {
    val s = new TestSubscriber()
    val p = basicPublisher()
    p.close()
    p.subscribe(s)
    s.awaitComplete()
    assertEquals(0, s.nexts)
    assertEquals(0, s.errors)
    assertEquals(1, s.completes, 1)
  }

  /** If closedExceptionally, upon subscription, the subscriber's onError method
   *  is invoked
   */
  @Test def testSubscribe3(): Unit = {
    val s = new TestSubscriber()
    val p = basicPublisher()
    val ex = new SPException()
    p.closeExceptionally(ex)
    assertTrue(p.isClosed())
    assertSame(p.getClosedException(), ex)
    p.subscribe(s)
    s.awaitError()
    assertEquals(0, s.nexts)
    assertEquals(1, s.errors)
  }

  /** Upon attempted resubscription, the subscriber's onError is called and the
   *  subscription is cancelled.
   */
  @Test def testSubscribe4(): Unit = {
    val s = new TestSubscriber()
    val p = basicPublisher()
    p.subscribe(s)
    assertTrue(p.hasSubscribers())
    assertEquals(1, p.getNumberOfSubscribers())
    assertTrue(p.getSubscribers().contains(s))
    assertTrue(p.isSubscribed(s))
    s.awaitSubscribe()
    assertNotNull(s.sn)
    assertEquals(0, s.nexts)
    assertEquals(0, s.errors)
    assertEquals(0, s.completes)
    p.subscribe(s)
    s.awaitError()
    assertEquals(0, s.nexts)
    assertEquals(1, s.errors)
    assertFalse(p.isSubscribed(s))
  }

  /** An exception thrown in onSubscribe causes onError */
  @Test def testSubscribe5(): Unit = {
    val s = new TestSubscriber()
    val p = basicPublisher()
    s.throwOnCall = true
    p.subscribe(s)
    s.awaitError()
    assertEquals(0, s.nexts)
    assertEquals(1, s.errors)
    assertEquals(0, s.completes)
  }

  /** subscribe(null) throws NPE
   */
  @Test def testSubscribe6(): Unit = {
    val p = basicPublisher()
    try {
      p.subscribe(null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
    checkInitialState(p)
  }

  /** Closing a publisher causes onComplete to subscribers
   */
  @Test def testCloseCompletes(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    p.submit(1)
    p.close()
    assertTrue(p.isClosed())
    assertNull(p.getClosedException())
    s1.awaitComplete()
    assertEquals(1, s1.nexts)
    assertEquals(1, s1.completes)
    s2.awaitComplete()
    assertEquals(1, s2.nexts)
    assertEquals(1, s2.completes)
  }

  /** Closing a publisher exceptionally causes onError to subscribers after they
   *  are subscribed
   */
  @Test def testCloseExceptionallyError(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    p.submit(1)
    p.closeExceptionally(new SPException())
    assertTrue(p.isClosed())
    s1.awaitSubscribe()
    s1.awaitError()
    assertTrue(s1.nexts <= 1)
    assertEquals(1, s1.errors)
    s2.awaitSubscribe()
    s2.awaitError()
    assertTrue(s2.nexts <= 1)
    assertEquals(1, s2.errors)
  }

  /** Cancelling a subscription eventually causes no more onNexts to be issued
   */
  @Test def testCancel(): Unit = {
    val p = new SubmissionPublisher[Integer](basicExecutor, 4) // must be < 20

    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    s1.awaitSubscribe()
    p.submit(1)
    s1.sn.cancel()
    for (i <- 2 to 20) {
      p.submit(i)
    }
    p.close()
    s2.awaitComplete()
    assertEquals(20, s2.nexts)
    assertEquals(1, s2.completes)
    assertTrue(s1.nexts < 20)
    assertFalse(p.isSubscribed(s1))
  }

  /** Throwing an exception in onNext causes onError */
  @Test def testThrowOnNext(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    s1.awaitSubscribe()
    p.submit(1)
    s1.throwOnCall = true
    p.submit(2)
    p.close()
    s2.awaitComplete()
    assertEquals(2, s2.nexts)
    s1.awaitComplete()
    assertEquals(1, s1.errors)
  }

  /** If a handler is supplied in constructor, it is invoked when subscriber
   *  throws an exception in onNext
   */
  @Test def testThrowOnNextHandler(): Unit = {
    val calls = new AtomicInteger()
    val p = new SubmissionPublisher[Integer](
      basicExecutor,
      8,
      (s: Flow.Subscriber[_ >: Integer], e: Throwable) =>
        calls.getAndIncrement()
    )
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    s1.awaitSubscribe()
    p.submit(1)
    s1.throwOnCall = true
    p.submit(2)
    p.close()
    s2.awaitComplete()
    assertEquals(2, s2.nexts)
    assertEquals(1, s2.completes)
    s1.awaitError()
    assertEquals(1, s1.errors)
    assertEquals(1, calls.get())
  }

  /** onNext items are issued in the same order to each subscriber */
  @Test def testOrder(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    for (i <- 1 to 20) {
      p.submit(i)
    }
    p.close()
    s2.awaitComplete()
    s1.awaitComplete()
    assertEquals(20, s2.nexts)
    assertEquals(1, s2.completes)
    assertEquals(20, s1.nexts)
    assertEquals(1, s1.completes)
  }

  /** onNext is issued only if requested
   */
  @Test def testRequest1(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    s1.request = false
    p.subscribe(s1)
    s1.awaitSubscribe()
    assertEquals(0, p.estimateMinimumDemand())
    val s2 = new TestSubscriber()
    p.subscribe(s2)
    p.submit(1)
    p.submit(2)
    s2.awaitNext(1)
    assertEquals(0, s1.nexts)
    s1.sn.request(3)
    p.submit(3)
    p.close()
    s2.awaitComplete()
    assertEquals(3, s2.nexts)
    assertEquals(1, s2.completes)
    s1.awaitComplete()
    assertTrue(s1.nexts > 0)
    assertEquals(1, s1.completes)
  }

  /** onNext is not issued when requests become zero */
  @Test def testRequest2(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    s1.request = false
    p.submit(1)
    p.submit(2)
    p.close()
    s2.awaitComplete()
    assertEquals(2, s2.nexts)
    assertEquals(1, s2.completes)
    s1.awaitNext(1)
    assertEquals(1, s1.nexts)
  }

  /** Non-positive request causes error */
  @Test def testRequest3(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    val s3 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    p.subscribe(s3)
    s3.awaitSubscribe()
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    s1.sn.request(-1L)
    s3.sn.request(0L)
    p.submit(1)
    p.submit(2)
    p.close()
    s2.awaitComplete()
    assertEquals(2, s2.nexts)
    assertEquals(1, s2.completes)
    s1.awaitError()
    assertEquals(1, s1.errors)
    assertTrue(s1.lastError.isInstanceOf[IllegalArgumentException])
    s3.awaitError()
    assertEquals(1, s3.errors)
    assertTrue(s3.lastError.isInstanceOf[IllegalArgumentException])
  }

  /** estimateMinimumDemand reports 0 until request, nonzero after request */
  @Test def testEstimateMinimumDemand(): Unit = {
    val s = new TestSubscriber()
    val p = basicPublisher()
    s.request = false
    p.subscribe(s)
    s.awaitSubscribe()
    assertEquals(0, p.estimateMinimumDemand())
    s.sn.request(1)
    assertEquals(1, p.estimateMinimumDemand())
  }

  /** submit to a publisher with no subscribers returns lag 0 */
  @Test def testEmptySubmit(): Unit = {
    val p = basicPublisher()
    assertEquals(0, p.submit(1))
  }

  /** submit(null) throws NPE */
  @Test def testNullSubmit(): Unit = {
    val p = basicPublisher()
    try {
      p.submit(null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** submit returns number of lagged items, compatible with result of
   *  estimateMaximumLag.
   */
  @Test def testLaggedSubmit(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    assertEquals(1, p.submit(1))
    assertTrue(p.estimateMaximumLag() >= 1)
    assertTrue(p.submit(2) >= 2)
    assertTrue(p.estimateMaximumLag() >= 2)
    s1.sn.request(4)
    assertTrue(p.submit(3) >= 3)
    assertTrue(p.estimateMaximumLag() >= 3)
    s2.sn.request(4)
    p.submit(4)
    p.close()
    s2.awaitComplete()
    assertEquals(4, s2.nexts)
    s1.awaitComplete()
    assertEquals(4, s2.nexts)
  }

  /** submit eventually issues requested items when buffer capacity is 1
   */
  @Test def testCap1Submit(): Unit = {
    val p = new SubmissionPublisher[Integer](basicExecutor, 1)
    val s1 = new TestSubscriber()
    val s2 = new TestSubscriber()
    p.subscribe(s1)
    p.subscribe(s2)
    for (i <- 1 to 20) {
      assertTrue(p.submit(i) >= 0)
    }
    p.close()
    s2.awaitComplete()
    s1.awaitComplete()
    assertEquals(20, s2.nexts)
    assertEquals(1, s2.completes)
    assertEquals(20, s1.nexts)
    assertEquals(1, s1.completes)
  }

  /** offer to a publisher with no subscribers returns lag 0 */
  @Test def testEmptyOffer(): Unit = {
    val p = basicPublisher()
    assertEquals(0, p.offer(1, null))
  }

  /** offer(null) throws NPE */
  @Test def testNullOffer(): Unit = {
    val p = basicPublisher()
    try {
      p.offer(null, null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** offer returns number of lagged items if not saturated */
  @Test def testLaggedOffer(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    assertTrue(p.offer(1, null) >= 1)
    assertTrue(p.offer(2, null) >= 2)
    s1.sn.request(4)
    assertTrue(p.offer(3, null) >= 3)
    s2.sn.request(4)
    p.offer(4, null)
    p.close()
    s2.awaitComplete()
    assertEquals(4, s2.nexts)
    s1.awaitComplete()
    assertEquals(4, s2.nexts)
  }

  /** offer reports drops if saturated */
  @Test def testDroppedOffer(): Unit = {
    val p = new SubmissionPublisher[Integer](basicExecutor, 4)
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    for (i <- 1 to 4) {
      assertTrue(p.offer(i, null) >= 0)
    }
    p.offer(5, null)
    assertTrue(p.offer(6, null) < 0)
    s1.sn.request(64)
    assertTrue(p.offer(7, null) < 0)
    s2.sn.request(64)
    p.close()
    s2.awaitComplete()
    assertTrue(s2.nexts >= 4)
    s1.awaitComplete()
    assertTrue(s1.nexts >= 4)
  }

  /** offer invokes drop handler if saturated */
  @Test def testHandledDroppedOffer(): Unit = {
    val calls = new AtomicInteger()
    val p = new SubmissionPublisher[Integer](basicExecutor, 4)
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    for (i <- 1 to 4) {
      assertTrue(
        p.offer(
          i,
          (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
        ) >= 0
      )
    }
    p.offer(
      4,
      (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
    )
    assertTrue(
      p.offer(
        6,
        (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
      ) < 0
    )
    s1.sn.request(64)
    assertTrue(
      p.offer(
        7,
        (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
      ) < 0
    )
    s2.sn.request(64)
    p.close()
    s2.awaitComplete()
    s1.awaitComplete()
    assertTrue(calls.get() >= 4)
  }

  /** offer succeeds if drop handler forces request */
  @Test def testRecoveredHandledDroppedOffer(): Unit = {
    val calls = new AtomicInteger()
    val p = new SubmissionPublisher[Integer](basicExecutor, 4)
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    var n = 0
    for (i <- 1 to 8) {
      val d = p.offer(
        i,
        (s: Flow.Subscriber[_ >: Integer], x: Integer) => reqHandle(calls, s)
      )
      n = n + 2 + (if (d < 0) d else 0)
    }
    p.close()
    s2.awaitComplete()
    s1.awaitComplete()
    assertEquals(n, s1.nexts + s2.nexts)
    assertTrue(calls.get() >= 2)
  }

  /** Timed offer to a publisher with no subscribers returns lag 0 */
  @Test def testEmptyTimedOffer(): Unit = {
    val p = basicPublisher()
    val startTime = System.nanoTime()
    assertEquals(0, p.offer(1, LONG_DELAY_MS, MILLISECONDS, null))
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
  }

  /** Timed offer with null item or TimeUnit throws NPE */
  @Test def testNullTimedOffer(): Unit = {
    val p = basicPublisher()
    val startTime = System.nanoTime()
    try {
      p.offer(null, LONG_DELAY_MS, MILLISECONDS, null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
    try {
      p.offer(1, LONG_DELAY_MS, null, null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
  }

  /** Timed offer returns number of lagged items if not saturated */
  @Test def testLaggedTimedOffer(): Unit = {
    val p = basicPublisher()
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    val startTime = System.nanoTime()
    assertTrue(p.offer(1, LONG_DELAY_MS, MILLISECONDS, null) >= 1)
    assertTrue(p.offer(2, LONG_DELAY_MS, MILLISECONDS, null) >= 2)
    s1.sn.request(4)
    assertTrue(p.offer(3, LONG_DELAY_MS, MILLISECONDS, null) >= 3)
    s2.sn.request(4)
    p.offer(4, LONG_DELAY_MS, MILLISECONDS, null)
    p.close()
    s2.awaitComplete()
    assertEquals(4, s2.nexts)
    s1.awaitComplete()
    assertEquals(4, s2.nexts)
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
  }

  /** Timed offer reports drops if saturated */
  @Test def testDroppedTimedOffer(): Unit = {
    val p = new SubmissionPublisher[Integer](basicExecutor, 4)
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    val delay = timeoutMillis()
    for (i <- 1 to 4) {
      assertTrue(p.offer(i, delay, MILLISECONDS, null) >= 0)
    }
    val startTime = System.nanoTime()
    assertTrue(p.offer(5, delay, MILLISECONDS, null) < 0)
    s1.sn.request(64)
    assertTrue(p.offer(6, delay, MILLISECONDS, null) < 0)
    // 2 * delay should elapse but check only 1 * delay to allow timer slop
    assertTrue(millisElapsedSince(startTime) >= delay)
    s2.sn.request(64)
    p.close()
    s2.awaitComplete()
    assertTrue(s2.nexts >= 2)
    s1.awaitComplete()
    assertTrue(s1.nexts >= 2)
  }

  /** Timed offer invokes drop handler if saturated */
  @Test def testHandledDroppedTimedOffer(): Unit = {
    val calls = new AtomicInteger()
    val p = new SubmissionPublisher[Integer](basicExecutor, 4)
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    val delay = timeoutMillis()
    for (i <- 1 to 4) {
      assertTrue(
        p.offer(
          i,
          delay,
          MILLISECONDS,
          (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
        ) >= 0
      )
    }
    val startTime = System.nanoTime()
    assertTrue(
      p.offer(
        5,
        delay,
        MILLISECONDS,
        (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
      ) < 0
    )
    s1.sn.request(64)
    assertTrue(
      p.offer(
        6,
        delay,
        MILLISECONDS,
        (s: Flow.Subscriber[_ >: Integer], x: Integer) => noopHandle(calls)
      ) < 0
    )
    assertTrue(millisElapsedSince(startTime) >= delay)
    s2.sn.request(64)
    p.close()
    s2.awaitComplete()
    s1.awaitComplete()
    assertTrue(calls.get() >= 2)
  }

  /** Timed offer succeeds if drop handler forces request */
  @Test def testRecoveredHandledDroppedTimedOffer(): Unit = {
    val calls = new AtomicInteger()
    val p = new SubmissionPublisher[Integer](basicExecutor, 4)
    val s1 = new TestSubscriber()
    s1.request = false
    val s2 = new TestSubscriber()
    s2.request = false
    p.subscribe(s1)
    p.subscribe(s2)
    s2.awaitSubscribe()
    s1.awaitSubscribe()
    var n = 0
    val delay = timeoutMillis()
    val startTime = System.nanoTime()
    for (i <- 1 to 6) {
      val d = p.offer(
        i,
        delay,
        MILLISECONDS,
        (s: Flow.Subscriber[_ >: Integer], x: Integer) => reqHandle(calls, s)
      )
      n = n + 2 + (if (d < 0) d else 0)
    }
    assertTrue(millisElapsedSince(startTime) >= delay)
    p.close()
    s2.awaitComplete()
    s1.awaitComplete()
    assertEquals(n, s1.nexts + s2.nexts)
    assertTrue(calls.get() >= 2)
  }

  /** consume returns a CompletableFuture that is done when publisher completes
   */
  @Test def testConsume(): Unit = {
    val sum = new AtomicInteger()
    val p = basicPublisher()
    val f = p.consume((x: Integer) => sum.getAndAdd(x.intValue))
    val n = 20
    for (i <- 1 to n) {
      p.submit(i)
    }
    p.close()
    f.join
    assertEquals((n * (n + 1)) / 2, sum.get())
  }

  /** consume(null) throws NPE */
  @Test def testConsumeNPE(): Unit = {
    val p = basicPublisher()
    try {
      val f = p.consume(null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** consume eventually stops processing published items if cancelled */
  @Test def testCancelledConsume(): Unit = {
    val count = new AtomicInteger()
    val p = basicPublisher()
    val f = p.consume((x: Integer) => count.getAndIncrement())
    f.cancel(true)
    val n = 1000000 // arbitrary limit

    for (i <- 1 to n) {
      p.submit(i)
    }
    assertTrue(count.get() < n)
  }

  /** Tests scenario for JDK-8187947: A race condition in SubmissionPublisher
   *  cvs update -D '2017-11-25'
   *  src/main/java/util/concurrent/SubmissionPublisher.java && ant
   *  -Djsr166.expensiveTests=true -Djsr166.tckTestClass=SubmissionPublisherTest
   *  -Djsr166.methodFilter=testMissedSignal tck; cvs update -A
   *  src/main/java/util/concurrent/SubmissionPublisher.java
   */
  @Test def testMissedSignal_8187947(): Unit = {
    // JDK-8212899
    val fjpFactor =
      if ((ForkJoinPool.getCommonPoolParallelism() < 2))
        (1 << 5) // 32
      else
        (1 << 10) // 1024
    val expenseFactor = if (expensiveTests) 1024 else 1
    val N = fjpFactor * expenseFactor
    val finished = new CountDownLatch(1)
    val pub = new SubmissionPublisher[Boolean]
    class Sub extends Flow.Subscriber[Boolean] {
      var received = 0

      override def onSubscribe(s: Flow.Subscription): Unit = {
        s.request(N)
      }

      override def onNext(item: Boolean): Unit = {
        if ({
              received += 1; received
            } == N) finished.countDown()
        else CompletableFuture.runAsync(() => pub.submit(true))
      }

      override def onError(t: Throwable): Unit = {
        throw new AssertionError(t)
      }

      override def onComplete(): Unit = {}
    }
    pub.subscribe(new Sub)
    CompletableFuture.runAsync(() => pub.submit(true))
    await(finished)
  }
}
