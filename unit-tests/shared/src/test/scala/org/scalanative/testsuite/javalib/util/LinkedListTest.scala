// Ported from Scala.js commit: d0808af dated: 2021-01-06
// Additional Spliterator code implemented for Scala Native.

// The corresponding ArrayListTest.scala is in the "java.util" package
// for historical reasons and because it does not run on JVM.

package org.scalanative.testsuite.javalib.util

import java.util.{LinkedList, Spliterator}

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Test

class LinkedListTest extends AbstractListTest {

  override def factory: LinkedListFactory = new LinkedListFactory

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
