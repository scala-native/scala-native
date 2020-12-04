// Ported from Scala.js commit: 9dc4d5b dated: 2018-10-11

package org.scalanative.testsuite.javalib.util

import org.junit.Test

import java.{util => ju}

import scala.reflect.ClassTag

class ArrayListTest extends AbstractListTest {

  override def factory: AbstractListFactory = new ArrayListFactory

  @Test def `shouldNotFailWithPreAllocationMethods`(): Unit = {
    // note that these methods become no ops in js
    val al = new ju.ArrayList[String]
    al.ensureCapacity(0)
    al.ensureCapacity(34)
    al.trimToSize()
  }
}

class ArrayListFactory extends AbstractListFactory {
  override def implementationName: String =
    "java.util.ArrayList"

  override def empty[E: ClassTag]: ju.ArrayList[E] =
    new ju.ArrayList[E]
}
