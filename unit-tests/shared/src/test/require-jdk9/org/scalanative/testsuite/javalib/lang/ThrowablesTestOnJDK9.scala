package org.scalanative.testsuite.javalib.lang

import java.util.Objects

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ThrowablesTestOnJDK9 {

  @Test def indexOutOfBoundsException_Int(): Unit = {
    val ioobExc = new IndexOutOfBoundsException(66)
    Objects.requireNonNull(ioobExc, "Int index")

    // Check Exception message visually, since can rightly vary across JVMs.
    assertThrows(
      "IndexOutOfBounds Int",
      classOf[java.lang.IndexOutOfBoundsException],
      throw ioobExc
    )
  }
}
