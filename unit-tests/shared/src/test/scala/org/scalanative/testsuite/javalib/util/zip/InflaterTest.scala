package org.scalanative.testsuite.javalib.util.zip

import java.io.ByteArrayOutputStream
import java.util.zip._

import org.junit.Assert._
import org.junit.{Before, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class InflaterTest {

  val classOfIllegalStateException =
    if (Platform.executingInJVMWithJDKIn(25 to Integer.MAX_VALUE))
      classOf[IllegalStateException]
    else
      classOf[NullPointerException] // Scala Native is JDK 8

  @Test def inflaterSetInputDoesNotThrowAnException(): Unit = {
    val inflater = new Inflater()
    val bytes = Array[Byte](1, 2, 3)
    inflater.setInput(bytes, 0, 3)
  }

  @Test def inflaterNeedsInputRightAfterBeingCreated(): Unit = {
    val inflater = new Inflater()
    assertTrue(inflater.needsInput())
  }

  @Test def inflaterDoesNotNeedInputAfterInputHasBeenSet(): Unit = {
    val inflater = new Inflater()
    val bytes = Array[Byte](1, 2, 3)
    assertTrue(inflater.needsInput())
    inflater.setInput(bytes)
    assertTrue(!inflater.needsInput())
  }

  @Test def inflaterDoesNotNeedDictionaryRightAfterBeingCreated(): Unit = {
    val inflater = new Inflater()
    assertTrue(!inflater.needsDictionary())
  }

  @Test def inflaterDoesNotNeedDictionaryRightAfterInputHasBeenSet(): Unit = {
    val inflater = new Inflater()
    val bytes = Array[Byte](1, 2, 3)
    assertTrue(!inflater.needsDictionary())
    inflater.setInput(bytes)
    assertTrue(!inflater.needsDictionary())
  }

  @Test def inflaterCanInflateByteArraysCompressedWithDefaultLevel(): Unit = {
    // Created using `Deflater` on the JVM -- this is the result of compressing
    // Array.fill[Byte](1024)(1)
    val bytes = Array[Byte](120, -100, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0,
      0, 6, 120, 4, 1)
    val inflater = new Inflater()
    val bos = new ByteArrayOutputStream()
    inflater.setInput(bytes)

    val buf = new Array[Byte](1024)
    var h = 0
    while (!inflater.finished()) {
      val count = inflater.inflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val decompressed = bos.toByteArray()

    assertTrue(decompressed.length == 1024)
    assertTrue(decompressed.forall(_ == 1))
  }

  @Test def inflaterCanInflateByteArraysCompressedWithBestLevel(): Unit = {
    val bytes = Array[Byte](120, -38, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0,
      0, 6, 120, 4, 1)
    val inflater = new Inflater()
    val bos = new ByteArrayOutputStream()
    inflater.setInput(bytes)

    val buf = new Array[Byte](1024)
    var h = 0
    while (!inflater.finished()) {
      val count = inflater.inflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val decompressed = bos.toByteArray()

    assertTrue(decompressed.length == 1024)
    assertTrue(decompressed.forall(_ == 1))
  }

  @Test def inflaterCanInflateByteArraysWhenGivenBufferIsSmallerThanTotalOutput()
      : Unit = {
    val bytes = Array[Byte](120, -38, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0,
      0, 6, 120, 4, 1)
    val inflater = new Inflater()
    val bos = new ByteArrayOutputStream()
    inflater.setInput(bytes)

    val buf = new Array[Byte](56)
    var h = 0
    while (!inflater.finished()) {
      val count = inflater.inflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val decompressed = bos.toByteArray()

    assertTrue(decompressed.length == 1024)
    assertTrue(decompressed.forall(_ == 1))
  }

  // The following tests are
  // Ported from Apache Harmony

  var outPutBuff1: Array[Byte] = null
  var outPutDiction: Array[Byte] = null

  @Before
  def setUp(): Unit = {
    outPutBuff1 = Array[Byte](120, 94, 99, 100, 102, 97, -25, 72, 45, 42, -87,
      52, 5, 0, 6, -12, 2, 17) ++ Array.fill[Byte](483)(0)
    outPutDiction = Array[Byte](120, 63, 13, 10, 107, 2, 20, 99, 97, 101, 102,
      74, 76, 98, 99, -25, -32, 100, 40, 54, 46, 47, 2, 13, 112, 2, 127) ++
      Array.fill[Byte](474)(0)
  }

  @Test def finished(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val inflate = new Inflater(false)
    val outPutInf = new Array[Byte](500)
    while (!(inflate.finished())) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      inflate.inflate(outPutInf)
    }
    assertTrue(inflate.finished())
    var i = 0
    while (i < byteArray.length) {
      assertTrue(outPutInf(i) == byteArray(i))
      i += 1
    }
    assertTrue(outPutInf(byteArray.length) == 0)
  }

  @Test def getAdler(): Unit = {
    val dictionaryArray = Array[Byte]('e', 'r', 't', 'a', 'b', 2, 3)
    val inflateDiction = new Inflater()
    inflateDiction.setInput(outPutDiction)
    if (inflateDiction.needsDictionary()) {
      val adl = new Adler32()
      adl.update(dictionaryArray)
      val checkSumR = adl.getValue()
      assertTrue(inflateDiction.getAdler() == checkSumR)
    }
  }

  @Test def getRemaining(): Unit = {
    val byteArray = Array[Byte](1, 3, 5, 6, 7)
    val inflate = new Inflater()
    assertTrue(inflate.getRemaining() == 0)
    inflate.setInput(byteArray)
    assertTrue(inflate.getRemaining() != 0)
  }

  @Test def getTotalIn(): Unit = {
    val outPutBuf = new Array[Byte](500)
    val byteArray = Array[Byte](1, 3, 4, 7, 8)
    val outPutInf = new Array[Byte](500)
    var x = 0
    val deflate = new Deflater(1)
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
    assertTrue(deflate.getTotalOut() == inflate.getTotalIn())

    val inflate2 = new Inflater()
    var offSet = 0
    var length = 4
    if (inflate2.needsInput()) {
      inflate2.setInput(outPutBuff1, offSet, length)
    }
    inflate2.inflate(outPutInf)

    assertTrue(length == inflate2.getTotalIn())
  }

  @Test def getTotalOut(): Unit = {
    val outPutBuf = new Array[Byte](500)
    val byteArray = Array[Byte](1, 3, 4, 7, 8)
    var y = 0
    var x = 0
    val deflate = new Deflater(1)
    deflate.setInput(byteArray)
    while (!deflate.needsInput()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    deflate.finish()
    while (!deflate.finished()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }

    val inflate = new Inflater()
    val outPutInf = new Array[Byte](500)
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuf)
      }
      y += inflate.inflate(outPutInf)
    }

    assertTrue(inflate.getTotalOut() == y)
    assertTrue(deflate.getTotalIn() == inflate.getTotalOut())

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

    assertTrue(y == inflate.getTotalOut())
    assertTrue(deflate.getTotalIn() == inflate.getTotalOut())

  }

  @Test def inflateArrayByteInt(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val outPutInf = new Array[Byte](500)
    val inflate = new Inflater()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      inflate.inflate(outPutInf)
    }

    var i = 0
    while (i < byteArray.length) {
      assertTrue(byteArray(i) == outPutInf(i))
      i += 1
    }
    assertTrue(0 == outPutInf(byteArray.length))

    val outPutBuf = new Array[Byte](500)
    val emptyArray = new Array[Byte](11)
    var x = 0
    val defEmpty = new Deflater(3)
    defEmpty.setInput(emptyArray)
    while (!defEmpty.needsInput()) {
      x += defEmpty.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    defEmpty.finish()
    while (!defEmpty.finished()) {
      x += defEmpty.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    assertTrue(x == defEmpty.getTotalOut())
    assertTrue(emptyArray.length == defEmpty.getTotalIn())

    val infEmpty = new Inflater()
    while (!infEmpty.finished()) {
      if (infEmpty.needsInput()) {
        infEmpty.setInput(outPutBuf)
      }
      infEmpty.inflate(outPutInf)
    }

    var j = 0
    while (j < emptyArray.length) {
      assertTrue(emptyArray(j) == outPutInf(j))
      assertTrue(outPutInf(j) == 0)
      j += 1
    }
    assertTrue(outPutInf(emptyArray.length) == 0)
  }

  @Test def inflateArrayByteInt1(): Unit = {
    val codedData = Array[Byte](120, -38, 75, -54, 73, -52, 80, 40, 46, 41, -54,
      -52, 75, 87, 72, -50, -49, 43, 73, -52, -52, 43, 86, 72, 2, 10, 34, 99,
      -123, -60, -68, 20, -80, 32, 0, -101, -69, 17, 84)
    val codedString = "blah string contains blahblahblahblah and blah"
    val infl1 = new Inflater()
    val infl2 = new Inflater()

    val result = new Array[Byte](100)
    var decLen = 0

    infl1.setInput(codedData, 0, codedData.length)
    decLen = infl1.inflate(result)
    infl1.end()
    assertTrue(codedString == new String(result, 0, decLen))
    codedData(5) = 0

    infl2.setInput(codedData, 0, codedData.length)
    assertThrows(classOf[DataFormatException], infl2.inflate(result))
    infl2.end()
  }

  @Test def inflateArrayByteIntInt(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val outPutInf = new Array[Byte](100)
    var y = 0
    val inflater = new Inflater()
    while (!inflater.finished()) {
      if (inflater.needsInput()) {
        assertTrue(inflater.inflate(outPutInf, 0, 1) == 0)
        inflater.setInput(outPutBuff1)
      }
      y += inflater.inflate(outPutInf, y, outPutInf.length - y)
    }

    var i = 0
    while (i < byteArray.length) {
      assertTrue(byteArray(i) == outPutInf(i))
      i += 1
    }
    assertTrue(0 == outPutInf(byteArray.length))

    inflater.reset()
    var r = 0
    var offSet = 0
    var lengthError = 101
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException], {
        if (inflater.needsInput()) {
          inflater.setInput(outPutBuff1)
        }
        inflater.inflate(outPutInf, offSet, lengthError)
      }
    )

    assertTrue(inflater.inflate(outPutInf, offSet, 0) == 0)
    inflater.end()

    assertThrows(
      classOfIllegalStateException,
      inflater.inflate(outPutInf, offSet, 1)
    )
  }

  @Test def inflateArrayByteInt3(): Unit = {
    val codedData = Array[Byte](120, -38, 75, -54, 73, -52, 80, 40, 46, 41, -54,
      -52, 75, 87, 72, -50, -49, 43, 73, -52, -52, 43, 86, 72, 2, 10, 34, 99,
      -123, -60, -68, 20, -80, 32, 0, -101, -69, 17, 84)
    val codedString = "blah string"

    val infl1 = new Inflater()
    val infl2 = new Inflater()
    val result = new Array[Byte](100)
    var decLen = 0

    infl1.setInput(codedData, 0, codedData.length)
    decLen = infl1.inflate(result, 10, 11)

    infl1.end()
    assertTrue(codedString == new String(result, 10, decLen))
    codedData(5) = 0

    infl2.setInput(codedData, 0, codedData.length)
    assertThrows(classOf[DataFormatException], infl2.inflate(result, 10, 11))
    infl2.end()
  }

  @Test def inflateZero(): Unit = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)
    deflaterOutputStream.close()
    val input = byteArrayOutputStream.toByteArray()

    val inflater = new Inflater()
    inflater.setInput(input)
    val buffer = new Array[Byte](0)
    var numRead = 0
    while (!inflater.finished()) {
      val inflatedChunkSize =
        inflater.inflate(buffer, numRead, buffer.length - numRead)
      numRead += inflatedChunkSize
    }
  }

  @Test def constructorBoolean(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val inflate = new Inflater(true)
    val outPutInf = new Array[Byte](500)
    var r = 0
    // An exception should be throws because of header inconsistency.
    assertThrows(
      classOf[DataFormatException], {
        while (!inflate.finished()) {
          if (inflate.needsInput()) {
            inflate.setInput(outPutBuff1)
          }
          inflate.inflate(outPutInf)
        }
      }
    )
  }

  @Test def needsDictionary(): Unit = {
    // note: the needsDictionary flag is set after inflate is called
    val outPutInf = new Array[Byte](500)
    val inflateDiction = new Inflater()

    // testing with dictionary set.
    if (inflateDiction.needsInput()) {
      inflateDiction.setInput(outPutDiction)
    }
    assertTrue(inflateDiction.inflate(outPutInf) == 0)
    assertTrue(inflateDiction.needsDictionary())

    val inflate = new Inflater()
    inflate.setInput(outPutBuff1)
    inflate.inflate(outPutInf)
    assertTrue(!inflate.needsDictionary())

    val inf = new Inflater()
    assertTrue(!inf.needsDictionary())
    assertTrue(0 == inf.getTotalIn())
    assertTrue(0 == inf.getTotalOut())
    assertTrue(0 == inf.getBytesRead())
    assertTrue(0 == inf.getBytesWritten())
    assertTrue(1 == inf.getAdler())
  }

  @Test def needsInput(): Unit = {
    val inflate = new Inflater()
    assertTrue(inflate.needsInput())
    val byteArray = Array[Byte](2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9)
    inflate.setInput(byteArray)
    assertTrue(!inflate.needsInput())

    inflate.reset()

    val byteArrayEmpty = new Array[Byte](0)
    inflate.setInput(byteArrayEmpty)
    assertTrue(inflate.needsInput())
  }

  @Test def reset(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 'e', 'r', 't', 'y', '5')
    val outPutInf = new Array[Byte](100)
    var y = 0
    val inflate = new Inflater()
    while (!inflate.finished()) {
      if (inflate.needsInput()) {
        inflate.setInput(outPutBuff1)
      }
      y += inflate.inflate(outPutInf, y, outPutInf.length - y)
    }
    var i = 0
    while (i < byteArray.length) {
      assertTrue(byteArray(i) == outPutInf(i))
      i += 1
    }
    assertTrue(outPutInf(byteArray.length) == 0)

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
      assertTrue(byteArray(j) == outPutInf(j))
      j += 1
    }
    assertTrue(outPutInf(byteArray.length) == 0)
  }

  @Test def setInputByteArray(): Unit = {
    val byteArray = Array[Byte](2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9)
    val inflate = new Inflater()
    inflate.setInput(byteArray)
    assertTrue(inflate.getRemaining() != 0)
  }

  @Test def setInputByteArray2(): Unit = {
    val byteArray = Array[Byte](2, 3, 4, 't', 'y', 'u', 'e', 'w', 7, 6, 5, 9)
    var offSet = 6
    var length = 6
    val inflate = new Inflater()
    inflate.setInput(byteArray, offSet, length)
    assertTrue(length == inflate.getRemaining())

    // boundary check
    inflate.reset()
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      inflate.setInput(byteArray, 100, 100)
    )
  }

  @Test def getBytesRead(): Unit = {
    val defl = new Deflater()
    val infl = new Inflater()
    assertTrue(0 == defl.getTotalIn())
    assertTrue(0 == defl.getTotalOut())
    assertTrue(0 == defl.getBytesRead())
    // Encode a String into bytes
    val inputString = "blahblahblah??"
    val input = inputString.getBytes("UTF-8")

    // Compress the bytes
    val output = new Array[Byte](100)
    defl.setInput(input)
    defl.finish()
    defl.deflate(output)
    infl.setInput(output)
    val compressedDataLength = infl.inflate(input)
    assertTrue(16 == infl.getTotalIn())
    assertTrue(compressedDataLength == infl.getTotalOut())
    assertTrue(16 == infl.getBytesRead())
  }

  @Test def getBytesWritten(): Unit = {
    val defl = new Deflater()
    val infl = new Inflater()
    assertTrue(0 == defl.getTotalIn())
    assertTrue(0 == defl.getTotalOut())
    assertTrue(0 == defl.getBytesWritten())
    // Encode a String into bytes
    val inputString = "blahblahblah??"
    val input = inputString.getBytes("UTF-8")

    // Compress the bytes
    val output = new Array[Byte](100)
    defl.setInput(input)
    defl.finish()
    defl.deflate(output)
    infl.setInput(output)
    val compressedDataLength = infl.inflate(input)
    assertTrue(16 == infl.getTotalIn())
    assertTrue(compressedDataLength == infl.getTotalOut())
    assertTrue(14 == infl.getBytesWritten())
  }

  @Test def inflate(): Unit = {
    val inf = new Inflater()
    val res = inf.inflate(new Array[Byte](0), 0, 0)
    assertTrue(0 == res)

    var inflater = new Inflater()
    val b = new Array[Byte](1024)
    assertTrue(0 == inflater.inflate(b))
    inflater.end()

    // NOTE: Here we differ from Harmony, and follow the JVM.
    // Harmony throws a DataFormatException in `inflate` here.
    inflater = new Inflater()
    inflater.setInput(Array[Byte](-1))
    assertTrue(0 == inflater.inflate(b))
    assertTrue(inflater.needsInput())

    inflater = new Inflater()
    inflater.setInput(Array[Byte](-1, -1, -1))
    assertThrows(classOf[DataFormatException], inflater.inflate(b))
  }

  @Test def setDictionaryByteArray(): Unit = {
    var i = 0
    val inputString = "blah string contains blahblahblahblah and blah"
    val dictionary1 = "blah"
    val dictionary2 = "1234"

    val outputNo = new Array[Byte](100)
    val output1 = new Array[Byte](100)
    val output2 = new Array[Byte](100)
    val defDictNo = new Deflater(9)
    val defDict1 = new Deflater(9)
    val defDict2 = new Deflater(9)

    defDict1.setDictionary(dictionary1.getBytes())
    defDict2.setDictionary(dictionary2.getBytes())

    defDictNo.setInput(inputString.getBytes())
    defDict1.setInput(inputString.getBytes())
    defDict2.setInput(inputString.getBytes())

    defDictNo.finish()
    defDict1.finish()
    defDict2.finish()

    val dataLenNo = defDictNo.deflate(outputNo)
    val dataLen1 = defDict1.deflate(output1)
    val dataLen2 = defDict2.deflate(output2)

    var passNo1 = false
    var passNo2 = false
    var pass12 = false

    i = 0
    while (!passNo1 && i < (if (dataLenNo < dataLen1) dataLenNo
                            else dataLen1)) {
      if (outputNo(i) != output1(i)) {
        passNo1 = true
      }
      i += 1
    }

    var j = 0
    while (!passNo2 && j < (if (dataLenNo < dataLen1) dataLenNo
                            else dataLen2)) {
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

    assertTrue(passNo1)
    assertTrue(passNo2)
    assertTrue(pass12)

    var inflNo = new Inflater()
    var infl1 = new Inflater()
    var infl2 = new Inflater()

    val result = new Array[Byte](100)
    var decLen = 0

    inflNo.setInput(outputNo, 0, dataLenNo)
    decLen = inflNo.inflate(result)

    assertTrue(!inflNo.needsDictionary())
    inflNo.end()
    assertTrue(inputString == new String(result, 0, decLen))

    infl1.setInput(output1, 0, dataLen1)
    decLen = infl1.inflate(result)

    assertTrue(infl1.needsDictionary())
    infl1.setDictionary(dictionary1.getBytes())
    decLen = infl1.inflate(result)
    infl1.end()
    assertTrue(inputString == new String(result, 0, decLen))

    infl2.setInput(output2, 0, dataLen2)
    decLen = infl2.inflate(result)

    assertTrue(infl2.needsDictionary())
    infl2.setDictionary(dictionary2.getBytes())
    decLen = infl2.inflate(result)
    infl2.end()
    assertTrue(inputString == new String(result, 0, decLen))

    inflNo = new Inflater()
    infl1 = new Inflater()
    inflNo.setInput(outputNo, 0, dataLenNo)
    assertThrows(
      classOf[IllegalArgumentException],
      infl1.setDictionary(dictionary1.getBytes())
    )
    inflNo.end()

    infl1.setInput(output1, 0, dataLen1)
    decLen = infl1.inflate(result)

    assertTrue(infl1.needsDictionary())
    assertThrows(
      classOf[IllegalArgumentException],
      infl1.setDictionary(dictionary2.getBytes())
    )
    infl1.end()
    assertThrows(
      classOfIllegalStateException,
      infl1.setDictionary(dictionary2.getBytes())
    )
  }

  @Test def exceptions(): Unit = {
    val byteArray = Array[Byte](5, 2, 3, 7, 8)

    val r = 0;
    val inflate = new Inflater()
    inflate.setInput(byteArray)
    inflate.end()

    assertThrows(classOfIllegalStateException, inflate.getAdler())

    assertThrows(classOfIllegalStateException, inflate.getBytesRead())

    assertThrows(classOfIllegalStateException, inflate.getBytesWritten())

    assertThrows(classOfIllegalStateException, inflate.getTotalIn())

    assertThrows(classOfIllegalStateException, inflate.getTotalOut())
  }
}
