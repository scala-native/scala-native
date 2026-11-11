package scala.scalanative.runtime.gc

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

// Threads stopped for a collection must all be resumed: many threads contend
// for a monitor (parking and unparking each other) and allocate while
// collections keep stopping them.
class SafepointWakeupTest {
  private final val Threads = 32
  private final val CollectionBudgetNanos = TimeUnit.SECONDS.toNanos(3)
  private final val CompletionTimeoutNanos = TimeUnit.SECONDS.toNanos(60)

  @Test def everyStoppedThreadIsResumed(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    val stop = new AtomicBoolean(false)
    val failure = new AtomicReference[Throwable]()
    val workers = new Array[Thread](Threads)
    val lock = new Object
    @volatile var shared = 0L

    def worker(body: => Unit): Thread = {
      val thread = new Thread(() => {
        try body
        catch {
          case error: Throwable =>
            failure.compareAndSet(null, error)
            ()
        }
      })
      // A thread left stopped must not keep the test process alive.
      thread.setDaemon(true)
      thread
    }

    for (i <- 0 until Threads) workers(i) = worker {
      var sink: AnyRef = null
      while (!stop.get()) {
        lock.synchronized {
          var j = 0
          while (j < 1000) { shared = shared * 31 + j; j += 1 }
        }
        sink = new Array[Byte](64)
      }
    }
    val collector = worker {
      val deadline = System.nanoTime() + CollectionBudgetNanos
      try while (System.nanoTime() < deadline) System.gc()
      finally stop.set(true)
    }
    val all = workers :+ collector
    all.foreach(_.start())

    val deadline = System.nanoTime() + CompletionTimeoutNanos
    all.foreach { thread =>
      val remaining = deadline - System.nanoTime()
      if (remaining > 0)
        thread.join(TimeUnit.NANOSECONDS.toMillis(remaining).max(1L))
      assertFalse(
        "a thread was not resumed after a collection",
        thread.isAlive()
      )
    }
    val error = failure.get()
    if (error != null) throw new AssertionError("worker failed", error)
  }
}
