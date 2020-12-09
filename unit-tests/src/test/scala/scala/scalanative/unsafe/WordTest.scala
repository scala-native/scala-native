package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

class WordTest {
  @Test def bitwiseInverse: Unit = {
    assertTrue(~(5.toWord).toInt == -6)
  }

  @Test def numericalNegation: Unit = {
    assertTrue(-(5.toWord).toInt == -5)
  }

  @Test def arithmeticShiftLeft: Unit = {
    assertTrue((5.toWord << 2).toInt == 20)
  }

  @Test def logicalShiftRight: Unit = {
    assertTrue((6.toWord >> 1).toInt == 3)
  }

  @Test def arithmeticShiftRight: Unit = {
    assertTrue((-6.toWord >> 1).toInt == -3)
  }

  @Test def equality: Unit = {
    assertTrue(6.toWord == 6.toWord)
    assertTrue(6.toWord.equals(6.toWord))
  }

  @Test def nonEquality: Unit = {
    assertTrue(6.toWord != 5.toWord)
  }

  @Test def lessThan: Unit = {
    assertTrue(5.toWord < 6.toWord)
  }

  @Test def lessOrEqual: Unit = {
    assertTrue(5.toWord <= 6.toWord)
    assertTrue(5.toWord <= 5.toWord)
  }

  @Test def greaterThan: Unit = {
    assertTrue(6.toWord > 5.toWord)
  }

  @Test def greaterOrEqual: Unit = {
    assertTrue(6.toWord >= 5.toWord)
    assertTrue(5.toWord >= 5.toWord)
  }

  @Test def bitwiseAnd: Unit = {
    assertTrue((123.toWord & 456.toWord).toInt == 72)
  }

  @Test def bitwiseOr: Unit = {
    assertTrue((123.toWord | 456.toWord).toInt == 507)
  }

  @Test def bitwiseXor: Unit = {
    assertTrue((123.toWord ^ 456.toWord).toInt == 435)
  }

  @Test def addition: Unit = {
    assertTrue((123.toWord + 456.toWord).toInt == 579)
  }

  @Test def subtraction: Unit = {
    assertTrue((123.toWord - 456.toWord).toInt == -333)
  }

  @Test def multiplication: Unit = {
    assertTrue((123.toWord * 3.toWord).toInt == 369)
  }

  @Test def division: Unit = {
    assertTrue((123.toWord / 2.toWord).toInt == 61)
  }

  @Test def modulo: Unit = {
    assertTrue((123.toWord % 13.toWord).toInt == 6)
  }
}
