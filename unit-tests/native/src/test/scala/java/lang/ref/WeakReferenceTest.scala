package java.lang.ref

import java.lang.ref._

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.runtime.GC

class WeakReferenceTest {

  case class A()

  @noinline def alloc(referenceQueue: ReferenceQueue[A]): WeakReference[A] = {
    var a = A()
    val weakRef = new WeakReference(a, referenceQueue)
    assertEquals("get() should return object reference", weakRef.get(), A())
    a = null
    weakRef
  }

  def forceGC(afterGC: => Unit): Unit = {
    var i = 0
    while (i <= 3) {
      if (i == 0) {
        GC.collect()
      }

      // We do not want to put the reference on stack
      // during GC, so we hide it behind an if block
      if (i == 3) {
        afterGC
      }
      i += 1
    }
  }

  @Test def referencesNullAfterGC(): Unit = {
    val weakRef = alloc(null)

    forceGC { assertEquals(weakRef.get(), null) }
  }

  @Test def addsToReferenceQueueAfterGC(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val weakRef1 = alloc(refQueue)
    val weakRef2 = alloc(refQueue)
    val weakRefList = List(weakRef1, weakRef2)

    forceGC {
      assertEquals(weakRef1.get(), null)
      assertEquals(weakRef2.get(), null)
      val a = refQueue.poll()
      val b = refQueue.poll()
      assertTrue(weakRefList.contains(a))
      assertTrue(weakRefList.contains(b))
      assertNotEquals(a, b)
      assertEquals(refQueue.poll(), null)
    }
  }

  @Test def clear(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val a = A()
    val weakRef = new WeakReference(a, refQueue)

    assertEquals(refQueue.poll(), null)

    weakRef.clear()
    assertEquals(weakRef.get(), null)
    assertEquals(refQueue.poll(), weakRef)
    assertEquals(refQueue.poll(), null)
  }

  @Test def enqueue(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val a = A()
    val weakRef = new WeakReference(a, refQueue)

    assertEquals(refQueue.poll(), null)

    weakRef.enqueue()
    assertEquals(weakRef.get(), a)
    assertEquals(refQueue.poll(), weakRef)
    assertEquals(refQueue.poll(), null)
  }

}
