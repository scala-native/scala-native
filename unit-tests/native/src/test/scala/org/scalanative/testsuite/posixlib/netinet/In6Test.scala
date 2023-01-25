package org.scalanative.testsuite.posixlib
package netinet

import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._

import scalanative.posix.inttypes._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.socketOps._

import scalanative.unsafe._
import scalanative.unsigned._

import org.junit.Test
import org.junit.Assert._

class In6Test {

  @Test def testSetSin6Family(): Unit = Zone { implicit z =>
    /* Setting the sin6_family field should work and should not clobber the
     * sin6_len field, if such exists on executing OS.
     */
    val in6SockAddr = alloc[sockaddr_in6]()

    val expectedSin6Len = in6SockAddr.sin6_len

    val expectedSin6Family = AF_INET6.toUShort
    in6SockAddr.sin6_family = expectedSin6Family

    assertEquals(
      "unexpected sin6_len",
      in6SockAddr.sin6_len,
      expectedSin6Len
    )

    assertEquals(
      "unexpected sin6_family",
      in6SockAddr.sin6_family,
      expectedSin6Family
    )
  }

  @Test def testSetSin6Len(): Unit = Zone { implicit z =>
    /* Setting the sin6_len field should work and should not clobber the
     * sin6_family field.
     */
    val in6SockAddr = alloc[sockaddr_in6]()

    val expectedSin6Family = AF_INET6.toUShort

    val suppliedSin6Len = 77.toUByte
    val expectedSin6Len: uint8_t = if (useSinXLen) {
      suppliedSin6Len
    } else {
      sizeof[sockaddr_in6].toUByte // field is synthesized on non-BSD
    }

    in6SockAddr.sin6_family = expectedSin6Family
    in6SockAddr.sin6_len = suppliedSin6Len

    assertEquals(
      "unexpected sin6_len",
      in6SockAddr.sin6_len,
      expectedSin6Len
    )

    assertEquals(
      "unexpected sin6_family",
      in6SockAddr.sin6_family,
      expectedSin6Family
    )
  }
}
