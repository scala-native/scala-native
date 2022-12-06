package javalib.net

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

// Test instance method(s)

  @Test def instanceInetAddresses(): Unit = {
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    val lbIf = NetworkInterface.getByName(localhostIf)
    assertNotNull(lbIf)

    // SN Stream implements neither Stream.count() nor Stream.reduce()
    val iaStream = lbIf.inetAddresses()

    var count = 0

    val itr = iaStream.iterator()

    while (itr.hasNext()) {
      itr.next()
      count += 1
    }

    assertTrue("count > 0", count > 0)
  }

}
