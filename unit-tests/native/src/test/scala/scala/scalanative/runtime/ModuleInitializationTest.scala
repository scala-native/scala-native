package scala.scalanative.runtime

import java.lang.Runtime
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

object TestModule {
  val slowInitField = {
    assertNotNull(this)
    assertNotNull(this.getClass())
    Thread.sleep(1000)
    42
  }
}

object ParkPermitTestControl {
  val initStarted = new CountDownLatch(1)
}

object ParkPermitTestModule {
  ParkPermitTestControl.initStarted.countDown()
  Thread.sleep(200)
  val value = 42
}

class ModuleInitializationTest {
  // Issue #4366: the wait may run nested inside a park, e.g. when the park
  // path loads a module, so it must not consume the outer park permit.
  @Test def waitingForModuleKeepsParkPermit(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val owner = new Thread(() => assertEquals(42, ParkPermitTestModule.value))
    owner.start()
    assertTrue(ParkPermitTestControl.initStarted.await(10, TimeUnit.SECONDS))

    LockSupport.unpark(Thread.currentThread())
    assertEquals(42, ParkPermitTestModule.value)
    val parkStart = System.nanoTime()
    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10))
    val parkedNanos = System.nanoTime() - parkStart
    owner.join()
    assertTrue(
      s"park permit lost, parked for ${parkedNanos / 1000000} ms",
      parkedNanos < TimeUnit.SECONDS.toNanos(5)
    )
  }

  @Test def initializeFromMultipleThreads(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val latch = new java.util.concurrent.CountDownLatch(1)
    val threads = Seq.fill(4) {
      new Thread(() => {
        latch.await()
        assertEquals(42, TestModule.slowInitField)
      })
    }
    threads.foreach(_.start())
    Thread.sleep(100)
    latch.countDown()
    threads.foreach(_.join())
    assertEquals(42, TestModule.slowInitField)
  }
}
