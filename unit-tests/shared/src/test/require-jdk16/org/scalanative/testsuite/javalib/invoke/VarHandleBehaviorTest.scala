package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{MethodHandles, VarHandle, WrongMethodTypeException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBehaviorBox { var value: Int = 7 }
class VarHandleBehaviorChild extends VarHandleBehaviorBox

class VarHandleBehaviorTest {
  private val handle = MethodHandles
    .privateLookupIn(classOf[VarHandleBehaviorBox], MethodHandles.lookup())
    .findVarHandle(classOf[VarHandleBehaviorBox], "value", classOf[Int])

  @Test def viewsAreImmutableAndShareTheField(): Unit = {
    val exact = handle.withInvokeExactBehavior()
    assertFalse(handle.hasInvokeExactBehavior())
    assertTrue(exact.hasInvokeExactBehavior())
    assertSame(handle, handle.withInvokeBehavior())
    assertSame(exact, exact.withInvokeExactBehavior())
    assertFalse(exact.withInvokeBehavior().hasInvokeExactBehavior())
    val box = new VarHandleBehaviorBox
    exact.set(box, 11)
    val value: Int = handle.get(box)
    assertEquals(11, value)
    assertEquals(handle.varType(), exact.varType())
    assertEquals(handle.coordinateTypes(), exact.coordinateTypes())
  }

  @Test def behaviorIsChosenByTheRuntimeHandle(): Unit = {
    val box = new VarHandleBehaviorBox
    def widened(h: VarHandle): Long = h.get(box)
    def boxed(h: VarHandle): AnyRef = h.get(box)
    assertEquals(7L, widened(handle))
    assertEquals(java.lang.Integer.valueOf(7), boxed(handle))
    val exact = handle.withInvokeExactBehavior()
    assertThrows(classOf[WrongMethodTypeException], widened(exact))
    assertThrows(classOf[WrongMethodTypeException], boxed(exact))
    assertEquals(7L, widened(exact.withInvokeBehavior()))
  }

  @Test def exactCoordinatesUseDeclaredTypes(): Unit = {
    val exact = handle.withInvokeExactBehavior()
    val child = new VarHandleBehaviorChild
    def readChild(): Int = exact.get(child)
    assertThrows(classOf[WrongMethodTypeException], readChild())
    val base: VarHandleBehaviorBox = child
    val value: Int = exact.get(base)
    assertEquals(7, value)
    val erased: AnyRef = base
    def readErased(): Int = exact.get(erased)
    assertThrows(classOf[WrongMethodTypeException], readErased())
  }

  @Test def argumentsAreEvaluatedOnceBeforeExactValidation(): Unit = {
    val box = new VarHandleBehaviorBox
    val exact = handle.withInvokeExactBehavior()
    var events = List.empty[String]
    def receiver(): VarHandle = { events = events :+ "handle"; exact }
    def coordinate(): VarHandleBehaviorBox = {
      events = events :+ "coordinate"; box
    }
    def operand(): java.lang.Integer = {
      events = events :+ "operand"; java.lang.Integer.valueOf(9)
    }
    assertThrows(
      classOf[WrongMethodTypeException],
      receiver().set(coordinate(), operand())
    )
    assertEquals(List("handle", "coordinate", "operand"), events)
    assertEquals(7, box.value)
    events = Nil
    val value: Int = receiver().get(coordinate())
    assertEquals(7, value)
    assertEquals(List("handle", "coordinate"), events)
  }

  @Test def exactNullCoordinateChecksItsDeclaredTypeBeforeDereferencing()
      : Unit = {
    val exact = handle.withInvokeExactBehavior()
    val typed: VarHandleBehaviorBox = null
    def readTyped(): Int = exact.get(typed)
    assertThrows(classOf[NullPointerException], readTyped())
    val erased: AnyRef = null
    def readErased(): Int = exact.get(erased)
    assertThrows(classOf[WrongMethodTypeException], readErased())
  }

  @Test def referenceHandlesRequireExactReferenceDescriptors(): Unit = {
    val reference = MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(
        classOf[VarHandleMetadataBox],
        "referenceValue",
        classOf[java.lang.Integer]
      )
    val exact = reference.withInvokeExactBehavior()
    val box = new VarHandleMetadataBox
    val integer = java.lang.Integer.valueOf(23)
    exact.set(box, integer)
    val read: java.lang.Integer = exact.get(box)
    assertSame(integer, read)
    def erasedRead(): AnyRef = exact.get(box)
    def unboxedRead(): Int = exact.get(box)
    assertThrows(classOf[WrongMethodTypeException], erasedRead())
    assertThrows(classOf[WrongMethodTypeException], unboxedRead())
    val adapted: Int = exact.withInvokeBehavior().get(box)
    assertEquals(23, adapted)
  }

  @Test def exactOperandsAndDiscardedResultsAreCheckedBeforeMutation(): Unit = {
    val exact = handle.withInvokeExactBehavior()
    val box = new VarHandleBehaviorBox
    val boxed = java.lang.Integer.valueOf(1)
    assertThrows(classOf[WrongMethodTypeException], exact.set(box, boxed))
    assertThrows(classOf[WrongMethodTypeException], exact.set(box, ()))
    assertThrows(
      classOf[WrongMethodTypeException],
      { exact.getAndAdd(box, 1); () }
    )
    assertEquals(7, box.value)
    val old: Int = exact.getAndAdd(box, 2)
    assertEquals(7, old)
    val changed: Boolean = exact.compareAndSet(box, 9, 12)
    assertTrue(changed)
    assertEquals(12, box.value)
    // CAS has a fixed boolean return descriptor even in statement position.
    exact.compareAndSet(box, 12, 13)
    assertEquals(13, box.value)
  }
}
