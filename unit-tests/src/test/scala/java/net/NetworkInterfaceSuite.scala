package java.net

import collection.JavaConverters._

// Ported from Apache Harmony

object NetworkInterfaceSuite extends tests.Suite {
  var theInterfaces: Iterator[NetworkInterface] = null
  var atLeastOneInterface                       = false
  var atLeastTwoInterfaces                      = false
  var networkInterface1: NetworkInterface       = null
  var sameAsNetworkInterface1: NetworkInterface = null
  var networkInterface2: NetworkInterface       = null

  protected def setUp(): Unit = {
    try {
      theInterfaces = NetworkInterface.getNetworkInterfaces().asScala
    } catch {
      case e: Exception => assert(false)
    }

    theInterfaces.find(netif => netif.getInetAddresses().asScala.hasNext) match {
      case Some(netif) => {
        atLeastOneInterface = true
        networkInterface1 = netif
      }
      case None => // Nothing
    }
    theInterfaces.find(
      netif =>
        netif
          .getInetAddresses()
          .asScala
          .hasNext && netif != networkInterface1) match {
      case Some(netif) => {
        atLeastTwoInterfaces = true
        networkInterface2 = netif
      }
      case None => // Nothing
    }

    if (atLeastOneInterface) {
      val addresses = networkInterface1.getInetAddresses().asScala
      if (addresses.hasNext) {
        try {
          sameAsNetworkInterface1 =
            NetworkInterface.getByInetAddress(addresses.next)
        } catch {
          case e: SocketException => assert(false)
        }
      }
    }

    theInterfaces = NetworkInterface.getNetworkInterfaces().asScala
  }

  protected def tearDown(): Unit = {}

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

  test("getName") {
    if (atLeastOneInterface) {
      assert(networkInterface1.getName() != null)
      assert(networkInterface1.getName() != "")
    }

    if (atLeastOneInterface) {
      assert(networkInterface1.getName() != networkInterface2.getName())
    }
  }

  test("getInetAddresses") {
    if (atLeastOneInterface) {
      networkInterface1
        .getInetAddresses()
        .asScala
        .foreach(addr => assert(addr != null))
    }

    if (atLeastTwoInterfaces) {
      networkInterface2
        .getInetAddresses()
        .asScala
        .foreach(addr => assert(addr != null))
    }
  }

  test("getDisplayName") {
    if (atLeastOneInterface) {
      assert(networkInterface1.getDisplayName() != null)
      assert(networkInterface1.getDisplayName != "")
    }

    if (atLeastTwoInterfaces) {
      assert(
        networkInterface1.getDisplayName() != networkInterface2
          .getDisplayName())
    }
  }

  test("getByName") {
    assertThrows[NullPointerException](NetworkInterface.getByName(null))
    assert(NetworkInterface.getByName("8not a name4") == null)

    if (atLeastOneInterface) {
      assert(
        networkInterface1 == NetworkInterface.getByName(
          networkInterface1.getName()))
    }

    if (atLeastTwoInterfaces) {
      assert(
        networkInterface2 == NetworkInterface.getByName(
          networkInterface2.getName()))
    }
  }

  test("getByInetAddress") {
    val addressBytes = Array[Byte](0, 0, 0, 0)

    assertThrows[NullPointerException](NetworkInterface.getByInetAddress(null))
    assert(
      NetworkInterface
        .getByInetAddress(InetAddress.getByAddress(addressBytes)) == null)

    if (atLeastOneInterface) {
      networkInterface1
        .getInetAddresses()
        .asScala
        .foreach(addr =>
          assert(networkInterface1 == NetworkInterface.getByInetAddress(addr)))
    }
    if (atLeastTwoInterfaces) {
      networkInterface2
        .getInetAddresses()
        .asScala
        .foreach(addr =>
          assert(networkInterface2 == NetworkInterface.getByInetAddress(addr)))
    }
  }

  test("getNetworkInterfaces") {
    NetworkInterface.getNetworkInterfaces()
  }

  test("equals") {
    if (atLeastOneInterface) {
      assert(sameAsNetworkInterface1 == networkInterface1)
      assert(networkInterface1 != null)
    }
    if (atLeastTwoInterfaces) {
      assert(networkInterface1 != networkInterface2)
    }
  }

  test("hashCode") {
    if (atLeastOneInterface) {
      assert(networkInterface1.hashCode() == networkInterface1.hashCode())
      assert(networkInterface1.hashCode() == sameAsNetworkInterface1.hashCode())
    }
  }

  test("toString") {
    if (atLeastOneInterface) {
      assert(networkInterface1.toString() != null)
      assert(networkInterface1.toString() != "")
    }

    if (atLeastTwoInterfaces) {
      assert(networkInterface1.toString() != networkInterface2.toString())
    }
  }

  test("getInterfaceAddresses") {
    NetworkInterface
      .getNetworkInterfaces()
      .asScala
      .foreach(
        netif =>
          netif
            .getInterfaceAddresses()
            .asScala
            .foreach(addr => assert(addr != null)))
    NetworkInterface.getNetworkInterfaces().asScala.foreach(netif => assert(netif.getInterfaceAddresses().asScala == netif.getInterfaceAddresses().asScala))
  }

  test("isLoopback") {
    if (theInterfaces != null) {
      theInterfaces.foreach(netif => {
        val loopback =
          netif.getInetAddresses().asScala.exists(_.isLoopbackAddress())
        assert(netif.isLoopback() == loopback)
      })
    }
  }

  test("getHardwareAddress") {
    if (theInterfaces != null) {
      theInterfaces.foreach(netif => {
        val hwAddr = netif.getHardwareAddress()
        if (netif.isLoopback())
          assert(hwAddr == null || hwAddr.size == 0)
        else
          assert(hwAddr.size >= 0)
      })
    }
  }

  test("getMTU") {
    if (theInterfaces != null) {
      theInterfaces.foreach(netif => assert(netif.getMTU() >= 0))
    }
  }
}
