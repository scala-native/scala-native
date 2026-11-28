package org.scalanative.testsuite.javalib.lang

import java.math.RoundingMode
import java.util.concurrent.TimeUnit

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// JDK enums, Scala 2 cannot define Java enums
class EnumReflectionTest {

  @Test def isEnum(): Unit = {
    assertTrue(classOf[TimeUnit].isEnum())
    assertTrue(classOf[RoundingMode].isEnum())
    assertFalse(classOf[java.lang.Enum[_]].isEnum())
    assertFalse(classOf[String].isEnum())
    assertFalse(classOf[Comparable[_]].isEnum())
    assertFalse(classOf[Array[TimeUnit]].isEnum())
  }

  @Test def getEnumConstants(): Unit = {
    val units = classOf[TimeUnit].getEnumConstants()
    assertArrayEquals(
      TimeUnit.values().asInstanceOf[Array[AnyRef]],
      units.asInstanceOf[Array[AnyRef]]
    )
    assertNotSame(units, classOf[TimeUnit].getEnumConstants())
    assertEquals(RoundingMode.UP, classOf[RoundingMode].getEnumConstants()(0))
    assertNull(classOf[String].getEnumConstants())
    assertNull(classOf[java.lang.Enum[_]].getEnumConstants())
  }

  @Test def getDeclaringClass(): Unit = {
    TimeUnit.values().foreach { unit =>
      assertSame(classOf[TimeUnit], unit.getDeclaringClass())
    }
    assertSame(classOf[RoundingMode], RoundingMode.DOWN.getDeclaringClass())
  }

  @Test def valueOf(): Unit = {
    assertSame(
      TimeUnit.HOURS,
      java.lang.Enum.valueOf(classOf[TimeUnit], "HOURS")
    )
    assertThrows(
      classOf[IllegalArgumentException],
      java.lang.Enum.valueOf(classOf[TimeUnit], "hours")
    )
    assertThrows(
      classOf[NullPointerException],
      java.lang.Enum.valueOf(classOf[TimeUnit], null)
    )
  }
}
