package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

class WordSuite {
  @Test def bitwiseNegation: Unit = {
    assertTrue(~(5.toWord).toInt == -6)
  }

  // test("Numerical negation") {
  //   assertEquals(-(5.toWord).toInt, -5)
  // }

  // test("Shift left") {
  //   assertEquals((5.toWord << 2).toInt, 20)
  // }

  // test("Logical shift right") {
  //   assertEquals((6.toWord >> 1).toInt, 3)
  // }

  // test("Signed shift right") {
  //   assertEquals((-6.toWord >> 1).toInt, -3)
  // }

  // test("Equals") {
  //   assert(6.toWord == 6.toWord)
  //   assert(6.toWord.equals(6.toWord))
  // }

  // test("Not equals") {
  //   assert(6.toWord != 5.toWord)
  // }

  // test("Less than") {
  //   assert(5.toWord < 6.toWord)
  // }

  // test("Less than or equal") {
  //   assert(5.toWord <= 6.toWord)
  //   assert(5.toWord <= 5.toWord)
  // }

  // test("Greater than") {
  //   assert(6.toWord > 5.toWord)
  // }

  // test("Greater than or equal") {
  //   assert(6.toWord >= 5.toWord)
  //   assert(5.toWord >= 5.toWord)
  // }

  // test("Bitwise and") {
  //   assertEquals((123.toWord & 456.toWord).toInt, 72)
  // }

  // test("Bitwise or") {
  //   assertEquals((123.toWord | 456.toWord).toInt, 507)
  // }

  // test("Bitwise xor") {
  //   assertEquals((123.toWord ^ 456.toWord).toInt, 435)
  // }

  // test("Addition") {
  //   assertEquals((123.toWord + 456.toWord).toInt, 579)
  // }

  // test("Subtraction") {
  //   assertEquals((123.toWord - 456.toWord).toInt, -333)
  // }

  // test("Multiplication") {
  //   assertEquals((123.toWord * 3.toWord).toInt, 369)
  // }

  // test("Division") {
  //   assertEquals((123.toWord / 2.toWord).toInt, 61)
  // }

  // test("Remainder") {
  //   assertEquals((123.toWord % 13.toWord).toInt, 6)
  // }
}
