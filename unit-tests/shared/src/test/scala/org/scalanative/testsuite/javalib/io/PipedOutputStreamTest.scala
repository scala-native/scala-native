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

import org.junit.*
import org.junit.Assert.*

import java.io.{
  IOException,
  PipedInputStream,
  PipedOutputStream,
  UnsupportedEncodingException
}

object PipedOutputStreamTest {
  @BeforeClass def checkRuntime(): Unit =
    scala.scalanative.junit.utils.AssumesHelper.assumeMultithreadingIsEnabled()
}
class PipedOutputStreamTest {

  private class PReader(out: PipedOutputStream) extends Runnable {
    private val reader: PipedInputStream = new PipedInputStream(out)

    def getReader: PipedInputStream = reader

    def available: Int = {
      try {
        reader.available()
      } catch {
        case _: Exception => -1
      }
    }

    def run(): Unit = {
      try {
        while (true) {
          Thread.sleep(1000)
          Thread.`yield`()
        }
      } catch {
        case _: InterruptedException =>
      }
    }

    def read(nbytes: Int): String = {
      val buf = new Array[Byte](nbytes)
      try {
        reader.read(buf, 0, nbytes)
        new String(buf, "UTF-8")
      } catch {
        case _: IOException =>
          println("Exception reading info")
          "ERROR"
      }
    }
  }

  private var rt: Thread = _
  private var reader: PReader = _
  private var out: PipedOutputStream = _

  /** @tests
   *    java.io.PipedOutputStream#PipedOutputStream()
   */
  @Test def test_Constructor(): Unit = {
    // Used in tests
  }

  /** @tests
   *    java.io.PipedOutputStream#PipedOutputStream(java.io.PipedInputStream)
   */
  @throws[Exception]
  @Test def test_Constructor_PipedInputStream(): Unit = {
    out = new PipedOutputStream(new PipedInputStream())
    out.write('b')
  }

  /** @tests
   *    java.io.PipedOutputStream#close()
   */
  @throws[Exception]
  @Test def test_close(): Unit = {
    out = new PipedOutputStream()
    reader = new PReader(out)
    rt = new Thread(reader)
    rt.start()
    out.close()
  }

  /** @tests
   *    java.io.PipedOutputStream#connect(java.io.PipedInputStream)
   */
  @throws[IOException]
  @Test def test_connect_PipedInputStream_Exception(): Unit = {
    out = new PipedOutputStream()
    out.connect(new PipedInputStream())
    try {
      out.connect(null)
      fail("should throw NullPointerException") // $NON-NLS-1$
    } catch {
      case _: NullPointerException => // expected
    }
  }

  /** @tests
   *    java.io.PipedOutputStream#connect(java.io.PipedInputStream)
   */
  @Test def test_connect_PipedInputStream(): Unit = {
    try {
      out = new PipedOutputStream()
      reader = new PReader(out)
      rt = new Thread(reader)
      rt.start()
      out.connect(new PipedInputStream())
      fail(
        "Failed to throw exception attempting connect on already connected stream"
      )
    } catch {
      case _: IOException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedOutputStream#flush()
   */
  @throws[IOException]
  @throws[UnsupportedEncodingException]
  @Test def test_flush(): Unit = {
    out = new PipedOutputStream()
    reader = new PReader(out)
    rt = new Thread(reader)
    rt.start()
    out.write("HelloWorld".getBytes("UTF-8"), 0, 10)
    assertTrue("Bytes written before flush", reader.available != 0)
    out.flush()
    assertEquals("Wrote incorrect bytes", "HelloWorld", reader.read(10))
  }

  /** @tests
   *    java.io.PipedOutputStream#write(byte[], int, int)
   */
  @throws[IOException]
  @throws[UnsupportedEncodingException]
  @Test def test_write_BII(): Unit = {
    out = new PipedOutputStream()
    reader = new PReader(out)
    rt = new Thread(reader)
    rt.start()
    out.write("HelloWorld".getBytes("UTF-8"), 0, 10)
    out.flush()
    assertEquals("Wrote incorrect bytes", "HelloWorld", reader.read(10))
  }

  /** @tests
   *    java.io.PipedOutputStream#write(byte[], int, int) Regression for
   *    HARMONY-387
   */
  @throws[IOException]
  @Test def test_write_BII_2(): Unit = {
    var pis: PipedInputStream = new PipedInputStream()
    var pos: PipedOutputStream = null
    try {
      pos = new PipedOutputStream(pis)
      pos.write(new Array[Byte](0), -1, -1)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case t: IndexOutOfBoundsException =>
        assertEquals(
          "IndexOutOfBoundsException rather than a subclass expected",
          classOf[IndexOutOfBoundsException],
          t.getClass
        )
    }

    // Regression for HARMONY-4311
    try {
      pis = new PipedInputStream()
      val out = new PipedOutputStream(pis)
      out.write(null, -10, 10)
      fail("should throw NullPointerException.")
    } catch {
      case _: NullPointerException => // expected
    }

    pis = new PipedInputStream()
    pos = new PipedOutputStream(pis)
    pos.close()
    pos.write(new Array[Byte](0), 0, 0)

    try {
      pis = new PipedInputStream()
      pos = new PipedOutputStream(pis)
      pos.write(new Array[Byte](0), -1, 0)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: IndexOutOfBoundsException => // expected
    }
    try {
      pis = new PipedInputStream()
      pos = new PipedOutputStream(pis)
      pos.write(null, -10, 0)
      fail("should throw NullPointerException.")
    } catch {
      case _: NullPointerException => // expected
    }
  }

  /** @tests
   *    java.io.PipedOutputStream#write(int)
   */
  @throws[IOException]
  @Test def test_write_I(): Unit = {
    out = new PipedOutputStream()
    reader = new PReader(out)
    rt = new Thread(reader)
    rt.start()
    out.write('c')
    out.flush()
    assertEquals("Wrote incorrect byte", "c", reader.read(1))
  }

  /** Tears down the fixture, for example, close a network connection. This
   *  method is called after a test is executed.
   */
  @After def tearDown(): Unit = {
    if (rt != null) {
      rt.interrupt()
    }
  }
}
