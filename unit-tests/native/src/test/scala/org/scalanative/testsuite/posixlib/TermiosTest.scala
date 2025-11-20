package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.libc.stdio
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scalanative.meta.LinktimeInfo.isWindows
import scalanative.posix.termios._

class TermiosTest {
  val verbose = false

  def setRawMode(): Unit = {
    Zone.acquire { implicit z =>
      val attrs = alloc[termios]()
      tcgetattr(1, attrs)
      attrs._4 = attrs._4 & ~(ECHO | ICANON).toUInt
      tcsetattr(1, TCSAFLUSH, attrs)
    }
  }

  @Test def testSetRawMode_4143(): Unit = if (!isWindows) {
    setRawMode() // failed here
    assertTrue(true)
  }

  @Test def testCfgetsetispeed(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val tio = alloc[termios]()
      val res = cfsetispeed(tio, B4800.toUInt)
      assertTrue("cfsetispeed", res == 0)
      val br = cfgetispeed(tio)
      if (verbose) stdio.printf(c"ispeed: %d\n", br)
      assertTrue("cfgetispeed", br == 4800)
    }
  }

  @Test def testCfgetsetospeed(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val tio = alloc[termios]()
      val res = cfsetospeed(tio, B38400.toUInt)
      assertTrue("cfsetospeed", res == 0)
      val br = cfgetospeed(tio)
      if (verbose) stdio.printf(c"ospeed: %d\n", br)
      assertTrue("cfgetospeed", br == 38400)
    }
  }

}
