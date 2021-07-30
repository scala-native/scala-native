package javalib.net

import java.net._

// Ported from Apache Harmony

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

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
        assertEquals(caddr(i), addr(i))
    } catch {
      case e: UnknownHostException => {}
    }

    val origBytes = Array[Byte](0.toByte, 1.toByte, 2.toByte, 3.toByte)
    val address = InetAddress.getByAddress(origBytes)
    origBytes(0) = -1
    val newBytes = address.getAddress()
    assertEquals(newBytes(0), 0.toByte)
  }

  @Test def getAllByName(): Unit = {
    val all = InetAddress.getAllByName("localhost")
    assertFalse(all == null)
    assertTrue(all.length >= 1)

    for (alias <- all)
      assertTrue(alias.getHostName().startsWith("localhost"))

    val ias = InetAddress.getAllByName(null)
    for (ia <- ias)
      assertTrue(ia.isLoopbackAddress())

    val ias2 = InetAddress.getAllByName("")
    for (ia <- ias2)
      assertTrue(ia.isLoopbackAddress())

    // Check that getting addresses by dotted string distingush IPv4 and IPv6 subtypes
    val list = InetAddress.getAllByName("192.168.0.1")
    for (addr <- list)
      assertFalse(addr.getClass == classOf[InetAddress])

  }

  @Test def getByName(): Unit = {
    val ia = InetAddress.getByName("127.0.0.1")

    val i1 = InetAddress.getByName("1.2.3")
    assertEquals("1.2.0.3", i1.getHostAddress())

    val i2 = InetAddress.getByName("1.2")
    assertEquals("1.0.0.2", i2.getHostAddress())

    val i3 = InetAddress.getByName(String.valueOf(0xffffffffL))
    assertEquals("255.255.255.255", i3.getHostAddress())
  }

  @Test def getHostAddress(): Unit = {
    assertEquals("1.3.0.4", InetAddress.getByName("1.3.4").getHostAddress())
    assertEquals(
      "0:0:0:0:0:0:0:1",
      InetAddress.getByName("::1").getHostAddress()
    )
  }

  @Test def isReachable(): Unit = {
    // Linux disables ICMP requests by default and most of the addresses
    // don't have echo servers running on port 7, so it's quite difficult
    // to test this method

    val addr = InetAddress.getByName("127.0.0.1")
    assertThrows(classOf[IllegalArgumentException], addr.isReachable(-1))
  }

  @Test def isMulticastAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertTrue(ia1.isMulticastAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse(ia2.isMulticastAddress())
  }

  @Test def isAnyLocalAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse(ia1.isAnyLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse(ia2.isAnyLocalAddress())
  }

  @Test def isLinkLocalAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse(ia1.isLinkLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse(ia2.isLinkLocalAddress())
  }

  @Test def isLoopbackAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse(ia1.isLoopbackAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertTrue(ia2.isLoopbackAddress())
    val ia3 = InetAddress.getByName("127.0.0.2")
    assertTrue(ia3.isLoopbackAddress())
  }

  @Test def isSiteLocalAddress(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse(ia1.isSiteLocalAddress())
    val ia2 = InetAddress.getByName("localhost")
    assertFalse(ia2.isSiteLocalAddress())
    val ia3 = InetAddress.getByName("127.0.0.2")
    assertFalse(ia3.isSiteLocalAddress())
    val ia4 = InetAddress.getByName("243.243.45.3")
    assertFalse(ia4.isSiteLocalAddress())
    val ia5 = InetAddress.getByName("10.0.0.2")
    assertTrue(ia5.isSiteLocalAddress())
  }

  @Test def mcMethods(): Unit = {
    val ia1 = InetAddress.getByName("239.255.255.255")
    assertFalse(ia1.isMCGlobal())
    assertFalse(ia1.isMCLinkLocal())
    assertFalse(ia1.isMCNodeLocal())
    assertFalse(ia1.isMCOrgLocal())
    assertTrue(ia1.isMCSiteLocal())

    val ia2 = InetAddress.getByName("243.243.45.3")
    assertFalse(ia2.isMCGlobal())
    assertFalse(ia2.isMCLinkLocal())
    assertFalse(ia2.isMCNodeLocal())
    assertFalse(ia2.isMCOrgLocal())
    assertFalse(ia2.isMCSiteLocal())

    val ia3 = InetAddress.getByName("250.255.255.254")
    assertFalse(ia3.isMCGlobal())
    assertFalse(ia3.isMCLinkLocal())
    assertFalse(ia3.isMCNodeLocal())
    assertFalse(ia3.isMCOrgLocal())
    assertFalse(ia3.isMCSiteLocal())

    val ia4 = InetAddress.getByName("10.0.0.2")
    assertFalse(ia4.isMCGlobal())
    assertFalse(ia4.isMCLinkLocal())
    assertFalse(ia4.isMCNodeLocal())
    assertFalse(ia4.isMCOrgLocal())
    assertFalse(ia4.isMCSiteLocal())
  }

  @Test def testToString(): Unit = {
    assertEquals("/127.0.0.1", InetAddress.getByName("127.0.0.1").toString)
  }

}
