package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.{LockSupport, ReentrantLock}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualPlatformInteropTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualPlatformInteropTest {
  private val Timeout = 5000L

  @Test def platformJoinsVirtual(): Unit = {
    val done = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      Thread.sleep(100)
      done.set(true)
    }
    // Current thread is a platform thread joining a virtual thread
    vt.join(Timeout)
    assertTrue("platform thread should successfully join VT", done.get())
    assertEquals(Thread.State.TERMINATED, vt.getState())
  }

  @Test def virtualJoinsPlatform(): Unit = {
    val done = new AtomicBoolean(false)
    val pt = Thread.ofPlatform().start { () =>
      Thread.sleep(100)
      done.set(true)
    }
    val vtResult = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      pt.join()
      vtResult.set(done.get())
    }
    vt.join(Timeout)
    assertTrue("VT should successfully join platform thread", vtResult.get())
  }

  @Test def platformInterruptsVirtual(): Unit = {
    val parked = new CountDownLatch(1)
    val interrupted = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      interrupted.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue(
      "platform thread should interrupt VT successfully",
      interrupted.get()
    )
  }

  @Test def virtualInterruptsPlatform(): Unit = {
    val parked = new CountDownLatch(1)
    val interrupted = new AtomicBoolean(false)
    val pt = Thread.ofPlatform().daemon(true).start { () =>
      parked.countDown()
      LockSupport.park()
      interrupted.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val vt = Thread.ofVirtual().start { () =>
      pt.interrupt()
    }
    vt.join(Timeout)
    pt.join(Timeout)
    assertTrue(
      "VT should interrupt platform thread successfully",
      interrupted.get()
    )
  }

  @Test def sharedMonitor(): Unit = {
    val lock = new Object
    val numVirtual = 10
    val numPlatform = 5
    val iterations = 200
    val counter = new AtomicInteger(0)
    val total = numVirtual + numPlatform
    val latch = new CountDownLatch(total)

    for (_ <- 0 until numVirtual) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          lock.synchronized { counter.incrementAndGet() }
        }
        latch.countDown()
      }
    }
    for (_ <- 0 until numPlatform) {
      Thread.ofPlatform().daemon(true).start { () =>
        for (_ <- 0 until iterations) {
          lock.synchronized { counter.incrementAndGet() }
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(total * iterations, counter.get())
  }

  @Test def sharedReentrantLock(): Unit = {
    val lock = new ReentrantLock()
    val numVirtual = 10
    val numPlatform = 5
    val iterations = 200
    var counter = 0
    val total = numVirtual + numPlatform
    val latch = new CountDownLatch(total)

    for (_ <- 0 until numVirtual) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          lock.lock()
          try counter += 1
          finally lock.unlock()
        }
        latch.countDown()
      }
    }
    for (_ <- 0 until numPlatform) {
      Thread.ofPlatform().daemon(true).start { () =>
        for (_ <- 0 until iterations) {
          lock.lock()
          try counter += 1
          finally lock.unlock()
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(total * iterations, counter)
  }

  @Test def sharedCountDownLatch(): Unit = {
    val latchSize = 5
    val latch = new CountDownLatch(latchSize)
    val vtWaiting = new AtomicInteger(0)
    val ptWaiting = new AtomicInteger(0)
    val done = new CountDownLatch(4) // 2 VT waiters + 2 PT waiters

    // VTs waiting
    for (_ <- 0 until 2) {
      Thread.ofVirtual().start { () =>
        latch.await()
        vtWaiting.incrementAndGet()
        done.countDown()
      }
    }
    // PTs waiting
    for (_ <- 0 until 2) {
      Thread.ofPlatform().daemon(true).start { () =>
        latch.await()
        ptWaiting.incrementAndGet()
        done.countDown()
      }
    }

    // Count down from mix of VTs and PTs
    Thread.sleep(50)
    for (i <- 0 until latchSize) {
      if (i % 2 == 0)
        Thread.ofVirtual().start(() => latch.countDown()).join(Timeout)
      else
        Thread
          .ofPlatform()
          .daemon(true)
          .start(() => latch.countDown())
          .join(Timeout)
    }

    assertTrue(done.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(2, vtWaiting.get())
    assertEquals(2, ptWaiting.get())
  }

  @Test def mixedObjectWaitNotify(): Unit = {
    val lock = new Object
    val vtDone = new AtomicBoolean(false)
    val ptDone = new AtomicBoolean(false)
    val vtInWait = new CountDownLatch(1)
    val ptInWait = new CountDownLatch(1)

    // VT waits, PT notifies
    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        vtInWait.countDown()
        lock.wait()
        vtDone.set(true)
      }
    }
    assertTrue(vtInWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val ptNotifier = Thread.ofPlatform().daemon(true).start { () =>
      lock.synchronized { lock.notify() }
    }
    ptNotifier.join(Timeout)
    vt.join(Timeout)
    assertTrue("VT should wake up from PT's notify", vtDone.get())

    // PT waits, VT notifies
    val pt = Thread.ofPlatform().daemon(true).start { () =>
      lock.synchronized {
        ptInWait.countDown()
        lock.wait()
        ptDone.set(true)
      }
    }
    assertTrue(ptInWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val vtNotifier = Thread.ofVirtual().start { () =>
      lock.synchronized { lock.notify() }
    }
    vtNotifier.join(Timeout)
    pt.join(Timeout)
    assertTrue("PT should wake up from VT's notify", ptDone.get())
  }
}
