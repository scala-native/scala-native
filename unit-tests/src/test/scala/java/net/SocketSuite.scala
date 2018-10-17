package java.net

import java.io.IOException

object SocketSuite extends tests.Suite {

  test("keepAlive") {
    val s = new Socket()
    try {
      val prevValue = s.getKeepAlive
      s.setKeepAlive(!prevValue)
      assertEquals(s.getKeepAlive, !prevValue)
    } finally {
      s.close()
    }
  }

  test("reuseAddr") {
    val s = new Socket()
    try {
      val prevValue = s.getReuseAddress
      s.setReuseAddress(!prevValue)
      assertEquals(s.getReuseAddress, !prevValue)
    } finally {
      s.close()
    }
  }

  test("OOBInline") {
    val s = new Socket()
    try {
      val prevValue = s.getOOBInline
      s.setOOBInline(!prevValue)
      assertEquals(s.getOOBInline, !prevValue)
    } finally {
      s.close()
    }
  }

  test("tcpNoDelay") {
    val s = new Socket()
    try {
      val prevValue = s.getTcpNoDelay
      s.setTcpNoDelay(!prevValue)
      assertEquals(s.getTcpNoDelay, !prevValue)
    } finally {
      s.close()
    }
  }

  test("soLinger") {
    val s = new Socket()
    try {
      s.setSoLinger(true, 100)
      assertEquals(s.getSoLinger, 100)
      s.setSoLinger(false, 50000000)
      assertEquals(s.getSoLinger, -1)
    } finally {
      s.close()
    }
  }

  test("soTimeout") {
    val s = new Socket()
    try {
      val prevValue = s.getSoTimeout
      s.setSoTimeout(prevValue + 1000)
      assertEquals(s.getSoTimeout, prevValue + 1000)
    } finally {
      s.close()
    }
  }

  test("receiveBufferSize") {
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
      assert(prevValue > 0)
      s.setReceiveBufferSize(prevValue + 100)
    } finally {
      s.close()
    }
  }

  test("sendBufferSize") {
    // This test basically checks that getSendBufferSize &
    // setSendBufferSize do not unexpectedly throw and that the former
    // returns a minimally sane value.
    // See more extensive comments in setBufferSize test.

    val s = new Socket()

    try {
      val prevValue = s.getSendBufferSize
      assert(prevValue > 0)
      s.setSendBufferSize(prevValue + 100)
    } finally {
      s.close()
    }
  }

  test("trafficClass") {
    val s = new Socket()
    try {
      s.setTrafficClass(0x28)
      assertEquals(s.getTrafficClass, 0x28)
    } finally {
      s.close()
    }
  }

  test("connect with timeout") {
    val s = new Socket()
    try {
      assertThrows[SocketTimeoutException] {
        s.connect(new InetSocketAddress("123.123.123.123", 12341), 100)
      }
    } finally {
      s.close()
    }
  }

  test("bind") {
    val s1 = new Socket
    try {
      val nonLocalAddr =
        new InetSocketAddress(InetAddress.getByName("123.123.123.123"), 0)
      assertThrows[BindException] { s1.bind(nonLocalAddr) }
    } finally {
      s1.close()
    }

    val s2 = new Socket
    try {
      s2.bind(new InetSocketAddress(InetAddress.getLoopbackAddress, 0))
      val port = s2.getLocalPort
      assertEquals(new InetSocketAddress(InetAddress.getLoopbackAddress, port),
                   s2.getLocalSocketAddress)
    } finally {
      s2.close()
    }

    val s3 = new Socket
    try {
      s3.bind(null)
      assert(s3.getLocalSocketAddress != null)
    } finally {
      s3.close()
    }

    val s4 = new Socket
    try {
      s4.bind(new InetSocketAddress(InetAddress.getLoopbackAddress, 0))
      val s5 = new Socket
      try {
        assertThrows[BindException] { s5.bind(s4.getLocalSocketAddress) }
      } finally {
        s5.close()
      }
    } finally {
      s4.close()
    }

    class UnsupportedSocketAddress extends SocketAddress
    val s6 = new Socket
    try {
      assertThrows[IllegalArgumentException] {
        s6.bind(new UnsupportedSocketAddress)
      }
    } finally {
      s6.close()
    }
  }

}
