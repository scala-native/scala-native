// Ported from Scala.js commit: 6fbe7b2 dated: 2019-08-14

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Test
import org.junit.Assert._

import scala.reflect.ClassTag

class LinkedHashSetTest extends HashSetTest {

  override def factory: LinkedHashSetFactory = new LinkedHashSetFactory

  @Test def shouldIterateOverElementsInSnOrderedManner(): Unit = {
    val hs = factory.empty[String]

    val l1 = TrivialImmutableCollection("ONE", "TWO", null)
    assertTrue(hs.addAll(l1))
    assertEquals(3, hs.size)

    val iter1 = hs.iterator()
    for (i <- 0 until 3) {
      assertTrue(iter1.hasNext())
      assertEquals(l1(i), iter1.next())
    }
    assertFalse(iter1.hasNext())

    val l2 = TrivialImmutableCollection("ONE", "TWO", null, "THREE")
    assertTrue(hs.add(l2(3)))

    val iter2 = hs.iterator()
    for (i <- 0 until 4) {
      assertTrue(iter2.hasNext())
      assertEquals(l2(i), iter2.next())
    }
    assertFalse(iter2.hasNext())
  }

}

class LinkedHashSetFactory extends HashSetFactory {
  override def implementationName: String =
    "java.util.LinkedHashSet"

  override def empty[E: ClassTag]: ju.LinkedHashSet[E] =
    new ju.LinkedHashSet[E]()
}
