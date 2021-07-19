package javalib.net

import java.net._

// Ported from Apache Harmony

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class Inet6AddressTest {

  @Test def isMulticastAddress(): Unit = {
    val addr = InetAddress.getByName("FFFF::42:42")
    assertTrue(addr.isMulticastAddress())

    val addr2 = InetAddress.getByName("42::42:42")
    assertFalse(addr2.isMulticastAddress())

    val addr3 = InetAddress.getByName("::224.42.42.42")
    assertFalse(addr3.isMulticastAddress())

    val addr4 = InetAddress.getByName("::42.42.42.42")
    assertFalse(addr4.isMulticastAddress())

    val addr5 = InetAddress.getByName("::FFFF:224.42.42.42")
    assert(addr5.isMulticastAddress())

    val addr6 = InetAddress.getByName("::FFFF:42.42.42.42")
    assertFalse(addr6.isMulticastAddress())
  }

  @Test def isAnyLocalAddress(): Unit = {
    val addr = InetAddress.getByName("::0")
    assert(addr.isAnyLocalAddress)

    val addr2 = InetAddress.getByName("::")
    assert(addr2.isAnyLocalAddress)

    val addr3 = InetAddress.getByName("::1")
    assertFalse(addr3.isAnyLocalAddress)
  }

  @Test def isLoopbackAddress(): Unit = {
    val addr = InetAddress.getByName("::1")
    assert(addr.isLoopbackAddress)

    val addr2 = InetAddress.getByName("::2")
    assertFalse(addr2.isLoopbackAddress)

    val addr3 = InetAddress.getByName("::FFFF:127.0.0.0")
    assert(addr3.isLoopbackAddress)
  }

  @Test def isLinkLocalAddress(): Unit = {
    val addr = InetAddress.getByName("FE80::0")
    assert(addr.isLinkLocalAddress)

    val addr2 = InetAddress.getByName("FEBF::FFFF:FFFF:FFFF:FFFF")
    assert(addr2.isLinkLocalAddress)

    val addr3 = InetAddress.getByName("FEC0::1")
    assertFalse(addr3.isLinkLocalAddress)
  }

  @Test def isSiteLocalAddress(): Unit = {
    val addr = InetAddress.getByName("FEC0::0")
    assert(addr.isSiteLocalAddress)

    val addr2 = InetAddress.getByName("FEBF::FFFF:FFFF:FFFF:FFFF:FFFF")
    assertFalse(addr2.isSiteLocalAddress)
  }

  @Test def isIPv4CompatibleAddress(): Unit = {
    val addr2 =
      InetAddress.getByName("::255.255.255.255").asInstanceOf[Inet6Address]
    assert(addr2.isIPv4CompatibleAddress)
  }

  @Test def getByAddress(): Unit = {
    assertThrows(
      classOf[UnknownHostException],
      Inet6Address.getByAddress("123", null, 0)
    )
    val addr1 = Array[Byte](127.toByte, 0.toByte, 0.toByte, 1.toByte)
    assertThrows(
      classOf[UnknownHostException],
      Inet6Address.getByAddress("123", addr1, 0)
    )

    val addr2 = Array[Byte](
      0xfe.toByte,
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
      0xff.toByte,
      0xfe.toByte,
      0xf8.toByte,
      0x7c.toByte,
      0xb2.toByte
    )

    Inet6Address.getByAddress("123", addr2, 3)
    Inet6Address.getByAddress("123", addr2, 0)
    Inet6Address.getByAddress("123", addr2, -1)
  }

}
