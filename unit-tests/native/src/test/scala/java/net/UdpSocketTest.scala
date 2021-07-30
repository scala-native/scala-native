package java.net

import scalanative.posix.arpa.inet._
import scalanative.posix.fcntl
import scalanative.posix.fcntl.{F_SETFL, O_NONBLOCK}
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.socketOps._
import scalanative.posix.unistd.close
import scalanative.unsafe._
import scalanative.unsigned._

import org.junit.Test
import org.junit.Assert._

class UdpSocketTest {
  // All tests in this class assume that an IPv4 network is up & running.

  @Test def sendtoRecvfrom(): Unit = Zone { implicit z =>
    val localhost = c"127.0.0.1"
    val localhostInetAddr = inet_addr(localhost)

    val inSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
    assertNotEquals("socket_1", -1, inSocket)

    try {
      val inAddr = alloc[sockaddr]
      val inAddrInPtr = inAddr.asInstanceOf[Ptr[sockaddr_in]]

      inAddrInPtr.sin_family = AF_INET.toUShort
      inAddrInPtr.sin_addr.s_addr = localhostInetAddr
      // inAddrInPtr.sin_port is already the desired 0; "find a free port".

      val fcntlStatus = fcntl.fcntl(inSocket, F_SETFL, O_NONBLOCK)
      assertNotEquals("fcntl", -1, fcntlStatus)

      // Get port for sendto() to use.
      val bindStatus = bind(inSocket, inAddr, sizeof[sockaddr].toUInt)
      assertNotEquals("bind", -1, bindStatus)

      val inAddrInfo = alloc[sockaddr]
      val gsnAddrLen = alloc[socklen_t]
      !gsnAddrLen = sizeof[sockaddr].toUInt

      val gsnStatus = getsockname(inSocket, inAddrInfo, gsnAddrLen)
      assertNotEquals("getsockname", -1, gsnStatus)

      // Now use port.
      val outSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
      assertNotEquals("socket_2", -1, outSocket)

      try {
        val outAddr = alloc[sockaddr]
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
        // If this race condition becomes bothersome, a Thead.sleep() can
        // be inserted here.

        /// Two tests using one inbound packet, save test duplication.

        // Provide extra room to allow detecting extra junk being sent.
        val maxInData = 2 * outData.length
        val inData = alloc[Byte](maxInData)

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

        // Friendlier code here and after the next recvfrom() would loop
        // on partial reads rather than fail.
        // Punt to a future generation. Since this is loopback and
        // writes are small, if any bytes are ready, all should be.
        assertEquals("recvfrom_1 length", nBytesSent, nBytesPeekedAt)

        // Test retrieving remote address.
        val srcAddr = alloc[sockaddr]
        val srcAddrLen = alloc[socklen_t]
        !srcAddrLen = sizeof[sockaddr].toUInt
        val nBytesRecvd =
          recvfrom(inSocket, inData, maxInData.toUInt, 0, srcAddr, srcAddrLen)

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
        close(outSocket)
      }

    } finally {
      close(inSocket)
    }
  }
}
