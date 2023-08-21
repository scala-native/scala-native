package org.scalanative.testsuite.posixlib
package sys

import scala.scalanative.unsafe._
import scala.scalanative.unsigned.UnsignedRichInt

import scala.scalanative.posix.sys.wait._

import scala.scalanative.meta.LinktimeInfo.isWindows

import org.junit.Test
import org.junit.Assert._

/* Design Note:
 *     By their definition, these "wait" methods block the current thread.
 *     They are also defined without a timeout value.
 *
 *     The ppoll/epoll/kevent methods needed for the classical
 *     "block in ppoll/epoll/kevent until either child exits or
 *     a specified timeout expires, call wait/waitpid/waitid" approach
 *     is not available. ppoll, epoll, and kevent are not defined in POSIX
 *     and are operating system specific. They are not implemented in
 *     Scala Native.
 *
 *     These conditions make proper unit-testing difficult.
 *
 *     "waitpid()" is/will be well exercise in javalib ProcessTest.
 *     To keep concerns separate, ProcessTest can not exercise both
 *     "waitpid()" & "waitid()".
 *
 *     Tests for "wait()" & "waitid()" are left as an exercise for the
 *     reader. Beware that one does not hang the entire Continuous Integration
 *     build.
 */

class WaitTest {

  /* The major purpose of this file is the above Design Note.
   * As long as we are here, might as well do some work.
   */

  def blackHole(a: Any): Unit = ()

  @Test def waitBindingsShouldCompileAndLink(): Unit = {
    if (!isWindows) {
      // zero initialized placeholder
      val wstatus = stackalloc[CInt](1)

      // idtype_t
      blackHole(P_ALL)
      blackHole(P_PGID)
      blackHole(P_PID)

// Symbolic constants, roughly in POSIX declaration order

      // "constants" for waitpid()

      blackHole(WCONTINUED) // XSI
      blackHole(WNOHANG)
      blackHole(WUNTRACED)

      // "constants" for waitid() options
      blackHole(WEXITED)
      blackHole(WNOWAIT)
      blackHole(WSTOPPED)

// POSIX "Macros"
      WEXITSTATUS(!wstatus)
      WIFCONTINUED(!wstatus) // XSI
      WIFEXITED(!wstatus)
      WIFSIGNALED(!wstatus)
      WIFSTOPPED(!wstatus)
      WSTOPSIG(!wstatus)
      WTERMSIG(!wstatus)
    }
  }
}
