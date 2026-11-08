package scala.scalanative.runtime.gc

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

class BlockingCallRootsTest {
  import BlockingCallRootsTest._

  // Issue #5162
  @Test def objectHeldInCalleeSavedRegistersSurvivesBlockingCall(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    assumeTrue("weak references", LinktimeInfo.isWeakReferenceSupported)
    assumeTrue("requires aarch64 or x86_64", Native.supported() != 0)

    val slot = stackalloc[Ptr[Byte]]()
    val ref = storeFreshObject(slot)
    Native.holdInCalleeSavedRegisters(slot)
    assertNotNull("object was collected during the blocking call", ref.get())
  }
}

object BlockingCallRootsTest {
  final class Box(val value: Long)

  @noinline def storeFreshObject(slot: Ptr[Ptr[Byte]]): WeakReference[Box] = {
    val obj = new Box(42L)
    !slot = fromRawPtr[Byte](Intrinsics.castObjectToRawPtr(obj))
    new WeakReference(obj)
  }

  @extern private object Native {
    @name("scalanative_test_blocking_call_roots_supported")
    def supported(): CInt = extern

    @name("scalanative_test_hold_in_callee_saved_registers")
    def holdInCalleeSavedRegisters(slot: Ptr[Ptr[Byte]]): Unit = extern
  }
}
