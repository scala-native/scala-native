package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{
  AtomicBoolean, AtomicInteger, AtomicReference
}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadMonitorTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadMonitorTest {
  private val Timeout = 10000L

  @Test def synchronizedBlockExclusivity(): Unit = {
    val numThreads = 50
    val iterations = 1000
    val counter = new AtomicInteger(0)
    val lock = new Object
    val latch = new CountDownLatch(numThreads)

    for (_ <- 0 until numThreads) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          lock.synchronized {
            counter.incrementAndGet()
          }
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(numThreads * iterations, counter.get())
  }

  @Test def objectWaitAndNotify(): Unit = {
    val lock = new Object
    val inWait = new CountDownLatch(1)
    val done = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        inWait.countDown()
        lock.wait()
        done.set(true)
      }
    }
    assertTrue(inWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    lock.synchronized {
      lock.notify()
    }
    vt.join(Timeout)
    assertTrue("VT should have resumed after notify", done.get())
  }

  @Test def objectWaitAndNotifyAll(): Unit = {
    val lock = new Object
    val count = 10
    val inWait = new CountDownLatch(count)
    val completed = new AtomicInteger(0)

    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        lock.synchronized {
          inWait.countDown()
          lock.wait()
          completed.incrementAndGet()
        }
      }
    }

    assertTrue(inWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(100)
    lock.synchronized {
      lock.notifyAll()
    }
    // Wait for all to complete
    val deadline = System.currentTimeMillis() + Timeout
    while (completed.get() < count && System.currentTimeMillis() < deadline) {
      Thread.sleep(10)
    }
    assertEquals("all waiting VTs should have resumed", count, completed.get())
  }

  @Test def objectWaitWithTimeout(): Unit = {
    val lock = new Object
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        val start = System.nanoTime()
        lock.wait(200)
        elapsed.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
      }
    }
    vt.join(Timeout)
    assertTrue(
      s"wait(200) should return after ~200ms, took ${elapsed.get()}ms",
      elapsed.get() >= 180
    )
  }

  @Test def objectWaitWithTimeoutAndNanos(): Unit = {
    val lock = new Object
    val elapsed = new java.util.concurrent.atomic.AtomicLong(0)
    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        val start = System.nanoTime()
        lock.wait(100, 500000)
        elapsed.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
      }
    }
    vt.join(Timeout)
    assertTrue(
      s"wait(100, 500000) should return after ~100.5ms, took ${elapsed.get()}ms",
      elapsed.get() >= 80
    )
  }

  @Test def objectWaitInterrupted(): Unit = {
    val lock = new Object
    val inWait = new CountDownLatch(1)
    val threw = new AtomicBoolean(false)
    val reacquiredMonitor = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        inWait.countDown()
        try lock.wait()
        catch {
          case _: InterruptedException =>
            threw.set(true)
            reacquiredMonitor.set(Thread.holdsLock(lock))
        }
      }
    }
    assertTrue(inWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue("wait should throw InterruptedException", threw.get())
    assertTrue("monitor should be re-acquired", reacquiredMonitor.get())
  }

  @Test def objectWaitIllegalMonitorState(): Unit = {
    val lock = new Object
    val threw = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      try lock.wait()
      catch {
        case _: IllegalMonitorStateException => threw.set(true)
      }
    }
    vt.join(Timeout)
    assertTrue(
      "wait() without holding lock should throw IllegalMonitorStateException",
      threw.get()
    )
  }

  @Test def nestedSynchronized(): Unit = {
    val lock1 = new Object
    val lock2 = new Object
    val done = new AtomicBoolean(false)
    runOnVirtualThread(Timeout) {
      lock1.synchronized {
        assertTrue(Thread.holdsLock(lock1))
        lock2.synchronized {
          assertTrue(Thread.holdsLock(lock1))
          assertTrue(Thread.holdsLock(lock2))
        }
        assertTrue(Thread.holdsLock(lock1))
        assertFalse(Thread.holdsLock(lock2))
        // Reentrant
        lock1.synchronized {
          assertTrue(Thread.holdsLock(lock1))
        }
        done.set(true)
      }
    }
    assertTrue("nested synchronized should work", done.get())
  }

  @Test def synchronizedPinsCarrier(): Unit = {
    val lock = new Object
    val insideSynchronized = new CountDownLatch(1)
    val otherDone = new CountDownLatch(1)

    // VT that holds a monitor and sleeps (pins the carrier)
    val pinned = Thread.ofVirtual().start { () =>
      lock.synchronized {
        insideSynchronized.countDown()
        Thread.sleep(300)
      }
    }
    assertTrue(insideSynchronized.await(Timeout, TimeUnit.MILLISECONDS))

    // Another VT should still be able to run on other carriers
    val other = Thread.ofVirtual().start { () =>
      otherDone.countDown()
    }
    assertTrue(
      "other VTs should still make progress when one pins a carrier",
      otherDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
    pinned.join(Timeout)
    other.join(Timeout)
  }

  @Test def monitorContention(): Unit = {
    val lock = new Object
    val numThreads = 20
    val iterations = 500
    var counter = 0
    val latch = new CountDownLatch(numThreads)

    for (_ <- 0 until numThreads) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          lock.synchronized {
            counter += 1
          }
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(
      "contended lock should still produce correct count",
      numThreads * iterations,
      counter
    )
  }

  @Test def holdsLock(): Unit = {
    val lock = new Object
    val result = new AtomicReference[Array[Boolean]]()
    val vt = Thread.ofVirtual().start { () =>
      val before = Thread.holdsLock(lock)
      lock.synchronized {
        val during = Thread.holdsLock(lock)
        result.set(Array(before, during))
      }
    }
    vt.join(Timeout)
    val r = result.get()
    assertNotNull(r)
    assertFalse("holdsLock should be false outside synchronized", r(0))
    assertTrue("holdsLock should be true inside synchronized", r(1))
  }

  @Test def waitReleasesAndReacquiresLock(): Unit = {
    val lock = new Object
    val inWait = new CountDownLatch(1)
    val entered = new AtomicBoolean(false)
    val done = new AtomicBoolean(false)

    val waiter = Thread.ofVirtual().start { () =>
      lock.synchronized {
        inWait.countDown()
        lock.wait()
        done.set(true)
      }
    }

    assertTrue(inWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)

    // Another thread should be able to enter synchronized while waiter is in wait()
    val checker = Thread.ofVirtual().start { () =>
      lock.synchronized {
        entered.set(true)
        lock.notify()
      }
    }
    checker.join(Timeout)
    waiter.join(Timeout)
    assertTrue(
      "another thread should enter lock while VT is in wait()",
      entered.get()
    )
    assertTrue("waiter should complete after being notified", done.get())
  }

  @Test def spuriousWakeup(): Unit = {
    val lock = new Object
    val ready = new CountDownLatch(1)
    @volatile var condition = false
    val done = new AtomicBoolean(false)

    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        ready.countDown()
        while (!condition) {
          lock.wait()
        }
        done.set(true)
      }
    }

    assertTrue(ready.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)

    // First notify without setting condition (simulates spurious wakeup pattern)
    lock.synchronized {
      lock.notify()
    }
    Thread.sleep(50)
    assertFalse("should still be waiting (condition not met)", done.get())

    // Now set condition and notify
    lock.synchronized {
      condition = true
      lock.notify()
    }
    vt.join(Timeout)
    assertTrue("should complete when condition is met", done.get())
  }
}
