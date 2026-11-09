package scala.scalanative.runtime.gc

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

class DeepStackScanTest {
  import DeepStackScanTest._

  // Issue #5086
  @Test def framesBelowSoftStackLimitAreScanned(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    assumeTrue("weak references", LinktimeInfo.isWeakReferenceSupported)
    assumeTrue("Linux", LinktimeInfo.isLinux && !LinktimeInfo.isAndroid)

    survived = false
    ran = false
    val thread =
      new Thread(() => ran = Native.callBelowSoftLimit(collectFromSlot) != 0)
    thread.start()
    thread.join()
    assumeTrue("thread stack does not extend past its soft limit", ran)
    assertTrue(
      "object reachable only from a deep frame was collected",
      survived
    )
  }
}

object DeepStackScanTest {
  final class Box(val value: Long)

  @volatile var survived = false
  @volatile var ran = false

  val collectFromSlot: CFuncPtr1[Ptr[Ptr[Byte]], Unit] =
    CFuncPtr1.fromScalaFunction { (slot: Ptr[Ptr[Byte]]) =>
      val ref = storeFreshObject(slot)
      System.gc()
      survived = ref.get() != null
    }

  @noinline def storeFreshObject(slot: Ptr[Ptr[Byte]]): WeakReference[Box] = {
    val obj = new Box(42L)
    !slot = fromRawPtr[Byte](Intrinsics.castObjectToRawPtr(obj))
    new WeakReference(obj)
  }

  @extern private object Native {
    @name("scalanative_test_call_below_soft_limit")
    def callBelowSoftLimit(callback: CFuncPtr1[Ptr[Ptr[Byte]], Unit]): CInt =
      extern
  }
}
