package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{MethodHandles, VarHandle}

import org.junit.Assert._
import org.junit.Test

class VarHandlePrivateLookupTest {
  @Test def companionHandleAccessesPrivateFieldOnMultipleInstances(): Unit = {
    val first = new VarHandlePrivateLookupTest.Box(1)
    val second = new VarHandlePrivateLookupTest.Box(10)
    val handle = VarHandlePrivateLookupTest.VALUE
    val previous: Int = handle.getAndAdd(first, 2)
    assertEquals(1, previous)
    assertEquals(3, first.current)
    assertEquals(10, second.current)
    assertTrue(handle.compareAndSet(second, 10, 20))
    val witness: Int = handle.compareAndExchange(second, 10, 30)
    assertEquals(20, witness)
    assertEquals(20, second.current)
  }
}

object VarHandlePrivateLookupTest {
  final class Box(private var value: Int) {
    def current: Int = value
  }

  val VALUE: VarHandle = MethodHandles
    .privateLookupIn(classOf[Box], MethodHandles.lookup())
    .findVarHandle(classOf[Box], "value", classOf[Int])
}
