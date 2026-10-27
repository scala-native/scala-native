package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

/** A virtual thread waiting to enter a monitor must stay `BLOCKED`, even after
 *  a nested contended monitor enter on its carrier finishes.
 */
class VirtualThreadMonitorEnterStateTest {
  private val Timeout = 10L

  @Test def nestedMonitorExitDoesNotClearBlockedState(): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()

    val gate = new Object
    val lock = new Object
    val releaseGate = new AtomicBoolean(false)
    val enteredCompensation = new AtomicInteger(0)
    val compensationFinished = new CountDownLatch(1)
    val gateHeld = new CountDownLatch(1)

    val helper = new Thread(() => {
      gate.synchronized {
        gateHeld.countDown()
        while (!releaseGate.get()) Thread.sleep(1)
      }
    })
    helper.setDaemon(true)
    helper.start()
    assertTrue(gateHeld.await(Timeout, TimeUnit.SECONDS))

    val scheduler = new VirtualThreadScheduler {
      def execute(task: Runnable): Unit = {
        val carrier = new Thread(task)
        carrier.setDaemon(true)
        carrier.start()
      }

      def schedule(
          task: Runnable,
          delay: Long,
          unit: TimeUnit
      ): VirtualThreadScheduler.Cancellable =
        throw new UnsupportedOperationException()

      override def beginCarrierCompensatedBlock(carrier: Thread): Boolean = {
        // Only the first call enters `gate`.
        if (enteredCompensation.getAndIncrement() == 0) {
          gate.synchronized {}
          compensationFinished.countDown()
        }
        false
      }
    }

    val previous = VirtualThread.defaultScheduler
    val vt =
      try {
        VirtualThread.setDefaultScheduler(scheduler)
        Thread
          .ofVirtual()
          .unstarted(() => {
            lock.synchronized {}
          })
      } finally VirtualThread.setDefaultScheduler(previous)

    try {
      lock.synchronized {
        vt.start()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(Timeout)
        while (enteredCompensation.get() == 0 && System.nanoTime() < deadline)
          Thread.sleep(1)
        assertTrue(
          "virtual thread never yielded into carrier compensation",
          enteredCompensation.get() > 0
        )
        // Let the gate enter reach its slow path.
        Thread.sleep(50)
        releaseGate.set(true)
        assertTrue(
          "nested monitor enter did not finish",
          compensationFinished.await(Timeout, TimeUnit.SECONDS)
        )
        val stateDeadline =
          System.nanoTime() + TimeUnit.SECONDS.toNanos(Timeout)
        var state = vt.getState()
        while (state != Thread.State.BLOCKED &&
            System.nanoTime() < stateDeadline) {
          Thread.sleep(10)
          state = vt.getState()
        }
        assertEquals(Thread.State.BLOCKED, state)
      }
    } finally {
      releaseGate.set(true)
      vt.join(TimeUnit.SECONDS.toMillis(Timeout))
      helper.join(TimeUnit.SECONDS.toMillis(Timeout))
    }
  }
}
