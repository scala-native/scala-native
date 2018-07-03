package java.net

// Ported from Apache Harmony

object DatagramPacketSuite extends tests.Suite {
  test("constructor Array[Byte], Int") {
    var dp = new DatagramPacket("Hello".getBytes(), 5)
    assertEquals("Hello", new String(dp.getData(), 0, dp.getData().size))
    assertEquals(5, dp.getLength())

    dp = new DatagramPacket(Array.fill[Byte](942)(0), 4)
    assertEquals(-1, dp.getPort())
    assertThrows[IllegalArgumentException] {
      dp.getSocketAddress()
    }
  }

  test("constructor Array[Byte], Int, Int") {
    val dp = new DatagramPacket("Hello".getBytes(), 2, 3)
    assertEquals("Hello", new String(dp.getData(), 0, dp.getData().size))
    assertEquals(3, dp.getLength())
    assertEquals(2, dp.getOffset())
  }

  test("constructor Array[Byte], Int, Int, InetAddress, Int") {
    val dp = new DatagramPacket("Hello".getBytes(),
                                2,
                                3,
                                InetAddress.getLocalHost(),
                                0)
    assertEquals(InetAddress.getLocalHost(), dp.getAddress())
    assertEquals(0, dp.getPort())
    assertEquals(3, dp.getLength())
    assertEquals(2, dp.getOffset())
  }

  test("constructor Array[Byte], Int, InetAddress, Int") {
    val dp =
      new DatagramPacket("Hello".getBytes(), 5, InetAddress.getLocalHost(), 0)
    assertEquals(InetAddress.getLocalHost(), dp.getAddress())
    assertEquals(0, dp.getPort())
    assertEquals(5, dp.getLength())
  }

  test("getAddress") {
    val dp =
      new DatagramPacket("Hello".getBytes(), 5, InetAddress.getLocalHost(), 0)
    assertEquals(InetAddress.getLocalHost(), dp.getAddress())
  }

  test("getData") {
    val dp = new DatagramPacket("Hello".getBytes(), 5)
    assertEquals("Hello", new String(dp.getData(), 0, dp.getData().size))
  }

  test("getLength") {
    val dp = new DatagramPacket("Hello".getBytes(), 5)
    assertEquals(5, dp.getLength())
  }

  test("getOffset") {
    val dp = new DatagramPacket("Hello".getBytes(), 3, 2)
    assertEquals(3, dp.getOffset())
  }

  test("getPort") {
    val dp = new DatagramPacket("Hello".getBytes(),
                                5,
                                InetAddress.getLocalHost(),
                                1000)
    assertEquals(1000, dp.getPort())

    val localhost = InetAddress.getLocalHost()
    val socket    = new DatagramSocket(0, localhost)
    val port      = socket.getLocalPort()

    socket.setSoTimeout(3000)
    val packet =
      new DatagramPacket(Array[Byte](1, 2, 3, 4, 5, 6), 6, localhost, port)
    socket.send(packet)
    socket.receive(packet)
    socket.close()
    assertEquals(packet.getPort(), port)
  }

  test("setAddress") {
    val ia = InetAddress.getByName("127.0.0.1")
    val dp =
      new DatagramPacket("Hello".getBytes(), 5, InetAddress.getLocalHost(), 0)
    dp.setAddress(ia)
    assertEquals(ia, dp.getAddress())
  }

  test("setData Array[Byte], Int, Int") {
    val dp = new DatagramPacket("Hello".getBytes(), 5)
    dp.setData("Wagga wagga".getBytes(), 2, 3)
    assertEquals("Wagga wagga", new String(dp.getData()))
  }

  test("setData Array[Byte]") {
    val dp = new DatagramPacket("Hello".getBytes(), 5)
    dp.setData("Ralph".getBytes())
    assertEquals("Ralph", new String(dp.getData(), 0, dp.getData().size))
  }

  test("setLength") {
    val dp = new DatagramPacket("Hello".getBytes(), 5)
    dp.setLength(1)
    assertEquals(1, dp.getLength())
  }

  test("setPort") {
    val dp = new DatagramPacket("Hello".getBytes(),
                                5,
                                InetAddress.getLocalHost(),
                                1000)
    dp.setPort(2000)
    assertEquals(2000, dp.getPort())
  }

  test("constructor Array[Byte], Int, SocketAddress") {
    class UnsupportedSocketAddress extends SocketAddress {}
    val buf = Array.fill[Byte](1)(0)
    assertThrows[IllegalArgumentException] {
      new DatagramPacket(buf, 1, new UnsupportedSocketAddress)
    }

    assertThrows[IllegalArgumentException] {
      new DatagramPacket(buf, 1, null)
    }

    val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 2067)
    val thePacket  = new DatagramPacket(buf, 1, theAddress)
    assertEquals(theAddress, thePacket.getSocketAddress())
    assertEquals(
      theAddress,
      new InetSocketAddress(thePacket.getAddress(), thePacket.getPort()))
  }

  test("constructor Array[Byte], Int, SocketAddress") {
    class UnsupportedSocketAddress extends SocketAddress {}
    val buf = Array.fill[Byte](2)(0)
    assertThrows[IllegalArgumentException] {
      new DatagramPacket(buf, 1, 1, new UnsupportedSocketAddress)
    }

    assertThrows[IllegalArgumentException] {
      new DatagramPacket(buf, 1, 1, null)
    }

    val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 2067)
    val thePacket  = new DatagramPacket(buf, 1, 1, theAddress)
    assertEquals(theAddress, thePacket.getSocketAddress())
    assertEquals(
      theAddress,
      new InetSocketAddress(thePacket.getAddress(), thePacket.getPort()))
    assertEquals(1, thePacket.getOffset())
  }

  test("getSocketAddress") {
    val buf       = Array.fill[Byte](1)(0)
    val thePacket = new DatagramPacket(buf, 1)

    val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0)
    thePacket.setSocketAddress(theAddress)
    assertEquals(theAddress, thePacket.getSocketAddress())
  }

  test("setSocketAddress") {
    class UnsupportedSocketAddress extends SocketAddress {}
    val buf       = Array.fill[Byte](1)(0)
    var thePacket = new DatagramPacket(buf, 1)

    assertThrows[IllegalArgumentException] {
      thePacket.setSocketAddress(new UnsupportedSocketAddress)
    }

    thePacket = new DatagramPacket(buf, 1)
    assertThrows[IllegalArgumentException] {
      thePacket.setSocketAddress(null)
    }

    val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 2049)
    thePacket = new DatagramPacket(buf, 1)
    thePacket.setSocketAddress(theAddress)
    assertEquals(theAddress, thePacket.getSocketAddress())
    assertEquals(
      theAddress,
      new InetSocketAddress(thePacket.getAddress(), thePacket.getPort()))
  }
}
