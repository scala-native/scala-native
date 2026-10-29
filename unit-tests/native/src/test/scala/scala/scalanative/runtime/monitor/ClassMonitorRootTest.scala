package scala.scalanative.runtime.monitor

import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch

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

  @Test def inflatedMonitorOfAStringLiteralSurvivesCollections(): Unit = {
    val string = "static string monitor root"
    string.synchronized(())
    assertStaticMonitorSurvives(string)
  }

  @Test def inflatedMonitorOfAConstantSingletonSurvivesCollections(): Unit =
    assertStaticMonitorSurvives(StaticSingleton)

  @Test def concurrentClassInflationSurvivesCollections(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    val classes =
      Seq(classOf[NeverInstantiated], classOf[AlsoNeverInstantiated])
    val monitors = new Array[WeakReference[ObjectMonitor]](classes.size)
    val start = new CountDownLatch(1)
    val workers = classes.indices.map { index =>
      new Thread(() => {
        start.await()
        val cls = classes(index)
        inflate(cls)
        monitors(index) = monitorOf(cls)
      })
    }
    workers.foreach(_.start())
    start.countDown()
    workers.foreach(_.join())
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
  private[monitor] def assertStaticMonitorSurvives(obj: AnyRef): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    // End the worker before collecting so its stack and registers cannot keep
    // the monitor alive instead of the static object's lock word.
    var monitor: WeakReference[ObjectMonitor] = null
    val thread = new Thread(() => {
      inflate(obj)
      monitor = monitorOf(obj)
    })
    thread.start()
    thread.join()
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
