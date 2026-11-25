package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

class ClassTestOnJDK12 {
  import ClassTestOnJDK12._

  @Test def arrayType(): Unit = {
    assertSame(classOf[Array[Int]], classOf[Int].arrayType())
    assertSame(classOf[Array[String]], classOf[String].arrayType())
    assertSame(classOf[Array[Array[Int]]], classOf[Array[Int]].arrayType())
    assertSame(classOf[Array[Elem]], classOf[Elem].arrayType())
    assertSame(classOf[Elem], classOf[Elem].arrayType().getComponentType)
  }

  @Test def componentType(): Unit = {
    assertSame(classOf[Int], classOf[Array[Int]].componentType())
    assertSame(
      classOf[Array[String]],
      classOf[Array[Array[String]]].componentType()
    )
    assertNull(classOf[String].componentType())
  }

  @Test def descriptorString(): Unit = {
    assertEquals("I", classOf[Int].descriptorString())
    assertEquals("V", java.lang.Void.TYPE.descriptorString())
    assertEquals("Ljava/lang/String;", classOf[String].descriptorString())
    assertEquals("[I", classOf[Array[Int]].descriptorString())
    assertEquals(
      "[[Ljava/lang/String;",
      classOf[Array[Array[String]]].descriptorString()
    )
  }
}

object ClassTestOnJDK12 {
  class Elem
}
