package org.scalanative.testsuite.javalib.util

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.{Date, Timer, TimerTask}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{After, BeforeClass, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper.assumeMultithreadingIsEnabled

object TimerTest {
  @BeforeClass def assumeThreads(): Unit = assumeMultithreadingIsEnabled()
}

/** `Timer` and `TimerTask`. Timers are daemons and are cancelled after each
 *  test.
 */
class TimerTest {
  private val timers = new java.util.ArrayList[Timer]()

  private def timer(): Timer = {
    val t = new Timer(true)
    timers.add(t)
    t
  }

  @After def cancelTimers(): Unit = {
    val it = timers.iterator()
    while (it.hasNext()) it.next().cancel()
  }

  private def task(body: => Unit = ()): TimerTask = new TimerTask {
    def run(): Unit = body
  }

  private def assertThrowsMessage[T <: Throwable](
      expected: Class[T],
      msg: String,
      f: => Unit
  ): Unit =
    assertEquals(msg, assertThrows(expected, f).getMessage())

  private def await(latch: CountDownLatch): Unit =
    assertTrue("timed out", latch.await(10, TimeUnit.SECONDS))

  @Test def argumentChecks(): Unit = {
    val t = timer()
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Negative delay.",
      t.schedule(task(), -1L)
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Negative delay.",
      t.schedule(task(), -1L, 10L)
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Non-positive period.",
      t.schedule(task(), 1L, 0L)
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Non-positive period.",
      t.scheduleAtFixedRate(task(), 1L, -5L)
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Illegal execution time.",
      t.schedule(task(), new Date(-1))
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Illegal execution time.",
      t.schedule(task(), new Date(-1), 10L)
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Non-positive period.",
      t.schedule(task(), new Date(), 0L)
    )
    assertThrowsMessage(
      classOf[IllegalArgumentException],
      "Illegal execution time.",
      t.schedule(task(), Long.MaxValue)
    )
    assertThrows(classOf[NullPointerException], t.schedule(null, 10L))
    assertThrows(classOf[NullPointerException], t.schedule(task(), null: Date))
    assertThrowsMessage(
      classOf[NullPointerException],
      "name cannot be null",
      new Timer(null: String)
    )
    assertThrowsMessage(
      classOf[NullPointerException],
      "name cannot be null",
      new Timer(null, true)
    )
  }

  @Test def taskStates(): Unit = {
    val t = timer()
    val twice = task()
    t.schedule(twice, 100000L)
    assertThrowsMessage(
      classOf[IllegalStateException],
      "Task already scheduled or cancelled",
      t.schedule(twice, 100000L)
    )
    val cancelled = task()
    cancelled.cancel()
    assertThrowsMessage(
      classOf[IllegalStateException],
      "Task already scheduled or cancelled",
      t.schedule(cancelled, 10L)
    )
    val fresh = task()
    assertFalse(fresh.cancel())
    assertFalse(fresh.cancel())
    assertEquals(0L, fresh.scheduledExecutionTime())
    val scheduled = task()
    t.schedule(scheduled, 100000L)
    assertTrue(scheduled.cancel())
    assertFalse(scheduled.cancel())
  }

  @Test def oneShotAndRepeating(): Unit = {
    val t = timer()
    val ran = new CountDownLatch(1)
    val once = task(ran.countDown())
    t.schedule(once, 1L)
    await(ran)
    Thread.sleep(20)
    assertFalse(once.cancel())
    assertTrue(once.scheduledExecutionTime() > 0)

    val twice = new CountDownLatch(2)
    val repeating = task(twice.countDown())
    t.schedule(repeating, 1L, 5L)
    await(twice)
    assertTrue(repeating.cancel())
    assertFalse(repeating.cancel())
  }

  @Test def scheduledTime(): Unit = {
    val t = timer()
    val done = new CountDownLatch(1)
    val seen = new Array[Long](1)
    val at = System.currentTimeMillis() + 30
    t.schedule(
      new TimerTask {
        def run(): Unit = {
          seen(0) = this.scheduledExecutionTime()
          done.countDown()
        }
      },
      new Date(at)
    )
    await(done)
    assertEquals(at, seen(0))
  }

  @Test def purgeAndCancel(): Unit = {
    val t = timer()
    val a, b, c = task()
    // The timer thread itself drops a cancelled task at the head of the queue: keep c first
    t.schedule(c, 100000L)
    t.schedule(a, 200000L)
    t.schedule(b, 200000L)
    a.cancel()
    b.cancel()
    assertEquals(2, t.purge())
    assertEquals(0, t.purge())
    t.cancel()
    t.cancel()
    assertEquals(0, t.purge())
    assertThrowsMessage(
      classOf[IllegalStateException],
      "Timer already cancelled.",
      t.schedule(task(), 10L)
    )
  }

  @Test def failingTaskEndsTimer(): Unit = {
    val t = timer()
    val ran = new CountDownLatch(1)
    t.schedule(
      task {
        ran.countDown(); throw new RuntimeException("expected by TimerTest")
      },
      1L
    )
    await(ran)
    Thread.sleep(50)
    assertThrowsMessage(
      classOf[IllegalStateException],
      "Timer already cancelled.",
      t.schedule(task(), 10L)
    )
  }

  @Test def threads(): Unit = {
    val named = new Timer("named", true)
    timers.add(named)
    val done = new CountDownLatch(1)
    val seen = new Array[String](2)
    named.schedule(
      task {
        seen(0) = Thread.currentThread().getName()
        seen(1) = String.valueOf(Thread.currentThread().isDaemon())
        done.countDown()
      },
      1L
    )
    await(done)
    assertEquals("named", seen(0))
    assertEquals("true", seen(1))
    val unnamed = timer()
    val done2 = new CountDownLatch(1)
    unnamed.schedule(
      task { seen(0) = Thread.currentThread().getName(); done2.countDown() },
      1L
    )
    await(done2)
    assertTrue(seen(0), seen(0).matches("Timer-\\d+"))
  }

  @Test def fixedRateCatchesUp(): Unit = {
    val count = new AtomicInteger()
    val past = new Date(System.currentTimeMillis() - 1000)
    val rate = timer()
    rate.scheduleAtFixedRate(task(count.incrementAndGet()), past, 100L)
    Thread.sleep(200)
    rate.cancel()
    assertTrue(count.get().toString, count.get() >= 10)
    val delayed = new AtomicInteger()
    val delay = timer()
    delay.schedule(task(delayed.incrementAndGet()), past, 100L)
    Thread.sleep(150)
    delay.cancel()
    assertTrue(delayed.get().toString, delayed.get() <= 3)
  }

  @Test def cancelFromTask(): Unit = {
    val t = timer()
    val done = new CountDownLatch(1)
    val result = new Array[Boolean](1)
    t.schedule(
      new TimerTask {
        def run(): Unit = {
          result(0) = cancel()
          done.countDown()
        }
      },
      1L,
      10L
    )
    await(done)
    assertTrue(result(0))
    val u = timer()
    val done2 = new CountDownLatch(1)
    u.schedule(task { u.cancel(); done2.countDown() }, 1L)
    await(done2)
    assertThrowsMessage(
      classOf[IllegalStateException],
      "Timer already cancelled.",
      u.schedule(task(), 1L)
    )
  }

  @Test def order(): Unit = {
    val t = timer()
    val order = new java.util.ArrayList[Integer]()
    val done = new CountDownLatch(3)
    val start = System.currentTimeMillis() + 50
    for (i <- Seq(3, 1, 2))
      t.schedule(
        task { order.synchronized(order.add(i)); done.countDown() },
        new Date(start + i * 20)
      )
    await(done)
    assertEquals("[1, 2, 3]", order.toString)
  }

  // The Timer is created and dropped on another thread, so that no stale stack
  // slot of the test thread keeps it alive (stack scanning is conservative).
  private def startIdleTimer(keep: Array[Timer]): Thread = {
    val thread = new Array[Thread](1)
    val creator = new Thread(() => {
      val t = new Timer(true)
      if (keep != null) keep(0) = t
      val ran = new CountDownLatch(1)
      t.schedule(
        task { thread(0) = Thread.currentThread(); ran.countDown() },
        1L
      )
      await(ran)
    })
    creator.start()
    creator.join()
    assertNotNull(thread(0))
    thread(0)
  }

  private def gcUntilDead(thread: Thread, timeoutMillis: Long): Unit = {
    val deadline = System.currentTimeMillis() + timeoutMillis
    while (thread.isAlive() && System.currentTimeMillis() < deadline) {
      locally { val _ = Seq.fill(1000)(new Object {}) }
      System.gc()
      thread.join(200)
    }
  }

  @Test def threadOfUnreachableTimerEnds(): Unit = {
    assumeTrue("requires weak references", Platform.isWeakReferenceSupported)
    assumeFalse(
      "weak references are sometimes not cleared on Windows CI, see WeakReferenceTestOnJDK9",
      sys.env.contains("CI") && Platform.isWindows
    )
    val thread = startIdleTimer(null)
    gcUntilDead(thread, 30000L)
    assertFalse(
      "thread of an unreachable idle Timer still alive",
      thread.isAlive()
    )
  }

  @Test def threadOfReachableTimerStays(): Unit = {
    assumeTrue("requires weak references", Platform.isWeakReferenceSupported)
    val keep = new Array[Timer](1)
    val thread = startIdleTimer(keep)
    timers.add(keep(0))
    gcUntilDead(thread, 2000L)
    assertTrue("thread of a reachable idle Timer ended", thread.isAlive())
    assertNotNull(keep(0))
  }
}
