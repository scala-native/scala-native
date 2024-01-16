package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

/* Design Notes:
 *    1) As the underlying implementation is Unix only, so are these Tests.
 *
 *    2) Network interface configuration is can and does vary greatly from
 *       system to system. These tests are written to succeed with the
 *       configuration used by the Scala Native Continuous Integration systems.
 *
 *       They may fail if used outside of that environment and require
 *       editing to reflect that local configuration.
 */

class NetworkInterfaceTest {

  val localhostIf =
    if (Platform.isLinux) "lo"
    else "lo0"

  val osIPv6LoopbackSuffix =
    s":0:0:0:0:0:0:1%${localhostIf}"

  val osIPv6LoopbackAddress =
    if (Platform.isMacOs) s"fe80${osIPv6LoopbackSuffix}"
    else s"0${osIPv6LoopbackSuffix}"

// Test static (object) methods

  @Test def getByIndexMinusTwo(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    assertThrows(
      "getByIndex(-2)",
      classOf[IllegalArgumentException],
      NetworkInterface.getByIndex(-2)
    )
  }

  @Test def getByIndexZero(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    assertNull(NetworkInterface.getByIndex(0))
  }

  @Test def getByIndexOne(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    val netIf = NetworkInterface.getByIndex(1) // loopback

    assertNotNull("a1", netIf)

    val sought = localhostIf
    val ifName = netIf.getName()
    assertEquals("a2", sought, ifName)
  }

  @Test def getByIndexMaxValue(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    val netIf = NetworkInterface.getByIndex(Integer.MAX_VALUE)
    assertNull("Unlikely interface found for MAX_VALUE index", netIf)
  }

  @Test def getByInetAddressNull(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    assertThrows(
      "getByInetAddress(null)",
      classOf[NullPointerException],
      NetworkInterface.getByInetAddress(null)
    )
  }

  @Test def getByInetAddressLoopbackIPv4(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    val lba4 = InetAddress.getByName("127.0.0.1")

    val netIf = NetworkInterface.getByInetAddress(lba4)

    assertNotNull("a1", netIf)

    val sought = localhostIf
    val ifName = netIf.getName()
    assertEquals("a1", sought, ifName)
  }

  @Test def getByInetAddressLoopbackIPv6(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    val lba6 = InetAddress.getByName("::1")

    val netIf = NetworkInterface.getByInetAddress(lba6)

    // Do not fail on null. IPv6 might not be enabled on the system.
    if (netIf != null) {
      val sought = localhostIf
      val ifName = netIf.getName()
      assertEquals("a1", sought, ifName)
    }
  }

  @Test def getByNameNull(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)
    assertThrows(
      "getByName(null)",
      classOf[NullPointerException],
      NetworkInterface.getByName(null)
    )
  }

  @Test def getByName(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val sought = localhostIf
    val netIf = NetworkInterface.getByName(sought)
    assertNotNull(netIf)

    val ifName = netIf.getName()

    assertEquals("a1", sought, ifName)
  }

  @Test def testToString(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val netIf = NetworkInterface.getByIndex(1) // loopback
    assertNotNull(netIf)

    val ifName = netIf.getName()

    // "lo" is Linux, systemd, "lo0" is macOS
    if ((ifName == "lo") || (ifName == "lo0")) {
      assertEquals("a1", s"name:${ifName} (${ifName})", netIf.toString)
    } // else unknown configuration; skip, not fail.
  }

  @Test def getNetworkInterfaces(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val netIfs = NetworkInterface.getNetworkInterfaces()
    assertNotNull(netIfs)

    var count = 0

    while (netIfs.hasMoreElements()) {
      netIfs.nextElement()
      count += 1
    }

    // count != 0 1 for loopback, 1 for World and possibly many more (macOS).
    assertTrue("count >= 2", count >= 2)
  }

// Test instance methods

  @Test def instanceGetIndex(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf1 = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf1)
    assertEquals(1, lbIf1.getIndex())
  }

  /*  @Test def instanceGetHardwareAddress(): Unit = {
   *  Not implemented - system dependent.
   *  Loopback addresses do not have hardware address to get.
   *  Non-loopback primary interface varies and can not be determined.
   */

  @Test def instanceGetMTU(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)

    val mtu = lbIf.getMTU()

    // To get tighter bounds, one would need to know config specific info.
    assertTrue("mtu > 0", mtu > 0)
    assertTrue("mtu <= 65536", mtu <= 65536)
  }

  @Test def instanceIsLoopback(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)
    assertEquals("a1", true, lbIf.isLoopback())
  }

  @Test def instanceIsPoinToPoint(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)
    assertEquals("a1", false, lbIf.isPointToPoint())
  }

  @Test def instanceIsUp(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)
    assertEquals("a1", true, lbIf.isUp())
  }

  @Test def instanceSupportsMulticast(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)

    val expected =
      if (Platform.isMacOs) true
      else false // Linux
    // else (FreeBSD?)

    assertEquals("a1", expected, lbIf.supportsMulticast())
  }

  @Test def instanceGetInetAddresses(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)

    val iaEnumeration = lbIf.getInetAddresses()

    var count = 0
    while (iaEnumeration.hasMoreElements()) {
      val hostAddr = iaEnumeration.nextElement().getHostAddress()
      count += 1

      // macOS can have two forms of IPv6 loopback address.
      val expected =
        if (!hostAddr.contains(":")) {
          "127.0.0.1"
        } else if (hostAddr.startsWith("0")) {
          s"0:0:0:0:0:0:0:1%${localhostIf}"
        } else if (hostAddr.startsWith("f")) {
          s"${osIPv6LoopbackAddress}"
        } else "" // fail in a way that will print out ifAddrString

      assertEquals("Unexpected result", expected, hostAddr)
    }

    assertTrue("count > 0", count > 0)
  }

  /* NetworkInterface#getInterfaceAddresses() is exercised in
   * InternetAddressTest#testGetAddress()
   */
}
