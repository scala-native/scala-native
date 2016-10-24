package scala.scalanative.native

object UnsignedSizeOfSuite extends tests.Suite {

  test("UByte's size should be 1") {
    assert(sizeof[UByte] == 1)
  }

  test("UShort's size should be 2") {
    assert(sizeof[UShort] == 2)
  }

  test("UInt's size should be 4") {
    assert(sizeof[UInt] == 4)
  }

  test("ULong's size should be 8") {
    assert(sizeof[ULong] == 8)
  }

}
