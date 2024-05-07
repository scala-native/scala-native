package org.scalanative.testsuite.javalib.nio.channels

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.BeforeClass
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels._
import java.net.{InetAddress, InetSocketAddress}

object DatagramChannelTest {
  private lazy val loopback = InetAddress.getLoopbackAddress()
}

class DatagramChannelTest {
  import DatagramChannelTest._

  private def withDatagramChannel(f: DatagramChannel => Unit): Unit = {
    val dc = DatagramChannel.open()
    try {
      f(dc)
    } finally {
      dc.close()
    }
  }

  @Test def open(): Unit = {
    withDatagramChannel { dc =>
      assertTrue("Channel should be open", dc.isOpen())
      assertNull("Local address should be null", dc.getLocalAddress())
    }
  }

  @Test def close(): Unit = {
    val dc = DatagramChannel.open()
    dc.close()
    assertFalse("Channel should not be open", dc.isOpen())
    val src = ByteBuffer.allocate(0)
    val target = new InetSocketAddress(loopback, 0)
    assertThrows(classOf[IOException], dc.send(src, target))
  }

  @Test def bind(): Unit = {
    withDatagramChannel { dc =>
      dc.bind(null)
      val saddr = dc.getLocalAddress().asInstanceOf[InetSocketAddress]
      assertTrue("Created channel with incorrect port", saddr.getPort > 0)
    }
  }

  @Test def sendReceive(): Unit = {
    withDatagramChannel { sender =>
      withDatagramChannel { receiver =>
        receiver.bind(null)
        val target = new InetSocketAddress(
          loopback,
          receiver.getLocalAddress().asInstanceOf[InetSocketAddress].getPort()
        )

        val data = "Test Data"
        val bytes = data.getBytes()
        val src = ByteBuffer.wrap(bytes)

        val n = sender.send(src, target)
        assertTrue("Send bytes should match data size", n == bytes.length)

        val dst = ByteBuffer.allocate(bytes.length)
        receiver.receive(dst).asInstanceOf[InetSocketAddress]
        dst.flip()
        val receivedBytes = new Array[Byte](dst.remaining())
        dst.get(receivedBytes)
        val receivedData = new String(receivedBytes)

        assertEquals(
          "Received data should be equal to sent data",
          data,
          receivedData
        )
      }
    }
  }

  @Test def readWrite(): Unit = {
    withDatagramChannel { sender =>
      withDatagramChannel { receiver =>
        sender.bind(null)
        val source = new InetSocketAddress(
          loopback,
          sender.getLocalAddress().asInstanceOf[InetSocketAddress].getPort()
        )
        receiver.bind(null)
        val target = new InetSocketAddress(
          loopback,
          receiver.getLocalAddress().asInstanceOf[InetSocketAddress].getPort()
        )

        sender.connect(target)
        receiver.connect(source)

        val data = "Test Data"
        val bytes = data.getBytes()
        val src = ByteBuffer.wrap(bytes)
        sender.write(src)

        val dst = ByteBuffer.allocate(bytes.length)
        receiver.read(dst)
        dst.flip()
        val readBytes = new Array[Byte](dst.remaining())
        dst.get(readBytes)
        val readData = new String(readBytes)
        assertEquals(
          "Read data should be equal to written data",
          data,
          readData
        )
      }
    }
  }

  @Test def nonBLocking(): Unit = {
    withDatagramChannel { dc =>
      assertTrue("Channel should be blocking by default", dc.isBlocking())
      dc.configureBlocking(false)
      assertFalse("Channel should be non-blocking", dc.isBlocking())
      val dst = ByteBuffer.allocate(1)
      val source = dc.receive(dst)
      assertNull("Receive should return a null source", source)
      dc.connect(new InetSocketAddress(loopback, 7))
      val n = dc.read(dst)
      assertTrue("Receive should return directly with zero bytes read", n == 0)
    }
  }
}
