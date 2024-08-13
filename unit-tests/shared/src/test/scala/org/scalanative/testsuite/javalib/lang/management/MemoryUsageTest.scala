package org.scalanative.testsuite.javalib.lang.management

import java.lang.management._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class MemoryUsageTest {

  @Test def failWhenRequirementsFail(): Unit = {
    assertThrows(
      classOf[java.lang.IllegalArgumentException],
      new MemoryUsage(-2L, 0L, 0L, 0L)
    )

    assertThrows(
      classOf[java.lang.IllegalArgumentException],
      new MemoryUsage(0L, -2L, 0L, 0L)
    )

    assertThrows(
      classOf[java.lang.IllegalArgumentException],
      new MemoryUsage(0L, 0L, -2L, 0L)
    )

    assertThrows(
      classOf[java.lang.IllegalArgumentException],
      new MemoryUsage(0L, 0L, 0L, -2L)
    )
  }

  @Test def constructAnInstance(): Unit = {
    val memoryUsage = new MemoryUsage(1024L, 2048L, 4096L, 8192L)

    assertTrue(memoryUsage.getInit == 1024L)
    assertTrue(memoryUsage.getUsed == 2048L)
    assertTrue(memoryUsage.getCommitted == 4096L)
    assertTrue(memoryUsage.getMax == 8192L)
  }

  @Test def properToString(): Unit = {
    val memoryUsage = new MemoryUsage(1024L, 2048L, 4096L, 8192L)
    val expected =
      "init = 1024(1K) used = 2048(2K) committed = 4096(4K) max = 8192(8K)"

    assertTrue(memoryUsage.toString == expected)
  }

}
