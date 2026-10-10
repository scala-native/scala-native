package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{VarHandle, WrongMethodTypeException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBoxingBoundaryTest {
  @Test def inferredAndUniversalResultsUseTheFieldWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 17
    def inferred(handle: VarHandle) = handle.get(box)
    def universal(handle: VarHandle): Any = handle.getAcquire(box)
    def comparable(handle: VarHandle): java.lang.Comparable[_] =
      handle.getVolatile(box).asInstanceOf[java.lang.Comparable[_]]
    assertEquals(java.lang.Integer.valueOf(17), inferred(box.handle))
    assertEquals(java.lang.Integer.valueOf(17), universal(box.handle))
    assertEquals(java.lang.Integer.valueOf(17), comparable(box.handle))
    val witness: Any = box.handle.getAndAdd(box, 2)
    assertEquals(java.lang.Integer.valueOf(17), witness)
    assertEquals(19, box.value)
  }

  // An Int witness boxes to Integer, not Double. Both platforms reject this.
  @Test def wrongWrapperResultEvaluatesArgumentsWithoutMutation(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 7
    var events = ""
    def handle(): VarHandle = { events += "h"; box.handle }
    def coordinate(): AnyRef = { events += "c"; box }
    def value(): Int = { events += "v"; 2 }
    def invoke(): java.lang.Double = handle().getAndAdd(coordinate(), value())
    assertThrows(classOf[WrongMethodTypeException], invoke())
    assertEquals("hcv", events)
    assertEquals(7, box.value)
  }

  @Test def unboxesOperandsAndBoxesWitnesses(): Unit = {
    val box = new VarHandleIntInstanceFixture
    box.value = 7
    def invoke(): AnyRef =
      box.handle.getAndAdd(box, java.lang.Integer.valueOf(2))
    assertEquals(java.lang.Integer.valueOf(7), invoke())
    assertEquals(9, box.value)
  }
}
