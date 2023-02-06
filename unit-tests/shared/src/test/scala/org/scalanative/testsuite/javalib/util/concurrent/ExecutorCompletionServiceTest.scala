/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent._
import java.util.concurrent.TimeUnit._
import java.util.concurrent.atomic.AtomicBoolean

import org.junit.Test
import org.junit.Assert._

class ExecutorCompletionServiceTest extends JSR166Test {
  import JSR166Test._

  /** new ExecutorCompletionService(null) throws NullPointerException
   */
  @Test def testConstructorNPE(): Unit = {
    try {
      new ExecutorCompletionService[Any](null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** new ExecutorCompletionService(e, null) throws NullPointerException
   */
  @Test def testConstructorNPE2(): Unit = {
    try {
      new ExecutorCompletionService[Any](cachedThreadPool, null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** ecs.submit(null) throws NullPointerException
   */
  @Test def testSubmitNullCallable(): Unit = {
    val cs =
      new ExecutorCompletionService[Any](cachedThreadPool)
    try {
      cs.submit(null.asInstanceOf[Callable[Any]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** ecs.submit(null, val) throws NullPointerException
   */
  @Test def testSubmitNullRunnable(): Unit = {
    val cs =
      new ExecutorCompletionService[Any](cachedThreadPool)
    try {
      cs.submit(null.asInstanceOf[Runnable], java.lang.Boolean.TRUE)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** A taken submitted task is completed
   */
  @throws[Exception]
  @Test def testTake(): Unit = {
    val cs = new ExecutorCompletionService[String](cachedThreadPool)
    cs.submit(new StringTask)
    val f = cs.take
    assertTrue(f.isDone)
    assertEquals(TEST_STRING, f.get)
  }

  /** Take returns the same future object returned by submit
   */
  @throws[InterruptedException]
  @Test def testTake2(): Unit = {
    val cs = new ExecutorCompletionService[String](cachedThreadPool)
    val f1 = cs.submit(new StringTask)
    val f2 = cs.take
    assertEquals(f1, f2)
  }

  /** poll returns non-null when the returned task is completed
   */
  @throws[Exception]
  @Test def testPoll1(): Unit = {
    val cs =
      new ExecutorCompletionService[String](cachedThreadPool)
    assertNull(cs.poll)
    cs.submit(new StringTask)
    val startTime = System.nanoTime
    var f: Future[String] = null
    while ({ f = cs.poll(); f == null }) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
      Thread.`yield`()
    }
    assertTrue(f.isDone)
    assertEquals(TEST_STRING, f.get)
  }

  /** timed poll returns non-null when the returned task is completed
   */
  @throws[Exception]
  @Test def testPoll2(): Unit = {
    val cs =
      new ExecutorCompletionService[String](cachedThreadPool)
    assertNull(cs.poll)
    cs.submit(new StringTask)
    val startTime = System.nanoTime
    var f: Future[String] = null
    while ({ f = cs.poll(timeoutMillis(), MILLISECONDS); f == null }) {
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
      Thread.`yield`()
    }
    assertTrue(f.isDone)
    assertEquals(TEST_STRING, f.get)
  }

  /** poll returns null before the returned task is completed
   */
  @throws[Exception]
  @Test def testPollReturnsNullBeforeCompletion(): Unit = {
    val cs =
      new ExecutorCompletionService[String](cachedThreadPool)
    val proceed = new CountDownLatch(1)
    cs.submit(new Callable[String]() {
      @throws[Exception]
      override def call: String = {
        await(proceed)
        TEST_STRING
      }
    })
    assertNull(cs.poll)
    assertNull(cs.poll(0L, MILLISECONDS))
    assertNull(cs.poll(java.lang.Long.MIN_VALUE, MILLISECONDS))
    val startTime = System.nanoTime
    assertNull(cs.poll(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    proceed.countDown()
    assertEquals(TEST_STRING, cs.take.get)
  }

  /** successful and failed tasks are both returned
   */
  @throws[Exception]
  @Test def testTaskAssortment(): Unit = {
    val cs =
      new ExecutorCompletionService[String](cachedThreadPool)
    val ex = new ArithmeticException
    val rounds = 2
    locally {
      var i = rounds
      while (i > 0) {
        i -= 1
        cs.submit(new StringTask)
        cs.submit(callableThrowing(ex))
        cs.submit(runnableThrowing(ex), null)
      }
    }
    locally {
      var normalCompletions = 0
      var exceptionalCompletions = 0
      var i = 3 * rounds
      while (i > 0) try {
        i -= 1
        assertEquals(TEST_STRING, cs.take.get)
        normalCompletions += 1
      } catch {
        case expected: ExecutionException =>
          assertEquals(ex, expected.getCause)
          exceptionalCompletions += 1
      }
      assertEquals(1 * rounds, normalCompletions)
      assertEquals(2 * rounds, exceptionalCompletions)
    }
    assertNull(cs.poll)
  }

  /** Submitting to underlying AES that overrides newTaskFor(Callable) returns
   *  and eventually runs Future returned by newTaskFor.
   */
  @throws[InterruptedException]
  @Test def testNewTaskForCallable(): Unit = {
    val _done = new AtomicBoolean(false)
    class MyCallableFuture[V](val c: Callable[V]) extends FutureTask[V](c) {
      override protected def done(): Unit = _done.set(true)
    }
    val e = new ThreadPoolExecutor(
      1,
      1,
      30L,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](1)
    ) {
      override protected def newTaskFor[T](c: Callable[T]) =
        new MyCallableFuture[T](c)
    }

    val cs = new ExecutorCompletionService[String](e)
    usingPoolCleaner(e) { e =>
      assertNull(cs.poll)
      val c = new StringTask
      val f1 = cs.submit(c)
      assertTrue(
        "submit must return MyCallableFuture",
        f1.isInstanceOf[MyCallableFuture[_]]
      )
      val f2 = cs.take
      assertEquals("submit and take must return same objects", f1, f2)
      assertTrue("completed task must have set done", _done.get)
    }
  }

  /** Submitting to underlying AES that overrides newTaskFor(Runnable,T) returns
   *  and eventually runs Future returned by newTaskFor.
   */
  @throws[InterruptedException]
  @Test def testNewTaskForRunnable(): Unit = {
    val _done = new AtomicBoolean(false)
    class MyRunnableFuture[V](val t: Runnable, val r: V)
        extends FutureTask[V](t, r) {
      override protected def done(): Unit = _done.set(true)
    }
    val e = new ThreadPoolExecutor(
      1,
      1,
      30L,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](1)
    ) {
      override protected def newTaskFor[T](
          t: Runnable,
          r: T
      ) = new MyRunnableFuture[T](t, r)
    }

    val cs = new ExecutorCompletionService[String](e)
    usingPoolCleaner(e) { e =>
      assertNull(cs.poll)
      val r = new NoOpRunnable
      val f1 = cs.submit(r, null)
      assertTrue(
        "submit must return MyRunnableFuture",
        f1.isInstanceOf[MyRunnableFuture[_]]
      )
      val f2 = cs.take
      assertEquals("submit and take must return same objects", f1, f2)
      assertTrue("completed task must have set done", _done.get)
    }
  }
}
