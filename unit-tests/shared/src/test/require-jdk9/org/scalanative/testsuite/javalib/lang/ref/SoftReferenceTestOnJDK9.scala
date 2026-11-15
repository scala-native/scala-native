package org.scalanative.testsuite.javalib.lang.ref

import java.lang.ref._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

// Clearing soft references under memory pressure is tested with a small
// maximal heap in scripted-tests/run/gc-soft-references.
class SoftReferenceTestOnJDK9 {
  import PhantomReferenceTestOnJDK9.clobberStack

  @noinline def allocSoftRefs(
      queue: ReferenceQueue[AnyRef],
      count: Int
  ): List[SoftReference[AnyRef]] =
    List.fill(count)(new SoftReference[AnyRef](new Array[Byte](64), queue))

  @Test def keepsReferentAcrossCollections(): Unit = {
    assumeFalse(
      "The JVM may clear soft references whenever it collects",
      Platform.executingInJVM
    )
    val queue = new ReferenceQueue[AnyRef]()
    val refs = allocSoftRefs(queue, 3)
    clobberStack(10000)
    for (_ <- 0 until 3) {
      locally {
        val _ = Seq.fill(1000)(new Object {})
      }
      System.gc()
      Thread.sleep(50)
    }
    refs.foreach { ref =>
      assertNotNull("softly reachable referent cleared", ref.get())
    }
    assertNull("softly reachable referent enqueued", queue.poll())
  }

  @Test def clearAndEnqueue(): Unit = {
    val queue = new ReferenceQueue[AnyRef]()
    val referent = new Object
    val ref = new SoftReference[AnyRef](referent, queue)
    assertSame(referent, ref.get())
    ref.clear()
    assertNull("get() after clear", ref.get())
    assertNull("clear() does not enqueue", queue.poll())
    assertTrue("enqueue() succeeds once", ref.enqueue())
    assertFalse("enqueue() fails the second time", ref.enqueue())
    assertSame(ref, queue.poll())
  }

  @Test def withoutQueue(): Unit = {
    val referent = new Object
    val ref = new SoftReference[AnyRef](referent)
    assertSame(referent, ref.get())
    assertFalse("never enqueued without a queue", ref.enqueue())
  }
}
