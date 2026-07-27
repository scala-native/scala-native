package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.io.{
  ByteArrayOutputStream, FileOutputStream, InputStream, OutputStream
}
import java.nio.charset.StandardCharsets
import java.nio.file.attribute.FileTime
import java.nio.file.{FileSystems, Files, OpenOption, Path}
import java.util.HashMap
import java.util.zip.{CRC32, ZipEntry, ZipFile, ZipOutputStream}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{After, Before, Test}

import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVM

/* Acceptance gate for ZipFS-produced archives. Native ZipFS writes
 * the archive; java.util.zip.ZipFile and system unzip then validate that the
 * central directory, compressed payloads, CRCs, and extraction paths are
 * consumable by independent readers.
 */
class ZipWriteConformanceTest {
  import ZipFileSystemTest._
  import ZipWriteConformanceTest._

  private var tempDir: Path = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-write-conformance")
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
    m.put("accessMode", "readWrite")
    m
  }

  private def seedArchive(jar: Path): Map[String, Array[Byte]] = {
    val storedBytes = Array.tabulate[Byte](256)(i => (255 - i).toByte)
    val zos = new ZipOutputStream(new FileOutputStream(jar.toFile))
    try {
      val e = new ZipEntry("stored.bin")
      e.setMethod(ZipEntry.STORED)
      e.setSize(storedBytes.length.toLong)
      e.setCompressedSize(storedBytes.length.toLong)
      val crc = new CRC32()
      crc.update(storedBytes)
      e.setCrc(crc.getValue())
      zos.putNextEntry(e)
      zos.write(storedBytes)
      zos.closeEntry()
    } finally zos.close()
    Map("stored.bin" -> storedBytes)
  }

  private def buildZipFsArchive(jar: Path): Map[String, Array[Byte]] = {
    val seeded = seedArchive(jar)
    val deflatedBytes = {
      val sb = new StringBuilder
      var i = 0
      while (i < 2048) {
        sb.append("zipfs deflated payload ")
        i += 1
      }
      sb.toString.getBytes(StandardCharsets.UTF_8)
    }
    val nestedBytes = "nested\n".getBytes(StandardCharsets.UTF_8)
    val nonAsciiBytes = "münzen\n".getBytes(StandardCharsets.UTF_8)
    val added = Map(
      "deflated.txt" -> deflatedBytes,
      "dir/nested.txt" -> nestedBytes,
      "münzen.txt" -> nonAsciiBytes
    )

    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      added.foreach {
        case (name, bytes) =>
          val path = fs.getPath("/" + name)
          val out = Files.newOutputStream(path, Array.empty[OpenOption]: _*)
          try out.write(bytes)
          finally out.close()
      }
      Files.setLastModifiedTime(
        fs.getPath("/deflated.txt"),
        FileTime.fromMillis(1_500_000_000_000L)
      )
    } finally fs.close()

    seeded ++ added
  }

  @Test def nativeZipFsArchiveReadableByZipFile(): Unit = {
    assumeFalse("validates Native ZipFS writer output", executingInJVM)
    val jar = tempDir.resolve("zipfs-conformance.jar")
    val expected = buildZipFsArchive(jar)

    val zf = new ZipFile(jar.toFile)
    try {
      expected.foreach {
        case (name, bytes) =>
          val e = zf.getEntry(name)
          assertNotNull(s"missing entry $name", e)
          val in = zf.getInputStream(e)
          try
            assertArrayEquals(s"payload mismatch for $name", bytes, readAll(in))
          finally in.close()
      }
      assertEquals(ZipEntry.STORED, zf.getEntry("stored.bin").getMethod())
      assertEquals(ZipEntry.DEFLATED, zf.getEntry("deflated.txt").getMethod())
    } finally zf.close()
  }

  @Test def nativeZipFsArchiveAcceptedByUnzip(): Unit = {
    assumeFalse("validates Native ZipFS writer output", executingInJVM)
    assumeUnzipUsable()
    val jar = tempDir.resolve("zipfs-unzip-conformance.jar")
    val expected = buildZipFsArchive(jar)
    val jarPath = jar.toAbsolutePath().toString

    val (listCode, listOut, listErr) = runUnzip(Array("-l", jarPath))
    assertEquals(s"unzip -l rc=$listCode stderr=$listErr", 0, listCode)
    assertTrue(listOut.contains("stored.bin"))
    assertTrue(listOut.contains("deflated.txt"))
    assertTrue(listOut.contains("dir/nested.txt"))

    val (testCode, testOut, testErr) = runUnzip(Array("-t", jarPath))
    assertEquals(
      s"unzip -t rc=$testCode\nstdout=$testOut\nstderr=$testErr",
      0,
      testCode
    )
    assertTrue(
      s"unzip -t did not report all entries OK:\n$testOut",
      testOut.contains("No errors detected")
    )

    expected.filter { case (name, _) => name.forall(_ < 0x80) }.foreach {
      case (name, bytes) =>
        val (code, outBytes, err) = runUnzipRaw(Array("-p", jarPath, name))
        assertEquals(s"unzip -p $name rc=$code err=$err", 0, code)
        assertArrayEquals(s"unzip -p $name payload mismatch", bytes, outBytes)
    }
  }
}

object ZipWriteConformanceTest {

  private def assumeUnzipUsable(): Unit = {
    assumeFalse("unzip exec not exercised on Windows", Platform.isWindows)
    assumeTrue("system `unzip` not on PATH", unzipAvailable)
  }

  private lazy val unzipAvailable: Boolean = {
    try {
      val pb = new ProcessBuilder("unzip", "-v")
      pb.redirectErrorStream(true)
      val proc = pb.start()
      proc.getInputStream.close()
      proc.waitFor() == 0
    } catch {
      case _: Throwable => false
    }
  }

  private def readAll(in: InputStream): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val buf = new Array[Byte](4096)
    var n = in.read(buf)
    while (n != -1) {
      out.write(buf, 0, n)
      n = in.read(buf)
    }
    out.toByteArray()
  }

  private def runUnzip(args: Array[String]): (Int, String, String) = {
    val (rc, out, err) = runUnzipRaw(args)
    (rc, new String(out, StandardCharsets.UTF_8), err)
  }

  private def runUnzipRaw(args: Array[String]): (Int, Array[Byte], String) = {
    val cmd = "unzip" +: args.toIndexedSeq
    val pb = new ProcessBuilder(cmd: _*)
    val proc = pb.start()
    val errBuf = new ByteArrayOutputStream()
    val errPump = new Thread(new Runnable {
      override def run(): Unit = {
        val err = proc.getErrorStream
        val b = new Array[Byte](4096)
        var n = err.read(b)
        while (n != -1) {
          errBuf.write(b, 0, n)
          n = err.read(b)
        }
        err.close()
      }
    })
    errPump.start()
    val out = readAll(proc.getInputStream)
    errPump.join()
    val rc = proc.waitFor()
    (rc, out, new String(errBuf.toByteArray, StandardCharsets.UTF_8))
  }
}
