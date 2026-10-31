package scala.scalanative.runtime

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

object MonitorTestControl {
  val constructed = new CountDownLatch(1)
  val blockingEntered = new CountDownLatch(1)
  val blockingProceed = new CountDownLatch(1)
}

object ContendedMonitorModule {
  MonitorTestControl.constructed.countDown()
  val value = 42
}

object BlockingModule {
  MonitorTestControl.blockingEntered.countDown()
  MonitorTestControl.blockingProceed.await()
  val value = 42
}

private final class BlockingModuleWaiter extends Thread {
  @volatile var value = 0
  @volatile var interruptStatus = false
  @volatile var failure: java.lang.Throwable = null

  override def run(): Unit =
    try {
      value = BlockingModule.value
      interruptStatus = Thread.currentThread().isInterrupted()
    } catch {
      case t: java.lang.Throwable => failure = t
    }
}

class ModuleInitializationMonitorTest {
  // `classOf` does not initialize the module, so this forces the contention.
  @Test def constructorDoesNotNeedClassMonitor(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    val constructed = MonitorTestControl.constructed

    val owner = new Thread(() => assertEquals(42, ContendedMonitorModule.value))
    classOf[ContendedMonitorModule.type].synchronized {
      owner.start()
      assertTrue(
        "module constructor blocked on its contended class monitor",
        constructed.await(10, TimeUnit.SECONDS)
      )
    }
    owner.join()
    assertEquals(42, ContendedMonitorModule.value)
  }

  @Test def waitingForModuleIsNotAnInterruptionPoint(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    val control = MonitorTestControl
    val entered = control.blockingEntered
    val proceed = control.blockingProceed

    val owner = new Thread(() => assertEquals(42, BlockingModule.value))
    owner.start()
    assertTrue(entered.await(10, TimeUnit.SECONDS))

    val waiter = new BlockingModuleWaiter
    waiter.start()
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
    while (waiter.getState() != Thread.State.WAITING &&
        System.nanoTime() < deadline) Thread.sleep(1)
    assertEquals(Thread.State.WAITING, waiter.getState())

    waiter.interrupt()
    Thread.sleep(50)
    proceed.countDown()
    owner.join()
    waiter.join()

    assertNull(waiter.failure)
    assertEquals(42, waiter.value)
    assertTrue("interrupt status should be restored", waiter.interruptStatus)
  }
}
