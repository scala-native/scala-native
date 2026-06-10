package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.io.FileOutputStream
import java.net.URI
import java.nio.file._
import java.nio.file.attribute.{
  BasicFileAttributeView, BasicFileAttributes, FileTime
}
import java.util.HashMap
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.junit.Assert._
import org.junit.Assume._
import org.junit._

import org.scalanative.testsuite.utils.Platform

/** `BasicFileAttributes` over zip entries.
 *
 *  Fixture: a small jar built at @Before time with one regular file, one
 *  explicit directory entry ("META-INF/"), and a nested file under an implicit
 *  directory ("dir/sub/file.txt" with no "dir/" entry).
 */
class ZipFileAttributesTest {

  private var tempDir: Path = _
  private var fs: FileSystem = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-attrs")
    val jar = tempDir.resolve("attrs.jar")
    val zos = new ZipOutputStream(new FileOutputStream(jar.toFile))
    try {
      val e1 = new ZipEntry("hello.txt")
      zos.putNextEntry(e1)
      zos.write("hello".getBytes("UTF-8"))
      zos.closeEntry()

      val e2 = new ZipEntry("META-INF/")
      zos.putNextEntry(e2)
      zos.closeEntry()

      val e3 = new ZipEntry("META-INF/MANIFEST.MF")
      zos.putNextEntry(e3)
      zos.write("Manifest-Version: 1.0\n".getBytes("UTF-8"))
      zos.closeEntry()

      // No "dir/" entry — "dir" and "dir/sub" must be synthesised.
      val e4 = new ZipEntry("dir/sub/file.txt")
      zos.putNextEntry(e4)
      zos.write("nested".getBytes("UTF-8"))
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

  private def attrs(s: String): BasicFileAttributes =
    Files.readAttributes(
      fs.getPath(s),
      classOf[BasicFileAttributes],
      Array.empty[LinkOption]: _*
    )

  @Test def regularFile(): Unit = {
    val a = attrs("/hello.txt")
    assertTrue(a.isRegularFile)
    assertFalse(a.isDirectory)
    assertFalse(a.isSymbolicLink)
    assertFalse(a.isOther)
    assertEquals(5L, a.size)
  }

  @Test def explicitDirectory(): Unit = {
    val a = attrs("/META-INF")
    assertTrue(a.isDirectory)
    assertFalse(a.isRegularFile)
    assertEquals(0L, a.size)
  }

  @Test def explicitDirectoryWithTrailingSlash(): Unit = {
    // ZipPath normalisation strips trailing slash, so "/META-INF/" and
    // "/META-INF" land on the same path; both should resolve to the dir.
    assertTrue(attrs("/META-INF/").isDirectory)
  }

  @Test def nestedFileUnderImpliedDirectory(): Unit = {
    val a = attrs("/dir/sub/file.txt")
    assertTrue(a.isRegularFile)
    assertEquals(6L, a.size)
  }

  @Test def impliedDirectorySynthesised(): Unit = {
    val a = attrs("/dir")
    assertTrue("/dir should be a synthesised directory", a.isDirectory)
    assertFalse(a.isRegularFile)

    val b = attrs("/dir/sub")
    assertTrue("/dir/sub should be a synthesised directory", b.isDirectory)
  }

  @Test def rootIsDirectory(): Unit = {
    val a = attrs("/")
    assertTrue(a.isDirectory)
    assertFalse(a.isRegularFile)
  }

  @Test def missingFileThrows(): Unit = {
    try {
      attrs("/no/such/entry")
      fail("expected NoSuchFileException")
    } catch { case _: NoSuchFileException => () }
  }

  @Test def stringBasicStar(): Unit = {
    val m = Files.readAttributes(
      fs.getPath("/hello.txt"),
      "basic:*",
      Array.empty[LinkOption]: _*
    )
    assertEquals(java.lang.Long.valueOf(5L), m.get("size"))
    assertEquals(java.lang.Boolean.TRUE, m.get("isRegularFile"))
    assertEquals(java.lang.Boolean.FALSE, m.get("isDirectory"))
    assertTrue(
      "expected lastModifiedTime entry",
      m.containsKey("lastModifiedTime")
    )
  }

  @Test def stringBasicSubset(): Unit = {
    val m = Files.readAttributes(
      fs.getPath("/hello.txt"),
      "basic:size,isDirectory",
      Array.empty[LinkOption]: _*
    )
    assertEquals(2, m.size)
    assertEquals(java.lang.Long.valueOf(5L), m.get("size"))
    assertEquals(java.lang.Boolean.FALSE, m.get("isDirectory"))
  }

  @Test def stringUnknownAttributesSkipped(): Unit = {
    // jdk.zipfs silently ignores unrecognised names in a comma list; match.
    val m = Files.readAttributes(
      fs.getPath("/hello.txt"),
      "basic:size,unknownAttr",
      Array.empty[LinkOption]: _*
    )
    assertEquals(1, m.size)
    assertEquals(java.lang.Long.valueOf(5L), m.get("size"))
  }

  @Test def providerStringEmptyAttrsReturnsEmpty(): Unit = {
    // Provider override directly: `"basic:"` means "no attributes
    // requested" — jdk.zipfs returns an empty map, not the full set.
    // Files.readAttributes on Native bypasses this code path via
    // FileAttributeView.asMap, so call the provider explicitly.
    val m = fs
      .provider()
      .readAttributes(
        fs.getPath("/hello.txt"),
        "basic:",
        Array.empty[LinkOption]: _*
      )
    assertTrue(m.isEmpty)
  }

  @Test def stringAllUnknownAttributesReturnsEmpty(): Unit = {
    val m = Files.readAttributes(
      fs.getPath("/hello.txt"),
      "basic:bogus1,bogus2",
      Array.empty[LinkOption]: _*
    )
    assertTrue(m.isEmpty)
  }

  @Test def stringNoViewPrefixDefaultsToBasic(): Unit = {
    // "size" alone (no "basic:" prefix) is accepted as the basic view.
    val m = Files.readAttributes(
      fs.getPath("/hello.txt"),
      "size",
      Array.empty[LinkOption]: _*
    )
    assertEquals(java.lang.Long.valueOf(5L), m.get("size"))
  }

  @Test def basicFileAttributeView(): Unit = {
    val v = Files.getFileAttributeView(
      fs.getPath("/hello.txt"),
      classOf[BasicFileAttributeView],
      Array.empty[LinkOption]: _*
    )
    assertNotNull(v)
    assertEquals("basic", v.name)
    val a = v.readAttributes()
    assertEquals(5L, a.size)
    assertTrue(a.isRegularFile)
  }

  @Test def setTimesOnReadOnlyMountThrows(): Unit = {
    // setTimes must throw ReadOnlyFileSystemException on an explicit
    // accessMode=readOnly mount. jdk.zipfs honours accessMode since
    // JDK 23.
    assumeFalse(
      "jdk.zipfs supports accessMode only since JDK 23",
      Platform.executingInJVMOnLowerThanJDK(23)
    )
    val roJar = tempDir.resolve("ro-attrs.jar")
    val zos = new ZipOutputStream(new FileOutputStream(roJar.toFile))
    try {
      zos.putNextEntry(new ZipEntry("hello.txt"))
      zos.write("hello".getBytes("UTF-8"))
      zos.closeEntry()
    } finally zos.close()
    val env = new HashMap[String, Object]()
    env.put("accessMode", "readOnly")
    val roFs = FileSystems.newFileSystem(
      new URI("jar:" + roJar.toUri.toString),
      env
    )
    try {
      val v = Files.getFileAttributeView(
        roFs.getPath("/hello.txt"),
        classOf[BasicFileAttributeView],
        Array.empty[LinkOption]: _*
      )
      try {
        v.setTimes(FileTime.fromMillis(0), null, null)
        fail("expected ReadOnlyFileSystemException from setTimes")
      } catch { case _: ReadOnlyFileSystemException => () }
    } finally roFs.close()
  }

  @Test def filesSizeDispatchesThroughProvider(): Unit = {
    assertEquals(5L, Files.size(fs.getPath("/hello.txt")))
    assertEquals(0L, Files.size(fs.getPath("/META-INF")))
  }

  @Test def filesIsRegularFileAndDirectory(): Unit = {
    assertTrue(
      Files.isRegularFile(fs.getPath("/hello.txt"), Array.empty[LinkOption]: _*)
    )
    assertFalse(
      Files.isDirectory(fs.getPath("/hello.txt"), Array.empty[LinkOption]: _*)
    )
    assertTrue(
      Files.isDirectory(fs.getPath("/META-INF"), Array.empty[LinkOption]: _*)
    )
    assertTrue(
      Files.isDirectory(fs.getPath("/dir"), Array.empty[LinkOption]: _*)
    )
  }
}
