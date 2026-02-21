package org.scalanative.testsuite.javalib.lang

import java.time.Duration
import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadJoinTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadJoinTest {
  private val Timeout = 5000L

  @Test def joinWaitsForCompletion(): Unit = {
    val done = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().start { () =>
      Thread.sleep(100)
      done.set(true)
    }
    thread.join()
    assertTrue("task should have completed", done.get())
    assertEquals(Thread.State.TERMINATED, thread.getState())
  }

  @Test def joinWithMillisTimeout(): Unit = {
    val latch = new CountDownLatch(1)
    val thread = Thread.ofVirtual().start { () =>
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    }
    try {
      val start = System.nanoTime()
      thread.join(100)
      val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
      assertTrue(
        s"join should have returned after ~100ms, took ${elapsed}ms",
        elapsed >= 80
      )
      assertTrue("thread should still be alive", thread.isAlive)
    } finally latch.countDown()
    thread.join(Timeout)
  }

  @Test def joinWithMillisTimeoutAndNanos(): Unit = {
    val latch = new CountDownLatch(1)
    val thread = Thread.ofVirtual().start { () =>
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    }
    try {
      val start = System.nanoTime()
      thread.join(100, 500000)
      val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
      assertTrue(
        s"join should respect nanos component, took ${elapsed}ms",
        elapsed >= 80
      )
    } finally latch.countDown()
    thread.join(Timeout)
  }

  @Test def joinWithDuration(): Unit = {
    val latch = new CountDownLatch(1)
    val thread = Thread.ofVirtual().start { () =>
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    }
    try {
      val result = thread.join(Duration.ofMillis(100))
      assertFalse("join should return false when thread still alive", result)
    } finally latch.countDown()
    thread.join(Timeout)
    val afterResult = thread.join(Duration.ofMillis(100))
    assertTrue("join should return true when thread terminated", afterResult)
  }

  @Test def joinOnUnstartedThread(): Unit = {
    val thread = Thread.ofVirtual().unstarted(() => ())
    assertThrows(
      classOf[IllegalThreadStateException],
      () => thread.join(Duration.ofMillis(100))
    )
  }

  @Test def joinOnTerminatedThreadReturnsImmediately(): Unit = {
    val thread = Thread.ofVirtual().start(() => ())
    thread.join(Timeout)
    assertEquals(Thread.State.TERMINATED, thread.getState())
    val start = System.nanoTime()
    thread.join(Timeout)
    val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    assertTrue(
      s"join on terminated thread should be near-instant, took ${elapsed}ms",
      elapsed < 500
    )
  }

  @Test def joinZeroTimeoutWaitsIndefinitely(): Unit = {
    val done = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().start { () =>
      Thread.sleep(200)
      done.set(true)
    }
    thread.join(0)
    assertTrue(
      "join(0) should block until termination",
      done.get()
    )
    assertEquals(Thread.State.TERMINATED, thread.getState())
  }

  @Test def joinInterruptedThrowsInterruptedException(): Unit = {
    val latch = new CountDownLatch(1)
    val thread = Thread.ofVirtual().start { () =>
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    }
    val joiner = Thread.ofVirtual().start { () =>
      try {
        thread.join()
        fail("should have been interrupted")
      } catch {
        case _: InterruptedException => ()
      }
    }
    Thread.sleep(50)
    joiner.interrupt()
    joiner.join(Timeout)
    latch.countDown()
    thread.join(Timeout)
  }

  @Test def virtualThreadJoinsVirtualThread(): Unit = {
    val inner = Thread.ofVirtual().start { () =>
      Thread.sleep(100)
    }
    val result = new AtomicBoolean(false)
    val outer = Thread.ofVirtual().start { () =>
      inner.join()
      result.set(true)
    }
    outer.join(Timeout)
    assertTrue("outer VT should have joined inner VT", result.get())
    assertEquals(Thread.State.TERMINATED, inner.getState())
    assertEquals(Thread.State.TERMINATED, outer.getState())
  }

  @Test def manyThreadsJoinSameThread(): Unit = {
    val count = 20
    val target = Thread.ofVirtual().start { () =>
      Thread.sleep(200)
    }
    val completed = new AtomicInteger(0)
    val joiners = (0 until count).map { _ =>
      Thread.ofVirtual().start { () =>
        target.join()
        completed.incrementAndGet()
      }
    }
    joiners.foreach(_.join(Timeout))
    assertEquals(count, completed.get())
    assertEquals(Thread.State.TERMINATED, target.getState())
  }
}
