package org.scalanative.testsuite.javalib.lang

import java.util
import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadExecutorsTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadExecutorsTest {
  private val Timeout = 10000L

  @Test def newVirtualThreadPerTaskExecutorExists(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    assertNotNull(executor)
    executor.close()
  }

  @Test def submitTaskReturnsResult(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    try {
      val future = executor.submit[String](() => "hello")
      assertEquals("hello", future.get(Timeout, TimeUnit.MILLISECONDS))
    } finally executor.close()
  }

  @Test def executeRunsOnVirtualThread(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    try {
      val isVirtual = new AtomicBoolean(false)
      val latch = new CountDownLatch(1)
      executor.execute { () =>
        isVirtual.set(Thread.currentThread().isVirtual)
        latch.countDown()
      }
      assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
      assertTrue("task should run on virtual thread", isVirtual.get())
    } finally executor.close()
  }

  @Test def shutdownAndAwait(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    val latch = new CountDownLatch(1)
    executor.submit[Unit] { () =>
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    }
    executor.shutdown()
    assertTrue(executor.isShutdown)
    latch.countDown()
    assertTrue(executor.awaitTermination(Timeout, TimeUnit.MILLISECONDS))
    assertTrue(executor.isTerminated)
  }

  @Test def newThreadPerTaskExecutorWithVirtualFactory(): Unit = {
    val factory = Thread.ofVirtual().factory()
    val executor = Executors.newThreadPerTaskExecutor(factory)
    try {
      val isVirtual = new AtomicBoolean(false)
      val latch = new CountDownLatch(1)
      executor.execute { () =>
        isVirtual.set(Thread.currentThread().isVirtual)
        latch.countDown()
      }
      assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
      assertTrue(isVirtual.get())
    } finally executor.close()
  }

  @Test def invokeAllOnVirtualThreads(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    try {
      val tasks = new util.ArrayList[Callable[Integer]]()
      for (i <- 0 until 10) {
        tasks.add(() => { Thread.sleep(50); Integer.valueOf(i) })
      }
      val futures = executor.invokeAll(tasks)
      assertEquals(10, futures.size())
      for (i <- 0 until 10) {
        assertEquals(Integer.valueOf(i), futures.get(i).get())
      }
    } finally executor.close()
  }

  @Test def invokeAnyOnVirtualThreads(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    try {
      val tasks = new util.ArrayList[Callable[String]]()
      tasks.add(() => { Thread.sleep(200); "slow" })
      tasks.add(() => "fast")
      val result = executor.invokeAny(tasks)
      assertEquals("fast", result)
    } finally executor.close()
  }

  @Test def closeShutsDownGracefully(): Unit = {
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    val done = new AtomicBoolean(false)
    executor.submit[Unit] { () =>
      Thread.sleep(100)
      done.set(true)
    }
    executor.close() // should wait for running tasks
    assertTrue(executor.isShutdown)
    assertTrue(executor.isTerminated)
    assertTrue("task should have completed before close returned", done.get())
  }

  // Interim test using manual VT factory until Executors APIs are available
  @Test def manualVirtualThreadFactoryWithExecutor(): Unit = {
    val factory = Thread.ofVirtual().factory()
    val count = 20
    val completed = new AtomicInteger(0)
    val latch = new CountDownLatch(count)

    for (_ <- 0 until count) {
      val t = factory.newThread { () =>
        completed.incrementAndGet()
        latch.countDown()
      }
      assertTrue("factory should create virtual threads", t.isVirtual)
      t.start()
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(count, completed.get())
  }
}
