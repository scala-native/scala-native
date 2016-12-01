package java.lang

object ClassSuite extends tests.Suite {
  test("getComponentType") {
    assert(Array(false).getClass.getComponentType == classOf[scala.Boolean])
    assert(Array('0').getClass.getComponentType == classOf[scala.Char])
    assert(Array(0.toByte).getClass.getComponentType == classOf[scala.Byte])
    assert(Array(0.toShort).getClass.getComponentType == classOf[scala.Short])
    assert(Array(0).getClass.getComponentType == classOf[scala.Int])
    assert(Array(0L).getClass.getComponentType == classOf[scala.Long])
    assert(Array(0F).getClass.getComponentType == classOf[scala.Float])
    assert(Array(0D).getClass.getComponentType == classOf[scala.Double])
    assert(
      Array(new java.lang.Object).getClass.getComponentType == classOf[
        java.lang.Object])
  }
}
