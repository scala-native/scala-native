package org.scalanative.testsuite.javalib.util

import java.util._
import java.util.function.{
  BiConsumer, BiFunction, Consumer, Function, Predicate
}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

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
    "Hashtable" -> (() => fill(new Hashtable[Integer, Integer]()))
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

  private val views: Seq[(String, JMap => Iterator[_])] = Seq(
    "keySet" -> (_.keySet().iterator()),
    "values" -> (_.values().iterator()),
    "entrySet" -> (_.entrySet().iterator())
  )

  private val structuralChanges: Seq[(String, JMap => Unit)] = Seq(
    "put(new)" -> (_.put(int(99), int(1))),
    "remove" -> (m => m.remove(key(m, 3))),
    "remove(k, v)" -> (m => m.remove(key(m, 3), m.get(key(m, 3)))),
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
    "keySet.remove" -> (m => m.keySet().remove(key(m, 3))),
    "values.remove" -> (m => m.values().remove(m.get(key(m, 3)))),
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
      assertThrows(s"$name", classOf[CME], it.next())
    }

  @Test def bulkTraversalsThrowWhenCallbackAddsMapping(): Unit = forAllMaps {
    (name, mk) =>
      def adding(m: JMap, k: Integer): Unit =
        if (k.intValue() == 1) m.put(int(99), int(1))

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
    assertTrue(h.keySet().remove(int(1)))
    assertFalse(h.containsKey(int(1)))
    assertTrue(h.values().remove(int(2)))
    assertFalse(h.containsKey(int(2)))
    val e = h.entrySet().iterator().next()
    e.setValue(int(42))
    assertEquals(int(42), h.get(e.getKey()))
    assertThrows(classOf[NullPointerException], e.setValue(null))
    h.put(int(7), int(7))
    assertTrue(h.keySet().contains(int(7)))
    assertEquals(4, h.entrySet().size())
  }

  private val sets: Seq[(String, () => Set[Integer])] = Seq(
    "HashSet" -> (() =>
      new HashSet[Integer](Arrays.asList[Integer](0, 1, 2, 3, 4))
    ),
    "LinkedHashSet" ->
      (() => new LinkedHashSet[Integer](Arrays.asList[Integer](0, 1, 2, 3, 4)))
  )

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
            if (x.intValue() == 1) s.add(int(99))
        })
      )
      assertThrows(
        s"$name.removeIf",
        classOf[CME],
        s.removeIf(new Predicate[Integer] {
          def test(x: Integer): Boolean = {
            if (x.intValue() == 1) s.add(int(98))
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
    for ((name, mk) <- sets) {
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
}
