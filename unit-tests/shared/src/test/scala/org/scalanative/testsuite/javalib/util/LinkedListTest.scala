// Ported from Scala.js commit: d0808af dated: 2021-01-06
// Additional Spliterator code implemented for Scala Native.

// The corresponding ArrayListTest.scala is in the "java.util" package
// for historical reasons and because it does not run on JVM.

package org.scalanative.testsuite.javalib.util

import java.util.{
  Arrays,
  Collection,
  Iterator,
  LinkedList,
  NoSuchElementException,
  Spliterator
}

import scala.reflect.ClassTag

import org.junit.Assert.{assertThrows => _, _}
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class LinkedListTest extends AbstractListTest {
  import JSR166Test._

  override def factory: LinkedListFactory = new LinkedListFactory

  private val zeroItem = itemFor(0)
  private val oneItem = itemFor(1)
  private val twoItem = itemFor(2)
  private val threeItem = itemFor(3)
  private val fourItem = itemFor(4)
  private val fiveItem = itemFor(5)

  private def assertIteratorExhausted(it: Iterator[_]): Unit = {
    assertFalse(it.hasNext())
    assertThrows(classOf[NoSuchElementException], it.next())
  }

  private def populatedQueue(n: Int): LinkedList[Item] = {
    val q = new LinkedList[Item]()
    assertTrue(q.isEmpty())
    var i = 0
    while (i < n) {
      mustOffer(q, i)
      i += 1
    }
    assertFalse(q.isEmpty())
    mustEqual(n, q.size())
    mustEqual(0, q.peekFirst())
    mustEqual(n - 1, q.peekLast())
    q
  }

  @Test def testConstructor1(): Unit =
    mustEqual(0, new LinkedList[Item]().size())

  @Test def testConstructor3(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new LinkedList[Item](null.asInstanceOf[Collection[Item]])
    )

  @Test def testConstructor6(): Unit = {
    val items = defaultItems
    val q = new LinkedList[Item](Arrays.asList(items: _*))
    var i = 0
    while (i < SIZE) {
      mustEqual(items(i), q.poll())
      i += 1
    }
  }

  @Test def testEmpty(): Unit = {
    val q = new LinkedList[Item]()
    assertTrue(q.isEmpty())
    q.add(oneItem)
    assertFalse(q.isEmpty())
    q.add(twoItem)
    q.remove()
    q.remove()
    assertTrue(q.isEmpty())
  }

  @Test def testSize(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(SIZE - i, q.size())
      q.remove()
      i += 1
    }
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.size())
      mustAdd(q, i)
      i += 1
    }
  }

  @Test def testOfferNull(): Unit = {
    val q = new LinkedList[Item]()
    q.offer(null)
    assertNull(q.get(0))
    assertTrue(q.contains(null))
  }

  @Test def testOffer(): Unit = {
    val q = new LinkedList[Item]()
    mustOffer(q, zeroItem)
    mustOffer(q, oneItem)
  }

  @Test def testAdd(): Unit = {
    val q = new LinkedList[Item]()
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.size())
      mustAdd(q, i)
      i += 1
    }
  }

  @Test def testAddAll1(): Unit = {
    val q = new LinkedList[Item]()
    assertThrows(classOf[NullPointerException], q.addAll(null))
  }

  @Test def testAddAll5(): Unit = {
    val empty = new Array[Item](0)
    val items = defaultItems
    val q = new LinkedList[Item]()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    var i = 0
    while (i < SIZE) {
      mustEqual(items(i), q.poll())
      i += 1
    }
  }

  @Test def testAddAll2_IndexOutOfBoundsException(): Unit = {
    val l = new LinkedList[Item]()
    l.add(zeroItem)
    val m = new LinkedList[Item]()
    m.add(oneItem)
    assertThrows(classOf[IndexOutOfBoundsException], l.addAll(4, m))
  }

  @Test def testAddAll4_BadIndex(): Unit = {
    val l = new LinkedList[Item]()
    l.add(zeroItem)
    val m = new LinkedList[Item]()
    m.add(oneItem)
    assertThrows(classOf[IndexOutOfBoundsException], l.addAll(-1, m))
  }

  @Test def testPoll(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.poll())
      i += 1
    }
    assertNull(q.poll())
  }

  @Test def testPeek(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.peek())
      mustEqual(i, q.poll())
      assertTrue(q.peek() == null || !q.peek().equals(itemFor(i)))
      i += 1
    }
    assertNull(q.peek())
  }

  @Test def testElement(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.element())
      mustEqual(i, q.poll())
      i += 1
    }
    assertThrows(classOf[NoSuchElementException], q.element())
  }

  @Test def testRemove(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.remove())
      i += 1
    }
    assertThrows(classOf[NoSuchElementException], q.remove())
  }

  @Test def testRemoveElement(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 1
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
    assertTrue(q.isEmpty())
  }

  @Test def testContains(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustContain(q, i)
      q.poll()
      mustNotContain(q, i)
      i += 1
    }
  }

  @Test def testClear(): Unit = {
    val q = populatedQueue(SIZE)
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    mustAdd(q, oneItem)
    assertFalse(q.isEmpty())
    q.clear()
    assertTrue(q.isEmpty())
  }

  @Test def testContainsAll(): Unit = {
    val q = populatedQueue(SIZE)
    val p = new LinkedList[Item]()
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  @Test def testRetainAll(): Unit = {
    val q = populatedQueue(SIZE)
    val p = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)
      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size())
      p.remove()
      i += 1
    }
  }

  @Test def testRemoveAll(): Unit = {
    var i = 1
    while (i < SIZE) {
      val q = populatedQueue(SIZE)
      val p = populatedQueue(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size())
      var j = 0
      while (j < i) {
        mustNotContain(q, p.remove())
        j += 1
      }
      i += 1
    }
  }

  @Test def testToArray(): Unit = {
    val q = populatedQueue(SIZE)
    val a = q.toArray()
    assertSame(classOf[Array[Object]], a.getClass())
    for (o <- a)
      assertSame(o, q.poll())
    assertTrue(q.isEmpty())
  }

  @Test def testToArray2(): Unit = {
    val q = populatedQueue(SIZE)
    val items = new Array[Item](SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    for (o <- items)
      assertSame(o, q.poll())
    assertTrue(q.isEmpty())
  }

  @Test def testToArray_NullArg(): Unit = {
    val l = new LinkedList[Item]()
    l.add(zeroItem)
    assertThrows(
      classOf[NullPointerException],
      l.toArray(null.asInstanceOf[Array[Item]])
    )
  }

  @Ignore("scala-native#4845: reference arrays need runtime component types")
  @Test def testToArray_incompatibleArrayType(): Unit = ()

  @Test def testIterator(): Unit = {
    val q = populatedQueue(SIZE)
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  @Test def testEmptyIterator(): Unit =
    assertIteratorExhausted(new LinkedList[Item]().iterator())

  @Test def testIteratorOrdering(): Unit = {
    val q = new LinkedList[Item]()
    q.add(oneItem)
    q.add(twoItem)
    q.add(threeItem)
    var k = 0
    val it = q.iterator()
    while (it.hasNext()) {
      k += 1
      mustEqual(k, it.next())
    }
    mustEqual(3, k)
  }

  @Test def testIteratorRemove(): Unit = {
    val q = new LinkedList[Item]()
    q.add(oneItem)
    q.add(twoItem)
    q.add(threeItem)
    var it = q.iterator()
    mustEqual(1, it.next())
    it.remove()
    it = q.iterator()
    mustEqual(2, it.next())
    mustEqual(3, it.next())
    assertFalse(it.hasNext())
  }

  @Test def testDescendingIterator(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    val it = q.descendingIterator()
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  @Test def testDescendingIteratorOrdering(): Unit = {
    val q = new LinkedList[Item]()
    q.add(threeItem)
    q.add(twoItem)
    q.add(oneItem)
    var k = 0
    val it = q.descendingIterator()
    while (it.hasNext()) {
      k += 1
      mustEqual(k, it.next())
    }
    mustEqual(3, k)
  }

  @Test def testDescendingIteratorRemove(): Unit = {
    val q = new LinkedList[Item]()
    q.add(threeItem)
    q.add(twoItem)
    q.add(oneItem)
    var it = q.descendingIterator()
    it.next()
    it.remove()
    it = q.descendingIterator()
    assertSame(twoItem, it.next())
    assertSame(threeItem, it.next())
    assertFalse(it.hasNext())
  }

  @Test def testToString(): Unit = {
    val q = populatedQueue(SIZE)
    val s = q.toString()
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  @Test def testAddFirst(): Unit = {
    val q = populatedQueue(3)
    q.addFirst(fourItem)
    assertSame(fourItem, q.peek())
  }

  @Test def testPush(): Unit = {
    val q = populatedQueue(3)
    q.push(fourItem)
    assertSame(fourItem, q.peekFirst())
  }

  @Test def testPop(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pop())
      i += 1
    }
    assertThrows(classOf[NoSuchElementException], q.pop())
  }

  @Test def testOfferFirst(): Unit = {
    val q = new LinkedList[Item]()
    assertTrue(q.offerFirst(zeroItem))
    assertTrue(q.offerFirst(oneItem))
  }

  @Test def testOfferLast(): Unit = {
    val q = new LinkedList[Item]()
    assertTrue(q.offerLast(zeroItem))
    assertTrue(q.offerLast(oneItem))
  }

  @Test def testPollLast(): Unit = {
    val q = populatedQueue(SIZE)
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(i, q.pollLast())
      i -= 1
    }
    assertNull(q.pollLast())
  }

  @Test def testPeekFirst(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.peekFirst())
      mustEqual(i, q.pollFirst())
      assertTrue(q.peekFirst() == null || !q.peekFirst().equals(itemFor(i)))
      i += 1
    }
    assertNull(q.peekFirst())
  }

  @Test def testPeekLast(): Unit = {
    val q = populatedQueue(SIZE)
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(i, q.peekLast())
      mustEqual(i, q.pollLast())
      assertTrue(q.peekLast() == null || !q.peekLast().equals(itemFor(i)))
      i -= 1
    }
    assertNull(q.peekLast())
  }

  @Test def testFirstElement(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.getFirst())
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertThrows(classOf[NoSuchElementException], q.getFirst())
  }

  @Test def testLastElement(): Unit = {
    val q = populatedQueue(SIZE)
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(i, q.getLast())
      mustEqual(i, q.pollLast())
      i -= 1
    }
    assertThrows(classOf[NoSuchElementException], q.getLast())
    assertNull(q.peekLast())
  }

  @Test def testRemoveFirstOccurrence(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 1
    while (i < SIZE) {
      assertTrue(q.removeFirstOccurrence(itemFor(i)))
      i += 2
    }
    i = 0
    while (i < SIZE) {
      assertTrue(q.removeFirstOccurrence(itemFor(i)))
      assertFalse(q.removeFirstOccurrence(itemFor(i + 1)))
      i += 2
    }
    assertTrue(q.isEmpty())
  }

  @Test def testRemoveLastOccurrence(): Unit = {
    val q = populatedQueue(SIZE)
    var i = 1
    while (i < SIZE) {
      assertTrue(q.removeLastOccurrence(itemFor(i)))
      i += 2
    }
    i = 0
    while (i < SIZE) {
      assertTrue(q.removeLastOccurrence(itemFor(i)))
      assertFalse(q.removeLastOccurrence(itemFor(i + 1)))
      i += 2
    }
    assertTrue(q.isEmpty())
  }

  @Test def addRemovePeekFirstAndLast(): Unit = {
    val ll = new LinkedList[Int]()

    ll.addLast(1)
    ll.removeFirst()
    ll.addLast(2)
    assertEquals(2, ll.peekFirst())

    ll.clear()

    ll.addFirst(1)
    ll.removeLast()
    ll.addFirst(2)
    assertEquals(2, ll.peekLast())
  }

  @Test def ctorCollectionInt(): Unit = {
    val l = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val ll = new LinkedList[Int](l)

    assertEquals(5, ll.size())

    for (i <- 0 until l.size())
      assertEquals(l(i), ll.poll())

    assertTrue(ll.isEmpty)
  }

  @Test def addAllAndAdd(): Unit = {
    val l = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val ll = new LinkedList[Int]()

    assertEquals(0, ll.size())
    ll.addAll(l)
    assertEquals(5, ll.size())
    ll.add(6)
    assertEquals(6, ll.size())
  }

  @Test def poll(): Unit = {
    val l = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val ll = new LinkedList[Int](l)

    assertEquals(5, ll.size())

    for (i <- 0 until l.size())
      assertEquals(l(i), ll.poll())

    assertTrue(ll.isEmpty)
  }

  @Test def pollLast(): Unit = {
    val llInt = new LinkedList[Int]()

    assertTrue(llInt.add(1000))
    assertTrue(llInt.add(10))
    assertEquals(10, llInt.pollLast())

    val llString = new LinkedList[String]()

    assertTrue(llString.add("pluto"))
    assertTrue(llString.add("pippo"))
    assertEquals("pippo", llString.pollLast())

    val llDouble = new LinkedList[Double]()

    assertTrue(llDouble.add(+10000.987))
    assertTrue(llDouble.add(-0.987))
    assertEquals(-0.987, llDouble.pollLast(), 0.0)
  }

  @Test def pushAndPop(): Unit = {
    val llInt = new LinkedList[Int]()

    llInt.push(1000)
    llInt.push(10)
    assertEquals(10, llInt.pop())
    assertEquals(1000, llInt.pop())
    assertTrue(llInt.isEmpty())

    val llString = new LinkedList[String]()

    llString.push("pluto")
    llString.push("pippo")
    assertEquals("pippo", llString.pop())
    assertEquals("pluto", llString.pop())
    assertTrue(llString.isEmpty())

    val llDouble = new LinkedList[Double]()

    llDouble.push(+10000.987)
    llDouble.push(-0.987)
    assertEquals(-0.987, llDouble.pop(), 0.0)
    assertEquals(+10000.987, llDouble.pop(), 0.0)
    assertTrue(llString.isEmpty())
  }

  @Test def peekPollFirstAndLast(): Unit = {
    val pq = new LinkedList[String]()

    assertTrue(pq.add("one"))
    assertTrue(pq.add("two"))
    assertTrue(pq.add("three"))

    assertTrue(pq.peek.equals("one"))
    assertTrue(pq.poll.equals("one"))

    assertTrue(pq.peekFirst.equals("two"))
    assertTrue(pq.pollFirst.equals("two"))

    assertTrue(pq.peekLast.equals("three"))
    assertTrue(pq.pollLast.equals("three"))

    assertNull(pq.peekFirst)
    assertNull(pq.pollFirst)

    assertNull(pq.peekLast)
    assertNull(pq.pollLast)
  }

  @Test def removeFirstOccurrence(): Unit = {
    val l = TrivialImmutableCollection("one", "two", "three", "two", "one")
    val ll = new LinkedList[String](l)

    assertTrue(ll.removeFirstOccurrence("one"))
    assertEquals(3, ll.indexOf("one"))
    assertTrue(ll.removeLastOccurrence("two"))
    assertEquals(0, ll.lastIndexOf("two"))
    assertTrue(ll.removeFirstOccurrence("one"))
    assertTrue(ll.removeLastOccurrence("two"))
    assertTrue(ll.removeFirstOccurrence("three"))
    assertFalse(ll.removeLastOccurrence("three"))
    assertTrue(ll.isEmpty)
  }

  @Test def iteratorAndDescendingIterator(): Unit = {
    val l = TrivialImmutableCollection("one", "two", "three")
    val ll = new LinkedList[String](l)

    val iter = ll.iterator()
    for (i <- 0 until l.size()) {
      assertTrue(iter.hasNext())
      assertEquals(l(i), iter.next())
    }
    assertFalse(iter.hasNext())

    val diter = ll.descendingIterator()
    for (i <- (0 until l.size()).reverse) {
      assertTrue(diter.hasNext())
      assertEquals(l(i), diter.next())
    }
    assertFalse(diter.hasNext())
  }

  // Issue #3351
  @Test def spliteratorHasExpectedCharacteristics(): Unit = {

    val coll =
      factory.fromElements[String]("Aegle", "Arethusa", "Hesperethusa")

    val expectedSize = 3
    assertEquals(expectedSize, coll.size())

    val cs = coll.spliterator().characteristics()

    // SIZED | SUBSIZED | ORDERED
    val expectedCharacteristics = 0x4050
    val csc = coll.spliterator().characteristics()

    val msg =
      s"expected 0x${expectedCharacteristics.toHexString.toUpperCase}" +
        s" but was: 0x${csc.toHexString.toUpperCase}"

    assertTrue(msg, expectedCharacteristics == csc)
  }

  @Test def spliteratorShouldAdvanceOverContent(): Unit = {
    val expectedElements = Array(
      "Bertha von Suttner",
      "Jane Addams",
      "Emily Greene Balch",
      "Betty Williams",
      "Mairead Corrigan",
      "Alva Myrdal"
    )
    val expectedSize = expectedElements.size

    val coll =
      factory.fromElements[String](expectedElements: _*)

    assertEquals(expectedSize, coll.size())

    // Let compiler check type returned is expected.
    val spliter: Spliterator[String] = coll.spliterator()

    assertEquals("estimateSize", expectedSize, spliter.estimateSize())
    assertEquals(
      "getExactSizeIfKnown",
      expectedSize,
      spliter.getExactSizeIfKnown()
    )
    // Check that both count & each element seen are as expected.

    var count = 0

    spliter.forEachRemaining((e: String) => {
      assertEquals(
        s"forEachRemaining contents(${count})",
        expectedElements(count),
        e
      )
      count += 1
    })
  }

}

class LinkedListFactory extends AbstractListFactory {
  override def implementationName: String =
    "java.util.LinkedList"

  override def empty[E: ClassTag]: LinkedList[E] =
    new LinkedList[E]()
}
