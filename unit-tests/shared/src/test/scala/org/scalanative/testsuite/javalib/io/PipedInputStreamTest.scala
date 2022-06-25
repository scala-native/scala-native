// Ported from Apache Harmony
package org.scalanative.testsuite.javalib.io

import java.io._
import org.junit.Test
import org.junit.Assert._
import org.junit.After

object PipedInputStreamTest {
  private[io] class PWriter(var pos: PipedOutputStream, val nbytes: Int)
      extends Runnable {
    bytes = new Array[Byte](nbytes)
    for (i <- 0 until bytes.length) {
      bytes(i) = (System.currentTimeMillis % 9).toByte
    }
    var bytes: Array[Byte] = null
    override def run(): Unit = {
      try {
        pos.write(bytes)
        this synchronized notify()
      } catch {
        case e: IOException =>
          e.printStackTrace(System.out)
          System.out.println("Could not write bytes")
      }
    }
  }
  private[io] class Worker private[io] (var out: PipedOutputStream)
      extends Thread {
    override def run(): Unit = {
      try {
        out.write(20)
        out.close()
        Thread.sleep(5000)
      } catch {
        case e: Exception =>

      }
    }
  }
}

class PipedInputStreamTest {
  import PipedInputStreamTest._

  private var t: Thread = _

  @throws[IOException]
  @Test def testConstructorPipedOutputStream(): Unit = {
    new PipedInputStream(new PipedOutputStream()).available()
  }

  @throws[IOException]
  @Test def testReadException(): Unit = {
    val pis = new PipedInputStream()
    val pos = new PipedOutputStream()
    try {
      pis.connect(pos)
      t = new Thread {
        val pw = new PipedInputStreamTest.PWriter(pos, 1000)
      }
      t.start()
      assertTrue(t.isAlive)
      while (true) {
        pis.read()
        t.interrupt()
      }
    } catch {
      case e: IOException =>
        if (!e.getMessage.contains("Write end dead")) throw e
    } finally
      try {
        pis.close()
        pos.close()
      } catch {
        case ee: IOException => ()
      }
  }

  @throws[Exception]
  @Test def testAvailable(): Unit = {
    val pis = new PipedInputStream
    val pos = new PipedOutputStream
    pis.connect(pos)
    var pw: PWriter = null
    t = new Thread {
      pw = new PipedInputStreamTest.PWriter(pos, 1000)
    }
    t.start()
    pw.synchronized { pw.wait(10000) }

    assertTrue(
      "Available returned incorrect number of bytes: " + pis.available(),
      pis.available() == 1000
    )
    val pin = new PipedInputStream()
    val pout = new PipedOutputStream(pin)
    // We know the PipedInputStream buffer size is 1024.
    // Writing another byte would cause the write to wait
    // for a read before returning
    for (i <- 0 until 1024) { pout.write(i) }
    assertEquals("Incorrect available count", 1024, pin.available())
  }

  @throws[IOException]
  @Test def testClose(): Unit = {
    val pis = new PipedInputStream()
    val pos = new PipedOutputStream()
    pis.connect(pos)
    pis.close()
    try {
      pos.write(127.toByte)
      fail("Failed to throw expected exception")
    } catch {
      case e: IOException =>

      // The spec for PipedInput saya an exception should be thrown if
      // a write is attempted to a closed input. The PipedOuput spec
      // indicates that an exception should be thrown only when the
      // piped input thread is terminated without closing
    }
  }

  @throws[Exception]
  @Test def testConnectPipedOutputStream(): Unit = {
    val pis = new PipedInputStream()
    val pos = new PipedOutputStream()
    assertEquals(
      "Non-conected pipe returned non-zero available bytes",
      0,
      pis.available()
    )
    pis.connect(pos)
    var pw: PWriter = null
    t = new Thread {
      pw = new PipedInputStreamTest.PWriter(pos, 1000)
    }
    t.start()
    pw.synchronized { pw.wait(10000) }

    assertEquals(
      "Available returned incorrect number of bytes",
      1000,
      pis.available()
    )
  }

  @throws[Exception]
  @Test def testRead(): Unit = {
    val pis = new PipedInputStream()
    val pos = new PipedOutputStream()
    pis.connect(pos)
    var pw: PWriter = null
    t = new Thread {
      pw = new PipedInputStreamTest.PWriter(pos, 1000)
    }
    t.start()
    pw.synchronized { pw.wait(10000) }

    assertEquals(
      "Available returned incorrect number of bytes",
      1000,
      pis.available()
    )
    assertEquals("read returned incorrect byte", pw.bytes(0), pis.read.toByte)
  }

  /** @tests
   *    java.io.PipedInputStream#read(byte[], int, int)
   */
  @throws[Exception]
  @Test def testReadArrayByte(): Unit = {
    val pis = new PipedInputStream
    val pos = new PipedOutputStream
    pis.connect(pos)
    var pw: PWriter = null
    t = new Thread { pw = new PipedInputStreamTest.PWriter(pos, 1000) }
    t.start()
    val buf = new Array[Byte](400)
    pw.synchronized { pw.wait(10000) }

    assertTrue(
      "Available returned incorrect number of bytes: " + pis.available(),
      pis.available() == 1000
    )
    pis.read(buf, 0, 400)
    for (i <- 0 until 400) {
      assertEquals("read returned incorrect byte[]", pw.bytes(i), buf(i))
    }
  }

  @throws[IOException]
  @Test def testReadArrayByte2(): Unit = {
    val obj = new PipedInputStream()
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
  @throws[IOException]
  @Test def testReadArrayByte3(): Unit = {
    val obj = new PipedInputStream
    try {
      obj.read(new Array[Byte](0), -1, 0)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case t: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case t: IndexOutOfBoundsException =>

    }
  }
  @throws[IOException]
  @Test def testReadArrayByte4(): Unit = {
    val obj = new PipedInputStream
    try {
      obj.read(new Array[Byte](0), -1, -1)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case t: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case t: IndexOutOfBoundsException =>
    }
  }

  @throws[IOException]
  @Test def testReceive(): Unit = {
    locally {
      val pis = new PipedInputStream
      val pos = new PipedOutputStream
      // test if writer recognizes dead reader
      pis.connect(pos)
      class WriteRunnable extends Runnable {
        private[io] var pass = false
        private[io] var readerAlive = true
        override def run(): Unit = {
          try {
            pos.write(1)
            while (readerAlive) {}
            // should throw exception since reader thread is now dead
            try pos.write(1)
            catch {
              case e: IOException =>
                pass = true
            }
          } catch {
            case e: IOException => ()
          }
        }
      }
      val writeRunnable = new WriteRunnable
      val writeThread = new Thread(writeRunnable)
      class ReadRunnable extends Runnable {
        private[io] var pass = false
        override def run(): Unit = {
          try {
            pis.read
            pass = true
          } catch {
            case e: IOException =>

          }
        }
      }

      val readRunnable = new ReadRunnable
      val readThread = new Thread(readRunnable)
      writeThread.start()
      readThread.start()
      while (readThread.isAlive) {}
      writeRunnable.readerAlive = false
      assertTrue("reader thread failed to read", readRunnable.pass)
      while (writeThread.isAlive) {}
      assertTrue(
        "writer thread failed to recognize dead reader",
        writeRunnable.pass
      )
    }
    // attempt to write to stream after writer closed
    locally {
      val pis = new PipedInputStream()
      val pos = new PipedOutputStream()
      pis.connect(pos)
      class MyRunnable extends Runnable {
        private[io] var pass = false
        override def run(): Unit = {
          try pos.write(1)
          catch {
            case e: IOException =>
              pass = true
          }
        }
      }
      val myRun = new MyRunnable
      t = pis.synchronized { new Thread(myRun) }
      // thread t will be blocked inside pos.write(1)
      // when it tries to call the synchronized method pis.receive
      // because we hold the monitor for object pis
      t.start()
      try // wait for thread t to get to the call to pis.receive
        Thread.sleep(100)
      catch {
        case e: InterruptedException =>

      }
      // now we close
      pos.close()

      // we have exited the synchronized block, so now thread t will make
      // a call to pis.receive AFTER the output stream was closed,
      // in which case an IOException should be thrown
      while (t.isAlive()) {}
      assertTrue(
        "write failed to throw IOException on closed PipedOutputStream",
        myRun.pass
      )
    }
  }
  @throws[Exception]
  @Test def testReadAfterWriteClose(): Unit = {
    val in = new PipedInputStream
    val out = new PipedOutputStream
    in.connect(out)
    val worker = new PipedInputStreamTest.Worker(out)
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

  /** Tears down the fixture, for example, close a network connection. This
   *  method is called after a test is executed.
   */
  @After def tearDown(): Unit = {
    try if (t != null) t.interrupt()
    catch {
      case ignore: Exception => ()
    }
  }
}
