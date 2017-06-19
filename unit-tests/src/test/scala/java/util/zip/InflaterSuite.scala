package java.util.zip

import java.io.ByteArrayOutputStream

object InflaterSuite extends tests.Suite {
  test("Inflater.setInput doesn't throw an exception") {
    val inflater = new Inflater()
    val bytes    = Array[Byte](1, 2, 3)
    inflater.setInput(bytes, 0, 3)
  }

  test("Inflater needs input right after being created") {
    val inflater = new Inflater()
    assert(inflater.needsInput())
  }

  test("Inflater doesn't need input after input has been set") {
    val inflater = new Inflater()
    val bytes    = Array[Byte](1, 2, 3)
    assert(inflater.needsInput())
    inflater.setInput(bytes)
    assert(!inflater.needsInput())
  }

  test("Inflater doesn't need dictionary right after being created") {
    val inflater = new Inflater()
    assert(!inflater.needsDictionary())
  }

  test("Inflater doesn't need dictionary right after input has been set") {
    val inflater = new Inflater()
    val bytes    = Array[Byte](1, 2, 3)
    assert(!inflater.needsDictionary())
    inflater.setInput(bytes)
    assert(!inflater.needsDictionary())
  }

  test(
    "Inflater can inflate byte arrays compressed with default compression level") {
    // Created using `Deflater` on the JVM -- this is the result of compressing
    // Array.fill[Byte](1024)(1)
    val bytes = Array[Byte](120, -100, 99, 100, 28, 5, -93, 96, 20, -116, 84,
      0, 0, 6, 120, 4, 1)
    val inflater = new Inflater()
    val bos      = new ByteArrayOutputStream()
    inflater.setInput(bytes)

    val buf = new Array[Byte](1024)
    var h   = 0
    while (!inflater.finished()) {
      val count = inflater.inflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val decompressed = bos.toByteArray()

    assert(decompressed.length == 1024)
    assert(decompressed.forall(_ == 1))
  }

  test(
    "Inflater can inflate byte arrays compressed with best compression level") {
    val bytes = Array[Byte](120, -38, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0,
      0, 6, 120, 4, 1)
    val inflater = new Inflater()
    val bos      = new ByteArrayOutputStream()
    inflater.setInput(bytes)

    val buf = new Array[Byte](1024)
    var h   = 0
    while (!inflater.finished()) {
      val count = inflater.inflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val decompressed = bos.toByteArray()

    assert(decompressed.length == 1024)
    assert(decompressed.forall(_ == 1))
  }

  test(
    "Inflater can inflate byte arrays when given a buffer smalled than total output") {
    val bytes = Array[Byte](120, -38, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0,
      0, 6, 120, 4, 1)
    val inflater = new Inflater()
    val bos      = new ByteArrayOutputStream()
    inflater.setInput(bytes)

    val buf = new Array[Byte](56)
    var h   = 0
    while (!inflater.finished()) {
      val count = inflater.inflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val decompressed = bos.toByteArray()

    assert(decompressed.length == 1024)
    assert(decompressed.forall(_ == 1))
  }

  // The following tests are
  // Ported from Apache Harmony

  var outPutBuff1: Array[Byte]   = null
  var outPutDiction: Array[Byte] = null
  private def setUp(): Unit = {
    outPutBuff1 = Array[Byte](120, 94, 99, 100, 102, 97, -25, 72, 45, 42, -87,
      52, 5, 0, 6, -12, 2, 17) ++ Array.fill[Byte](483)(0)
    outPutDiction = Array[Byte](120, 63, 13, 10, 107, 2, 20, 99, 97, 101, 102,
      74, 76, 98, 99, -25, -32, 100, 40, 54, 46, 47, 2, 13, 112, 2,
      127) ++ Array.fill[Byte](474)(0)
  }

  test("finished") {
    setUp()
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val inflate   = new Inflater(false)
    val outPutInf = new Array[Byte](500)
    while (!(inflate.finished())) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      inflate.inflate(outPutInf)
    }
    assert(inflate.finished())
    var i = 0
    while (i < byteArray.length) {
      assert(outPutInf(i) == byteArray(i))
      i += 1
    }
    assert(outPutInf(byteArray.length) == 0)
  }

  test("getAdler()") {
    setUp()
    val dictionaryArray = Array[Byte]('e', 'r', 't', 'a', 'b', 2, 3)
    val inflateDiction  = new Inflater()
    inflateDiction.setInput(outPutDiction)
    if (inflateDiction.needsDictionary()) {
      val adl = new Adler32()
      adl.update(dictionaryArray)
      val checkSumR = adl.getValue()
      assert(inflateDiction.getAdler() == checkSumR)
    }
  }

  test("getRemaining()") {
    val byteArray = Array[Byte](1, 3, 5, 6, 7)
    val inflate   = new Inflater()
    assert(inflate.getRemaining() == 0)
    inflate.setInput(byteArray)
    assert(inflate.getRemaining() != 0)
  }

  test("getTotalIn()") {
    setUp()
    val outPutBuf = new Array[Byte](500)
    val byteArray = Array[Byte](1, 3, 4, 7, 8)
    val outPutInf = new Array[Byte](500)
    var x         = 0
    val deflate   = new Deflater(1)
    deflate.setInput(byteArray)
    while (!deflate.needsInput()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    deflate.finish()
    while (!deflate.finished()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }

    val inflate = new Inflater()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuf)
      }
      inflate.inflate(outPutInf)
    }
    assert(deflate.getTotalOut() == inflate.getTotalIn())

    val inflate2 = new Inflater()
    var offSet   = 0
    var length   = 4
    if (inflate2.needsInput()) {
      inflate2.setInput(outPutBuff1, offSet, length)
    }
    inflate2.inflate(outPutInf)

    assert(length == inflate2.getTotalIn())
  }

  test("getTotalOut()") {
    setUp()
    val outPutBuf = new Array[Byte](500)
    val byteArray = Array[Byte](1, 3, 4, 7, 8)
    var y         = 0
    var x         = 0
    val deflate   = new Deflater(1)
    deflate.setInput(byteArray)
    while (!deflate.needsInput()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    deflate.finish()
    while (!deflate.finished()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }

    val inflate   = new Inflater()
    val outPutInf = new Array[Byte](500)
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuf)
      }
      y += inflate.inflate(outPutInf)
    }

    assert(inflate.getTotalOut() == y)
    assert(deflate.getTotalIn() == inflate.getTotalOut())

    inflate.reset()
    y = 0
    var offSet = 0
    var length = 4

    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuf)
      }
      y += inflate.inflate(outPutInf, offSet, length)
    }

    assert(y == inflate.getTotalOut())
    assert(deflate.getTotalIn() == inflate.getTotalOut())

  }

  test("inflate$B") {
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val outPutInf = new Array[Byte](500)
    val inflate   = new Inflater()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      inflate.inflate(outPutInf)
    }

    var i = 0
    while (i < byteArray.length) {
      assert(byteArray(i) == outPutInf(i))
      i += 1
    }
    assert(0 == outPutInf(byteArray.length))

    val outPutBuf  = new Array[Byte](500)
    val emptyArray = new Array[Byte](11)
    var x          = 0
    val defEmpty   = new Deflater(3)
    defEmpty.setInput(emptyArray)
    while (!defEmpty.needsInput()) {
      x += defEmpty.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    defEmpty.finish()
    while (!defEmpty.finished()) {
      x += defEmpty.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    assert(x == defEmpty.getTotalOut())
    assert(emptyArray.length == defEmpty.getTotalIn())

    val infEmpty = new Inflater()
    while (!infEmpty.finished()) {
      if (infEmpty.needsInput()) {
        infEmpty.setInput(outPutBuf)
      }
      infEmpty.inflate(outPutInf)
    }

    var j = 0
    while (j < emptyArray.length) {
      assert(emptyArray(j) == outPutInf(j))
      assert(outPutInf(j) == 0)
      j += 1
    }
    assert(outPutInf(emptyArray.length) == 0)
  }

  test("inflate$B1") {
    val codedData = Array[Byte](120, -38, 75, -54, 73, -52, 80, 40, 46, 41,
      -54, -52, 75, 87, 72, -50, -49, 43, 73, -52, -52, 43, 86, 72, 2, 10, 34,
      99, -123, -60, -68, 20, -80, 32, 0, -101, -69, 17, 84)
    val codedString = "blah string contains blahblahblahblah and blah"
    val infl1       = new Inflater()
    val infl2       = new Inflater()

    val result = new Array[Byte](100)
    var decLen = 0

    infl1.setInput(codedData, 0, codedData.length)
    decLen = infl1.inflate(result)
    infl1.end()
    assert(codedString == new String(result, 0, decLen))
    codedData(5) = 0

    infl2.setInput(codedData, 0, codedData.length)
    assertThrows[DataFormatException] {
      infl2.inflate(result)
    }
    infl2.end()
  }

  test("inflate$BII") {
    setUp()
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val outPutInf = new Array[Byte](100)
    var y         = 0
    val inflate   = new Inflater()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        assert(inflate.inflate(outPutInf, 0, 1) == 0)
        inflate.setInput(outPutBuff1)
      }
      y += inflate.inflate(outPutInf, y, outPutInf.length - y)
    }

    var i = 0
    while (i < byteArray.length) {
      assert(byteArray(i) == outPutInf(i))
      i += 1
    }
    assert(0 == outPutInf(byteArray.length))

    inflate.reset()
    var r           = 0
    var offSet      = 0
    var lengthError = 101
    assertThrows[ArrayIndexOutOfBoundsException] {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      inflate.inflate(outPutInf, offSet, lengthError)
    }

    assert(inflate.inflate(outPutInf, offSet, 0) == 0)
    inflate.end()

    assertThrows[IllegalStateException] {
      inflate.inflate(outPutInf, offSet, 1)
    }
  }

  test("inflate$BII1") {
    val codedData = Array[Byte](120, -38, 75, -54, 73, -52, 80, 40, 46, 41,
      -54, -52, 75, 87, 72, -50, -49, 43, 73, -52, -52, 43, 86, 72, 2, 10, 34,
      99, -123, -60, -68, 20, -80, 32, 0, -101, -69, 17, 84)
    val codedString = "blah string"

    val infl1  = new Inflater()
    val infl2  = new Inflater()
    val result = new Array[Byte](100)
    var decLen = 0

    infl1.setInput(codedData, 0, codedData.length)
    decLen = infl1.inflate(result, 10, 11)

    infl1.end()
    assert(codedString == new String(result, 10, decLen))
    codedData(5) = 0

    infl2.setInput(codedData, 0, codedData.length)
    assertThrows[DataFormatException] {
      infl2.inflate(result, 10, 11)
    }
    infl2.end()
  }

  test("inflateZero") {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val deflaterOutputStream  = new DeflaterOutputStream(byteArrayOutputStream)
    deflaterOutputStream.close()
    val input = byteArrayOutputStream.toByteArray()

    val inflater = new Inflater()
    inflater.setInput(input)
    val buffer  = new Array[Byte](0)
    var numRead = 0
    while (!inflater.finished()) {
      val inflatedChunkSize =
        inflater.inflate(buffer, numRead, buffer.length - numRead)
      numRead += inflatedChunkSize
    }
  }

  test("Constructor(Boolean)") {
    setUp()
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val inflate   = new Inflater(true)
    val outPutInf = new Array[Byte](500)
    var r         = 0
    // An exception should be throws because of header inconsistency.
    assertThrows[DataFormatException] {
      while (!inflate.finished()) {
        if (inflate.needsInput()) {
          inflate.setInput(outPutBuff1)
        }
        inflate.inflate(outPutInf)
      }
    }
  }

  test("needsDictionary()") {
    setUp()
    // note: the needsDictionary flag is set after inflate is called
    val outPutInf      = new Array[Byte](500)
    val inflateDiction = new Inflater()

    // testing with dictionary set.
    if (inflateDiction.needsInput()) {
      inflateDiction.setInput(outPutDiction)
    }
    assert(inflateDiction.inflate(outPutInf) == 0)
    assert(inflateDiction.needsDictionary())

    val inflate = new Inflater()
    inflate.setInput(outPutBuff1)
    inflate.inflate(outPutInf)
    assert(!inflate.needsDictionary())

    val inf = new Inflater()
    assert(!inf.needsDictionary())
    assert(0 == inf.getTotalIn())
    assert(0 == inf.getTotalOut())
    assert(0 == inf.getBytesRead())
    assert(0 == inf.getBytesWritten())
    assert(1 == inf.getAdler())
  }

  test("needsInput()") {
    val inflate = new Inflater()
    assert(inflate.needsInput())
    val byteArray = Array[Byte](2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9)
    inflate.setInput(byteArray)
    assert(!inflate.needsInput())

    inflate.reset()

    val byteArrayEmpty = new Array[Byte](0)
    inflate.setInput(byteArrayEmpty)
    assert(inflate.needsInput())
  }

  test("reset()") {
    setUp()
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val outPutInf = new Array[Byte](100)
    var y         = 0
    val inflate   = new Inflater()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      y += inflate.inflate(outPutInf, y, outPutInf.length - y)
    }
    var i = 0
    while (i < byteArray.length) {
      assert(byteArray(i) == outPutInf(i))
      i += 1
    }
    assert(outPutInf(byteArray.length) == 0)

    // testing that resetting the inflater will also return the correct
    // decompressed data

    inflate.reset()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      inflate.inflate(outPutInf)
    }
    var j = 0
    while (j < byteArray.length) {
      assert(byteArray(j) == outPutInf(j))
      j += 1
    }
    assert(outPutInf(byteArray.length) == 0)
  }

  test("setInput$B") {
    val byteArray = Array[Byte](2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9)
    val inflate   = new Inflater()
    inflate.setInput(byteArray)
    assert(inflate.getRemaining() != 0)
  }

  test("setInput$BII") {
    val byteArray = Array[Byte](2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9)
    var offSet    = 6
    var length    = 6
    val inflate   = new Inflater()
    inflate.setInput(byteArray, offSet, length)
    assert(length == inflate.getRemaining())

    // boundary check
    inflate.reset()
    assertThrows[ArrayIndexOutOfBoundsException] {
      inflate.setInput(byteArray, 100, 100)
    }
  }

  test("getBytesRead()") {
    val defl = new Deflater()
    val infl = new Inflater()
    assert(0 == defl.getTotalIn())
    assert(0 == defl.getTotalOut())
    assert(0 == defl.getBytesRead())
    // Encode a String into bytes
    val inputString = "blahblahblah??"
    val input       = inputString.getBytes("UTF-8")

    // Compress the bytes
    val output = new Array[Byte](100)
    defl.setInput(input)
    defl.finish()
    defl.deflate(output)
    infl.setInput(output)
    val compressedDataLength = infl.inflate(input)
    assert(16 == infl.getTotalIn())
    assert(compressedDataLength == infl.getTotalOut())
    assert(16 == infl.getBytesRead())
  }

  test("getBytesWritten()") {
    val defl = new Deflater()
    val infl = new Inflater()
    assert(0 == defl.getTotalIn())
    assert(0 == defl.getTotalOut())
    assert(0 == defl.getBytesWritten())
    // Encode a String into bytes
    val inputString = "blahblahblah??"
    val input       = inputString.getBytes("UTF-8")

    // Compress the bytes
    val output = new Array[Byte](100)
    defl.setInput(input)
    defl.finish()
    defl.deflate(output)
    infl.setInput(output)
    val compressedDataLength = infl.inflate(input)
    assert(16 == infl.getTotalIn())
    assert(compressedDataLength == infl.getTotalOut())
    assert(14 == infl.getBytesWritten())
  }

  test("inflate") {
    val inf = new Inflater()
    val res = inf.inflate(new Array[Byte](0), 0, 0)
    assert(0 == res)

    var inflater = new Inflater()
    val b        = new Array[Byte](1024)
    assert(0 == inflater.inflate(b))
    inflater.end()

    // NOTE: Here we differ from Harmony, and follow the JVM.
    // Harmony throws a DataFormatException in `inflate` here.
    inflater = new Inflater()
    inflater.setInput(Array[Byte](-1))
    assert(0 == inflater.inflate(b))
    assert(inflater.needsInput())

    inflater = new Inflater()
    inflater.setInput(Array[Byte](-1, -1, -1))
    assertThrows[DataFormatException] {
      inflater.inflate(b)
    }
  }

  test("setDictionary$B") {
    var i           = 0
    val inputString = "blah string contains blahblahblahblah and blah"
    val dictionary1 = "blah"
    val dictionary2 = "1234"

    val outputNo  = new Array[Byte](100)
    val output1   = new Array[Byte](100)
    val output2   = new Array[Byte](100)
    val defDictNo = new Deflater(9)
    val defDict1  = new Deflater(9)
    val defDict2  = new Deflater(9)

    defDict1.setDictionary(dictionary1.getBytes())
    defDict2.setDictionary(dictionary2.getBytes())

    defDictNo.setInput(inputString.getBytes())
    defDict1.setInput(inputString.getBytes())
    defDict2.setInput(inputString.getBytes())

    defDictNo.finish()
    defDict1.finish()
    defDict2.finish()

    val dataLenNo = defDictNo.deflate(outputNo)
    val dataLen1  = defDict1.deflate(output1)
    val dataLen2  = defDict2.deflate(output2)

    var passNo1 = false
    var passNo2 = false
    var pass12  = false

    i = 0
    while (!passNo1 && i < (if (dataLenNo < dataLen1) dataLenNo else dataLen1)) {
      if (outputNo(i) != output1(i)) {
        passNo1 = true
      }
      i += 1
    }

    var j = 0
    while (!passNo2 && j < (if (dataLenNo < dataLen1) dataLenNo else dataLen2)) {
      if (outputNo(j) != output2(j)) {
        passNo2 = true
      }
      j += 1
    }

    var k = 0
    while (!pass12 && k < (if (dataLen1 < dataLen2) dataLen1 else dataLen2)) {
      if (output1(k) != output2(k)) {
        pass12 = true
      }
      k += 1
    }

    assert(passNo1)
    assert(passNo2)
    assert(pass12)

    var inflNo = new Inflater()
    var infl1  = new Inflater()
    var infl2  = new Inflater()

    val result = new Array[Byte](100)
    var decLen = 0

    inflNo.setInput(outputNo, 0, dataLenNo)
    decLen = inflNo.inflate(result)

    assert(!inflNo.needsDictionary())
    inflNo.end()
    assert(inputString == new String(result, 0, decLen))

    infl1.setInput(output1, 0, dataLen1)
    decLen = infl1.inflate(result)

    assert(infl1.needsDictionary())
    infl1.setDictionary(dictionary1.getBytes())
    decLen = infl1.inflate(result)
    infl1.end()
    assert(inputString == new String(result, 0, decLen))

    infl2.setInput(output2, 0, dataLen2)
    decLen = infl2.inflate(result)

    assert(infl2.needsDictionary())
    infl2.setDictionary(dictionary2.getBytes())
    decLen = infl2.inflate(result)
    infl2.end()
    assert(inputString == new String(result, 0, decLen))

    inflNo = new Inflater()
    infl1 = new Inflater()
    inflNo.setInput(outputNo, 0, dataLenNo)
    assertThrows[IllegalArgumentException] {
      infl1.setDictionary(dictionary1.getBytes())
    }
    inflNo.end()

    infl1.setInput(output1, 0, dataLen1)
    decLen = infl1.inflate(result)

    assert(infl1.needsDictionary())
    assertThrows[IllegalArgumentException] {
      infl1.setDictionary(dictionary2.getBytes())
    }
    infl1.end()
    assertThrows[IllegalStateException] {
      infl1.setDictionary(dictionary2.getBytes())
    }
  }

  test("Exceptions") {
    val byteArray = Array[Byte](5, 2, 3, 7, 8)

    val r       = 0;
    val inflate = new Inflater()
    inflate.setInput(byteArray)
    inflate.end()

    assertThrows[IllegalStateException] {
      inflate.getAdler()
    }

    assertThrows[NullPointerException] {
      inflate.getBytesRead()
    }

    assertThrows[NullPointerException] {
      inflate.getBytesWritten()
    }

    assertThrows[IllegalStateException] {
      inflate.getTotalIn()
    }

    assertThrows[IllegalStateException] {
      inflate.getTotalOut()
    }
  }

}
