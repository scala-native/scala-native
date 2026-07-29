package org.scalanative.testsuite.javalib.util

import java.util.{function => juf}
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ComparatorTestOnJDK26 {

  @Test def max_Exceptions_NPE(): Unit = {
    val cmp = ju.Comparator.naturalOrder[String]()
    val s = "xyz"

    assertThrows(
      "cmp.max(null, s)",
      classOf[NullPointerException],
      cmp.max(null, s)
    )

    assertThrows(
      "cmp.max(s, null)",
      classOf[NullPointerException],
      cmp.max(s, null)
    )
  }

  @Test def max_Exceptions_ClassCast(): Unit = {
    class TestComparator extends ju.Comparator[AnyVal] {
      def compare(v1: AnyVal, v2: AnyVal): Int =
        throw new ClassCastException("max")
    }

    val cmp = new TestComparator
    val s = "xyz"
    val blivet = java.lang.Long.valueOf(3)

    assertThrows(
      "cmp.max(s, blivet)",
      classOf[ClassCastException],
      cmp.max(s, blivet)
    )

    assertThrows(
      "cmp.max(blivet, s)",
      classOf[ClassCastException],
      cmp.max(blivet, s)
    )
  }

  @Test def max(): Unit = {
    val cmp = ju.Comparator.naturalOrder[String]()
    val s1 = "abc"
    val s2 = "aec"

    locally {
      assertTrue(
        s"""max("${s1}", "${s2}") should be "${s2}"""",
        cmp.max(s1, s2).equals(s2)
      )

      assertTrue(
        s"""max("${s2}", "${s1}") should be "${s2}"""",
        cmp.max(s2, s1).equals(s2)
      )
    }
  }

  @Test def min_Exceptions_NPE(): Unit = {
    val cmp = ju.Comparator.naturalOrder[String]()
    val s = "xyz"

    assertThrows(
      "cmp.min(null, s)",
      classOf[NullPointerException],
      cmp.min(null, s)
    )

    assertThrows(
      "cmp.min(s, null)",
      classOf[NullPointerException],
      cmp.min(s, null)
    )
  }

  @Test def min_Exceptions_ClassCast(): Unit = {
    class TestComparator extends ju.Comparator[AnyVal] {
      def compare(v1: AnyVal, v2: AnyVal): Int =
        throw new ClassCastException("min")
    }

    val cmp = new TestComparator
    val s = "xyz"
    val blivet = java.lang.Long.valueOf(3)

    assertThrows(
      "cmp.min(s, blivet)",
      classOf[ClassCastException],
      cmp.min(s, blivet)
    )

    assertThrows(
      "cmp.min(blivet, s)",
      classOf[ClassCastException],
      cmp.min(blivet, s)
    )
  }

  @Test def min(): Unit = {
    val cmp = ju.Comparator.naturalOrder[String]()
    val s1 = "abc"
    val s2 = "aec"

    assertTrue(
      s"""min("${s1}", "${s2}") should be "${s1}"""",
      cmp.min(s1, s2).equals(s1)
    )

    assertTrue(
      s"""min("${s2}", "${s1}") should be "${s1}"""",
      cmp.min(s2, s1).equals(s1)
    )
  }
}
