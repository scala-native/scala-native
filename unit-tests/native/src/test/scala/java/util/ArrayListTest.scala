// leave in this package for now - does not work on jvm
package java.util

import java.util.*

import org.junit.Test
import org.junit.Assert.*
import scala.scalanative.junit.utils.CollectionConverters.*
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArrayListTest {

  @Test def constructor(): Unit = {
    val al = new ArrayList()
    assertTrue(al.size() == 0)
    assertTrue(al.isEmpty())
  }

  @Test def constructorInt(): Unit = {
    val al = new ArrayList(10)
    assertTrue(al.size() == 0)
    assertTrue(al.isEmpty())
    // the capacity is opaque. exposing it just for testing is avoided
  }

  @Test def constructorCollectionInteger(): Unit = {
    // for AnyVal
    val is = Seq(1, 2, 3)
    val al = new ArrayList(is.toJavaList)
    assertTrue(al.size() == 3)
    assertTrue(!al.isEmpty())
    assertTrue(al.get(0) == 1)
    assertTrue(al.get(1) == 2)
    assertTrue(al.get(2) == 3)
    assertTrue(al.toScalaSeq == Seq(1, 2, 3))
  }

  @Test def constructorCollectionString(): Unit = {
    // for AnyRef
    val is = Seq(1, 2, 3).map(_.toString)
    val al = new ArrayList(is.toJavaList)
    assertTrue(al.size() == 3)
    assertTrue(!al.isEmpty())
    assertTrue(al.get(0) == "1")
    assertTrue(al.get(1) == "2")
    assertTrue(al.get(2) == "3")
    assertTrue(al.toScalaSeq == Seq("1", "2", "3"))
  }

  @Test def constructorNullThrowsNullPointerException(): Unit = {
    assertThrows(classOf[NullPointerException], new ArrayList(null))
  }

  @Test def equalsForEmptyLists(): Unit = {
    val e1 = new ArrayList()
    val e2 = new ArrayList()
    val ne1 = new ArrayList(Seq(1).toJavaList)
    assertTrue(e1 == e2)
    assertTrue(e2 == e1)
    assertTrue(e1 != ne1)
    assertTrue(ne1 != e1)
  }

  @Test def equalsForNonEmptyLists(): Unit = {
    val ne1a = new ArrayList(Seq(1, 2, 3).toJavaList)
    val ne1b = new ArrayList(Seq(1, 2, 3).toJavaList)
    val ne2 = new ArrayList(Seq(1).toJavaList)
    assertTrue(ne1a == ne1b)
    assertTrue(ne1b == ne1a)
    assertTrue(ne1a != ne2)
    assertTrue(ne2 != ne1a)
    assertTrue(ne1b != ne2)
    assertTrue(ne2 != ne1b)
  }

  @Test def trimToSizeForNonEmptyListsWithDifferentCapacities(): Unit = {
    val al1 = new ArrayList(Seq(1, 2, 3).toJavaList)
    val al2 = new ArrayList(Seq(1, 2, 3).toJavaList)
    al2.ensureCapacity(100)
    val al3 = new ArrayList[Int](50)
    al3.add(1)
    al3.add(2)
    al3.add(3)
    assertTrue(al1 == al2)
    assertTrue(al2 == al3)
  }

  @Test def trimToSizeForEmptyLists(): Unit = {
    val al1 = new ArrayList()
    al1.trimToSize()
    val al2 = new ArrayList()
    assertTrue(al1 == al2)
  }

  @Test def trimToSizeForNonEmptyLists(): Unit = {
    val al1 = new ArrayList(Seq(1, 2, 3).toJavaList)
    val al2 = new ArrayList(Seq(1, 2, 3).toJavaList)
    al2.ensureCapacity(100)
    val al3 = new ArrayList[Int](50)
    al3.add(1)
    al3.add(2)
    al3.add(3)
    al1.trimToSize()
    al2.trimToSize()
    al3.trimToSize()
    assertTrue(al1 == al2)
    assertTrue(al2 == al3)
  }

  @Test def size(): Unit = {
    val al1 = new ArrayList[Int]()
    assertTrue(al1.size() == 0)
    val al2 = new ArrayList[Int](Seq(1, 2, 3).toJavaList)
    assertTrue(al2.size() == 3)
    val al3 = new ArrayList[Int](10)
    // not to be confused with its capacity.
    assertTrue(al3.size() == 0)
  }

  @Test def isEmpty(): Unit = {
    val al1 = new ArrayList[Int]()
    assertTrue(al1.isEmpty())
    val al2 = new ArrayList[Int](Seq(1, 2, 3).toJavaList)
    assertTrue(!al2.isEmpty())
    val al3 = new ArrayList[Int](10)
    // not to be confused with its capacity.
    assertTrue(al3.isEmpty())
  }

  @Test def indexOfAny(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(al1.indexOf(2) == 1)
  }

  @Test def lastIndexOfAny(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(al1.lastIndexOf(2) == 3)
  }

  @Test def testClone(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    val al2 = al1.clone().asInstanceOf[ArrayList[Int]]
    assertTrue(al1 == al2)
    al1.add(1)
    assertTrue(al1 != al2)
    al2.add(1)
    assertTrue(al1 == al2)
  }

  @Test def cloneWithSizeNotEqualCapacity(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    al1.ensureCapacity(20)
    val al2 = al1.clone().asInstanceOf[ArrayList[Int]]
    assertTrue(al1 == al2)
    al1.add(1)
    assertTrue(al1 != al2)
    al2.add(1)
    assertTrue(al1 == al2)
  }

  @Test def cloneNoSharedState(): Unit = {
    val al1 = new ArrayList[Int](1)
    val al2 = al1.clone().asInstanceOf[ArrayList[Int]]
    al1.add(0)
    al2.add(1)
    assertTrue(al1.get(0) == 0)
  }

  @Test def toArray(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(
      (Array(1, 2, 3, 2).map(_.asInstanceOf[AnyRef])) sameElements
        (al1.toArray())
    )
  }

  @Test def toArrayDefaultInitialCapacityThenAddElements(): Unit = {
    // Issue #1500 discovered by re2s ExecTestSuite.

    val al1 = new ArrayList[String]()
    val data = Array("alpha", "omega")
    val expectedSize = data.size

    for (d <- data) al1.add(d)

    val arr1 = al1.toArray
    val arr1Size = arr1.size

    assertTrue(
      s"toArray.size: $arr1Size != expected: $expectedSize",
      arr1Size == expectedSize
    )

    // Discovering code in re2s ExecTestSuite used .deep not sameElements.
    // Should have same result as sameElements, but via different path.

    assertTrue(
      "a1.toArray.deep != data.deep",
      Arrays.deepEquals(arr1, data.asInstanceOf[Array[AnyRef]])
    )
  }

  @Test def toArrayArrayWhenArrayIsShorter(): Unit = {
    val al1 = new ArrayList[String](Seq("apple", "banana", "cherry").toJavaList)
    val ain = Array.empty[String]
    val aout = al1.toArray(ain)
    assertTrue(ain ne aout)
    assertTrue(Array("apple", "banana", "cherry") sameElements aout)
  }

  @Test def toArrayArrayWhenArrayIsWithTheSameLengthOrLonger(): Unit = {
    val al1 = new ArrayList[String](Seq("apple", "banana", "cherry").toJavaList)
    val ain = Array.fill(4)("foo")
    val aout = al1.toArray(ain)
    assertTrue(ain eq aout)
    assertTrue(Array("apple", "banana", "cherry", null) sameElements aout)
  }

  @Test def arrayEToArrayTWhenTSubE(): Unit = {
    class SuperClass
    class SubClass extends SuperClass
    val in = Seq.fill(2)(new SubClass)
    val al1 = new ArrayList[SubClass](in.toJavaList)
    val aout = al1.toArray(Array.empty[SuperClass])
    assertTrue(in.toArray sameElements aout)
  }

  // This test works on Scastie/JVM
  @Test def arrayEToArrayTShouldThrowArrayStoreExceptionWhenNotTSubE(): Unit = {
    class NotSuperClass
    class SubClass
    val al1 = new ArrayList[SubClass]()
    assertTrue(
      al1
        .toArray(Array.empty[NotSuperClass])
        .isInstanceOf[Array[NotSuperClass]]
    )
  }

  @Test def toArrayNullThrowsNull(): Unit = {
    val al1 = new ArrayList[String](Seq("apple", "banana", "cherry").toJavaList)
    assertThrows(classOf[NullPointerException], al1.toArray(null))
  }

  @Test def getInt(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(al1.get(0) == 1)
    assertTrue(al1.get(1) == 2)
    assertTrue(al1.get(2) == 3)
    assertTrue(al1.get(3) == 2)
    assertThrows(classOf[IndexOutOfBoundsException], al1.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], al1.get(4))
  }

  @Test def setInt(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(al1.set(1, 4) == 2)
    assertTrue(Seq(1, 4, 3, 2) == al1.toScalaSeq)
  }

  @Test def add(): Unit = {
    val al1 = new ArrayList[Int]()
    al1.add(1)
    assertTrue(al1.toScalaSeq == Seq(1))
    al1.add(2)
    assertTrue(al1.toScalaSeq == Seq(1, 2))
  }

  @Test def addInt(): Unit = {
    val al1 = new ArrayList[Int]()
    al1.add(0, 1)
    assertTrue(al1.toScalaSeq == Seq(1))
    al1.add(0, 2)
    assertTrue(al1.toScalaSeq == Seq(2, 1))
  }

  @Test def addIntWhenTheCapacityHasToBeExpanded(): Unit = {
    val al1 = new ArrayList[Int](0)
    al1.add(0, 1)
    assertTrue(al1.toScalaSeq == Seq(1))
    al1.add(0, 2)
    assertTrue(al1.toScalaSeq == Seq(2, 1))
  }

  @Test def addAll(): Unit = {
    val l = new java.util.ArrayList[String]()
    l.add("First")
    l.add("Second")
    val l2 = new java.util.ArrayList[String]()
    l2.addAll(0, l)
    val iter = l2.iterator()
    assertTrue(iter.next() == "First")
    assertTrue(iter.next() == "Second")
    assertTrue(!iter.hasNext())
  }

  @Test def removeInt(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2, 3).toJavaList)
    // remove last
    assertTrue(al1.remove(4) == 3)
    // remove head
    assertTrue(al1.remove(0) == 1)
    // remove middle
    assertTrue(al1.remove(1) == 3)
    assertThrows(classOf[IndexOutOfBoundsException], al1.remove(4))
    assertTrue(Seq(2, 2) == al1.toScalaSeq)
  }

  @Test def removeAny(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(al1.remove(2: Any) == true)
    assertTrue(Seq(1, 3, 2) == al1.toScalaSeq)
    assertTrue(al1.remove(4: Any) == false)
    assertTrue(Seq(1, 3, 2) == al1.toScalaSeq)
  }

  @Test def removeRangeFromToIndenticalInvalidIndices(): Unit = {
    val aList = new ArrayList[Int](Seq(-175, 24, 7, 44).toJavaList)
    val expected = new ArrayList[Int](Seq(-175, 24, 7, 44).toJavaList) // ibid.

    // Yes, the indices are invalid but no exception is expected because
    // they are identical, which is tested first. That is called a 'quirk'
    // or 'implementation dependent detail' of the documented specification.

    aList.removeRange(-1, -1)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToInvalidIndices(): Unit = {
    val aList = new ArrayList[Int](Seq(175, -24, -7, -44).toJavaList)

    assertThrows(
      classOf[java.lang.ArrayIndexOutOfBoundsException],
      aList.removeRange(-1, 2)
    ) // fromIndex < 0

    assertThrows(
      classOf[java.lang.ArrayIndexOutOfBoundsException],
      // Beware that from != too in this test.
      // See 'from == to' quirk tested above.
      aList.removeRange(aList.size, aList.size + 2)
    ) // fromIndex >= _size

    assertThrows(
      classOf[java.lang.ArrayIndexOutOfBoundsException],
      aList.removeRange(0, aList.size + 1)
    ) // toIndex > size

    assertThrows(
      classOf[java.lang.ArrayIndexOutOfBoundsException],
      aList.removeRange(2, -1)
    ) // toIndex < fromIndex
  }

  @Test def removeRangeFromToFirstTwoElements(): Unit = {
    val aList = new ArrayList[Int](Seq(284, -27, 995, 500, 267, 904).toJavaList)
    val expected = new ArrayList[Int](Seq(995, 500, 267, 904).toJavaList)

    aList.removeRange(0, 2)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToFirstTwoElementsAtHead(): Unit = {
    val aList = new ArrayList[Int](Seq(284, -27, 995, 500, 267, 904).toJavaList)
    val expected = new ArrayList[Int](Seq(995, 500, 267, 904).toJavaList)

    aList.removeRange(0, 2)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToTwoElementsFromMiddle(): Unit = {
    val aList = new ArrayList[Int](Seq(7, 9, -1, 20).toJavaList)
    val expected = new ArrayList[Int](Seq(7, 20).toJavaList)

    aList.removeRange(1, 3)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToLastTwoElementsAtTail(): Unit = {
    val aList = new ArrayList[Int](Seq(50, 72, 650, 12, 7, 28, 3).toJavaList)
    val expected = new ArrayList[Int](Seq(50, 72, 650, 12, 7).toJavaList)

    aList.removeRange(aList.size - 2, aList.size)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def removeRangeFromToEntireListAllElements(): Unit = {
    val aList = new ArrayList[Int](Seq(50, 72, 650, 12, 7, 28, 3).toJavaList)
    val expected = new ArrayList[Int](Seq.empty.toJavaList)

    aList.removeRange(0, aList.size)

    assertTrue(s"result: $aList != expected: $expected", aList == expected)
  }

  @Test def clear(): Unit = {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    al1.clear()
    assertTrue(al1.isEmpty())
    // makes sure that clear()ing an already empty list is safe
    al1.clear()
  }

  @Test def shouldThrowAnErrorWithNegativeInitialCapacity(): Unit = {
    assertThrows(classOf[IllegalArgumentException], new ArrayList(-1))
  }

  @Test def containsAny(): Unit = {
    val al = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)
    assertTrue(al.contains(1))
    assertTrue(!al.contains(5))
  }

  @Test def testToString(): Unit = {
    val al = new ArrayList[Int](Seq(1, 2, 3, 2).toJavaList)

    // Note well the space/blank after the commas. This matches Scala JVM.
    val expected = "[1, 2, 3, 2]"

    val result = al.toString
    assertTrue(
      s"result: ${result} != expected: ${expected}",
      result == expected
    )
  }

}
