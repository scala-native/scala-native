package java.net

// Ported from Apache Harmony
object InetSocketAddressSuite extends tests.Suite {

  test("this(String, Int)") {
    val address = new InetSocketAddress("127.0.0.1", 0)
    assertEquals("/127.0.0.1:0", address.toString)
    val localhostName = address.getHostName
    assertNot(localhostName == null)
    assertEquals(localhostName + "/127.0.0.1:0", address.toString)
  }

  test("createUnresolved") {
    val pairs = Array(("127.0.0.1", 1234),
                      ("192.168.0.1", 10000),
                      ("127.0.0", 0),
                      ("127.0.0", 65535),
                      ("strange host", 65535))
    for ((host, port) <- pairs) {
      val addr = InetSocketAddress.createUnresolved(host, port)
      assert(addr.isUnresolved)
      assert(addr.getAddress == null)
      assertEquals(addr.getHostString, host)
      assertEquals(addr.getHostName, host)
      assertEquals(addr.getPort, port)
    }
  }

  test("createUnresolved should throw IllegalArgumentException") {
    val pairs = Array((null, 1), ("host", -1), ("host", 65536))
    for ((host, port) <- pairs) {
      assertThrows[IllegalArgumentException] {
        InetSocketAddress.createUnresolved(host, port)
      }
    }
  }
}
