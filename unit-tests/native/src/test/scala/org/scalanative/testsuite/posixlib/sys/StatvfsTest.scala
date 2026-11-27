package org.scalanative.testsuite.posixlib
package sys

import java.nio.file.Files

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import scala.scalanative.libc.LibcExt
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.sys.statvfs
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

object StatvfsTest {
  private var workDirString: String = _

  @BeforeClass
  def beforeClass(): Unit = {
    if (!LinktimeInfo.isWindows) {
      workDirString = Files
        .createTempDirectory("scala-native-testsuite-StatvfsTest")
        .toString()
    }
  }
}

class StatvfsTest {
  import StatvfsTest.workDirString

  private def checkStatvfs(what: String, buf: Ptr[statvfs.statvfs]): Unit = {
    val frsize = buf._2
    val blocks = buf._3
    val bfree = buf._4
    val bavail = buf._5

    assertTrue(s"$what: f_frsize must be positive", frsize > 0.toULong)
    assertTrue(s"$what: f_blocks must be positive", blocks > 0.toULong)
    assertTrue(
      s"$what: f_bfree ($bfree) must not exceed f_blocks ($blocks)",
      bfree <= blocks
    )
    assertTrue(
      s"$what: f_bavail ($bavail) must not exceed f_bfree ($bfree)",
      bavail <= bfree
    )
  }

  @Test def statvfsOfPath(): Unit = if (!LinktimeInfo.isWindows) {
    Zone.acquire { implicit z =>
      val path = toCString(workDirString)
      val buf = stackalloc[statvfs.statvfs]()

      val code = statvfs.statvfs(path, buf)
      assertEquals(
        s"statvfs failed on ${workDirString}: ${LibcExt.strError()}",
        0,
        code
      )
      checkStatvfs(s"statvfs(${workDirString})", buf)
    }
  }

  @Test def fstatvfsOfFd(): Unit = if (!LinktimeInfo.isWindows) {
    Zone.acquire { implicit z =>
      val fd = fcntl.open(toCString(workDirString), fcntl.O_RDONLY)
      assertTrue(
        s"failed to open ${workDirString}: ${LibcExt.strError()}",
        fd > -1
      )

      try {
        val buf = stackalloc[statvfs.statvfs]()

        val code = statvfs.fstatvfs(fd, buf)
        assertEquals(
          s"fstatvfs failed on fd $fd of ${workDirString}: " +
            LibcExt.strError(),
          0,
          code
        )
        checkStatvfs(s"fstatvfs(fd of ${workDirString})", buf)
      } finally {
        unistd.close(fd)
      }
    }
  }
}
