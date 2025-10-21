/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.{Arrays, Collection, NoSuchElementException, Queue}

import org.junit.Assert._
import org.junit._

object ConcurrentLinkedQueueTest {
  import JSR166Test._

  /** Returns a new queue of given size containing consecutive Items 0 ... n -
   *  \1.
   */
  private def populatedQueue(n: Int): ConcurrentLinkedQueue[Item] = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    assertTrue(q.isEmpty)
    for (i <- 0 until n) {
      mustOffer(q, i)
    }
    assertFalse(q.isEmpty)
    mustEqual(n, q.size)
    mustEqual(0, q.peek)
    return q
  }
}

class ConcurrentLinkedQueueTest extends JSR166Test {
  import JSR166Test._

  /** new queue is empty
   */
  @Test def testConstructor1(): Unit = {
    mustEqual(0, new ConcurrentLinkedQueue[Item]().size)
  }

  /** Initializing from null Collection throws NPE
   */
  @Test def testConstructor3(): Unit = {
    try {
      new ConcurrentLinkedQueue[Item](null.asInstanceOf[Collection[Item]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Initializing from Collection of null elements throws NPE
   */
  @Test def testConstructor4(): Unit = {
    try {
      new ConcurrentLinkedQueue[Item](Arrays.asList(new Array[Item](SIZE): _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Initializing from Collection with some null elements throws NPE
   */
  @Test def testConstructor5(): Unit = {
    val items: Array[Item] = new Array[Item](2)
    items(0) = zero
    try {
      new ConcurrentLinkedQueue[Item](Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Queue contains all elements of collection used to initialize
   */
  @Test def testConstructor6(): Unit = {
    val items: Array[Item] = defaultItems
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue(
      Arrays.asList(items: _*)
    )
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(items(i), q.poll)
      i += 1
    }
  }

  /** isEmpty is true before add, false after
   */
  @Test def testEmpty(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    assertTrue(q.isEmpty)
    q.add(one)
    assertFalse(q.isEmpty)
    q.add(two)
    q.remove()
    q.remove()
    assertTrue(q.isEmpty)
  }

  /** size changes when elements added and removed
   */
  @Test def testSize(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(SIZE - i, q.size)
      q.remove()

      i += 1
    }
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.size)
      mustAdd(q, i)

      i += 1
    }
  }

  /** offer(null) throws NPE
   */
  @Test def testOfferNull(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    try {
      q.offer(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** add(null) throws NPE
   */
  @Test def testAddNull(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    try {
      q.add(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Offer returns true
   */
  @Test def testOffer(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    assertTrue(q.offer(zero))
    assertTrue(q.offer(one))
  }

  /** add returns true
   */
  @Test def testAdd(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(i, q.size)
      mustAdd(q, i)

      i += 1
    }
  }

  /** addAll(null) throws NullPointerException
   */
  @Test def testAddAll1(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    try {
      q.addAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** addAll(this) throws IllegalArgumentException
   */
  @Test def testAddAllSelf(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** addAll of a collection with null elements throws NullPointerException
   */
  @Test def testAddAll2(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    try {
      q.addAll(Arrays.asList(new Array[Item](SIZE): _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testAddAll3(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    val items: Array[Item] = new Array[Item](2)
    items(0) = zero
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** Queue contains all elements, in traversal order, of successful addAll
   */
  @Test def testAddAll5(): Unit = {
    val empty: Array[Item] = new Array[Item](0)
    val items: Array[Item] = defaultItems
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(items(i), q.poll)
      i += 1
    }
  }

  /** poll succeeds unless empty
   */
  @Test def testPoll(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(i, q.poll)

      i += 1
    }
    assertNull(q.poll)
  }

  /** peek returns next element, or null if empty
   */
  @Test def testPeek(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(i, q.peek)
      mustEqual(i, q.poll)
      assertTrue(q.peek == null || !(q.peek.intValue.equals(i)))

      i += 1
    }
    assertNull(q.peek)
  }

  /** element returns next element, or throws NSEE if empty
   */
  @Test def testElement(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(i, q.element)
      mustEqual(i, q.poll)

      i += 1
    }
    try {
      q.element
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  /** remove removes next element, or throws NSEE if empty
   */
  @Test def testRemove(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      mustEqual(i, q.remove())

      i += 1
    }
    try {
      q.remove()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>

    }
  }

  /** remove(x) removes x and returns true if present
   */
  @Test def testRemoveElement(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 1
    while (i < SIZE) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      mustContain(q, i - 1)

      i += 2
    }
    i = 0
    while (i < SIZE) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      mustNotRemove(q, i + 1)
      mustNotContain(q, i + 1)

      i += 2
    }
    assertTrue(q.isEmpty)
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      mustContain(q, i)
      q.poll
      mustNotContain(q, i)

      i += 1
    }
  }

  /** clear removes all elements
   */
  @Test def testClear(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    q.clear()
    assertTrue(q.isEmpty)
    mustEqual(0, q.size)
    q.add(one)
    assertFalse(q.isEmpty)
    q.clear()
    assertTrue(q.isEmpty)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    val p: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    var i: Int = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)

      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if change
   */
  @Test def testRetainAll(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    val p: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    var i: Int = 0
    while (i < SIZE) {
      val changed: Boolean = q.retainAll(p)
      if (i == 0) {
        assertFalse(changed)
      } else {
        assertTrue(changed)
      }
      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size)
      p.remove()

      i += 1
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testRemoveAll(): Unit = {
    var i: Int = 1
    while (i < SIZE) {
      val q: ConcurrentLinkedQueue[Item] =
        ConcurrentLinkedQueueTest.populatedQueue(SIZE)
      val p: ConcurrentLinkedQueue[Item] =
        ConcurrentLinkedQueueTest.populatedQueue(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size)
      for (j <- 0 until i) {
        val x: Item = p.remove()
        assertFalse(q.contains(x))
      }

      i += 1
    }
  }

  /** toArray contains all elements in FIFO order
   */
  @Test def testToArray(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    val a: Array[AnyRef] = q.toArray
    assertSame(classOf[Array[AnyRef]], a.getClass)
    for (o <- a) {
      assertSame(o, q.poll)
    }
    assertTrue(q.isEmpty)
  }

  /** toArray(a) contains all elements in FIFO order
   */
  @Test def testToArray2(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    val items: Array[Item] = new Array[Item](SIZE)
    val array: Array[Item] = q.toArray(items)
    assertSame(items, array)
    for (o <- items) {
      assertSame(o, q.poll)
    }
    assertTrue(q.isEmpty)
  }

  /** toArray(null) throws NullPointerException
   */
  @Test def testToArray_NullArg(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    try {
      q.toArray(null.asInstanceOf[Array[AnyRef]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** toArray(incompatible array type) throws ArrayStoreException
   */
  @Ignore("No array object exact element type information in SN runtime")
  @Test def testToArray_incompatibleArrayType(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case success: ArrayStoreException =>
    }
  }

  /** iterator iterates through all elements
   */
  @Test def testIterator(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    val it: java.util.Iterator[_ <: Item] = q.iterator()
    var i: Int = 0
    i = 0
    while (it.hasNext) {
      mustContain(q, it.next)
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(new ConcurrentLinkedQueue[AnyRef]().iterator)
  }

  /** iterator ordering is FIFO
   */
  @Test def testIteratorOrdering(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    q.add(one)
    q.add(two)
    q.add(three)
    var k: Int = 0
    val it: java.util.Iterator[_ <: Item] = q.iterator
    while (it.hasNext) {
      mustEqual(
        {
          k += 1; k
        },
        it.next
      )
    }
    mustEqual(3, k)
  }

  /** Modifications do not cause iterators to fail
   */
  @Test def testWeaklyConsistentIteration(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    q.add(one)
    q.add(two)
    q.add(three)
    val it: java.util.Iterator[_ <: Item] = q.iterator
    while (it.hasNext) {
      q.remove()
      it.next
    }
    mustEqual(0, q.size)
  }

  /** iterator.remove removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q: ConcurrentLinkedQueue[Item] = new ConcurrentLinkedQueue[Item]
    q.add(itemFor(one))
    q.add(itemFor(two))
    q.add(itemFor(three))
    var it: java.util.Iterator[_ <: Item] = q.iterator
    it.next
    it.remove()
    it = q.iterator
    assertSame(it.next, itemFor(two))
    assertSame(it.next, itemFor(three))
    assertFalse(it.hasNext)
  }

  /** toString contains toStrings of elements
   */
  @Test def testToString(): Unit = {
    val q: ConcurrentLinkedQueue[Item] =
      ConcurrentLinkedQueueTest.populatedQueue(SIZE)
    val s: String = q.toString
    var i: Int = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))

      i += 1
    }
  }

  // /**
  //  * A deserialized/reserialized queue has same elements in same order
  //  * UNSUPOPORTED
  //  */
  // @throws[Exception]
  // def testSerialization(): Unit = {
  //   val x: Queue[Item] = ConcurrentLinkedQueueTest.populatedQueue(SIZE)
  //   val y: Queue[Item] = serialClone(x)
  //   assertNotSame(x, y)
  //   mustEqual(x.size, y.size)
  //   mustEqual(x.toString, y.toString)
  //   assertTrue(Arrays.equals(x.toArray, y.toArray))
  //   while (!(x.isEmpty)) {
  //     assertFalse(y.isEmpty)
  //     mustEqual(x.remove, y.remove)
  //   }
  //   assertTrue(y.isEmpty)
  // }

  /** remove(null), contains(null) always return false
   */
  @Test def testNeverContainsNull(): Unit = {
    val qs: Array[Collection[_]] = Array(
      new ConcurrentLinkedQueue[AnyRef],
      ConcurrentLinkedQueueTest.populatedQueue(2)
    )
    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
    }
  }
}
