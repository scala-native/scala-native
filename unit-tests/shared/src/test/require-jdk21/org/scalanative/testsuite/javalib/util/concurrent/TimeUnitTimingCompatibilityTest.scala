package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.*
import org.junit.Assert.*

import scala.scalanative.junit.utils.AssumesHelper

object TimeUnitTimingCompatibilityTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class TimeUnitTimingCompatibilityTest {
  private val Timeout = 5000L

  @Test def timedJoinPreservesSubMillisecondTimeout(): Unit = {
    val releaseTarget = new CountDownLatch(1)
    val targetStarted = new CountDownLatch(1)
    val target = Thread
      .ofPlatform()
      .start(() => {
        targetStarted.countDown()
        releaseTarget.await(Timeout, TimeUnit.MILLISECONDS)
      })
    assertTrue(targetStarted.await(Timeout, TimeUnit.MILLISECONDS))

    val aliveAfterJoin = new AtomicBoolean(false)
    val joinError = new AtomicReference[Throwable](null)
    val joinFinished = new CountDownLatch(1)
    val joiner = Thread
      .ofPlatform()
      .start(() => {
        try {
          TimeUnit.MICROSECONDS.timedJoin(target, 500)
          aliveAfterJoin.set(target.isAlive)
        } catch {
          case t: Throwable =>
            joinError.set(t)
        } finally {
          joinFinished.countDown()
        }
      })

    val finishedInTime = joinFinished.await(500, TimeUnit.MILLISECONDS)
    releaseTarget.countDown()
    joiner.join(Timeout)
    target.join(Timeout)

    assertNull("timedJoin should not throw", joinError.get())
    assertTrue(
      "sub-millisecond timedJoin should time out instead of waiting indefinitely",
      finishedInTime
    )
    assertTrue(
      "target thread should still be alive when timedJoin returns",
      aliveAfterJoin.get()
    )
  }

  @Test def timedWaitPreservesSubMillisecondTimeout(): Unit = {
    val lock = new Object
    val waitError = new AtomicReference[Throwable](null)
    val waitFinished = new CountDownLatch(1)
    val timedOutNaturally = new AtomicBoolean(false)

    val waiter = Thread
      .ofPlatform()
      .start(() => {
        try {
          lock.synchronized {
            TimeUnit.MICROSECONDS.timedWait(lock, 500)
          }
          timedOutNaturally.set(true)
        } catch {
          case t: Throwable =>
            waitError.set(t)
        } finally {
          waitFinished.countDown()
        }
      })

    val finishedInTime = waitFinished.await(500, TimeUnit.MILLISECONDS)
    if (!finishedInTime) {
      lock.synchronized {
        lock.notifyAll()
      }
      waiter.interrupt()
    }
    waiter.join(Timeout)

    assertTrue(
      "sub-millisecond timedWait should time out instead of waiting indefinitely",
      finishedInTime
    )
    assertTrue(
      "timedWait should return normally when the timeout elapses",
      timedOutNaturally.get()
    )
    assertNull("timedWait should not throw", waitError.get())
  }
}
