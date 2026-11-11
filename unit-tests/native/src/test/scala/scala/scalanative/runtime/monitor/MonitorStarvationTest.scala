package scala.scalanative.runtime.monitor

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.MemoryLayout

// Issue #3594: a thread re-entering a monitor right after exiting it must not
// starve another thread waiting to enter it.
class MonitorStarvationTest {
  import MonitorStarvationTest._

  @Test def waiterOnThinMonitorIsNotStarved(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    val lock = new Object
    val failure = new AtomicReference[Throwable]()
    val holding = new CountDownLatch(1)
    val release = new CountDownLatch(1)
    @volatile var done = false
    @volatile var inflatedAfterFirstExit = false
    val owner = worker(failure) {
      lock.synchronized {
        holding.countDown()
        assertTrue("owner not released", release.await(20, TimeUnit.SECONDS))
      }
      inflatedAfterFirstExit = lockWord(lock).isInflated
      reenterUntil(lock, done)
    }
    owner.start()
    val entries = new Entries
    try {
      assertTrue("owner did not lock", holding.await(20, TimeUnit.SECONDS))
      assertFalse("monitor already inflated", lockWord(lock).isInflated)

      val waiter = worker(failure)(enterRepeatedly(lock, entries))
      waiter.start()
      // The waiter asks the thin lock owner to inflate the monitor on exit
      val deadline = System.nanoTime() + DeadlineNanos
      while (!lockWord(lock).isContended && System.nanoTime() < deadline)
        Thread.sleep(1)
      assertTrue(
        "thin lock was not flagged contended",
        lockWord(lock).isContended
      )
      release.countDown()
      awaitWorkers(Seq(waiter), failure)
    } finally {
      release.countDown()
      done = true
    }
    awaitWorkers(Seq(owner), failure)
    assertTrue(
      "contended thin lock was not inflated by its owner on exit",
      inflatedAfterFirstExit
    )
    entries.assertEnough()
  }

  @Test def waiterOnInflatedMonitorIsNotStarved(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    val lock = new Object
    lock.synchronized(lock.wait(1))
    assertTrue("monitor not inflated", lockWord(lock).isInflated)
    val failure = new AtomicReference[Throwable]()
    val ownerRunning = new CountDownLatch(1)
    @volatile var done = false
    val owner = worker(failure) {
      lock.synchronized(ownerRunning.countDown())
      reenterUntil(lock, done)
    }
    owner.start()
    val entries = new Entries
    try {
      assertTrue(
        "owner did not start",
        ownerRunning.await(20, TimeUnit.SECONDS)
      )
      val waiter = worker(failure)(enterRepeatedly(lock, entries))
      waiter.start()
      awaitWorkers(Seq(waiter), failure)
    } finally done = true
    awaitWorkers(Seq(owner), failure)
    entries.assertEnough()
  }
}

object MonitorStarvationTest {
  // Generous for coarse timers (Windows parks round up to ~15.6ms ticks).
  final val ExpectedEntries = 20
  final val DeadlineNanos = TimeUnit.SECONDS.toNanos(10)

  final class Entries {
    @volatile var count = 0
    def assertEnough(): Unit = assertTrue(
      s"waiter entered only $count of $ExpectedEntries times within 10s",
      count >= ExpectedEntries
    )
  }

  private def busyWait(nanos: Long): Unit = {
    val until = System.nanoTime() + nanos
    while (System.nanoTime() < until) ()
  }

  // The owner re-enters right after each exit, keeping the monitor mostly held.
  private def reenterUntil(lock: AnyRef, done: => Boolean): Unit =
    while (!done) lock.synchronized(busyWait(100000L))

  private def enterRepeatedly(lock: AnyRef, entries: Entries): Unit = {
    val deadline = System.nanoTime() + DeadlineNanos
    while (entries.count < ExpectedEntries && System.nanoTime() < deadline)
      lock.synchronized(entries.count += 1)
  }

  private def worker(failure: AtomicReference[Throwable])(
      body: => Unit
  ): Thread = {
    val thread = new Thread(() => {
      try body
      catch {
        case error: Throwable =>
          failure.compareAndSet(null, error)
          ()
      }
    })
    // A deadlocked worker must not keep the test process alive.
    thread.setDaemon(true)
    thread
  }

  private def awaitWorkers(
      workers: Seq[Thread],
      failure: AtomicReference[Throwable]
  ): Unit = {
    workers.foreach { thread =>
      thread.join(TimeUnit.SECONDS.toMillis(20))
      assertFalse("monitor worker did not finish in time", thread.isAlive())
    }
    val error = failure.get()
    if (error != null) throw new AssertionError("monitor worker failed", error)
  }

  private def lockWord(obj: AnyRef): LockWord =
    loadRawPtr(
      elemRawPtr(
        castObjectToRawPtr(obj),
        castIntToRawSize(MemoryLayout.Object.LockWordOffset)
      )
    )
}
