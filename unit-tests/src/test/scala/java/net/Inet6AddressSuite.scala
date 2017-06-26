package java.net

// Ported from Apache Harmony
object Inet6AddressSuite extends tests.Suite {

  test("isMulticastAddress") {
    val addr = InetAddress.getByName("FFFF::42:42")
    assert(addr.isMulticastAddress())

    val addr2 = InetAddress.getByName("42::42:42")
    assertNot(addr2.isMulticastAddress())

    val addr3 = InetAddress.getByName("::224.42.42.42")
    assertNot(addr3.isMulticastAddress())

    val addr4 = InetAddress.getByName("::42.42.42.42")
    assertNot(addr4.isMulticastAddress())

    val addr5 = InetAddress.getByName("::FFFF:224.42.42.42")
    assert(addr5.isMulticastAddress())

    val addr6 = InetAddress.getByName("::FFFF:42.42.42.42")
    assertNot(addr6.isMulticastAddress())
  }

  test("isAnyLocalAddress") {
    val addr = InetAddress.getByName("::0")
    assert(addr.isAnyLocalAddress)

    val addr2 = InetAddress.getByName("::")
    assert(addr2.isAnyLocalAddress)

    val addr3 = InetAddress.getByName("::1")
    assertNot(addr3.isAnyLocalAddress)
  }

  test("isLoopbackAddress") {
    val addr = InetAddress.getByName("::1")
    assert(addr.isLoopbackAddress)

    val addr2 = InetAddress.getByName("::2")
    assertNot(addr2.isLoopbackAddress)

    val addr3 = InetAddress.getByName("::FFFF:127.0.0.0")
    assert(addr3.isLoopbackAddress)
  }

  test("isLinkLocalAddress") {
    val addr = InetAddress.getByName("FE80::0")
    assert(addr.isLinkLocalAddress)

    val addr2 = InetAddress.getByName("FEBF::FFFF:FFFF:FFFF:FFFF")
    assert(addr2.isLinkLocalAddress)

    val addr3 = InetAddress.getByName("FEC0::1")
    assertNot(addr3.isLinkLocalAddress)
  }

  test("isSiteLocalAddress") {
    val addr = InetAddress.getByName("FEC0::0")
    assert(addr.isSiteLocalAddress)

    val addr2 = InetAddress.getByName("FEBF::FFFF:FFFF:FFFF:FFFF:FFFF")
    assertNot(addr2.isSiteLocalAddress)
  }

  test("isIPv4CompatibleAddress") {
    val addr2 =
      InetAddress.getByName("::255.255.255.255").asInstanceOf[Inet6Address]
    assert(addr2.isIPv4CompatibleAddress)
  }

  test("getByAddress") {
    assertThrows[UnknownHostException] {
      Inet6Address.getByAddress("123", null, 0)
    }
    val addr1 = Array[Byte](127.toByte, 0.toByte, 0.toByte, 1.toByte)
    assertThrows[UnknownHostException] {
      Inet6Address.getByAddress("123", addr1, 0)
    }

    val addr2 = Array[Byte](
      0xFE.toByte,
      0x80.toByte,
      0.toByte,
      0.toByte,
      0.toByte,
      0.toByte,
      0.toByte,
      0.toByte,
      0x02.toByte,
      0x11.toByte,
      0x25.toByte,
      0xFF.toByte,
      0xFE.toByte,
      0xF8.toByte,
      0x7C.toByte,
      0xB2.toByte
    )

    Inet6Address.getByAddress("123", addr2, 3)
    Inet6Address.getByAddress("123", addr2, 0)
    Inet6Address.getByAddress("123", addr2, -1)
  }

}
