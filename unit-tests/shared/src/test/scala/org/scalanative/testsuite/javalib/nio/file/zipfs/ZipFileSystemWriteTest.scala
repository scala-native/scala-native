package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.io.{ByteArrayOutputStream, FileOutputStream, InputStream}
import java.net.URI
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.nio.file.{
  CopyOption, DirectoryNotEmptyException, FileAlreadyExistsException,
  FileSystem, FileSystems, Files, LinkOption, OpenOption, Path,
  StandardCopyOption, StandardOpenOption
}
import java.util.HashMap
import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{After, Before, Test}

import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVM

/* Writable mounts: create=true mounts, the close-time archive rewrite,
 * entry writes/overwrites/appends, createDirectory/delete/copy/move,
 * setAttribute times, and the compressionMethod env key. Runs on the
 * JVM too, with jdk.zipfs as the conformance oracle; round-trips are
 * verified by re-reading the rewritten archive with
 * `java.util.zip.ZipFile`.
 */
class ZipFileSystemWriteTest {
  import ZipFileSystemTest._

  private var tempDir: Path = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-write")
  }

  @After def tearDown(): Unit = {
    try {
      val it = Files.walk(tempDir).iterator()
      val all = new java.util.ArrayList[Path]()
      while (it.hasNext()) all.add(it.next())
      for (i <- (all.size() - 1) to 0 by -1) {
        try Files.deleteIfExists(all.get(i))
        catch { case _: Throwable => () }
      }
    } catch { case _: Throwable => () }
  }

  private def writeEnv(): java.util.Map[String, Object] = {
    val m = new HashMap[String, Object]()
    m.put("create", "true")
    m
  }

  private def storedEnv(): java.util.Map[String, Object] = {
    val m = writeEnv()
    m.put("compressionMethod", "STORED")
    m
  }

  private def readAll(in: InputStream): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val buf = new Array[Byte](1024)
    var n = in.read(buf)
    while (n != -1) {
      out.write(buf, 0, n)
      n = in.read(buf)
    }
    out.toByteArray()
  }

  private def writeString(p: Path, value: String): Unit = {
    val out = Files.newOutputStream(p, Array.empty[OpenOption]: _*)
    try out.write(value.getBytes("UTF-8"))
    finally out.close()
  }

  @Test def createMissingArchive(): Unit = {
    val jar = tempDir.resolve("brand-new.jar")
    assertFalse("precondition: archive does not exist", Files.exists(jar))

    val uri = jarUri(jar)
    val fs = FileSystems.newFileSystem(uri, writeEnv())
    assertFalse("writable mount → !isReadOnly", fs.isReadOnly())
    fs.close()

    assertTrue("archive exists on disk after close", Files.exists(jar))
    val zf = new ZipFile(jar.toFile)
    try assertEquals(0, zf.size())
    finally zf.close()
  }

  @Test def existingArchiveCleanClose(): Unit = {
    val jar = tempDir.resolve("existing.jar")
    val zos = new ZipOutputStream(new FileOutputStream(jar.toFile))
    try {
      val e = new ZipEntry("a.txt")
      zos.putNextEntry(e)
      zos.write("alpha".getBytes("UTF-8"))
      zos.closeEntry()
      val e2 = new ZipEntry("dir/b.bin")
      zos.putNextEntry(e2)
      zos.write(Array.tabulate[Byte](128)(i => i.toByte))
      zos.closeEntry()
    } finally zos.close()

    val uri = jarUri(jar)
    val env = new HashMap[String, Object]()
    env.put("accessMode", "readWrite")
    val fs = FileSystems.newFileSystem(uri, env)
    try assertFalse("readWrite mount → !isReadOnly", fs.isReadOnly())
    finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      assertEquals(2, zf.size())
      assertNotNull(zf.getEntry("a.txt"))
      assertNotNull(zf.getEntry("dir/b.bin"))
      // a.txt content survives the close-rewrite even though we
      // didn't mutate anything (rewrite was a no-op fast path:
      // `dirty == false`).
      val in = zf.getInputStream(zf.getEntry("a.txt"))
      try {
        val buf = new Array[Byte](32)
        val n = in.read(buf)
        assertEquals("alpha", new String(buf, 0, n, "UTF-8"))
      } finally in.close()
    } finally zf.close()
  }

  @Test def readOnlyMountStaysReadOnly(): Unit = {
    // Empty env matches jdk.zipfs: writable. Read-only is the opt-in
    // accessMode=readOnly, honoured by jdk.zipfs since JDK 23.
    assumeFalse(
      "jdk.zipfs supports accessMode only since JDK 23",
      Platform.executingInJVMOnLowerThanJDK(23)
    )
    val jar = makeJar(tempDir, "ro.jar")
    val env = new HashMap[String, Object]()
    env.put("accessMode", "readOnly")
    val fs = FileSystems.newFileSystem(jarUri(jar), env)
    try assertTrue("accessMode=readOnly mount is read-only", fs.isReadOnly())
    finally fs.close()
  }

  @Test def writeEntryVisibleBeforeCloseAndAfterReopen(): Unit = {
    val jar = tempDir.resolve("write-entry.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/hello.txt")
      val out = Files.newOutputStream(p, Array.empty[OpenOption]: _*)
      try out.write("hello zipfs".getBytes("UTF-8"))
      finally out.close()

      assertTrue(Files.exists(p))
      assertEquals(
        "hello zipfs",
        new String(Files.readAllBytes(p), "UTF-8")
      )

      val ds = Files.newDirectoryStream(fs.getPath("/"))
      try {
        val it = ds.iterator()
        assertTrue("new child visible in directory stream", it.hasNext())
        assertEquals("/hello.txt", it.next().toString)
      } finally ds.close()
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      val e = zf.getEntry("hello.txt")
      assertNotNull(e)
      val in = zf.getInputStream(e)
      try assertEquals("hello zipfs", new String(readAll(in), "UTF-8"))
      finally in.close()
    } finally zf.close()
  }

  @Test def overwriteAppendDeleteOnCloseAndSetTime(): Unit = {
    val jar = tempDir.resolve("mutate-entry.jar")
    val zos = new ZipOutputStream(new FileOutputStream(jar.toFile))
    try {
      val e = new ZipEntry("a.txt")
      zos.putNextEntry(e)
      zos.write("alpha".getBytes("UTF-8"))
      zos.closeEntry()
    } finally zos.close()

    val env = new HashMap[String, Object]()
    env.put("accessMode", "readWrite")
    val fs = FileSystems.newFileSystem(jarUri(jar), env)
    try {
      val p = fs.getPath("/a.txt")
      val out = Files.newOutputStream(
        p,
        Array[OpenOption](
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE
        ): _*
      )
      try out.write("bravo".getBytes("UTF-8"))
      finally out.close()

      val append = Files.newByteChannel(
        p,
        Array[OpenOption](
          StandardOpenOption.APPEND,
          StandardOpenOption.WRITE
        ): _*
      )
      try append.write(java.nio.ByteBuffer.wrap("!".getBytes("UTF-8")))
      finally append.close()

      val stamp = FileTime.fromMillis(1234567890000L)
      Files.setAttribute(
        p,
        "basic:lastModifiedTime",
        stamp,
        Array.empty[LinkOption]: _*
      )
      val attrs = Files.readAttributes(
        p,
        classOf[BasicFileAttributes],
        Array.empty[LinkOption]: _*
      )
      assertEquals(stamp.toMillis(), attrs.lastModifiedTime().toMillis())
      assertEquals("bravo!", new String(Files.readAllBytes(p), "UTF-8"))

      val doomed = fs.getPath("/doomed.txt")
      val ch = Files.newByteChannel(
        doomed,
        Array[OpenOption](
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.DELETE_ON_CLOSE
        ): _*
      )
      ch.close()
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      val e = zf.getEntry("a.txt")
      assertNotNull(e)
      val in = zf.getInputStream(e)
      try assertEquals("bravo!", new String(readAll(in), "UTF-8"))
      finally in.close()
      if (!executingInJVM)
        assertNull(zf.getEntry("doomed.txt"))
    } finally zf.close()
  }

  @Test def createDirectoryDeleteCopyAndMoveEntries(): Unit = {
    val jar = tempDir.resolve("dir-copy-move.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val dir = fs.getPath("/dir")
      Files.createDirectory(dir)
      assertTrue(Files.isDirectory(dir, Array.empty[LinkOption]: _*))

      val file = fs.getPath("/dir/a.txt")
      val out = Files.newOutputStream(file, Array.empty[OpenOption]: _*)
      try out.write("alpha".getBytes("UTF-8"))
      finally out.close()

      try {
        Files.delete(dir)
        fail("expected DirectoryNotEmptyException")
      } catch { case _: DirectoryNotEmptyException => () }

      val copied = fs.getPath("/copy.txt")
      Files.copy(file, copied, Array.empty[CopyOption]: _*)
      assertEquals("alpha", new String(Files.readAllBytes(copied), "UTF-8"))

      val moved = fs.getPath("/moved.txt")
      Files.move(
        copied,
        moved,
        Array[CopyOption](StandardCopyOption.REPLACE_EXISTING): _*
      )
      assertFalse(Files.exists(copied))
      assertEquals("alpha", new String(Files.readAllBytes(moved), "UTF-8"))

      Files.delete(file)
      Files.delete(dir)
      assertFalse(Files.exists(dir))
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      assertNull(zf.getEntry("dir/"))
      assertNull(zf.getEntry("dir/a.txt"))
      val e = zf.getEntry("moved.txt")
      assertNotNull(e)
      val in = zf.getInputStream(e)
      try assertEquals("alpha", new String(readAll(in), "UTF-8"))
      finally in.close()
    } finally zf.close()
  }

  @Test def copyAndMoveAcrossDefaultFsAndZipFs(): Unit = {
    val jar = tempDir.resolve("cross-provider.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    val defaultSource = tempDir.resolve("default-source.txt")
    val defaultMoved = tempDir.resolve("default-moved.txt")
    try {
      writeString(defaultSource, "from default")

      val copiedIntoZip = fs.getPath("/copied-into-zip.txt")
      Files.copy(defaultSource, copiedIntoZip, Array.empty[CopyOption]: _*)
      assertEquals(
        "from default",
        new String(Files.readAllBytes(copiedIntoZip), "UTF-8")
      )

      val movedIntoZip = fs.getPath("/moved-into-zip.txt")
      Files.move(defaultSource, movedIntoZip, Array.empty[CopyOption]: _*)
      assertFalse(Files.exists(defaultSource))
      assertEquals(
        "from default",
        new String(Files.readAllBytes(movedIntoZip), "UTF-8")
      )

      Files.copy(movedIntoZip, defaultMoved, Array.empty[CopyOption]: _*)
      assertEquals(
        "from default",
        new String(Files.readAllBytes(defaultMoved), "UTF-8")
      )
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      assertNotNull(zf.getEntry("copied-into-zip.txt"))
      val e = zf.getEntry("moved-into-zip.txt")
      assertNotNull(e)
      val in = zf.getInputStream(e)
      try assertEquals("from default", new String(readAll(in), "UTF-8"))
      finally in.close()
    } finally zf.close()
  }

  @Test def copyAndMoveReplaceExistingEntries(): Unit = {
    val jar = tempDir.resolve("replace-existing.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val source = fs.getPath("/source.txt")
      val target = fs.getPath("/target.txt")
      writeString(source, "source")
      writeString(target, "target")

      try {
        Files.copy(source, target, Array.empty[CopyOption]: _*)
        fail("expected FileAlreadyExistsException")
      } catch { case _: FileAlreadyExistsException => () }
      assertEquals("target", new String(Files.readAllBytes(target), "UTF-8"))

      Files.copy(
        source,
        target,
        Array[CopyOption](StandardCopyOption.REPLACE_EXISTING): _*
      )
      assertEquals("source", new String(Files.readAllBytes(target), "UTF-8"))

      writeString(source, "moved source")
      Files.move(
        source,
        target,
        Array[CopyOption](StandardCopyOption.REPLACE_EXISTING): _*
      )
      assertFalse(Files.exists(source))
      assertEquals(
        "moved source",
        new String(Files.readAllBytes(target), "UTF-8")
      )
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      assertNull(zf.getEntry("source.txt"))
      val e = zf.getEntry("target.txt")
      assertNotNull(e)
      val in = zf.getInputStream(e)
      try assertEquals("moved source", new String(readAll(in), "UTF-8"))
      finally in.close()
    } finally zf.close()
  }

  @Test def moveDirectoryReparentsChildren(): Unit = {
    assumeFalse(
      "jdk.zipfs does not reparent children for this directory move shape",
      executingInJVM
    )
    val jar = tempDir.resolve("move-dir.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      Files.createDirectory(fs.getPath("/src"))
      val child = fs.getPath("/src/a.txt")
      val out = Files.newOutputStream(child, Array.empty[OpenOption]: _*)
      try out.write("alpha".getBytes("UTF-8"))
      finally out.close()

      Files.move(
        fs.getPath("/src"),
        fs.getPath("/dst"),
        Array.empty[CopyOption]: _*
      )
      assertEquals(
        "alpha",
        new String(Files.readAllBytes(fs.getPath("/dst/a.txt")), "UTF-8")
      )
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      assertNull(zf.getEntry("src/a.txt"))
      val e = zf.getEntry("dst/a.txt")
      assertNotNull(e)
      val in = zf.getInputStream(e)
      try assertEquals("alpha", new String(readAll(in), "UTF-8"))
      finally in.close()
    } finally zf.close()
  }

  @Test def compressionMethodStoredEnv(): Unit = {
    val jar = tempDir.resolve("stored-env.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), storedEnv())
    try {
      val p = fs.getPath("/stored.txt")
      val out = Files.newOutputStream(p, Array.empty[OpenOption]: _*)
      try out.write("stored bytes".getBytes("UTF-8"))
      finally out.close()
    } finally fs.close()

    val zf = new ZipFile(jar.toFile)
    try {
      val e = zf.getEntry("stored.txt")
      assertNotNull(e)
      assertEquals(ZipEntry.STORED, e.getMethod())
      val in = zf.getInputStream(e)
      try assertEquals("stored bytes", new String(readAll(in), "UTF-8"))
      finally in.close()
    } finally zf.close()
  }

  @Test def invalidCompressionMethodEnvRejected(): Unit = {
    val env = writeEnv()
    env.put("compressionMethod", "BZIP2")
    try {
      FileSystems.newFileSystem(jarUri(tempDir.resolve("bad-method.jar")), env)
      fail("expected IllegalArgumentException")
    } catch {
      case _: IllegalArgumentException => ()
    }
  }
}
