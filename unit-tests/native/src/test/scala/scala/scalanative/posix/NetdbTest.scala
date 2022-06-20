package scala.scalanative.posix

import org.scalanative.testsuite.utils.Platform

import scalanative.unsafe._

import scalanative.meta.LinktimeInfo.isWindows

import scalanative.libc.string.strlen

import scalanative.posix.netdb._
import scalanative.posix.netdbOps._
import scalanative.posix.sys.socket.{AF_INET, SOCK_DGRAM}

import org.junit.Test
import org.junit.Assert._

class NetdbTest {

  @Test def gai_strerrorMustTranslateErrorCodes(): Unit = Zone { implicit z =>
    if (!isWindows) {
      val resultPtr = stackalloc[Ptr[addrinfo]]()

      // Workaround Issue #2314 - getaddrinfo fails with null hints.
      val hints = stackalloc[addrinfo]()
      hints.ai_family = AF_INET
      hints.ai_socktype = SOCK_DGRAM

      // Calling with no host & no service should cause gai error EAI_NONAME.
      val status = getaddrinfo(null, null, hints, resultPtr);

      assertNotEquals(s"Expected getaddrinfo call to fail,", 0, status)

      assertEquals(s"Unexpected getaddrinfo failure,", EAI_NONAME, status)

      val gaiFailureMsg = gai_strerror(status)

      assertNotNull(s"gai_strerror returned NULL/null,", status)

      /* Check that translated text exists but not for the exact text.
       * The text may vary by operating system and C locale.
       * Such translations from integers to varying text is gai_strerror()'s
       * very reason for being.
       *
       * One common linux translation of EAI_NONAME is:
       * "Name or service not known".
       */

      assertNotEquals(
        s"gai_strerror returned zero length string,",
        0,
        strlen(gaiFailureMsg)
      )
    }
  }

}
