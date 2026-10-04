package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import scala.scalanative.junit.utils.AssumesHelper

/** Unpark that races the publish-then-park window must resume the waiter.
 *
 *  The waiter CASes itself into an `AtomicReference` and only then parks. The
 *  waker unparks as soon as that publish is visible, including a second unpark
 *  and a `parkNanos` waiter whose timer must still run a continuation left in
 *  `Unparked`.
 */
object VirtualThreadUnparkDuringParkTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadUnparkDuringParkTest {
  private val RoundTimeoutMs = 4000L
  private val Iterations = 120
  private val Registering: AnyRef = "registering"
  private val Released: AnyRef = "released"

  @Test def unparkDuringParkTransitionResumesWaiter(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    // Leave two carriers for the waiter and the spinning waker.
    val fillers = math.max(0, math.min(2, processors - 2))
    var iteration = 1
    while (iteration <= Iterations) {
      assertTrue(
        s"waiter still parked after racy unpark (iteration $iteration, " +
          s"processors $processors)",
        runRound(fillers, timed = iteration % 2 == 0)
      )
      iteration += 1
    }
  }

  private def runRound(fillers: Int, timed: Boolean): Boolean = {
    val slot = new AtomicReference[AnyRef](Registering)
    val finished = new CountDownLatch(1)
    val stopFillers = new AtomicBoolean(false)
    val fillersStarted = new CountDownLatch(fillers)
    val wakerReady = new CountDownLatch(1)

    var f = 0
    while (f < fillers) {
      Thread.ofVirtual().start { () =>
        fillersStarted.countDown()
        while (!stopFillers.get()) Thread.onSpinWait()
      }
      f += 1
    }

    Thread.ofVirtual().start { () =>
      wakerReady.countDown()
      var done = false
      while (!done) {
        slot.get() match {
          case t: Thread =>
            if (slot.compareAndSet(t, Released)) {
              LockSupport.unpark(t)
              // Second unpark: must schedule a continuation already moved
              // out of Parked by the first unpark or by the post-yield path.
              LockSupport.unpark(t)
              done = true
            }
          case Released =>
            done = true
          case _ =>
            Thread.onSpinWait()
        }
      }
    }

    if (!fillersStarted.await(2, TimeUnit.SECONDS) ||
        !wakerReady.await(2, TimeUnit.SECONDS)) {
      stopFillers.set(true)
      return false
    }

    Thread.ofVirtual().start { () =>
      val me = Thread.currentThread()
      if (slot.compareAndSet(Registering, me)) {
        if (timed)
          LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000))
        else
          while (slot.get() eq me)
            LockSupport.park()
      }
      finished.countDown()
    }

    val ok = finished.await(RoundTimeoutMs, TimeUnit.MILLISECONDS)
    stopFillers.set(true)
    ok
  }
}
