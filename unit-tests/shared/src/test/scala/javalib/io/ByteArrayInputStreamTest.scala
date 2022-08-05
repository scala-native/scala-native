package javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Test

class ByteArrayInputStreamTest {
  private val example =
    new ByteArrayInputStream(
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]
    )

  @Test def readsNBytesCorrectly(): Unit = {
    val res1 = example.readNBytes(4)
    assertEquals(res1, Array[Byte](0, 1, 2, 3))

    val res2 = example.readNBytes(1000)
    assertEquals(res1, Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
  }

  @Test def readsNBytesCorrectlyWithBufferArguments(): Unit = {
    val b1 = Array.ofDim[Byte](7)
    val n1 = example.readNBytes(b1, 1, 7)
    assertEquals(n1, 7)
    assertEquals(b1, Array[Byte](1, 2, 3, 4, 5, 6, 7))

    val b2 = Array.ofDim[Byte](7)
    val n2 = example.readNBytes(b2, 150, 7)
    assertEquals(n2, 0)
    assertEquals(b2, Array(0, 0, 0, 0, 0, 0, 0))

    val b3 = Array.ofDim[Byte](7)
    val n3 = example.readNBytes(b3, 7, 5)
    assertEquals(n3, 3)
    assertEquals(b3, Array(7, 8, 9, 0, 0, 0, 0))
  }
}
