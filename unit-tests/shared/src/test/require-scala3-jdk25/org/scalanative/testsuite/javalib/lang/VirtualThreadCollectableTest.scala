package org.scalanative.testsuite.javalib.lang

import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

import scala.noinline

import org.junit.Assert._
import org.junit.Assume._
import org.junit._

import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadCollectableTest {
  @BeforeClass def checkRuntime(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    assumeTrue(
      "WeakReferences work only on Commix and Immix GC",
      Platform.isWeakReferenceSupported
    )
  }
}

class VirtualThreadCollectableTest {
  private val Timeout = 5000L

  @Test def unstartedVirtualThreadIsCollectable(): Unit = {
    waitUntilCleared(newUnstartedThreadRef())
  }

  @Test def terminatedVirtualThreadIsCollectable(): Unit = {
    waitUntilCleared(newTerminatedThreadRef())
  }

  @noinline private def newUnstartedThreadRef(): WeakReference[Thread] = {
    val thread = Thread.ofVirtual().unstarted(() => ())
    val ref = new WeakReference[Thread](thread)
    assertNotNull(
      "unstarted VT should be strongly reachable before GC",
      ref.get()
    )
    ref
  }

  @noinline private def newTerminatedThreadRef(): WeakReference[Thread] = {
    val thread = Thread.ofVirtual().start(() => ())
    thread.join()
    val ref = new WeakReference[Thread](thread)
    assertNotNull(
      "terminated VT should be strongly reachable before GC",
      ref.get()
    )
    ref
  }

  private def waitUntilCleared(ref: WeakReference[_]): Unit = {
    val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Timeout)
    while (ref.get() != null && System.nanoTime() < deadline) {
      System.gc()
      Thread.sleep(20)
    }
    assertNull("virtual thread should be reclaimable after GC", ref.get())
  }
}
