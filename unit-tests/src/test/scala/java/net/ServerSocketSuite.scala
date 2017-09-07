package java.net

object ServerSocketSuite extends tests.Suite {

  test("bind") {
    val s1   = new ServerSocket
    val addr = new InetSocketAddress(InetAddress.getLoopbackAddress, 0)

    s1.bind(addr)
    val port = s1.getLocalPort

    assertEquals(s1.getLocalSocketAddress,
                 new InetSocketAddress(InetAddress.getLoopbackAddress, port))
    assert(s1.isBound)

    val s2 = new ServerSocket
    val s3 = new ServerSocket
    s2.bind(addr)

    assertThrows[BindException] { s3.bind(s2.getLocalSocketAddress) }

    val s4 = new ServerSocket
    assertThrows[BindException] {
      s4.bind(new InetSocketAddress(InetAddress.getByName("101.0.0.0"), 0))
    }

    class UnsupportedSocketAddress extends SocketAddress {}

    val s5 = new ServerSocket
    assertThrows[IllegalArgumentException] {
      s5.bind(new UnsupportedSocketAddress)
    }

    s1.close
    s2.close
    s3.close
    s4.close
    s5.close
  }

  test("accept") {
    val s = new ServerSocket(0)
    s.setSoTimeout(1)
    assertThrows[SocketTimeoutException] { s.accept }
  }

  test("close") {
    val s = new ServerSocket(0)
    s.close
    assertThrows[SocketException] { s.accept }
  }

  test("soTimeout") {
    val s = new ServerSocket(0)

    val prevValue = s.getSoTimeout
    s.setSoTimeout(prevValue + 100)
    assertEquals(prevValue + 100, s.getSoTimeout)
  }

  test("toString") {
    val s1    = new ServerSocket(0)
    val port1 = s1.getLocalPort
    assertEquals("ServerSocket[addr=0.0.0.0/0.0.0.0,localport=" + port1 + "]",
                 s1.toString)

    val s2 = new ServerSocket
    assertEquals("ServerSocket[unbound]", s2.toString)

    s2.bind(new InetSocketAddress("127.0.0.1", 0))
    val port2 = s2.getLocalPort
    assertEquals("ServerSocket[addr=/127.0.0.1,localport=" + port2 + "]",
                 s2.toString)
  }

}
