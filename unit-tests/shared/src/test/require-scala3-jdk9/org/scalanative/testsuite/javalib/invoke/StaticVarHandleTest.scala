package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{MethodHandles, VarHandle}

import scala.annotation.static

import org.junit.Assert._
import org.junit.Test

/** Scala 3 emits @static members of a companion as Java static fields. */
class StaticVarHandleTest {
  @Test def lookupOutsideCompanionBindsRequestedStaticField(): Unit = {
    val handle = MethodHandles
      .lookup()
      .findStaticVarHandle(
        classOf[StaticVarHandleTest.StaticBox],
        "second",
        java.lang.Long.TYPE
      )
    StaticVarHandleTest.StaticBox.value = 7
    handle.set(23L)
    val previous: Long = handle.getAndAdd(2L)
    assertEquals(23L, previous)
    assertEquals(25L, StaticVarHandleTest.StaticBox.second)
    assertEquals(7, StaticVarHandleTest.StaticBox.value)
  }

  @Test def resolvesEachOfMultipleStaticFields(): Unit = {
    val first = StaticVarHandleTest.StaticBox.handle
    val second = StaticVarHandleTest.StaticBox.secondHandle
    first.set(37)
    second.set(39L)
    val firstValue: Int = first.get()
    val secondValue: Long = second.get()
    assertEquals(37, firstValue)
    assertEquals(39L, secondValue)
    assertEquals(37, StaticVarHandleTest.StaticBox.value)
    assertEquals(39L, StaticVarHandleTest.StaticBox.second)
  }

  @Test def staticCompareExchangeHasNoReceiverCoordinate(): Unit = {
    val handle = StaticVarHandleTest.StaticBox.handle
    handle.set(17)
    val witness: Int = handle.compareAndExchangeAcquire(16, 20)
    assertEquals(17, witness)
    val previous: Int = handle.getAndSetRelease(0)
    assertEquals(17, previous)
    assertEquals(0, StaticVarHandleTest.StaticBox.value)
  }

  @Test def companionStoredHandleDispatchesToScalaStaticField(): Unit = {
    val handle = StaticVarHandleTest.StaticBox.handle
    assertNotNull(handle)

    handle.set(40)
    assertEquals(40, StaticVarHandleTest.StaticBox.value)
    val old: Int = handle.getAndAdd(2)
    assertEquals(40, old)
    val value: Int = handle.getVolatile()
    assertEquals(42, value)
    assertTrue(handle.compareAndSet(42, 0))
    assertEquals(0, StaticVarHandleTest.StaticBox.value)
  }
}

object StaticVarHandleTest {
  final class StaticBox
  object StaticBox {
    @static var value: Int = 0
    @static var second: Long = 0L

    val handle: VarHandle = MethodHandles
      .lookup()
      .findStaticVarHandle(classOf[StaticBox], "value", java.lang.Integer.TYPE)
    val secondHandle: VarHandle = MethodHandles
      .lookup()
      .findStaticVarHandle(classOf[StaticBox], "second", java.lang.Long.TYPE)
  }
}
