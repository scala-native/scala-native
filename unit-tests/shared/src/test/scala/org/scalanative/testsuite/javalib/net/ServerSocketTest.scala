package org.scalanative.testsuite.javalib.net

import java.net._
import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ServerSocketTest {

  @Test def bind(): Unit = {
    val s1 = new ServerSocket()
    try {
      val addr = new InetSocketAddress(InetAddress.getLoopbackAddress, 0)

      s1.bind(addr)
      val port = s1.getLocalPort

      assertEquals(
        s1.getLocalSocketAddress,
        new InetSocketAddress(InetAddress.getLoopbackAddress, port)
      )
      assertTrue(s1.isBound)

      val s2 = new ServerSocket()
      val s3 = new ServerSocket() // creating new socket unlikely to throw.
      try {
        s2.bind(addr)
        assertThrows(classOf[BindException], s3.bind(s2.getLocalSocketAddress))
      } finally {
        s3.close()
        s2.close()
      }
    } finally {
      s1.close()
    }

    val s4 = new ServerSocket()
    try {
      assertThrows(
        classOf[BindException],
        s4.bind(new InetSocketAddress(InetAddress.getByName("101.0.0.0"), 0))
      )
    } finally {
      s4.close()
    }

    class UnsupportedSocketAddress extends SocketAddress {}

    val s5 = new ServerSocket()
    try {
      assertThrows(
        classOf[IllegalArgumentException],
        s5.bind(new UnsupportedSocketAddress)
      )
    } finally {
      s5.close()
    }
  }

  @Test def accept(): Unit = {
    val s = new ServerSocket(0)
    try {
      s.setSoTimeout(1)
      assertThrows(classOf[SocketTimeoutException], s.accept)
    } finally {
      s.close()
    }
  }

  @Test def close(): Unit = {
    val s = new ServerSocket(0)
    s.close
    assertThrows(classOf[SocketException], s.accept)
    // socket already closed, all paths.
  }

  @Test def closeWithActiveAccept(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val server = new ServerSocket(0)
    val serverThread = Future {
      // Accept will block until the server is closed. At that point it should throw SocketException.
      server.accept()
    }
    Thread.sleep(1000) // Give future plenty of time to get to accept()
    if (serverThread.isCompleted)
      fail("Server thread exited early: expected it to be blocked in accept()")
    server.close()
    assertThrows(
      classOf[java.net.SocketException],
      Await.result(serverThread, 10.seconds)
    )
  }

  @Test def soTimeout(): Unit = {
    val s = new ServerSocket(0)
    try {
      val prevValue = s.getSoTimeout
      s.setSoTimeout(prevValue + 100)
      assertEquals(prevValue + 100, s.getSoTimeout)
    } finally {
      s.close()
    }
  }

  @Test def testToString(): Unit = {
    val s1 = new ServerSocket(0)
    try {
      val port1 = s1.getLocalPort
      assertEquals(
        "ServerSocket[addr=0.0.0.0/0.0.0.0,localport="
          + port1 + "]",
        s1.toString
      )

      val s2 = new ServerSocket()
      try {
        assertEquals("ServerSocket[unbound]", s2.toString)

        s2.bind(new InetSocketAddress("127.0.0.1", 0))
        val port2 = s2.getLocalPort
        assertEquals(
          "ServerSocket[addr=/127.0.0.1,localport=" + port2 + "]",
          s2.toString
        )
      } finally {
        s2.close()
      }
    } finally {
      s1.close()
    }
  }
}
