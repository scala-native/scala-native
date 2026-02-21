package org.scalanative.testsuite.javalib.lang

import java.time.Duration
import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadSleepTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadSleepTest {
  private val Timeout = 10000L

  @Test def sleepMillis(): Unit = {
    val sleepMs = 200L
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val thread = Thread.ofVirtual().start { () =>
      val start = System.nanoTime()
      Thread.sleep(sleepMs)
      elapsed.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
    }
    thread.join(Timeout)
    assertTrue(
      s"sleep($sleepMs) should sleep at least ${sleepMs}ms, actual: ${elapsed.get()}ms",
      elapsed.get() >= sleepMs - 20 // small tolerance
    )
  }

  @Test def sleepDuration(): Unit = {
    val sleepMs = 200L
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val thread = Thread.ofVirtual().start { () =>
      val start = System.nanoTime()
      Thread.sleep(Duration.ofMillis(sleepMs))
      elapsed.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
    }
    thread.join(Timeout)
    assertTrue(
      s"sleep(Duration) should sleep at least ${sleepMs}ms, actual: ${elapsed.get()}ms",
      elapsed.get() >= sleepMs - 20
    )
  }

  @Test def sleepZero(): Unit = {
    val done = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().start { () =>
      Thread.sleep(0)
      done.set(true)
    }
    thread.join(Timeout)
    assertTrue("sleep(0) should yield and not block", done.get())
  }

  @Test def sleepNegativeThrows(): Unit = {
    val threw = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().start { () =>
      try Thread.sleep(-1)
      catch {
        case _: IllegalArgumentException => threw.set(true)
      }
    }
    thread.join(Timeout)
    assertTrue("sleep(-1) should throw IllegalArgumentException", threw.get())
  }

  @Test def sleepInterruptedBefore(): Unit = {
    val threw = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().unstarted { () =>
      Thread.currentThread().interrupt()
      try Thread.sleep(1000)
      catch {
        case _: InterruptedException => threw.set(true)
      }
    }
    thread.start()
    thread.join(Timeout)
    assertTrue(
      "sleep with pre-set interrupt should throw InterruptedException",
      threw.get()
    )
  }

  @Test def sleepInterruptedDuring(): Unit = {
    val threw = new AtomicBoolean(false)
    val started = new CountDownLatch(1)
    val thread = Thread.ofVirtual().start { () =>
      started.countDown()
      try Thread.sleep(5000)
      catch {
        case _: InterruptedException =>
          threw.set(true)
          assertFalse(
            "interrupt status should be cleared after InterruptedException",
            Thread.currentThread().isInterrupted
          )
      }
    }
    assertTrue(started.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    thread.interrupt()
    thread.join(Timeout)
    assertTrue(
      "sleep interrupted during should throw InterruptedException",
      threw.get()
    )
  }

  @Test def sleepDoesNotPinCarrier(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    val threadCount = processors * 4
    val allStarted = new CountDownLatch(threadCount)
    val allDone = new CountDownLatch(threadCount)

    val start = System.nanoTime()
    for (_ <- 0 until threadCount)
      Thread.ofVirtual().start { () =>
        allStarted.countDown()
        Thread.sleep(200)
        allDone.countDown()
      }

    assertTrue(allStarted.await(Timeout, TimeUnit.MILLISECONDS))
    assertTrue(allDone.await(Timeout, TimeUnit.MILLISECONDS))
    val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    val sequential = 200L * threadCount
    assertTrue(
      s"${threadCount} sleeping VTs should run concurrently (took ${elapsed}ms, sequential would be ${sequential}ms)",
      elapsed < sequential / 2
    )
  }

  @Test def manySleepingThreads(): Unit = {
    val count = 10000
    val latch = new CountDownLatch(count)
    val start = System.nanoTime()
    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        Thread.sleep(100)
        latch.countDown()
      }
    }
    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    assertTrue(
      s"10000 VTs sleeping 100ms should complete in well under 10s (took ${elapsed}ms)",
      elapsed < 5000
    )
  }

  @Test def sleepWithNanosAccuracy(): Unit = {
    val done = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().start { () =>
      val start = System.nanoTime()
      Thread.sleep(0, 500000) // 0.5ms
      val elapsed = System.nanoTime() - start
      assertTrue("should sleep at least ~500us", elapsed >= 100000)
      done.set(true)
    }
    thread.join(Timeout)
    assertTrue(done.get())
  }
}
