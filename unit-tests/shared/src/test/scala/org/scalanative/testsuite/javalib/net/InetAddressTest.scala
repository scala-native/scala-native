package org.scalanative.testsuite.javalib.net

import java.net._

/* Originally ported from Apache Harmony.
 * Extensively modified for Scala Native. Additional test cases added.
 */

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import org.scalanative.testsuite.utils.Platform

class InetAddressTest {

  @Test def equalsShouldWorkOnLocalhostsFromGetByName(): Unit = {
    val ia1 = InetAddress.getByName("127.1")
    val ia2 = InetAddress.getByName("127.0.0.1")
    assertEquals(ia1, ia2)
  }

  @Test def getAddress(): Unit = {
    try {
      val ia = InetAddress.getByName("127.0.0.1")
      val caddr = Array[Byte](127.toByte, 0.toByte, 0.toByte, 1.toByte)
      val addr = ia.getAddress()
      for (i <- addr.indices)
        assertEquals("a1", caddr(i), addr(i))
    } catch {
      case e: UnknownHostException => // OK
    }

    val origBytes = Array[Byte](0.toByte, 1.toByte, 2.toByte, 3.toByte)
    val address = InetAddress.getByAddress(origBytes)
    origBytes(0) = -1
    val newBytes = address.getAddress()
    assertEquals("a2", newBytes(0), 0.toByte)
  }

  @Test def getAllByName(): Unit = {
    val all = InetAddress.getAllByName("localhost")
    assertNotNull("a1", all)
    assertTrue("a1.1", all.length >= 1)

    if (!Platform.isWindows) {
      for (alias <- all) {
        assertTrue("a2", alias.getCanonicalHostName().startsWith("localhost"))
      }
    }

    for (alias <- all)
      assertTrue("a3", alias.getHostName().startsWith("localhost"))

    val ias = InetAddress.getAllByName(null)
    for (ia <- ias)
      assertTrue("a4", ia.isLoopbackAddress())

    // match JVM behavior, not getAllByName("localhost"), which can give size 2
    assertEquals("a4.1", 1, ias.length)

    val ias2 = InetAddress.getAllByName("")
    for (ia <- ias2)
      assertTrue("a5", ia.isLoopbackAddress())

    // match JVM behavior, not getAllByName("localhost"), which can give size 2
    assertEquals("a5.1", 1, ias2.length)

    /* Check that getting addresses by dotted string distinguishes
     * IPv4 and IPv6 subtypes
     */
    val list = InetAddress.getAllByName("192.168.0.1")
    for (addr <- list)
      assertFalse("a6", addr.getClass == classOf[InetAddress])
    assertEquals("a6.1", 1, list.length)
  }

  @Test def getByName(): Unit = {
    val ia = InetAddress.getByName("127.0.0.1") // numeric lookup path

    val ia2 = InetAddress.getByName("localhost") // non-numeric lookup path
    assertEquals("a1", ia, ia2)

    // Test IPv4 archaic variant addresses.
    val i1 = InetAddress.getByName("1.2.3")
    assertEquals("a2", "1.2.0.3", i1.getHostAddress())

    val i2 = InetAddress.getByName("1.2")
    assertEquals("a3", "1.0.0.2", i2.getHostAddress())

    val i3 = InetAddress.getByName(String.valueOf(0xffffffffL))
    assertEquals("a4", "255.255.255.255", i3.getHostAddress())

    // case from 'Comcast/ip4s' project, lookup non-existing host.
    assertThrows(
      "getByName(not.example.com)",
      classOf[UnknownHostException],
      InetAddress.getByName("not.example.com")
    )
  }

  @Test def getByNameInvalidIPv4Addresses(): Unit = {

    assertThrows(
      "getByName(\"240.0.0.\" )",
      classOf[UnknownHostException],
      InetAddress.getByName("240.0.0.")
    )

    // Establish baseline: variant IPv4 address does not throw
    val ia1 = InetAddress.getByName("10")

    /* same address with scope_id is detected as invalid.
     * It is taken as a non-numeric host, which is never found because
     * '%' is not valid in a hostname.
     */
    assertThrows(
      "getByName(\"10%en0\")",
      classOf[UnknownHostException],
      InetAddress.getByName("10%en0")
    )

  }

  @Test def getHostAddress(): Unit = {
    assertEquals(
      "a1",
      "1.3.0.4",
      InetAddress.getByName("1.3.4").getHostAddress()
    )
    assertEquals(
      "a2",
      "0:0:0:0:0:0:0:1",
      InetAddress.getByName("::1").getHostAddress()
    )
  }

  @Test def getHostName(): Unit = {
    /* This test only yields useful information if a capable nameserver
     * is active.
     */

    // he.net - Hurricane Electric, CNAME: www.he.net
    val heNet = "216.218.236.2" // "$dig he.net ANY"
    val hostName = InetAddress.getByName(heNet).getHostName()

    if (Character.isDigit(hostName(0))) {
      // Nothing learned, name server could not resolve name, as can happen.
      assertEquals("a1", heNet, hostName)
    } else {
      assertEquals("a1", "he.net", hostName)
    }
  }

  @Test def getLocalHost(): Unit = {
// One success, can I get two?  i.e. is problem intermittent?
    /* 2023-10-08 12:26 -0400 // LeeT Debugging reported macOS-12 failure. // FIXME
    assumeFalse(
      "Spuriously fails in the CI on MacOS-12",
      Platform.isMacOs && sys.env.contains("CI")
    )
     */ // FIXME // trying to get CI macOs to run.

    /* If compiler does not optimize away, check that no Exception is thrown
     * and something other than null is returned.
     * This code will be run on many machines, with varied names.
     * It is hard to check the actual InetAddress returned.
     */
    assertNotNull(InetAddress.getLocalHost())
  }

  @Test def getLoopbackAddress(): Unit = {
    // Skip testing the "system" case. Save that for some future evolution.
    val useIPv6Addrs =
      System.getProperty("java.net.preferIPv6Addresses", "false")
    val lba = InetAddress.getLoopbackAddress().getHostAddress()

    if (useIPv6Addrs == "true") {
      assertEquals("0:0:0:0:0:0:0:1", lba)
    } else {
      assertEquals("127.0.0.1", lba)
    }
  }

  @Test def isMulticastAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertTrue("ia1", ia1.isMulticastAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse("ia2", ia2.isMulticastAddress())
  }

  @Test def isAnyLocalAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse("ia1", ia1.isAnyLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse("ia2", ia2.isAnyLocalAddress())
  }

  @Test def isLinkLocalAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse("ia1", ia1.isLinkLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse("ia2", ia2.isLinkLocalAddress())
  }

  @Test def isLoopbackAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse("ia1", ia1.isLoopbackAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertTrue("ia2", ia2.isLoopbackAddress())
    val ia3 = InetAddress.getByName("127.0.0.2")
    assertTrue("ia3", ia3.isLoopbackAddress())
  }

  @Test def isReachableIllegalArgument(): Unit = {
    val addr = InetAddress.getByName("127.0.0.1")
    assertThrows(
      "isReachable(-1)",
      classOf[IllegalArgumentException],
      addr.isReachable(-1)
    )
  }

  @Test def isReachable(): Unit = {
    /* Linux disables ICMP requests by default and most addresses do not
     * have echo servers running on port 7, so it's quite difficult
     * to test this method.
     *
     * This test exercises the parts of the code path that it can.
     */

    val addr = InetAddress.getByName("127.0.0.1")
    try {
      addr.isReachable(10) // Unexpected success is OK.
    } catch {
      /* A better test would try to distinguish the varieties of
       * ConnectionException. Local setup, on the network, etc.
       * That would help with supporting users who report problems.
       */
      case ex: ConnectException => // expected, do nothing
      // SocketTimeoutException is thrown only on Windows. OK to do nothing
      case ex: SocketTimeoutException => // do nothing
      // We want to see other timeouts and exception, let them bubble up.

    }
  }

  @Test def isSiteLocalAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse("ia1", ia1.isSiteLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse("ia2", ia2.isSiteLocalAddress())
    val ia3 = InetAddress.getByName("127.0.0.2")
    assertFalse("ia3", ia3.isSiteLocalAddress())
    val ia4 = InetAddress.getByName("243.243.45.3")
    assertFalse("ia4", ia4.isSiteLocalAddress())
    val ia5 = InetAddress.getByName("10.0.0.2")
    assertTrue("ia5", ia5.isSiteLocalAddress())
  }

  @Test def mcMethods(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse("ia1.1", ia1.isMCGlobal())
    assertFalse("ia1.2", ia1.isMCLinkLocal())
    assertFalse("ia1.3", ia1.isMCNodeLocal())
    assertFalse("ia1.4", ia1.isMCOrgLocal())
    assertTrue("ia1.5", ia1.isMCSiteLocal())

    val ia2 = InetAddress.getByName("243.243.45.3")
    assertFalse("ia2.1", ia2.isMCGlobal())
    assertFalse("ia2.2", ia2.isMCLinkLocal())
    assertFalse("ia2.3", ia2.isMCNodeLocal())
    assertFalse("ia2.4", ia2.isMCOrgLocal())
    assertFalse("ia2.5", ia2.isMCSiteLocal())

    val ia3 = InetAddress.getByName("250.255.255.254")
    assertFalse("ia3.1", ia3.isMCGlobal())
    assertFalse("ia3.2", ia3.isMCLinkLocal())
    assertFalse("ia3.3", ia3.isMCNodeLocal())
    assertFalse("ia3.4", ia3.isMCOrgLocal())
    assertFalse("ia3.5", ia3.isMCSiteLocal())

    val ia4 = InetAddress.getByName("10.0.0.2")
    assertFalse("ia4.1", ia4.isMCGlobal())
    assertFalse("ia4.2", ia4.isMCLinkLocal())
    assertFalse("ia4.3", ia4.isMCNodeLocal())
    assertFalse("ia4.4", ia4.isMCOrgLocal())
    assertFalse("ia4.5", ia4.isMCSiteLocal())
  }

  @Test def testToString(): Unit = {
    assertEquals("/127.0.0.1", InetAddress.getByName("127.0.0.1").toString)
  }

}
