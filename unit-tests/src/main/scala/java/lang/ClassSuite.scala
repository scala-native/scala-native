package java.lang

object ClassSuite extends tests.Suite {

  test("primitives have their own classes") {
    assert(classOf[scala.Boolean] != classOf[java.lang.Boolean])
    assert(classOf[scala.Byte] != classOf[java.lang.Byte])
    assert(classOf[scala.Char] != classOf[java.lang.Character])
    assert(classOf[scala.Short] != classOf[java.lang.Short])
    assert(classOf[scala.Int] != classOf[java.lang.Integer])
    assert(classOf[scala.Long] != classOf[java.lang.Long])
    assert(classOf[scala.Float] != classOf[java.lang.Float])
    assert(classOf[scala.Double] != classOf[java.lang.Double])
    assert(classOf[scala.Unit] != classOf[scala.runtime.BoxedUnit])
  }

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

  test("isPrimitive") {
    assert(classOf[scala.Boolean].isPrimitive)
    assert(classOf[scala.Char].isPrimitive)
    assert(classOf[scala.Byte].isPrimitive)
    assert(classOf[scala.Short].isPrimitive)
    assert(classOf[scala.Int].isPrimitive)
    assert(classOf[scala.Long].isPrimitive)
    assert(classOf[scala.Float].isPrimitive)
    assert(classOf[scala.Double].isPrimitive)
    assert(classOf[scala.Unit].isPrimitive)
    assert(!classOf[java.lang.Object].isPrimitive)
    assert(!classOf[java.lang.String].isPrimitive)
  }

  test("isArray") {
    assert(classOf[Array[scala.Boolean]].isArray)
    assert(classOf[Array[scala.Char]].isArray)
    assert(classOf[Array[scala.Byte]].isArray)
    assert(classOf[Array[scala.Short]].isArray)
    assert(classOf[Array[scala.Int]].isArray)
    assert(classOf[Array[scala.Long]].isArray)
    assert(classOf[Array[scala.Float]].isArray)
    assert(classOf[Array[scala.Double]].isArray)
    assert(classOf[Array[scala.Unit]].isArray)
    assert(classOf[Array[java.lang.Object]].isArray)
    assert(classOf[Array[java.lang.String]].isArray)
    assert(!classOf[java.lang.Object].isArray)
    assert(!classOf[java.lang.String].isArray)
  }
}
