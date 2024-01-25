package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.BeforeClass

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

object NetworkInterfaceTest {
  @BeforeClass
  def beforeClass(): Unit = {

//    assumeFalse(
    assumeTrue(
      "Test has not yet been configured for FreeBSD",
      Platform.isFreeBSD
    )

    assumeFalse("Not implemented in Windows", Platform.isWindows)
  }
}

class NetworkInterfaceTest {

  val loopbackIfName =
    if (Platform.isLinux) "lo"
    else "lo0"

  val loopbackIfIndex =
    if (Platform.isFreeBSD) 2
    else 1

  val osIPv6LoopbackSuffix =
    s":0:0:0:0:0:0:1%${loopbackIfName}"

  val osIPv6LoopbackAddress =
    if ((Platform.isMacOs) || (Platform.isFreeBSD))
      s"fe80${osIPv6LoopbackSuffix}"
    else
      s"0${osIPv6LoopbackSuffix}"

// Test static (object) methods

  @Test def getByIndexMinusTwo(): Unit = {
    assertThrows(
      "getByIndex(-2)",
      classOf[IllegalArgumentException],
      NetworkInterface.getByIndex(-2)
    )
  }

  @Test def getByIndexZero(): Unit = {
    assertNull(NetworkInterface.getByIndex(0))
  }

  @Test def getByIndexOne(): Unit = {
    val netIf = NetworkInterface.getByIndex(1)

    assertNotNull("a1", netIf)

    val sought =
      if (Platform.isFreeBSD) "em0"
      else loopbackIfName
    val ifName = netIf.getName()
    assertEquals("a2", sought, ifName)
  }

  @Test def getByIndexMaxValue(): Unit = {
    val netIf = NetworkInterface.getByIndex(Integer.MAX_VALUE)
    assertNull("Unlikely interface found for MAX_VALUE index", netIf)
  }

  @Test def getByInetAddressNull(): Unit = {
    assertThrows(
      "getByInetAddress(null)",
      classOf[NullPointerException],
      NetworkInterface.getByInetAddress(null)
    )
  }

  @Test def getByInetAddressLoopbackIPv4(): Unit = {
    val lba4 = InetAddress.getByName("127.0.0.1")

    val netIf = NetworkInterface.getByInetAddress(lba4)

    assertNotNull("a1", netIf)

    val sought = loopbackIfName
    val ifName = netIf.getName()
    assertEquals("a1", sought, ifName)
  }

  @Test def getByInetAddressLoopbackIPv6(): Unit = {
    val lba6 = InetAddress.getByName("::1")

    val netIf = NetworkInterface.getByInetAddress(lba6)

    // Do not fail on null. IPv6 might not be enabled on the system.
    if (netIf != null) {
      val sought = loopbackIfName
      val ifName = netIf.getName()
      assertEquals("a1", sought, ifName)
    }
  }

  @Test def getByNameNull(): Unit = {
    assertThrows(
      "getByName(null)",
      classOf[NullPointerException],
      NetworkInterface.getByName(null)
    )
  }

  @Test def getByName(): Unit = {
    val sought = loopbackIfName
    val netIf = NetworkInterface.getByName(sought)
    assertNotNull(netIf)

    val ifName = netIf.getName()

    assertEquals("a1", sought, ifName)
  }

  @Test def testToString(): Unit = {
    val netIf = NetworkInterface.getByIndex(loopbackIfIndex)
    assertNotNull(netIf)

    val ifName = netIf.getName()

    // "lo" is Linux, systemd, "lo0" is macOS
    if ((ifName == "lo") || (ifName == "lo0")) {
      assertEquals("a1", s"name:${ifName} (${ifName})", netIf.toString)
    } // else unknown configuration; skip, not fail.
  }

  @Test def getNetworkInterfaces(): Unit = {
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
    val lbIf1 = NetworkInterface.getByName(loopbackIfName)
    assertNotNull(lbIf1)
    assertEquals(loopbackIfIndex, lbIf1.getIndex())
  }

  /*  @Test def instanceGetHardwareAddress(): Unit = {
   *  Not implemented - system dependent.
   *  Loopback addresses do not have hardware address to get.
   *  Non-loopback primary interface varies and can not be determined.
   */

  @Test def instanceGetMTU(): Unit = {
    val lbIf = NetworkInterface.getByName(loopbackIfName)
    assertNotNull(lbIf)

    val mtu = lbIf.getMTU()

    // To get tighter bounds, one would need to know config specific info.
    assertTrue("mtu > 0", mtu > 0)
    assertTrue("mtu <= 65536", mtu <= 65536)
  }

  @Test def instanceIsLoopback(): Unit = {
    val lbIf = NetworkInterface.getByName(loopbackIfName)
    assertNotNull(lbIf)
    assertEquals("a1", true, lbIf.isLoopback())
  }

  @Test def instanceIsPoinToPoint(): Unit = {
    val lbIf = NetworkInterface.getByName(loopbackIfName)
    assertNotNull(lbIf)
    assertEquals("a1", false, lbIf.isPointToPoint())
  }

  @Test def instanceIsUp(): Unit = {
    val lbIf = NetworkInterface.getByName(loopbackIfName)
    assertNotNull(lbIf)
    assertEquals("a1", true, lbIf.isUp())
  }

  @Test def instanceSupportsMulticast(): Unit = {
    val lbIf = NetworkInterface.getByName(loopbackIfName)
    assertNotNull(lbIf)

    val expected =
      if ((Platform.isMacOs) || (Platform.isFreeBSD)) true
      else false // Linux

    assertEquals("a1", expected, lbIf.supportsMulticast())
  }

  @Test def instanceGetInetAddresses(): Unit = {
    val lbIf = NetworkInterface.getByName(loopbackIfName)
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
          s"0:0:0:0:0:0:0:1%${loopbackIfName}"
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
