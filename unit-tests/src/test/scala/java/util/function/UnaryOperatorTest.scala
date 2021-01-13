package java.util.function

import org.junit.Test
import org.junit.Assert._

class UnaryOperatorTest {
  @Test def testUnaryOperator(): Unit = {
    val unaryOperatorString: UnaryOperator[String] = UnaryOperator.identity()
    assertTrue(unaryOperatorString.apply("scala") == "scala")
  }
}
