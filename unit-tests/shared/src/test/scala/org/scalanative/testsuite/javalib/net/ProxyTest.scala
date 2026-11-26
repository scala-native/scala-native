package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ProxyTest {
  private def addr(host: String, port: Int) =
    InetSocketAddress.createUnresolved(host, port)

  @Test def noProxy(): Unit = {
    assertEquals(Proxy.Type.DIRECT, Proxy.NO_PROXY.`type`())
    assertNull(Proxy.NO_PROXY.address())
    assertEquals("DIRECT", Proxy.NO_PROXY.toString())
    assertEquals(Proxy.Type.DIRECT.hashCode(), Proxy.NO_PROXY.hashCode())
  }

  @Test def constructorRejectsDirectAndNullAddress(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException],
      new Proxy(Proxy.Type.DIRECT, addr("h", 1))
    )
    assertThrows(
      classOf[IllegalArgumentException],
      new Proxy(Proxy.Type.HTTP, null)
    )
    // A null type is accepted
    assertNull(new Proxy(null, addr("h", 1)).`type`())
  }

  @Test def constructorRejectsOtherAddressTypes(): Unit = {
    val other = new SocketAddress {}
    assertThrows(
      classOf[IllegalArgumentException],
      new Proxy(Proxy.Type.HTTP, other)
    )
  }

  @Test def accessorsAndToString(): Unit = {
    val a = addr("proxy", 8080)
    val p = new Proxy(Proxy.Type.HTTP, a)
    assertEquals(Proxy.Type.HTTP, p.`type`())
    assertSame(a, p.address())
    assertEquals("HTTP @ " + a, p.toString())
    assertEquals("SOCKS @ " + a, new Proxy(Proxy.Type.SOCKS, a).toString())
  }

  @Test def toStringOfUnresolvedAddress(): Unit = {
    assertEquals(
      "HTTP @ proxy/<unresolved>:8080",
      new Proxy(Proxy.Type.HTTP, addr("proxy", 8080)).toString()
    )
  }

  @Test def equalsAndHashCode(): Unit = {
    val p = new Proxy(Proxy.Type.HTTP, addr("p", 1))
    assertEquals(p, new Proxy(Proxy.Type.HTTP, addr("p", 1)))
    assertNotEquals(p, new Proxy(Proxy.Type.SOCKS, addr("p", 1)))
    assertNotEquals(p, new Proxy(Proxy.Type.HTTP, addr("p", 2)))
    assertNotEquals(Proxy.NO_PROXY, p)
    assertNotEquals(p, Proxy.NO_PROXY)
    assertNotEquals(p, "HTTP")
    assertEquals(
      Proxy.Type.HTTP.hashCode() + addr("p", 1).hashCode(),
      p.hashCode()
    )
  }

  @Test def typeEnum(): Unit = {
    assertArrayEquals(
      Array[AnyRef](Proxy.Type.DIRECT, Proxy.Type.HTTP, Proxy.Type.SOCKS),
      Proxy.Type.values().asInstanceOf[Array[AnyRef]]
    )
    assertEquals(Proxy.Type.SOCKS, Proxy.Type.valueOf("SOCKS"))
    assertEquals(2, Proxy.Type.SOCKS.ordinal())
  }
}
