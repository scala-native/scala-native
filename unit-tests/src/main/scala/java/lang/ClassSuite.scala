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

  class A extends X
  class B extends A with Y
  class C
  trait X
  trait Y extends X
  trait Z

  test("isInstance") {
    assert(classOf[A].isInstance(new A))
    assert(classOf[A].isInstance(new B))
    assert(!classOf[A].isInstance(new C))
    assert(!classOf[B].isInstance(new A))
    assert(classOf[B].isInstance(new B))
    assert(!classOf[B].isInstance(new C))
    assert(!classOf[C].isInstance(new A))
    assert(!classOf[C].isInstance(new B))
    assert(classOf[C].isInstance(new C))
    assert(classOf[X].isInstance(new A))
    assert(classOf[X].isInstance(new B))
    assert(!classOf[X].isInstance(new C))
    assert(!classOf[Y].isInstance(new A))
    assert(classOf[Y].isInstance(new B))
    assert(!classOf[Y].isInstance(new C))
    assert(!classOf[Z].isInstance(new A))
    assert(!classOf[Z].isInstance(new B))
    assert(!classOf[Z].isInstance(new C))
  }

  test("isAssignableFrom") {
    assert(classOf[A].isAssignableFrom(classOf[A]))
    assert(classOf[A].isAssignableFrom(classOf[B]))
    assert(!classOf[A].isAssignableFrom(classOf[C]))
    assert(!classOf[A].isAssignableFrom(classOf[X]))
    assert(!classOf[A].isAssignableFrom(classOf[Y]))
    assert(!classOf[A].isAssignableFrom(classOf[Z]))
    assert(!classOf[B].isAssignableFrom(classOf[A]))
    assert(classOf[B].isAssignableFrom(classOf[B]))
    assert(!classOf[B].isAssignableFrom(classOf[C]))
    assert(!classOf[B].isAssignableFrom(classOf[X]))
    assert(!classOf[B].isAssignableFrom(classOf[Y]))
    assert(!classOf[B].isAssignableFrom(classOf[Z]))
    assert(!classOf[C].isAssignableFrom(classOf[A]))
    assert(!classOf[C].isAssignableFrom(classOf[B]))
    assert(classOf[C].isAssignableFrom(classOf[C]))
    assert(!classOf[C].isAssignableFrom(classOf[X]))
    assert(!classOf[C].isAssignableFrom(classOf[Y]))
    assert(!classOf[C].isAssignableFrom(classOf[Z]))
    assert(classOf[X].isAssignableFrom(classOf[A]))
    assert(classOf[X].isAssignableFrom(classOf[B]))
    assert(!classOf[X].isAssignableFrom(classOf[C]))
    assert(classOf[X].isAssignableFrom(classOf[X]))
    assert(classOf[X].isAssignableFrom(classOf[Y]))
    assert(!classOf[X].isAssignableFrom(classOf[Z]))
    assert(!classOf[Y].isAssignableFrom(classOf[A]))
    assert(classOf[Y].isAssignableFrom(classOf[B]))
    assert(!classOf[Y].isAssignableFrom(classOf[C]))
    assert(!classOf[Y].isAssignableFrom(classOf[X]))
    assert(classOf[Y].isAssignableFrom(classOf[Y]))
    assert(!classOf[Y].isAssignableFrom(classOf[Z]))
    assert(!classOf[Z].isAssignableFrom(classOf[A]))
    assert(!classOf[Z].isAssignableFrom(classOf[B]))
    assert(!classOf[Z].isAssignableFrom(classOf[C]))
    assert(!classOf[Z].isAssignableFrom(classOf[X]))
    assert(!classOf[Z].isAssignableFrom(classOf[Y]))
    assert(classOf[Z].isAssignableFrom(classOf[Z]))
  }

  test("toString") {
    assert(classOf[java.lang.Class[_]].toString == "class java.lang.Class")
    assert(
      classOf[java.lang.Runnable].toString == "interface java.lang.Runnable")
  }

  test("isInterface") {
    assert(!classOf[java.lang.Class[_]].isInterface)
    assert(classOf[java.lang.Runnable].isInterface)
  }
}
