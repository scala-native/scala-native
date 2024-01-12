package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.Platform

/* Design Notes:
 *    1)  See Design Notes in NetworkInterfaceTest.scala
 */

class NetworkInterfaceTestOnJDK9 {

  val localhostIf =
    if (Platform.isLinux) "lo"
    else "lo0"

  val osIPv6LoopbackSuffix =
    s":0:0:0:0:0:0:1%${localhostIf}"

  val osIPv6LoopbackAddress =
    if (Platform.isMacOs) s"fe80${osIPv6LoopbackSuffix}"
    else s"0${osIPv6LoopbackSuffix}"

// Test instance method(s)

  @Test def instanceInetAddresses(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)

    val iaStream = lbIf.inetAddresses()

    val count = iaStream
      .filter(e => {

        val hostAddr = e.getHostAddress()

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
        true
      })
      .count

    /* Linux tends to have two addresses, one IPv4 & one IPv6.
     * macOS has three. It adds a link local (fe80) address.
     */
    assertTrue("count > 0", count > 2)
  }

}
