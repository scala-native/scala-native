package scala.scalanative.posix
package sys

import scala.scalanative.unsafe._
import scalanative.unsigned.UnsignedRichInt

import scala.scalanative.posix.sys.wait._

import org.junit.Test
import org.junit.Assert._

/* Design Note:
 *
 */

class WaitTest {
  def blackHole(a: Any): Unit = ()

  @Test def waitBindingsShouldCompileAndLink(): Unit = {
    // zero initialized placeholder
    val wstatus = stackalloc[CInt](1.toUSize)

    // idtype_t
    blackHole(P_ALL)
    blackHole(P_PGID)
    blackHole(P_PID)

// Symbolic constants, roughly in POSIX declaration order

    // "constants" for waitpid()

    blackHole(WCONTINUED)    // XSI
    blackHole(WNOHANG)
    blackHole(WUNTRACED)

    // "constants" for waitid() options
    blackHole(WEXITED)
    blackHole(WNOWAIT)
    blackHole(WSTOPPED)

// POSIX "Macros"
    WEXITSTATUS(!wstatus)
    WIFCONTINUED(!wstatus)    // XSI
    WIFEXITED(!wstatus)
    WIFSIGNALED(!wstatus)
    WIFSTOPPED(!wstatus)
    WSTOPSIG(!wstatus)
    WTERMSIG(!wstatus)
  }

}
