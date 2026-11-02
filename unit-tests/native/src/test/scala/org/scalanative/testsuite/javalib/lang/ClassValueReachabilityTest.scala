package org.scalanative.testsuite.javalib.lang

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Before, Test}

import org.scalanative.testsuite.utils.Platform

// Classes are never collected, so values are owned by their ClassValue.
class ClassValueReachabilityTest {
  import ClassValueReachabilityTest._

  @Before def requiresWeakReferences(): Unit = {
    assumeTrue(
      "requires a GC with weak references",
      Platform.isWeakReferenceSupported
    )
    // The stack of a terminated thread can't retain the tested objects
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
  }

  // Control for the tests below, not related to ClassValue
  @Test def unreachableObjectIsCollected(): Unit =
    assertCollected(onOtherThread(new WeakReference(new Object)))

  @Test def valueLivesAsLongAsItsClassValue(): Unit = {
    val classValue = new Plain
    val value = onOtherThread(weakValueOf(classValue))

    collect()
    collect()
    assertNotNull(value.get())
    assertSame(value.get(), classValue.get(classOf[String]))
  }

  @Test def removedValueIsReleased(): Unit = {
    val classValue = new Plain
    val value = onOtherThread {
      val ref = weakValueOf(classValue)
      classValue.remove(classOf[String])
      ref
    }

    assertCollected(value)
    assertNotNull(classValue.get(classOf[String]))
  }

  @Test def valueDiesWithItsClassValue(): Unit =
    assertCollected(onOtherThread(weakValueOf(new Plain)))

  // Would be retained forever if the table was referenced by a static Class
  @Test def valueReferringToItsClassValueDiesWithIt(): Unit =
    assertCollected(onOtherThread(weakValueOf(new SelfReferring)))
}

object ClassValueReachabilityTest {
  class Plain extends ClassValue[AnyRef] {
    protected def computeValue(cls: Class[_]): AnyRef = new Object
  }

  class SelfReferring extends ClassValue[AnyRef] {
    protected def computeValue(cls: Class[_]): AnyRef = new Holder(this)
  }

  class Holder(val owner: ClassValue[AnyRef])

  @noinline private def weakValueOf(
      classValue: ClassValue[AnyRef]
  ): WeakReference[AnyRef] =
    new WeakReference(classValue.get(classOf[String]))

  private def onOtherThread[T <: AnyRef](compute: => T): T = {
    var result: T = null.asInstanceOf[T]
    val thread = new Thread(() => result = compute)
    thread.start()
    thread.join()
    result
  }

  @noinline private def isCleared(ref: WeakReference[AnyRef]): Boolean =
    ref.get() eq null

  @noinline private def collect(): Unit = {
    Seq.fill(1000)(new Array[Byte](64))
    System.gc()
    Thread.sleep(10)
  }

  private def assertCollected(ref: WeakReference[AnyRef]): Unit = {
    var attempts = 100
    while (!isCleared(ref) && attempts > 0) {
      collect()
      attempts -= 1
    }
    assertTrue("the object was not collected", isCleared(ref))
  }
}
