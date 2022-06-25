// Ported from Apache Harmony
package org.scalanative.testsuite.javalib.io

import java.io._
import org.junit.Test
import org.junit.Assert._

// object PipedWriterTest { private[io]  class PReader extends Runnable { var pr: PipedReader = null
//   var buf = new Array[Char](10)
//   def this(pw: PipedWriter)
//   def this(pr: PipedReader) { this()
//     this.pr = pr
//   }
//   override def run(): Unit =  { try {var r = 0
//     for (i <- 0 until buf.length)  { r = pr.read
//       if (r == -1) break //todo: break is not supported
//       buf(i) = r.toChar
//     }} catch {
//     case e: Exception =>
//       System.out.println("Exception reading (" + Thread.currentThread.getName + "): " + e.toString)
//   }
//   }
// }
// }
// class PipedWriterTest extends TestCase { private[io]  var rdrThread = null
//   private[io]  var reader = null
//   private[io]  var pw = null
//   /**
//    * @tests java.io.PipedWriter#PipedWriter()
//    */def test_Constructor(): Unit =  {
//     // Test for method java.io.PipedWriter()
//     // Used in tests
//   }
//   /**
//    * @tests java.io.PipedWriter#PipedWriter(java.io.PipedReader)
//    */@throws[Exception]
//   def test_ConstructorLjava_io_PipedReader(): Unit =  { // Test for method java.io.PipedWriter(java.io.PipedReader)
//     val buf = new Array[Char](10)
//     "HelloWorld".getChars(0, 10, buf, 0)
//     val rd = new PipedReader
//     pw = new PipedWriter(rd)
//     rdrThread = new Thread(reader = new PipedWriterTest.PReader(rd), "Constructor(Reader)")
//     rdrThread.start()
//     pw.write(buf)
//     pw.close()
//     rdrThread.join(500)
//     assertEquals("Failed to construct writer", "HelloWorld", new String(reader.buf))
//   }
//   /**
//    * @tests java.io.PipedWriter#close()
//    */@throws[Exception]
//   def test_close(): Unit =  { // Test for method void java.io.PipedWriter.close()
//     val buf = new Array[Char](10)
//     "HelloWorld".getChars(0, 10, buf, 0)
//     val rd = new PipedReader
//     pw = new PipedWriter(rd)
//     reader = new PipedWriterTest.PReader(rd)
//     pw.close()
//     try {pw.write(buf)
//       fail("Should have thrown exception when attempting to write to closed writer.")} catch {
//       case e: Exception =>

//       // correct
//     }
//   }
//   /**
//    * @tests java.io.PipedWriter#connect(java.io.PipedReader)
//    */@throws[Exception]
//   def test_connectLjava_io_PipedReader(): Unit =  { // Test for method void java.io.PipedWriter.connect(java.io.PipedReader)
//     val buf = new Array[Char](10)
//     "HelloWorld".getChars(0, 10, buf, 0)
//     val rd = new PipedReader
//     pw = new PipedWriter
//     pw.connect(rd)
//     rdrThread = new Thread(reader = new PipedWriterTest.PReader(rd), "connect")
//     rdrThread.start()
//     pw.write(buf)
//     pw.close()
//     rdrThread.join(500)
//     assertEquals("Failed to write correct chars", "HelloWorld", new String(reader.buf))
//   }
//   /**
//    * @tests java.io.PipedWriter#flush()
//    */@throws[Exception]
//   def test_flush(): Unit =  { // Test for method void java.io.PipedWriter.flush()
//     val buf = new Array[Char](10)
//     "HelloWorld".getChars(0, 10, buf, 0)
//     pw = new PipedWriter
//     rdrThread = new Thread(reader = new PipedWriterTest.PReader(pw), "flush")
//     rdrThread.start()
//     pw.write(buf)
//     pw.flush()
//     rdrThread.join(700)
//     assertEquals("Failed to flush chars", "HelloWorld", new String(reader.buf))
//   }
//   /**
//    * @tests java.io.PipedWriter#write(char[], int, int)
//    */@throws[Exception]
//   def test_write$CII(): Unit =  { // Test for method void java.io.PipedWriter.write(char [], int, int)
//     val buf = new Array[Char](10)
//     "HelloWorld".getChars(0, 10, buf, 0)
//     pw = new PipedWriter
//     rdrThread = new Thread(reader = new PipedWriterTest.PReader(pw), "writeCII")
//     rdrThread.start()
//     pw.write(buf, 0, 10)
//     pw.close()
//     rdrThread.join(1000)
//     assertEquals("Failed to write correct chars", "HelloWorld", new String(reader.buf))
//   }
//   /**
//    * @tests java.io.PipedWriter#write(char[], int, int) Regression for
//    *        HARMONY-387
//    */@throws[IOException]
//   def test_write$CII_2(): Unit =  { val pr = new PipedReader
//     var obj = null
//     try {obj = new PipedWriter(pr)
//       obj.write(new Array[Char](0), 0.toInt, -1.toInt)
//       fail("IndexOutOfBoundsException expected")} catch {
//       case t: IndexOutOfBoundsException =>
//         assertEquals("IndexOutOfBoundsException rather than a subclass expected", classOf[IndexOutOfBoundsException], t.getClass)
//     }
//   }
//   @throws[IOException]
//   def test_write$CII_3(): Unit =  { val pr = new PipedReader
//     var obj = null
//     try {obj = new PipedWriter(pr)
//       obj.write(new Array[Char](0), -1.toInt, 0.toInt)
//       fail("IndexOutOfBoundsException expected")} catch {
//       case t: ArrayIndexOutOfBoundsException =>
//         fail("IndexOutOfBoundsException expected")
//       case t: IndexOutOfBoundsException =>

//     }
//   }
//   @throws[IOException]
//   def test_write$CII_4(): Unit =  { val pr = new PipedReader
//     var obj = null
//     try {obj = new PipedWriter(pr)
//       obj.write(new Array[Char](0), -1.toInt, -1.toInt)
//       fail("IndexOutOfBoundsException expected")} catch {
//       case t: ArrayIndexOutOfBoundsException =>
//         fail("IndexOutOfBoundsException expected")
//       case t: IndexOutOfBoundsException =>

//     }
//   }
//   @throws[IOException]
//   def test_write$CII_5(): Unit =  { val pr = new PipedReader
//     var obj = null
//     try {obj = new PipedWriter(pr)
//       obj.write(null.asInstanceOf[Array[Char]], -1.toInt, 0.toInt)
//       fail("NullPointerException expected")} catch {
//       case t: IndexOutOfBoundsException =>
//         fail("NullPointerException expected")
//       case t: NullPointerException =>

//     }
//   }
//   @throws[IOException]
//   def test_write$CII_6(): Unit =  { val pr = new PipedReader
//     var obj = null
//     try {obj = new PipedWriter(pr)
//       obj.write(null.asInstanceOf[Array[Char]], -1.toInt, -1.toInt)
//       fail("NullPointerException expected")} catch {
//       case t: IndexOutOfBoundsException =>
//         fail("NullPointerException expected")
//       case t: NullPointerException =>

//     }
//   }
//   @throws[IOException]
//   def test_write$CII_notConnected(): Unit =  { // Regression test for Harmony-2404
//     // create not connected pipe
//     val obj = new PipedWriter
//     // char array is null
//     try {obj.write(null.asInstanceOf[Array[Char]], 0, 1)
//       fail("IOException expected")} catch {
//       case ioe: IOException =>

//       // expected
//     }
//     // negative offset
//     try {obj.write(Array[Char](1), -10, 1)
//       fail("IOException expected")} catch {
//       case ioe: IOException =>

//     }
//     // wrong offset
//     try {obj.write(Array[Char](1), 10, 1)
//       fail("IOException expected")} catch {
//       case ioe: IOException =>

//     }
//     // negative length
//     try {obj.write(Array[Char](1), 0, -10)
//       fail("IOException expected")} catch {
//       case ioe: IOException =>

//     }
//     // all valid params
//     try {obj.write(Array[Char](1, 1), 0, 1)
//       fail("IOException expected")} catch {
//       case ioe: IOException =>

//     }
//   }
//   /**
//    * @tests java.io.PipedWriter#write(int)
//    */@throws[IOException]
//   def test_write_I_MultiThread(): Unit =  { val pr = new PipedReader
//     val pw = new PipedWriter
//     // test if writer recognizes dead reader
//     pr.connect(pw)
//     class WriteRunnable extends Runnable { private[io]  var pass = false
//       private[io]  val readerAlive = true
//       override def run(): Unit =  { try {pw.write(1)
//         while ( { readerAlive})  {
//           // wait the reader thread dead
//         }
//         try // should throw exception since reader thread
//           // is now dead
//           pw.write(1)
//         catch {
//           case e: IOException =>
//             pass = true
//         }} catch {
//         case e: IOException =>

//         //ignore
//       }
//       }
//     }
//     val writeRunnable = new WriteRunnable
//     val writeThread = new Thread(writeRunnable)
//     class ReadRunnable extends Runnable { private[io]  var pass = false
//       override def run(): Unit =  { try {pr.read
//         pass = true} catch {
//         case e: IOException =>

//       }
//       }
//     }
//     val readRunnable = new ReadRunnable
//     val readThread = new Thread(readRunnable)
//     writeThread.start()
//     readThread.start()
//     while ( { readThread.isAlive})  {
//       //wait the reader thread dead
//     }
//     writeRunnable.readerAlive = false
//     assertTrue("reader thread failed to read", readRunnable.pass)
//     while ( { writeThread.isAlive})  {
//       //wait the writer thread dead
//     }
//     assertTrue("writer thread failed to recognize dead reader", writeRunnable.pass)
//   }
//   /**
//    * @tests java.io.PipedWriter#write(char[],int,int)
//    */@throws[Exception]
//   def test_write_$CII_MultiThread(): Unit =  { val pr = new PipedReader
//     val pw = new PipedWriter
//     pr.connect(pw)
//     class WriteRunnable extends Runnable { private[io]  var pass = false
//       private[io]  val readerAlive = true
//       override def run(): Unit =  { try {pw.write(1)
//         while ( { readerAlive})  {
//         }
//         try {val buf = new Array[Char](10)
//           pw.write(buf, 0, 10)} catch {
//           case e: IOException =>
//             pass = true
//         }} catch {
//         case e: IOException =>

//       }
//       }
//     }
//     val writeRunnable = new WriteRunnable
//     val writeThread = new Thread(writeRunnable)
//     class ReadRunnable extends Runnable { private[io]  var pass = false
//       override def run(): Unit =  { try {pr.read
//         pass = true} catch {
//         case e: IOException =>

//       }
//       }
//     }
//     val readRunnable = new ReadRunnable
//     val readThread = new Thread(readRunnable)
//     writeThread.start()
//     readThread.start()
//     while ( { readThread.isAlive})  {
//     }
//     writeRunnable.readerAlive = false
//     assertTrue("reader thread failed to read", readRunnable.pass)
//     while ( { writeThread.isAlive})  {
//     }
//     assertTrue("writer thread failed to recognize dead reader", writeRunnable.pass)
//   }
//   @throws[Exception]
//   def test_writeI(): Unit =  { // Test for method void java.io.PipedWriter.write(int)
//     pw = new PipedWriter
//     rdrThread = new Thread(reader = new PipedWriterTest.PReader(pw), "writeI")
//     rdrThread.start()
//     pw.write(1)
//     pw.write(2)
//     pw.write(3)
//     pw.close()
//     rdrThread.join(1000)
//     assertTrue("Failed to write correct chars: " + reader.buf(0).toInt + " " + reader.buf(1).toInt + " " + reader.buf(2).toInt, reader.buf(0) == 1 && reader.buf(1) == 2 && reader.buf(2) == 3)
//   }
//   /**
//    * Tears down the fixture, for example, close a network connection. This
//    * method is called after a test is executed.
//    */@throws[Exception]
//   override protected def tearDown(): Unit =  { try if (rdrThread != null) rdrThread.interrupt()
//   catch {
//     case ignore: Exception =>

//   }
//     try if (pw != null) pw.close()
//     catch {
//       case ignore: Exception =>

//     }
//     super.tearDown()
//   }
// }
