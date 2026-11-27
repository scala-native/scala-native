package org.scalanative.testsuite.javalib.util

import java.util.{EnumMap, HashMap, Map as JMap}

import org.junit.Assert.*
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// User-defined Java enums can only be written in Scala 3.

object EnumMapScala3Test {
  enum Color extends java.lang.Enum[Color]:
    case Red, Green, Blue

  enum Planet(val mass: Double) extends java.lang.Enum[Planet]:
    case Mercury extends Planet(3.3e23)
    case Earth extends Planet(5.97e24)
}

class EnumMapScala3Test {
  import EnumMapScala3Test.*

  @Test def constructorFromMapFindsEnumClass(): Unit = {
    val hm = new HashMap[Color, String]()
    hm.put(Color.Blue, "b")
    hm.put(Color.Red, "r")
    val m = new EnumMap[Color, String](hm)
    assertEquals("{Red=r, Blue=b}", m.toString())
    m.put(Color.Green, "g")
    assertEquals("{Red=r, Green=g, Blue=b}", m.toString())
    assertThrows(
      classOf[ClassCastException],
      m.asInstanceOf[JMap[Any, String]].put(Planet.Earth, "e")
    )
  }

  @Test def parameterizedEnum(): Unit = {
    val hm = new HashMap[Planet, String]()
    hm.put(Planet.Earth, "e")
    val m = new EnumMap[Planet, String](hm)
    m.put(Planet.Mercury, "m")
    assertEquals("{Mercury=m, Earth=e}", m.toString())
    assertFalse(m.containsKey(Color.Red))
  }
}
