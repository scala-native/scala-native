package org.scalanative.testsuite.posixlib
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.string.memcmp

import scalanative.posix.arpa.inet.inet_addr
import scalanative.posix.errno.errno
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.sys.socket._
import scalanative.posix.sys.socketOps._
import scalanative.posix.time._
import scalanative.posix.sys.time.timeval
import scalanative.posix.sys.timeOps._
import scalanative.posix.sys.uio._
import scalanative.posix.sys.uioOps._

import scalanative.meta.LinktimeInfo.isWindows

import org.scalanative.testsuite.posixlib.sys.SocketTestHelpers._
import org.scalanative.testsuite.utils.Platform

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Before

/** Exercise the POSIX socket.h sendmsg and recvmg routines.
 *
 *  Those functions do not exist on Windows.
 */
class MsgIoSocketTest {

  @Before
  def before(): Unit = {
    val isIPv4Available = hasLoopbackAddress(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
    assumeTrue("IPv4 UDP loopback is not available", isIPv4Available)

    assumeTrue(
      "POSIX sendmsg & recvmsg are not available on Windows",
      !isWindows
    )
  }

  /* Percy Bysshe Shelly - Ozymandias - 1818
   * This poem is in the public domain.
   *
   * URL: https://en.wikisource.org/wiki/Ozymandias_(Shelley)
   * Thank you, wikisource.
   */

  private final val poemHeader =
    """	  |
	  |Percy Bysshe Shelley, 1818 -- Public Domain
	  |
	  |OZYMANDIAS of EGYPT
	  |
	  |""".stripMargin

  private final val chunk1 =
    """	  |I met a traveller from an antique land
	  |Who said:—Two vast and trunkless legs of stone
	  |Stand in the desert. Near them on the sand,
	  |Half sunk, a shatter'd visage lies, whose frown
	  |And wrinkled lip and sneer of cold command
	  |""".stripMargin

  private final val chunk2 =
    """	  |Tell that its sculptor well those passions read
	  |Which yet survive, stamp'd on these lifeless things,
	  |The hand that mock'd them and the heart that fed.
	  |And on the pedestal these words appear:
	  |"My name is Ozymandias, king of kings:
	  |
	  |""".stripMargin

  private final val chunk3 =
    """	  |Look on my works, ye mighty, and despair!"
	  |Nothing beside remains: round the decay
	  |Of that colossal wreck, boundless and bare,
	  |The lone and level sands stretch far away.
	  |
	  |""".stripMargin

  @Test def msgIoShouldScatterGather(): Unit = if (!isWindows) {
    // sendmsg() should gather, recvmsg() should scatter, the twain shall meet
    Zone { implicit z =>
      val (inSocket, outSocket, dstAddr) = getUdpLoopbackSockets(AF_INET)

      try {
        val outData0 = poemHeader + chunk1 + chunk2
        val outData1 = chunk3

        val nOutIovs = 2
        val outVec = alloc[iovec](nOutIovs)

        // outData created with only 1 byte UTF-8 chars, so length method OK.

        outVec(0).iov_base = toCString(outData0)
        outVec(0).iov_len = outData0.length.toUSize

        outVec(1).iov_base = toCString(outData1)
        outVec(1).iov_len = outData1.length.toUSize

        val outMsgHdr = alloc[msghdr]()
        outMsgHdr.msg_name = dstAddr.asInstanceOf[Ptr[Byte]]
        outMsgHdr.msg_namelen = sizeof[sockaddr_in].toUInt
        outMsgHdr.msg_iov = outVec
        outMsgHdr.msg_iovlen = nOutIovs

        val nBytesSent = sendmsg(outSocket, outMsgHdr, 0)

        checkIoResult(nBytesSent, "sendmsg_1")

        // When sending a small UDP datagram, data will be sent in one shot.
        val expectedBytesSent = outData0.size + outData1.size
        assertEquals("sendmsg_2", expectedBytesSent, nBytesSent.toInt)

        // If inSocket did not get data by timeout, it probably never will.
        pollReadyToRecv(inSocket, 30 * 1000) // assert fail on error or timeout

        // Design Notes: Scatter read at least 2 buffers.

        // To mix things up, read data in reverse order of how it was sent.

        val inData0Size = outData1.size
        val inData0: Ptr[Byte] = alloc[Byte](inData0Size.toInt)

        val inData1Size = outData0.size
        val inData1: Ptr[Byte] = alloc[Byte](inData1Size.toInt)

        val nInIovs = 2
        val inVec = alloc[iovec](nInIovs)

        inVec(0).iov_base = inData0
        inVec(0).iov_len = inData0Size.toUInt

        inVec(1).iov_base = inData1
        inVec(1).iov_len = inData1Size.toUInt

        val srcAddr = alloc[sockaddr_in]()
        val srcAddrLen = sizeof[sockaddr_in].toUInt
        val srcAddrLenBefore = srcAddrLen

        val inMsgHdr = alloc[msghdr]()
        inMsgHdr.msg_name = srcAddr.asInstanceOf[Ptr[Byte]]
        inMsgHdr.msg_namelen = srcAddrLen
        inMsgHdr.msg_iov = inVec
        inMsgHdr.msg_iovlen = nInIovs.toInt

        val nBytesRead = recvmsg(inSocket, inMsgHdr, 0)

        checkIoResult(nBytesRead, "recvmsg_1")

        assertEquals(
          "recmsg data was truncated",
          0,
          (inMsgHdr.msg_flags & MSG_TRUNC)
        )

        // When reading small UDP packets, all data should be there together.
        // Given msg_flags MSG_TRUNC assert above, this should never trigger.
        assertEquals("recvmsg_2", nBytesRead, nBytesSent)

        /* Did address size change out from underneath us?
         * sockaddr_in supplied is expected to stay that way, at least
         * on known continuous integration (CI) systems.
         *
         * There is a chance that, in the wild, we could get an IPv4
         * mapped IPv6 address.
         *
         * If a sockaddr_in6 comes back, we want to know about it.
         * srcAddr is truncated & trash in that case.
         *
         * This is why we check corner cases.
         */
        assertEquals(
          "Unexpected change in source address size",
          srcAddrLenBefore,
          inMsgHdr.msg_namelen
        )

        // Did packet came from where we expected, and not from Mars?
        assertEquals(
          "unexpected remote address",
          dstAddr.asInstanceOf[Ptr[sockaddr_in]].sin_addr.s_addr,
          srcAddr.sin_addr.s_addr
        )

        /// Check that contents are as expected; nothing got mangled.

        val peck1 = outVec(0).iov_base
        val peck1Len = inVec(0).iov_len // 171 bytes
        assertTrue("recvmsg lengths_1", peck1Len <= outVec(0).iov_len)

        val cmp1 = memcmp(peck1, inData0, peck1Len)
        assertEquals("recvmsg content_1", 0, cmp1)

        val peck2 = outVec(0).iov_base + inVec(0).iov_len
        val peck2Len = outVec(0).iov_len - inVec(0).iov_len // 519 - 171 == 348
        assertTrue("recvmsg lengths_2", peck2Len <= inVec(1).iov_len)

        val cmp2 = memcmp(peck2, inData1, peck2Len)
        assertEquals("recvmsg content_2", 0, cmp2)

        val peck3 = outVec(1).iov_base
        val peck3Len = inVec(1).iov_len - peck2Len // 519 - 348 == 171
        assertTrue("recvmsg lengths_3", peck3Len <= outVec(1).iov_len)

        val cmp3 = memcmp(peck3, inData1 + peck2Len, peck3Len)
        assertEquals("recvmsg content_3", 0, cmp3)

        // Q.E.D.
      } finally {
        SocketTestHelpers.closeSocket(inSocket)
        SocketTestHelpers.closeSocket(outSocket)
      }
    }
  }

  /* Exercise the complex Linux 64 bit case.
   * Portable code leads to a tangle of "isPlatform" branches. Focusing
   * on one OS & architecture makes the cmsg unit-under-test logic
   * stand out.
   *
   * A macOs or 32 bit Linux test would look substantially the same
   * but be a bit easier because cmsg.cmsg_level & cmsg.cmsg_type
   * would be directly available. Of course, there would be different
   * constant names & values.
   *
   */
  @Test def linux64ControlMessages(): Unit = if (!isWindows) {
    // The focus is on control messages, the data sent is only an excuse.
    if (Platform.isLinux && !Platform.is32BitPlatform) Zone { implicit z =>
      // Linux bit definition. Useful for cmsg_level & cmsg_type on 64 bit OS.
      type l64cmsghdr = CStruct3[
        size_t, // cmsg_len
        CInt, // cmsg_level
        CInt // cmsg_type
      ]

      val (inSocket, outSocket, dstAddr) = getUdpLoopbackSockets(AF_INET)

      try {
        // Linux values, empirically determined
        val SO_TIMESTAMP = 0x1d // decimal 29
        val SCM_TIMESTAMP = 0x1d // decimal 29
        val SOF_TIMESTAMPING_SOFTWARE = 0x10 // decimal 16

        val sOpt = stackalloc[Int](1)
        !sOpt = SOF_TIMESTAMPING_SOFTWARE

        val ssoStatus = setsockopt(
          inSocket,
          SOL_SOCKET,
          SO_TIMESTAMP,
          sOpt.asInstanceOf[Ptr[Byte]],
          sizeof[Int].toUInt
        )

        assertEquals(s"setsockopt errno: ${errno}", 0, ssoStatus)

        val outData0 = poemHeader + chunk1 + chunk2
        val outData1 = chunk3

        val nOutIovs = 2
        val outVec = alloc[iovec](nOutIovs)

        // outData created with only 1 byte UTF-8 chars, so length method OK.

        outVec(0).iov_base = toCString(outData0)
        outVec(0).iov_len = outData0.length.toUSize

        outVec(1).iov_base = toCString(outData1)
        outVec(1).iov_len = outData1.length.toUSize

        val outMsgHdr = alloc[msghdr]()
        outMsgHdr.msg_name = dstAddr.asInstanceOf[Ptr[Byte]]
        outMsgHdr.msg_namelen = sizeof[sockaddr_in].toUInt
        outMsgHdr.msg_iov = outVec
        outMsgHdr.msg_iovlen = nOutIovs

        val nBytesSent = sendmsg(outSocket, outMsgHdr, 0)

        checkIoResult(nBytesSent, "sendmsg_1")

        // When sending a small UDP datagram, data will be sent in one shot.
        val expectedBytesSent = outData0.size + outData1.size
        assertEquals("sendmsg_2", expectedBytesSent, nBytesSent.toInt)

        // If inSocket did not get data by timeout, it probably never will.
        pollReadyToRecv(
          inSocket,
          30 * 1000
        ) // assert fail on error or timeout

        // Read all in one gulp. We are only marginally interested in data.

        val inData0Size = nBytesSent
        val inData0: Ptr[Byte] = alloc[Byte](inData0Size.toInt)

        val nInIovs = 1
        val inVec = alloc[iovec](nInIovs)

        inVec(0).iov_base = inData0
        inVec(0).iov_len = inData0Size.toUInt

        /* Here we get down to the matter at hand: control messages
         * Pause for a moment and get your true geek on before proceeding.
         * Do you know Dante's famous quote about the Gates of Hell?
         */

        /* BEWARE: The obvious
         *	   'type timestampCtlMsg_t = CStruct2[cmsghdr, timeval]'
         *	   will pad 4 bytes between the two fields, yielding 32
         *	   bytes. ptr._2 will not match OS and you will waste time.
         *
         * Supply a buffer slightly larger than sizeof[linux cmsghdr].
         * Make it less than one complete linux cmsghdr so that any
         * unexpected additional message(s) returned get reported as truncated.
         */

        val nCtlBuf = 40 // sizeof[linux cmsghdr] + 8 // 8 is a guess
        val ctlBuf = alloc[Byte](nCtlBuf)

        val inMsgHdr = alloc[msghdr]()
        inMsgHdr.msg_iov = inVec
        inMsgHdr.msg_iovlen = nInIovs.toInt
        inMsgHdr.msg_control = ctlBuf
        inMsgHdr.msg_controllen = nCtlBuf.toUInt

        val nBytesRead = recvmsg(inSocket, inMsgHdr, 0)

        checkIoResult(nBytesRead, "recvmsg_1")

        assertEquals(
          "recmsg content data was truncated",
          0,
          (inMsgHdr.msg_flags & MSG_TRUNC)
        )

        assertEquals(
          "recmsg control data was truncated",
          0,
          (inMsgHdr.msg_flags & MSG_TRUNC)
        )

        // When reading small UDP packets, all data should be there together.
        // Given msg_flags MSG_TRUNC assert above, this should never trigger.
        assertEquals("recvmsg_2", nBytesRead, nBytesSent)

        /* Open Group 2018 documenataion discourages hand parsing of cmsghdr.
         * The Scala Native implementation of the CMSG 'macros' work on
         * Linux, macOS, and others.
         */

        // A Linux64 OS cmsghdr, cmsg_level & cmsg_type are harder to get.
        val l64cmsg = CMSG_FIRSTHDR(inMsgHdr).asInstanceOf[Ptr[l64cmsghdr]]
        assertNotNull("l64cmsg_1", l64cmsg)

        // redundant, but establishes a confidence baseline.
        assertEquals("l64cmsg should == ctlBuf", l64cmsg, ctlBuf)

        // Received the expected TIMESTAMP?
        assertEquals(
          "l64cmsg.cmsg_level is not SOL_SOCKET",
          SOL_SOCKET,
          l64cmsg._2
        )
        assertEquals(
          "l64cmsg.cmsg_type is not SCM_TIMESTAMP",
          SCM_TIMESTAMP,
          l64cmsg._3
        )

        val tv = CMSG_DATA(l64cmsg.asInstanceOf[Ptr[cmsghdr]])
          .asInstanceOf[Ptr[timeval]]

        val now: time_t = scala.scalanative.posix.time.time(null)

        /* Is value received as CMS_DATA roughly correct/as_expected?
         *
         * Comparing timestamps is exact, especially when they are
         * not retrieved atomically.
         *
         * Another instance of a classic ROC (receiver operating
         * characteristic) curve decision.
         */
        val tolerance = 3.0f // Allow _some_ slack, but not too much!
        assertEquals(tv.tv_sec.toLong.toFloat, now.toLong.toFloat, tolerance)

        // Q.E.D.
      } finally {
        SocketTestHelpers.closeSocket(inSocket)
        SocketTestHelpers.closeSocket(outSocket)
      }
    }
  }
}
