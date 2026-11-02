package scala.scalanative.posix

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{BeforeClass, Test}

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.timeOps.timespecOps
import scala.scalanative.unsafe._

object ExternRedirectTest {
  @BeforeClass def assumeSupported(): Unit =
    assumeFalse("POSIX time is not available on Windows", isWindows)
}

/** Each call below uses the extern name known to libraries compiled against
 *  0.5.12. A redirect defined using the bare C name would shadow the libc
 *  function, making the glue it calls recurse until the stack overflows.
 */
class ExternRedirectTest {

  @Test def clockGettime(): Unit = if (!isWindows) {
    val ts = stackalloc[time.timespec]()
    assertEquals(0, time._clock_gettime(time.CLOCK_REALTIME, ts))
    assertTrue(ts.tv_sec.toLong > 0L)
  }

  @Test def nanosleep(): Unit = if (!isWindows) {
    val ts = stackalloc[time.timespec]()
    ts.tv_sec = 0
    ts.tv_nsec = 1
    assertEquals(0, time._nanosleep(ts, null))
  }

  @Test def socket(): Unit = if (!isWindows) {
    val fd = sys.socket._socket(sys.socket.AF_INET, sys.socket.SOCK_STREAM, 0)
    assertTrue(s"socket failed: $fd", fd >= 0)
    assertEquals(0, unistd.close(fd))
  }

  @Test def sigaltstack(): Unit = if (!isWindows) {
    assertEquals(0, signal._sigaltstack(null, null))
  }
}
