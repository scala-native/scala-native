package org.scalanative.testsuite.javalib.net

import java.net.{
  DatagramSocket, Inet4Address, Inet6Address, InetAddress, InetSocketAddress,
  NetworkInterface, SocketException
}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.CollectionConverters._

class DatagramSocketTestOnJDK17 {

  private def group(host: String) =
    new InetSocketAddress(InetAddress.getByName(host), 0)

  private def multicastInterface(
      hasAddress: InetAddress => Boolean
  ): NetworkInterface = {
    assumeFalse(
      "NetworkInterface not implemented in Windows",
      Platform.isWindows
    )
    val candidates = NetworkInterface
      .getNetworkInterfaces()
      .toScalaSeq
      .filter { ni =>
        ni.isUp() && ni.supportsMulticast() &&
        ni.getInetAddresses().toScalaSeq.exists(hasAddress)
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
    assumeFalse(
      "FreeBSD and NetBSD JDKs cannot join IPv4 groups on their dual-stack sockets",
      Platform.executingInJVM && (Platform.isFreeBSD || Platform.isNetBSD) &&
        g.getAddress().isInstanceOf[Inet4Address]
    )
    ds.joinGroup(g, netIf)
    assertThrows(classOf[SocketException], ds.joinGroup(g, netIf))
    ds.leaveGroup(g, netIf)
    assertThrows(classOf[SocketException], ds.leaveGroup(g, netIf))
    ds.joinGroup(g, netIf)
    ds.leaveGroup(g, netIf)
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
    val netIf = multicastInterface(_.isInstanceOf[Inet6Address])
    val ds = new DatagramSocket(0)
    try joinThenLeave(ds, "ff02::5354", netIf)
    finally ds.close()
  }

  @Test def joinDistinctIPv6GroupsWithEqualHashes(): Unit = {
    val netIf = multicastInterface(_.isInstanceOf[Inet6Address])
    val ds = new DatagramSocket(0)
    try {
      val g1 = group("ff02::1f")
      val g2 = group("ff02::100")
      ds.joinGroup(g1, netIf)
      ds.joinGroup(g2, netIf)
      ds.leaveGroup(g1, netIf)
      ds.leaveGroup(g2, netIf)
    } finally ds.close()
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
