package org.scalanative.testsuite.javalib.util

import java.lang._
import java.util.EnumSet

import org.junit.Assert._
import org.junit.Test

// Tested only in Scala 3 becouse we cannot create Java enums in Scala 2

object EnumSetTest {
  enum Value extends java.lang.Enum[Value]:
    case A, B, C, D, E, F
}

class EnumSetTest {
  import EnumSetTest.Value

  @Test def noneOf(): Unit = {
    val s = EnumSet.noneOf(classOf[Value])
    assertTrue(s.isEmpty())
    assertEquals(0, s.size())
    assertFalse(s.iterator().hasNext())
  }

  @Test def of1(): Unit = {
    val s = EnumSet.of(Value.A)
    assertFalse(s.isEmpty())
    assertEquals(1, s.size())
    val it = s.iterator()
    assertTrue(it.hasNext())
    assertEquals(Value.A, it.next())
  }

  @Test def of2(): Unit = {
    val s = EnumSet.of(Value.A, Value.B)
    assertFalse(s.isEmpty())
    assertEquals(2, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
  }

  @Test def of3(): Unit = {
    val s = EnumSet.of(Value.A, Value.B, Value.C)
    assertFalse(s.isEmpty())
    assertEquals(3, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
  }

  @Test def of4(): Unit = {
    val s = EnumSet.of(Value.A, Value.B, Value.C, Value.D)
    assertFalse(s.isEmpty())
    assertEquals(4, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
    assertTrue(s.contains(Value.D))
  }

  @Test def of5(): Unit = {
    val s = EnumSet.of(Value.A, Value.B, Value.C, Value.D, Value.E)
    assertFalse(s.isEmpty())
    assertEquals(5, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
    assertTrue(s.contains(Value.D))
    assertTrue(s.contains(Value.E))
  }

  @Test def ofVarArg(): Unit = {
    val s = EnumSet.of(Value.A, Value.B, Value.C, Value.D, Value.E, Value.F)
    assertFalse(s.isEmpty())
    assertEquals(6, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
    assertTrue(s.contains(Value.D))
    assertTrue(s.contains(Value.E))
    assertTrue(s.contains(Value.F))
  }

  @Test def ofVarArg2(): Unit = {
    val s =
      EnumSet.of(Value.A, Seq(Value.B, Value.C, Value.D, Value.E, Value.F): _*)
    assertFalse(s.isEmpty())
    assertEquals(6, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
    assertTrue(s.contains(Value.D))
    assertTrue(s.contains(Value.E))
    assertTrue(s.contains(Value.F))
  }

  @Test def copyOf(): Unit = {
    val s = EnumSet.of(Value.A, Value.B, Value.C)
    val c = EnumSet.copyOf(s)
    assertNotSame(s, c)
    assertEquals(s, c)
  }

  @Test def copyOfCollection(): Unit = {
    val c: java.util.Collection[Value] = new java.util.LinkedList[Value]()
    c.add(Value.A)
    c.add(Value.B)
    c.add(Value.C)
    val s = EnumSet.copyOf(c)

    assertNotSame(s, c)
    assertEquals(3, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
  }

  @Test def uniqness(): Unit = {
    val s = EnumSet.of(Value.A, Value.A, Value.B, Value.C, Value.B)
    assertEquals(3, s.size())
    assertTrue(s.contains(Value.A))
    assertTrue(s.contains(Value.B))
    assertTrue(s.contains(Value.C))
  }

}
