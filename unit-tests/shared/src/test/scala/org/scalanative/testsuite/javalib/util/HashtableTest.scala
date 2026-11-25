package org.scalanative.testsuite.javalib.util

import java.util._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class HashtableTest {

  @Test def putOnNullKeyOrValue(): Unit = {
    val t = new Hashtable[AnyRef, AnyRef]()
    assertThrows(classOf[NullPointerException], t.put(null, "value"))
    assertThrows(classOf[NullPointerException], t.put("key", null))
  }
  @Test def capacityConstructor(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException],
      new Hashtable[String, String](-1)
    )
    for (capacity <- Seq(0, 1, 100)) {
      val table = new Hashtable[String, String](capacity)
      assertTrue(table.isEmpty())
      for (i <- 0 until 200) table.put("key" + i, "value" + i)
      assertEquals(200, table.size())
      for (i <- 0 until 200) assertEquals("value" + i, table.get("key" + i))
    }
  }

  @Test def cloneKeepsRuntimeClassAndCopiesEntries(): Unit = {
    val t = new HashtableTest.SubHashtable
    t.put("a", "1")
    val c = t.clone()
    assertTrue(c.isInstanceOf[HashtableTest.SubHashtable])
    val copy = c.asInstanceOf[HashtableTest.SubHashtable]
    assertEquals("1", copy.get("a"))
    copy.put("b", "2")
    t.remove("a")
    assertFalse(t.containsKey("b"))
    assertEquals("1", copy.get("a"))
  }
}

object HashtableTest {
  class SubHashtable extends Hashtable[AnyRef, AnyRef]
}
