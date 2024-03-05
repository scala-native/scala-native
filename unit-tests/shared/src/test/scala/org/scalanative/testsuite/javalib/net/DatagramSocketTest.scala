package org.scalanative.testsuite.javalib.net

import java.io.IOException
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.{util => ju}

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform
import scala.collection.JavaConverters._

class DatagramSocketTest {

  private val loopback = InetAddress.getLoopbackAddress()

  @Test def constructor(): Unit = {
    val ds = new DatagramSocket(0, loopback)
    try {
      assertTrue("Created socket with incorrect port", ds.getLocalPort() > 0)
      assertEquals(
        "Created socket with incorrect address",
        loopback,
        ds.getLocalAddress()
      )
    } finally {
      ds.close()
    }
  }

  @Test def close(): Unit = {
    val ds = new DatagramSocket(0)
    ds.close()
    val dp = new DatagramPacket(
      "Test String".getBytes,
      11,
      loopback,
      0
    )
    assertThrows(classOf[IOException], ds.send(dp))
  }

  @Test def getLocalAddress(): Unit = {
    val ds = new DatagramSocket(null)
    try {
      assertTrue(
        "Returned incorrect local address when not bound",
        ds.getLocalAddress.isAnyLocalAddress
      )
      ds.bind(new InetSocketAddress(loopback, 0))
      assertEquals(
        "Returned incorrect local port when bound",
        loopback,
        ds.getLocalAddress
      )
      ds.close()
      assertTrue(
        "Returned incorrect local port when closed",
        ds.getLocalAddress == null
      )
    } finally {
      if (!ds.isClosed) ds.close()
    }
  }

  @Test def getLocalPort(): Unit = {
    val ds = new DatagramSocket(null)
    try {
      assertEquals(
        "Returned incorrect local port when not bound",
        0,
        ds.getLocalPort
      )
      ds.bind(null)
      assertTrue(
        "Returned incorrect local port when bound",
        ds.getLocalPort > 0
      )
      ds.close()
      assertEquals(
        "Returned incorrect local port when closed",
        -1,
        ds.getLocalPort
      )
    } finally {
      if (!ds.isClosed) ds.close()
    }
  }

  @Test def getInetAddress(): Unit = {
    val ds = new DatagramSocket()
    try {
      assertTrue(
        "Returned incorrect remote address when not connected",
        ds.getInetAddress() == null
      );
      ds.connect(loopback, 49152) // any valid port number
      assertEquals(
        "Returned incorrect remote address when connected",
        loopback,
        ds.getInetAddress()
      )
      ds.close()
      assertEquals(
        "Returned incorrect remote address when closed",
        loopback,
        ds.getInetAddress()
      )
    } finally {
      if (!ds.isClosed) ds.close()
    }
  }

  @Test def getPort(): Unit = {
    val ds = new DatagramSocket()
    try {
      assertEquals(
        "Returned incorrect remote port when not connected",
        -1,
        ds.getPort()
      );
      val port = 49152 // any valid port number
      ds.connect(loopback, port)
      assertEquals(
        "Returned incorrect remote port when connected",
        port,
        ds.getPort()
      )
      ds.close()
      assertEquals(
        "Returned incorrect remote port when closed",
        port,
        ds.getPort()
      )
    } finally {
      if (!ds.isClosed) ds.close()
    }
  }

  @Test def receiveBufferSize(): Unit = {
    // This test basically checks that getReceiveBufferSize &
    // setReceiveBufferSize do not unexpectedly throw and that the former
    // returns a minimally sane value.
    //
    // The Java 8 documentation at URL
    // https://docs.oracle.com/javase/8/docs/api/java/net/\
    //     Socket.html#setReceiveBufferSize-int- [sic trailing dash]
    // describes the argument for setReceiveBufferSize(int) &
    // setSendBufferSize(int) as a _hint_ to the operating system, _not_
    // a requirement or demand.  This description is basically unaltered
    // in Java 10.
    //
    // There are a number of reasons the operating system can choose to
    // ignore the hint. Changing the buffer size, even before a bind() call,
    // may not be implemented. The buffer size may already be at its
    // maximum.
    //
    // Since, by definition, the OS can ignore the hint, it makes no
    // sense to set the size, then re-read it and see if it changed.
    //
    // The sendBuffersize test refers to this comment.
    // Please keep both tests synchronized.

    val ds = new DatagramSocket(null)
    try {
      val prevValue = ds.getReceiveBufferSize
      assertTrue(prevValue > 0)
      ds.setReceiveBufferSize(prevValue + 100)
    } finally {
      ds.close()
    }
  }

  @Test def sendBufferSize(): Unit = {
    // This test basically checks that getSendBufferSize &
    // setSendBufferSize do not unexpectedly throw and that the former
    // returns a minimally sane value.
    // See more extensive comments in setBufferSize test.

    val ds = new DatagramSocket(null)
    try {
      val prevValue = ds.getSendBufferSize
      assertTrue(prevValue > 0)
      ds.setSendBufferSize(prevValue + 100)
    } finally {
      ds.close()
    }
  }

  @Test def broadcast(): Unit = {
    val ds = new DatagramSocket(null)
    try {
      val prevValue = ds.getBroadcast()
      assertTrue(prevValue)
      ds.setBroadcast(!prevValue)
    } finally {
      ds.close()
    }
  }

  @Test def bind(): Unit = {
    val ds1 = new DatagramSocket(null)
    try {
      val nonLocalAddr =
        new InetSocketAddress(InetAddress.getByName("123.123.123.123"), 0)
      assertThrows(
        "bind must fail for non local address",
        classOf[BindException],
        ds1.bind(nonLocalAddr)
      )
    } finally {
      ds1.close()
    }

    val ds2 = new DatagramSocket(null)
    try {
      ds2.bind(new InetSocketAddress(loopback, 0))
      val port = ds2.getLocalPort
      assertTrue("socket must be bound", ds2.isBound())
      assertEquals(
        "bind must use the given address",
        new InetSocketAddress(loopback, port),
        ds2.getLocalSocketAddress
      )
    } finally {
      ds2.close()
    }

    val ds3 = new DatagramSocket(null)
    try {
      ds3.bind(null)
      assertTrue("socket must be bound", ds3.isBound())
      assertTrue(
        "bind must use any available address when not provided",
        ds3.getLocalSocketAddress != null
      )
    } finally {
      ds3.close()
    }

    val ds4 = new DatagramSocket(null)
    try {
      ds4.bind(new InetSocketAddress(loopback, 0))
      val ds5 = new DatagramSocket()
      try {
        assertThrows(
          "bind must fail if the address is already in use",
          classOf[SocketException],
          ds5.bind(ds4.getLocalSocketAddress)
        )
      } finally {
        ds5.close()
      }
    } finally {
      ds4.close()
    }

    class UnsupportedSocketAddress extends SocketAddress
    val ds6 = new DatagramSocket(null)
    try {
      assertThrows(
        "bind must fail for unsupported SocketAddress type",
        classOf[IllegalArgumentException],
        ds6.bind(new UnsupportedSocketAddress)
      )
    } finally {
      ds6.close()
    }

    val ds7 = new DatagramSocket(null)
    try {
      assertThrows(
        "bind must fail for unresolved address",
        classOf[SocketException],
        ds7.bind(InetSocketAddress.createUnresolved("localhost", 0))
      )
    } finally {
      ds7.close()
    }
  }

  @Test def sendReceive(): Unit = {
    val ds1 = new DatagramSocket(new InetSocketAddress(loopback, 0))
    val ds2 = new DatagramSocket(new InetSocketAddress(loopback, 0))
    try {
      val data = "Test Data"
      val bytes = data.getBytes()
      val packet = new DatagramPacket(bytes, bytes.length)
      packet.setSocketAddress(ds2.getLocalSocketAddress())
      ds1.send(packet)

      val result =
        new DatagramPacket(Array.ofDim[Byte](bytes.length), bytes.length)
      ds2.setSoTimeout(500)
      ds2.receive(result)

      val receivedData = new String(result.getData())
      val remoteAddress =
        result.getSocketAddress().asInstanceOf[InetSocketAddress]
      assertEquals("Received incorrect data", data, receivedData)

      // Compare only address bytes, host names may vary (null, "", etc)
      assertTrue(
        "Received incorrect address",
        ju.Arrays.equals(
          ds1.getLocalAddress().getAddress,
          remoteAddress.getAddress().getAddress
        )
      )

      assertEquals(
        "Received incorrect port",
        ds1.getLocalPort(),
        remoteAddress.getPort()
      )
    } finally {
      ds1.close()
      ds2.close()
    }
  }

  @Test def connect(): Unit = {
    val ds1 = new DatagramSocket(new InetSocketAddress(loopback, 0))
    val ds2 = new DatagramSocket(new InetSocketAddress(loopback, 0))
    val ds3 = new DatagramSocket(new InetSocketAddress(loopback, 0))
    try {
      ds3.setSoTimeout(500)
      // connect ds3 to ds1.
      // Since Java 17: Datagrams in the socket's socket receive buffer,
      // which have not been received before invoking this method, may be discarded.
      ds3.connect(ds1.getLocalSocketAddress())
      assertTrue("Socket is not connected", ds3.isConnected())
      assertEquals(
        "Socket has incorrect remote address",
        ds1.getLocalAddress(),
        ds3.getInetAddress()
      )
      assertEquals(
        "Socket has incorrect remote port",
        ds1.getLocalPort(),
        ds3.getPort()
      )

      val data = "Test Data"
      val bytes = data.getBytes()
      val packet = new DatagramPacket(bytes, bytes.length)
      packet.setSocketAddress(ds3.getLocalSocketAddress())
      ds1.send(packet)

      val filteredData = "Bad Data"
      val filteredBytes = filteredData.getBytes()
      packet.setData(filteredBytes)
      ds2.send(packet)

      val result =
        new DatagramPacket(Array.ofDim[Byte](bytes.length), bytes.length)
      ds3.receive(result)
      val receivedData = new String(result.getData())
      val remoteAddress =
        result.getSocketAddress().asInstanceOf[InetSocketAddress]
      assertEquals("Received incorrect data", data, receivedData)

      // no message from ds2 should be received
      assertThrows(
        "Received unexpected data",
        classOf[SocketTimeoutException],
        ds3.receive(result)
      )
    } finally {
      ds1.close()
      ds2.close()
      ds3.close()
    }

    val ds4 = new DatagramSocket(new InetSocketAddress(loopback, 0))
    try {
      assertThrows(
        "Unresolved address can't be connected to",
        classOf[SocketException],
        ds4.connect(InetSocketAddress.createUnresolved("localhost", 8080))
      )
    } finally {}
  }

  @Test def sendReceiveBroadcast(): Unit = {
    // NetworkInterface.getNetworkInterfaces is not implemented in Windows
    assumeFalse("Not implemented in Windows", Platform.isWindows)

    // we need to find a network interface with broadcast support for this test
    NetworkInterface
      .getNetworkInterfaces()
      .asScala
      .filter(_.isUp())
      .flatMap(_.getInterfaceAddresses().asScala)
      .find(_.getBroadcast() != null)
      .foreach { ifBroadcastAddr =>
        val address = ifBroadcastAddr.getAddress()
        val broadcastAddress = ifBroadcastAddr.getBroadcast()
        val ds1 = new DatagramSocket(new InetSocketAddress(address, 0))
        val ds2 = new DatagramSocket(null)
        try {
          ds2.setSoTimeout(500)
          ds2.setReuseAddress(true)
          ds2.bind(new InetSocketAddress("0.0.0.0", 0))

          // joinGroup is Java 17+. Wildcard address in bind should be enough
          // val loopbackItf = NetworkInterface.getByInetAddress(address)
          // ds2.joinGroup(broadcastAddress, loopbackItf)

          val data = "Test Data"
          val bytes = data.getBytes()
          val packet = new DatagramPacket(bytes, bytes.length)
          packet.setAddress(broadcastAddress)
          packet.setPort(ds2.getLocalPort())
          ds1.setBroadcast(true)
          ds1.send(packet)

          val result =
            new DatagramPacket(Array.ofDim[Byte](bytes.length), bytes.length)
          ds2.receive(result)

          val receivedData = new String(result.getData())
          assertEquals("Received incorrect data", data, receivedData)
        } finally {
          ds1.close()
          ds2.close()
        }
      }
  }
}
