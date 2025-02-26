package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{lang => jl}

class MathTestOnJDK15 {

  @Test def absExactInt(): Unit = {

    assertThrows(
      "jl.Integer.MIN_VALUE",
      classOf[ArithmeticException],
      Math.absExact(Integer.MIN_VALUE)
    )

    assertEquals(
      s"unexpected absolute value",
      1,
      Math.absExact(-1)
    )
  }

  @Test def absExactLong(): Unit = {

    assertThrows(
      "jl.Long.MIN_VALUE",
      classOf[ArithmeticException],
      Math.absExact(jl.Long.MIN_VALUE)
    )

    assertEquals(
      s"unexpected absolute value",
      Integer.MAX_VALUE + 1L,
      Math.absExact(-Integer.MAX_VALUE - 1L)
    )
  }

}
