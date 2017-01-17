package java.lang

object MathSuite extends tests.Suite {
  test("max") {
    val a = 123.123d
    val b = 456.456d
    assert(Math.max(a, b) == b)
    assert(Math.max(a.toFloat, b.toFloat) == b.toFloat)
    assert(Math.max(a.toInt, b.toInt) == b.toInt)
    assert(Math.max(a.toLong, b.toLong) == b.toLong)
  }

  test("min") {
    val a = 123.123d
    val b = 456.456d
    assert(Math.min(a, b) == a)
    assert(Math.min(a.toFloat, b.toFloat) == a.toFloat)
    assert(Math.min(a.toInt, b.toInt) == a.toInt)
    assert(Math.min(a.toLong, b.toLong) == a.toLong)
  }
}
