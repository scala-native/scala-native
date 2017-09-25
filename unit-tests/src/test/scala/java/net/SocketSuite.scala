package java.net

import java.io.IOException

object SocketSuite extends tests.Suite {

  test("keepAlive") {
    val s         = new Socket()
    val prevValue = s.getKeepAlive
    s.setKeepAlive(!prevValue)
    assertEquals(s.getKeepAlive, !prevValue)
    s.close()
  }

  test("reuseAddr") {
    val s         = new Socket()
    val prevValue = s.getReuseAddress
    s.setReuseAddress(!prevValue)
    assertEquals(s.getReuseAddress, !prevValue)
    s.close()
  }

  test("OOBInline") {
    val s         = new Socket()
    val prevValue = s.getOOBInline
    s.setOOBInline(!prevValue)
    assertEquals(s.getOOBInline, !prevValue)
    s.close()
  }

  test("tcpNoDelay") {
    val s         = new Socket()
    val prevValue = s.getTcpNoDelay
    s.setTcpNoDelay(!prevValue)
    assertEquals(s.getTcpNoDelay, !prevValue)
    s.close()
  }

  test("soLinger") {
    val s = new Socket()
    s.setSoLinger(true, 100)
    assertEquals(s.getSoLinger, 100)
    s.setSoLinger(false, 50000000)
    assertEquals(s.getSoLinger, -1)
    s.close()
  }

  test("soTimeout") {
    val s         = new Socket()
    val prevValue = s.getSoTimeout
    s.setSoTimeout(prevValue + 1000)
    assertEquals(s.getSoTimeout, prevValue + 1000)
    s.close()
  }

  test("receiveBufferSize") {
    val s         = new Socket()
    val prevValue = s.getReceiveBufferSize
    s.setReceiveBufferSize(prevValue + 100)
    // On linux the size is actually set to the double of given parameter
    // so we don't test if it's equal
    assert(s.getReceiveBufferSize >= prevValue + 100)
    s.close()
  }

  test("sendBufferSize") {
    val s         = new Socket()
    val prevValue = s.getSendBufferSize
    s.setSendBufferSize(prevValue + 100)
    // On linux the size is actually set to the double of given parameter
    // so we don't test if it's equal
    assert(s.getSendBufferSize >= prevValue + 100)
    s.close()
  }

  test("trafficClass") {
    val s = new Socket()
    s.setTrafficClass(0x28)
    assertEquals(s.getTrafficClass, 0x28)
    s.close()
  }

  test("connect with timeout") {
    val s = new Socket()
    assertThrows[SocketTimeoutException] {
      s.connect(new InetSocketAddress("123.123.123.123", 12341), 100)
    }
    s.close()
  }

  test("bind") {
    val s1 = new Socket
    val nonLocalAddr =
      new InetSocketAddress(InetAddress.getByName("123.123.123.123"), 0)
    assertThrows[BindException] { s1.bind(nonLocalAddr) }
    s1.close()

    val s2 = new Socket
    s2.bind(new InetSocketAddress(InetAddress.getLoopbackAddress, 0))
    val port = s2.getLocalPort
    assertEquals(new InetSocketAddress(InetAddress.getLoopbackAddress, port),
                 s2.getLocalSocketAddress)
    s2.close()

    val s3 = new Socket
    s3.bind(null)
    assert(s3.getLocalSocketAddress != null)
    s3.close()

    val s4 = new Socket
    s4.bind(new InetSocketAddress(InetAddress.getLoopbackAddress, 0))
    val s5 = new Socket
    assertThrows[BindException] { s5.bind(s4.getLocalSocketAddress) }
    s4.close()
    s5.close()

    class UnsupportedSocketAddress extends SocketAddress
    val s6 = new Socket
    assertThrows[IllegalArgumentException] {
      s6.bind(new UnsupportedSocketAddress)
    }
    s6.close()
  }

  test("getChannel") {
    val s = new Socket
    assertEquals(s.getChannel, null)
  }

}
