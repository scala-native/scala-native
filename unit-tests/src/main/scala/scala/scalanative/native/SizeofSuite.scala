package scala.scalanative.native

object SizeofSuite extends tests.Suite {

  test("Byte") {
    assert(sizeof[Byte] == 1)
  }

  test("UByte") {
    assert(sizeof[UByte] == 1)
  }

  test("Short") {
    assert(sizeof[Short] == 2)
  }

  test("UShort") {
    assert(sizeof[UShort] == 2)
  }

  test("Int") {
    assert(sizeof[Int] == 4)
  }

  test("UInt") {
    assert(sizeof[UInt] == 4)
  }

  test("Long") {
    assert(sizeof[Long] == 8)
  }

  test("ULong") {
    assert(sizeof[ULong] == 8)
  }

  test("Ptr") {
    assert(sizeof[Ptr[_]] == 8)
  }

  test("CStruct1[Byte]") {
    assert(sizeof[CStruct1[Byte]] == 1)
  }

  test("CStruct2[Byte, Byte]") {
    assert(sizeof[CStruct2[Byte, Byte]] == 2)
  }

  test("CStruct2[Byte, Int]") {
    assert(sizeof[CStruct2[Byte, Int]] == 8)
  }
}
