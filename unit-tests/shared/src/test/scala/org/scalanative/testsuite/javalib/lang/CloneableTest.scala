package org.scalanative.testsuite.javalib.lang

import java.lang.*

import org.junit.{Ignore, Test}
import org.junit.Assert.*

class CloneableTest {

  class Foo(val x: Int, val y: String) {
    override def clone(): Foo = super.clone().asInstanceOf[Foo]
  }
  class CloneableFoo(val x: Int, val y: String) extends Cloneable() {
    override def clone(): CloneableFoo =
      super.clone().asInstanceOf[CloneableFoo]
  }

  @Test def isNotClonable(): Unit = {
    val instance = new Foo(42, "*")
    assertFalse(instance.isInstanceOf[Cloneable])
    assertThrows(
      classOf[CloneNotSupportedException],
      () => instance.clone()
    )
  }

  @Test def isClonable(): Unit = {
    val instance = new CloneableFoo(42, "*")
    assertTrue(instance.isInstanceOf[Cloneable])
    val clone = instance.clone()
    assertEquals(instance.getClass(), clone.getClass())
    assertEquals(instance.x, clone.x)
    assertEquals(instance.y, clone.y)
    assertNotSame(instance, clone)
  }
}
