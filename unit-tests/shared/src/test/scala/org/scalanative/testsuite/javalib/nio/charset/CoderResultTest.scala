package org.scalanative.testsuite.javalib.nio.charset

import java.nio.charset.CoderResult

import org.junit.Assert._
import org.junit.Test

class CoderResultTest {

  @Test def toString_Malformed(): Unit = {

    // Scala Native Issue #4297
    assertEquals(
      "MALFORMED[3]",
      CoderResult.malformedForLength(3).toString()
    )

    assertEquals(
      "OVERFLOW",
      CoderResult.OVERFLOW.toString()
    )

    assertEquals(
      "UNDERFLOW",
      CoderResult.UNDERFLOW.toString()
    )

    assertEquals(
      "UNMAPPABLE[4]",
      CoderResult.unmappableForLength(4).toString()
    )
  }
}
