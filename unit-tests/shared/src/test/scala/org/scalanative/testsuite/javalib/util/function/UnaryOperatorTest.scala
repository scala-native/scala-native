package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Test
import org.junit.Assert._

class UnaryOperatorTest {
  @Test def testUnaryOperator(): Unit = {
    val unaryOperatorString: UnaryOperator[String] = UnaryOperator.identity()
    assertTrue(unaryOperatorString.apply("scala") == "scala")
  }
}
