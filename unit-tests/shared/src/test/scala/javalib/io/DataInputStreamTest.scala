// Ported from Scala.js commit: cbf86bb dated: 2020-10-23
//
// Porting Note:
//   New test staticReadUTF() added for ScalaNative.
//   DataInputStream.readUTF() does not have that static method.

package javalib.io

import java.io._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper._

import org.junit._
import org.junit.Assert._
import org.junit.Assume._

trait DataInputStreamTest {

  protected def inFromBytes(bytes: Seq[Byte]): InputStream

  private def newStream(data: Int*) =
    new DataInputStream(inFromBytes(data.map(_.toByte)))

  @Test def readBoolean(): Unit = {
    val data = Seq(0x00, 0x01, 0xf1, 0x00, 0x01)
    val stream = newStream(data: _*)

    for (d <- data)
      assertEquals(d != 0, stream.readBoolean())

    assertThrows(classOf[EOFException], stream.readBoolean())
  }

  @Test def readByte(): Unit = {
    val data = Seq(0x00, 0x01, 0xf1, 0x7d, 0x35)
    val stream = newStream(data: _*)

    for (d <- data)
      assertEquals(d.toByte, stream.readByte())

    assertThrows(classOf[EOFException], stream.readBoolean())
  }

  @Test def readChar(): Unit = {
    val stream = newStream(
      0x00, 0x48, // H
      0x00, 0xf6, // ö
      0x00, 0x6c, // l
      0x00, 0x6c, // l
      0x00, 0xf6, // ö
      0x00, 0x20, // [space]
      0x00, 0x57, // W
      0x01, 0x03, // ă
      0x00, 0x72, // r
      0x02, 0x34, // ȴ
      0x01, 0x11, // đ
      0x56 // dangling
    )
    var res = ""

    for (i <- 1 to 11)
      res += stream.readChar()

    assertEquals("Höllö Wărȴđ", res)

    assertThrows(classOf[EOFException], stream.readChar()) // Dangling + EOF
  }

  @Test def readDouble(): Unit = {
    val stream = newStream(
      0x3f, 0xe6, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x41, 0x15, 0x19, 0x20,
      0x45, 0x8d, 0x9b, 0x5f, 0xc0, 0xab, 0x20, 0x22, 0x75, 0x25, 0x46, 0x0b,
      0x7f, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f, 0xf0, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0xc0, 0x1c, 0x0d, 0xca, 0x65, 0xea, 0x3f, 0xa4,
      0xff, 0xf0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 // dangling
    )

    assertEquals(0.7, stream.readDouble(), 0.0)
    assertEquals(345672.067923, stream.readDouble(), 0.0)
    assertEquals(-3472.0673, stream.readDouble(), 0.0)
    assertTrue(stream.readDouble().isNaN)
    assertEquals(Double.PositiveInfinity, stream.readDouble(), 0.0)
    assertEquals(-7.0134674, stream.readDouble(), 0.0)
    assertEquals(Double.NegativeInfinity, stream.readDouble(), 0.0)
    assertEquals(0.0, stream.readDouble(), 0.0)
    assertThrows(classOf[EOFException], stream.readDouble())
  }

  @Test def readFloat(): Unit = {
    val stream = newStream(
      0xbf, 0x80, 0x00, 0x00, 0x45, 0x8e, 0x9c, 0x83, 0x80, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x7f, 0xc0, 0x00, 0x00, 0x7f, 0x80, 0x00, 0x00,
      0xbb, 0x03, 0x12, 0x6f, 0xff, 0x80, 0x00, 0x00, 0xff // dangling
    )

    assertEquals(-1.0f, stream.readFloat(), 1e-6f)
    assertEquals(4563.564f, stream.readFloat(), 1e-6f)
    assertEquals(-0.0f, stream.readFloat(), 1e-6f)
    assertEquals(0.0f, stream.readFloat(), 1e-6f)
    assertTrue(stream.readFloat().isNaN)
    assertEquals(Float.PositiveInfinity, stream.readFloat(), 0.0f)
    assertEquals(-0.002f, stream.readFloat(), 1e-6f)
    assertEquals(Float.NegativeInfinity, stream.readFloat(), 0.0f)
    assertThrows(classOf[EOFException], stream.readFloat())
  }

  @Test def readInt(): Unit = {
    val stream = newStream(0x00, 0x00, 0x00, 0x00, 0x7f, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xfc, 0x00, 0x00, 0x00, 0x53, 0x00, 0x00, 0x89, 0xa2, 0xff,
      0xfe, 0x82, 0xfd, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)

    assertEquals(0, stream.readInt())
    assertEquals(Int.MaxValue, stream.readInt())
    assertEquals(-4, stream.readInt())
    assertEquals(83, stream.readInt())
    assertEquals(35234, stream.readInt())
    assertEquals(-97539, stream.readInt())
    assertEquals(Int.MinValue, stream.readInt())
    assertEquals(1, stream.readInt())
    assertThrows(classOf[EOFException], stream.readInt())
  }

  @Test def readLong(): Unit = {
    val stream = newStream(0x00, 0x01, 0xf0, 0xec, 0x59, 0x0c, 0x70, 0x9a, 0xff,
      0xff, 0xff, 0xff, 0xfe, 0x10, 0xd5, 0x5e, 0x7f, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0x80, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0x79, 0x24, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2f)

    assertEquals(546372873646234L, stream.readLong())
    assertEquals(-32451234L, stream.readLong())
    assertEquals(Long.MaxValue, stream.readLong())
    assertEquals(0L, stream.readLong())
    assertEquals(-1L, stream.readLong())
    assertEquals(Long.MinValue, stream.readLong())
    assertEquals(-34524L, stream.readLong())
    assertEquals(47L, stream.readLong())
    assertThrows(classOf[EOFException], stream.readLong())
  }

  @Test def readShort(): Unit = {
    val stream = newStream(
      0x01, 0xc5, 0xff, 0xd5, 0x7f, 0xff, 0x18, 0xb0, 0x00, 0x00, 0x80, 0x00,
      0xfe, 0xa6, 0x00, 0x22, 0x01 // dangling
    )

    assertEquals(453, stream.readShort())
    assertEquals(-43, stream.readShort())
    assertEquals(Short.MaxValue, stream.readShort())
    assertEquals(6320, stream.readShort())
    assertEquals(0, stream.readShort())
    assertEquals(Short.MinValue, stream.readShort())
    assertEquals(-346, stream.readShort())
    assertEquals(34, stream.readShort())
    assertThrows(classOf[EOFException], stream.readDouble())
  }

  @Test def readUnsignedByte(): Unit = {
    val data = Seq(0x00, 0x01, 0xf1, 0x7d, 0x35)
    val stream = newStream(data: _*)

    for (d <- data)
      assertEquals(d, stream.readUnsignedByte())

    assertThrows(classOf[EOFException], stream.readBoolean()) // EOF
  }

  @Test def readUnsignedShort(): Unit = {
    val stream = newStream(
      0xfe, 0x4c, 0x00, 0x00, 0x18, 0xee, 0x0d, 0xed, 0x00, 0x2b, 0x01, 0xce,
      0x01, 0x56, 0x64, 0x2b, 0x01 // dangling
    )

    assertEquals(65100, stream.readUnsignedShort())
    assertEquals(0, stream.readUnsignedShort())
    assertEquals(6382, stream.readUnsignedShort())
    assertEquals(3565, stream.readUnsignedShort())
    assertEquals(43, stream.readUnsignedShort())
    assertEquals(462, stream.readUnsignedShort())
    assertEquals(342, stream.readUnsignedShort())
    assertEquals(25643, stream.readUnsignedShort())
    assertThrows(classOf[EOFException], stream.readDouble())
  }

  @Test def readFullyOneArgThreeArg(): Unit = {
    val stream = newStream(-100 to 99: _*)
    val buf = new Array[Byte](50)

    stream.readFully(buf)
    assertArrayEquals(toByteArray(-100 to -51), buf)

    assertThrows(classOf[Exception], stream.readFully(null))

    stream.readFully(buf, 40, 10)
    assertArrayEquals(toByteArray((-100 to -61) ++ (-50 to -41)), buf)

    assertThrows(classOf[Exception], stream.readFully(buf, 70, 1))
    assertThrows(classOf[Exception], stream.readFully(buf, 10, 100))
    assertThrows(classOf[Exception], stream.readFully(buf, -1, 2))
    assertThrows(classOf[Exception], stream.readFully(buf, 0, -1))

    stream.readFully(buf, 0, 50)
    assertArrayEquals(toByteArray(-40 to 9), buf)

    stream.readFully(buf, 0, 50)
    assertArrayEquals(toByteArray(10 to 59), buf)

    assertThrows(classOf[Exception], stream.readFully(buf))
  }

  @Test def readFullyForBurstyStreams(): Unit = {
    class BurstyStream(length: Int, burst: Int) extends InputStream {
      private var i: Int = 0
      def read(): Int =
        if (i < length) { i += 1; i }
        else -1
      override def read(buf: Array[Byte], off: Int, reqLen: Int): Int = {
        val len = Math.min(Math.min(reqLen, burst), length - i)
        if (reqLen == 0) 0
        else if (len == 0) -1
        else {
          var j: Int = 0
          while (j < len) {
            buf(off + j) = read().toByte
            j += 1
          }
          len
        }
      }
    }

    val stream = new DataInputStream(new BurstyStream(100, 5))
    val buf = new Array[Byte](50)

    stream.readFully(buf)
    assertArrayEquals(toByteArray(1 to 50), buf)

    stream.readFully(buf, 40, 10)
    assertArrayEquals(toByteArray((1 to 40) ++ (51 to 60)), buf)

    assertThrows(classOf[EOFException], stream.readFully(buf))
  }

  @Test def readUTF(): Unit = {
    val stream = newStream(0x00, 0x10, 0x48, 0xc3, 0xb6, 0x6c, 0x6c, 0xc3, 0xb6,
      0x20, 0x57, 0xc4, 0x83, 0x72, 0xc8, 0xb4, 0xc4, 0x91, 0x00, 0x0d, 0x70,
      0x6f, 0x6f, 0x20, 0x2d, 0x3e, 0x20, 0xed, 0xa0, 0xbd, 0xed, 0xb2, 0xa9,
      0x00, 0x03, 0xe6, 0x84, 0x9b)

    assertEquals("Höllö Wărȴđ", stream.readUTF)
    assertEquals("poo -> 💩", stream.readUTF)
    assertEquals("愛", stream.readUTF)

    val badStream = newStream(0x00, 0x01, 0xc0, 0x82)
    assertThrows(classOf[UTFDataFormatException], badStream.readUTF)
  }

  @Test def staticReadUTF(): Unit = {
    val stream = newStream(0x00, 0x10, 0x48, 0xc3, 0xb6, 0x6c, 0x6c, 0xc3, 0xb6,
      0x20, 0x57, 0xc4, 0x83, 0x72, 0xc8, 0xb4, 0xc4, 0x91, 0x00, 0x0d, 0x70,
      0x6f, 0x6f, 0x20, 0x2d, 0x3e, 0x20, 0xed, 0xa0, 0xbd, 0xed, 0xb2, 0xa9,
      0x00, 0x03, 0xe6, 0x84, 0x9b)

    assertEquals("Höllö Wărȴđ", DataInputStream.readUTF(stream))
    assertEquals("poo -> 💩", stream.readUTF)
    assertEquals("愛", stream.readUTF)

    val badStream = newStream(0x00, 0x01, 0xc0, 0x82)
    assertThrows(classOf[UTFDataFormatException], badStream.readUTF)
  }

  @Test def readUTFWithVeryLongString(): Unit = {
    val length = 40000
    val inputBytes = new Array[Byte](2 + length)
    inputBytes(0) = (length >> 8).toByte
    inputBytes(1) = length.toByte
    for (i <- 2 until (2 + length))
      inputBytes(i) = 'a'.toByte

    val stream = new DataInputStream(new ByteArrayInputStream(inputBytes))
    val result = stream.readUTF()
    assertEquals(length, result.length)
    assertTrue(result.forall(_ == 'a'))
    assertEquals(-1, stream.read())
  }

  @deprecated @Test def readLine(): Unit = {
    val stream = newStream(
      "Hello World\nUNIX\nWindows\r\nMac (old)\rStuff".map(_.toInt): _*
    )

    assertEquals("Hello World", stream.readLine())
    assertEquals("UNIX", stream.readLine())
    assertEquals("Windows", stream.readLine())
    assertEquals("Mac (old)", stream.readLine())
    assertEquals("Stuff", stream.readLine())
    assertEquals(null, stream.readLine())
  }

  @deprecated @Test def markReadLinePushBack(): Unit = {
    assumeNotJVMCompliant()

    val stream = newStream(
      "Hello World\nUNIX\nWindows\r\nMac (old)\rStuff".map(_.toInt): _*
    )

    assertEquals("Hello World", stream.readLine())
    stream.mark(1000)
    assertEquals("UNIX", stream.readLine())
    stream.reset()
    assertEquals("UNIX", stream.readLine())
    assertEquals("Windows", stream.readLine())
    assertEquals("Mac (old)", stream.readLine())
    stream.mark(1000)
    assertEquals("Stuff", stream.readLine())
    stream.reset()
    assertEquals("Stuff", stream.readLine())
    assertNull(stream.readLine())
  }

  private def toByteArray(range: Iterable[Int]): Array[Byte] =
    range.toArray.map(_.asInstanceOf[Byte])
}

class DataInputStreamGenericTest extends DataInputStreamTest {
  protected def inFromBytes(bytes: Seq[Byte]): InputStream =
    new ByteArrayInputStream(bytes.toArray)
}
