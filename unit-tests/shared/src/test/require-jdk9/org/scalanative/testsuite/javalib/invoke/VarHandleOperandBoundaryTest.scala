package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{VarHandle, WrongMethodTypeException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleOperandBoundaryTest {
  private class ReferenceBox {
    var value: String = "initial"
    val handle: VarHandle = java.lang.invoke.MethodHandles
      .lookup()
      .findVarHandle(classOf[ReferenceBox], "value", classOf[String])
  }

  @Test def mixedReferenceCasChecksTheDeclaredFieldType(): Unit = {
    val box = new ReferenceBox
    val invalid: AnyRef = java.lang.Integer.valueOf(9)
    assertThrows(
      classOf[ClassCastException],
      box.handle.compareAndSet(box, "initial", invalid)
    )
    assertEquals("initial", box.value)
    val valid: AnyRef = "desired"
    assertTrue(box.handle.compareAndSet(box, "initial", valid))
    assertEquals("desired", box.value)
  }

  @Test def widenedRmwReturnsTheFieldTypeNotTheOperandType(): Unit = {
    val box = new VarHandleLongInstanceFixture
    box.value = 9007199254740993L
    val witness: Long = box.handle.getAndAdd(box, 2)
    assertEquals(9007199254740993L, witness)
    assertEquals(9007199254740995L, box.value)
    val boxed: AnyRef = box.handle.getAndAdd(box, java.lang.Integer.valueOf(2))
    assertEquals(java.lang.Long.valueOf(9007199254740995L), boxed)
    assertEquals(9007199254740997L, box.value)
  }

  @Test def mixedCasOperandsConvertIndependently(): Unit = {
    val box = new VarHandleLongInstanceFixture
    box.value = 7L
    assertTrue(
      box.handle.compareAndSet(box, 7, java.lang.Short.valueOf(9.toShort))
    )
    val failed: Long =
      box.handle.compareAndExchange(box, java.lang.Integer.valueOf(7), 11L)
    assertEquals(9L, failed)
    assertEquals(9L, box.value)
    val witness: Long = box.handle.compareAndExchange(
      box,
      9.toByte,
      java.lang.Integer.valueOf(11)
    )
    assertEquals(9L, witness)
    assertEquals(11L, box.value)
  }

  @Test def failedDesiredConversionDoesNotMutateOrCompare(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 7
    val invalid: AnyRef = "not a number"
    assertThrows(
      classOf[ClassCastException],
      box.handle.compareAndSet(box, 0, invalid)
    )
    assertEquals(7, box.value)
    assertThrows(
      classOf[ClassCastException],
      { val witness: Int = box.handle.compareAndExchange(box, 7, invalid) }
    )
    assertEquals(7, box.value)
  }

  @Test def nullOperandsThrowWithoutMutation(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 7
    val absent: java.lang.Integer = null
    assertThrows(classOf[NullPointerException], box.handle.set(box, absent))
    assertThrows(
      classOf[NullPointerException],
      box.handle.compareAndSet(box, absent, 9)
    )
    assertThrows(
      classOf[NullPointerException],
      box.handle.compareAndSet(box, 0, absent)
    )
    assertEquals(7, box.value)
  }

  @Test def staticallyImpossibleUnboxingThrowsWrongMethodType(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 7
    assertThrows(
      classOf[WrongMethodTypeException],
      box.handle.set(box, java.lang.Long.valueOf(9L))
    )
    assertThrows(classOf[WrongMethodTypeException], box.handle.set(box, "9"))
    assertThrows(classOf[WrongMethodTypeException], box.handle.set(box, true))
    assertEquals(7, box.value)
  }

  @Test def erasedInvalidWrapperThrowsClassCast(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 7
    val tooWide: AnyRef = java.lang.Long.valueOf(9L)
    assertThrows(classOf[ClassCastException], box.handle.set(box, tooWide))
    assertEquals(7, box.value)
  }

  @Test def byteAndShortCannotWidenToChar(): Unit = {
    val box = new VarHandleCharInstanceFixture
    box.value = 'a'
    assertThrows(
      classOf[WrongMethodTypeException],
      box.handle.set(box, 98.toByte)
    )
    assertThrows(
      classOf[WrongMethodTypeException],
      box.handle.set(box, java.lang.Short.valueOf(98.toShort))
    )
    assertEquals('a', box.value)
  }

  @Test def invalidWitnessSignaturePreventsMutation(): Unit = {
    val box = new VarHandleLongInstanceFixture
    box.value = 7L
    def narrowing(): Int = box.handle.getAndAdd(box, 2)
    def wrongWrapper(): java.lang.Integer =
      box.handle.getAndAdd(box, java.lang.Integer.valueOf(2))
    assertThrows(classOf[WrongMethodTypeException], narrowing())
    assertThrows(classOf[WrongMethodTypeException], wrongWrapper())
    assertEquals(7L, box.value)
  }

  @Test def argumentsAreEvaluatedOnceInSourceOrder(): Unit = {
    val box = new VarHandleLongInstanceFixture
    box.value = 7L
    var events = ""
    def handle(): VarHandle = { events += "h"; box.handle }
    def coordinate(): AnyRef = { events += "c"; box }
    def expected(): Int = { events += "e"; 7 }
    def desired(): java.lang.Integer = {
      events += "d"; java.lang.Integer.valueOf(9)
    }
    val witness: Long =
      handle().compareAndExchange(coordinate(), expected(), desired())
    assertEquals(7L, witness)
    assertEquals(9L, box.value)
    assertEquals("hced", events)
  }

  @Test def numberTypedOperandsCanUnboxAndWiden(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    box.value = 7.0d
    val operand: java.lang.Number = java.lang.Integer.valueOf(2)
    val witness: Double = box.handle.getAndAdd(box, operand)
    assertEquals(7.0d, witness, 0.0d)
    assertEquals(9.0d, box.value, 0.0d)
  }
}
