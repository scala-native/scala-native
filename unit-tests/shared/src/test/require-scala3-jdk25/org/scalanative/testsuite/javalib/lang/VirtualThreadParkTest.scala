package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadParkTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadParkTest {
  private val Timeout = 5000L

  @Test def parkAndUnpark(): Unit = {
    val parked = new CountDownLatch(1)
    val result = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      result.set(true)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50) // let it actually park
    LockSupport.unpark(vt)
    vt.join(Timeout)
    assertTrue("VT should have resumed after unpark", result.get())
  }

  @Test def parkWithBlocker(): Unit = {
    val blocker = new Object
    val parked = new CountDownLatch(1)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park(blocker)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val reportedBlocker = LockSupport.getBlocker(vt)
    LockSupport.unpark(vt)
    vt.join(Timeout)
    assertSame("blocker should be set while parked", blocker, reportedBlocker)
  }

  @Test def parkNanosTimesOut(): Unit = {
    val parkNs = 200_000_000L // 200ms
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val vt = Thread.ofVirtual().start { () =>
      val start = System.nanoTime()
      LockSupport.parkNanos(parkNs)
      elapsed.set(System.nanoTime() - start)
    }
    vt.join(Timeout)
    assertTrue(
      s"parkNanos should timeout after ~200ms, elapsed: ${elapsed.get() / 1_000_000}ms",
      elapsed.get() >= parkNs - 20_000_000
    )
  }

  @Test def parkNanosWithBlocker(): Unit = {
    val blocker = new Object
    val parkNs = 200_000_000L
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val parked = new CountDownLatch(1)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      val start = System.nanoTime()
      LockSupport.parkNanos(blocker, parkNs)
      elapsed.set(System.nanoTime() - start)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val reportedBlocker = LockSupport.getBlocker(vt)
    vt.join(Timeout)
    assertSame(blocker, reportedBlocker)
    assertTrue(elapsed.get() >= parkNs - 20_000_000)
  }

  @Test def parkUntilDeadline(): Unit = {
    val deadlineMs = System.currentTimeMillis() + 200
    val vt = Thread.ofVirtual().start { () =>
      LockSupport.parkUntil(deadlineMs)
    }
    vt.join(Timeout)
    assertTrue(
      "should have waited until deadline",
      System.currentTimeMillis() >= deadlineMs - 20
    )
  }

  @Test def parkUntilWithBlocker(): Unit = {
    val blocker = new Object
    val deadlineMs = System.currentTimeMillis() + 200
    val parked = new CountDownLatch(1)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.parkUntil(blocker, deadlineMs)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val reportedBlocker = LockSupport.getBlocker(vt)
    vt.join(Timeout)
    assertSame(blocker, reportedBlocker)
    assertTrue(System.currentTimeMillis() >= deadlineMs - 20)
  }

  @Test def unparkBeforePark(): Unit = {
    val done = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().unstarted { () =>
      done.set(true)
    }
    // unpark before start -- permit should be consumed by next park
    LockSupport.unpark(vt)
    vt.start()
    vt.join(Timeout)
    assertTrue(done.get())

    // More explicit: unpark, then park should return immediately
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val vt2 = Thread.ofVirtual().start { () =>
      // Let the main thread call unpark first
      Thread.sleep(100)
      val start = System.nanoTime()
      LockSupport.park()
      elapsed.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
    }
    Thread.sleep(50)
    LockSupport.unpark(vt2)
    vt2.join(Timeout)
    assertTrue(
      s"park after unpark should return quickly, took ${elapsed.get()}ms",
      elapsed.get() < 1000
    )
  }

  @Test def parkStateIsWaiting(): Unit = {
    val parked = new CountDownLatch(1)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(100) // allow time to settle into parked state
    assertEquals(
      "parked VT should be in WAITING state",
      Thread.State.WAITING,
      vt.getState()
    )
    LockSupport.unpark(vt)
    vt.join(Timeout)
  }

  @Test def parkNanosStateIsTimedWaiting(): Unit = {
    val parked = new CountDownLatch(1)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.parkNanos(5_000_000_000L) // 5 seconds
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(100)
    assertEquals(
      "parkNanos VT should be in TIMED_WAITING state",
      Thread.State.TIMED_WAITING,
      vt.getState()
    )
    LockSupport.unpark(vt)
    vt.join(Timeout)
  }

  @Test def parkDoesNotPinCarrier(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    val threadCount = processors * 4
    val allParked = new CountDownLatch(threadCount)
    val allDone = new CountDownLatch(threadCount)
    val threads = new Array[Thread](threadCount)

    for (i <- 0 until threadCount) {
      threads(i) = Thread.ofVirtual().start { () =>
        allParked.countDown()
        LockSupport.park()
        allDone.countDown()
      }
    }

    assertTrue(allParked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    for (t <- threads) LockSupport.unpark(t)
    assertTrue(allDone.await(Timeout, TimeUnit.MILLISECONDS))
    for (t <- threads) t.join(Timeout)
  }

  @Test def parkInterruptWakesUp(): Unit = {
    val parked = new CountDownLatch(1)
    val wasInterrupted = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      wasInterrupted.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue(
      "interrupt should wake up parked VT with interrupt status set",
      wasInterrupted.get()
    )
  }

  @Test def parkAlreadyInterrupted(): Unit = {
    val done = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      Thread.currentThread().interrupt()
      val start = System.nanoTime()
      LockSupport.park()
      val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
      assertTrue(
        "park with pre-set interrupt should return immediately",
        elapsed < 500
      )
      done.set(true)
    }
    vt.join(Timeout)
    assertTrue(done.get())
  }

  @Test def manyParkedThreads(): Unit = {
    val count = 10000
    val allParked = new CountDownLatch(count)
    val allDone = new CountDownLatch(count)
    val threads = new Array[Thread](count)

    for (i <- 0 until count) {
      threads(i) = Thread.ofVirtual().start { () =>
        allParked.countDown()
        LockSupport.park()
        allDone.countDown()
      }
    }

    assertTrue(
      "all threads should park",
      allParked.await(Timeout, TimeUnit.MILLISECONDS)
    )
    Thread.sleep(100)
    for (t <- threads) LockSupport.unpark(t)
    assertTrue(
      "all threads should resume after unpark",
      allDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
    for (t <- threads) t.join(Timeout)
  }
}
