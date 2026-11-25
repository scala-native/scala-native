package org.scalanative.testsuite.runtime

import java.lang.ref.WeakReference

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.runtime.{
  ArrayClasses, ByteArray, IntArray, ObjectArray
}

/* Classes of arrays of references: defined by the linker for array types
 * used by the program, created at runtime for others, see
 * scala.scalanative.runtime.ArrayClasses
 */
class ArrayClassesTest {
  import ArrayClassesTest._

  // No code mentions arrays of OnlyReflective, the linker does not see them
  @Test def classesOfArrayTypesUnknownToTheLinker(): Unit = {
    val component: Class[_] = (new OnlyReflective: AnyRef).getClass
    val array = java.lang.reflect.Array
      .newInstance(component, 64)
      .asInstanceOf[Array[AnyRef]]
    val cls = array.getClass
    assertSame(cls, ArrayClasses.of(component))
    assertEquals("[L" + component.getName() + ";", cls.getName())
    assertSame(component, cls.getComponentType())
    assertTrue((array: AnyRef).isInstanceOf[ObjectArray])
    assertTrue(classOf[Array[AnyRef]].isAssignableFrom(cls))
    assertFalse(classOf[Array[String]].isAssignableFrom(cls))
    assertSame(cls, array.clone().getClass)

    for (i <- 0 until array.length) array(i) = new OnlyReflective
    val refs = array.map(new WeakReference(_))
    for (_ <- 0 until 5) {
      Seq.fill(1000)(new Array[Byte](64))
      System.gc()
    }
    refs.foreach(ref => assertNotNull(ref.get()))

    val nested = java.lang.reflect.Array.newInstance(cls, 1).getClass
    assertSame(cls, nested.getComponentType())
    assertEquals("[[L" + component.getName() + ";", nested.getName())
    assertSame(nested, Class.forName(nested.getName()))
  }

  @Test def classesAreCreatedOnceAndCached(): Unit = {
    val cls = ArrayClasses.of(classOf[Component])
    assertSame(cls, ArrayClasses.of(classOf[Component]))
    assertSame(cls, new Array[Component](0).getClass)
    assertSame(classOf[Component], cls.getComponentType)
    val array: AnyRef = new Array[Component](0)
    assertTrue(array.isInstanceOf[ObjectArray])
  }

  @Test def classesOfPrimitivesAndObjectAreTheRuntimeArrayClasses(): Unit = {
    assertSame(classOf[IntArray], ArrayClasses.of(classOf[scala.Int]))
    assertSame(classOf[ByteArray], ArrayClasses.of(classOf[scala.Byte]))
    assertSame(classOf[ObjectArray], ArrayClasses.of(classOf[Object]))
  }

  // The name is a heap object referenced only by the class, outside the heap
  @Test def nameOfARuntimeClassSurvivesCollections(): Unit = {
    val name = new WeakReference(
      ArrayClasses.of(classOf[NamedAtRuntime]).getName()
    )
    for (_ <- 0 until 5) {
      Seq.fill(1000)(new Array[Byte](64))
      System.gc()
    }
    assertNotNull("the name of the class was collected", name.get())
    assertEquals(
      "[L" + classOf[NamedAtRuntime].getName() + ";",
      ArrayClasses.of(classOf[NamedAtRuntime]).getName()
    )
  }

  @Test def elementsOfArraysWithRuntimeClassesAreScanned(): Unit = {
    val arrays = Array.fill(16)(Array.fill(64)(new Component))
    val refs = arrays.map(a => new WeakReference(a(0)))
    for (_ <- 0 until 5) {
      Seq.fill(1000)(new Array[Byte](64))
      System.gc()
    }
    refs.foreach(ref => assertNotNull(ref.get()))
    arrays.foreach(a => assertSame(classOf[Array[Component]], a.getClass))
  }
}

object ArrayClassesTest {
  private class Component
  private class NamedAtRuntime
  private class OnlyReflective
}
