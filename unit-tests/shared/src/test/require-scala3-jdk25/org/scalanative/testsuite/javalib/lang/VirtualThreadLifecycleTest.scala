package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadLifecycleTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadLifecycleTest {
  private val Timeout = 5000L // ms

  @Test def unstartedThreadIsNew(): Unit = {
    val thread = Thread.ofVirtual().unstarted(() => ())
    assertTrue("should be virtual", thread.isVirtual)
    assertEquals(Thread.State.NEW, thread.getState())
    assertFalse("should not be alive", thread.isAlive)
  }

  @Test def startedThreadTransitionsFromNew(): Unit = {
    val started = new CountDownLatch(1)
    val thread = Thread.ofVirtual().unstarted { () =>
      started.countDown()
      Thread.sleep(50)
    }
    assertEquals(Thread.State.NEW, thread.getState())
    thread.start()
    assertTrue(started.await(Timeout, TimeUnit.MILLISECONDS))
    assertNotEquals(Thread.State.NEW, thread.getState())
    thread.join(Timeout)
    assertEquals(Thread.State.TERMINATED, thread.getState())
  }

  @Test def startVirtualThreadConvenience(): Unit = {
    val result = new AtomicBoolean(false)
    val thread = Thread.startVirtualThread { () =>
      result.set(true)
    }
    assertTrue("should be virtual", thread.isVirtual)
    thread.join(Timeout)
    assertTrue("task should have run", result.get())
    assertEquals(Thread.State.TERMINATED, thread.getState())
  }

  @Test def doubleStartThrows(): Unit = {
    val latch = new CountDownLatch(1)
    val thread = Thread.ofVirtual().start { () =>
      latch.await(Timeout, TimeUnit.MILLISECONDS)
    }
    try assertThrows(classOf[IllegalThreadStateException], () => thread.start())
    finally latch.countDown()
    thread.join(Timeout)
  }

  @Test def virtualThreadIsAlwaysDaemon(): Unit = {
    val thread = Thread.ofVirtual().unstarted(() => ())
    assertTrue("virtual thread must be daemon", thread.isDaemon)
    assertThrows(
      classOf[IllegalArgumentException],
      () => thread.setDaemon(false)
    )
    assertTrue("daemon unchanged after rejected setDaemon", thread.isDaemon)
  }

  @Test def virtualThreadPriorityIsNormPriority(): Unit = {
    val thread = Thread.ofVirtual().unstarted(() => ())
    assertEquals(Thread.NORM_PRIORITY, thread.getPriority)
    thread.setPriority(Thread.MAX_PRIORITY)
    assertEquals(
      "setPriority should be no-op for virtual threads",
      Thread.NORM_PRIORITY,
      thread.getPriority
    )
  }

  @Test def virtualThreadGroup(): Unit = {
    val thread = Thread.ofVirtual().unstarted(() => ())
    val group = thread.getThreadGroup
    assertNotNull("thread group should not be null before start", group)
    assertEquals("VirtualThreads", group.getName)
  }

  @Test def threadIdIsUnique(): Unit = {
    val count = 100
    val ids = new ConcurrentHashMap[java.lang.Long, java.lang.Boolean]()
    val latch = new CountDownLatch(count)
    val threads = (0 until count).map { _ =>
      Thread.ofVirtual().start { () =>
        ids.put(Thread.currentThread().threadId(), java.lang.Boolean.TRUE)
        latch.countDown()
      }
    }
    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    threads.foreach(_.join(Timeout))
    assertEquals("all thread IDs should be unique", count, ids.size())
  }

  @Test def virtualThreadToString(): Unit = {
    val thread = Thread.ofVirtual().name("test-vt").unstarted(() => ())
    val str = thread.toString()
    assertTrue(
      s"toString should contain 'VirtualThread': $str",
      str.contains("VirtualThread")
    )
    assertTrue(
      s"toString should contain thread name 'test-vt': $str",
      str.contains("test-vt")
    )
  }

  @Test def threadStateAfterTermination(): Unit = {
    val thread = Thread.ofVirtual().start(() => ())
    thread.join(Timeout)
    assertEquals(Thread.State.TERMINATED, thread.getState())
    assertFalse("should not be alive after termination", thread.isAlive)
  }

  @Test def unstartedFactoryThread(): Unit = {
    val factory = Thread.ofVirtual().factory()
    val done = new AtomicBoolean(false)
    val thread = factory.newThread(() => done.set(true))
    assertTrue("factory thread should be virtual", thread.isVirtual)
    assertEquals(Thread.State.NEW, thread.getState())
    thread.start()
    thread.join(Timeout)
    assertTrue("factory thread task should have run", done.get())
  }

  @Test def runMethodNotCallableDirectly(): Unit = {
    val ran = new AtomicBoolean(false)
    val thread = Thread.ofVirtual().unstarted(() => ran.set(true))
    try thread.run()
    catch { case _: Throwable => () }
    assertFalse(
      "run() should not execute the task (only start() does)",
      ran.get()
    )
  }
}
