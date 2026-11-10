package scala.scalanative.runtime.monitor

import java.util.concurrent.CountDownLatch

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo

// Issue #3594: a thread re-entering a monitor right after exiting it must not
// starve another thread waiting to enter it.
class MonitorStarvationTest {

  @Test def waiterOnThinMonitorIsNotStarved(): Unit =
    assertWaiterEnters(new Object)

  @Test def waiterOnInflatedMonitorIsNotStarved(): Unit = {
    val lock = new Object
    lock.synchronized(lock.wait(1))
    assertWaiterEnters(lock)
  }

  private def busyWait(nanos: Long): Unit = {
    val until = System.nanoTime() + nanos
    while (System.nanoTime() < until) ()
  }

  private def assertWaiterEnters(lock: AnyRef): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    @volatile var done = false
    @volatile var entries = 0
    val ownerRunning = new CountDownLatch(1)
    val owner = new Thread(() =>
      while (!done) lock.synchronized {
        ownerRunning.countDown()
        busyWait(100000L)
      }
    )
    owner.start()
    ownerRunning.await()
    val waiter = new Thread(() => {
      val deadline = System.nanoTime() + 1000000000L
      while (System.nanoTime() < deadline) lock.synchronized { entries += 1 }
    })
    waiter.start()
    waiter.join()
    done = true
    owner.join()
    // A starving-free monitor lets the waiter in every few milliseconds.
    assertTrue(s"waiter entered only $entries times in 1s", entries >= 100)
  }
}
