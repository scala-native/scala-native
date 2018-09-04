package java.nio.channels

import java.net._
import java.nio._

object SocketChannelSuite extends tests.Suite {

  test("bind") {
    val ch   = SocketChannel.open
    val sock = ch.socket
    ch.bind(new InetSocketAddress("127.0.0.1", 0))
    val inetSockAddr = ch.getLocalAddress.asInstanceOf[InetSocketAddress]

    assertEquals("127.0.0.1", inetSockAddr.getHostString)
    assertEquals("127.0.0.1", sock.getLocalAddress.getHostAddress)
    assert(inetSockAddr.getPort > 0)
    assertEquals(sock.getLocalPort, inetSockAddr.getPort)
    assertThrows[AlreadyBoundException] {
      ch.bind(new InetSocketAddress("127.0.0.1", 0))
    }
    assertThrows[BindException] {
      sock.bind(new InetSocketAddress("127.0.0.1", 0))
    }

    ch.close
  }

  test("bind with null") {
    val ch = SocketChannel.open
    ch.bind(null)

    assertEquals(
      ch.getLocalAddress.asInstanceOf[InetSocketAddress].getHostString,
      "0.0.0.0")

    ch.close
  }

  test("close") {
    val ch  = SocketChannel.open
    val buf = ByteBuffer.wrap(new Array[Byte](8))
    ch.close()

    assertNot(ch.isOpen)
    assert(ch.socket.isClosed)
    assertThrows[ClosedChannelException] { ch.read(buf) }
    assertThrows[ClosedChannelException] { ch.write(buf) }
    assertThrows[SocketException] { ch.socket.getInputStream }
    assertThrows[SocketException] { ch.socket.getOutputStream }
  }

  test("open") {
    val ch = SocketChannel.open

    assertNot(ch.socket.isClosed)
    assertNot(ch.socket.isBound)
    assertNot(ch.socket.isConnected)

    ch.close()
  }

  test("getLocalAddress") {
    val ch = SocketChannel.open

    assertEquals(ch.getLocalAddress, null)

    ch.bind(new InetSocketAddress("127.0.0.1", 0))

    ch.close()

    assertThrows[ClosedChannelException] { ch.getLocalAddress }
  }

  test("configureBlocking") {
    val ch = SocketChannel.open

    assert(ch.isBlocking)
    ch.configureBlocking(false)
    assertNot(ch.isBlocking)

    ch.close()
    assertThrows[ClosedChannelException] { ch.configureBlocking(true) }
  }

  test("connect") {
    val ch = SocketChannel.open

    class WrongSocketAddress extends SocketAddress

    assertThrows[UnsupportedAddressTypeException] {
      ch.connect(new WrongSocketAddress)
    }

    assertThrows[UnresolvedAddressException] {
      val addr = InetSocketAddress.createUnresolved("foo", 70)
      ch.connect(addr)
    }

    val httpAddr = new InetSocketAddress("scala-lang.org", 80)

    assertEquals(ch.connect(httpAddr), true)
    assertThrows[AlreadyConnectedException] { ch.connect(httpAddr) }

    ch.close()
    assertThrows[ClosedChannelException] {
      ch.connect(new InetSocketAddress("111.111.111.0", 80))
    }
  }

  test("read and write") {
    val ch  = SocketChannel.open
    val buf = ByteBuffer.allocate(1)

    assertThrows[NotYetConnectedException] { ch.read(buf) }
    assertThrows[NotYetConnectedException] { ch.write(buf) }

    assertThrows[NullPointerException] { ch.read(null: ByteBuffer) }
    assertThrows[NullPointerException] { ch.write(null: ByteBuffer) }

    ch.close()
    assertThrows[ClosedChannelException] { ch.read(buf) }
    assertThrows[ClosedChannelException] { ch.write(buf) }
  }
  /*
  test("SO_KEEPALIVE") {
    val ch = SocketChannel.open
    val prevValue = ch.getOption(StandardSocketOptions.SO_KEEPALIVE)
    ch.setOption(StandardSocketOptions.SO_KEEPALIVE, !prevValue)
    assertEquals(ch.getOption(StandardSocketOptions.SO_KEEPALIVE), !prevValue)
    ch.close
  }

  test("SO_SNDBUF") {
    val ch        = SocketChannel.open
    val prevValue = ch.getOption(StandardSocketOptions.SO_SNDBUF)
    ch.setOption(StandardSocketOptions.SO_SNDBUF, prevValue + 100)
    assert(ch.getOption(StandardSocketOptions.SO_SNDBUF) >= prevValue + 100)
    ch.close
  }

  test("SO_RCVBUF") {
    val ch = SocketChannel.open
    val prevValue = ch.getOption(StandardSocketOptions.SO_KEEPALIVE)
    ch.setOption(StandardSocketOptions.SO_KEEPALIVE, prevValue + 100)
    assert(ch.getOption(StandardSocketOptions.SO_KEEPALIVE) >= prevValue + 100)
    ch.close
  }

  test("SO_REUSEADDR") {
    val ch = SocketChannel.open
    val prevValue = ch.getOption(StandardSocketOptions.SO_REUSEADDR)
    ch.setOption(StandardSocketOptions.SO_REUSEADDR, !prevValue)
    assertEquals(ch.getOption(StandardSocketOptions.SO_REUSEADDR), !prevValue)
    ch.close
  }

  test("SO_LINGER") {
    val ch = SocketChannel.open
    val prevValue = ch.getOption(StandardSocketOptions.SO_LINGER)
    ch.setOption(StandardSocketOptions.SO_LINGER, prevValue + 100)
    assertEquals(ch.getOption(StandardSocketOptions.SO_LINGER), prevValue + 100)
    ch.close
  }

  test("TCP_NODELAY") {
    val ch        = SocketChannel.open
    val prevValue = ch.getOption(StandardSocketOptions.TCP_NODELAY)
    ch.setOption[java.lang.Boolean](StandardSocketOptions.TCP_NODELAY, java.lang.Boolean.TRUE)
    assertEquals(ch.getOption(StandardSocketOptions.TCP_NODELAY), !prevValue)
    ch.close
  } */

  test("blocking gather & scatter - integration test") {
    val ch = SocketChannel.open(new InetSocketAddress("google.com", 80))

    val writeBufs = Array("HEAD", " / HTTP", "/1.0\r\n", "\r\n").map(str =>
      ByteBuffer.wrap(str.getBytes))

    assertEquals(ch.write(writeBufs), writeBufs.map(_.limit).sum)

    val readBufs = Array.fill(3)(ByteBuffer.allocate(6))
    assertEquals(ch.read(readBufs), 18)

    assertEquals(new String(readBufs(0).array), "HTTP/1")
    assertEquals(new String(readBufs(1).array), ".0 302")
    assertEquals(new String(readBufs(2).array), " Found")

    ch.close
  }

  test("nonblocking gather & scatter - integration test") {
    val ch = SocketChannel.open(new InetSocketAddress("google.com", 80))
    ch.configureBlocking(false)

    val writeBufs = Array("HEAD", " / HTTP", "/1.0\r\n", "\r\n").map(str =>
      ByteBuffer.wrap(str.getBytes))

    assertEquals(ch.write(writeBufs), writeBufs.map(_.limit).sum)

    val readBufs = Array.fill(3)(ByteBuffer.allocate(6))

    // check that it's nonblocking
    var count = 0
    while (ch.read(readBufs) == 0) { count += 1 }
    assert(count > 0)

    assertEquals(new String(readBufs(0).array), "HTTP/1")
    assertEquals(new String(readBufs(1).array), ".0 302")
    assertEquals(new String(readBufs(2).array), " Found")

    ch.close
  }

  test("blocking http connection - integration test") {
    val ch      = SocketChannel.open(new InetSocketAddress("google.com", 80))
    val str     = "HEAD / HTTP/1.0\r\n\r\n"
    val sendBuf = ByteBuffer.wrap(str.getBytes)

    assertEquals(ch.write(sendBuf), str.length)

    val readBuf    = ByteBuffer.allocate(64)
    val howMuch    = ch.read(readBuf)
    val readString = new String(readBuf.array)
    assert(readString.startsWith("HTTP/1.0 302 Found"))

    ch.close
  }

  test("nonblocking http connection - integration test") {
    // tests with Selector are in SelectorSuite
    val ch = SocketChannel.open
    ch.configureBlocking(false)

    val connectResult = ch.connect(new InetSocketAddress("google.com", 80))
    assert(connectResult == false)

    while (!ch.finishConnect()) {}

    val str     = "HEAD / HTTP/1.0\r\n\r\n"
    val sendBuf = ByteBuffer.wrap(str.getBytes)

    var sentNum = 0
    val len     = str.length
    while (sentNum != len) {
      sentNum += ch.write(sendBuf)
    }
    assertEquals(sentNum, len)

    val readBuf = ByteBuffer.allocate(64)

    // safe because there is no way the response comes so fast
    assertEquals(ch.read(readBuf), 0)

    while (ch.read(readBuf) == 0) {}
    val readString = new String(readBuf.array)
    assert(readString.startsWith("HTTP/1.0 302 Found"))

    ch.close
  }

}
