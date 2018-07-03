package java.net

import collection.JavaConverters._

// Ported from Apache Harmony

object InterfaceAddressSuite extends tests.Suite {
  private var interfaceAddr: InterfaceAddress        = null
  private var anotherInterfaceAddr: InterfaceAddress = null

  override def test(name: String)(body: => Unit): Unit =
    super.test(name) {
      setUp()
      try {
        body
      } finally {
        tearDown()
      }
    }

  override def testFails(name: String, issue: Int)(body: => Unit): Unit =
    super.testFails(name, issue) {
      setUp()
      try {
        body
      } finally {
        tearDown()
      }
    }

  protected def setUp(): Unit = {
    interfaceAddr = NetworkInterface
      .getNetworkInterfaces()
      .asScala
      .find(netif => netif.getInterfaceAddresses().size != 0) match {
      case Some(netIf) => {
        netIf.getInterfaceAddresses().get(0)
      }
      case None => null
    }

    if (interfaceAddr != null) {
      anotherInterfaceAddr = NetworkInterface
        .getNetworkInterfaces()
        .asScala
        .find(netif => netif.getInterfaceAddresses().size != 0) match {
        case Some(netIf) => netIf.getInterfaceAddresses().get(0)
        case None        => null
      }
    }
  }

  protected def tearDown(): Unit = {
    interfaceAddr = null
    anotherInterfaceAddr = null
  }

  test("hashCode") {
    if (interfaceAddr != null) {
      assert(interfaceAddr == anotherInterfaceAddr)
      assert(anotherInterfaceAddr.hashCode() == interfaceAddr.hashCode())
    }
  }

  test("equals") {
    if (interfaceAddr != null) {
      assert(!(interfaceAddr == null))
      assert(!(interfaceAddr == new Object()))

      assert(interfaceAddr == anotherInterfaceAddr)
      assert(!(interfaceAddr eq anotherInterfaceAddr))
    }
  }

  test("toString") {
    if (interfaceAddr != null) {
      assert(interfaceAddr.toString() != null)
      assert(anotherInterfaceAddr.toString() == interfaceAddr.toString())
      assert(interfaceAddr.toString().contains("/"))
      assert(interfaceAddr.toString().contains("["))
      assert(interfaceAddr.toString().contains("]"))
    }
  }

  test("getAddress") {
    if (interfaceAddr != null) {
      val addr1 = interfaceAddr.getAddress()
      assert(addr1 != null)
      val addr2 = anotherInterfaceAddr.getAddress()
      assert(addr2 != null)
      assert(addr1 == addr2)
    }
  }

  test("getBroadcast") {
    if (interfaceAddr != null) {
      val addr  = interfaceAddr.getAddress()
      val addr1 = interfaceAddr.getBroadcast()
      val addr2 = anotherInterfaceAddr.getBroadcast()

      addr match {
        case addr4: Inet4Address => assert(addr2 == addr1)
        case addr6: Inet6Address => assert(addr1 == null && addr2 == null)
      }
    }
  }

  test("getNetworkPrefixLength") {
    if (interfaceAddr != null) {
      val prefix1 = interfaceAddr.getNetworkPrefixLength()
      val prefix2 = interfaceAddr.getNetworkPrefixLength()
      assert(prefix1 == prefix2)
    }
  }
}
