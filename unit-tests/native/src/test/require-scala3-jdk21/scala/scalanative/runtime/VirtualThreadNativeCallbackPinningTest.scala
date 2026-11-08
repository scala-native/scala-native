package scala.scalanative.runtime

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.runtime.NativeCallbackBlocking

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.unsafe.testlib

import NativeCallbackBlocking.Mode

/** A VT blocking inside a native callback must keep its carrier. */
class VirtualThreadNativeCallbackPinningTest {
  @Test def timedParkInsideCallbackKeepsCarrier(): Unit =
    check(Mode.TimedPark)

  @Test def parkInsideCallbackKeepsCarrier(): Unit =
    check(Mode.Park)

  @Test def monitorWaitInsideCallbackKeepsCarrier(): Unit =
    check(Mode.MonitorWait)

  @Test def monitorWaitInsideCallbackKeepsParkPermit(): Unit = {
    check(Mode.MonitorWaitKeepsPermit)
    assertTrue(
      "wait consumed the park permit",
      NativeCallbackBlocking.permitKept
    )
  }

  private def check(mode: Int): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()
    NativeCallbackBlocking.reset(mode)
    val completed = new CountDownLatch(1)
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
      ): VirtualThreadScheduler.Cancellable = {
        val timer = new Thread(() => {
          unit.sleep(delay)
          execute(task)
        })
        timer.setDaemon(true)
        timer.start()
        new VirtualThreadScheduler.Cancellable {
          def cancel(): Boolean = false
          def isDone(): Boolean = !timer.isAlive()
        }
      }
    }

    val previous = VirtualThread.defaultScheduler
    val vt =
      try {
        VirtualThread.setDefaultScheduler(scheduler)
        Thread
          .ofVirtual()
          .unstarted(() => {
            assertEquals(1, testlib.exec0(NativeCallbackBlocking.callback))
            completed.countDown()
          })
      } finally VirtualThread.setDefaultScheduler(previous)

    vt.start()
    if (mode != Mode.TimedPark) {
      // Wake the VT only once it is blocked inside the callback.
      val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
      while (vt.getState() != Thread.State.WAITING &&
          System.nanoTime() < deadline) Thread.sleep(1)
      assertEquals(Thread.State.WAITING, vt.getState())
      NativeCallbackBlocking.wake(vt)
    }
    assertTrue(
      "virtual thread did not leave the callback",
      completed.await(10, TimeUnit.SECONDS)
    )
    assertFalse(
      "callback resumed on another carrier",
      NativeCallbackBlocking.carrierChanged
    )
  }
}
