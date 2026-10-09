package org.scalanative.testsuite.javalib.util

import java.util.{function => juf}
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ComparatorTestOnJDK26 {

  /* Note:
   *   Declarations such as the two below are used to test that
   *   the JDK documented ClassCastException is thrown at time.
   *     val s = "xyz"
   *     val blivet = java.lang.Long.valueOf(3)
   *
   *   Context: using a 'Comparator[AnyVal]'
   *
   *   Scala 2.12 and 2.13 fail to compile on JVM and Scala Native with idiom
   *     'cmp.max(blivet, s)'.
   *   Both require
   *     'cmp.max(blivet.asInstanceOf[AnyVal], s.asInstanceOf[AnyVal])'
   *
   *   Scala 3 compiles both forms.
   *
   *   Once compiled, the Tests for ClassCastException detect the expected
   *   Exception at runtime.
   *
   *   This file uses the Scala 2.n form so that it can run on all
   *   currently supported Scala versions.
   */

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
      cmp.max(s.asInstanceOf[AnyVal], blivet.asInstanceOf[AnyVal])
    )

    assertThrows(
      "cmp.max(blivet, s)",
      classOf[ClassCastException],
      cmp.max(blivet.asInstanceOf[AnyVal], s.asInstanceOf[AnyVal])
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
      cmp.min(s.asInstanceOf[AnyVal], blivet.asInstanceOf[AnyVal])
    )

    assertThrows(
      "cmp.min(blivet, s)",
      classOf[ClassCastException],
      cmp.min(blivet.asInstanceOf[AnyVal], s.asInstanceOf[AnyVal])
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
