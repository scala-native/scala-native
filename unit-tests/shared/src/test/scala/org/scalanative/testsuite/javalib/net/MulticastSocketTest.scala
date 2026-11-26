package org.scalanative.testsuite.javalib.net

import java.net._

import scala.collection.JavaConverters._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class MulticastSocketTest {

  private def withSocket[T](ms: MulticastSocket)(
      body: MulticastSocket => T
  ): T =
    try body(ms)
    finally ms.close()

  // #5419: FreeBSD and NetBSD JDKs open dual-stack sockets that cannot
  // join IPv4 groups, Scala Native is not affected
  private def assumeCanJoinIPv4Group(): Unit =
    assumeFalse(
      "FreeBSD and NetBSD JDKs cannot join IPv4 groups on their dual-stack sockets",
      Platform.executingInJVM && (Platform.isFreeBSD || Platform.isNetBSD)
    )

  // TODO: native IPv4 joins fail with EINVAL on FreeBSD
  private def assumeNativeIPv4Join(): Unit =
    assumeFalse(
      "IPv4 joins fail on FreeBSD",
      Platform.executingInScalaNative && Platform.isFreeBSD
    )

  @Test def constructors(): Unit = {
    withSocket(new MulticastSocket()) { ms =>
      assertTrue(ms.isBound())
      assertTrue(ms.getLocalPort() > 0)
      assertTrue(ms.getReuseAddress())
    }
    withSocket(new MulticastSocket(null: SocketAddress)) { ms =>
      assertFalse(ms.isBound())
      assertTrue(ms.getReuseAddress())
      ms.bind(new InetSocketAddress(0))
      assertTrue(ms.isBound())
    }
  }

  @Test def timeToLive(): Unit = withSocket(new MulticastSocket()) { ms =>
    assertEquals(1, ms.getTimeToLive())
    ms.setTimeToLive(42)
    assertEquals(42, ms.getTimeToLive())
    assertThrows(classOf[IllegalArgumentException], ms.setTimeToLive(256))
    assertThrows(classOf[IllegalArgumentException], ms.setTimeToLive(-1))
  }

  @Test def loopbackMode(): Unit = withSocket(new MulticastSocket()) { ms =>
    assertFalse(ms.getLoopbackMode())
    ms.setLoopbackMode(true)
    assertTrue(ms.getLoopbackMode())
    ms.setLoopbackMode(false)
    assertFalse(ms.getLoopbackMode())
  }

  @Test def networkInterface(): Unit = {
    assumeFalse(
      "NetworkInterface not implemented in Windows",
      Platform.isWindows
    )
    val loopbackIf =
      NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress())
    assumeTrue("no loopback interface", loopbackIf != null)
    MulticastInterface.assumeReadable()
    withSocket(new MulticastSocket()) { ms =>
      assertNotNull(ms.getNetworkInterface())
      assertTrue(ms.getInterface().isAnyLocalAddress())

      ms.setNetworkInterface(loopbackIf)
      assertEquals(loopbackIf, ms.getNetworkInterface())
      assertTrue(
        loopbackIf.getInetAddresses().asScala.contains(ms.getInterface())
      )

      ms.setInterface(InetAddress.getLoopbackAddress())
      assertEquals(InetAddress.getLoopbackAddress(), ms.getInterface())
      assertEquals(loopbackIf, ms.getNetworkInterface())
    }
  }

  @Test def invalidInterface(): Unit = withSocket(new MulticastSocket()) { ms =>
    assertThrows(
      classOf[IllegalArgumentException],
      ms.setNetworkInterface(null)
    )
    assertThrows(classOf[SocketException], ms.setInterface(null))
  }

  @Test def joinGroupInvalid(): Unit = withSocket(new MulticastSocket()) { ms =>
    assertThrows(
      classOf[NullPointerException],
      ms.joinGroup(null: InetAddress)
    )
    assertThrows(
      classOf[SocketException],
      ms.joinGroup(InetAddress.getLoopbackAddress())
    )
  }

  @Test def joinLeaveDefaultInterface(): Unit = {
    assumeCanJoinIPv4Group()
    assumeNativeIPv4Join()
    MulticastInterface.assumeReadable()
    withSocket(new MulticastSocket(0)) { ms =>
      val group = InetAddress.getByName("239.255.53.64")
      ms.joinGroup(group)
      ms.leaveGroup(group)
    }
  }

  @Test def joinUsesConfiguredInterface(): Unit = {
    assumeFalse(
      "NetworkInterface not implemented in Windows",
      Platform.isWindows
    )
    assumeCanJoinIPv4Group()
    assumeNativeIPv4Join()
    MulticastInterface.assumeReadable()
    val candidates = NetworkInterface
      .getNetworkInterfaces()
      .asScala
      .filter { ni =>
        ni.isUp() && ni.supportsMulticast() &&
        ni.getInetAddresses().asScala.exists(_.isInstanceOf[Inet4Address])
      }
      .toList
    assumeTrue("no multicast capable interface", candidates.nonEmpty)
    val netIf = candidates.find(_.isLoopback()).getOrElse(candidates.head)
    val group = InetAddress.getByName("239.255.53.65")
    withSocket(new MulticastSocket(0)) { ms =>
      ms.setNetworkInterface(netIf)
      ms.joinGroup(group)
      assertThrows(
        classOf[SocketException],
        ms.joinGroup(new InetSocketAddress(group, 0), netIf)
      )
      ms.leaveGroup(new InetSocketAddress(group, 0), netIf)
      assertThrows(classOf[SocketException], ms.leaveGroup(group))
    }
  }

  @Test def closedSocket(): Unit = {
    val ms = new MulticastSocket()
    ms.close()
    assertThrows(classOf[SocketException], ms.getTimeToLive())
    assertThrows(classOf[SocketException], ms.setTimeToLive(1))
    assertThrows(classOf[SocketException], ms.getLoopbackMode())
    assertThrows(classOf[SocketException], ms.setLoopbackMode(true))
    assertThrows(classOf[SocketException], ms.getInterface())
    assertThrows(classOf[SocketException], ms.getNetworkInterface())
  }
}

object MulticastInterface {
  // TODO: qemu-user cannot get IP_MULTICAST_IF (ENOPROTOOPT), see #5425
  def assumeReadable(): Unit = {
    val ms = new MulticastSocket()
    try {
      val readable =
        try { ms.getInterface(); true }
        catch { case _: SocketException => false }
      assumeTrue("IP_MULTICAST_IF cannot be read", readable)
    } finally ms.close()
  }
}
