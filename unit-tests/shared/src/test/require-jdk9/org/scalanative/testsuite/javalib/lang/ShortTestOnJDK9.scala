package org.scalanative.testsuite.javalib.lang

import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ShortTestOnJDK9 {

  @Test def compareUnsigned(): Unit = {
    val shortZero = 0.toShort

    assertTrue(
      "compare signed",
      jl.Short.compare(jl.Short.MIN_VALUE, shortZero) < 0
    )

    assertTrue(
      "compare unsigned",
      jl.Short.compareUnsigned(jl.Short.MIN_VALUE, shortZero) > 0
    )
  }
}
