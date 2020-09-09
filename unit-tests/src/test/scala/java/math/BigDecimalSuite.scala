package java.math

object BigDecimalSuite extends tests.Suite {
//   __scala_==

  test("BigDecimal == BigDecimal") {
    val token                      = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)

    // Depending upon possible caching, they may or may not be eq.
    assert(jbd1 == jbd2)
  }

  test("BigDecimal.equals(BigDecimal)") {
    val token                      = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)

    // Depending upon possible caching, they may or may not be reference eq.
    assert(jbd1.equals(jbd2))
  }

  test("BigDecimal does not == BigDecimal with different value") {
    val token                      = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token + 1.0)

    assertFalse(jbd1 == jbd2)
  }
}
