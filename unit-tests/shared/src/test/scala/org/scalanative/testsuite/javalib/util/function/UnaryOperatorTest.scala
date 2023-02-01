// Ported from Scala.js, commit SHA: cbf86bbb8 dated: 2020-10-23
package org.scalanative.testsuite.javalib.util.function

import java.util.function.UnaryOperator

import org.junit.Assert._
import org.junit.Test

class UnaryOperatorTest {
  import UnaryOperatorTest._

  @Test def identity(): Unit = {
    val unaryOperatorString: UnaryOperator[String] = UnaryOperator.identity()
    assertEquals("scala", unaryOperatorString.apply("scala"))
  }

  @Test def createAndApply(): Unit = {
    val double: UnaryOperator[Int] = makeUnaryOperator(_ * 2)
    assertEquals(20, double.apply(10))
    assertEquals(20, double.apply(10))
  }
}

object UnaryOperatorTest {
  private def makeUnaryOperator[T](f: T => T): UnaryOperator[T] = {
    new UnaryOperator[T] {
      def apply(t: T): T = f(t)
    }
  }
}
