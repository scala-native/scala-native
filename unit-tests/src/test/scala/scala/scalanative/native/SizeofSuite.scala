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

  test("CStruct3[Byte, Short, Byte]") {
    assert(sizeof[CStruct3[Byte, Short, Byte]] == 6)
  }

  test("CStruct4[Byte, Short, Byte, Int]") {
    assert(sizeof[CStruct4[Byte, Short, Byte, Int]] == 12)
  }

  test("inner struct CStruct2[Byte, CStruct2[Long, Byte]]") {
    assert(sizeof[CStruct2[Byte, CStruct2[Long, Byte]]] == 24)
  }

  test("inner struct CStruct3[Byte, Long, CStruct3[Int, Int, Byte]]") {
    assert(sizeof[CStruct3[Byte, Long, CStruct3[Int, Int, Byte]]] == 32)
  }

  test(
    "inner struct CStruct3[Byte, Long, CStruct3[Int, Int, CStruct4[Byte, Int, Short, Byte]]") {
    assert(
      sizeof[
        CStruct3[Byte,
                 Long,
                 CStruct3[Int, Int, CStruct4[Byte, Int, Short, Byte]]]] == 40)
  }

  type _32   = Nat.Digit[Nat._3, Nat._2]
  type _128  = Nat.Digit[Nat._1, Nat.Digit[Nat._2, Nat._8]]
  type _1024 = Nat.Digit[Nat._1, Nat.Digit[Nat._0, Nat.Digit[Nat._2, Nat._4]]]

  test("CArray[Byte, _32]") {
    assert(sizeof[CArray[Byte, _32]] == 32)
  }

  test("CArray[Byte, _128]") {
    assert(sizeof[CArray[Byte, _128]] == 128)
  }

  test("CArray[Byte, _1024]") {
    assert(sizeof[CArray[Byte, _1024]] == 1024)
  }

  test("CArray[CStruct3[Byte, Int, Byte], _32]") {
    assert(sizeof[CArray[CStruct3[Byte, Int, Byte], _32]] == 12 * 32)
  }
}
