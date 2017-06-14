package java.util.zip

import java.io.ByteArrayOutputStream

object DeflaterSuite extends tests.Suite {
  test("Deflater.setInput doesn't throw an exception") {
    val deflater = new Deflater()
    val bytes    = Array[Byte](1, 2, 3)
    deflater.setInput(bytes, 0, 3)
  }

  test("Deflater needs input right after being created") {
    val deflater = new Deflater()
    assert(deflater.needsInput())
  }

  test("Deflater doesn't need input after input has been set") {
    val deflater = new Deflater()
    val bytes    = Array[Byte](1, 2, 3)
    assert(deflater.needsInput())
    deflater.setInput(bytes)
    assert(!deflater.needsInput())
  }

  test("Deflater can deflate byte arrays with default compression level") {
    val bytes = Array.fill[Byte](1024)(1)
    val expected = Array[Byte](120, -100, 99, 100, 28, 5, -93, 96, 20, -116,
      84, 0, 0, 6, 120, 4, 1)
    val deflater = new Deflater()
    val bos      = new ByteArrayOutputStream()
    deflater.setInput(bytes)
    deflater.finish()

    val buf = new Array[Byte](1024)
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      bos.write(buf, 0, count)
    }
    val compressed = bos.toByteArray()

    assert(compressed.length == expected.length)
    compressed.zip(expected).foreach {
      case (a, b) => assert(a == b)
    }
  }

  test("Deflater can deflate with best compression level") {
    val bytes = Array.fill[Byte](1024)(1)
    val expected = Array[Byte](120, -38, 99, 100, 28, 5, -93, 96, 20, -116, 84,
      0, 0, 6, 120, 4, 1)
    val deflater = new Deflater(Deflater.BEST_COMPRESSION)
    val bos      = new ByteArrayOutputStream()
    deflater.setInput(bytes)
    deflater.finish()

    val buf = new Array[Byte](1024)
    var h   = 0
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val compressed = bos.toByteArray()

    assert(compressed.length == expected.length)
    compressed.zip(expected).foreach {
      case (a, b) => assert(a == b)
    }
  }

  test("Deflater can deflate given a buffer smaller than total amount of data") {
    val bytes = Array.fill[Byte](1024)(1)
    val expected = Array[Byte](120, -100, 99, 100, 28, 5, -93, 96, 20, -116,
      84, 0, 0, 6, 120, 4, 1)
    val deflater = new Deflater()
    val bos      = new ByteArrayOutputStream()
    deflater.setInput(bytes)
    deflater.finish()

    val buf = new Array[Byte](5)
    var h   = 0
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      bos.write(buf, 0, count)
      h += 1
    }
    val compressed = bos.toByteArray()

    assert(compressed.length == expected.length)
    compressed.zip(expected).foreach {
      case (a, b) => assert(a == b)
    }
  }
}
