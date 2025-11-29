package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scalanative.meta.LinktimeInfo.isWindows
import scalanative.posix.fcntl._
import scalanative.posix.stddef._
import scalanative.posix.stdio
import scalanative.posix.stdlib._
import scalanative.posix.string._
import scalanative.posix.sys.types.ssize_t
import scalanative.posix.termios._
import scalanative.posix.termiosOps._
import scalanative.posix.unistd._
import scalanative.unsafe.Nat._
import scalanative.unsafe._
import scalanative.unsigned._

/** Used to test terminal in CI env where real devices are not available. Simple
 *  POSIX pseudoterminal test using termios Modern terminology: primary
 *  (controller) / secondary (session) converted from AI suggestion in C. No
 *  specific attributable references found
 */
class PseudoTerminalTest {
  val verbose = false // change to true for debug output

  @Test def testPt(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>

      var primary_fd: CInt = -1
      var secondary_fd: CInt = -1
      var secondary_name = stackalloc[CChar]()
      secondary_name = null // NULL is CVoidPtr not CString
      val tio = stackalloc[termios]()

      /* Open a new pseudoterminal primary (controller) side */
      primary_fd = posix_openpt(O_RDWR | O_NOCTTY)
      assertFalse("posix_openpt", primary_fd < 0)
      assertFalse(
        "grantpt/unlockpt failed",
        (grantpt(primary_fd) < 0 || unlockpt(primary_fd) < 0)
      )

      assertTrue("ptsname before", secondary_name == null)
      secondary_name = ptsname(primary_fd)
      assertFalse("ptsname after", secondary_name == null)
      if (verbose)
        stdio.printf(
          c"Opened PTY pair: primary=%d, secondary=%s\n",
          primary_fd,
          secondary_name
        )

      /* Open the secondary side */
      secondary_fd = open(secondary_name, O_RDWR | O_NOCTTY);
      assertFalse("open(secondary)", secondary_fd < 0)

      /* Get current terminal attributes */
      assertFalse("tcgetattr", tcgetattr(secondary_fd, tio) < 0)

      /* Modify attributes: disable echo and canonical mode */
      tio.c_lflag = tio.c_lflag & ~(ECHO | ICANON).toUInt
      tio.c_cc(VMIN) = 1.toUByte
      tio.c_cc(VTIME) = 0.toUByte

      /** Disable output post-processing (OPOST) This stops the \n to \r\n
       *  conversion and other processing
       */
      tio.c_oflag = tio.c_oflag & ~(OPOST.toUInt);

      assertFalse("tcsetattr", tcsetattr(secondary_fd, TCSANOW, tio) < 0)
      if (verbose)
        stdio.printf(c"Termios configured: ECHO and ICANON disabled\n")

      assertTrue("ECHO off", (tio.c_lflag & ECHO.toUInt) == 0)
      assertTrue("ICANON off", (tio.c_lflag & ICANON.toUInt) == 0)

      /* Write from primary to secondary */
      val msg = c"Hello from primary!\n"
      assertFalse(
        "write(primary)",
        write(primary_fd, msg, strlen(msg)) < 0
      )

      /* Read from secondary */
      type _128 = Digit3[_1, _2, _8]
      val buf = stackalloc[CArray[CChar, _128]]()
      val bufSize = 128.toCSize - 1.toCSize
      var n = read(secondary_fd, buf, bufSize)
      if (n > 0) {
        !(buf.at(n)) = '\u0000' // '\0'
        if (verbose) {
          stdio.printf(c"Secondary received: %s", buf)
        }
        assertTrue("read(secondary) not expected", strcmp(msg, buf.at(0)) == 0)
      } else {
        assertFalse("read(secondary)", n < 0)
      }
      // n could be 0

      /* Write from secondary to primary */
      val reply = c"Reply from secondary.\n"
      assertFalse(
        "write(secondary)",
        write(secondary_fd, reply, strlen(reply)) < 0
      )

      /* Read from primary */
      n = read(primary_fd, buf, bufSize)
      if (n > 0) {
        !(buf.at(n)) = '\u0000'
        if (verbose) {
          stdio.printf(c"Reply: %d: %s", strlen(reply), reply)
          print(fromCString(reply), strlen(reply).toInt)
          stdio.printf(c"Primary received: %s", buf)
          print(fromCString(buf.at(0)), strlen(buf.at(0)).toInt)
        }
        assertTrue(s"read(primary) not expected", strcmp(reply, buf.at(0)) == 0)
      } else {
        assertFalse("read(primary)", n < 0)
      }
      // n could be 0

      close(secondary_fd);
      close(primary_fd);
      if (verbose) stdio.printf(c"PTY test complete.\n");
    }
  }

  def print(buf: String, len: Int): Unit = {
    printf("\n--- Inspection Results (%d bytes read) ---\n", len);

    var i = 0
    while (i < len) {
      val c = buf(i);

      // Print index, decimal value, hex value, and description/representation
      printf("Index %d: Dec=%d, Hex=0x%02x, Char=", i, c.toInt, c.toInt);

      if (c == '\n') {
        printf("'\\n' (Newline)\n");
      } else if (c == '\r') {
        printf("'\\r' (Carriage Return)\n");
      } else if (c == '\t') {
        printf("'\\t' (Tab)\n");
      } else if (!Character.isISOControl(c)) {
        printf("'%c' (Printable)\n", c);
      } else {
        printf("'.' (Non-printable/Control Char)\n");
      }
      i += 1
    }
    printf("--------------------------------------------\n");
  }

}
