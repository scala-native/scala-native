package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.{Ignore, Test}
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
      Array(0.toShort).getClass.getComponentType == classOf[scala.Short]
    )
    assertTrue(Array(0).getClass.getComponentType == classOf[scala.Int])
    assertTrue(Array(0L).getClass.getComponentType == classOf[scala.Long])
    assertTrue(Array(0f).getClass.getComponentType == classOf[scala.Float])
    assertTrue(Array(0d).getClass.getComponentType == classOf[scala.Double])
    assertTrue(
      Array(new java.lang.Object).getClass.getComponentType == classOf[
        java.lang.Object
      ]
    )
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

  @Test def isAssignableFrom2(): Unit = {
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Byte]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Short]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Int]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Long]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Float]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Double]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Unit]))
    assertFalse(classOf[Any].isAssignableFrom(classOf[scala.Boolean]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[String]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Byte]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Short]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Integer]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Long]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Float]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Double]))
    assertTrue(classOf[Any].isAssignableFrom(classOf[java.lang.Boolean]))
  }

  @Test def testToString(): Unit = {
    assertEquals(
      "class java.lang.Class",
      classOf[java.lang.Class[_]].toString()
    )
    assertEquals(
      "interface java.lang.Runnable",
      classOf[java.lang.Runnable].toString()
    )
    assertEquals("byte", classOf[scala.Byte].toString())
    assertEquals("short", classOf[scala.Short].toString())
    assertEquals("char", classOf[scala.Char].toString())
    assertEquals("int", classOf[scala.Int].toString())
    assertEquals("long", classOf[scala.Long].toString())
    assertEquals("float", classOf[scala.Float].toString())
    assertEquals("double", classOf[scala.Double].toString())
    assertEquals("boolean", classOf[scala.Boolean].toString())
    // assertEquals("size", classOf[scala.scalanative.runtime.RawSize].toString())
    // assertEquals("pointer", classOf[scala.scalanative.runtime.RawPtr].toString())
  }

  @Test def isInterface(): Unit = {
    assertFalse(classOf[java.lang.Class[_]].isInterface)
    assertTrue(classOf[java.lang.Runnable].isInterface)
  }

  @Test def getInterfaces(): Unit = {
    def hasInterfaces(cls: Class[_], expected: Set[Class[_]]) = {
      val interfaces = cls.getInterfaces().toSet
      val diff = interfaces.diff(expected)
      assertTrue(
        s"For ${cls}\nExpected: ${expected}\nGot:${interfaces}",
        diff.isEmpty
      )
    }
    hasInterfaces(classOf[A], Set(classOf[X]))
    hasInterfaces(classOf[B], Set(classOf[X], classOf[Y]))
    hasInterfaces(classOf[C], Set())
    hasInterfaces(classOf[X], Set())
    hasInterfaces(classOf[Y], Set(classOf[X]))
  }

  @Test def getSuperClass(): Unit = {
    def checkSuperClassOf(cls: Class[_], expected: Class[_]) =
      assertEquals(cls.toString(), expected, cls.getSuperclass())
    checkSuperClassOf(classOf[A], classOf[AnyRef])
    checkSuperClassOf(classOf[B], classOf[A])
    checkSuperClassOf(classOf[C], classOf[AnyRef])
    checkSuperClassOf(classOf[X], null)
    checkSuperClassOf(classOf[Y], null)
  }

  private def assertDiffClass(
      l: java.lang.Class[_],
      r: java.lang.Class[_]
  ): Unit = {
    assertTrue(s"$l eq $r", l ne r)
  }

  private def assertEqualClass(
      l: java.lang.Class[_],
      r: java.lang.Class[_]
  ): Unit = {
    assertTrue(s"$l ne $r", l eq r)
  }

  @Test def classInstancesAreCache(): Unit = {

    val cls1 = "asd".getClass
    val cls2 = "xyz".getClass

    assertEqualClass(classOf[String], cls1)
    assertEqualClass(cls1, cls2)
    assertEqualClass(classOf[String], classOf[String])

    assertTrue(123L.getClass != 42.getClass)
  }

  @Test def distinguishableClassInstancesForPrimitiveArrays(): Unit = {
    val cls1 = Array.empty[Int].getClass
    val cls2 = Array.empty[Array[Int]].getClass

    assertEqualClass(cls1, cls1)
    assertEqualClass(cls1, classOf[Array[Int]])
    assertEqualClass(cls2, classOf[Array[Array[Int]]])
    assertDiffClass(cls1, cls2)

    assertDiffClass(cls1, classOf[Array[scala.Long]])
    assertDiffClass(cls1, classOf[Array[scala.Boolean]])
    assertDiffClass(cls1, classOf[Array[scala.Float]])
    assertDiffClass(cls1, classOf[Array[scala.Double]])
    assertDiffClass(cls1, classOf[Array[scala.Short]])
    assertDiffClass(cls1, classOf[Array[scala.Byte]])
    assertDiffClass(cls1, classOf[Array[Object]])
  }

  @Ignore("#1435, nested arrays are stored as ObjectArray")
  @Test def distinguishableClassInstancesForNestedPrimitiveArrays(): Unit = {
    val cls1 = classOf[Array[Array[Int]]]
    val cls2 = classOf[Array[Array[Array[Int]]]]
    assertDiffClass(cls1, cls2)
  }

  @Ignore("#1435, all object arrays are classOf[ObjectArray]")
  @Test def distinguishableClassInstancesForNestedObjectArrays(): Unit = {
    val cls1 = Array.empty[String].getClass
    val cls2 = Array.empty[Array[String]].getClass

    assertDiffClass(cls1, cls2)
  }

  @Test def classForName(): Unit = {
    val cls1 = Class.forName("java.lang.String")
    val cls2 = classOf[String]
    val cls3 = Class.forName("java.lang.Double")

    assertEqualClass(cls1, cls2)
    assertDiffClass(cls1, cls3)

    assertThrows(
      classOf[ClassNotFoundException],
      () => Class.forName("not.existing.class.name")
    )
  }

  @Test def getClassLoader(): Unit = {
    val cl = getClass().getClassLoader()
    assertTrue(cl != null)
  }
}
