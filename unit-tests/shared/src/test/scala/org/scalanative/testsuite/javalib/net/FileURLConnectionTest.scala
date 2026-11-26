package org.scalanative.testsuite.javalib.net

import java.io.{
  ByteArrayOutputStream, File, FileNotFoundException, FileOutputStream,
  InputStream
}
import java.net._
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Assume.assumeFalse
import org.junit.{After, Before, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVMOnLowerThanJDK

@nowarn("cat=deprecation")
class FileURLConnectionTest {
  private var dir: File = null

  private def write(name: String, content: String): File = {
    val f = new File(dir, name)
    val out = new FileOutputStream(f)
    try out.write(content.getBytes(StandardCharsets.UTF_8))
    finally out.close()
    f
  }

  @Before def setUp(): Unit = {
    // The URLs below are built from POSIX paths
    assumeFalse(Platform.isWindows)
    dir = Files.createTempDirectory("nh-file-url").toFile()
    write("a.txt", "hello").setLastModified(1577930645000L)
    new File(dir, "sub").mkdir()
    write("sub/b", "x")
    write("noext", "<html>")
    write("sp ace.txt", "q")
  }

  @After def tearDown(): Unit = if (dir != null) {
    def delete(f: File): Unit = {
      val children = f.listFiles()
      if (children != null) children.foreach(delete)
      f.delete()
    }
    delete(dir)
  }

  private def url(name: String): URL = new URL(
    "file:" + dir.getPath() + "/" + name
  )

  private def read(in: InputStream): String =
    try {
      val out = new ByteArrayOutputStream()
      var b = in.read()
      while (b != -1) {
        out.write(b)
        b = in.read()
      }
      new String(out.toByteArray(), StandardCharsets.UTF_8)
    } finally in.close()

  @Test def regularFile(): Unit = {
    val c = url("a.txt").openConnection()
    // Header access connects
    assertEquals(3, c.getHeaderFields().size())
    assertEquals(
      java.util.Collections.singletonList("5"),
      c.getHeaderFields().get("content-length")
    )
    assertEquals(
      Seq("content-type", "content-length", "last-modified"),
      (0 to 2).map(c.getHeaderFieldKey)
    )
    assertEquals(
      Seq("text/plain", "5", "Thu, 02 Jan 2020 02:04:05 GMT"),
      (0 to 2).map(i => c.getHeaderField(i))
    )
    assertNull(c.getHeaderFieldKey(3))
    assertNull(c.getHeaderField(3))
    assertEquals("text/plain", c.getContentType())
    assertEquals("text/plain", c.getHeaderField("Content-Type"))
    assertEquals(
      "Thu, 02 Jan 2020 02:04:05 GMT",
      c.getHeaderField("Last-Modified")
    )
    assertEquals(5, c.getContentLength())
    assertEquals(5L, c.getContentLengthLong())
    assertEquals(1577930645000L, c.getLastModified())
    assertEquals(0L, c.getDate())
    assertNull(c.getContentEncoding())
    assertSame(c.getInputStream(), c.getInputStream())
    assertEquals("hello", read(c.getInputStream()))
    assertTrue(c.toString().endsWith(":" + url("a.txt")))
    assertThrows(classOf[UnknownServiceException], c.getOutputStream())
    assertThrows(classOf[IllegalStateException], c.setDoOutput(true))
  }

  @Test def directory(): Unit = {
    val c = url("sub").openConnection()
    assertEquals("content-type", c.getHeaderFieldKey(0))
    assertNull(c.getHeaderFieldKey(1))
    assertEquals("text/plain", c.getContentType())
    assertEquals("b\n", read(c.getInputStream()))
    assertEquals("b\n", read(url("sub/").openStream()))
  }

  @Test def contentTypeFromStream(): Unit =
    assertEquals("text/html", url("noext").openConnection().getContentType())

  @Test def percentDecoding(): Unit = {
    assertEquals("q", read(url("sp%20ace.txt").openStream()))
    assertEquals("q", read(url("sp ace.txt").openStream()))
  }

  @Test def queryAndReferenceIgnored(): Unit = {
    assertEquals("hello", read(url("a.txt?x").openStream()))
    assertEquals("hello", read(url("a.txt#x").openStream()))
  }

  @Test def hosts(): Unit = {
    assertEquals(
      "hello",
      read(new URL("file://localhost" + dir.getPath() + "/a.txt").openStream())
    )
    assertEquals(
      "hello",
      read(new URL("file://" + dir.getPath() + "/a.txt").openStream())
    )
    // Rejected since a JDK newer than 17
    if (!executingInJVMOnLowerThanJDK(25)) {
      val e = assertThrows(
        classOf[MalformedURLException],
        new URL("file://otherhost.invalid/x").openConnection()
      )
      assertEquals("Unsupported non-local file URL", e.getMessage())
    }
  }

  @Test def missingFile(): Unit = {
    val c = url("missing").openConnection()
    assertThrows(classOf[FileNotFoundException], c.connect())
    assertEquals("content/unknown", c.getContentType())
    assertEquals(0, c.getContentLength())
    assertEquals(0L, c.getLastModified())
    assertTrue(c.getHeaderFields().isEmpty())
    assertThrows(classOf[FileNotFoundException], c.getInputStream())
  }

  @Test def getContentIsAStream(): Unit = {
    val content = url("a.txt").getContent()
    assertTrue(content.isInstanceOf[InputStream])
    assertEquals("hello", read(content.asInstanceOf[InputStream]))
  }

  @Test def proxyIgnored(): Unit = {
    val p =
      new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("p", 1))
    assertEquals("hello", read(url("a.txt").openConnection(p).getInputStream()))
  }
}
