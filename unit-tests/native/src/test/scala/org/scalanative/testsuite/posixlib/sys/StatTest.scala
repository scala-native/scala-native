package org.scalanative.testsuite.posixlib
package sys

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.BeforeClass

import scala.scalanative.meta.LinktimeInfo

import java.nio.file.{Files, Path}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.errno.errno
import scala.scalanative.posix.stdlib.mkstemp
import scala.scalanative.posix.string.strerror
import scala.scalanative.posix.sys.stat

object StatTest {
  private var workDirString: String = _

  /* It would be nice someday to have a @AfterClass which cleaned up
   * after a successful Test run by deleting the files created by the Test.
   * There would probably need to be a debug configuration toggle to always
   * leave the file in place, successful or not
   */

  @BeforeClass
  def beforeClass(): Unit = {
    if (!LinktimeInfo.isWindows) {
      val orgDir = Files.createTempDirectory("scala-native-testsuite")
      val posixlibDir = orgDir.resolve("posixlib")
      workDirString = Files
        .createDirectories(posixlibDir.resolve("StatTest"))
        .toString()
    }
  }
}

class StatTest {
  import StatTest.workDirString

  @Test def fileStatTest(): Unit = if (!LinktimeInfo.isWindows) {
    Zone.acquire { implicit z =>
      import scala.scalanative.posix.sys.statOps.statOps

      // Note: tmpname template gets modified by a successful mkstemp().
      val tmpname = toCString(s"${workDirString}/StatTestFileXXXXXX")
      val fd = mkstemp(tmpname)

      assertTrue(
        s"failed to create ${fromCString(tmpname)}:" +
          s" ${fromCString(strerror(errno))}",
        fd > -1
      )

      val statFromPath = stackalloc[stat.stat]()
      val code = stat.stat(tmpname, statFromPath)
      assertEquals(
        s"failed to get stat from ${fromCString(tmpname)}:" +
          s" ${fromCString(strerror(errno))}",
        0,
        code
      )
      val statFromFd = stackalloc[stat.stat]()
      val code0 = stat.fstat(fd, statFromFd)
      assertEquals(
        s"failed to get stat from fd $fd of ${fromCString(tmpname)}:" +
          s" ${fromCString(strerror(errno))}",
        0,
        code0
      )
      assertEquals(
        "st_dev from path and from fd must be the same",
        statFromPath.st_dev,
        statFromFd.st_dev
      )
      assertEquals(
        "st_rdev from path and from fd must be the same",
        statFromPath.st_rdev,
        statFromFd.st_rdev
      )

      val expectedRdev =
        if (!LinktimeInfo.isFreeBSD) 0.toUSize // Linux, macOS
        else ULong.MaxValue.toUSize

      assertEquals(
        s"st_rdev must be ${expectedRdev} for regular file",
        expectedRdev,
        statFromPath.st_rdev
      )
      assertEquals(
        "st_ino from path and from fd must be the same",
        statFromPath.st_ino,
        statFromFd.st_ino
      )
      assertEquals(
        "st_uid from path and from fd must be the same",
        statFromPath.st_uid,
        statFromFd.st_uid
      )
      assertEquals(
        "st_gid from path and from fd must be the same",
        statFromPath.st_gid,
        statFromFd.st_gid
      )

      assertEquals("tmpfile must be empty", 0, statFromPath.st_size)
      assertEquals("tmpfile must be empty", 0, statFromFd.st_size)

      val expectedBlksize =
        if (!LinktimeInfo.isFreeBSD) 0 // Linux, macOS
        else 1

      assertEquals(
        "unexpected statFromPath.blksize",
        expectedBlksize,
        statFromPath.st_blocks
      )
      assertEquals(
        "unexpected statFromFd.blksize",
        expectedBlksize,
        statFromFd.st_blocks
      )

      assertEquals(
        "st_atime from path and from fd must be the same",
        statFromPath.st_atime,
        statFromFd.st_atime
      )
      assertEquals(
        "st_mtime from path and from fd must be the same",
        statFromPath.st_mtime,
        statFromFd.st_mtime
      )
      assertEquals(
        "st_ctime from path and from fd must be the same",
        statFromPath.st_ctime,
        statFromFd.st_ctime
      )
      assertEquals(
        "st_mode from path and from fd must be the same",
        statFromPath.st_mode,
        statFromFd.st_mode
      )
      assertEquals(
        "second part of st_atim from path and from fd must be the same",
        statFromPath.st_atim._1,
        statFromFd.st_atim._1
      )
      assertEquals(
        "nanosecond part of st_atim from path and from fd must be the same",
        statFromPath.st_atim._2,
        statFromFd.st_atim._2
      )
      assertEquals(
        "second part of st_mtim from path and from fd must be the same",
        statFromPath.st_mtim._1,
        statFromFd.st_mtim._1
      )
      assertEquals(
        "nanosecond part of st_mtim from path and from fd must be the same",
        statFromPath.st_mtim._2,
        statFromFd.st_mtim._2
      )
      assertEquals(
        "second part of st_ctim from path and from fd must be the same",
        statFromPath.st_ctim._1,
        statFromFd.st_ctim._1
      )
      assertEquals(
        "nanosecond part of st_ctim from path and from fd must be the same",
        statFromPath.st_ctim._2,
        statFromFd.st_ctim._2
      )
      assertEquals(
        "st_nlink from path and from fd must be the same",
        statFromPath.st_nlink,
        statFromFd.st_nlink
      )
      assert(
        statFromPath.st_nlink.toInt >= 1,
        "regular file must have at least 1 nlink"
      )
      assertEquals(
        "tmpfile must be regular file",
        1,
        stat.S_ISREG(statFromPath.st_mode)
      )
      assertEquals(
        "tmpfile must not be dir",
        0,
        stat.S_ISDIR(statFromPath.st_mode)
      )
      assertEquals(
        "tmpfile must not be chr",
        0,
        stat.S_ISCHR(statFromPath.st_mode)
      )
      assertEquals(
        "tmpfile must not be blk",
        0,
        stat.S_ISBLK(statFromPath.st_mode)
      )
      assertEquals(
        "tmpfile must not be fifo",
        0,
        stat.S_ISFIFO(statFromPath.st_mode)
      )
      assertEquals(
        "tmpfile must not be lnk",
        0,
        stat.S_ISLNK(statFromPath.st_mode)
      )
      assertEquals(
        "tmpfile must not be sock",
        0,
        stat.S_ISSOCK(statFromPath.st_mode)
      )

      /* Note well:
       *   This _exactly_ the classic "tmpnam()" race discussion that
       *   lead to that function being deprecated.
       *
       *   The objective here is to exercise stat.mkdir(), so mkdtemp() is
       *   not appropriate.
       *
       *   The chance of two concurrent executions of StatTest trying to
       *   create the exact same directory are reduced/avoided in this code
       *   by having the almost-top level orgDir be created as a temporary
       *   file. Different StatTest instances should be always using different
       *   working directories.
       *
       *   If experience demands, one could also create the posixlibDir
       *   and workDir as temporary directories.
       */

      val tmpdirname = toCString(s"${workDirString}/StatTestDir")
      val dirFd = stat.mkdir(tmpdirname, Integer.parseInt("0777", 8).toUInt)

      val dirStatFromPath = stackalloc[stat.stat]()
      val dircode = stat.stat(tmpdirname, dirStatFromPath)
      assertEquals(0, dircode)
      assertEquals(
        0,
        stat.S_ISREG(dirStatFromPath.st_mode)
      )
      assertEquals(
        1,
        stat.S_ISDIR(dirStatFromPath.st_mode)
      )

      /* OpenBSD returns some vlaue as st_rdev for directory,
       * which seems to be related to inode => we can't predict it */
      if (!LinktimeInfo.isOpenBSD) {
        assertEquals(
          s"st_rdev must be ${expectedRdev} for dir file",
          expectedRdev,
          dirStatFromPath.st_rdev
        )
      }

      assert(
        dirStatFromPath.st_nlink.toInt >= 2
      )
    }
  }
}
