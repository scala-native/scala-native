package org.scalanative.testsuite.posixlib
package sys

import scalanative.unsafe.*
import scalanative.unsigned.*

import scalanative.posix.errno.errno
import scalanative.posix.netinet.in.*
import scalanative.posix.netinet.inOps.*
import scalanative.posix.sys.socket.*

import org.scalanative.testsuite.posixlib.sys.SocketTestHelpers.*

import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows.*
import scala.scalanative.windows.WinSocketApi.*
import scala.scalanative.windows.WinSocketApiExt.*
import scala.scalanative.windows.WinSocketApiOps.*
import scala.scalanative.windows.ErrorHandlingApi.*

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.BeforeClass

object UdpSocketTest {
  @BeforeClass
  def beforeClass(): Unit = {
    val isIPv4Available = hasLoopbackAddress(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
    assumeTrue("IPv4 UDP loopback is not available", isIPv4Available)
  }
}

class UdpSocketTest {

  @Test def sendtoRecvfrom(): Unit = Zone.acquire { implicit z =>
    if (isWindows) {
      WinSocketApiOps.init()
    }

    val (inSocket, outSocket, outAddr) = getUdpLoopbackSockets(AF_INET)

    try {
      val outData =
        """
          |Four Freedoms -
          |   Freedom of speech
          |   Freedom of worship
          |   Freedom from want
          |   Freedom from fear
          """.stripMargin

      val nBytesSent = sendto(
        outSocket,
        toCString(outData),
        outData.length.toUSize,
        0,
        outAddr,
        sizeof[sockaddr].toUInt
      )

      assertTrue(s"sendto failed errno: ${errno}\n", (nBytesSent >= 0))
      assertEquals("sendto", outData.size, nBytesSent.toInt)

      // If inSocket did not get data by timeout, it probably never will.
      pollReadyToRecv(inSocket, 30 * 1000) // assert fail on error or timeout

      /// Two tests using one inbound packet, save test duplication.

      // Provide extra room to allow detecting extra junk being sent.
      val maxInData = 2 * outData.length
      val inData: Ptr[Byte] = alloc[Byte](maxInData)

      // Test not fetching remote address. Exercise last two args as nulls.
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
      val srcAddr = alloc[sockaddr]()
      val srcAddrLen = alloc[socklen_t]()
      !srcAddrLen = sizeof[sockaddr].toUInt
      val nBytesRecvd =
        recvfrom(inSocket, inData, maxInData.toUInt, 0, srcAddr, srcAddrLen)

      checkIoResult(nBytesRecvd, "recvfrom_2")
      assertEquals("recvfrom_2 length", nBytesSent, nBytesRecvd)

      // Packet came from where we expected, and not Mars.
      assertEquals(
        "unexpected remote address",
        outAddr.asInstanceOf[Ptr[sockaddr_in]].sin_addr.s_addr,
        srcAddr.asInstanceOf[Ptr[sockaddr_in]].sin_addr.s_addr
      )

      assertEquals("inData NUL termination", 0, inData(nBytesRecvd.toUSize))

      assertEquals("recvfrom content", outData, fromCString(inData))
      // Contents are good.

    } finally {
      SocketTestHelpers.closeSocket(inSocket)
      SocketTestHelpers.closeSocket(outSocket)
    }
  }
}
