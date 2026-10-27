package org.scalanative.testsuite.javalib.net

import java.net.NetworkInterface

import org.junit.Assert._

private[net] object LoopbackAddressTestUtils {
  def withoutScope(hostAddress: String, loopback: NetworkInterface): String = {
    val scopeStart = hostAddress.indexOf('%')
    if (scopeStart < 0) hostAddress
    else {
      val address = hostAddress.substring(0, scopeStart)
      assertTrue(
        s"IPv4 address has a scope: $hostAddress",
        address.contains(":")
      )
      val scope = hostAddress.substring(scopeStart + 1)
      assertTrue(
        s"Unexpected loopback scope: $hostAddress",
        scope == loopback.getName() || scope == loopback.getIndex().toString
      )
      address
    }
  }
}
