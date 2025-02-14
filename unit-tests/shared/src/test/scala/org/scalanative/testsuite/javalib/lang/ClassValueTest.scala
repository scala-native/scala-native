// Ported from Scala.js commit: 3cc6db1 dated: 2020-10-28
// Modified for Scala Native. Unit () is not an AnyRef.

package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._

import java.lang.ClassValue

class ClassValueTest {
  final val testVal = "Test Value"

  @Test def testClassValue(): Unit = {
    type T = AnyRef
    val classValue = new ClassValue[T] {
      private var counter: Int = 0

      // null is the corner-case of Scala Native implementation
      protected def computeValue(cls: Class[_]): T = {
        counter += 1
        cls match {
          case Integer.TYPE           => testVal
          case _ if cls.isPrimitive() => null.asInstanceOf[T]
          case _                      => s"${cls.getName()} ${counter}"
        }
      }
    }

    assertEquals("java.lang.String 1", classValue.get(classOf[String]))
    assertEquals("scala.Option 2", classValue.get(classOf[Option[_]]))
    assertEquals("java.lang.String 1", classValue.get(classOf[String]))
    assertEquals("scala.Option 2", classValue.get(classOf[Option[_]]))

    assertEquals(null, classValue.get(classOf[Boolean])) // insert
    assertEquals(
      null,
      classValue.get(classOf[Boolean])
    ) // lookup, does not touch counter

    assertEquals(testVal, classValue.get(classOf[Int])) // insert
    assertEquals(
      testVal,
      classValue.get(classOf[Int])
    ) // lookup, does not touch counter

    // the counter was incremented exactly twice for the primitives
    assertEquals(
      "scala.collection.immutable.List 5",
      classValue.get(classOf[List[_]])
    )
    assertEquals(
      "scala.collection.immutable.List 5",
      classValue.get(classOf[List[_]])
    )

    assertEquals("java.lang.String 1", classValue.get(classOf[String]))

    classValue.remove(classOf[String])
    assertEquals(
      "scala.collection.immutable.List 5",
      classValue.get(classOf[List[_]])
    )
    assertEquals("java.lang.String 6", classValue.get(classOf[String]))
    assertEquals("java.lang.String 6", classValue.get(classOf[String]))
  }
}
