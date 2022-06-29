package scala.scalanative.posix
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.errno
import scalanative.libc.string.{memcmp, strerror}

import scalanative.posix.arpa.inet._
import scalanative.posix.fcntl
import scalanative.posix.fcntl.{F_SETFL, O_NONBLOCK}
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.SocketTestHelpers._

import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.runtime.Platform
import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows.WinSocketApiOps
import scala.scalanative.windows.ErrorHandlingApi._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Before

class Udp6SocketTest {

  val isIPv6Available = if (isWindows) {
    // A Windows expert could probably made this test work.
    // There is Windows code in this file but, for want of skill,
    // it has not been exercised.

    false

  } else if (Platform.isMac()) {
    // Tests are failing on GitHub CI
    // (errno 47: Address family not supported by protocol).
    // This is caused by SN not properly handling sin6_len on BSD.
    // See SN Issue #2626. Disable testing on IPv6 until that Issue is fixed.

    false

  } else {
//    hasLoopbackAddress(AF_INET6, SOCK_DGRAM, IPPROTO_UDP)
    true
  }

  @Before
  def before(): Unit = {
    assumeTrue("IPv6 UDP loopback is not available", isIPv6Available)
  }

  // For some unknown reason inlining content of this method leads to failures
  // on Unix, probably due to bug in linktime conditions.
  private def setSocketBlocking(socket: CInt): Unit = {
    if (isWindows) {
      val mode = stackalloc[CInt]()
      !mode = 1
      assertNotEquals(
        "iotctl setBLocking",
        -1,
        ioctlSocket(socket.toPtr[Byte], FIONBIO, mode)
      )
    } else {
      assertNotEquals(
        s"fcntl set blocking",
        -1,
        fcntl.fcntl(socket, F_SETFL, O_NONBLOCK)
      )
    }
  }

  private def createAndCheckUdpSocket(): CInt = {
    if (isWindows) {
      val socket = WSASocketW(
        addressFamily = AF_INET6,
        socketType = SOCK_DGRAM,
        protocol = IPPROTO_UDP,
        protocolInfo = null,
        group = 0.toUInt,
        flags = WSA_FLAG_OVERLAPPED
      )
      assertNotEquals("socket create", InvalidSocket, socket)
      socket.toInt
    } else {
      val sock = sys.socket.socket(AF_INET6, SOCK_DGRAM, IPPROTO_UDP)
      assertNotEquals("socket create", -1, sock)
      sock
    }
  }

  private def closeSocket(socket: CInt): Unit = {
    if (isWindows) WinSocketApi.closeSocket(socket.toPtr[Byte])
    else unistd.close(socket)
  }

  private def checkRecvfromResult(v: CSSize, label: String): Unit = {
    if (v.toInt < 0) {
      val reason =
        if (isWindows) ErrorHandlingApiOps.errorMessage(GetLastError())
        else fromCString(strerror(errno.errno))
      fail(s"$label failed - $reason")
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

    assertNotEquals(s"inet_ntop failed errno: ${errno.errno}", result, null)

    fromCString(dst)
  }

  @Test def sendtoRecvfrom(): Unit = Zone { implicit z =>
    if (isWindows) {
      WinSocketApiOps.init()
    }

    val in6SockAddr = alloc[sockaddr_in6]()
    in6SockAddr.sin6_family = AF_INET6.toUShort

    // Scala Native currently implements neither inaddr_loopback
    // nor IN6ADDR_LOOPBACK_INIT. When they become available,
    // this code can be simplified by using the former instead
    // of the inet_pton(code below). All things in due time.
    //
    // in6SockAddr.sin6_addr = in6addr_loopback

    // sin6_port is already the desired 0; "find a free port".
    // all other fields already 0.

    val localhost = c"::1"
    val ptonStatus = inet_pton(
      AF_INET6,
      localhost,
      in6SockAddr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]
    )

    assertEquals(s"inet_pton failed errno: ${errno.errno}", ptonStatus, 1)

    val inSocket: CInt = createAndCheckUdpSocket()

    try {
      setSocketBlocking(inSocket)

      // Get port for sendto() to use.
      val bindStatus = bind(
        inSocket,
        in6SockAddr
          .asInstanceOf[Ptr[sockaddr]],
        sizeof[sockaddr_in6].toUInt
      )

      assertNotEquals(s"bind failed,  errno: ${errno.errno}", -1, bindStatus)

      val inAddrInfo = alloc[sockaddr_in6]()
      val gsnAddrLen = alloc[socklen_t]()
      !gsnAddrLen = sizeof[sockaddr_in6].toUInt

      val gsnStatus = getsockname(
        inSocket,
        inAddrInfo.asInstanceOf[Ptr[sockaddr]],
        gsnAddrLen
      )

      assertNotEquals("getsockname failed errno: ${errno.errno}", -1, gsnStatus)

      // Now use port.
      val outSocket = createAndCheckUdpSocket()

      try {
        val out6Addr = alloc[sockaddr_in6]()
        out6Addr.sin6_family = AF_INET6.toUShort
        out6Addr.sin6_port = inAddrInfo.sin6_port
        out6Addr.sin6_addr = in6SockAddr.sin6_addr

        // all other fields in out6Addr have been cleared by alloc[].

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
          outData.length.toULong,
          0,
          out6Addr.asInstanceOf[Ptr[sockaddr]],
          sizeof[sockaddr_in6].toUInt
        )

        assertTrue(s"sendto failed errno: ${errno.errno}\n", (nBytesSent >= 0))
        assertEquals("sendto length", outData.size, nBytesSent)

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

        checkRecvfromResult(nBytesPeekedAt, "recvfrom_1")

        // Friendlier code here and after the next recvfrom() would loop
        // on partial reads rather than fail.
        // Punt to a future generation. Since this is loopback and
        // writes are small, if any bytes are ready, all should be.
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

        checkRecvfromResult(nBytesRecvd, "recvfrom_2")
        assertEquals("recvfrom_2 length", nBytesSent, nBytesRecvd)

        // Did packet came from where we expected, and not from Mars?

        val addrsMatch = {
          0 == memcmp(
            in6SockAddr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]],
            srcAddr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]],
            sizeof[in6_addr]
          )
        }

        if (!addrsMatch) {
          val expectedNtop = formatIn6addr(in6SockAddr.sin6_addr)
          val gotNtop = formatIn6addr(srcAddr.sin6_addr)

          val msg =
            s"expected remote address: '${expectedNtop}' got: '${gotNtop}'"
          fail(msg)
        }

        assertEquals("inData is not NUL terminated", 0, inData(nBytesRecvd))

        // Are received contents good?
        assertEquals("recvfrom content", outData, fromCString(inData))

      } finally {
        closeSocket(outSocket)
      }
    } finally {
      closeSocket(inSocket)
    }
  }
}
