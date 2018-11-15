package scala

object FloatingToIntegerOverflowSuite extends tests.Suite {
  @noinline def tooSmallFloatToInt   = java.lang.Integer.MIN_VALUE.toFloat - 42
  @noinline def tooSmallDoubleToLong = java.lang.Long.MIN_VALUE.toDouble - 42
  @noinline def tooBigFloatToInt     = java.lang.Integer.MAX_VALUE.toFloat + 42
  @noinline def tooBigDoubleToLong   = java.lang.Long.MAX_VALUE.toDouble + 42
  @noinline def floatNaN             = java.lang.Float.NaN
  @noinline def doubleNaN            = java.lang.Double.NaN

  test("nan float to int") {
    assert(floatNaN.toInt == 0)
  }

  test("nan float to long") {
    assert(floatNaN.toLong == 0L)
  }

  test("nan double to int") {
    assert(doubleNaN.toInt == 0)
  }

  test("nan double to long") {
    assert(doubleNaN.toLong == 0L)
  }

  test("float too small to fit in int") {
    assert(tooSmallFloatToInt.toInt == java.lang.Integer.MIN_VALUE)
  }

  test("float too small to fit in long") {
    assert(tooSmallDoubleToLong.toLong == java.lang.Long.MIN_VALUE)
  }

  test("float too big to fit in int") {
    assert(tooBigFloatToInt.toInt == java.lang.Integer.MAX_VALUE)
  }

  test("float too small to fit in long") {
    assert(tooBigDoubleToLong.toLong == java.lang.Long.MAX_VALUE)
  }
}
