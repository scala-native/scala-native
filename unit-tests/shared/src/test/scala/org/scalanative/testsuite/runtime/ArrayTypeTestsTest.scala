package org.scalanative.testsuite.runtime

import java.io.Serializable
import java.lang.reflect.{Array => JArray}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// Values go through opaque helpers and directly: covers checks and folding
object ArrayTypeTestsTest {
  import TypeTestsTest._

  class Elem
  class SubElem extends Elem
  trait Marker
  final class MarkedElem extends Elem with Marker

  @noinline def opaque(x: AnyRef): AnyRef = x

  def kind(x: Any): String = x match {
    case _: Array[String]        => "String[]"
    case _: Array[Array[Int]]    => "int[][]"
    case _: Array[Array[String]] => "String[][]"
    case _: Array[Int]           => "int[]"
    case _: Array[Elem]          => "Elem[]"
    case _: Array[AnyRef]        => "Object[]"
    case _: Array[_]             => "array"
    case _                       => "other"
  }

  def deepChain: Array[D17] = Array(new D17)
}

class ArrayTypeTestsTest {
  import ArrayTypeTestsTest._
  import TypeTestsTest._

  @Test def isInstanceOfReferenceArrays(): Unit = {
    val strings = opaque(Array("a"))
    val objects = opaque(new Array[AnyRef](1))
    val subElems = opaque(Array(new SubElem))

    assertTrue(strings.isInstanceOf[Array[String]])
    assertTrue(strings.isInstanceOf[Array[AnyRef]])
    assertTrue(strings.isInstanceOf[Array[CharSequence]])
    assertTrue(strings.isInstanceOf[Array[Comparable[_]]])
    assertTrue(strings.isInstanceOf[Serializable])
    assertTrue(strings.isInstanceOf[java.lang.Cloneable])
    assertFalse(strings.isInstanceOf[Array[Elem]])
    assertFalse(strings.isInstanceOf[Array[Array[String]]])

    assertTrue(objects.isInstanceOf[Array[AnyRef]])
    assertFalse(objects.isInstanceOf[Array[String]])
    assertFalse(objects.isInstanceOf[Array[Elem]])
    assertFalse(objects.isInstanceOf[Array[Array[AnyRef]]])

    assertTrue(subElems.isInstanceOf[Array[Elem]])
    assertTrue(subElems.isInstanceOf[Array[SubElem]])
    assertFalse(subElems.isInstanceOf[Array[MarkedElem]])
    assertFalse(subElems.isInstanceOf[Array[Marker]])
  }

  @Test def isInstanceOfPrimitiveAndNestedArrays(): Unit = {
    val ints = opaque(Array(1))
    val intMatrix = opaque(Array.ofDim[Int](1, 1))
    val stringCube = opaque(Array.ofDim[String](1, 1, 1))

    assertTrue(ints.isInstanceOf[Array[Int]])
    assertFalse(ints.isInstanceOf[Array[AnyRef]])
    assertFalse(ints.isInstanceOf[Array[Long]])
    assertFalse(ints.isInstanceOf[Array[Array[Int]]])

    assertTrue(intMatrix.isInstanceOf[Array[Array[Int]]])
    assertTrue(intMatrix.isInstanceOf[Array[AnyRef]])
    assertTrue(intMatrix.isInstanceOf[Array[java.lang.Cloneable]])
    assertFalse(intMatrix.isInstanceOf[Array[Int]])
    assertFalse(intMatrix.isInstanceOf[Array[Array[AnyRef]]])
    assertFalse(intMatrix.isInstanceOf[Array[Array[Long]]])

    assertTrue(stringCube.isInstanceOf[Array[Array[Array[String]]]])
    assertTrue(stringCube.isInstanceOf[Array[Array[Array[AnyRef]]]])
    assertTrue(stringCube.isInstanceOf[Array[Array[AnyRef]]])
    assertTrue(stringCube.isInstanceOf[Array[Serializable]])
    assertFalse(stringCube.isInstanceOf[Array[Array[String]]])
    assertFalse(stringCube.isInstanceOf[Array[Array[Array[Array[AnyRef]]]]])
  }

  @Test def isInstanceOfWithStaticallyKnownAllocation(): Unit = {
    // The optimizer sees the allocation and must not fold these to true
    val objects: AnyRef = new Array[AnyRef](1)
    assertFalse(objects.isInstanceOf[Array[String]])
    val elems: AnyRef = new Array[Elem](1)
    assertFalse(elems.isInstanceOf[Array[SubElem]])
    val matrix: AnyRef = new Array[Array[Int]](1)
    assertFalse(matrix.isInstanceOf[Array[Array[Long]]])
    assertTrue(matrix.isInstanceOf[Array[AnyRef]])
    val strings: AnyRef = Array("a")
    assertTrue(strings.isInstanceOf[Array[AnyRef]])
    assertFalse(strings.isInstanceOf[Array[Elem]])
  }

  @Test def isInstanceOfWithDeepElementHierarchies(): Unit = {
    // Element classes deeper than the lineage stored inline in RTTI
    val deep = opaque(deepChain)
    assertTrue(deep.isInstanceOf[Array[D17]])
    assertTrue(deep.isInstanceOf[Array[D8]])
    assertTrue(deep.isInstanceOf[Array[D0]])
    assertTrue(deep.isInstanceOf[Array[Base3]])
    assertFalse(deep.isInstanceOf[Array[Sibling]])
    assertFalse(deep.isInstanceOf[Array[NeverImplemented]])
    val shallow = opaque(Array(new D8))
    assertFalse(shallow.isInstanceOf[Array[D17]])
    assertTrue(shallow.isInstanceOf[Array[Base1]])
  }

  @Test def asInstanceOfArrays(): Unit = {
    val strings = opaque(Array("a"))
    val objects = opaque(Array[AnyRef]("a"))
    val intMatrix = opaque(Array.ofDim[Int](1, 1))

    assertEquals(1, strings.asInstanceOf[Array[String]].length)
    assertEquals(1, strings.asInstanceOf[Array[AnyRef]].length)
    assertEquals(1, intMatrix.asInstanceOf[Array[AnyRef]].length)

    assertThrows(
      classOf[ClassCastException],
      objects.asInstanceOf[Array[String]].length
    )
    assertThrows(
      classOf[ClassCastException],
      strings.asInstanceOf[Array[Elem]].length
    )
    assertThrows(
      classOf[ClassCastException],
      intMatrix.asInstanceOf[Array[Array[AnyRef]]].length
    )
    assertThrows(
      classOf[ClassCastException],
      intMatrix.asInstanceOf[Array[Array[Long]]].length
    )
  }

  @Test def classCastExceptionMessageNamesArrayTypes(): Unit = {
    val objects = opaque(Array[AnyRef]("a"))
    val e = assertThrows(
      classOf[ClassCastException],
      objects.asInstanceOf[Array[String]].length
    )
    assertNotNull(e.getMessage)
    assertTrue(e.getMessage, e.getMessage.contains("[Ljava.lang.Object;"))
    assertTrue(e.getMessage, e.getMessage.contains("[Ljava.lang.String;"))
  }

  @Test def patternMatchingOnArrayTypes(): Unit = {
    assertEquals("String[]", kind(Array("a")))
    assertEquals("int[][]", kind(Array.ofDim[Int](1, 1)))
    assertEquals("String[][]", kind(Array.ofDim[String](1, 1)))
    assertEquals("int[]", kind(Array(1)))
    assertEquals("Elem[]", kind(Array(new SubElem)))
    assertEquals("Object[]", kind(Array[AnyRef]("a")))
    assertEquals("Object[]", kind(Array.ofDim[Long](1, 1)))
    assertEquals("Object[]", kind(Array(new Object)))
    assertEquals("array", kind(Array(1L)))
    assertEquals("other", kind("a"))
    assertEquals(
      "String[]",
      kind(JArray.newInstance(classOf[String], 1))
    )
    assertEquals(
      "Elem[]",
      kind(JArray.newInstance(classOf[MarkedElem], 1))
    )
  }

  @Test def castThroughClass(): Unit = {
    val strings: Object = Array("a")
    val objects: Object = Array[Object]("a")
    assertSame(strings, classOf[Array[Object]].cast(strings))
    assertSame(strings, classOf[Array[String]].cast(strings))
    assertThrows(
      classOf[ClassCastException],
      classOf[Array[String]].cast(objects)
    )
  }

  @Test def arrayApplyKeepsElementType(): Unit = {
    def make[T: scala.reflect.ClassTag](xs: T*): Array[T] = Array[T](xs: _*)
    val ints = make(1, 2, 3)
    assertSame(classOf[Array[Int]], ints.getClass)
    assertEquals(List(1, 2, 3), ints.toList)
    val strings = make("a", "b")
    assertSame(classOf[Array[String]], strings.getClass)
  }
}
