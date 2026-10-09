package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{MethodHandles, WrongMethodTypeException}

import scala.annotation.static

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StaticVarHandleBehaviorBox
object StaticVarHandleBehaviorBox {
  @static var value: Int = 0
}

class StaticVarHandleBehaviorTest {
  @Test def staticViewsHaveNoCoordinatesAndEnforceExactTypes(): Unit = {
    val handle = MethodHandles
      .lookup()
      .findStaticVarHandle(
        classOf[StaticVarHandleBehaviorBox],
        "value",
        classOf[Int]
      )
    val exact = handle.withInvokeExactBehavior()
    assertTrue(handle.coordinateTypes().isEmpty())
    assertTrue(exact.coordinateTypes().isEmpty())
    exact.set(23)
    val value: Int = exact.get()
    assertEquals(23, value)
    def widened(): Long = exact.get()
    assertThrows(classOf[WrongMethodTypeException], widened())
    val adapted: Long = exact.withInvokeBehavior().get()
    assertEquals(23L, adapted)
    assertThrows(classOf[WrongMethodTypeException], { exact.getAndAdd(1); () })
    assertEquals(23, StaticVarHandleBehaviorBox.value)
    def extraCoordinate(): Int = exact.get(new StaticVarHandleBehaviorBox)
    assertThrows(classOf[WrongMethodTypeException], extraCoordinate())
    def unitCoordinate(): Int = exact.get(())
    assertThrows(classOf[WrongMethodTypeException], unitCoordinate())
    def adaptingUnitCoordinate(): Int = handle.get(())
    assertThrows(classOf[WrongMethodTypeException], adaptingUnitCoordinate())
  }
}
