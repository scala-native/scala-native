package org.scalanative.testsuite.javalib.net

import java.net._

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* URL.setURLStreamHandlerFactory works once per process: keep this the only
 * test that calls it. The factory serves a test protocol only.
 */
@nowarn("cat=deprecation")
class URLStreamHandlerFactoryTest {

  @Test def factoryOnce(): Unit = {
    assertThrows(classOf[MalformedURLException], new URL("nhtest://h/"))
    val handler = new URLStreamHandler {
      override protected def openConnection(u: URL): URLConnection = null
      override protected def getDefaultPort(): Int = 4242
    }
    URL.setURLStreamHandlerFactory(p => if (p == "nhtest") handler else null)
    val u = new URL("NHTEST://h/x")
    assertEquals("nhtest", u.getProtocol())
    assertEquals(4242, u.getDefaultPort())
    assertEquals("http", new URL("http://h/").getProtocol())
    val e =
      assertThrows(classOf[Error], URL.setURLStreamHandlerFactory(_ => null))
    assertEquals("factory already defined", e.getMessage())
  }
}
