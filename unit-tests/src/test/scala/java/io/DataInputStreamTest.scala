package java.io

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class DataInputStreamTest {

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

}
