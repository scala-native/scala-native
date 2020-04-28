package java.io

object BufferedInputStreamSuite extends tests.Suite {

  val exampleBytes0 =
    List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

  test("creating a buffer of negative size throws IllegalArgumentException") {
    assertThrows[IllegalArgumentException] {

      val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

      new BufferedInputStream(arrayIn, -1)
    }
  }

  test("simple reads") {

    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    assert(in.read() == 0)

    assert(in.read() == 1)

    assert(in.read() == 2)
  }

  test("simple array reads") {

    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    val a = new Array[Byte](7)

    assert(in.skip(3) == 3)
    assert(in.read(a, 0, 7) == 7)

    assert(
      a(0) == 3 && a(1) == 4 && a(2) == 5 && a(3) == 6 && a(4) == 7 && a(5) == 8 && a(
        6) == 9)

  }

  test("read to closed buffer throws IOException") {

    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

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

    assertThrows[java.lang.IndexOutOfBoundsException] { in.read(a, 0, 11) }
  }

  test(
    "read into array behaves correctly when asking more elements than are in the buffer") {

    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    // start with buffer of size 2 to force multiple refills of the buffer
    val in = new BufferedInputStream(arrayIn, 2)

    val a = new Array[Byte](10)

    in.read(a, 0, 10)
    assert(a.toSeq == exampleBytes0.toSeq)
  }

  /* interestingly... failing is not technically required according to the InputStream or
   *  BufferedInputStream spec.
   */
  test("reset throws IOException if no prior mark") {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    in.read()
    in.read()
    in.read()

    assertThrows[IOException](in.reset())
  }

  test("reset moves position to mark - no buffer resize") {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn, 10)
    in.mark(Int.MaxValue)

    assert(in.read() == 0)
    assert(in.read() == 1)
    assert(in.read() == 2)

    in.reset()

    assert(in.read() == 0)
    assert(in.read() == 1)
    assert(in.read() == 2)
  }

  test("reset moves position to mark - with buffer resize") {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn, 2)
    in.mark(Int.MaxValue)

    assert(in.read() == 0)
    assert(in.read() == 1)
    assert(in.read() == 2)

    in.reset()

    assert(in.read() == 0)
    assert(in.read() == 1)
    assert(in.read() == 2)
  }

  // there is no requirement in the spec that the mark is invalidated
  // exactly when read limit bytes exceeded: the exception "might be thrown"
  test("mark is invalidated after read limit bytes and buffer exceeded") {
    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn, 2)

    in.mark(2)

    assert(in.read() == 0)
    assert(in.read() == 1)

    in.reset()

    // read enough to definitely invalidate mark
    for (_ <- 0 until 5) in.read()

    assertThrows[IOException](in.reset())
  }

  test("available after close throws IOException") {

    val arrayIn = new ByteArrayInputStream(exampleBytes0, 0, 10)

    val in = new BufferedInputStream(arrayIn)

    in.close()
    assertThrows[IOException](in.available())
  }

  test("available behaves correctly") {

    val in = new BufferedInputStream(new ByteArrayInputStream(exampleBytes0))

    assert(in.available() > 0)

    val tmp        = new Array[Byte](10)
    var countBytes = 0
    while (in.available() > 0) {
      countBytes += in.read(tmp, 0, 5)
    }

    assert(countBytes == 10)
    assert(in.available() == 0)

    val emptyIn = new BufferedInputStream(new ByteArrayInputStream(Array()))
    assert(emptyIn.available() == 0)
  }

}
