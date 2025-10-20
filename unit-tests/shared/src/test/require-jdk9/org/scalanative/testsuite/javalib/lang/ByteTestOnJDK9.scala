package org.scalanative.testsuite.javalib.lang

import java.lang as jl

import org.junit.Assert.*
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ByteTestOnJDK9 {
  @Test def compareUnsigned(): Unit = {
    val byteZero = 0.toByte

    assertTrue(
      "compare signed",
      jl.Byte.compare(jl.Byte.MIN_VALUE, byteZero) < 0
    )

    assertTrue(
      "compare unsigned",
      jl.Byte.compareUnsigned(jl.Byte.MIN_VALUE, byteZero) > 0
    )
  }
}
