package java.net

import java.io.IOException

object SocketSuite extends tests.Suite {

  test("keepAlive") {
    val s         = new Socket()
    val prevValue = s.getKeepAlive
    s.setKeepAlive(!prevValue)
    assertEquals(s.getKeepAlive, !prevValue)
  }

  test("reuseAddr") {
    val s         = new Socket()
    val prevValue = s.getReuseAddress
    s.setReuseAddress(!prevValue)
    assertEquals(s.getReuseAddress, !prevValue)
  }

  test("OOBInline") {
    val s         = new Socket()
    val prevValue = s.getOOBInline
    s.setOOBInline(!prevValue)
    assertEquals(s.getOOBInline, !prevValue)
  }

  test("tcpNoDelay") {
    val s         = new Socket()
    val prevValue = s.getTcpNoDelay
    s.setTcpNoDelay(!prevValue)
    assertEquals(s.getTcpNoDelay, !prevValue)
  }

  test("soLinger") {
    val s = new Socket()
    s.setSoLinger(true, 100)
    assertEquals(s.getSoLinger, 100)
    s.setSoLinger(false, 50000000)
    assertEquals(s.getSoLinger, -1)
  }

  test("soTimeout") {
    val s         = new Socket()
    val prevValue = s.getSoTimeout
    s.setSoTimeout(prevValue + 1000)
    assertEquals(s.getSoTimeout, prevValue + 1000)
  }

  test("receiveBufferSize") {
    val s         = new Socket()
    val prevValue = s.getReceiveBufferSize
    s.setReceiveBufferSize(prevValue + 100)
    // On linux the size is actually set to the double of given parameter
    // so we don't test if it's equal
    assert(s.getReceiveBufferSize >= prevValue + 100)
  }

  test("sendBufferSize") {
    val s         = new Socket()
    val prevValue = s.getSendBufferSize
    s.setSendBufferSize(prevValue + 100)
    // On linux the size is actually set to the double of given parameter
    // so we don't test if it's equal
    assert(s.getSendBufferSize >= prevValue + 100)
  }

  test("trafficClass") {
    val s = new Socket()
    s.setTrafficClass(0x28)
    assertEquals(s.getTrafficClass, 0x28)
  }

  test("connect with timeout") {
    val sock = new Socket()
    assertThrows[SocketTimeoutException] {
      sock.connect(new InetSocketAddress("123.123.123.123", 12341), 200)
    }
    sock.close
  }

}
