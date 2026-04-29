/* Ported from public-domain JSR166 TCK.
 * URL:
 *   https://gee.cs.oswego.edu/dl/concurrency-interest/
 *
 * Modified for Scala Native.
 */

/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicReference}
import java.util.concurrent.{Executors, TimeUnit}
import java.util.function.{Consumer, Predicate}
import java.util.{concurrent => juc}
import java.{util => ju}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.DelayQueueTest.PDelay
import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class Collection8Test extends JSR166Test {
  import JSR166Test._

  private trait CollectionImplementation {
    def name: String
    def klazz(): Class[_]
    def emptyCollection(): ju.Collection[AnyRef]
    def makeElement(i: Int): AnyRef = itemFor(i)
    def isConcurrent(): Boolean
    def permitsNulls(): Boolean
  }

  private abstract class ItemCollectionImplementation(
      val name: String,
      val klazzValue: Class[_],
      val concurrent: Boolean,
      val nulls: Boolean
  ) extends CollectionImplementation {
    override def klazz(): Class[_] = klazzValue
    override def isConcurrent(): Boolean = concurrent
    override def permitsNulls(): Boolean = nulls
  }

  private def randomEmptySubList(
      parent: CollectionImplementation
  ): ju.Collection[AnyRef] = {
    val list = parent.emptyCollection().asInstanceOf[ju.List[AnyRef]]
    val rnd = juc.ThreadLocalRandom.current()
    if (rnd.nextBoolean()) list.add(parent.makeElement(rnd.nextInt()))
    val i = rnd.nextInt(list.size() + 1)
    list.subList(i, i)
  }

  private val arrayListImpl = new ItemCollectionImplementation(
    "ArrayList",
    classOf[ju.ArrayList[_]],
    concurrent = false,
    nulls = true
  ) {
    override def emptyCollection(): ju.Collection[AnyRef] =
      new ju.ArrayList[AnyRef]()
  }

  private val vectorImpl = new ItemCollectionImplementation(
    "Vector",
    classOf[ju.Vector[_]],
    concurrent = false,
    nulls = true
  ) {
    override def emptyCollection(): ju.Collection[AnyRef] =
      new ju.Vector[AnyRef]()
  }

  private val linkedListImpl = new ItemCollectionImplementation(
    "LinkedList",
    classOf[ju.LinkedList[_]],
    concurrent = false,
    nulls = true
  ) {
    override def emptyCollection(): ju.Collection[AnyRef] =
      new ju.LinkedList[AnyRef]()
  }

  private val copyOnWriteArrayListImpl = new ItemCollectionImplementation(
    "CopyOnWriteArrayList",
    classOf[juc.CopyOnWriteArrayList[_]],
    concurrent = true,
    nulls = true
  ) {
    override def emptyCollection(): ju.Collection[AnyRef] =
      new juc.CopyOnWriteArrayList[AnyRef]()
  }

  private val allImplementations: Array[CollectionImplementation] = Array(
    new ItemCollectionImplementation(
      "ArrayDeque",
      classOf[ju.ArrayDeque[_]],
      concurrent = false,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new ju.ArrayDeque[AnyRef]()
    },
    arrayListImpl,
    new ItemCollectionImplementation(
      "ArrayList.subList",
      classOf[ju.ArrayList[_]],
      concurrent = false,
      nulls = true
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        arrayListImpl
          .emptyCollection()
          .asInstanceOf[ju.List[AnyRef]]
          .subList(0, 0)
    },
    vectorImpl,
    new ItemCollectionImplementation(
      "Vector.subList",
      classOf[ju.Vector[_]],
      concurrent = false,
      nulls = true
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        randomEmptySubList(vectorImpl)
    },
    linkedListImpl,
    new ItemCollectionImplementation(
      "LinkedList.subList",
      classOf[ju.LinkedList[_]],
      concurrent = false,
      nulls = true
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        randomEmptySubList(linkedListImpl)
    },
    new ItemCollectionImplementation(
      "PriorityQueue",
      classOf[ju.PriorityQueue[_]],
      concurrent = false,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new ju.PriorityQueue[AnyRef]()
    },
    new ItemCollectionImplementation(
      "ConcurrentLinkedQueue",
      classOf[juc.ConcurrentLinkedQueue[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.ConcurrentLinkedQueue[AnyRef]()
    },
    new ItemCollectionImplementation(
      "ConcurrentLinkedDeque",
      classOf[juc.ConcurrentLinkedDeque[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.ConcurrentLinkedDeque[AnyRef]()
    },
    new ItemCollectionImplementation(
      "ArrayBlockingQueue",
      classOf[juc.ArrayBlockingQueue[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.ArrayBlockingQueue[AnyRef](SIZE)
    },
    new ItemCollectionImplementation(
      "LinkedBlockingQueue",
      classOf[juc.LinkedBlockingQueue[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.LinkedBlockingQueue[AnyRef]()
    },
    new ItemCollectionImplementation(
      "LinkedBlockingDeque",
      classOf[juc.LinkedBlockingDeque[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.LinkedBlockingDeque[AnyRef]()
    },
    new ItemCollectionImplementation(
      "LinkedTransferQueue",
      classOf[juc.LinkedTransferQueue[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.LinkedTransferQueue[AnyRef]()
    },
    new ItemCollectionImplementation(
      "PriorityBlockingQueue",
      classOf[juc.PriorityBlockingQueue[_]],
      concurrent = true,
      nulls = false
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.PriorityBlockingQueue[AnyRef]()
    },
    copyOnWriteArrayListImpl,
    new ItemCollectionImplementation(
      "CopyOnWriteArrayList.subList",
      classOf[juc.CopyOnWriteArrayList[_]],
      concurrent = true,
      nulls = true
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        randomEmptySubList(copyOnWriteArrayListImpl)
    },
    new ItemCollectionImplementation(
      "CopyOnWriteArraySet",
      classOf[juc.CopyOnWriteArraySet[_]],
      concurrent = true,
      nulls = true
    ) {
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.CopyOnWriteArraySet[AnyRef]()
    },
    new CollectionImplementation {
      override def name: String = "DelayQueue"
      override def klazz(): Class[_] = classOf[juc.DelayQueue[_]]
      override def emptyCollection(): ju.Collection[AnyRef] =
        new juc.DelayQueue[PDelay]().asInstanceOf[ju.Collection[AnyRef]]
      override def makeElement(i: Int): AnyRef = new PDelay(i)
      override def isConcurrent(): Boolean = true
      override def permitsNulls(): Boolean = false
    }
  )

  private val collection8Issue4850: Set[String] = Set(
    "ArrayList",
    "ArrayList.subList",
    "Vector",
    "Vector.subList",
    "LinkedList.subList",
    "ConcurrentLinkedDeque",
    "LinkedBlockingDeque",
    "PriorityBlockingQueue",
    "CopyOnWriteArrayList",
    "CopyOnWriteArrayList.subList",
    "CopyOnWriteArraySet"
  )

  private val implementations: Array[CollectionImplementation] =
    allImplementations.filterNot(impl => collection8Issue4850(impl.name))

  private def forAllImplementations(testName: String)(
      body: CollectionImplementation => Unit
  ): Unit = {
    implementations.foreach { impl =>
      try body(impl)
      catch {
        case fail: AssertionError =>
          throw new AssertionError(s"$testName failed for ${impl.name}", fail)
        case fail: Throwable =>
          throw new AssertionError(s"$testName failed for ${impl.name}", fail)
      }
    }
  }

  private def bomb(): AnyRef = new Object {
    override def equals(x: Any): Boolean = throw new AssertionError()
    override def hashCode(): Int = throw new AssertionError()
    override def toString(): String = throw new AssertionError()
  }

  @Test def testEmptyMeansEmpty(): Unit =
    forAllImplementations("testEmptyMeansEmpty") { impl =>
      emptyMeansEmpty(impl, impl.emptyCollection())
      cloneableClone(impl.emptyCollection()) match {
        case null  =>
        case clone => emptyMeansEmpty(impl, clone)
      }
    }

  private def emptyMeansEmpty(
      impl: CollectionImplementation,
      c: ju.Collection[AnyRef]
  ): Unit = {
    assertTrue(c.isEmpty())
    mustEqual(0, c.size())
    mustEqual("[]", c.toString())
    if (c.isInstanceOf[ju.List[_]]) {
      val x = c.asInstanceOf[ju.List[AnyRef]]
      mustEqual(1, x.hashCode())
      mustEqual(x, ju.Collections.emptyList[AnyRef]())
      mustEqual(ju.Collections.emptyList[AnyRef](), x)
      mustEqual(-1, x.indexOf(impl.makeElement(86)))
      mustEqual(-1, x.lastIndexOf(impl.makeElement(99)))
      assertThrows(classOf[IndexOutOfBoundsException], x.get(0))
      assertThrows(
        classOf[IndexOutOfBoundsException],
        x.set(0, impl.makeElement(42))
      )
    } else if (c.isInstanceOf[ju.Set[_]]) {
      mustEqual(0, c.hashCode())
      mustEqual(c, ju.Collections.emptySet[AnyRef]())
      mustEqual(ju.Collections.emptySet[AnyRef](), c)
    }
    val a = c.toArray()
    mustEqual(0, a.length)
    assertSame(classOf[Array[Object]], a.getClass())

    val emptyObjects = new Array[Object](0)
    assertSame(emptyObjects, c.toArray(emptyObjects))

    val emptyItems = new Array[Item](0)
    assertSame(emptyItems, c.toArray(emptyItems))

    val items = Array(itemFor(1), itemFor(2), itemFor(3))
    assertSame(items, c.toArray(items))
    assertNull(items(0))
    mustEqual(2, items(1))
    mustEqual(3, items(2))

    assertIteratorExhausted(c.iterator())
    val alwaysThrows: Consumer[AnyRef] = _ => throw new AssertionError()
    c.forEach(alwaysThrows)
    c.iterator().forEachRemaining(alwaysThrows)
    c.spliterator().forEachRemaining(alwaysThrows)
    assertFalse(c.spliterator().tryAdvance(alwaysThrows))
    if (c.spliterator().hasCharacteristics(ju.Spliterator.SIZED))
      mustEqual(0L, c.spliterator().estimateSize())
    assertFalse(c.contains(bomb()))
    assertFalse(c.remove(bomb()))
    c match {
      case q: ju.Queue[_] =>
        assertNull(q.peek())
        assertNull(q.poll())
      case _ =>
    }
    c match {
      case d: ju.Deque[_] =>
        assertNull(d.peekFirst())
        assertNull(d.peekLast())
        assertNull(d.pollFirst())
        assertNull(d.pollLast())
        assertIteratorExhausted(d.descendingIterator())
        d.descendingIterator().forEachRemaining(alwaysThrows)
        assertFalse(d.removeFirstOccurrence(bomb()))
        assertFalse(d.removeLastOccurrence(bomb()))
      case _ =>
    }
    c match {
      case q: juc.BlockingQueue[_] =>
        assertNull(q.poll(randomExpiredTimeout(), randomTimeUnit()))
      case _ =>
    }
    c match {
      case q: juc.BlockingDeque[_] =>
        assertNull(q.pollFirst(randomExpiredTimeout(), randomTimeUnit()))
        assertNull(q.pollLast(randomExpiredTimeout(), randomTimeUnit()))
      case _ =>
    }
  }

  @Test def testNullPointerExceptions(): Unit =
    forAllImplementations("testNullPointerExceptions") { impl =>
      val c = impl.emptyCollection()
      assertThrowsAll(classOf[NullPointerException])(
        () => c.addAll(null),
        () => c.containsAll(null),
        () => c.retainAll(null),
        () => c.removeAll(null),
        () => c.removeIf(null),
        () => c.forEach(null),
        () => c.toArray(null.asInstanceOf[Array[Object]])
      )
      if (!impl.permitsNulls())
        assertThrows(classOf[NullPointerException], c.add(null))
      c match {
        case q: ju.Queue[AnyRef @unchecked] if !impl.permitsNulls() =>
          assertThrows(classOf[NullPointerException], q.offer(null))
        case _ =>
      }
      c match {
        case d: ju.Deque[AnyRef @unchecked] if !impl.permitsNulls() =>
          assertThrowsAll(classOf[NullPointerException])(
            () => d.addFirst(null),
            () => d.addLast(null),
            () => d.offerFirst(null),
            () => d.offerLast(null),
            () => d.push(null),
            () => d.descendingIterator().forEachRemaining(null)
          )
        case _ =>
      }
      c match {
        case q: juc.BlockingQueue[AnyRef @unchecked] =>
          assertThrowsAll(classOf[NullPointerException])(
            () => q.offer(null, 1L, TimeUnit.HOURS),
            () => q.put(null)
          )
        case _ =>
      }
      c match {
        case q: juc.BlockingDeque[AnyRef @unchecked] =>
          assertThrowsAll(classOf[NullPointerException])(
            () => q.offerFirst(null, 1L, TimeUnit.HOURS),
            () => q.offerLast(null, 1L, TimeUnit.HOURS),
            () => q.putFirst(null),
            () => q.putLast(null)
          )
        case _ =>
      }
    }

  @Test def testNoSuchElementExceptions(): Unit =
    forAllImplementations("testNoSuchElementExceptions") { impl =>
      val c = impl.emptyCollection()
      assertThrows(classOf[NoSuchElementException], c.iterator().next())
      c match {
        case q: ju.Queue[_] =>
          assertThrowsAll(classOf[NoSuchElementException])(
            () => q.element(),
            () => q.remove()
          )
        case _ =>
      }
      c match {
        case d: ju.Deque[_] =>
          assertThrowsAll(classOf[NoSuchElementException])(
            () => d.getFirst(),
            () => d.getLast(),
            () => d.removeFirst(),
            () => d.removeLast(),
            () => d.pop(),
            () => d.descendingIterator().next()
          )
        case _ =>
      }
      c match {
        case x: ju.List[_] =>
          assertThrowsAll(classOf[NoSuchElementException])(
            () => x.iterator().next(),
            () => x.listIterator().next(),
            () => x.listIterator(0).next(),
            () => x.listIterator().previous(),
            () => x.listIterator(0).previous()
          )
        case _ =>
      }
    }

  @Test def testRemoveIf(): Unit =
    forAllImplementations("testRemoveIf") { impl =>
      val c = impl.emptyCollection()
      val ordered = c.spliterator().hasCharacteristics(ju.Spliterator.ORDERED)
      val rnd = juc.ThreadLocalRandom.current()
      val n = rnd.nextInt(6)
      var i = 0
      while (i < n) {
        c.add(impl.makeElement(i))
        i += 1
      }
      val threwAt = new AtomicReference[AnyRef](null)
      val orig =
        if (rnd.nextBoolean()) new ju.ArrayList[AnyRef](c)
        else new ju.ArrayList[AnyRef](ju.Arrays.asList(c.toArray(): _*))
      val it = if (rnd.nextBoolean()) c.iterator() else null
      val survivors = new ju.ArrayList[AnyRef]()
      val accepts = new ju.ArrayList[AnyRef]()
      val rejects = new ju.ArrayList[AnyRef]()
      val randomPredicate: Predicate[AnyRef] = e => {
        assertNull(threwAt.get())
        rnd.nextInt(3) match {
          case 0 =>
            accepts.add(e); true
          case 1 =>
            rejects.add(e); false
          case 2 =>
            threwAt.set(e); throw new ArithmeticException()
        }
      }
      try {
        try {
          val modified = c.removeIf(randomPredicate)
          assertNull(threwAt.get())
          mustEqual(modified, accepts.size() > 0)
          mustEqual(modified, rejects.size() != n)
          mustEqual(accepts.size() + rejects.size(), n)
          if (ordered)
            mustEqual(
              rejects,
              new ju.ArrayList[AnyRef](ju.Arrays.asList(c.toArray(): _*))
            )
          else
            mustEqual(
              new ju.HashSet[AnyRef](rejects),
              new ju.HashSet[AnyRef](ju.Arrays.asList(c.toArray(): _*))
            )
        } catch {
          case _: ArithmeticException =>
            assertNotNull(threwAt.get())
            assertTrue(c.contains(threwAt.get()))
        }
        if (it != null && impl.isConcurrent())
          while (it.hasNext()) assertTrue(orig.contains(it.next()))
        rnd.nextInt(4) match {
          case 0 => survivors.addAll(c)
          case 1 => survivors.addAll(ju.Arrays.asList(c.toArray(): _*))
          case 2 => c.forEach((e: AnyRef) => survivors.add(e))
          case 3 =>
            val z = c.iterator()
            while (z.hasNext()) survivors.add(z.next())
        }
        assertTrue(orig.containsAll(accepts))
        assertTrue(orig.containsAll(rejects))
        assertTrue(orig.containsAll(survivors))
        assertTrue(orig.containsAll(c))
        assertTrue(c.containsAll(rejects))
        assertTrue(c.containsAll(survivors))
        assertTrue(survivors.containsAll(rejects))
        if (threwAt.get() == null) {
          mustEqual(n - accepts.size(), c.size())
          val z = accepts.iterator()
          while (z.hasNext()) assertFalse(c.contains(z.next()))
        } else {
          assertTrue(n == c.size() || n == c.size() + accepts.size())
          var k = 0
          val z = accepts.iterator()
          while (z.hasNext()) if (c.contains(z.next())) k += 1
          assertTrue(k == accepts.size() || k == 0)
        }
      } catch {
        case fail: Throwable =>
          System.err.println(impl.klazz())
          throw fail
      }
    }

  @Test def testElementRemovalDuringTraversal(): Unit =
    forAllImplementations("testElementRemovalDuringTraversal") { impl =>
      val c = impl.emptyCollection()
      val rnd = juc.ThreadLocalRandom.current()
      val n = rnd.nextInt(6)
      val copy = new ju.ArrayList[AnyRef]()
      var i = 0
      while (i < n) {
        val x = impl.makeElement(i)
        copy.add(x)
        c.add(x)
        i += 1
      }
      val iterated = new ju.ArrayList[AnyRef]()
      val spliterated = new ju.ArrayList[AnyRef]()
      val s = c.spliterator()
      val it = c.iterator()
      i = rnd.nextInt(n + 1)
      while ({ i -= 1; i >= 0 }) {
        assertTrue(s.tryAdvance((e: AnyRef) => spliterated.add(e)))
        if (rnd.nextBoolean()) assertTrue(it.hasNext())
        iterated.add(it.next())
      }
      val alwaysThrows: Consumer[AnyRef] = _ => throw new AssertionError()
      if (s.hasCharacteristics(ju.Spliterator.CONCURRENT)) {
        c.clear()
        if (testImplementationDetails && !c
              .isInstanceOf[juc.ArrayBlockingQueue[_]]) {
          if (rnd.nextBoolean()) assertFalse(s.tryAdvance(alwaysThrows))
          else s.forEachRemaining(alwaysThrows)
        }
        if (it.hasNext()) iterated.add(it.next())
        if (rnd.nextBoolean()) assertIteratorExhausted(it)
      }
      assertTrue(copy.containsAll(iterated))
      assertTrue(copy.containsAll(spliterated))
    }

  @Test def testRandomElementRemovalDuringTraversal(): Unit =
    forAllImplementations("testRandomElementRemovalDuringTraversal") { impl =>
      val c = impl.emptyCollection()
      val rnd = juc.ThreadLocalRandom.current()
      val n = rnd.nextInt(6)
      val copy = new ju.ArrayList[AnyRef]()
      var i = 0
      while (i < n) {
        val x = impl.makeElement(i)
        copy.add(x)
        c.add(x)
        i += 1
      }
      val iterated = new ju.ArrayList[AnyRef]()
      val spliterated = new ju.ArrayList[AnyRef]()
      val removed = new ju.ArrayList[AnyRef]()
      val s = c.spliterator()
      val it = c.iterator()
      if (s.hasCharacteristics(ju.Spliterator.CONCURRENT) ||
          s.hasCharacteristics(ju.Spliterator.IMMUTABLE)) {
        i = rnd.nextInt(n + 1)
        while ({ i -= 1; i >= 0 }) {
          assertTrue(s.tryAdvance((_: AnyRef) => ()))
          if (rnd.nextBoolean()) assertTrue(it.hasNext())
          it.next()
        }
        var unsupportedIteratorRemove = false
        if (rnd.nextBoolean()) {
          val z = c.iterator()
          while (z.hasNext()) {
            val e = z.next()
            if (rnd.nextBoolean()) {
              try z.remove()
              catch {
                case _: UnsupportedOperationException =>
                  unsupportedIteratorRemove = true
              }
              removed.add(e)
            }
          }
        } else {
          c.removeIf((e: AnyRef) =>
            if (rnd.nextBoolean()) {
              removed.add(e); true
            } else false
          )
        }
        if (!unsupportedIteratorRemove) {
          s.forEachRemaining((e: AnyRef) => spliterated.add(e))
          while (it.hasNext()) iterated.add(it.next())
          assertTrue(copy.containsAll(iterated))
          assertTrue(copy.containsAll(spliterated))
          assertTrue(copy.containsAll(removed))
          if (s.hasCharacteristics(ju.Spliterator.CONCURRENT)) {
            val iteratedAndRemoved = new ju.ArrayList[AnyRef](iterated)
            val spliteratedAndRemoved = new ju.ArrayList[AnyRef](spliterated)
            iteratedAndRemoved.retainAll(removed)
            spliteratedAndRemoved.retainAll(removed)
            assertTrue(iteratedAndRemoved.size() <= 1)
            assertTrue(spliteratedAndRemoved.size() <= 1)
            if (testImplementationDetails && !c
                  .isInstanceOf[juc.ArrayBlockingQueue[_]])
              assertTrue(spliteratedAndRemoved.isEmpty())
          }
        }
      }
    }

  @Test def testTraversalEquivalence(): Unit =
    forAllImplementations("testTraversalEquivalence") { impl =>
      val c = impl.emptyCollection()
      val rnd = juc.ThreadLocalRandom.current()
      val n = rnd.nextInt(6)
      var i = 0
      while (i < n) {
        c.add(impl.makeElement(i))
        i += 1
      }
      val iterated = new ju.ArrayList[AnyRef]()
      val iteratedForEachRemaining = new ju.ArrayList[AnyRef]()
      val tryAdvanced = new ju.ArrayList[AnyRef]()
      val spliterated = new ju.ArrayList[AnyRef]()
      val splitonced = new ju.ArrayList[AnyRef]()
      val forEached = new ju.ArrayList[AnyRef]()
      val streamForEached = new ju.ArrayList[AnyRef]()
      val parallelStreamForEached = new juc.ConcurrentLinkedQueue[AnyRef]()
      val removeIfed = new ju.ArrayList[AnyRef]()
      val it0 = c.iterator()
      while (it0.hasNext()) iterated.add(it0.next())
      c.iterator()
        .forEachRemaining((e: AnyRef) => iteratedForEachRemaining.add(e))
      val s0 = c.spliterator()
      while (s0.tryAdvance((e: AnyRef) => tryAdvanced.add(e))) ()
      c.spliterator().forEachRemaining((e: AnyRef) => spliterated.add(e))
      val s1 = c.spliterator()
      val s2 = s1.trySplit()
      if (s2 != null) s2.forEachRemaining((e: AnyRef) => splitonced.add(e))
      s1.forEachRemaining((e: AnyRef) => splitonced.add(e))
      c.forEach((e: AnyRef) => forEached.add(e))
      c.stream().forEach((e: AnyRef) => streamForEached.add(e))
      c.parallelStream().forEach((e: AnyRef) => parallelStreamForEached.add(e))
      c.removeIf((e: AnyRef) => { removeIfed.add(e); false })
      val ordered = c.spliterator().hasCharacteristics(ju.Spliterator.ORDERED)
      if (c.isInstanceOf[ju.List[_]] || c.isInstanceOf[ju.Deque[_]])
        assertTrue(ordered)
      val cset = new ju.HashSet[AnyRef](c)
      mustEqual(cset, new ju.HashSet[AnyRef](parallelStreamForEached))
      if (ordered) {
        mustEqual(iterated, iteratedForEachRemaining)
        mustEqual(iterated, tryAdvanced)
        mustEqual(iterated, spliterated)
        mustEqual(iterated, splitonced)
        mustEqual(iterated, forEached)
        mustEqual(iterated, streamForEached)
        mustEqual(iterated, removeIfed)
      } else {
        mustEqual(cset, new ju.HashSet[AnyRef](iterated))
        mustEqual(cset, new ju.HashSet[AnyRef](iteratedForEachRemaining))
        mustEqual(cset, new ju.HashSet[AnyRef](tryAdvanced))
        mustEqual(cset, new ju.HashSet[AnyRef](spliterated))
        mustEqual(cset, new ju.HashSet[AnyRef](splitonced))
        mustEqual(cset, new ju.HashSet[AnyRef](forEached))
        mustEqual(cset, new ju.HashSet[AnyRef](streamForEached))
        mustEqual(cset, new ju.HashSet[AnyRef](removeIfed))
      }
      c match {
        case d: ju.Deque[AnyRef @unchecked] =>
          val descending = new ju.ArrayList[AnyRef]()
          val descendingForEachRemaining = new ju.ArrayList[AnyRef]()
          val it = d.descendingIterator()
          while (it.hasNext()) descending.add(it.next())
          d.descendingIterator()
            .forEachRemaining((e: AnyRef) => descendingForEachRemaining.add(e))
          ju.Collections.reverse(descending)
          ju.Collections.reverse(descendingForEachRemaining)
          mustEqual(iterated, descending)
          mustEqual(iterated, descendingForEachRemaining)
        case _ =>
      }
    }

  @Test def testForEachRemainingConsistentWithDefaultImplementation(): Unit =
    forAllImplementations(
      "testForEachRemainingConsistentWithDefaultImplementation"
    ) { impl =>
      val c = impl.emptyCollection()
      if (testImplementationDetails && c
            .getClass() != classOf[ju.LinkedList[_]]) {
        val rnd = juc.ThreadLocalRandom.current()
        val n = 1 + rnd.nextInt(3)
        var i = 0
        while (i < n) {
          c.add(impl.makeElement(i))
          i += 1
        }
        val iterated = new ju.ArrayList[AnyRef]()
        val iteratedForEachRemaining = new ju.ArrayList[AnyRef]()
        val it1 = c.iterator()
        val it2 = c.iterator()
        assertTrue(it1.hasNext())
        assertTrue(it2.hasNext())
        c.clear()
        val concurrentModificationResult = "ConcurrentModificationException"
        val r1 =
          try {
            while (it1.hasNext()) iterated.add(it1.next())
            iterated
          } catch {
            case _: ju.ConcurrentModificationException =>
              assertFalse(impl.isConcurrent())
              concurrentModificationResult
          }
        val r2 =
          try {
            it2.forEachRemaining((e: AnyRef) => iteratedForEachRemaining.add(e))
            iteratedForEachRemaining
          } catch {
            case _: ju.ConcurrentModificationException =>
              assertFalse(impl.isConcurrent())
              concurrentModificationResult
          }
        mustEqual(r1, r2)
      }
    }

  @Test def testRemoveAfterForEachRemaining(): Unit =
    forAllImplementations("testRemoveAfterForEachRemaining") { impl =>
      val c = impl.emptyCollection()
      val rnd = juc.ThreadLocalRandom.current()
      val copy = new ju.ArrayList[AnyRef]()
      val ordered = c.spliterator().hasCharacteristics(ju.Spliterator.ORDERED)
      val n = 3 + rnd.nextInt(2)
      var i = 0
      while (i < n) {
        val x = impl.makeElement(i)
        c.add(x)
        copy.add(x)
        i += 1
      }
      val it = c.iterator()
      if (ordered) {
        if (rnd.nextBoolean()) assertTrue(it.hasNext())
        mustEqual(impl.makeElement(0), it.next())
        if (rnd.nextBoolean()) assertTrue(it.hasNext())
        mustEqual(impl.makeElement(1), it.next())
      } else {
        if (rnd.nextBoolean()) assertTrue(it.hasNext())
        assertTrue(copy.contains(it.next()))
        if (rnd.nextBoolean()) assertTrue(it.hasNext())
        assertTrue(copy.contains(it.next()))
      }
      if (rnd.nextBoolean()) assertTrue(it.hasNext())
      it.forEachRemaining((e: AnyRef) => {
        assertTrue(c.contains(e))
        assertTrue(copy.contains(e))
      })
      if (testImplementationDetails) {
        if (c.isInstanceOf[juc.ArrayBlockingQueue[_]]) {
          assertIteratorExhausted(it)
        } else {
          try {
            it.remove()
            mustEqual(n - 1, c.size())
            if (ordered) {
              i = 0
              while (i < n - 1) {
                assertTrue(c.contains(impl.makeElement(i)))
                i += 1
              }
              assertFalse(c.contains(impl.makeElement(n - 1)))
            }
          } catch { case _: UnsupportedOperationException => () }
        }
      }
      c match {
        case _: ju.Deque[_] =>
          val d = impl.emptyCollection().asInstanceOf[ju.Deque[AnyRef]]
          assertTrue(ordered)
          i = 0
          while (i < n) {
            d.add(impl.makeElement(i))
            i += 1
          }
          val dit = d.descendingIterator()
          assertTrue(dit.hasNext())
          mustEqual(impl.makeElement(n - 1), dit.next())
          assertTrue(dit.hasNext())
          mustEqual(impl.makeElement(n - 2), dit.next())
          dit.forEachRemaining((e: AnyRef) => assertTrue(c.contains(e)))
          if (testImplementationDetails) {
            dit.remove()
            mustEqual(n - 1, d.size())
            i = 1
            while (i < n) {
              assertTrue(d.contains(impl.makeElement(i)))
              i += 1
            }
            assertFalse(d.contains(impl.makeElement(0)))
          }
        case _ =>
      }
    }

  @Test def testStreamForEach(): Unit =
    forAllImplementations("testStreamForEach") { impl =>
      val c = impl.emptyCollection()
      val x = impl.makeElement(1)
      val y = impl.makeElement(2)
      val found = new ju.ArrayList[AnyRef]()
      val spy: Consumer[AnyRef] = o => found.add(o)
      c.stream().forEach(spy)
      assertTrue(found.isEmpty())

      assertTrue(c.add(x))
      c.stream().forEach(spy)
      mustEqual(ju.Collections.singletonList(x), found)
      found.clear()

      assertTrue(c.add(y))
      c.stream().forEach(spy)
      mustEqual(2, found.size())
      assertTrue(found.contains(x))
      assertTrue(found.contains(y))
      found.clear()

      c.clear()
      c.stream().forEach(spy)
      assertTrue(found.isEmpty())
    }

  @Test def testStreamForEachConcurrentStressTest(): Unit =
    forAllImplementations("testStreamForEachConcurrentStressTest") { impl =>
      if (impl.isConcurrent()) {
        val c = impl.emptyCollection()
        val done = new AtomicBoolean(false)
        val elt = impl.makeElement(1)
        val pool = Executors.newCachedThreadPool()
        var f1: juc.Future[_] = null
        var f2: juc.Future[_] = null
        try {
          val threadsStarted = new juc.CountDownLatch(2)
          val checkElt = new Runnable {
            override def run(): Unit = {
              threadsStarted.countDown()
              while (!done.get())
                c.stream().forEach((x: AnyRef) => assertSame(elt, x))
            }
          }
          val addRemove = new Runnable {
            override def run(): Unit = {
              threadsStarted.countDown()
              while (!done.get()) {
                assertTrue(c.add(elt))
                assertTrue(c.remove(elt))
              }
            }
          }
          f1 = pool.submit(checkElt)
          f2 = pool.submit(addRemove)
          assertTrue(threadsStarted.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS))
          Thread.sleep(timeoutMillis())
        } finally {
          done.set(true)
          pool.shutdown()
          if (!pool.awaitTermination(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS))
            pool.shutdownNow()
        }
        assertNull(f1.get(LONG_DELAY_MS, TimeUnit.MILLISECONDS))
        assertNull(f2.get(LONG_DELAY_MS, TimeUnit.MILLISECONDS))
      }
    }

  @Test def testForEach(): Unit =
    forAllImplementations("testForEach") { impl =>
      val c = impl.emptyCollection()
      val x = impl.makeElement(1)
      val y = impl.makeElement(2)
      val found = new ju.ArrayList[AnyRef]()
      val spy: Consumer[AnyRef] = o => found.add(o)
      c.forEach(spy)
      assertTrue(found.isEmpty())

      assertTrue(c.add(x))
      c.forEach(spy)
      mustEqual(ju.Collections.singletonList(x), found)
      found.clear()

      assertTrue(c.add(y))
      c.forEach(spy)
      mustEqual(2, found.size())
      assertTrue(found.contains(x))
      assertTrue(found.contains(y))
      found.clear()

      c.clear()
      c.forEach(spy)
      assertTrue(found.isEmpty())
    }

  @Test def testStickySpliteratorExhaustion(): Unit =
    forAllImplementations("testStickySpliteratorExhaustion") { impl =>
      if (impl.isConcurrent() && testImplementationDetails) {
        val rnd = juc.ThreadLocalRandom.current()
        val alwaysThrows: Consumer[AnyRef] = _ => throw new AssertionError()
        val c = impl.emptyCollection()
        val s = c.spliterator()
        if (rnd.nextBoolean()) assertFalse(s.tryAdvance(alwaysThrows))
        else s.forEachRemaining(alwaysThrows)
        val one = impl.makeElement(1)
        c.add(one)
        if (rnd.nextBoolean()) assertFalse(s.tryAdvance(alwaysThrows))
        else s.forEachRemaining(alwaysThrows)
      }
    }

  @Ignore("scala-native#4850: Collection8 concurrent race stress can hang")
  @Test def testDetectRaces(): Unit =
    forAllImplementations("testDetectRaces") { impl =>
      if (impl.isConcurrent()) {
        val rnd = juc.ThreadLocalRandom.current()
        val c = impl.emptyCollection()
        val done = new AtomicBoolean(false)
        val one = impl.makeElement(1)
        val two = impl.makeElement(2)
        val checkSanity: Consumer[AnyRef] =
          x => assertTrue((x eq one) || (x eq two))
        val checkArraySanity: Consumer[Array[AnyRef]] =
          array => array.foreach(checkSanity.accept)
        val emptyArray = java.lang.reflect.Array
          .newInstance(one.getClass(), 0)
          .asInstanceOf[Array[AnyRef]]
        val threadsStarted = new juc.Phaser(1)
        val frobbers: Array[Runnable] = Array(
          () => c.forEach(checkSanity),
          () => c.stream().forEach(checkSanity),
          () => c.parallelStream().forEach(checkSanity),
          () => {
            c.spliterator().trySplit()
            ()
          },
          () => {
            val s = c.spliterator()
            s.tryAdvance(checkSanity)
            s.trySplit()
            ()
          },
          () => {
            val s = c.spliterator()
            while (s.tryAdvance(checkSanity)) ()
          },
          () => {
            val it = c.iterator()
            while (it.hasNext()) checkSanity.accept(it.next())
          },
          () => checkArraySanity.accept(c.toArray()),
          () => checkArraySanity.accept(c.toArray(emptyArray)),
          () => {
            val a = new Array[AnyRef](5)
            val three = impl.makeElement(3)
            ju.Arrays.fill(a, 0, a.length, three)
            val x = c.toArray(a)
            if (x eq a) {
              var i = 0
              while (i < a.length && a(i) != null) {
                checkSanity.accept(a(i))
                i += 1
              }
            } else checkArraySanity.accept(x)
          },
          adderRemover(c, one),
          adderRemover(c, two)
        )
        val tasks = frobbers.filter(_ => rnd.nextBoolean())
        val futures = new ju.ArrayList[juc.Future[_]]()
        val pool = Executors.newCachedThreadPool()
        try {
          threadsStarted.bulkRegister(tasks.length)
          tasks.foreach { task =>
            futures.add(pool.submit(new Runnable {
              override def run(): Unit = {
                threadsStarted.arriveAndAwaitAdvance()
                while (!done.get()) task.run()
              }
            }))
          }
          threadsStarted.arriveAndDeregister()
          Thread.sleep(timeoutMillis())
        } finally {
          done.set(true)
          pool.shutdown()
          if (!pool.awaitTermination(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS))
            pool.shutdownNow()
        }
        val it = futures.iterator()
        while (it.hasNext())
          assertNull(it.next().get(LONG_DELAY_MS, TimeUnit.MILLISECONDS))
      }
    }

  @Test def testLateBindingStyle(): Unit =
    forAllImplementations("testLateBindingStyle") { impl =>
      if (testImplementationDetails &&
          impl.klazz() != classOf[ju.ArrayList[_]] &&
          !impl
            .emptyCollection()
            .spliterator()
            .hasCharacteristics(ju.Spliterator.IMMUTABLE)) {
        val one = impl.makeElement(1)
        val c1 = impl.emptyCollection()
        val split1 = c1.spliterator()
        c1.add(one)
        assertTrue(split1.tryAdvance((e: AnyRef) => assertSame(e, one)))
        assertFalse(
          split1.tryAdvance((_: AnyRef) => throw new AssertionError())
        )
        assertTrue(c1.contains(one))

        val count = new AtomicLong(0L)
        val c2 = impl.emptyCollection()
        val split2 = c2.spliterator()
        c2.add(one)
        split2.forEachRemaining((e: AnyRef) => {
          assertSame(e, one)
          count.getAndIncrement()
        })
        mustEqual(1L, count.get())
        assertFalse(
          split2.tryAdvance((_: AnyRef) => throw new AssertionError())
        )
        assertTrue(c2.contains(one))
      }
    }

  @Test def testGetComparator_IllegalStateException(): Unit =
    forAllImplementations("testGetComparator_IllegalStateException") { impl =>
      val s = impl.emptyCollection().spliterator()
      val reportsSorted = s.hasCharacteristics(ju.Spliterator.SORTED)
      try {
        s.getComparator()
        assertTrue(reportsSorted)
      } catch {
        case _: IllegalStateException => assertFalse(reportsSorted)
      }
    }

  @Test def testCollectionCopies(): Unit =
    forAllImplementations("testCollectionCopies") { impl =>
      val rnd = juc.ThreadLocalRandom.current()
      val c = impl.emptyCollection()
      var n = rnd.nextInt(4)
      while ({ n -= 1; n >= 0 })
        c.add(impl.makeElement(rnd.nextInt()))
      mustEqual(c, c)
      c match {
        case _: ju.List[_] =>
          assertCollectionsEquals(c, new ju.ArrayList[AnyRef](c))
        case _: ju.Set[_] =>
          assertCollectionsEquals(c, new ju.HashSet[AnyRef](c))
        case _: ju.Deque[_] =>
          assertCollectionsEquivalent(c, new ju.ArrayDeque[AnyRef](c))
        case _ =>
      }
      cloneableClone(c) match {
        case null  =>
        case clone =>
          assertSame(c.getClass(), clone.getClass())
          assertCollectionsEquivalent(c, clone)
      }
    }

  private def assertThrowsAll[T <: Throwable](
      expected: Class[T]
  )(ops: (() => Any)*): Unit =
    ops.foreach(op => assertThrows(expected, op()))

  private def cloneableClone(c: ju.Collection[AnyRef]): ju.Collection[AnyRef] =
    c match {
      case x: ju.ArrayList[_] =>
        x.clone().asInstanceOf[ju.Collection[AnyRef]]
      case x: ju.Vector[_] =>
        x.clone().asInstanceOf[ju.Collection[AnyRef]]
      case x: juc.CopyOnWriteArrayList[_] =>
        x.clone().asInstanceOf[ju.Collection[AnyRef]]
      case _ => null
    }

  private def adderRemover(c: ju.Collection[AnyRef], e: AnyRef): Runnable =
    chooseOne[Runnable](
      () => {
        assertTrue(c.add(e))
        assertTrue(c.contains(e))
        assertTrue(c.remove(e))
        assertFalse(c.contains(e))
      },
      () => {
        assertTrue(c.add(e))
        assertTrue(c.contains(e))
        assertTrue(c.removeIf((x: AnyRef) => x == e))
        assertFalse(c.contains(e))
      },
      () => {
        assertTrue(c.add(e))
        assertTrue(c.contains(e))
        val it = c.iterator()
        var removed = false
        while (!removed)
          if (it.next() == e) {
            try it.remove()
            catch { case _: UnsupportedOperationException => c.remove(e) }
            assertFalse(c.contains(e))
            removed = true
          }
      }
    )

  private def chooseOne[T](ts: T*): T =
    ts(juc.ThreadLocalRandom.current().nextInt(ts.length))
}
