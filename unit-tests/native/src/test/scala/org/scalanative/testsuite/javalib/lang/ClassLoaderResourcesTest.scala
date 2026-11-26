package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ClassLoaderResourcesTest {
  private val embedded = "embedded-resources-tests/basic-file.txt"
  private val missing = "no/such/resource"

  private def cl = ClassLoader.getSystemClassLoader()

  @Test def getSystemResourceAsStream(): Unit = {
    val in = ClassLoader.getSystemResourceAsStream(embedded)
    assertNotNull(in)
    in.close()
    assertNull(ClassLoader.getSystemResourceAsStream("/" + embedded))
    assertNull(ClassLoader.getSystemResourceAsStream(missing))
  }

  @Test def urlLookupsAreEmpty(): Unit = {
    for (name <- Seq(embedded, missing)) {
      assertFalse(cl.getResources(name).hasMoreElements())
      assertFalse(ClassLoader.getSystemResources(name).hasMoreElements())
      assertEquals(0L, cl.resources(name).count())
    }
  }

  @Test def nullNameThrowsNPE(): Unit = {
    assertThrows(classOf[NullPointerException], cl.getResources(null))
    assertThrows(
      classOf[NullPointerException],
      ClassLoader.getSystemResources(null)
    )
    assertThrows(classOf[NullPointerException], cl.resources(null))
    assertThrows(
      classOf[NullPointerException],
      ClassLoader.getSystemResourceAsStream(null)
    )
  }
}
