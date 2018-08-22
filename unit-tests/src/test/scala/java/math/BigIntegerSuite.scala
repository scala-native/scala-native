package java.math

object BigIntegerSuite extends tests.Suite {

// byteValueExact

  val byteMaxBi = new BigInteger(java.lang.Byte.MAX_VALUE.toString)
  val byteMinBi = new BigInteger(java.lang.Byte.MIN_VALUE.toString)

  test("byteValueExact with BigInteger > Byte.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = byteMaxBi.add(BigInteger.ONE)
      bi.byteValueExact()
    }
  }

  test("byteValueExact with BigInteger == Byte.MAX_VALUE should not throw") {
    assert(byteMaxBi.byteValueExact() == java.lang.Byte.MAX_VALUE)
  }

  test("byteValueExact with BigInteger == Byte.MIN_VALUE should not throw") {
    assert(byteMinBi.byteValueExact() == java.lang.Byte.MIN_VALUE)
  }

  test("byteValueExact with BigInteger < Byte.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = byteMinBi.subtract(BigInteger.ONE)
      bi.byteValueExact()
    }
  }

// intValueExact

  val intMaxBi = new BigInteger(java.lang.Integer.MAX_VALUE.toString)
  val intMinBi = new BigInteger(java.lang.Integer.MIN_VALUE.toString)

  test("intValueExact with BigInteger > Integer.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = intMaxBi.add(BigInteger.ONE)
      bi.intValueExact()
    }
  }

  test("intValueExact with BigInteger == Integer.MAX_VALUE should not throw") {
    assert(intMaxBi.intValueExact() == java.lang.Integer.MAX_VALUE)
  }

  test("intValueExact with BigInteger == Integer.MIN_VALUE should not throw") {
    assert(intMinBi.intValueExact() == java.lang.Integer.MIN_VALUE)
  }

  test("intValueExact with BigInteger < Integer.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = intMinBi.subtract(BigInteger.ONE)
      bi.intValueExact()
    }
  }

// longValueExact

  val longMaxBi = new BigInteger(java.lang.Long.MAX_VALUE.toString)
  val longMinBi = new BigInteger(java.lang.Long.MIN_VALUE.toString)

  test("longValueExact with BigInteger > Long.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = longMaxBi.add(BigInteger.ONE)
      bi.longValueExact()
    }
  }

  test("longValueExact with BigInteger == Long.MAX_VALUE should not throw") {
    assert(longMaxBi.longValueExact() == java.lang.Long.MAX_VALUE)
  }

  test("longValueExact with BigInteger == Long.MIN_VALUE should not throw") {
    assert(longMinBi.longValueExact() == java.lang.Long.MIN_VALUE)
  }

  test("longValueExact with BigInteger < Long.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = longMinBi.subtract(BigInteger.ONE)
      bi.longValueExact()
    }
  }

// shortValueExact

  val shortMaxBi = new BigInteger(java.lang.Short.MAX_VALUE.toString)
  val shortMinBi = new BigInteger(java.lang.Short.MIN_VALUE.toString)

  test("shortValueExact with BigInteger > Short.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = shortMaxBi.add(BigInteger.ONE)
      bi.shortValueExact()
    }
  }

  test("shortValueExact with BigInteger == Short.MAX_VALUE should not throw") {
    assert(shortMaxBi.shortValueExact() == java.lang.Short.MAX_VALUE)
  }

  test("shortValueExact with BigInteger == Short.MIN_VALUE should not throw") {
    assert(shortMinBi.shortValueExact() == java.lang.Short.MIN_VALUE)
  }

  test("shortValueExact with BigInteger < Short.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = shortMinBi.subtract(BigInteger.ONE)
      bi.shortValueExact()
    }
  }

}
