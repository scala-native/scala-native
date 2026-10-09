package org.scalanative.testsuite.javalib.util

import java.io.Serializable
import java.util.{AbstractMap, ArrayList, HashMap, Map}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class MapEntryCopyTest {
  @Test def copyIsImmutableSnapshotOfMutableEntry(): Unit = {
    val original = new AbstractMap.SimpleEntry("key", "before")
    val copy = Map.Entry.copyOf[String, String](original)
    original.setValue("after")
    assertEquals("key", copy.getKey())
    assertEquals("before", copy.getValue())
    assertThrows(
      classOf[UnsupportedOperationException],
      copy.setValue("changed")
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      copy.setValue("before")
    )
    assertThrows(classOf[UnsupportedOperationException], copy.setValue(null))
    assertFalse(copy.isInstanceOf[Serializable])
  }

  @Test def copyIsDisconnectedFromBackingMap(): Unit = {
    val map = new HashMap[String, String]()
    map.put("key", "before")
    val original = map.entrySet().iterator().next()
    val copy = Map.Entry.copyOf[String, String](original)
    original.setValue("after")
    assertEquals("after", map.get("key"))
    map.clear()
    assertEquals("key", copy.getKey())
    assertEquals("before", copy.getValue())
  }

  @Test def copyRejectsNullEntryKeyAndValue(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      Map.Entry.copyOf[String, String](null)
    )
    assertThrows(
      classOf[NullPointerException],
      Map.Entry.copyOf[String, String](
        new AbstractMap.SimpleEntry[String, String](null, "value")
      )
    )
    assertThrows(
      classOf[NullPointerException],
      Map.Entry.copyOf[String, String](
        new AbstractMap.SimpleEntry[String, String]("key", null)
      )
    )
  }

  @Test def copyAndMapEntryHaveConsistentEqualityAndHashCodes(): Unit = {
    val original = new AbstractMap.SimpleEntry("key", "value")
    val copy = Map.Entry.copyOf[String, String](original)
    val entry = Map.entry("key", "value")
    assertEquals(original, copy)
    assertEquals(copy, original)
    assertEquals(entry, copy)
    assertEquals(original.hashCode(), copy.hashCode())
    assertEquals(original.hashCode(), entry.hashCode())
    assertEquals(copy, Map.Entry.copyOf[String, String](copy))
    assertEquals(copy, Map.Entry.copyOf[String, String](entry))
  }

  @Test def copyAllowsWidenedTypesAndRetainsReferences(): Unit = {
    val value = new ArrayList[String]()
    value.add("before")
    val original =
      new AbstractMap.SimpleEntry[String, ArrayList[String]]("key", value)
    val copy: Map.Entry[CharSequence, AnyRef] =
      Map.Entry.copyOf[CharSequence, AnyRef](original)
    assertSame(original.getKey(), copy.getKey())
    assertSame(value, copy.getValue())
    value.add("after")
    assertEquals(2, value.size())
    assertSame(value, copy.getValue())
  }
}
