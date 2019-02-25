package scala

object DivisionOverflowSuite extends tests.Suite {
  @noinline def intMinus1  = -1
  @noinline def longMinus1 = -1L

  test("Integer.MIN_VALUE / -1") {
    assert(
      (java.lang.Integer.MIN_VALUE / intMinus1) == java.lang.Integer.MIN_VALUE)
  }

  test("Integer.MIN_VALUE % -1") {
    assert((java.lang.Integer.MIN_VALUE % intMinus1) == 0)
  }

  test("Long.MIN_VALUE / -1") {
    assert((java.lang.Long.MIN_VALUE / longMinus1) == java.lang.Long.MIN_VALUE)
  }

  test("Long.MIN_VALUE % -1") {
    assert((java.lang.Long.MIN_VALUE % longMinus1) == 0)
  }
}
