package java.io

import scalanative.unsafe.sizeof

object DataInputStreamSuite extends tests.Suite {

  // read() is inherited from the underlying InputStream. It is not a method
  // implemented in DIS. Since the internals of DIS use in.read(), test
  // the method here. Detect defects as close to the cause as feasible.

  test("read() - end of file") {

    val expected   = 98 // ASCII 'b'
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = new Array[Byte](inputArray.length)

    val result = dis.read()

    assert(result == -1, s"result: ${result} != expected: -1 (EOF)")
  }

  test("read() - byte 255") {

    val expected   = 255 // ASCII nbsp, particularly troublesome
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.read()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // read(b)

  test("read(b) - end of file") {

    val expected = 99.toByte // ASCII 'c'

    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = new Array[Byte](inputArray.length)

    val nRead = dis.read(buffer)

    assert(nRead == -1, s"result: ${nRead} != expected: -1 (EOF)")
  }

  test("read(b)") {

    val expected       = "Blue eyes crying in the rain".getBytes
    val expectedLength = expected.length

    val arrayIn = new ByteArrayInputStream(expected, 0, expectedLength)
    val dis     = new DataInputStream(arrayIn)

    val buffer = new Array[Byte](expectedLength)

    val nRead = dis.read(buffer)

    assert(nRead == expectedLength,
           s"result: ${nRead} != expected: ${expected}")

    for (i <- 0 until expectedLength) {
      assert(buffer(i) == expected(i),
             s"input(${i}): ${buffer(i)} != expected(${i}): ${expected(i)}")
    }
  }

  // read(b, off, len)

  test("read(b, off, len) - end of file") {
    val expected   = 100 // ASCII 'd'
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = 0

    val buffer = new Array[Byte](inputArray.length)

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val nRead = dis.read(buffer, 0, 1)

    assert(nRead == -1, s"result: ${nRead} != expected: -1 (EOF)")

  }

  test("read(b, off, len)") {

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

    val nRead = dis.read(buffer, off, len)

    assert(nRead == len, s"result: ${nRead} != expected: ${len}")

    for (i <- 0 until expectedLength) {
      assert(buffer(i) == expectedBytes(i),
             s"buffer(${i}): ${buffer(i)} != " +
               s"expected(${i}): ${expectedBytes(i)}")
    }
  }

  // Test readByte before other readFoo because the internals of the
  // latter use the former. If there is an error in readByte detect
  // it early, and not have to weed through follow on shrapnel.

  // readByte

  test("readByte() - end of file") {

    val expected   = 101.toByte // ASCII 'e'
    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readByte()
    }
  }

  test("readByte()") {

    val expected   = 102.toByte // ASCII 'f'
    val inputArray = Array[Byte](expected)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readByte()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readBoolean

  test("readBoolean() - end of file") {

    val expected   = 0.toByte
    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readBoolean()
    }
  }

  test("readBoolean() - false") {

    val expected   = false
    val inputArray = Array[Byte](0.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readBoolean()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  test("readBoolean() - true") {

    val expected   = true
    val inputArray = Array[Byte](1.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readBoolean()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // Test readUnsignedByte early before it is used by other readFoo methods.

  // readUnsignedByte

  test("readUnsignedByte() - end of file") {

    val expected   = 255.toByte // ASCII nbsp
    val inputArray = Array[Byte](expected)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readUnsignedByte()
    }
  }

  test("readUnsignedByte() - byte 255") {

    // Broken implementations of readUnsignedByte will report character
    // 255 as -1, high bytes set, not required 255, high bytes clear.
    val expected   = 255 // ASCII nbsp
    val inputArray = Array[Byte](expected.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readUnsignedByte()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readChar

  test("readChar() - end of file") {

    // readChar expects to read 2 bytes, cause EOF by providing only 1.
    val inputArray = Array[Byte](0x97.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readChar()
    }
  }

  test("readChar()") {

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

    val result = dis.readChar()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readDouble

  test("readDouble() - end of file") {

    // readDouble expects to read 8 bytes, cause EOF by providing only 3.
    val inputArray = Array[Byte](0x03.toByte, 0xd5.toByte, 0xFF.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readDouble()
    }
  }

  test("readDouble()") {

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

    val result = dis.readDouble()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readFloat

  test("readFloat() - end of file") {

    // readFloat expects to read 4 bytes, cause EOF by providing only 3.
    val inputArray = Array[Byte](0x03.toByte, 0xd5.toByte, 0xFF.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readFloat()
    }
  }

  test("readFloat()") {

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

    val result = dis.readFloat()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readFully

  test("readFully(b, off, len) - null buffer") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[NullPointerException] {
      dis.readFully(null.asInstanceOf[Array[Byte]], 0, 1)
    }
  }

  test("readFully(b, off, len) - invalid offset argument") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn     = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis         = new DataInputStream(arrayIn)
    val outputArray = new Array[Byte](iaLength)

    assertThrows[IndexOutOfBoundsException] {
      dis.readFully(outputArray, -1, 1)
    }
  }

  test("readFully(b, off, len) - invalid length argument") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn     = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis         = new DataInputStream(arrayIn)
    val outputArray = new Array[Byte](iaLength)

    assertThrows[IndexOutOfBoundsException] {
      dis.readFully(outputArray, 0, -1)
    }
  }

  test("readFully(b, off, len) - invalid offset + length arguments") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn     = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis         = new DataInputStream(arrayIn)
    val outputArray = new Array[Byte](iaLength)

    assertThrows[IndexOutOfBoundsException] {
      dis.readFully(outputArray, 1, outputArray.length)
    }
  }

  test("readFully(b, off, len) - len == 0") {

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

      assert(false,
             s"byte at index ${index}: ${result} != expected: ${expected}")
    }
  }

  test("readFully(b, off, len) - len == b.length") {

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

      assert(false,
             s"byte at index ${index}: ${result} != expected: ${expected}")
    }
  }

  test("readFully(b, off, len) - patch middle of buffer") {

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

      assert(false,
             s"byte at index ${index}: ${result} != expected: ${expected}")
    }
  }

  test("readFully(b, off, len) - unexpected end of file") {

    val inputArray = Array.tabulate[Byte](128)(i => i.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val outputArray = Array.fill[Byte](iaLength + 1)(0.toByte)

    assertThrows[EOFException] {
      dis.readFully(outputArray, 0, outputArray.length)
    }
  }

  test("readFully(b) - null buffer") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val iaLength = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[NullPointerException] {
      dis.readFully(null.asInstanceOf[Array[Byte]])
    }
  }

  test("readFully(b)") {

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

      assert(false,
             s"byte at index ${index}: ${result} != expected: ${expected}")
    }
  }

  // readInt

  test("readInt() - end of file") {

    // readInt expects to read 4 bytes, cause EOF by providing only 2.
    val inputArray = Array[Byte](0x03.toByte, 0xFF.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readInt()
    }
  }

  test("readInt()") {

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

    val result = dis.readInt()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readLine has been deprecated since Java JDK 1.1.
  // Test it anyway, help prevent headstrong people from falling into
  // infinite loops. If it exists, someone if bound to try it.

  // readLine

  test("readLine() - end of file") {

    val inputArray = "These are the times that try people's souls.\n".getBytes
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readLine()

    assert(result == null, s"result: ${result} != expected: null")
  }

  test("readLine() - line terminator '\\n'") {

    val expected = "These are the times that try people's souls."

    val inputArray = s"${expected}\n".getBytes
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readLine()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  test("readLine() - no line terminator") {

    val expected = "These are the times that try people's souls."

    val inputArray = expected.getBytes
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    val result = dis.readLine()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readLong

  test("readLong() - end of file") {

    // readLong expects to read 8 bytes, cause EOF by providing only 5.
    val inputArray = List(0x03, 0xFF, 0x100, 0x101, 0x102)
      .map(_.toByte)
      .toArray[Byte]

    val iaLength = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readLong()
    }
  }

  test("readLong()") {

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

    val result = dis.readLong()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readShort

  test("readShort() - end of file") {

    // readShort expects to read 2 bytes, cause EOF by providing zero.
    val inputArray = Array[Byte](0x97.toByte)
    val iaLength   = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readShort()
    }
  }

  test("readShort()") {

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

    val result = dis.readShort()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readnUsignedShort

  test("readUnsignedShort() - end of file") {

    // readUnsignedShort expects to read 2 bytes, cause EOF by providing 1.
    val inputArray = Array[Byte](0x97.toByte)
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readUnsignedShort()
    }
  }

  test("readUnsignedShort()") {

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

    val result = dis.readUnsignedShort()

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // readUTF

  test("readUTF() - end of file") {

    // This is deliberately mis-configured Java modified UTF-8 file.
    // A length (first unsigned short, 16 bits) is given, but not
    // that many following bytes. This is to trigger EOF.

    val expected = 103 // ASCII 'g'
    val inputArray =
      List(1, 5, 20, 50).map(_.toByte).toArray[Byte]
    val iaLength = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      dis.readUTF()
    }
  }

  test("readUTF()") {
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

    assert(result.length == expectedLength,
           s"result: ${result.length} != expected: ${expectedLength}")

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  test("readUTF(in) - end of file") {
    // This is deliberately mis-configured Java modified UTF-8 file.
    // A length (first unsigned short, 16 bits) is given, but not
    // that many following bytes. This is to trigger EOF.

    val expected = 103 // ASCII 'g'
    val inputArray =
      List(44, 0, 43, 12, 87).map(_.toByte).toArray[Byte]
    val iaLength = 0

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    assertThrows[EOFException] {
      DataInputStream.readUTF(dis)
    }
  }

  test("readUTF(in)") {
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

    assert(result.length == expectedLength,
           s"result: ${result.length} != expected: ${expectedLength}")

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  // skipBytes

  test("skipBytes() - end of file") {

    val chars = ('b' to 'q')

    val inputArray = chars.map(_.toByte).toArray[Byte]
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    // Do something unusual, start someplace other than 0 and an odd boundary.
    dis.readLong()
    dis.readByte()

    val expected = iaLength - (sizeof[Long] + sizeof[Byte]).toInt

    // skipBytes does not throw EOFException, returns short count read instead.
    val result = dis.skipBytes(iaLength)

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  test("skipBytes()") {

    val chars = ('a' to 'z')

    val inputArray = chars.map(_.toByte).toArray[Byte]
    val iaLength   = inputArray.length

    val arrayIn = new ByteArrayInputStream(inputArray, 0, iaLength)
    val dis     = new DataInputStream(arrayIn)

    // Do something unusual, start someplace other than 0 and an odd boundary.
    dis.readInt()
    dis.readByte()

    // Test going up to just before EOF, but not triggering it.
    val expected = iaLength - (sizeof[Int] + sizeof[Byte]).toInt

    val result = dis.skipBytes(expected)

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

}
