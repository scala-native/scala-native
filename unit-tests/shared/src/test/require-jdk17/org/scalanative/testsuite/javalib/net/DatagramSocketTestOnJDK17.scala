package org.scalanative.testsuite.javalib.net

import java.net.{
  DatagramSocket, Inet4Address, Inet6Address, InetAddress, InetSocketAddress,
  NetworkInterface, SocketException
}

import scala.collection.JavaConverters._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class DatagramSocketTestOnJDK17 {

  private def group(host: String) =
    new InetSocketAddress(InetAddress.getByName(host), 0)

  private def multicastInterface(
      hasAddress: InetAddress => Boolean
  ): NetworkInterface = {
    val candidates = NetworkInterface
      .getNetworkInterfaces()
      .asScala
      .filter { ni =>
        ni.isUp() && ni.supportsMulticast() &&
        ni.getInetAddresses().asScala.exists(hasAddress)
      }
      .toList
    assumeTrue("no multicast capable interface", candidates.nonEmpty)
    candidates.find(_.isLoopback()).getOrElse(candidates.head)
  }

  private def joinThenLeave(
      ds: DatagramSocket,
      host: String,
      netIf: NetworkInterface
  ): Unit = {
    val g = group(host)
    ds.joinGroup(g, netIf)
    assertThrows(classOf[SocketException], ds.joinGroup(g, netIf))
    ds.leaveGroup(g, netIf)
    assertThrows(classOf[SocketException], ds.leaveGroup(g, netIf))
  }

  @Test def joinLeaveIPv4GroupOnInterface(): Unit = {
    val netIf = multicastInterface(_.isInstanceOf[Inet4Address])
    val ds = new DatagramSocket(0)
    try joinThenLeave(ds, "239.255.53.54", netIf)
    finally ds.close()
  }

  @Test def joinLeaveIPv4GroupOnDefaultInterface(): Unit = {
    val ds = new DatagramSocket(0)
    try joinThenLeave(ds, "239.255.53.55", null)
    finally ds.close()
  }

  @Test def joinLeaveIPv6GroupOnInterface(): Unit = {
    assumeFalse("IPv4 only sockets on Windows", Platform.isWindows)
    val netIf = multicastInterface(_.isInstanceOf[Inet6Address])
    val ds = new DatagramSocket(0)
    try joinThenLeave(ds, "ff02::5354", netIf)
    finally ds.close()
  }

  @Test def joinNonMulticastAddressThrows(): Unit = {
    val ds = new DatagramSocket(0)
    try
      assertThrows(
        classOf[SocketException],
        ds.joinGroup(group("127.0.0.1"), null)
      )
    finally ds.close()
  }

  @Test def joinOnClosedSocketThrows(): Unit = {
    val ds = new DatagramSocket(0)
    ds.close()
    assertThrows(
      classOf[SocketException],
      ds.joinGroup(group("239.255.53.56"), null)
    )
  }
}
