// Ported from Apache Harmony
package org.scalanative.testsuite.javalib.io

import java.io._
import org.junit.Test
import org.junit.Assert._

// class PipedOutputStreamTest {
//   private class PReader(val out: PipedOutputStream) extends Runnable {
//     private[io] var reader = null
//     try reader = new PipedInputStream(out)
//     catch {
//       case e: Exception =>
//         System.out.println("Couldn't start reader")
//     }
//      def getReader: PipedInputStream = reader
//      def available: Int = try reader.available
//      catch {
//        case e: Exception =>
//          -1
//      }
//      override def run(): Unit = {
//        try
//          while ({ true }) {
//            Thread.sleep(1000)
//            Thread.`yield`
//          }
//        catch {
//          case e: InterruptedException =>

//        }
//      }
//      def read(nbytes: Int): String = {
//        val buf = new Array[Byte](nbytes)
//        try {
//          reader.read(buf, 0, nbytes)
//          new String(buf, "UTF-8")
//        } catch {
//          case e: IOException =>
//            System.out.println("Exception reading info")
//            "ERROR"
//        }
//      }
//    }

//   private[io] var rt = null
//   private[io] var reader = null
//   private[io] var out = null

//   /** @tests
//    *    java.io.PipedOutputStream#PipedOutputStream()
//    */
//   def test_Constructor(): Unit = {
//     // Used in tests
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#PipedOutputStream(java.io.PipedInputStream)
//    */
//   @throws[Exception]
//   def test_ConstructorLjava_io_PipedInputStream(): Unit = {
//     out = new PipedOutputStream(new PipedInputStream)
//     out.write('b')
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#close()
//    */
//   @throws[Exception]
//   def test_close(): Unit = {
//     out = new PipedOutputStream
//     rt = new Thread(reader = new PipedOutputStreamTest.PReader(out))
//     rt.start()
//     out.close()
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#connect(java.io.PipedInputStream)
//    */
//   @throws[IOException]
//   def test_connectLjava_io_PipedInputStream_Exception(): Unit = {
//     out = new PipedOutputStream
//     out.connect(new PipedInputStream)
//     try {
//       out.connect(null)
//       fail("should throw NullPointerException") // $NON-NLS-1$
//     } catch {
//       case e: NullPointerException =>

//       // expected
//     }
//   }
//   def test_connectLjava_io_PipedInputStream(): Unit = {
//     try {
//       out = new PipedOutputStream
//       rt = new Thread(reader = new PipedOutputStreamTest.PReader(out))
//       rt.start()
//       out.connect(new PipedInputStream)
//       fail(
//         "Failed to throw exception attempting connect on already connected stream"
//       )
//     } catch {
//       case e: IOException =>

//       // Expected
//     }
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#flush()
//    */
//   @throws[IOException]
//   @throws[UnsupportedEncodingException]
//   def test_flush(): Unit = {
//     out = new PipedOutputStream
//     rt = new Thread(reader = new PipedOutputStreamTest.PReader(out))
//     rt.start()
//     out.write("HelloWorld".getBytes("UTF-8"), 0, 10)
//     assertTrue("Bytes written before flush", reader.available != 0)
//     out.flush()
//     assertEquals("Wrote incorrect bytes", "HelloWorld", reader.read(10))
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#write(byte[], int, int)
//    */
//   @throws[IOException]
//   @throws[UnsupportedEncodingException]
//   def test_write$BII(): Unit = {
//     out = new PipedOutputStream
//     rt = new Thread(reader = new PipedOutputStreamTest.PReader(out))
//     rt.start()
//     out.write("HelloWorld".getBytes("UTF-8"), 0, 10)
//     out.flush()
//     assertEquals("Wrote incorrect bytes", "HelloWorld", reader.read(10))
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#write(byte[], int, int) Regression for
//    *    HARMONY-387
//    */
//   @throws[IOException]
//   def test_write$BII_2(): Unit = {
//     var pis = new PipedInputStream
//     var pos = null
//     try {
//       pos = new PipedOutputStream(pis)
//       pos.write(new Array[Byte](0), -1, -1)
//       fail("IndexOutOfBoundsException expected")
//     } catch {
//       case t: IndexOutOfBoundsException =>
//         assertEquals(
//           "IndexOutOfBoundsException rather than a subclass expected",
//           classOf[IndexOutOfBoundsException],
//           t.getClass
//         )
//     }
//     // Regression for HARMONY-4311
//     try {
//       pis = new PipedInputStream
//       val out = new PipedOutputStream(pis)
//       out.write(null, -10, 10)
//       fail("should throw NullPointerException.")
//     } catch {
//       case e: NullPointerException =>

//     }
//     pis = new PipedInputStream
//     pos = new PipedOutputStream(pis)
//     pos.close()
//     pos.write(new Array[Byte](0), 0, 0)
//     try {
//       pis = new PipedInputStream
//       pos = new PipedOutputStream(pis)
//       pos.write(new Array[Byte](0), -1, 0)
//       fail("IndexOutOfBoundsException expected")
//     } catch {
//       case t: IndexOutOfBoundsException =>

//       //expected
//     }
//     try {
//       pis = new PipedInputStream
//       pos = new PipedOutputStream(pis)
//       pos.write(null, -10, 0)
//       fail("should throw NullPointerException.")
//     } catch {
//       case e: NullPointerException =>

//     }
//   }

//   /** @tests
//    *    java.io.PipedOutputStream#write(int)
//    */
//   @throws[IOException]
//   def test_writeI(): Unit = {
//     out = new PipedOutputStream
//     rt = new Thread(reader = new PipedOutputStreamTest.PReader(out))
//     rt.start()
//     out.write('c')
//     out.flush()
//     assertEquals("Wrote incorrect byte", "c", reader.read(1))
//   }

//   /** Tears down the fixture, for example, close a network connection. This
//    *  method is called after a test is executed.
//    */
//   override protected def tearDown(): Unit = {
//     if (rt != null) rt.interrupt()
//   }
// }
