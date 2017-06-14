package java.util.zip

// Ported from Apache Harmony

object Adler32Suite extends tests.Suite {

  test("Constructor") {
    val adl = new Adler32()
    assert(adl.getValue() == 1)
  }

  test("getValue()") {
    val adl = new Adler32()
    assert(adl.getValue() == 1)

    adl.reset()
    adl.update(1)
    assert(adl.getValue() == 131074)
    adl.reset()
    assert(adl.getValue() == 1)

    adl.reset()
    adl.update(Int.MinValue)
    assert(adl.getValue() == 65537L)
  }

  test("reset()") {
    val adl = new Adler32()
    adl.update(1)
    assert(adl.getValue() == 131074)
    adl.reset()
    assert(adl.getValue() == 1)
  }

  test("update(Int)") {
    val adl = new Adler32()
    adl.update(1)
    assert(adl.getValue() == 131074)

    adl.reset()
    adl.update(Int.MaxValue)
    assert(adl.getValue() == 16777472L)

    adl.reset()
    adl.update(Int.MinValue)
    assert(adl.getValue() == 65537L)
  }

  test("update(Array[Byte])") {
    val byteArray = Array[Byte](1, 2)
    val adl       = new Adler32()
    adl.update(byteArray)
    assert(adl.getValue() == 393220)

    adl.reset()
    val byteEmpty = new Array[Byte](10000)
    adl.update(byteEmpty)
    assert(adl.getValue() == 655360001L)
  }

  test("updateArray(Byte, Int, Int)") {
    val byteArray = Array[Byte](1, 2, 3)
    val adl       = new Adler32()
    val off       = 2
    val len       = 1
    val lenError  = 3
    val offError  = 4
    adl.update(byteArray, off, len)
    assert(adl.getValue() == 262148)

    assertThrows[ArrayIndexOutOfBoundsException] {
      adl.update(byteArray, off, lenError)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      adl.update(byteArray, offError, len)
    }
  }

}
