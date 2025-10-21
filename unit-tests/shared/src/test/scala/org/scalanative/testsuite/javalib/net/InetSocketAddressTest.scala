package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InetSocketAddressTest {

  @Test def thisStringInt(): Unit = {
    val address = new InetSocketAddress("127.0.0.1", 0)
    assertEquals("/127.0.0.1:0", address.toString)

    /* This section explains deleted lines, so that somebody does not restore
     * them.
     *
     * InetSocketAddress calls InetAddress with a numeric argument to
     * create an underlying InetAddress. The InetAddress so created will have
     * a null host. There is no attempt to resolve the hostname.
     * The address.toString test is correct in expecting a empty_string
     * hostname (left of the slash).
     *
     * 'address.getHostName'will attempt to resolve the hostname if it has not
     * been resolved before. Recall that at creation the hostname was not
     * resolved.
     *
     * Almost all systems the IPv4 loopback address will resolve to
     * "localhost". Only a tiny minority of systems are configured otherwise.
     * This makes the test below chancy at best and better called invalid.
     *
     *   val localhostName = address.getHostName
     *   assertFalse(localhostName == null)
     *   assertEquals(localhostName + "/127.0.0.1:0", address.toString)
     *
     * The bug is that this test ever passed in the wild.
     */
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
