package scala

object DivisionByZeroSuite extends tests.Suite {
  @noinline def byte1  = 1.toByte
  @noinline def char1  = 1.toChar
  @noinline def short1 = 1.toShort
  @noinline def int1   = 1
  @noinline def long1  = 1L
  @noinline def byte0  = 0.toByte
  @noinline def char0  = 0.toChar
  @noinline def short0 = 0.toShort
  @noinline def int0   = 0
  @noinline def long0  = 0L

  test("byte / zero") {
    assertThrows[ArithmeticException](byte1 / byte0)
    assertThrows[ArithmeticException](byte1 / short0)
    assertThrows[ArithmeticException](byte1 / char0)
    assertThrows[ArithmeticException](byte1 / int0)
    assertThrows[ArithmeticException](byte1 / long0)
  }

  test("byte % zero") {
    assertThrows[ArithmeticException](byte1 / byte0)
    assertThrows[ArithmeticException](byte1 / short0)
    assertThrows[ArithmeticException](byte1 / char0)
    assertThrows[ArithmeticException](byte1 / int0)
    assertThrows[ArithmeticException](byte1 / long0)
  }

  test("short / zero") {
    assertThrows[ArithmeticException](short1 / byte0)
    assertThrows[ArithmeticException](short1 / short0)
    assertThrows[ArithmeticException](short1 / char0)
    assertThrows[ArithmeticException](short1 / int0)
    assertThrows[ArithmeticException](short1 / long0)
  }

  test("short % zero") {
    assertThrows[ArithmeticException](short1 / byte0)
    assertThrows[ArithmeticException](short1 / short0)
    assertThrows[ArithmeticException](short1 / char0)
    assertThrows[ArithmeticException](short1 / int0)
    assertThrows[ArithmeticException](short1 / long0)
  }

  test("char / zero") {
    assertThrows[ArithmeticException](char1 / byte0)
    assertThrows[ArithmeticException](char1 / short0)
    assertThrows[ArithmeticException](char1 / char0)
    assertThrows[ArithmeticException](char1 / int0)
    assertThrows[ArithmeticException](char1 / long0)
  }

  test("char % zero") {
    assertThrows[ArithmeticException](char1 / byte0)
    assertThrows[ArithmeticException](char1 / short0)
    assertThrows[ArithmeticException](char1 / char0)
    assertThrows[ArithmeticException](char1 / int0)
    assertThrows[ArithmeticException](char1 / long0)
  }

  test("int / zero") {
    assertThrows[ArithmeticException](int1 / byte0)
    assertThrows[ArithmeticException](int1 / short0)
    assertThrows[ArithmeticException](int1 / char0)
    assertThrows[ArithmeticException](int1 / int0)
    assertThrows[ArithmeticException](int1 / long0)
  }

  test("int % zero") {
    assertThrows[ArithmeticException](int1 / byte0)
    assertThrows[ArithmeticException](int1 / short0)
    assertThrows[ArithmeticException](int1 / char0)
    assertThrows[ArithmeticException](int1 / int0)
    assertThrows[ArithmeticException](int1 / long0)
  }

  test("long / zero") {
    assertThrows[ArithmeticException](long1 / byte0)
    assertThrows[ArithmeticException](long1 / short0)
    assertThrows[ArithmeticException](long1 / char0)
    assertThrows[ArithmeticException](long1 / int0)
    assertThrows[ArithmeticException](long1 / long0)
  }

  test("long % zero") {
    assertThrows[ArithmeticException](long1 / byte0)
    assertThrows[ArithmeticException](long1 / short0)
    assertThrows[ArithmeticException](long1 / char0)
    assertThrows[ArithmeticException](long1 / int0)
    assertThrows[ArithmeticException](long1 / long0)
  }
}
