package org.scalanative.testsuite.javalib.util

import java.math.RoundingMode
import java.util.concurrent.TimeUnit
import java.util.{AbstractMap, EnumMap, HashMap, Map => JMap}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

// Uses JDK enums so that the test cross-compiles with Scala 2, which cannot
// define Java enums. On Scala Native the TimeUnit constants are anonymous
// subclasses of TimeUnit, which exercises the key class lookup.
class EnumMapTest {
  import TimeUnit._

  private def units(): EnumMap[TimeUnit, String] =
    new EnumMap[TimeUnit, String](classOf[TimeUnit])

  @Test def constructorNullKeyType(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new EnumMap[TimeUnit, String](null: Class[TimeUnit])
    )

  @Test def constructorFromEmptyMap(): Unit =
    assertThrows(
      classOf[IllegalArgumentException],
      new EnumMap[TimeUnit, String](new HashMap[TimeUnit, String]())
    )

  @Test def constructorFromEmptyEnumMap(): Unit = {
    val copy = new EnumMap[TimeUnit, String](units(): JMap[TimeUnit, String])
    assertTrue(copy.isEmpty())
    copy.put(SECONDS, "s")
    assertEquals("s", copy.get(SECONDS))
  }

  @Test def constructorFromEnumMap(): Unit = {
    val m = units()
    m.put(DAYS, "d")
    val copy = new EnumMap[TimeUnit, String](m)
    assertEquals(m, copy)
    copy.put(NANOSECONDS, "n")
    assertEquals(1, m.size())
    assertEquals("{NANOSECONDS=n, DAYS=d}", copy.toString())
  }

  @Test def constructorFromMap(): Unit = {
    val hm = new HashMap[TimeUnit, String]()
    hm.put(DAYS, "d")
    hm.put(NANOSECONDS, "n")
    val m = new EnumMap[TimeUnit, String](hm)
    assertEquals("{NANOSECONDS=n, DAYS=d}", m.toString())
    m.put(HOURS, "h")
    assertEquals("{NANOSECONDS=n, HOURS=h, DAYS=d}", m.toString())
    assertThrows(
      classOf[ClassCastException],
      m.asInstanceOf[JMap[Any, String]].put(RoundingMode.UP, "u")
    )
  }

  @Test def putGetRemove(): Unit = {
    val m = units()
    assertNull(m.put(MINUTES, "m"))
    assertEquals("m", m.put(MINUTES, "m2"))
    assertNull(m.put(SECONDS, null))
    assertEquals(2, m.size())
    assertTrue(m.containsKey(SECONDS))
    assertNull(m.get(SECONDS))
    assertTrue(m.containsValue(null))
    assertTrue(m.containsValue("m2"))
    assertFalse(m.containsValue("x"))
    assertFalse(m.containsKey(DAYS))
    assertFalse(m.containsKey(RoundingMode.UP))
    assertFalse(m.containsKey(null))
    assertNull(m.get(RoundingMode.UP))
    assertNull(m.get("SECONDS"))
    assertNull(m.get(DAYS))
    assertNull(m.remove(SECONDS))
    assertFalse(m.containsKey(SECONDS))
    assertNull(m.remove(RoundingMode.UP))
    assertNull(m.remove(DAYS))
    assertEquals(1, m.size())
  }

  @Test def putRejectsNullAndForeignKeys(): Unit = {
    val m = units()
    assertThrows(classOf[NullPointerException], m.put(null, "x"))
    assertThrows(
      classOf[ClassCastException],
      m.asInstanceOf[JMap[Any, String]].put(RoundingMode.UP, "u")
    )
  }

  @Test def iterationInOrdinalOrder(): Unit = {
    val m = units()
    m.put(DAYS, "d")
    m.put(NANOSECONDS, "n")
    m.put(MINUTES, "m")
    assertEquals("{NANOSECONDS=n, MINUTES=m, DAYS=d}", m.toString())
    assertEquals("[NANOSECONDS, MINUTES, DAYS]", m.keySet().toString())
    assertEquals("[n, m, d]", m.values().toString())
  }

  @Test def entrySetOperations(): Unit = {
    val m = units()
    m.put(SECONDS, "s")
    m.put(DAYS, "d")
    val it = m.entrySet().iterator()
    val e = it.next()
    assertEquals(SECONDS, e.getKey())
    assertEquals("s", e.setValue("S"))
    assertEquals("S", m.get(SECONDS))
    m.put(SECONDS, "S2")
    assertEquals("S2", e.getValue())
    it.remove()
    assertThrows(classOf[IllegalStateException], it.remove())
    assertThrows(classOf[IllegalStateException], e.getKey())
    assertThrows(classOf[IllegalStateException], e.getValue())
    assertThrows(classOf[IllegalStateException], e.setValue("x"))
    assertTrue(e.equals(e))
    assertFalse(e.equals(new AbstractMap.SimpleEntry(SECONDS, "S2")))
    assertFalse(m.containsKey(SECONDS))
    assertEquals(1, m.size())
    assertTrue(
      m.entrySet().contains(new AbstractMap.SimpleEntry(DAYS, "d"))
    )
    assertFalse(
      m.entrySet().contains(new AbstractMap.SimpleEntry(DAYS, "x"))
    )
    assertTrue(
      m.entrySet().remove(new AbstractMap.SimpleEntry(DAYS, "d"))
    )
    assertTrue(m.isEmpty())
    assertFalse(m.entrySet().iterator().hasNext())
  }

  @Test def entryReadsThroughAfterMapRemove(): Unit = {
    val m = units()
    m.put(DAYS, "d")
    val e = m.entrySet().iterator().next()
    assertNull(m.remove(HOURS))
    m.remove(DAYS)
    assertEquals(DAYS, e.getKey())
    assertNull(e.getValue())
    m.put(DAYS, "d2")
    assertEquals("d2", e.getValue())
    assertEquals("DAYS=d2", e.toString())
    assertEquals(new AbstractMap.SimpleEntry(DAYS, "d2"), e)
    assertEquals("d2", e.setValue("d3"))
    assertEquals("d3", m.get(DAYS))
    m.remove(DAYS)
    assertNull(e.setValue("d4"))
    assertEquals("d4", m.get(DAYS))
    // The JDK maps the key again without counting it in size()
    if (!Platform.executingInJVM) assertEquals(1, m.size())
  }

  @Test def iteratorSeesLaterPut(): Unit = {
    val m = units()
    m.put(NANOSECONDS, "n")
    val it = m.entrySet().iterator()
    assertEquals(NANOSECONDS, it.next().getKey())
    m.put(DAYS, "d") // grows the table
    assertTrue(it.hasNext())
    assertEquals(new AbstractMap.SimpleEntry(DAYS, "d"), it.next())
    assertFalse(it.hasNext())

    val values = m.values().iterator()
    assertEquals("n", values.next())
    m.put(HOURS, "h")
    assertEquals("h", values.next())
    assertEquals("d", values.next())
    assertFalse(values.hasNext())
  }

  @Test def iteratorSkipsLaterRemove(): Unit = {
    val m = units()
    m.put(NANOSECONDS, "n")
    m.put(DAYS, "d")
    val keys = m.keySet().iterator()
    assertEquals(NANOSECONDS, keys.next())
    m.remove(DAYS)
    assertFalse(keys.hasNext())
    assertThrows(classOf[NoSuchElementException], keys.next())
  }

  @Test def iteratorExhausted(): Unit = {
    val m = units()
    m.put(HOURS, "h")
    val it = m.keySet().iterator()
    assertThrows(classOf[IllegalStateException], it.remove())
    assertEquals(HOURS, it.next())
    assertFalse(it.hasNext())
    assertThrows(classOf[NoSuchElementException], it.next())
  }

  @Test def keySetAndValuesRemove(): Unit = {
    val m = units()
    m.put(SECONDS, "s")
    m.put(MINUTES, "m")
    m.keySet().remove(SECONDS)
    assertEquals("{MINUTES=m}", m.toString())
    m.values().remove("m")
    assertTrue(m.isEmpty())
  }

  @Test def equalsHashCodeClone(): Unit = {
    val m = units()
    m.put(SECONDS, "s")
    m.put(DAYS, null)
    val hm = new HashMap[TimeUnit, String]()
    hm.put(SECONDS, "s")
    hm.put(DAYS, null)
    assertEquals(hm, m)
    assertEquals(m, hm)
    assertEquals(hm.hashCode(), m.hashCode())
    val c = m.clone()
    assertEquals(m, c)
    c.put(HOURS, "h")
    assertNotEquals(m, c)
    assertFalse(m.containsKey(HOURS))
    assertEquals(
      new EnumMap[RoundingMode, String](classOf[RoundingMode]),
      units()
    )
    val rm = new EnumMap[RoundingMode, String](classOf[RoundingMode])
    rm.put(RoundingMode.UP, "s")
    assertNotEquals(rm, m)
  }

  @Test def putAllAndClear(): Unit = {
    val m = units()
    val hm = new HashMap[TimeUnit, String]()
    hm.put(MINUTES, "m")
    hm.put(DAYS, "d")
    m.putAll(hm)
    assertEquals(2, m.size())
    m.clear()
    assertTrue(m.isEmpty())
    assertNull(m.get(MINUTES))
    m.put(DAYS, "d2")
    assertEquals("{DAYS=d2}", m.toString())
  }
}
