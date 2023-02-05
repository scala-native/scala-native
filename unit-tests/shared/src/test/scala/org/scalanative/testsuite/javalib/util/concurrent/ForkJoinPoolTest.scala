/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent._
import java.util.concurrent.TimeUnit._
import java.util.concurrent.atomic._
import java.util.concurrent.locks._

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object ForkJoinPoolTest {
  class MyError extends Error {}

  // to test handlers
  class FailingFJWSubclass(p: ForkJoinPool) extends ForkJoinWorkerThread(p) {
    override protected def onStart(): Unit = {
      super.onStart()
      throw new MyError()
    }
  }

  class FailingThreadFactory()
      extends ForkJoinPool.ForkJoinWorkerThreadFactory {
    final val calls = new AtomicInteger(0)
    override def newThread(p: ForkJoinPool): ForkJoinWorkerThread = {
      if (calls.incrementAndGet() > 1) null
      else new FailingFJWSubclass(p)
    }
  }

  class SubFJP() extends ForkJoinPool(1) {
    // to expose protected
    def drainTasks[T](c: Collection[_ >: ForkJoinTask[_]]) =
      super.drainTasksTo(c)
    // def drainTasksTo(c: Collection[ForkJoinTask[_]]) = super.drainTasksTo(c)
    override def pollSubmission() = super.pollSubmission()
  }

  class ManagedLocker(lock: ReentrantLock) extends ForkJoinPool.ManagedBlocker {
    var hasLock = false
    def block(): Boolean = {
      if (!hasLock) lock.lock()
      true
    }
    def isReleasable(): Boolean = hasLock || {
      hasLock = lock.tryLock()
      hasLock
    }
  }

  // A simple recursive task for testing
  final class FibTask(number: Int) extends RecursiveTask[Int] {
    override protected def compute(): Int = {
      val n = number
      if (n <= 1) n
      else {
        val f1 = new FibTask(n - 1)
        f1.fork()
        new FibTask(n - 2).compute() + f1.join()
      }
    }
  }

  //  // A failing task for testing
  //  static final class FailingTask extends ForkJoinTask<Void> {
  //      public final Void getRawResult() { return null }
  //      protected final void setRawResult(Void mustBeNull) { }
  //      protected final boolean exec() { throw new Error() }
  //      FailingTask() {}
  //  }

  // Fib needlessly using locking to test ManagedBlockers
  final class LockingFibTask(
      number: Int,
      locker: ManagedLocker,
      lock: ReentrantLock
  ) extends RecursiveTask[Int] {
    override protected def compute(): Int = {
      var f1: LockingFibTask = null
      var f2: LockingFibTask = null
      locker.block()
      val n = number
      if (n > 1) {
        f1 = new LockingFibTask(n - 1, locker, lock)
        f2 = new LockingFibTask(n - 2, locker, lock)
      }
      lock.unlock()
      if (n <= 1) n
      else {
        f1.fork()
        f2.compute() + f1.join()
      }
    }
  }
}

class ForkJoinPoolTest extends JSR166Test {
  import JSR166Test._
  import ForkJoinPoolTest._
  /*
   * Testing coverage notes:
   *
   * 1. shutdown and related methods are tested via super.joinPool.
   *
   * 2. newTaskFor and adapters are tested in submit/invoke tests
   *
   * 3. We cannot portably test monitoring methods such as
   * getStealCount() since they rely ultimately on random task
   * stealing that may cause tasks not to be stolen/propagated
   * across threads, especially on uniprocessors.
   *
   * 4. There are no independently testable ForkJoinWorkerThread
   * methods, but they are covered here and in task tests.
   */

  // Some classes to test extension and factory methods

  /** Successfully constructed pool reports default factory, parallelism and
   *  async mode policies, no active threads or tasks, and quiescent running
   *  state.
   */
  @Test def testDefaultInitialState(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { p =>
      assertSame(
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        p.getFactory()
      )
      assertFalse(p.getAsyncMode())
      assertEquals(0, p.getActiveThreadCount())
      assertEquals(0, p.getStealCount())
      assertEquals(0, p.getQueuedTaskCount())
      assertEquals(0, p.getQueuedSubmissionCount())
      assertFalse(p.hasQueuedSubmissions())
      assertFalse(p.isShutdown())
      assertFalse(p.isTerminating())
      assertFalse(p.isTerminated())
    }
  }

  /** Constructor throws if size argument is less than zero
   */
  @Test def testConstructor1(): Unit = assertThrows(
    classOf[IllegalArgumentException],
    new ForkJoinPool(-1)
  )

  /** Constructor throws if factory argument is null
   */
  @Test def testConstructor2(): Unit = assertThrows(
    classOf[NullPointerException],
    new ForkJoinPool(1, null, null, false)
  )

  /** getParallelism returns size set in constructor
   */
  @Test def testGetParallelism(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { p =>
      assertEquals(1, p.getParallelism())
    }
  }

  /** getPoolSize returns number of started workers.
   */
  @Test def testGetPoolSize(): Unit = {
    val taskStarted = new CountDownLatch(1)
    val done = new CountDownLatch(1)
    val p = usingPoolCleaner(new ForkJoinPool(1)) { p =>
      assertEquals(0, p.getActiveThreadCount())
      val task: CheckedRunnable = () => {
        taskStarted.countDown()
        assertEquals(1, p.getPoolSize())
        assertEquals(1, p.getActiveThreadCount())
        await(done)
      }
      val future = p.submit(task)
      await(taskStarted)
      assertEquals(1, p.getPoolSize())
      assertEquals(1, p.getActiveThreadCount())
      done.countDown()
      p
    }
    assertEquals(0, p.getPoolSize())
    assertEquals(0, p.getActiveThreadCount())
  }

  // awaitTermination on a non-shutdown pool times out
  @Test def testAwaitTerminationTimesOut(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { p =>
      assertFalse(p.isTerminated())
      assertFalse(p.awaitTermination(java.lang.Long.MIN_VALUE, NANOSECONDS))
      assertFalse(p.awaitTermination(java.lang.Long.MIN_VALUE, MILLISECONDS))
      assertFalse(p.awaitTermination(-1L, NANOSECONDS))
      assertFalse(p.awaitTermination(-1L, MILLISECONDS))
      assertFalse(p.awaitTermination(randomExpiredTimeout(), randomTimeUnit()))

      locally {
        val timeoutNanos = 999999L
        val startTime = System.nanoTime()
        assertFalse(p.awaitTermination(timeoutNanos, NANOSECONDS))
        assertTrue(System.nanoTime() - startTime >= timeoutNanos)
        assertFalse(p.isTerminated())
      }
      locally {
        val startTime = System.nanoTime()
        val timeoutMillis = JSR166Test.timeoutMillis()
        assertFalse(p.awaitTermination(timeoutMillis, MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
      }
      assertFalse(p.isTerminated())
      p.shutdown()
      assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(p.isTerminated())
    }
  }

  /** setUncaughtExceptionHandler changes handler for uncaught exceptions.
   *
   *  Additionally tests: Overriding ForkJoinWorkerThread.onStart performs its
   *  defined action
   */
  @Test def testSetUncaughtExceptionHandler(): Unit = {
    val uehInvoked = new CountDownLatch(1)
    val ueh: Thread.UncaughtExceptionHandler = (t: Thread, e: Throwable) => {
      threadAssertTrue(e.isInstanceOf[MyError])
      threadAssertTrue(t.isInstanceOf[FailingFJWSubclass])
      uehInvoked.countDown()
    }
    usingPoolCleaner(
      new ForkJoinPool(1, new FailingThreadFactory(), ueh, false)
    ) { p =>
      assertSame(ueh, p.getUncaughtExceptionHandler())
      try {
        p.execute(new FibTask(8))
        await(uehInvoked)
      } finally p.shutdownNow() // failure might have prevented processing task
    }
  }

  /** After invoking a single task, isQuiescent eventually becomes true, at
   *  which time queues are empty, threads are not active, the task has
   *  completed successfully, and construction parameters continue to hold
   */
  @Test def testIsQuiescent(): Unit = usingPoolCleaner(new ForkJoinPool(2)) {
    p =>
      assertTrue(p.isQuiescent())
      val startTime = System.nanoTime()
      val f = new FibTask(20)
      p.invoke(f)
      assertSame(
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        p.getFactory()
      )
      while (!p.isQuiescent()) {
        if (millisElapsedSince(startTime) > LONG_DELAY_MS)
          throw new AssertionError("timed out")
        assertFalse(p.getAsyncMode())
        assertFalse(p.isShutdown())
        assertFalse(p.isTerminating())
        assertFalse(p.isTerminated())
        Thread.`yield`()
      }

      assertTrue(p.isQuiescent())
      assertFalse(p.getAsyncMode())
      assertEquals(0, p.getQueuedTaskCount())
      assertEquals(0, p.getQueuedSubmissionCount())
      assertFalse(p.hasQueuedSubmissions())
      while (p.getActiveThreadCount() != 0
          && millisElapsedSince(startTime) < LONG_DELAY_MS) {
        Thread.`yield`()
      }
      assertFalse(p.isShutdown())
      assertFalse(p.isTerminating())
      assertFalse(p.isTerminated())
      assertTrue(f.isDone())
      assertEquals(6765, f.get())
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
  }

  /** Completed submit(ForkJoinTask) returns result
   */
  @Test def testSubmitForkJoinTask(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { p =>
      val f = p.submit(new FibTask(8))
      assertEquals(21, f.get())
    }

  /** A task submitted after shutdown is rejected
   */
  @Test def testSubmitAfterShutdown(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { p =>
      p.shutdown()
      assertTrue(p.isShutdown())
      assertThrows(
        classOf[RejectedExecutionException],
        p.submit(new FibTask(8))
      )
    }

  /** Pool maintains parallelism when using ManagedBlocker
   */
  @Test def testBlockingForkJoinTask(): Unit =
    usingPoolCleaner(new ForkJoinPool(4)) { p =>
      val lock = new ReentrantLock()
      val locker = new ManagedLocker(lock)
      val f = new LockingFibTask(20, locker, lock)
      p.execute(f)
      assertEquals(6765, f.get())
    }

  /** pollSubmission returns unexecuted submitted task, if present
   */
  @Test def testPollSubmission(): Unit =
    usingPoolCleaner(new SubFJP()) { p =>
      val done = new CountDownLatch(1)
      val a = p.submit(awaiter(done))
      val b = p.submit(awaiter(done))
      val c = p.submit(awaiter(done))
      val r = p.pollSubmission()
      assertTrue(r == a || r == b || r == c)
      assertFalse(r.isDone())
      done.countDown()
    }

  /** drainTasksTo transfers unexecuted submitted tasks, if present
   */
  @Test def testDrainTasksTo(): Unit = usingPoolCleaner(new SubFJP()) { p =>
    val done = new CountDownLatch(1)
    val a = p.submit(awaiter(done))
    val b = p.submit(awaiter(done))
    val c = p.submit(awaiter(done))
    val al = new ArrayList[ForkJoinTask[_]]()
    p.drainTasks(al)
    assertTrue("was empty", al.size() > 0)
    al.forEach { r =>
      assertTrue(r == a || r == b || r == c)
      assertFalse(r.isDone())
    }
    done.countDown()
  }

  // FJ Versions of AbstractExecutorService tests

  /** execute(runnable) runs it to completion
   */
  @Test def testExecuteRunnable(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val done = new AtomicBoolean(false)
      val future = e.submit(new CheckedRunnable {
        override def realRun() = done.set(true)
      })
      assertNull(future.get())
      assertNull(future.get(randomExpiredTimeout(), randomTimeUnit()))
      assertTrue(done.get())
      assertTrue(future.isDone())
      assertFalse(future.isCancelled())
    }

  /** Completed submit(callable) returns result
   */
  @Test def testSubmitCallable(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val future = e.submit(new StringTask())
      assertEquals(TEST_STRING, future.get())
      assertTrue(future.isDone())
      assertFalse(future.isCancelled())
  }

  /** Completed submit(runnable) returns successfully
   */
  @Test def testSubmitRunnable(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val future = e.submit(new NoOpRunnable())
      assertNull(future.get())
      assertTrue(future.isDone())
      assertFalse(future.isCancelled())
  }

  /** Completed submit(runnable, result) returns result
   */
  @Test def testSubmitRunnable2(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val future = e.submit(new NoOpRunnable(), TEST_STRING)
      assertEquals(TEST_STRING, future.get())
      assertTrue(future.isDone())
      assertFalse(future.isCancelled())
    }

  // tests not making sense in Scala Native due to java.lang.security.PrivilagedAction
  // @Test def testSubmitPrivilegedAction(): Unit = ()
  // @Test def testSubmitPrivilegedExceptionAction(): Unit = ()
  // @Test def testSubmitFailedPrivilegedExceptionAction(): Unit = ()

  @Test def testExecuteNullRunnable(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      assertThrows(classOf[NullPointerException], e.submit(null: Runnable))
    }

  /** submit(null callable) throws NullPointerException
   */
  @Test def testSubmitNullCallable(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      assertThrows(classOf[NullPointerException], e.submit(null: Callable[_]))
    }

  /** submit(callable).get() throws InterruptedException if interrupted
   */
  @Test def testInterruptedSubmit(): Unit = {
    val submitted = new CountDownLatch(1)
    val quittingTime = new CountDownLatch(1)
    val awaiter: CheckedCallable[Unit] = { () =>
      assertTrue(quittingTime.await(2 * LONG_DELAY_MS, MILLISECONDS))
    }
    usingPoolCleaner(
      new ForkJoinPool(1),
      cleaner(_: ForkJoinPool, quittingTime)
    ) { p =>
      val t = new Thread(new CheckedInterruptedRunnable() {
        def realRun() = {
          val future = p.submit(awaiter)
          submitted.countDown()
          future.get()
        }
      })
      t.start()
      await(submitted)
      t.interrupt()
      awaitTermination(t)
    }
  }

  /** get of submit(callable) throws ExecutionException if callable throws
   *  exception
   */
  @Test def testSubmitEE(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { p =>
      val ex = assertThrows(
        classOf[ExecutionException],
        p.submit {
          new Callable[Any] {
            def call(): Any = throw new ArithmeticException()
          }
        }.get()
      )
      assertTrue(ex.getCause().isInstanceOf[ArithmeticException])
    }
  }

  /** invokeAny(null) throws NullPointerException
   */
  @Test def testInvokeAny1(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      assertThrows(classOf[NullPointerException], e.invokeAny(null))
    }

  /** invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Throwable]
  @Test def testInvokeAny2(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      assertThrows(
        classOf[IllegalArgumentException],
        e.invokeAny(new ArrayList[Callable[String]]())
      )
  }

  /** invokeAny(c) throws NullPointerException if c has a single null element
   */
  @throws[Throwable]
  @Test def testInvokeAny3(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(null)
      assertThrows(classOf[NullPointerException], e.invokeAny(l))
  }

  /** invokeAny(c) throws NullPointerException if c has null elements
   */
  @Test def testInvokeAny4(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val latch = new CountDownLatch(1)
      val l = new ArrayList[Callable[String]]()
      l.add(latchAwaitingStringTask(latch))
      l.add(null)
      assertThrows(
        classOf[NullPointerException],
        e.invokeAny(l)
      )
      latch.countDown()
    }
  }

  @Test def testInvokeAny5(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new NPETask())
      val ex = assertThrows(
        classOf[ExecutionException],
        e.invokeAny(l)
      )
      assertTrue(ex.getCause().isInstanceOf[NullPointerException])
    }
  }

  /** invokeAny(c) returns result of some task in c if at least one completes
   */
  @Test def testInvokeAny6(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      l.add(new StringTask())
      val result = e.invokeAny(l)
      assertEquals(TEST_STRING, result)
  }

  /** invokeAll(null) throws NullPointerException
   */
  @Test def testInvokeAll1(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      assertThrows(classOf[NullPointerException], e.invokeAll(null))
  }

  /** invokeAll(empty collection) returns empty list
   */
  @Test def testInvokeAll2(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val emptyCollection = Collections.emptyList[Callable[String]]()
      val r = e.invokeAll(emptyCollection)
      assertTrue(r.isEmpty())
  }

  /** invokeAll(c) throws NullPointerException if c has null elements
   */
  @throws[InterruptedException]
  @Test def testInvokeAll3(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      l.add(null)
      assertThrows(classOf[NullPointerException], e.invokeAll(l))
  }

  /** get of returned element of invokeAll(c) throws ExecutionException on
   *  failed task
   */
  @Test def testInvokeAll4(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new NPETask())
      val futures = e.invokeAll(l)
      assertEquals(1, futures.size())
      val ex = assertThrows(classOf[ExecutionException], futures.get(0).get())
      assertTrue(ex.getCause().isInstanceOf[NullPointerException])
    }

  /** invokeAll(c) returns results of all completed tasks in c
   */
  @Test def testInvokeAll5(): Unit = usingPoolCleaner(new ForkJoinPool(1)) {
    (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      l.add(new StringTask())
      val futures = e.invokeAll(l)
      assertEquals(2, futures.size())
      futures.forEach(f => assertEquals(TEST_STRING, f.get()))
  }

  /** timed invokeAny(null) throws NullPointerException
   */
  @Test def testTimedInvokeAny1(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      assertThrows(
        classOf[NullPointerException],
        e.invokeAny(null, randomTimeout(), randomTimeUnit())
      )
    }

  /** timed invokeAny(null time unit) throws NullPointerException
   */
  @Test def testTimedInvokeAnyNullTimeUnit(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      assertThrows(
        classOf[NullPointerException],
        e.invokeAny(l, randomTimeout(), null)
      )
    }

  /** timed invokeAny(empty collection) throws IllegalArgumentException
   */
  @Test def testTimedInvokeAny2(): Unit = {
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      assertThrows(
        classOf[IllegalArgumentException],
        e.invokeAny(new ArrayList(), randomTimeout(), randomTimeUnit())
      )
    }
  }

  /** timed invokeAny(c) throws NullPointerException if c has null elements
   */
  @Test def testTimedInvokeAny3(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val latch = new CountDownLatch(1)
      val l = new ArrayList[Callable[String]]()
      l.add(latchAwaitingStringTask(latch))
      l.add(null)
      assertThrows(
        classOf[NullPointerException],
        e.invokeAny(l, randomTimeout(), randomTimeUnit())
      )
      latch.countDown()
    }

  /** timed invokeAny(c) throws ExecutionException if no task completes
   */
  @Test def testTimedInvokeAny4(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val startTime = System.nanoTime()
      val l = new ArrayList[Callable[String]]()
      l.add(new NPETask())
      val ex = assertThrows(
        classOf[ExecutionException],
        e.invokeAny(l, LONG_DELAY_MS, MILLISECONDS)
      )
      assertTrue(ex.getCause().isInstanceOf[NullPointerException])
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }

  /** timed invokeAny(c) returns result of some task in c
   */
  @Test def testTimedInvokeAny5(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val startTime = System.nanoTime()
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      l.add(new StringTask())
      val result = e.invokeAny(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(TEST_STRING, result)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }

  /** timed invokeAll(null) throws NullPointerException
   */
  @Test def testTimedInvokeAll1(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      assertThrows(
        classOf[NullPointerException],
        e.invokeAll(null, randomTimeout(), randomTimeUnit())
      )
    }

  /** timed invokeAll(null time unit) throws NullPointerException
   */
  @Test def testTimedInvokeAllNullTimeUnit(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      assertThrows(
        classOf[NullPointerException],
        e.invokeAll(l, randomTimeout(), null)
      )
    }

  /** timed invokeAll(empty collection) returns empty list
   */
  @Test def testTimedInvokeAll2(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val r = e.invokeAll(
        Collections.emptyList(),
        randomTimeout(),
        randomTimeUnit()
      )
      assertTrue(r.isEmpty())
    }

  /** timed invokeAll(c) throws NullPointerException if c has null elements
   */
  @Test def testTimedInvokeAll3(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      l.add(null)
      assertThrows(
        classOf[NullPointerException],
        e.invokeAll(l, randomTimeout(), randomTimeUnit())
      )
    }

  /** get of returned element of invokeAll(c) throws exception on failed task
   */
  @throws[Throwable]
  @Test def testTimedInvokeAll4(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new NPETask())
      val futures = e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(1, futures.size())
      val ex = assertThrows(classOf[ExecutionException], futures.get(0).get())
      assertTrue(ex.getCause().isInstanceOf[NullPointerException])
    }

  /** timed invokeAll(c) returns results of all completed tasks in c
   */
  @Test def testTimedInvokeAll5(): Unit =
    usingPoolCleaner(new ForkJoinPool(1)) { (e: ExecutorService) =>
      val l = new ArrayList[Callable[String]]()
      l.add(new StringTask())
      l.add(new StringTask())
      val futures = e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(2, futures.size())
      futures.forEach(f => assertEquals(TEST_STRING, f.get()))
    }

}
