package scala.scalanative.unsigned

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.unsafe._

class UWordTest {
  @Test def bitwiseInverse: Unit = {
    assertTrue(~(5.toUWord).toInt == -6)
  }

  @Test def arithmeticShiftLeft: Unit = {
    assertTrue((5.toUWord << 2).toInt == 20)
  }

  @Test def logicalShiftRight: Unit = {
    assertTrue((6.toUWord >> 1).toInt == 3)
  }

  @Test def equality: Unit = {
    assertTrue(6.toUWord == 6.toUWord)
    assertTrue(6.toUWord.equals(6.toUWord))
  }

  @Test def nonEquality: Unit = {
    assertTrue(6.toUWord != 5.toUWord)
  }

  @Test def lessThan: Unit = {
    assertTrue(5.toUWord < 6.toUWord)
  }

  @Test def lessOrEqual: Unit = {
    assertTrue(5.toUWord <= 6.toUWord)
    assertTrue(5.toUWord <= 5.toUWord)
  }

  @Test def greaterThan: Unit = {
    assertTrue(6.toUWord > 5.toUWord)
  }

  @Test def greaterOrEqual: Unit = {
    assertTrue(6.toUWord >= 5.toUWord)
    assertTrue(5.toUWord >= 5.toUWord)
  }

  @Test def bitwiseAnd: Unit = {
    assertTrue((123.toUWord & 456.toUWord).toInt == 72)
  }

  @Test def bitwiseOr: Unit = {
    assertTrue((123.toUWord | 456.toUWord).toInt == 507)
  }

  @Test def bitwiseXor: Unit = {
    assertTrue((123.toUWord ^ 456.toUWord).toInt == 435)
  }

  @Test def addition: Unit = {
    assertTrue((123.toUWord + 456.toUWord).toInt == 579)
  }

  @Test def subtraction: Unit = {
    assertTrue((456.toUWord - 123.toUWord).toInt == 333)
  }

  @Test def multiplication: Unit = {
    assertTrue((123.toUWord * 3.toUWord).toInt == 369)
  }

  @Test def division: Unit = {
    assertTrue((123.toUWord / 2.toUWord).toInt == 61)
    if (!is32) {
      assertTrue((-1L.toUWord / 2.toUWord).toLong == 9223372036854775807L)
    } else {
      // TODO(shadaj)
    }
  }

  @Test def modulo: Unit = {
    assertTrue((123.toUWord % 13.toUWord).toInt == 6)
    assertTrue((-1L.toUWord % 10.toUWord).toInt == 5)
  }
}
