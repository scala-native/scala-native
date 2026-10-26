package scala.scalanative.runtime

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, ForkJoinPool, TimeUnit}

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadUnparkOrderTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadUnparkOrderTest {
  @Test def unparkFromVirtualThreadUsesCarrierQueue(): Unit = {
    val pool = new ForkJoinPool(
      1,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory,
      null,
      true
    )
    val scheduler = new VirtualThreadScheduler {
      def execute(task: Runnable): Unit = pool.execute(task)
      def schedule(
          task: Runnable,
          delay: Long,
          unit: TimeUnit
      ): VirtualThreadScheduler.Cancellable =
        throw new UnsupportedOperationException("untimed parks only")
    }
    val ownerRan = new AtomicBoolean(false)
    val wakeRequested = new AtomicBoolean(false)
    val releaseWorker = new CountDownLatch(1)
    val workerBlocked = new CountDownLatch(1)
    val spinnerGaveUp = new AtomicBoolean(false)
    val wakerIdentityRestored = new AtomicBoolean(false)
    val previous = VirtualThread.defaultScheduler
    // Select the scheduler only while constructing these threads. Each VT keeps
    // its scheduler, so the rest of the suite can use the original default.
    val (owner, waker, spinner) = try {
      VirtualThread.setDefaultScheduler(scheduler)
      val owner = Thread
        .ofVirtual()
        .unstarted(() => {
          while (!wakeRequested.get()) LockSupport.park()
          ownerRan.set(true)
        })
      val waker = Thread
        .ofVirtual()
        .unstarted(() => {
          val current = Thread.currentThread()
          wakeRequested.set(true)
          LockSupport.unpark(owner)
          wakerIdentityRestored.set(Thread.currentThread() eq current)
        })
      val spinner = Thread
        .ofVirtual()
        .unstarted(() => {
          val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
          while (!ownerRan.get() && System.nanoTime() < deadline)
            Thread.onSpinWait()
          spinnerGaveUp.set(!ownerRan.get())
        })
      (owner, waker, spinner)
    } finally VirtualThread.setDefaultScheduler(previous)

    try {
      owner.start()
      // FIFO submission from this platform thread guarantees that the owner
      // has completely unmounted before this task occupies the only carrier.
      pool.execute(() => {
        workerBlocked.countDown()
        releaseWorker.await()
      })
      assertTrue(workerBlocked.await(10, TimeUnit.SECONDS))
      assertEquals(Thread.State.WAITING, owner.getState())
      waker.start()
      spinner.start()
      releaseWorker.countDown()
      waker.join(10000)
      spinner.join(10000)
      owner.join(10000)
      assertFalse("waker did not terminate", waker.isAlive())
      assertFalse("spinner did not terminate", spinner.isAlive())
      assertFalse("owner did not terminate", owner.isAlive())
      assertTrue("waker identity was not restored", wakerIdentityRestored.get())
      assertFalse(
        "queued spinner occupied the carrier before the owner",
        spinnerGaveUp.get()
      )
    } finally {
      ownerRan.set(true)
      wakeRequested.set(true)
      releaseWorker.countDown()
      LockSupport.unpark(owner)
      owner.join(10000)
      waker.join(10000)
      spinner.join(10000)
      pool.shutdown()
      assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS))
    }
  }
}
