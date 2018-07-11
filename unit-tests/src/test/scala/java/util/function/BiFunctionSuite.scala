package java.util.function

object BiFunctionSuite extends tests.Suite {
  val f = new BiFunction[Integer, Integer, Integer] {
    override def apply(x: Integer, y: Integer): Integer = {
      x + y
    }
  }

  test("BiFunction.apply(Integer, Integer)") {
    assertEquals(f(1, 2), 3)
  }

  // andThen doesn't work currently
  /*
  val ff = new Function[Integer, Integer] {
    override def apply(x: Integer): Integer = x + 1
  }

  test("BiFunction.andThen") {
    assertEquals(f.andThen(ff)(1, 2), 4)
  }
 */
}
