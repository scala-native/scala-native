// Ported from Apache Harmony
package org.scalanative.testsuite.javalib.io

import java.io._
import org.junit.Test
import org.junit.Assert._

// object PipedReaderTest { private[io]  class PWriter  ( ) extends Runnable { pw = new PipedWriter
//   var pw: PipedWriter = null
//   def this(reader: PipedReader)
//   override def run(): Unit =  { try {val c = new Array[Char](11)
//     "Hello World".getChars(0, 11, c, 0)
//     pw.write(c)
//     Thread.sleep(10000)} catch {
//     case e: InterruptedException =>

//     case e: Exception =>
//       System.out.println("Exception occurred: " + e.toString)
//   }
//   }
// }
// }
// class PipedReaderTest extends TestCase { private[io]  var preader = null
//   private[io]  var pwriter = null
//   private[io]  var t = null
//   /**
//    * @tests java.io.PipedReader#PipedReader()
//    */def test_Constructor(): Unit =  {
//     // Used in test
//   }
//   /**
//    * @tests java.io.PipedReader#PipedReader(java.io.PipedWriter)
//    */@throws[IOException]
//   def test_ConstructorLjava_io_PipedWriter(): Unit =  { preader = new PipedReader(new PipedWriter)
//   }
//   /**
//    * @tests java.io.PipedReader#close()
//    */@throws[Exception]
//   def test_close(): Unit =  { var c = null
//     preader = new PipedReader
//     t = new Thread(new PipedReaderTest.PWriter(preader), "")
//     t.start()
//     Thread.sleep(500)// Allow writer to start

//     c = new Array[Char](11)
//     preader.read(c, 0, 11)
//     preader.close()
//     assertEquals("Read incorrect chars", "Hello World", new String(c))
//   }
//   /**
//    * @tests java.io.PipedReader#connect(java.io.PipedWriter)
//    */@throws[Exception]
//   def test_connectLjava_io_PipedWriter(): Unit =  { var c = null
//     preader = new PipedReader
//     t = new Thread(pwriter = new PipedReaderTest.PWriter, "")
//     preader.connect(pwriter.pw)
//     t.start()
//     Thread.sleep(500)
//     c = new Array[Char](11)
//     preader.read(c, 0, 11)
//     assertEquals("Read incorrect chars", "Hello World", new String(c))
//     try {preader.connect(pwriter.pw)
//       fail("Failed to throw exception connecting to pre-connected reader")} catch {
//       case e: IOException =>

//       // Expected
//     }
//   }
//   /**
//    * @tests java.io.PipedReader#read()
//    */@throws[Exception]
//   def test_read(): Unit =  { var c = null
//     preader = new PipedReader
//     t = new Thread(new PipedReaderTest.PWriter(preader), "")
//     t.start()
//     Thread.sleep(500)
//     c = new Array[Char](11)
//     for (i <- 0 until c.length)  { c(i) = preader.read.toChar
//     }
//     assertEquals("Read incorrect chars", "Hello World", new String(c))
//   }
//   /**
//    * @tests java.io.PipedReader#read(char[], int, int)
//    */@throws[Exception]
//   def test_read$CII(): Unit =  { var c = null
//     preader = new PipedReader
//     t = new Thread(new PipedReaderTest.PWriter(preader), "")
//     t.start()
//     Thread.sleep(500)
//     c = new Array[Char](11)
//     var n = 0
//     var x = n
//     while ( { x < 11})  { n = preader.read(c, x, 11 - x)
//       x = x + n
//     }
//     assertEquals("Read incorrect chars", "Hello World", new String(c))
//     try {preader.close()
//       preader.read(c, 8, 7)
//       fail("Failed to throw exception reading from closed reader")} catch {
//       case e: IOException =>

//     }
//   }
//   @throws[IOException]
//   def test_read$CII_2(): Unit =  { // Regression for HARMONY-387
//     val pw = new PipedWriter
//     var obj = null
//     try {obj = new PipedReader(pw)
//       obj.read(new Array[Char](0), 0.toInt, -1.toInt)
//       fail("IndexOutOfBoundsException expected")} catch {
//       case t: IndexOutOfBoundsException =>
//         assertEquals("IndexOutOfBoundsException rather than a subclass expected", classOf[IndexOutOfBoundsException], t.getClass)
//     }
//   }
//   @throws[IOException]
//   def test_read$CII_3(): Unit =  { val pw = new PipedWriter
//     var obj = null
//     try {obj = new PipedReader(pw)
//       obj.read(new Array[Char](0), -1.toInt, 0.toInt)
//       fail("IndexOutOfBoundsException expected")} catch {
//       case t: ArrayIndexOutOfBoundsException =>
//         fail("IndexOutOfBoundsException expected")
//       case t: IndexOutOfBoundsException =>

//     }
//   }
//   @throws[IOException]
//   def test_read$CII_4(): Unit =  { val pw = new PipedWriter
//     var obj = null
//     try {obj = new PipedReader(pw)
//       obj.read(new Array[Char](0), -1.toInt, -1.toInt)
//       fail("IndexOutOfBoundsException expected")} catch {
//       case t: ArrayIndexOutOfBoundsException =>
//         fail("IndexOutOfBoundsException expected")
//       case t: IndexOutOfBoundsException =>

//     }
//   }
//   @throws[IOException]
//   def test_read_$CII_IOException(): Unit =  { var pw = new PipedWriter
//     var pr = new PipedReader(pw)
//     var buf = null
//     pr.close()
//     try {pr.read(buf, 0, 10)
//       fail("Should throws IOException")//$NON-NLS-1$
//     } catch {
//       case e: IOException =>

//       // expected
//     } finally {
//       pw = null
//       pr = null
//     }
//     pr = new PipedReader
//     buf = null
//     pr.close()
//     try {pr.read(buf, 0, 10)
//       fail("Should throws IOException")} catch {
//       case e: IOException =>

//     } finally pr = null
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     buf = new Array[Char](10)
//     pr.close()
//     try {pr.read(buf, -1, 0)
//       fail("Should throws IOException")} catch {
//       case e: IOException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     buf = new Array[Char](10)
//     pr.close()
//     try {pr.read(buf, 0, -1)
//       fail("Should throws IOException")} catch {
//       case e: IOException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     buf = new Array[Char](10)
//     pr.close()
//     try {pr.read(buf, 1, 10)
//       fail("Should throws IOException")} catch {
//       case e: IOException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     pr.close()
//     try {pr.read(new Array[Char](0), -1, -1)
//       fail("should throw IOException")} catch {
//       case e: IOException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     pr.close()
//     try {pr.read(null, 0, 1)
//       fail("should throw IOException")} catch {
//       case e: IOException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     try {pr.read(null, -1, 1)
//       fail("should throw IndexOutOfBoundsException")} catch {
//       case e: IndexOutOfBoundsException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     try {pr.read(null, 0, -1)
//       fail("should throw NullPointerException")} catch {
//       case e: NullPointerException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     try {pr.read(new Array[Char](10), 11, 0)
//       fail("should throw IndexOutOfBoundsException")} catch {
//       case e: IndexOutOfBoundsException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//     pw = new PipedWriter
//     pr = new PipedReader(pw)
//     try {pr.read(null, 1, 0)
//       fail("should throw NullPointerException")} catch {
//       case e: NullPointerException =>

//     } finally {
//       pw = null
//       pr = null
//     }
//   }
//   /**
//    * @tests java.io.PipedReader#ready()
//    */@throws[Exception]
//   def test_ready(): Unit =  { var c = null
//     preader = new PipedReader
//     t = new Thread(new PipedReaderTest.PWriter(preader), "")
//     t.start()
//     Thread.sleep(500)
//     assertTrue("Reader should be ready", preader.ready)
//     c = new Array[Char](11)
//     for (i <- 0 until c.length)  { c(i) = preader.read.toChar}
//     assertFalse("Reader should not be ready after reading all chars", preader.ready)
//   }
//   /**
//    * Tears down the fixture, for example, close a network connection. This
//    * method is called after a test is executed.
//    */@throws[Exception]
//   override protected def tearDown(): Unit =  { if (t != null) t.interrupt()
//     super.tearDown()
//   }
// }
