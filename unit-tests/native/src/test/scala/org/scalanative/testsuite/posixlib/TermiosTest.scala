package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scalanative.meta.LinktimeInfo.isWindows
import scalanative.posix.termios

class TermiosTest {

  def setRawMode(): Unit = {
    Zone.acquire { implicit z =>
      val attrs = alloc[termios.termios]()
      termios.tcgetattr(1, attrs)
      attrs._4 = attrs._4 & ~(termios.ECHO | termios.ICANON).toUInt
      termios.tcsetattr(1, termios.TCSAFLUSH, attrs)
    }
  }

  @Test def testSetRawMode(): Unit = if (!isWindows) {
    setRawMode()
    assertTrue(true)
  }

}
