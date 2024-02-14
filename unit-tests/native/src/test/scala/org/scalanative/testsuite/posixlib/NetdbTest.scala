package org.scalanative.testsuite.posixlib

import org.scalanative.testsuite.utils.Platform
import scalanative.meta.LinktimeInfo.isWindows

import scala.annotation.tailrec

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.string.{strlen, strncmp}

import scalanative.posix.netdb._
import scalanative.posix.netdbOps._
import scalanative.posix.sys.socket.{AF_INET, AF_UNSPEC, SOCK_DGRAM}

import org.junit.Test
import org.junit.Assert._

class NetdbTest {

  @tailrec
  private def compareAddrinfoLists(
      ai1Ptr: Ptr[addrinfo],
      ai2Ptr: Ptr[addrinfo]
  ): Unit = {

    if (((ai1Ptr == null) || (ai2Ptr == null))) {
      assertEquals("unmatched addrinfo null pointers,", ai1Ptr, ai2Ptr)
    } else {
      assertEquals(
        s"unmatched field: ai_flags, ",
        ai1Ptr.ai_flags,
        ai2Ptr.ai_flags
      )

      assertEquals(
        s"unmatched field: ai_family, ",
        ai1Ptr.ai_family,
        ai2Ptr.ai_family
      )

      assertEquals(
        s"unmatched field: ai_socktype, ",
        ai1Ptr.ai_socktype,
        ai2Ptr.ai_socktype
      )

      assertEquals(
        s"unmatched field: ai_protocol, ",
        ai1Ptr.ai_protocol,
        ai2Ptr.ai_protocol
      )

      assertEquals(
        s"unmatched field: ai_addrlen, ",
        ai1Ptr.ai_addrlen,
        ai2Ptr.ai_addrlen
      )

      if (((ai1Ptr.ai_canonname == null) || (ai2Ptr.ai_canonname == null))) {
        assertEquals("ai_canonname,", ai1Ptr.ai_canonname, ai2Ptr.ai_canonname)
      } else {

        val cmp = strncmp(
          ai1Ptr.ai_canonname,
          ai2Ptr.ai_canonname,
          // 255 is largest FQDN (fully qualified domain name) allowed.
          255.toUInt
        )

        if (cmp != 0) {
          val ai1Name = fromCString(ai1Ptr.ai_canonname)
          val ai2Name = fromCString(ai2Ptr.ai_canonname)

          assertEquals(s"ai_canonname: '${ai1Name}' != '${ai2Name}'", 0, cmp)
        }
      }

      compareAddrinfoLists(
        ai1Ptr.ai_next.asInstanceOf[Ptr[addrinfo]],
        ai2Ptr.ai_next.asInstanceOf[Ptr[addrinfo]]
      )
    }
  }

  private def callGetaddrinfo(host: CString, hints: Ptr[addrinfo])(implicit
      z: Zone
  ): Ptr[addrinfo] = {

    val resultPtr = stackalloc[Ptr[addrinfo]]()

    val status = getaddrinfo(host, null, hints, resultPtr);

    assertEquals(
      s"getaddrinfo failed: ${fromCString(gai_strerror(status))}",
      0,
      status
    )

    assertNotNull("getaddrinfo returned empty list", !resultPtr)

    !resultPtr
  }

  @Test def gai_strerrorMustTranslateErrorCodes(): Unit = Zone.acquire {
    implicit z =>
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

  @Test def getaddrinfoWithNullHintsShouldFollowPosixSpec(): Unit =
    Zone.acquire { implicit z =>
      if (!isWindows) {

        val host = c"127.0.0.1"

        val nullHintsAiPtr = callGetaddrinfo(host, null)

        try {

          /* Calling getaddrinfo with these hints and with null hints
           * should return identical results.
           *
           * In particular, ai_flags are left with the 0 as created
           * by stackalloc(). This is the value defined by Posix.
           * GNU defines a different and possibly more useful value.
           *
           * The provided hints are from the Posix specification of the
           * equivalent of calling getaddrinfo null hints. The two
           * results should match.
           */

          val hints = stackalloc[addrinfo]()
          hints.ai_family = AF_UNSPEC

          val defaultHintsAiPtr = callGetaddrinfo(host, hints)

          try {
            assertEquals(
              s"unexpected ai_family,",
              AF_INET,
              nullHintsAiPtr.ai_family
            )

            compareAddrinfoLists(nullHintsAiPtr, defaultHintsAiPtr)

          } finally {
            freeaddrinfo(defaultHintsAiPtr)
          }
        } finally {
          freeaddrinfo(nullHintsAiPtr)
        }
      }
    }

}
