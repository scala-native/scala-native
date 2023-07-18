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

import java.io._
import org.junit._
import org.junit.Assert._

object PipedWriterTest {
  @BeforeClass def checkRuntime(): Unit =
    scala.scalanative.junit.utils.AssumesHelper.assumeMultithreadingIsEnabled()
}
class PipedWriterTest {

  private class PReader(var pr: PipedReader) extends Runnable {
    var buf: Array[Char] = new Array[Char](10)

    def this(pw: PipedWriter) = {
      this(new PipedReader(pw))
    }

    def run(): Unit = {
      try {
        var r = 0
        var i = 0
        var break = false
        while (!break && i < buf.length) {
          r = pr.read()
          if (r == -1)
            break = true
          buf(i) = r.asInstanceOf[Char]
          i += 1
        }
      } catch {
        case e: Exception =>
          println(
            "Exception reading (" + Thread
              .currentThread()
              .getName + "): " + e.toString
          )
      }
    }
  }

  private var rdrThread: Thread = _
  private var reader: PReader = _
  private var pw: PipedWriter = _

  /** @tests
   *    java.io.PipedWriter#PipedWriter()
   */
  @Test def test_Constructor(): Unit = {
    // Test for method java.io.PipedWriter()
    // Used in tests
  }

  /** @tests
   *    java.io.PipedWriter#PipedWriter(java.io.PipedReader)
   */
  @throws[Exception]
  @Test def test_Constructor_PipedReader(): Unit = {
    // Test for method java.io.PipedWriter(java.io.PipedReader)
    val buf = new Array[Char](10)
    "HelloWorld".getChars(0, 10, buf, 0)
    val rd = new PipedReader()
    pw = new PipedWriter(rd)
    reader = new PReader(rd)
    rdrThread = new Thread(reader, "Constructor(Reader)")
    rdrThread.start()
    pw.write(buf)
    pw.close()
    rdrThread.join(500)
    assertEquals(
      "Failed to construct writer",
      "HelloWorld",
      new String(reader.buf)
    )
  }

  /** @tests
   *    java.io.PipedWriter#close()
   */
  @throws[Exception]
  @Test def test_close(): Unit = {
    // Test for method void java.io.PipedWriter.close()
    val buf = new Array[Char](10)
    "HelloWorld".getChars(0, 10, buf, 0)
    val rd = new PipedReader()
    pw = new PipedWriter(rd)
    reader = new PReader(rd)
    pw.close()
    try {
      pw.write(buf)
      fail(
        "Should have thrown exception when attempting to write to closed writer."
      )
    } catch {
      case _: Exception => // correct
    }
  }

  /** @tests
   *    java.io.PipedWriter#connect(java.io.PipedReader)
   */
  @throws[Exception]
  @Test def test_connect_PipedReader(): Unit = {
    // Test for method void java.io.PipedWriter.connect(java.io.PipedReader)
    val buf = new Array[Char](10)
    "HelloWorld".getChars(0, 10, buf, 0)
    val rd = new PipedReader()
    pw = new PipedWriter()
    pw.connect(rd)
    reader = new PReader(rd)
    rdrThread = new Thread(reader, "connect")
    rdrThread.start()
    pw.write(buf)
    pw.close()
    rdrThread.join(500)
    assertEquals(
      "Failed to write correct chars",
      "HelloWorld",
      new String(reader.buf)
    )
  }

  /** @tests
   *    java.io.PipedWriter#flush()
   */
  @throws[Exception]
  @Test def test_flush(): Unit = {
    // Test for method void java.io.PipedWriter.flush()
    val buf = new Array[Char](10)
    "HelloWorld".getChars(0, 10, buf, 0)
    pw = new PipedWriter()
    reader = new PReader(pw)
    rdrThread = new Thread(reader, "flush")
    rdrThread.start()
    pw.write(buf)
    pw.flush()
    rdrThread.join(700)
    assertEquals(
      "Failed to flush chars",
      "HelloWorld",
      new String(reader.buf)
    )
  }

  /** @tests
   *    java.io.PipedWriter#write(char[], int, int)
   */
  @throws[Exception]
  @Test def test_write_CII(): Unit = {
    // Test for method void java.io.PipedWriter.write(char [], int, int)
    val buf = new Array[Char](10)
    "HelloWorld".getChars(0, 10, buf, 0)
    pw = new PipedWriter()
    reader = new PReader(pw)
    rdrThread = new Thread(reader, "writeCII")
    rdrThread.start()
    pw.write(buf, 0, 10)
    pw.close()
    rdrThread.join(1000)
    assertEquals(
      "Failed to write correct chars",
      "HelloWorld",
      new String(reader.buf)
    )
  }

  /** @tests
   *    java.io.PipedWriter#write(char[], int, int) Regression for HARMONY-387
   */
  @throws[IOException]
  @Test def test_write_$CII_2(): Unit = {
    val pr = new PipedReader()
    var obj: PipedWriter = null
    try {
      obj = new PipedWriter(pr)
      obj.write(new Array[Char](0), 0, -1)
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
   *    java.io.PipedWriter#write(char[], int, int)
   */
  @throws[IOException]
  @Test def test_write_$CII_3(): Unit = {
    val pr = new PipedReader()
    var obj: PipedWriter = null
    try {
      obj = new PipedWriter(pr)
      obj.write(new Array[Char](0), -1, 0)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case _: IndexOutOfBoundsException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedWriter#write(char[], int, int)
   */
  @throws[IOException]
  @Test def test_write_$CII_4(): Unit = {
    val pr = new PipedReader()
    var obj: PipedWriter = null
    try {
      obj = new PipedWriter(pr)
      obj.write(new Array[Char](0), -1, -1)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case _: IndexOutOfBoundsException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedWriter#write(char[], int, int)
   */
  @throws[IOException]
  @Test def test_write_$CII_5(): Unit = {
    val pr = new PipedReader()
    var obj: PipedWriter = null
    try {
      obj = new PipedWriter(pr)
      obj.write(null.asInstanceOf[Array[Char]], -1, 0)
      fail("NullPointerException expected")
    } catch {
      case _: IndexOutOfBoundsException =>
        fail("NullPointerException expected")
      case _: NullPointerException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedWriter#write(char[], int, int)
   */
  @throws[IOException]
  @Test def test_write_$CII_6(): Unit = {
    val pr = new PipedReader()
    var obj: PipedWriter = null
    try {
      obj = new PipedWriter(pr)
      obj.write(null.asInstanceOf[Array[Char]], -1, -1)
      fail("NullPointerException expected")
    } catch {
      case _: IndexOutOfBoundsException =>
        fail("NullPointerException expected")
      case _: NullPointerException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedWriter#write(char[], int, int)
   */
  @throws[IOException]
  @Test def test_write_$CII_notConnected(): Unit = {
    // Regression test for Harmony-2404
    // create not connected pipe
    val obj = new PipedWriter()

    // char array is null
    try {
      obj.write(null.asInstanceOf[Array[Char]], 0, 1)
      fail("IOException expected")
    } catch {
      case _: IOException => // expected
    }

    // negative offset
    try {
      obj.write(Array[Char](1), -10, 1)
      fail("IOException expected")
    } catch {
      case _: IOException => // expected
    }

    // wrong offset
    try {
      obj.write(Array[Char](1), 10, 1)
      fail("IOException expected")
    } catch {
      case _: IOException => // expected
    }

    // negative length
    try {
      obj.write(Array[Char](1), 0, -10)
      fail("IOException expected")
    } catch {
      case _: IOException => // expected
    }

    // all valid params
    try {
      obj.write(Array[Char](1, 1), 0, 1)
      fail("IOException expected")
    } catch {
      case _: IOException => // expected
    }
  }

  /** @tests
   *    java.io.PipedWriter#write(int)
   */
  @throws[Exception]
  @Test def test_write_I_MultiThread(): Unit = {
    val pr = new PipedReader()
    val pw = new PipedWriter()
    // test if writer recognizes dead reader
    pr.connect(pw)

    class WriteRunnable extends Runnable {
      var pass = false
      @volatile var readerAlive = true

      def run(): Unit = {
        try {
          pw.write(1)
          while (readerAlive) {
            // wait the reader thread dead
          }
          try {
            // should throw exception since reader thread
            // is now dead
            pw.write(1)
          } catch {
            case _: IOException => pass = true
          }
        } catch {
          case _: IOException => // ignore
        }
      }
    }
    val writeRunnable = new WriteRunnable
    val writeThread = new Thread(writeRunnable)
    class ReadRunnable extends Runnable {
      var pass = false

      def run(): Unit = {
        try {
          pr.read()
          pass = true
        } catch {
          case _: IOException => // ignore
        }
      }
    }
    val readRunnable = new ReadRunnable
    val readThread = new Thread(readRunnable)
    writeThread.start()
    readThread.start()
    while (readThread.isAlive) {
      // wait the reader thread dead
    }
    writeRunnable.readerAlive = false
    assertTrue("reader thread failed to read", readRunnable.pass)
    while (writeThread.isAlive) {
      // wait the writer thread dead
    }
    assertTrue(
      "writer thread failed to recognize dead reader",
      writeRunnable.pass
    )
  }

  /** @tests
   *    java.io.PipedWriter#write(char[],int,int)
   */
  @throws[Exception]
  @Test def test_write_$CII_MultiThread(): Unit = {
    val pr = new PipedReader()
    val pw = new PipedWriter()

    // test if writer recognizes dead reader
    pr.connect(pw)

    class WriteRunnable extends Runnable {
      var pass = false
      @volatile var readerAlive = true

      def run(): Unit = {
        try {
          pw.write(1)
          while (readerAlive) {
            // wait the reader thread dead
          }
          try {
            // should throw exception since reader thread
            // is now dead
            val buf = new Array[Char](10)
            pw.write(buf, 0, 10)
          } catch {
            case _: IOException => pass = true
          }
        } catch {
          case _: IOException => // ignore
        }
      }
    }
    val writeRunnable = new WriteRunnable
    val writeThread = new Thread(writeRunnable)
    class ReadRunnable extends Runnable {
      var pass = false

      def run(): Unit = {
        try {
          pr.read()
          pass = true
        } catch {
          case _: IOException => // ignore
        }
      }
    }
    val readRunnable = new ReadRunnable
    val readThread = new Thread(readRunnable)
    writeThread.start()
    readThread.start()
    while (readThread.isAlive) {
      // wait the reader thread dead
    }
    writeRunnable.readerAlive = false
    assertTrue("reader thread failed to read", readRunnable.pass)
    while (writeThread.isAlive) {
      // wait the writer thread dead
    }
    assertTrue(
      "writer thread failed to recognize dead reader",
      writeRunnable.pass
    )
  }

  /** @tests
   *    java.io.PipedWriter#write(int)
   */
  @throws[Exception]
  @Test def test_writeI(): Unit = {
    // Test for method void java.io.PipedWriter.write(int)

    pw = new PipedWriter()
    reader = new PReader(pw)
    rdrThread = new Thread(reader, "writeI")
    rdrThread.start()
    pw.write(1)
    pw.write(2)
    pw.write(3)
    pw.close()
    rdrThread.join(1000)
    assertTrue(
      "Failed to write correct chars: " +
        reader.buf(0).toInt + " " +
        reader.buf(1).toInt + " " +
        reader.buf(2).toInt,
      reader.buf(0) == 1 && reader.buf(1) == 2 && reader.buf(2) == 3
    )
  }

  /** Tears down the fixture, for example, close a network connection. This
   *  method is called after a test is executed.
   */
  @After def tearDown(): Unit = {
    try {
      if (rdrThread != null) {
        rdrThread.interrupt()
      }
    } catch {
      case _: Exception => // ignore}
        try {
          if (pw != null) {
            pw.close()
          }
        } catch {
          case _: Exception => // ignore}
        }
    }
  }
}
