package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.{ReentrantLock, ReentrantReadWriteLock}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadJUCLocksTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadJUCLocksTest {
  private val Timeout = 10000L

  @Test def reentrantLockBasic(): Unit = {
    val lock = new ReentrantLock()
    val numThreads = 20
    val iterations = 500
    var counter = 0
    val latch = new CountDownLatch(numThreads)

    for (_ <- 0 until numThreads) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          lock.lock()
          try counter += 1
          finally lock.unlock()
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(numThreads * iterations, counter)
  }

  @Test def reentrantLockConditionAwaitSignal(): Unit = {
    val lock = new ReentrantLock()
    val condition = lock.newCondition()
    val inAwait = new CountDownLatch(1)
    val done = new AtomicBoolean(false)

    val vt = Thread.ofVirtual().start { () =>
      lock.lock()
      try {
        inAwait.countDown()
        condition.await()
        done.set(true)
      } finally lock.unlock()
    }

    assertTrue(inAwait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)

    lock.lock()
    try condition.signal()
    finally lock.unlock()

    vt.join(Timeout)
    assertTrue("VT should resume after condition.signal()", done.get())
  }

  @Test def reentrantLockConditionAwaitTimeout(): Unit = {
    val lock = new ReentrantLock()
    val condition = lock.newCondition()
    val result = new AtomicBoolean(true)

    val vt = Thread.ofVirtual().start { () =>
      lock.lock()
      try {
        val notSignaled = condition.await(200, TimeUnit.MILLISECONDS)
        result.set(notSignaled)
      } finally lock.unlock()
    }

    vt.join(Timeout)
    assertFalse(
      "condition.await with timeout should return false when not signaled",
      result.get()
    )
  }

  @Test def reentrantLockConditionAwaitInterrupted(): Unit = {
    val lock = new ReentrantLock()
    val condition = lock.newCondition()
    val inAwait = new CountDownLatch(1)
    val threw = new AtomicBoolean(false)

    val vt = Thread.ofVirtual().start { () =>
      lock.lock()
      try {
        inAwait.countDown()
        condition.await()
      } catch {
        case _: InterruptedException => threw.set(true)
      } finally lock.unlock()
    }

    assertTrue(inAwait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue(
      "condition.await should throw InterruptedException",
      threw.get()
    )
  }

  @Test def countDownLatchAwait(): Unit = {
    val latch = new CountDownLatch(3)
    val count = 3
    val completed = new AtomicInteger(0)

    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        latch.await()
        completed.incrementAndGet()
      }
    }

    Thread.sleep(50)
    assertEquals("no VT should complete before countdown", 0, completed.get())
    latch.countDown()
    latch.countDown()
    latch.countDown()

    val deadline = System.currentTimeMillis() + Timeout
    while (completed.get() < count && System.currentTimeMillis() < deadline) {
      Thread.sleep(10)
    }
    assertEquals(
      "all VTs should complete after countdown",
      count,
      completed.get()
    )
  }

  @Test def countDownLatchAwaitTimeout(): Unit = {
    val latch = new CountDownLatch(1)
    val result = new AtomicBoolean(true)

    val vt = Thread.ofVirtual().start { () =>
      val r = latch.await(200, TimeUnit.MILLISECONDS)
      result.set(r)
    }

    vt.join(Timeout)
    assertFalse(
      "latch.await should return false on timeout",
      result.get()
    )
  }

  @Test def semaphoreAcquireRelease(): Unit = {
    val permits = 3
    val sem = new Semaphore(permits)
    val numThreads = 10
    val maxConcurrent = new AtomicInteger(0)
    val currentConcurrent = new AtomicInteger(0)
    val latch = new CountDownLatch(numThreads)

    for (_ <- 0 until numThreads) {
      Thread.ofVirtual().start { () =>
        sem.acquire()
        try {
          val cur = currentConcurrent.incrementAndGet()
          maxConcurrent.updateAndGet(m => Math.max(m, cur))
          Thread.sleep(50)
          currentConcurrent.decrementAndGet()
        } finally {
          sem.release()
          latch.countDown()
        }
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertTrue(
      s"max concurrent should not exceed permit count ($permits), was ${maxConcurrent.get()}",
      maxConcurrent.get() <= permits
    )
  }

  @Test def reentrantLockDoesNotPinCarrier(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    val threadCount = processors * 4
    val lock = new ReentrantLock()
    val allDone = new CountDownLatch(threadCount)

    val start = System.nanoTime()
    for (_ <- 0 until threadCount) {
      Thread.ofVirtual().start { () =>
        lock.lock()
        try Thread.sleep(10)
        finally lock.unlock()
        allDone.countDown()
      }
    }

    assertTrue(
      "all VTs with contended ReentrantLock should complete",
      allDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
  }

  @Test def readWriteLock(): Unit = {
    val rwLock = new ReentrantReadWriteLock()
    var sharedData = 0
    val numReaders = 10
    val numWriters = 5
    val iterations = 100
    val latch = new CountDownLatch(numReaders + numWriters)
    val errors = new AtomicInteger(0)

    for (_ <- 0 until numWriters) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          rwLock.writeLock().lock()
          try sharedData += 1
          finally rwLock.writeLock().unlock()
        }
        latch.countDown()
      }
    }

    for (_ <- 0 until numReaders) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          rwLock.readLock().lock()
          try {
            val _ = sharedData // read without modification
          } finally rwLock.readLock().unlock()
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(numWriters * iterations, sharedData)
    assertEquals(0, errors.get())
  }

  @Test def cyclicBarrierWithVirtualThreads(): Unit = {
    val parties = 5
    val barrier = new CyclicBarrier(parties)
    val arrivedAll = new AtomicInteger(0)
    val latch = new CountDownLatch(parties)

    for (_ <- 0 until parties) {
      Thread.ofVirtual().start { () =>
        barrier.await(Timeout, TimeUnit.MILLISECONDS)
        arrivedAll.incrementAndGet()
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(
      "all VTs should have passed the barrier",
      parties,
      arrivedAll.get()
    )
  }
}
