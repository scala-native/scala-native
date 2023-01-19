package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scalanative.junit.utils.AssumesHelper._

class BufferedInputStreamTest {
  private val exampleBytes0 =
    List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

  @Test def bufferOfNegativeSizeThrowsIllegalArgumentException(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException], {
        val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
        val in = new BufferedInputStream(arrayIn, -1)
      }
    )
  }

  @Test def simpleReads(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
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

  @Test def simpleArrayRead(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    val a = new Array[Byte](7)

    assertEquals(3, in.skip(3))
    assertEquals(7, in.read(a, 0, 7))

    assertEquals(3, a(0))
    assertEquals(4, a(1))
    assertEquals(5, a(2))
    assertEquals(6, a(3))
    assertEquals(7, a(4))
    assertEquals(8, a(5))
    assertEquals(9, a(6))
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
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
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
    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      in.read(a, 0, 11)
    )
  }

  @Test def readArrayBehavesCorrectlyWhenAskingForElementsInBuffer(): Unit = {
    assumeNotJVMCompliant()
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    // start with buffer of size 2 to force multiple refills of the buffer
    val in = new BufferedInputStream(arrayIn, 2)
    val a = new Array[Byte](10)
    in.read(a, 0, 10)
    assertArrayEquals(exampleBytes0, a)
  }

  /* interestingly... failing is not technically required according to the
   * InputStream or BufferedInputStream spec.
   */
  @Test def resetThrowsIOExceptionIfNoPriorMark(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    val in = new BufferedInputStream(arrayIn)
    in.read()
    in.read()
    in.read()

    assertThrows(classOf[IOException], in.reset())
  }

  @Test def resetMovesPositionToMark_NoBufferResize(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    val in = new BufferedInputStream(arrayIn, 10)
    in.mark(Int.MaxValue)

    assertEquals(0, in.read())
    assertEquals(1, in.read())
    assertEquals(2, in.read())

    in.reset()

    assertEquals(0, in.read())
    assertEquals(1, in.read())
    assertEquals(2, in.read())
  }

  @Test def resetMovesPositionToMark_WithBufferResize(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    val in = new BufferedInputStream(arrayIn, 2)
    in.mark(Int.MaxValue)

    assertEquals(0, in.read())
    assertEquals(1, in.read())
    assertEquals(2, in.read())

    in.reset()

    assertEquals(0, in.read())
    assertEquals(1, in.read())
    assertEquals(2, in.read())
  }

  // there is no requirement in the spec that the mark is invalidated
  // exactly when read limit bytes exceeded: the exception "might be thrown"
  @Test def markIsInvalidatedAfterReadLimitBytesAndBufferExceeded(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    val in = new BufferedInputStream(arrayIn, 2)

    in.mark(2)

    assertEquals(0, in.read())
    assertEquals(1, in.read())

    in.reset()

    // read enough to definitely invalidate mark
    for (_ <- 0 until 5) in.read()

    assertThrows(classOf[IOException], in.reset())
  }

  @Test def availableAfterCloseThrowsIOException(): Unit = {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)
    val in = new BufferedInputStream(arrayIn)

    in.close()
    assertThrows(classOf[IOException], in.available())
  }

  @Test def availableBehaveCorrectly(): Unit = {
    val in = new BufferedInputStream(new ByteArrayInputStream(exampleBytes0))
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
