package org.scalanative.testsuite.posixlib
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.string.memcmp

import scalanative.posix.arpa.inet._
import scalanative.posix.errno.errno
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.sys.socket._

import org.scalanative.testsuite.posixlib.sys.SocketTestHelpers._

import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows.WinSocketApiOps
import scala.scalanative.windows.ErrorHandlingApi._

import org.scalanative.testsuite.utils.Platform

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Before

class Udp6SocketTest {
  @Before
  def before(): Unit = {
    assumeTrue(
      "IPv6 UDP loopback is not available",
      hasLoopbackAddress(AF_INET6, SOCK_DGRAM, IPPROTO_UDP)
    )

    /* Scala Native Continuous Integration linux-arm64 multiarch runs
     * fail, where they succeed on other test configurations.
     *
     * The failing tests use qemu emulator. Test should succeed on real
     * hardware. Disable everywhere because GITHUB_* environment variables
     * are not passed to qemu.
     *
     * The failing tests report that an IPv6 loopback address is available
     * but fail when these tests attempt to bind() to it. Private debugging
     * indicates that the address passed to bind() should be good.
     * Probably a problem with qemu configuration & IPv6. Which is why
     * we run test matrices.
     */

    if (Platform.isLinux) {
      // IPv6 appears to not be configured on CI Docker/qemu
      assumeFalse(
        "IPv6 UDP loopback is not available on linux-arm64 CI",
        Platform.isArm64
      )
      assumeFalse(
        "IPv6 UDP loopback is not available on linux-x86 CI",
        Platform.is32BitPlatform
      )
    }
  }

  private def formatIn6addr(addr: in6_addr): String = Zone { implicit z =>
    val dstSize = INET6_ADDRSTRLEN + 1
    val dst = alloc[Byte](dstSize)

    val result = inet_ntop(
      AF_INET6,
      addr.at1.at(0).asInstanceOf[Ptr[Byte]],
      dst,
      dstSize.toUInt
    )

    assertNotEquals(s"inet_ntop failed errno: ${errno}", result, null)

    fromCString(dst)
  }

  @Test def sendtoRecvfrom(): Unit = Zone { implicit z =>
    if (isWindows) {
      WinSocketApiOps.init()
    }

    val (inSocket, outSocket, out6Addr) = getUdpLoopbackSockets(AF_INET6)

    try {
      val outData =
        """
          |"She moved through the fair" lyrics, Traditional, no copyright
          |   I dreamed it last night
          |   That my true love came in
          |   So softly she entered
          |   Her feet made no din
          |   She came close beside me
          |   And this she did say,
          |   "It will not be long, love
          |   Till our wedding day."
          """.stripMargin

      val nBytesSent = sendto(
        outSocket,
        toCString(outData),
        outData.length.toUSize,
        0,
        out6Addr.asInstanceOf[Ptr[sockaddr]],
        sizeof[sockaddr_in6].toUInt
      )

      assertTrue(s"sendto failed errno: ${errno}\n", (nBytesSent >= 0))
      assertEquals("sendto length", outData.size, nBytesSent.toInt)

      // If inSocket did not get data by timeout, it probably never will.
      pollReadyToRecv(inSocket, 30 * 1000) // assert fail on error or timeout

      /// Two tests using one inbound packet, save test duplication.

      // Provide extra room to allow detecting extra junk being sent.
      val maxInData = 2 * outData.length
      val inData: Ptr[Byte] = alloc[Byte](maxInData)

      // Test not fetching remote address. Exercise last two arguments.
      val nBytesPeekedAt =
        recvfrom(
          inSocket,
          inData,
          maxInData.toUInt,
          MSG_PEEK,
          null.asInstanceOf[Ptr[sockaddr]],
          null.asInstanceOf[Ptr[socklen_t]]
        )

      checkIoResult(nBytesPeekedAt, "recvfrom_1")

      // When sending a small UDP datagram, data will be sent in one shot.
      assertEquals("recvfrom_1 length", nBytesSent, nBytesPeekedAt)

      // Test retrieving remote address.
      val srcAddr = alloc[sockaddr_in6]()
      val srcAddrLen = alloc[socklen_t]()
      !srcAddrLen = sizeof[sockaddr_in6].toUInt

      val nBytesRecvd =
        recvfrom(
          inSocket,
          inData,
          maxInData.toUInt,
          0,
          srcAddr.asInstanceOf[Ptr[sockaddr]],
          srcAddrLen
        )

      checkIoResult(nBytesRecvd, "recvfrom_2")
      assertEquals("recvfrom_2 length", nBytesSent, nBytesRecvd)

      // Did packet came from where we expected, and not from Mars?

      val expectedAddr = out6Addr.asInstanceOf[Ptr[sockaddr_in6]]

      val addrsMatch = {
        0 == memcmp(
          expectedAddr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]],
          srcAddr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]],
          sizeof[in6_addr]
        )
      }

      if (!addrsMatch) {
        val expectedNtop = formatIn6addr(expectedAddr.sin6_addr)
        val gotNtop = formatIn6addr(srcAddr.sin6_addr)

        val msg =
          s"expected remote address: '${expectedNtop}' got: '${gotNtop}'"
        fail(msg)
      }

      assertEquals("inData is not NUL terminated", 0, inData(nBytesRecvd))

      // Are received contents good?
      assertEquals("recvfrom content", outData, fromCString(inData))
    } finally {
      SocketTestHelpers.closeSocket(inSocket)
      SocketTestHelpers.closeSocket(outSocket)
    }
  }
}
