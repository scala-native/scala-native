package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.util.HashMap

import org.junit.Assert._
import org.junit.Assume._
import org.junit._

import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVM

/** `newInputStream`, `newDirectoryStream`, `checkAccess` over a mounted
 *  archive.
 *
 *  Cross-conformance: same fixture, same assertions on JVM (jdk.zipfs) and
 *  Native (ZipFileSystemProvider). The fixture is a pre-built jar shipped in
 *  `unit-tests/shared/src/test/resources/zipfs/io-fixture.jar`, copied into a
 *  per-test temp dir at @Before. Pre-building (vs writing with
 *  `ZipOutputStream` at runtime) keeps these tests decoupled from current
 *  `java.util.zip` write/time behaviour and guarantees JVM and Native read
 *  identical bytes.
 *
 *  Fixture entries: hello.txt = "hello" (5 bytes) META-INF/ (explicit dir
 *  entry) META-INF/MANIFEST.MF = "Manifest-Version: 1.0\n" (22 bytes) dir/a.txt =
 *  "a" dir/b.txt = "b" dir/sub/nested.txt = "nested" (implied "dir/" and
 *  "dir/sub/") with space.txt = "spc" (entry name with space) Тест.nir =
 *  "russian" (non-ASCII entry name)
 */
object ZipFileSystemIOTest {
  private val fixtureResource = "io-fixture.jar"

  // Same dual-cwd pattern as other shared/resource tests: JVM cwd is
  // unit-tests/jvm/<scala-version>, Native cwd is the project root.
  private def sourceFixturePath: Path = {
    val root = if (Platform.executingInJVM) "../.." else "unit-tests"
    Paths.get(s"$root/shared/src/test/resources/zipfs/$fixtureResource")
  }
}

class ZipFileSystemIOTest {
  import ZipFileSystemIOTest._

  private var tempDir: Path = _
  private var jar: Path = _
  private var fs: FileSystem = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-io")
    jar = tempDir.resolve("io.jar")
    Files.copy(sourceFixturePath, jar)
    // accessMode=readOnly so both platforms expose the same read-only
    // contract (jdk.zipfs would otherwise mount writable by default).
    fs = FileSystems.newFileSystem(
      new URI("jar:" + jar.toUri.toString),
      ZipFileSystemTest.readOnlyEnv()
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

  private def readAll(is: java.io.InputStream): String = {
    val br = new BufferedReader(
      new InputStreamReader(is, StandardCharsets.UTF_8)
    )
    try {
      val sb = new java.lang.StringBuilder()
      var c = br.read()
      while (c != -1) { sb.append(c.toChar); c = br.read() }
      sb.toString
    } finally br.close()
  }

  private def listChildren(dir: Path): java.util.List[String] = {
    val out = new java.util.ArrayList[String]()
    val ds = Files.newDirectoryStream(dir)
    try {
      val it = ds.iterator()
      while (it.hasNext()) out.add(it.next().getFileName.toString)
    } finally ds.close()
    java.util.Collections.sort(out)
    out
  }

  // --- newInputStream ------------------------------------------------------

  @Test def newInputStreamReadsFile(): Unit = {
    val is = Files.newInputStream(fs.getPath("/hello.txt"))
    try assertEquals("hello", readAll(is))
    finally is.close()
  }

  @Test def newInputStreamReadsManifest(): Unit = {
    val is = Files.newInputStream(fs.getPath("/META-INF/MANIFEST.MF"))
    try assertEquals("Manifest-Version: 1.0\n", readAll(is))
    finally is.close()
  }

  @Test def newInputStreamReadsNonAsciiEntryName(): Unit = {
    // Cross-checks that ZipFile.getInputStream skips the LFH using the byte
    // length of the encoded name, not the Java String length.
    val is = Files.newInputStream(fs.getPath("/Тест.nir"))
    try assertEquals("russian", readAll(is))
    finally is.close()
  }

  @Test def newInputStreamMissingThrows(): Unit = {
    try {
      Files.newInputStream(fs.getPath("/no/such/entry"))
      fail("expected NoSuchFileException")
    } catch { case _: NoSuchFileException => () }
  }

  @Test def newInputStreamRejectsWriteOption(): Unit = {
    try {
      Files.newInputStream(fs.getPath("/hello.txt"), StandardOpenOption.WRITE)
      fail(
        "expected UnsupportedOperationException for WRITE on read-only ZipFS"
      )
    } catch { case _: UnsupportedOperationException => () }
  }

  @Test def newInputStreamRejectsUnknownOption(): Unit = {
    // Native-only: a custom OpenOption must be rejected, not silently
    // ignored. jdk.zipfs delegates to its own ChannelInputStream which is
    // tolerant of additional options, so this contract is Native-side.
    assumeTrue("Native-only strict OpenOption contract", !executingInJVM)
    val custom = new OpenOption { override def toString = "CUSTOM" }
    try {
      Files.newInputStream(fs.getPath("/hello.txt"), custom)
      fail("expected UnsupportedOperationException for unknown OpenOption")
    } catch { case _: UnsupportedOperationException => () }
  }

  @Test def newInputStreamOnDirectoryThrows(): Unit = {
    try {
      val is = Files.newInputStream(fs.getPath("/META-INF"))
      try is.close()
      catch { case _: Throwable => () }
      fail("expected IOException for opening a directory as a stream")
    } catch { case _: java.io.IOException => () }
  }

  // --- newDirectoryStream --------------------------------------------------

  @Test def directoryStreamRoot(): Unit = {
    val names = listChildren(fs.getPath("/"))
    // Root immediate children: META-INF, dir, hello.txt, with space.txt, Тест.nir
    assertTrue(names.toString, names.contains("META-INF"))
    assertTrue(names.contains("dir"))
    assertTrue(names.contains("hello.txt"))
    assertTrue(names.contains("with space.txt"))
    assertTrue(names.contains("Тест.nir"))
  }

  @Test def directoryStreamExplicitSubdir(): Unit = {
    val names = listChildren(fs.getPath("/META-INF"))
    assertEquals(1, names.size())
    assertEquals("MANIFEST.MF", names.get(0))
  }

  @Test def directoryStreamImpliedSubdir(): Unit = {
    // "dir/" was never written explicitly; children must still enumerate.
    val names = listChildren(fs.getPath("/dir"))
    assertTrue(names.toString, names.contains("a.txt"))
    assertTrue(names.contains("b.txt"))
    // "sub" is a synthesised sub-directory (no explicit "dir/sub/" entry).
    assertTrue(names.contains("sub"))
  }

  @Test def directoryStreamNestedSubdir(): Unit = {
    val names = listChildren(fs.getPath("/dir/sub"))
    assertEquals(1, names.size())
    assertEquals("nested.txt", names.get(0))
  }

  @Test def directoryStreamMissingThrows(): Unit = {
    // Native ZipFS raises NoSuchFileException first; jdk.zipfs reports the
    // same missing-entry case as NotDirectoryException. Accept either —
    // the spec only says "directory does not exist or other I/O error".
    try {
      Files.newDirectoryStream(fs.getPath("/no/such/dir")).close()
      fail("expected NoSuchFileException or NotDirectoryException")
    } catch {
      case _: NoSuchFileException   => ()
      case _: NotDirectoryException => ()
    }
  }

  @Test def directoryStreamOnFileThrows(): Unit = {
    try {
      Files.newDirectoryStream(fs.getPath("/hello.txt")).close()
      fail("expected NotDirectoryException")
    } catch { case _: NotDirectoryException => () }
  }

  @Test def directoryStreamFilter(): Unit = {
    val onlyTxt = new DirectoryStream.Filter[Path] {
      override def accept(entry: Path): Boolean =
        entry.getFileName.toString.endsWith(".txt")
    }
    val out = new java.util.ArrayList[String]()
    val ds = Files.newDirectoryStream(fs.getPath("/dir"), onlyTxt)
    try {
      val it = ds.iterator()
      while (it.hasNext()) out.add(it.next().getFileName.toString)
    } finally ds.close()
    java.util.Collections.sort(out)
    assertEquals(2, out.size())
    assertEquals("a.txt", out.get(0))
    assertEquals("b.txt", out.get(1))
  }

  @Test def directoryStreamRelativeDirYieldsRelativeChildren(): Unit = {
    // JDK contract: child entries are resolved against the supplied `dir`.
    // For a relative `dir`, children must stay relative — passing through
    // `toAbsolutePath` before joining would yield "/dir/a.txt" instead of
    // "dir/a.txt".
    val ds = Files.newDirectoryStream(fs.getPath("dir"))
    try {
      val it = ds.iterator()
      while (it.hasNext()) {
        val child = it.next()
        assertFalse(
          s"expected relative child path, got $child",
          child.isAbsolute()
        )
        assertEquals(fs.getPath("dir"), child.getParent)
      }
    } finally ds.close()
  }

  @Test def directoryStreamIteratorOnlyOnce(): Unit = {
    val ds = Files.newDirectoryStream(fs.getPath("/dir"))
    try {
      ds.iterator()
      try {
        ds.iterator()
        fail("expected IllegalStateException on second iterator() call")
      } catch { case _: IllegalStateException => () }
    } finally ds.close()
  }

  // --- checkAccess ---------------------------------------------------------

  @Test def checkAccessReadOnExistingFile(): Unit = {
    fs.provider()
      .checkAccess(fs.getPath("/hello.txt"), Array(AccessMode.READ): _*)
  }

  @Test def checkAccessReadOnImpliedDirectory(): Unit = {
    fs.provider().checkAccess(fs.getPath("/dir"), Array.empty[AccessMode]: _*)
  }

  @Test def checkAccessMissingThrows(): Unit = {
    try {
      fs.provider()
        .checkAccess(fs.getPath("/no/such"), Array.empty[AccessMode]: _*)
      fail("expected NoSuchFileException")
    } catch { case _: NoSuchFileException => () }
  }

  @Test def checkAccessWriteDenied(): Unit = {
    // The mount is read-only (accessMode=readOnly), so a WRITE access
    // check must be denied. jdk.zipfs honours accessMode since JDK 23.
    assumeFalse(
      "jdk.zipfs supports accessMode only since JDK 23",
      Platform.executingInJVMOnLowerThanJDK(23)
    )
    try {
      fs.provider()
        .checkAccess(fs.getPath("/hello.txt"), Array(AccessMode.WRITE): _*)
      fail("expected AccessDeniedException")
    } catch { case _: AccessDeniedException => () }
  }

  @Test def checkAccessExecuteDenied(): Unit = {
    try {
      fs.provider()
        .checkAccess(fs.getPath("/hello.txt"), Array(AccessMode.EXECUTE): _*)
      fail("expected AccessDeniedException")
    } catch { case _: AccessDeniedException => () }
  }
}
