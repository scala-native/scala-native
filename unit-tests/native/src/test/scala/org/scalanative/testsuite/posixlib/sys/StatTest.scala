package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Before, After}
import scala.scalanative.posix.limits
import scala.scalanative.meta.LinktimeInfo.{isLinux, isWindows}

import scala.scalanative.libc.stdio
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.errno
import scala.scalanative.posix.fcntl
import scala.scalanative.posix.sys.stat
import scala.scalanative.posix.unistd

class StatTest {

  @Test def fileStatTest(): Unit = if (!isWindows) {
    Zone { implicit z =>
      import scala.scalanative.posix.sys.statOps.statOps
      val partSize = limits.PATH_MAX.toUInt
      val buf: CString = alloc[Byte](partSize)
      val tmpname = stdio.tmpnam(buf)
      val fd = fcntl.open(tmpname, fcntl.O_CREAT)
      val statFromPath = alloc[stat.stat]()
      val code = stat.stat(tmpname, statFromPath)
      assertEquals(
        s"failed to get stat from $tmpname, errno = ${errno.errno}",
        0,
        code
      )
      val statFromFd = alloc[stat.stat]()
      val code0 = stat.fstat(fd, statFromFd)
      assertEquals(
        s"failed to get stat from fd $fd of $tmpname, errno = ${errno.errno}",
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
      assertEquals(
        "st_rdev must be 0 for regular file",
        0.toUSize,
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
      assertEquals("tmpfile must not have blksize", 0, statFromPath.st_blocks)
      assertEquals("tmpfile must not have blksize", 0, statFromFd.st_blocks)
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

      val dirnamebuf: CString = alloc[Byte](partSize)
      val tmpdirname = stdio.tmpnam(dirnamebuf)
      val dirFd = stat.mkdir(tmpdirname, Integer.parseInt("0777", 8).toUInt)
      val dirStatFromPath = alloc[stat.stat]()
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
      assertEquals(
        "st_rdev must be 0 for dir",
        0.toUSize,
        dirStatFromPath.st_rdev
      )
      assert(
        dirStatFromPath.st_nlink.toInt >= 2
      )
    }
  }
}
