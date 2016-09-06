package java.lang

object DoubleSuite extends tests.Suite {
  test("parseDouble") {
    assert(Double.parseDouble("1.0") == 1.0)
    assert(Double.parseDouble("-1.0") == -1.0)
    assert(Double.parseDouble("0.0") == 0.0)
    assert(Double.parseDouble("-0.0") == -0.0)
    assert(Double.parseDouble("Infinity") == Double.POSITIVE_INFINITY)
    assert(Double.parseDouble("-Infinity") == Double.NEGATIVE_INFINITY)
    assert(Double.isNaN(Double.parseDouble("NaN")))
  }

  // Not fully JVM compliant yet
  test("toString") {
    assert(Double.toString(1.0).equals("1.000000"))
    assert(Double.toString(-1.0).equals("-1.000000"))
    assert(Double.toString(0.0).equals("0.000000"))
    assert(Double.toString(-0.0).equals("-0.000000"))
    assert(Double.toString(Double.POSITIVE_INFINITY).equals("Infinity"))
    assert(Double.toString(Double.NEGATIVE_INFINITY).equals("-Infinity"))
    assert(Double.toString(Double.NaN).equals("NaN"))
  }
}
