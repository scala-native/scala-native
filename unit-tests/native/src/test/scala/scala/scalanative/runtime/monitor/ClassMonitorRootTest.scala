package scala.scalanative.runtime.monitor

import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.MemoryLayout

// Once the monitor of a class without instances is inflated, the class's lock
// word, outside the heap, alone keeps the monitor
class ClassMonitorRootTest {
  import ClassMonitorRootTest._

  @Test def inflatedMonitorOfAStringLiteralSurvivesCollections(): Unit =
    assertStaticMonitorSurvives("static string monitor root")

  @Test def inflatedMonitorOfAConstantSingletonSurvivesCollections(): Unit =
    assertStaticMonitorSurvives(StaticSingleton)

  @Test def concurrentClassInflationSurvivesCollections(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    val classes =
      Seq(classOf[NeverInstantiated], classOf[AlsoNeverInstantiated])
    val monitors = new Array[WeakReference[ObjectMonitor]](classes.size)
    val start = new CountDownLatch(1)
    val failure = new AtomicReference[Throwable]()
    val workers = classes.indices.map { index =>
      worker(failure) {
        assertTrue(
          "workers were not released",
          start.await(10, TimeUnit.SECONDS)
        )
        val cls = classes(index)
        inflate(cls)
        monitors(index) = monitorOf(cls)
      }
    }
    workers.foreach(_.start())
    start.countDown()
    awaitWorkers(workers, failure)
    for (_ <- 0 until 5) System.gc()
    classes.indices.foreach { index =>
      assertNotNull("the class monitor was collected", monitors(index).get())
      assertSame(monitors(index).get(), currentMonitor(classes(index)))
    }
  }

  @Test def inflatedMonitorOfAClassSurvivesCollections(): Unit =
    // This class must have no allocated instances that could keep its monitor.
    assertStaticMonitorSurvives(classOf[NeverInstantiated])
}

object ClassMonitorRootTest {
  private def worker(
      failure: AtomicReference[Throwable]
  )(body: => Unit): Thread = {
    val thread = new Thread(() => {
      try body
      catch { case error: Throwable => failure.compareAndSet(null, error) }
    })
    // A deadlocked worker must not keep the test process alive after failure.
    thread.setDaemon(true)
    thread
  }

  private def awaitWorkers(
      workers: Seq[Thread],
      failure: AtomicReference[Throwable]
  ): Unit = {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20)
    workers.foreach { thread =>
      val remaining = deadline - System.nanoTime()
      if (remaining > 0) {
        thread.join(math.max(1L, TimeUnit.NANOSECONDS.toMillis(remaining)))
      }
      assertFalse(
        "monitor worker did not finish before the deadline",
        thread.isAlive()
      )
    }
    val error = failure.get()
    if (error != null) throw new AssertionError("monitor worker failed", error)
  }

  private[monitor] def assertStaticMonitorSurvives(obj: AnyRef): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    // End the worker before collecting so its stack and registers cannot keep
    // the monitor alive instead of the static object's lock word.
    var monitor: WeakReference[ObjectMonitor] = null
    val failure = new AtomicReference[Throwable]()
    val thread = worker(failure) {
      inflate(obj)
      monitor = monitorOf(obj)
    }
    thread.start()
    awaitWorkers(Seq(thread), failure)
    for (_ <- 0 until 5) {
      Seq.fill(1000)(new Array[Byte](64))
      System.gc()
    }
    assertNotNull("the static object's monitor was collected", monitor.get())
    assertSame(monitor.get(), currentMonitor(obj))
    obj.synchronized(obj.notifyAll())
  }

  private class NeverInstantiated
  private class AlsoNeverInstantiated
  private object StaticSingleton

  private def lockWord(obj: AnyRef): LockWord =
    loadRawPtr(
      elemRawPtr(
        castObjectToRawPtr(obj),
        castIntToRawSize(MemoryLayout.Object.LockWordOffset)
      )
    )

  private def currentMonitor(obj: AnyRef): ObjectMonitor = {
    val word = lockWord(obj)
    assertTrue("the monitor is inflated", word.isInflated)
    word.getObjectMonitor
  }

  // Waiting inflates the monitor
  @noinline private def inflate(obj: AnyRef): Unit =
    obj.synchronized(obj.wait(1))

  @noinline private def monitorOf(obj: AnyRef): WeakReference[ObjectMonitor] =
    new WeakReference(currentMonitor(obj))
}
