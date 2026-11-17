package org.scalanative.testsuite.javalib.util

import java.util._
import java.util.function.{
  BiConsumer, BiFunction, Consumer, Function, Predicate
}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class MapFailFastTest {

  private type CME = ConcurrentModificationException
  private type JMap = java.util.Map[Integer, Integer]

  private def int(i: Int): Integer = Integer.valueOf(i)

  private def fill[M <: JMap](m: M): M = {
    var i = 0
    while (i < 5) {
      m.put(int(i), int(i))
      i += 1
    }
    m
  }

  private val maps: Seq[(String, () => JMap)] = Seq(
    "HashMap" -> (() => fill(new HashMap[Integer, Integer]())),
    "LinkedHashMap" -> (() => fill(new LinkedHashMap[Integer, Integer]())),
    "LinkedHashMap(accessOrder)" ->
      (() => fill(new LinkedHashMap[Integer, Integer](16, 0.75f, true))),
    "IdentityHashMap" -> (() => fill(new IdentityHashMap[Integer, Integer]())),
    "WeakHashMap" -> (() => fill(new WeakHashMap[Integer, Integer]())),
    "Hashtable" -> (() => fill(new Hashtable[Integer, Integer]())),
    "TreeMap" -> (() => fill(new TreeMap[Integer, Integer]()))
  )

  /* IdentityHashMap compares keys by reference: use the keys of the map
   * itself rather than new boxes.
   */
  private def key(m: JMap, i: Int): Integer = {
    val it = m.keySet().iterator()
    var result: Integer = null
    while (it.hasNext()) {
      val k = it.next()
      if (k.intValue() == i) result = k
    }
    result
  }

  // Reads the stored value without get(), which reorders access-order maps.
  private def value(m: JMap, i: Int): Integer = {
    val it = m.entrySet().iterator()
    var result: Integer = null
    while (it.hasNext()) {
      val e = it.next()
      if (e.getKey().intValue() == i) result = e.getValue()
    }
    result
  }

  private val views: Seq[(String, JMap => Iterator[_])] = Seq(
    "keySet" -> (_.keySet().iterator()),
    "values" -> (_.values().iterator()),
    "entrySet" -> (_.entrySet().iterator())
  )

  private val structuralChanges: Seq[(String, JMap => Unit)] = Seq(
    "put(new)" -> (_.put(int(99), int(1))),
    "remove" -> (m => assertNotNull(m.remove(key(m, 3)))),
    "remove(k, v)" -> (m => assertTrue(m.remove(key(m, 3), value(m, 3)))),
    "clear" -> (_.clear()),
    "putAll(new)" -> (_.putAll(Collections.singletonMap(int(99), int(1)))),
    "computeIfAbsent(new)" -> (_.computeIfAbsent(
      int(99),
      new Function[Integer, Integer] {
        def apply(k: Integer): Integer = int(1)
      }
    )),
    "merge(new)" -> (_.merge(
      int(99),
      int(1),
      new BiFunction[Integer, Integer, Integer] {
        def apply(a: Integer, b: Integer): Integer = a
      }
    )),
    "keySet.remove" -> (m => assertTrue(m.keySet().remove(key(m, 3)))),
    "values.remove" -> (m => assertTrue(m.values().remove(value(m, 3)))),
    "entrySet.removeIf" -> (_.entrySet().removeIf(
      new Predicate[Map.Entry[Integer, Integer]] {
        def test(e: Map.Entry[Integer, Integer]): Boolean =
          e.getKey().intValue() == 3
      }
    ))
  )

  private val nonStructuralChanges: Seq[(String, JMap => Unit)] = Seq(
    "remove(absent)" -> (_.remove(int(99))),
    "remove(k, wrong v)" -> (m => m.remove(key(m, 3), int(-1))),
    "containsKey" -> (_.containsKey(int(1))),
    "containsValue" -> (_.containsValue(int(1))),
    "putAll(empty)" -> (_.putAll(Collections.emptyMap[Integer, Integer]())),
    "entry.setValue" -> (_.entrySet().iterator().next().setValue(int(42))),
    "replaceAll" -> (_.replaceAll(new BiFunction[Integer, Integer, Integer] {
      def apply(k: Integer, v: Integer): Integer = int(v.intValue() + 1)
    }))
  )

  private val sameKeyChanges: Seq[(String, JMap => Unit)] = Seq(
    "get" -> (m => m.get(key(m, 1))),
    "put(existing)" -> (m => m.put(key(m, 1), int(7))),
    "putIfAbsent(existing)" -> (m => m.putIfAbsent(key(m, 1), int(7))),
    "replace" -> (m => m.replace(key(m, 1), int(8)))
  )

  private def forAllMaps(body: (String, () => JMap) => Unit): Unit =
    maps.foreach { case (name, mk) => body(name, mk) }

  private def isAccessOrdered(name: String) = name.contains("accessOrder")

  @Test def iteratorNextThrowsAfterStructuralChange(): Unit = forAllMaps {
    (name, mk) =>
      for ((view, iter) <- views; (op, change) <- structuralChanges) {
        val m = mk()
        val it = iter(m)
        it.next()
        change(m)
        assertThrows(s"$name.$view: next after $op", classOf[CME], it.next())
      }
  }

  @Test def iteratorSurvivesNonStructuralChange(): Unit = forAllMaps {
    (name, mk) =>
      for ((view, iter) <- views; (op, change) <- nonStructuralChanges) {
        val m = mk()
        val it = iter(m)
        it.next()
        change(m)
        assertTrue(s"$name.$view: hasNext after $op", it.hasNext())
        it.next()
      }
  }

  @Test def accessOrderedLinkedHashMapAccessIsStructural(): Unit = {
    forAllMaps { (name, mk) =>
      for ((op, change) <- sameKeyChanges) {
        val m = mk()
        val it = m.keySet().iterator()
        it.next()
        change(m)
        if (isAccessOrdered(name))
          assertThrows(s"$name: next after $op", classOf[CME], it.next())
        else
          it.next()
      }
    }

    val m = fill(new LinkedHashMap[Integer, Integer](16, 0.75f, true))
    val it = m.keySet().iterator()
    it.next()
    m.get(int(4)) // already the most recently accessed
    m.get(int(99))
    assertEquals(1, it.next().intValue())
  }

  @Test def accessOrderedComputeFunctionAccessIsAModification(): Unit = {
    def lhm() = fill(new LinkedHashMap[Integer, Integer](16, 0.75f, true))
    def readOther(m: JMap) = new BiFunction[Integer, Integer, Integer] {
      def apply(a: Integer, b: Integer): Integer =
        int(m.get(int(1)).intValue() + 1)
    }

    val m1 = lhm()
    assertThrows(classOf[CME], m1.compute(int(0), readOther(m1)))

    val m2 = lhm()
    assertThrows(classOf[CME], m2.computeIfPresent(int(0), readOther(m2)))

    val m3 = lhm()
    assertThrows(classOf[CME], m3.merge(int(0), int(5), readOther(m3)))

    // Without a reordering access in the function, the entry moves last.
    val m4 = lhm()
    m4.compute(
      int(0),
      new BiFunction[Integer, Integer, Integer] {
        def apply(k: Integer, v: Integer): Integer = int(v.intValue() + 10)
      }
    )
    assertEquals(
      Arrays.asList[Integer](int(1), int(2), int(3), int(4), int(0)),
      new ArrayList[Integer](m4.keySet())
    )
    assertEquals(10, m4.get(int(0)).intValue())
  }

  @Test def accessOrderedComputeMovesTheEntryAfterTheFunction(): Unit = {
    def lhm() = fill(new LinkedHashMap[Integer, Integer](16, 0.75f, true))
    def keys(m: JMap) = new ArrayList[Integer](m.keySet())
    val movedLast =
      Arrays.asList[Integer](int(1), int(2), int(3), int(4), int(0))
    // Reading the youngest entry does not reorder the map.
    def readYoungest(m: JMap) = new BiFunction[Integer, Integer, Integer] {
      def apply(a: Integer, b: Integer): Integer = m.get(int(4))
    }

    val m1 = lhm()
    assertEquals(4, m1.compute(int(0), readYoungest(m1)).intValue())
    assertEquals(movedLast, keys(m1))

    val m2 = lhm()
    assertEquals(4, m2.computeIfPresent(int(0), readYoungest(m2)).intValue())
    assertEquals(movedLast, keys(m2))

    val m3 = lhm()
    assertEquals(4, m3.merge(int(0), int(5), readYoungest(m3)).intValue())
    assertEquals(movedLast, keys(m3))

    val m4 = lhm()
    m4.computeIfAbsent(
      int(0),
      new Function[Integer, Integer] {
        def apply(k: Integer): Integer = throw new AssertionError("called")
      }
    )
    assertEquals(movedLast, keys(m4))

    // An entry removed by the function result is not accessed.
    val m5 = lhm()
    assertNull(
      m5.compute(
        int(0),
        new BiFunction[Integer, Integer, Integer] {
          def apply(a: Integer, b: Integer): Integer = null
        }
      )
    )
    assertEquals(
      Arrays.asList[Integer](int(1), int(2), int(3), int(4)),
      keys(m5)
    )
  }

  @Test def identityHashMapViewRemoveAllOfItself(): Unit = {
    def ihm() = fill(new IdentityHashMap[Integer, Integer]())
    val m1 = ihm()
    assertTrue(m1.values().removeAll(m1.values()))
    assertTrue(m1.isEmpty())
    val m2 = ihm()
    assertTrue(m2.keySet().removeAll(m2.keySet()))
    assertTrue(m2.isEmpty())
    val m3 = ihm()
    assertTrue(m3.entrySet().removeAll(m3.entrySet()))
    assertTrue(m3.isEmpty())
  }

  @Test def iteratorRemoveKeepsIteratorValid(): Unit = forAllMaps {
    (name, mk) =>
      val m = mk()
      val it1 = m.keySet().iterator()
      val it2 = m.keySet().iterator()
      it1.next()
      it2.next()
      it1.remove()
      it1.next()
      assertEquals(s"$name size", 4, m.size())
      assertThrows(s"$name: other iterator", classOf[CME], it2.next())

      val it3 = m.keySet().iterator()
      it3.next()
      m.put(int(99), int(1))
      assertThrows(s"$name: remove after put", classOf[CME], it3.remove())
  }

  @Test def exhaustedIteratorNextThrowsAfterStructuralChange(): Unit =
    forAllMaps { (name, mk) =>
      val m = mk()
      val it = m.keySet().iterator()
      while (it.hasNext()) it.next()
      m.put(int(99), int(1))
      // The JVM TreeMap iterator reports exhaustion first
      val expected =
        if (name == "TreeMap") classOf[NoSuchElementException]
        else classOf[CME]
      assertThrows(s"$name", expected, it.next())
    }

  @Test def bulkTraversalsThrowWhenCallbackAddsMapping(): Unit = forAllMaps {
    (name, mk) =>
      // Change the map in the first callback: the iteration order of
      // IdentityHashMap varies between runs.
      def adding(m: JMap, k: Integer): Unit =
        if (!m.containsKey(int(99))) m.put(int(99), int(1))

      val m1 = mk()
      assertThrows(
        s"$name.forEach",
        classOf[CME],
        m1.forEach(new BiConsumer[Integer, Integer] {
          def accept(k: Integer, v: Integer): Unit = adding(m1, k)
        })
      )
      val m2 = mk()
      assertThrows(
        s"$name.keySet.forEach",
        classOf[CME],
        m2.keySet()
          .forEach(new Consumer[Integer] {
            def accept(k: Integer): Unit = adding(m2, k)
          })
      )
      val m3 = mk()
      assertThrows(
        s"$name.entrySet.forEach",
        classOf[CME],
        m3.entrySet()
          .forEach(new Consumer[Map.Entry[Integer, Integer]] {
            def accept(e: Map.Entry[Integer, Integer]): Unit =
              adding(m3, e.getKey())
          })
      )
      val m4 = mk()
      assertThrows(
        s"$name.replaceAll",
        classOf[CME],
        m4.replaceAll(new BiFunction[Integer, Integer, Integer] {
          def apply(k: Integer, v: Integer): Integer = {
            adding(m4, k)
            v
          }
        })
      )
      val m5 = mk()
      assertThrows(
        s"$name.entrySet.removeIf",
        classOf[CME],
        m5.entrySet()
          .removeIf(new Predicate[Map.Entry[Integer, Integer]] {
            def test(e: Map.Entry[Integer, Integer]): Boolean = {
              adding(m5, e.getKey())
              false
            }
          })
      )

      val m6 = mk()
      m6.forEach(new BiConsumer[Integer, Integer] {
        def accept(k: Integer, v: Integer): Unit =
          if (!isAccessOrdered(name)) m6.put(k, int(v.intValue() + 1))
      })
  }

  @Test def computeThrowsWhenFunctionChangesTheMap(): Unit = {
    for ((name, mk) <- maps
        if !name.startsWith("Identity") && !name.startsWith("Weak")) {
      def adding(m: JMap): Integer = {
        m.put(int(99), int(1))
        int(1)
      }
      val m1 = mk()
      assertThrows(
        s"$name.computeIfAbsent",
        classOf[CME],
        m1.computeIfAbsent(
          int(98),
          new Function[Integer, Integer] {
            def apply(k: Integer): Integer = adding(m1)
          }
        )
      )
      val m2 = mk()
      assertThrows(
        s"$name.compute",
        classOf[CME],
        m2.compute(
          int(98),
          new BiFunction[Integer, Integer, Integer] {
            def apply(k: Integer, v: Integer): Integer = adding(m2)
          }
        )
      )
      val m3 = mk()
      assertThrows(
        s"$name.computeIfPresent",
        classOf[CME],
        m3.computeIfPresent(
          int(1),
          new BiFunction[Integer, Integer, Integer] {
            def apply(k: Integer, v: Integer): Integer = adding(m3)
          }
        )
      )
      val m4 = mk()
      assertThrows(
        s"$name.merge",
        classOf[CME],
        m4.merge(
          int(1),
          int(1),
          new BiFunction[Integer, Integer, Integer] {
            def apply(a: Integer, b: Integer): Integer = adding(m4)
          }
        )
      )
    }
  }

  @Test def spliteratorIsLateBindingAndFailFast(): Unit = forAllMaps {
    (name, mk) =>
      val m = mk()
      val lateBinding = m.keySet().spliterator()
      m.put(int(5), int(5))
      var count = 0
      lateBinding.forEachRemaining(new Consumer[Integer] {
        def accept(k: Integer): Unit = count += 1
      })
      assertEquals(s"$name late binding", 6, count)

      val started = m.keySet().spliterator()
      assertTrue(started.tryAdvance(new Consumer[Integer] {
        def accept(k: Integer): Unit = ()
      }))
      m.put(int(6), int(6))
      assertThrows(
        s"$name: forEachRemaining after change",
        classOf[CME],
        started.forEachRemaining(new Consumer[Integer] {
          def accept(k: Integer): Unit = ()
        })
      )

      val mutating = m.keySet().spliterator()
      var seen = 0
      assertThrows(
        s"$name: action adds mapping",
        classOf[CME],
        mutating.forEachRemaining(new Consumer[Integer] {
          def accept(k: Integer): Unit = {
            seen += 1
            if (seen == 2) m.put(int(99), int(1))
          }
        })
      )
  }

  @Test def hashtableEnumerationsAreNotFailFast(): Unit = {
    val h = fill(new Hashtable[Integer, Integer]())
    val keys = h.keys()
    val elements = h.elements()
    keys.nextElement()
    elements.nextElement()
    h.put(int(99), int(1))
    keys.nextElement()
    elements.nextElement()
  }

  @Test def hashtableViewsWriteThrough(): Unit = {
    val h = fill(new Hashtable[Integer, Integer]())
    val keys = h.keySet()
    val entries = h.entrySet()
    assertTrue(h.keySet().remove(int(1)))
    assertFalse(h.containsKey(int(1)))
    assertTrue(h.values().remove(int(2)))
    assertFalse(h.containsKey(int(2)))
    val e = h.entrySet().iterator().next()
    e.setValue(int(42))
    assertEquals(int(42), h.get(e.getKey()))
    assertThrows(classOf[NullPointerException], e.setValue(null))
    h.put(int(7), int(7))
    assertTrue(keys.contains(int(7)))
    assertEquals(4, keys.size())
    assertEquals(4, entries.size())
  }

  @Test def updatingAnExistingKeyAtTheResizeThresholdKeepsIteration(): Unit = {
    // 11 mappings: the next new key would grow the default table
    val m = new HashMap[Integer, Integer]()
    for (i <- 0 until 11) m.put(int(i), int(i))
    val it = m.keySet().iterator()
    it.next()
    m.put(int(3), int(33))
    m.putIfAbsent(int(4), int(44))
    var seen = 1
    while (it.hasNext()) {
      it.next()
      seen += 1
    }
    assertEquals(11, seen)
  }

  @Test def viewForEachDetectsChangeByLastCallback(): Unit = {
    val hashMaps: Seq[(String, () => JMap)] = Seq(
      "HashMap" -> (() => new HashMap[Integer, Integer]()),
      "LinkedHashMap" -> (() => new LinkedHashMap[Integer, Integer]()),
      "LinkedHashMap(accessOrder)" ->
        (() => new LinkedHashMap[Integer, Integer](16, 0.75f, true))
    )
    /* On the JVM an IdentityHashMap view notices the new key only if it lands
     * after the visited one in the table, which depends on identity hashes.
     */
    val oneEntryMaps: Seq[(String, () => JMap)] =
      if (Platform.executingInJVM) hashMaps
      else
        hashMaps :+
          ("IdentityHashMap" -> (() => new IdentityHashMap[Integer, Integer]()))
    for ((name, mk) <- oneEntryMaps) {
      def one(): JMap = {
        val m = mk()
        m.put(int(1), int(1))
        m
      }
      val m1 = one()
      assertThrows(
        s"$name: keySet",
        classOf[CME],
        m1.keySet()
          .forEach(new Consumer[Integer] {
            def accept(k: Integer): Unit = m1.put(int(2), int(2))
          })
      )
      val m2 = one()
      assertThrows(
        s"$name: values",
        classOf[CME],
        m2.values()
          .forEach(new Consumer[Integer] {
            def accept(v: Integer): Unit = m2.put(int(2), int(2))
          })
      )
      val m3 = one()
      assertThrows(
        s"$name: entrySet",
        classOf[CME],
        m3.entrySet()
          .forEach(new Consumer[Map.Entry[Integer, Integer]] {
            def accept(e: Map.Entry[Integer, Integer]): Unit =
              m3.put(int(2), int(2))
          })
      )
    }
  }

  private val hashSets: Seq[(String, () => Set[Integer])] = Seq(
    "HashSet" -> (() =>
      new HashSet[Integer](Arrays.asList[Integer](0, 1, 2, 3, 4))
    ),
    "LinkedHashSet" ->
      (() => new LinkedHashSet[Integer](Arrays.asList[Integer](0, 1, 2, 3, 4)))
  )

  private val sets: Seq[(String, () => Set[Integer])] = hashSets :+
    ("TreeSet" -> (() =>
      new TreeSet[Integer](Arrays.asList[Integer](0, 1, 2, 3, 4))
    ))

  @Test def setIteratorsAreFailFast(): Unit = {
    for ((name, mk) <- sets) {
      val structural: Seq[(String, Set[Integer] => Any)] = Seq(
        "add(new)" -> (_.add(int(99))),
        "remove" -> (_.remove(int(3))),
        "clear" -> (_.clear()),
        "addAll" -> (_.addAll(Arrays.asList[Integer](98, 99))),
        "removeAll" -> (_.removeAll(Arrays.asList[Integer](3))),
        "retainAll" -> (_.retainAll(Arrays.asList[Integer](0, 1)))
      )
      for ((op, change) <- structural) {
        val s = mk()
        val it = s.iterator()
        it.next()
        change(s)
        assertThrows(s"$name: next after $op", classOf[CME], it.next())
      }

      val nonStructural: Seq[(String, Set[Integer] => Any)] = Seq(
        "add(existing)" -> (_.add(int(1))),
        "remove(absent)" -> (_.remove(int(99))),
        "contains" -> (_.contains(int(1))),
        "retainAll(all)" -> (s => s.retainAll(new ArrayList[Integer](s)))
      )
      for ((op, change) <- nonStructural) {
        val s = mk()
        val it = s.iterator()
        it.next()
        change(s)
        it.next()
      }

      val s = mk()
      assertThrows(
        s"$name.forEach",
        classOf[CME],
        s.forEach(new Consumer[Integer] {
          def accept(x: Integer): Unit =
            if (!s.contains(int(99))) s.add(int(99))
        })
      )
      assertThrows(
        s"$name.removeIf",
        classOf[CME],
        s.removeIf(new Predicate[Integer] {
          def test(x: Integer): Boolean = {
            if (!s.contains(int(98))) s.add(int(98))
            false
          }
        })
      )

      val s2 = mk()
      val it = s2.iterator()
      it.next()
      it.remove()
      it.next()
      assertEquals(4, s2.size())
      assertTrue(s2.removeAll(s2))
      assertTrue(s2.isEmpty())
    }
  }

  @Test def hashSetCloneIsIndependent(): Unit = {
    for ((name, mk) <- hashSets) {
      val s = mk()
      val c =
        s.asInstanceOf[HashSet[Integer]].clone().asInstanceOf[Set[Integer]]
      assertEquals(name, s.getClass(), c.getClass())
      c.remove(int(1))
      assertTrue(name, s.contains(int(1)))
      assertEquals(name, 4, c.size())
      assertEquals(name, 5, s.size())
    }
  }

  private def treeMap(): TreeMap[Integer, Integer] =
    fill(new TreeMap[Integer, Integer]())

  private val navigableViews: Seq[
    (String, TreeMap[Integer, Integer] => NavigableMap[Integer, Integer])
  ] =
    Seq(
      "subMap" -> (_.subMap(int(1), true, int(4), true)),
      "headMap" -> (_.headMap(int(3), true)),
      "tailMap" -> (_.tailMap(int(1), true)),
      "descendingMap" -> (_.descendingMap()),
      "subMap.descendingMap" ->
        (_.subMap(int(1), true, int(4), true).descendingMap()),
      "descendingMap.headMap" -> (_.descendingMap().headMap(int(1), true))
    )

  @Test def treeMapViewIteratorsAreFailFast(): Unit = {
    for ((name, view) <- navigableViews) {
      val m1 = treeMap()
      val it1 = view(m1).keySet().iterator()
      it1.next()
      m1.put(int(99), int(1)) // outside of the range of most views
      assertThrows(s"$name: after parent put", classOf[CME], it1.next())

      val m2 = treeMap()
      val it2 = view(m2).entrySet().iterator()
      it2.next()
      m2.remove(int(0))
      assertThrows(s"$name: after parent remove", classOf[CME], it2.next())

      val m3 = treeMap()
      val v3 = view(m3)
      val it3 = v3.values().iterator()
      it3.next()
      v3.pollLastEntry()
      assertThrows(s"$name: after view poll", classOf[CME], it3.next())

      val m4 = treeMap()
      val it4 = view(m4).navigableKeySet().descendingIterator()
      it4.next()
      m4.put(int(2), int(9))
      it4.next() // replacing a value is not structural
      it4.remove()
      it4.next()

      val m5 = treeMap()
      val it5 = view(m5).keySet().iterator()
      it5.next()
      m5.clear()
      assertThrows(s"$name: after parent clear", classOf[CME], it5.next())
    }

    val m = treeMap()
    val it = m.descendingKeySet().iterator()
    it.next()
    m.pollFirstEntry()
    assertThrows(classOf[CME], it.next())

    val m2 = treeMap()
    val it2 = m2.keySet().iterator()
    it2.next()
    m2.firstEntry()
    m2.ceilingKey(int(2))
    assertEquals(1, it2.next().intValue())
  }

  @Test def treeSetViewIteratorsAreFailFast(): Unit = {
    def treeSet() = new TreeSet[Integer](Arrays.asList[Integer](0, 1, 2, 3, 4))
    val views: Seq[(String, TreeSet[Integer] => Iterator[Integer])] = Seq(
      "descendingIterator" -> (_.descendingIterator()),
      "subSet" -> (_.subSet(int(1), true, int(4), true).iterator()),
      "headSet" -> (_.headSet(int(3)).iterator()),
      "tailSet.descendingSet" ->
        (_.tailSet(int(1), true).descendingSet().iterator()),
      "descendingSet" -> (_.descendingSet().iterator())
    )
    for ((name, view) <- views) {
      val s1 = treeSet()
      val it1 = view(s1)
      it1.next()
      s1.add(int(99))
      assertThrows(s"$name: after add", classOf[CME], it1.next())

      val s2 = treeSet()
      val it2 = view(s2)
      it2.next()
      s2.add(int(2))
      it2.next() // adding an existing element is not structural
      it2.remove()
      it2.next()

      val s3 = treeSet()
      val it3 = view(s3)
      it3.next()
      s3.pollFirst()
      assertThrows(s"$name: after pollFirst", classOf[CME], it3.next())

      val s4 = treeSet()
      val it4 = view(s4)
      it4.next()
      s4.pollLast()
      assertThrows(s"$name: after pollLast", classOf[CME], it4.next())
    }
  }
}
