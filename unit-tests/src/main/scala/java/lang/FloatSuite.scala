package java.lang

object FloatSuite extends tests.Suite {
  test("parseFloat") {
    assert(Float.parseFloat("1.0") == 1.0f)
    assert(Float.parseFloat("-1.0") == -1.0f)
    assert(Float.parseFloat("0.0") == 0.0f)
    assert(Float.parseFloat("-0.0") == -0.0f)
    assert(Float.parseFloat("Infinity") == Float.POSITIVE_INFINITY)
    assert(Float.parseFloat("-Infinity") == Float.NEGATIVE_INFINITY)
    assert(Float.isNaN(Float.parseFloat("NaN")))
  }

  test("toString") {
    assert(Float.toString(1.0f).equals("1.000000"))
    assert(Float.toString(-1.0f).equals("-1.000000"))
    assert(Float.toString(0.0f).equals("0.000000"))
    assert(Float.toString(-0.0f).equals("-0.000000"))
    assert(Float.toString(Float.POSITIVE_INFINITY).equals("Infinity"))
    assert(Float.toString(Float.NEGATIVE_INFINITY).equals("-Infinity"))
    assert(Float.toString(Float.NaN).equals("NaN"))
  }
}
