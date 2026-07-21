package org.scalanative.testsuite.javalib.math

import java.math._
import java.{lang => jl, util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class BigIntegerTestOnJDK26 {

  @Test def rootn_Exceptions(): Unit = {
    locally {
      val bi = BigInteger.TWO

      ju.List.of(0, -1).forEach { n =>
        assertThrows(
          s"n: $n <= 0",
          classOf[ArithmeticException],
          bi.rootn(n)
        )
      }
    }

    locally {
      val bi = new BigInteger("-2")
      val n = 4

      assertThrows(
        s"n: $n is even, this ${bi.longValue()} is negative",
        classOf[ArithmeticException],
        bi.rootn(n)
      )
    }
  }

  @Test def rootnThisGeZero(): Unit = {
    locally {
      val expectedRoot = 5L
      val power = 7
      val testValue = jl.Math.powExact(expectedRoot, power)

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }

    locally {
      val expectedRoot = 0L
      val power = 7
      val testValue = expectedRoot // 0 to 7th known to be 0

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }
  }

  @Test def rootnThisLtZero(): Unit = {

    // Exact power, expected remainder 0.
    locally {
      val origin = -3L
      val power = 5
      val expectedRoot = origin

      val testValue = jl.Math.powExact(expectedRoot, power)

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }

    // Less absolute magnitude than an exact power.
    locally {
      val origin = -3L
      val offset = 1
      val power = 3
      val expectedRoot = -2

      val testValue = jl.Math.powExact(origin, power) + offset

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }

    // Greater absolute magnitude than an exact power.
    locally {
      val origin = -3L
      val offset = -2
      val power = 3
      val expectedRoot = origin

      val testValue = jl.Math.powExact(origin, power) + offset

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }
  }

  /* Someday convert this to use a data table then test many values, not
   * just one. Then again, two test points are better than zero.
   */
  @Test def rootnBigIntegerMagnitudeGreaterThanLong(): Unit = {
    locally {
      val expectedRoot = 4294967295L
      val power = 2

      // Exceed capacity of a Long to exercise large BI math.
      val testValue = jl.Long.MAX_VALUE.toString
      val bi = (new BigInteger(testValue)).multiply(BigInteger.TWO)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }

    locally {
      val expectedRoot = -4518177L
      val power = 3

      // Exceed capacity of a Long to exercise large BI math.
      val testValue = jl.Long.MIN_VALUE.toString
      val bi = (new BigInteger(testValue)).multiply(BigInteger.TEN)

      val foundRoot = bi.rootn(power).longValue

      assertEquals(expectedRoot, foundRoot)
    }
  }

  @Test def rootnAndRemainder_Exceptions()(): Unit = {
    locally {
      val bi = BigInteger.TEN

      ju.List.of(0, -2).forEach { n =>
        assertThrows(
          s"n: $n <= 0",
          classOf[ArithmeticException],
          bi.rootnAndRemainder(n)
        )
      }
    }

    locally {
      val bi = new BigInteger("-4")
      val n = 2

      assertThrows(
        s"n: $n is even, this ${bi.longValue()} is negative",
        classOf[ArithmeticException],
        bi.rootnAndRemainder(n)
      )
    }
  }

  @Test def rootnAndRemainderThisGeZero(): Unit = {
    locally {
      val expectedRoot = 5L
      val expectedRemainder = 0L
      val power = 7
      val testValue = jl.Math.powExact(expectedRoot, power)

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }

    locally {
      val expectedRoot = 7L
      val expectedRemainder = 3L
      val power = 4
      val testValue = jl.Math.powExact(expectedRoot, power) + expectedRemainder

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }

    locally {
      val expectedRoot = 7L
      val expectedRemainder = 0L
      val power = 1
      val testValue = jl.Math.powExact(expectedRoot, power)

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }

    locally {
      val expectedRoot = 0L
      val expectedRemainder = 0L
      val power = 7
      val testValue = expectedRoot // 0 to 7th known to be 0

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }
  }

  @Test def rootnAndRemainderThisLtZero(): Unit = {
    // Exact power, expected remainder 0.
    locally {
      val origin = -3L
      val expectedRoot = origin
      val expectedRemainder = 0L
      val power = 5

      val testValue = jl.Math.powExact(expectedRoot, power)

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }

    // Less absolute magnitude than an exact power.
    locally {
      val origin = -3L
      val expectedRoot = -2
      val expectedRemainder = -18L
      val offset = 1L
      val power = 3

      val testValue = jl.Math.powExact(origin, power) + offset

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }

    // Greater absolute magnitude than an exact power.
    locally {
      val origin = -3L
      val expectedRoot = origin
      val expectedRemainder = -3L
      val offset = expectedRemainder
      val power = 3

      val testValue = jl.Math.powExact(origin, power) + offset

      val bi = new BigInteger(testValue.toString)

      val result = bi.rootnAndRemainder(power)

      assertEquals(expectedRoot, result(0).longValue)
      assertEquals(expectedRemainder, result(1).longValue)
    }
  }
}
