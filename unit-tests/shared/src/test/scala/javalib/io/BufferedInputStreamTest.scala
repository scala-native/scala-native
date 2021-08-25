package javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class BufferedInputStreamTest {

  @Test def bufferOfNegativeSizeThrowsIllegalArgumentException(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException], {
        val inputArray =
          List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
        val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)
        val in = new BufferedInputStream(arrayIn, -1)
      }
    )
  }

  @Test def simpleReads(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    assertTrue(in.read() == 0)
    assertTrue(in.read() == 1)
    assertTrue(in.read() == 2)

    val a = new Array[Byte](7)
    assertTrue(in.read(a, 0, 7) == 7)
    assertTrue(
      a(0) == 3 && a(1) == 4 && a(2) == 5 &&
        a(3) == 6 && a(4) == 7 && a(5) == 8 && a(6) == 9
    )
  }

  @Test def readValueBiggerThan0x7f(): Unit = {
    val inputArray = Array[Byte](0x85.toByte)
    val arrayIn = new ByteArrayInputStream(inputArray)
    val in = new BufferedInputStream(arrayIn)
    assertTrue(in.read() == 0x85)
    assertTrue(in.read() == -1)
  }

  @Test def readToClosedBufferThrowsIOException(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    in.close()
    assertThrows(classOf[java.io.IOException], in.read())
    assertThrows(classOf[java.io.IOException], in.read())
    assertThrows(classOf[java.io.IOException], in.read())
  }

  @Test def readIntoArrayWithBadIndexOrLengthThrowsException(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    val a = new Array[Byte](10)
    assertThrows(classOf[java.lang.IndexOutOfBoundsException], in.read(a, 8, 7))
    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      in.read(a, 0, -1)
    )
    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      in.read(a, -1, 7)
    )
  }

  @Test def readArrayBehavesCorrectlyWhenAskingForElementsInBuffer(): Unit = {
    val inputArray =
      List(0, 1, 2).map(_.toByte).toArray[Byte]
    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    val a = new Array[Byte](10)
    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      in.read(a, 0, 10)
    )
  }

  @Test def markAndResetBehaveCorrectly(): Unit = {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    in.read()
    in.read()
    in.read()
    assertThrows(classOf[IOException], in.reset())

    in.mark(3)
    assertTrue(in.read() == 3)
    assertTrue(in.read() == 4)
    assertTrue(in.read() == 5)

    in.reset()
    assertTrue(in.read() == 3)
    assertTrue(in.read() == 4)
    assertTrue(in.read() == 5)
    assertTrue(in.read() == 6)
    assertTrue(in.read() == 7)
    assertTrue(in.read() == 8)
    assertTrue(in.read() == 9)
  }

  @Test def availableBehaveCorrectly(): Unit = {
    val inputArray = Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte)
    val in = new BufferedInputStream(new ByteArrayInputStream(inputArray))
    assertTrue(in.available() > 0)

    val tmp = new Array[Byte](10)
    var countBytes = 0
    while (in.available() > 0) {
      countBytes += in.read(tmp, 0, 5)
    }
    assertTrue(countBytes == 10)
    assertTrue(in.available() == 0)

    val emptyIn = new BufferedInputStream(new ByteArrayInputStream(Array()))
    assertTrue(emptyIn.available() == 0)
  }
}
