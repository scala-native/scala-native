package java.lang

object MathSuite extends tests.Suite {

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

  // round()

  test("round(Double) - special values") {

    assert(Math.round(Double.NaN) == 0L, "round(NaN) != 0L")

    // value d as reported in issue #1071
    val dTooLarge: Double      = 4228438087383875356545203991520.000000d
    val roundedTooLarge: Long  = Math.round(dTooLarge)
    val expectedTooLarge: Long = scala.Long.MaxValue

    assert(roundedTooLarge == expectedTooLarge,
           s"${roundedTooLarge} != ${expectedTooLarge}" +
             " when Double > Long.MaxValue")

    val roundedTooNegative: Long  = Math.round(-1.0 * dTooLarge)
    val expectedTooNegative: Long = scala.Long.MinValue

    assert(roundedTooNegative == expectedTooNegative,
           s"${roundedTooNegative} != ${expectedTooNegative}" +
             " when Double < Long.MinValue")
  }

  test("round(Double) - ties rounding towards +Infinity") {

    case class TestPoint(value: Double, expected: Long)

    // Check that implementation addition of 0.5 does not cause
    // overflow into negative numbers. Values near MinValue do not
    // have this potential flaw.

    val testPointsOverflow = Seq(
      TestPoint((scala.Double.MaxValue - 0.4d), scala.Long.MaxValue)
    )

    // Useful/Definitive cases from URL:
    // https://docs.oracle.com/javase/10/docs/api/java/math/RoundingMode.html
    //
    // The expected values are from the Scala REPL/JVM.
    // The "ties towards +Infinity" rule best explains the observed results.
    // Note well that _none_ of the rounding modes at that URL describe the
    // the Scala REPL results, not even HALF_UP.

    val testPointsJavaApi = Seq(
      TestPoint(+5.5d, +6L),
      TestPoint(+2.5d, +3L),
      TestPoint(+1.6d, +2L),
      TestPoint(+1.1d, +1L),
      TestPoint(+1.0d, +1L),
      TestPoint(-1.0d, -1L),
      TestPoint(-1.1d, -1L),
      TestPoint(-1.6d, -2L),
      TestPoint(-2.5d, -2L),
      TestPoint(-5.5d, -5L)
    )

    // +2.5 and -2.5 are the distinguishing cases. They show that
    // math.round() is correctly rounding towards positive Infinity,
    //
    // The other cases are sanity cases to establish context.

    val testPoints = Seq(
      TestPoint(-2.6d, -3L),
      TestPoint(-2.5d, -2L),
      TestPoint(-2.4d, -2L),
      TestPoint(+2.4d, +2L),
      TestPoint(+2.5d, +3L),
      TestPoint(+2.6d, +3L)
    )

    val TestPointGroup = Seq(
      testPointsOverflow,
      testPointsJavaApi,
      testPoints
    )

    for (testPoints <- TestPointGroup) {
      for (testPoint <- testPoints) {
        val v: Double      = testPoint.value
        val result: Long   = math.round(v)
        val expected: Long = testPoint.expected

        assert(result == testPoint.expected,
               s"round(${v}) result: ${result} != expected: ${expected}")
      }
    }
  }

  test("round(Float) - special values") {

    assert(Math.round(Float.NaN) == 0, "round(NaN) != 0")

    val fTooLarge: Float      = scala.Float.MaxValue
    val roundedTooLarge: Int  = Math.round(fTooLarge)
    val expectedTooLarge: Int = scala.Int.MaxValue

    assert(roundedTooLarge == expectedTooLarge,
           s"${roundedTooLarge} != ${expectedTooLarge}" +
             " when Float > Int.MaxValue")

    val roundedTooNegative: Int  = Math.round(scala.Float.MinValue)
    val expectedTooNegative: Int = scala.Int.MinValue

    assert(roundedTooNegative == expectedTooNegative,
           s"${roundedTooNegative} != ${expectedTooNegative}" +
             " when Float < Int.MinValue")
  }

  test("round(Float) - ties rounding towards +Infinity") {

    case class TestPoint(value: Float, expected: Int)

    // See extensive comments in test for round(Double) above.

    // Check that implementation addition of 0.5 does not cause
    // overflow into negative numbers. Values near MinValue do not
    // have this potential flaw.

    val testPointsOverflow = Seq(
      TestPoint((scala.Float.MaxValue - 0.4f), scala.Int.MaxValue)
    )

    // Useful/Definitive cases from URL:
    // https://docs.oracle.com/javase/10/docs/api/java/math/RoundingMode.html
    //
    // The expected values are from the Scala REPL/JVM.
    // The "ties towards +Infinity" rule best explains the observed results.
    // Note well that _none_ of the rounding modes at that URL describe the
    // the Scala REPL results, not even HALF_UP.

    val testPointsJavaApi = Seq(
      TestPoint(+5.5f, +6),
      TestPoint(+2.5f, +3),
      TestPoint(+1.6f, +2),
      TestPoint(+1.1f, +1),
      TestPoint(+1.0f, +1),
      TestPoint(-1.0f, -1),
      TestPoint(-1.1f, -1),
      TestPoint(-1.6f, -2),
      TestPoint(-2.5f, -2),
      TestPoint(-5.5f, -5)
    )

    val testPoints = Seq(
      TestPoint(-97.6f, -98),
      TestPoint(-97.5f, -97),
      TestPoint(-97.4f, -97),
      TestPoint(+97.4f, +97),
      TestPoint(+97.5f, +98),
      TestPoint(+97.6f, +98)
    )

    val TestPointGroup = Seq(
      testPointsOverflow,
      testPointsJavaApi,
      testPoints
    )

    for (testPoints <- TestPointGroup) {
      for (testPoint <- testPoints) {
        val v: Float      = testPoint.value
        val result: Int   = math.round(v)
        val expected: Int = testPoint.expected

        assert(result == testPoint.expected,
               s"round(${v}) result: ${result} != expected: ${expected}")
      }
    }
  }

}
