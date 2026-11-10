package scala.scalanative.runtime.gc

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr}
import scala.scalanative.unsafe._

class CalleeSavedRegistersRootsTest {
  import CalleeSavedRegistersRootsTest._

  // Issue #3172
  @Test def objectsHeldOnlyInCalleeSavedRegistersSurviveCollection(): Unit = {
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    assumeTrue("weak references", LinktimeInfo.isWeakReferenceSupported)
    assumeTrue("requires aarch64 or x86_64", Native.supported() != 0)

    val slots = stackalloc[Ptr[Byte]](2)
    val first = storeFreshObject(slots, 0, 0x5a5a5a5a5a5a5a5aL)
    val second = storeFreshObject(slots, 1, 0x3c3c3c3c3c3c3c3cL)
    Native.collectHoldingInCalleeSavedRegisters(slots)

    assertNotNull("object held in r13/x28 was collected", first.get())
    assertNotNull("object held in rbp/x27 was collected", second.get())
    assertEquals(0x5a5a5a5a5a5a5a5aL, loadObject(slots, 0).value)
    assertEquals(0x3c3c3c3c3c3c3c3cL, loadObject(slots, 1).value)
  }
}

object CalleeSavedRegistersRootsTest {
  final class Box(val value: Long)

  @noinline def storeFreshObject(
      slots: Ptr[Ptr[Byte]],
      idx: Int,
      value: Long
  ): WeakReference[Box] = {
    val obj = new Box(value)
    slots(idx) = fromRawPtr[Byte](Intrinsics.castObjectToRawPtr(obj))
    new WeakReference(obj)
  }

  @noinline def loadObject(slots: Ptr[Ptr[Byte]], idx: Int): Box =
    Intrinsics.castRawPtrToObject(toRawPtr(slots(idx))).asInstanceOf[Box]

  @extern private object Native {
    @name("scalanative_test_callee_saved_roots_supported")
    def supported(): CInt = extern

    @name("scalanative_test_collect_holding_in_callee_saved_registers")
    def collectHoldingInCalleeSavedRegisters(slots: Ptr[Ptr[Byte]]): Unit =
      extern
  }
}
