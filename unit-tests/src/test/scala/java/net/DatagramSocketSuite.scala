package java.net

import java.io.IOException
import java.io.InterruptedIOException
import java.util.Date

// Ported from Apache Harmony

object DatagramSocketSuite extends tests.Suite {
  test("constructor") {
    new DatagramSocket()
  }

  test("constructor Int") {
    val ds = new DatagramSocket(0)
    ds.close()
  }

  test("constructor Int, InetAddress") {
    val ds = new DatagramSocket(0, InetAddress.getLocalHost())
    assert(ds.getLocalPort() != 0)
    assertEquals(InetAddress.getLocalHost(), ds.getLocalAddress())
  }

  test("close") {
    val ds = new DatagramSocket(0)
    val dp = new DatagramPacket("Test string".getBytes(),
                                11,
                                InetAddress.getLocalHost(),
                                0)
    ds.close()
    assertThrows[SocketException] {
      ds.send(dp)
    }
  }

  test("connect InetAddress, Int") {
    var ds          = new DatagramSocket()
    var inetAddress = InetAddress.getLocalHost()
    ds.connect(inetAddress, 0)
    assertEquals(inetAddress, ds.getInetAddress())
    assertEquals(0, ds.getPort())
    ds.disconnect()

    assertThrows[PortUnreachableException] {
      val localHost = InetAddress.getLocalHost()
      ds = new DatagramSocket()
      val port = ds.getLocalPort()
      ds.connect(localHost, port)
      val send =
        new DatagramPacket(Array.fill[Byte](10)(0), 10, localHost, port)
      ds.send(send)
      val receive = new DatagramPacket(Array.fill[Byte](20)(0), 20)
      ds.setSoTimeout(10000)
      ds.receive(receive)
      ds.close
    }

    var portNumber = 0
    ds = new DatagramSocket()
    inetAddress = InetAddress.getLocalHost()
    portNumber = ds.getLocalPort()
    ds.connect(inetAddress, portNumber)
    ds.disconnect()
    ds.close()

    assertThrows[IllegalArgumentException] {
      ds = new DatagramSocket()
      inetAddress = InetAddress.getLocalHost()
      portNumber = ds.getLocalPort()
      ds.connect(inetAddress, portNumber)
      val send = new DatagramPacket(Array.fill[Byte](10)(0),
                                    10,
                                    inetAddress,
                                    portNumber + 1)
      ds.send(send)
      ds.close()
    }

    ds = new DatagramSocket()
    val bytes = Array[Byte](0, 0, 0, 0)
    inetAddress = InetAddress.getByAddress(bytes)
    portNumber = ds.getLocalPort()
    ds.connect(inetAddress, portNumber)
  }

  test("disconnect") {
    val ds          = new DatagramSocket()
    val inetAddress = InetAddress.getLocalHost()
    ds.connect(inetAddress, 0)
    ds.disconnect()
    assert(ds.getInetAddress() == null)
    assertEquals(-1, ds.getPort())
  }

  test("getLocalAddress") {
    val local: InetAddress = InetAddress.getLocalHost()
    val portNumber         = 0
    val ds                 = new DatagramSocket(portNumber, local)
    assertEquals(
      InetAddress.getByName(InetAddress.getLocalHost().getHostName()),
      ds.getLocalAddress())
  }

  test("getLocalPort") {
    val ds = new DatagramSocket()
    assert(ds.getLocalPort() != 0)
  }

  test("getPort") {
    val theSocket = new DatagramSocket()
    assertEquals(-1, theSocket.getPort())

    val portNumber = 49152
    theSocket.connect(InetAddress.getLocalHost(), portNumber)
    assertEquals(portNumber, theSocket.getPort())
  }

  test("getReceiveBufferSize") {
    val ds = new DatagramSocket()
    ds.setReceiveBufferSize(130)
    assert(ds.getReceiveBufferSize() >= 130)
  }

  test("getSendBufferSize") {
    val ds = new DatagramSocket()
    ds.setSendBufferSize(134)
    assert(ds.getSendBufferSize() >= 134)
  }

  test("getSoTimeout") {
    val ds = new DatagramSocket()
    ds.setSoTimeout(100)
    assertEquals(100, ds.getSoTimeout)
  }

  test("receive DatagramPacket") {
    val localHost  = InetAddress.getLocalHost()
    val portNumber = 49154
    val sds        = new DatagramSocket(49153)
    var ds         = new DatagramSocket(portNumber)
    val sdp        = new DatagramPacket("Test String".getBytes(), 11, localHost, 49154)
    sds.send(sdp)
    sds.close()
    ds.setSoTimeout(6000)
    val rbuf = Array.fill[Byte](1000)(0)
    val rdp  = new DatagramPacket(rbuf, rbuf.length)
    ds.receive(rdp)
    ds.close()
    assertEquals(new String(rbuf, 0, 11), "Test String")

    ds = new DatagramSocket()
    ds.setSoTimeout(500)
    val start       = new Date()
    var interrupted = false
    try {
      ds.receive(new DatagramPacket(Array.fill[Byte](1)(0), 1))
    } catch {
      case e: InterruptedIOException => interrupted = true
    }
    assert(interrupted)
    val delay = (new Date().getTime() - start.getTime()).toInt
    assert(delay >= 490)
  }

  test("receive oversize DatagramPacket") {
    val localHost  = InetAddress.getLocalHost()
    val portNumber = 49154
    val sds        = new DatagramSocket(49153)
    val ds         = new DatagramSocket(portNumber)
    val sdp        = new DatagramPacket("0123456789".getBytes(), 10, localHost, 49154)
    sds.send(sdp)
    sds.close()
    ds.setSoTimeout(6000)
    val rbuf = Array.fill[Byte](5)(0)
    val rdp  = new DatagramPacket(rbuf, rbuf.length)
    ds.receive(rdp)
    ds.close()
    assertEquals(new String(rbuf, 0, 5), "01234")
  }

  test("send DatagramPacket") {
    val i = InetAddress.getByName("127.0.0.1")
    val d = new DatagramSocket(0, i)
    assertThrows[NullPointerException] {
      d.send(new DatagramPacket(Array[Byte](1), 1))
    }
    d.close()
  }

  test("send DatagramPacket 2") {
    val udpPort    = 20000
    val sendPort   = 23000
    val udpSocket  = new DatagramSocket(udpPort)
    val data       = Array[Byte](65)
    val sendPacket = new DatagramPacket(data, data.length, null, sendPort)
    assertThrows[NullPointerException] {
      udpSocket.send(sendPacket)
    }
    udpSocket.close()
  }

  test("setSendBufferSize Int") {
    val portNumber = 49153
    val ds         = new DatagramSocket(portNumber)
    ds.setSendBufferSize(134)
    assert(ds.getSendBufferSize() >= 134)
    ds.close()
  }

  test("setReceiveBufferSize Int") {
    val portNumber = 49153
    val ds         = new DatagramSocket(portNumber)
    ds.setReceiveBufferSize(130)
    assert(ds.getReceiveBufferSize() >= 130)
    ds.close()
  }

  test("setSoTimeout Int") {
    val ds = new DatagramSocket()
    ds.setSoTimeout(100)
    assert(ds.getSoTimeout() >= 100)
  }

  test("constructor DatagramSocketImpl") {
    assertThrows[NullPointerException] {
      val impl: DatagramSocketImpl = null
      new DatagramSocket(impl)
    }
  }

  test("constructor SocketAddress") {
    class UnsupportedSocketAddress extends SocketAddress

    var ds =
      new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0))
    assert(ds.getBroadcast())
    assert(ds.getLocalPort() != 0)
    assertEquals(InetAddress.getLocalHost(), ds.getLocalAddress())
    assertThrows[IllegalArgumentException] {
      ds = new DatagramSocket(new UnsupportedSocketAddress())
    }
    val sockAddr: SocketAddress = null
    ds = new DatagramSocket(sockAddr)
    assert(ds.getBroadcast())
  }

  test("bind SocketAddress") {
    class mySocketAddress extends SocketAddress

    val portNumber = 49153
    var theSocket = new DatagramSocket(
      new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    assertEquals(theSocket.getLocalSocketAddress(),
                 new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    theSocket.close()

    val sockAddr: SocketAddress = null
    theSocket = new DatagramSocket(sockAddr)
    theSocket.bind(sockAddr)
    assert(theSocket.getLocalSocketAddress() != null)
    theSocket.close()

    theSocket = new DatagramSocket(sockAddr)
    assertThrows[BindException] {
      theSocket.bind(
        new InetSocketAddress(InetAddress.getByAddress(Array[Byte](1, 0, 0, 0)),
                              49153))
    }
    theSocket.close()

    theSocket = new DatagramSocket(sockAddr)
    val theSocket2 = new DatagramSocket(49153)
    assertThrows[BindException] {
      val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 49154)
      theSocket.bind(theAddress)
      theSocket2.bind(theAddress)
    }
    theSocket.close()
    theSocket2.close()

    theSocket = new DatagramSocket(sockAddr)
    assertThrows[IllegalArgumentException] {
      theSocket.bind(new mySocketAddress())
    }
    theSocket.close()
  }

  test("connect SocketAddress") {
    var ds: DatagramSocket = null
    assertThrows[PortUnreachableException] {
      val localHost = InetAddress.getLocalHost()
      ds = new DatagramSocket()
      val port = 49153
      ds.connect(new InetSocketAddress(localHost, port))
      val send =
        new DatagramPacket(Array.fill[Byte](10)(0), 10, localHost, port)
      ds.send(send)
      val receive = new DatagramPacket(Array.fill[Byte](20)(0), 20)
      ds.setSoTimeout(10000)
      ds.receive(receive)
      ds.close
    }

    ds = new DatagramSocket()
    val inetAddress = InetAddress.getLocalHost()
    val portNumber  = 49153
    ds.connect(new InetSocketAddress(inetAddress, portNumber))
    ds.disconnect()
    ds.close()

    assertThrows[IllegalArgumentException] {
      ds = new DatagramSocket()
      val inetAddress = InetAddress.getLocalHost()
      val portNumber  = 49153
      ds.connect(new InetSocketAddress(inetAddress, portNumber))
      val senddp = new DatagramPacket(Array.fill[Byte](10)(0),
                                      10,
                                      inetAddress,
                                      portNumber + 1)
      ds.send(senddp)
      ds.close()
    }

    assertThrows[IllegalArgumentException] {
      ds = new DatagramSocket()
      val addressBytes = Array[Byte](0, 0, 0, 0)
      val inetAddress  = InetAddress.getByAddress(addressBytes)
      val portNumber   = 49153
      val localHostIA  = InetAddress.getLocalHost()
      ds.connect(new InetSocketAddress(inetAddress, portNumber))
      assert(ds.isConnected())
      val sendBytesArray = Array[Byte]('T', 'e', 's', 't', 0)
      val senddp = new DatagramPacket(sendBytesArray,
                                      sendBytesArray.length,
                                      localHostIA,
                                      portNumber)
      ds.send(senddp)
      ds.close()
    }
  }

  test("isBound") {
    val addr      = InetAddress.getLocalHost()
    var theSocket = new DatagramSocket(49153)
    assert(theSocket.isBound())
    theSocket.close()

    theSocket = new DatagramSocket(new InetSocketAddress(addr, 49154))
    assert(theSocket.isBound())
    theSocket.close()

    val sockAddr: SocketAddress = null
    theSocket = new DatagramSocket(sockAddr)
    assert(!theSocket.isBound())
    theSocket.close()

    theSocket = new DatagramSocket(sockAddr)
    theSocket.connect(new InetSocketAddress(addr, 49154))
    assert(theSocket.isBound())
    theSocket.close()

    val theLocalAddress =
      new InetSocketAddress(InetAddress.getLocalHost(), 49155)
    theSocket = new DatagramSocket(sockAddr)
    assert(!theSocket.isBound())
    theSocket.bind(theLocalAddress)
    assert(theSocket.isBound())
    theSocket.close()
    assert(theSocket.isBound())
  }

  test("isConnected") {
    val addr      = InetAddress.getLocalHost()
    var theSocket = new DatagramSocket(49154)
    assert(!theSocket.isConnected())
    theSocket.connect(new InetSocketAddress(addr, 49153))
    assert(theSocket.isConnected())

    theSocket.connect(new InetSocketAddress(addr, 49155))
    assert(theSocket.isConnected())

    theSocket.disconnect()
    assert(!theSocket.isConnected())
    theSocket.close()

    theSocket = new DatagramSocket(49156)
    theSocket.connect(new InetSocketAddress(addr, 49153))
    theSocket.close()
    assert(theSocket.isConnected())
  }

  test("getRemoteSocketAddress") {
    val sport      = 49153
    var portNumber = 49154
    val s = new DatagramSocket(
      new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    s.connect(new InetSocketAddress(InetAddress.getLocalHost(), sport))
    assertEquals(s.getRemoteSocketAddress,
                 new InetSocketAddress(InetAddress.getLocalHost(), sport))
    s.close()

    val sockAddr: SocketAddress = null
    val theSocket               = new DatagramSocket(sockAddr)
    portNumber = 49155
    theSocket.bind(
      new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    assert(theSocket.getRemoteSocketAddress() == null)

    theSocket.connect(new InetSocketAddress(InetAddress.getLocalHost(), sport))
    assertEquals(theSocket.getRemoteSocketAddress(),
                 new InetSocketAddress(InetAddress.getLocalHost(), sport))
    theSocket.close()
  }

  test("getLocalSocketAddress") {
    var portNumber = 49153
    var s = new DatagramSocket(
      new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    assertEquals(s.getLocalSocketAddress(),
                 new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    s.close()

    val sockAddr: SocketAddress = null
    val theSocket               = new DatagramSocket(sockAddr)
    assert(theSocket.getLocalSocketAddress() == null)

    portNumber = 49154
    theSocket.bind(
      new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    assertEquals(theSocket.getLocalSocketAddress(),
                 new InetSocketAddress(InetAddress.getLocalHost(), portNumber))
    theSocket.close()
  }

  test("setReuseAddress Boolean") {
    var theSocket1: DatagramSocket = null
    var theSocket2: DatagramSocket = null

    val sockAddr: SocketAddress = null
    assertThrows[BindException] {
      val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 49153)
      theSocket1 = new DatagramSocket(sockAddr)
      theSocket2 = new DatagramSocket(sockAddr)
      theSocket1.setReuseAddress(false)
      theSocket2.setReuseAddress(false)
      theSocket1.bind(theAddress)
      theSocket2.bind(theAddress)
    }
    theSocket1.close()
    theSocket2.close()
    var theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 49154)
    theSocket1 = new DatagramSocket(sockAddr)
    theSocket2 = new DatagramSocket(sockAddr)
    theSocket1.setReuseAddress(true)
    theSocket2.setReuseAddress(true)
    theSocket1.bind(theAddress)
    theSocket2.bind(theAddress)

    theSocket1.close()
    theSocket2.close()

    assertThrows[BindException] {
      theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 49155)
      theSocket1 = new DatagramSocket(sockAddr)
      theSocket2 = new DatagramSocket(sockAddr)
      theSocket1.bind(theAddress)
      theSocket2.bind(theAddress)
    }
  }

  test("getReuseAddress") {
    val theSocket = new DatagramSocket()
    theSocket.setReuseAddress(true)
    assert(theSocket.getReuseAddress())
    theSocket.setReuseAddress(false)
    assert(!theSocket.getReuseAddress())
  }

  test("setBroadcast Boolean") {
    val theSocket = new DatagramSocket(49153)
    theSocket.setBroadcast(false)
    val theBytes = Array[Byte](-1, -1, -1, -1)

    assertThrows[ConnectException] {
      theSocket.connect(
        new InetSocketAddress(InetAddress.getByAddress(theBytes), 49154))
    }

    theSocket.setBroadcast(true)
    theSocket.connect(
      new InetSocketAddress(InetAddress.getByAddress(theBytes), 49155))
    theSocket.close()
  }

  test("getBroadcast") {
    val theSocket = new DatagramSocket()
    theSocket.setBroadcast(true)
    assert(theSocket.getBroadcast())
    theSocket.setBroadcast(false)
    assert(!theSocket.getBroadcast())
  }

  test("setTrafficClass Int") {
    val IPTOS_LOWCOST     = 0x2
    val IPTOS_RELIABILITY = 0x4
    val IPTOS_THROUGHPUT  = 0x8
    val IPTOS_LOWDELAY    = 0x10

    val theSocket = new DatagramSocket(49153)

    assertThrows[IllegalArgumentException] {
      theSocket.setTrafficClass(256)
    }

    assertThrows[IllegalArgumentException] {
      theSocket.setTrafficClass(-1)
    }

    theSocket.setTrafficClass(IPTOS_LOWCOST)
    theSocket.setTrafficClass(IPTOS_THROUGHPUT)
    theSocket.close()
  }

  test("getTrafficClass") {
    val IPTOS_LOWCOST     = 0x2
    val IPTOS_RELIABILITY = 0x4
    val IPTOS_THROUGHPUT  = 0x8
    val IPTOS_LOWDELAY    = 0x10

    val theSocket = new DatagramSocket(49153)
    theSocket.setTrafficClass(IPTOS_LOWCOST)
    assertEquals(IPTOS_LOWCOST, theSocket.getTrafficClass())
    theSocket.close()
  }

  test("isClosed") {
    var theSocket = new DatagramSocket()

    assert(!theSocket.isClosed())
    theSocket.close()
    assert(theSocket.isClosed())

    val theAddress = new InetSocketAddress(InetAddress.getLocalHost(), 49153)
    theSocket = new DatagramSocket(theAddress)
    assert(!theSocket.isClosed())
    theSocket.close()
    assert(theSocket.isClosed())
  }
}
