package javalib.math

import java.math._

import org.junit.Test
import org.junit.Assert._

class BigDecimalTest {
//   __scala_==

  @Test def bigDecimalEqualEqualBigDecimal(): Unit = {
    val token = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)

    // Depending upon possible caching, they may or may not be eq.
    assertTrue(jbd1 == jbd2)
  }

  @Test def bigDecimalEqualsBigDecimal(): Unit = {
    val token = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)

    // Depending upon possible caching, they may or may not be reference eq.
    assertTrue(jbd1.equals(jbd2))
  }

  @Test def bigDecimalDoesNotEqualEqualBigDecimalWithDifferentValue(): Unit = {
    val token = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token + 1.0)

    assertFalse(jbd1 == jbd2)
  }
}
