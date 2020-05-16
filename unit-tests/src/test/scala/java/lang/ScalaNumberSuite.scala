package java.lang

// This suite should be exhaustive is not. It contains the bare minimum
// to confirm the existance of Scala Native Issue #1770 and then to verify
// that it is fixed.
//
// Some is better than none.

object ScalaNumberSuite extends tests.Suite {

  //   __scala_==

  test("BigInt == BigInt") {
    val token                   = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    val sbi2: scala.math.BigInt = scala.math.BigInt(token)

    assert(sbi1 == sbi2)
  }

  test("BigInt.equals(BigInt)") {
    val token                   = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    val sbi2: scala.math.BigInt = scala.math.BigInt(token)

    assert(sbi1.equals(sbi2))
  }

  test("BigDecimal == BigDecimal") {
    val token                       = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token)

    assert(sbd1 == sbd2)
  }

  test("BigDecimal.equals(BigDecimal)") {
    val token                       = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token)

    assert(sbd1.equals(sbd2))
  }
}
