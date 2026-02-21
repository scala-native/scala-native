package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadThreadLocalTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()

  val Local = new ThreadLocal[AnyRef]
  val InheritedLocal = new InheritableThreadLocal[AnyRef]
}

class VirtualThreadThreadLocalTest {
  import VirtualThreadThreadLocalTest._
  private val Timeout = 5000L

  @Test def threadLocalIsolation(): Unit = {
    val count = 10
    val values = new ConcurrentHashMap[Long, AnyRef]()
    val latch = new CountDownLatch(count)

    for (i <- 0 until count) {
      Thread.ofVirtual().start { () =>
        val v = new Object
        Local.set(v)
        Thread.`yield`()
        val tid = Thread.currentThread().threadId()
        values.put(tid, Local.get())
        latch.countDown()
      }
    }

    assertTrue(latch.await(Timeout, TimeUnit.MILLISECONDS))
    // Each VT should have read back its own distinct object
    assertEquals(
      "each VT should have its own ThreadLocal",
      count,
      values.size()
    )
  }

  @Test def threadLocalNotSharedWithCarrier(): Unit = {
    val marker = "VT-MARKER"
    val carrierSaw = new AtomicReference[AnyRef]()
    val vtDone = new CountDownLatch(1)

    // Set a value in VT, then verify it's not visible on the carrier thread
    val vt = Thread.ofVirtual().start { () =>
      Local.set(marker)
      assertEquals(marker, Local.get())
      vtDone.countDown()
    }
    vt.join(Timeout)

    // Platform thread checking that the local is not set
    val pt = Thread.ofPlatform().start { () =>
      carrierSaw.set(Local.get())
    }
    pt.join(Timeout)
    assertNull(
      "carrier thread should not see VT's ThreadLocal value",
      carrierSaw.get()
    )
  }

  @Test def inheritableThreadLocalInherited(): Unit = {
    val parentValue = new Object
    InheritedLocal.set(parentValue)
    val childSeen = new AtomicReference[AnyRef]()
    val vt = Thread.ofVirtual().start { () =>
      childSeen.set(InheritedLocal.get())
    }
    vt.join(Timeout)
    assertSame(
      "VT should inherit InheritableThreadLocal from parent",
      parentValue,
      childSeen.get()
    )
  }

  @Test def inheritableThreadLocalNotInherited(): Unit = {
    val parentValue = new Object
    InheritedLocal.set(parentValue)
    val childSeen = new AtomicReference[AnyRef]()
    val vt = Thread
      .ofVirtual()
      .inheritInheritableThreadLocals(false)
      .start { () =>
        childSeen.set(InheritedLocal.get())
      }
    vt.join(Timeout)
    assertNull(
      "VT with inheritInheritableThreadLocals(false) should not inherit",
      childSeen.get()
    )
  }

  @Test def threadLocalSurvivesYield(): Unit = {
    val result = new AtomicReference[AnyRef]()
    val value = new Object
    val vt = Thread.ofVirtual().start { () =>
      Local.set(value)
      Thread.`yield`()
      result.set(Local.get())
    }
    vt.join(Timeout)
    assertSame(
      "ThreadLocal should persist across Thread.yield()",
      value,
      result.get()
    )
  }

  @Test def threadLocalSurvivesPark(): Unit = {
    val result = new AtomicReference[AnyRef]()
    val value = new Object
    val parked = new CountDownLatch(1)
    val vt = Thread.ofVirtual().start { () =>
      Local.set(value)
      parked.countDown()
      LockSupport.park()
      result.set(Local.get())
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    LockSupport.unpark(vt)
    vt.join(Timeout)
    assertSame(
      "ThreadLocal should persist across park/unpark",
      value,
      result.get()
    )
  }

  @Test def threadLocalSurvivesSleep(): Unit = {
    val result = new AtomicReference[AnyRef]()
    val value = new Object
    val vt = Thread.ofVirtual().start { () =>
      Local.set(value)
      Thread.sleep(100)
      result.set(Local.get())
    }
    vt.join(Timeout)
    assertSame(
      "ThreadLocal should persist across Thread.sleep()",
      value,
      result.get()
    )
  }

  @Test def threadLocalAcrossCarrierSwitch(): Unit = {
    // Force multiple yield/sleep cycles to increase likelihood of carrier switch
    val result = new AtomicReference[AnyRef]()
    val value = new Object
    val vt = Thread.ofVirtual().start { () =>
      Local.set(value)
      for (_ <- 0 until 10) {
        Thread.`yield`()
        Thread.sleep(10)
      }
      result.set(Local.get())
    }
    vt.join(Timeout)
    assertSame(
      "ThreadLocal should persist even if carrier thread changes",
      value,
      result.get()
    )
  }
}
