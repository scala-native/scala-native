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
    val ctx = startVirtualThread(() => {
      Thread.sleep(100)
      done.set(true)
    })
    ctx.thread.join()
    ctx.rethrowException()
    assertTrue("task should have completed", done.get())
    assertEquals(Thread.State.TERMINATED, ctx.thread.getState())
  }

  @Test def joinWithMillisTimeout(): Unit = {
    val latch = new CountDownLatch(1)
    val ctx =
      startVirtualThread(() => latch.await(Timeout, TimeUnit.MILLISECONDS))
    try {
      val start = System.nanoTime()
      ctx.thread.join(100)
      val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
      assertTrue(
        s"join should have returned after ~100ms, took ${elapsed}ms",
        elapsed >= 80
      )
      assertTrue("thread should still be alive", ctx.thread.isAlive)
    } finally latch.countDown()
    ctx.thread.join(Timeout)
    ctx.rethrowException()
  }

  @Test def joinWithMillisTimeoutAndNanos(): Unit = {
    val latch = new CountDownLatch(1)
    val ctx =
      startVirtualThread(() => latch.await(Timeout, TimeUnit.MILLISECONDS))
    try {
      val start = System.nanoTime()
      ctx.thread.join(100, 500000)
      val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
      assertTrue(
        s"join should respect nanos component, took ${elapsed}ms",
        elapsed >= 80
      )
    } finally latch.countDown()
    ctx.thread.join(Timeout)
    ctx.rethrowException()
  }

  // @Test def joinWithDuration(): Unit = {
  //   val latch = new CountDownLatch(1)
  //   val ctx = startVirtualThread(() =>
  //     latch.await(Timeout, TimeUnit.MILLISECONDS)
  //   )
  //   try {
  //     val result = ctx.thread.join(Duration.ofMillis(100))
  //     assertFalse("join should return false when thread still alive", result)
  //   } finally latch.countDown()
  //   ctx.thread.join(Timeout)
  //   ctx.rethrowException()
  //   val afterResult = ctx.thread.join(Duration.ofMillis(100))
  //   assertTrue("join should return true when thread terminated", afterResult)
  // }

  // @Test def joinOnUnstartedThread(): Unit = {
  //   val thread = Thread.ofVirtual().unstarted(() => ())
  //   assertThrows(
  //     classOf[IllegalThreadStateException],
  //     () => thread.join(Duration.ofMillis(100))
  //   )
  // }

  @Test def joinOnTerminatedThreadReturnsImmediately(): Unit = {
    val ctx = startVirtualThread(() => ())
    ctx.thread.join(Timeout)
    ctx.rethrowException()
    assertEquals(Thread.State.TERMINATED, ctx.thread.getState())
    val start = System.nanoTime()
    ctx.thread.join(Timeout)
    val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    assertTrue(
      s"join on terminated thread should be near-instant, took ${elapsed}ms",
      elapsed < 500
    )
  }

  @Test def joinZeroTimeoutWaitsIndefinitely(): Unit = {
    val done = new AtomicBoolean(false)
    val ctx = startVirtualThread(() => {
      Thread.sleep(200)
      done.set(true)
    })
    ctx.thread.join(0)
    ctx.rethrowException()
    assertTrue(
      "join(0) should block until termination",
      done.get()
    )
    assertEquals(Thread.State.TERMINATED, ctx.thread.getState())
  }

  @Test def joinInterruptedThrowsInterruptedException(): Unit = {
    val latch = new CountDownLatch(1)
    val threadCtx =
      startVirtualThread(() => latch.await(Timeout, TimeUnit.MILLISECONDS))
    val joinerCtx = startVirtualThread { () =>
      threadCtx.thread.join()
      fail("should have been interrupted")
    }
    Thread.sleep(50)
    joinerCtx.thread.interrupt()
    joinerCtx.thread.join(Timeout)
    assertThrows(classOf[InterruptedException], () => joinerCtx.rethrowException())
    latch.countDown()
    threadCtx.thread.join(Timeout)
    threadCtx.rethrowException()
  }

  @Test def virtualThreadJoinsVirtualThread(): Unit = {
    val innerCtx = startVirtualThread(() => Thread.sleep(100))
    val result = new AtomicBoolean(false)
    val outerCtx = startVirtualThread(() => {
      innerCtx.thread.join()
      result.set(true)
    })
    outerCtx.thread.join(Timeout)
    outerCtx.rethrowException()
    innerCtx.rethrowException()
    assertTrue("outer VT should have joined inner VT", result.get())
    assertEquals(Thread.State.TERMINATED, innerCtx.thread.getState())
    assertEquals(Thread.State.TERMINATED, outerCtx.thread.getState())
  }

  @Test def manyThreadsJoinSameThread(): Unit = {
    val count = 20
    val targetCtx = startVirtualThread(() => Thread.sleep(200))
    val completed = new AtomicInteger(0)
    val joiners = (0 until count).map { _ =>
      startVirtualThread(() => {
        targetCtx.thread.join()
        completed.incrementAndGet()
      })
    }
    joiners.foreach(_.join(Timeout))
    targetCtx.rethrowException()
    joiners.foreach(_.rethrowException())
    assertEquals(count, completed.get())
    assertEquals(Thread.State.TERMINATED, targetCtx.thread.getState())
  }
}
