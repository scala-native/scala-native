package java.math

object BigIntegerSuite extends tests.Suite {

// BigInteger.TWO

  test("BigInteger.TWO should exist") {
    val bi       = BigInteger.TWO
    val expected = 2
    val result   = bi.intValue
    assert(
      result == expected,
      s"BigInteger.TWO result: ${result} != expected: ${expected}"
    )
  }

// Constructors

  test("Testing constructor BigInteger(byteArray)") {}

  test("* null byteArray should throw NPE") {
    assertThrows[NullPointerException] {
      val bi = new BigInteger(null: Array[Byte])
    }
  }

  test("* empty byteArray should throw") {
    assertThrows[NumberFormatException] {
      val byteArr = Array.empty[Byte]
      val bi      = new BigInteger(byteArr)
    }
  }

  test("* with valid arguments should create expected") {
    val byteArr  = Array[Byte](2, 1)
    val expected = 513
    val bi       = new BigInteger(byteArr)
    val result   = bi.intValueExact()

    assert(
      result == expected,
      s"BigInteger(${byteArr}) " +
        s"result: ${result} != expected: ${expected}"
    )
  }

  test("Testing constructor BigInteger(byteArray, off, len)") {}

  test("* null byteArray should throw NPE") {
    assertThrows[NullPointerException] {
      val bi = new BigInteger(null: Array[Byte], 0, 10)
    }
  }

  test("* empty byteArray should throw") {
    assertThrows[NumberFormatException] {
      val byteArr = Array.empty[Byte]
      val bi      = new BigInteger(byteArr, 1, 10)
    }
  }

  test("* off < 0 should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val byteArr = Array[Byte](9, 8, 4, 5)
      val bi      = new BigInteger(byteArr, -1, 10)
    }
  }

  test("* len < 0 should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val byteArr = Array[Byte](9, 8, 4, 5)
      val bi      = new BigInteger(byteArr, 0, -2)
    }
  }

  test("* off + len too big should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val byteArr = Array[Byte](9, 8, 4, 5)
      val bi      = new BigInteger(byteArr, 2, 5)
    }
  }

  test("* with valid arguments should create expected") {
    val byteArr  = Array[Byte](9, 8, 4, 5)
    val expected = 1029
    val bi       = new BigInteger(byteArr, 2, 2)
    val result   = bi.intValueExact()

    assert(
      result == expected,
      s"BigInteger(${byteArr}) " +
        s"result: ${result} != expected: ${expected}"
    )
  }

  test("Testing constructor BigInteger(signum, null, off, len)") {}

  test("* null magnitude array should throw NPE") {
    assertThrows[NullPointerException] {
      val bi = new BigInteger(0, null: Array[Byte], 1, 6)
    }
  }

  test("* empty magnitude array should create BigInteger(0)") {
    val expected = BigInteger.ZERO
    val result   = new BigInteger(0, Array.empty[Byte], -1, -2)
  }

  test("* signum < -1 should throw") {
    assertThrows[NumberFormatException] {
      val bi = new BigInteger(-2, Array[Byte](1), 2, 7)
    }
  }

  test("* signum > 1 should throw") {
    assertThrows[NumberFormatException] {
      val bi = new BigInteger(2, Array[Byte](99), 1, 3)
    }
  }

  test("* empty array, with any valid signum should create BigInteger(0)") {
    val expected     = BigInteger.ZERO
    val result_plus  = new BigInteger(1, Array.empty[Byte], -1, -2)
    val result_zero  = new BigInteger(0, Array.empty[Byte], -3, -4)
    val result_minus = new BigInteger(-1, Array.empty[Byte], -5, 6)

    assert(result_plus.compareTo(expected) == 0,
           s"signum == 1 result: ${result_plus} != expected: ${expected}")

    assert(result_zero.compareTo(expected) == 0,
           s"signum == 0 result: ${result_zero} != expected: ${expected}")

    assert(result_minus.compareTo(expected) == 0,
           s"signum == -1 result: ${result_minus} != expected: ${expected}")
  }

  test("* signum == 0 with one or more non-zero array bytes should throw") {
    assertThrows[NumberFormatException] {
      val bi = new BigInteger(0, Array[Byte](0, 1, 0), -1, -2)
    }
  }

  test("* signum == 0 with valid array bytes should create BigInteger(0)") {
    val expected = BigInteger.ZERO
    val result   = new BigInteger(0, Array[Byte](0, 0, 0), 2, 1)

    assert(result.compareTo(expected) == 0,
           s"result: ${result} != expected: ${expected}")
  }

  test("* len == 0, with valid signum & bytes should create BigInteger(0)") {
    val expected = BigInteger.ZERO

    val result_plus  = new BigInteger(1, Array[Byte](1, 0), 0, 0)
    val result_zero  = new BigInteger(0, Array[Byte](0, 0), 1, 0)
    val result_minus = new BigInteger(-1, Array[Byte](0, 1), 0, 0)

    assert(result_plus.compareTo(expected) == 0,
           s"signum == 1 result: ${result_plus} != expected: ${expected}")

    assert(result_zero.compareTo(expected) == 0,
           s"signum == 0 result: ${result_zero} != expected: ${expected}")

    assert(result_minus.compareTo(expected) == 0,
           s"signum == -1 result: ${result_minus} != expected: ${expected}")
  }

  test("* off < 0, signum == 1 with non-empty magnitude should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(1, Array[Byte](0, 1, 0), -1, 4)
    }
  }

  test("* off < 0, signum == 0 with non-empty magnitude should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(0, Array[Byte](0, 0), -1, 2)
    }
  }

  test("* off < 0, signum == -1 with non-empty magnitude should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(-1, Array[Byte](8, 4, 1), -5, 4)
    }
  }

  test("* len < 0, signum == 1 with non-empty magnitude should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(1, Array[Byte](0, 1, 0), 1, -3)
    }
  }

  test("* len < 0, signum == 0 with non-empty magnitude should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(0, Array[Byte](0, 0), 0, -4)
    }
  }

  test("* len < 0, signum == -1 with non-empty magnitude should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(-1, Array[Byte](8, 4, 1), 2, -2)
    }
  }

  test("* off + len > magnitude.length, signum == 1 should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(1, Array[Byte](3, 2, 4, 9), 3, 2)
    }
  }

  test("* off + len > magnitude.length, signum == 0 should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(0, Array[Byte](0, 0, 0, 0), 2, 3)
    }
  }

  test("* off + len > magnitude.length, signum == -1 should throw") {
    assertThrows[IndexOutOfBoundsException] {
      val bi = new BigInteger(-1, Array[Byte](1, 2, 3, 5), 2, 7)
    }
  }

// ---
  test("* valid arguments should create expected") {

    val expected_plus = new BigInteger(1, Array[Byte](3, 9))
    val result_plus   = new BigInteger(1, Array[Byte](3, 9, 7), 0, 2)

    assert(result_plus.compareTo(expected_plus) == 0,
           s"result: ${result_plus} != expected: ${expected_plus}")

    val expected_minus = new BigInteger(-1, Array[Byte](9, 7))
    val result_minus   = new BigInteger(-1, Array[Byte](3, 9, 7), 1, 2)

    assert(result_minus.compareTo(expected_minus) == 0,
           s"result: ${result_minus} != expected: ${expected_minus}")
  }

// byteValueExact

  test("Testing byteValueExact") {}

  val byteMaxBi = new BigInteger(java.lang.Byte.MAX_VALUE.toString)
  val byteMinBi = new BigInteger(java.lang.Byte.MIN_VALUE.toString)

  test("* BigInteger > Byte.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = byteMaxBi.add(BigInteger.ONE)
      bi.byteValueExact()
    }
  }

  test("* BigInteger == Byte.MAX_VALUE should not throw") {
    assert(byteMaxBi.byteValueExact() == java.lang.Byte.MAX_VALUE)
  }

  test("* BigInteger == Byte.MIN_VALUE should not throw") {
    assert(byteMinBi.byteValueExact() == java.lang.Byte.MIN_VALUE)
  }

  test("* BigInteger < Byte.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = byteMinBi.subtract(BigInteger.ONE)
      bi.byteValueExact()
    }
  }

// intValueExact

  test("Testing intValueExact") {}

  val intMaxBi = new BigInteger(java.lang.Integer.MAX_VALUE.toString)
  val intMinBi = new BigInteger(java.lang.Integer.MIN_VALUE.toString)

  test("* BigInteger > Integer.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = intMaxBi.add(BigInteger.ONE)
      bi.intValueExact()
    }
  }

  test("* BigInteger == Integer.MAX_VALUE should not throw") {
    assert(intMaxBi.intValueExact() == java.lang.Integer.MAX_VALUE)
  }

  test("* BigInteger == Integer.MIN_VALUE should not throw") {
    assert(intMinBi.intValueExact() == java.lang.Integer.MIN_VALUE)
  }

  test("* BigInteger < Integer.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = intMinBi.subtract(BigInteger.ONE)
      bi.intValueExact()
    }
  }

// longValueExact

  test("Testing longValueExact") {}

  val longMaxBi = new BigInteger(java.lang.Long.MAX_VALUE.toString)
  val longMinBi = new BigInteger(java.lang.Long.MIN_VALUE.toString)

  test("* BigInteger > Long.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = longMaxBi.add(BigInteger.ONE)
      bi.longValueExact()
    }
  }

  test("* BigInteger == Long.MAX_VALUE should not throw") {
    assert(longMaxBi.longValueExact() == java.lang.Long.MAX_VALUE)
  }

  test("* BigInteger == Long.MIN_VALUE should not throw") {
    assert(longMinBi.longValueExact() == java.lang.Long.MIN_VALUE)
  }

  test("* BigInteger < Long.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = longMinBi.subtract(BigInteger.ONE)
      bi.longValueExact()
    }
  }

// shortValueExact

  test("Testing shortValueExact") {}

  val shortMaxBi = new BigInteger(java.lang.Short.MAX_VALUE.toString)
  val shortMinBi = new BigInteger(java.lang.Short.MIN_VALUE.toString)

  test("* BigInteger > Short.MAX_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = shortMaxBi.add(BigInteger.ONE)
      bi.shortValueExact()
    }
  }

  test("* BigInteger == Short.MAX_VALUE should not throw") {
    assert(shortMaxBi.shortValueExact() == java.lang.Short.MAX_VALUE)
  }

  test("* BigInteger == Short.MIN_VALUE should not throw") {
    assert(shortMinBi.shortValueExact() == java.lang.Short.MIN_VALUE)
  }

  test("* BigInteger < Short.MIN_VALUE should throw") {
    assertThrows[ArithmeticException] {
      val bi = shortMinBi.subtract(BigInteger.ONE)
      bi.shortValueExact()
    }
  }

// sqrt

  test("Testing sqrt") {}

  test("* of negative value should throw") {
    assertThrows[ArithmeticException] {
      val bi = BigInteger.ONE.negate
      bi.sqrt
    }
  }

  test("* of one below power-of-2 perfect square should be correct") {
    val n        = 63
    val bi       = BigInteger.valueOf(n)
    val expected = 7
    val result   = bi.sqrt.intValueExact

    assert(
      result == expected,
      s"sqrt ${n} result: ${result} != expected: ${expected}"
    )
  }

  test("* of power-of-2 perfect square should be correct") {
    val n        = 64
    val bi       = BigInteger.valueOf(n)
    val expected = 8
    val result   = bi.sqrt.intValueExact

    assert(
      result == expected,
      s"sqrt ${n} result: ${result} != expected: ${expected}"
    )
  }

  test("* of one above power-of-2 perfect square should be correct") {
    val n        = 65
    val bi       = BigInteger.valueOf(n)
    val expected = 8
    val result   = bi.sqrt.intValueExact

    assert(
      result == expected,
      s"sqrt ${n} result: ${result} != expected: ${expected}"
    )
  }

// sqrtAndRemainder

  test("Testing sqrtAndRemainder") {}

  test("* of negative value should throw") {
    assertThrows[ArithmeticException] {
      val bi = BigInteger.ONE.negate
      bi.sqrtAndRemainder
    }
  }

  test("* of one below perfect square should be correct") {
    val n                 = 99
    val bi                = BigInteger.valueOf(n)
    val expectedWhole     = BigInteger.valueOf(9)
    val expectedRemainder = BigInteger.valueOf(18)
    val result            = bi.sqrtAndRemainder

    assert(
      result(0).compareTo(expectedWhole) == 0,
      s"sqrtAndRemainder ${n} " +
        s"whole: ${result(0)} != expected: ${expectedWhole}"
    )

    assert(
      result(1).compareTo(expectedRemainder) == 0,
      s"sqrtAndRemainder ${n} " +
        s"remainder: ${result(1)} != expected: ${expectedRemainder}"
    )
  }

  test("* of perfect square should be correct") {
    val n                 = 100
    val bi                = BigInteger.valueOf(n)
    val expectedWhole     = BigInteger.TEN
    val expectedRemainder = BigInteger.ZERO
    val result            = bi.sqrtAndRemainder

    assert(
      result(0).compareTo(expectedWhole) == 0,
      s"sqrtAndRemainder ${n} " +
        s"whole: ${result(0)} != expected: ${expectedWhole}"
    )

    assert(
      result(1).compareTo(expectedRemainder) == 0,
      s"sqrtAndRemainder ${n} " +
        s"remainder: ${result(1)} != expected: ${expectedRemainder}"
    )
  }

  test("* of one above perfect square should be correct") {
    val n                 = 101
    val bi                = BigInteger.valueOf(n)
    val expectedWhole     = BigInteger.TEN
    val expectedRemainder = BigInteger.ONE
    val result            = bi.sqrtAndRemainder

    assert(
      result(0).compareTo(expectedWhole) == 0,
      s"sqrtAndRemainder ${n} " +
        s"whole: ${result(0)} != expected: ${expectedWhole}"
    )

    assert(
      result(1).compareTo(expectedRemainder) == 0,
      s"sqrtAndRemainder ${n} " +
        s"remainder: ${result(1)} != expected: ${expectedRemainder}"
    )
  }

}
