package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert.*

class SizeTest {
  @Test def bitwiseInverse: Unit = {
    assertTrue(~(5.toSize).toInt == -6)
  }

  @Test def numericalNegation: Unit = {
    assertTrue(-(5.toSize).toInt == -5)
  }

  @Test def arithmeticShiftLeft: Unit = {
    assertTrue((5.toSize << 2).toInt == 20)
  }

  @Test def logicalShiftRight: Unit = {
    assertTrue((6.toSize >> 1).toInt == 3)
  }

  @Test def arithmeticShiftRight: Unit = {
    assertTrue((-6.toSize >> 1).toInt == -3)
  }

  @Test def equality: Unit = {
    assertTrue(6.toSize == 6.toSize)
    assertTrue(6.toSize.equals(6.toSize))
  }

  @Test def nonEquality: Unit = {
    assertTrue(6.toSize != 5.toSize)
  }

  @Test def lessThan: Unit = {
    assertTrue(5.toSize < 6.toSize)
  }

  @Test def lessOrEqual: Unit = {
    assertTrue(5.toSize <= 6.toSize)
    assertTrue(5.toSize <= 5.toSize)
  }

  @Test def greaterThan: Unit = {
    assertTrue(6.toSize > 5.toSize)
  }

  @Test def greaterOrEqual: Unit = {
    assertTrue(6.toSize >= 5.toSize)
    assertTrue(5.toSize >= 5.toSize)
  }

  @Test def bitwiseAnd: Unit = {
    assertTrue((123.toSize & 456.toSize).toInt == 72)
  }

  @Test def bitwiseOr: Unit = {
    assertTrue((123.toSize | 456.toSize).toInt == 507)
  }

  @Test def bitwiseXor: Unit = {
    assertTrue((123.toSize ^ 456.toSize).toInt == 435)
  }

  @Test def addition: Unit = {
    assertTrue((123.toSize + 456.toSize).toInt == 579)
  }

  @Test def subtraction: Unit = {
    assertTrue((123.toSize - 456.toSize).toInt == -333)
  }

  @Test def multiplication: Unit = {
    assertTrue((123.toSize * 3.toSize).toInt == 369)
  }

  @Test def division: Unit = {
    assertTrue((123.toSize / 2.toSize).toInt == 61)
  }

  @Test def modulo: Unit = {
    assertTrue((123.toSize % 13.toSize).toInt == 6)
  }
}
