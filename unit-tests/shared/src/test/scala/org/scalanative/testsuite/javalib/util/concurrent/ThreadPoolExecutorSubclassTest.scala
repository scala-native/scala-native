/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit._
import java.util
import java.util._
import java.util.Collections
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks._

import org.junit._
import org.junit.Assert._

import JSR166Test._

object ThreadPoolExecutorSubclassTest {
  class CustomTask[V](callable: Callable[V]) extends RunnableFuture[V] {
    if (callable == null) throw new NullPointerException
    def this(r: Runnable, res: V) = this({
      if (r == null) throw new NullPointerException
      () => {
        r.run()
        res
      }
    }: Callable[V])
    var result: V = _
    var exception: Exception = _
    var thread: Thread = _

    final val lock = new ReentrantLock
    final val cond = lock.newCondition
    var done = false
    var cancelled = false
    override def isDone: Boolean = {
      lock.lock()
      try done
      finally lock.unlock()
    }
    override def isCancelled: Boolean = {
      lock.lock()
      try cancelled
      finally lock.unlock()
    }
    override def cancel(mayInterrupt: Boolean): Boolean = {
      lock.lock()
      try {
        if (!done) {
          cancelled = true
          done = true
          if (mayInterrupt && thread != null) thread.interrupt()
          return true
        }
        false
      } finally lock.unlock()
    }
    override def run(): Unit = {
      lock.lock()
      try {
        if (done) return
        thread = Thread.currentThread
      } finally lock.unlock()

      var v: V = null.asInstanceOf[V]
      var e: Exception = null
      try v = callable.call()
      catch {
        case ex: Exception =>
          e = ex
      }
      lock.lock()
      try
        if (!done) {
          result = v
          exception = e
          done = true
          thread = null
          cond.signalAll()
        }
      finally lock.unlock()
    }
    @throws[InterruptedException]
    @throws[ExecutionException]
    override def get: V = {
      lock.lock()
      try {
        while (!done) cond.await()
        if (cancelled) throw new CancellationException
        if (exception != null) throw new ExecutionException(exception)
        result
      } finally lock.unlock()
    }
    @throws[InterruptedException]
    @throws[ExecutionException]
    @throws[TimeoutException]
    override def get(timeout: Long, unit: TimeUnit): V = {
      var nanos = unit.toNanos(timeout)
      lock.lock()
      try {
        while (!done) {
          if (nanos <= 0L) throw new TimeoutException
          nanos = cond.awaitNanos(nanos)
        }
        if (cancelled) throw new CancellationException
        if (exception != null) throw new ExecutionException(exception)
        result
      } finally lock.unlock()
    }
  }
  class CustomTPE(
      corePoolSize: Int,
      maximumPoolSize: Int,
      keepAliveTime: Long,
      unit: TimeUnit,
      workQueue: BlockingQueue[Runnable],
      threadFactory: ThreadFactory,
      handler: RejectedExecutionHandler
  ) extends ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        handler
      ) {
    override protected def newTaskFor[V](c: Callable[V]) =
      new CustomTask[V](c)
    override protected def newTaskFor[V](r: Runnable, v: V) =
      new CustomTask[V](r, v)
    def this(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue[Runnable],
        threadFactory: ThreadFactory
    ) =
      this(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        new ThreadPoolExecutor.AbortPolicy()
      )
    def this(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue[Runnable]
    ) = this(
      corePoolSize,
      maximumPoolSize,
      keepAliveTime,
      unit,
      workQueue,
      Executors.defaultThreadFactory()
    )
    def this(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue[Runnable],
        handler: RejectedExecutionHandler
    ) =
      this(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        Executors.defaultThreadFactory(),
        handler
      )
    def this() =
      this(1, 1, LONG_DELAY_MS, MILLISECONDS, new SynchronousQueue[Runnable])
    final val beforeCalledLatch = new CountDownLatch(1)
    final val afterCalledLatch = new CountDownLatch(1)
    final val terminatedCalledLatch = new CountDownLatch(1)
    override protected def beforeExecute(t: Thread, r: Runnable): Unit = {
      beforeCalledLatch.countDown()
    }
    override protected def afterExecute(r: Runnable, t: Throwable): Unit = {
      afterCalledLatch.countDown()
    }
    override protected def terminated(): Unit = {
      terminatedCalledLatch.countDown()
    }
    def beforeCalled: Boolean = beforeCalledLatch.getCount == 0
    def afterCalled: Boolean = afterCalledLatch.getCount == 0
    def terminatedCalled: Boolean = terminatedCalledLatch.getCount == 0
  }
  class FailingThreadFactory extends ThreadFactory {
    var calls = 0
    override def newThread(r: Runnable): Thread = {
      if ({ calls += 1; calls } > 1) return null
      new Thread(r)
    }
  }
}
class ThreadPoolExecutorSubclassTest extends JSR166Test {
  import JSR166Test._
  import ThreadPoolExecutorSubclassTest._

  /** execute successfully executes a runnable
   */
  @throws[InterruptedException]
  @Test def testExecute(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      2 * LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val done = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = { done.countDown() }
      }
      p.execute(task)
      await(done)

    }
  }

  /** getActiveCount increases but doesn't overestimate, when a thread becomes
   *  active
   */
  @throws[InterruptedException]
  @Test def testGetActiveCount(): Unit = {
    val done = new CountDownLatch(1)
    val p = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val threadStarted = new CountDownLatch(1)
      assertEquals(0, p.getActiveCount)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadStarted.countDown()
          assertEquals(1, p.getActiveCount)
          await(done)
        }
      })
      await(threadStarted)
      assertEquals(1, p.getActiveCount)
    }
  }

  /** prestartCoreThread starts a thread if under corePoolSize, else doesn't
   */
  @Test def testPrestartCoreThread(): Unit = {
    val p = new CustomTPE(
      2,
      6,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      assertEquals(0, p.getPoolSize)
      assertTrue(p.prestartCoreThread)
      assertEquals(1, p.getPoolSize)
      assertTrue(p.prestartCoreThread)
      assertEquals(2, p.getPoolSize)
      assertFalse(p.prestartCoreThread)
      assertEquals(2, p.getPoolSize)
      p.setCorePoolSize(4)
      assertTrue(p.prestartCoreThread)
      assertEquals(3, p.getPoolSize)
      assertTrue(p.prestartCoreThread)
      assertEquals(4, p.getPoolSize)
      assertFalse(p.prestartCoreThread)
      assertEquals(4, p.getPoolSize)

    }
  }

  /** prestartAllCoreThreads starts all corePoolSize threads
   */
  @Test def testPrestartAllCoreThreads(): Unit = {
    val p = new CustomTPE(
      2,
      6,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      assertEquals(0, p.getPoolSize)
      p.prestartAllCoreThreads
      assertEquals(2, p.getPoolSize)
      p.prestartAllCoreThreads
      assertEquals(2, p.getPoolSize)
      p.setCorePoolSize(4)
      p.prestartAllCoreThreads
      assertEquals(4, p.getPoolSize)
      p.prestartAllCoreThreads
      assertEquals(4, p.getPoolSize)

    }
  }

  /** getCompletedTaskCount increases, but doesn't overestimate, when tasks
   *  complete
   */
  @throws[InterruptedException]
  @Test def testGetCompletedTaskCount(): Unit = {
    val p = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val threadStarted = new CountDownLatch(1)
      val threadProceed = new CountDownLatch(1)
      val threadDone = new CountDownLatch(1)
      assertEquals(0, p.getCompletedTaskCount)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadStarted.countDown()
          assertEquals(0, p.getCompletedTaskCount)
          await(threadProceed)
          threadDone.countDown()
        }
      })
      await(threadStarted)
      assertEquals(0, p.getCompletedTaskCount)
      threadProceed.countDown()
      await(threadDone)
      val startTime = System.nanoTime
      while (p.getCompletedTaskCount != 1) {
        if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
        Thread.`yield`()
      }

    }
  }

  /** getCorePoolSize returns size given in constructor if not otherwise set
   */
  @Test def testGetCorePoolSize(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p => assertEquals(1, p.getCorePoolSize) }
  }

  /** getKeepAliveTime returns value given in constructor if not otherwise set
   */
  @Test def testGetKeepAliveTime(): Unit = {
    val p = new CustomTPE(
      2,
      2,
      1000,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p => assertEquals(1, p.getKeepAliveTime(SECONDS)) }
  }

  /** getThreadFactory returns factory in constructor if not set
   */
  @Test def testGetThreadFactory(): Unit = {
    val threadFactory = new SimpleThreadFactory
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10),
      threadFactory,
      new NoOpREHandler
    )
    usingPoolCleaner(p) { p => assertSame(threadFactory, p.getThreadFactory) }
  }

  /** setThreadFactory sets the thread factory returned by getThreadFactory
   */
  @Test def testSetThreadFactory(): Unit = {
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val threadFactory = new SimpleThreadFactory
      p.setThreadFactory(threadFactory)
      assertSame(threadFactory, p.getThreadFactory)

    }
  }

  /** setThreadFactory(null) throws NPE
   */
  @Test def testSetThreadFactoryNull(): Unit = {
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      try {
        p.setThreadFactory(null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }
  }

  /** getRejectedExecutionHandler returns handler in constructor if not set
   */
  @Test def testGetRejectedExecutionHandler(): Unit = {
    val handler = new NoOpREHandler
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10),
      handler
    )
    usingPoolCleaner(p) { p =>
      assertSame(handler, p.getRejectedExecutionHandler)

    }
  }

  /** setRejectedExecutionHandler sets the handler returned by
   *  getRejectedExecutionHandler
   */
  @Test def testSetRejectedExecutionHandler(): Unit = {
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val handler = new NoOpREHandler
      p.setRejectedExecutionHandler(handler)
      assertSame(handler, p.getRejectedExecutionHandler)

    }
  }

  /** setRejectedExecutionHandler(null) throws NPE
   */
  @Test def testSetRejectedExecutionHandlerNull(): Unit = {
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      try {
        p.setRejectedExecutionHandler(null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }

    }
  }

  /** getLargestPoolSize increases, but doesn't overestimate, when multiple
   *  threads active
   */
  @throws[InterruptedException]
  @Test def testGetLargestPoolSize(): Unit = {
    val THREADS = 3
    val done = new CountDownLatch(1)
    val p = new CustomTPE(
      THREADS,
      THREADS,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      assertEquals(0, p.getLargestPoolSize)
      val threadsStarted = new CountDownLatch(THREADS)
      for (i <- 0 until THREADS) {
        p.execute(new CheckedRunnable() {
          @throws[InterruptedException]
          override def realRun(): Unit = {
            threadsStarted.countDown()
            await(done)
            assertEquals(THREADS, p.getLargestPoolSize)
          }
        })
      }
      await(threadsStarted)
      assertEquals(THREADS, p.getLargestPoolSize)

    }
    assertEquals(THREADS, p.getLargestPoolSize)
  }

  /** getMaximumPoolSize returns value given in constructor if not otherwise set
   */
  @Test def testGetMaximumPoolSize(): Unit = {
    val p = new CustomTPE(
      2,
      3,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      assertEquals(3, p.getMaximumPoolSize)
      p.setMaximumPoolSize(5)
      assertEquals(5, p.getMaximumPoolSize)
      p.setMaximumPoolSize(4)
      assertEquals(4, p.getMaximumPoolSize)

    }
  }

  /** getPoolSize increases, but doesn't overestimate, when threads become
   *  active
   */
  @throws[InterruptedException]
  @Test def testGetPoolSize(): Unit = {
    val done = new CountDownLatch(1)
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      assertEquals(0, p.getPoolSize)
      val threadStarted = new CountDownLatch(1)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadStarted.countDown()
          assertEquals(1, p.getPoolSize)
          await(done)
        }
      })
      await(threadStarted)
      assertEquals(1, p.getPoolSize)

    }
  }

  /** getTaskCount increases, but doesn't overestimate, when tasks submitted
   */
  @throws[InterruptedException]
  @Test def testGetTaskCount(): Unit = {
    val TASKS = 3
    val done = new CountDownLatch(1)
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val threadStarted = new CountDownLatch(1)
      assertEquals(0, p.getTaskCount)
      assertEquals(0, p.getCompletedTaskCount)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadStarted.countDown()
          await(done)
        }
      })
      await(threadStarted)
      assertEquals(1, p.getTaskCount)
      assertEquals(0, p.getCompletedTaskCount)
      for (i <- 0 until TASKS) {
        assertEquals(1 + i, p.getTaskCount)
        p.execute(new CheckedRunnable() {
          @throws[InterruptedException]
          override def realRun(): Unit = {
            threadStarted.countDown()
            assertEquals(1 + TASKS, p.getTaskCount)
            await(done)
          }
        })
      }
      assertEquals(1 + TASKS, p.getTaskCount)
      assertEquals(0, p.getCompletedTaskCount)

    }
    assertEquals(1 + TASKS, p.getTaskCount)
    assertEquals(1 + TASKS, p.getCompletedTaskCount)
  }

  /** isShutdown is false before shutdown, true after
   */
  @Test def testIsShutdown(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      assertFalse(p.isShutdown)
      try p.shutdown()
      catch { case ok: SecurityException => () }
      assertTrue(p.isShutdown)

    }
  }

  /** isTerminated is false before termination, true after
   */
  @throws[InterruptedException]
  @Test def testIsTerminated(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val threadStarted = new CountDownLatch(1)
      val done = new CountDownLatch(1)
      assertFalse(p.isTerminating)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          assertFalse(p.isTerminating)
          threadStarted.countDown()
          await(done)
        }
      })
      await(threadStarted)
      assertFalse(p.isTerminating)
      done.countDown()
      try p.shutdown()
      catch { case ok: SecurityException => }
      assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(p.isTerminated)
      assertFalse(p.isTerminating)

    }
  }

  /** isTerminating is not true when running or when terminated
   */
  @throws[InterruptedException]
  @Test def testIsTerminating(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val threadStarted = new CountDownLatch(1)
      val done = new CountDownLatch(1)
      assertFalse(p.isTerminating)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          assertFalse(p.isTerminating)
          threadStarted.countDown()
          await(done)
        }
      })
      await(threadStarted)
      assertFalse(p.isTerminating)
      done.countDown()
      try p.shutdown()
      catch { case ok: SecurityException => }
      assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(p.isTerminated)
      assertFalse(p.isTerminating)

    }
  }

  /** getQueue returns the work queue, which contains queued tasks
   */
  @throws[InterruptedException]
  @Test def testGetQueue(): Unit = {
    val done = new CountDownLatch(1)
    val q = new ArrayBlockingQueue[Runnable](10)
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      q
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val threadStarted = new CountDownLatch(1)
      val tasks = new Array[FutureTask[_]](5)
      for (i <- 0 until tasks.length) {
        val task = new CheckedCallable[Boolean]() {
          @throws[InterruptedException]
          override def realCall(): Boolean = {
            threadStarted.countDown()
            assertSame(q, p.getQueue)
            await(done)
            java.lang.Boolean.TRUE
          }
        }
        tasks(i) = new FutureTask(task)
        p.execute(tasks(i))
      }
      await(threadStarted)
      assertSame(q, p.getQueue)
      assertFalse(q.contains(tasks(0)))
      assertTrue(q.contains(tasks(tasks.length - 1)))
      assertEquals(tasks.length - 1, q.size)

    }
  }

  /** remove(task) removes queued task, and fails to remove active task
   */
  @throws[InterruptedException]
  @Test def testRemove(): Unit = {
    val done = new CountDownLatch(1)
    val q = new ArrayBlockingQueue[Runnable](10)
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      q
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val tasks = new Array[Runnable](6)
      val threadStarted = new CountDownLatch(1)
      for (i <- 0 until tasks.length) {
        tasks(i) = new CheckedRunnable() {
          @throws[InterruptedException]
          override def realRun(): Unit = {
            threadStarted.countDown()
            await(done)
          }
        }
        p.execute(tasks(i))
      }
      await(threadStarted)
      assertFalse(p.remove(tasks(0)))
      assertTrue(q.contains(tasks(4)))
      assertTrue(q.contains(tasks(3)))
      assertTrue(p.remove(tasks(4)))
      assertFalse(p.remove(tasks(4)))
      assertFalse(q.contains(tasks(4)))
      assertTrue(q.contains(tasks(3)))
      assertTrue(p.remove(tasks(3)))
      assertFalse(q.contains(tasks(3)))

    }
  }

  /** purge removes cancelled tasks from the queue
   */
  @throws[InterruptedException]
  @Test def testPurge(): Unit = {
    val threadStarted = new CountDownLatch(1)
    val done = new CountDownLatch(1)
    val q = new ArrayBlockingQueue[Runnable](10)
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      q
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val tasks = new Array[FutureTask[_]](5)
      for (i <- 0 until tasks.length) {
        val task = new CheckedCallable[Boolean]() {
          @throws[InterruptedException]
          override def realCall(): Boolean = {
            threadStarted.countDown()
            await(done)
            java.lang.Boolean.TRUE
          }
        }
        tasks(i) = new FutureTask(task)
        p.execute(tasks(i))
      }
      await(threadStarted)
      assertEquals(tasks.length, p.getTaskCount)
      assertEquals(tasks.length - 1, q.size)
      assertEquals(1L, p.getActiveCount)
      assertEquals(0L, p.getCompletedTaskCount)
      tasks(4).cancel(true)
      tasks(3).cancel(false)
      p.purge()
      assertEquals(tasks.length - 3, q.size)
      assertEquals(tasks.length - 2, p.getTaskCount)
      p.purge() // Nothing to do

      assertEquals(tasks.length - 3, q.size)
      assertEquals(tasks.length - 2, p.getTaskCount)

    }
  }

  /** shutdownNow returns a list containing tasks that were not run, and those
   *  tasks are drained from the queue
   */
  @throws[InterruptedException]
  @Test def testShutdownNow(): Unit = {
    val poolSize = 2
    val count = 5
    val ran = new AtomicInteger(0)
    val p = new CustomTPE(
      poolSize,
      poolSize,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    val threadsStarted = new CountDownLatch(poolSize)
    val waiter = new CheckedRunnable() {
      override def realRun(): Unit = {
        threadsStarted.countDown()
        try MILLISECONDS.sleep(LONGER_DELAY_MS)
        catch {
          case success: InterruptedException =>

        }
        ran.getAndIncrement
      }
    }
    for (i <- 0 until count) { p.execute(waiter) }
    await(threadsStarted)
    assertEquals(poolSize, p.getActiveCount)
    assertEquals(0, p.getCompletedTaskCount)
    try {
      val queuedTasks = p.shutdownNow
      assertTrue(p.isShutdown)
      assertTrue(p.getQueue.isEmpty)
      assertEquals(count - poolSize, queuedTasks.size)
      assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(p.isTerminated)
      assertEquals(poolSize, ran.get)
      assertEquals(poolSize, p.getCompletedTaskCount)
    } catch {
      case ok: SecurityException => // Allowed in case test doesn't have privs
    }

  }

  /** Constructor throws if corePoolSize argument is less than zero
   */
  // Exception Tests
  @Test def testConstructor1(): Unit = {
    try {
      new CustomTPE(
        -1,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10)
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is less than zero
   */
  @Test def testConstructor2(): Unit = {
    try {
      new CustomTPE(
        1,
        -1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10)
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is equal to zero
   */
  @Test def testConstructor3(): Unit = {
    try {
      new CustomTPE(
        1,
        0,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10)
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if keepAliveTime is less than zero
   */
  @Test def testConstructor4(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        -1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10)
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if corePoolSize is greater than the maximumPoolSize
   */
  @Test def testConstructor5(): Unit = {
    try {
      new CustomTPE(
        2,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10)
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if workQueue is set to null
   */
  @Test def testConstructorNullPointerException(): Unit = {
    try {
      new CustomTPE(1, 2, 1L, SECONDS, null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if corePoolSize argument is less than zero
   */
  @Test def testConstructor6(): Unit = {
    try {
      new CustomTPE(
        -1,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is less than zero
   */
  @Test def testConstructor7(): Unit = {
    try {
      new CustomTPE(
        1,
        -1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is equal to zero
   */
  @Test def testConstructor8(): Unit = {
    try {
      new CustomTPE(
        1,
        0,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if keepAliveTime is less than zero
   */
  @Test def testConstructor9(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        -1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if corePoolSize is greater than the maximumPoolSize
   */
  @Test def testConstructor10(): Unit = {
    try {
      new CustomTPE(
        2,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if workQueue is set to null
   */
  @Test def testConstructorNullPointerException2(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        null,
        new SimpleThreadFactory
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if threadFactory is set to null
   */
  @Test def testConstructorNullPointerException3(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        null.asInstanceOf[ThreadFactory]
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if corePoolSize argument is less than zero
   */
  @Test def testConstructor11(): Unit = {
    try {
      new CustomTPE(
        -1,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is less than zero
   */
  @Test def testConstructor12(): Unit = {
    try {
      new CustomTPE(
        1,
        -1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is equal to zero
   */
  @Test def testConstructor13(): Unit = {
    try {
      new CustomTPE(
        1,
        0,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if keepAliveTime is less than zero
   */
  @Test def testConstructor14(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        -1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if corePoolSize is greater than the maximumPoolSize
   */
  @Test def testConstructor15(): Unit = {
    try {
      new CustomTPE(
        2,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if workQueue is set to null
   */
  @Test def testConstructorNullPointerException4(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        null,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if handler is set to null
   */
  @Test def testConstructorNullPointerException5(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        null.asInstanceOf[RejectedExecutionHandler]
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if corePoolSize argument is less than zero
   */
  @Test def testConstructor16(): Unit = {
    try {
      new CustomTPE(
        -1,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is less than zero
   */
  @Test def testConstructor17(): Unit = {
    try {
      new CustomTPE(
        1,
        -1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if maximumPoolSize is equal to zero
   */
  @Test def testConstructor18(): Unit = {
    try {
      new CustomTPE(
        1,
        0,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if keepAliveTime is less than zero
   */
  @Test def testConstructor19(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        -1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if corePoolSize is greater than the maximumPoolSize
   */
  @Test def testConstructor20(): Unit = {
    try {
      new CustomTPE(
        2,
        1,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** Constructor throws if workQueue is null
   */
  @Test def testConstructorNullPointerException6(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        null,
        new SimpleThreadFactory,
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if handler is null
   */
  @Test def testConstructorNullPointerException7(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        new SimpleThreadFactory,
        null.asInstanceOf[RejectedExecutionHandler]
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Constructor throws if ThreadFactory is null
   */
  @Test def testConstructorNullPointerException8(): Unit = {
    try {
      new CustomTPE(
        1,
        2,
        1L,
        SECONDS,
        new ArrayBlockingQueue[Runnable](10),
        null.asInstanceOf[ThreadFactory],
        new NoOpREHandler
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Submitted tasks are rejected when saturated or shutdown
   */
  @throws[InterruptedException]
  @Test def testSubmittedTasksRejectedWhenSaturatedOrShutdown(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](1)
    )
    val saturatedSize = JSR166Test.saturatedSize(p)
    val rnd = ThreadLocalRandom.current()
    val threadsStarted = new CountDownLatch(p.getMaximumPoolSize())
    val done = new CountDownLatch(1)
    val r: CheckedRunnable = () => {
      threadsStarted.countDown()
      var break = false
      while (!break)
        try {
          done.await()
          break = true
        } catch { case shutdownNowDeliberatelyIgnored: InterruptedException => }
    }
    val c: CheckedCallable[java.lang.Boolean] = () => {
      threadsStarted.countDown()
      var break = false
      while (!break) try {
        done.await()
        break = true
      } catch { case shutdownNowDeliberatelyIgnored: InterruptedException => }
      java.lang.Boolean.TRUE
    }
    val shutdownNow = rnd.nextBoolean()
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      // saturate
      for (i <- saturatedSize until 0 by -1) {
        rnd.nextInt(4) match {
          case 0 => p.execute(r)
          case 1 => assertFalse(p.submit(r).isDone)
          case 2 => assertFalse(p.submit(r, java.lang.Boolean.TRUE).isDone)
          case 3 => assertFalse(p.submit(c).isDone)
        }
      }
      await(threadsStarted)
      assertTaskSubmissionsAreRejected(p)
      if (shutdownNow) p.shutdownNow
      else p.shutdown()
      // Pool is shutdown, but not yet terminated
      assertTaskSubmissionsAreRejected(p)
      assertFalse(p.isTerminated)
      done.countDown() // release blocking tasks

      assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
      assertTaskSubmissionsAreRejected(p)

    }
    assertEquals(
      JSR166Test.saturatedSize(p) -
        (if (shutdownNow) p.getQueue.remainingCapacity else 0),
      p.getCompletedTaskCount
    )
  }

  /** executor using DiscardOldestPolicy drops oldest task if saturated.
   */
  @Test def testSaturatedExecute_DiscardOldestPolicy(): Unit = {
    val done = new CountDownLatch(1)
    val r1 = awaiter(done)
    val r2 = awaiter(done)
    val r3 = awaiter(done)
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](1),
      new ThreadPoolExecutor.DiscardOldestPolicy
    )
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      assertEquals(LatchAwaiter.NEW, r1.state)
      assertEquals(LatchAwaiter.NEW, r2.state)
      assertEquals(LatchAwaiter.NEW, r3.state)
      p.execute(r1)
      p.execute(r2)
      assertTrue(p.getQueue.contains(r2))
      p.execute(r3)
      assertFalse(p.getQueue.contains(r2))
      assertTrue(p.getQueue.contains(r3))

    }
    assertEquals(LatchAwaiter.DONE, r1.state)
    assertEquals(LatchAwaiter.NEW, r2.state)
    assertEquals(LatchAwaiter.DONE, r3.state)
  }

  /** execute using DiscardOldestPolicy drops task on shutdown
   */
  @Test def testDiscardOldestOnShutdown(): Unit = {
    val p = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](1),
      new ThreadPoolExecutor.DiscardOldestPolicy
    )
    try p.shutdown()
    catch {
      case ok: SecurityException =>
        return
    }
    usingPoolCleaner(p) { p =>
      val r = new TrackedNoOpRunnable
      p.execute(r)
      assertFalse(r.done)

    }
  }

  /** Submitting null tasks throws NullPointerException
   */
  @Test def testNullTaskSubmission(): Unit = {
    val p = new CustomTPE(
      1,
      2,
      1L,
      SECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      assertNullTaskSubmissionThrowsNullPointerException(p)

    }
  }

  /** setCorePoolSize of negative value throws IllegalArgumentException
   */
  @Test def testCorePoolSizeIllegalArgumentException(): Unit = {
    val p = new CustomTPE(
      1,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      try {
        p.setCorePoolSize(-1)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** setMaximumPoolSize(int) throws IllegalArgumentException if given a value
   *  less the core pool size
   */
  @Test def testMaximumPoolSizeIllegalArgumentException(): Unit = {
    val p = new CustomTPE(
      2,
      3,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      try {
        p.setMaximumPoolSize(1)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** setMaximumPoolSize throws IllegalArgumentException if given a negative
   *  value
   */
  @Test def testMaximumPoolSizeIllegalArgumentException2(): Unit = {
    val p = new CustomTPE(
      2,
      3,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      try {
        p.setMaximumPoolSize(-1)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** setKeepAliveTime throws IllegalArgumentException when given a negative
   *  value
   */
  @Test def testKeepAliveTimeIllegalArgumentException(): Unit = {
    val p = new CustomTPE(
      2,
      3,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      try {
        p.setKeepAliveTime(-1, MILLISECONDS)
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** terminated() is called on termination
   */
  @Test def testTerminated(): Unit = {
    val p = new CustomTPE
    usingPoolCleaner(p) { p =>
      try p.shutdown()
      catch { case ok: SecurityException => }
      assertTrue(p.terminatedCalled)
      assertTrue(p.isShutdown)

    }
  }

  /** beforeExecute and afterExecute are called when executing task
   */
  @throws[InterruptedException]
  @Test def testBeforeAfter(): Unit = {
    val p = new CustomTPE
    usingPoolCleaner(p) { p =>
      val done = new CountDownLatch(1)
      p.execute(new CheckedRunnable() {
        override def realRun(): Unit = { done.countDown() }
      })
      await(p.afterCalledLatch)
      assertEquals(0, done.getCount)
      assertTrue(p.afterCalled)
      assertTrue(p.beforeCalled)

    }
  }

  /** completed submit of callable returns result
   */
  @throws[Exception]
  @Test def testSubmitCallable(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val future = e.submit(new StringTask)
      val result = future.get
      assertSame(TEST_STRING, result)

    }
  }

  /** completed submit of runnable returns successfully
   */
  @throws[Exception]
  @Test def testSubmitRunnable(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val future = e.submit(new NoOpRunnable)
      future.get
      assertTrue(future.isDone)

    }
  }

  /** completed submit of (runnable, result) returns result
   */
  @throws[Exception]
  @Test def testSubmitRunnable2(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val future = e.submit(new NoOpRunnable, TEST_STRING)
      val result = future.get
      assertSame(TEST_STRING, result)

    }
  }

  /** invokeAny(null) throws NullPointerException
   */
  @throws[Exception]
  @Test def testInvokeAny1(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      try {
        e.invokeAny(null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
    }
  }

  /** invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Exception]
  @Test def testInvokeAny2(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      try {
        e.invokeAny(new util.ArrayList[Callable[String]])
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>

      }
    }
  }

  /** invokeAny(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testInvokeAny3(): Unit = {
    val latch = new CountDownLatch(1)
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(latchAwaitingStringTask(latch))
      l.add(null)
      try {
        e.invokeAny(l)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
      latch.countDown()

    }
  }

  /** invokeAny(c) throws ExecutionException if no task completes
   */
  @throws[Exception]
  @Test def testInvokeAny4(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
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
  }

  /** invokeAny(c) returns result of some task
   */
  @throws[Exception]
  @Test def testInvokeAny5(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val result = e.invokeAny(l)
      assertSame(TEST_STRING, result)

    }
  }

  /** invokeAll(null) throws NPE
   */
  @throws[Exception]
  @Test def testInvokeAll1(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      try {
        e.invokeAll(null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
    }
  }

  /** invokeAll(empty collection) returns empty list
   */
  @throws[Exception]
  @Test def testInvokeAll2(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val r = e.invokeAll(Collections.emptyList)
      assertTrue(r.isEmpty)

    }
  }

  /** invokeAll(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testInvokeAll3(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(null)
      try {
        e.invokeAll(l)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }

    }
  }

  /** get of element of invokeAll(c) throws exception on failed task
   */
  @throws[Exception]
  @Test def testInvokeAll4(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
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
  }

  /** invokeAll(c) returns results of all completed tasks
   */
  @throws[Exception]
  @Test def testInvokeAll5(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val futures = e.invokeAll(l)
      assertEquals(2, futures.size)
      futures.forEach { future => assertSame(TEST_STRING, future.get) }

    }
  }

  /** timed invokeAny(null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAny1(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      try {
        e.invokeAny(null, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }
  }

  /** timed invokeAny(,,null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAnyNullTimeUnit(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      try {
        e.invokeAny(l, randomTimeout(), null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }

    }
  }

  /** timed invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Exception]
  @Test def testTimedInvokeAny2(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      try {
        e.invokeAny(Collections.emptyList, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>
      }

    }
  }

  /** timed invokeAny(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testTimedInvokeAny3(): Unit = {
    val latch = new CountDownLatch(1)
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(latchAwaitingStringTask(latch))
      l.add(null)
      try {
        e.invokeAny(l, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
      latch.countDown()

    }
  }

  /** timed invokeAny(c) throws ExecutionException if no task completes
   */
  @throws[Exception]
  @Test def testTimedInvokeAny4(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
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
  }

  /** timed invokeAny(c) returns result of some task
   */
  @throws[Exception]
  @Test def testTimedInvokeAny5(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val startTime = System.nanoTime
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val result = e.invokeAny(l, LONG_DELAY_MS, MILLISECONDS)
      assertSame(TEST_STRING, result)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)

    }
  }

  /** timed invokeAll(null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAll1(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      try {
        e.invokeAll(null, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }
  }

  /** timed invokeAll(,,null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAllNullTimeUnit(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      try {
        e.invokeAll(l, randomTimeout(), null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }
  }

  /** timed invokeAll(empty collection) returns empty list
   */
  @throws[Exception]
  @Test def testTimedInvokeAll2(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val r =
        e.invokeAll(Collections.emptyList, randomTimeout(), randomTimeUnit())
      assertTrue(r.isEmpty)

    }
  }

  /** timed invokeAll(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testTimedInvokeAll3(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(null)
      try {
        e.invokeAll(l, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }

    }
  }

  /** get of element of invokeAll(c) throws exception on failed task
   */
  @throws[Exception]
  @Test def testTimedInvokeAll4(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
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
  }

  /** timed invokeAll(c) returns results of all completed tasks
   */
  @throws[Exception]
  @Test def testTimedInvokeAll5(): Unit = {
    val e = new CustomTPE(
      2,
      2,
      LONG_DELAY_MS,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(e) { e =>
      val l = new util.ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val futures = e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(2, futures.size)
      futures.forEach { future => assertSame(TEST_STRING, future.get) }

    }
  }

  /** timed invokeAll(c) cancels tasks not completed by timeout
   */
  @throws[Exception]
  @Test def testTimedInvokeAll6(): Unit = {
    var timeout = timeoutMillis()
    var break = false
    while (!break) {
      val done = new CountDownLatch(1)
      val waiter = new CheckedCallable[String]() {
        override def realCall(): String = {
          try done.await(LONG_DELAY_MS, MILLISECONDS)
          catch {
            case ok: InterruptedException =>

          }
          "1"
        }
      }
      val p = new CustomTPE(
        2,
        2,
        LONG_DELAY_MS,
        MILLISECONDS,
        new ArrayBlockingQueue[Runnable](10)
      )
      usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
        val tasks = new util.ArrayList[Callable[String]]
        tasks.add(new StringTask("0"))
        tasks.add(waiter)
        tasks.add(new StringTask("2"))
        val startTime = System.nanoTime
        val futures = p.invokeAll(tasks, timeout, MILLISECONDS)
        assertEquals(tasks.size, futures.size)
        assertTrue(millisElapsedSince(startTime) >= timeout)
        futures.forEach { future => assertTrue(future.isDone) }
        assertTrue(futures.get(1).isCancelled)
        try {
          assertEquals("0", futures.get(0).get)
          assertEquals("2", futures.get(2).get)
          break = true
        } catch {
          case retryWithLongerTimeout: CancellationException =>
            timeout *= 2
            if (timeout >= LONG_DELAY_MS / 2)
              fail("expected exactly one task to be cancelled")
        }

      }
    }
  }

  /** Execution continues if there is at least one thread even if thread factory
   *  fails to create more
   */
  @throws[InterruptedException]
  @Test def testFailingThreadFactory(): Unit = {
    val e = new CustomTPE(
      100,
      100,
      LONG_DELAY_MS,
      MILLISECONDS,
      new LinkedBlockingQueue[Runnable],
      new FailingThreadFactory
    )
    usingPoolCleaner(e) { e =>
      val TASKS = 100
      val done = new CountDownLatch(TASKS)
      for (k <- 0 until TASKS) {
        e.execute(new CheckedRunnable() {
          override def realRun(): Unit = { done.countDown() }
        })
      }
      await(done)
    }
  }

  /** allowsCoreThreadTimeOut is by default false.
   */
  @Test def testAllowsCoreThreadTimeOut(): Unit = {
    val p = new CustomTPE(
      2,
      2,
      1000,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p => assertFalse(p.allowsCoreThreadTimeOut) }
  }

  /** allowCoreThreadTimeOut(true) causes idle threads to time out
   */
  @throws[Exception]
  @Test def testAllowCoreThreadTimeOut_true(): Unit = {
    val keepAliveTime = timeoutMillis()
    val p = new CustomTPE(
      2,
      10,
      keepAliveTime,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val threadStarted = new CountDownLatch(1)
      p.allowCoreThreadTimeOut(true)
      p.execute(new CheckedRunnable() {
        override def realRun(): Unit = {
          threadStarted.countDown()
          assertEquals(1, p.getPoolSize)
        }
      })
      await(threadStarted)
      delay(keepAliveTime)
      val startTime = System.nanoTime
      while (p.getPoolSize > 0 && millisElapsedSince(
            startTime
          ) < LONG_DELAY_MS) Thread.`yield`()
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      assertEquals(0, p.getPoolSize)

    }
  }

  /** allowCoreThreadTimeOut(false) causes idle threads not to time out
   */
  @throws[Exception]
  @Test def testAllowCoreThreadTimeOut_false(): Unit = {
    val keepAliveTime = timeoutMillis()
    val p = new CustomTPE(
      2,
      10,
      keepAliveTime,
      MILLISECONDS,
      new ArrayBlockingQueue[Runnable](10)
    )
    usingPoolCleaner(p) { p =>
      val threadStarted = new CountDownLatch(1)
      p.allowCoreThreadTimeOut(false)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          threadStarted.countDown()
          assertTrue(p.getPoolSize >= 1)
        }
      })
      delay(2 * keepAliveTime)
      assertTrue(p.getPoolSize >= 1)

    }
  }

  /** get(cancelled task) throws CancellationException (in part, a test of
   *  CustomTPE itself)
   */
  @throws[Exception]
  @Test def testGet_cancelled(): Unit = {
    val done = new CountDownLatch(1)
    val e = new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new LinkedBlockingQueue[Runnable]
    )
    usingWrappedPoolCleaner(e)(cleaner(_, done)) { e =>
      val blockerStarted = new CountDownLatch(1)
      val futures = new util.ArrayList[Future[_]]
      for (i <- 0 until 2) {
        val r = new CheckedRunnable() {
          @throws[Throwable]
          override def realRun(): Unit = {
            blockerStarted.countDown()
            assertTrue(done.await(2 * LONG_DELAY_MS, MILLISECONDS))
          }
        }
        futures.add(e.submit(r))
      }
      await(blockerStarted)
      futures.forEach(_.cancel(false))
      futures.forEach { future =>
        try {
          future.get
          shouldThrow()
        } catch {
          case success: CancellationException =>
        }
        try {
          future.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: CancellationException =>
        }
        assertTrue(future.isCancelled)
        assertTrue(future.isDone)
      }
    }
  }

  @deprecated @Test def testFinalizeMethodCallsSuperFinalize(): Unit = {
    new CustomTPE(
      1,
      1,
      LONG_DELAY_MS,
      MILLISECONDS,
      new LinkedBlockingQueue[Runnable]
    ) {

      /** A finalize method without "throws Throwable", that calls
       *  super.finalize().
       */
      override protected def finalize(): Unit = { super.finalize() }
    }.shutdown()
  }
}
