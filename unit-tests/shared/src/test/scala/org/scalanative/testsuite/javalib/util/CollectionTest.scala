// Ported from Scala.js commit: f9fc1ae dated: 2020-03-06
// Spliterator test added to celebrate Scala Native multithreading.

package org.scalanative.testsuite.javalib.util

import java.{util => ju, lang => jl}
import java.util.Spliterator
import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.javalib.lang.IterableFactory
import org.scalanative.testsuite.javalib.lang.IterableTest

import scala.reflect.ClassTag

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import Utils._

/* Design Note:
 *     Note the "trait" keyword and not the "class" keyword. That
 *     means that CollectionTest does not run by itself. It is only run
 *     when other tests which extend this trait are run.
 *     "sbt tests3/testOnly *.CollectionTest" will fail. If you are lucky
 *     it will take only a few days of your life to understand the failure.
 *     If you are even luckier, you will remember the cause when you
 *     encounter the quirk six months or a year later.
 *
 *     "sbt tests3/testOnly *.AbstractCollectionTest", for one, should work.
 */

trait CollectionTest extends IterableTest {

  def factory: CollectionFactory

  @Test def shouldStoreStrings(): Unit = {
    val coll = factory.empty[String]

    assertEquals(0, coll.size())
    coll.add("one")
    assertEquals(1, coll.size())

    coll.clear()
    assertEquals(0, coll.size())
    assertFalse(coll.addAll(TrivialImmutableCollection[String]()))
    assertEquals(0, coll.size())

    assertTrue(coll.addAll(TrivialImmutableCollection("one")))
    assertEquals(1, coll.size())

    coll.clear()
    assertTrue(coll.addAll(TrivialImmutableCollection("one", "two", "one")))
    assertTrue(coll.size() >= 1)
  }

  @Test def shouldStoreIntegers(): Unit = {
    val coll = factory.empty[Int]

    assertEquals(0, coll.size())
    coll.add(1)
    assertEquals(1, coll.size())

    coll.clear()
    assertEquals(0, coll.size())
    assertFalse(coll.addAll(TrivialImmutableCollection[Int]()))
    assertEquals(0, coll.size())

    assertTrue(coll.addAll(TrivialImmutableCollection(1)))
    assertEquals(1, coll.size())

    coll.clear()
    assertTrue(coll.addAll(TrivialImmutableCollection(1, 2, 1)))
    assertTrue(coll.size() >= 1)
  }

  @Test def shouldStoreDoubles(): Unit = {
    val coll = factory.empty[Double]

    assertEquals(0, coll.size())
    coll.add(1.234)
    assertEquals(1, coll.size())

    coll.clear()
    assertEquals(0, coll.size())
    assertFalse(coll.addAll(TrivialImmutableCollection[Double]()))
    assertEquals(0, coll.size())

    assertTrue(coll.addAll(TrivialImmutableCollection(1.234)))
    assertEquals(1, coll.size())

    coll.clear()
    assertTrue(coll.addAll(TrivialImmutableCollection(1.234, 2.345, 1.234)))
    assertTrue(coll.size() >= 1)

    coll.clear()
    coll.add(+0.0)
    assertTrue(coll.contains(+0.0))
    assertFalse(coll.contains(-0.0))

    coll.clear()
    coll.add(-0.0)
    assertFalse(coll.contains(+0.0))
    assertTrue(coll.contains(-0.0))

    coll.clear()
    coll.add(Double.NaN)
    assertEquals(1, coll.size())
    assertTrue(coll.contains(Double.NaN))
  }

  @Test def shouldStoreCustomObjects(): Unit = {
    case class TestObj(num: Int) extends jl.Comparable[TestObj] {
      def compareTo(o: TestObj): Int =
        o.num.compareTo(num)
    }

    val coll = factory.empty[TestObj]

    coll.add(TestObj(100))
    assertEquals(1, coll.size())
    assertTrue(coll.contains(TestObj(100)))
    assertFalse(coll.contains(TestObj(200)))
  }

  @Test def shouldRemoveStoredElements(): Unit = {
    val coll = factory.empty[String]

    coll.add("one")
    coll.add("two")
    coll.add("three")
    coll.add("two")

    val initialSize = coll.size()
    assertFalse(coll.remove("four"))
    assertEquals(initialSize, coll.size())
    assertTrue(coll.remove("two"))
    assertEquals(initialSize - 1, coll.size())
    assertTrue(coll.remove("one"))
    assertEquals(initialSize - 2, coll.size())
  }

  @Test def shouldRemoveStoredElementsOnDoubleCornerCases(): Unit = {
    val coll = factory.empty[Double]

    coll.add(1.234)
    coll.add(2.345)
    coll.add(Double.NaN)
    coll.add(+0.0)
    coll.add(-0.0)

    // coll == ArrayCollection(1.234, 2.345, NaN, +0.0, -0.0)
    assertTrue(coll.remove(Double.NaN))
    // coll == ArrayCollection(1.234, 2.345, +0.0, -0.0)
    assertEquals(4, coll.size())
    assertTrue(coll.remove(2.345))
    // coll == ArrayCollection(1.234, +0.0, -0.0)
    assertEquals(3, coll.size())
    assertTrue(coll.remove(1.234))
    // coll == ArrayCollection(+0.0, -0.0)
    assertEquals(2, coll.size())
    assertTrue(coll.remove(-0.0))
    // coll == ArrayCollection(NaN, +0.0)
    assertEquals(1, coll.size())

    coll.clear()

    assertTrue(coll.isEmpty)
  }

  @Test def shouldBeClearedWithOneOperation(): Unit = {
    val coll = factory.empty[String]

    coll.add("one")
    coll.add("two")
    assertEquals(2, coll.size)
    coll.clear()
    assertEquals(0, coll.size)
  }

  @Test def shouldCheckContainedPresence(): Unit = {
    val coll = factory.empty[String]

    coll.add("one")
    assertTrue(coll.contains("one"))
    assertFalse(coll.contains("two"))
    if (factory.allowsNullElementQuery) {
      assertFalse(coll.contains(null))
    } else {
      assertThrows(classOf[Exception], coll.contains(null))
    }
  }

  @Test def shouldCheckContainedPresenceForDoubleCornerCases(): Unit = {
    val coll = factory.empty[Double]

    coll.add(-0.0)
    assertTrue(coll.contains(-0.0))
    assertFalse(coll.contains(+0.0))

    coll.clear()

    coll.add(+0.0)
    assertFalse(coll.contains(-0.0))
    assertTrue(coll.contains(+0.0))
  }

  @Test def shouldGiveProperIteratorOverElements(): Unit = {
    val coll = factory.empty[String]
    coll.add("one")
    coll.add("two")
    coll.add("three")
    coll.add("three")
    coll.add("three")

    assertIteratorSameElementsAsSetDupesAllowed("one", "two", "three")(
      coll.iterator()
    )
  }

  @Test def removeIf(): Unit = {
    val coll = factory.fromElements[Int](42, 50, 12, 0, -45, 102, 32, 75)
    assertEquals(8, coll.size())

    assertTrue(coll.removeIf(new java.util.function.Predicate[Int] {
      def test(x: Int): Boolean = x >= 50
    }))
    assertEquals(5, coll.size())
    assertIteratorSameElementsAsSet(-45, 0, 12, 32, 42)(coll.iterator())

    assertFalse(coll.removeIf(new java.util.function.Predicate[Int] {
      def test(x: Int): Boolean = x >= 45
    }))
    assertEquals(5, coll.size())
    assertIteratorSameElementsAsSet(-45, 0, 12, 32, 42)(coll.iterator())
  }

  @Test def toStringShouldConvertEmptyCollection(): Unit = {
    val coll = factory.empty[Double]
    assertEquals("[]", coll.toString())
  }

  @Test def toStringShouldConvertOneElementCollection(): Unit = {
    val coll = factory.fromElements[Double](1.01)
    // JavaScript displays n.0 as n, so one trailing digit must be non-zero.
    assertEquals("[1.01]", coll.toString())
  }

  @Test def toStringShouldUseCommaSpace(): Unit = {
    // Choose Doubles which display the same in Java and Scala.js.
    // JavaScript displays n.0 as n, so one trailing digit must be non-zero.
    val elements = Seq(88.42, -23.36, 60.173)

    val coll = factory.fromElements[Double](elements: _*)

    val result = coll.toString()

    // The order of elements returned by each collection is defined
    // by the collection. Be prepared to handle the general case of any
    // order here. Specific collections should test the order they specify.
    val expected = elements.permutations.map(_.mkString("[", ", ", "]")).toSet

    assertTrue(
      s"result '${result}' not in expected set '${expected}'",
      expected.contains(result)
    )
  }

  @Test def toStringShouldHandleNullElements(): Unit = {
    if (factory.allowsNullElement) {
      val elements = Seq(-1, -2, null, -3)

      val coll = factory.fromElements[Any](elements: _*)

      val result = coll.toString()

      val expected = elements.permutations.map(_.mkString("[", ", ", "]")).toSet
      assertTrue(
        s"result '${result}' not in expected set '${expected}'",
        expected.contains(result)
      )
    }
  }

  @Test def toStringInCustomClassShouldWork(): Unit = {
    case class Custom(name: String, id: Int) extends Ordered[Custom] {
      def compare(that: Custom): Int = this.id - that.id
    }

    val elements = Seq(Custom("A", 1), Custom("b", 2), Custom("C", 3))

    val coll = factory.fromElements[Custom](elements: _*)

    val result = coll.toString()
    val expected = elements.permutations.map(_.mkString("[", ", ", "]")).toSet
    assertTrue(
      s"result '${result}' not in expected set '${expected}'",
      expected.contains(result)
    )
  }

  @Test def spliteratorShouldExist(): Unit = {
    /* CollectionTest is a trait, which get mixed into the tests for
     * several Collections. Spliterators() tend to be tailored to the
     * individual collection: the whole reason for overriding the default
     * implementation.
     *
     * Trying to account here for some Collections using the default
     * Collection.spliterator() and some overriding it quickly leads to
     * a tangled mess.
     *
     * CollectionDefaultSpliteratorTest.scala exercises the default
     * Collection.spliterator() method using a collection know to use
     * that implementation. Because it is a separate test (and a "class"),
     * it is called once, in a known environment.
     */
    val coll =
      factory.fromElements[String]("Aegle", "Arethusa", "Hesperethusa")

    val expectedSize = 3
    assertEquals(expectedSize, coll.size())

    val spliter = coll.spliterator()
    assertNotNull("Null coll.spliterator", spliter)
  }
}

trait CollectionFactory extends IterableFactory {
  def empty[E: ClassTag]: ju.Collection[E]
  def allowsMutationThroughIterator: Boolean = true
  def allowsNullElementQuery: Boolean = true
  def allowsNullElement: Boolean = true

  override def fromElements[E: ClassTag](elems: E*): ju.Collection[E] = {
    val coll = empty[E]
    coll.addAll(TrivialImmutableCollection(elems: _*))
    coll
  }
}
