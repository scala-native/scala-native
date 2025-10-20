package org.scalanative.testsuite.javalib.net

import java.net.*

import org.junit.Test
import org.junit.Assert.*

import scala.annotation.nowarn
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class URITest {

  // suppress warning for URL constructor deprecated in JDK20
  @Test @nowarn def toURL(): Unit = {
    assertThrows(classOf[IllegalArgumentException], new URI("a/b").toURL())
    assertEquals(
      new URI("http://a/b").toURL().toString(),
      new URL("http://a/b").toString()
    )
  }

}
