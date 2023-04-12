/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit._
import java.io._
import java.util._
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

import org.junit.Assert._
import org.junit.BeforeClass
import scala.scalanative.junit.utils.AssumesHelper

/** Base class for JSR166 Junit TCK tests. Defines some constants, utility
 *  methods and classes, as well as a simple framework for helping to make sure
 *  that assertions failing in generated threads cause the associated test that
 *  generated them to itself fail (which JUnit does not otherwise arrange). The
 *  rules for creating such tests are:
 */
abstract class JSR166Test {
  import JSR166Test._

  /** Returns a random element from given choices.
   */
  def chooseRandomly[T](choices: List[T]): T =
    choices.get(ThreadLocalRandom.current().nextInt(choices.size()))

  /** Returns a random element from given choices.
   */
  def chooseRandomly[T](choices: Array[T]): T = {
    choices(ThreadLocalRandom.current().nextInt(choices.length))
  }

  /** Returns the shortest timed delay. This can be scaled up for slow machines
   *  using the jsr166.delay.factor system property, or via jtreg's
   *  -timeoutFactor: flag. http://openjdk.java.net/jtreg/command-help.html
   */
  protected def getShortDelay(): Long = SHORT_DELAY_MS

  /** Returns a new Date instance representing a time at least delayMillis
   *  milliseconds in the future.
   */
  def delayedDate(delayMillis: Long): Date = {
    // Add 1 because currentTimeMillis is known to round into the past.
    new Date(System.currentTimeMillis() + delayMillis + 1)
  }

  /** The first exception encountered if any threadAssertXXX method fails.
   */
  private final val threadFailure: AtomicReference[Throwable] =
    new AtomicReference(null)

  /** Records an exception so that it can be rethrown later in the test harness
   *  thread, triggering a test case failure. Only the first failure is recorded
   *  subsequent calls to this method from within the same test have no effect.
   */
  def threadRecordFailure(t: Throwable) = {
    System.err.println(t)
    if (threadFailure.compareAndSet(null, t)) () // dumpTestThreads()
  }

  /** Just like fail(reason), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadFail(reason: String): Unit = {
    try fail(reason)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
    }
  }

  /** Just like assertTrue(b), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadAssertTrue(pred: => Boolean): Unit = {
    try assertTrue(pred)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
    }
  }

  /** Just like assertFalse(b), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadAssertFalse(pred: => Boolean): Unit = {
    try assertFalse(pred)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
    }
  }

  /** Just like assertNull(x), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadAssertNull(x: Object): Unit =
    try assertNull(x)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
    }

  /** Just like assertEquals(x, y), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadAssertEquals(x: Long, y: Long): Unit =
    try assertEquals(x, y)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
    }

  /** Just like assertEquals(x, y), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadAssertEquals(x: Object, y: Object): Unit =
    try assertEquals(x, y)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }

  /** Fails with message "should throw exception".
   */
  def shouldThrow(exceptionName: String = "exception"): Unit = fail(
    s"Should throw $exceptionName"
  )

  /** Just like assertSame(x, y), but additionally recording (using
   *  threadRecordFailure) any AssertionError thrown, so that the current
   *  testcase will fail.
   */
  def threadAssertSame(x: Object, y: Object): Unit =
    try assertSame(x, y)
    catch {
      case fail: AssertionError =>
        threadRecordFailure(fail)
        throw fail
    }

  /** Calls threadFail with message "should throw exception".
   */
  def threadShouldThrow(): Unit = threadFail("should throw exception")

  /** Calls threadFail with message "should throw" + exceptionName.
   */
  def threadShouldThrow(exceptionName: String): Unit = threadFail(
    "should throw " + exceptionName
  )

  /** Records the given exception using {@link #threadRecordFailure}, then
   *  rethrows the exception, wrapping it in an AssertionError if necessary.
   */
  def threadUnexpectedException(t: Throwable): Unit = {
    threadRecordFailure(t)
    // t.printStackTrace()
    t match {
      case t: RuntimeException => throw t
      case t: Error            => throw t
      case t => throw new AssertionError(s"unexpected exception: $t", t)
    }
  }

  /** Allows use of try-with-resources with per-test thread pools.
   */
  class PoolCleaner(val pool: ExecutorService) extends AutoCloseable {
    def close(): Unit = joinPool(pool)
  }

  /** An extension of PoolCleaner that has an action to release the pool.
   */
  class PoolCleanerWithReleaser(pool: ExecutorService, releaser: Runnable)
      extends PoolCleaner(pool) {
    override def close(): Unit = {
      try releaser.run()
      finally super.close()
    }
  }

  def usingWrappedPoolCleaner[Executor <: ExecutorService, T](pool: Executor)(
      wrapper: Executor => PoolCleaner
  )(fn: Executor => T): T = usingPoolCleaner(pool, wrapper)(fn)

  def usingPoolCleaner[Executor <: ExecutorService, T](
      pool: Executor,
      wrapper: Executor => PoolCleaner = cleaner(_: ExecutorService)
  )(fn: Executor => T): T = {
    val cleaner = wrapper(pool)
    try fn(pool)
    catch {
      case t: Throwable =>
        throw new RuntimeException("Pool cleanup failed", t)
        // fail(s"Pool cleanup failed: $t")
        null.asInstanceOf[T]
    } finally cleaner.close()
  }

  def cleaner(pool: ExecutorService): PoolCleaner = new PoolCleaner(pool)
  def cleaner(pool: ExecutorService, releaser: Runnable) =
    new PoolCleanerWithReleaser(pool, releaser)
  def cleaner(pool: ExecutorService, latch: CountDownLatch) =
    new PoolCleanerWithReleaser(pool, releaser(latch))
  def cleaner(pool: ExecutorService, flag: AtomicBoolean) =
    new PoolCleanerWithReleaser(pool, releaser(flag))

  def releaser(latch: CountDownLatch) = new Runnable() {
    def run(): Unit = while ({
      latch.countDown()
      latch.getCount() > 0
    }) ()
  }

  def releaser(flag: AtomicBoolean) = new Runnable() {
    def run(): Unit = flag.set(true)
  }

  /** Waits out termination of a thread pool or fails doing so.
   */
  def joinPool(pool: ExecutorService): Unit =
    try {
      pool.shutdown()
      if (!pool.awaitTermination(20 * LONG_DELAY_MS, MILLISECONDS)) {
        try {
          threadFail(
            s"ExecutorService $pool did not terminate in a timely manner"
          )
        } finally {
          // last resort, for the benefit of subsequent tests
          pool.shutdownNow()
          val res = pool.awaitTermination(MEDIUM_DELAY_MS, MILLISECONDS)
        }
      }
    } catch {
      case ok: SecurityException =>
        // Allowed in case test doesn't have privs
        ()
      case fail: InterruptedException =>
        threadFail("Unexpected InterruptedException")
    }

  /** Like Runnable, but with the freedom to throw anything. junit folks had the
   *  same idea:
   *  http://junit.org/junit5/docs/snapshot/api/org/junit/gen5/api/Executable.html
   */
  trait Action { def run(): Unit }

  /** Runs all the given actions in parallel, failing if any fail. Useful for
   *  running multiple variants of tests that are necessarily individually slow
   *  because they must block.
   */
  def testInParallel(actions: Action*): Unit =
    usingPoolCleaner(Executors.newCachedThreadPool()) { pool =>
      actions
        .map { action =>
          pool.submit(new CheckedRunnable() {
            def realRun(): Unit = action.run()
          })
        }
        .foreach { future =>
          try assertNull(future.get(LONG_DELAY_MS, MILLISECONDS))
          catch {
            case ex: ExecutionException =>
              threadUnexpectedException(ex.getCause())
            case ex: Exception => threadUnexpectedException(ex)
          }
        }
    }

  /** Checks that thread eventually enters the expected blocked thread state.
   */
  def assertThreadBlocks(thread: Thread, expected: Thread.State): Unit = {
    // always sleep at least 1 ms, with high probability avoiding
    // transitory states
    var retries = LONG_DELAY_MS * 3 / 4
    while (retries > 0) {
      try delay(1)
      catch {
        case fail: InterruptedException =>
          throw new AssertionError("Unexpected InterruptedException", fail)
      }
      val s = thread.getState()
      if (s == expected) return ()
      else if (s == Thread.State.TERMINATED)
        fail("Unexpected thread termination")
      retries -= 1
    }
    fail("timed out waiting for thread to enter thread state " + expected)
  }

  /** Checks that future.get times out, with the default timeout of {@code
   *  timeoutMillis()}.
   */
  def assertFutureTimesOut(future: Future[_]): Unit =
    assertFutureTimesOut(future, timeoutMillis())

  /** Checks that future.get times out, with the given millisecond timeout.
   */
  def assertFutureTimesOut(future: Future[_], timeoutMillis: Long): Unit = {
    val startTime = System.nanoTime()
    try {
      future.get(timeoutMillis, MILLISECONDS)
      shouldThrow()
    } catch {
      case _: TimeoutException => ()
      case fail: Exception     => threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
    assertFalse(future.isDone())
  }

  /** Spin-waits up to the specified number of milliseconds for the given thread
   *  to enter a wait state: BLOCKED, WAITING, or TIMED_WAITING.
   *  @param waitingForGodot
   *    if non-null, an additional condition to satisfy
   */
  def waitForThreadToEnterWaitState(
      thread: Thread,
      timeoutMillis: Long,
      waitingForGodot: Callable[Boolean]
  ): Unit = {
    lazy val startTime = System.nanoTime()
    import Thread.State._
    while (true) {
      thread.getState() match {
        case BLOCKED | WAITING | TIMED_WAITING =>
          try {
            if (waitingForGodot == null || waitingForGodot.call()) return ()
          } catch { case fail: Throwable => threadUnexpectedException(fail) }
        case TERMINATED =>
          fail("Unexpected thread termination")
        case _ => ()
      }
      if (millisElapsedSince(startTime) > timeoutMillis) {
        assertTrue(thread.isAlive())
        if (waitingForGodot == null
            || thread.getState() == Thread.State.RUNNABLE)
          fail("timed out waiting for thread to enter wait state")
        else
          fail(
            s"timed out waiting for condition, thread state=${thread.getState()}"
          )
      }
      Thread.`yield`()
    }
  }

  /** Spin-waits up to the specified number of milliseconds for the given thread
   *  to enter a wait state: BLOCKED, WAITING, or TIMED_WAITING.
   */
  def waitForThreadToEnterWaitState(thread: Thread, timeoutMillis: Long): Unit =
    waitForThreadToEnterWaitState(thread, timeoutMillis, null)

  /** Spin-waits up to LONG_DELAY_MS milliseconds for the given thread to enter
   *  a wait state: BLOCKED, WAITING, or TIMED_WAITING.
   */
  def waitForThreadToEnterWaitState(thread: Thread): Unit =
    waitForThreadToEnterWaitState(thread, LONG_DELAY_MS, null)

  /** Spin-waits up to LONG_DELAY_MS milliseconds for the given thread to enter
   *  a wait state: BLOCKED, WAITING, or TIMED_WAITING, and additionally satisfy
   *  the given condition.
   */
  def waitForThreadToEnterWaitState(
      thread: Thread,
      waitingForGodot: Callable[Boolean]
  ): Unit =
    waitForThreadToEnterWaitState(thread, LONG_DELAY_MS, waitingForGodot)

  /** Spin-waits up to LONG_DELAY_MS milliseconds for the current thread to be
   *  interrupted. Clears the interrupt status before returning.
   */
  def awaitInterrupted(): Unit = {
    lazy val startTime = System.nanoTime()
    while (!Thread.interrupted()) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS)
        fail("timed out waiting for thread interrupt")
      Thread.`yield`()
    }
  }

  /** Checks that timed f.get() returns the expected value, and does not wait
   *  for the timeout to elapse before returning.
   */
  def checkTimedGet[T](
      f: Future[T],
      expectedValue: T,
      timeoutMillis: Long
  ): Unit = {
    val startTime = System.nanoTime()
    val actual =
      try f.get(timeoutMillis, MILLISECONDS)
      catch {
        case fail: Throwable =>
          threadUnexpectedException(fail)
          null
      }
    assertEquals(expectedValue, actual)
    if (millisElapsedSince(startTime) > timeoutMillis / 2)
      throw new AssertionError("timed get did not return promptly")
  }
  def checkTimedGet[T](f: Future[T], expectedValue: T): Unit =
    checkTimedGet(f, expectedValue, LONGER_DELAY_MS)

  /** Returns a new started daemon Thread running the given runnable.
   */
  def newStartedThread(runnable: Runnable): Thread = {
    val t = new Thread(runnable)
    t.setDaemon(true)
    t.start()
    t
  }

  /** Returns a new started daemon Thread running the given action, wrapped in a
   *  CheckedRunnable.
   */
  def newStartedThread(action: Action): Thread = newStartedThread(
    checkedRunnable(action)
  )

  /** Waits for the specified time (in milliseconds) for the thread to terminate
   *  (using {@link Thread#join(long)}), else interrupts the thread (in the hope
   *  that it may terminate later) and fails.
   */
  def awaitTermination(thread: Thread, timeoutMillis: Long = LONG_DELAY_MS) = {
    try thread.join(timeoutMillis)
    catch { case fail: InterruptedException => threadUnexpectedException(fail) }
    if (thread.getState() != Thread.State.TERMINATED) {
      try
        threadFail(
          s"timed out waiting for thread to terminate, thread=$thread, state=${thread.getState()}"
        )
      // Interrupt thread __after__ having reported its stack trace
      finally thread.interrupt()
    }
  }

  // Some convenient Runnable classes

  abstract class CheckedRunnable extends Runnable {
    @throws[Throwable]
    protected def realRun(): Unit

    final def run(): Unit = {
      try realRun()
      catch {
        case fail: Throwable => threadUnexpectedException(fail)
      }
    }
  }

  def checkedRunnable(action: Action): Runnable = new CheckedRunnable() {
    def realRun(): Unit = action.run()
  }

  abstract class ThreadShouldThrow[T](val exceptionClass: Class[T])
      extends Thread {
    protected def realRun(): Unit
    final override def run(): Unit = {
      try {
        realRun()
        threadShouldThrow(exceptionClass.getSimpleName())
      } catch {
        case t: Throwable =>
          if (!exceptionClass.isInstance(t)) threadUnexpectedException(t)
      }
    }
  }

  abstract class CheckedInterruptedRunnable extends Runnable {
    protected def realRun(): Unit

    final def run(): Unit = {
      try
        assertThrows(classOf[InterruptedException], () => realRun())
      catch {
        case success: InterruptedException =>
          threadAssertFalse(Thread.interrupted())
        case fail: Throwable => threadUnexpectedException(fail)
      }

    }
  }

  abstract class CheckedCallable[T] extends Callable[T] {
    protected def realCall(): T
    final def call(): T = {
      try return realCall()
      catch {
        case fail: Throwable =>
          threadUnexpectedException(fail)
          null.asInstanceOf[T]
      }
      throw new AssertionError("unreached")
    }
  }

  class NoOpRunnable extends Runnable {
    def run() = ()
  }

  class NoOpCallable extends Callable[Any] {
    def call(): Any = java.lang.Boolean.TRUE
  }

  final val TEST_STRING = "a test string"

  class StringTask(value: String = TEST_STRING) extends Callable[String] {
    def call() = value
  }

  def latchAwaitingStringTask(latch: CountDownLatch): Callable[String] =
    new CheckedCallable[String] {
      override protected def realCall(): String = {
        try latch.await()
        catch {
          case quittingTime: InterruptedException => ()
        }
        TEST_STRING
      }
    }

  def countDowner(latch: CountDownLatch): Runnable = new CheckedRunnable() {
    protected def realRun(): Unit = latch.countDown()
  }

  object LatchAwaiter {
    final val NEW = 0
    final val RUNNING = 1
    final val DONE = 2
  }
  class LatchAwaiter(latch: CountDownLatch) extends CheckedRunnable {
    import LatchAwaiter._
    var state = NEW
    @throws[InterruptedException]
    def realRun(): Unit = {
      state = 1
      await(latch)
      state = 2
    }
  }

  def awaiter(latch: CountDownLatch) = new LatchAwaiter(latch)

  def await(latch: CountDownLatch, timeoutMillis: Long = LONG_DELAY_MS) = {
    val timedOut =
      try !latch.await(timeoutMillis, MILLISECONDS)
      catch {
        case fail: Throwable => threadUnexpectedException(fail); false
      }
    if (timedOut) {
      fail(
        s"timed out waiting for CountDownLatch for ${timeoutMillis / 1000} sec"
      )
    }
  }

  def await(semaphore: Semaphore): Unit = {
    val timedOut =
      try !semaphore.tryAcquire(LONG_DELAY_MS, MILLISECONDS)
      catch {
        case fail: Throwable =>
          threadUnexpectedException(fail)
          false
      }
    if (timedOut)
      fail(
        "timed out waiting for Semaphore for "
          + (LONG_DELAY_MS / 1000) + " sec"
      )
  }

  def await(barrier: CyclicBarrier): Unit =
    try barrier.await(LONG_DELAY_MS, MILLISECONDS)
    catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)

    }

  /** Spin-waits up to LONG_DELAY_MS until flag becomes true.
   */
  def await(flag: AtomicBoolean): Unit = await(flag, LONG_DELAY_MS)

  /** Spin-waits up to the specified timeout until flag becomes true.
   */
  def await(flag: AtomicBoolean, timeoutMillis: Long) = {
    val startTime = System.nanoTime()
    while (!flag.get()) {
      if (millisElapsedSince(startTime) > timeoutMillis)
        throw new AssertionError("timed out")
      Thread.`yield`()
    }
  }

  class NPETask extends Callable[String] {
    override def call(): String = throw new NullPointerException()
  }

  def possiblyInterruptedRunnable(timeoutMillis: Long): Runnable =
    new CheckedRunnable() {
      override protected def realRun() =
        try delay(timeoutMillis)
        catch { case ok: InterruptedException => () }
    }

  /** For use as ThreadFactory in constructors
   */
  class SimpleThreadFactory extends ThreadFactory {
    def newThread(r: Runnable): Thread = new Thread(r)
  }

  trait TrackedRunnable extends Runnable {
    def isDone: Boolean
  }

  class TrackedNoOpRunnable extends Runnable {
    @volatile var done = false
    def run(): Unit = {
      done = true
    }
  }

  /** Analog of CheckedRunnable for RecursiveAction
   */
  abstract class CheckedRecursiveAction extends RecursiveAction {
    protected def realCompute(): Unit

    override protected final def compute(): Unit =
      try realCompute()
      catch { case fail: Throwable => threadUnexpectedException(fail) }
  }

  /** Analog of CheckedCallable for RecursiveTask
   */
  abstract class CheckedRecursiveTask[T] extends RecursiveTask[T] {
    protected def realCompute(): T
    override final protected def compute(): T = {
      try {
        return realCompute()
      } catch {
        case fail: Throwable =>
          threadUnexpectedException(fail)
      }
      throw new AssertionError("unreached")
    }
  }

  /** For use as RejectedExecutionHandler in constructors
   */
  class NoOpREHandler extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, executor: ThreadPoolExecutor): Unit = ()
  }

  /** A CyclicBarrier that uses timed await and fails with AssertionErrors
   *  instead of throwing checked exceptions.
   */
  class CheckedBarrier(parties: Int) extends CyclicBarrier(parties) {
    override def await(): Int = {
      try super.await(LONGER_DELAY_MS, MILLISECONDS)
      catch {
        case _: TimeoutException => throw new AssertionError("timed out")
        case fail: Exception =>
          throw new AssertionError("Unexpected exception: " + fail, fail)
      }
    }
  }

  def checkEmpty(q: BlockingQueue[_]): Unit = {
    try {
      assertTrue(q.isEmpty())
      assertEquals(0, q.size())
      assertNull(q.peek())
      assertNull(q.poll())
      assertNull(q.poll(randomExpiredTimeout(), randomTimeUnit()))
      assertEquals(q.toString(), "[]")
      assertTrue(Arrays.equals(q.toArray(), Array.empty[AnyRef]))
      assertFalse(q.iterator().hasNext())
      try {
        q.element()
        shouldThrow()
      } catch { case _: NoSuchElementException => () }
      try {
        q.iterator().next()
        shouldThrow()
      } catch { case _: NoSuchElementException => () }
      try {
        q.remove()
        shouldThrow()
      } catch { case _: NoSuchElementException => () }
    } catch {
      case fail: InterruptedException => threadUnexpectedException(fail)
    }
  }

  def assertImmutable(o: Object): Unit = {
    o match {
      case c: Collection[Any] @unchecked =>
        assertThrows(
          classOf[UnsupportedOperationException],
          () => c.add(null: Any)
        )
      case _ => ()
    }
  }

  def assertEachThrows(
      expectedExceptionClass: Class[_ <: Throwable],
      throwingActions: Action*
  ): Unit = {
    for (throwingAction <- throwingActions) {
      try {
        throwingAction.run()
        shouldThrow(expectedExceptionClass.getName())
      } catch {
        case t: Throwable =>
          if (!expectedExceptionClass.isInstance(t))
            throw new AssertionError(
              "Expected " + expectedExceptionClass.getName() +
                ", got " + t.getClass().getName(),
              t
            )
      }
    }
  }

  def assertIteratorExhausted(it: Iterator[_]): Unit = {
    try {
      it.next()
      shouldThrow()
    } catch { case _: NoSuchElementException => () }
    assertFalse(it.hasNext())
  }

  def callableThrowing[T](ex: Exception): Callable[T] = new Callable[T] {
    def call(): T = throw ex
  }

  def runnableThrowing(ex: Exception): Runnable = new Runnable {
    def run(): Unit = throw ex
  }

  /** A reusable thread pool to be shared by tests. */
  final lazy val cachedThreadPool: ExecutorService =
    new ThreadPoolExecutor(
      0,
      Integer.MAX_VALUE,
      1000L,
      MILLISECONDS,
      new SynchronousQueue[Runnable]()
    )

  def shuffle[T](array: Array[T]) = {
    Collections.shuffle(Arrays.asList(array), ThreadLocalRandom.current())
  }

  /** Returns the same String as would be returned by {@link Object#toString},
   *  whether or not the given object's class overrides toString().
   *
   *  @see
   *    System#identityHashCode
   */
  def identityString(x: AnyRef): String = {
    x.getClass().getName() + "@" +
      Integer.toHexString(System.identityHashCode(x))
  }

  // --- Shared assertions for Executor tests ---

  def assertNullTaskSubmissionThrowsNullPointerException(e: Executor): Unit = {
    val nullRunnable: Runnable = null
    val nullCallable: Callable[Any] = null
    try {
      e.execute(nullRunnable)
      shouldThrow()
    } catch { case success: NullPointerException => () }

    e match {
      case es: ExecutorService =>
        try {
          es.submit(nullRunnable)
          shouldThrow()
        } catch { case _: NullPointerException => () }

        try {
          es.submit(nullRunnable, java.lang.Boolean.TRUE)
          shouldThrow()
        } catch { case sucess: NullPointerException => () }
        try {
          es.submit(nullCallable)
          shouldThrow()
        } catch { case sucess: NullPointerException => () }

      case _ => ()
    }

    e match {
      case ses: ScheduledExecutorService =>
        try {
          ses.schedule(nullRunnable, randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch { case sucess: NullPointerException => () }
        try {
          ses.schedule(nullCallable, randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch { case sucess: NullPointerException => () }
        try {
          ses.scheduleAtFixedRate(
            nullRunnable,
            randomTimeout(),
            LONG_DELAY_MS,
            MILLISECONDS
          )
          shouldThrow()
        } catch { case sucess: NullPointerException => () }
        try {
          ses.scheduleWithFixedDelay(
            nullRunnable,
            randomTimeout(),
            LONG_DELAY_MS,
            MILLISECONDS
          )
          shouldThrow()
        } catch { case sucess: NullPointerException => () }
      case _ => ()
    }

  }

  def setRejectedExecutionHandler(
      p: ThreadPoolExecutor,
      handler: RejectedExecutionHandler
  ): Unit = {
    p.setRejectedExecutionHandler(handler)
    assertSame(handler, p.getRejectedExecutionHandler())
  }

  def assertTaskSubmissionsAreRejected(p: ThreadPoolExecutor): Unit = {
    val savedHandler = p.getRejectedExecutionHandler()
    val savedTaskCount = p.getTaskCount()
    val savedCompletedTaskCount = p.getCompletedTaskCount()
    val savedQueueSize = p.getQueue().size()
    val stock = true // (p.getClass().getClassLoader() == null)

    val r: Runnable = () => {}
    val c: Callable[Boolean] = () => java.lang.Boolean.TRUE

    class Recorder extends RejectedExecutionHandler {
      @volatile var r: Runnable = _
      @volatile var p: ThreadPoolExecutor = _
      def reset(): Unit = { r = null; p = null }
      def rejectedExecution(r: Runnable, p: ThreadPoolExecutor): Unit = {
        assertNull(this.r)
        assertNull(this.p)
        this.r = r
        this.p = p
      }
    }

    // check custom handler is invoked exactly once per task
    val recorder = new Recorder()
    setRejectedExecutionHandler(p, recorder)
    (2 to 0 by -1).foreach { i =>
      recorder.reset()
      p.execute(r)
      if (stock && p.getClass() == classOf[ThreadPoolExecutor])
        assertSame(r, recorder.r)
      assertSame(p, recorder.p)

      recorder.reset()
      assertFalse(p.submit(r).isDone())
      if (stock) assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
      assertSame(p, recorder.p)

      recorder.reset()
      assertFalse(p.submit(r, java.lang.Boolean.TRUE).isDone())
      if (stock) assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
      assertSame(p, recorder.p)

      recorder.reset()
      assertFalse(p.submit(c).isDone())
      if (stock) assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
      assertSame(p, recorder.p)

      p match {
        case s: ScheduledExecutorService =>
          var future: ScheduledFuture[_] = null

          recorder.reset()
          future = s.schedule(r, randomTimeout(), randomTimeUnit())
          assertFalse(future.isDone())
          if (stock)
            assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
          assertSame(p, recorder.p)

          recorder.reset()
          future = s.schedule(c, randomTimeout(), randomTimeUnit())
          assertFalse(future.isDone())
          if (stock)
            assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
          assertSame(p, recorder.p)

          recorder.reset()
          future = s.scheduleAtFixedRate(
            r,
            randomTimeout(),
            LONG_DELAY_MS,
            MILLISECONDS
          )
          assertFalse(future.isDone())
          if (stock)
            assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
          assertSame(p, recorder.p)

          recorder.reset()
          future = s.scheduleWithFixedDelay(
            r,
            randomTimeout(),
            LONG_DELAY_MS,
            MILLISECONDS
          )
          assertFalse(future.isDone())
          if (stock)
            assertTrue(!(recorder.r.asInstanceOf[Future[_]]).isDone())
          assertSame(p, recorder.p)

        case _ => ()
      }
    }

    // Checking our custom handler above should be sufficient, but
    // we add some integration tests of standard handlers.
    val thread = new AtomicReference[Thread]()
    val setThread: Runnable = () => thread.set(Thread.currentThread())

    setRejectedExecutionHandler(p, new ThreadPoolExecutor.AbortPolicy())
    try {
      p.execute(setThread)
      shouldThrow()
    } catch { case _: RejectedExecutionException => () }
    assertNull(thread.get())

    setRejectedExecutionHandler(p, new ThreadPoolExecutor.DiscardPolicy())
    p.execute(setThread)
    assertNull(thread.get())

    setRejectedExecutionHandler(p, new ThreadPoolExecutor.CallerRunsPolicy())
    p.execute(setThread)
    if (p.isShutdown())
      assertNull(thread.get())
    else
      assertSame(Thread.currentThread(), thread.get())

    setRejectedExecutionHandler(p, savedHandler)

    // check that pool was not perturbed by handlers
    assertEquals(savedTaskCount, p.getTaskCount())
    assertEquals(savedCompletedTaskCount, p.getCompletedTaskCount())
    assertEquals(savedQueueSize, p.getQueue().size())
  }

  def assertCollectionsEquals(x: Collection[_], y: Collection[_]): Unit = {
    assertEquals(x, y)
    assertEquals(y, x)
    assertEquals(x.isEmpty(), y.isEmpty())
    assertEquals(x.size(), y.size())
    if (x.isInstanceOf[List[_]]) {
      assertEquals(x.toString(), y.toString())
    }
    if (x.isInstanceOf[List[_]] || x.isInstanceOf[Set[_]]) {
      assertEquals(x.hashCode(), y.hashCode())
    }
    if (x.isInstanceOf[List[_]] || x.isInstanceOf[Deque[_]]) {
      assertTrue(Arrays.equals(x.toArray(), y.toArray()))
      assertTrue(
        Arrays.equals(
          x.toArray(new Array[Object](0)),
          y.toArray(new Array[Object](0))
        )
      )
    }
  }

  /** A weaker form of assertCollectionsEquals which does not insist that the
   *  two collections satisfy Object#equals(Object), since they may use identity
   *  semantics as Deques do.
   */
  def assertCollectionsEquivalent(x: Collection[_], y: Collection[_]): Unit = {
    if (x.isInstanceOf[List[_]] || x.isInstanceOf[Set[_]])
      assertCollectionsEquals(x, y)
    else {
      assertEquals(x.isEmpty(), y.isEmpty())
      assertEquals(x.size(), y.size())
      assertEquals(new HashSet(x), new HashSet(y))
      if (x.isInstanceOf[Deque[_]]) {
        assertTrue(Arrays.equals(x.toArray(), y.toArray()))
        assertTrue(
          Arrays.equals(
            x.toArray(new Array[Object](0)),
            y.toArray(new Array[Object](0))
          )
        )
      }
    }
  }
}

object JSR166Test {
  @BeforeClass def checkRuntime(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
  }

  final val expensiveTests = true

  /** If true, also run tests that are not part of the official tck because they
   *  test unspecified implementation details.
   */
  final val testImplementationDetails = true

  /** If true, report on stdout all "slow" tests, that is, ones that take more
   *  than profileThreshold milliseconds to execute.
   */
  final val profileTests = true

  /** The number of milliseconds that tests are permitted for execution without
   *  being reported, when profileTests is set.
   */
  final val profileThreshold = true

  /** The scaling factor to apply to standard delays used in tests. May be
   *  initialized from any of:
   *    - the "jsr166.delay.factor" system property
   *    - the "test.timeout.factor" system property (as used by jtreg) See:
   *      http://openjdk.java.net/jtreg/tag-spec.html
   *    - hard-coded fuzz factor when using a known slowpoke VM
   */
  private val delayFactor = 1.0f

  // Delays for timing-dependent tests, in milliseconds.
  final val SHORT_DELAY_MS = (50 * delayFactor).toLong
  final val SMALL_DELAY_MS = SHORT_DELAY_MS * 5
  final val MEDIUM_DELAY_MS = SHORT_DELAY_MS * 10
  final val LONG_DELAY_MS = SHORT_DELAY_MS * 200

  /** A delay significantly longer than LONG_DELAY_MS. Use this in a thread that
   *  is waited for via awaitTermination(Thread).
   */
  final val LONGER_DELAY_MS = LONG_DELAY_MS * 2

  // SN note: We define this variables as functions for source-compatibility with JSR 166 tests for easier porting of tests
  final val (randomTimeout, randomExpiredTimeout, randomTimeUnit) = {
    val rnd = ThreadLocalRandom.current()
    val timeouts =
      Array(java.lang.Long.MIN_VALUE, -1, 0, 1, java.lang.Long.MAX_VALUE)
    val timeUnits = TimeUnit.values()

    val timeout = timeouts(rnd.nextInt(timeouts.length))
    val expired = timeouts(rnd.nextInt(3))
    val timeUnit = timeUnits(rnd.nextInt(timeUnits.length))
    (() => timeout, () => expired, () => timeUnit)
  }

  /** Returns a random boolean a "coin flip".
   */
  def randomBoolean(): Boolean = ThreadLocalRandom.current().nextBoolean()

  private final lazy val TIMEOUT_DELAY_MS =
    (12.0 * Math.cbrt(delayFactor)).toLong

  /** Returns a timeout in milliseconds to be used in tests that verify that
   *  operations block or time out. We want this to be longer than the OS
   *  scheduling quantum, but not too long, so don't scale linearly with
   *  delayFactor we use "crazy" cube root instead.
   */
  def timeoutMillis(): Long = TIMEOUT_DELAY_MS

  /** Delays, via Thread.sleep, for the given millisecond delay, but if the
   *  sleep is shorter than specified, may re-sleep or yield until time elapses.
   *  Ensures that the given time, as measured by System.nanoTime(), has
   *  elapsed.
   */
  def delay(ms: Long) = {
    var millis = ms
    var nanos = millis * (1000 * 1000)
    var wakeupTime = System.nanoTime() + nanos
    while ({
      if (millis > 0L) Thread.sleep(millis)
      else Thread.`yield`() // too short to sleep
      nanos = wakeupTime - System.nanoTime()
      millis = nanos / (1000 * 1000)
      nanos >= 0L
    }) ()
  }

  def sleep(millis: Long): Unit = {
    try delay(millis)
    catch {
      case fail: InterruptedException =>
        throw new AssertionError("Unexpected InterruptedException", fail)
    }
  }

  /** The maximum number of consecutive spurious wakeups we should tolerate
   *  (from APIs like LockSupport.park) before failing a test.
   */
  final val MAX_SPURIOUS_WAKEUPS = 10

  /** The number of elements to place in collections, arrays, etc.
   */
  final val SIZE = 20

  // Some convenient Integer constants
  final val zero = Integer.valueOf(0)
  final val one = Integer.valueOf(1)
  final val two = Integer.valueOf(2)
  final val three = Integer.valueOf(3)
  final val four = Integer.valueOf(4)
  final val five = Integer.valueOf(5)
  final val six = Integer.valueOf(6)
  final val seven = Integer.valueOf(7)
  final val eight = Integer.valueOf(8)
  final val nine = Integer.valueOf(9)
  final val m1 = Integer.valueOf(-1)
  final val m2 = Integer.valueOf(-2)
  final val m3 = Integer.valueOf(-3)
  final val m4 = Integer.valueOf(-4)
  final val m5 = Integer.valueOf(-5)
  final val m6 = Integer.valueOf(-6)
  final val m10 = Integer.valueOf(-10)

  /** Returns the number of milliseconds since time given by startNanoTime,
   *  which must have been previously returned from a call to {@link
   *  System#nanoTime()}.
   */
  def millisElapsedSince(startNanoTime: Long): Long = {
    NANOSECONDS.toMillis(System.nanoTime() - startNanoTime)
  }

  /** Returns maximum number of tasks that can be submitted to given pool (with
   *  bounded queue) before saturation (when submission throws
   *  RejectedExecutionException).
   */
  def saturatedSize(pool: ThreadPoolExecutor): Int = {
    val q = pool.getQueue()
    pool.getMaximumPoolSize() + q.size() + q.remainingCapacity()
  }
}
