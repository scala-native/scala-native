package scala.scalanative.unsigned

import org.junit.Test
import org.junit.Assert.*

import scala.scalanative.unsafe.*
import scala.scalanative.meta.LinktimeInfo.is32BitPlatform

class USizeTest {
  @Test def bitwiseInverse: Unit = {
    assertTrue(~(5.toUSize).toInt == -6)
  }

  @Test def arithmeticShiftLeft: Unit = {
    assertTrue((5.toUSize << 2).toInt == 20)
  }

  @Test def logicalShiftRight: Unit = {
    assertTrue((6.toUSize >> 1).toInt == 3)
  }

  @Test def equality: Unit = {
    assertTrue(6.toUSize == 6.toUSize)
    assertTrue(6.toUSize.equals(6.toUSize))
  }

  @Test def nonEquality: Unit = {
    assertTrue(6.toUSize != 5.toUSize)
  }

  @Test def lessThan: Unit = {
    assertTrue(5.toUSize < 6.toUSize)
  }

  @Test def lessOrEqual: Unit = {
    assertTrue(5.toUSize <= 6.toUSize)
    assertTrue(5.toUSize <= 5.toUSize)
  }

  @Test def greaterThan: Unit = {
    assertTrue(6.toUSize > 5.toUSize)
  }

  @Test def greaterOrEqual: Unit = {
    assertTrue(6.toUSize >= 5.toUSize)
    assertTrue(5.toUSize >= 5.toUSize)
  }

  @Test def bitwiseAnd: Unit = {
    assertTrue((123.toUSize & 456.toUSize).toInt == 72)
  }

  @Test def bitwiseOr: Unit = {
    assertTrue((123.toUSize | 456.toUSize).toInt == 507)
  }

  @Test def bitwiseXor: Unit = {
    assertTrue((123.toUSize ^ 456.toUSize).toInt == 435)
  }

  @Test def addition: Unit = {
    assertTrue((123.toUSize + 456.toUSize).toInt == 579)
  }

  @Test def subtraction: Unit = {
    assertTrue((456.toUSize - 123.toUSize).toInt == 333)
  }

  @Test def multiplication: Unit = {
    assertTrue((123.toUSize * 3.toUSize).toInt == 369)
  }

  @Test def division: Unit = {
    assertTrue((123.toUSize / 2.toUSize).toInt == 61)
    assertTrue((-1L.toUSize / 2.toUSize).toLong == (if (!is32BitPlatform) {
                                                      ~(1L << 63)
                                                    } else {
                                                      (~(1 << 31)).toLong
                                                    }))
  }

  @Test def modulo: Unit = {
    assertTrue((123.toUSize % 13.toUSize).toInt == 6)
    assertTrue((-1L.toUSize % 10.toUSize).toInt == 5)
  }
}
