package org.scalanative.testsuite.posixlib

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.sys.stat
import scala.scalanative.posix.utime
import scala.scalanative.unsafe._

class UtimeTest {

  @Test def utimeSetsAccessAndModificationTimes(): Unit = {
    assumeFalse(LinktimeInfo.isWindows)
    val file = Files.createTempFile("UtimeTest", ".txt")
    try
      Zone.acquire { implicit z =>
        import scala.scalanative.posix.sys.statOps.statOps
        val path = toCString(file.toString())
        val times = alloc[utime.utimbuf]()
        times._1 = 1500000000L.toSize
        times._2 = 1577930645L.toSize
        assertEquals(0, utime.utime(path, times))

        val buf = alloc[stat.stat]()
        assertEquals(0, stat.stat(path, buf))
        assertEquals(1500000000L, buf.st_atime.toLong)
        assertEquals(1577930645L, buf.st_mtime.toLong)
      }
    finally Files.delete(file)
  }
}
