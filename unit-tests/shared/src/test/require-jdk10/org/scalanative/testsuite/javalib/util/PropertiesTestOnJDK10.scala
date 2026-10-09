package org.scalanative.testsuite.javalib.util

import java.util.Properties

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class PropertiesTestOnJDK10 {
  @Test def negativeCapacity(): Unit = {
    for (capacity <- Seq(-1, Int.MinValue))
      assertThrows(classOf[IllegalArgumentException], new Properties(capacity))
  }

  @Test def capacityConstructorsHaveNoDefaults(): Unit = {
    for (capacity <- Seq(0, 1, 16, 100)) {
      val properties = new Properties(capacity)
      assertTrue(properties.isEmpty())
      assertNull(properties.getProperty("missing"))
      assertEquals("fallback", properties.getProperty("missing", "fallback"))
      assertFalse(properties.propertyNames().hasMoreElements())
    }
  }

  @Test def capacityConstructorsSupportGrowthAndReplacement(): Unit = {
    for (capacity <- Seq(0, 1, 16, 100)) {
      val properties = new Properties(capacity)
      for (i <- 0 until 200) {
        assertNull(properties.setProperty("key" + i, "value" + i))
      }
      assertEquals(200, properties.size())
      for (i <- 0 until 200)
        assertEquals("value" + i, properties.getProperty("key" + i))
      assertEquals("value0", properties.setProperty("key0", "updated"))
      assertEquals("updated", properties.remove("key0"))
      assertEquals(199, properties.size())
    }
  }
}
