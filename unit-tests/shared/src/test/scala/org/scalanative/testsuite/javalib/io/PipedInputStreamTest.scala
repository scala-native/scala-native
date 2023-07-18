/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.scalanative.testsuite.javalib.io

import java.io.{IOException, PipedInputStream, PipedOutputStream}
import org.junit._
import org.junit.Assert._

object PipedInputStreamTest {
  @BeforeClass def checkRuntime(): Unit =
    scala.scalanative.junit.utils.AssumesHelper.assumeMultithreadingIsEnabled()
}
class PipedInputStreamTest {

  /** Tears down the fixture, for example, close a network connection. This
   *  method is called after a test is executed.
   */
  @After def tearDown(): Unit = {
    try
      if (t != null) {
        t.interrupt()
      }
    catch { case _: Exception => }
  }

  private class PWriter(var pos: PipedOutputStream, var bytes: Array[Byte])
      extends Runnable {
    override def run(): Unit = {
      try {
        pos.write(bytes)
        synchronized {
          notify()
        }
      } catch {
        case e: IOException =>
          e.printStackTrace(System.out)
          println("Could not write bytes")
      }
    }
  }

  private var t: Thread = _
  private var pw: PWriter = _
  private var pis: PipedInputStream = _
  private var pos: PipedOutputStream = _

  /** @tests
   *    java.io.PipedInputStream#PipedInputStream()
   */
  @Test def test_Constructor(): Unit = {
    // Used in tests
  }

  /** @tests
   *    java.io.PipedInputStream#PipedInputStream(java.io.PipedOutputStream)
   */
  @throws[IOException]
  @Test def test_Constructor_PipedOutputStream(): Unit = {
    pis = new PipedInputStream(new PipedOutputStream)
    pis.available()
  }

  /** @test
   *    java.io.PipedInputStream#read()
   */
  @throws[IOException]
  @Test def test_readException(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream

    try {
      pis.connect(pos)
      pw = new PWriter(pos, new Array[Byte](1000))
      t = new Thread(pw)
      t.start()
      assertTrue(t.isAlive)
      while (true) {
        pis.read()
        t.interrupt()
      }
    } catch {
      case e: IOException =>
        if (!e.getMessage.contains("Write end dead")) {
          throw e
        }
    } finally {
      try {
        pis.close()
        pos.close()
      } catch {
        case _: IOException =>
      }
    }
  }

  /** @tests
   *    java.io.PipedInputStream#available()
   */
  @throws[Exception]
  @Test def test_available(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream

    pis.connect(pos)
    pw = new PWriter(pos, new Array[Byte](1000))
    t = new Thread(pw)
    t.start()

    pw.synchronized {
      pw.wait(10000)
    }
    assertTrue(
      "Available returned incorrect number of bytes: " + pis.available(),
      pis.available == 1000
    )

    val pin = new PipedInputStream
    val pout = new PipedOutputStream(pin)
    // We know the PipedInputStream buffer size is 1024.
    // Writing another byte would cause the write to wait
    // for a read before returning
    for (i <- 0 until 1024) {
      pout.write(i)
    }
    assertEquals("Incorrect available count", 1024, pin.available)
  }

  /** @tests
   *    java.io.PipedInputStream#close()
   */
  @throws[IOException]
  @Test def test_close(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream
    pis.connect(pos)
    pis.close()
    try {
      pos.write(127.asInstanceOf[Byte])
      fail("Failed to throw expected exception")
    } catch {
      case _: IOException =>
      // The spec for PipedInput saya an exception should be thrown if
      // a write is attempted to a closed input. The PipedOuput spec
      // indicates that an exception should be thrown only when the
      // piped input thread is terminated without closing
    }
  }

  /** @tests
   *    java.io.PipedInputStream#connect(java.io.PipedOutputStream)
   */
  @throws[Exception]
  @Test def test_connect_PipedOutputStream(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream
    assertEquals(
      "Non-conected pipe returned non-zero available bytes",
      0,
      pis.available
    )

    pis.connect(pos)
    pw = new PWriter(pos, new Array[Byte](1000))
    t = new Thread(pw)
    t.start()

    pw.synchronized {
      pw.wait(10000)
    }
    assertEquals(
      "Available returned incorrect number of bytes",
      1000,
      pis.available
    )
  }

  /** @tests
   *    java.io.PipedInputStream#read()
   */
  @throws[Exception]
  @Test def test_read(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream

    pis.connect(pos)
    pw = new PWriter(pos, new Array[Byte](1000))
    t = new Thread(pw)
    t.start()

    pw.synchronized {
      pw.wait(10000)
    }
    assertEquals(
      "Available returned incorrect number of bytes",
      1000,
      pis.available
    )
    assertEquals(
      "read returned incorrect byte",
      pw.bytes(0),
      pis.read.asInstanceOf[Byte]
    )
  }

  /** @tests
   *    java.io.PipedInputStream#read(byte[], int, int)
   */
  @throws[Exception]
  @Test def test_read_BII(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream

    pis.connect(pos)
    pw = new PWriter(pos, new Array[Byte](1000))
    t = new Thread(pw)
    t.start()

    val buf = new Array[Byte](400)
    pw.synchronized {
      pw.wait(10000)
    }
    assertTrue(
      "Available returned incorrect number of bytes: " + pis.available,
      pis.available == 1000
    )
    pis.read(buf, 0, 400)
    for (i <- 0 until 400) {
      assertEquals("read returned incorrect byte[]", pw.bytes(i), buf(i))
    }
  }

  /** @tests
   *    java.io.PipedInputStream#read(byte[], int, int) Regression for
   *    HARMONY-387
   */
  @throws[IOException]
  @Test def test_read_BII_2(): Unit = {
    val obj = new PipedInputStream
    try {
      obj.read(new Array[Byte](0), 0, -1)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case t: IndexOutOfBoundsException =>
        assertEquals(
          "IndexOutOfBoundsException rather than a subclass expected",
          classOf[IndexOutOfBoundsException],
          t.getClass
        )
    }
  }

  /** @tests
   *    java.io.PipedInputStream#read(byte[], int, int)
   */
  @throws[IOException]
  @Test def test_read_BII_3(): Unit = {
    val obj = new PipedInputStream
    try {
      obj.read(new Array[Byte](0), -1, 0)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case _: IndexOutOfBoundsException =>
    }
  }

  /** @tests
   *    java.io.PipedInputStream#read(byte[], int, int)
   */
  @throws[IOException]
  @Test def test_read_BII_4(): Unit = {
    val obj = new PipedInputStream
    try {
      obj.read(new Array[Byte](0), -1, -1)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case _: IndexOutOfBoundsException =>
    }
  }

  /** @tests
   *    java.io.PipedInputStream#receive(int)
   */
  @throws[IOException]
  @Test def test_receive(): Unit = {
    pis = new PipedInputStream
    pos = new PipedOutputStream

    // test if writer recognizes dead reader
    pis.connect(pos)
    object writeRunnable extends Runnable {
      var pass = false
      @volatile var readerAlive = true

      override def run(): Unit = {
        try {
          pos.write(1)
          while (readerAlive) {
            // do nothing
          }
          try {
            // should throw exception since reader thread
            // is now dead
            pos.write(1)
          } catch {
            case _: IOException =>
              pass = true
          }
        } catch {
          case _: IOException =>
        }
      }
    }
    val writeThread = new Thread(writeRunnable)
    object readRunnable extends Runnable {
      var pass = false

      override def run(): Unit = {
        try {
          pis.read()
          pass = true
        } catch {
          case _: IOException =>
        }
      }
    }
    val readThread = new Thread(readRunnable)
    writeThread.start()
    readThread.start()
    while (readThread.isAlive) {
      // do nothing
    }
    writeRunnable.readerAlive = false
    assertTrue("reader thread failed to read", readRunnable.pass)
    while (writeThread.isAlive) {
      // do nothing
    }
    assertTrue(
      "writer thread failed to recognize dead reader",
      writeRunnable.pass
    )

    // attempt to write to stream after writer closed
    pis = new PipedInputStream
    pos = new PipedOutputStream

    pis.connect(pos)
    object myRun extends Runnable {
      var pass = false

      override def run(): Unit = {
        try {
          pos.write(1)
        } catch {
          case _: IOException =>
            pass = true
        }
      }
    }
    pis.synchronized {
      t = new Thread(myRun)
      // thread t will be blocked inside pos.write(1)
      // when it tries to call the synchronized method pis.receive
      // because we hold the monitor for object pis
      t.start()
      try {
        // wait for thread t to get to the call to pis.receive
        Thread.sleep(100)
      } catch {
        case _: InterruptedException =>
      }
      // now we close
      pos.close()
    }
    // we have exited the synchronized block, so now thread t will make
    // a call to pis.receive AFTER the output stream was closed,
    // in which case an IOException should be thrown
    while (t.isAlive) {
      // do nothing
    }
    assertTrue(
      "write failed to throw IOException on closed PipedOutputStream",
      myRun.pass
    )
  }

  private class Worker(private val out: PipedOutputStream) extends Thread {
    override def run(): Unit = {
      try {
        out.write(20)
        out.close()
        Thread.sleep(5000)
      } catch {
        case _: Exception =>
      }
    }
  }

  @throws[Exception]
  @Test def test_read_after_write_close(): Unit = {
    val in = new PipedInputStream
    val out = new PipedOutputStream
    in.connect(out)
    val worker = new Worker(out)
    worker.start()
    Thread.sleep(2000)
    assertEquals("Should read 20.", 20, in.read)
    worker.join()
    assertEquals("Write end is closed, should return -1", -1, in.read)
    val buf = new Array[Byte](1)
    assertEquals(
      "Write end is closed, should return -1",
      -1,
      in.read(buf, 0, 1)
    )
    assertEquals("Buf len 0 should return first", 0, in.read(buf, 0, 0))
    in.close()
    out.close()
  }

}
