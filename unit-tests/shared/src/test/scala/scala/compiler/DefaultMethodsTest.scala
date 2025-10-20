// Ported from Scala.js commit SHA1: ac06525 dated: 2016-01-05

package org.scalanative.testsuite.compiler

import org.junit.Test
import org.junit.Assert.*

import java.util as ju

class DefaultMethodsTest {

  @Test def canOverrideDefaultMethod(): Unit = {
    var counter = 0

    class SpecialIntComparator extends ju.Comparator[Int] {
      def compare(o1: Int, o2: Int): Int =
        o1.compareTo(o2)

      override def reversed(): ju.Comparator[Int] = {
        counter += 1
        super.reversed()
      }
    }

    val c = new SpecialIntComparator
    assertTrue(c.compare(5, 7) < 0)
    assertEquals(0, counter)

    val reversed = c.reversed()
    assertEquals(1, counter)
    assertTrue(reversed.compare(5, 7) > 0)
  }
}
