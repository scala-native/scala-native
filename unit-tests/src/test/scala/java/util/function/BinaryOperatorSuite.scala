package java.util.function

import java.util.Collections

object BinaryOperatorSuite extends tests.Suite {
  test("minBy") {
    val binaryOperator = BinaryOperator.minBy(Collections.reverseOrder())
    val min            = binaryOperator.apply(2004, 2018)

    assert(min == 2018)
  }

  test("maxBy") {
    val binaryOperator = BinaryOperator.maxBy(Collections.reverseOrder())
    val max            = binaryOperator.apply(2004, 2018)

    assert(max == 2004)
  }
}
