package org.scalanative.testsuite.javalib.lang

import org.junit.Assert.*
import org.junit.Test

// User-defined Java enums can only be written in Scala 3.

object EnumReflectionScala3Test {
  enum Color extends java.lang.Enum[Color]:
    case Red, Green, Blue

  enum Planet(val mass: Double) extends java.lang.Enum[Planet]:
    case Mercury extends Planet(3.3e23)
    case Earth extends Planet(5.97e24)
}

class EnumReflectionScala3Test {
  import EnumReflectionScala3Test.*

  @Test def userEnums(): Unit = {
    assertTrue(classOf[Color].isEnum())
    assertEquals(
      "Red,Green,Blue",
      classOf[Color].getEnumConstants().mkString(",")
    )
    assertEquals(
      "Mercury,Earth",
      classOf[Planet].getEnumConstants().mkString(",")
    )
    assertSame(Color.Green, java.lang.Enum.valueOf(classOf[Color], "Green"))
  }

  @Test def constantClasses(): Unit = {
    assertSame(classOf[Color], Color.Red.getDeclaringClass())
    assertSame(classOf[Planet], Planet.Earth.getDeclaringClass())
    val cls = Color.Blue.getClass()
    if (cls ne classOf[Color]) {
      assertFalse(cls.isEnum())
      assertNull(cls.getEnumConstants())
    }
  }
}
