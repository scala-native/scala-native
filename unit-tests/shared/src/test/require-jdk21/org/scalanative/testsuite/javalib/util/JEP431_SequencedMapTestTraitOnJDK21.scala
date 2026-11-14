package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* This trait is used by JEP431_LinkedHashMapTestOnJDK21 and
 * JEP431_TreeMapTestOnJDK21 to ensure that a common core of Tests
 * are run in each class; a single point of truth.
 *
 * Each of those classes implement additional Tests for class specific
 * overrides.
 */

trait JEP431_SequencedMapTestTraitOnJDK21 {

  val classUnderTest: String

  /* Using String keys from 1 to populationSize creates keys
   * {key_1=1, key_10=10, ... key_9=9}. This allows distinguishing
   * TreeMap lastEntry-by-key-order from LinkedHashMap
   * lastEntry-by-insertion-order.
   */

  val populationSize = 10 // a small arbitrary number greater than 2
  val keyPrefix = "key_"

  val expectedFirstValue = 1
  val expectedFirstKey = s"${keyPrefix}${expectedFirstValue}"

  val expectedLastValue: Int // TreeMap & LinkedHashMap differ
  val expectedLastKey: String

  def sequencedMapFactory[K, V](): ju.SequencedMap[K, V] // Abstract

  def populateStringIntMap(
      seqMap: ju.SequencedMap[String, Int]
  ): ju.SequencedMap[String, Int] = {

    for (j <- expectedFirstValue to populationSize)
      seqMap.put(s"${keyPrefix}${j}", j)
    seqMap
  }

  def validateEntry(
      entry: ju.Map.Entry[String, Int],
      expectedKey: String,
      expectedValue: Int
  ) = {}

  @Test def firstEntry_empty(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()

    assertEquals(
      s"${classUnderTest}#firstEntry on empty map should return null",
      null,
      seqMap.firstEntry()
    )
  }

  @Test def firstEntry(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val fe = seqMap.firstEntry()

    assertEquals(
      s"${classUnderTest}#firstEntry unexpected key",
      expectedFirstKey,
      fe.getKey()
    )

    assertEquals(
      s"${classUnderTest}#firstEntry unexpected value",
      expectedFirstValue,
      fe.getValue()
    )
  }

  @Test def lastEntry_empty(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()

    assertEquals(
      s"${classUnderTest}#lastEntry on empty map should return null",
      null,
      seqMap.lastEntry()
    )
  }

  @Test def lastEntry(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val fe = seqMap.lastEntry()

    assertEquals(
      s"${classUnderTest}#lastEntry unexpected key",
      expectedLastKey,
      fe.getKey()
    )

    assertEquals(
      s"${classUnderTest}#lastEntry unexpected value",
      expectedLastValue,
      fe.getValue()
    )
  }

  @Test def pollFirstEntry_empty(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()

    assertEquals(
      s"${classUnderTest}#pollFirstEntry on empty map should return null",
      null,
      seqMap.pollFirstEntry()
    )
  }

  @Test def pollFirstEntry(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val fe = seqMap.pollFirstEntry()

    assertEquals(
      s"${classUnderTest}#pollFirstEntry unexpected key",
      expectedFirstKey,
      fe.getKey()
    )

    assertEquals(
      s"${classUnderTest}#pollFirstEntry unexpected value",
      expectedFirstValue,
      fe.getValue()
    )

    val fk = expectedFirstKey
    assertFalse(
      s"${classUnderTest}#pollFirstEntry map still contains key: ${fk}",
      seqMap.containsKey(fk)
    )

    val fv = expectedFirstValue
    assertFalse(
      s"${classUnderTest}#pollFirstEntry map still contains value: ${fv}",
      seqMap.containsValue(fv)
    )
  }

  @Test def pollLastEntry_empty(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()

    assertEquals(
      s"${classUnderTest}#pollLastEntry on empty map should return null",
      null,
      seqMap.pollLastEntry()
    )
  }

  @Test def pollLastEntry(): Unit = {

    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val le = seqMap.pollLastEntry()

    assertEquals(
      s"${classUnderTest}#pollLastEntry unexpected key",
      expectedLastKey,
      le.getKey()
    )

    assertEquals(
      s"${classUnderTest}#pollLastEntry unexpected value",
      expectedLastValue,
      le.getValue()
    )

    val lk = expectedLastKey
    assertFalse(
      s"${classUnderTest}#pollLastEntry map still contains key: ${lk}",
      seqMap.containsKey(lk)
    )

    val lv = expectedLastValue
    assertFalse(
      s"${classUnderTest}#pollLastEntry map still contains value: ${lv}",
      seqMap.containsValue(lv)
    )
  }

// ---------------------------------------------------------------------------

  /* LinkedHashMap does not throw so Tests for these two can not be in
   * the this common trait and must be in individual Test files.
   *   def putFirst(key: K, value: V): V
   *   def putLast(key: K, value: V): V
   */

// ---------------------------------------------------------------------------
  /* Test for reversed() is minimal. It tests only that entry point
   * activates and returns expected size, contents, and order.
   * Eventually more comprehensive tests should be created.
   */

  @Test def reversed(): Unit = {

    // Soft fail on not-yet-implemented LinkedHashMap & SortedMap
    val isImplemented = (classUnderTest == "TreeMap")
    assumeTrue(
      s"${classUnderTest}#reversed() is Not Yet Implemented",
      isImplemented
    )

    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val reversedSeqMap = seqMap.reversed()

    val reversedEntrySet = reversedSeqMap.entrySet()
    val reversedSeqEntrySet = reversedSeqMap.sequencedEntrySet()

    assertEquals(
      s"${classUnderTest}#reversed size()",
      reversedEntrySet.size(),
      reversedSeqEntrySet.size()
    )

    val reversedEntryList = new ju.ArrayList(reversedEntrySet)
    val reversedSeqEntryList = new ju.ArrayList(reversedSeqEntrySet)

    assertEquals(
      s"${classUnderTest}# reversed contents",
      reversedEntryList,
      reversedSeqEntryList
    )
  }

// ---------------------------------------------------------------------------

  /* Tests for sequencedEntrySet(), sequencedKeySet(), and sequencedValues()
   * Rely upon the JEP 431 assurance that an iterator on the result of
   * a call to one of entrySet(), keySet(), or values() for a
   * SequencedMap will have the same order as the corresponding
   * sequenced method
   *
   * Spooky action, but documented.
   */

  /* Tests for sequencedEntrySet(), sequencedKeySet(), and sequencedValues()
   * are minimal. They test only expected size, contents, and order.
   * The JDK 27 documentation for 'sequenced*()' describes some
   * pretty strict requirements. Eventually each of those requirements should
   * have a test clause.
   */

  @Test def sequencedEntrySet(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val entrySet = seqMap.entrySet()
    val seqEntrySet = seqMap.sequencedEntrySet()

    assertEquals(
      s"${classUnderTest}#sequencedEntrySet size()",
      entrySet.size(),
      seqEntrySet.size()
    )

    val entryList = new ju.ArrayList(entrySet)
    val seqEntryList = new ju.ArrayList(seqEntrySet)

    assertEquals(
      s"${classUnderTest}#sequencedEntrySet contents",
      entryList,
      seqEntryList
    )
  }

  @Test def sequencedKeySet(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val keySet = seqMap.keySet()
    val seqKeySet = seqMap.sequencedKeySet()

    assertEquals(
      s"${classUnderTest}#sequencedKeySet size()",
      keySet.size(),
      seqKeySet.size()
    )

    val keyList = new ju.ArrayList(keySet)
    val seqKeyList = new ju.ArrayList(seqKeySet)

    assertEquals(
      s"${classUnderTest}#sequencedKeySet contents",
      keyList,
      seqKeyList
    )
  }

  @Test def sequencedValues(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    val values = seqMap.values()
    val seqValues = seqMap.sequencedValues()

    assertEquals(
      s"${classUnderTest}#sequencedValues size()",
      values.size(),
      seqValues.size()
    )

    val valuesList = new ju.ArrayList(values)
    val seqValuesList = new ju.ArrayList(seqValues)

    assertEquals(
      s"${classUnderTest}#sequencedValues contents",
      valuesList,
      seqValuesList
    )
  }
}
