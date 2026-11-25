package org.scalanative.testsuite.javalib.lang

import java.io.Serializable
import java.lang.reflect.{Array => JArray}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* java.lang.Class of array types: identity, names, component types,
 * supertypes and assignability. Every array type has exactly one Class
 * instance, whichever way it is obtained, and it knows its element type and
 * number of dimensions.
 */
object ArrayClassTest {
  class Elem
  class SubElem extends Elem
  trait Marker
  class MarkedElem extends Elem with Marker

  // Element types whose arrays appear only through reflection
  class OnlyReflective
  class OnlyForName
}

class ArrayClassTest {
  import ArrayClassTest._

  private def assertSameClass(expected: Class[_], actual: Class[_]): Unit = {
    assertEquals(expected, actual)
    assertSame(expected, actual)
    assertEquals(
      System.identityHashCode(expected),
      System.identityHashCode(actual)
    )
  }

  private def assertDifferentClass(a: Class[_], b: Class[_]): Unit = {
    assertNotEquals(a, b)
    assertNotSame(a, b)
  }

  // Identity

  @Test def classOfArrayEqualsGetClassOfInstance(): Unit = {
    assertSameClass(classOf[Array[String]], new Array[String](0).getClass)
    assertSameClass(classOf[Array[String]], Array("a", "b").getClass)
    assertSameClass(classOf[Array[Elem]], new Array[Elem](3).getClass)
    assertSameClass(classOf[Array[Marker]], new Array[Marker](3).getClass)
    assertSameClass(classOf[Array[Int]], new Array[Int](1).getClass)
    assertSameClass(
      classOf[Array[Array[Int]]],
      new Array[Array[Int]](1).getClass
    )
    assertSameClass(
      classOf[Array[Array[String]]],
      new Array[Array[String]](1).getClass
    )
    assertSameClass(
      classOf[Array[Array[Array[Elem]]]],
      new Array[Array[Array[Elem]]](1).getClass
    )
  }

  @Test def classOfArrayEqualsReflectivelyCreatedClass(): Unit = {
    assertSameClass(
      classOf[Array[String]],
      JArray.newInstance(classOf[String], 0).getClass
    )
    assertSameClass(
      classOf[Array[Elem]],
      JArray.newInstance(classOf[Elem], 2).getClass
    )
    assertSameClass(
      classOf[Array[Array[Int]]],
      JArray.newInstance(classOf[Array[Int]], 2).getClass
    )
    assertSameClass(
      classOf[Array[Array[Int]]],
      JArray.newInstance(classOf[Int], 2, 3).getClass
    )
    assertSameClass(
      classOf[Array[Array[Array[String]]]],
      JArray.newInstance(classOf[String], 1, 1, 1).getClass
    )
  }

  @Test def arrayTypesCreatedOnlyAtRuntimeAreCanonical(): Unit = {
    val c1 = JArray.newInstance(classOf[OnlyReflective], 0).getClass
    val c2 = JArray.newInstance(classOf[OnlyReflective], 5).getClass
    assertSameClass(c1, c2)
    assertSameClass(classOf[OnlyReflective], c1.getComponentType)
    assertSameClass(c1, JArray.newInstance(c1.getComponentType, 1).getClass)

    val c3 = JArray.newInstance(c1, 0).getClass
    val c4 = JArray.newInstance(classOf[OnlyReflective], 1, 1).getClass
    assertSameClass(c3, c4)
    assertSameClass(c1, c3.getComponentType)
    assertDifferentClass(c1, c3)
  }

  @Test def distinctArrayTypesHaveDistinctClasses(): Unit = {
    val classes = Seq[Class[_]](
      classOf[Array[Object]],
      classOf[Array[String]],
      classOf[Array[Elem]],
      classOf[Array[SubElem]],
      classOf[Array[Marker]],
      classOf[Array[Int]],
      classOf[Array[Long]],
      classOf[Array[java.lang.Integer]],
      classOf[Array[Array[Object]]],
      classOf[Array[Array[String]]],
      classOf[Array[Array[Int]]],
      classOf[Array[Array[Long]]],
      classOf[Array[Array[Array[Int]]]],
      classOf[Array[Array[Array[Object]]]]
    )
    for {
      (a, i) <- classes.zipWithIndex
      (b, j) <- classes.zipWithIndex
      if i != j
    } assertDifferentClass(a, b)
  }

  // Names

  @Test def getNameOfPrimitiveArrays(): Unit = {
    assertEquals("[Z", classOf[Array[scala.Boolean]].getName)
    assertEquals("[C", classOf[Array[scala.Char]].getName)
    assertEquals("[B", classOf[Array[scala.Byte]].getName)
    assertEquals("[S", classOf[Array[scala.Short]].getName)
    assertEquals("[I", classOf[Array[scala.Int]].getName)
    assertEquals("[J", classOf[Array[scala.Long]].getName)
    assertEquals("[F", classOf[Array[scala.Float]].getName)
    assertEquals("[D", classOf[Array[scala.Double]].getName)
    assertEquals("[I", new Array[Int](0).getClass.getName)
  }

  @Test def getNameOfReferenceArrays(): Unit = {
    assertEquals("[Ljava.lang.Object;", classOf[Array[Object]].getName)
    assertEquals("[Ljava.lang.String;", classOf[Array[String]].getName)
    assertEquals("[Ljava.lang.String;", Array("a").getClass.getName)
    assertEquals(
      "[Ljava.lang.Integer;",
      classOf[Array[java.lang.Integer]].getName
    )
    assertEquals(
      "[Lscala.runtime.BoxedUnit;",
      classOf[Array[scala.Unit]].getName
    )
    assertEquals(
      "[L" + classOf[Elem].getName + ";",
      classOf[Array[Elem]].getName
    )
  }

  @Test def getNameOfMultiDimensionalArrays(): Unit = {
    assertEquals("[[I", classOf[Array[Array[Int]]].getName)
    assertEquals("[[[D", classOf[Array[Array[Array[Double]]]].getName)
    assertEquals("[[Ljava.lang.String;", classOf[Array[Array[String]]].getName)
    assertEquals("[[I", JArray.newInstance(classOf[Int], 1, 1).getClass.getName)
    assertEquals(
      "[[[Ljava.lang.Object;",
      JArray.newInstance(classOf[Object], 1, 1, 1).getClass.getName
    )
  }

  @Test def getSimpleNameOfArrays(): Unit = {
    assertEquals("int[]", classOf[Array[Int]].getSimpleName)
    assertEquals("int[][]", classOf[Array[Array[Int]]].getSimpleName)
    assertEquals("String[]", classOf[Array[String]].getSimpleName)
    assertEquals("String[][]", classOf[Array[Array[String]]].getSimpleName)
    assertEquals("Elem[]", classOf[Array[Elem]].getSimpleName)
  }

  @Test def getTypeNameOfArrays(): Unit = {
    assertEquals("int[]", classOf[Array[Int]].getTypeName)
    assertEquals(
      "java.lang.String[][]",
      classOf[Array[Array[String]]].getTypeName
    )
    assertEquals("java.lang.String", classOf[String].getTypeName)
  }

  @Test def toStringOfArrays(): Unit = {
    assertEquals("class [I", classOf[Array[Int]].toString)
    assertEquals("class [Ljava.lang.String;", classOf[Array[String]].toString)
    assertEquals("class [[J", classOf[Array[Array[Long]]].toString)
  }

  // Component type and dimensions

  @Test def getComponentTypeOfPrimitiveArrays(): Unit = {
    assertSameClass(classOf[Int], classOf[Array[Int]].getComponentType)
    assertSameClass(
      classOf[Array[Int]],
      classOf[Array[Array[Int]]].getComponentType
    )
    assertSameClass(
      classOf[Array[Array[Double]]],
      classOf[Array[Array[Array[Double]]]].getComponentType
    )
  }

  @Test def getComponentTypeOfReferenceArrays(): Unit = {
    assertSameClass(classOf[Object], classOf[Array[Object]].getComponentType)
    assertSameClass(classOf[Object], classOf[Array[Any]].getComponentType)
    assertSameClass(classOf[String], classOf[Array[String]].getComponentType)
    assertSameClass(classOf[String], Array("a").getClass.getComponentType)
    assertSameClass(classOf[Elem], new Array[Elem](1).getClass.getComponentType)
    assertSameClass(classOf[Marker], classOf[Array[Marker]].getComponentType)
    assertSameClass(
      classOf[java.lang.Long],
      new Array[java.lang.Long](5).getClass.getComponentType
    )
    assertSameClass(
      classOf[scala.runtime.BoxedUnit],
      classOf[Array[Unit]].getComponentType
    )
    assertSameClass(
      classOf[Array[String]],
      classOf[Array[Array[String]]].getComponentType
    )
    assertSameClass(
      classOf[Array[Object]],
      JArray.newInstance(classOf[Object], 1, 1).getClass.getComponentType
    )
  }

  @Test def getComponentTypeOfNonArraysIsNull(): Unit = {
    assertNull(classOf[Object].getComponentType)
    assertNull(classOf[String].getComponentType)
    assertNull(classOf[Int].getComponentType)
  }

  @Test def walkingComponentTypesGivesTheDimensions(): Unit = {
    def dimensions(cls: Class[_]): Int = {
      var c = cls
      var n = 0
      while (c.isArray) { c = c.getComponentType; n += 1 }
      n
    }
    def innermost(cls: Class[_]): Class[_] = {
      var c = cls
      while (c.isArray) c = c.getComponentType
      c
    }
    assertEquals(1, dimensions(classOf[Array[String]]))
    assertEquals(3, dimensions(classOf[Array[Array[Array[String]]]]))
    assertEquals(2, dimensions(Array.ofDim[Int](2, 2).getClass))
    assertEquals(
      4,
      dimensions(JArray.newInstance(classOf[Long], 1, 1, 1, 1).getClass)
    )
    assertSameClass(classOf[Long], innermost(classOf[Array[Array[Long]]]))
    assertSameClass(classOf[Elem], innermost(classOf[Array[Array[Elem]]]))
  }

  @Test def maximalNumberOfDimensionsIs255(): Unit = {
    val dims255 = JArray.newInstance(classOf[Int], Array.fill(255)(0): _*)
    var c: Class[_] = dims255.getClass
    var n = 0
    while (c.isArray) { c = c.getComponentType; n += 1 }
    assertEquals(255, n)
    assertSameClass(classOf[Int], c)

    assertThrows(
      classOf[IllegalArgumentException],
      JArray.newInstance(dims255.getClass, 0)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.newInstance(classOf[Int], Array.fill(256)(0): _*)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      JArray.newInstance(classOf[Array[Int]], Array.fill(255)(0): _*)
    )
  }

  // Kind, supertypes and interfaces

  @Test def arrayClassKind(): Unit = {
    for (cls <- Seq[Class[_]](
          classOf[Array[Int]],
          classOf[Array[String]],
          classOf[Array[Array[Int]]],
          classOf[Array[Marker]],
          JArray.newInstance(classOf[OnlyReflective], 0).getClass
        )) {
      assertTrue(cls.getName, cls.isArray)
      assertFalse(cls.getName, cls.isInterface)
      assertFalse(cls.getName, cls.isPrimitive)
    }
  }

  @Test def getSuperclassOfArraysIsObject(): Unit = {
    assertSameClass(classOf[Object], classOf[Array[Int]].getSuperclass)
    assertSameClass(classOf[Object], classOf[Array[Object]].getSuperclass)
    assertSameClass(classOf[Object], classOf[Array[String]].getSuperclass)
    assertSameClass(classOf[Object], classOf[Array[SubElem]].getSuperclass)
    assertSameClass(classOf[Object], classOf[Array[Array[Int]]].getSuperclass)
  }

  @Test def getInterfacesOfArrays(): Unit = {
    val expected =
      Set[Class[_]](classOf[java.lang.Cloneable], classOf[Serializable])
    for (cls <- Seq[Class[_]](
          classOf[Array[Int]],
          classOf[Array[String]],
          classOf[Array[Array[Elem]]]
        )) {
      val interfaces = cls.getInterfaces
      assertEquals(cls.getName, 2, interfaces.length)
      assertEquals(cls.getName, expected, interfaces.toSet)
    }
  }

  // Assignability, following the subtyping rules of arrays in the JLS

  private def assertAssignable(to: Class[_], from: Class[_]): Unit =
    assertTrue(
      s"${to.getName} should be assignable from ${from.getName}",
      to.isAssignableFrom(from)
    )

  private def assertNotAssignable(to: Class[_], from: Class[_]): Unit =
    assertFalse(
      s"${to.getName} should not be assignable from ${from.getName}",
      to.isAssignableFrom(from)
    )

  @Test def arraysAreObjectsCloneableAndSerializable(): Unit = {
    for (cls <- Seq[Class[_]](
          classOf[Array[Int]],
          classOf[Array[String]],
          classOf[Array[Array[Int]]],
          classOf[Array[Marker]]
        )) {
      assertAssignable(classOf[Object], cls)
      assertAssignable(classOf[java.lang.Cloneable], cls)
      assertAssignable(classOf[Serializable], cls)
      assertNotAssignable(classOf[Comparable[_]], cls)
      assertNotAssignable(cls, classOf[Object])
    }
  }

  @Test def referenceArraysAreCovariant(): Unit = {
    assertAssignable(classOf[Array[Object]], classOf[Array[String]])
    assertAssignable(classOf[Array[CharSequence]], classOf[Array[String]])
    assertAssignable(classOf[Array[Comparable[_]]], classOf[Array[String]])
    assertAssignable(classOf[Array[Serializable]], classOf[Array[String]])
    assertAssignable(classOf[Array[Elem]], classOf[Array[SubElem]])
    assertAssignable(classOf[Array[Marker]], classOf[Array[MarkedElem]])
    assertAssignable(classOf[Array[Number]], classOf[Array[java.lang.Integer]])
    assertAssignable(classOf[Array[Runnable]], classOf[Array[Thread]])

    assertNotAssignable(classOf[Array[String]], classOf[Array[Object]])
    assertNotAssignable(classOf[Array[SubElem]], classOf[Array[Elem]])
    assertNotAssignable(classOf[Array[Marker]], classOf[Array[Elem]])
    assertNotAssignable(classOf[Array[Elem]], classOf[Array[Marker]])
    assertNotAssignable(
      classOf[Array[java.lang.Integer]],
      classOf[Array[Number]]
    )
    assertNotAssignable(
      classOf[Array[java.lang.Long]],
      classOf[Array[java.lang.Integer]]
    )
    assertNotAssignable(classOf[Array[CharSequence]], classOf[Array[Object]])
  }

  @Test def primitiveArraysAreInvariant(): Unit = {
    assertNotAssignable(classOf[Array[Object]], classOf[Array[Int]])
    assertNotAssignable(classOf[Array[Long]], classOf[Array[Int]])
    assertNotAssignable(classOf[Array[Int]], classOf[Array[Short]])
    assertNotAssignable(classOf[Array[java.lang.Integer]], classOf[Array[Int]])
    assertNotAssignable(classOf[Array[Int]], classOf[Array[java.lang.Integer]])
    assertNotAssignable(classOf[Array[Int]], classOf[Array[Object]])
  }

  @Test def multiDimensionalArraysAreArraysOfObjects(): Unit = {
    // int[][] is an Object[], its elements being int[]
    assertAssignable(classOf[Array[Object]], classOf[Array[Array[Int]]])
    assertAssignable(
      classOf[Array[java.lang.Cloneable]],
      classOf[Array[Array[Int]]]
    )
    assertAssignable(
      classOf[Array[Serializable]],
      classOf[Array[Array[String]]]
    )
    assertAssignable(classOf[Array[Object]], classOf[Array[Array[String]]])
    assertAssignable(
      classOf[Array[Array[Object]]],
      classOf[Array[Array[String]]]
    )
    assertAssignable(
      classOf[Array[Array[Object]]],
      classOf[Array[Array[Array[Int]]]]
    )
    assertAssignable(
      classOf[Array[Array[Elem]]],
      classOf[Array[Array[SubElem]]]
    )

    assertNotAssignable(
      classOf[Array[Array[Object]]],
      classOf[Array[Array[Int]]]
    )
    assertNotAssignable(classOf[Array[Array[Object]]], classOf[Array[Object]])
    assertNotAssignable(classOf[Array[Array[Object]]], classOf[Array[String]])
    assertNotAssignable(classOf[Array[Array[Int]]], classOf[Array[Array[Long]]])
    assertNotAssignable(classOf[Array[Array[Int]]], classOf[Array[Object]])
    assertNotAssignable(
      classOf[Array[Array[Int]]],
      classOf[Array[Array[Array[Int]]]]
    )
    assertNotAssignable(classOf[Array[String]], classOf[Array[Array[String]]])
    assertNotAssignable(
      classOf[Array[Comparable[_]]],
      classOf[Array[Array[String]]]
    )
  }

  @Test def isInstanceFollowsAssignability(): Unit = {
    val strings: Object = Array("a")
    val objects: Object = Array[Object]("a")
    val ints: Object = Array(1)
    val intMatrix: Object = Array.ofDim[Int](1, 1)
    val subElems: Object = Array(new SubElem)

    assertTrue(classOf[Array[String]].isInstance(strings))
    assertTrue(classOf[Array[Object]].isInstance(strings))
    assertTrue(classOf[Array[CharSequence]].isInstance(strings))
    assertFalse(classOf[Array[String]].isInstance(objects))
    assertFalse(classOf[Array[Integer]].isInstance(objects))

    assertTrue(classOf[Array[Int]].isInstance(ints))
    assertFalse(classOf[Array[Object]].isInstance(ints))
    assertFalse(classOf[Array[Long]].isInstance(ints))

    assertTrue(classOf[Array[Object]].isInstance(intMatrix))
    assertTrue(classOf[Array[Array[Int]]].isInstance(intMatrix))
    assertFalse(classOf[Array[Array[Object]]].isInstance(intMatrix))
    assertFalse(classOf[Array[Int]].isInstance(intMatrix))

    assertTrue(classOf[Array[Elem]].isInstance(subElems))
    assertFalse(classOf[Array[MarkedElem]].isInstance(subElems))
    assertFalse(classOf[Array[Marker]].isInstance(subElems))
  }

  // Class.forName with binary names of array types

  @Test def forNameOfArrayTypes(): Unit = {
    assertSameClass(classOf[Array[Int]], Class.forName("[I"))
    assertSameClass(classOf[Array[Array[Int]]], Class.forName("[[I"))
    assertSameClass(
      classOf[Array[String]],
      Class.forName("[Ljava.lang.String;")
    )
    assertSameClass(
      classOf[Array[Array[Object]]],
      Class.forName("[[Ljava.lang.Object;")
    )
    val reflective =
      Class.forName("[L" + classOf[OnlyForName].getName + ";")
    assertSameClass(classOf[OnlyForName], reflective.getComponentType)
    assertSameClass(
      reflective,
      JArray.newInstance(classOf[OnlyForName], 0).getClass
    )
  }

  @Test def forNameOfMalformedArrayTypes(): Unit = {
    for (name <- Seq(
          "[",
          "[[",
          "[Q",
          "[Ljava.lang.String",
          "[Lnot.existing.Clazz;",
          "int[]",
          "java.lang.String[]"
        ))
      assertThrows(classOf[ClassNotFoundException], Class.forName(name))
  }
}
