package scala.scalanative.runtime

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

class NativeThreadUnparkTest {
  private final val TimeoutSeconds = 30L

  // Monitor exit unparks its successor. If unpark took the successor's
  // monitor, a chain of contended successors recursed until StackOverflowError.
  @Test def unparkDoesNotTakeTargetMonitor(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val target = new AtomicReference[NativeThread]()
    val captured = new CountDownLatch(1)
    val release = new CountDownLatch(1)
    val parker = new Thread(() => {
      target.set(NativeThread.currentNativeThread)
      captured.countDown()
      release.await()
    })
    parker.setDaemon(true)
    parker.start()
    assertTrue(captured.await(TimeoutSeconds, TimeUnit.SECONDS))

    val unparked = new CountDownLatch(1)
    val unparker = new Thread(() => {
      LockSupport.unpark(parker)
      unparked.countDown()
    })
    unparker.setDaemon(true)
    try
      target.get().synchronized {
        unparker.start()
        assertTrue(
          "unpark blocked on the target thread's monitor",
          unparked.await(TimeoutSeconds, TimeUnit.SECONDS)
        )
      }
    finally release.countDown()
    unparker.join()
    parker.join()
  }
}
