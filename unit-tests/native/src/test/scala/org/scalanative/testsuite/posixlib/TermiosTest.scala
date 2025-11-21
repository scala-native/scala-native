package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.libc.stdio
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.termios
import scala.scalanative.posix.termios._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

class TermiosTest {
  val verbose = true

  def setRawMode()(implicit z: Zone): Ptr[termios] = {
    val attrs = alloc[termios]()
    tcgetattr(1, attrs)
    attrs._4 = attrs._4 & ~(ECHO | ICANON).toUInt
    tcsetattr(1, TCSAFLUSH, attrs)
    attrs
  }

  @Test def testSetRawMode_4143(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val tio = setRawMode() // bug failed here
      assertTrue(true)
    }
  }

  @Test def testGetDefaultISpeed(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val tio = setRawMode() // bug failed here
      val sp = cfgetispeed(tio)
      if (verbose) stdio.printf(c"Raw default ispeed: %d\n", sp)
      val res = cfsetispeed(tio, B9600.toUInt)
      if (verbose) stdio.printf(c"Raw set result: %d\n", res)
      val newsp = cfgetispeed(tio)
      if (verbose) stdio.printf(c"Raw after ispeed: %d\n", newsp)
      assertTrue("raw cfgetispeed", newsp == 9600)
      assertTrue(true)
    }
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

  @Test def testCheckStructSize(): Unit = if (!isWindows) {
    val ss = sizeof[termios]
    if (verbose) stdio.printf(c"termios sizeof: %d\n", ss)

    val a = alignmentof[termios]
    if (verbose) stdio.printf(c"termios alignmentof: %d\n", a)

    assertTrue(true)
  }

}
