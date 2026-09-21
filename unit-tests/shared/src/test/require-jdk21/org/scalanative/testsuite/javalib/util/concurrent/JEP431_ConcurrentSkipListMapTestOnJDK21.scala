package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{ConcurrentNavigableMap, ConcurrentSkipListMap}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class JEP431_ConcurrentSkipListMapTestOnJDK21 extends JSR166Test {
  import JSR166Test._

  private val iZero = itemFor(0)
  private val iOne = itemFor(1)
  private val iTwo = itemFor(2)
  private val iThree = itemFor(3)
  private val iFour = itemFor(4)
  private val iFive = itemFor(5)
  private val iSix = itemFor(6)
  private val iSeven = itemFor(7)

  private def map5(): ConcurrentSkipListMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    map.put(iOne, "A")
    map.put(iFive, "E")
    map.put(iThree, "C")
    map.put(iTwo, "B")
    map.put(iFour, "D")
    map
  }

  private def subMap5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    map.put(iZero, "Z")
    map.put(iOne, "A")
    map.put(iFive, "E")
    map.put(iThree, "C")
    map.put(iTwo, "B")
    map.put(iFour, "D")
    map.put(iSeven, "F")
    map.subMap(iOne, true, iSeven, false)
  }

  @Test def testSequencedMapViews(): Unit = {
    val map = map5()

    mustEqual(iOne, map.sequencedKeySet().getFirst())
    mustEqual(iFive, map.sequencedKeySet().reversed().getFirst())

    mustEqual("A", map.sequencedValues().getFirst())
    mustEqual("E", map.sequencedValues().reversed().getFirst())

    mustEqual(iOne, map.sequencedEntrySet().getFirst().getKey())
    mustEqual("A", map.sequencedEntrySet().getFirst().getValue())
    mustEqual(iFive, map.sequencedEntrySet().reversed().getFirst().getKey())
    mustEqual("E", map.sequencedEntrySet().reversed().getFirst().getValue())

    mustEqual(iFive, map.reversed().firstEntry().getKey())
    mustEqual(iOne, map.reversed().reversed().firstEntry().getKey())
  }

  @Test def testSequencedMapPutFirstLastUnsupported(): Unit = {
    val map = map5()
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putFirst(null, "X")
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putFirst(iZero, null)
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putFirst(iZero, "X")
    )
    assertThrows(classOf[UnsupportedOperationException], map.putLast(iSix, "X"))
  }

  @Test def testSequencedValuesRemoveFirstLast(): Unit = {
    val map = map5()

    mustEqual("A", map.sequencedValues().removeFirst())
    assertFalse(map.containsKey(iOne))
    mustEqual(iTwo, map.firstKey())

    mustEqual("E", map.sequencedValues().removeLast())
    assertFalse(map.containsKey(iFive))
    mustEqual(iFour, map.lastKey())
  }

  @Test def testSequencedEntrySetRemoveFirstLast(): Unit = {
    val map = map5()

    val first = map.sequencedEntrySet().removeFirst()
    mustEqual(iOne, first.getKey())
    mustEqual("A", first.getValue())
    assertFalse(map.containsKey(iOne))

    val last = map.sequencedEntrySet().removeLast()
    mustEqual(iFive, last.getKey())
    mustEqual("E", last.getValue())
    assertFalse(map.containsKey(iFive))
  }

  @Test def testSubMapSequencedMapViews(): Unit = {
    val map = subMap5()

    mustEqual(iOne, map.sequencedKeySet().getFirst())
    mustEqual(iFive, map.sequencedKeySet().reversed().getFirst())

    mustEqual("A", map.sequencedValues().getFirst())
    mustEqual("E", map.sequencedValues().reversed().getFirst())

    mustEqual(iOne, map.sequencedEntrySet().getFirst().getKey())
    mustEqual("A", map.sequencedEntrySet().getFirst().getValue())
    mustEqual(iFive, map.sequencedEntrySet().reversed().getFirst().getKey())
    mustEqual("E", map.sequencedEntrySet().reversed().getFirst().getValue())

    mustEqual(iFive, map.reversed().firstEntry().getKey())
    mustEqual(iOne, map.reversed().reversed().firstEntry().getKey())
  }

  @Test def testSubMapSequencedMapPutFirstLastUnsupported(): Unit = {
    val map = subMap5()
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putFirst(null, "X")
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putFirst(iTwo, null)
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putFirst(iTwo, "X")
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      map.putLast(iThree, "X")
    )
  }

  @Test def testSubMapSequencedValuesRemoveFirstLast(): Unit = {
    val map = subMap5()

    mustEqual("A", map.sequencedValues().removeFirst())
    assertFalse(map.containsKey(iOne))
    mustEqual(iTwo, map.firstKey())

    mustEqual("E", map.sequencedValues().removeLast())
    assertFalse(map.containsKey(iFive))
    mustEqual(iFour, map.lastKey())
  }

  @Test def testSubMapSequencedEntrySetRemoveFirstLast(): Unit = {
    val map = subMap5()

    val first = map.sequencedEntrySet().removeFirst()
    mustEqual(iOne, first.getKey())
    mustEqual("A", first.getValue())
    assertFalse(map.containsKey(iOne))

    val last = map.sequencedEntrySet().removeLast()
    mustEqual(iFive, last.getKey())
    mustEqual("E", last.getValue())
    assertFalse(map.containsKey(iFive))
  }
}
