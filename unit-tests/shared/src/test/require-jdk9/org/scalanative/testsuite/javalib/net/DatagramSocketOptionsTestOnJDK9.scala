package org.scalanative.testsuite.javalib.net

import java.io.IOException
import java.net.StandardSocketOptions._
import java.net._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.CollectionConverters._

class DatagramSocketOptionsTestOnJDK9 {

  private def withSocket[T](ds: DatagramSocket)(body: DatagramSocket => T): T =
    try body(ds)
    finally ds.close()

  private def multicastInterface(): NetworkInterface = {
    assumeFalse(
      "NetworkInterface not implemented in Windows",
      Platform.isWindows
    )
    val candidates = NetworkInterface
      .getNetworkInterfaces()
      .toScalaSeq
      .filter { ni =>
        ni.isUp() && ni.supportsMulticast() &&
        ni.getInetAddresses().toScalaSeq.exists(_.isInstanceOf[Inet4Address])
      }
      .toList
    assumeTrue("no multicast capable interface", candidates.nonEmpty)
    candidates.find(_.isLoopback()).getOrElse(candidates.head)
  }

  @Test def supportedOptions(): Unit = {
    val expected = Set[SocketOption[_]](
      SO_SNDBUF,
      SO_RCVBUF,
      SO_REUSEADDR,
      SO_BROADCAST,
      IP_TOS,
      IP_MULTICAST_IF,
      IP_MULTICAST_TTL,
      IP_MULTICAST_LOOP
    )
    withSocket(new DatagramSocket()) { ds =>
      val supported = ds.supportedOptions()
      assertTrue(supported.containsAll(expected.toJavaList))
      assertFalse(supported.contains(TCP_NODELAY))
      assertThrows(
        classOf[UnsupportedOperationException],
        supported.add(TCP_NODELAY)
      )
    }
    withSocket(new MulticastSocket()) { ms =>
      assertTrue(ms.supportedOptions().containsAll(expected.toJavaList))
    }
  }

  @Test def multicastTtl(): Unit = withSocket(new DatagramSocket()) { ds =>
    assertEquals(1, ds.getOption(IP_MULTICAST_TTL).intValue())
    assertSame(ds, ds.setOption(IP_MULTICAST_TTL, Integer.valueOf(7)))
    assertEquals(7, ds.getOption(IP_MULTICAST_TTL).intValue())
    ds.setOption(IP_MULTICAST_TTL, Integer.valueOf(0))
    assertEquals(0, ds.getOption(IP_MULTICAST_TTL).intValue())
    ds.setOption(IP_MULTICAST_TTL, Integer.valueOf(255))
    assertEquals(255, ds.getOption(IP_MULTICAST_TTL).intValue())
    assertThrows(
      classOf[IllegalArgumentException],
      ds.setOption(IP_MULTICAST_TTL, Integer.valueOf(256))
    )
    assertThrows(
      classOf[IllegalArgumentException],
      ds.setOption(IP_MULTICAST_TTL, Integer.valueOf(-1))
    )
  }

  @Test def multicastLoop(): Unit = withSocket(new DatagramSocket()) { ds =>
    assertTrue(ds.getOption(IP_MULTICAST_LOOP).booleanValue())
    ds.setOption(IP_MULTICAST_LOOP, java.lang.Boolean.FALSE)
    assertFalse(ds.getOption(IP_MULTICAST_LOOP).booleanValue())
    ds.setOption(IP_MULTICAST_LOOP, java.lang.Boolean.TRUE)
    assertTrue(ds.getOption(IP_MULTICAST_LOOP).booleanValue())
  }

  @Test def multicastInterfaceOption(): Unit = {
    val netIf = multicastInterface()
    MulticastInterface.assumeReadable()
    withSocket(new DatagramSocket()) { ds =>
      assertNull(ds.getOption(IP_MULTICAST_IF))
      ds.setOption(IP_MULTICAST_IF, netIf)
      assertEquals(netIf, ds.getOption(IP_MULTICAST_IF))
    }
  }

  @Test def socketOptions(): Unit = withSocket(new DatagramSocket()) { ds =>
    ds.setOption(SO_REUSEADDR, java.lang.Boolean.TRUE)
    assertTrue(ds.getOption(SO_REUSEADDR).booleanValue())
    assertTrue(ds.getReuseAddress())

    ds.setOption(SO_BROADCAST, java.lang.Boolean.FALSE)
    assertFalse(ds.getOption(SO_BROADCAST).booleanValue())
    ds.setOption(SO_BROADCAST, java.lang.Boolean.TRUE)
    assertTrue(ds.getOption(SO_BROADCAST).booleanValue())

    ds.setOption(SO_RCVBUF, Integer.valueOf(16384))
    assertTrue(ds.getOption(SO_RCVBUF).intValue() > 0)
    assertEquals(ds.getReceiveBufferSize(), ds.getOption(SO_RCVBUF).intValue())
    ds.setOption(SO_SNDBUF, Integer.valueOf(16384))
    assertTrue(ds.getOption(SO_SNDBUF).intValue() > 0)
    assertEquals(ds.getSendBufferSize(), ds.getOption(SO_SNDBUF).intValue())

    ds.setOption(IP_TOS, Integer.valueOf(0))
    assertEquals(ds.getTrafficClass(), ds.getOption(IP_TOS).intValue())
  }

  @Test def invalidValues(): Unit = withSocket(new DatagramSocket()) { ds =>
    def iae(body: => Any): Unit =
      assertThrows(classOf[IllegalArgumentException], body)
    iae(ds.setOption(IP_MULTICAST_TTL, null))
    iae(ds.setOption(IP_MULTICAST_LOOP, null))
    iae(ds.setOption(IP_MULTICAST_IF, null))
    iae(ds.setOption(SO_REUSEADDR, null))
    iae(ds.setOption(IP_TOS, Integer.valueOf(256)))
    iae(ds.setOption(IP_TOS, Integer.valueOf(-1)))
    iae(ds.setOption(SO_RCVBUF, Integer.valueOf(-1)))
    iae(ds.setOption(SO_SNDBUF, Integer.valueOf(-1)))
    iae(
      ds.setOption(
        SO_SNDBUF.asInstanceOf[SocketOption[AnyRef]],
        "1024": AnyRef
      )
    )
  }

  @Test def nullOption(): Unit = withSocket(new DatagramSocket()) { ds =>
    assertThrows(
      classOf[NullPointerException],
      ds.setOption(null: SocketOption[Integer], Integer.valueOf(1))
    )
    assertThrows(
      classOf[NullPointerException],
      ds.getOption(null: SocketOption[Integer])
    )
  }

  @Test def unsupportedOption(): Unit = {
    val ds = new DatagramSocket()
    def check(): Unit = {
      assertThrows(
        classOf[UnsupportedOperationException],
        ds.setOption(TCP_NODELAY, java.lang.Boolean.TRUE)
      )
      assertThrows(
        classOf[UnsupportedOperationException],
        ds.getOption(SO_KEEPALIVE)
      )
    }
    try check()
    finally ds.close()
    check()
  }

  @Test def closedSocket(): Unit = {
    val ds = new DatagramSocket()
    ds.close()
    assertThrows(
      classOf[IOException],
      ds.setOption(IP_MULTICAST_TTL, Integer.valueOf(3))
    )
    assertThrows(classOf[IOException], ds.getOption(IP_MULTICAST_TTL))
    assertThrows(classOf[IOException], ds.getOption(SO_RCVBUF))
    assertTrue(ds.supportedOptions().contains(IP_MULTICAST_TTL))
  }
}
