package java.util.function

import java.util.Collections

import org.junit.Test
import org.junit.Assert._

class BinaryOperatorTest {
  @Test def testMinBy(): Unit = {
    val binaryOperator = BinaryOperator.minBy(Collections.reverseOrder())
    val min            = binaryOperator.apply(2004, 2018)

    assertTrue(min == 2018)
  }

  @Test def testMaxBy(): Unit = {
    val binaryOperator = BinaryOperator.maxBy(Collections.reverseOrder())
    val max            = binaryOperator.apply(2004, 2018)

    assertTrue(max == 2004)
  }
}
