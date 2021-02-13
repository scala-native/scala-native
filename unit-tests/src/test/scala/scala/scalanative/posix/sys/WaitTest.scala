package scala.scalanative.posix.sys

import scala.scalanative.posix.sys.wait._
import scala.scalanative.posix.unistd.vfork
import scala.scalanative.unsafe._

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.libc.errno.{errno, errno_=}
import scala.scalanative.libc.string.strerror

class WaitTest {
  @Test def waitWaitsChildExit(): Unit = {
    val statLoc = stackalloc[CInt](1)

    def blackHole(a: Any): Unit = ()

    // these are just here to test the bindings compile and link
    blackHole(WCONTINUED)
    blackHole(WNOHANG)
    blackHole(WUNTRACED)

    WIFSIGNALED(!statLoc)
    WTERMSIG(!statLoc)
    WIFSTOPPED(!statLoc)
    WSTOPSIG(!statLoc)
    WIFCONTINUED(!statLoc)

    //done

    errno = 0
    val pid = vfork()
    if (pid == 0) {
      sys.exit(0)
    } else if (pid < 0) {
      val err = errno
      val str = fromCString(strerror(err))
      assert(false, s"forking failed: $err - $str")

    } else {
      val childPid = wait(statLoc)

      assert(
        pid == childPid,
        "child that was observed exiting was not the child spawned by fork")
      assert(WIFEXITED(!statLoc) == 1, "child exited abnormally")

    }

    errno = 0
    val pid2 = vfork()
    if (pid2 == 0) {
      sys.exit(1)
    } else if (pid < 0) {
      val err = errno
      val str = fromCString(strerror(err))
      assert(false, s"forking failed: $err - $str")
    } else {
      val childPid = waitpid(pid2, statLoc, 0)

      assert(
        pid2 == childPid,
        "child that was observed exiting was not the child spawned by fork")
      assert(WIFEXITED(!statLoc) == 1, "child exited abnormally")
      assert(WEXITSTATUS(!statLoc) == 1, "status doesn't match expected status")
    }
  }
}
