package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.io.FileOutputStream
import java.net.URI
import java.nio.file._
import java.util.HashMap
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.junit.Assert._
import org.junit._

/** Path semantics for the `jar:` filesystem.
 *
 *  Tests exercise pure `ZipPath` value-logic (no entry reads), and are written
 *  against the `java.nio.file.Path` API so they run unchanged on both Native
 *  (our `ZipPath`) and JVM (jdk.zipfs's `ZipPath`).
 */
class ZipPathTest {

  private var tempDir: Path = _
  private var fs: FileSystem = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-path")
    val jar = tempDir.resolve("paths.jar")
    val zos = new ZipOutputStream(new FileOutputStream(jar.toFile))
    try {
      zos.putNextEntry(new ZipEntry("a.txt"))
      zos.write("a".getBytes("UTF-8"))
      zos.closeEntry()
    } finally zos.close()
    fs = FileSystems.newFileSystem(
      new URI("jar:" + jar.toUri.toString),
      new HashMap[String, Object]()
    )
  }

  @After def tearDown(): Unit = {
    try fs.close()
    catch { case _: Throwable => () }
    try {
      val it = Files.walk(tempDir).iterator()
      val all = new java.util.ArrayList[Path]()
      while (it.hasNext()) all.add(it.next())
      var i = all.size() - 1
      while (i >= 0) {
        try Files.deleteIfExists(all.get(i))
        catch { case _: Throwable => () }
        i -= 1
      }
    } catch { case _: Throwable => () }
  }

  private def p(s: String): Path = fs.getPath(s)

  @Test def basicGetPath(): Unit = {
    assertEquals("/foo/bar", p("/foo/bar").toString)
    assertEquals("foo/bar", p("foo/bar").toString)
    assertEquals("/", p("/").toString)
    assertEquals("", p("").toString)
  }

  @Test def getPathVarargsJoinsWithSlash(): Unit = {
    assertEquals("/a/b/c", fs.getPath("/a", "b", "c").toString)
    assertEquals("a/b/c", fs.getPath("a", "b", "c").toString)
    // empty components are skipped
    assertEquals("/a/b", fs.getPath("/a", "", "b").toString)
    // trailing slash on first must not produce doubles
    assertEquals("/a/b", fs.getPath("/a/", "b").toString)
  }

  @Test def isAbsoluteAndRoot(): Unit = {
    assertTrue(p("/a").isAbsolute)
    assertFalse(p("a").isAbsolute)
    assertEquals("/", p("/a/b").getRoot.toString)
    assertNull(p("a/b").getRoot)
    assertNull(p("").getRoot)
  }

  @Test def getFileNameAndParent(): Unit = {
    assertEquals("c", p("/a/b/c").getFileName.toString)
    assertEquals("c", p("a/b/c").getFileName.toString)
    assertNull(p("/").getFileName)

    assertEquals("/a/b", p("/a/b/c").getParent.toString)
    assertEquals("a/b", p("a/b/c").getParent.toString)
    assertEquals("/", p("/a").getParent.toString)
    assertNull(p("a").getParent)
    assertNull(p("/").getParent)
  }

  @Test def nameCountAndGetName(): Unit = {
    assertEquals(0, p("/").getNameCount)
    // Empty relative path has a single empty name (JDK convention).
    assertEquals(1, p("").getNameCount)
    assertEquals(3, p("/a/b/c").getNameCount)
    assertEquals(3, p("a/b/c").getNameCount)
    assertEquals("a", p("/a/b/c").getName(0).toString)
    assertEquals("c", p("/a/b/c").getName(2).toString)
  }

  @Test def subpath(): Unit = {
    assertEquals("a/b", p("/a/b/c").subpath(0, 2).toString)
    assertEquals("b/c", p("/a/b/c").subpath(1, 3).toString)
    assertEquals("b", p("/a/b/c").subpath(1, 2).toString)
  }

  @Test def normalize(): Unit = {
    assertEquals("/a/c", p("/a/b/../c").normalize.toString)
    assertEquals("/a", p("/a/./b/..").normalize.toString)
    assertEquals("/", p("/..").normalize.toString)
    assertEquals("../a", p("../a").normalize.toString)
    assertEquals("a", p("./a").normalize.toString)
  }

  @Test def resolve(): Unit = {
    assertEquals("/a/b/c", p("/a/b").resolve("c").toString)
    assertEquals("/a/b/c", p("/a/b").resolve(p("c")).toString)
    // absolute "other" wins
    assertEquals("/x", p("/a/b").resolve(p("/x")).toString)
    // empty "other" returns this
    assertEquals("/a/b", p("/a/b").resolve("").toString)
  }

  @Test def resolveSibling(): Unit = {
    assertEquals("/a/x", p("/a/b").resolveSibling("x").toString)
    assertEquals("x", p("a").resolveSibling("x").toString)
  }

  @Test def relativize(): Unit = {
    assertEquals("c", p("/a/b").relativize(p("/a/b/c")).toString)
    assertEquals("../d", p("/a/b/c").relativize(p("/a/b/d")).toString)
    assertEquals("..", p("/a/b").relativize(p("/a")).toString)
    assertEquals("", p("/a/b").relativize(p("/a/b")).toString)
  }

  @Test def startsWithEndsWith(): Unit = {
    assertTrue(p("/a/b/c").startsWith(p("/a/b")))
    assertTrue(p("/a/b/c").startsWith("/a/b"))
    assertFalse(p("/a/b/c").startsWith(p("a/b")))
    assertFalse(p("a/b/c").startsWith(p("/a/b")))

    assertTrue(p("/a/b/c").endsWith(p("b/c")))
    assertTrue(p("/a/b/c").endsWith("c"))
    assertTrue(p("/a/b/c").endsWith(p("/a/b/c")))
    assertFalse(p("/a/b/c").endsWith(p("/b/c")))
  }

  @Test def toAbsolutePath(): Unit = {
    assertEquals("/a/b", p("/a/b").toAbsolutePath.toString)
    assertEquals("/a/b", p("a/b").toAbsolutePath.toString)
  }

  @Test def equalsAndHashCode(): Unit = {
    assertEquals(p("/a/b"), p("/a/b"))
    assertEquals(p("/a/b").hashCode, p("/a/b").hashCode)
    assertNotEquals(p("/a/b"), p("a/b"))
  }

  @Test def compareToOrder(): Unit = {
    assertEquals(0, p("/a").compareTo(p("/a")))
    assertTrue(p("/a").compareTo(p("/b")) < 0)
    assertTrue(p("/b").compareTo(p("/a")) > 0)
  }

  @Test def iteratorYieldsNames(): Unit = {
    val it = p("/a/b/c").iterator()
    val acc = new java.util.ArrayList[String]()
    while (it.hasNext()) acc.add(it.next().toString)
    assertEquals(3, acc.size)
    assertEquals("a", acc.get(0))
    assertEquals("b", acc.get(1))
    assertEquals("c", acc.get(2))
  }

  @Test def toUriRoundTrip(): Unit = {
    val u = p("/META-INF/MANIFEST.MF").toUri
    assertEquals("jar", u.getScheme)
    val s = u.toString
    assertTrue(s"unexpected toUri: $s", s.endsWith("!/META-INF/MANIFEST.MF"))
  }

  @Test def toUriEncodesSpecialChars(): Unit = {
    // Space and `#` in entry names must be percent-encoded, not parsed as
    // URI structure (`#` would otherwise be taken as a fragment).
    val uSpace = p("/a b").toUri
    assertNull("space must not produce a fragment", uSpace.getFragment)
    val sSpace = uSpace.toString
    assertTrue(s"unexpected: $sSpace", sSpace.endsWith("!/a%20b"))

    val uHash = p("/a#b").toUri
    assertNull("`#` must not produce a fragment", uHash.getFragment)
    val sHash = uHash.toString
    assertTrue(s"unexpected: $sHash", sHash.endsWith("!/a%23b"))
  }

  @Test def getPathRejectsNullComponents(): Unit = {
    try {
      fs.getPath(null.asInstanceOf[String])
      fail("expected NPE for null first")
    } catch { case _: NullPointerException => () }

    try {
      fs.getPath("a", null.asInstanceOf[String])
      fail("expected NPE for null more[0]")
    } catch { case _: NullPointerException => () }

    try {
      fs.getPath("a", "b", null.asInstanceOf[String], "c")
      fail("expected NPE for null in middle of more[]")
    } catch { case _: NullPointerException => () }
  }

  @Test def toFileThrows(): Unit = {
    try {
      p("/a").toFile()
      fail("expected UnsupportedOperationException")
    } catch { case _: UnsupportedOperationException => () }
  }

  @Test def slashCollapse(): Unit = {
    assertEquals("/a/b", p("//a//b").toString)
    assertEquals("/a/b", p("/a/b/").toString)
    assertEquals("/", p("///").toString)
  }
}
