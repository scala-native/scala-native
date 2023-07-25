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
import scalanative.posix.sys.uio._
import scalanative.posix.sys.uioOps._

import org.scalanative.testsuite.posixlib.sys.SocketTestHelpers._

import scalanative.meta.LinktimeInfo.isWindows

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Before

class UioTest {
  /* writev() & readv() also work with files. Using sockets here makes
   * it easier to eventually create a unit-test for socket-only methods
   * sendmsg() & recvmsg() and to highlight the parallels.
   */

  @Before
  def before(): Unit = {
    assumeTrue(
      "POSIX uio writev() & readv() are not available on Windows",
      !isWindows
    )

    val isIPv4Available = hasLoopbackAddress(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
    assumeTrue("IPv4 UDP loopback is not available", isIPv4Available)
  }

  private def getConnectedUdp4LoopbackSockets()(implicit
      z: Zone
  ): Tuple2[CInt, CInt] = {

    val (sin, sout, outAddr) =
      getUdpLoopbackSockets(AF_INET)

    try {
      val connectOutStatus =
        connect(sout, outAddr, sizeof[sockaddr].toUInt)
      assertNotEquals(
        s"connect output socket failed,  errno: ${errno}",
        -1,
        connectOutStatus
      )

      (sin, sout)
    } catch {
      case e: Throwable =>
        SocketTestHelpers.closeSocket(sout)
        SocketTestHelpers.closeSocket(sin)
        throw e
        (-1, -1) // should never get here.
    }
  }

  /* Emily Dickinson - The Chariot
   * This version is in the public domain.
   * URL: https://www.gutenberg.org/files/12242/12242-h/
   *           12242-h.htm#Because_I_could_not_stop_for_Death
   */

  private final val poemHeader =
    """   | 
          |Emily Dickinson, 1890 -- Public Domain
          |XXVII.
          |
          |THE CHARIOT.
          |
          |""".stripMargin

  private final val verse1 =
    """   |Because I could not stop for Death,
          |He kindly stopped for me;
          |The carriage held but just ourselves
          |And Immortality.
          | 
          |""".stripMargin

  private final val verse2 =
    """   |We slowly drove, he knew no haste,
          |And I had put away
          |My labor, and my leisure too,
          |For his civility.
          |
          |""".stripMargin

  private final val verse3 =
    """   |We passed the school where children played,
          |Their lessons scarcely done;
          |We passed the fields of gazing grain,
          |We passed the setting sun.
          |
          |""".stripMargin

  private final val verse4 =
    """   |We paused before a house that seemed
          |A swelling of the ground;
          |The roof was scarcely visible,
          |The cornice but a mound.
          |
          |""".stripMargin

  private final val verse5 =
    """   |Since then 't is centuries; but each
          |Feels shorter than the day
          |I first surmised the horses' heads
          |Were toward eternity.
          |
          |""".stripMargin

  @Test def writevReadvShouldPlayNicely(): Unit = Zone { implicit z =>
    // writev() should gather, readv() should scatter, the 2 should pass data.
    if (!isWindows) {
      val (inSocket, outSocket) = getConnectedUdp4LoopbackSockets()

      try {
        val outData0 = poemHeader + verse1 + verse2
        val outData1 = verse3
        val outData2 = verse4 + verse5

        // Design Note: Gather write more than 2 buffers
        val nOutIovs = 3
        val outVec = alloc[iovec](nOutIovs)

        outVec(0).iov_base = toCString(outData0)
        outVec(0).iov_len = outData0.length.toUSize

        outVec(1).iov_base = toCString(outData1)
        outVec(1).iov_len = outData1.length.toUSize

        outVec(2).iov_base = toCString(outData2)
        outVec(2).iov_len = outData2.length.toUSize

        val nBytesSent = writev(outSocket, outVec, nOutIovs)

        checkIoResult(nBytesSent, "writev_1")

        // When sending a small UDP datagram, data will be sent in one shot.
        val expectedBytesSent = outData0.size + outData1.size + outData2.size
        assertEquals("writev_2", expectedBytesSent, nBytesSent.toInt)

        // If inSocket did not get data by timeout, it probably never will.
        pollReadyToRecv(inSocket, 30 * 1000) // assert fail on error or timeout

        /* Design Notes: Scatter read at least 2 buffers.
         *   - Be playful here. Mix things up, users will do the unexpected.
         *
         *   - Allocate one byte more than the number of bytes to be
         *     read into that buffer.  This is not needed for straight
         *     execution.
         *
         *     It greatly eases debugging by allowing a C NUL to
         *     be placed in the buffer without clobbering good data.
         *     'fromCString()' can then be called for printing and easier
         *     String comparisons. You will understand & appreciate the
         *     slight extra complexity if you ever have to debug this code.
         *         !(inData2 + inData2Size) = 0 // NUL terminate CString
         *         val msg: String = fromCString(inData2)
         *
         * Cumbersome type manipulation ((inData0Size.toInt + 1).toUSize)
         * is to accomodate both Scala 2 & 3 with same code.
         */

        val inData0Size = outData0.size + outData1.size
        val inData0: Ptr[Byte] = alloc[Byte]((inData0Size.toInt + 1))

        val inData1Size = 1.toSize // odd read, just to throw things off.
        val inData1: Ptr[Byte] = alloc[Byte]((inData0Size.toInt + 1))

        val inData2Size = (verse4.length - 1).toSize
        val inData2: Ptr[Byte] = alloc[Byte]((inData0Size.toInt + 1))

        val inData3Size = verse5.length.toSize
        val inData3: Ptr[Byte] = alloc[Byte]((inData0Size.toInt + 1))

        val nInIovs = 4
        val inVec = alloc[iovec](nInIovs)

        inVec(0).iov_base = inData0
        inVec(0).iov_len = inData0Size.toUInt

        inVec(1).iov_base = inData1
        inVec(1).iov_len = inData1Size.toUInt

        inVec(2).iov_base = inData2
        inVec(2).iov_len = inData2Size.toUInt

        inVec(3).iov_base = inData3
        inVec(3).iov_len = inData3Size.toUInt

        val nBytesRead = readv(inSocket, inVec, nInIovs)

        checkIoResult(nBytesRead, "readv_1")

        // When reading small UDP packets, all data should be there together.
        assertEquals("readv_2", nBytesRead, nBytesSent)

        /// Check that contents are as expected; nothing got mangled.

        // outData(0) & (1) were gathered then scattered to inData(0).
        val cmp1 = memcmp(outVec(0).iov_base, inData0, outVec(0).iov_len)
        assertEquals("readv content_1", 0, cmp1)

        val cmp2 = memcmp(
          outVec(1).iov_base,
          inData0 + outVec(0).iov_len,
          outVec(1).iov_len
        )
        assertEquals("readv content_2", 0, cmp2)

        // One byte of outData(2) was scattered to inData(1).
        val cmp3 = memcmp(outVec(2).iov_base, inData1, inData1Size.toUInt)
        assertEquals("readv content_3", 0, cmp3)

        // Some of outData(2) was scattered to inData(2).
        val cmp4 = memcmp(outVec(2).iov_base + 1, inData2, inData2Size.toUInt)
        assertEquals("readv content_4", 0, cmp4)

        // The rest of outData(2) was scattered to inData(3).
        val cmp5 = memcmp(
          outVec(2).iov_base + 1 + inData2Size,
          inData3,
          inData3Size.toUInt
        )
        assertEquals("readv content_5", 0, cmp5)

        // Verse 5 now stands existentially alone in inData3.
        val cmp6 = memcmp(toCString(verse5), inData3, inData3Size.toUInt)
        assertEquals("readv content_6", 0, cmp6)

        // Q.E.D.
      } finally {
        SocketTestHelpers.closeSocket(inSocket)
        SocketTestHelpers.closeSocket(outSocket)
      }
    }
  }
}
