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

import org.junit._
import org.junit.Assert._

import java.io.{IOException, PipedReader, PipedWriter}

object PipedReaderTest {
  @BeforeClass def checkRuntime(): Unit =
    scala.scalanative.junit.utils.AssumesHelper.assumeMultithreadingIsEnabled()
}
class PipedReaderTest {

  private class PWriter(reader: PipedReader) extends Runnable {
    var pw: PipedWriter =
      if (reader != null) new PipedWriter(reader)
      else new PipedWriter()

    def this() = {
      this(null)
    }

    def run(): Unit = {
      try {
        val c = "Hello World".toCharArray
        pw.write(c)
        Thread.sleep(10000)
      } catch {
        case _: InterruptedException => ()
        case e: Exception            =>
          println("Exception occurred: " + e.toString)
      }
    }
  }

  private var preader: PipedReader = _
  private var pwriter: PWriter = _
  private var t: Thread = _

  /** @tests
   *    java.io.PipedReader#PipedReader()
   */
  @Test def test_Constructor(): Unit = {
    // Used in test
  }

  /** @tests
   *    java.io.PipedReader#PipedReader(java.io.PipedWriter)
   */
  @throws[IOException]
  @Test def test_Constructor_PipedWriter(): Unit = {
    preader = new PipedReader(new PipedWriter())
  }

  /** @tests
   *    java.io.PipedReader#close()
   */
  @throws[Exception]
  @Test def test_close(): Unit = {
    var c: Array[Char] = null
    preader = new PipedReader()
    t = new Thread(new PWriter(preader), "")
    t.start()
    Thread.sleep(500) // Allow writer to start
    c = new Array[Char](11)
    preader.read(c, 0, 11)
    preader.close()
    assertEquals("Read incorrect chars", "Hello World", new String(c))
  }

  /** @tests
   *    java.io.PipedReader#connect(java.io.PipedWriter)
   */
  @throws[Exception]
  @Test def test_connect_PipedWriter(): Unit = {
    var c: Array[Char] = null

    preader = new PipedReader()
    pwriter = new PWriter()
    t = new Thread(pwriter, "")
    preader.connect(pwriter.pw)
    t.start()
    Thread.sleep(500) // Allow writer to start
    c = new Array[Char](11)
    preader.read(c, 0, 11)

    assertEquals("Read incorrect chars", "Hello World", new String(c))
    try {
      preader.connect(pwriter.pw)
      fail("Failed to throw exception connecting to pre-connected reader")
    } catch {
      case _: IOException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedReader#read()
   */
  @throws[Exception]
  @Test def test_read(): Unit = {
    var c: Array[Char] = null
    preader = new PipedReader()
    t = new Thread(new PWriter(preader), "")
    t.start()
    Thread.sleep(500) // Allow writer to start
    c = new Array[Char](11)
    for (i <- 0 until c.length) {
      c(i) = preader.read().asInstanceOf[Char]
    }
    assertEquals("Read incorrect chars", "Hello World", new String(c))
  }

  /** @tests
   *    java.io.PipedReader#read(char[], int, int)
   */
  @throws[Exception]
  @Test def test_read_CII(): Unit = {
    var c: Array[Char] = null
    preader = new PipedReader()
    t = new Thread(new PWriter(preader), "")
    t.start()
    Thread.sleep(500) // Allow writer to start
    c = new Array[Char](11)
    var n = 0
    var x = n
    while (x < 11) {
      n = preader.read(c, x, 11 - x)
      x = x + n
    }
    assertEquals("Read incorrect chars", "Hello World", new String(c))
    try {
      preader.close()
      preader.read(c, 8, 7)
      fail("Failed to throw exception reading from closed reader")
    } catch {
      case _: IOException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedReader#read(char[], int, int)
   */
  @throws[IOException]
  @Test def test_read_$CII_2(): Unit = {
    // Regression for HARMONY-387
    val pw = new PipedWriter()
    var obj: PipedReader = null
    try {
      obj = new PipedReader(pw)
      obj.read(new Array[Char](0), 0, -1)
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
   *    java.io.PipedReader#read(char[], int, int)
   */
  @throws[IOException]
  @Test def test_read_$CII_3(): Unit = {
    val pw = new PipedWriter()
    var obj: PipedReader = null
    try {
      obj = new PipedReader(pw)
      obj.read(new Array[Char](0), -1, 0)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case _: IndexOutOfBoundsException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedReader#read(char[], int, int)
   */
  @throws[IOException]
  @Test def test_read_$CII_4(): Unit = {
    val pw = new PipedWriter()
    var obj: PipedReader = null
    try {
      obj = new PipedReader(pw)
      obj.read(new Array[Char](0), -1, -1)
      fail("IndexOutOfBoundsException expected")
    } catch {
      case _: ArrayIndexOutOfBoundsException =>
        fail("IndexOutOfBoundsException expected")
      case _: IndexOutOfBoundsException => // Expected
    }
  }

  /** @tests
   *    java.io.PipedReader#read(char[], int, int)
   */
  @throws[IOException]
  @Test def test_read_$CII_IOException(): Unit = {
    var pw: PipedWriter = new PipedWriter()
    var pr: PipedReader = new PipedReader(pw)
    var buf: Array[Char] = null
    pr.close()
    try {
      pr.read(buf, 0, 10)
      fail("Should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pw = null
      pr = null
    }

    pr = new PipedReader()
    buf = null
    pr.close()
    try {
      pr.read(buf, 0, 10)
      fail("Should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    buf = new Array[Char](10)
    pr.close()
    try {
      pr.read(buf, -1, 0)
      fail("Should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    buf = new Array[Char](10)
    pr.close()
    try {
      pr.read(buf, 0, -1)
      fail("Should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    buf = new Array[Char](10)
    pr.close()
    try {
      pr.read(buf, 1, 10)
      fail("Should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    pr.close()
    try {
      pr.read(new Array[Char](0), -1, -1)
      fail("should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    pr.close()
    try {
      pr.read(null, 0, 1)
      fail("should throw IOException") // $NON-NLS-1$
    } catch {
      case _: IOException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    try {
      pr.read(Array(), -1, 1)
      fail("should throw IndexOutOfBoundsException") // $NON-NLS-1$
    } catch {
      case _: IndexOutOfBoundsException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    try {
      pr.read(null, 0, -1)
      fail("should throw NullPointerException") // $NON-NLS-1$
    } catch {
      case _: NullPointerException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    try {
      pr.read(new Array[Char](10), 11, 0)
      fail("should throw IndexOutOfBoundsException") // $NON-NLS-1$
    } catch {
      case _: IndexOutOfBoundsException => // expected
    } finally {
      pw = null
      pr = null
    }

    pw = new PipedWriter()
    pr = new PipedReader(pw)
    try {
      pr.read(null, 1, 0)
      fail("should throw NullPointerException") // $NON-NLS-1$
    } catch {
      case _: NullPointerException => // expected
    } finally {
      pw = null
      pr = null
    }
  }

  /** @tests
   *    java.io.PipedReader#ready()
   */
  @throws[Exception]
  @Test def test_ready(): Unit = {
    var c: Array[Char] = null
    preader = new PipedReader()
    t = new Thread(new PWriter(preader), "")
    t.start()
    Thread.sleep(500) // Allow writer to start
    assertTrue("Reader should be ready", preader.ready())
    c = new Array[Char](11)
    for (i <- 0 until c.length)
      c(i) = preader.read().asInstanceOf[Char]
    assertFalse(
      "Reader should not be ready after reading all chars",
      preader.ready()
    )
  }

  /** Tears down the fixture, for example, close a network connection. This
   *  method is called after a test is executed.
   */
  @After def tearDown(): Unit = {
    if (t != null) {
      t.interrupt()
    }
  }
}
