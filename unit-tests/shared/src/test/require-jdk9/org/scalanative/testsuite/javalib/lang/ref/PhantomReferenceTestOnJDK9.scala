package org.scalanative.testsuite.javalib.lang.ref

import java.lang.ref._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

class PhantomReferenceTestOnJDK9 {
  import PhantomReferenceTestOnJDK9._

  @Test def getIsAlwaysNull(): Unit = {
    val queue = new ReferenceQueue[AnyRef]()
    val referent = new Object
    val ref = new PhantomReference[AnyRef](referent, queue)
    assertNull("get() of a phantom reference", ref.get())
    assertFalse("isEnqueued() before enqueue", ref.isEnqueued())
    assertNotNull(referent)
  }

  @Test def acceptsNullQueue(): Unit = {
    val ref = new PhantomReference[AnyRef](new Object, null)
    assertNull(ref.get())
    assertFalse("never enqueued without a queue", ref.enqueue())
  }

  @Test def enqueueAndClear(): Unit = {
    val queue = new ReferenceQueue[AnyRef]()
    val referent = new Object
    val ref = new PhantomReference[AnyRef](referent, queue)
    assertNull("queue empty before enqueue", queue.poll())
    assertTrue("enqueue() succeeds once", ref.enqueue())
    assertTrue("isEnqueued() after enqueue", ref.isEnqueued())
    assertFalse("enqueue() fails the second time", ref.enqueue())
    assertSame("queue holds the reference", ref, queue.poll())
    assertNull("queue holds only the reference", queue.poll())
    assertNull(ref.get())
    assertNotNull(referent)
  }

  @Test def enqueuedAfterReferentIsCollected(): Unit = {
    assumeTrue(
      "References are cleared only by Immix, Commix and Boehm",
      Platform.executingInJVM || Platform.isWeakReferenceSupported
    )
    assumeFalse(
      "Unreliable collection on Windows in the CI, see WeakReferenceTestOnJDK9",
      sys.env.contains("CI") && Platform.isWindows
    )
    val queue = new ReferenceQueue[AnyRef]()
    val refs = allocPhantomRefs(queue, 3)
    clobberStack(10000)

    val enqueued = collectUntilEnqueued(queue, refs.size)
    assertEquals("enqueued references", refs.toSet, enqueued.toSet)
    refs.foreach { ref => assertNull("get() after enqueue", ref.get()) }
    assertNull("nothing else enqueued", queue.poll())
  }

  @Test def notEnqueuedWhileReferentIsReachable(): Unit = {
    assumeTrue(
      "References are cleared only by Immix, Commix and Boehm",
      Platform.executingInJVM || Platform.isWeakReferenceSupported
    )
    val queue = new ReferenceQueue[AnyRef]()
    val referent = new Object
    val ref = new PhantomReference[AnyRef](referent, queue)
    for (_ <- 0 until 3) collect()
    assertNull("reachable referent enqueued", queue.poll())
    Reference.reachabilityFence(referent)
    Reference.reachabilityFence(ref)
  }
}

object PhantomReferenceTestOnJDK9 {
  @noinline def allocPhantomRefs(
      queue: ReferenceQueue[AnyRef],
      count: Int
  ): List[PhantomReference[AnyRef]] =
    List.fill(count)(new PhantomReference[AnyRef](new Object, queue))

  // Overwrites stale stack slots that could keep referents alive
  @noinline def clobberStack(n: Int): Int =
    if (n > 0) clobberStack(n - 1) + 1 else 0

  // Allocates some garbage and collects
  def collect(): Unit = {
    locally {
      val _ = Seq.fill(1000)(new Object {})
    }
    System.gc()
  }

  /** Collects until `count` references were enqueued on `queue`, fails after a
   *  minute.
   */
  def collectUntilEnqueued(
      queue: ReferenceQueue[AnyRef],
      count: Int
  ): List[Reference[_ <: AnyRef]] = {
    val deadline = System.currentTimeMillis() + 60 * 1000L
    var enqueued = List.empty[Reference[_ <: AnyRef]]
    while (enqueued.size < count) {
      if (System.currentTimeMillis() >= deadline)
        fail(s"only ${enqueued.size} of $count references enqueued")
      collect()
      val ref = queue.remove(200)
      if (ref != null) enqueued ::= ref
    }
    enqueued
  }
}
