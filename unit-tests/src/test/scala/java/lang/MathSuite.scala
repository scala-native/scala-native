package java.lang

object MathSuite extends tests.Suite {

  // This method can/will be removed when/if pull request #1305 is merged.
  // Until then, this allows tests to be written as though #1305 were
  // effective yet still pass Travis CI until then.
  private def assert(cond: Boolean, message: String): Unit =
    assert(cond)

  // Max()

  test("max with NaN arguments") {
    val a = 123.123d
    val b = 456.456d

    assert(Math.max(Double.NaN, b).isNaN, "Double.NaN as first argument")
    assert(Math.max(a, Double.NaN).isNaN, "Double.NaN as second argument")

    assert(Math.max(Float.NaN, b.toFloat).isNaN, "Float.NaN as first argument")
    assert(Math.max(a, Float.NaN).isNaN, "Float.NaN as second argument")
  }

  test("max a > b") {
    val a = 578.910d
    val b = 456.456d
    assert(Math.max(a, b) == a, "Double")
    assert(Math.max(a.toFloat, b.toFloat) == a.toFloat, "Float")
    assert(Math.max(a.toInt, b.toInt) == a.toInt, "Int")
    assert(Math.max(a.toLong, b.toLong) == a.toLong, "Long")
  }

  test("max a == b") {
    val a = 576528
    val b = a
    assert(Math.max(a, b) == a, "Double")
    assert(Math.max(a.toFloat, b.toFloat) == a.toFloat, "Float")
    assert(Math.max(a.toInt, b.toInt) == a.toInt, "Int")
    assert(Math.max(a.toLong, b.toLong) == a.toLong, "Long")
  }

  test("max a < b") {
    val a = 123.123d
    val b = 456.456d
    assert(Math.max(a, b) == b, "Double")
    assert(Math.max(a.toFloat, b.toFloat) == b.toFloat, "Float")
    assert(Math.max(a.toInt, b.toInt) == b.toInt, "Int")
    assert(Math.max(a.toLong, b.toLong) == b.toLong, "Long")
  }

  // Min()

  test("min with NaN arguments") {
    val a = 773.211d
    val b = 843.531d

    assert(Math.max(Double.NaN, b).isNaN, "Double.NaN as first argument")
    assert(Math.max(a, Double.NaN).isNaN, "Double.NaN as second argument")

    assert(Math.max(Float.NaN, b.toFloat).isNaN, "Float.NaN as first argument")
    assert(Math.max(a, Float.NaN).isNaN, "Float.NaN as second argument")
  }

  test("min a > b") {
    val a = 949.538d
    val b = 233.411d
    assert(Math.min(a, b) == b, "Double")
    assert(Math.min(a.toFloat, b.toFloat) == b.toFloat, "Float")
    assert(Math.min(a.toInt, b.toInt) == b.toInt, "Int")
    assert(Math.min(a.toLong, b.toLong) == b.toLong, "Long")
  }

  test("min a == b") {
    val a = 553.838d
    val b = a
    assert(Math.min(a, b) == b, "Double")
    assert(Math.min(a.toFloat, b.toFloat) == b.toFloat, "Float")
    assert(Math.min(a.toInt, b.toInt) == b.toInt, "Int")
    assert(Math.min(a.toLong, b.toLong) == b.toLong, "Long")
  }

  test("min a < b") {
    val a = 312.966d
    val b = 645.521d
    assert(Math.min(a, b) == a, "Double")
    assert(Math.min(a.toFloat, b.toFloat) == a.toFloat, "Float")
    assert(Math.min(a.toInt, b.toInt) == a.toInt, "Int")
    assert(Math.min(a.toLong, b.toLong) == a.toLong, "Long")
  }

}
