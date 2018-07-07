package java.util.function

object UnaryOperatorSuite extends tests.Suite {
  test("UnaryOperator") {
    val unaryOperatorString: UnaryOperator[String] = UnaryOperator.identity()
    assert(unaryOperatorString.apply("scala") == "scala")
  }
}
