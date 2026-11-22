package org.scalanative.testsuite.javalib.lang.ref

import java.lang.ref._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

class ReachabilityFenceTestOnJDK9 {
  import PhantomReferenceTestOnJDK9.{clobberStack, collectUntilEnqueued}

  @Test def acceptsAnyValue(): Unit = {
    Reference.reachabilityFence(null)
    Reference.reachabilityFence(new Object)
    Reference.reachabilityFence(42)
  }

  @noinline private def allocWeakRef(
      queue: ReferenceQueue[AnyRef]
  ): WeakReference[AnyRef] =
    new WeakReference[AnyRef](new Object, queue)

  /* The control referent is collected while the fenced one, not used after
   * its reference is created but kept by the fence, survives.
   */
  @noinline private def fencedReferent(
      queue: ReferenceQueue[AnyRef]
  ): WeakReference[AnyRef] = {
    val controlQueue = new ReferenceQueue[AnyRef]()
    val control = allocWeakRef(controlQueue)
    val referent = new Object
    val ref = new WeakReference[AnyRef](referent, queue)
    clobberStack()
    assertEquals(
      "control collected",
      List(control),
      collectUntilEnqueued(controlQueue, 1)
    )
    assertNotNull("referent collected before the fence", ref.get())
    assertNull("reference enqueued before the fence", queue.poll())
    Reference.reachabilityFence(referent)
    ref
  }

  @Test def keepsReferentReachableUntilTheFence(): Unit = {
    assumeTrue(
      "References are cleared only by Immix, Commix and Boehm",
      Platform.executingInJVM || Platform.isWeakReferenceSupported
    )
    assumeFalse(
      "Unreliable collection on Windows in the CI, see WeakReferenceTestOnJDK9",
      sys.env.contains("CI") && Platform.isWindows
    )
    val queue = new ReferenceQueue[AnyRef]()
    val ref = fencedReferent(queue)
    clobberStack()
    assertEquals(
      "enqueued after the fence",
      List(ref),
      collectUntilEnqueued(queue, 1)
    )
    assertNull("referent cleared after the fence", ref.get())
  }
}
