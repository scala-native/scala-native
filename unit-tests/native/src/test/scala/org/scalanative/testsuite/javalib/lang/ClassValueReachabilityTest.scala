package org.scalanative.testsuite.javalib.lang

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Before, Test}

import org.scalanative.testsuite.utils.Platform

class ClassValueReachabilityTest {
  import ClassValueReachabilityTest._

  @Before def requiresWeakReferences(): Unit = {
    assumeTrue(
      "requires a GC with weak references",
      Platform.isWeakReferenceSupported
    )
    // Objects are created on threads that exit, so no stack retains them
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    // Conservative scanning can retain any object on some platforms
    assumeTrue(
      "unreachable objects are not reliably collected",
      isCollected(onOtherThread(new WeakReference(new Object)))
    )
  }

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

  // Would leak if Class held the table, as on the JVM
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

  private def isCollected(ref: WeakReference[AnyRef]): Boolean = {
    var attempts = 100
    while (!isCleared(ref) && attempts > 0) {
      collect()
      attempts -= 1
    }
    isCleared(ref)
  }

  private def assertCollected(ref: WeakReference[AnyRef]): Unit =
    assertTrue("the object was not collected", isCollected(ref))
}
