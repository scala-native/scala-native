package org.scalanative.testsuite.javalib.util.zip

import java.io.{BufferedOutputStream, FileOutputStream, IOException}
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Paths}
import java.util.Arrays
import java.util.zip.{ZipEntry, ZipException, ZipFile, ZipOutputStream}

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

/* Do not disturb the peace of Tests written when the  Harmony code
 * was ported to Scala Native.
 *
 * Consolidate Test(s) written well after that time in this separate file.
 */

object ZipEntryIssuesTest {

  private var workDirString: String = _

  private val zipTestDataFileName = "zipEntryReadCommentTestData.zip"
  private val zipTestSetDosTimeFileName = "zipEntrySetDosTimeTestData.zip"

  private def makeTestDirs(): String = {
    val orgDir = Files.createTempDirectory("scala-native-testsuite")
    val javalibDir = orgDir.resolve("javalib")
    val testDirRootPath = javalibDir
      .resolve("java")
      .resolve("util")
      .resolve("zip")
      .resolve("ZipEntriesIssuesTest")

    val testDirSrcPath = testDirRootPath.resolve("src")
    val testDirDstPath = testDirRootPath.resolve("dst")

    Files.createDirectories(testDirRootPath)
    Files.createDirectory(testDirSrcPath)
    Files.createDirectory(testDirDstPath)

    testDirRootPath.toString()
  }

  private def createZipFile(
      location: String,
      entryNames: Array[String]
  ): Unit = {
    val zipOut = new ZipOutputStream(
      new BufferedOutputStream(new FileOutputStream(location))
    )
    try {
      zipOut.setComment("Some interesting moons of Saturn.")

      Arrays
        .stream(entryNames)
        .forEach(e => zipOut.putNextEntry(new ZipEntry(e)))

    } finally {
      zipOut.close()
    }
  }

  private def provisionZipEntrySetDosTimeTestData(zosTestDir: String): Unit = {
    // In JVM, cwd is set to unit-tests/jvm/[scala-version]
    val inputRootDir =
      if (Platform.executingInJVM) "../.."
      else "unit-tests"

    val outputFileQualifiedName =
      s"${zosTestDir}/src/${zipTestSetDosTimeFileName}"

    val entryNames = Array(
      "Rhea_1",
      "Prometheus_2",
      "Phoebe_3",
      "Tethys_4",
      "Iapetus_5"
    )

    createZipFile(outputFileQualifiedName, entryNames)
  }

  private def provisionZipEntryIssuesTestData(zeTestDir: String): Unit = {
    // In JVM, cwd is set to unit-tests/jvm/[scala-version]
    val inputRootDir =
      if (Platform.executingInJVM) "../.."
      else "unit-tests"

    val inputSubDirs =
      s"shared/src/test/resources/testsuite/javalib/java/util/zip/"

    val inputDir = s"${inputRootDir}/${inputSubDirs}"

    val inputFileName = s"${inputDir}/${zipTestDataFileName}"

    val outputFileName = s"${zeTestDir}/src/${zipTestDataFileName}"

    Files.copy(Paths.get(inputFileName), Paths.get(outputFileName))
  }

  @BeforeClass
  def beforeClass(): Unit = {
    workDirString = makeTestDirs()
    provisionZipEntryIssuesTestData(workDirString)
    provisionZipEntrySetDosTimeTestData(workDirString)
  }
}

class ZipEntryIssuesTest {
  import ZipEntryIssuesTest._

  // Issue 3755
  @Test def readEntryComment(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestDataFileName}"

    val zf = new ZipFile(srcName)
    try {
      val entryName = "LoremIpsum.utf-8"
      val ze = zf.getEntry(entryName)
      assertNotNull("zipEntry '${entryName}' not found", ze)

      // How do we know? Manual "zip -l" exam of src .zip told us. Who told it?
      val expected = "Better days are coming"

      val comment = ze.getComment()

      assertNotNull("zipEntry comment '${entryName}' not found", comment)

      assertEquals("Entry comment", expected, comment)
    } finally {
      zf.close()
    }
  }

  // Issue 3816 - stress the setTime/getTime path that previously
  // segfaulted on macOS due to uninitialised stackalloc'd `tm`
  // (mktime reads tm_gmtoff/tm_zone/tm_wday past the fields we set).
  // Run a tight loop in case the segfault is intermittent.
  @Test def setTimeGetTimeStressLoop(): Unit = {
    // Y2K UTC; deterministic round-trip independent of local tz, since
    // setTime(localtime) and getTime(mktime) cancel local offset.
    val expectedMillis = 946684800000L
    val ze = new ZipEntry("stress")
    var i = 0
    while (i < 100) {
      ze.setTime(expectedMillis)
      assertEquals("getTime round-trip", expectedMillis, ze.getTime())
      i += 1
    }
  }

  // Regression: msDosYears == 0 (year 1980, the MS-DOS epoch) is a
  // valid encoding; the previous guard `<= 0` collapsed every 1980
  // date to 1980-01-01 00:00.
  @Test def setTimeMidYear1980RoundTrip(): Unit = {
    // 1980-06-15 00:00:00 UTC — seconds-mod-2 == 0 so MS-DOS's 2-second
    // granularity is lossless; mid-year so local-tz offset can't push
    // the date out of 1980 in any sane zone.
    val mid1980Millis = 329875200000L
    val ze = new ZipEntry("y1980")
    ze.setTime(mid1980Millis)
    assertEquals("getTime round-trip", mid1980Millis, ze.getTime())
  }

  // Regression: timestamps past 2038-01-19 are within the DOS date
  // range (1980..2107) but exceeded `__time32_t` on Windows. Linux/macOS
  // already use 64-bit time_t; this guards against a future regression
  // and exercises the same code path that the Windows 64-bit binding
  // depends on.
  @Test def setTimePost2038RoundTrip(): Unit = {
    // 3_000_000_000 seconds since epoch ≈ 2065-01-24 UTC
    val post2038Millis = 3000000000000L
    val ze = new ZipEntry("post2038")
    ze.setTime(post2038Millis)
    assertEquals("getTime round-trip", post2038Millis, ze.getTime())
  }

  // Issue 3788 — round-trip the Java 8 FileTime triple through a
  // ZipOutputStream → ZipFile cycle. Without UT/NTFS extra fields,
  // ZIP archives only carry the MS-DOS modification time (2-second
  // granularity, no atime/ctime, no millisecond fraction).
  @Test def fileTimeRoundTripThroughExtraField(): Unit = {
    val zipPath = Files.createTempFile("scala-native-filetime", ".zip")
    try {
      // Picks values past the 2038 signed-int32 ceiling to exercise
      // the unsigned-int32 UT seconds encoding (which extends the
      // range to 2106-02-07). 3e12 ms ≈ 2065-01-24 UTC.
      val mtime = FileTime.fromMillis(3000000000000L)
      val atime = FileTime.fromMillis(3100000000000L)
      val ctime = FileTime.fromMillis(2900000000000L)

      val zos = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(zipPath.toFile))
      )
      try {
        val entry = new ZipEntry("with-times.bin")
        entry.setLastModifiedTime(mtime)
        entry.setLastAccessTime(atime)
        entry.setCreationTime(ctime)
        zos.putNextEntry(entry)
        zos.write(Array[Byte](1, 2, 3))
        zos.closeEntry()

        val plain = new ZipEntry("no-times.bin")
        plain.setTime(946684800000L) // Y2K UTC; MS-DOS mtime only
        zos.putNextEntry(plain)
        zos.write(Array[Byte](4, 5, 6))
        zos.closeEntry()
      } finally {
        zos.close()
      }

      val zf = new ZipFile(zipPath.toFile)
      try {
        val ze = zf.getEntry("with-times.bin")
        assertNotNull("with-times.bin not found", ze)
        assertEquals(
          "mtime millis",
          mtime.toMillis(),
          ze.getLastModifiedTime().toMillis()
        )
        // getTime() must also see the high-precision mtime, not just
        // the MS-DOS-truncated copy.
        assertEquals("getTime via mtime", mtime.toMillis(), ze.getTime())
        // CDH UT only carries mtime per APPNOTE 4.5.7, so atime/ctime
        // are lost when reading from the central directory. JDK
        // additionally emits an NTFS (0x000A) block that carries all
        // three; assert spec-conformant Native behaviour only.
        if (!Platform.executingInJVM) {
          assertNull("atime should be unset from CDH", ze.getLastAccessTime())
          assertNull("ctime should be unset from CDH", ze.getCreationTime())
        }

        val plainZe = zf.getEntry("no-times.bin")
        assertNotNull("no-times.bin not found", plainZe)
        // Falls back to MS-DOS mtime (2-second granularity).
        assertEquals(
          "MS-DOS mtime fallback",
          946684800000L,
          plainZe.getLastModifiedTime().toMillis()
        )
        assertNull("no UT atime", plainZe.getLastAccessTime())
        assertNull("no UT ctime", plainZe.getCreationTime())
      } finally {
        zf.close()
      }
    } finally {
      Files.deleteIfExists(zipPath)
    }
  }

  // Issue 3788 — setTime() must clear the high-precision FileTime
  // mtime, so a subsequent getLastModifiedTime() / getTime() reflects
  // the legacy call. Otherwise FileTime / legacy callers see stale
  // values when interleaved.
  @Test def setTimeClearsFileTimeMtime(): Unit = {
    val fileTime = FileTime.fromMillis(1700000000000L)
    val legacy = 946684800000L // Y2K UTC; MS-DOS 2-sec granular

    val ze = new ZipEntry("e")
    ze.setLastModifiedTime(fileTime)
    assertEquals("getTime sees mtime", fileTime.toMillis(), ze.getTime())

    ze.setTime(legacy)
    assertEquals("setTime overrides getTime", legacy, ze.getTime())
    // getLastModifiedTime falls back to getTime when mtime is unset,
    // so the FileTime accessor must reflect the latest write.
    assertEquals(
      "getLastModifiedTime after setTime",
      legacy,
      ze.getLastModifiedTime().toMillis()
    )
  }

  // Issue 3788 — re-writing an entry that already carries a UT block
  // must replace it, not append a second one. A duplicate-tag block
  // is ambiguous for other tools.
  @Test def noDuplicateUTOnReWrite(): Unit = {
    val srcPath = Files.createTempFile("scala-native-ut-src", ".zip")
    val dstPath = Files.createTempFile("scala-native-ut-dst", ".zip")
    try {
      // Write a source archive carrying a UT block.
      val zos1 = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(srcPath.toFile))
      )
      try {
        val e = new ZipEntry("x")
        e.setLastModifiedTime(FileTime.fromMillis(1700000000000L))
        zos1.putNextEntry(e)
        zos1.write(Array[Byte](1))
        zos1.closeEntry()
      } finally {
        zos1.close()
      }

      // Read it; copy into a fresh archive (carries the entry's
      // existing `extra`, then ZOS appends its own UT block).
      val srcZf = new ZipFile(srcPath.toFile)
      val srcEntry =
        try srcZf.getEntry("x")
        finally srcZf.close()

      val zos2 = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(dstPath.toFile))
      )
      try {
        zos2.putNextEntry(new ZipEntry(srcEntry))
        zos2.write(Array[Byte](1))
        zos2.closeEntry()
      } finally {
        zos2.close()
      }

      val dstZf = new ZipFile(dstPath.toFile)
      try {
        val dstEntry = dstZf.getEntry("x")
        val extra = dstEntry.getExtra()
        // Count 0x5455 (UT) blocks in `extra`. Must be exactly one.
        var p = 0
        var utCount = 0
        while (extra != null && p + 4 <= extra.length) {
          val tag = (extra(p) & 0xff) | ((extra(p + 1) & 0xff) << 8)
          val size = (extra(p + 2) & 0xff) | ((extra(p + 3) & 0xff) << 8)
          if (tag == 0x5455) utCount += 1
          p += 4 + size
        }
        assertEquals("exactly one UT block after copy", 1, utCount)
      } finally {
        dstZf.close()
      }
    } finally {
      Files.deleteIfExists(srcPath)
      Files.deleteIfExists(dstPath)
    }
  }

  // Issue 3788 — `extra` length is a 16-bit field. A user-supplied
  // ~64 KiB extra plus a UT block must NOT silently truncate the
  // length and leave the on-disk bytes longer than the declared
  // size. Native rejects with ZipException; JDK's ZipOutputStream
  // has its own (ZIP64-flavoured) handling, so this is Native-only.
  @Test def overlongExtraWithUTRejected(): Unit = {
    if (Platform.executingInJVM) return
    val zipPath = Files.createTempFile("scala-native-ut-overflow", ".zip")
    try {
      val zos = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(zipPath.toFile))
      )
      try {
        val e = new ZipEntry("oversize")
        // 65535-byte placeholder extra (tag 0xCAFE) consumes the
        // entire 16-bit field budget on its own.
        val extra = new Array[Byte](0xffff)
        extra(0) = 0xfe.toByte; extra(1) = 0xca.toByte
        val payloadSize = 0xffff - 4
        extra(2) = (payloadSize & 0xff).toByte
        extra(3) = ((payloadSize >> 8) & 0xff).toByte
        e.setExtra(extra)
        e.setLastModifiedTime(FileTime.fromMillis(1700000000000L))
        assertThrows(classOf[ZipException], zos.putNextEntry(e))
      } finally {
        zos.close()
      }
    } finally {
      Files.deleteIfExists(zipPath)
    }
  }

  // Issue 3788 — copying an entry whose extras already contain UT or
  // NTFS records and whose FileTimes are outside the UT-encodable
  // range: the writer cannot emit a fresh UT block, but the stale
  // ones must be stripped so they don't override the freshly-set
  // MS-DOS time on readback.
  @Test def staleTimestampBlocksStrippedWhenNoReplacement(): Unit = {
    val zipPath = Files.createTempFile("scala-native-ut-stale", ".zip")
    try {
      val zos = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(zipPath.toFile))
      )
      try {
        val e = new ZipEntry("e")
        // Hand-craft a UT block with mtime = 1_700_000_000 secs
        // (2023-11-14); will be parsed into e.mtime when setExtra
        // runs. Then bump mtime to Y2K-2107 boundary out of UT range
        // (negative seconds via a pre-1970 millis) so buildUTExtraBlock
        // returns null and we exercise the strip-without-replacement
        // path.
        val utBytes = Array[Byte](
          0x55.toByte,
          0x54.toByte, // tag UT
          5.toByte,
          0.toByte, // size = 5
          0x1.toByte, // flags: mtime only
          0x00.toByte,
          0x14.toByte,
          0x55.toByte,
          0x65.toByte // ≈1_700_000_000
        )
        e.setExtra(utBytes)
        // Force out-of-UT-range mtime: pre-1970 → negative epoch secs
        // fails fitsInUInt32Seconds, so no replacement UT is emitted.
        e.setLastModifiedTime(FileTime.fromMillis(-1000000000L))
        zos.putNextEntry(e)
        zos.write(Array[Byte](1))
        zos.closeEntry()
      } finally {
        zos.close()
      }

      val zf = new ZipFile(zipPath.toFile)
      try {
        val ze = zf.getEntry("e")
        // The stale UT (2023-11-14) must not show up. With no UT in
        // the resulting archive, getLastModifiedTime falls back to
        // the MS-DOS time (set by setLastModifiedTime → setTime).
        assertTrue(
          "stale UT mtime leaked through",
          ze.getLastModifiedTime().toMillis() != 1700000000000L
        )
      } finally {
        zf.close()
      }
    } finally {
      Files.deleteIfExists(zipPath)
    }
  }

  // Issue 3788 — `setExtra(utBytes)` must parse the bytes and
  // populate getLastModifiedTime etc., matching JDK behaviour.
  @Test def setExtraParsesUTTimestamps(): Unit = {
    val ze = new ZipEntry("e")
    val mtimeSecs = 1700000000L
    val utBytes = Array[Byte](
      0x55.toByte,
      0x54.toByte, // tag UT (0x5455 LE)
      5.toByte,
      0.toByte, // size = 5
      0x1.toByte, // flags: mtime only
      (mtimeSecs & 0xff).toByte,
      ((mtimeSecs >> 8) & 0xff).toByte,
      ((mtimeSecs >> 16) & 0xff).toByte,
      ((mtimeSecs >> 24) & 0xff).toByte
    )
    ze.setExtra(utBytes)
    val ft = ze.getLastModifiedTime()
    assertNotNull("setExtra should populate mtime", ft)
    assertEquals(
      "setExtra parsed mtime",
      mtimeSecs * 1000L,
      ft.toMillis()
    )
  }

  // Issue 3788 — `setExtra` must clear any previously-parsed
  // FileTimes before re-parsing. Without this, `setExtra(utBytes)`
  // followed by `setExtra(null)` would leave stale mtime visible
  // via getLastModifiedTime. OpenJDK retains stale state here;
  // Native is stricter, so this is Native-only.
  @Test def setExtraClearsStaleFileTimes(): Unit = {
    if (Platform.executingInJVM) return
    val ze = new ZipEntry("e")
    val utBytes = Array[Byte](
      0x55.toByte,
      0x54.toByte, // tag UT
      5.toByte,
      0.toByte, // size = 5
      0x1.toByte, // flags: mtime
      0x00.toByte,
      0x14.toByte,
      0x55.toByte,
      0x65.toByte // 1_700_000_000
    )
    ze.setExtra(utBytes)
    assertNotNull("populated by first setExtra", ze.getLastModifiedTime())

    ze.setExtra(null)
    // After clearing the extra block, the high-precision mtime must
    // be gone. getLastModifiedTime falls back to getTime/MS-DOS, and
    // since no setTime was called either, that returns -1 → null.
    assertNull("stale mtime after setExtra(null)", ze.getLastModifiedTime())
    assertNull("stale atime", ze.getLastAccessTime())
    assertNull("stale ctime", ze.getCreationTime())
  }

  // Issue 3788 — clearing parsed FileTimes during setExtra must not
  // discard values explicitly supplied through the FileTime setters.
  @Test def setExtraPreservesExplicitFileTimes(): Unit = {
    if (Platform.executingInJVM) return
    val ze = new ZipEntry("e")
    val mtime = FileTime.fromMillis(1700000000000L)
    val atime = FileTime.fromMillis(1700000100000L)
    val ctime = FileTime.fromMillis(1700000200000L)

    ze.setLastModifiedTime(mtime)
    ze.setLastAccessTime(atime)
    ze.setCreationTime(ctime)

    // Non-timestamp extra block: tag 0x1234, size 0.
    ze.setExtra(Array[Byte](0x34.toByte, 0x12.toByte, 0.toByte, 0.toByte))

    assertEquals("explicit mtime", mtime, ze.getLastModifiedTime())
    assertEquals("explicit atime", atime, ze.getLastAccessTime())
    assertEquals("explicit ctime", ctime, ze.getCreationTime())
  }

  // Issue 3788 — getTime() now prefers parsed mtime, so ZOS must not
  // use getTime() to decide whether the raw MS-DOS time/date fields
  // have been initialized.
  @Test def putNextEntryInitializesDosTimeWithOnlyExtraMtime(): Unit = {
    if (Platform.executingInJVM) return
    val zipPath = Files.createTempFile("scala-native-ut-dos", ".zip")
    try {
      val mtimeSecs = 1700000000L
      val utBytes = Array[Byte](
        0x55.toByte,
        0x54.toByte, // tag UT
        5.toByte,
        0.toByte, // size = 5
        0x1.toByte, // flags: mtime
        (mtimeSecs & 0xff).toByte,
        ((mtimeSecs >> 8) & 0xff).toByte,
        ((mtimeSecs >> 16) & 0xff).toByte,
        ((mtimeSecs >> 24) & 0xff).toByte
      )

      val zos = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(zipPath.toFile))
      )
      try {
        val e = new ZipEntry("x")
        e.setExtra(utBytes)
        zos.putNextEntry(e)
        zos.write(Array[Byte](1))
        zos.closeEntry()
      } finally {
        zos.close()
      }

      val bytes = Files.readAllBytes(zipPath)
      val dosTime = (bytes(10) & 0xff) | ((bytes(11) & 0xff) << 8)
      val dosDate = (bytes(12) & 0xff) | ((bytes(13) & 0xff) << 8)
      assertNotEquals("raw DOS time must be initialized", 0xffff, dosTime)
      assertNotEquals("raw DOS date must be initialized", 0xffff, dosDate)
    } finally {
      Files.deleteIfExists(zipPath)
    }
  }

  // Issue 3788 — overlong-extra rejection must happen BEFORE any
  // LFH bytes hit the stream. Otherwise a thrown ZipException leaves
  // a corrupt half-LFH in the file plus a stale name in `entries`.
  @Test def putNextEntryAtomicOnOverlongExtra(): Unit = {
    if (Platform.executingInJVM) return
    val zipPath = Files.createTempFile("scala-native-atomic", ".zip")
    try {
      val zos = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(zipPath.toFile))
      )
      try {
        // First, an oversize entry that should be rejected cleanly.
        val bad = new ZipEntry("bad")
        val extra = new Array[Byte](0xffff)
        extra(0) = 0xfe.toByte; extra(1) = 0xca.toByte
        val payloadSize = 0xffff - 4
        extra(2) = (payloadSize & 0xff).toByte
        extra(3) = ((payloadSize >> 8) & 0xff).toByte
        bad.setExtra(extra)
        bad.setLastModifiedTime(FileTime.fromMillis(1700000000000L))
        assertThrows(classOf[ZipException], zos.putNextEntry(bad))

        // Now a normal entry — should succeed because the failed
        // putNextEntry did not commit any state, leaving the stream
        // and the `entries` set in their pre-call shape.
        val good = new ZipEntry("good")
        zos.putNextEntry(good)
        zos.write(Array[Byte](1, 2, 3))
        zos.closeEntry()

        // And re-use the name "bad" with a small extra — would fail
        // if the prior rejected call had still recorded "bad" in
        // `entries` (it would throw "Entry already exists").
        val bad2 = new ZipEntry("bad")
        zos.putNextEntry(bad2)
        zos.write(Array[Byte](4))
        zos.closeEntry()
      } finally {
        zos.close()
      }

      // Round-trip read to confirm the archive is valid.
      val zf = new ZipFile(zipPath.toFile)
      try {
        assertNotNull("good entry present", zf.getEntry("good"))
        assertNotNull("bad-name reuse present", zf.getEntry("bad"))
      } finally {
        zf.close()
      }
    } finally {
      Files.deleteIfExists(zipPath)
    }
  }

  // Issue 3787
  @Test def setEntryDosTime(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestSetDosTimeFileName}"

    val dstName =
      s"${workDirString}/dst/CopyOf_${zipTestSetDosTimeFileName}"

    /*  expectedMillis generated using JVM:
     *  val y2k = Instant.parse("2000-01-01T00:00:00.00Z").toEpochMilli
     *  val y2k: Long = 946684800000
     */

    val changeEntry = "Tethys_4"

    val expectedMillis = 946684800000L

    val zf = new ZipFile(srcName)
    try {
      val zipOut = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(dstName))
      )

      try {
        zf.stream()
          .limit(99)
          .forEach(e => {
            zipOut.putNextEntry(e)

            if (!e.isDirectory()) {
              val fis = zf.getInputStream(e)
              val buf = new Array[Byte](2 * 1024)

              try {
                var nRead = 0
                // Poor but useful idioms creep in: porting from Java style
                while ({ nRead = fis.read(buf); nRead } > 0) {
                  zipOut.write(buf, 0, nRead)
                  assertEquals("fis nRead", e.getSize(), nRead)
                }
              } finally {
                fis.close()
              }
            }
            // make a change to modification time which should be noticable.
            if (e.getName() == changeEntry) {
              e.setTime(expectedMillis)
              e.setComment(
                "ms-dos modtime should be Year 2000 UTC, " +
                  s"local to where file was written."
              )
            }
            zipOut.closeEntry()
          })

      } finally {
        zipOut.close()
      }

    } finally {
      zf.close()
    }

    /* Re-read to see if getTime() returns the expected value.
     * If not, manual visual inspection of the output file will distinguish
     * if the change was durable or if getTime() mangled reading it.
     */

    val zfDst = new ZipFile(dstName)
    try {
      val ze = zfDst.getEntry(changeEntry)
      assertNotNull("zipEntry '${changeEntry}' not found", ze)
      assertEquals("getTime()", expectedMillis, ze.getTime())
    } finally {
      zfDst.close()
    }
  }
}
