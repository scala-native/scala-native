/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.*
import java.util
import java.util.*
import java.util.Collections
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.stream.Stream

import org.junit.{Test, Ignore}
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.function.ThrowingRunnable

import org.scalanative.testsuite.utils.Platform

object ScheduledExecutorTest {
  class RunnableCounter extends Runnable {
    val count = new AtomicInteger(0)
    override def run(): Unit = { count.getAndIncrement }
  }
}

class ScheduledExecutorTest extends JSR166Test {
  import JSR166Test.*

  /** execute successfully executes a runnable
   */
  @throws[InterruptedException]
  @Test def testExecute(): Unit = usingPoolCleaner(
    new ScheduledThreadPoolExecutor(1)
  ) { p =>
    val done = new CountDownLatch(1)
    val task = new CheckedRunnable() {
      override def realRun(): Unit = { done.countDown() }
    }
    p.execute(task)
    await(done)
  }

  /** delayed schedule of callable successfully executes after delay
   */
  @throws[Exception]
  @Test def testSchedule1(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      val startTime = System.nanoTime
      val done = new CountDownLatch(1)
      val task = new CheckedCallable[Boolean]() {
        override def realCall(): Boolean = {
          done.countDown()
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
          java.lang.Boolean.TRUE
        }
      }
      val f = p.schedule(task, timeoutMillis(), MILLISECONDS)
      assertSame(java.lang.Boolean.TRUE, f.get)
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      assertEquals(0L, done.getCount)
    }

  /** delayed schedule of runnable successfully executes after delay
   */
  @throws[Exception]
  @Test def testSchedule3(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      val startTime = System.nanoTime
      val done = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = {
          done.countDown()
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        }
      }
      val f = p.schedule(task, timeoutMillis(), MILLISECONDS)
      await(done)
      assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    }

  /** scheduleAtFixedRate executes runnable after given initial delay
   */
  @throws[Exception]
  @Test def testSchedule4(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      val startTime = System.nanoTime
      val done = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = {
          done.countDown()
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        }
      }
      val f = p.scheduleAtFixedRate(
        task,
        timeoutMillis(),
        LONG_DELAY_MS,
        MILLISECONDS
      )
      await(done)
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      f.cancel(true)
    }

  /** scheduleWithFixedDelay executes runnable after given initial delay
   */
  @throws[Exception]
  @Test def testSchedule5(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      val startTime = System.nanoTime
      val done = new CountDownLatch(1)
      val task = new CheckedRunnable() {
        override def realRun(): Unit = {
          done.countDown()
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
        }
      }
      val f = p.scheduleWithFixedDelay(
        task,
        timeoutMillis(),
        LONG_DELAY_MS,
        MILLISECONDS
      )
      await(done)
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      f.cancel(true)
    }

  /** scheduleAtFixedRate executes series of tasks at given rate. Eventually, it
   *  must hold that: cycles - 1 <= elapsedMillis/delay < cycles
   */
  @throws[InterruptedException]
  @Test def testFixedRateSequence(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      import scala.util.control.Breaks.*
      var delay = 1
      breakable {
        while ({ delay <= LONG_DELAY_MS }) {
          val startTime = System.nanoTime
          val cycles = 8
          val done = new CountDownLatch(cycles)
          val task = new CheckedRunnable() {
            override def realRun(): Unit = { done.countDown() }
          }
          val periodicTask =
            p.scheduleAtFixedRate(task, 0, delay, MILLISECONDS)
          val totalDelayMillis = (cycles - 1) * delay
          await(done, totalDelayMillis + LONG_DELAY_MS)
          periodicTask.cancel(true)
          val elapsedMillis = millisElapsedSince(startTime)
          assertTrue(elapsedMillis >= totalDelayMillis)
          if (elapsedMillis <= cycles * delay) break()
          // else retry with longer delay

          delay *= 3
        }
        fail("unexpected execution rate")
      }
    }

  /** scheduleWithFixedDelay executes series of tasks with given period.
   *  Eventually, it must hold that each task starts at least delay and at most
   *  2 * delay after the termination of the previous task.
   */
  @throws[InterruptedException]
  @Test def testFixedDelaySequence(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      var delay = 1
      import scala.util.control.Breaks.*
      breakable {
        while ({ delay <= LONG_DELAY_MS }) {
          val startTime = System.nanoTime
          val previous = new AtomicLong(startTime)
          val tryLongerDelay = new AtomicBoolean(false)
          val cycles = 8
          val done = new CountDownLatch(cycles)
          val d = delay
          val task = new CheckedRunnable() {
            override def realRun(): Unit = {
              val now = System.nanoTime
              val elapsedMillis = NANOSECONDS.toMillis(now - previous.get)
              if (done.getCount == cycles) { // first execution
                if (elapsedMillis >= d) tryLongerDelay.set(true)
              } else {
                assertTrue(elapsedMillis >= d)
                if (elapsedMillis >= 2 * d) tryLongerDelay.set(true)
              }
              previous.set(now)
              done.countDown()
            }
          }
          val periodicTask =
            p.scheduleWithFixedDelay(task, 0, delay, MILLISECONDS)
          val totalDelayMillis = (cycles - 1) * delay
          await(done, totalDelayMillis + cycles * LONG_DELAY_MS)
          periodicTask.cancel(true)
          val elapsedMillis = millisElapsedSince(startTime)
          assertTrue(elapsedMillis >= totalDelayMillis)
          if (!tryLongerDelay.get) break()

          delay *= 3
        }
        fail("unexpected execution rate")
      }
    }

  /** Submitting null tasks throws NullPointerException
   */
  @Test def testNullTaskSubmission(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) {
      assertNullTaskSubmissionThrowsNullPointerException
    }

  /** Submitted tasks are rejected when shutdown
   */
  @throws[InterruptedException]
  @Test def testSubmittedTasksRejectedWhenShutdown(): Unit = {
    val p = new ScheduledThreadPoolExecutor(1)
    val done = new CountDownLatch(1)
    val rnd = ThreadLocalRandom.current
    val threadsStarted = new CountDownLatch(p.getCorePoolSize)
    val r: Runnable = () => {
      def foo(): Unit = {
        threadsStarted.countDown()
        var break = false
        while (true) try {
          done.await()
          return
        } catch {
          case shutdownNowDeliberatelyIgnored: InterruptedException =>
        }
      }
      foo()
    }
    val c: Callable[Boolean] = () => {
      def foo(): Boolean = {
        threadsStarted.countDown()
        while (true) try {
          done.await()
          return java.lang.Boolean.TRUE
        } catch {
          case shutdownNowDeliberatelyIgnored: InterruptedException => ()
        }
        false
      }
      foo()
    }

    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      var i = p.getCorePoolSize
      while ({ { i -= 1; i + 1 } > 0 }) rnd.nextInt(4) match {
        case 0 => p.execute(r)
        case 1 => assertFalse(p.submit(r).isDone)
        case 2 => assertFalse(p.submit(r, java.lang.Boolean.TRUE).isDone)
        case 3 => assertFalse(p.submit(c).isDone)

      }
      // ScheduledThreadPoolExecutor has an unbounded queue, so never saturated.
      await(threadsStarted)
      if (rnd.nextBoolean) p.shutdownNow
      else p.shutdown()
      // Pool is shutdown, but not yet terminated
      assertTaskSubmissionsAreRejected(p)
      assertFalse(p.isTerminated)
      done.countDown() // release blocking tasks

      assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
      assertTaskSubmissionsAreRejected(p)
    }
    assertEquals(p.getCorePoolSize, p.getCompletedTaskCount)
  }

  /** getActiveCount increases but doesn't overestimate, when a thread becomes
   *  active
   */
  @throws[InterruptedException]
  @Test def testGetActiveCount(): Unit = {
    val done = new CountDownLatch(1)
    val p = new ScheduledThreadPoolExecutor(2)
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

  /** getCompletedTaskCount increases, but doesn't overestimate, when tasks
   *  complete
   */
  @throws[InterruptedException]
  @Test def testGetCompletedTaskCount(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { p =>
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
      while ({ p.getCompletedTaskCount != 1 }) {
        if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
        Thread.`yield`()
      }
    }

  /** getCorePoolSize returns size given in constructor if not otherwise set
   */
  @throws[InterruptedException]
  @Test def testGetCorePoolSize(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      assertEquals(1, p.getCorePoolSize)
    }

  /** getLargestPoolSize increases, but doesn't overestimate, when multiple
   *  threads active
   */
  @throws[InterruptedException]
  @Test def testGetLargestPoolSize(): Unit = {
    val THREADS = 3
    val p = new ScheduledThreadPoolExecutor(THREADS)
    val threadsStarted = new CountDownLatch(THREADS)
    val done = new CountDownLatch(1)
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      assertEquals(0, p.getLargestPoolSize)
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

  /** getPoolSize increases, but doesn't overestimate, when threads become
   *  active
   */
  @throws[InterruptedException]
  @Test def testGetPoolSize(): Unit = {
    val p = new ScheduledThreadPoolExecutor(1)
    val threadStarted = new CountDownLatch(1)
    val done = new CountDownLatch(1)
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      assertEquals(0, p.getPoolSize)
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
    val p = new ScheduledThreadPoolExecutor(1)
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

  /** getThreadFactory returns factory in constructor if not set
   */
  @throws[InterruptedException]
  @Test def testGetThreadFactory(): Unit = {
    val threadFactory = new SimpleThreadFactory
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1, threadFactory)) { p =>
      assertSame(threadFactory, p.getThreadFactory)
    }
  }

  /** setThreadFactory sets the thread factory returned by getThreadFactory
   */
  @throws[InterruptedException]
  @Test def testSetThreadFactory(): Unit = {
    val threadFactory = new SimpleThreadFactory
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      p.setThreadFactory(threadFactory)
      assertSame(threadFactory, p.getThreadFactory)
    }
  }

  /** setThreadFactory(null) throws NPE
   */
  @throws[InterruptedException]
  @Test def testSetThreadFactoryNull(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      try {
        p.setThreadFactory(null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }

  /** The default rejected execution handler is AbortPolicy.
   */
  @Test def testDefaultRejectedExecutionHandler(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      assertTrue(
        p.getRejectedExecutionHandler
          .isInstanceOf[ThreadPoolExecutor.AbortPolicy]
      )
    }

  /** isShutdown is false before shutdown, true after
   */
  @Test def testIsShutdown(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      assertFalse(p.isShutdown)
      try {
        p.shutdown()
        assertTrue(p.isShutdown)
      } catch {
        case ok: SecurityException =>
      }
    }

  /** isTerminated is false before termination, true after
   */
  @throws[InterruptedException]
  @Test def testIsTerminated(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      val threadStarted = new CountDownLatch(1)
      val done = new CountDownLatch(1)
      assertFalse(p.isTerminated)
      p.execute(new CheckedRunnable() {
        @throws[InterruptedException]
        override def realRun(): Unit = {
          assertFalse(p.isTerminated)
          threadStarted.countDown()
          await(done)
        }
      })
      await(threadStarted)
      assertFalse(p.isTerminating)
      done.countDown()
      try {
        p.shutdown()
        assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
        assertTrue(p.isTerminated)
      } catch { case ok: SecurityException => () }
    }

  /** isTerminating is not true when running or when terminated
   */
  @throws[InterruptedException]
  @Test def testIsTerminating(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
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
      try {
        p.shutdown()
        assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
        assertTrue(p.isTerminated)
        assertFalse(p.isTerminating)
      } catch { case ok: SecurityException => () }
    }

  /** getQueue returns the work queue, which contains queued tasks
   */
  @throws[InterruptedException]
  @Test def testGetQueue(): Unit = {
    val done = new CountDownLatch(1)
    val p = new ScheduledThreadPoolExecutor(1)
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val threadStarted = new CountDownLatch(1)
      val tasks = new Array[ScheduledFuture[?]](5)
      for (i <- 0 until tasks.length) {
        val r = new CheckedRunnable() {
          @throws[InterruptedException]
          override def realRun(): Unit = {
            threadStarted.countDown()
            await(done)
          }
        }
        tasks(i) = p.schedule(r, 1, MILLISECONDS)
      }
      await(threadStarted)
      val q = p.getQueue
      assertTrue(q.contains(tasks(tasks.length - 1)))
      assertFalse(q.contains(tasks(0)))
    }
  }

  /** remove(task) removes queued task, and fails to remove active task
   */
  @throws[InterruptedException]
  @Test def testRemove(): Unit = {
    val done = new CountDownLatch(1)
    val p = new ScheduledThreadPoolExecutor(1)
    usingWrappedPoolCleaner(p)(cleaner(_, done)) { p =>
      val tasks = new Array[ScheduledFuture[?]](5)
      val threadStarted = new CountDownLatch(1)
      for (i <- 0 until tasks.length) {
        val r = new CheckedRunnable() {
          @throws[InterruptedException]
          override def realRun(): Unit = {
            threadStarted.countDown()
            await(done)
          }
        }
        tasks(i) = p.schedule(r, 1, MILLISECONDS)
      }
      await(threadStarted)
      val q = p.getQueue
      assertFalse(p.remove(tasks(0).asInstanceOf[Runnable]))
      assertTrue(q.contains(tasks(4).asInstanceOf[Runnable]))
      assertTrue(q.contains(tasks(3).asInstanceOf[Runnable]))
      assertTrue(p.remove(tasks(4).asInstanceOf[Runnable]))
      assertFalse(p.remove(tasks(4).asInstanceOf[Runnable]))
      assertFalse(q.contains(tasks(4).asInstanceOf[Runnable]))
      assertTrue(q.contains(tasks(3).asInstanceOf[Runnable]))
      assertTrue(p.remove(tasks(3).asInstanceOf[Runnable]))
      assertFalse(q.contains(tasks(3).asInstanceOf[Runnable]))
    }
  }

  /** purge eventually removes cancelled tasks from the queue
   */
  @throws[InterruptedException]
  @Test def testPurge(): Unit = {
    val tasks = new Array[ScheduledFuture[?]](5)
    val releaser = new Runnable() {
      override def run(): Unit = {
        for (task <- tasks) { if (task != null) task.cancel(true) }
      }
    }
    usingWrappedPoolCleaner(new ScheduledThreadPoolExecutor(1))(
      cleaner(_, releaser)
    ) { p =>
      for (i <- 0 until tasks.length) {
        tasks(i) = p.schedule(
          possiblyInterruptedRunnable(SMALL_DELAY_MS),
          LONG_DELAY_MS,
          MILLISECONDS
        )
      }
      var max = tasks.length
      if (tasks(4).cancel(true)) max -= 1
      if (tasks(3).cancel(true)) max -= 1
      // There must eventually be an interference-free point at
      // which purge will not fail. (At worst, when queue is empty.)
      val startTime = System.nanoTime
      import scala.util.control.Breaks.*
      breakable {
        while ({
          p.purge()
          val count = p.getTaskCount
          if (count == max) break()
          millisElapsedSince(startTime) < LONG_DELAY_MS
        }) ()
        fail("Purge failed to remove cancelled tasks")
      }
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
    val p = new ScheduledThreadPoolExecutor(poolSize)
    val threadsStarted = new CountDownLatch(poolSize)
    val waiter = new CheckedRunnable() {
      override def realRun(): Unit = {
        threadsStarted.countDown()
        try MILLISECONDS.sleep(LONGER_DELAY_MS)
        catch { case success: InterruptedException => }
        ran.getAndIncrement()
      }
    }
    for (i <- 0 until count) { p.execute(waiter) }
    await(threadsStarted)
    assertEquals("activeCount", poolSize, p.getActiveCount)
    assertEquals(0, p.getCompletedTaskCount)
    val queuedTasks =
      try p.shutdownNow
      catch {
        case ok: SecurityException =>
          return // Allowed in case test doesn't have privs
      }
    assertTrue(p.isShutdown)
    assertTrue(p.getQueue.isEmpty)
    assertEquals("queuedTasks", count - poolSize, queuedTasks.size)
    assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
    assertTrue(p.isTerminated)
    assertEquals(s"ran $ran - $threadsStarted", poolSize, ran.get)
    assertEquals("completed", poolSize, p.getCompletedTaskCount)
  }

  @throws[InterruptedException]
  @Test def testShutdownNow_delayedTasks(): Unit = {
    val p = new ScheduledThreadPoolExecutor(1)
    val tasks = new ArrayList[ScheduledFuture[?]]
    for (i <- 0 until 3) {
      val r = new NoOpRunnable
      tasks.add(p.schedule(r, 9, SECONDS))
      tasks.add(p.scheduleAtFixedRate(r, 9, 9, SECONDS))
      tasks.add(p.scheduleWithFixedDelay(r, 9, 9, SECONDS))
    }
    if (testImplementationDetails)
      assertEquals(new HashSet(tasks), new HashSet(p.getQueue))
    val queuedTasks =
      try p.shutdownNow
      catch {
        case ok: SecurityException =>
          return
      }
    assertTrue(p.isShutdown)
    assertTrue(p.getQueue.isEmpty)
    if (testImplementationDetails)
      assertEquals(new HashSet(tasks), new HashSet(queuedTasks))
    assertEquals(tasks.size, queuedTasks.size)
    tasks.forEach { task =>
      assertFalse(task.isDone)
      assertFalse(task.isCancelled)
    }
    assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
    assertTrue(p.isTerminated)
  }

  /** By default, periodic tasks are cancelled at shutdown. By default, delayed
   *  tasks keep running after shutdown. Check that changing the default values
   *  work:
   *    - setExecuteExistingDelayedTasksAfterShutdownPolicy
   *    - setContinueExistingPeriodicTasksAfterShutdownPolicy
   */
  @SuppressWarnings(Array("FutureReturnValueIgnored")) @throws[Exception]
  @Test def testShutdown_cancellation(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val poolSize = 4
    val p = new ScheduledThreadPoolExecutor(poolSize)
    val q = p.getQueue
    val rnd = ThreadLocalRandom.current
    val delay = rnd.nextInt(2)
    val rounds = rnd.nextInt(1, 3)
    var effectiveDelayedPolicy = false
    var effectivePeriodicPolicy = false
    var effectiveRemovePolicy = false
    if (rnd.nextBoolean) {
      effectiveDelayedPolicy = rnd.nextBoolean
      p.setExecuteExistingDelayedTasksAfterShutdownPolicy(
        effectiveDelayedPolicy
      )
    } else effectiveDelayedPolicy = true

    assertEquals(
      effectiveDelayedPolicy,
      p.getExecuteExistingDelayedTasksAfterShutdownPolicy
    )
    if (rnd.nextBoolean) {
      effectivePeriodicPolicy = rnd.nextBoolean
      p.setContinueExistingPeriodicTasksAfterShutdownPolicy(
        effectivePeriodicPolicy
      )
    } else effectivePeriodicPolicy = false
    assertEquals(
      effectivePeriodicPolicy,
      p.getContinueExistingPeriodicTasksAfterShutdownPolicy()
    )

    if (rnd.nextBoolean) {
      effectiveRemovePolicy = rnd.nextBoolean
      p.setRemoveOnCancelPolicy(effectiveRemovePolicy)
    } else effectiveRemovePolicy = false
    assertEquals(effectiveRemovePolicy, p.getRemoveOnCancelPolicy)

    val periodicTasksContinue =
      effectivePeriodicPolicy && rnd.nextBoolean
    // Strategy: Wedge the pool with one wave of "blocker" tasks,
    // then add a second wave that waits in the queue until unblocked.
    val ran = new AtomicInteger(0)
    val poolBlocked = new CountDownLatch(poolSize)
    val unblock = new CountDownLatch(1)
    val exception = new RuntimeException
    class Task extends Runnable {
      override def run(): Unit = {
        try {
          ran.getAndIncrement
          poolBlocked.countDown()
          await(unblock)
        } catch {
          case fail: Throwable =>
            threadUnexpectedException(fail)
        }
      }
    }
    class PeriodicTask(var rounds: Int) extends Task {
      override def run(): Unit = {
        if ({ rounds -= 1; rounds } == 0) super.run()
        // throw exception to surely terminate this periodic task,
        // but in a separate execution and in a detectable way.
        if (rounds == -1) throw exception
      }
    }
    val task = new Task
    val immediates = new ArrayList[Future[?]]
    val delayeds = new ArrayList[Future[?]]
    val periodics = new ArrayList[Future[?]]
    immediates.add(p.submit(task))
    delayeds.add(p.schedule(task, delay, MILLISECONDS))
    periodics.add(
      p.scheduleAtFixedRate(new PeriodicTask(rounds), delay, 1, MILLISECONDS)
    )
    periodics.add(
      p.scheduleWithFixedDelay(new PeriodicTask(rounds), delay, 1, MILLISECONDS)
    )
    await(poolBlocked)
    assertEquals(poolSize, ran.get)
    assertEquals(poolSize, p.getActiveCount)
    assertTrue(q.isEmpty)
    // Add second wave of tasks.
    immediates.add(p.submit(task))
    delayeds.add(
      p.schedule(
        task,
        if (effectiveDelayedPolicy) delay.toLong else LONG_DELAY_MS,
        MILLISECONDS
      )
    )
    periodics.add(
      p.scheduleAtFixedRate(new PeriodicTask(rounds), delay, 1, MILLISECONDS)
    )
    periodics.add(
      p.scheduleWithFixedDelay(new PeriodicTask(rounds), delay, 1, MILLISECONDS)
    )
    assertEquals(poolSize, q.size)
    assertEquals(poolSize, ran.get)
    immediates.forEach((f: Future[?]) =>
      assertTrue(
        f.asInstanceOf[ScheduledFuture[?]].getDelay(NANOSECONDS) <= 0L
      )
    )
    locally {
      val stream = new ArrayList[Future[?]]
      stream.addAll(immediates)
      stream.addAll(delayeds)
      stream.addAll(periodics)
      stream.forEach { (f: Future[?]) => assertFalse(f.isDone) }
    }
    try p.shutdown()
    catch {
      case ok: SecurityException =>
        return
    }
    assertTrue(p.isShutdown)
    assertTrue(p.isTerminating)
    assertFalse(p.isTerminated)
    if (rnd.nextBoolean)
      Seq(
        { () => p.submit(task) }: ThrowingRunnable,
        { () => p.schedule(task, 1, SECONDS) }: ThrowingRunnable,
        { () =>
          p.scheduleAtFixedRate(new PeriodicTask(1), 1, 1, SECONDS)
        }: ThrowingRunnable,
        { () =>
          p.scheduleWithFixedDelay(new PeriodicTask(2), 1, 1, SECONDS)
        }: ThrowingRunnable
      ).foreach(
        assertThrows(
          classOf[RejectedExecutionException],
          _
        )
      )
    assertTrue(q.contains(immediates.get(1)))
    assertTrue(!effectiveDelayedPolicy ^ q.contains(delayeds.get(1)))
    assertTrue(
      !effectivePeriodicPolicy ^ q.containsAll(periodics.subList(2, 4))
    )
    immediates.forEach((f: Future[?]) => assertFalse(f.isDone))
    assertFalse(delayeds.get(0).isDone)
    if (effectiveDelayedPolicy) assertFalse(delayeds.get(1).isDone)
    else assertTrue(delayeds.get(1).isCancelled)
    if (effectivePeriodicPolicy) periodics.forEach((f: Future[?]) => {
      def foo(f: Future[?]) = {
        assertFalse(f.isDone)
        if (!periodicTasksContinue) {
          assertTrue(f.cancel(false))
          assertTrue(f.isCancelled)
        }
      }
      foo(f)
    })
    else {
      periodics.subList(0, 2).forEach((f: Future[?]) => assertFalse(f.isDone))
      periodics
        .subList(2, 4)
        .forEach((f: Future[?]) => assertTrue(f.isCancelled))
    }
    unblock.countDown() // Release all pool threads

    assertTrue(p.awaitTermination(LONG_DELAY_MS, MILLISECONDS))
    assertFalse(p.isTerminating)
    assertTrue(p.isTerminated)
    assertTrue(q.isEmpty)

    locally {
      val stream = new ArrayList[Future[?]]
      stream.addAll(immediates)
      stream.addAll(delayeds)
      stream.addAll(periodics)
      stream.forEach { (f: Future[?]) => assertTrue(f.isDone) }
    }
    immediates.forEach { f => assertNull(f.get) }
    assertNull(delayeds.get(0).get)
    if (effectiveDelayedPolicy) assertNull(delayeds.get(1).get)
    else assertTrue(delayeds.get(1).isCancelled)
    if (periodicTasksContinue) periodics.forEach((f: Future[?]) => {
      def foo(f: Future[?]) = try f.get
      catch {
        case success: ExecutionException =>
          assertSame(exception, success.getCause)
        case fail: Throwable =>
          threadUnexpectedException(fail)
      }
      foo(f)
    })
    else periodics.forEach((f: Future[?]) => assertTrue(f.isCancelled))
    assertEquals(
      poolSize + 1 + (if (effectiveDelayedPolicy) 1
                      else 0) + (if (periodicTasksContinue) 2
                                 else 0),
      ran.get
    )
  }

  /** completed submit of callable returns result
   */
  @throws[Exception]
  @Test def testSubmitCallable(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val future = e.submit(new StringTask)
      val result = future.get
      assertEquals(TEST_STRING, result)
    }

  /** completed submit of runnable returns successfully
   */
  @throws[Exception]
  @Test def testSubmitRunnable(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val future = e.submit(new NoOpRunnable)
      future.get
      assertTrue(future.isDone)
    }

  /** completed submit of (runnable, result) returns result
   */
  @throws[Exception]
  @Test def testSubmitRunnable2(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val future = e.submit(new NoOpRunnable, TEST_STRING)
      val result = future.get
      assertEquals(TEST_STRING, result)
    }

  /** invokeAny(null) throws NullPointerException
   */
  @throws[Exception]
  @Test def testInvokeAny1(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      try {
        e.invokeAny(null)
        shouldThrow()
      } catch { case success: NullPointerException => }
    }

  /** invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Exception]
  @Test def testInvokeAny2(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      try {
        e.invokeAny(new ArrayList[Callable[String]])
        shouldThrow()
      } catch { case success: IllegalArgumentException => }
    }

  /** invokeAny(c) throws NullPointerException if c has null elements
   */
  @throws[Exception]
  @Test def testInvokeAny3(): Unit = {
    val latch = new CountDownLatch(1)
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(latchAwaitingStringTask(latch))
      l.add(null)
      try {
        e.invokeAny(l)
        shouldThrow()
      } catch { case success: NullPointerException => }
      latch.countDown()
    }
  }

  /** invokeAny(c) throws ExecutionException if no task completes
   */
  @throws[Exception]
  @Test def testInvokeAny4(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new NPETask)
      try {
        e.invokeAny(l)
        shouldThrow()
      } catch {
        case success: ExecutionException =>
          assertTrue(success.getCause.isInstanceOf[NullPointerException])
      }
    }

  /** invokeAny(c) returns result of some task
   */
  @throws[Exception]
  @Test def testInvokeAny5(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val result = e.invokeAny(l)
      assertEquals(TEST_STRING, result)
    }

  /** invokeAll(null) throws NPE
   */
  @throws[Exception]
  @Test def testInvokeAll1(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      try {
        e.invokeAll(null)
        shouldThrow()
      } catch { case success: NullPointerException => }
    }

  /** invokeAll(empty collection) returns empty list
   */
  @throws[Exception]
  @Test def testInvokeAll2(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val r = e.invokeAll(Collections.emptyList)
      assertTrue(r.isEmpty)
    }

  /** invokeAll(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testInvokeAll3(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(null)
      try {
        e.invokeAll(l)
        shouldThrow()
      } catch { case success: NullPointerException => }
    }

  /** get of invokeAll(c) throws exception on failed task
   */
  @throws[Exception]
  @Test def testInvokeAll4(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
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

  /** invokeAll(c) returns results of all completed tasks
   */
  @throws[Exception]
  @Test def testInvokeAll5(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val futures = e.invokeAll(l)
      assertEquals(2, futures.size)
      futures.forEach { future => assertEquals(TEST_STRING, future.get) }
    }

  /** timed invokeAny(null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAny1(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      try {
        e.invokeAny(null, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch { case success: NullPointerException => }
    }

  /** timed invokeAny(,,null) throws NullPointerException
   */
  @throws[Exception]
  @Test def testTimedInvokeAnyNullTimeUnit(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      try {
        e.invokeAny(l, randomTimeout(), null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }

  /** timed invokeAny(empty collection) throws IllegalArgumentException
   */
  @throws[Exception]
  @Test def testTimedInvokeAny2(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      try {
        e.invokeAny(Collections.emptyList, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: IllegalArgumentException =>
      }
    }

  /** timed invokeAny(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testTimedInvokeAny3(): Unit = {
    val latch = new CountDownLatch(1)
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(latchAwaitingStringTask(latch))
      l.add(null)
      try {
        e.invokeAny(l, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch { case success: NullPointerException => }
      latch.countDown()
    }
  }

  /** timed invokeAny(c) throws ExecutionException if no task completes
   */
  @throws[Exception]
  @Test def testTimedInvokeAny4(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val startTime = System.nanoTime
      val l = new ArrayList[Callable[String]]
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

  /** timed invokeAny(c) returns result of some task
   */
  @throws[Exception]
  @Test def testTimedInvokeAny5(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val startTime = System.nanoTime
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val result = e.invokeAny(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(TEST_STRING, result)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }

  /** timed invokeAll(null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAll1(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      try {
        e.invokeAll(null, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }

  /** timed invokeAll(,,null) throws NPE
   */
  @throws[Exception]
  @Test def testTimedInvokeAllNullTimeUnit(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
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
  @throws[Exception]
  @Test def testTimedInvokeAll2(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val r =
        e.invokeAll(Collections.emptyList, randomTimeout(), randomTimeUnit())
      assertTrue(r.isEmpty)
    }

  /** timed invokeAll(c) throws NPE if c has null elements
   */
  @throws[Exception]
  @Test def testTimedInvokeAll3(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(null)
      try {
        e.invokeAll(l, randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch { case success: NullPointerException => }
    }

  /** get of element of invokeAll(c) throws exception on failed task
   */
  @throws[Exception]
  @Test def testTimedInvokeAll4(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new NPETask)
      val futures =
        e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(1, futures.size)
      try {
        futures.get(0).get
        shouldThrow()
      } catch {
        case success: ExecutionException =>
          assertTrue(success.getCause.isInstanceOf[NullPointerException])
      }
    }

  /** timed invokeAll(c) returns results of all completed tasks
   */
  @throws[Exception]
  @Test def testTimedInvokeAll5(): Unit =
    usingPoolCleaner(new ScheduledThreadPoolExecutor(2)) { e =>
      val l = new ArrayList[Callable[String]]
      l.add(new StringTask)
      l.add(new StringTask)
      val futures = e.invokeAll(l, LONG_DELAY_MS, MILLISECONDS)
      assertEquals(2, futures.size)
      futures.forEach { future => assertEquals(TEST_STRING, future.get) }
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
      usingWrappedPoolCleaner(new ScheduledThreadPoolExecutor(2))(
        cleaner(_, done)
      ) { p =>
        val tasks = new ArrayList[Callable[String]]
        tasks.add(new StringTask("0"))
        tasks.add(waiter)
        tasks.add(new StringTask("2"))
        val startTime = System.nanoTime
        val futures =
          p.invokeAll(tasks, timeout, MILLISECONDS)
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

  /** A fixed delay task with overflowing period should not prevent a one-shot
   *  task from executing. https://bugs.openjdk.java.net/browse/JDK-8051859
   */
  @SuppressWarnings(Array("FutureReturnValueIgnored")) @throws[Exception]
  @Test def testScheduleWithFixedDelay_overflow(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val delayedDone = new CountDownLatch(1)
    val immediateDone = new CountDownLatch(1)
    usingPoolCleaner(new ScheduledThreadPoolExecutor(1)) { p =>
      val delayed: Runnable = () => {
        def foo() = {
          delayedDone.countDown()
          p.submit({ () => immediateDone.countDown() }: Runnable)
        }
        foo()
      }
      p.scheduleWithFixedDelay(delayed, 0L, java.lang.Long.MAX_VALUE, SECONDS)
      await(delayedDone)
      await(immediateDone)
    }
  }
}
