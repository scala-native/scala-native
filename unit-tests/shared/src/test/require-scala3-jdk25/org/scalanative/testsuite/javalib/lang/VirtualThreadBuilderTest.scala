package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadBuilderTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()

  val Local = new ThreadLocal[AnyRef]
  val InheritedLocal = new InheritableThreadLocal[AnyRef]

  class TestException extends RuntimeException
}

class VirtualThreadBuilderTest {
  import VirtualThreadBuilderTest._
  private val Timeout = 5000L

  @Test def builderWithName(): Unit = {
    val thread = Thread.ofVirtual().name("foo").unstarted(() => ())
    assertEquals("foo", thread.getName)

    val done = new AtomicBoolean(false)
    val started = Thread.ofVirtual().name("bar").start { () =>
      done.set(true)
    }
    assertEquals("bar", started.getName)
    started.join(Timeout)
    assertTrue(done.get())

    val factory = Thread.ofVirtual().name("baz").factory()
    val factoryThread = factory.newThread(() => ())
    assertEquals("baz", factoryThread.getName)
  }

  @Test def builderWithNameAndCounter(): Unit = {
    val builder = Thread.ofVirtual().name("vt-", 0)
    val t1 = builder.unstarted(() => ())
    val t2 = builder.unstarted(() => ())
    val t3 = builder.unstarted(() => ())
    assertEquals("vt-0", t1.getName)
    assertEquals("vt-1", t2.getName)
    assertEquals("vt-2", t3.getName)
  }

  @Test def factoryWithNameAndCounter(): Unit = {
    val builder = Thread.ofVirtual().name("f-", 10)
    val factory = builder.factory()
    val t1 = factory.newThread(() => ())
    val t2 = factory.newThread(() => ())
    val t3 = factory.newThread(() => ())
    assertEquals("f-10", t1.getName)
    assertEquals("f-11", t2.getName)
    assertEquals("f-12", t3.getName)
  }

  @Test def builderInheritInheritableThreadLocals(): Unit = {
    val value = new AnyRef
    InheritedLocal.set(value)
    val seen = new AtomicReference[AnyRef]()
    val thread = Thread
      .ofVirtual()
      .inheritInheritableThreadLocals(true)
      .start { () =>
        seen.set(InheritedLocal.get())
      }
    thread.join(Timeout)
    assertSame(
      "child VT should see parent's inheritable local",
      value,
      seen.get()
    )
  }

  @Test def builderNoInheritInheritableThreadLocals(): Unit = {
    val value = new AnyRef
    InheritedLocal.set(value)
    val seen = new AtomicReference[AnyRef]()
    val thread = Thread
      .ofVirtual()
      .inheritInheritableThreadLocals(false)
      .start { () =>
        seen.set(InheritedLocal.get())
      }
    thread.join(Timeout)
    assertNull(
      "child VT should not see parent's inheritable local",
      seen.get()
    )
  }

  @Test def builderThreadLocalSupport(): Unit = {
    val done = new AtomicBoolean(false)
    runOnVirtualThread(Timeout) {
      val value = new AnyRef
      Local.set(value)
      assertSame(value, Local.get())
      done.set(true)
    }
    assertTrue("thread local get/set should work", done.get())
  }

  @Test def builderUncaughtExceptionHandler(): Unit = {
    val threadRef = new AtomicReference[Thread]()
    val exRef = new AtomicReference[Throwable]()
    val thread = Thread
      .ofVirtual()
      .uncaughtExceptionHandler { (t, e) =>
        threadRef.set(t)
        exRef.set(e)
      }
      .start { () =>
        throw new TestException
      }
    thread.join(Timeout)
    assertSame(
      "UEH should receive the virtual thread",
      thread,
      threadRef.get()
    )
    assertTrue(
      "UEH should receive the exception",
      exRef.get().isInstanceOf[TestException]
    )
  }

  @Test def builderNullArgs(): Unit = {
    val builder = Thread.ofVirtual()
    assertThrows(classOf[NullPointerException], () => builder.name(null))
    assertThrows(
      classOf[NullPointerException],
      () => builder.name(null, 0)
    )
    assertThrows(
      classOf[NullPointerException],
      () => builder.uncaughtExceptionHandler(null)
    )
    assertThrows(
      classOf[NullPointerException],
      () => builder.unstarted(null)
    )
    assertThrows(classOf[NullPointerException], () => builder.start(null))
  }
}
