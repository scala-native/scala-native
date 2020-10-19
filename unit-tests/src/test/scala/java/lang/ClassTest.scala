package java.lang

import org.junit.Test
import org.junit.Assert._

class ClassTest {

  @Test def primitivesHaveTheirOwnClasses(): Unit = {
    assertTrue(classOf[scala.Boolean] != classOf[java.lang.Boolean])
    assertTrue(classOf[scala.Byte] != classOf[java.lang.Byte])
    assertTrue(classOf[scala.Char] != classOf[java.lang.Character])
    assertTrue(classOf[scala.Short] != classOf[java.lang.Short])
    assertTrue(classOf[scala.Int] != classOf[java.lang.Integer])
    assertTrue(classOf[scala.Long] != classOf[java.lang.Long])
    assertTrue(classOf[scala.Float] != classOf[java.lang.Float])
    assertTrue(classOf[scala.Double] != classOf[java.lang.Double])
    assertTrue(classOf[scala.Unit] != classOf[scala.runtime.BoxedUnit])
  }

  @Test def getComponentType(): Unit = {
    assertTrue(Array(false).getClass.getComponentType == classOf[scala.Boolean])
    assertTrue(Array('0').getClass.getComponentType == classOf[scala.Char])
    assertTrue(Array(0.toByte).getClass.getComponentType == classOf[scala.Byte])
    assertTrue(
      Array(0.toShort).getClass.getComponentType == classOf[scala.Short])
    assertTrue(Array(0).getClass.getComponentType == classOf[scala.Int])
    assertTrue(Array(0L).getClass.getComponentType == classOf[scala.Long])
    assertTrue(Array(0F).getClass.getComponentType == classOf[scala.Float])
    assertTrue(Array(0D).getClass.getComponentType == classOf[scala.Double])
    assertTrue(
      Array(new java.lang.Object).getClass.getComponentType == classOf[
        java.lang.Object])
  }

  @Test def isPrimitive(): Unit = {
    assertTrue(classOf[scala.Boolean].isPrimitive)
    assertTrue(classOf[scala.Char].isPrimitive)
    assertTrue(classOf[scala.Byte].isPrimitive)
    assertTrue(classOf[scala.Short].isPrimitive)
    assertTrue(classOf[scala.Int].isPrimitive)
    assertTrue(classOf[scala.Long].isPrimitive)
    assertTrue(classOf[scala.Float].isPrimitive)
    assertTrue(classOf[scala.Double].isPrimitive)
    assertTrue(classOf[scala.Unit].isPrimitive)
    assertFalse(classOf[java.lang.Object].isPrimitive)
    assertFalse(classOf[java.lang.String].isPrimitive)
  }

  @Test def isArray(): Unit = {
    assertTrue(classOf[Array[scala.Boolean]].isArray)
    assertTrue(classOf[Array[scala.Char]].isArray)
    assertTrue(classOf[Array[scala.Byte]].isArray)
    assertTrue(classOf[Array[scala.Short]].isArray)
    assertTrue(classOf[Array[scala.Int]].isArray)
    assertTrue(classOf[Array[scala.Long]].isArray)
    assertTrue(classOf[Array[scala.Float]].isArray)
    assertTrue(classOf[Array[scala.Double]].isArray)
    assertTrue(classOf[Array[scala.Unit]].isArray)
    assertTrue(classOf[Array[java.lang.Object]].isArray)
    assertTrue(classOf[Array[java.lang.String]].isArray)
    assertFalse(classOf[java.lang.Object].isArray)
    assertFalse(classOf[java.lang.String].isArray)
  }

  class A extends X
  class B extends A with Y
  class C
  trait X
  trait Y extends X
  trait Z

  @Test def isInstance(): Unit = {
    assertTrue(classOf[A].isInstance(new A))
    assertTrue(classOf[A].isInstance(new B))
    assertFalse(classOf[A].isInstance(new C))
    assertFalse(classOf[B].isInstance(new A))
    assertTrue(classOf[B].isInstance(new B))
    assertFalse(classOf[B].isInstance(new C))
    assertFalse(classOf[C].isInstance(new A))
    assertFalse(classOf[C].isInstance(new B))
    assertTrue(classOf[C].isInstance(new C))
    assertTrue(classOf[X].isInstance(new A))
    assertTrue(classOf[X].isInstance(new B))
    assertFalse(classOf[X].isInstance(new C))
    assertFalse(classOf[Y].isInstance(new A))
    assertTrue(classOf[Y].isInstance(new B))
    assertFalse(classOf[Y].isInstance(new C))
    assertFalse(classOf[Z].isInstance(new A))
    assertFalse(classOf[Z].isInstance(new B))
    assertFalse(classOf[Z].isInstance(new C))
  }

  @Test def isAssignableFrom(): Unit = {
    assertTrue(classOf[A].isAssignableFrom(classOf[A]))
    assertTrue(classOf[A].isAssignableFrom(classOf[B]))
    assertFalse(classOf[A].isAssignableFrom(classOf[C]))
    assertFalse(classOf[A].isAssignableFrom(classOf[X]))
    assertFalse(classOf[A].isAssignableFrom(classOf[Y]))
    assertFalse(classOf[A].isAssignableFrom(classOf[Z]))
    assertFalse(classOf[B].isAssignableFrom(classOf[A]))
    assertTrue(classOf[B].isAssignableFrom(classOf[B]))
    assertFalse(classOf[B].isAssignableFrom(classOf[C]))
    assertFalse(classOf[B].isAssignableFrom(classOf[X]))
    assertFalse(classOf[B].isAssignableFrom(classOf[Y]))
    assertFalse(classOf[B].isAssignableFrom(classOf[Z]))
    assertFalse(classOf[C].isAssignableFrom(classOf[A]))
    assertFalse(classOf[C].isAssignableFrom(classOf[B]))
    assertTrue(classOf[C].isAssignableFrom(classOf[C]))
    assertFalse(classOf[C].isAssignableFrom(classOf[X]))
    assertFalse(classOf[C].isAssignableFrom(classOf[Y]))
    assertFalse(classOf[C].isAssignableFrom(classOf[Z]))
    assertTrue(classOf[X].isAssignableFrom(classOf[A]))
    assertTrue(classOf[X].isAssignableFrom(classOf[B]))
    assertFalse(classOf[X].isAssignableFrom(classOf[C]))
    assertTrue(classOf[X].isAssignableFrom(classOf[X]))
    assertTrue(classOf[X].isAssignableFrom(classOf[Y]))
    assertFalse(classOf[X].isAssignableFrom(classOf[Z]))
    assertFalse(classOf[Y].isAssignableFrom(classOf[A]))
    assertTrue(classOf[Y].isAssignableFrom(classOf[B]))
    assertFalse(classOf[Y].isAssignableFrom(classOf[C]))
    assertFalse(classOf[Y].isAssignableFrom(classOf[X]))
    assertTrue(classOf[Y].isAssignableFrom(classOf[Y]))
    assertFalse(classOf[Y].isAssignableFrom(classOf[Z]))
    assertFalse(classOf[Z].isAssignableFrom(classOf[A]))
    assertFalse(classOf[Z].isAssignableFrom(classOf[B]))
    assertFalse(classOf[Z].isAssignableFrom(classOf[C]))
    assertFalse(classOf[Z].isAssignableFrom(classOf[X]))
    assertFalse(classOf[Z].isAssignableFrom(classOf[Y]))
    assertTrue(classOf[Z].isAssignableFrom(classOf[Z]))
  }

  @Test def testToString(): Unit = {
    assertTrue(classOf[java.lang.Class[_]].toString == "class java.lang.Class")
    assertTrue(
      classOf[java.lang.Runnable].toString == "interface java.lang.Runnable")
  }

  @Test def isInterface(): Unit = {
    assertFalse(classOf[java.lang.Class[_]].isInterface)
    assertTrue(classOf[java.lang.Runnable].isInterface)
  }
}
