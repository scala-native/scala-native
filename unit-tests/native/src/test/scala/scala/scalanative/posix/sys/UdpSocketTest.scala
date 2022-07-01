package scala.scalanative.posix
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.errno
import scalanative.libc.string.strerror

import scalanative.posix.arpa.inet.inet_addr
import scalanative.posix.fcntl.{F_SETFL, O_NONBLOCK}
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.SocketTestHelpers._
import scalanative.posix.unistd

import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows.WinSocketApiOps._
import scala.scalanative.windows.ErrorHandlingApi._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Before

class UdpSocketTest {

  val isIPv4Available = hasLoopbackAddress(AF_INET, SOCK_DGRAM, IPPROTO_UDP)

  @Before
  def before(): Unit = {
    assumeTrue("IPv4 UDP loopback is not available", isIPv4Available)
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
        addressFamily = AF_INET,
        socketType = SOCK_DGRAM,
        protocol = IPPROTO_UDP,
        protocolInfo = null,
        group = 0.toUInt,
        flags = WSA_FLAG_OVERLAPPED
      )
      assertNotEquals("socket create", InvalidSocket, socket)
      socket.toInt
    } else {
      val sock = sys.socket.socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
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

  // Make available to Udp6SocketTest.scala
  private[sys] def pollReadyToRecv(fd: CInt, timeout: CInt) = {
    // timeout is in milliseconds

    if (isWindows) {
      val fds = stackalloc[WSAPollFd](1.toUSize)
      fds.socket = fd.toPtr[Byte]
      fds.events = WinSocketApiExt.POLLIN

      val ret = WSAPoll(fds, 1.toUInt, timeout)

      if (ret == 0) {
        fail(s"poll timed out after ${timeout} milliseconds")
      } else if (ret < 0) {
        val reason = ErrorHandlingApiOps.errorMessage(GetLastError())
        fail(s"poll for input failed - $reason")
      }
    } else {
      val fds = stackalloc[struct_pollfd](1.toUSize)
      (fds + 0).fd = fd
      (fds + 0).events = pollEvents.POLLIN | pollEvents.POLLRDNORM

      errno.errno = 0
      // poll() sounds like a nasty busy wait loop, but is event driven in kernel

      val ret = poll(fds, 1.toUInt, timeout)

      if (ret == 0) {
        fail(s"poll timed out after ${timeout} milliseconds")
      } else if (ret < 0) {
        val reason = fromCString(strerror(errno.errno))
        fail(s"poll for input failed - $reason")
      }
      // else good to go
    }
  }

  @Test def sendtoRecvfrom(): Unit = Zone { implicit z =>
    if (isWindows) {
      WinSocketApiOps.init()
    }
    val localhost = c"127.0.0.1"
    val localhostInetAddr = inet_addr(localhost)

    val inSocket: CInt = createAndCheckUdpSocket()

    try {
      val inAddr = alloc[sockaddr]()
      val inAddrInPtr = inAddr.asInstanceOf[Ptr[sockaddr_in]]

      inAddrInPtr.sin_family = AF_INET.toUShort
      inAddrInPtr.sin_addr.s_addr = localhostInetAddr
      // inAddrInPtr.sin_port is already the desired 0; "find a free port".

      setSocketBlocking(inSocket)

      // Get port for sendto() to use.
      val bindStatus = bind(inSocket, inAddr, sizeof[sockaddr].toUInt)
      assertNotEquals(s"bind failed,  errno: ${errno.errno}", -1, bindStatus)

      val inAddrInfo = alloc[sockaddr]()
      val gsnAddrLen = alloc[socklen_t]()
      !gsnAddrLen = sizeof[sockaddr].toUInt

      val gsnStatus = getsockname(inSocket, inAddrInfo, gsnAddrLen)
      assertNotEquals("getsockname", -1, gsnStatus)

      // Now use port.
      val outSocket = createAndCheckUdpSocket()

      try {
        val outAddr = alloc[sockaddr]()
        val outAddrInPtr = outAddr.asInstanceOf[Ptr[sockaddr_in]]
        outAddrInPtr.sin_family = AF_INET.toUShort
        outAddrInPtr.sin_addr.s_addr = localhostInetAddr
        outAddrInPtr.sin_port =
          inAddrInfo.asInstanceOf[Ptr[sockaddr_in]].sin_port

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

        assertTrue(s"sendto failed errno: ${errno.errno}\n", (nBytesSent >= 0))
        assertEquals("sendto", outData.size, nBytesSent)

        // If inSocket did not get data by timeout, it probably never will.
        pollReadyToRecv(inSocket, 30 * 1000) // assert fail on error or timeout

        /// Two tests using one inbound packet, save test duplication.

        // Provide extra room to allow detecting extra junk being sent.
        val maxInData = 2 * outData.length
        val inData: Ptr[Byte] = alloc[Byte](maxInData.toUSize)

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
        checkRecvfromResult(nBytesPeekedAt, "recvfrom_1")

        // Friendlier code here and after the next recvfrom() would loop
        // on partial reads rather than fail.
        // Punt to a future generation. Since this is loopback and
        // writes are small, if any bytes are ready, all should be.
        assertEquals("recvfrom_1 length", nBytesSent, nBytesPeekedAt)

        // Test retrieving remote address.
        val srcAddr = alloc[sockaddr]()
        val srcAddrLen = alloc[socklen_t]()
        !srcAddrLen = sizeof[sockaddr].toUInt
        val nBytesRecvd =
          recvfrom(inSocket, inData, maxInData.toUInt, 0, srcAddr, srcAddrLen)

        checkRecvfromResult(nBytesRecvd, "recvfrom_2")
        assertEquals("recvfrom_2 length", nBytesSent, nBytesRecvd)

        // Packet came from where we expected, and not Mars.
        assertEquals(
          "unexpected remote address",
          localhostInetAddr,
          srcAddr.asInstanceOf[Ptr[sockaddr_in]].sin_addr.s_addr
        )

        assertEquals("inData NUL termination", 0, inData(nBytesRecvd.toUSize))

        // Contents are good.
        assertEquals("recvfrom content", outData, fromCString(inData))
      } finally {
        closeSocket(outSocket)
      }

    } finally {
      closeSocket(inSocket)
    }
  }
}
