package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

/** Tests for lazy submit and unpark behaviour.
 *    - Timeout-driven resumption uses lazy submit when on carrier with empty
 *      queue.
 *    - Unpark from another thread uses normal submit so the VT is scheduled
 *      promptly.
 *    - Sleep (parkNanos) uses resubmitYield with lazy; all paths must complete.
 */
object VirtualThreadLazySubmitCompatibilityTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadLazySubmitCompatibilityTest {
  private val Timeout = 10000L

  /** Many VTs each park with timeout; all must complete via timeout expiry.
   *  Exercises park timeout callback path (unpark(lazily = true)).
   */
  @Test def manyParkNanosCompleteViaTimeout(): Unit = {
    val count = 64
    val parkNanos = 200_000_000L // 200ms
    val done = new CountDownLatch(count)
    val start = System.nanoTime()
    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        LockSupport.parkNanos(parkNanos)
        done.countDown()
      }
    }
    assertTrue(
      s"all $count VTs should complete after parkNanos timeout",
      done.await(Timeout, TimeUnit.MILLISECONDS)
    )
    val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    assertTrue(
      s"parkNanos($parkNanos) should have elapsed for all (took ${elapsedMs}ms)",
      elapsedMs >= TimeUnit.NANOSECONDS.toMillis(parkNanos) - 100
    )
  }

  /** Unpark from a platform thread must use normal submit and wake the VT
   *  promptly.
   */
  @Test def unparkFromPlatformThreadWakesVT(): Unit = {
    val parked = new CountDownLatch(1)
    val completed = new AtomicInteger(0)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      completed.incrementAndGet()
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(80)
    LockSupport.unpark(vt)
    vt.join(Timeout)
    assertEquals(
      "VT should have resumed once after unpark from platform thread",
      1,
      completed.get()
    )
  }

  /** Mix of sleep (resubmitYield lazy path) and park + unpark from outside
   *  (normal submit).
   */
  @Test def sleepThenParkUnparkFromPlatformThread(): Unit = {
    val count = 16
    val allDone = new CountDownLatch(count)
    val threads = new Array[Thread](count)
    for (i <- 0 until count) {
      threads(i) = Thread.ofVirtual().start { () =>
        Thread.sleep(20)
        LockSupport.park()
        allDone.countDown()
      }
    }
    Thread.sleep(100)
    for (t <- threads) LockSupport.unpark(t)
    assertTrue(
      "all VTs should complete after sleep then park/unpark",
      allDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
    for (t <- threads) t.join(Timeout)
  }

  /** Many VTs sleep then complete; exercises resubmitYield (lazy when on
   *  carrier).
   */
  @Test def manySleepingThreadsComplete(): Unit = {
    val count = 128
    val done = new CountDownLatch(count)
    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        Thread.sleep(50)
        done.countDown()
      }
    }
    assertTrue(
      s"all $count sleeping VTs should complete",
      done.await(Timeout, TimeUnit.MILLISECONDS)
    )
  }

  /** Half of VTs wake by parkNanos timeout (lazy path), half by unpark from
   *  main (normal submit).
   */
  @Test def mixParkNanosTimeoutAndUnpark(): Unit = {
    val n = 16
    val byTimeout = new CountDownLatch(n / 2)
    val byUnpark = new CountDownLatch(n / 2)
    val parkedForUnpark = new CountDownLatch(n / 2)
    val threads = new Array[Thread](n)

    for (i <- 0 until n) {
      val idx =
        i // capture per iteration so closure sees correct branch on both JVM and SN
      threads(i) = Thread.ofVirtual().start { () =>
        if (idx % 2 == 0) {
          LockSupport.parkNanos(300_000_000L) // 300ms
          byTimeout.countDown()
        } else {
          parkedForUnpark.countDown()
          LockSupport.park()
          byUnpark.countDown()
        }
      }
    }
    assertTrue(parkedForUnpark.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    for (i <- 0 until n if i % 2 != 0) LockSupport.unpark(threads(i))
    assertTrue(byUnpark.await(Timeout, TimeUnit.MILLISECONDS))
    assertTrue(byTimeout.await(Timeout, TimeUnit.MILLISECONDS))
    for (t <- threads) t.join(Timeout)
  }
}
