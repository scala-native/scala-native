package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{VarHandle, WrongMethodTypeException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleWideningBoundaryTest {
  @Test def widensReadsThroughUnknownHandlesWithoutIntermediateFloat(): Unit = {
    def read(handle: VarHandle, receiver: AnyRef): Double =
      handle.getAcquire(receiver)
    val int = new VarHandleIntInstanceFixture
    int.value = 16777217
    assertEquals(16777217.0d, read(int.handle, int), 0.0d)
    val long = new VarHandleLongInstanceFixture
    long.value = 16777217L
    assertEquals(16777217.0d, read(long.handle, long), 0.0d)
    val float = new VarHandleFloatInstanceFixture
    float.handle.set(float, java.lang.Float.intBitsToFloat(Int.MinValue))
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(float.value))
    assertEquals(
      Long.MinValue,
      java.lang.Double.doubleToRawLongBits(read(float.handle, float))
    )
  }

  @Test def widenedRmwEvaluatesArgumentsExactlyOnce(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 16777217
    var events = ""
    def handle(): VarHandle = { events += "h"; box.handle }
    def receiver(): AnyRef = { events += "c"; box }
    def value(): Int = { events += "v"; 2 }
    val witness: Double = handle().getAndAdd(receiver(), value())
    assertEquals("hcv", events)
    assertEquals(16777217.0d, witness, 0.0d)
    assertEquals(16777219, box.value)
  }

  @Test def wideningDoesNotChangeIntegralOverflow(): Unit = {
    val byte = new VarHandleByteInstanceFixture
    byte.value = Byte.MaxValue
    val beforeByte: Double = byte.handle.getAndAdd(byte, 1.toByte)
    assertEquals(127.0d, beforeByte, 0.0d)
    assertEquals(Byte.MinValue, byte.value)
    val int = new VarHandleIntInstanceFixture
    int.value = Int.MaxValue
    val beforeInt: Float = int.handle.getAndAdd(int, 1)
    assertEquals(Int.MaxValue.toFloat, beforeInt, 0.0f)
    assertEquals(Int.MinValue, int.value)
    val char = new VarHandleCharInstanceFixture
    char.value = Char.MaxValue
    val beforeChar: Long = char.handle.getAndAdd(char, 1.toChar)
    assertEquals(65535L, beforeChar)
    assertEquals(0.toChar, char.value)
  }

  @Test def floatSpecialValuesSurviveWidening(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    for (value <- List(
          Float.NaN,
          Float.PositiveInfinity,
          Float.NegativeInfinity,
          java.lang.Float.intBitsToFloat(Int.MinValue)
        )) {
      box.value = value
      val read: Double = box.handle.getVolatile(box)
      val witness: Double = box.handle.getAndSetRelease(box, 1.0f)
      if (value.isNaN) {
        assertTrue(read.isNaN)
        assertTrue(witness.isNaN)
      } else {
        val expected = java.lang.Double.doubleToRawLongBits(value.toDouble)
        assertEquals(expected, java.lang.Double.doubleToRawLongBits(read))
        assertEquals(expected, java.lang.Double.doubleToRawLongBits(witness))
      }
      assertEquals(1.0f, box.value, 0.0f)
    }
  }

  @Test def rejectedNarrowingEvaluatesArgumentsWithoutMutation(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 4
    var events = ""
    def handle(): VarHandle = { events += "h"; box.handle }
    def receiver(): AnyRef = { events += "c"; box }
    def expected(): Int = { events += "e"; 4 }
    def desired(): Int = { events += "d"; 5 }
    def invoke(): Short =
      handle().compareAndExchange(receiver(), expected(), desired())
    assertThrows(classOf[WrongMethodTypeException], invoke())
    assertEquals("hced", events)
    assertEquals(4, box.value)
  }
}
