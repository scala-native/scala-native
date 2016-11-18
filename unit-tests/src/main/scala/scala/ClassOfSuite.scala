package scala

object ClassOfSuite extends tests.Suite {
  test("boxed and unboxed classes are not the same") {
    assert(classOf[Boolean] != classOf[java.lang.Boolean])
    assert(classOf[Byte] != classOf[java.lang.Byte])
    assert(classOf[Char] != classOf[java.lang.Character])
    assert(classOf[Short] != classOf[java.lang.Short])
    assert(classOf[Int] != classOf[java.lang.Integer])
    assert(classOf[Long] != classOf[java.lang.Long])
    assert(classOf[Float] != classOf[java.lang.Float])
    assert(classOf[Double] != classOf[java.lang.Double])
    assert(classOf[Unit] != classOf[scala.runtime.BoxedUnit])
  }
}
