package java.lang

//  Exercise __scala_==

object ScalaNumberSuite extends tests.Suite {

  test("BigInt") { // Section header, visually group tests
  }

  test("  BigInt == BigInt") {
    val token                   = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    val sbi2: scala.math.BigInt = scala.math.BigInt(token)

    assert(sbi1 == sbi2)
  }

  test("  BigInt.equals(BigInt)") {
    val token                   = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    val sbi2: scala.math.BigInt = scala.math.BigInt(token)

    assert(sbi1.equals(sbi2))
  }

  test("  BigInt does not == BigInt with different value") {
    val token                   = 2047L
    val sbi1: scala.math.BigInt = scala.math.BigInt(token)
    // avoid powers of 2 because of possible caching.
    val sbi2: scala.math.BigInt = scala.math.BigInt(token + 2)

    assertFalse(sbi1 == sbi2)
  }

  test("  BigInt == j.l.Long") {
    val token                  = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long     = new java.lang.Long(token.toString)

    assert(sbi == jl)
  }

  test("  BigInt does not == j.l.Long with different value") {
    val token                  = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long     = new java.lang.Long((token + 2).toString)

    assertFalse(sbi == jl)
  }

  test("  j.l.Long == BigInt") {
    val token                  = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long     = new java.lang.Long(token.toString)

    assert(jl == sbi)
  }

  test("  j.l.Long does not == BigInt with different value") {
    val token                  = Int.MaxValue + 2L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val jl: java.lang.Long     = new java.lang.Long((token + 2).toString)

    assertFalse(jl == sbi)
  }

  test("  j.l.Long == j.l.Long") {
    val token               = 2047
    val jl1: java.lang.Long = new java.lang.Long(token)
    val jl2: java.lang.Long = new java.lang.Long(token)

    assert(jl1 == jl2)
  }

  test("  j.l.Long does not == j.l.Long with different value") {
    val token               = 2047
    val jl1: java.lang.Long = new java.lang.Long(token)
    val jl2: java.lang.Long = new java.lang.Long(token + 2)

    assertFalse(jl1 == jl2)
  }

  test("  BigInt == j.l.Integer") {
    val token                  = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer  = new java.lang.Integer(token.toString)

    assert(sbi == ji)
  }

  test("  BigInt does not == j.l.Integer with different value") {
    val token                  = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer  = new java.lang.Integer((token + 2).toString)

    assertFalse(sbi == ji)
  }

  test("  j.l.Integer == BigInt") {
    val token                  = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer  = new java.lang.Integer(token.toString)

    assert(ji == sbi)
  }

  test("  j.l.Integer does not == BigInt with different value") {
    val token                  = 2047L
    val sbi: scala.math.BigInt = scala.math.BigInt(token)
    val ji: java.lang.Integer  = new java.lang.Integer((token + 2).toString)

    assertFalse(ji == sbi)
  }

  test("  j.l.Integer == j.l.Integer") {
    val token                  = 2047
    val ji1: java.lang.Integer = new java.lang.Integer(token)
    val ji2: java.lang.Integer = new java.lang.Integer(token)

    assert(ji1 == ji2)
  }

  test("  j.l.Integer does not == j.l.Integer with different value") {
    val token                  = 2047
    val ji1: java.lang.Integer = new java.lang.Integer(token)
    val ji2: java.lang.Integer = new java.lang.Integer(token + 2)

    assertFalse(ji1 == ji2)
  }

  test("BigDecimal") { // Section header, visually group tests
  }

  test("  BigDecimal == BigDecimal") {
    val token                       = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token)

    assert(sbd1 == sbd2)
  }

  test("  BigDecimal.equals(BigDecimal)") {
    val token                       = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token)

    assert(sbd1.equals(sbd2))
  }

  test("  BigDecimal does not == BigDecimal with different value") {
    val token                       = 2046.5
    val sbd1: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val sbd2: scala.math.BigDecimal = scala.math.BigDecimal(token - 2.0)

    assertFalse(sbd1 == sbd2)
  }

  test("  BigDecimal == j.l.Double") {
    val token                      = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double       = new java.lang.Double(token.toString)

    assert(sbd == jd)
  }

  test("  BigDecimal does not == j.l.Double with different value") {
    val token                      = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double       = new java.lang.Double((token - 2.0).toString)

    assertFalse(sbd == jd)
  }

  test("  j.l.Double == BigDecimal") {
    val token                      = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double       = new java.lang.Double(token.toString)

    assert(jd == sbd)
  }

  test("  j.l.Double does not == BigDecimal with different value") {
    val token                      = 2046.5
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jd: java.lang.Double       = new java.lang.Double((token - 2.0).toString)

    assertFalse(sbd == jd)
  }

  test("  j.l.Double == j.l.Double") {
    val token                 = 2046.5
    val jd1: java.lang.Double = new java.lang.Double(token)
    val jd2: java.lang.Double = new java.lang.Double(token)

    assert(jd1 == jd2)
  }

  test("  j.l.Double does not == j.l.Double with different value") {
    val token                 = 2046.5
    val jd1: java.lang.Double = new java.lang.Double(token)
    val jd2: java.lang.Double = new java.lang.Double((token - 2.0).toString)

    assertFalse(jd1 == jd2)
  }

  test("  BigDecimal == j.l.Float") {
    val token                      = 2046.5F
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float        = new java.lang.Float(token.toString)

    assert(sbd == jf)
  }

  test("  BigDecimal does not == j.l.Float with different value") {
    val token                      = 2046.5F
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float        = new java.lang.Float((token - 2.0).toString)

    assertFalse(sbd == jf)
  }

  test("  j.l.Float == BigDecimal") {
    val token                      = 2046.5F
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float        = new java.lang.Float(token.toString)

    assert(jf == sbd)
  }

  test("  j.l.Float does not == BigDecimal with different value") {
    val token                      = 2046.5F
    val sbd: scala.math.BigDecimal = scala.math.BigDecimal(token)
    val jf: java.lang.Float        = new java.lang.Float((token - 2.0).toString)

    assertFalse(jf == sbd)
  }

  test("  j.l.Float == j.l.Float") {
    val token                = 2046.5F
    val jf1: java.lang.Float = new java.lang.Float(token)
    val jf2: java.lang.Float = new java.lang.Float(token)

    assert(jf1 == jf2)
  }

  test("  j.l.Float does not == j.l.Float with different value") {
    val token                = 2046.5F
    val jf1: java.lang.Float = new java.lang.Float(token)
    val jf2: java.lang.Float = new java.lang.Float((token - 2.0).toString)

    assertFalse(jf1 == jf2)
  }
}
