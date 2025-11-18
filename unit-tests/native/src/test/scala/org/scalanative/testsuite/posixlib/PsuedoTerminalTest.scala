package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scalanative.meta.LinktimeInfo.isWindows
import scalanative.posix.fcntl._
import scalanative.posix.stddef._
import scalanative.posix.stdio
import scalanative.posix.stdlib._
import scalanative.posix.string.strlen
import scalanative.posix.sys.types.ssize_t
import scalanative.posix.termios._
import scalanative.posix.termiosOps._
import scalanative.posix.unistd._
import scalanative.unsafe.Nat._
import scalanative.unsafe._
import scalanative.unsigned._

/** Used to test terminal in CI env where real devices are not available. Simple
 *  POSIX pseudoterminal test using termios Modern terminology: primary
 *  (controller) / secondary (session)
 */
class PseudoTerminalTest {

  @Test def testPt(): Unit = if (!isWindows) {
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

    secondary_name = ptsname(primary_fd)
    assertFalse("ptsname", secondary_name == null)
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

    var echo = if ((tio.c_lflag & ECHO.toUInt) != 0) c"on" else c"off"
    var icanon = if ((tio.c_lflag & ICANON.toUInt) != 0) c"on" else c"off"

    stdio.printf(c"ECHO: %s\n", echo)
    stdio.printf(c"ICANON: %s\n", icanon)

    /* Modify attributes: disable echo and canonical mode */
    tio.c_lflag = tio.c_lflag & ~(ECHO.toUInt | ICANON.toUInt)
    tio.c_cc(VMIN) = 1.toUByte
    tio.c_cc(VTIME) = 0.toUByte

    echo = if ((tio.c_lflag & ECHO.toUInt) != 0) c"on" else c"off"
    icanon = if ((tio.c_lflag & ICANON.toUInt) != 0) c"on" else c"off"

    stdio.printf(c"ECHO: %s\n", echo)
    stdio.printf(c"ICANON: %s\n", icanon)

    assertFalse("tcsetattr", tcsetattr(secondary_fd, TCSANOW, tio) < 0)
    stdio.printf(c"Termios configured: ECHO and ICANON disabled\n")

    echo = if ((tio.c_lflag & ECHO.toUInt) != 0) c"on" else c"off"
    icanon = if ((tio.c_lflag & ICANON.toUInt) != 0) c"on" else c"off"

    stdio.printf(c"ECHO: %s\n", echo)
    stdio.printf(c"ICANON: %s\n", icanon)

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
      stdio.printf(c"Secondary received: %s", buf)
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
      stdio.printf(c"Primary received: %s", buf);
    } else {
      assertFalse("read(primary)", n < 0)
    }
    // n could be 0

    close(secondary_fd);
    close(primary_fd);
    stdio.printf(c"PTY test complete.\n");

    assertTrue(true)
  }

//   /*
//    * test_termios_pty.c
//    * Simple POSIX pseudoterminal test using termios
//    * Modern terminology: primary (controller) / secondary (session)
//    *
//  * Build: gcc -Wall -O2 test_termios_pty.c -o test_termios_pty
//    */

// #define _XOPEN_SOURCE 600
// #include <stdio.h>
// #include <stdlib.h>
// #include <fcntl.h>
// #include <unistd.h>
// #include <termios.h>
// #include <string.h>
// #include <errno.h>

// int main(void) {
//     int primary_fd = -1, secondary_fd = -1;
//     char *secondary_name = NULL;
//     struct termios tio;

//     /* Open a new pseudoterminal primary (controller) side */
//     primary_fd = posix_openpt(O_RDWR | O_NOCTTY);
//     if (primary_fd < 0) {
//         perror("posix_openpt");
//         return 1;
//     }

//     if (grantpt(primary_fd) < 0 || unlockpt(primary_fd) < 0) {
//         perror("grantpt/unlockpt");
//         close(primary_fd);
//         return 1;
//     }

//     secondary_name = ptsname(primary_fd);
//     if (!secondary_name) {
//         perror("ptsname");
//         close(primary_fd);
//         return 1;
//     }

//     printf("Opened PTY pair: primary=%d, secondary=%s\n", primary_fd, secondary_name);

//     /* Open the secondary side */
//     secondary_fd = open(secondary_name, O_RDWR | O_NOCTTY);
//     if (secondary_fd < 0) {
//         perror("open(secondary)");
//         close(primary_fd);
//         return 1;
//     }

//     /* Get current terminal attributes */
//     if (tcgetattr(secondary_fd, &tio) < 0) {
//         perror("tcgetattr");
//         close(secondary_fd);
//         close(primary_fd);
//         return 1;
//     }

//     /* Modify attributes: disable echo and canonical mode */
//     tio.c_lflag &= ~(ECHO | ICANON);
//     tio.c_cc[VMIN] = 1;
//     tio.c_cc[VTIME] = 0;

//     if (tcsetattr(secondary_fd, TCSANOW, &tio) < 0) {
//         perror("tcsetattr");
//         close(secondary_fd);
//         close(primary_fd);
//         return 1;
//     }

//     printf("Termios configured: ECHO and ICANON disabled\n");

//     /* Write from primary to secondary */
//     const char *msg = "Hello from primary!\n";
//     if (write(primary_fd, msg, strlen(msg)) < 0) {
//         perror("write(primary)");
//     }

//     /* Read from secondary */
//     char buf[128];
//     ssize_t n = read(secondary_fd, buf, sizeof(buf) - 1);
//     if (n > 0) {
//         buf[n] = '\0';
//         printf("Secondary received: %s", buf);
//     } else if (n < 0) {
//         perror("read(secondary)");
//     }

//     /* Write from secondary to primary */
//     const char *reply = "Reply from secondary.\n";
//     if (write(secondary_fd, reply, strlen(reply)) < 0) {
//         perror("write(secondary)");
//     }

//     /* Read from primary */
//     n = read(primary_fd, buf, sizeof(buf) - 1);
//     if (n > 0) {
//         buf[n] = '\0';
//         printf("Primary received: %s", buf);
//     } else if (n < 0) {
//         perror("read(primary)");
//     }

//     close(secondary_fd);
//     close(primary_fd);
//     printf("PTY test complete.\n");
//     return 0;
// }

}
