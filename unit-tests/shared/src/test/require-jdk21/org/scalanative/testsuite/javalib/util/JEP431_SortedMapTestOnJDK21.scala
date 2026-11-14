package org.scalanative.testsuite.javalib.util

import java.util.TreeMap
import java.{util => ju}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

/* This file exercises only SortedMap default methods.
 */

class JEP431_SortedMapTestOnJDK21 {

  val classUnderTest = "SortedMap"

  def sortedMapFactory[K, V](): ju.SortedMap[K, V] = {
    new ju.SortedMap[K, V]() {
      val inner = new ju.TreeMap[K, V]

      def comparator(): java.util.Comparator[_ >: K] = inner.comparator()
      def get(x$1: Any): V = inner.get(x$1)
      def headMap(x$1: K): java.util.SortedMap[K, V] = inner.headMap(x$1)
      def isEmpty(): Boolean = inner.isEmpty()
      def lastKey(): K = inner.lastKey()
      def put(x$1: K, x$2: V): V = inner.put(x$1, x$2)
      def size(): Int = inner.size()

      // Members declared in java.util.Map
      def clear(): Unit = ???
      def containsKey(x$1: Any): Boolean = ???
      def containsValue(x$1: Any): Boolean = ???
      def putAll(x$1: java.util.Map[_ <: K, _ <: V]): Unit = ???
      def remove(x$1: Any): V = ???

      // Members declared in java.util.SortedMap
      def entrySet(): java.util.Set[java.util.Map.Entry[K, V]] = ???
      def firstKey(): K = ???
      def keySet(): java.util.Set[K] = ???
      def subMap(x$1: K, x$2: K): java.util.SortedMap[K, V] = ???
      def tailMap(x$1: K): java.util.SortedMap[K, V] = ???
      def values(): java.util.Collection[V] = ???
    }
  }

  val populationSize = 9 // keep sort simple, avoid key_10 < key_2
  val keyPrefix = "key_"

  def populateSortedStringIntMap(
      sortedMap: ju.SortedMap[String, Int]
  ): ju.SortedMap[String, Int] = {

    for (j <- 1 to populationSize)
      sortedMap.put(s"${keyPrefix}${j}", j)
    sortedMap
  }

  @Test def sortedMap_putFirst(): Unit = {
    val sortedMap = sortedMapFactory[String, Int]()
    populateSortedStringIntMap(sortedMap)

    assertThrows(
      s"${classUnderTest}#putFirst",
      classOf[UnsupportedOperationException],
      sortedMap.putFirst("a", -1)
    )
  }

  @Test def sortedMap_putLast(): Unit = {
    val sortedMap = sortedMapFactory[String, Int]()
    populateSortedStringIntMap(sortedMap)

    assertThrows(
      s"${classUnderTest}#putLast",
      classOf[UnsupportedOperationException],
      sortedMap.putLast("z", -2)
    )
  }

  @Test def reversed(): Unit = {

    // Soft fail on not-yet-implemented SortedMap
    assumeTrue(
      s"${classUnderTest}#reversed() is Not Yet Implemented on Scala Native",
      Platform.executingInJVM
    )

    val sortedMap = sortedMapFactory[String, Int]()
    populateSortedStringIntMap(sortedMap)

    val reversedSortedMap = sortedMap.reversed()

    val reversedEntrySet = reversedSortedMap.entrySet()
    val reversedSeqEntrySet = reversedSortedMap.sequencedEntrySet()

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
}
