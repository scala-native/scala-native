package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scalanative.libc.stdio
import scalanative.meta.LinktimeInfo.{isLinux, isWindows}
import scalanative.posix.fcntl._
import scalanative.posix.stdlib._
import scalanative.posix.termios
import scalanative.posix.termios._
import scalanative.posix.termiosOps._
import scalanative.posix.unistd._
import scalanative.unsafe._
import scalanative.unsigned._

/** Basically to do anything meaningful you need to use pseudo terminals.
 */
class TermiosTest {
  val verbose = false // change to true for debug output

  def setRawMode()(implicit z: Zone): Ptr[termios] = {
    val tio = alloc[termios]()
    tcgetattr(STDOUT_FILENO, tio)
    tio._4 = tio._4 & ~(ECHO | ICANON).toUInt
    tcsetattr(1, TCSAFLUSH, tio)
    tio
  }

  @Test def testSetRawMode_4143(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val tio = setRawMode() // bug failed here
      assertTrue(true)
    }
  }

  @Test def testCheckStructSize(): Unit = if (!isWindows) {
    val ss = sizeof[termios]
    if (verbose) stdio.printf(c"termios sizeof: %d\n", ss)
    assertTrue("Termios size", ss == 44)
    val a = alignmentof[termios]
    if (verbose) stdio.printf(c"termios alignmentof: %d\n", a)
    assertTrue("Termios alignment", a == 4)

    assertTrue(true)
  }

}
