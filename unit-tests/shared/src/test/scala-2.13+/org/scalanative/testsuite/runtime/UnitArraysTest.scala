package org.scalanative.testsuite.runtime

import org.junit.Assert._
import org.junit.Test

class UnitArraysTest {
  @noinline def opaque(x: AnyRef): AnyRef = x

  @Test def unitArraysAreTheirOwnType(): Unit = {
    val units: AnyRef = Array[Unit]((), ())
    assertTrue(opaque(units).isInstanceOf[Array[scala.runtime.BoxedUnit]])
    assertFalse(
      opaque(new Array[AnyRef](1)).isInstanceOf[Array[scala.runtime.BoxedUnit]]
    )
    // Pads with () as on the JVM, not with null
    val padded = Array.copyOf(Array[Unit]((), ()), 4)
    assertEquals(4, padded.length)
    padded.foreach(u => assertEquals((), u))
    val objects = Array.copyOf(Array[AnyRef]("a"), 2)
    assertSame(classOf[Array[AnyRef]], objects.getClass)
    assertNull(objects(1))
  }
}
