package scala.scalanative.runtime.gc

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

class FakeStackRootTest {
  import FakeStackRootTest._

  @Test def stackallocRootSurvivesGC(): Unit = {
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    assumeTrue("weak references", LinktimeInfo.isWeakReferenceSupported)

    val ref = hold()
    assertNotNull("object in stackalloc was collected", ref.get())
  }
}

object FakeStackRootTest {
  final class Box(val value: Long)

  @noinline def store(slot: Ptr[Ptr[Byte]]): WeakReference[Box] = {
    val obj = new Box(42L)
    !slot = fromRawPtr[Byte](Intrinsics.castObjectToRawPtr(obj))
    new WeakReference(obj) // not a GC root, so the object can be collected
  }

  @noinline def hold(): WeakReference[Box] = {
    val slot = stackalloc[Ptr[Byte]]()
    val ref = store(slot)
    assumeTrue(
      "ASAN_OPTIONS=detect_stack_use_after_return=1 is required",
      !LinktimeInfo.asanEnabled || Native.inFakeStack(slot) != 0
    )
    Native.collect(slot)
    ref
  }

  @extern private object Native {
    @name("scalanative_test_slot_is_in_fake_stack")
    def inFakeStack(slot: Ptr[Ptr[Byte]]): CInt = extern

    @name("scalanative_test_collect_with_escaped_slot")
    def collect(slot: Ptr[Ptr[Byte]]): Unit = extern
  }
}
