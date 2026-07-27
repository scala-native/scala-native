package org.scalanative.testsuite.javalib.util.zip

import java.io.{
  BufferedOutputStream, ByteArrayOutputStream, File, FileOutputStream,
  InputStream
}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.{Deflater, ZipEntry, ZipOutputStream}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

/* Slice 8.0b acceptance gate: produce a fixture archive via our own
 * ZipOutputStream and feed it to the system `unzip` to validate
 * central-directory readability (`-l`), full integrity (`-t`), and
 * per-entry extraction (`-p`). Skipped when `unzip` is unavailable
 * (CI containers without it, Windows).
 */
class ZipOutputStreamConformanceTest {
  import ZipOutputStreamConformanceTest._

  private def buildFixture(target: File): Map[String, Array[Byte]] = {
    val ascii = "ascii.txt" -> "hello world\n".getBytes(StandardCharsets.UTF_8)
    val nested =
      "dir/sub/nested.bin" -> Array.tabulate[Byte](512)(i => i.toByte)
    val nonAscii = "münzen.txt" -> "Üäß€\n".getBytes(StandardCharsets.UTF_8)
    // Larger payload so the DEFLATED path actually compresses meaningfully.
    val biggerPayload = {
      val sb = new StringBuilder
      var i = 0
      while (i < 4096) {
        sb.append("the quick brown fox jumps over the lazy dog ")
        i += 1
      }
      sb.toString.getBytes(StandardCharsets.UTF_8)
    }
    val deflated = "deflated.txt" -> biggerPayload
    val stored = "stored.bin" -> Array.tabulate[Byte](256)(_.toByte)

    val entries = Seq(ascii, nested, nonAscii, deflated, stored)

    val zos = new ZipOutputStream(
      new BufferedOutputStream(new FileOutputStream(target))
    )
    try {
      zos.setLevel(Deflater.BEST_SPEED)
      entries.foreach {
        case (name, bytes) =>
          val e = new ZipEntry(name)
          if (name == "stored.bin") {
            e.setMethod(ZipEntry.STORED)
            e.setSize(bytes.length.toLong)
            e.setCompressedSize(bytes.length.toLong)
            val crc = new java.util.zip.CRC32()
            crc.update(bytes)
            e.setCrc(crc.getValue)
          } else {
            e.setMethod(ZipEntry.DEFLATED)
          }
          if (name == "ascii.txt") {
            e.setLastModifiedTime(FileTime.fromMillis(1_500_000_000_000L))
          }
          zos.putNextEntry(e)
          zos.write(bytes)
          zos.closeEntry()
      }
    } finally zos.close()

    entries.toMap
  }

  private def assumeUnzipUsable(): Unit = {
    assumeFalse("unzip exec not exercised on Windows", Platform.isWindows)
    assumeTrue("system `unzip` not on PATH", unzipAvailable)
  }

  @Test def unzipListsArchive(): Unit = {
    assumeUnzipUsable()
    val tmp = Files.createTempFile("zos-conformance", ".zip").toFile
    try {
      buildFixture(tmp)
      val (code, out, err) = runUnzip(Array("-l", tmp.getAbsolutePath))
      assertEquals(s"unzip -l rc=$code stderr=$err", 0, code)
      assertTrue(
        s"missing entry in -l output:\n$out",
        out.contains("ascii.txt")
      )
      assertTrue(
        s"missing nested entry:\n$out",
        out.contains("dir/sub/nested.bin")
      )
    } finally tmp.delete()
  }

  @Test def unzipTestsIntegrity(): Unit = {
    assumeUnzipUsable()
    val tmp = Files.createTempFile("zos-conformance", ".zip").toFile
    try {
      buildFixture(tmp)
      val (code, out, err) = runUnzip(Array("-t", tmp.getAbsolutePath))
      assertEquals(
        s"unzip -t rc=$code\nstdout=$out\nstderr=$err",
        0,
        code
      )
      assertTrue(
        s"unzip -t did not report all entries OK:\n$out",
        out.contains("No errors detected")
      )
    } finally tmp.delete()
  }

  @Test def unzipExtractsEachEntry(): Unit = {
    assumeUnzipUsable()
    val tmp = Files.createTempFile("zos-conformance", ".zip").toFile
    try {
      val expected = buildFixture(tmp)
      // Info-ZIP `unzip 6.0` mangles non-ASCII filenames when matching
      // arguments on the command line (its UTF-8 support is partial). The
      // UTF-8 entry is still exercised by `unzip -l` / `unzip -t` above,
      // which validate central-directory + payload integrity without
      // needing argument-side name matching. Skip non-ASCII here.
      val isAscii: String => Boolean = _.forall(_ < 0x80)
      expected.filter { case (n, _) => isAscii(n) }.foreach {
        case (name, bytes) =>
          val (code, outBytes, err) =
            runUnzipRaw(Array("-p", tmp.getAbsolutePath, name))
          assertEquals(s"unzip -p $name rc=$code err=$err", 0, code)
          assertArrayEquals(s"unzip -p $name payload mismatch", bytes, outBytes)
      }
    } finally tmp.delete()
  }
}

object ZipOutputStreamConformanceTest {

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

  private def drain(in: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val buf = new Array[Byte](4096)
    var n = in.read(buf)
    while (n != -1) {
      baos.write(buf, 0, n)
      n = in.read(buf)
    }
    in.close()
    baos.toByteArray
  }

  private def runUnzip(args: Array[String]): (Int, String, String) = {
    val (rc, out, err) = runUnzipRaw(args)
    (rc, new String(out, StandardCharsets.UTF_8), err)
  }

  private def runUnzipRaw(args: Array[String]): (Int, Array[Byte], String) = {
    val cmd = "unzip" +: args.toIndexedSeq
    val pb = new ProcessBuilder(cmd: _*)
    val proc = pb.start()
    // stderr drained on a thread so a large stdout doesn't deadlock against
    // a small pipe buffer.
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
    val out = drain(proc.getInputStream)
    errPump.join()
    val rc = proc.waitFor()
    (rc, out, new String(errBuf.toByteArray, StandardCharsets.UTF_8))
  }
}
