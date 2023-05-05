package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._
import java.util.stream.Collector.Characteristics

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CollectorTest {
  @Test def collecterCharacteristicsEnum(): Unit = {
    assertEquals("values", 3, Characteristics.values().size)

    assertEquals("CONCURRENT", 0, Characteristics.valueOf("CONCURRENT").ordinal)

    assertEquals("UNORDERED", 1, Characteristics.valueOf("UNORDERED").ordinal)
    assertEquals(
      "IDENTITY_FINISH",
      2,
      Characteristics.valueOf("IDENTITY_FINISH").ordinal
    )

    assertThrows(
      classOf[IllegalArgumentException],
      Characteristics.valueOf("<cause exception>").ordinal
    )
  }

}
