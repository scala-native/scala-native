package org.scalanative.testsuite.javalib.nio.file

/* FilesTest.scala contains a number of tests for Files.copy().
 * This file contains additional Tests developed in the course of
 * investigating Scala Native Issue #4382.
 *
 * It aims to improve the SN 0.5.8 situation but does _not_ give complete
 * test coverage of the three Files.copy() methods.  It provides a place
 * for Tests as more extensive coverage is added.
 * 
 * It is hard to set or read the process environment umask in CI.
 * Several advanced Tests are provided and marked @Ignore. These
 * are intended to be run manually for development and maintenance
 * where the process umask can be set manually before running the
 * test environment.
 */

/* General requirement for Continuous Integration (CI) environment:
 *   umask in the execution environment is of the form 0xx. That is, no
 *   user bits are masked off.
 */

import java.io.ByteArrayInputStream
import java.io.{BufferedInputStream, BufferedOutputStream}
import java.io.{DataInputStream, DataOutputStream}

import java.nio.charset.StandardCharsets

import java.nio.file.{Files, Path, StandardCopyOption}

import java.nio.file.attribute.{
  PosixFileAttributes,
  PosixFilePermission,
  PosixFilePermissions
}

import java.util.SplittableRandom

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.BeforeClass
import org.junit.Ignore

import org.scalanative.testsuite.utils.Platform

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object FilesCopyTest {
  @BeforeClass
  def assumeIsUnix(): Unit = {
    assumeFalse(
      "Not checking Unix file permissions on Windows",
      Platform.isWindows
    )
  }
}

class FilesCopyTest {
  import FilesTest.*

  private def createOsFile(
      path: Path,
      permissions: String,
      content: String
  ): Unit = {
    val permissionsSet = PosixFilePermissions.fromString(permissions)

    Files.createFile(path)
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))

    Files.setPosixFilePermissions(path, permissionsSet)
  }

  private def validateStringLine(content: String, path: Path): Unit = {
    /* There is no Files.readString() in Java 8. It was introduced in Java 11.
     * Fallback to this workaround.
     */
    val pathLines = Files.readAllLines(path, StandardCharsets.UTF_8)

    assertEquals("number path lines", 1, pathLines.size())

    assertEquals(s"string content", content, pathLines.get(0))
  }

  private def validateLineContent(path_1: Path, path_2: Path): Unit = {
    /* This is intended to validate a "small" number of lines, where
     * the violation of "small" is defined by caller dissatisfaction.
     *
     * This "by line" is used in order to give a more informative
     * assertion on mismatched content.
     */

    /* lines() will remove line terminator. OK here, since that is done
     * for both paths, maintaining the match. There is a vanishingly
     * slight chance that two paths will match if all bytes but the
     * line terminator match. To ward that off, one needs to do a binary
     *  and not a line-oriented validate.
     */

    val expected = Files
      .lines(path_1, StandardCharsets.UTF_8)
      .toArray(n => new Array[String](n))

    val uut = Files
      .lines(path_2, StandardCharsets.UTF_8) // unit-under-test
      .toArray(n => new Array[String](n))

    val expectedLength = expected.length
    assertEquals("length", expectedLength, uut.length)

    for (j <- 0 until expectedLength)
      assertEquals(s"content line $j", expected(j), uut(j))
  }

// Continuous Integration --------------------------------------------

  @Test def copyStreamPathOptions_PosixPermissions_NoReplace(): Unit = {
    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-x------"

      /*  group & other bits depend on umask, which is unknown &
       *  uncontrollable at this point. For reference:
       *    For umask 000, the usual default is   rw-rw-r--
       *    With the usual umask 022, one gets    rw-r--r--
       *    With conservative umask 077, one gets rw-------
       */

      // '-' indicates must be OFF, '?' indicates could be either on or off.
      val expectedToUserPerms = "?w-??-?--"

      val toPath = dirPath.resolve(s"toPath_${expectedToUserPerms}")

      val testString =
        s"test-string: Files.copy(InputStream, Path, NO_REPLACE)\n"

      val inStream =
        new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8))

      Files.copy(inStream, toPath, StandardCopyOption.REPLACE_EXISTING)

      val toPermissions = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      /* Given that File.copy() did not throw, the output file almost
       * certainly has OWNER_WRITE. An adverse umask. 40mumble, may
       * have turned OWNER_READ off, so do not test that bit.
       */

      assertFalse(
        "user execute bit",
        toPermissions.contains(PosixFilePermission.OWNER_EXECUTE)
      )

      assertFalse(
        "group execute bit",
        toPermissions.contains(PosixFilePermission.GROUP_EXECUTE)
      )

      assertFalse(
        "others write bit",
        toPermissions.contains(PosixFilePermission.OTHERS_WRITE)
      )

      assertFalse(
        "others execute bit",
        toPermissions.contains(PosixFilePermission.OTHERS_EXECUTE)
      )

      validateStringLine(testString.trim(), toPath)

      cleanupWorkArea(dirPath, toPath)
    }
  }

  @Test def copyStreamPathOptions_PosixPermissions_Replace(): Unit = {
    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-x------"
      val differentBitsOn = "--xrwx---" // test also that x gets cleared.

      /* Files created with umask 000 (no bits masked off) usually
       * have permission "rw-rw-r--"
       */

      val expectedToUserPermsPrefix = "rw-"

      val toPath = dirPath.resolve(s"toPath_rw")

      val testString =
        s"test-string: Files.copy(InputStream, Path, REPLACE_EXISTING)\n"

      createOsFile(toPath, differentBitsOn, s"Existing toPath instance\n")

      val inStream =
        new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8))

      Files.copy(inStream, toPath, StandardCopyOption.REPLACE_EXISTING)

      val toPermissions = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      /* Depending on umask, Files.copy(stream, Path)
       * may or may not have set OWNER_READ, OWNER_WRITE, GROUP_READ,
       * and GROUP_WRITE, so do not test those bits.
       * All *_EXECUTE bits and OTHERS_WRITE should definitely
       * be off/clear.
       *
       * umask 000 will cause this test to fail, but such a mask is
       * not used in CI and rare in the wild. Usually it is 022 or 077.
       */

      assertFalse(
        "user execute bit",
        toPermissions.contains(PosixFilePermission.OWNER_EXECUTE)
      )

      assertFalse(
        "group execute bit",
        toPermissions.contains(PosixFilePermission.GROUP_EXECUTE)
      )

      assertFalse(
        "others write bit",
        toPermissions.contains(PosixFilePermission.OTHERS_WRITE)
      )

      assertFalse(
        "others execute bit",
        toPermissions.contains(PosixFilePermission.OTHERS_EXECUTE)
      )

      validateStringLine(testString.trim(), toPath)

      cleanupWorkArea(dirPath, toPath)
    }
  }

  @Test def copyPathPathOptions_PosixPermissions_NoCopyAttr(): Unit = {
    /* Verify fix for "execute bit is not propagated" as reported by 'os-lib'
     * when COPY_ATTRIBUTES is not specified.
     */

    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-x------"

      // Expect 'from' and 'to' permissions to be same, no other bits change
      val expectedToUserPerms = posixBitsOn

      val fromPath = dirPath.resolve(s"fromPath_${posixBitsOn}")
      val toPath = dirPath.resolve(s"toPath_${posixBitsOn}")

      val testString = s"test-string: Files.copy(Path, Path)\n"
      createOsFile(fromPath, posixBitsOn, testString)

      Files.copy(fromPath, toPath)

      val toPermissionsSet = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      assertEquals(
        s"to file permissions",
        expectedToUserPerms,
        PosixFilePermissions.toString(toPermissionsSet)
      )

      cleanupWorkArea(dirPath, fromPath, toPath)
    }
  }

  @Test def copyPathPathOptions_PosixPermissions_NoCopyAttr_Replace(): Unit = {
    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-x------"

      // Expect 'from' and 'to' permissions to be same, no other bits change
      val expectedToUserPerms = posixBitsOn

      val fromPath = dirPath.resolve(s"fromPath_${posixBitsOn}")
      val toPath = dirPath.resolve(s"toPath_${posixBitsOn}")

      val testString =
        s"test-string: Files.copy(Path, Path, REPLACE_EXISTING)\n"

      createOsFile(fromPath, posixBitsOn, testString)

      Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING)

      val toPermissionsSet = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      assertEquals(
        s"to file permissions",
        expectedToUserPerms,
        PosixFilePermissions.toString(toPermissionsSet)
      )

      cleanupWorkArea(dirPath, fromPath, toPath)
    }
  }

  @Test def copyPathPathOptions_PosixPermissions_CopyAttr(): Unit = {
    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-x------"

      /* Files.copy(Path, Path)  and Files.copy(Path, Path, COPY_ATTRIBUTES)
       * should both give same fromFile permissions to toFile.
       */

      // Expect 'from' and 'to' permissions to be same, no other bits change
      val expectedToUserPerms = posixBitsOn

      val fromPath = dirPath.resolve(s"fromPath_${posixBitsOn}")
      val toPath = dirPath.resolve(s"toPath_${posixBitsOn}")

      val testString =
        s"test-string: Files.copy(Path, Path, COPY_ATTRIBUTES)\n"
      createOsFile(fromPath, posixBitsOn, testString)

      Files.copy(fromPath, toPath, StandardCopyOption.COPY_ATTRIBUTES)

      val toPermissionsSet = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      assertEquals(
        s"to file permissions, COPY_ATTRIBUTES",
        expectedToUserPerms,
        PosixFilePermissions.toString(toPermissionsSet)
      )

      validateLineContent(fromPath, toPath)

      cleanupWorkArea(dirPath, fromPath, toPath)
    }
  }

// Manual advanced development & maintenance tests -------------------

  /* Precondition: environment umask is 022
   * That is, no user bits are masked off but group and other can have r-x
   *
   * umask may need to be manually set before starting sbt.
   */
  @Ignore // This is a manual special purpose advanced test.
  @Test def copyPathPathOptions_PosixPermissions_NoCopyAttr_umask022(): Unit = {

    /* copy a file r-xr-xr-x then verify that permission bits set (a.k.a on, 1)
     * in umask are clear (a.k.a off, 0) in resultant file.
     *
     * This is also a secondary verification that the 'x' bit is propagated.
     */

    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-xrwxr-x"

      // bits set in umask have been cleared in toUserPerms
      val expectedToUserPerms = "r-xr-xr-x"

      val fromPath = dirPath.resolve(s"fromPath_${posixBitsOn}")
      val toPath = dirPath.resolve(s"toPath_${posixBitsOn}")

      val testString = s"test-string: Files.copy(Path, Path)\n"
      createOsFile(fromPath, posixBitsOn, testString)

      Files.copy(fromPath, toPath)

      val toPermissionsSet = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      assertEquals(
        s"to file permissions",
        expectedToUserPerms,
        PosixFilePermissions.toString(toPermissionsSet)
      )

      validateLineContent(fromPath, toPath)

      cleanupWorkArea(dirPath, fromPath, toPath)
    }
  }

  /* Precondition: environment umask is 022
   * That is, no user bits are masked off but group and other can have r-x
   * 
   * umask may need to be manually set before starting sbt.
   */
  @Ignore // This is a manual special purpose advanced test.
  @Test def copyPathPathOptions_PosixPermissions_NoCopyAttr_Replace_mask022()
      : Unit = {

    /* copy a file r-xr-xr-x then verify that permission bits set (a.k.a on, 1)
     * in umask are clear (a.k.a off, 0) in resultant file.
     *
     * This is also a secondary verification that the 'x' bit is propagated.
     */

    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-xrwxr-x"
      val differentBitsOn = "r-xrwx---"

      // bits set in umask have been cleared in toUserPerms
      val expectedToUserPerms = "r-xr-xr-x"

      val fromPath = dirPath.resolve(s"fromPath_${posixBitsOn}")
      val toPath = dirPath.resolve(s"toPath_${posixBitsOn}")

      val testString =
        s"test-string: Files.copy(Path, Path, REPLACE_EXISTING)\n"

      createOsFile(fromPath, posixBitsOn, testString)

      createOsFile(toPath, differentBitsOn, s"Existing toPath instance\n")

      /* On success, toPath will be replaced and its permissions possibly
       * modified success.  Create clone toPath as it exists before the copy.
       * This allows examining the intermediate file permissions.
       * May you never have to use that information.
       */

      val existingToPath =
        dirPath.resolve(s"toPath-for-Replacement_${posixBitsOn}")
      createOsFile(
        existingToPath,
        differentBitsOn,
        s"Existing toPath instance\n"
      )

      Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING)

      val toPermissionsSet = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      assertEquals(
        s"to file permissions",
        expectedToUserPerms,
        PosixFilePermissions.toString(toPermissionsSet)
      )

      validateLineContent(fromPath, toPath)

      cleanupWorkArea(dirPath, fromPath, toPath, existingToPath)
    }
  }

  /* Precondition: environment umask is 077.
   * That is, no user bits are masked off but all 'group' and 'other' bits are.
   * 
   * umask may need to be manually set before starting sbt.
   */

  @Ignore // This is a manual special purpose advanced test.
  @Test def copyPathPathOptions_PosixPermissions_NoCopyAttr_umask077(): Unit = {
    /* copy a file r-xr-xr-x then verify that permission bits set (a.k.a on, 1)
     * in umask are clear (a.k.a off, 0) in resultant file.
     *
     * This is also a secondary verification that the 'x' bit is propagated.
     */

    withTemporaryDirectoryPath { dirPath =>

      val posixBitsOn = "r-xr-xr-x"

      // bits set in umask have been cleared in toUserPerms
      val expectedToUserPerms = "r-x------"

      val fromPath = dirPath.resolve(s"fromPath_${posixBitsOn}")
      val toPath = dirPath.resolve(s"toPath_${posixBitsOn}")

      val testString = s"test-string: Files.copy(Path, Path)\n"
      createOsFile(fromPath, posixBitsOn, testString)

      Files.copy(fromPath, toPath)

      val toPermissionsSet = Files
        .readAttributes(toPath, classOf[PosixFileAttributes])
        .permissions()

      assertEquals(
        s"to file permissions",
        expectedToUserPerms,
        PosixFilePermissions.toString(toPermissionsSet)
      )

      cleanupWorkArea(dirPath, fromPath, toPath)
    }
  }

  @Ignore // This is a manual special purpose advanced test.
  @Test def copyPathPathOptions_LargeFile(): Unit = {
    /* The rationale for this test is to stress the internal buffer
     * used by Files.copy(Path, Path, options) by ensuring that more
     * bytes are provide than can fit into one buffer. Do the transitions
     * occur correctly? Are all bytes copied? Are some dropped from
     * any final buffer partial fill?
     * 
     * To enhance the power of this test, also reduce the size in
     * unix Files.copy(Path, Path) to something small and odd, say, 3.
     */
    withTemporaryDirectoryPath { dirPath =>
      /* A guess > 4K or 8K internal buffer and not exact multiple of 4K,
       * vary to taste & need.
       */
      val kBytes = 42

      // nBytes should be a power of 8, sizeof(Long)
      val nBytes = 1024 * kBytes

      def createSourceFile(path: Path, nKBytes: Int): Unit = {
        val nLongs = nKBytes / 8

        val seed = 872145L // https://random.colorado.edu/
        val rng = new SplittableRandom(seed)

        val bos = new java.io.BufferedOutputStream(Files.newOutputStream(path))
        val dos = new java.io.DataOutputStream(bos)

        try {
          rng.longs(nLongs).forEach(rv => dos.writeLong(rv))
        } finally {
          dos.close()
        }
      }
      def validateBinaryContent(pathA: Path, pathB: Path): Unit = {
        /* This implementation for comparing data, such as raw binary, where
         * there is no line structure.
         *
         * The method gets style points for being done,
         * believably fit-for-purpose, and implemented for JDK 8.
         *
         * It gets zero style points for runtime efficiency.
         * Comparing by 8 bytes is better than comparing by 1, but not
         * as good as comparing blocks of, say 1024, bytes.
         * Such an improvement is left for someday.
         *
         * JDK 12 brings Files.mismatch(), which is a perfect fit
         * for this method. As long as the current code is believed correct,
         * it may not make sense spending much time on runtime improvements
         * for a manually executed file.
         */

        val bisA = new BufferedInputStream(Files.newInputStream(pathA))
        val disA = new DataInputStream(bisA)

        val bisB = new BufferedInputStream(Files.newInputStream(pathB))
        val disB = new DataInputStream(bisB)

        try {
          while ((disA.available() > 0) && (disB.available() > 0)) {
            val longA = disA.readLong()
            val longB = disB.readLong()
            assertEquals("validate content", longA, longB)
          }

          assertTrue(
            "validate end size",
            (disA.available() == 0) && (disB.available() == 0)
          )

        } finally {
          disA.close()
          disB.close()
        }
      }

      val fromPath = dirPath.resolve(s"fromPath_${kBytes}KB")
      val toPath = dirPath.resolve(s"toPath_${kBytes}KB")

      createSourceFile(fromPath, nBytes)
      assertEquals("from size", Files.size(fromPath), nBytes)

      Files.copy(fromPath, toPath)

      assertEquals("to size", Files.size(toPath), nBytes)
      validateBinaryContent(fromPath, toPath)

      cleanupWorkArea(dirPath, toPath)
    }
  }
}
