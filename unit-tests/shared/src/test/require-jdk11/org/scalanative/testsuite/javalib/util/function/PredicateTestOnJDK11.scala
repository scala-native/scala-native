// Adapted from SN Java 8 version which was ported from Scala.js
package org.scalanative.testsuite.javalib.util.function

import java.util.function.Predicate

import org.junit.Assert.*
import org.junit.Test

class PredicateTestOnJDK11 {
  import PredicateTestOnJDK11.*

  private val largerThan10 = makePredicate[Int](_ > 10)

  @Test def negate(): Unit = {
    // Truth table
    val notLargerThan10 = Predicate.not(largerThan10)
    assertTrue(notLargerThan10.test(5))
    assertFalse(notLargerThan10.test(15))
  }
}

object PredicateTestOnJDK11 {
  final class ThrowingPredicateException(x: Any)
      extends Exception(s"throwing predicate called with $x")

  def makePredicate[T](f: T => Boolean): Predicate[T] = {
    new Predicate[T] {
      def test(t: T): Boolean = f(t)
    }
  }
}
