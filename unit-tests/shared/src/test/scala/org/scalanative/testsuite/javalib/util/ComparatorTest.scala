// Ported from Scala.js commit SHA1: d94325e dated: 2020-10-08

package org.scalanative.testsuite.javalib.util

import java.util._

import org.junit.Test
import org.junit.Assert._

import java.{util => ju}

class ComparatorTest {
  @Test def reversed(): Unit = {
    class IntComparator extends ju.Comparator[Int] {
      def compare(a: Int, b: Int): Int = {
        /* Using Int.MinValue makes sure that Comparator.reversed() does not
         * use the naive implementation of negating the original comparator's
         * result.
         */
        if (a == b) 0
        else if (a < b) Int.MinValue
        else Int.MaxValue
      }
    }

    val comparator = new IntComparator
    val reversed = comparator.reversed()

    assertEquals(0, reversed.compare(5, 5))
    assertTrue(reversed.compare(3, 1) < 0)
    assertTrue(reversed.compare(6, 8) > 0)
  }
}
