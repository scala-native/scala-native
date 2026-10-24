package org.scalanative.testsuite.javalib.net

import java.net.{InetAddress, UnknownHostException}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.scalanative.meta.LinktimeInfo

// These boundaries exercise Scala Native's Bionic fallback, not the host's
// resolver or a JVM's version-dependent treatment of numeric hostnames.
class InetAddressLegacyIPv4Test {
  @Test def getByNameLegacyIPv4Bounds(): Unit = {
    assumeTrue("Bionic IPv4 literal fallback", LinktimeInfo.isAndroid)
    if (LinktimeInfo.isAndroid) {
      for ((host, expected) <- Seq(
            "4294967295" -> "255.255.255.255",
            "1.16777215" -> "1.255.255.255",
            "1.2.65535" -> "1.2.255.255"
          )) {
        assertEquals(
          host,
          expected,
          InetAddress.getByName(host).getHostAddress()
        )
        val all = InetAddress.getAllByName(host)
        assertEquals(1, all.length)
        assertEquals(
          host,
          expected,
          all.head.getHostAddress()
        )
      }
      for (host <- Seq("4294967296", "256.1", "1.16777216", "1.2.65536")) {
        assertThrows(
          host,
          classOf[UnknownHostException],
          InetAddress.getByName(host)
        )
        assertThrows(
          host,
          classOf[UnknownHostException],
          InetAddress.getAllByName(host)
        )
      }
    }
  }

}
