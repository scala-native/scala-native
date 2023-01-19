package org.scalanative.testsuite.javalib.net

import java.net._

// Ported from Apache Harmony

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class Inet6AddressTest {

  @Test def getByNameIPv6ScopedZoneId(): Unit = {

    // Establish baseline: valid address does not throw
    val ia1 = InetAddress.getByName("::1")
    assertEquals("/0:0:0:0:0:0:0:1", ia1.toString())

    // Numeric address with numeric scope id does not throw.
    val ia2 = InetAddress.getByName("::1%99")
    assertEquals("/0:0:0:0:0:0:0:1%99", ia2.toString()) // shows proper zoneId

    /* Scala JVM has a large number of corner cases where it throws an
     * Exception when an interface (a.k.a scope) id is not valid.
     * It is simply not economic to try to match the early/late timing
     * and message of those conditions.
     *
     * Test here that an Exception _is_ thrown in a known case where
     * ScalaJVM on some operating systems throws one.
     */

    if (!Platform.isMacOs) {
      // Invalid interface name does throw.
      assertThrows(
        "getByName(\"::1%bogus\")",
        classOf[UnknownHostException],
        InetAddress.getByName("::1%bogus")
      )
    }
  }

  @Test def isMulticastAddress(): Unit = {
    val addr = InetAddress.getByName("FFFF::42:42")
    assertTrue("a1", addr.isMulticastAddress())

    val addr2 = InetAddress.getByName("42::42:42")
    assertFalse("a2", addr2.isMulticastAddress())

    val addr3 = InetAddress.getByName("::224.42.42.42")
    assertFalse("a3", addr3.isMulticastAddress())

    val addr4 = InetAddress.getByName("::42.42.42.42")
    assertFalse("a4", addr4.isMulticastAddress())

    val addr5 = InetAddress.getByName("::FFFF:224.42.42.42")
    assertTrue("a5", addr5.isMulticastAddress())

    val addr6 = InetAddress.getByName("::FFFF:42.42.42.42")
    assertFalse("a6", addr6.isMulticastAddress())
  }

  @Test def isAnyLocalAddress(): Unit = {
    val addr = InetAddress.getByName("::0")
    assertTrue("a1", addr.isAnyLocalAddress)

    val addr2 = InetAddress.getByName("::")
    assertTrue("a2", addr2.isAnyLocalAddress)

    val addr3 = InetAddress.getByName("::1")
    assertFalse("a3", addr3.isAnyLocalAddress)
  }

  @Test def isLoopbackAddress(): Unit = {
    val addr = InetAddress.getByName("::1")
    assertTrue("a1", addr.isLoopbackAddress)

    val addr2 = InetAddress.getByName("::2")
    assertFalse("a2", addr2.isLoopbackAddress)

    val addr3 = InetAddress.getByName("::FFFF:127.0.0.0")
    assertTrue("a3", addr3.isLoopbackAddress)
  }

  @Test def isLinkLocalAddress(): Unit = {
    val addr = InetAddress.getByName("FE80::0")
    assertTrue("a1", addr.isLinkLocalAddress)

    val addr2 = InetAddress.getByName("FEBF::FFFF:FFFF:FFFF:FFFF")
    assertTrue("a2", addr2.isLinkLocalAddress)

    val addr3 = InetAddress.getByName("FEC0::1")
    assertFalse("a3", addr3.isLinkLocalAddress)
  }

  @Test def isSiteLocalAddress(): Unit = {
    val addr = InetAddress.getByName("FEC0::0")
    assertTrue("a1", addr.isSiteLocalAddress)

    val addr2 = InetAddress.getByName("FEBF::FFFF:FFFF:FFFF:FFFF:FFFF")
    assertFalse("a2", addr2.isSiteLocalAddress)
  }

  @Test def isIPv4CompatibleAddress(): Unit = {
    val addr2 =
      InetAddress.getByName("::255.255.255.255").asInstanceOf[Inet6Address]
    assertTrue(addr2.isIPv4CompatibleAddress)
  }

  @Test def getByAddress(): Unit = {
    assertThrows(
      "getByAddress(\"123\" , null, 0)",
      classOf[UnknownHostException],
      Inet6Address.getByAddress("123", null, 0)
    )

    // Lookup IPv4 as an non-mapped IPv6, should fail
    val addr1 = Array[Byte](127.toByte, 0.toByte, 0.toByte, 1.toByte)
    assertThrows(
      "getByAddress(null, Array[Byte](127, 0, 0, 1), 0)",
      classOf[UnknownHostException],
      Inet6Address.getByAddress(null, addr1, 0)
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

    // Test specifying IPv6 scope_id. Is scope_id durable?

    val scope_0 = 0
    val addr3 = Inet6Address.getByAddress("125", addr2, scope_0)
    assertEquals(scope_0, addr3.getScopeId())

    val scope_minus1 = -1
    val addr4 = Inet6Address.getByAddress("126", addr2, scope_minus1)
    assertEquals(scope_0, addr4.getScopeId()) // yes, scope_0

    val scope_3 = 3
    val addr5 = Inet6Address.getByAddress("124", addr2, scope_3)
    assertEquals(scope_3, addr5.getScopeId())
  }

  // Issue 2313
  @Test def trailing0NotLost(): Unit = {
    val addr = InetAddress.getByName("1c1e::")
    assertTrue(addr.getHostAddress().endsWith("0"))
  }

}
