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

}
