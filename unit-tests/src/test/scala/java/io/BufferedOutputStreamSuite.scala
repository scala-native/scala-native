package java.io

object BufferedOutputStreamSuite extends tests.Suite {

  test("creating a buffer of negative size throws IllegalArgumentException") {
    assertThrows[IllegalArgumentException] {
      val out = new BufferedOutputStream(new ByteArrayOutputStream(), -1)
    }
  }

  test("write to closed Buffer throws IOException") {

    val out = new BufferedOutputStream(new ByteArrayOutputStream())

    out.close()

    assertThrows[java.io.IOException](out.write(1))

  }

  test("simple write") {

    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    out.write(0)
    out.write(1)
    out.write(2)

    out.flush()

    val ans = arrayOut.toByteArray

    assert(ans(0) == 0 && ans(1) == 1 && ans(2) == 2)
  }

  test("write without flush does nothing") {
    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    out.write(0)
    out.write(1)
    out.write(2)

    assert(arrayOut.toByteArray.isEmpty)
  }

  test("simple write Array") {

    val array = List(0, 1, 2).map(_.toByte).toArray[Byte]

    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    out.write(array, 0, 3)

    out.flush()

    val ans = arrayOut.toByteArray
    assert(ans(0) == 0 && ans(1) == 1 && ans(2) == 2)

  }

  test("write array with bad index or length throw exceptions") {

    val array = List(0, 1, 2).map(_.toByte).toArray[Byte]

    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    assertThrows[IndexOutOfBoundsException] {
      out.write(array, 0, 4)
    }

    assertThrows[IndexOutOfBoundsException] {
      out.write(array, 4, 3)
    }

    assertThrows[IndexOutOfBoundsException] {
      out.write(array, -1, 3)
    }

    assertThrows[IndexOutOfBoundsException] {
      out.write(array, 4, -1)
    }

  }

}
