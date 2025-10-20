package org.scalanative.testsuite.posixlib
package netinet

import scalanative.posix.netinet.in.*
import scalanative.posix.netinet.inOps.*

import scalanative.posix.inttypes.*
import scalanative.posix.sys.socket.*
import scalanative.posix.sys.socketOps.*

import scalanative.unsafe.*
import scalanative.unsigned.*

import org.junit.Test
import org.junit.Assert.*

class InTest {

  @Test def testSetSinFamily(): Unit = Zone.acquire { implicit z =>
    /* Setting the sin_family field should work and should not clobber the
     * sin_len field, if such exists on executing OS.
     */
    val in4SockAddr = alloc[sockaddr_in]()

    val expectedSinLen = in4SockAddr.sin_len

    val expectedSinFamily = AF_INET.toUShort
    in4SockAddr.sin_family = expectedSinFamily

    assertEquals(
      "unexpected sin_len",
      in4SockAddr.sin_len,
      expectedSinLen
    )

    assertEquals(
      "unexpected sin_family",
      in4SockAddr.sin_family,
      expectedSinFamily
    )
  }

  @Test def testSetSinLen(): Unit = Zone.acquire { implicit z =>
    /* Setting the sin_len field should work and should not clobber the
     * sin_family field.
     */
    val in4SockAddr = alloc[sockaddr_in]()

    val expectedSinFamily = AF_INET.toUShort

    val suppliedSinLen = 66.toUByte
    val expectedSinLen: uint8_t = if (useSinXLen) {
      suppliedSinLen
    } else {
      sizeof[sockaddr_in].toUByte // field is synthesized on non-BSD
    }

    in4SockAddr.sin_family = expectedSinFamily
    in4SockAddr.sin_len = suppliedSinLen

    assertEquals(
      "unexpected sin_len",
      in4SockAddr.sin_len,
      expectedSinLen
    )

    assertEquals(
      "unexpected sin_family",
      in4SockAddr.sin_family,
      expectedSinFamily
    )
  }
}
