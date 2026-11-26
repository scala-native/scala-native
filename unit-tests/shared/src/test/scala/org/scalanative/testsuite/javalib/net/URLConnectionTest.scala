package org.scalanative.testsuite.javalib.net

import java.io.{ByteArrayInputStream, InputStream}
import java.net._
import java.util.TreeMap

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Assume.assumeFalse
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.executingInJVMOnLowerThanJDK

@nowarn("cat=deprecation")
class URLConnectionTest {

  private class Conn(u: URL) extends URLConnection(u) {
    val headers = new TreeMap[String, String](String.CASE_INSENSITIVE_ORDER)
    val asked = new java.util.ArrayList[String]()
    var in: InputStream = null
    override def connect(): Unit = connected = true
    override def getHeaderField(name: String): String = {
      asked.add(name)
      headers.get(name)
    }
    override def getInputStream(): InputStream =
      if (in == null) throw new UnknownServiceException("none") else in
    def isConnected: Boolean = connected
  }

  private def conn(spec: String = "http://h/x.html") = new Conn(new URL(spec))

  @Test def defaults(): Unit = {
    val c = conn()
    assertTrue(c.getDoInput())
    assertFalse(c.getDoOutput())
    assertTrue(c.getUseCaches())
    assertTrue(c.getDefaultUseCaches())
    assertFalse(c.getAllowUserInteraction())
    assertEquals(0L, c.getIfModifiedSince())
    assertEquals(0, c.getConnectTimeout())
    assertEquals(0, c.getReadTimeout())
    assertEquals("http://h/x.html", c.getURL().toString())
    assertEquals(c.getClass().getName() + ":http://h/x.html", c.toString())
    assertFalse(URLConnection.getDefaultAllowUserInteraction())
    URLConnection.setDefaultRequestProperty("a", "b")
    assertNull(URLConnection.getDefaultRequestProperty("a"))
  }

  @Test def headerDefaults(): Unit = {
    val c = conn()
    assertEquals(-1, c.getContentLength())
    assertEquals(-1L, c.getContentLengthLong())
    assertNull(c.getContentType())
    assertNull(c.getContentEncoding())
    assertEquals(0L, c.getDate())
    assertEquals(0L, c.getExpiration())
    assertEquals(0L, c.getLastModified())
    assertNull(c.getHeaderFieldKey(0))
    assertNull(c.getHeaderField(0))
    assertTrue(c.getHeaderFields().isEmpty())
    assertEquals(
      java.util.Arrays.asList(
        "content-length",
        "content-length",
        "content-type",
        "content-encoding",
        "date",
        "expires",
        "last-modified"
      ),
      c.asked
    )
  }

  @Test def numericHeaders(): Unit = {
    val c = conn()
    c.headers.put("content-length", "12345678901")
    assertEquals(-1, c.getContentLength())
    assertEquals(12345678901L, c.getContentLengthLong())
    c.headers.put("content-length", " 5 ")
    assertEquals(-1, c.getContentLength())
    c.headers.put("Int", " 42 ")
    assertEquals(-1, c.getHeaderFieldInt("Int", -1))
    c.headers.put("Neg", "-7")
    assertEquals(-7, c.getHeaderFieldInt("Neg", -1))
    assertEquals(-7L, c.getHeaderFieldLong("Neg", -1))
    assertEquals(3, c.getHeaderFieldInt("missing", 3))
  }

  @Test def dateHeaders(): Unit = {
    val c = conn()
    c.headers.put("Date", "Thu, 01 Jan 1970 00:00:01 GMT")
    c.headers.put("Expires", "Thu, 01 Jan 1970 00:00:02 GMT")
    c.headers.put("Last-Modified", "Thu, 01 Jan 1970 00:00:03 GMT")
    assertEquals(
      Seq(1000L, 2000L, 3000L),
      Seq(c.getDate(), c.getExpiration(), c.getLastModified())
    )
    c.headers.put("Last-Modified", "Thu, 01 Jan 1970 00:00:04 GMT")
    assertEquals(4000L, c.getLastModified()) // not cached
    val cases = Seq(
      "Wed, 09 Jun 2100 10:18:14 GMT" -> 4116219494000L,
      "Wednesday, 09-Jun-00 10:18:14 GMT" -> 960545894000L,
      "Wed, 09 Jun 2100 10:18:14 +0100" -> 4116215894000L,
      "Wed, 09 Jun 2100 10:18:14 PST" -> 4116248294000L,
      "9 Jun 2100 10:18:14 GMT" -> 4116219494000L,
      "Thu, 01 Jan 1970 00:00:00 GMT" -> 0L,
      "Thu, 01 Jan 1970 00:00:01 UTC" -> 1000L,
      "Thu, 01 Jan 1970 00:00:01 UT" -> 1000L,
      "Thu, 01 Jan 1970 00:00:01 EST" -> 18001000L,
      "Thu, 01 Jan 1970 00:00:01 EDT" -> 14401000L,
      "Thu, 01 Jan 1970 00:00:01 CST" -> 21601000L,
      "Thu, 01 Jan 1970 00:00:01 MST" -> 25201000L,
      "Thu, 01 Jan 1970 00:00:01 PDT" -> 25201000L,
      "garbage" -> -1L,
      "" -> -1L,
      "1 Jan 1970 00:00 GMT" -> 0L,
      "01/02/1970 GMT" -> 86400000L,
      "Jan 2 1970 GMT" -> 86400000L,
      "Thu, 01 Jan 70 00:00:01 GMT" -> 1000L,
      "Thu, 01 Jan 1970 12:00:01 PM GMT" -> 43201000L,
      "Thu, 01 Jan 1970 00:00:01 GMT+0130" -> -5399000L,
      "Thu, 01 Jan 1970 00:00:01 -0130" -> 5401000L,
      "Thu, 01 Jan 1970 00:00:01 +01:30" -> -1L
    )
    cases.foreach {
      case (d, expected) =>
        c.headers.put("D", d)
        assertEquals(d, expected, c.getHeaderFieldDate("D", -1))
    }
  }

  @Test def requestProperties(): Unit = {
    val c = conn()
    assertNull(c.getRequestProperty("a"))
    assertNull(c.getRequestProperty(null))
    assertThrows(classOf[NullPointerException], c.setRequestProperty(null, "v"))
    assertThrows(classOf[NullPointerException], c.addRequestProperty(null, "v"))
    c.setRequestProperty("Accept", "a")
    c.addRequestProperty("Accept", "b")
    c.addRequestProperty("X", "1")
    c.setRequestProperty("Y", null)
    assertEquals("b", c.getRequestProperty("Accept"))
    assertEquals("b", c.getRequestProperty("accept"))
    val props = c.getRequestProperties()
    // JDK 17 and 21 list the values of a key newest first
    if (!executingInJVMOnLowerThanJDK(25))
      assertEquals("{Accept=[a, b], X=[1], Y=[null]}", props.toString)
    assertThrows(classOf[UnsupportedOperationException], props.put("z", null))
    assertThrows(
      classOf[UnsupportedOperationException],
      props.get("Accept").add("z")
    )
    c.setRequestProperty("accept", "c") // replaces the last value
    if (!executingInJVMOnLowerThanJDK(25))
      assertEquals(
        "{Accept=[a, c], X=[1], Y=[null]}",
        c.getRequestProperties().toString
      )
  }

  @Test def timeouts(): Unit = {
    val c = conn()
    val e =
      assertThrows(classOf[IllegalArgumentException], c.setConnectTimeout(-1))
    assertEquals("timeout can not be negative", e.getMessage())
    assertThrows(classOf[IllegalArgumentException], c.setReadTimeout(-1))
    c.setConnectTimeout(5)
    c.setReadTimeout(6)
    assertEquals(5, c.getConnectTimeout())
    assertEquals(6, c.getReadTimeout())
  }

  @Test def stateAfterConnect(): Unit = {
    val c = conn()
    c.setRequestProperty("Accept", "a")
    c.connect()
    Seq[() => Any](
      () => c.setDoInput(false),
      () => c.setDoOutput(true),
      () => c.setUseCaches(false),
      () => c.setAllowUserInteraction(true),
      () => c.setIfModifiedSince(1),
      () => c.setRequestProperty("a", "b"),
      () => c.addRequestProperty("a", "b"),
      () => c.getRequestProperty("Accept"),
      () => c.getRequestProperties()
    )
      .foreach { f =>
        val e = assertThrows(classOf[IllegalStateException], f())
        assertEquals("Already connected", e.getMessage())
      }
    c.setConnectTimeout(5)
    c.setDefaultUseCaches(true)
  }

  @Test def defaultUseCaches(): Unit = {
    val c = conn()
    c.setDefaultUseCaches(false)
    try {
      val c2 = conn()
      assertFalse(c2.getUseCaches())
      assertFalse(c2.getDefaultUseCaches())
      assertTrue(c.getUseCaches())
    } finally c.setDefaultUseCaches(true)
  }

  @Test def unsupportedStreams(): Unit = {
    val c = new URLConnection(new URL("http://h/")) { def connect(): Unit = () }
    assertEquals(
      "protocol doesn't support input",
      assertThrows(classOf[UnknownServiceException], c.getInputStream())
        .getMessage()
    )
    assertEquals(
      "protocol doesn't support output",
      assertThrows(classOf[UnknownServiceException], c.getOutputStream())
        .getMessage()
    )
  }

  @Test def getContent(): Unit = {
    def withType(t: String) = {
      val c = conn("http://h/x.html")
      if (t != null) c.headers.put("content-type", t)
      c.in = new ByteArrayInputStream("hi".getBytes())
      c
    }
    // Without a content handler factory the content is the input stream
    val c = withType("x/y")
    assertSame(c.in, c.getContent())
    val w = withType("text/html; charset=utf-8")
    assertSame(w.in, w.getContent())
    val d = withType("x/y")
    assertSame(
      d.in,
      d.getContent(Array[Class[_]](classOf[String], classOf[InputStream]))
    )
    assertNull(withType("x/y").getContent(Array[Class[_]](classOf[String])))
    val e = assertThrows(
      classOf[UnknownServiceException],
      withType(null).getContent()
    )
    assertEquals("no content-type", e.getMessage())
  }

  @Test def contentHandlerDefault(): Unit = {
    val c = conn()
    c.in = new ByteArrayInputStream(Array[Byte](1))
    val h = new ContentHandler {
      def getContent(urlc: URLConnection): AnyRef = urlc.getInputStream()
    }
    assertSame(c.in, h.getContent(c, Array[Class[_]](classOf[InputStream])))
    assertNull(h.getContent(c, Array[Class[_]](classOf[String])))
  }

  @Test def guessContentTypeFromName(): Unit = {
    // The built-in table follows JDK 25
    assumeFalse(executingInJVMOnLowerThanJDK(25))
    val cases = Seq(
      "txt" -> "text/plain",
      "java" -> "text/plain",
      "html" -> "text/html",
      "htm" -> "text/html",
      "xml" -> "application/xml",
      "css" -> "text/css",
      "js" -> "text/javascript",
      "json" -> "application/json",
      "gif" -> "image/gif",
      "jpg" -> "image/jpeg",
      "jpeg" -> "image/jpeg",
      "png" -> "image/png",
      "svg" -> "image/svg+xml",
      "pdf" -> "application/pdf",
      "zip" -> "application/zip",
      "jar" -> "application/java-archive",
      "gz" -> "application/gzip",
      "tar" -> "application/x-tar",
      "mp3" -> "audio/mpeg",
      "mp4" -> "video/mp4",
      "wav" -> "audio/x-wav",
      "bin" -> "application/octet-stream",
      "PNG" -> "image/png",
      "Html" -> "text/html",
      "wasm" -> "application/wasm",
      "csv" -> "text/csv",
      "zzz" -> null,
      "class" -> null,
      "ico" -> null,
      "" -> null
    )
    cases.foreach {
      case (ext, t) =>
        assertEquals(ext, t, URLConnection.guessContentTypeFromName("f." + ext))
    }
    assertNull(URLConnection.guessContentTypeFromName("a/b/"))
    assertNull(URLConnection.guessContentTypeFromName("README"))
    assertEquals(
      "text/html",
      URLConnection.guessContentTypeFromName("a.html#x")
    )
    assertNull(URLConnection.guessContentTypeFromName("a.html?x=1"))
    assertNull(URLConnection.guessContentTypeFromName("a.d/b"))
    assertEquals(
      "image/png",
      URLConnection.guessContentTypeFromName("http://h/a.png")
    )
    assertEquals("image/gif", URLConnection.guessContentTypeFromName(".gif"))
    assertNull(URLConnection.guessContentTypeFromName("a."))
    assertThrows(
      classOf[NullPointerException],
      URLConnection.guessContentTypeFromName(null)
    )
  }

  @Test def fileNameMap(): Unit = {
    val default = URLConnection.getFileNameMap()
    assertEquals("image/png", default.getContentTypeFor("x.png"))
    // Delegates to the default map: other test classes run concurrently on the JVM
    try {
      URLConnection.setFileNameMap(name =>
        if (name.endsWith(".sntest")) "custom/" + name
        else default.getContentTypeFor(name)
      )
      assertEquals(
        "custom/a.sntest",
        URLConnection.guessContentTypeFromName("a.sntest")
      )
      assertEquals("image/png", URLConnection.guessContentTypeFromName("a.png"))
    } finally URLConnection.setFileNameMap(default)
    assertNull(URLConnection.guessContentTypeFromName("a.sntest"))
  }

  private def bytes(s: String): Array[Byte] = s.toCharArray.map(_.toByte)

  @Test def guessContentTypeFromStream(): Unit = {
    val cases = Seq(
      "GIF89a" -> "image/gif",
      "GIF8" -> "image/gif",
      "GI" -> null,
      "<html>" -> "text/html",
      "<HTML>" -> "text/html",
      "<Html>" -> null,
      "  <HTML>" -> null,
      "<!DOCTYPE html>" -> "text/html",
      "<!x" -> "text/html",
      "<!" -> "text/html",
      "<head>" -> "text/html",
      "<HEAD" -> "text/html",
      "<Head>" -> null,
      "<body>" -> "text/html",
      "<BODY>" -> "text/html",
      "<htm" -> null,
      "<?xml version" -> "application/xml",
      "<?xml " -> "application/xml",
      "<?xml" -> null,
      "<?XML version" -> null,
      "<TITLE>" -> null,
      "<h1>" -> null,
      "hello" -> null,
      "" -> null,
      "PK\u0003\u0004" -> null,
      "#def" -> "image/x-bitmap",
      "! XPM2" -> "image/x-pixmap",
      "\u0089PNG\r\n\u001a\nxx" -> "image/png",
      "\u0089PNG" -> null,
      "ÿØÿà" -> "image/jpeg",
      "ÿØÿî" -> "image/jpeg",
      "ÿØÿá" -> null,
      "Êþº¾" -> "application/java-vm",
      "Êþº" -> null,
      "¬í" -> "application/x-java-serialized-object",
      "RIFF" -> "audio/x-wav",
      ".snd" -> "audio/basic",
      "II*\u0000" -> "image/tiff",
      "MM\u0000*" -> "image/tiff",
      "%PDF-1.4" -> null,
      "BM" -> null,
      "ÿþ<\u0000?\u0000x\u0000m\u0000l\u0000" -> "application/xml",
      "þÿ\u0000<\u0000?\u0000x\u0000m\u0000l" -> "application/xml",
      "\u0000\u0000þÿ\u0000\u0000\u0000<\u0000\u0000\u0000?\u0000\u0000\u0000x\u0000\u0000\u0000m\u0000\u0000\u0000l" -> "application/xml",
      "þÿ\u0000<\u0000h\u0000t\u0000m\u0000l\u0000>" -> null,
      "ï»¿<?xml" -> "application/xml",
      "ï»¿<html>" -> null
    )
    cases.foreach {
      case (s, t) =>
        assertEquals(
          s,
          t,
          URLConnection.guessContentTypeFromStream(
            new ByteArrayInputStream(bytes(s))
          )
        )
    }
    val in = new ByteArrayInputStream(bytes("<html>"))
    URLConnection.guessContentTypeFromStream(in)
    assertEquals('<'.toInt, in.read())
    val noMark = new InputStream {
      def read(): Int = '<'
      override def markSupported(): Boolean = false
    }
    assertNull(URLConnection.guessContentTypeFromStream(noMark))
  }
}
