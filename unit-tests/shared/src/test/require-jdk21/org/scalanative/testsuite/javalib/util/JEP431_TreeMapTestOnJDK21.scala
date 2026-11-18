package org.scalanative.testsuite.javalib.util

import java.util.TreeMap
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* This file exercises local overrides of SequencedMap and SortedMap
 * default methods.
 */

class JEP431_TreeMapTestOnJDK21 extends JEP431_SequencedMapTestTraitOnJDK21 {

  val classUnderTest = "TreeMap"

  def sequencedMapFactory[K, V](): ju.SequencedMap[K, V] =
    new ju.TreeMap[K, V]()

  val expectedLastValue = populationSize - 1
  val expectedLastKey = s"${keyPrefix}${expectedLastValue}"

  @Test def treeMap_putFirst(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    assertThrows(
      s"${classUnderTest}#putFirst",
      classOf[UnsupportedOperationException],
      seqMap.putFirst("a", -1)
    )
  }

  @Test def treeMap_putLast(): Unit = {
    val seqMap = sequencedMapFactory[String, Int]()
    populateStringIntMap(seqMap)

    assertThrows(
      s"${classUnderTest}#putLast",
      classOf[UnsupportedOperationException],
      seqMap.putLast("z", -2)
    )
  }
}
