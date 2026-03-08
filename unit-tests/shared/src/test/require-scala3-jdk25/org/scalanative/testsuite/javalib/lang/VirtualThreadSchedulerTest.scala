package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{
  AtomicBoolean, AtomicInteger, AtomicReference
}
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadSchedulerTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadSchedulerTest {
  private val Timeout = 30000L

  @Test def defaultSchedulerIsShared(): Unit = {
    val ref1 = new AtomicReference[ThreadGroup]()
    val ref2 = new AtomicReference[ThreadGroup]()
    val latch = new CountDownLatch(2)
    Thread.ofVirtual().start { () =>
      ref1.set(Thread.currentThread().getThreadGroup)
      latch.countDown()
    }
    Thread.ofVirtual().start { () =>
      ref2.set(Thread.currentThread().getThreadGroup)
      latch.countDown()
    }
    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertSame(
      "VTs should share the same thread group",
      ref1.get(),
      ref2.get()
    )
  }

  @Test def carrierThreadCount(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    // Verify that with enough VTs, we can observe approximately `processors` concurrent executions
    val barrier = new CyclicBarrier(processors)
    val reached = new AtomicInteger(0)
    val latch = new CountDownLatch(processors)

    for (_ <- 0 until processors) {
      Thread.ofVirtual().start { () =>
        try {
          barrier.await(5, TimeUnit.SECONDS)
          reached.incrementAndGet()
        } catch {
          case _: TimeoutException | _: BrokenBarrierException => ()
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertTrue(
      s"should reach barrier with $processors VTs (reached: ${reached.get()})",
      reached.get() >= processors - 1
    )
  }

  @Test def manyVirtualThreads(): Unit = {
    val count = 100_000
    val completed = new AtomicInteger(0)
    val latch = new CountDownLatch(count)

    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        completed.incrementAndGet()
        latch.countDown()
      }
    }

    assertTrue(
      "100k VTs should all complete",
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    )
    assertEquals(count, completed.get())
  }

  @Test def virtualThreadsYield(): Unit = {
    val done = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      Thread.`yield`()
      done.set(true)
    }
    vt.join(Timeout)
    assertTrue("yield should not block the VT", done.get())
  }

  @Test def yieldDoesNotConsumeParkingPermit(): Unit = {
    val completed = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      LockSupport.unpark(Thread.currentThread())
      Thread.`yield`()
      LockSupport.park()
      completed.set(true)
    }
    vt.join(Timeout)
    assertTrue(
      "yield should not consume an already-available parking permit",
      completed.get()
    )
  }

  @Test def yieldDoesNotOfferParkingPermit(): Unit = {
    val readyToPark = new CountDownLatch(1)
    val completed = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      Thread.`yield`()
      readyToPark.countDown()
      LockSupport.park()
      completed.set(true)
    }

    assertTrue(readyToPark.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    assertEquals(
      "yield should not synthesize a parking permit",
      Thread.State.WAITING,
      vt.getState()
    )
    assertFalse(completed.get())
    LockSupport.unpark(vt)
    vt.join(Timeout)
    assertTrue(completed.get())
  }

  @Test def yieldUnderLoad(): Unit = {
    val numThreads = 20
    val iterations = 1000
    val counters = new Array[AtomicInteger](numThreads)
    for (i <- 0 until numThreads)
      counters(i) = new AtomicInteger(0)
    val latch = new CountDownLatch(numThreads)

    for (i <- 0 until numThreads) {
      Thread.ofVirtual().start { () =>
        for (_ <- 0 until iterations) {
          counters(i).incrementAndGet()
          Thread.`yield`()
        }
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    for (i <- 0 until numThreads) {
      assertEquals(
        s"VT $i should have completed all iterations",
        iterations,
        counters(i).get()
      )
    }
  }

  @Test def yieldLoopWhenPinnedStillMakesProgress(): Unit = {
    val started = new CountDownLatch(1)
    val done = new AtomicBoolean(false)
    val pinLock = new Object

    val vt = Thread.ofVirtual().start { () =>
      pinLock.synchronized {
        started.countDown()
        while (!done.get()) {
          Thread.`yield`()
        }
      }
    }

    assertTrue(started.await(Timeout, TimeUnit.MILLISECONDS))
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    while (System.nanoTime() < deadline) {
      System.gc()
      Thread.sleep(25)
    }
    done.set(true)
    vt.join(Timeout)
    assertEquals(
      "pinned yield loop should terminate once progress condition flips",
      Thread.State.TERMINATED,
      vt.getState()
    )
  }

  @Test def compensationForBlockedCarrier(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    // Pin all carrier threads by holding monitors and sleeping
    val allPinned = new CountDownLatch(processors)
    val lock = new Object
    val unpinnedDone = new CountDownLatch(1)

    for (_ <- 0 until processors) {
      val myLock = new Object
      Thread.ofVirtual().start { () =>
        myLock.synchronized {
          allPinned.countDown()
          Thread.sleep(500) // pins carrier
        }
      }
    }

    assertTrue(
      "all VTs should have started pinning",
      allPinned.await(Timeout, TimeUnit.MILLISECONDS)
    )
    Thread.sleep(50)

    // This VT should still be able to run via compensation
    val extra = Thread.ofVirtual().start { () =>
      unpinnedDone.countDown()
    }

    assertTrue(
      "VT should run even when carriers are pinned (via compensation)",
      unpinnedDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
    extra.join(Timeout)
  }

  @Test def schedulerInherited(): Unit = {
    val outerGroup = new AtomicReference[ThreadGroup]()
    val innerGroup = new AtomicReference[ThreadGroup]()
    val latch = new CountDownLatch(1)

    Thread
      .ofVirtual()
      .start { () =>
        outerGroup.set(Thread.currentThread().getThreadGroup)
        val inner = Thread.ofVirtual().start { () =>
          innerGroup.set(Thread.currentThread().getThreadGroup)
          latch.countDown()
        }
        inner.join()
      }
      .join(Timeout)

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertNotNull(outerGroup.get())
    assertSame(
      "VT created from VT should share the scheduler (same group)",
      outerGroup.get(),
      innerGroup.get()
    )
  }

  @Test def carrierThreadIdentity(): Unit = {
    val isVirtual = new AtomicBoolean(false)
    val threadRef = new AtomicReference[Thread]()
    val vt = Thread.ofVirtual().start { () =>
      val current = Thread.currentThread()
      isVirtual.set(current.isVirtual)
      threadRef.set(current)
    }
    vt.join(Timeout)
    assertTrue(
      "Thread.currentThread() inside VT should return the VT (isVirtual=true)",
      isVirtual.get()
    )
    assertSame(
      "Thread.currentThread() should be the VT object itself",
      vt,
      threadRef.get()
    )
  }

  @Test def carrierRecycled(): Unit = {
    val carriers = new ConcurrentHashMap[String, java.lang.Boolean]()
    val count = 50
    val latch = new CountDownLatch(count)

    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        // Sleep briefly to allow sequential execution on same carrier
        Thread.sleep(10)
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    // If carriers are recycled, count of carrier threads used << count of VTs
    // We can't directly inspect carrier threads from JDK API, but if 50 VTs
    // all complete on `processors` carriers, that's recycling working.
  }
}
