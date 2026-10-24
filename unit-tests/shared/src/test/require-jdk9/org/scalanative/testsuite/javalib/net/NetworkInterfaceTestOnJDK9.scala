package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

/* Design Notes:
 *    1)  See Design Notes in NetworkInterfaceTest.scala
 */

class NetworkInterfaceTestOnJDK9 {

  val localhostIf =
    if (Platform.isLinux) "lo"
    else "lo0"

  val osIPv6LoopbackSuffix =
    if (Platform.isOpenBSD && Platform.executingInScalaNative)
      s":3:0:0:0:0:0:1%${localhostIf}"
    else if (Platform.isNetBSD)
      s":2:0:0:0:0:0:1%${localhostIf}"
    else
      s":0:0:0:0:0:0:1%${localhostIf}"

  val osIPv6LoopbackAddress =
    if (Platform.isMacOs || Platform.isFreeBSD) s"fe80${osIPv6LoopbackSuffix}"
    else if (Platform.isOpenBSD && Platform.executingInScalaNative)
      s"fe80${osIPv6LoopbackSuffix}"
    else if (Platform.isNetBSD)
      s"fe80${osIPv6LoopbackSuffix}"
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
        val scopeStart = hostAddr.indexOf('%')
        val address =
          if (scopeStart < 0) hostAddr else hostAddr.substring(0, scopeStart)

        // Loopback can be returned with or without a scope identifier.
        // A supplied scope must still identify this loopback interface.
        if (scopeStart >= 0) {
          assertTrue(s"IPv4 address has a scope: ${hostAddr}", address.contains(":"))
          val scope = hostAddr.substring(scopeStart + 1)
          assertTrue(
            s"Unexpected loopback scope: ${hostAddr}",
            scope == localhostIf || scope == lbIf.getIndex().toString
          )
        }

        // BSD loopback interfaces can have both ::1 and a link-local address.
        val expected =
          if (!address.contains(":")) {
            "127.0.0.1"
          } else if (address.startsWith("0")) {
            "0:0:0:0:0:0:0:1"
          } else if (address.startsWith("f")) {
            osIPv6LoopbackAddress.takeWhile(_ != '%')
          } else "" // fail in a way that will print out ifAddrString

        assertEquals(s"Unexpected result: ${hostAddr}", expected, address)
        true
      })
      .count

    /* Out-of-the-box Linux tends to have two addresses, one IPv4 & one IPv6.
     * macOS has three. It adds a link local (fe80) address.
     * Of course, a user may configure their system differently and
     * break this test. Thus, OpenBSD has only one IPv4 address by default.
     */
    val atLeast =
      if (Platform.isOpenBSD || Platform.isNetBSD) 1
      else 2
    assertTrue(s"count ${count} not >= ${atLeast}", count >= atLeast)
  }

}
