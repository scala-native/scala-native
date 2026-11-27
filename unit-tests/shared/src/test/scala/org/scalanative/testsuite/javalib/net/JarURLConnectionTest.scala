package org.scalanative.testsuite.javalib.net

import java.io.{
  File, FileNotFoundException, FileOutputStream, IOException, InputStream
}
import java.net._
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.jar.{Attributes, JarEntry, JarOutputStream, Manifest}

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Assume.assumeFalse
import org.junit.{After, Before, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

@nowarn("cat=deprecation")
class JarURLConnectionTest {
  private var dir: File = null
  private var jar: File = null

  private def writeJar(f: File, manifest: Manifest)(
      entries: (String, String)*
  ): Unit = {
    val out =
      if (manifest == null) new JarOutputStream(new FileOutputStream(f))
      else new JarOutputStream(new FileOutputStream(f), manifest)
    try
      entries.foreach {
        case (name, content) =>
          out.putNextEntry(new JarEntry(name))
          if (content != null)
            out.write(content.getBytes(StandardCharsets.UTF_8))
          out.closeEntry()
      }
    finally out.close()
  }

  @Before def setUp(): Unit = {
    // The URLs below are built from POSIX paths
    assumeFalse(Platform.isWindows)
    dir = Files.createTempDirectory("jar-url-test").toFile()
    jar = new File(dir, "t t.jar")
    val manifest = new Manifest()
    val main = manifest.getMainAttributes()
    main.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    main.putValue("Main-Class", "a.B")
    val attrs = new Attributes()
    attrs.putValue("X-Foo", "bar")
    manifest.getEntries().put("a/b.txt", attrs)
    writeJar(jar, manifest)(
      "a/" -> null,
      "a/b.txt" -> "hello",
      "noext" -> "<html>",
      "sp ace.txt" -> "xy",
      "d!/x" -> "bang"
    )
  }

  @After def tearDown(): Unit = if (dir != null) {
    def delete(f: File): Unit = {
      val children = f.listFiles()
      if (children != null) children.foreach(delete)
      f.delete()
    }
    delete(dir)
  }

  private def fileURL(f: File): String = f.toURI().toURL().toExternalForm()

  private def jarURL(f: File): String = "jar:" + fileURL(f) + "!/"

  private def open(entry: String, f: File = jar): JarURLConnection =
    new URL(jarURL(f) + entry).openConnection().asInstanceOf[JarURLConnection]

  private def read(in: InputStream): String =
    try new String(in.readAllBytes(), StandardCharsets.UTF_8)
    finally in.close()

  @Test def entry(): Unit = {
    val c = open("a/b.txt")
    assertEquals(new URL(fileURL(jar)), c.getJarFileURL())
    assertEquals("a/b.txt", c.getEntryName())
    assertEquals("text/plain", c.getContentType())
    assertEquals(5, c.getContentLength())
    assertEquals(5L, c.getContentLengthLong())
    assertEquals("a/b.txt", c.getJarEntry().getName())
    assertEquals("bar", c.getAttributes().getValue("X-Foo"))
    assertEquals("a.B", c.getMainAttributes().getValue("Main-Class"))
    assertTrue(c.getLastModified() > 0)
    assertEquals("hello", read(c.getInputStream()))
    assertThrows(classOf[IllegalStateException], c.setUseCaches(false))
  }

  @Test def manifestIsACopy(): Unit = {
    val c = open("a/b.txt")
    assertNotSame(c.getJarFile().getManifest(), c.getManifest())
    c.getManifest().getMainAttributes().putValue("Z", "1")
    c.getMainAttributes().putValue("Y", "1")
    val main = c.getJarFile().getManifest().getMainAttributes()
    assertNull(main.getValue("Z"))
    assertNull(main.getValue("Y"))
    c.getManifest().getAttributes("a/b.txt").putValue("Q", "1")
    assertNull(
      c.getJarFile().getManifest().getAttributes("a/b.txt").getValue("Q")
    )
  }

  @Test def noManifest(): Unit = {
    val f = new File(dir, "n.jar")
    writeJar(f, null)("x" -> "")
    val c = open("x", f)
    assertNull(c.getManifest())
    assertNull(c.getMainAttributes())
    assertNull(c.getAttributes())
  }

  @Test def archive(): Unit = {
    val c = open("")
    assertNull(c.getEntryName())
    assertEquals("x-java/jar", c.getContentType())
    assertEquals(jar.length(), c.getContentLengthLong())
    assertNull(c.getJarEntry())
    assertNull(c.getAttributes())
    assertEquals("a.B", c.getMainAttributes().getValue("Main-Class"))
    assertNotNull(c.getJarFile().getEntry("noext"))
    assertThrows(classOf[IOException], c.getInputStream())
  }

  @Test def contentTypes(): Unit = {
    assertEquals("text/html", open("noext").getContentType())
    val d = open("a/")
    assertEquals("content/unknown", d.getContentType())
    assertEquals(0, d.getContentLength())
    assertEquals(-1, d.getInputStream().read())
  }

  @Test def encodedEntryName(): Unit = {
    val c = open("sp%20ace.txt")
    assertEquals("sp ace.txt", c.getEntryName())
    assertEquals("xy", read(c.getInputStream()))
    assertEquals("sp ace.txt", open("sp ace.txt").getEntryName())
    assertEquals("a/b.txt", open("a/b.txt#ref").getEntryName())
  }

  @Test def missingEntry(): Unit = {
    val c = open("missing")
    assertEquals("missing", c.getEntryName())
    assertEquals("content/unknown", c.getContentType())
    assertEquals(-1, c.getContentLength())
    assertThrows(classOf[FileNotFoundException], c.connect())
    assertThrows(classOf[FileNotFoundException], c.getInputStream())
    assertThrows(classOf[FileNotFoundException], c.getJarEntry())
    assertThrows(classOf[FileNotFoundException], c.getJarFile())
  }

  @Test def missingArchive(): Unit = {
    val missing = new File(dir, "missing.jar")
    // Guessed from the name without connecting
    assertEquals("text/plain", open("a/b.txt", missing).getContentType())
    assertEquals("content/unknown", open("noext", missing).getContentType())
    val c = open("a/b.txt", missing)
    assertEquals(-1, c.getContentLength())
    assertThrows(classOf[IOException], c.connect())
    assertThrows(classOf[IOException], c.getJarFile())
  }

  @Test def separatorInEntryName(): Unit = {
    val c = open("d!/x")
    assertEquals("d!/x", c.getEntryName())
    assertEquals("bang", read(c.getInputStream()))
  }

  // The JDK rejects them when connecting, we already when parsing
  @Test def nestedURLs(): Unit =
    assertThrows(
      classOf[MalformedURLException],
      new URL("jar:" + jarURL(jar) + "a.jar!/x").openConnection()
    )

  @Test def nulInPath(): Unit =
    assertThrows(
      classOf[IOException],
      new URL("jar:" + fileURL(jar) + "%00x!/a/b.txt")
        .openConnection()
        .getInputStream()
    )

  @Test def content(): Unit = {
    val c = open("")
    assertSame(c.getJarFile(), c.getContent())
    assertEquals(
      "hello",
      read(open("a/b.txt").getContent().asInstanceOf[InputStream])
    )
  }

  @Test def caches(): Unit = {
    val c1 = open("a/b.txt")
    assertTrue(c1.getUseCaches())
    val c2 = open("")
    assertSame(c1.getJarFile(), c2.getJarFile())
    c1.getInputStream().close()
    assertNotNull(c1.getJarFile().getManifest())
    // A closed JarFile leaves the cache
    c1.getJarFile().close()
    assertEquals("hello", read(open("a/b.txt").getInputStream()))
  }

  @Test def noCaches(): Unit = {
    val cached = open("a/b.txt").getJarFile()
    val c = open("a/b.txt")
    c.setUseCaches(false)
    val f = c.getJarFile()
    assertNotSame(cached, f)
    assertEquals("hello", read(c.getInputStream()))
    assertThrows(classOf[IllegalStateException], f.getManifest())
    assertNotNull(cached.getManifest())
  }
}
