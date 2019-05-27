package scala.scalanative
package unsafe

object WordSuite extends tests.Suite {
  test("Bitwise negation") {
    assertEquals(~(5.toWord).toInt, -6)
  }

  test("Decimal negation") {
    assertEquals(-(5.toWord).toInt, -5)
  }

  test("Shift left") {
    assertEquals((5.toWord << 2).toInt, 20)
  }

  test("Bitwise and") {
    assertEquals((123.toWord & 456.toWord).toInt, 72)
  }

  test("Bitwise or") {
    assertEquals((123.toWord | 456.toWord).toInt, 507)
  }

  test("Bitwise xor") {
    assertEquals((123.toWord ^ 456.toWord).toInt, 435)
  }

  test("Decimal addition") {
    assertEquals((123.toWord + 456.toWord).toInt, 579)
  }

  test("Decimal subtraction") {
    assertEquals((123.toWord - 456.toWord).toInt, -333)
  }

  test("Decimal multiplication") {
    assertEquals((123.toWord * 3.toWord).toInt, 369)
  }

  test("Decimal division") {
    assertEquals((123.toWord / 2.toWord).toInt, 61)
  }

  test("Modulo") {
    assertEquals((123.toWord % 13.toWord).toInt, 6)
  }
}