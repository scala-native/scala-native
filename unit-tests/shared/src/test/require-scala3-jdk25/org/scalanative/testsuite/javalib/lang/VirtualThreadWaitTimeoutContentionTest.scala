package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.atomic.{
  AtomicBoolean, AtomicInteger, AtomicReference
}
import java.util.concurrent.{
  BrokenBarrierException, CountDownLatch, CyclicBarrier, Executors,
  ThreadLocalRandom, TimeUnit, TimeoutException
}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadWaitTimeoutContentionTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadWaitTimeoutContentionTest {
  private val Timeout = 10000L
  private val WaitMillis = 60000L
  private val RaceRoundTimeout = 3000L
  private val TimedWaitRaceIterations = 30
  private val TimedWaitRaceTimeouts = Array(10L, 25L, 60L)
  private val NotifiedThenTimedOutIterations = 300

  @Test def objectWaitTimeoutCancellationUnderContention(): Unit = {
    val lockCount = 4
    val threadCount = 256
    val locks = Array.fill[Object](lockCount)(new Object)
    val releaseWaiters = new AtomicBoolean(false)
    val finished = new AtomicInteger(0)
    val executor = Executors.newVirtualThreadPerTaskExecutor()

    try {
      for (i <- 0 until threadCount) {
        val lock = locks((i * 17) % lockCount)

        executor.submit[Unit] { () =>
          lock.synchronized {
            if (!releaseWaiters.get()) {
              lock.wait(WaitMillis)
            }
          }
          finished.incrementAndGet()
        }

        lock.synchronized {
          lock.notify()
        }
      }

      val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Timeout)
      while (finished.get() < threadCount && System.nanoTime() < deadline) {
        notifyOnePerLock(locks)
        Thread.sleep(1)
      }

      assertEquals(
        s"all $threadCount waiters should complete without getting stuck on cancelled timers",
        threadCount,
        finished.get()
      )
    } finally {
      releaseWaiters.set(true)
      notifyAllWaiters(locks)
      executor.close()
    }
  }

  @Test def timedWaitNotifyRaceDoesNotStall(): Unit = {
    runTimedWaitRaceStress(notify = true, interrupt = false)
  }

  @Test def timedWaitInterruptRaceDoesNotStall(): Unit = {
    runTimedWaitRaceStress(notify = false, interrupt = true)
  }

  @Test def timedWaitNotifyAndInterruptRaceDoesNotStall(): Unit = {
    runTimedWaitRaceStress(notify = true, interrupt = true)
  }

  @Test def notifiedThenTimedOutSecondWaitRaceBarrierInSynchronizedDoesNotStall()
      : Unit = {
    runNotifiedThenTimedOutRace(
      iterations = NotifiedThenTimedOutIterations,
      barrierInSynchronizedBlock = true
    )
  }

  @Test
  def notifiedThenTimedOutSecondWaitRaceBarrierBeforeSynchronizedDoesNotStall()
      : Unit = {
    runNotifiedThenTimedOutRace(
      iterations = NotifiedThenTimedOutIterations,
      barrierInSynchronizedBlock = false
    )
  }

  private def notifyOnePerLock(locks: Array[Object]): Unit = {
    var i = 0
    while (i < locks.length) {
      locks(i).synchronized {
        locks(i).notify()
      }
      i += 1
    }
  }

  private def notifyAllWaiters(locks: Array[Object]): Unit = {
    var i = 0
    while (i < locks.length) {
      locks(i).synchronized {
        locks(i).notifyAll()
      }
      i += 1
    }
  }

  private def runTimedWaitRaceStress(
      notify: Boolean,
      interrupt: Boolean
  ): Unit = {
    var i = 0
    while (i < TimedWaitRaceIterations) {
      var j = 0
      while (j < TimedWaitRaceTimeouts.length) {
        runTimedWaitRaceRound(
          timeoutMillis = TimedWaitRaceTimeouts(j),
          notify = notify,
          interrupt = interrupt
        )
        j += 1
      }
      i += 1
    }
  }

  private def runTimedWaitRaceRound(
      timeoutMillis: Long,
      notify: Boolean,
      interrupt: Boolean
  ): Unit = {
    val lock = new Object
    val inWait = new CountDownLatch(1)
    val done = new CountDownLatch(1)
    val failure = new AtomicReference[Throwable](null)

    val waiter = Thread.ofVirtual().start { () =>
      try {
        lock.synchronized {
          inWait.countDown()
          lock.wait(timeoutMillis)
        }
      } catch {
        case _: InterruptedException if interrupt => ()
        case ex: InterruptedException             =>
          failure.compareAndSet(
            null,
            new AssertionError(
              "wait should not be interrupted in notify/timeout-only race",
              ex
            )
          )
        case ex: Throwable =>
          failure.compareAndSet(null, ex)
      } finally {
        done.countDown()
      }
    }

    assertTrue(
      s"waiter should start waiting (timeout=$timeoutMillis, notify=$notify, interrupt=$interrupt)",
      inWait.await(Timeout, TimeUnit.MILLISECONDS)
    )

    val notifier =
      if (notify) Some(startTimedNotify(lock, timeoutMillis))
      else None
    val interrupter =
      if (interrupt) Some(startTimedInterrupt(waiter, timeoutMillis))
      else None

    assertTrue(
      s"waiter stuck in timed wait race (timeout=$timeoutMillis, notify=$notify, interrupt=$interrupt)",
      done.await(RaceRoundTimeout, TimeUnit.MILLISECONDS)
    )

    waiter.join(Timeout)
    assertFalse("waiter should terminate after race round", waiter.isAlive())
    notifier.foreach(joinAuxThread(_, "notifier"))
    interrupter.foreach(joinAuxThread(_, "interrupter"))

    val thrown = failure.get()
    if (thrown != null)
      throw new AssertionError("waiter failed in timed wait race", thrown)
  }

  private def startTimedNotify(lock: Object, timeoutMillis: Long): Thread = {
    Thread.ofVirtual().start { () =>
      if (ThreadLocalRandom.current().nextBoolean()) {
        lock.synchronized {
          sleepSlightlyLessThan(timeoutMillis)
          lock.notifyAll()
        }
      } else {
        sleepSlightlyLessThan(timeoutMillis)
        lock.synchronized {
          lock.notifyAll()
        }
      }
    }
  }

  private def startTimedInterrupt(
      waiter: Thread,
      timeoutMillis: Long
  ): Thread = {
    Thread.ofVirtual().start { () =>
      sleepSlightlyLessThan(timeoutMillis)
      waiter.interrupt()
    }
  }

  private def joinAuxThread(thread: Thread, role: String): Unit = {
    thread.join(Timeout)
    assertFalse(s"$role should terminate after race round", thread.isAlive())
  }

  private def sleepSlightlyLessThan(timeoutMillis: Long): Unit = {
    if (timeoutMillis <= 1L) return

    val maxDelta = Math.min(5L, timeoutMillis - 1L).toInt
    val delta =
      if (maxDelta <= 0) 0
      else ThreadLocalRandom.current().nextInt(maxDelta + 1)
    Thread.sleep(timeoutMillis - delta)
  }

  private def runNotifiedThenTimedOutRace(
      iterations: Int,
      barrierInSynchronizedBlock: Boolean
  ): Unit = {
    val waitTimeoutMillis = 1L
    val lock = new Object
    val start = new CyclicBarrier(2)
    val end =
      if (barrierInSynchronizedBlock) Some(new CyclicBarrier(2))
      else None
    val waiterFailure = new AtomicReference[Throwable](null)
    val notifierFailure = new AtomicReference[Throwable](null)

    val waiter = Thread.ofVirtual().start { () =>
      try {
        var i = 0
        while (i < iterations) {
          if (!barrierInSynchronizedBlock) {
            awaitBarrier(start, s"start-before-lock/waiter/$i")
          }
          lock.synchronized {
            if (barrierInSynchronizedBlock) {
              awaitBarrier(start, s"start-in-lock/waiter/$i")
            }
            lock.wait(waitTimeoutMillis)
            lock.wait(waitTimeoutMillis)
          }
          end.foreach(barrier => awaitBarrier(barrier, s"end/waiter/$i"))
          i += 1
        }
      } catch {
        case ex: Throwable =>
          waiterFailure.compareAndSet(null, ex)
      }
    }

    val notifierBuilder =
      if (ThreadLocalRandom.current().nextBoolean()) Thread.ofPlatform()
      else Thread.ofVirtual()
    val notifier = notifierBuilder.start { () =>
      try {
        var i = 0
        while (i < iterations) {
          awaitBarrier(start, s"start/notifier/$i")
          lock.synchronized {
            lock.notify()
          }
          end.foreach(barrier => awaitBarrier(barrier, s"end/notifier/$i"))
          i += 1
        }
      } catch {
        case ex: Throwable =>
          notifierFailure.compareAndSet(null, ex)
      }
    }

    waiter.join(Timeout)
    notifier.join(Timeout)

    assertFalse(
      s"waiter should terminate in notified-then-timeout race (barrierInSynchronizedBlock=$barrierInSynchronizedBlock)",
      waiter.isAlive()
    )
    assertFalse(
      s"notifier should terminate in notified-then-timeout race (barrierInSynchronizedBlock=$barrierInSynchronizedBlock)",
      notifier.isAlive()
    )

    val waiterThrown = waiterFailure.get()
    if (waiterThrown != null)
      throw new AssertionError(
        "waiter failed in notified-then-timeout race",
        waiterThrown
      )

    val notifierThrown = notifierFailure.get()
    if (notifierThrown != null)
      throw new AssertionError(
        "notifier failed in notified-then-timeout race",
        notifierThrown
      )
  }

  private def awaitBarrier(barrier: CyclicBarrier, label: String): Unit = {
    try {
      barrier.await(RaceRoundTimeout, TimeUnit.MILLISECONDS)
    } catch {
      case ex: BrokenBarrierException =>
        throw new AssertionError(s"barrier broken at $label", ex)
      case ex: TimeoutException =>
        throw new AssertionError(
          s"barrier synchronization timed out at $label",
          ex
        )
    }
  }
}
