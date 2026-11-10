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

  // More lock words than one chunk of the runtime's list of static lock words
  @Test def inflatedMonitorsOfManyStaticObjectsSurviveCollections(): Unit =
    assertStaticMonitorsSurvive(
      ManyStringLiterals :+ classOf[NeverInstantiated] :+ classOf[
        AlsoNeverInstantiated
      ]
    )
}

object ClassMonitorRootTest {
  private def worker(
      failure: AtomicReference[Throwable]
  )(body: => Unit): Thread = {
    val thread = new Thread(() => {
      try body
      catch {
        case error: Throwable =>
          failure.compareAndSet(null, error)
          ()
      }
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

  private[monitor] def assertStaticMonitorSurvives(obj: AnyRef): Unit =
    assertStaticMonitorsSurvive(Seq(obj))

  private def assertStaticMonitorsSurvive(objs: Seq[AnyRef]): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    // End the worker before collecting so its stack and registers cannot keep
    // the monitors alive instead of the static objects' lock words.
    val monitors = new Array[WeakReference[ObjectMonitor]](objs.size)
    val failure = new AtomicReference[Throwable]()
    val thread = worker(failure) {
      objs.zipWithIndex.foreach {
        case (obj, index) =>
          inflate(obj)
          monitors(index) = monitorOf(obj)
      }
    }
    thread.start()
    awaitWorkers(Seq(thread), failure)
    for (_ <- 0 until 5) {
      Seq.fill(1000)(new Array[Byte](64))
      System.gc()
    }
    objs.zipWithIndex.foreach {
      case (obj, index) =>
        assertNotNull(
          s"the monitor of static object $index was collected",
          monitors(index).get()
        )
        assertSame(monitors(index).get(), currentMonitor(obj))
        obj.synchronized(obj.notifyAll())
    }
  }

  private val ManyStringLiterals: Seq[AnyRef] = Seq(
    "static monitor root 0",
    "static monitor root 1",
    "static monitor root 2",
    "static monitor root 3",
    "static monitor root 4",
    "static monitor root 5",
    "static monitor root 6",
    "static monitor root 7",
    "static monitor root 8",
    "static monitor root 9",
    "static monitor root 10",
    "static monitor root 11",
    "static monitor root 12",
    "static monitor root 13",
    "static monitor root 14",
    "static monitor root 15",
    "static monitor root 16",
    "static monitor root 17",
    "static monitor root 18",
    "static monitor root 19",
    "static monitor root 20",
    "static monitor root 21",
    "static monitor root 22",
    "static monitor root 23",
    "static monitor root 24",
    "static monitor root 25",
    "static monitor root 26",
    "static monitor root 27",
    "static monitor root 28",
    "static monitor root 29",
    "static monitor root 30",
    "static monitor root 31",
    "static monitor root 32",
    "static monitor root 33",
    "static monitor root 34",
    "static monitor root 35",
    "static monitor root 36",
    "static monitor root 37",
    "static monitor root 38",
    "static monitor root 39",
    "static monitor root 40",
    "static monitor root 41",
    "static monitor root 42",
    "static monitor root 43",
    "static monitor root 44",
    "static monitor root 45",
    "static monitor root 46",
    "static monitor root 47",
    "static monitor root 48",
    "static monitor root 49",
    "static monitor root 50",
    "static monitor root 51",
    "static monitor root 52",
    "static monitor root 53",
    "static monitor root 54",
    "static monitor root 55",
    "static monitor root 56",
    "static monitor root 57",
    "static monitor root 58",
    "static monitor root 59",
    "static monitor root 60",
    "static monitor root 61",
    "static monitor root 62",
    "static monitor root 63",
    "static monitor root 64",
    "static monitor root 65",
    "static monitor root 66",
    "static monitor root 67",
    "static monitor root 68",
    "static monitor root 69",
    "static monitor root 70",
    "static monitor root 71",
    "static monitor root 72",
    "static monitor root 73",
    "static monitor root 74",
    "static monitor root 75",
    "static monitor root 76",
    "static monitor root 77",
    "static monitor root 78",
    "static monitor root 79"
  )

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
