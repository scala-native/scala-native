package org.scalanative.testsuite.javalib.net

import java.net._

// Ported from Scala.js and Apache Harmony

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class URITest {

  @Test def toURL(): Unit = {
    assertThrows(classOf[IllegalArgumentException], new URI("a/b").toURL())
    assertEquals(
      new URI("http://a/b").toURL().toString(),
      new URL("http://a/b").toString()
    )
  }

}
