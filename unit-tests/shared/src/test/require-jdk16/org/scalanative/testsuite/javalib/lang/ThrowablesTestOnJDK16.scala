package org.scalanative.testsuite.javalib.lang

import java.util.Objects
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ThrowablesTestOnJDK16 {

  @Test def indexOutOfBoundsException_Long(): Unit = {
    val ioobExc = new IndexOutOfBoundsException(jl.Long.MAX_VALUE)
    Objects.requireNonNull(ioobExc, "Long index")

    // Check Exception message visually, since can rightly vary across JVMs.
    assertThrows(
      "IndexOutOfBounds Long",
      classOf[java.lang.IndexOutOfBoundsException],
      throw ioobExc
    )
  }
}
