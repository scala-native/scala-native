package org.scalanative.testsuite.javalib.util

import java.util._
import java.util.function.{Consumer, Predicate, UnaryOperator}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ListFailFastTest {

  private type CME = ConcurrentModificationException

  private def fill[L <: List[Integer]](l: L): L = {
    var i = 0
    while (i < 5) {
      l.add(Integer.valueOf(i))
      i += 1
    }
    l
  }

  /* The sublists wrap the elements 0 until 5 between two outer elements,
   * so that their offset into the parent is not 0.
   */
  private def withSubList(parent: List[Integer]): List[Integer] = {
    parent.add(Integer.valueOf(-1))
    fill(parent)
    parent.add(Integer.valueOf(-2))
    parent.subList(1, 6)
  }

  private val factories: Seq[(String, () => List[Integer])] = Seq(
    "ArrayList" -> (() => fill(new ArrayList[Integer]())),
    "Vector" -> (() => fill(new Vector[Integer]())),
    "LinkedList" -> (() => fill(new LinkedList[Integer]())),
    "ArrayList.subList" -> (() => withSubList(new ArrayList[Integer]())),
    "Vector.subList" -> (() => withSubList(new Vector[Integer]())),
    "LinkedList.subList" -> (() => withSubList(new LinkedList[Integer]())),
    "ArrayList.subList.subList" ->
      (() => withSubList(new ArrayList[Integer]()).subList(0, 5)),
    "LinkedList.subList.subList" ->
      (() => withSubList(new LinkedList[Integer]()).subList(0, 5))
  )

  private val structuralChanges: Seq[(String, List[Integer] => Unit)] = Seq(
    "add" -> (l => l.add(Integer.valueOf(9))),
    "add(0, e)" -> (l => l.add(0, Integer.valueOf(9))),
    "remove(0)" -> (l => l.remove(0)),
    "remove(Object)" -> (l => l.remove(Integer.valueOf(3))),
    "clear" -> (l => l.clear()),
    "addAll" -> (l => l.addAll(Arrays.asList(Integer.valueOf(7)))),
    "addAll(0, c)" -> (l => l.addAll(0, Arrays.asList(Integer.valueOf(7)))),
    "removeIf" -> (l => l.removeIf(x => x.intValue() == 1)),
    "removeAll" -> (l => l.removeAll(Arrays.asList(Integer.valueOf(1)))),
    "retainAll" -> (l => l.retainAll(Arrays.asList(Integer.valueOf(1)))),
    "subList.clear" -> (l => l.subList(1, 3).clear()),
    "subList.add" -> (l => l.subList(1, 3).add(Integer.valueOf(9)))
  )

  private val nonStructuralChanges: Seq[(String, List[Integer] => Unit)] =
    Seq(
      "set" -> (l => l.set(0, Integer.valueOf(9))),
      "removeIf(none)" -> (l => l.removeIf(x => x.intValue() > 100)),
      "removeAll(none)" -> (l =>
        l.removeAll(Arrays.asList(Integer.valueOf(99)))
      ),
      "retainAll(all)" -> (l => l.retainAll(new ArrayList[Integer](l))),
      "get" -> (l => l.get(2)),
      "subList" -> (l => l.subList(1, 3)),
      "listIterator.set" -> { l =>
        val it = l.listIterator()
        it.next()
        it.set(Integer.valueOf(9))
      }
    )

  private def forAll(body: (String, () => List[Integer]) => Unit): Unit =
    factories.foreach { case (name, mk) => body(name, mk) }

  @Test def iteratorNextThrowsAfterStructuralChange(): Unit = forAll {
    (name, mk) =>
      structuralChanges.foreach {
        case (op, change) =>
          val l = mk()
          val it = l.iterator()
          it.next()
          change(l)
          if (name.contains("subList") && !it.hasNext()) {
            // The JVM AbstractList.subList iterator checks the bounds first
            try {
              it.next()
              fail(s"$name: next after $op should throw")
            } catch {
              case _: CME | _: NoSuchElementException => ()
            }
          } else {
            assertThrows(
              s"$name: next after $op",
              classOf[CME],
              it.next()
            )
          }
      }
  }

  @Test def iteratorSurvivesNonStructuralChange(): Unit = forAll { (name, mk) =>
    nonStructuralChanges.foreach {
      case (op, change) =>
        val l = mk()
        val it = l.iterator()
        it.next()
        change(l)
        assertTrue(s"$name: hasNext after $op", it.hasNext())
        assertEquals(s"$name: next after $op", l.get(1), it.next())
    }
  }

  @Test def listIteratorOperationsThrowAfterStructuralChange(): Unit =
    forAll { (name, mk) =>
      def stale(): ListIterator[Integer] = {
        val l = mk()
        val it = l.listIterator(1)
        it.next()
        l.add(Integer.valueOf(9))
        it
      }
      assertThrows(s"$name: next", classOf[CME], stale().next())
      assertThrows(s"$name: previous", classOf[CME], stale().previous())
      assertThrows(s"$name: remove", classOf[CME], stale().remove())
      assertThrows(
        s"$name: set",
        classOf[CME],
        stale().set(Integer.valueOf(1))
      )
      assertThrows(
        s"$name: add",
        classOf[CME],
        stale().add(Integer.valueOf(1))
      )
      assertThrows(
        s"$name: forEachRemaining",
        classOf[CME],
        stale().forEachRemaining(new Consumer[Integer] {
          def accept(t: Integer): Unit = ()
        })
      )
    }

  @Test def iteratorDoesNotThrowOnItsOwnChanges(): Unit = forAll { (name, mk) =>
    val l = mk()
    val it = l.listIterator()
    assertEquals(0, it.next().intValue())
    it.remove()
    assertEquals(1, it.next().intValue())
    it.add(Integer.valueOf(10))
    assertEquals(2, it.next().intValue())
    assertEquals(2, it.previous().intValue())
    assertEquals(10, it.previous().intValue())
    it.set(Integer.valueOf(11))
    while (it.hasNext()) it.next()
    assertEquals(
      s"$name contents",
      Arrays.asList[Integer](1, 11, 2, 3, 4),
      l
    )
  }

  @Test def otherIteratorsThrowAfterIteratorRemove(): Unit = forAll {
    (name, mk) =>
      val l = mk()
      val it1 = l.iterator()
      val it2 = l.iterator()
      it1.next()
      it2.next()
      it1.remove()
      assertEquals(1, it1.next().intValue())
      assertThrows(s"$name", classOf[CME], it2.next())
  }

  @Test def hasNextAfterClearIsFalse(): Unit = forAll { (name, mk) =>
    val l = mk()
    val it = l.iterator()
    l.clear()
    assertFalse(s"$name", it.hasNext())
  }

  @Test def forEachThrowsWhenActionAddsElements(): Unit = forAll { (name, mk) =>
    val l = mk()
    assertThrows(
      s"$name",
      classOf[CME],
      l.forEach(new Consumer[Integer] {
        def accept(x: Integer): Unit =
          if (x.intValue() == 1) l.add(Integer.valueOf(9))
      })
    )
  }

  @Test def removeIfThrowsWhenPredicateAddsElements(): Unit = forAll {
    (name, mk) =>
      val l = mk()
      assertThrows(
        s"$name",
        classOf[CME],
        l.removeIf(new Predicate[Integer] {
          def test(x: Integer): Boolean = {
            if (x.intValue() == 1) l.add(Integer.valueOf(9))
            x.intValue() == 2
          }
        })
      )
  }

  @Test def replaceAllThrowsWhenOperatorAddsElements(): Unit = forAll {
    (name, mk) =>
      val l = mk()
      assertThrows(
        s"$name",
        classOf[CME],
        l.replaceAll(new UnaryOperator[Integer] {
          def apply(x: Integer): Integer = {
            if (x.intValue() == 1) l.add(Integer.valueOf(9))
            x
          }
        })
      )
  }

  @Test def spliteratorIsLateBindingAndFailFast(): Unit = forAll { (name, mk) =>
    val l = mk()
    val lateBinding = l.spliterator()
    l.add(Integer.valueOf(5))
    val seen = new ArrayList[Integer]()
    lateBinding.forEachRemaining(new Consumer[Integer] {
      def accept(x: Integer): Unit = seen.add(x)
    })
    assertEquals(s"$name late binding", l, seen)

    val started = l.spliterator()
    assertTrue(started.tryAdvance(new Consumer[Integer] {
      def accept(x: Integer): Unit = ()
    }))
    l.add(Integer.valueOf(6))
    assertThrows(
      s"$name: forEachRemaining after change",
      classOf[CME],
      started.forEachRemaining(new Consumer[Integer] {
        def accept(x: Integer): Unit = ()
      })
    )

    val mutating = l.spliterator()
    assertThrows(
      s"$name: action adds element",
      classOf[CME],
      mutating.forEachRemaining(new Consumer[Integer] {
        def accept(x: Integer): Unit =
          if (x.intValue() == 1) l.add(Integer.valueOf(9))
      })
    )
  }

  @Test def arrayListAndVectorStructuralChanges(): Unit = {
    val changes: Seq[(String, List[Integer] => Unit)] = Seq(
      "sort" -> (l => l.sort(Collections.reverseOrder[Integer]())),
      "replaceAll" -> (l => l.replaceAll(x => Integer.valueOf(x + 1))),
      "ensureCapacity" -> {
        case l: ArrayList[_] => l.ensureCapacity(1000)
        case l: Vector[_]    => l.ensureCapacity(1000)
        case _               => ()
      },
      "trimToSize" -> {
        case l: ArrayList[_] => l.trimToSize()
        case l: Vector[_]    => l.trimToSize()
        case _               => ()
      }
    )
    for {
      (name, mk) <- Seq[(String, () => List[Integer])](
        "ArrayList" -> (() => fill(new ArrayList[Integer]())),
        "Vector" -> (() => fill(new Vector[Integer]()))
      )
      (op, change) <- changes
    } {
      val l = mk()
      val it = l.iterator()
      it.next()
      change(l)
      assertThrows(s"$name: next after $op", classOf[CME], it.next())
    }

    val v = fill(new Vector[Integer]())
    val it = v.iterator()
    it.next()
    v.setSize(7)
    assertThrows("Vector: next after setSize", classOf[CME], it.next())
  }

  @Test def vectorElementsIsNotFailFast(): Unit = {
    val v = fill(new Vector[Integer]())
    val e = v.elements()
    assertEquals(0, e.nextElement().intValue())
    v.remove(0)
    v.add(Integer.valueOf(9))
    assertEquals(2, e.nextElement().intValue())
  }

  @Test def linkedListDescendingIteratorIsFailFast(): Unit = {
    val l = fill(new LinkedList[Integer]())
    val it = l.descendingIterator()
    assertEquals(4, it.next().intValue())
    l.addFirst(Integer.valueOf(9))
    assertThrows(classOf[CME], it.next())

    val it2 = l.descendingIterator()
    it2.next()
    l.set(0, Integer.valueOf(8))
    assertEquals(3, it2.next().intValue())
    it2.remove()
    assertEquals(2, it2.next().intValue())
  }

  @Test def subListThrowsAfterParentStructuralChange(): Unit = {
    val ops: Seq[(String, List[Integer] => Any)] = Seq(
      "size" -> (_.size()),
      "isEmpty" -> (_.isEmpty()),
      "get" -> (_.get(0)),
      "set" -> (_.set(0, Integer.valueOf(1))),
      "add" -> (_.add(Integer.valueOf(1))),
      "add(0, e)" -> (_.add(0, Integer.valueOf(1))),
      "remove(0)" -> (_.remove(0)),
      "iterator" -> (_.iterator()),
      "listIterator" -> (_.listIterator()),
      "clear" -> (_.clear()),
      "toString" -> (_.toString()),
      "hashCode" -> (_.hashCode()),
      "contains" -> (_.contains(Integer.valueOf(1))),
      "indexOf" -> (_.indexOf(Integer.valueOf(1))),
      "toArray" -> (_.toArray()),
      "forEach" -> (_.forEach(new Consumer[Integer] {
        def accept(x: Integer): Unit = ()
      }))
    )
    for {
      (name, mk) <- Seq[(String, () => List[Integer])](
        "ArrayList" -> (() => fill(new ArrayList[Integer]())),
        "Vector" -> (() => fill(new Vector[Integer]())),
        "LinkedList" -> (() => fill(new LinkedList[Integer]()))
      )
      (op, f) <- ops
    } {
      val parent = mk()
      val sub = parent.subList(1, 4)
      parent.add(Integer.valueOf(9))
      assertThrows(s"$name.subList: $op", classOf[CME], f(sub))

      val parent2 = mk()
      val sub2 = parent2.subList(1, 4)
      parent2.set(0, Integer.valueOf(9))
      f(sub2) // non-structural change of the parent is fine
    }
  }

  @Test def subListChangesKeepParentAndNestedViewsInSync(): Unit = {
    for (mk <- Seq[() => List[Integer]](
          () => fill(new ArrayList[Integer]()),
          () => fill(new Vector[Integer]()),
          () => fill(new LinkedList[Integer]())
        )) {
      val l = mk()
      val s = l.subList(1, 4)
      val ss = s.subList(0, 2)
      ss.add(Integer.valueOf(7))
      assertEquals(4, s.size())
      assertEquals(Arrays.asList[Integer](0, 1, 2, 7, 3, 4), l)

      val it = ss.listIterator()
      it.next()
      it.remove()
      it.add(Integer.valueOf(8))
      it.next()
      assertEquals(Arrays.asList[Integer](8, 2, 7), ss)
      assertEquals(Arrays.asList[Integer](8, 2, 7, 3), s)
      assertEquals(Arrays.asList[Integer](0, 8, 2, 7, 3, 4), l)

      s.add(Integer.valueOf(5))
      assertThrows(classOf[CME], ss.size())
      assertEquals(5, s.size())

      val sibling = l.subList(0, 2)
      s.remove(0)
      assertThrows(classOf[CME], sibling.size())

      val parentIt = l.iterator()
      parentIt.next()
      s.add(Integer.valueOf(6))
      assertThrows(classOf[CME], parentIt.next())

      val nested = l.subList(1, 4).subList(0, 2)
      l.add(Integer.valueOf(1))
      assertThrows(classOf[CME], nested.size())
      assertThrows(classOf[CME], nested.get(0))
    }
  }

  @Test def subListOfStaleSubListIsStale(): Unit = {
    val l = fill(new ArrayList[Integer]())
    val parent = l.subList(1, 3)
    l.clear()
    val child = parent.subList(0, 1)
    assertThrows(classOf[CME], child.size())
    assertThrows(classOf[CME], child.get(0))
  }

  @Test def arrayListSubListRemoveIfThrowsWhenPredicateClearsTheView(): Unit = {
    val l = new ArrayList[Integer](Arrays.asList[Integer](9, 0, 1, 2, 9))
    val view = l.subList(1, 4)
    assertThrows(
      classOf[CME],
      view.removeIf(new Predicate[Integer] {
        def test(x: Integer): Boolean = {
          view.clear()
          false
        }
      })
    )
    assertEquals(Arrays.asList[Integer](9, 9), l)
  }

  @Test def arrayListSubListBulkRemovalAcceptsSiblingView(): Unit = {
    def mk() = new ArrayList[Integer](Arrays.asList[Integer](0, 1, 0, 1, 2))
    def expect(expected: Int*)(l: List[Integer]) =
      assertEquals(expected.mkString("[", ", ", "]"), l.toString())

    val l1 = mk()
    assertTrue(l1.subList(2, 5).removeAll(l1.subList(0, 2)))
    expect(0, 1, 2)(l1)

    val l2 = mk()
    assertTrue(l2.subList(2, 5).retainAll(l2.subList(0, 2)))
    expect(0, 1, 0, 1)(l2)

    val l3 = mk()
    val sibling = l3.subList(0, 2)
    assertTrue(
      l3.subList(2, 5)
        .removeIf(new Predicate[Integer] {
          def test(e: Integer): Boolean = sibling.contains(e)
        })
    )
    expect(0, 1, 2)(l3)

    val l4 = mk()
    val view = l4.subList(1, 5)
    assertFalse(view.removeAll(Collections.singleton(Integer.valueOf(9))))
    assertEquals(4, view.size())
  }

  @Test def emptyClearThroughViewInvalidatesOnlyIfListCountsIt(): Unit = {
    class Custom extends AbstractList[Integer] {
      private val inner = fill(new ArrayList[Integer]())
      def get(i: Int): Integer = inner.get(i)
      def size(): Int = inner.size()
      override def add(i: Int, e: Integer): Unit = {
        inner.add(i, e); modCount += 1
      }
      override def remove(i: Int): Integer = { modCount += 1; inner.remove(i) }
    }
    class RandomCustom extends Custom with RandomAccess

    def check(name: String, l: List[Integer], expectCME: Boolean): Unit = {
      val view = l.subList(0, 5)
      val it = view.iterator()
      it.next()
      view.subList(0, 0).clear()
      if (expectCME) assertThrows(name, classOf[CME], it.next())
      else assertEquals(name, 1, it.next().intValue())
    }

    // ArrayList and Vector count an empty range removal, as on the JVM
    check("ArrayList", fill(new ArrayList[Integer]()), expectCME = true)
    check("Vector", fill(new Vector[Integer]()), expectCME = true)
    check("LinkedList", fill(new LinkedList[Integer]()), expectCME = false)
    check("custom", new Custom, expectCME = false)
    check("custom RandomAccess", new RandomCustom, expectCME = false)
  }

  @Test def arrayListSpliteratorBindsOnEstimateSize(): Unit = {
    val l = fill(new ArrayList[Integer]())
    val sized = l.spliterator()
    assertEquals(5L, sized.estimateSize())
    l.add(Integer.valueOf(5))
    assertEquals(5L, sized.estimateSize())
    assertThrows(
      classOf[CME],
      sized.forEachRemaining(new Consumer[Integer] {
        def accept(x: Integer): Unit = ()
      })
    )

    val split = fill(new ArrayList[Integer]()).spliterator()
    val prefix = split.trySplit()
    assertEquals(2L, prefix.estimateSize())
    assertEquals(3L, split.estimateSize())
  }

  @Test def customAbstractListUsesModCount(): Unit = {
    class Growable extends AbstractList[Integer] with RandomAccess {
      var n = 3
      def get(i: Int): Integer = {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException(i.toString)
        Integer.valueOf(i)
      }
      def size(): Int = n
      def grow(): Unit = {
        n += 1
        modCount += 1
      }
      def currentModCount: Int = modCount
    }

    val g = new Growable
    assertEquals(0, g.currentModCount)
    val it = g.iterator()
    it.next()
    g.grow()
    assertThrows(classOf[CME], it.next())

    val sub = g.subList(0, 2)
    g.grow()
    assertThrows(classOf[CME], sub.size())

    // A list that never updates modCount is not fail-fast.
    class Quiet extends AbstractList[Integer] {
      val inner = new ArrayList[Integer](Arrays.asList[Integer](1, 2, 3))
      def get(i: Int): Integer = inner.get(i)
      def size(): Int = inner.size()
      override def remove(i: Int): Integer = inner.remove(i)
      def currentModCount: Int = modCount
    }
    val q = new Quiet
    val qit = q.iterator()
    qit.next()
    qit.remove()
    assertEquals(2, qit.next().intValue())
    q.clear()
    assertEquals(0, q.currentModCount)
    assertTrue(q.isEmpty())
  }

  @Test def nextAfterShrinkWithoutModCountThrowsNoSuchElement(): Unit = {
    class Shrinking extends AbstractList[Integer] {
      var n = 3
      def get(i: Int): Integer = {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException(i.toString)
        Integer.valueOf(i)
      }
      def size(): Int = n
    }
    val s = new Shrinking
    val it = s.iterator()
    it.next()
    it.next()
    s.n = 1
    assertTrue(it.hasNext())
    assertThrows(classOf[NoSuchElementException], it.next())
  }

  @Test def bulkOperationsAcceptTheListOrItsViews(): Unit = forAll {
    (name, mk) =>
      def check(op: String, expected: List[Integer])(
          f: List[Integer] => Any
      ): Unit = {
        val l = mk()
        f(l)
        assertEquals(s"$name: $op", expected, l)
      }
      check(
        "addAll(self)",
        Arrays.asList[Integer](0, 1, 2, 3, 4, 0, 1, 2, 3, 4)
      )(l => l.addAll(l))
      check(
        "addAll(0, self)",
        Arrays.asList[Integer](0, 1, 2, 3, 4, 0, 1, 2, 3, 4)
      )(l => l.addAll(0, l))
      check("addAll(subList)", Arrays.asList[Integer](0, 1, 2, 3, 4, 0, 1))(l =>
        l.addAll(l.subList(0, 2))
      )
      check("addAll(0, subList)", Arrays.asList[Integer](1, 2, 0, 1, 2, 3, 4))(
        l => l.addAll(0, l.subList(1, 3))
      )
      check("removeAll(self)", Arrays.asList[Integer]())(l => l.removeAll(l))
      check("retainAll(self)", Arrays.asList[Integer](0, 1, 2, 3, 4))(l =>
        l.retainAll(l)
      )
  }

  @Test def arrayListAndVectorBulkRemoveAcceptTheirViews(): Unit = {
    for (mk <- Seq[() => List[Integer]](
          () => fill(new ArrayList[Integer]()),
          () => fill(new Vector[Integer]())
        )) {
      val l1 = mk()
      assertTrue(l1.removeAll(l1.subList(0, 2)))
      assertEquals(Arrays.asList[Integer](2, 3, 4), l1)
      val l2 = mk()
      assertTrue(l2.retainAll(l2.subList(0, 2)))
      assertEquals(Arrays.asList[Integer](0, 1), l2)
      val l3 = mk()
      assertFalse(l3.retainAll(l3.subList(0, 5)))
      assertEquals(5, l3.size())
    }
  }
}
