package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicReference}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadEdgeCaseTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadEdgeCaseTest {
  private val Timeout = 30000L

  @Test def startFromVirtualThread(): Unit = {
    val done = new AtomicBoolean(false)
    val outer = Thread.ofVirtual().start { () =>
      val inner = Thread.ofVirtual().start { () =>
        done.set(true)
      }
      inner.join()
    }
    outer.join(Timeout)
    assertTrue("VT should be able to start another VT", done.get())
  }

  @Test def deeplyNestedVirtualThreads(): Unit = {
    val depth = 100
    val result = new AtomicInteger(0)

    def spawn(remaining: Int): Unit = {
      if (remaining <= 0) {
        result.set(depth)
      } else {
        val child = Thread.ofVirtual().start { () =>
          spawn(remaining - 1)
        }
        child.join()
      }
    }

    val root = Thread.ofVirtual().start { () => spawn(depth) }
    root.join(Timeout)
    assertEquals(
      "deeply nested VTs should all complete",
      depth,
      result.get()
    )
  }

  @Test def rapidCreateAndJoin(): Unit = {
    val count = 100_000
    val completed = new AtomicInteger(0)

    for (_ <- 0 until count) {
      val vt = Thread.ofVirtual().start { () =>
        completed.incrementAndGet()
      }
      vt.join(Timeout)
    }

    assertEquals(
      "rapid create-start-join cycle should complete all VTs",
      count,
      completed.get()
    )
  }

  @Test def pinningDetection(): Unit = {
    val processors = Runtime.getRuntime.availableProcessors()
    // Pin all carriers with synchronized + sleep
    val pinnedStarted = new CountDownLatch(processors)
    val unpinnedDone = new CountDownLatch(1)

    for (_ <- 0 until processors) {
      val myLock = new Object
      Thread.ofVirtual().start { () =>
        myLock.synchronized {
          pinnedStarted.countDown()
          Thread.sleep(1000) // pins carrier
        }
      }
    }

    assertTrue(
      "pinned VTs should start",
      pinnedStarted.await(Timeout, TimeUnit.MILLISECONDS)
    )
    Thread.sleep(100)

    // This VT should eventually run (via compensation or after pins release)
    val extra = Thread.ofVirtual().start { () =>
      unpinnedDone.countDown()
    }

    assertTrue(
      "additional VT should be able to run despite pinned carriers",
      unpinnedDone.await(Timeout, TimeUnit.MILLISECONDS)
    )
    extra.join(Timeout)
  }

  @Test def continuationStackOverflow(): Unit = {
    val threw = new AtomicReference[Throwable]()
    val vt = Thread
      .ofVirtual()
      .uncaughtExceptionHandler((_, e) => threw.set(e))
      .start { () =>
        def recurse(n: Int): Int = n + recurse(n + 1)
        recurse(0)
      }
    vt.join(Timeout)
    assertTrue(
      "deep recursion in VT should produce StackOverflowError",
      threw.get().isInstanceOf[StackOverflowError]
    )
  }

  @Test def threadReferenceLeak(): Unit = {
    // Create many VTs and let them terminate, then verify GC can reclaim
    val count = 1000
    val latch = new CountDownLatch(count)
    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        latch.countDown()
      }
    }
    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    // If there were a leak, this would eventually OOM in a stress test.
    // Here we just verify that the VTs terminate cleanly.
    System.gc()
  }

  @Test def virtualThreadInForkJoinTask(): Unit = {
    val pool = new ForkJoinPool()
    val result = new AtomicBoolean(false)
    try {
      val task = ForkJoinTask.adapt { () =>
        val vt = Thread.ofVirtual().start { () =>
          result.set(true)
        }
        vt.join()
        null
      }
      pool.submit(task)
      task.get(Timeout, TimeUnit.MILLISECONDS)
    } finally pool.shutdown()
    assertTrue(
      "VT started from within ForkJoinTask should work",
      result.get()
    )
  }

  @Test def selfJoinDeadlock(): Unit = {
    // On JDK 25, Thread.currentThread().join(timeout) with a timeout should
    // return after the timeout (the thread never terminates while it's joining itself).
    val result = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      // join with a timeout to avoid actual permanent deadlock
      Thread.currentThread().join(200)
      result.set(true)
    }
    vt.join(Timeout)
    assertTrue(
      "self-join with timeout should eventually return",
      result.get()
    )
  }

  @Test def virtualThreadWithLargeStack(): Unit = {
    val result = new AtomicInteger(0)
    val vt = Thread.ofVirtual().start { () =>
      def deepCall(n: Int): Int = {
        if (n <= 0) 0
        else {
          // Use stack space with local variables
          val a = n * 2
          val b = n * 3
          a + b + deepCall(n - 1)
        }
      }
      deepCall(5000)
      result.set(1)
    }
    vt.join(Timeout)
    assertEquals(
      "VT with deep call stack (5k frames) should complete",
      1,
      result.get()
    )
  }

  @Test def manyWaitingOnSameMonitor(): Unit = {
    val lock = new Object
    val count = 1000
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

    assertTrue(
      "all VTs should enter wait",
      inWait.await(Timeout, TimeUnit.MILLISECONDS)
    )
    Thread.sleep(100)

    lock.synchronized {
      lock.notifyAll()
    }

    val deadline = System.currentTimeMillis() + Timeout
    while (completed.get() < count && System.currentTimeMillis() < deadline) {
      Thread.sleep(50)
    }
    assertEquals(
      "all VTs should complete after notifyAll",
      count,
      completed.get()
    )
  }
}
