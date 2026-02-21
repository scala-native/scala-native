package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{
  AtomicBoolean, AtomicInteger, AtomicReference
}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadExceptionTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()

  class TestException extends RuntimeException("test")
  class TestError extends Error("test error")
}

class VirtualThreadExceptionTest {
  import VirtualThreadExceptionTest._
  private val Timeout = 5000L

  @Test def uncaughtExceptionHandlerInvoked(): Unit = {
    val threadRef = new AtomicReference[Thread]()
    val exRef = new AtomicReference[Throwable]()
    val latch = new CountDownLatch(1)
    val vt = Thread
      .ofVirtual()
      .uncaughtExceptionHandler { (t, e) =>
        threadRef.set(t)
        exRef.set(e)
        latch.countDown()
      }
      .start { () =>
        throw new TestException
      }
    vt.join(Timeout)
    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertSame("UEH should receive the VT", vt, threadRef.get())
    assertTrue(
      "UEH should receive the thrown exception",
      exRef.get().isInstanceOf[TestException]
    )
  }

  @Test def defaultUncaughtExceptionHandler(): Unit = {
    val prevHandler = Thread.getDefaultUncaughtExceptionHandler
    try {
      val threadRef = new AtomicReference[Thread]()
      val exRef = new AtomicReference[Throwable]()
      val latch = new CountDownLatch(1)
      Thread.setDefaultUncaughtExceptionHandler { (t, e) =>
        threadRef.set(t)
        exRef.set(e)
        latch.countDown()
      }
      val vt = Thread.ofVirtual().start { () =>
        throw new TestException
      }
      vt.join(Timeout)
      assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
      assertSame(vt, threadRef.get())
      assertTrue(exRef.get().isInstanceOf[TestException])
    } finally Thread.setDefaultUncaughtExceptionHandler(prevHandler)
  }

  @Test def exceptionDoesNotAffectOtherVTs(): Unit = {
    val count = 10
    val completed = new AtomicInteger(0)
    val latch = new CountDownLatch(count)

    // One VT throws
    Thread
      .ofVirtual()
      .uncaughtExceptionHandler((_, _) => ())
      .start { () =>
        throw new TestException
      }

    Thread.sleep(50)

    // Other VTs should still work fine
    for (_ <- 0 until count) {
      Thread.ofVirtual().start { () =>
        completed.incrementAndGet()
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertEquals(
      "exception in one VT should not affect others",
      count,
      completed.get()
    )
  }

  @Test def errorInVirtualThread(): Unit = {
    val exRef = new AtomicReference[Throwable]()
    val latch = new CountDownLatch(1)
    val vt = Thread
      .ofVirtual()
      .uncaughtExceptionHandler { (_, e) =>
        exRef.set(e)
        latch.countDown()
      }
      .start { () =>
        throw new TestError
      }
    vt.join(Timeout)
    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    assertTrue(
      "Error should propagate to UEH",
      exRef.get().isInstanceOf[TestError]
    )
  }

  @Test def exceptionInNestedVirtualThread(): Unit = {
    val parentOk = new AtomicBoolean(false)
    val childException = new AtomicReference[Throwable]()
    val parent = Thread.ofVirtual().start { () =>
      val child = Thread
        .ofVirtual()
        .uncaughtExceptionHandler { (_, e) =>
          childException.set(e)
        }
        .start { () =>
          throw new TestException
        }
      child.join()
      parentOk.set(true)
    }
    parent.join(Timeout)
    assertTrue(
      "parent VT should not be affected by child's exception",
      parentOk.get()
    )
    assertTrue(
      "child's exception should have been caught",
      childException.get().isInstanceOf[TestException]
    )
  }
}
