package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

class VirtualThreadParkLockTest {
  @Test def compensationMayUnparkTheParkingCarrier(): Unit =
    checkPark(timed = false, unparkOnBegin = true)

  @Test def compensationMayUnparkTheTimedParkingCarrier(): Unit =
    checkPark(timed = true, unparkOnBegin = true)

  @Test def compensationEndsAfterTimedParkReleasesTheLock(): Unit =
    checkPark(timed = true, unparkOnBegin = false)

  private def checkPark(timed: Boolean, unparkOnBegin: Boolean): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()
    val armed = new AtomicBoolean(false)
    val beginCalls = new AtomicInteger(0)
    val endCalls = new AtomicInteger(0)
    val completed = new CountDownLatch(1)
    val timeoutNanos = TimeUnit.MILLISECONDS.toNanos(10)
    val scheduler = new VirtualThreadScheduler {
      def execute(task: Runnable): Unit = {
        val carrier = new Thread(task)
        // A regression must fail without keeping the test process alive.
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
        if (armed.get() && beginCalls.incrementAndGet() == 1) {
          if (unparkOnBegin) LockSupport.unpark(carrier)
          true
        } else false
      }

      override def endCarrierCompensatedBlock(
          carrier: Thread,
          attempted: Boolean
      ): Unit = {
        if (attempted) {
          endCalls.incrementAndGet()
          // end must also run after the carrier has released its park mutex.
          LockSupport.unpark(carrier)
        }
      }
    }

    val previous = VirtualThread.defaultScheduler
    val vt = try {
      VirtualThread.setDefaultScheduler(scheduler)
      Thread
        .ofVirtual()
        .unstarted(() => {
          val native = NativeThread.currentNativeThread
          // Drain any startup permit and initialize the timed-park path.
          native.parkNanos(1L)
          // There are no blocking calls between arming the hook and native park.
          // Use the carrier's park directly to avoid VT suspension and stack walks.
          armed.set(true)
          if (timed) native.parkNanos(timeoutNanos)
          else native.park()
          armed.set(false)
          completed.countDown()
        })
    } finally VirtualThread.setDefaultScheduler(previous)

    vt.start()
    assertTrue(
      "carrier stuck in park: compensation could not unpark it",
      completed.await(10, TimeUnit.SECONDS)
    )
    assertEquals(
      "park must start compensation exactly once",
      1,
      beginCalls.get()
    )
    assertEquals(
      "park must finish compensation exactly once",
      1,
      endCalls.get()
    )
  }
}
