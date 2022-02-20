package java.net

import scalanative.posix.arpa.inet._
import scalanative.posix.fcntl
import scalanative.posix.fcntl.{F_SETFL, O_NONBLOCK}
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.socketOps._
import scalanative.posix.unistd
import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows.WinSocketApiOps
import scala.scalanative.windows.ErrorHandlingApi._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import scala.scalanative.libc.errno
import scala.scalanative.libc.string.strerror

class UdpSocketTest {
  // All tests in this class assume that an IPv4 network is up & running.

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
      val sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
      assertNotEquals("socket create", -1, sock)
      sock
    }
  }

  private def closeSocket(socket: CInt): Unit = {
    if (isWindows) WinSocketApi.closeSocket(socket.toPtr[Byte])
    else unistd.close(socket)
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
      assertNotEquals("bind", -1, bindStatus)

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
          outData.length.toULong,
          0,
          outAddr,
          sizeof[sockaddr].toUInt
        )
        assertEquals("sendto", outData.size, nBytesSent)

        // There is a "pick your poison" design choice here.
        // inSocket is set O_NONBLOCK to eliminate the possibility
        // that a bad sendto() or readfrom() implemenation would hang
        // for a long time.
        //
        // This introduces the theoretical possiblity the sendto() above
        // does not complete before recvfrom() looks for data, causing
        // failure. Since this is send/recv pair is explicitly loopback,
        // that is highly unlikely.
        //
        /// Two tests using one inbound packet, save test duplication.

        // Provide extra room to allow detecting extra junk being sent.
        val maxInData = 2 * outData.length
        val inData: Ptr[Byte] = alloc[Byte](maxInData)

        // Try to prevent spourious race conditions
        Thread.sleep(100)

        def checkRecvfromResult(v: CSSize, label: String): Unit = {
          if (v.toInt < 0) {
            val reason =
              if (isWindows) ErrorHandlingApiOps.errorMessage(GetLastError())
              else fromCString(strerror(errno.errno))
            fail(s"$label failed - $reason")
          }
        }

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

        assertEquals("inData NUL termination", 0, inData(nBytesRecvd))

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
