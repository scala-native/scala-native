package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// Issue #4321
class DescendingViewsTest {

  private def elems[E](it: ju.Iterator[E]): List[E] = {
    val b = List.newBuilder[E]
    while (it.hasNext()) b += it.next()
    b.result()
  }

  private def elems[E](c: ju.Collection[E]): List[E] = elems(c.iterator())

  private def i(x: Int): Integer = Integer.valueOf(x)

  private def nav[E](s: ju.SortedSet[E]): ju.NavigableSet[E] =
    s.asInstanceOf[ju.NavigableSet[E]]

  private def nav[K, V](m: ju.SortedMap[K, V]): ju.NavigableMap[K, V] =
    m.asInstanceOf[ju.NavigableMap[K, V]]

  private def newSet(): ju.TreeSet[Integer] = {
    val s = new ju.TreeSet[Integer]()
    for (i <- 1 to 9) s.add(i)
    s
  }

  private def newMap(): ju.TreeMap[Integer, String] = {
    val m = new ju.TreeMap[Integer, String]()
    for (i <- 1 to 9) m.put(i, s"v$i")
    m
  }

  private def checkDescending(
      s: ju.NavigableSet[Integer],
      expected: List[Int]
  ): Unit = {
    val exp: List[Integer] = expected.map(Integer.valueOf(_))
    assertEquals(exp, elems(s))
    assertEquals(exp.reverse, elems(s.descendingIterator()))
    assertEquals(exp.reverse, elems(s.descendingSet()))
    assertEquals(exp.size, s.size())
    if (exp.nonEmpty) {
      assertEquals(exp.head, s.first())
      assertEquals(exp.last, s.last())
    }
    val cmp = s.comparator()
    assertNotNull(cmp)
    for (Seq(a, b) <- exp.sliding(2) if exp.size >= 2)
      assertTrue(cmp.compare(a, b) < 0)
  }

  @Test def descendingSetBasics(): Unit = {
    val d = newSet().descendingSet()
    checkDescending(d, (9 to 1 by -1).toList)

    assertEquals(i(4), d.higher(i(5)))
    assertEquals(i(6), d.lower(i(5)))
    assertEquals(i(5), d.ceiling(i(5)))
    assertEquals(i(5), d.floor(i(5)))
    assertNull(d.higher(i(1)))
    assertNull(d.lower(i(9)))
  }

  @Test def descendingSetHeadSet(): Unit = {
    val d = newSet().descendingSet()
    checkDescending(nav(d.headSet(i(5))), List(9, 8, 7, 6))
    checkDescending(d.headSet(i(5), true), List(9, 8, 7, 6, 5))
    checkDescending(d.headSet(i(5), false), List(9, 8, 7, 6))
  }

  @Test def descendingSetTailSet(): Unit = {
    val d = newSet().descendingSet()
    checkDescending(nav(d.tailSet(i(5))), List(5, 4, 3, 2, 1))
    checkDescending(d.tailSet(i(5), true), List(5, 4, 3, 2, 1))
    checkDescending(d.tailSet(i(5), false), List(4, 3, 2, 1))
  }

  @Test def descendingSetSubSet(): Unit = {
    val d = newSet().descendingSet()
    checkDescending(nav(d.subSet(i(7), i(3))), List(7, 6, 5, 4))
    checkDescending(d.subSet(i(7), false, i(3), true), List(6, 5, 4, 3))
    checkDescending(d.subSet(i(7), true, i(3), true), List(7, 6, 5, 4, 3))
  }

  @Test def descendingSetNestedViews(): Unit = {
    val d = newSet().descendingSet()
    val sub = nav(d.headSet(i(2))).tailSet(i(7), false)
    checkDescending(sub, List(6, 5, 4, 3))
    checkDescending(nav(sub.headSet(i(4), true).tailSet(i(5))), List(5, 4))

    assertEquals(i(3), sub.higher(i(4)))
    assertEquals(i(5), sub.lower(i(4)))
    assertNull(sub.higher(i(3)))
    assertNull(sub.lower(i(6)))
    assertEquals(i(6), sub.ceiling(i(8)))
    assertEquals(i(3), sub.floor(i(1)))

    val asc = sub.descendingSet()
    assertEquals(List[Integer](3, 4, 5, 6), elems(asc))
    assertEquals(List[Integer](4, 5), elems(asc.subSet(i(4), i(6))))
  }

  @Test def navigationWithArgumentsOutsideOfView(): Unit = {
    val asc = newSet().subSet(i(3), true, i(7), false)
    assertEquals(i(3), asc.ceiling(i(1)))
    assertEquals(i(3), asc.higher(i(1)))
    assertEquals(i(6), asc.floor(i(8)))
    assertEquals(i(6), asc.lower(i(8)))
    assertNull(asc.ceiling(i(8)))
    assertNull(asc.floor(i(1)))

    val desc = asc.descendingSet()
    assertEquals(i(6), desc.ceiling(i(8)))
    assertEquals(i(6), desc.higher(i(8)))
    assertEquals(i(3), desc.floor(i(1)))
    assertEquals(i(3), desc.lower(i(1)))
    assertNull(desc.ceiling(i(1)))
    assertNull(desc.floor(i(8)))

    val m = nav(newMap().subMap(i(3), i(7)))
    assertEquals(i(3), m.ceilingKey(i(1)))
    assertEquals(i(6), m.floorKey(i(8)))
    assertEquals(i(6), m.descendingMap().ceilingKey(i(8)))
    assertEquals(i(3), m.descendingMap().floorKey(i(1)))
  }

  @Test def descendingSetViewsWriteThrough(): Unit = {
    val s = newSet()
    val head = nav(s.descendingSet().headSet(i(5)))

    assertTrue(head.remove(i(7)))
    assertFalse(head.remove(i(3)))
    assertFalse(s.contains(7))
    assertTrue(s.contains(3))

    assertTrue(head.add(i(7)))
    assertTrue(s.contains(7))
    assertThrows(classOf[IllegalArgumentException], head.add(i(2)))

    assertEquals(i(9), head.pollFirst())
    assertEquals(i(6), head.pollLast())
    checkDescending(head, List(8, 7))
    assertEquals(List[Integer](1, 2, 3, 4, 5, 7, 8), elems(s))

    val it = head.iterator()
    assertEquals(i(8), it.next())
    it.remove()
    assertEquals(List[Integer](1, 2, 3, 4, 5, 7), elems(s))

    s.add(i(10))
    s.add(i(0))
    checkDescending(head, List(10, 7))
  }

  @Test def descendingMapViews(): Unit = {
    val d = newMap().descendingMap()

    def keys(m: ju.NavigableMap[Integer, String]): List[Integer] =
      elems(m.navigableKeySet())

    assertEquals(List[Integer](9, 8, 7, 6), keys(nav(d.headMap(i(5)))))
    assertEquals(List[Integer](9, 8, 7, 6, 5), keys(d.headMap(i(5), true)))
    assertEquals(List[Integer](5, 4, 3, 2, 1), keys(nav(d.tailMap(i(5)))))
    assertEquals(List[Integer](4, 3, 2, 1), keys(d.tailMap(i(5), false)))
    assertEquals(List[Integer](7, 6, 5, 4), keys(nav(d.subMap(i(7), i(3)))))
    assertEquals(
      List[Integer](6, 5, 4, 3),
      keys(d.subMap(i(7), false, i(3), true))
    )

    val sub = nav(d.headMap(i(2))).tailMap(i(7), false)
    assertEquals(List[Integer](6, 5, 4, 3), keys(sub))
    assertEquals(i(6), sub.firstKey())
    assertEquals(i(3), sub.lastKey())
    assertEquals(i(3), sub.higherKey(i(4)))
    assertEquals(i(5), sub.lowerKey(i(4)))
    assertEquals(List[Integer](3, 4, 5, 6), elems(sub.descendingKeySet()))
    assertEquals(List[Integer](3, 4, 5, 6), keys(sub.descendingMap()))
    assertEquals(
      List("v6", "v5", "v4", "v3"),
      elems(sub.values())
    )
    assertEquals(i(6), sub.ceilingKey(i(8)))
    assertEquals(i(3), sub.floorKey(i(1)))
    assertNull(sub.lowerKey(i(8)))
    assertNull(sub.higherKey(i(1)))
    assertEquals("v6", sub.pollFirstEntry().getValue())
    assertEquals(List[Integer](5, 4, 3), keys(sub))

    assertEquals(
      List("v9", "v8", "v7", "v5", "v4", "v3", "v2", "v1"),
      elems(d.values())
    )
    assertEquals(i(9), d.entrySet().iterator().next().getKey())
  }
}
