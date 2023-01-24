package org.scalanative.testsuite.javalib.util.zip

import java.util.zip._
import java.io.ByteArrayOutputStream

import org.junit.Test
import org.junit.Assert._

class DeflaterTest {

  @Test def deflaterSetInputDoesNotThrowAnException(): Unit = {
    val deflater = new Deflater()
    val bytes = Array[Byte](1, 2, 3)
    deflater.setInput(bytes, 0, 3)
  }

  @Test def deflaterNeedsInputRightAfterBeingCreated(): Unit = {
    val deflater = new Deflater()
    assertTrue(deflater.needsInput())
  }

  @Test def deflaterDoesNotNeedInputAfterInputHasBeenSet(): Unit = {
    val deflater = new Deflater()
    val bytes = Array[Byte](1, 2, 3)
    assertTrue(deflater.needsInput())
    deflater.setInput(bytes)
    assertTrue(!deflater.needsInput())
  }

  @Test def deflaterCanDeflateByteArraysWithDefaultCompressionLevel(): Unit = {
    val bytes = Array.fill[Byte](1024)(1)
    val expected = Array[Byte](120, -100, 99, 100, 28, 5, -93, 96, 20, -116, 84,
      0, 0, 6, 120, 4, 1)
    val deflater = new Deflater()
    val bos = new ByteArrayOutputStream()
    deflater.setInput(bytes)
    deflater.finish()

    val buf = new Array[Byte](1024)
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      bos.write(buf, 0, count)
    }
    val compressed = bos.toByteArray()

    assertTrue(compressed.length == expected.length)
    compressed.zip(expected).foreach {
      case (a, b) => assertTrue(a == b)
    }
  }

  @Test def deflaterCanDeflateWithBestCompressionLevel(): Unit = {
    val bytes = Array.fill[Byte](1024)(1)
    val expected = Array[Byte](120, -38, 99, 100, 28, 5, -93, 96, 20, -116, 84,
      0, 0, 6, 120, 4, 1)
    val deflater = new Deflater(Deflater.BEST_COMPRESSION)
    val bos = new ByteArrayOutputStream()
    deflater.setInput(bytes)
    deflater.finish()

    val buf = new Array[Byte](1024)
    var h = 0
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val compressed = bos.toByteArray()

    assertTrue(compressed.length == expected.length)
    compressed.zip(expected).foreach {
      case (a, b) => assertTrue(a == b)
    }
  }

  @Test def deflaterCanDeflateGivenBufferSmallerThanTotalAmountOfData()
      : Unit = {
    val bytes = Array.fill[Byte](1024)(1)
    val expected = Array[Byte](120, -100, 99, 100, 28, 5, -93, 96, 20, -116, 84,
      0, 0, 6, 120, 4, 1)
    val deflater = new Deflater()
    val bos = new ByteArrayOutputStream()
    deflater.setInput(bytes)
    deflater.finish()

    val buf = new Array[Byte](5)
    var h = 0
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val compressed = bos.toByteArray()

    assertTrue(compressed.length == expected.length)
    compressed.zip(expected).foreach {
      case (a, b) => assertTrue(a == b)
    }
  }
}
