// Ported from Scala.js commit: a6c1451 dated: 2021-10-16

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Test

class HashSetTest extends AbstractSetTest {
  def factory: HashSetFactory = new HashSetFactory

  // Issue #5327
  @Test def containsAllAndEqualsWithAnotherSet(): Unit = {
    def setOf(elems: String*): ju.Set[String] = {
      val set = factory.empty[String]
      set.addAll(ju.Arrays.asList(elems: _*))
      set
    }
    assertTrue(setOf("x").containsAll(ju.Arrays.asList("x")))
    assertTrue(setOf("x", "y").containsAll(setOf("y", "x")))
    assertFalse(setOf("x").containsAll(setOf("x", "y")))
    assertEquals(setOf("x", "y"), setOf("y", "x"))
    assertNotEquals(setOf("x", "y"), setOf("x", "z"))
  }
}

class HashSetFactory extends AbstractSetFactory {
  def implementationName: String =
    "java.util.HashSet"

  def empty[E: ClassTag]: ju.HashSet[E] =
    new ju.HashSet[E]()
}
