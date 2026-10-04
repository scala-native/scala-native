package org.scalanative.testsuite.javalib.util

import java.io.Serializable
import java.util.{AbstractMap, Comparator, Map}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class MapEntryComparatorTest {
  @Test def naturalKeyOrderIgnoresValues(): Unit = {
    val cmp = Map.Entry.comparingByKey[String, String]()
    val a = new AbstractMap.SimpleEntry("a", "z")
    val b = new AbstractMap.SimpleEntry("b", "a")
    assertTrue(cmp.compare(a, b) < 0)
    assertTrue(cmp.compare(b, a) > 0)
    assertEquals(0, cmp.compare(a, new AbstractMap.SimpleEntry("a", null)))
  }

  @Test def naturalValueOrderIgnoresKeys(): Unit = {
    val cmp = Map.Entry.comparingByValue[String, String]()
    val a = new AbstractMap.SimpleEntry("z", "a")
    val b = new AbstractMap.SimpleEntry("a", "b")
    assertTrue(cmp.compare(a, b) < 0)
    assertTrue(cmp.compare(b, a) > 0)
    assertEquals(0, cmp.compare(a, new AbstractMap.SimpleEntry(null, "a")))
  }

  private val lengthComparator = new Comparator[CharSequence]
    with Serializable {
    def compare(a: CharSequence, b: CharSequence): Int =
      Integer.compare(a.length(), b.length())
  }

  @Test def suppliedKeyComparatorSupportsSupertypesAndNulls(): Unit = {
    val cmp = Map.Entry.comparingByKey[String, String](
      Comparator.nullsFirst(lengthComparator)
    )
    val a = new AbstractMap.SimpleEntry("z", "z")
    val b = new AbstractMap.SimpleEntry("aa", "a")
    assertTrue(cmp.compare(a, b) < 0)
    assertEquals(0, cmp.compare(a, new AbstractMap.SimpleEntry("a", null)))
    assertTrue(cmp.compare(new AbstractMap.SimpleEntry(null, "z"), a) < 0)
  }

  @Test def suppliedValueComparatorSupportsSupertypesAndNulls(): Unit = {
    val cmp = Map.Entry.comparingByValue[String, String](
      Comparator.nullsLast(lengthComparator)
    )
    val a = new AbstractMap.SimpleEntry("z", "z")
    val b = new AbstractMap.SimpleEntry("a", "aa")
    assertTrue(cmp.compare(a, b) < 0)
    assertEquals(0, cmp.compare(a, new AbstractMap.SimpleEntry(null, "a")))
    assertTrue(cmp.compare(new AbstractMap.SimpleEntry("z", null), a) > 0)
  }

  @Test def suppliedComparatorsAreRequiredEagerly(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      Map.Entry.comparingByKey[String, String](null)
    )
    assertThrows(
      classOf[NullPointerException],
      Map.Entry.comparingByValue[String, String](null)
    )
  }

  @Test def naturalOrderRejectsNullSelectedFieldsAndNullEntries(): Unit = {
    val byKey = Map.Entry.comparingByKey[String, String]()
    val byValue = Map.Entry.comparingByValue[String, String]()
    val entry = new AbstractMap.SimpleEntry("key", "value")
    assertThrows(classOf[NullPointerException], byKey.compare(null, entry))
    assertThrows(classOf[NullPointerException], byValue.compare(entry, null))
    assertThrows(
      classOf[NullPointerException],
      byKey.compare(new AbstractMap.SimpleEntry(null, "value"), entry)
    )
    assertThrows(
      classOf[NullPointerException],
      byValue.compare(new AbstractMap.SimpleEntry("key", null), entry)
    )
  }

  @Test def returnedComparatorsImplementSerializable(): Unit = {
    val comparators = Array(
      Map.Entry.comparingByKey[String, String](),
      Map.Entry.comparingByValue[String, String](),
      Map.Entry.comparingByKey[String, String](lengthComparator),
      Map.Entry.comparingByValue[String, String](lengthComparator)
    )
    comparators.foreach(cmp => assertTrue(cmp.isInstanceOf[Serializable]))
  }

  private class Ranked(val rank: Int) extends Comparable[Ranked] {
    def compareTo(other: Ranked): Int = Integer.compare(rank, other.rank)
  }

  private class ChildRanked(rank: Int) extends Ranked(rank)

  @Test def naturalOrderSupportsComparableOfSuperclass(): Unit = {
    val a = new ChildRanked(1)
    val b = new ChildRanked(2)
    val byKey = Map.Entry.comparingByKey[ChildRanked, String]()
    assertTrue(
      byKey.compare(
        new AbstractMap.SimpleEntry(a, "z"),
        new AbstractMap.SimpleEntry(b, "a")
      ) < 0
    )
    val byValue = Map.Entry.comparingByValue[String, ChildRanked]()
    assertTrue(
      byValue.compare(
        new AbstractMap.SimpleEntry("z", a),
        new AbstractMap.SimpleEntry("a", b)
      ) < 0
    )
  }
}
