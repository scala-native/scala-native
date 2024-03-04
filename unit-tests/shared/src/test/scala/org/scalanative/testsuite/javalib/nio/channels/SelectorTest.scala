package org.scalanative.testsuite.javalib.nio.channels

import java.nio._
import java.net._

class SelectorTest {

//  test("nonblockin DatagramChannel dns - integration test") {
//    val selector = Selector.open
//    val channels = List.fill(3)(DatagramChannel.open)
//    val keys = channels.map { ch =>
//      ch.configureBlocking(false)
//      val key = ch.register(selector, SelectionKey.OP_CONNECT)
//      assertNot(ch.connect(new InetSocketAddress("google.com", 80)))
//      key
//    }
//
//    var selectedNum = 0
//    while (selectedNum != 3) {
//      selectedNum += selector.select()
//
//      selector.selectedKeys.foreach(key => {
//        assert(key.channel.asInstanceOf[SocketChannel].finishConnect())
//      })
//      selector.selectedKeys.clear()
//    }
//
//    channels.foreach(ch => assert(ch.isConnected))
//
//    /*
//     * There is a bug here for now
//    val str = "HEAD / HTTP/1.0\r\n\r\n"
//    val len = str.length
//    val bufs = List.fill(3)(ByteBuffer.wrap(str.getBytes))
//    (channels zip bufs).
//      map { case (ch, b) => ch.write(b) }.
//      foreach(assertEquals(len, _))
//    val readBuf = ByteBuffer.allocate(64)
//    assertEquals(ch.read(readBuf), 0)
//    while (ch.read(readBuf) == 0) { }
//    val readString = new String(readBuf.array)
//    assert(readString.startsWith("HTTP/1.0 302 Found"))*/
//
//    channels.foreach(_.close)
//  }

}
