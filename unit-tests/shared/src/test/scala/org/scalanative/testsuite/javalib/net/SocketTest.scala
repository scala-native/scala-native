package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class SocketTest {

  @Test def keepAlive(): Unit = {
    val s = new Socket()
    try {
      val prevValue = s.getKeepAlive
      s.setKeepAlive(!prevValue)
      assertEquals(s.getKeepAlive, !prevValue)
    } finally {
      s.close()
    }
  }

  @Test def reuseAddr(): Unit = {
    val s = new Socket()
    try {
      val prevValue = s.getReuseAddress
      s.setReuseAddress(!prevValue)
      assertEquals(s.getReuseAddress, !prevValue)
    } finally {
      s.close()
    }
  }

  @Test def oobInline(): Unit = {
    val s = new Socket()
    try {
      val prevValue = s.getOOBInline
      s.setOOBInline(!prevValue)
      assertEquals(s.getOOBInline, !prevValue)
    } finally {
      s.close()
    }
  }

  @Test def tcpNoDelay(): Unit = {
    val s = new Socket()
    try {
      val prevValue = s.getTcpNoDelay
      s.setTcpNoDelay(!prevValue)
      assertEquals(s.getTcpNoDelay, !prevValue)
    } finally {
      s.close()
    }
  }

  @Test def soLinger(): Unit = {
    val s = new Socket()
    try {
      s.setSoLinger(true, 100)
      assertEquals(s.getSoLinger, 100)
      s.setSoLinger(false, 50000000)
      assertEquals(s.getSoLinger, -1)
      s.setSoLinger(true, 0)
      assertEquals(s.getSoLinger, 0)
    } finally {
      s.close()
    }
  }

  @Test def soTimeout(): Unit = {
    assumeFalse(
      "getsockopt return not yet supported error on aarch64-linux-gnu",
      Platform.isArm64 && Platform.isLinux &&
        !Platform.executingInJVM
    )

    val s = new Socket()
    try {
      val prevValue = s.getSoTimeout
      s.setSoTimeout(prevValue + 1000)
      assertEquals(s.getSoTimeout, prevValue + 1000)
    } finally {
      s.close()
    }
  }

  @Test def receiveBufferSize(): Unit = {
    // This test basically checks that getReceiveBufferSize &
    // setReceiveBufferSize do not unexpectedly throw and that the former
    // returns a minimally sane value.
    //
    // The Java 8 documentation at URL
    // https://docs.oracle.com/javase/8/docs/api/java/net/\
    //     Socket.html#setReceiveBufferSize-int- [sic trailing dash]
    // describes the argument for setReceiveBufferSize(int) &
    // setSendBufferSize(int) as a _hint_ to the operating system, _not_
    // a requirement or demand.  This description is basically unaltered
    // in Java 10.
    //
    // There are a number of reasons the operating system can choose to
    // ignore the hint. Changing the buffer size, even before a bind() call,
    // may not be implemented. The buffer size may already be at its
    // maximum.
    //
    // Since, by definition, the OS can ignore the hint, it makes no
    // sense to set the size, then re-read it and see if it changed.
    //
    // The sendBuffersize test refers to this comment.
    // Please keep both tests synchronized.

    val s = new Socket()

    try {
      val prevValue = s.getReceiveBufferSize
      assertTrue(prevValue > 0)
      s.setReceiveBufferSize(prevValue + 100)
    } finally {
      s.close()
    }
  }

  @Test def sendBufferSize(): Unit = {
    // This test basically checks that getSendBufferSize &
    // setSendBufferSize do not unexpectedly throw and that the former
    // returns a minimally sane value.
    // See more extensive comments in setBufferSize test.

    val s = new Socket()

    try {
      val prevValue = s.getSendBufferSize
      assertTrue(prevValue > 0)
      s.setSendBufferSize(prevValue + 100)
    } finally {
      s.close()
    }
  }

  /* The Oracle documentation for Socket method traffic class in both
   * Java 8 and 17 describe setTrafficClass as providing a hint
   * and that a setTrafficClass followed by a getTrafficClass of the
   * might not return the same value.
   *
   * But wait! It gets better.
   *
   * The setTrafficClass and getTrafficClass methods both use
   * the StandardSystemsOption IP_TOS field. Both Java 8 and 17
   * describe this field:
   *   "The behavior of this socket option on a stream-oriented socket,
   *   or an IPv6 socket, is not defined in this release."
   *
   * This file is testing Sockets() which means stream (TCP) sockets. Strike 1.
   * The default underlying protocol is now IPv6. Strike 2.
   *
   * In the general case and inherent design, this test is bogus.
   * It is executed only in cases where it has historically passed.
   * Some day they may break and need to be skipped.
   *
   * Other cases are silently skipped, so as to not cause anxiety
   * over a 'normal' situation.
   */
  @Test def trafficClass(): Unit = {

    val prop = System.getProperty("java.net.preferIPv4Stack")
    val useIPv4 = (prop != null) && (prop.toLowerCase() == "true")

    val disabled = if (!Platform.isWindows) {
      false
    } else { // No sense testing in these cases
      /* Windows lacks support for setoption IPV6_TCLASS.
       *
       * When execution on Windows with Java 17 trafficClass is not set.
       * s.getTrafficClass returns 0 instead of 0x28
       * See above, it is normal for some network implementations to not
       * take the hint.
       */
      (!useIPv4) || (Platform.executingInJVMOnJDK17)
    }

    if (!disabled) { // yes, enIPv6 will be tested, if available.
      val s = new Socket()
      try {
        /* Reference:
         *   https://docs.oracle.com/javase/8/docs/api/
         *       java/net/Socket.html#setTrafficClass
         *
         *  The value 0x28 has been in this test for eons. Interpreting
         *  its meaning on-sight is difficult.
         *
         *  It is possibly a six leftmost bit DSCP AF11 (0xA) with
         *  the low (rightmost) ECN 2 bits as 0. (0xA << 2 == 0x28)
         *
         *  Jargon:
         *     AF11 -> Priority Precedence, Low drop probability.
         *     DSCP - Differentiated Serviddes Code Point, RFC 2474
         *     ECN - Explicit Congestion Notification, RFC 3168
         *
         *  That wild guess and $5.00 might get you a cup of coffee.
         *  Obscurity keeps the weenies and follow-on maintainers out.
         */
        val tc = 0x28
        s.setTrafficClass(tc)
        assertEquals(s.getTrafficClass, tc)
      } finally {
        s.close()
      }
    }
  }

  @Test def connect(): Unit = {
    val s = new Socket()
    try {
      assertThrows(
        classOf[UnknownHostException],
        s.connect(InetSocketAddress.createUnresolved("localhost", 0))
      )
    } finally {
      s.close()
    }
  }

  @Test def connectWithTimeout(): Unit = {
    val s = new Socket()
    try {
      assertThrows(
        classOf[SocketTimeoutException],
        // Use a document-only Internet address and very short timeout.
        s.connect(new InetSocketAddress("203.0.113.1", 12341), 2)
      )
    } finally {
      s.close()
    }
  }

  /* Fill the accept queue of a listener which never accepts, so that the
   * next connect attempt to it blocks. Skips the test if the platform does
   * not block in that case (e.g. it refuses the connection instead).
   */
  private def withBlockingListener(body: InetSocketAddress => Unit): Unit = {
    val loopback = InetAddress.getLoopbackAddress
    val server = new ServerSocket(0, 1, loopback)
    val addr = new InetSocketAddress(loopback, server.getLocalPort)
    val fillers = scala.collection.mutable.ListBuffer.empty[Socket]
    try {
      var blocking = false
      while (!blocking && fillers.size < 64) {
        val s = new Socket()
        fillers += s
        try s.connect(addr, 200)
        catch {
          case _: SocketTimeoutException => blocking = true
          case _: java.io.IOException    => ()
        }
      }
      assumeTrue("Could not exhaust the listener backlog", blocking)
      body(addr)
    } finally {
      fillers.foreach(_.close())
      server.close()
    }
  }

  private def checkCloseUnblocksConnect(timeout: Int): Unit = {
    assumeTrue("requires multithreading", Platform.isMultithreadingEnabled)
    withBlockingListener { addr =>
      val s = new Socket()
      @volatile var result: Throwable = null
      val connector = new Thread(() =>
        try {
          s.connect(addr, timeout)
          result = new AssertionError("connect unexpectedly succeeded")
        } catch { case e: Throwable => result = e }
      )
      connector.start()
      connector.join(500)
      assumeTrue("connect did not block", connector.isAlive())

      s.close()
      connector.join(10000)
      assertFalse("connect still blocked after close", connector.isAlive())
      assertTrue(
        s"expected SocketException, got $result",
        result.isInstanceOf[SocketException]
      )
    }
  }

  @Test def connectToServerSendingGreeting(): Unit = {
    assumeTrue("requires multithreading", Platform.isMultithreadingEnabled)
    val loopback = InetAddress.getLoopbackAddress
    val server = new ServerSocket(0, 2, loopback)
    val timeouts = Seq(0, 10000)
    try {
      val acceptor = new Thread(() =>
        timeouts.foreach { _ =>
          val peer = server.accept()
          try peer.getOutputStream().write(42)
          finally peer.close()
        }
      )
      acceptor.start()
      timeouts.foreach { timeout =>
        val s = new Socket()
        try {
          s.connect(
            new InetSocketAddress(loopback, server.getLocalPort),
            timeout
          )
          assertEquals(42, s.getInputStream().read())
        } finally s.close()
      }
      acceptor.join(10000)
    } finally server.close()
  }

  // Issue #4480
  @Test def closeUnblocksConnect(): Unit =
    checkCloseUnblocksConnect(timeout = 0)

  // Issue #4480
  @Test def closeUnblocksConnectWithTimeout(): Unit =
    checkCloseUnblocksConnect(timeout = 30000)

  @Test def bind(): Unit = {
    val s1 = new Socket()
    try {
      val nonLocalAddr =
        new InetSocketAddress(InetAddress.getByName("123.123.123.123"), 0)
      assertThrows(
        "bind must fail for non local address",
        classOf[BindException],
        s1.bind(nonLocalAddr)
      )
    } finally {
      s1.close()
    }

    val s2 = new Socket()
    try {
      s2.bind(new InetSocketAddress(InetAddress.getLoopbackAddress, 0))
      val port = s2.getLocalPort
      assertEquals(
        "bind must use the given address",
        new InetSocketAddress(InetAddress.getLoopbackAddress, port),
        s2.getLocalSocketAddress
      )
    } finally {
      s2.close()
    }

    val s3 = new Socket()
    try {
      s3.bind(null)
      assertTrue(
        "bind must use any available address when not provided",
        s3.getLocalSocketAddress != null
      )
    } finally {
      s3.close()
    }

    val s4 = new Socket()
    try {
      s4.bind(new InetSocketAddress(InetAddress.getLoopbackAddress, 0))
      val s5 = new Socket()
      try {
        assertThrows(
          "bind must fail if the address is already in use",
          classOf[BindException],
          s5.bind(s4.getLocalSocketAddress)
        )
      } finally {
        s5.close()
      }
    } finally {
      s4.close()
    }

    class UnsupportedSocketAddress extends SocketAddress
    val s6 = new Socket()
    try {
      assertThrows(
        "bind must fail for unsupported SocketAddress type",
        classOf[IllegalArgumentException],
        s6.bind(new UnsupportedSocketAddress)
      )
    } finally {
      s6.close()
    }

    val s7 = new Socket()
    try {
      assertThrows(
        "bind must fail for unresolved address",
        classOf[SocketException],
        s7.bind(InetSocketAddress.createUnresolved("localhost", 0))
      )
    } finally {
      s7.close()
    }
  }

}
