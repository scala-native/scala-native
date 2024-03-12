package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.util.function.Consumer

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

class InterfaceAddressTest {

  /* The tests in this class depend upon a competent NetworkInterface class.
   * They also assume, perhaps unwisely, that the loopback address has index 1.
   */

  val loopbackIfName =
    if (Platform.isLinux) "lo"
    else "lo0"

  val loopbackIfIndex =
    if (Platform.isFreeBSD || Platform.isNetBSD) 2
    else if (Platform.isOpenBSD) 3
    else 1

  val osIPv6PrefixLength =
    if ((Platform.isMacOs) || (Platform.isFreeBSD) || (Platform.isOpenBSD) || (Platform.isNetBSD))
      64
    else 128

  val osIPv6LoopbackSuffix =
    if (Platform.isOpenBSD)
      s":3:0:0:0:0:0:1%${loopbackIfName}"
    else if (Platform.isNetBSD)
      s":2:0:0:0:0:0:1%${loopbackIfName}"
    else
      s":0:0:0:0:0:0:1%${loopbackIfName}"

  val osIPv6LoopbackAddress =
    if ((Platform.isMacOs) || (Platform.isFreeBSD) || (Platform.isOpenBSD) || (Platform.isNetBSD))
      s"fe80${osIPv6LoopbackSuffix}"
    else
      s"0${osIPv6LoopbackSuffix}"

  /* Test equals() but there is no good, simple way to test corresponding
   * hashCode(). The contents of the components used in the hash vary by
   * operating system.
   *
   * equals() calls hashCode() showing that the latter at least executes.
   */
  @Test def testEquals(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val netIf = NetworkInterface.getByIndex(loopbackIfIndex)
    assertNotNull(netIf)

    val ifAddresses = netIf.getInterfaceAddresses()
    assertTrue("No InterfaceAddress found", ifAddresses.size > 0)

    assumeTrue("not enough ifAddresses for test", ifAddresses.size >= 2)

    val ifa1 = ifAddresses.get(0)
    val ifa2 = ifAddresses.get(1)

    assertEquals("InterfaceAddress equal", ifa1, ifa1)
    assertNotEquals("InterfaceAddress not equal", ifa1, ifa2)
  }

  @Test def testGetAddress(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val netIf = NetworkInterface.getByIndex(loopbackIfIndex)
    assertNotNull(netIf)

    val ifAddresses = netIf.getInterfaceAddresses()
    assertTrue("No InterfaceAddress found", ifAddresses.size > 0)

    // Scala 2.11 demands this gronking forEach idiom.
    val consumer = new Consumer[InterfaceAddress] {
      def accept(addr: InterfaceAddress): Unit = {
        val hostAddr = addr.getAddress().getHostAddress()
        // macOS can have two forms of IPv6 loopback address.
        val expected =
          if (!hostAddr.contains(":")) {
            "127.0.0.1"
          } else if (hostAddr.startsWith("0")) {
            val suffix =
              if (Platform.isFreeBSD) ""
              else s"%${loopbackIfName}"
            s"0:0:0:0:0:0:0:1${suffix}"
          } else if (hostAddr.startsWith("f")) {
            s"${osIPv6LoopbackAddress}"
          } else "" // fail in a way that will print out ifAddrString

        assertEquals("Unexpected result", expected, hostAddr)
      }
    }

    ifAddresses.forEach(consumer)
  }

  /*  @Test def testGetBroadcast(): Unit = {}
   *  Not implemented - system dependent.
   *  Loopback addresses have not broadcast address to get.
   *  Non-loopback primary interface varies and can not be determined.
   */

  @Test def testGetNetworkPrefixLength(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val netIf = NetworkInterface.getByIndex(loopbackIfIndex)
    assertNotNull(netIf)

    val ifAddresses = netIf.getInterfaceAddresses()
    assertTrue("No InterfaceAddress found", ifAddresses.size > 0)

    // Scala 2.11 demands this gronking forEach idiom.
    val consumer = new Consumer[InterfaceAddress] {
      def accept(addr: InterfaceAddress): Unit = {
        val ia = addr.getAddress().getAddress()
        val len = ia.length

        val expected =
          if (len == 4) 8.toShort // IPv4
          else if (len != 16) -1.toShort // fail but print prefixLen
          else if (ia(0) == 0) 128.toShort // Linux & macOS ::1 form
          else osIPv6PrefixLength.toShort // macOs ff80::1 form

        val prefixLen = addr.getNetworkPrefixLength()
        assertEquals("unexpected prefix length", expected, prefixLen)
      }
    }

    ifAddresses.forEach(consumer)
  }

  @Test def testLoopbackToString(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    /* The toString should have the form:
     *     InetAddress / prefix length [ broadcast address ]
     */

    val netIf = NetworkInterface.getByIndex(loopbackIfIndex)
    assertNotNull(netIf)

    val ifAddresses = netIf.getInterfaceAddresses()
    assertTrue("No InterfaceAddress found", ifAddresses.size > 0)

    // Scala 2.11 demands this gronking forEach idiom.
    val consumer = new Consumer[InterfaceAddress] {
      def accept(addr: InterfaceAddress): Unit = {
        val ifAddrString = addr.toString

        // macOS can have two forms of IPv6 loopback address.
        val expected =
          if (!ifAddrString.contains(":")) {
            "/127.0.0.1/8 [null]"
          } else if (ifAddrString.startsWith("/0")) {
            val stem =
              if (Platform.isFreeBSD) ""
              else s"%${loopbackIfName}"
            s"/0:0:0:0:0:0:0:1${stem}/128 [null]"
          } else if (ifAddrString.startsWith("/f")) {
            s"/${osIPv6LoopbackAddress}/${osIPv6PrefixLength} [null]"
          } else "" // fail in a way that will print out ifAddrString

        assertEquals("InterfaceAddress", expected, ifAddrString)
      }
    }

    ifAddresses.forEach(consumer)
  }

}
