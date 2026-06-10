package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.net.URI
import java.nio.file._
import java.util.HashMap

import org.junit.Assert._
import org.junit.Assume._
import org.junit._

import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVM

/** `provider.getPath(uri)`, `isSameFile`, `isHidden`.
 *
 *  Drives the prebuilt `io-fixture.jar` fixture. Cross-checks Native
 *  ZipFS against jdk.zipfs.
 */
object ZipUriRoundtripTest {
  private val fixtureResource = "io-fixture.jar"
  private def sourceFixturePath: Path = {
    val root = if (Platform.executingInJVM) "../.." else "unit-tests"
    Paths.get(s"$root/shared/src/test/resources/zipfs/$fixtureResource")
  }
}

class ZipUriRoundtripTest {
  import ZipUriRoundtripTest._

  private var tempDir: Path = _
  private var jar: Path = _
  private var fs: FileSystem = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-uri")
    jar = tempDir.resolve("uri.jar")
    Files.copy(sourceFixturePath, jar)
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

  // --- getPath(uri) -------------------------------------------------------

  @Test def getPathRoundTripViaToUri(): Unit = {
    val original = fs.getPath("/META-INF/MANIFEST.MF")
    val uri = original.toUri()
    val resolved = fs.provider().getPath(uri)
    assertEquals(original, resolved)
  }

  @Test def getPathRootEntry(): Unit = {
    // jdk.zipfs requires `!/` even for the root — match the same
    // requirement on Native so consumers can rely on a single URI shape.
    val rootUri = new URI("jar:" + jar.toUri.toString + "!/")
    val p = fs.provider().getPath(rootUri)
    assertEquals(fs.getPath("/"), p)
  }

  @Test def getPathBareArchiveUriRejected(): Unit = {
    // No `!/` — jdk.zipfs rejects with IllegalArgumentException; Native
    // matches that contract.
    val bare = new URI("jar:" + jar.toUri.toString)
    try {
      fs.provider().getPath(bare)
      fail("expected IllegalArgumentException for URI without `!/`")
    } catch { case _: IllegalArgumentException => () }
  }

  @Test def getPathWithSpaceInEntryName(): Unit = {
    val original = fs.getPath("/with space.txt")
    val uri = original.toUri()
    // Percent-encoding round-trip: toUri encodes the space, getPath decodes.
    assertEquals(original, fs.provider().getPath(uri))
  }

  @Test def getPathWithPlusInEntryName(): Unit = {
    // `+` is a literal character in URI path semantics (form-encoding
    // would decode it as a space). Round-trip must preserve `+`.
    val original = fs.getPath("/a+b.txt")
    val uri = original.toUri()
    val resolved = fs.provider().getPath(uri)
    assertEquals(original, resolved)
    assertEquals("/a+b.txt", resolved.toString)
  }

  @Test def getPathWithPercentEncodedQuestionMark(): Unit = {
    // `?` is a URI query delimiter even for opaque URIs, so it has to be
    // percent-encoded as `%3F` to survive into the entry tail. With that
    // encoding the round-trip must decode it back to a literal `?`.
    val raw = "jar:" + jar.toUri.toString + "!/a%3Fb.txt"
    val resolved = fs.provider().getPath(new URI(raw))
    assertEquals(fs.getPath("/a?b.txt"), resolved)
    assertEquals("/a?b.txt", resolved.toString)
  }

  @Test def getPathRejectsMalformedUtf8PercentEscape(): Unit = {
    // `%FF` is syntactically a valid percent-escape but not a valid UTF-8
    // start byte. Strict decoding must surface an IllegalArgumentException
    // rather than silently inserting U+FFFD into the entry name.
    //
    // jdk.zipfs decodes leniently and produces a path containing the
    // replacement character — another spec-conformance divergence we
    // intentionally don't mirror.
    assumeTrue(
      "jdk.zipfs decodes malformed UTF-8 leniently; Native rejects",
      !executingInJVM
    )
    val bad = new URI("jar:" + jar.toUri.toString + "!/%FF.txt")
    try {
      fs.provider().getPath(bad)
      fail("expected IllegalArgumentException for malformed UTF-8 escape")
    } catch { case _: IllegalArgumentException => () }
  }

  @Test def getPathDoesNotSilentlyTruncateRawQuestionMark(): Unit = {
    // Raw `?` in a `jar:` URI tail must NOT silently truncate the entry
    // name. The two `URI` implementations split this differently:
    //   - JVM URI treats `?` as a query delimiter even on opaque URIs,
    //     so getRawSchemeSpecificPart() loses the tail; we then reject
    //     in parseJarUri with IllegalArgumentException.
    //   - Native URI keeps the `?` inside the SSP; the percent-only
    //     decoder preserves it and the resolved path is `/a?b.txt`.
    // Either way, what we must NOT do is silently resolve to `/a`.
    val raw = new URI("jar:" + jar.toUri.toString + "!/a?b.txt")
    try {
      val resolved = fs.provider().getPath(raw)
      assertEquals(fs.getPath("/a?b.txt"), resolved)
    } catch { case _: IllegalArgumentException => () }
  }

  @Test def getPathOnUnopenedArchiveThrows(): Unit = {
    // Build a URI pointing at a *different* (non-existent) archive — no
    // ZipFileSystem registered for it → FileSystemNotFoundException.
    val otherJar = tempDir.resolve("nope.jar")
    val uri = new URI("jar:" + otherJar.toUri.toString + "!/anything")
    try {
      fs.provider().getPath(uri)
      fail("expected FileSystemNotFoundException")
    } catch { case _: FileSystemNotFoundException => () }
  }

  @Test def getPathRejectsNonJarScheme(): Unit = {
    try {
      fs.provider().getPath(new URI("file:/foo.txt"))
      fail("expected IllegalArgumentException")
    } catch {
      case _: IllegalArgumentException => ()
      // jdk.zipfs may report ProviderMismatchException for non-jar URIs
      // depending on dispatch path; accept either.
      case _: ProviderMismatchException => ()
    }
  }

  // --- isSameFile ---------------------------------------------------------

  @Test def isSameFileIdentity(): Unit = {
    val p = fs.getPath("/hello.txt")
    assertTrue(fs.provider().isSameFile(p, p))
  }

  @Test def isSameFileTwoConstructionsSamePath(): Unit = {
    val a = fs.getPath("/hello.txt")
    val b = fs.getPath("hello.txt").toAbsolutePath()
    assertTrue(fs.provider().isSameFile(a, b))
  }

  @Test def isSameFileNormalisation(): Unit = {
    val a = fs.getPath("/META-INF/MANIFEST.MF")
    val b = fs.getPath("/META-INF/../META-INF/MANIFEST.MF")
    assertTrue(fs.provider().isSameFile(a, b))
  }

  @Test def isSameFileDifferentEntries(): Unit = {
    val a = fs.getPath("/hello.txt")
    val b = fs.getPath("/META-INF/MANIFEST.MF")
    assertFalse(fs.provider().isSameFile(a, b))
  }

  @Test def isSameFileAcrossFileSystems(): Unit = {
    // Open a second ZipFS instance over a different archive (a copy of
    // the same bytes, different canonical path). Paths from each must
    // not compare as same.
    val secondJar = tempDir.resolve("second.jar")
    Files.copy(jar, secondJar)
    val fs2 = FileSystems.newFileSystem(
      new URI("jar:" + secondJar.toUri.toString),
      new HashMap[String, Object]()
    )
    try {
      val a = fs.getPath("/hello.txt")
      val b = fs2.getPath("/hello.txt")
      assertFalse(fs.provider().isSameFile(a, b))
    } finally fs2.close()
  }

  // --- isHidden -----------------------------------------------------------

  @Test def isHiddenAlwaysFalse(): Unit = {
    assertFalse(fs.provider().isHidden(fs.getPath("/hello.txt")))
    assertFalse(fs.provider().isHidden(fs.getPath("/")))
    assertFalse(fs.provider().isHidden(fs.getPath("/META-INF/.hidden")))
  }
}
