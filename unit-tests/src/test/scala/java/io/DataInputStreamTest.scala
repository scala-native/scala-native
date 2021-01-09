package java.io

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

import scalanative.unsafe.sizeof

class DataInputStreamTest {
  // read() is inherited from the underlying InputStream. It is not a method
  // implemented in DIS. Since the internals of DIS use in.read(), test
  // the method here. Detect defects as close to the cause as feasible.

  @Test def readNoArgEndOfFile(): Unit = {
    val expected   = 98 // ASCII 'b'
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = new Array[Byte](inputArray.length)

    assertEquals(-1, dis.read())
  }

  @Test def readNoArgByte255(): Unit = {
    val expected   = 255 // ASCII nbsp, particularly troublesome
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    // 255 is ASCII nbsp, a particularly troublesome case.
    assertEquals(255, dis.read())
  }

  // read(b)

  @Test def readBufEndOfFile(): Unit = {
    val expected = 99.toByte // ASCII 'c'

    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = new Array[Byte](inputArray.length)

    assertEquals(-1, dis.read(buffer))
  }

  @Test def readBuf(): Unit = {
    val expected       = "Blue eyes crying in the rain".getBytes
    val expectedLength = expected.length

    val arrayIn = new ByteArrayInputStream(expected, 0, expectedLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = new Array[Byte](expectedLength)

    assertEquals(expectedLength, dis.read(buffer))

    for (i <- 0 until expectedLength) {
      assertEquals(expected(i), buffer(i))
    }
  }

  // read(b, off, len)

  @Test def readBufOffLenEndOfFile(): Unit = {
    val expected   = 100 // ASCII 'd'
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = 0

    val buffer = new Array[Byte](inputArray.length)

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(-1, dis.read(buffer, 0, 1))
  }

  @Test def readBufOffLen(): Unit = {
    val off = 10
    val len = 3

    val text       = "Fido is a cat, not a goat!"
    val textBytes  = text.getBytes
    val textLength = textBytes.length

    val dog       = "dog"
    val dogBytes  = dog.getBytes
    val dogLength = dogBytes.length

    val expected       = text.replace("cat", "dog")
    val expectedBytes  = expected.getBytes
    val expectedLength = expectedBytes.length

    val arrayIn = new ByteArrayInputStream(dogBytes, 0, dogLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = textBytes

    assertEquals(len, dis.read(buffer, off, len))

    for (i <- 0 until expectedLength) {
      assertEquals(expectedBytes(i), buffer(i))
    }
  }

  // readByte

  @Test def readByteEOF(): Unit = {
    val expected   = 101.toByte // ASCII 'e'
    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readByte())
  }

  @Test def readByte(): Unit = {
    val expected   = 102.toByte // ASCII 'f'
    val inputArray = Array[Byte](expected)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readByte())
  }

  // readBoolean

  @Test def readBooleanEOF(): Unit = {
    val expected   = 0.toByte
    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readBoolean())
  }

  @Test def readBooleanFalse(): Unit = {
    val inputArray = Array[Byte](0.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(false, dis.readBoolean())
  }

  @Test def readBooleanTrue(): Unit = {
    val inputArray = Array[Byte](1.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(true, dis.readBoolean())
  }

  // readUnsignedByte

  @Test def readUnsignedByteEndOfFile(): Unit = {
    val expected   = 255.toByte // ASCII nbsp
    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readUnsignedByte())
  }

  @Test def readUnsignedByteByte255(): Unit = {
    // Broken implementations of readUnsignedByte will report character
    // 255 as -1, high bytes set, not required 255, high bytes clear.
    val expected   = 255 // ASCII nbsp
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readUnsignedByte())
  }

  // readChar

  @Test def readCharEOF(): Unit = {
    // readChar expects to read 2 bytes, cause EOF by providing only 1.
    val inputArray = Array[Byte](0x97.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readChar())
  }

  @Test def readChar(): Unit = {
    // Java is reading Big Endian (high byte first) UTF-16.
    //
    // Beware: The attractive Character.getBytes apparently returns
    // modified UTF-8.
    //
    // Helpful Unicode URL: https://unicode-table.com/en/03D5/

    // Greek phi symbol U+03D5 is a Java Character which is 2
    // bytes in modified UTF-8 and has with bits set in both upper &
    // lower bytes. It is also both non-ASCII & easy to recognize on screen.
    // UTF-16BE: decimal:   981, hex: 03 D5,  dec bytes:  3 213

    val expected = '\u03d5'

    val inputArray = Array[Byte](0x03.toByte, 0xd5.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readChar())
  }

  // readDouble

  @Test def readDoubleEOF(): Unit = {
    // readDouble expects to read 8 bytes, cause EOF by providing only 3.
    val inputArray = Array[Byte](0x03.toByte, 0xd5.toByte, 0xFF.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readDouble())
  }

  @Test def readDouble(): Unit = {
    val expected = -0.0 // Negative zero is a common troublemaker.

    var bits = java.lang.Double.doubleToLongBits(expected)

    val nBytes = sizeof[Long].toInt
    val data   = new Array[Byte](nBytes)

    for (i <- (nBytes - 1) to 0 by -1) {
      data(i) = bits.toByte
      bits >>>= 8
    }

    val inputArray = Array[Byte](data: _*)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readDouble(), 0.0001)
  }

  // readFloat

  @Test def readFloatEOF(): Unit = {
    // readFloat expects to read 4 bytes, cause EOF by providing only 3.
    val inputArray = Array[Byte](0x03.toByte, 0xd5.toByte, 0xFF.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readFloat())
  }

  @Test def readFloat(): Unit = {
    val expected = Float.MinPositiveValue

    var bits = java.lang.Float.floatToIntBits(expected)

    val nBytes = sizeof[Int].toInt
    val data   = new Array[Byte](nBytes)

    for (i <- (nBytes - 1) to 0 by -1) {
      data(i) = bits.toByte
      bits >>>= 8
    }

    val inputArray = Array[Byte](data: _*)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readFloat(), 0.0001)
  }

  // readFully

  @Test def readFullyBufOffLenNullBuffer(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[NullPointerException],
                 dis.readFully(null.asInstanceOf[Array[Byte]], 0, 1))
  }

  @Test def readFullyBufOffLenInvalidOffsetArgument(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn     = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis         = new DataInputStream(arrayIn)
    val outputArray = new Array[Byte](iaLength)

    assertThrows(classOf[IndexOutOfBoundsException],
                 dis.readFully(outputArray, -1, 1))
  }

  @Test def readFullyBufOffLenInvalidLengthArgument(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn     = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis         = new DataInputStream(arrayIn)
    val outputArray = new Array[Byte](iaLength)

    assertThrows(classOf[IndexOutOfBoundsException],
                 dis.readFully(outputArray, 0, -1))
  }

  @Test def readFullyBufOffLenInvalidOffsetPlusLengthArguments(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn     = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis         = new DataInputStream(arrayIn)
    val outputArray = new Array[Byte](iaLength)

    assertThrows(classOf[IndexOutOfBoundsException],
                 dis.readFully(outputArray, 1, outputArray.length))
  }

  @Test def readFullyBufOffLenMinusLen0(): Unit = {
    val inputArray = Array.tabulate[Byte](256)(i => i.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val marker      = 255.toByte
    val outputArray = Array.fill[Byte](iaLength)(marker.toByte)

    dis.readFully(outputArray, 10, 0)

    val index = outputArray.indexWhere(e => e != marker)

    if (index >= 0) {
      val result   = outputArray(index) & 0xFF // want to print 0-255
      val expected = marker & 0xFF

      assertTrue(s"byte at index ${index}: ${result} != expected: ${expected}",
                 false)
    }
  }

  @Test def readFullyBufOffLenMinusLenEqualsLength(): Unit = {
    val inputArray = Array.tabulate[Byte](256)(i => i.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val outputArray = Array.fill[Byte](iaLength)(0.toByte)
    outputArray(0) = 255.toByte

    dis.readFully(outputArray, 0, outputArray.length)

    val zipped = outputArray.zip(inputArray)
    val index  = zipped.indexWhere(x => x._1 != x._2)

    if (index >= 0) {
      val result   = outputArray(index) & 0xFF // want to print 0-255
      val expected = inputArray(index) & 0xFF

      assertTrue(s"byte at index ${index}: ${result} != expected: ${expected}",
                 false)
    }
  }

  @Test def readFullyBufOffLenPatchMiddleOfBuffer(): Unit = {
    val inputArray = Array.tabulate[Byte](10)(i => (i + 20).toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val outputArray = Array.fill[Byte](20)(1.toByte)

    val expectedArray = Array.fill[Byte](outputArray.length)(outputArray(0))

    for (j <- 0 until iaLength) {
      expectedArray(10 + j) = inputArray(j)
    }

    dis.readFully(outputArray, 10, inputArray.length)

    val zipped = outputArray.zip(expectedArray)
    val index  = zipped.indexWhere(x => x._1 != x._2)

    if (index >= 0) {
      val result   = outputArray(index) & 0xFF // want to print 0-255
      val expected = inputArray(index) & 0xFF

      assertTrue(s"byte at index ${index}: ${result} != expected: ${expected}",
                 false)
    }
  }

  @Test def readFullyBufOffLenUnexpectedEndOfFile(): Unit = {
    val inputArray = Array.tabulate[Byte](128)(i => i.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val outputArray = Array.fill[Byte](iaLength + 1)(0.toByte)

    assertThrows(classOf[EOFException],
                 dis.readFully(outputArray, 0, outputArray.length))
  }

  @Test def readFullyBufNullBuffer(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[NullPointerException],
                 dis.readFully(null.asInstanceOf[Array[Byte]]))
  }

  @Test def readFullyBuf(): Unit = {
    val inputArray = Array.tabulate[Byte](256)(i => i.toByte).reverse
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val outputArray = Array.fill[Byte](iaLength)(0.toByte)

    dis.readFully(outputArray)

    val zipped = outputArray.zip(inputArray)
    val index  = zipped.indexWhere(x => x._1 != x._2)

    if (index >= 0) {
      val result   = outputArray(index) & 0xFF // want to print 0-255
      val expected = inputArray(index) & 0xFF

      assertTrue(s"byte at index ${index}: ${result} != expected: ${expected}",
                 false)
    }
  }

  // readInt

  @Test def readIntEndOfFile(): Unit = {
    // readInt expects to read 4 bytes, cause EOF by providing only 2.
    val inputArray = Array[Byte](0x03.toByte, 0xFF.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readInt())
  }

  @Test def readInt(): Unit = {
    val expected = -2019

    var bits = expected

    val nBytes = sizeof[Int].toInt
    val data   = new Array[Byte](nBytes)

    for (i <- (nBytes - 1) to 0 by -1) {
      data(i) = bits.toByte
      bits >>>= 8
    }

    val inputArray = Array[Byte](data: _*)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readInt())
  }

  // readLine has been deprecated since Java JDK 1.1.
  // Test it anyway, help prevent headstrong people from falling into
  // infinite loops. If it exists, someone if bound to try it.

  // readLine

  @Test def readLineEndOfFile(): Unit = {
    val inputArray = "These are the times that try people's souls.\n".getBytes
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertNull(dis.readLine())
  }

  @Test def readLineTermLf(): Unit = {
    val expected = "These are the times that try people's souls."

    val inputArray = s"${expected}\n".getBytes
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readLine())
  }

  @Test def readLineTermNone(): Unit = {
    val expected = "These are the times that try people's souls."

    val inputArray = expected.getBytes
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readLine())
  }

  // readLong

  @Test def readLongEndOfFile(): Unit = {
    // readLong expects to read 8 bytes, cause EOF by providing only 5.
    val inputArray = List(0x03, 0xFF, 0x100, 0x101, 0x102)
      .map(_.toByte)
      .toArray[Byte]

    val iaLength = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readLong())
  }

  @Test def readLong(): Unit = {
    val expected = Long.MaxValue

    var bits = expected

    val nBytes = sizeof[Long].toInt
    val data   = new Array[Byte](nBytes)

    for (i <- (nBytes - 1) to 0 by -1) {
      data(i) = bits.toByte
      bits >>>= 8
    }

    val inputArray = Array[Byte](data: _*)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readLong())
  }

  // readShort

  @Test def readShortEndOfFile(): Unit = {
    // readShort expects to read 2 bytes, cause EOF by providing zero.
    val inputArray = Array[Byte](0x97.toByte)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readShort())
  }

  @Test def readShort(): Unit = {
    val expected = -1984

    var bits = expected

    val nBytes = sizeof[Short].toInt
    val data   = new Array[Byte](nBytes)

    for (i <- (nBytes - 1) to 0 by -1) {
      data(i) = bits.toByte
      bits >>>= 8
    }

    val inputArray = Array[Byte](data: _*)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readShort())
  }

  // readUnsignedShort
  @Test def readUnsignedShortEndOfFile(): Unit = {
    // readUnsignedShort expects to read 2 bytes, cause EOF by providing 1.
    val inputArray = Array[Byte](0x97.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readUnsignedShort())
  }

  @Test def readUnsignedShort(): Unit = {
    val expected = 0xFEEB

    var bits = expected

    val nBytes = sizeof[Short].toInt
    val data   = new Array[Byte](nBytes)

    for (i <- (nBytes - 1) to 0 by -1) {
      data(i) = bits.toByte
      bits >>>= 8
    }

    val inputArray = Array[Byte](data: _*)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertEquals(expected, dis.readUnsignedShort())
  }

  // readUTF

  @Test def readUtfEndOfFile(): Unit = {
    // This is a deliberately mis-configured Java modified UTF-8 file.
    // A length (first unsigned short, 16 bits) is given, but not
    // that many following bytes. This is to trigger EOF.

    val expected = 103 // ASCII 'g'
    val inputArray =
      List(1, 5, 20, 50).map(_.toByte).toArray[Byte]
    val iaLength = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], dis.readUTF())
  }

  @Test def readUtf(): Unit = {
    // Do not use DataOutputStream#writeUTF to avoid matched and compensating
    // errors in implementation.

    // Try to break readUTF8:
    //   Dollar Sign expands to 1 byte in Java modified UTF-8
    //   Pound Sign (\u00a3) expands to 2 bytes.
    //   Euro Sign (\u20ac) expands to 2 bytes.
    //   OxFFFF (valid Unicode but not defined as a character) is three
    //       bytes of devilry to push high bound & break things.

    val expected       = "$\u00a3\u20ac\uFFFF"
    val expectedLength = expected.length

    val dataBytes  = expected.getBytes
    val dataLength = dataBytes.length
    val highB      = ((dataLength & 0xFFFF0000) >>> 8).toByte
    val lowB       = dataLength.toByte

    val inputArray = Array(highB, lowB) ++ dataBytes
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readUTF()

    assertEquals(expectedLength, result.length)
    assertEquals(expected, result)
  }

  @Test def readUtfOneArgEndOfFile(): Unit = {
    // This is deliberately mis-configured Java modified UTF-8 file.
    // A length (first unsigned short, 16 bits) is given, but not
    // that many following bytes. This is to trigger EOF.

    val expected = 103 // ASCII 'g'
    val inputArray =
      List(44, 0, 43, 12, 87).map(_.toByte).toArray[Byte]
    val iaLength = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows(classOf[EOFException], DataInputStream.readUTF(dis))
  }

  @Test def readUtfOneArg(): Unit = {
    // Do not use DataOutputStream#writeUTF to avoid matched and compensating
    // errors in implementation.

    // Try to break readUTF8:
    //   Dollar Sign expands to 1 byte in Java modified UTF-8
    //   Pound Sign (\u00a3) expands to 2 bytes.
    //   Euro Sign (\u20ac) expands to 2 bytes.
    //   OxFFFF (valid Unicode but not defined as a character) is three
    //       bytes of devilry to push high bound & break things.

    val expected       = "$\u00a3\u20ac\uFFFF"
    val expectedLength = expected.length

    val dataBytes  = expected.getBytes
    val dataLength = dataBytes.length
    val highB      = ((dataLength & 0xFFFF0000) >>> 8).toByte
    val lowB       = dataLength.toByte

    val inputArray = Array(highB, lowB) ++ dataBytes
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = DataInputStream.readUTF(dis)

    assertEquals(expectedLength, result.length)
    assertEquals(expected, result)
  }

  // skipBytes

  @Test def skipBytesEndOfFile(): Unit = {
    val chars = 'b' to 'q'

    val inputArray = chars.map(_.toByte).toArray[Byte]
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    // Do something unusual, start someplace other than 0 and an odd boundary.
    dis.readLong()
    dis.readByte()

    val expected = iaLength - (sizeof[Long] + sizeof[Byte]).toInt

    // skipBytes does not throw EOFException, returns short count read instead.
    assertEquals(expected, dis.skipBytes(iaLength))
  }

  @Test def skipBytes(): Unit = {
    val chars = 'a' to 'z'

    val inputArray = chars.map(_.toByte).toArray[Byte]
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    // Do something unusual, start someplace other than 0 and an odd boundary.
    dis.readInt()
    dis.readByte()

    // Test going up to just before EOF, but not triggering it.
    val expected = iaLength - (sizeof[Int] + sizeof[Byte]).toInt

    assertEquals(expected, dis.skipBytes(expected))
  }
}
