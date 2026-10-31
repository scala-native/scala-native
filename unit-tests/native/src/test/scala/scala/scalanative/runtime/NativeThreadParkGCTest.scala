package scala.scalanative.runtime

import java.lang.ref.{ReferenceQueue, WeakReference}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo

class NativeThreadParkGCTest {
  // A collection walks the heap shared with other tests in this process, which
  // takes hundreds of milliseconds per run under emulation. Bound the time
  // spent collecting, so that the timeout below only detects deadlocks.
  private final val CollectionBudgetNanos = 5000000000L
  private final val CompletionTimeoutSeconds = 60L

  @Test def parkWhileProcessingWeakReferences(): Unit = checkPark(timed = false)

  @Test def timedParkWhileProcessingWeakReferences(): Unit =
    checkPark(timed = true)

  private def checkPark(timed: Boolean): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    assumeTrue("weak references", LinktimeInfo.isWeakReferenceSupported)

    val start = new CountDownLatch(1)
    val completed = new CountDownLatch(3)
    val stop = new AtomicBoolean(false)
    val parkingDone = new AtomicBoolean(false)
    val collectionsDone = new AtomicBoolean(false)
    val failure = new AtomicReference[java.lang.Throwable]()
    val queue = new ReferenceQueue[AnyRef]()

    def worker(name: String)(body: => Unit): Thread = {
      val thread = new Thread(() => {
        try {
          start.await()
          body
        } catch {
          case error: java.lang.Throwable =>
            failure.compareAndSet(null, error)
            ()
        } finally completed.countDown()
      })
      // A parking regression must not keep the native test process alive.
      thread.setDaemon(true)
      thread.setName(name)
      thread
    }

    val parker = worker("GC parking regression") {
      try {
        var iteration = 0
        while (!stop.get() && (iteration < 512 || !collectionsDone.get())) {
          if (timed) LockSupport.parkNanos(1000000L)
          else LockSupport.park()
          iteration += 1
        }
      } finally parkingDone.set(true)
    }

    val unparker = worker("GC unparking regression") {
      try {
        while (!stop.get() && !parkingDone.get()) {
          LockSupport.unpark(parker)
          Thread.`yield`()
        }
      } finally LockSupport.unpark(parker)
    }

    val collector = worker("GC weak-reference regression") {
      try {
        val references = new scala.Array[WeakReference[AnyRef]](64)
        val deadline = System.nanoTime() + CollectionBudgetNanos
        var collection = 0
        while (collection < 32 && !stop.get() &&
            (collection == 0 || System.nanoTime() - deadline < 0)) {
          var index = 0
          while (index < references.length) {
            references(index) = newReference(queue)
            index += 1
          }
          GC.collect()
          while (queue.poll() != null) {}
          collection += 1
        }
      } finally collectionsDone.set(true)
    }

    parker.start()
    unparker.start()
    collector.start()
    start.countDown()
    try {
      assertTrue(
        "park/unpark and weak-reference processing must complete",
        completed.await(CompletionTimeoutSeconds, TimeUnit.SECONDS)
      )
      val error = failure.get()
      if (error != null) throw error
    } finally stop.set(true)
  }

  // Leave the referent's allocating frame before requesting a collection.
  @noinline private def newReference(
      queue: ReferenceQueue[AnyRef]
  ): WeakReference[AnyRef] = new WeakReference[AnyRef](new Object(), queue)
}
