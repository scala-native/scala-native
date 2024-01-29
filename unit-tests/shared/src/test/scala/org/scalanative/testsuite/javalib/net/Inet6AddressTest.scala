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

  // Issue 2911
  @Test def shouldUseOnlyLowercaseHexDigits(): Unit = {
    val addr = InetAddress.getByName("FEBF::ABCD:EF01:2345:67AB:CDEF")
    assertNotNull("InetAddress.getByName() failed to find name", addr)

    val addrString = addr.getHostAddress()

    // All JVM non-numeric hexadecimal digits are lowercase. Require the same.
    val hexDigitsAreAllLowerCase = addrString
      .forall(ch => (Character.isDigit(ch) || "abcdef:".contains(ch)))

    assertTrue(
      s"Not all hex characters in ${addrString} are lower case",
      hexDigitsAreAllLowerCase
    )
  }

  // Issue 3707
  @Test def hashcodeShouldBeRobustToNullHostnames(): Unit = {
    /* hashCode() was throwing NullPointerException when the Inet6Address
     * was created with an explicitly null hostname. If such creation
     * _can_ be done, it _will_ be done in the wild.
     *
     * Use the == method to test both itself & the hashCode it uses internally.
     */

    val addrBytes = Array[Byte](
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

    val commonScopeId = 41 // Use an arbitrary non-zero positive number.

    val addr6_1 = Inet6Address.getByAddress(null, addrBytes, commonScopeId)
    val addr6_2 = Inet6Address.getByAddress(null, addrBytes, commonScopeId)

    // make addrs differ. Pick an arbitrary byte & arbitrary different value.
    val differentAddrBytes = addrBytes.clone() // ensure different arrays
    differentAddrBytes(14) = 0xff.toByte
    val addr6_3 =
      Inet6Address.getByAddress(null, differentAddrBytes, commonScopeId)

    assertNotNull("addr6_1", addr6_1)
    assertNotNull("addr6_2", addr6_2)
    assertNotNull("addr6_3", addr6_3)

    assertEquals(
      "hashCodes addr6_1 & addr6_2 ",
      addr6_1.hashCode(),
      addr6_2.hashCode()
    )

    /* Careful here!
     *   One would expect the "assertTrue" here and the corresponding
     *   "assertFalse" statements here to be "assertEquals" &
     *   "assertNotEquals" so that the arguments would get printed out
     *   on failure. That speeds debugging.
     *
     *   Unfortunately, "assertEquals" and "assertNotEquals" are not
     *   useful here.
     *
     *   The addresses in this test have a strictly positive scope_id
     *   by intent. That will cause, say, addr6_1.toString() to create
     *   a string containing the '%' character.
     *
     *   Both Scala JVM and Native have difficulties formatting
     *   the '%' when used in a string interpolator. "assertEquals"
     *   and "assertNotEquals" appear to use a string interpolator
     *   and fail when given the '%'.
     */

    assertTrue("expected addr6_1 & addr6_2 to be ==", addr6_1 == addr6_2)

    assertNotEquals(
      "hashCodes addr6_1 & addr6_3",
      addr6_1.hashCode(),
      addr6_3.hashCode()
    )

    assertFalse("expected addr6_1 & addr6_3 to be !=", addr6_1 == addr6_3)
  }

  // Issue 3708
  @Test def constructorIpAddressShouldBeImmutable(): Unit = {
    val addrBytes = Array[Byte](
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

    val commonScopeId = 43 // Use an arbitrary non-zero positive number.

    val addr6_1 = Inet6Address.getByAddress(null, addrBytes, commonScopeId)
    val addr6_2 = Inet6Address.getByAddress(null, addrBytes, commonScopeId)

    // Mutate common array. Pick an arbitrary index & arbitrary different value
    val differentAddrBytes = addrBytes // mutate common array.
    addrBytes(14) = 0xff.toByte
    val addr6_3 =
      Inet6Address.getByAddress(null, addrBytes, commonScopeId)

    assertNotNull("addr6_1", addr6_1)
    assertNotNull("addr6_2", addr6_2)
    assertNotNull("addr6_3", addr6_3)

    /* Careful here!
     *   See comment about difficulties using "assertEquals" &
     *   "assertNotEquals" with strings containing the '%' character
     *   in Test hashcodeShouldBeRobustToNullHostnames() above.
     */

    assertTrue("expected addr6_1 & addr6_2 to be ==", addr6_1 == addr6_2)

    assertFalse("expected addr6_1 & addr6_3 to be !=", addr6_1 == addr6_3)
  }

}
