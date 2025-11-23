package org.scalanative.testsuite.javalib.lang.ref

import java.lang.ref._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

// "AfterGC" tests are very sensitive to optimizations,
// both by Scala Native and LLVM.
class WeakReferenceTestOnJDK9 {

  case class A()
  class SubclassedWeakRef[A](a: A, referenceQueue: ReferenceQueue[A])
      extends WeakReference[A](a, referenceQueue)

  def gcAssumption(): Unit = {
    assumeTrue(
      "WeakReferences work only on Commix and Immix GC",
      Platform.isWeakReferenceSupported
    )
  }

  @noinline def allocStrongRef = A()

  @noinline def allocWeakRef(
      referenceQueue: ReferenceQueue[A],
      factory: (A, ReferenceQueue[A]) => WeakReference[A]
  ): WeakReference[A] = {
    val weakRef = factory(allocStrongRef, referenceQueue)
    assertNotNull("get() should return object reference", weakRef.get())
    weakRef
  }

  @deprecated @Test def addsToReferenceQueueAfterGC(): Unit = {
    assumeFalse(
      "In the CI Scala 3 sometimes SN fails to clean weak references in some of Windows build configurations",
      sys.env.contains("CI") && Platform.isWindows
    )

    @noinline def assertEventuallyIsCollected(
        clue: String,
        ref: WeakReference[_],
        deadline: Long
    ): Unit = {
      ref.get() match {
        case null =>
          val waitForEnqueue = 0
            .until(10)
            .iterator
            .map(_ => Thread.sleep(100))
            .takeWhile(_ => !ref.isEnqueued())
            .foreach(_ => ())
          assertTrue("collected but not enqueued", ref.isEnqueued())
        case v =>
          if (System.currentTimeMillis() < deadline) {
            // Give GC something to collect
            locally {
              val _ = Seq.fill(1000)(new Object {})
            }
            System.gc()
            Thread.sleep(200)
            assertEventuallyIsCollected(clue, ref, deadline)
          } else {
            fail(
              s"$clue - expected that WeakReference would be collected, but it contains value ${v}"
            )
          }
      }
    }

    gcAssumption()
    val refQueue = new ReferenceQueue[A]()
    val weakRef1 = allocWeakRef(refQueue, new WeakReference(_, _))
    val weakRef2 = allocWeakRef(refQueue, new WeakReference(_, _))
    val weakRef3 = allocWeakRef(refQueue, new SubclassedWeakRef(_, _))
    val weakRefList = List(weakRef1, weakRef2, weakRef3)
    // Clobber the stack from possible stale references to allocated objects
    def recurse(n: Int): Unit =
      if (n > 0) recurse(n - 1)
      else ()
    recurse(10000)

    System.gc()
    def newDeadline() = System.currentTimeMillis() + 60 * 1000
    assertEventuallyIsCollected("weakRef1", weakRef1, deadline = newDeadline())
    assertEventuallyIsCollected("weakRef2", weakRef2, deadline = newDeadline())
    assertEventuallyIsCollected("weakRef3", weakRef3, deadline = newDeadline())

    assertEquals("weakRef1", null, weakRef1.get())
    assertEquals("weakRef2", null, weakRef2.get())
    assertEquals("weakRef3", null, weakRef3.get())
    val a = refQueue.poll()
    assertNotNull("a was null", a)
    val b = refQueue.poll()
    assertNotNull("b was null", b)
    val c = refQueue.poll()
    assertNotNull("c was null", c)
    assertTrue("!contains a", weakRefList.contains(a))
    assertTrue("!contains b", weakRefList.contains(b))
    assertTrue("!contains c", weakRefList.contains(c))
    def allDistinct(list: List[_]): Unit = list match {
      case head :: next =>
        next.foreach(assertNotEquals(_, head)); allDistinct(next)
      case Nil => ()
    }
    allDistinct(List(a, b, c))
    assertEquals("pool not null", null, refQueue.poll())
  }

  @Test def clear(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val a = A()
    val weakRef = new WeakReference(a, refQueue)

    assertEquals("queue empty before clear", refQueue.poll(), null)
    assertEquals("weakref was non-null before clear", weakRef.get(), a)

    weakRef.clear()
    assertEquals("queue empty after clear", refQueue.poll(), null)
    assertEquals("weakref was null after clear", weakRef.get(), null)
  }

  @Test def enqueue(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val a = A()
    val weakRef = new WeakReference(a, refQueue)

    assertEquals("queue empty before enqueue", refQueue.poll(), null)
    assertEquals("weakref was non-null before enqueue", weakRef.get(), a)

    weakRef.enqueue()
    assertEquals("queue had weakref after enqueue", refQueue.poll(), weakRef)
    assertEquals("queue had only weakref after enqueue", refQueue.poll(), null)
    assertEquals("weakref was null after enqueue", weakRef.get(), null)
  }

}
