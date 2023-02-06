/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.Test

import java.util
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.{ArrayList, Collection, Collections, List}
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean

object AbstractExecutorServiceTest {

  /** A no-frills implementation of AbstractExecutorService, designed to test
   *  the submit methods only.
   */
  class DirectExecutorService extends AbstractExecutorService {
    override def execute(r: Runnable): Unit = r.run()
    override def shutdown(): Unit = inShutdown = true
    override def shutdownNow: util.List[Runnable] = {
      inShutdown = true
      Collections.EMPTY_LIST.asInstanceOf[util.List[Runnable]]
    }
    override def isShutdown: Boolean = inShutdown
    override def isTerminated: Boolean = inShutdown
    override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
      isShutdown
    private var inShutdown: Boolean = false
  }
}

class AbstractExecutorServiceTest extends JSR166Test {
  import JSR166Test._

  /** execute(runnable) runs it to completion
   */
  @throws[Exception]
  @Test def testExecuteRunnable(): Unit = {
    val e = new AbstractExecutorServiceTest.DirectExecutorService
    val done = new AtomicBoolean(false)
    val future = e.submit(new CheckedRunnable() {
      override def realRun(): Unit = { done.set(true) }
    })
    assertNull(future.get)
    assertNull(future.get(0, MILLISECONDS))
    assertTrue(done.get)
    assertTrue(future.isDone)
    assertFalse(future.isCancelled)
  }

  /** Completed submit(callable) returns result
   */
  @throws[Exception]
  @Test def testSubmitCallable(): Unit = {
    val e = new AbstractExecutorServiceTest.DirectExecutorService
    val future = e.submit(new StringTask)
    val result = future.get
    assertEquals(TEST_STRING, result)
  }

  /** Completed submit(runnable) returns successfully
   */
  @throws[Exception]
  @Test def testSubmitRunnable(): Unit = {
    val e = new AbstractExecutorServiceTest.DirectExecutorService
    val future = e.submit(new NoOpRunnable)
    future.get
    assertTrue(future.isDone)
  }

  /** Completed submit(runnable, result) returns result
   */
  @throws[Exception]
  @Test def testSubmitRunnable2(): Unit = {
    val e = new AbstractExecutorServiceTest.DirectExecutorService
    val future = e.submit(new NoOpRunnable, TEST_STRING)
    val result = future.get
    assertEquals(TEST_STRING, result)
  }

  // No PrivilegedAction in Scala Native
  // @Test def testSubmitPrivilegedAction(): Unit = {}
  // @Test def testSubmitPrivilegedExceptionAction(): Unit = {}
  // @Test def testSubmitFailedPrivilegedExceptionAction(): Unit = {}

  /** Submitting null tasks throws NullPointerException
   */
  @Test def testNullTaskSubmission(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { assertNullTaskSubmissionThrowsNullPointerException }

  /** submit(callable).get() throws InterruptedException if interrupted
   */
  @throws[InterruptedException]
  @Test def testInterruptedSubmit(): Unit = {
    val submitted = new CountDownLatch(1)
    val quittingTime = new CountDownLatch(1)
    val awaiter = new CheckedCallable[Void]() {
      @throws[InterruptedException]
      override def realCall(): Void = {
        assertTrue(quittingTime.await(2 * LONG_DELAY_MS, MILLISECONDS))
        null
      }
    }
    usingPoolCleaner[ThreadPoolExecutor, Unit](
      new ThreadPoolExecutor(
        1,
        1,
        60,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue[Runnable](10)
      ),
      cleaner(_, quittingTime)
    ) { p =>
      val t = newStartedThread(
        new CheckedInterruptedRunnable() {
          @throws[Exception]
          override def realRun(): Unit = {
            val future = p.submit(awaiter)
            submitted.countDown()
            future.get
          }
        }
      )
      await(submitted)
      t.interrupt()
      awaitTermination(t)
    }
  }

  /** get of submit(callable) throws ExecutionException if callable throws
   *  exception
   */
  @throws[InterruptedException]
  @Test def testSubmitEE(): Unit = usingPoolCleaner(
    new ThreadPoolExecutor(
      1,
      1,
      60,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
  ) { p =>
    val c = new Callable[Any]() {
      override def call = throw new ArithmeticException
    }
    try {
      p.submit(c).get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertTrue(success.getCause.isInstanceOf[ArithmeticException])
    }
  }

  /** invokeAny(null) throws NPE
   */
  @throws[Exception]
  @Test def testInvokeAny1(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    try {
      e.invokeAny(null)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Exception]
  @Test def testInvokeAny2(): Unit =
    usingPoolCleaner(new AbstractExecutorServiceTest.DirectExecutorService) {
      e =>
        val emptyCollection = Collections.emptyList
        try {
          e.invokeAny(emptyCollection)
          shouldThrow()
        } catch { case success: IllegalArgumentException => () }
    }

  /** invokeAny(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testInvokeAny3(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[Long]]
    l.add(new Callable[Long]() {
      override def call = throw new ArithmeticException
    })
    l.add(null)
    try {
      e.invokeAny(l)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** invokeAny(c) throws ExecutionException if no task in c completes
   */
  @throws[InterruptedException]
  @Test def testInvokeAny4(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new NPETask)
    try {
      e.invokeAny(l)
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertTrue(success.getCause.isInstanceOf[NullPointerException])
    }
  }

  /** invokeAny(c) returns result of some task in c if at least one completes
   */
  @throws[Exception]
  @Test def testInvokeAny5(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    l.add(new StringTask)
    val result = e.invokeAny(l)
    assertEquals(TEST_STRING, result)
  }

  /** invokeAll(null) throws NPE
   */
  @throws[InterruptedException]
  @Test def testInvokeAll1(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    try {
      e.invokeAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** invokeAll(empty collection) returns empty list
   */
  @throws[InterruptedException]
  @Test def testInvokeAll2(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val emptyCollection = Collections.emptyList
    val r = e.invokeAll(emptyCollection)
    assertTrue(r.isEmpty)
  }

  /** invokeAll(c) throws NPE if c has null elements
   */
  @throws[InterruptedException]
  @Test def testInvokeAll3(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    l.add(null)
    try {
      e.invokeAll(l)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** get of returned element of invokeAll(c) throws exception on failed task
   */
  @throws[Exception]
  @Test def testInvokeAll4(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new NPETask)
    val futures = e.invokeAll(l)
    assertEquals(1, futures.size)
    try {
      futures.get(0).get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertTrue(success.getCause.isInstanceOf[NullPointerException])
    }
  }

  /** invokeAll(c) returns results of all completed tasks in c
   */
  @throws[Exception]
  @Test def testInvokeAll5(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    l.add(new StringTask)
    val futures = e.invokeAll(l)
    assertEquals(2, futures.size)
    futures.forEach { future => assertEquals(TEST_STRING, future.get) }
  }

  /** timed invokeAny(null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAny1(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    try {
      e.invokeAny(null, randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** timed invokeAny(null time unit) throws NullPointerException
   */
  @throws[Exception]
  @Test def testTimedInvokeAnyNullTimeUnit(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    try {
      e.invokeAny(l, randomTimeout(), null)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** timed invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Exception]
  @Test def testTimedInvokeAny2(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val emptyCollection = Collections.emptyList
    try {
      e.invokeAny(emptyCollection, randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch { case success: IllegalArgumentException => () }
  }

  /** timed invokeAny(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testTimedInvokeAny3(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[Long]]
    l.add(new Callable[Long]() {
      override def call = throw new ArithmeticException
    })
    l.add(null)
    try {
      e.invokeAny(l, randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  /** timed invokeAny(c) throws ExecutionException if no task completes
   */
  @throws[Exception]
  @Test def testTimedInvokeAny4(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val startTime = System.nanoTime
    val l = new util.ArrayList[Callable[String]]
    l.add(new NPETask)
    try {
      e.invokeAny(l, LONG_DELAY_MS, MILLISECONDS)
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertTrue(success.getCause.isInstanceOf[NullPointerException])
    }
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
  }

  /** timed invokeAny(c) returns result of some task in c
   */
  @throws[Exception]
  @Test def testTimedInvokeAny5(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val startTime = System.nanoTime
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    l.add(new StringTask)
    val result = e.invokeAny(l, LONG_DELAY_MS, MILLISECONDS)
    assertEquals(TEST_STRING, result)
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
  }

  /** timed invokeAll(null) throws NullPointerException
   */
  @throws[InterruptedException]
  @Test def testTimedInvokeAll1(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    try {
      e.invokeAll(null, randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** timed invokeAll(null time unit) throws NPE
   */
  @throws[InterruptedException]
  @Test def testTimedInvokeAllNullTimeUnit(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    try {
      e.invokeAll(l, randomTimeout(), null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** timed invokeAll(empty collection) returns empty list
   */
  @throws[InterruptedException]
  @Test def testTimedInvokeAll2(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val emptyCollection = Collections.emptyList
    val r = e.invokeAll(emptyCollection, randomTimeout(), randomTimeUnit())
    assertTrue(r.isEmpty)
  }

  /** timed invokeAll(c) throws NullPointerException if c has null elements
   */
  @throws[InterruptedException]
  @Test def testTimedInvokeAll3(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    l.add(null)
    try {
      e.invokeAll(l, randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: NullPointerException => ()
    }
  }

  @throws[Exception]
  @Test def testTimedInvokeAll4(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new NPETask)
    val futures = e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
    assertEquals(1, futures.size)
    try {
      futures.get(0).get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertTrue(success.getCause.isInstanceOf[NullPointerException])
    }
  }

  /** timed invokeAll(c) returns results of all completed tasks in c
   */
  @throws[Exception]
  @Test def testTimedInvokeAll5(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    val l = new util.ArrayList[Callable[String]]
    l.add(new StringTask)
    l.add(new StringTask)
    val futures = e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
    assertEquals(2, futures.size)
    futures.forEach { future => assertEquals(TEST_STRING, future.get) }
  }

  /** timed invokeAll cancels tasks not completed by timeout
   */
  @throws[Exception]
  @Test def testTimedInvokeAll6(): Unit = usingPoolCleaner(
    new AbstractExecutorServiceTest.DirectExecutorService
  ) { e =>
    var timeout = timeoutMillis()
    import scala.util.control.Breaks
    val outer = new Breaks()
    val continue = new Breaks()
    outer.breakable {
      while (true) {
        continue.breakable {
          val tasks = new util.ArrayList[Callable[String]]
          tasks.add(new StringTask("0"))
          tasks.add(
            Executors.callable(
              possiblyInterruptedRunnable(timeout),
              TEST_STRING
            )
          )
          tasks.add(new StringTask("2"))
          val startTime = System.nanoTime
          val futures = e.invokeAll(tasks, timeout, MILLISECONDS)
          assertEquals(tasks.size, futures.size)
          assertTrue(millisElapsedSince(startTime) >= timeout)
          futures.forEach { future => assertTrue(future.isDone) }
          try {
            assertEquals("0", futures.get(0).get)
            assertEquals(TEST_STRING, futures.get(1).get)
          } catch {
            case retryWithLongerTimeout: CancellationException =>
              // unusual delay before starting second task
              timeout *= 2
              if (timeout >= LONG_DELAY_MS / 2)
                fail("expected exactly one task to be cancelled")
              continue.break()
          }
          assertTrue(futures.get(2).isCancelled)
          outer.break()
        }
      }
    }
  }
}
