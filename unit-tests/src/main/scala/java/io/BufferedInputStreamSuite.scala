package java.io

object BufferedInputStreamSuite extends tests.Suite {

  test("creating a buffer of negative size throws IllegalArgumentException") {
    assertThrows[IllegalArgumentException] {
      val inputArray =
        List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

      val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)

      val in = new BufferedInputStream(arrayIn, -1)
    }
  }

  test("simple reads") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    assert(in.read() == 0)

    assert(in.read() == 1)

    assert(in.read() == 2)

    val a = new Array[Byte](7)

    assert(in.read(a, 0, 7) == 7)

    assert(a(0) == 3 && a(1) == 4 && a(2) == 5 && a(3) == 6 && a(4) == 7 && a(
      5) == 8 && a(6) == 9)

  }

  test("read to closed buffer throws IOException") {

    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    in.close()

    assertThrows[java.io.IOException] { in.read() }

    assertThrows[java.io.IOException] { in.read() }

    assertThrows[java.io.IOException] { in.read() }
  }

  test("read into array with bad index or length throw exceptions") {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    val a = new Array[Byte](10)

    assertThrows[java.lang.IndexOutOfBoundsException] { in.read(a, 8, 7) }

    assertThrows[java.lang.IndexOutOfBoundsException] { in.read(a, 0, -1) }

    assertThrows[java.lang.IndexOutOfBoundsException] { in.read(a, -1, 7) }

  }

  test(
    "read into array behaves correctly when asking more elements that are in the buffer") {
    val inputArray =
      List(0, 1, 2).map(_.toByte).toArray[Byte]

    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    val a = new Array[Byte](10)

    assertThrows[java.lang.IndexOutOfBoundsException] { in.read(a, 0, 10) }

  }

  test("mark and reset behave correctly") {
    val inputArray =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

    val arrayIn = new ByteArrayInputStream(inputArray, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    in.read()
    in.read()
    in.read()

    assertThrows[IOException](in.reset())

    in.mark(3)

    assert(in.read() == 3)
    assert(in.read() == 4)
    assert(in.read() == 5)

    in.reset()

    assert(in.read() == 3)
    assert(in.read() == 4)
    assert(in.read() == 5)
    assert(in.read() == 6)
    assert(in.read() == 7)
    assert(in.read() == 8)
    assert(in.read() == 9)

  }

}
