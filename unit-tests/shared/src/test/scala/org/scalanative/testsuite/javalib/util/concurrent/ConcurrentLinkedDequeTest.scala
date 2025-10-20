/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

/* Lightly modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util
import java.util.{Arrays, Collection, NoSuchElementException}
import java.util.Random
import java.util.concurrent.ConcurrentLinkedDeque

import org.junit.Test
import org.junit.Assert.*
import org.junit.Ignore

class ConcurrentLinkedDequeTest extends JSR166Test {
  import JSR166Test.*

  /** Returns a new deque of given size containing consecutive Integers 0 ... n.
   */
  private def populatedDeque(n: Int) = {
    val q = new ConcurrentLinkedDeque[Integer]
    assertTrue(q.isEmpty)
    for (i <- 0 until n) {
      assertTrue(q.offer(new Integer(i)))
    }
    assertFalse(q.isEmpty)
    assertEquals(n, q.size)
    q
  }

  private def populatedDequeOfItem(n: Int) = {
    val q = new ConcurrentLinkedDeque[Item]
    assertTrue(q.isEmpty)
    for (i <- 0 until n) {
      assertTrue(q.offer(new Item(i)))
    }
    assertFalse(q.isEmpty)
    assertEquals(n, q.size)
    q
  }

  /** new deque is empty
   */
  @Test def testConstructor1(): Unit = {
    assertTrue(new ConcurrentLinkedDeque[AnyRef]().isEmpty)
    assertEquals(0, new ConcurrentLinkedDeque[AnyRef]().size)
  }

  /** Initializing from null Collection throws NPE
   */
  @Test def testConstructor3(): Unit = {
    try {
      new ConcurrentLinkedDeque[AnyRef](null.asInstanceOf[Collection[AnyRef]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Initializing from Collection of null elements throws NPE
   */
  @Test def testConstructor4(): Unit = {
    try {
      new ConcurrentLinkedDeque[AnyRef](
        Arrays.asList(new Array[AnyRef](SIZE)*)
      )
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Initializing from Collection with some null elements throws NPE
   */
  @Test def testConstructor5(): Unit = {
    val items: Array[Item] = new Array[Item](5)
    items(0) = zero
    try {
      new ConcurrentLinkedDeque[Item](Arrays.asList(items*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Deque contains all elements of collection used to initialize
   */
  @Test def testConstructor6(): Unit = {
    val items: Array[Item] = defaultItems
    val q: ConcurrentLinkedDeque[Item] =
      new ConcurrentLinkedDeque(
        Arrays.asList(items*)
      )

    val expectedSize = SIZE // SIZE is defined in JSR166Test.scala as 20

    assertEquals(s"q.size() expect: ${expectedSize}", expectedSize, q.size())
  }

  /** isEmpty is true before add, false after
   */
  @Test def testEmpty(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    assertTrue(q.isEmpty)
    q.add(one)
    assertFalse(q.isEmpty)
    q.add(two)
    q.remove
    q.remove
    assertTrue(q.isEmpty)
  }

  /** size() changes when elements added and removed
   */
  @Test def testSize(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(SIZE - i, q.size)
      q.remove

      i += 1
    }

    i = 0
    while (i < SIZE) {
      assertEquals(i, q.size)
      q.add(new Integer(i))

      i += 1
    }
  }

  /** push(null) throws NPE
   */
  @Test def testPushNull(): Unit = {
    val q = new ConcurrentLinkedDeque[AnyRef]
    try {
      q.push(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** peekFirst() returns element inserted with push
   */
  @Test def testPush(): Unit = {
    val q = populatedDeque(3)
    q.pollLast
    q.push(four)
    assertSame(four, q.peekFirst)
  }

  /** pop() removes first element, or throws NSEE if empty
   */
  @Test def testPop(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.pop)

      i += 1
    }
    try {
      q.pop
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
  }

  /** offer(null) throws NPE
   */
  @Test def testOfferNull(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.offer(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** offerFirst(null) throws NPE
   */
  @Test def testOfferFirstNull(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.offerFirst(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** offerLast(null) throws NPE
   */
  @Test def testOfferLastNull(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.offerLast(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** offer(x) succeeds
   */
  @Test def testOffer(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    assertTrue(q.offer(zero))
    assertTrue(q.offer(one))
    assertSame(zero, q.peekFirst().intValue)
    assertSame(one, q.peekLast().intValue)
  }

  /** offerFirst(x) succeeds
   */
  @Test def testOfferFirst(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    assertTrue(q.offerFirst(zero))
    assertTrue(q.offerFirst(one))
    assertSame(one, q.peekFirst().intValue)
    assertSame(zero, q.peekLast().intValue)
  }

  /** offerLast(x) succeeds
   */
  @Test def testOfferLast(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    assertTrue(q.offerLast(zero))
    assertTrue(q.offerLast(one))
    assertSame(zero, q.peekFirst().intValue)
    assertSame(one, q.peekLast().intValue)
  }

  /** add(null) throws NPE
   */
  @Test def testAddNull(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.add(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addFirst(null) throws NPE
   */
  @Test def testAddFirstNull(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.addFirst(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addLast(null) throws NPE
   */
  @Test def testAddLastNull(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.addLast(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** add(x) succeeds
   */
  @Test def testAdd(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    assertTrue(q.add(zero))
    assertTrue(q.add(one))
    assertSame(zero, q.peekFirst().intValue)
    assertSame(one, q.peekLast().intValue)
  }

  /** addFirst(x) succeeds
   */
  @Test def testAddFirst(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    q.addFirst(zero)
    q.addFirst(one)
    assertSame(one, q.peekFirst().intValue)
    assertSame(zero, q.peekLast().intValue)
  }

  /** addLast(x) succeeds
   */
  @Test def testAddLast(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    q.addLast(zero)
    q.addLast(one)
    assertSame(zero, q.peekFirst().intValue)
    assertSame(one, q.peekLast().intValue)
  }

  /** addAll(null) throws NPE
   */
  @Test def testAddAll1(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.addAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addAll(this) throws IAE
   */
  @Test def testAddAllSelf(): Unit = {
    val q = populatedDeque(SIZE)
    try {
      q.addAll(q)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>
    }
  }

  /** addAll of a collection with null elements throws NPE
   */
  @Test def testAddAll2(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    try {
      q.addAll(Arrays.asList(new Array[Item](SIZE)*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testAddAll3(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    val items = new Array[Item](SIZE)
    items(0) = zero
    try {
      q.addAll(Arrays.asList(items*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Deque contains all elements, in traversal order, of successful addAll
   */
  @Test def testAddAll5(): Unit = {
    val empty: Array[Item] = new Array[Item](0)
    val items: Array[Item] = defaultItems

    val q = new ConcurrentLinkedDeque[Item]
    assertFalse(q.addAll(Arrays.asList(empty*)))
    assertTrue(q.addAll(Arrays.asList(items*)))

    var i = 0
    while (i < SIZE) {
      assertEquals(items(i), q.poll)
      i += 1
    }
  }

  /** pollFirst() succeeds unless empty
   */
  @Test def testPollFirst(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.pollFirst)
      i += 1
    }
    assertNull(q.pollFirst())
  }

  /** pollLast() succeeds unless empty
   */
  @Test def testPollLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) {
      assertEquals(i, q.pollLast().intValue)
    }
    assertNull(q.pollLast())
  }

  /** poll() succeeds unless empty
   */
  @Test def testPoll(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.poll().intValue)
      i += 1
    }
    assertNull(q.poll)
  }

  /** peek() returns next element, or null if empty
   */
  @Test def testPeek(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.peek)
      assertEquals(i, q.poll)
      assertTrue(q.peek == null || !(q.peek == i))

      i += 1
    }
    assertNull(q.peek)
  }

  /** element() returns first element, or throws NSEE if empty
   */
  @Test def testElement(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.element)
      assertEquals(i, q.poll)

      i += 1
    }
    try {
      q.element
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
  }

  /** remove() removes next element, or throws NSEE if empty
   */
  @Test def testRemove(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.remove)

      i += 1
    }
    try {
      q.remove
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
  }

  /** remove(x) removes x and returns true if present
   */
  @Test def testRemoveElement(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 1
    while (i < SIZE) {
      assertTrue(q.contains(i))
      assertTrue(q.remove(i))
      assertFalse(q.contains(i))
      assertTrue(q.contains(i - 1))

      i += 2
    }

    i = 0
    while (i < SIZE) {
      assertTrue(q.contains(i))
      assertTrue(q.remove(i))
      assertFalse(q.contains(i))
      assertFalse(q.remove(i + 1))
      assertFalse(q.contains(i + 1))

      i += 2
    }
    assertTrue(q.isEmpty)
  }

  /** peekFirst() returns next element, or null if empty
   */
  @Test def testPeekFirst(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.peekFirst)
      assertEquals(i, q.pollFirst)
      assertTrue(q.peekFirst == null || !(q.peekFirst == i))

      i += 1
    }
    assertNull(q.peekFirst)
  }

  /** peekLast() returns next element, or null if empty
   */
  @Test def testPeekLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) {
      assertEquals(i, q.peekLast)
      assertEquals(i, q.pollLast)
      assertTrue(q.peekLast == null || !(q.peekLast == i))
    }
    assertNull(q.peekLast)
  }

  /** getFirst() returns first element, or throws NSEE if empty
   */
  @Test def testFirstElement(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.getFirst)
      assertEquals(i, q.pollFirst)

      i += 1
    }
    try {
      q.getFirst
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
  }

  /** getLast() returns last element, or throws NSEE if empty
   */
  @Test def testLastElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) {
      assertEquals(i, q.getLast)
      assertEquals(i, q.pollLast)
    }
    try {
      q.getLast
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
    assertNull(q.peekLast)
  }

  /** removeFirst() removes first element, or throws NSEE if empty
   */
  @Test def testRemoveFirst(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertEquals(i, q.removeFirst)

      i += 1
    }
    try {
      q.removeFirst
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
    assertNull(q.peekFirst)
  }

  /** removeLast() removes last element, or throws NSEE if empty
   */
  @Test def testRemoveLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- SIZE - 1 to 0 by -1) {
      assertEquals(i, q.removeLast)
    }
    try {
      q.removeLast
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
    assertNull(q.peekLast)
  }

  /** removeFirstOccurrence(x) removes x and returns true if present
   */
  @Test def testRemoveFirstOccurrence(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 1
    while (i < SIZE) {
      assertTrue(q.removeFirstOccurrence(new Integer(i)))

      i += 2
    }

    i = 0
    while (i < SIZE) {
      assertTrue(q.removeFirstOccurrence(new Integer(i)))
      assertFalse(q.removeFirstOccurrence(new Integer(i + 1)))

      i += 2
    }
    assertTrue(q.isEmpty)
  }

  /** removeLastOccurrence(x) removes x and returns true if present
   */
  @Test def testRemoveLastOccurrence(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 1
    while (i < SIZE) {
      assertTrue(q.removeLastOccurrence(new Integer(i)))

      i += 2
    }

    i = 0
    while (i < SIZE) {
      assertTrue(q.removeLastOccurrence(new Integer(i)))
      assertFalse(q.removeLastOccurrence(new Integer(i + 1)))

      i += 2
    }
    assertTrue(q.isEmpty)
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      assertTrue(q.contains(new Integer(i)))
      q.poll
      assertFalse(q.contains(new Integer(i)))

      i += 1
    }
  }

  /** clear() removes all elements
   */
  @Test def testClear(): Unit = {
    val q = populatedDeque(SIZE)
    q.clear()
    assertTrue(q.isEmpty)
    assertEquals(0, q.size)
    q.add(one)
    assertFalse(q.isEmpty)
    q.clear()
    assertTrue(q.isEmpty)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val q = populatedDequeOfItem(SIZE)
    val p = new ConcurrentLinkedDeque[Item]
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      p.add(new Integer(i))

      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if change
   */
  @Test def testRetainAll(): Unit = {
    val q = populatedDeque(SIZE)
    val p = populatedDeque(SIZE)
    var i = 0
    while (i < SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)
      assertTrue(q.containsAll(p))
      assertEquals(SIZE - i, q.size)
      p.remove

      i += 1
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testRemoveAll(): Unit = {
    var i = 1
    while (i < SIZE) {
      val q = populatedDeque(SIZE)
      val p = populatedDeque(i)
      assertTrue(q.removeAll(p))
      assertEquals(SIZE - i, q.size)
      for (j <- 0 until i) {
        val x = p.remove.asInstanceOf[Integer]
        assertFalse(q.contains(x))
      }

      i += 1
    }
  }

  /** toArray() contains all elements in FIFO order
   */
  @Test def testToArray(): Unit = {
    val q = populatedDeque(SIZE)
    val o = q.toArray
    for (i <- 0 until o.length) {
      assertSame(o(i), q.poll)
    }
  }

  /** toArray(a) contains all elements in FIFO order
   */
  @Test def testToArray2(): Unit = {
    val q = populatedDeque(SIZE)
    val ints = new Array[Integer](SIZE)
    val array = q.toArray(ints)
    assertSame(ints, array)
    for (i <- 0 until ints.length) {
      assertSame(ints(i), q.poll)
    }
  }

  /** toArray(null) throws NullPointerException
   */
  @Test def testToArray_NullArg(): Unit = {
    val q = populatedDeque(SIZE)
    try {
      q.toArray(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** toArray(incompatible array type) throws ArrayStoreException
   */
  // Lack of ArrayStoreExeption in Scala Native is a known defect/insufficiency
  @Ignore("No distinguishment in Array component types in Scala Native")
  @Test def testToArray1_BadArg(): Unit = {
    val q = populatedDeque(SIZE)
    try {
      q.toArray(new Array[String](10))
      shouldThrow()
    } catch {
      case success: ArrayStoreException =>
    }
  }

  /** Iterator iterates through all elements
   */
  @Test def testIterator(): Unit = {
    val q = populatedDeque(SIZE)
    val it = q.iterator
    var i = 0
    i = 0
    while (it.hasNext) {
      assertTrue(q.contains(it.next))
      i += 1
    }
    assertEquals(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    val c = new ConcurrentLinkedDeque[Item]
    assertIteratorExhausted(c.iterator)
    assertIteratorExhausted(c.descendingIterator)
  }

  /** Iterator ordering is FIFO
   */
  @Test def testIteratorOrdering(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    q.add(one)
    q.add(two)
    q.add(three)
    var k = 0
    val it = q.iterator
    while (it.hasNext)
      assertEquals(
        {
          k += 1; k
        },
        it.next.intValue
      )
    assertEquals(3, k)
  }

  /** Modifications do not cause iterators to fail
   */
  @Test def testWeaklyConsistentIteration(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    q.add(one)
    q.add(two)
    q.add(three)
    val it = q.iterator
    while (it.hasNext) {
      q.remove
      it.next
    }
    assertEquals("deque should be empty again", 0, q.size)
  }

  /** iterator.remove() removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    val rng = new Random
    for (iters <- 0 until 100) {
      val max = rng.nextInt(5) + 2
      val split = rng.nextInt(max - 1) + 1
      for (j <- 1 to max) {
        q.add(new Integer(j))
      }
      var it = q.iterator
      for (j <- 1 to split) {
        assertEquals(it.next, new Item(j))
      }
      it.remove()
      assertEquals(it.next, new Item(split + 1))
      for (j <- 1 to split) {
        q.remove(new Item(j))
      }
      it = q.iterator
      for (j <- split + 1 to max) {
        assertEquals(it.next, new Item(j))
        it.remove()
      }
      assertFalse(it.hasNext)
      assertTrue(q.isEmpty)
    }
  }

  /** Descending iterator iterates through all elements
   */
  @Test def testDescendingIterator(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    val it = q.descendingIterator
    while (it.hasNext) {
      assertTrue(q.contains(it.next))
      i += 1
    }
    assertEquals(i, SIZE)
    assertFalse(it.hasNext)
    try {
      it.next
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }
  }

  /** Descending iterator ordering is reverse FIFO
   */
  @Test def testDescendingIteratorOrdering(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    for (iters <- 0 until 100) {
      q.add(new Item(3))
      q.add(new Item(2))
      q.add(new Item(1))
      var k = 0
      val it = q.descendingIterator
      while (it.hasNext)
        assertEquals(
          {
            k += 1; k
          },
          it.next.intValue
        )
      assertEquals(3, k)
      q.remove
      q.remove
      q.remove
    }
  }

  /** descendingIterator.remove() removes current element
   */
  @Test def testDescendingIteratorRemove(): Unit = {
    val q = new ConcurrentLinkedDeque[Item]
    val rng = new Random
    for (iters <- 0 until 100) {
      val max = rng.nextInt(5) + 2
      val split = rng.nextInt(max - 1) + 1
      for (j <- max to 1 by -1) {
        q.add(new Item(j))
      }
      var it = q.descendingIterator
      for (j <- 1 to split) {
        assertEquals(it.next, new Item(j))
      }
      it.remove()
      assertEquals(it.next, new Item(split + 1))
      for (j <- 1 to split) {
        q.remove(new Item(j))
      }
      it = q.descendingIterator
      for (j <- split + 1 to max) {
        assertEquals(it.next, new Item(j))
        it.remove()
      }
      assertFalse(it.hasNext)
      assertTrue(q.isEmpty)
    }
  }

  /** toString() contains toStrings of elements
   */
  @Test def testToString(): Unit = {
    val q = populatedDeque(SIZE)
    val s = q.toString
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))

      i += 1
    }
  }

  // /**
  //  * A deserialized serialized deque has same elements in same order
  //  * UNSUPPORTED
  //  */
  // @throws[Exception]
  // @Test def testSerialization(): Unit = {
  //   val x = populatedDeque(SIZE)
  //   val y = serialClone(x)
  //   assertNotSame(x, y)
  //   assertEquals(x.size, y.size)
  //   assertEquals(x.toString, y.toString)
  //   assertTrue(util.Arrays.equals(x.toArray, y.toArray))
  //   while (!x.isEmpty) {
  //     assertFalse(y.isEmpty)
  //     assertEquals(x.remove, y.remove)
  //   }
  //   assertTrue(y.isEmpty)
  // }

  /** contains(null) always return false. remove(null) always throws
   *  NullPointerException.
   */
  @Test def testNeverContainsNull(): Unit = {
    val qs = Array(new ConcurrentLinkedDeque[AnyRef], populatedDeque(2))
    for (q <- qs) {
      assertFalse(q.contains(null))
      try {
        assertFalse(q.remove(null))
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
      try {
        assertFalse(q.removeFirstOccurrence(null))
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
      try {
        assertFalse(q.removeLastOccurrence(null))
        shouldThrow()
      } catch {
        case success: NullPointerException =>
      }
    }
  }

}
