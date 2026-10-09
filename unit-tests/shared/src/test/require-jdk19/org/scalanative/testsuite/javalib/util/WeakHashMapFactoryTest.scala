package org.scalanative.testsuite.javalib.util

import java.util.WeakHashMap

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class WeakHashMapFactoryTest {
  @Test def negativeExpectedSizesAreRejected(): Unit = {
    for (size <- Array(-1, Int.MinValue))
      assertThrows(
        classOf[IllegalArgumentException],
        WeakHashMap.newWeakHashMap[String, String](size)
      )
  }

  @Test def expectedMappingsCanBeAddedAndRetrieved(): Unit = {
    for (size <- Array(0, 1, 2, 3, 6, 11, 12, 24, 128, 257)) {
      val map = WeakHashMap.newWeakHashMap[Integer, String](size)
      val keys = Array.tabulate[Integer](size)(i => Integer.valueOf(1000 + i))
      assertTrue(map.isEmpty())
      for (i <- keys.indices) map.put(keys(i), i.toString())
      assertEquals(size, map.size())
      for (i <- keys.indices) assertEquals(i.toString(), map.get(keys(i)))
    }
  }

  @Test def newMapSupportsNullsUpdatesAndRemovals(): Unit = {
    val map = WeakHashMap.newWeakHashMap[String, String](2)
    map.put(null, "null-key")
    map.put("key", null)
    assertTrue(map.containsKey(null))
    assertTrue(map.containsKey("key"))
    assertNull(map.get("key"))
    assertEquals("null-key", map.put(null, "updated"))
    assertEquals("updated", map.get(null))
    assertTrue(map.keySet().remove("key"))
    assertEquals(1, map.size())
    assertEquals("updated", map.remove(null))
    assertTrue(map.isEmpty())
  }

  @Test def factoriesReturnIndependentMaps(): Unit = {
    val first = WeakHashMap.newWeakHashMap[String, String](0)
    val second = WeakHashMap.newWeakHashMap[String, String](0)
    assertNotSame(first, second)
    first.put("key", "value")
    assertTrue(second.isEmpty())
    first.clear()
    assertTrue(first.isEmpty())
  }
}
