package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadLockSupportPermitRegressionTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadLockSupportPermitRegressionTest {
  private val Timeout = 10000L

  @Test def parkNanosTimeoutDoesNotUnparkNextPark(): Unit = {
    val readyToPark = new CountDownLatch(1)
    val resumed = new CountDownLatch(1)

    val vt = Thread.ofVirtual().start { () =>
      LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20))
      readyToPark.countDown()
      LockSupport.park()
      resumed.countDown()
    }

    assertTrue(readyToPark.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    assertFalse(
      "parkNanos timeout must not leave a LockSupport permit for the next park",
      resumed.await(150, TimeUnit.MILLISECONDS)
    )

    LockSupport.unpark(vt)
    assertTrue(resumed.await(Timeout, TimeUnit.MILLISECONDS))
    vt.join(Timeout)
  }

  @Test def timedParkFollowedByExplicitUnparkProvidesSinglePermit(): Unit = {
    val firstParkReady = new CountDownLatch(1)
    val firstParkReturned = new CountDownLatch(1)
    val secondParkReady = new CountDownLatch(1)
    val secondParkReturned = new CountDownLatch(1)

    val vt = Thread.ofVirtual().start { () =>
      LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20))

      firstParkReady.countDown()
      LockSupport.park()
      firstParkReturned.countDown()

      secondParkReady.countDown()
      LockSupport.park()
      secondParkReturned.countDown()
    }

    assertTrue(firstParkReady.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    assertFalse(
      "timed wakeup must not satisfy the next park without an explicit unpark",
      firstParkReturned.await(150, TimeUnit.MILLISECONDS)
    )

    LockSupport.unpark(vt)
    assertTrue(firstParkReturned.await(Timeout, TimeUnit.MILLISECONDS))

    assertTrue(secondParkReady.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    assertFalse(
      "a single explicit unpark must provide only one permit",
      secondParkReturned.await(150, TimeUnit.MILLISECONDS)
    )

    LockSupport.unpark(vt)
    assertTrue(secondParkReturned.await(Timeout, TimeUnit.MILLISECONDS))
    vt.join(Timeout)
  }

  @Test def semaphoreAcquireReleaseRepeatedWithSleep(): Unit = {
    val permits = 3
    val sem = new Semaphore(permits)
    val numThreads = 10
    val iterations = 20
    val maxConcurrent = new AtomicInteger(0)
    val currentConcurrent = new AtomicInteger(0)
    val completedOps = new AtomicInteger(0)
    val allDone = new CountDownLatch(numThreads)
    val failures = new ConcurrentLinkedQueue[Throwable]()

    for (_ <- 0 until numThreads) {
      Thread.ofVirtual().start { () =>
        try {
          for (_ <- 0 until iterations) {
            sem.acquire()
            try {
              val cur = currentConcurrent.incrementAndGet()
              maxConcurrent.updateAndGet(m => Math.max(m, cur))
              try Thread.sleep(1)
              finally currentConcurrent.decrementAndGet()
              completedOps.incrementAndGet()
            } finally sem.release()
          }
        } catch {
          case ex: Throwable => failures.add(ex)
        } finally {
          allDone.countDown()
        }
      }
    }

    assertTrue(
      "all VT semaphore workers should complete repeated acquire/release",
      allDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
    assertTrue(s"worker failures: $failures", failures.isEmpty())
    assertEquals(numThreads * iterations, completedOps.get())
    assertEquals(0, currentConcurrent.get())
    assertTrue(
      s"max concurrent should not exceed permit count ($permits), was ${maxConcurrent.get()}",
      maxConcurrent.get() <= permits
    )
  }
}
