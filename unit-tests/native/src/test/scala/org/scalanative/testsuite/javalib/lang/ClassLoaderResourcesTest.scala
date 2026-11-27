package org.scalanative.testsuite.javalib.lang

import java.io.FileNotFoundException
import java.net.URL
import java.nio.charset.StandardCharsets

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ClassLoaderResourcesTest {
  private val embedded = "embedded-resources-tests/basic-file.txt"
  private val missing = "no/such/resource"

  private def cl = ClassLoader.getSystemClassLoader()

  private def read(url: URL): String = {
    val in = url.openStream()
    try new String(in.readAllBytes(), StandardCharsets.UTF_8)
    finally in.close()
  }

  private def expectedContent: String = {
    val in = cl.getResourceAsStream(embedded)
    try new String(in.readAllBytes(), StandardCharsets.UTF_8)
    finally in.close()
  }

  @Test def getResource(): Unit = {
    val url = cl.getResource(embedded)
    assertNotNull(url)
    assertEquals("resource:/" + embedded, url.toString())
    assertEquals(expectedContent, read(url))
    assertEquals("text/plain", url.openConnection().getContentType())
    assertEquals(url, ClassLoader.getSystemResource(embedded))
    assertEquals(url, new URL(url.toString()))
    assertEquals(url, url.toURI().toURL())
  }

  @Test def getResourceAbsentOrAbsoluteIsNull(): Unit = {
    assertNull(cl.getResource(missing))
    assertNull(cl.getResource("/" + embedded))
    assertNull(ClassLoader.getSystemResource(missing))
  }

  @Test def relativeResourceURL(): Unit = {
    val url = cl.getResource(embedded)
    val sibling = new URL(url, "no_extension_utf8")
    assertEquals("śćŹŻźó", read(sibling).trim())
    assertThrows(
      classOf[FileNotFoundException],
      new URL(url, "missing.txt").openStream()
    )
  }

  @Test def getResources(): Unit = {
    val expected = cl.getResource(embedded)
    val urls = cl.getResources(embedded)
    assertTrue(urls.hasMoreElements())
    assertEquals(expected, urls.nextElement())
    assertFalse(urls.hasMoreElements())

    val system = ClassLoader.getSystemResources(embedded)
    assertEquals(expected, system.nextElement())
    assertFalse(system.hasMoreElements())

    assertArrayEquals(
      Array[Object](expected),
      cl.resources(embedded).toArray()
    )

    assertFalse(cl.getResources(missing).hasMoreElements())
    assertFalse(ClassLoader.getSystemResources(missing).hasMoreElements())
    assertEquals(0L, cl.resources(missing).count())
  }

  @Test def resourcesUsesOverriddenGetResources(): Unit = {
    val url = new URL("resource:/virtual/config")
    val loader = new ClassLoader() {
      override def getResources(name: String): java.util.Enumeration[URL] =
        java.util.Collections.enumeration(java.util.Arrays.asList(url, url))
    }
    assertArrayEquals(
      Array[Object](url, url),
      loader.resources("virtual/config").toArray()
    )
  }

  @Test def getSystemResourceAsStream(): Unit = {
    val in = ClassLoader.getSystemResourceAsStream(embedded)
    assertNotNull(in)
    in.close()
    assertNull(ClassLoader.getSystemResourceAsStream("/" + embedded))
    assertNull(ClassLoader.getSystemResourceAsStream(missing))
  }

  @Test def nullNameThrowsNPE(): Unit = {
    assertThrows(classOf[NullPointerException], cl.getResource(null))
    assertThrows(classOf[NullPointerException], cl.getResources(null))
    assertThrows(classOf[NullPointerException], cl.resources(null))
    assertThrows(
      classOf[NullPointerException],
      ClassLoader.getSystemResource(null)
    )
    assertThrows(
      classOf[NullPointerException],
      ClassLoader.getSystemResources(null)
    )
    assertThrows(
      classOf[NullPointerException],
      ClassLoader.getSystemResourceAsStream(null)
    )
  }
}
