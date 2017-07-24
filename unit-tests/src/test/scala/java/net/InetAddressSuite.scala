package java.net

// Ported from Apache Harmony
object InetAddressSuite extends tests.Suite {

  test("equals should work on localhosts from getByName") {
    val ia1 = InetAddress.getByName("127.1")
    val ia2 = InetAddress.getByName("127.0.0.1")
    assertEquals(ia1, ia2)
  }

  test("getAddress") {
    try {
      val ia    = InetAddress.getByName("127.0.0.1")
      val caddr = Array[Byte](127.toByte, 0.toByte, 0.toByte, 1.toByte)
      val addr  = ia.getAddress()
      for (i <- addr.indices)
        assertEquals(caddr(i), addr(i))
    } catch {
      case e: UnknownHostException => {}
    }

    val origBytes = Array[Byte](0.toByte, 1.toByte, 2.toByte, 3.toByte)
    val address   = InetAddress.getByAddress(origBytes)
    origBytes(0) = -1
    val newBytes = address.getAddress()
    assertEquals(newBytes(0), 0.toByte)
  }

  test("getAllByName") {
    val all = InetAddress.getAllByName("localhost")
    assertNot(all == null)
    assert(all.length >= 1)

    for (alias <- all)
      assert(alias.getHostName().startsWith("localhost"))

    val ias = InetAddress.getAllByName(null)
    for (ia <- ias)
      assert(ia.isLoopbackAddress())

    val ias2 = InetAddress.getAllByName("")
    for (ia <- ias2)
      assert(ia.isLoopbackAddress())

    // Check that getting addresses by dotted string distingush IPv4 and IPv6 subtypes
    val list = InetAddress.getAllByName("192.168.0.1")
    for (addr <- list)
      assertNot(addr.getClass == classOf[InetAddress])

  }

  test("getByName") {
    val ia = InetAddress.getByName("127.0.0.1")

    val i1 = InetAddress.getByName("1.2.3")
    assertEquals("1.2.0.3", i1.getHostAddress())

    val i2 = InetAddress.getByName("1.2")
    assertEquals("1.0.0.2", i2.getHostAddress())

    val i3 = InetAddress.getByName(String.valueOf(0xffffffffL))
    assertEquals("255.255.255.255", i3.getHostAddress())
  }

  test("getHostAddress") {
    assertEquals("1.3.0.4", InetAddress.getByName("1.3.4").getHostAddress())
    assertEquals("0:0:0:0:0:0:0:1",
                 InetAddress.getByName("::1").getHostAddress())
  }

  test("isReachable") {
    // actual testing done using sbt scripted test framework
    val addr = InetAddress.getByName("127.0.0.1")
    assertThrows[IllegalArgumentException] { addr.isReachable(-1) }
  }

  test("isMulticastAddress") {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assert(ia1.isMulticastAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertNot(ia2.isMulticastAddress())
  }

  test("isAnyLocalAddress") {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertNot(ia1.isAnyLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertNot(ia2.isAnyLocalAddress())
  }

  test("isLinkLocalAddress") {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertNot(ia1.isLinkLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertNot(ia2.isLinkLocalAddress())
  }

  test("isLoopbackAddress") {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertNot(ia1.isLoopbackAddress())
    val ia2 = InetAddress.getByName("localhost")
    assert(ia2.isLoopbackAddress())
    val ia3 = InetAddress.getByName("127.0.0.2")
    assert(ia3.isLoopbackAddress())
  }

  test("isSiteLocalAddress") {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertNot(ia1.isSiteLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertNot(ia2.isSiteLocalAddress())
    val ia3 = InetAddress.getByName("127.0.0.2")
    assertNot(ia3.isSiteLocalAddress())
    val ia4 = InetAddress.getByName("243.243.45.3")
    assertNot(ia4.isSiteLocalAddress())
    val ia5 = InetAddress.getByName("10.0.0.2")
    assert(ia5.isSiteLocalAddress())
  }

  test("MC methods") {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertNot(ia1.isMCGlobal())
    assertNot(ia1.isMCLinkLocal())
    assertNot(ia1.isMCNodeLocal())
    assertNot(ia1.isMCOrgLocal())
    assert(ia1.isMCSiteLocal())

    val ia2 = InetAddress.getByName("243.243.45.3")
    assertNot(ia2.isMCGlobal())
    assertNot(ia2.isMCLinkLocal())
    assertNot(ia2.isMCNodeLocal())
    assertNot(ia2.isMCOrgLocal())
    assertNot(ia2.isMCSiteLocal())

    val ia3 = InetAddress.getByName("250.255.255.254")
    assertNot(ia3.isMCGlobal())
    assertNot(ia3.isMCLinkLocal())
    assertNot(ia3.isMCNodeLocal())
    assertNot(ia3.isMCOrgLocal())
    assertNot(ia3.isMCSiteLocal())

    val ia4 = InetAddress.getByName("10.0.0.2")
    assertNot(ia4.isMCGlobal())
    assertNot(ia4.isMCLinkLocal())
    assertNot(ia4.isMCNodeLocal())
    assertNot(ia4.isMCOrgLocal())
    assertNot(ia4.isMCSiteLocal())
  }

  test("toString") {
    assertEquals("/127.0.0.1", InetAddress.getByName("127.0.0.1").toString)
  }

}
