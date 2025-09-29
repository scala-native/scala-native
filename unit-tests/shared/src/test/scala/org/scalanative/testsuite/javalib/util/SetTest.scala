// Ported from Scala.js commit: a6c1451 dated: 2021-10-16
//
// 'equalsOnlyOtherSets' Test added for Scala Native.

package org.scalanative.testsuite.javalib.util

import java.{lang => jl, util => ju}

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import Utils._

trait SetTest extends CollectionTest {

  def factory: SetFactory

  @Test def size(): Unit = {
    val hs = factory.empty[String]

    assertEquals(0, hs.size())
    assertTrue(hs.add("ONE"))
    assertEquals(1, hs.size())
    assertTrue(hs.add("TWO"))
    assertEquals(2, hs.size())
  }

  @Test def addInt(): Unit = {
    val hs = factory.empty[Int]

    assertTrue(hs.add(100))
    assertEquals(1, hs.size())
    assertTrue(hs.contains(100))
    assertEquals(100, hs.iterator.next())
  }

  @Test def addAnyRefCustomObjectsWithSameHashCode(): Unit = {
    val hs = factory.empty[AnyRef]
    trait A extends Comparable[A] {
      def compareTo(o: A): Int = toString.compareTo(o.toString)
    }
    object B extends A {
      override def hashCode(): Int = 42
    }
    object C extends A {
      override def hashCode(): Int = 42
    }

    assertTrue(hs.add(B))
    assertTrue(hs.add(C))
    assertEquals(2, hs.size())
  }

  @Test def addDoubleCornerCases(): Unit = {
    val hs = factory.empty[Double]

    assertTrue(hs.add(11111.0))
    assertEquals(1, hs.size())
    assertTrue(hs.contains(11111.0))
    assertEquals(11111.0, hs.iterator.next(), 0.0)

    assertTrue(hs.add(Double.NaN))
    assertEquals(2, hs.size())
    assertTrue(hs.contains(Double.NaN))
    assertFalse(hs.contains(+0.0))
    assertFalse(hs.contains(-0.0))

    assertTrue(hs.remove(Double.NaN))
    assertTrue(hs.add(+0.0))
    assertEquals(2, hs.size())
    assertFalse(hs.contains(Double.NaN))
    assertTrue(hs.contains(+0.0))
    assertFalse(hs.contains(-0.0))

    assertTrue(hs.remove(+0.0))
    assertTrue(hs.add(-0.0))
    assertEquals(2, hs.size())
    assertFalse(hs.contains(Double.NaN))
    assertFalse(hs.contains(+0.0))
    assertTrue(hs.contains(-0.0))

    assertTrue(hs.add(+0.0))
    assertTrue(hs.add(Double.NaN))
    assertTrue(hs.contains(Double.NaN))
    assertTrue(hs.contains(+0.0))
    assertTrue(hs.contains(-0.0))
  }

  @Test def addCustomClass(): Unit = {
    case class TestObj(num: Int) extends jl.Comparable[TestObj] {
      override def compareTo(o: TestObj): Int = o.num - num
    }

    val hs = factory.empty[TestObj]

    assertTrue(hs.add(TestObj(100)))
    assertEquals(1, hs.size())
    assertTrue(hs.contains(TestObj(100)))
    assertEquals(100, hs.iterator.next().num)
  }

  @Test def removeRemoveAllRetainAll(): Unit = {
    val hs = factory.empty[String]

    assertEquals(0, hs.size())
    assertTrue(hs.add("ONE"))
    assertFalse(hs.add("ONE"))
    assertEquals(1, hs.size())
    assertTrue(hs.add("TWO"))
    assertEquals(2, hs.size())
    assertTrue(hs.remove("ONE"))
    assertFalse(hs.remove("ONE"))
    assertEquals(1, hs.size())
    assertTrue(hs.remove("TWO"))
    assertEquals(0, hs.size())

    assertTrue(hs.add("ONE"))
    assertTrue(hs.add("TWO"))
    assertEquals(2, hs.size())
    assertTrue(hs.removeAll(TrivialImmutableCollection("ONE", "TWO")))
    assertEquals(0, hs.size())

    assertTrue(hs.add("ONE"))
    assertTrue(hs.add("TWO"))
    assertEquals(2, hs.size())
    assertTrue(hs.retainAll(TrivialImmutableCollection("ONE", "THREE")))
    assertEquals(1, hs.size())
    assertTrue(hs.contains("ONE"))
    assertFalse(hs.contains("TWO"))
  }

  @Test def clearSet(): Unit = {
    val hs = factory.empty[String]

    assertTrue(hs.add("ONE"))
    assertTrue(hs.add("TWO"))
    assertEquals(2, hs.size())

    hs.clear()
    assertEquals(0, hs.size())
    assertTrue(hs.isEmpty)
  }

  @Test def contains(): Unit = {
    val hs = factory.empty[String]

    assertTrue(hs.add("ONE"))
    assertTrue(hs.contains("ONE"))
    assertFalse(hs.contains("TWO"))
    if (factory.allowsNullElement) {
      assertFalse(hs.contains(null))
      assertTrue(hs.add(null))
      assertTrue(hs.contains(null))
    } else {
      assertThrows(classOf[Exception], hs.add(null))
    }
  }

  @Test def addAllCollectionStringSet(): Unit = {
    val hs = factory.empty[String]

    val l = TrivialImmutableCollection("ONE", "TWO", null)

    if (factory.allowsNullElement) {
      assertTrue(hs.addAll(l))
      assertEquals(3, hs.size)
      assertTrue(hs.contains("ONE"))
      assertTrue(hs.contains("TWO"))
      assertTrue(hs.contains(null))
    } else {
      assertThrows(classOf[Exception], hs.addAll(l))
    }
  }

  @Test def iterator(): Unit = {
    val hs = factory.empty[String]

    val l = {
      if (factory.allowsNullElement)
        List("ONE", "TWO", null)
      else
        List("ONE", "TWO", "THREE")
    }
    assertTrue(hs.addAll(TrivialImmutableCollection(l: _*)))
    assertEquals(3, hs.size)

    assertIteratorSameElementsAsSet(l: _*)(hs.iterator())
  }

  @Test def equalsOnlyOtherSets(): Unit = {
    val hsUut = factory.empty[String] // Unit under test
    val hsMatch = factory.empty[String]
    val hsNoMatch = factory.empty[String]

    val l = {
      if (factory.allowsNullElement)
        List("ONE", "TWO", null)
      else
        List("ONE", "TWO", "THREE")
    }

    val coll = TrivialImmutableCollection(l: _*)
    assertTrue(hsUut.addAll(coll))
    assertEquals(3, hsUut.size)

    assertTrue(hsMatch.addAll(coll))
    assertEquals(hsUut.size, hsMatch.size)

    assertTrue(hsNoMatch.addAll(coll))
    assertTrue(hsNoMatch.add("MISMATCH"))
    assertEquals(hsUut.size + 1, hsNoMatch.size)

    assertTrue("should match other content-equal set", hsUut.equals(hsMatch))

    assertFalse(
      "should not match other content-not-equal set",
      hsUut.equals(hsNoMatch)
    )

    assertFalse(
      "should not match other content-equal not-Set Collection",
      hsUut.equals(coll)
    )
  }
}

trait SetFactory extends CollectionFactory {
  def empty[E: ClassTag]: ju.Set[E]
}
