package javalib.net

import java.net._

// Ported from Apache Harmony

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class InetSocketAddressTest {

  @Test def thisStringInt(): Unit = {
    val address = new InetSocketAddress("127.0.0.1", 0)
    assertEquals("/127.0.0.1:0", address.toString)
    val localhostName = address.getHostName
    assertFalse(localhostName == null)
    assertEquals(localhostName + "/127.0.0.1:0", address.toString)
  }

  @Test def createUnresolved(): Unit = {
    val pairs = Array(
      ("127.0.0.1", 1234),
      ("192.168.0.1", 10000),
      ("127.0.0", 0),
      ("127.0.0", 65535),
      ("strange host", 65535)
    )
    for ((host, port) <- pairs) {
      val addr = InetSocketAddress.createUnresolved(host, port)
      assertTrue(addr.isUnresolved)
      assertTrue(addr.getAddress == null)
      assertEquals(addr.getHostString, host)
      assertEquals(addr.getHostName, host)
      assertEquals(addr.getPort, port)
    }
  }

  @Test def createUnresolvedShouldThrowIllegalArgumentException(): Unit = {
    val pairs = Array((null, 1), ("host", -1), ("host", 65536))
    for ((host, port) <- pairs) {
      assertThrows(
        classOf[IllegalArgumentException],
        InetSocketAddress.createUnresolved(host, port)
      )
    }
  }
}
