package org.scalanative.testsuite.javalib.lang.ref

import java.lang.ref._

import org.junit.Assert._
import org.junit.Test

class ReferenceRefersToTestOnJDK16 {

  private def checkRefersTo(
      kind: String,
      referent: AnyRef,
      ref: Reference[AnyRef]
  ): Unit = {
    assertTrue(s"$kind refers to its referent", ref.refersTo(referent))
    assertFalse(s"$kind refers to another object", ref.refersTo(new Object))
    assertFalse(s"$kind refers to null", ref.refersTo(null))
    ref.clear()
    assertFalse(s"$kind refers to its referent after clear", ref.refersTo(referent))
    assertTrue(s"$kind refers to null after clear", ref.refersTo(null))
  }

  @Test def weakReference(): Unit = {
    val referent = new Object
    checkRefersTo("WeakReference", referent, new WeakReference(referent))
  }

  @Test def softReference(): Unit = {
    val referent = new Object
    checkRefersTo("SoftReference", referent, new SoftReference(referent))
  }

  @Test def phantomReference(): Unit = {
    val referent = new Object
    val queue = new ReferenceQueue[AnyRef]()
    val ref = new PhantomReference[AnyRef](referent, queue)
    assertNull("get() of a phantom reference", ref.get())
    checkRefersTo("PhantomReference", referent, ref)
  }

  @Test def nullReferent(): Unit = {
    val ref = new WeakReference[AnyRef](null)
    assertTrue(ref.refersTo(null))
    assertFalse(ref.refersTo(new Object))
  }

  @Test def enqueueClears(): Unit = {
    val referent = new Object
    val queue = new ReferenceQueue[AnyRef]()
    val ref = new WeakReference[AnyRef](referent, queue)
    assertTrue(ref.enqueue())
    assertTrue("enqueue clears the referent", ref.refersTo(null))
  }
}
