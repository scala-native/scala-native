package org.scalanative.testsuite.javalib.net

import java.io.{ByteArrayInputStream, InputStream}
import java.net._

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Test

/* URLConnection.setContentHandlerFactory works once per process: keep this
 * the only test that calls it. The factory serves a test type only.
 */
@nowarn("cat=deprecation")
class ContentHandlerFactoryTest {

  private class Conn(tpe: String) extends URLConnection(new URL("http://h/")) {
    def connect(): Unit = ()
    override def getContentType(): String = tpe
    override def getInputStream(): InputStream =
      new ByteArrayInputStream(Array[Byte](1))
  }

  @Test def factoryGetsTheTypeWithoutParameters(): Unit = {
    val asked = new java.util.ArrayList[String]()
    val handler = new ContentHandler {
      def getContent(urlc: URLConnection): AnyRef = "sntest content"
    }
    URLConnection.setContentHandlerFactory { mime =>
      asked.add(mime)
      if (mime == "sntest/CASE") handler else null
    }
    assertEquals("sntest content", new Conn("sntest/CASE; a=b").getContent())
    assertTrue(
      new Conn("sntest/case").getContent().isInstanceOf[InputStream]
    )
    assertTrue(asked.toString, asked.contains("sntest/CASE"))
  }
}
