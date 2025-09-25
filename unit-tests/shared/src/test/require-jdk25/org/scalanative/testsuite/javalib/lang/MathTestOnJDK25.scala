package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.lang._

class MathTestOnJDK25 {

  @Test def powExact_IntInt_ArithmeticExceptions(): Unit = {
    assertThrows(
      "n is negative",
      classOf[ArithmeticException],
      Math.powExact(Integer.MAX_VALUE, -1)
    )

    assertThrows(
      "overflows Int",
      classOf[ArithmeticException],
      Math.powExact(
        Math.ceil(Math.sqrt(Integer.MAX_VALUE)).toInt,
        2
      )
    )
  }

  @Test def powExact_IntInt(): Unit = {
    case class powExactCase(x: Int, y: Int, expected: Int)

    val testCases = java.util.List.of(
      powExactCase(-1, 0, 1),
      powExactCase(-1, 1, -1),
      powExactCase(-1, 2, 1),
      powExactCase(1, 2, 1),
      powExactCase(2, 2, 4),
      powExactCase(2, 3, 8),
      powExactCase(2, 9, 512),
      powExactCase(2, 10, 1024),
      powExactCase(2, 19, 524288),
      powExactCase(3, 3, 27),
      powExactCase(3, 5, 243),
      powExactCase(
        Math.floor(Math.sqrt(Integer.MAX_VALUE)).toInt,
        2,
        2147395600
      )
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.powExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException powExact(${tc.x}, ${tc.y})")
      }
    }
  }

  @Test def powExact_LongInt_ArithmeticExceptions(): Unit = {
    assertThrows(
      "n is negative",
      classOf[ArithmeticException],
      Math.powExact(Long.MAX_VALUE, -2)
    )

    assertThrows(
      "overflows Int",
      classOf[ArithmeticException],
      Math.powExact(
        Math.ceil(Math.cbrt(Long.MAX_VALUE)).toInt,
        3
      )
    )
  }

  @Test def powExact_LongInt(): Unit = {
    case class powExactCase(x: Long, y: Int, expected: Long)

    val testCases = java.util.List.of(
      powExactCase(-1L, 0, 1L),
      powExactCase(-1L, 1, -1L),
      powExactCase(-1L, 2, 1L),
      powExactCase(1L, 2, 1L),
      powExactCase(2L, 2, 4L),
      powExactCase(2L, 3, 8L),
      powExactCase(2L, 9, 512L),
      powExactCase(2L, 10, 1024L),
      powExactCase(2L, 19, 524288L),
      powExactCase(3L, 3, 27L),
      powExactCase(3L, 5, 243L),
      powExactCase(
        Math.floor(Math.sqrt(Long.MAX_VALUE)).toLong,
        2,
        9223372030926249001L
      )
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.powExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException powExact(${tc.x}, ${tc.y})")
      }
    }

  }

  @Test def unsignedMultiplyExact_IntInt_ArithmeticException(): Unit = {
    val x = Integer.MIN_VALUE
    assertThrows(
      "overflows unsigned Int",
      classOf[ArithmeticException],
      Math.unsignedMultiplyExact(x, 2)
    )
  }

  @Test def unsignedMultiplyExact_IntInt(): Unit = {
    case class unsignedMultiplyExactCase(x: Int, y: Int, expected: Int)

    val m1 = Math.ceil(Math.sqrt(Integer.MAX_VALUE)).toInt

    val testCases = java.util.List.of(
      unsignedMultiplyExactCase(m1, m1, -2147479015),
      unsignedMultiplyExactCase(Integer.MAX_VALUE, 2, -2),
      unsignedMultiplyExactCase(Integer.MAX_VALUE >> 1, 3, -1073741827)
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.unsignedMultiplyExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException unsignedMultiplyExact(${tc.x}, ${tc.y})")
      }
    }
  }

  @Test def unsignedMultiplyExact_LongInt_ArithmeticException(): Unit = {
    val x = Long.MIN_VALUE
    assertThrows(
      "overflows unsigned Long",
      classOf[ArithmeticException],
      Math.unsignedMultiplyExact(x, 3)
    )
  }

  @Test def unsignedMultiplyExact_LongInt(): Unit = {
    case class unsignedMultiplyExactCase(x: Long, y: Int, expected: Long)

    val m1 = Math.ceil(Math.sqrt(Long.MAX_VALUE)).toLong

    val testCases = java.util.List.of(
      unsignedMultiplyExactCase(m1, m1.toInt, -9223372036709301616L),
      unsignedMultiplyExactCase(Long.MAX_VALUE, 2, -2L)
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.unsignedMultiplyExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException unsignedMultiplyExact(${tc.x}, ${tc.y})")
      }
    }
  }

  @Test def unsignedMultiplyExact_LongLong_ArithmeticException(): Unit = {
    val x = Long.MIN_VALUE
    assertThrows(
      "overflows unsigned Long",
      classOf[ArithmeticException],
      Math.unsignedMultiplyExact(x, x)
    )
  }

  @Test def unsignedMultiplyExact_LongLong(): Unit = {
    case class unsignedMultiplyExactCase(x: Long, y: Long, expected: Long)

    val m1 = Math.ceil(Math.sqrt(Long.MAX_VALUE)).toLong

    val testCases = java.util.List.of(
      unsignedMultiplyExactCase(m1, m1, -9223372036709301616L),
      unsignedMultiplyExactCase(Long.MAX_VALUE, 2L, -2L)
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.unsignedMultiplyExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException unsignedMultiplyExact(${tc.x}, ${tc.y})")
      }
    }
  }

  @Test def unsignedPowExact_IntInt_ArithmeticExceptions(): Unit = {
    assertThrows(
      "n is negative",
      classOf[ArithmeticException],
      Math.unsignedPowExact(Integer.MAX_VALUE, -1)
    )

    assertThrows(
      "overflows Int",
      classOf[ArithmeticException],
      Math.unsignedPowExact(-1, 3)
    )
  }

  @Test def unsignedPowExact_IntInt(): Unit = {
    case class unsignedPowExactCase(x: Int, y: Int, expected: Int)

    /* Note: Any negative x to a power higher than 1 will cause
     * ArithmeticException because the correct math result overflows.
     */

    val testCases = java.util.List.of(
      unsignedPowExactCase(-1, 0, 1),
      unsignedPowExactCase(-1, 1, -1),
      unsignedPowExactCase(1, 2, 1),
      unsignedPowExactCase(2, 2, 4),
      unsignedPowExactCase(2, 3, 8),
      unsignedPowExactCase(2, 9, 512),
      unsignedPowExactCase(2, 10, 1024),
      unsignedPowExactCase(2, 19, 524288),
      unsignedPowExactCase(3, 3, 27),
      unsignedPowExactCase(3, 5, 243),
      unsignedPowExactCase(
        Math.floor(Math.sqrt(Integer.MAX_VALUE)).toInt,
        2,
        2147395600
      ),
      unsignedPowExactCase(
        Math.ceil(Math.sqrt(Integer.MAX_VALUE)).toInt,
        2,
        -2147479015 // observe the move into unsigned space
      )
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.unsignedPowExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException unsignedPowExact(${tc.x}, ${tc.y})")
      }
    }
  }

  @Test def unsignedPowExact_LongInt(): Unit = {
    case class unsignedPowExactCase(x: Long, y: Int, expected: Long)

    /* Note: Any negative x to a power higher than 1 will cause
     * ArithmeticException because the correct math result overflows.
     */

    val testCases = java.util.List.of(
      unsignedPowExactCase(-1L, 0, 1L),
      unsignedPowExactCase(-1L, 1, -1L),
      unsignedPowExactCase(1L, 2, 1L),
      unsignedPowExactCase(2L, 2, 4L),
      unsignedPowExactCase(2L, 3, 8L),
      unsignedPowExactCase(2L, 9, 512L),
      unsignedPowExactCase(2L, 10, 1024L),
      unsignedPowExactCase(2L, 19, 524288L),
      unsignedPowExactCase(3L, 3, 27L),
      unsignedPowExactCase(3L, 5, 243L),
      unsignedPowExactCase(
        Math.floor(Math.sqrt(Long.MAX_VALUE)).toLong,
        2,
        9223372030926249001L
      ),
      unsignedPowExactCase(
        Math.ceil(Math.sqrt(Long.MAX_VALUE)).toLong,
        2,
        -9223372036709301616L // observe the move into unsigned space
      )
    )

    testCases.forEach { tc =>
      try {
        assertEquals(tc.expected, Math.unsignedPowExact(tc.x, tc.y))
      } catch {
        case e: ArithmeticException =>
          fail(s"ArithmeticException unsignedPowExact(${tc.x}, ${tc.y})")
      }
    }
  }
}
