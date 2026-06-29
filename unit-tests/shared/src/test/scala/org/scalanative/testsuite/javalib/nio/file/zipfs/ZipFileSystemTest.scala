package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.io.FileOutputStream
import java.net.URI
import java.nio.file.{
  FileSystem, FileSystemAlreadyExistsException, FileSystemNotFoundException,
  FileSystems, Files, Path, ProviderNotFoundException
}
import java.util.HashMap
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.junit.Assert._
import org.junit.Assume._
import org.junit._

import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVM

/** Mount/close lifecycle, registry behaviour and `FileSystem` surface of the
 *  zip file system.
 *
 *  These tests run on the JVM too, where `jdk.zipfs` serves as the conformance
 *  oracle; Native-only assertions are guarded with assumptions that explain the
 *  divergence.
 */
object ZipFileSystemTest {
  // Build a tiny but real jar in the per-test temp directory.
  def makeJar(dir: Path, name: String): Path = {
    val p = dir.resolve(name)
    val zos = new ZipOutputStream(new FileOutputStream(p.toFile))
    try {
      val e = new ZipEntry("hello.txt")
      zos.putNextEntry(e)
      zos.write("hi".getBytes("UTF-8"))
      zos.closeEntry()
    } finally zos.close()
    p
  }

  def emptyEnv(): java.util.Map[String, Object] =
    new HashMap[String, Object]()

  /** Env that asks for a read-only mount. Native ZipFS honours
   *  `accessMode=readOnly` always; jdk.zipfs does so since JDK 23 (older JDKs
   *  ignore the key and mount writable, so read-only-contract assertions must
   *  be guarded with a JDK-version assumption).
   */
  def readOnlyEnv(): java.util.Map[String, Object] = {
    val m = new HashMap[String, Object]()
    m.put("accessMode", "readOnly")
    m
  }

  def jarUri(p: Path): URI =
    new URI("jar:" + p.toUri.toString)
}

class ZipFileSystemTest {
  import ZipFileSystemTest._

  private var tempDir: Path = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-test")
  }

  @After def tearDown(): Unit = {
    // best-effort cleanup; ignore failures
    try {
      val it = Files.walk(tempDir).iterator()
      val all = new java.util.ArrayList[Path]()
      while (it.hasNext()) all.add(it.next())
      // delete deepest first
      for (i <- (all.size() - 1) to 0 by -1) {
        try Files.deleteIfExists(all.get(i))
        catch { case _: Throwable => () }
      }
    } catch { case _: Throwable => () }
  }

  private def withMountedJar(name: String)(body: FileSystem => Unit): Unit = {
    val jar = makeJar(tempDir, name)
    val fs = FileSystems.newFileSystem(jarUri(jar), readOnlyEnv())
    try body(fs)
    finally {
      try fs.close()
      catch { case _: Throwable => () }
    }
  }

  @Test def providerIsRegistered(): Unit = {
    // Smoke check that the SPI plumbing finds *some* jar provider. On Native
    // that comes from our service-loader entry; on JVM from jdk.zipfs.
    val jar = makeJar(tempDir, "providerCheck.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), emptyEnv())
    try assertEquals("jar", fs.provider().getScheme())
    finally fs.close()
  }

  @Test def basicInvariants(): Unit = withMountedJar("invariants.jar") { fs =>
    assertTrue(fs.isOpen())
    assertEquals("/", fs.getSeparator())
    assertEquals("jar", fs.provider().getScheme())
    assertTrue(fs.supportedFileAttributeViews().contains("basic"))
  }

  @Test def readOnlyMountIsReadOnly(): Unit = {
    assumeFalse(
      "jdk.zipfs supports accessMode only since JDK 23",
      Platform.executingInJVMOnLowerThanJDK(23)
    )
    withMountedJar("ro.jar") { fs =>
      assertTrue("accessMode=readOnly mount", fs.isReadOnly())
    }
  }

  @Test def defaultMountIsWritable(): Unit = {
    // Matches jdk.zipfs: an empty env yields a writable mount; read-only
    // is the opt-in accessMode=readOnly (covered separately).
    val jar = makeJar(tempDir, "defaultmode.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), emptyEnv())
    try assertFalse("default mount is writable", fs.isReadOnly())
    finally fs.close()
  }

  @Test def accessModeReadWriteAccepted(): Unit = {
    val jar = makeJar(tempDir, "rw.jar")
    val env = new HashMap[String, Object]()
    env.put("accessMode", "readWrite")
    val fs = FileSystems.newFileSystem(jarUri(jar), env)
    try assertFalse("readWrite mount is writable", fs.isReadOnly())
    finally fs.close()
  }

  @Test def bogusAccessModeRejected(): Unit = {
    assumeFalse(
      "jdk.zipfs supports accessMode only since JDK 23",
      Platform.executingInJVMOnLowerThanJDK(23)
    )
    val jar = makeJar(tempDir, "bogusmode.jar")
    val env = new HashMap[String, Object]()
    env.put("accessMode", "readMostly")
    try {
      val fs = FileSystems.newFileSystem(jarUri(jar), env)
      try fail("expected IllegalArgumentException")
      finally fs.close()
    } catch { case _: IllegalArgumentException => () }
  }

  @Test def getRootDirectoriesYieldsSingleSlash(): Unit =
    withMountedJar("roots.jar") { fs =>
      val it = fs.getRootDirectories().iterator()
      assertTrue(it.hasNext())
      val root = it.next()
      assertEquals("/", root.toString)
      assertFalse("only one root", it.hasNext())
    }

  @Test def getFileStoresIsIterable(): Unit =
    withMountedJar("stores.jar") { fs =>
      val it = fs.getFileStores().iterator()
      assertTrue("expected one ZipFileStore", it.hasNext())
      assertNotNull(it.next())
      assertFalse("only one store", it.hasNext())
    }

  @Test def getPathRoundTrip(): Unit = withMountedJar("getpath.jar") { fs =>
    assertEquals("/a/b/c", fs.getPath("/a/b/c").toString)
    assertEquals("/a/b/c", fs.getPath("/a", "b", "c").toString)
  }

  @Test def pathMatcherGlob(): Unit = withMountedJar("glob.jar") { fs =>
    val m = fs.getPathMatcher("glob:**/*.txt")
    assertTrue(m.matches(fs.getPath("/foo/a.txt")))
    assertFalse(m.matches(fs.getPath("/foo/a.bin")))
  }

  @Test def pathMatcherRegex(): Unit = withMountedJar("regex.jar") { fs =>
    val m = fs.getPathMatcher("regex:/[a-z]+\\.txt")
    assertTrue(m.matches(fs.getPath("/hello.txt")))
    assertFalse(m.matches(fs.getPath("/Hello.txt")))
  }

  @Test def pathMatcherUnknownSyntaxRejected(): Unit =
    withMountedJar("badsyntax.jar") { fs =>
      try {
        fs.getPathMatcher("bogus:foo")
        fail("expected UnsupportedOperationException")
      } catch { case _: UnsupportedOperationException => () }
    }

  @Test def pathMatcherMalformedRejected(): Unit =
    withMountedJar("malformed.jar") { fs =>
      try {
        fs.getPathMatcher("noColonHere")
        fail("expected IllegalArgumentException")
      } catch { case _: IllegalArgumentException => () }
    }

  @Test def closeIsIdempotent(): Unit = withMountedJar("close.jar") { fs =>
    fs.close()
    assertFalse(fs.isOpen())
    fs.close() // must not throw
  }

  @Test def introspectionAfterCloseStillWorks(): Unit =
    withMountedJar("postclose.jar") { fs =>
      // jdk.zipfs gates ClosedFileSystemException on actual I/O methods
      // (newByteChannel, newDirectoryStream, readAttributes, ...) — not on
      // path-construction / metadata / introspection. We match that.
      fs.close()
      assertFalse(fs.isOpen())
      assertEquals("jar", fs.provider().getScheme())
      assertEquals("/", fs.getSeparator())
      assertEquals("/a", fs.getPath("/a").toString)
    }

  @Test def openCloseLifecycle(): Unit = {
    val jar = makeJar(tempDir, "lifecycle.jar")
    val uri = jarUri(jar)

    val fs1 = FileSystems.newFileSystem(uri, readOnlyEnv())
    assertTrue("fs1 open", fs1.isOpen())

    val fs1b = FileSystems.getFileSystem(uri)
    assertSame("getFileSystem returns same instance", fs1, fs1b)

    fs1.close()
    assertFalse("fs1 closed", fs1.isOpen())

    try {
      FileSystems.getFileSystem(uri)
      fail("expected FileSystemNotFoundException after close")
    } catch { case _: FileSystemNotFoundException => () }

    // reopen should work
    val fs2 = FileSystems.newFileSystem(uri, readOnlyEnv())
    try assertTrue("fs2 open", fs2.isOpen())
    finally fs2.close()
  }

  @Test def doubleOpenRejected(): Unit = {
    val jar = makeJar(tempDir, "dup.jar")
    val uri = jarUri(jar)
    val fs = FileSystems.newFileSystem(uri, readOnlyEnv())
    try {
      try {
        FileSystems.newFileSystem(uri, readOnlyEnv())
        fail("expected FileSystemAlreadyExistsException")
      } catch { case _: FileSystemAlreadyExistsException => () }
    } finally fs.close()
  }

  @Test def aliasedPathsCollide(): Unit = {
    // Native-only: our implementation canonicalises registry keys via
    // toRealPath, so two Path values that resolve to the same on-disk file
    // must map to the same FS instance. JDK's jdk.zipfs keys by raw path
    // and intentionally doesn't collide here, so skip on JVM.
    assumeTrue("Native-only stricter aliasing contract", !executingInJVM)

    val jar = makeJar(tempDir, "alias.jar")
    val viaDot = tempDir.resolve(".").resolve("alias.jar")
    assertNotEquals(
      "fixture must produce distinct Path values",
      jar.toString,
      viaDot.toString
    )

    val fs = FileSystems.newFileSystem(jar, null.asInstanceOf[ClassLoader])
    try {
      try {
        FileSystems.newFileSystem(viaDot, null.asInstanceOf[ClassLoader])
        fail("aliased path should collide with existing open FS")
      } catch { case _: FileSystemAlreadyExistsException => () }
    } finally fs.close()
  }

  @Test def newFileSystemPathOverloadRejectsNonZip(): Unit = {
    // FileSystems.newFileSystem(Path, ClassLoader) probes providers; a plain
    // text file should yield ProviderNotFoundException (no provider claims
    // it) rather than masking a real error.
    val plain = tempDir.resolve("notazip.txt")
    Files.write(plain, "hello".getBytes("UTF-8"))
    try {
      FileSystems.newFileSystem(plain, null.asInstanceOf[ClassLoader])
      fail("expected ProviderNotFoundException")
    } catch {
      case _: ProviderNotFoundException => ()
    }
  }

  @Test def newFileSystemPathOverloadOpensZip(): Unit = {
    val jar = makeJar(tempDir, "pathopen.jar")
    val fs =
      FileSystems.newFileSystem(jar, null.asInstanceOf[ClassLoader])
    try {
      assertEquals("jar", fs.provider().getScheme())
      assertTrue(fs.isOpen())
    } finally fs.close()
  }

  @Test def uriParsingRejectsBadInputs(): Unit = {
    // These should fail one way or another. The JDK's jdk.zipfs has slightly
    // different error mappings (e.g. it tries to open `/x.jar` and reports
    // NoSuchFileException instead of rejecting the fragment), so we accept
    // the union of plausible exception types here. The negative-shape
    // contract is "doesn't silently succeed".
    val cases = Seq(
      new URI("file:/foo.jar"), // wrong scheme — no jar provider matches
      new URI("jar:http://example.com/x.jar"), // unsupported inner scheme
      new URI("jar:file:/x.jar#frag") // fragment present
    )
    for (u <- cases) {
      try {
        val fs = FileSystems.newFileSystem(u, emptyEnv())
        try fail(s"expected failure for $u")
        finally fs.close()
      } catch {
        case _: IllegalArgumentException          => ()
        case _: ProviderNotFoundException         => ()
        case _: FileSystemNotFoundException       => ()
        case _: java.nio.file.NoSuchFileException => ()
        case _: FileSystemAlreadyExistsException  =>
          fail(s"registry should not be reached for $u")
      }
    }
  }

  @Test def uriOverloadRejectsNonZipAsRealError(): Unit = {
    // URI overload: the caller named `jar:`, so wrong magic is a real
    // "not a zip" error, not the path-probing "not mine" signal.
    val plain = tempDir.resolve("plain.txt")
    Files.write(plain, "hello".getBytes("UTF-8"))
    val uri = new URI("jar:" + plain.toUri.toString)
    try {
      val fs = FileSystems.newFileSystem(uri, emptyEnv())
      try fail("expected IOException from URI overload on non-zip")
      finally fs.close()
    } catch {
      // Native: ZipException from our discriminator. JVM JDK 11+ may
      // reject `jar:file:/foo.txt` (no `!/`) at URI-dispatch level with
      // ProviderNotFoundException — both shapes are acceptable since the
      // contract is "does not silently succeed".
      case _: java.io.IOException       => ()
      case _: ProviderNotFoundException => ()
    }
  }

  @Test def corruptZipIsRealError(): Unit = {
    // A file whose first 4 bytes look like a zip local-file-header but whose
    // body is garbage must NOT masquerade as "not a zip" (which would turn
    // into ProviderNotFoundException). It must surface as a real IOException
    // from ZipFile parsing.
    val corrupt = tempDir.resolve("corrupt.jar")
    // PK\003\004 header followed by junk; ZipFile parser will fail looking
    // for the EOCD record.
    val bytes = Array[Byte]('P', 'K', 3, 4, 0, 0, 0, 0, 0, 0, 0, 0)
    Files.write(corrupt, bytes)

    try {
      FileSystems.newFileSystem(corrupt, null.asInstanceOf[ClassLoader])
      fail("expected IOException for corrupt zip")
    } catch {
      // We accept either: native ZipException (most likely on Native), or
      // the JDK's various wrappings. The contract is "not
      // ProviderNotFoundException" — i.e. the dispatcher didn't silently
      // skip our provider on a real failure.
      case _: java.io.IOException => ()
    }
  }

  @Test def getFileSystemUnopenedThrows(): Unit = {
    val jar = makeJar(tempDir, "neveropen.jar")
    try {
      FileSystems.getFileSystem(jarUri(jar))
      fail("expected FileSystemNotFoundException")
    } catch { case _: FileSystemNotFoundException => () }
  }
}
