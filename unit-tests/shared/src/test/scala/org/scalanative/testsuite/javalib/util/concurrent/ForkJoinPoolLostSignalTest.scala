package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{BeforeClass, Test}

import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper

object ForkJoinPoolLostSignalTest {
  @BeforeClass def checkRuntime(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    // The same lost wake-up still reproduces on JDK 21 (e.g. 21.0.13).
    assumeFalse(
      "ForkJoinPool lost wake-up is not fixed in this JDK",
      Platform.executingInJVMOnLowerThanJDK(22)
    )
  }
}

/** A task submitted while the only free worker is going idle must still run
 *  when the other workers are blocked in user code without `managedBlock` (no
 *  compensation): the blocked workers keep the pool's active count at its
 *  parallelism, so the submitting thread may not signal anyone.
 */
class ForkJoinPoolLostSignalTest {

  private def runRounds(
      parallelism: Int,
      submit: (ForkJoinPool, Runnable) => Unit
  ): Unit = {
    val pool = new ForkJoinPool(parallelism)
    val release = new CountDownLatch(1)
    val blocked = new CountDownLatch(parallelism - 1)
    var failed = true // do not mask an earlier failure in finally
    try {
      for (_ <- 1 until parallelism) pool.execute(new Runnable {
        def run(): Unit = {
          blocked.countDown()
          try release.await() // plain blocking, no ManagedBlocker
          catch { case _: InterruptedException => () } // shutdownNow
        }
      })
      assertTrue(
        "workers did not block",
        blocked.await(10, TimeUnit.SECONDS)
      )

      val done = new AtomicInteger(0)
      val task = new Runnable {
        def run(): Unit = done.incrementAndGet()
      }
      val roundTimeoutNanos = TimeUnit.SECONDS.toNanos(2)
      val testDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3)
      var round = 0
      while (System.nanoTime() - testDeadline < 0) {
        round += 1
        submit(pool, task)
        // Spin rather than park, so that the next submission lands while the
        // free worker is between its last failed scan and parking.
        val roundDeadline = System.nanoTime() + roundTimeoutNanos
        while (done.get() < round) {
          if (System.nanoTime() - roundDeadline > 0)
            fail(s"task of round $round was never run: $pool")
          Thread.onSpinWait()
        }
      }
      failed = false
    } finally {
      release.countDown()
      pool.shutdownNow()
      val terminated = pool.awaitTermination(10, TimeUnit.SECONDS)
      if (!failed) assertTrue(s"pool did not terminate: $pool", terminated)
    }
  }

  @Test def externalSubmitWhileOthersBlocked(): Unit =
    runRounds(2, (pool, task) => pool.execute(task))

  @Test def externalSubmitWhileOthersBlockedParallelism3(): Unit =
    runRounds(3, (pool, task) => pool.execute(task))
}
