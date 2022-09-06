// Ported from Scala.js commit: e7f1ff7 dated: 2022-06-01

package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert._

import java.{lang => jl}
import java.{util => ju}
import java.util.function.UnaryOperator

import scala.reflect.ClassTag
import scalanative.junit.utils.AssertThrows.assertThrows
import scalanative.junit.utils.CollectionsTestBase

trait ListTest extends CollectionTest with CollectionsTestBase {

  def factory: ListFactory

  @Test def addStringGetIndex(): Unit = {
    val lst = factory.empty[String]

    assertEquals(0, lst.size())
    lst.add("one")
    assertEquals(1, lst.size())
    assertEquals("one", lst.get(0))
    lst.add("two")
    assertEquals(2, lst.size())
    assertEquals("one", lst.get(0))
    assertEquals("two", lst.get(1))

    assertThrows(classOf[IndexOutOfBoundsException], lst.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], lst.get(lst.size))
  }

  @Test def addIntGetIndex(): Unit = {
    val lst = factory.empty[Int]

    lst.add(1)
    assertEquals(1, lst.size())
    assertEquals(1, lst.get(0))
    lst.add(2)
    assertEquals(2, lst.size())
    assertEquals(1, lst.get(0))
    assertEquals(2, lst.get(1))

    assertThrows(classOf[IndexOutOfBoundsException], lst.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], lst.get(lst.size))
  }

  @Test def addDoubleGetIndex(): Unit = {
    val lst = factory.empty[Double]

    lst.add(1.234)
    assertEquals(1, lst.size())
    assertEquals(1.234, lst.get(0), 0.0)
    lst.add(2.345)
    assertEquals(2, lst.size())
    assertEquals(1.234, lst.get(0), 0.0)
    assertEquals(2.345, lst.get(1), 0.0)
    lst.add(Double.NaN)
    lst.add(+0.0)
    lst.add(-0.0)
    assertEquals(5, lst.size())
    assertEquals(1.234, lst.get(0), 0.0)
    assertEquals(2.345, lst.get(1), 0.0)
    assertTrue(lst.get(2).isNaN)
    assertTrue(lst.get(3).equals(+0.0))
    assertTrue(lst.get(4).equals(-0.0))

    assertThrows(classOf[IndexOutOfBoundsException], lst.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], lst.get(lst.size))
  }

  @Test def addCustomObjectsGetIndex(): Unit = {
    case class TestObj(num: Int)

    val lst = factory.empty[TestObj]

    lst.add(TestObj(100))
    assertEquals(1, lst.size())
    assertEquals(TestObj(100), lst.get(0))

    assertThrows(classOf[IndexOutOfBoundsException], lst.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], lst.get(lst.size))
  }

  @Test def removeStringRemoveIndex(): Unit = {
    val lst = factory.empty[String]

    lst.add("one")
    lst.add("two")
    lst.add("three")

    assertFalse(lst.remove("four"))
    assertEquals(3, lst.size())
    assertTrue(lst.remove("two"))
    assertEquals(2, lst.size())
    assertEquals("one", lst.remove(0))
    assertEquals(1, lst.size())
    assertEquals("three", lst.get(0))

    assertThrows(classOf[IndexOutOfBoundsException], lst.remove(-1))
    assertThrows(classOf[IndexOutOfBoundsException], lst.remove(lst.size))
  }

  @Test def removeDoubleOnCornerCases(): Unit = {
    val al = factory.empty[Double]

    al.add(1.234)
    al.add(2.345)
    al.add(Double.NaN)
    al.add(+0.0)
    al.add(-0.0)

    // al == ArrayList(1.234, 2.345, NaN, +0.0, -0.0)
    assertTrue(al.remove(Double.NaN))
    // al == ArrayList(1.234, 2.345, +0.0, -0.0)
    assertEquals(4, al.size())
    assertTrue(al.remove(2.345))
    // al == ArrayList(1.234, +0.0, -0.0)
    assertEquals(3, al.size())
    assertEquals(1.234, al.remove(0), 0.0)
    // al == ArrayList(+0.0, -0.0)
    assertEquals(2, al.size())
    assertTrue(al.remove(-0.0))
    // al == ArrayList(NaN, +0.0)
    assertEquals(1, al.size())

    al.clear()

    assertTrue(al.isEmpty)
  }

  @Test def clearList(): Unit = {
    val al = factory.empty[String]

    al.add("one")
    al.add("two")
    assertEquals(2, al.size)
    al.clear()
    assertEquals(0, al.size)
  }

  @Test def containsStringList(): Unit = {
    val al = factory.empty[String]

    al.add("one")
    assertTrue(al.contains("one"))
    assertFalse(al.contains("two"))
    assertFalse(al.contains(null))
  }

  @Test def containedDoubleOnCornerCases(): Unit = {
    val al = factory.empty[Double]

    al.add(-0.0)
    assertTrue(al.contains(-0.0))
    assertFalse(al.contains(+0.0))

    al.clear()

    al.add(+0.0)
    assertFalse(al.contains(-0.0))
    assertTrue(al.contains(+0.0))
  }

  @Test def setString(): Unit = {
    val al = factory.empty[String]
    al.add("one")
    al.add("two")
    al.add("three")

    al.set(1, "four")
    assertEquals("one", al.get(0))
    assertEquals("four", al.get(1))
    assertEquals("three", al.get(2))

    assertThrows(classOf[IndexOutOfBoundsException], al.set(-1, ""))
    assertThrows(classOf[IndexOutOfBoundsException], al.set(al.size, ""))
  }

  @Test def iterator(): Unit = {
    val al = factory.empty[String]
    al.add("one")
    al.add("two")
    al.add("three")

    val elements = al.iterator()
    assertTrue(elements.hasNext)
    assertEquals("one", elements.next())
    assertTrue(elements.hasNext)
    assertEquals("two", elements.next())
    assertTrue(elements.hasNext)
    assertEquals("three", elements.next())
    assertFalse(elements.hasNext)
  }

  @Test def toArrayObjectForList(): Unit = {
    val coll = factory.fromElements("one", "two", "three", "four", "five")

    val result = coll.toArray()
    assertSame(classOf[Array[AnyRef]], result.getClass())
    assertArrayEquals(
      Array[AnyRef]("one", "two", "three", "four", "five"),
      result
    )
  }

  @Test def toArraySpecificForList(): Unit = {
    val coll = factory.fromElements("one", "two", "three", "four", "five")

    val arrayString3 = new Array[String](3)
    val result1 = coll.toArray(arrayString3)
    assertNotSame(arrayString3, result1)
    assertSame(classOf[Array[String]], result1.getClass())
    assertArrayEquals(
      Array[AnyRef]("one", "two", "three", "four", "five"),
      result1.asInstanceOf[Array[AnyRef]]
    )

    val arrayString5 = new Array[String](5)
    val result2 = coll.toArray(arrayString5)
    assertSame(arrayString5, result2)
    assertSame(classOf[Array[String]], result2.getClass())
    assertArrayEquals(
      Array[AnyRef]("one", "two", "three", "four", "five"),
      result2.asInstanceOf[Array[AnyRef]]
    )

    val arrayString7 = new Array[String](7)
    arrayString7(5) = "foo"
    arrayString7(6) = "bar"
    val result3 = coll.toArray(arrayString7)
    assertSame(arrayString7, result3)
    assertSame(classOf[Array[String]], result3.getClass())
    assertArrayEquals(
      Array[AnyRef]("one", "two", "three", "four", "five", null, "bar"),
      result3.asInstanceOf[Array[AnyRef]]
    )
  }

  @Test def listIterator(): Unit = {
    val lst = factory.empty[String]
    lst.add("one")
    lst.add("two")
    lst.add("three")

    val elements = lst.listIterator()
    assertFalse(elements.hasPrevious)
    assertTrue(elements.hasNext)
    assertEquals("one", elements.next())
    assertTrue(elements.hasPrevious)
    assertTrue(elements.hasNext)
    assertEquals("two", elements.next())
    assertTrue(elements.hasPrevious)
    assertTrue(elements.hasNext)
    assertEquals("three", elements.next())
    assertTrue(elements.hasPrevious)
    assertFalse(elements.hasNext)
    assertEquals("three", elements.previous())
    assertEquals("two", elements.previous())
    assertEquals("one", elements.previous())
  }

  @Test def addIndex(): Unit = {
    val al = factory.empty[String]
    al.add(0, "one") // ["one"]
    al.add(0, "two") // ["two", "one"]
    al.add(1, "three") // ["two", "three", "one"]

    assertEquals("two", al.get(0))
    assertEquals("three", al.get(1))
    assertEquals("one", al.get(2))

    assertThrows(classOf[IndexOutOfBoundsException], al.add(-1, ""))
    assertThrows(classOf[IndexOutOfBoundsException], al.add(al.size + 1, ""))
  }

  @Test def indexOf(): Unit = {
    val al = factory.empty[String]
    al.add("one")
    al.add("two")
    al.add("three")
    al.add("one")
    al.add("two")
    al.add("three")

    assertEquals(0, al.indexOf("one"))
    assertEquals(1, al.indexOf("two"))
    assertEquals(2, al.indexOf("three"))
    assertEquals(-1, al.indexOf("four"))
  }

  @Test def lastIndexOf(): Unit = {
    val al = factory.empty[String]
    al.add("one")
    al.add("two")
    al.add("three")
    al.add("one")
    al.add("two")
    al.add("three")

    assertEquals(3, al.lastIndexOf("one"))
    assertEquals(4, al.lastIndexOf("two"))
    assertEquals(5, al.lastIndexOf("three"))
    assertEquals(-1, al.lastIndexOf("four"))
  }

  @Test def indexOfLastIndexOfDoubleCornerCases(): Unit = {
    val al = factory.empty[Double]

    al.add(-0.0)
    al.add(+0.0)
    al.add(Double.NaN)
    al.add(+0.0)
    al.add(-0.0)
    al.add(Double.NaN)

    assertEquals(0, al.indexOf(-0.0))
    assertEquals(1, al.indexOf(+0.0))
    assertEquals(2, al.indexOf(Double.NaN))

    assertEquals(3, al.lastIndexOf(+0.0))
    assertEquals(4, al.lastIndexOf(-0.0))
    assertEquals(5, al.lastIndexOf(Double.NaN))
  }

  @Test def subListBackedByList(): Unit = {
    def testListIterator(list: ju.List[String], expected: Seq[String]): Unit = {
      val iter = list.listIterator()
      for (elem <- expected) {
        assertTrue(iter.hasNext)
        assertEquals(elem, iter.next())
      }
      assertFalse(iter.hasNext)

      for (elem <- expected.reverse) {
        assertTrue(iter.hasPrevious)
        assertEquals(elem, iter.previous())
      }
      assertFalse(iter.hasPrevious)
    }

    val al = factory.empty[String]

    al.add("one")
    al.add("two")
    al.add("three")
    al.add("four")
    al.add("five")
    al.add("six")

    testListIterator(al, Seq("one", "two", "three", "four", "five", "six"))

    val al0 = al.subList(0, al.size)
    assertEquals(6, al0.size)
    assertEquals(al.size, al0.size)
    for (i <- 0 until al.size)
      assertEquals(al.get(i), al0.get(i))
    al0.set(3, "zero")
    assertEquals("zero", al0.get(3))
    for (i <- 0 until al.size)
      assertEquals(al.get(i), al0.get(i))
    testListIterator(al, Seq("one", "two", "three", "zero", "five", "six"))
    testListIterator(al0, Seq("one", "two", "three", "zero", "five", "six"))

    val al1 = al.subList(2, 5)
    assertEquals(3, al1.size)
    for (i <- 0 until 3)
      assertEquals(al.get(2 + i), al1.get(i))
    al1.set(0, "nine")
    assertEquals("nine", al1.get(0))
    for (i <- 0 until 3) {
      assertEquals(al.get(2 + i), al1.get(i))
      if (!al.isInstanceOf[ju.concurrent.CopyOnWriteArrayList[_]]) {
        /* For CopyOnWriteArrayList, accessing al0 after al has been modified
         * through al1 (i.e., through anything bug al0 itself) is undefined
         * behavior.
         */
        assertEquals(al0.get(2 + i), al1.get(i))
      }
    }
    assertEquals("nine", al1.get(0))
    assertEquals("zero", al1.get(1))
    assertEquals("five", al1.get(2))

    testListIterator(al, Seq("one", "two", "nine", "zero", "five", "six"))
    testListIterator(al1, Seq("nine", "zero", "five"))

    al1.clear()

    assertEquals("one", al.get(0))
    assertEquals("two", al.get(1))
    assertEquals("six", al.get(2))
    assertEquals(3, al.size)
    assertEquals(0, al1.size)
    testListIterator(al, Seq("one", "two", "six"))
    testListIterator(al1, Seq.empty)

    assertTrue(al1.add("ten"))
    testListIterator(al, Seq("one", "two", "ten", "six"))
    testListIterator(al1, Seq("ten"))

    if (factory.allowsMutationThroughIterator) {
      val iter = al1.listIterator
      iter.add("three")
      iter.next()
      iter.add("zero")

      testListIterator(al, Seq("one", "two", "three", "ten", "zero", "six"))
      testListIterator(al1, Seq("three", "ten", "zero"))
    }
  }

  @Test def iteratorSetRemoveIfAllowed(): Unit = {
    if (factory.allowsMutationThroughIterator) {
      val s = Seq("one", "two", "three")
      val ll = factory.empty[String]

      for (e <- s)
        ll.add(e)

      val iter = ll.listIterator(1)

      assertTrue(iter.hasNext())
      assertTrue(iter.hasPrevious())

      assertEquals("one", iter.previous())

      assertTrue(iter.hasNext())
      assertFalse(iter.hasPrevious())

      assertEquals("one", iter.next())

      assertEquals("two", iter.next())
      assertEquals("three", iter.next())

      assertFalse(iter.hasNext())
      assertTrue(iter.hasPrevious())

      iter.add("four")

      assertFalse(iter.hasNext())
      assertTrue(iter.hasPrevious())

      assertEquals("four", iter.previous())

      iter.remove()

      assertFalse(iter.hasNext())
      assertTrue(iter.hasPrevious())
      assertEquals("three", iter.previous())
      iter.set("THREE")
      assertEquals("two", iter.previous())
      iter.set("TWO")
      assertEquals("one", iter.previous())
      iter.set("ONE")
      assertTrue(iter.hasNext())
      assertFalse(iter.hasPrevious())

      assertEquals("ONE", iter.next())
      iter.remove()
      assertEquals("TWO", iter.next())
      iter.remove()
      assertEquals("THREE", iter.next())
      iter.remove()

      assertFalse(iter.hasNext())
      assertFalse(iter.hasPrevious())

      assertTrue(ll.isEmpty())
    }
  }

  @Test def replaceAll(): Unit = {
    val list = factory.fromElements(2, 45, 8, -2, 4)
    list.replaceAll(new UnaryOperator[Int] {
      def apply(t: Int): Int = t * 3
    })

    assertEquals(5, list.size())
    assertEquals(6, list.get(0))
    assertEquals(135, list.get(1))
    assertEquals(24, list.get(2))
    assertEquals(-6, list.get(3))
    assertEquals(12, list.get(4))
  }

  @Test def sortWithNaturalOrdering(): Unit = {
    testSortWithNaturalOrdering[CustomComparable](
      new CustomComparable(_),
      absoluteOrder = false
    )
    testSortWithNaturalOrdering[jl.Integer](jl.Integer.valueOf)
    testSortWithNaturalOrdering[jl.Long](_.toLong)
    testSortWithNaturalOrdering[jl.Double](_.toDouble)
  }

  @Test def sortWithComparator(): Unit = {
    testSortWithComparator[CustomComparable](
      new CustomComparable(_),
      (x, y) => x.compareTo(y),
      absoluteOrder = false
    )
    testSortWithComparator[jl.Integer](_.toInt, (x, y) => x.compareTo(y))
    testSortWithComparator[jl.Long](_.toLong, (x, y) => x.compareTo(y))
    testSortWithComparator[jl.Double](_.toDouble, (x, y) => x.compareTo(y))
  }

  private def testSortWithNaturalOrdering[T <: AnyRef with Comparable[
    T
  ]: ClassTag](toElem: Int => T, absoluteOrder: Boolean = true): Unit = {

    val list = factory.empty[T]

    def testIfSorted(rangeValues: Boolean): Unit = {
      for (i <- range.init)
        assertTrue(list.get(i).compareTo(list.get(i + 1)) <= 0)
      if (absoluteOrder && rangeValues) {
        for (i <- range)
          assertEquals(0, list.get(i).compareTo(toElem(i)))
      }
    }

    list.addAll(rangeOfElems(toElem))
    list.sort(null)
    testIfSorted(true)

    list.clear()
    list.addAll(TrivialImmutableCollection(range.reverse.map(toElem): _*))
    list.sort(null)
    testIfSorted(true)

    for (seed <- List(0, 1, 42, -5432, 2341242)) {
      val rnd = new scala.util.Random(seed)
      list.clear()
      list.addAll(
        TrivialImmutableCollection(range.map(_ => toElem(rnd.nextInt())): _*)
      )
      list.sort(null)
      testIfSorted(false)
    }
  }

  private def testSortWithComparator[T: ClassTag](
      toElem: Int => T,
      cmpFun: (T, T) => Int,
      absoluteOrder: Boolean = true
  ): Unit = {

    val list = factory.empty[T]

    def testIfSorted(rangeValues: Boolean): Unit = {
      for (i <- range.init)
        assertTrue(cmpFun(list.get(i), list.get(i + 1)) <= 0)
      if (absoluteOrder && rangeValues) {
        for (i <- range)
          assertEquals(0, cmpFun(list.get(i), toElem(i)))
      }
    }

    val cmp = new ju.Comparator[T] {
      override def compare(o1: T, o2: T): Int = cmpFun(o1, o2)
    }

    list.addAll(rangeOfElems(toElem))
    list.sort(cmp)
    testIfSorted(true)

    list.clear()
    list.addAll(TrivialImmutableCollection(range.reverse.map(toElem): _*))
    list.sort(cmp)
    testIfSorted(true)

    for (seed <- List(0, 1, 42, -5432, 2341242)) {
      val rnd = new scala.util.Random(seed)
      list.clear()
      list.addAll(
        TrivialImmutableCollection(range.map(_ => toElem(rnd.nextInt())): _*)
      )
      list.sort(cmp)
      testIfSorted(false)
    }
  }
}

trait ListFactory extends CollectionFactory {
  def empty[E: ClassTag]: ju.List[E]

  // Refines the result type of CollectionFactory.fromElements
  override def fromElements[E: ClassTag](elems: E*): ju.List[E] = {
    val coll = empty[E]
    coll.addAll(TrivialImmutableCollection(elems: _*))
    coll
  }
}
