package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{MethodHandles, VarHandle}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/** A stored field handle must retain signature-polymorphic typing at every
 *  access site. Keep the result type on the call itself: casting afterwards
 *  would not exercise the compiler's VarHandle typing path.
 */
class VarHandleTest {
  @Test def protectedLookupRestrictsReceiverToLookupSubclass(): Unit = {
    val child = new VarHandleProtectedChild
    val handle = child.handle
    val input = new java.io.ByteArrayInputStream(Array[Byte](1))
    handle.set(child, input)
    val stored: java.io.InputStream = handle.get(child)
    assertSame(input, stored)
    val sibling = new java.io.FilterInputStream(null) {}
    assertThrows(classOf[ClassCastException], handle.set(sibling, input))
  }

  @Test def instanceLookupDoesNotBindCompanionField(): Unit = {
    val box = new VarHandleCompanionCollision
    val handle = box.handle
    handle.set(box, 12)
    val previous: Int = handle.getAndAdd(box, 3)
    assertEquals(12, previous)
    assertEquals(15, box.current)
    assertEquals(99L, VarHandleCompanionCollision.value)
  }

  @Test def incompatibleReadThrowsWrongMethodType(): Unit = {
    val box = new VarHandleTest.Box
    box.intValue = 37
    def read(handle: VarHandle): Short = handle.get(box)
    val failure = assertThrows(
      classOf[java.lang.invoke.WrongMethodTypeException],
      read(VarHandleTest.intHandle)
    )
    val message = failure.getMessage
    assertTrue(message, message.contains("int") || message.contains("Int"))
    assertTrue(message, message.contains("short") || message.contains("Short"))
    assertEquals(37, box.intValue)
  }

  @Test def incompatibleStoreThrowsWrongMethodTypeWithoutMutation(): Unit = {
    val box = new VarHandleTest.Box
    box.intValue = 37
    def write(handle: VarHandle): Unit = handle.set(box, 38L)
    assertThrows(
      classOf[java.lang.invoke.WrongMethodTypeException],
      write(VarHandleTest.intHandle)
    )
    assertEquals(37, box.intValue)
  }

  @Test def exactReadWorksThroughAnUnknownHandle(): Unit = {
    def read(handle: VarHandle, box: VarHandleTest.Box): Int = handle.get(box)
    val box = new VarHandleTest.Box
    VarHandleTest.intHandle.set(box, 37)
    assertEquals(37, read(VarHandleTest.intHandle, box))
  }

  @Test def widensAndBoxesPrimitiveResults(): Unit = {
    val box = new VarHandleTest.Box
    val handle = VarHandleTest.intHandle
    handle.set(box, 37)
    def boxed(): AnyRef = handle.get(box)
    def widened(): Double = handle.get(box)
    def widenedFloat(): Float = handle.get(box)
    assertEquals(java.lang.Integer.valueOf(37), boxed())
    assertEquals(37.0d, widened(), 0.0d)
    assertEquals(37.0f, widenedFloat(), 0.0f)
    val value: Int = handle.get(box)
    assertEquals(37, value)
  }

  @Test def widensStoreOperands(): Unit = {
    val box = new VarHandleTest.Box
    val handle = VarHandleTest.longHandle
    handle.set(box, 12L)
    def widenedStore(): Unit = handle.set(box, 13)
    widenedStore()
    assertEquals(13L, box.longValue)
  }

  @Test def discardedRmwStillUpdatesTheField(): Unit = {
    val box = new VarHandleTest.Box
    val handle = VarHandleTest.intHandle
    handle.set(box, 4)
    handle.getAndAdd(box, 3)
    assertEquals(7, box.intValue)
  }

  @Test def boxedRmwEvaluatesArgumentsExactlyOnce(): Unit = {
    val box = new VarHandleTest.Box
    var events = ""
    def handle(): VarHandle = { events += "h"; VarHandleTest.intHandle }
    def coordinate(): VarHandleTest.Box = { events += "c"; box }
    def value(): Int = { events += "v"; 3 }
    def invoke(): AnyRef = handle().getAndAdd(coordinate(), value())
    assertEquals(java.lang.Integer.valueOf(0), invoke())
    assertEquals(3, box.intValue)
    assertEquals("hcv", events)
  }

  @Test def discardedRmwEvaluatesArgumentsExactlyOnce(): Unit = {
    val box = new VarHandleTest.Box
    box.intValue = 4
    var events = ""
    def handle(): VarHandle = { events += "h"; VarHandleTest.intHandle }
    def coordinate(): VarHandleTest.Box = { events += "c"; box }
    def value(): Int = { events += "v"; 3 }
    handle().getAndAdd(coordinate(), value())
    assertEquals("hcv", events)
    assertEquals(7, box.intValue)
  }

  @Test def discardedOuterCallDoesNotDiscardAnOperandResult(): Unit = {
    val box = new VarHandleTest.Box
    box.intValue = 4
    val handle = VarHandleTest.intHandle
    def invoke(): Unit = handle.getAndAdd(
      box, {
        val boxed: AnyRef = handle.getAndAdd(box, 3)
        boxed.asInstanceOf[Int]
      }
    )
    invoke()
    assertEquals(11, box.intValue)
  }

  @Test def discardedRmwStillRejectsIncompatibleOperands(): Unit = {
    val box = new VarHandleTest.Box
    box.intValue = 4
    val handle = VarHandleTest.intHandle
    def invoke(): Unit = handle.compareAndExchange(box, 4, 5L)
    assertThrows(classOf[java.lang.invoke.WrongMethodTypeException], invoke())
    assertEquals(4, box.intValue)
  }

  @Test def discardedRmwInControlFlow(): Unit = {
    val box = new VarHandleTest.Box
    val handle = VarHandleTest.intHandle
    box.intValue = 4
    if (box.intValue == 4) handle.getAndAdd(box, 3)
    else handle.getAndAdd(box, 100)
    box.intValue match {
      case 7 => handle.getAndAdd(box, 2)
      case _ => handle.getAndAdd(box, 100)
    }
    try handle.getAndAdd(box, 1)
    finally assertEquals(10, box.intValue)
    assertEquals(10, box.intValue)
  }

  @Test def unsupportedTypeOperationsDoNotModifyTheField(): Unit = {
    val box = new VarHandleTest.Box
    val bool = VarHandleTest.booleanHandle
    val float = VarHandleTest.floatHandle
    bool.set(box, true)
    float.set(box, 2.0f)
    assertThrows(
      classOf[UnsupportedOperationException], {
        val old: Boolean = bool.getAndAdd(box, true)
      }
    )
    assertThrows(
      classOf[UnsupportedOperationException], {
        val old: Float = float.getAndBitwiseOr(box, 1.0f)
      }
    )
    assertTrue(box.booleanValue)
    assertEquals(2.0f, box.floatValue, 0.0f)
  }

  @Test def compareExchangeReturnsTheActualWitness(): Unit = {
    val box = new VarHandleTest.Box
    val handle = VarHandleTest.intHandle
    handle.set(box, 10)
    val failed: Int = handle.compareAndExchange(box, 9, 20)
    assertEquals(10, failed)
    assertEquals(10, box.intValue)
    val succeeded: Int = handle.compareAndExchangeRelease(box, 10, 30)
    assertEquals(10, succeeded)
    assertEquals(30, box.intValue)
  }

  @Test def floatingPointAdditionSupportsEveryMemoryOrder(): Unit = {
    val box = new VarHandleTest.Box
    val float = VarHandleTest.floatHandle
    val double = VarHandleTest.doubleHandle
    float.set(box, 1.0f)
    double.set(box, 1.0d)
    val f1: Float = float.getAndAdd(box, 0.5f)
    val f2: Float = float.getAndAddAcquire(box, 0.5f)
    val f3: Float = float.getAndAddRelease(box, 0.5f)
    val d1: Double = double.getAndAdd(box, 0.5d)
    val d2: Double = double.getAndAddAcquire(box, 0.5d)
    val d3: Double = double.getAndAddRelease(box, 0.5d)
    assertEquals(1.0f, f1, 0.0f)
    assertEquals(1.5f, f2, 0.0f)
    assertEquals(2.0f, f3, 0.0f)
    assertEquals(1.0d, d1, 0.0d)
    assertEquals(1.5d, d2, 0.0d)
    assertEquals(2.0d, d3, 0.0d)
    assertEquals(2.5f, box.floatValue, 0.0f)
    assertEquals(2.5d, box.doubleValue, 0.0d)
  }

  @Test def contendedFloatingPointAdditionDoesNotLoseUpdates(): Unit = {
    val box = new VarHandleTest.Box
    val float = VarHandleTest.floatHandle
    val double = VarHandleTest.doubleHandle
    val threads = Array.fill(4)(new Thread(new Runnable {
      def run(): Unit = {
        var i = 0
        while (i < 1000) {
          val f: Float = float.getAndAddRelease(box, 1.0f)
          val d: Double = double.getAndAddAcquire(box, 1.0d)
          i += 1
        }
      }
    }))
    threads.foreach(_.start())
    threads.foreach(_.join())
    assertEquals(4000.0f, box.floatValue, 0.0f)
    assertEquals(4000.0d, box.doubleValue, 0.0d)
  }

  @Test def storedInstanceHandleDispatchesEveryIntAccessMode(): Unit = {
    val box = new VarHandleTest.Box
    val handle = VarHandleTest.intHandle
    assertNotNull(handle)

    handle.set(box, 1)
    assertEquals(1, box.intValue)
    val plain: Int = handle.get(box)
    assertEquals(1, plain)
    handle.setOpaque(box, 2)
    val opaque: Int = handle.getOpaque(box)
    assertEquals(2, opaque)
    handle.setRelease(box, 3)
    val acquired: Int = handle.getAcquire(box)
    assertEquals(3, acquired)
    handle.setVolatile(box, 4)
    val volatile: Int = handle.getVolatile(box)
    assertEquals(4, volatile)

    assertTrue(handle.compareAndSet(box, 4, 5))
    assertFalse(handle.compareAndSet(box, 4, 6))
    val exchanged: Int = handle.compareAndExchange(box, 5, 6)
    assertEquals(5, exchanged)
    // Weak CAS may fail spuriously on either implementation.
    while (!handle.weakCompareAndSetPlain(box, 6, 7)) {}
    while (!handle.weakCompareAndSetAcquire(box, 7, 8)) {}
    while (!handle.weakCompareAndSetRelease(box, 8, 9)) {}

    val replaced: Int = handle.getAndSet(box, 10)
    assertEquals(9, replaced)
    val added: Int = handle.getAndAdd(box, 4)
    assertEquals(10, added)
    val ored: Int = handle.getAndBitwiseOr(box, 0x10)
    assertEquals(14, ored)
    val anded: Int = handle.getAndBitwiseAndAcquire(box, 0x1e)
    assertEquals(30, anded)
    val xored: Int = handle.getAndBitwiseXorRelease(box, 0x03)
    assertEquals(30, xored)
    assertEquals(29, box.intValue)
  }

  @Test def storedHandlesSupportNarrowAndLongRmw(): Unit = {
    val box = new VarHandleTest.Box

    assertNotNull(VarHandleTest.byteHandle)

    VarHandleTest.byteHandle.set(box, 126.toByte)
    val byteOld: Byte = VarHandleTest.byteHandle.getAndAdd(box, 2.toByte)
    assertEquals(126.toByte, byteOld)
    assertEquals((-128).toByte, box.byteValue)

    VarHandleTest.shortHandle.set(box, Short.MaxValue)
    val shortOld: Short = VarHandleTest.shortHandle.getAndAdd(box, 1.toShort)
    assertEquals(Short.MaxValue, shortOld)
    assertEquals(Short.MinValue, box.shortValue)

    VarHandleTest.charHandle.set(box, Char.MaxValue)
    val charOld: Char = VarHandleTest.charHandle.getAndAdd(box, 1.toChar)
    assertEquals(Char.MaxValue, charOld)
    assertEquals(0.toChar, box.charValue)

    VarHandleTest.longHandle.set(box, Long.MaxValue)
    val longOld: Long = VarHandleTest.longHandle.getAndAdd(box, 1L)
    assertEquals(Long.MaxValue, longOld)
    assertEquals(Long.MinValue, box.longValue)
  }

  @Test def booleanAndReferenceHandlesUseExpectedComparisonRules(): Unit = {
    val box = new VarHandleTest.Box
    val bool = VarHandleTest.booleanHandle
    bool.set(box, true)
    assertTrue(bool.compareAndSet(box, true, false))
    val boolOld: Boolean = bool.getAndBitwiseOr(box, true)
    assertFalse(boolOld)
    assertTrue(box.booleanValue)

    val ref = VarHandleTest.referenceHandle
    val first = new Object
    val equalButDistinct = new String("value")
    val sameValue = new String("value")
    ref.set(box, first)
    assertFalse(ref.compareAndSet(box, equalButDistinct, sameValue))
    assertTrue(ref.compareAndSet(box, first, null))
    val old: AnyRef = ref.getAndSet(box, sameValue)
    assertNull(old)
    val read: AnyRef = ref.getVolatile(box)
    assertSame(sameValue, read)
  }

  @Test def floatingPointHandlesCompareRawBits(): Unit = {
    val box = new VarHandleTest.Box

    VarHandleTest.floatHandle.set(box, -0.0f)
    assertFalse(VarHandleTest.floatHandle.compareAndSet(box, 0.0f, 1.0f))
    assertTrue(VarHandleTest.floatHandle.compareAndSet(box, -0.0f, Float.NaN))
    val floatOld: Float = VarHandleTest.floatHandle.getAndSet(box, 2.0f)
    assertTrue(floatOld.isNaN)

    VarHandleTest.doubleHandle.set(box, -0.0d)
    assertFalse(VarHandleTest.doubleHandle.compareAndSet(box, 0.0d, 1.0d))
    assertTrue(VarHandleTest.doubleHandle.compareAndSet(box, -0.0d, Double.NaN))
    val doubleOld: Double = VarHandleTest.doubleHandle.getAndSet(box, 2.0d)
    assertTrue(doubleOld.isNaN)
  }
}

class VarHandleCompanionCollision {
  private var value: Int = 0
  def current: Int = value
  def handle: VarHandle = MethodHandles
    .lookup()
    .findVarHandle(
      classOf[VarHandleCompanionCollision],
      "value",
      java.lang.Integer.TYPE
    )
}

class VarHandleProtectedChild extends java.io.FilterInputStream(null) {
  def handle: VarHandle = MethodHandles
    .lookup()
    .findVarHandle(
      classOf[java.io.FilterInputStream],
      "in",
      classOf[java.io.InputStream]
    )
}

object VarHandleCompanionCollision {
  var value: Long = 99L
}

object VarHandleTest {
  final class Box {
    private[invoke] var booleanValue: Boolean = false
    private[invoke] var byteValue: Byte = 0
    private[invoke] var shortValue: Short = 0
    private[invoke] var charValue: Char = 0
    private[invoke] var intValue: Int = 0
    private[invoke] var longValue: Long = 0L
    private[invoke] var floatValue: Float = 0.0f
    private[invoke] var doubleValue: Double = 0.0d
    private[invoke] var referenceValue: AnyRef = null

    /* Lookup executes in Box to retain private-field access, and the compiler
     * requires the member name at this call site. */
    private[invoke] def booleanHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "booleanValue", java.lang.Boolean.TYPE)
    private[invoke] def byteHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "byteValue", java.lang.Byte.TYPE)
    private[invoke] def shortHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "shortValue", java.lang.Short.TYPE)
    private[invoke] def charHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "charValue", java.lang.Character.TYPE)
    private[invoke] def intHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "intValue", java.lang.Integer.TYPE)
    private[invoke] def longHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "longValue", java.lang.Long.TYPE)
    private[invoke] def floatHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "floatValue", java.lang.Float.TYPE)
    private[invoke] def doubleHandle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "doubleValue", java.lang.Double.TYPE)
    private[invoke] def referenceHandle: VarHandle =
      MethodHandles
        .lookup()
        .findVarHandle(classOf[Box], "referenceValue", classOf[AnyRef])
  }

  private val lookupOwner = new Box
  val booleanHandle = lookupOwner.booleanHandle
  val byteHandle = lookupOwner.byteHandle
  val shortHandle = lookupOwner.shortHandle
  val charHandle = lookupOwner.charHandle
  val intHandle = lookupOwner.intHandle
  val longHandle = lookupOwner.longHandle
  val floatHandle = lookupOwner.floatHandle
  val doubleHandle = lookupOwner.doubleHandle
  val referenceHandle = lookupOwner.referenceHandle
}
