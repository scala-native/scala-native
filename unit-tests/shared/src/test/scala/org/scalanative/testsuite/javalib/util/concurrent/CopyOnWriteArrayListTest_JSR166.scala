/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{CopyOnWriteArrayList, ThreadLocalRandom}
import java.util.{
  ArrayList, Arrays, Collection, Collections, Iterator, List, ListIterator,
  NoSuchElementException
}

import org.junit.Assert._
import org.junit._

object CopyOnWriteArrayListTest_JSR166 extends ConcurrentSkipListItems {
  import JSR166Test.{mustAdd, mustEqual}

  val eightysix: Item = JSR166Test.itemFor(86)

  private def populatedList(n: Int): CopyOnWriteArrayList[Item] = {
    val list = new CopyOnWriteArrayList[Item]()
    assertTrue(list.isEmpty())
    var i = 0
    while (i < n) {
      mustAdd(list, i)
      i += 1
    }
    mustEqual(n <= 0, list.isEmpty())
    mustEqual(n, list.size())
    list
  }

  private def populatedList(
      elements: Array[Item]
  ): CopyOnWriteArrayList[Item] = {
    val list = new CopyOnWriteArrayList[Item]()
    assertTrue(list.isEmpty())
    var i = 0
    while (i < elements.length) {
      list.add(elements(i))
      i += 1
    }
    assertFalse(list.isEmpty())
    mustEqual(elements.length, list.size())
    list
  }
}

class CopyOnWriteArrayListTest_JSR166 extends JSR166Test {
  import CopyOnWriteArrayListTest_JSR166._
  import JSR166Test.{
    SIZE, defaultItems, fortytwo, itemFor, mustAdd, mustContain, mustEqual,
    mustNotContain, mustNotRemove, mustRemove, ninetynine, seqItems
  }

  /** Same as JSR166TestCase.assertThrows(Class, Action...). */
  def assertThrows(
      expectedExceptionClass: Class[_ <: Throwable],
      throwingActions: Action*
  ): Unit =
    assertEachThrows(expectedExceptionClass, throwingActions: _*)

  /** a new list is empty
   */
  @Test def testConstructor(): Unit = {
    val list: List[Item] = new CopyOnWriteArrayList[Item]()
    assertTrue(list.isEmpty())
  }

  /** new list contains all elements of initializing array
   */
  @Test def testConstructor2(): Unit = {
    val elts = defaultItems
    val list: List[Item] = new CopyOnWriteArrayList[Item](elts)
    var i = 0
    while (i < SIZE) {
      mustEqual(elts(i), list.get(i))
      i += 1
    }
  }

  /** new list contains all elements of initializing collection
   */
  @Test def testConstructor3(): Unit = {
    val elts = defaultItems
    val list: List[Item] =
      new CopyOnWriteArrayList[Item](Arrays.asList(elts: _*))
    var i = 0
    while (i < SIZE) {
      mustEqual(elts(i), list.get(i))
      i += 1
    }
  }

  /** addAll adds each element from the given collection, including duplicates
   */
  @Test def testAddAll(): Unit = {
    val list: List[Item] = populatedList(3)
    assertTrue(list.addAll(Arrays.asList(three, four, five)))
    mustEqual(6, list.size())
    assertTrue(list.addAll(Arrays.asList(three, four, five)))
    mustEqual(9, list.size())
  }

  /** addAllAbsent adds each element from the given collection that did not
   *  already exist in the List
   */
  @Test def testAddAllAbsent(): Unit = {
    val list = populatedList(3)
    // "one" is duplicate and will not be added
    mustEqual(2, list.addAllAbsent(Arrays.asList(three, four, one)))
    mustEqual(5, list.size())
    mustEqual(0, list.addAllAbsent(Arrays.asList(three, four, one)))
    mustEqual(5, list.size())
  }

  /** addIfAbsent will not add the element if it already exists in the list
   */
  @Test def testAddIfAbsent(): Unit = {
    val list = populatedList(SIZE)
    list.addIfAbsent(one)
    mustEqual(SIZE, list.size())
  }

  /** addIfAbsent adds the element when it does not exist in the list
   */
  @Test def testAddIfAbsent2(): Unit = {
    val list = populatedList(SIZE)
    list.addIfAbsent(three)
    mustContain(list, three)
  }

  /** clear removes all elements from the list
   */
  @Test def testClear(): Unit = {
    val list: List[Item] = populatedList(SIZE)
    list.clear()
    mustEqual(0, list.size())
  }

  /** Cloned list is equal
   */
  @Test def testClone(): Unit = {
    val l1 = populatedList(SIZE)
    val l2 = l1.clone().asInstanceOf[CopyOnWriteArrayList[Item]]
    mustEqual(l1, l2)
    l1.clear()
    assertFalse(l1.equals(l2))
  }

  /** contains is true for added elements
   */
  @Test def testContains(): Unit = {
    val list: List[Item] = populatedList(3)
    mustContain(list, one)
    mustNotContain(list, five)
  }

  /** adding at an index places it in the indicated index
   */
  @Test def testAddIndex(): Unit = {
    val list: List[Item] = populatedList(3)
    list.add(0, minusOne)
    mustEqual(4, list.size())
    mustEqual(minusOne, list.get(0))
    mustEqual(zero, list.get(1))

    list.add(2, minusTwo)
    mustEqual(5, list.size())
    mustEqual(minusTwo, list.get(2))
    mustEqual(two, list.get(4))
  }

  /** lists with same elements are equal and have same hashCode
   */
  @Test def testEquals(): Unit = {
    val a: List[Item] = populatedList(3)
    val b: List[Item] = populatedList(3)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    assertTrue(a.containsAll(b))
    assertTrue(b.containsAll(a))
    mustEqual(a.hashCode(), b.hashCode())
    a.add(minusOne)
    assertFalse(a.equals(b))
    assertFalse(b.equals(a))
    assertTrue(a.containsAll(b))
    assertFalse(b.containsAll(a))
    b.add(minusOne)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    assertTrue(a.containsAll(b))
    assertTrue(b.containsAll(a))
    mustEqual(a.hashCode(), b.hashCode())

    assertFalse(a.equals(null))
  }

  /** containsAll returns true for collections with subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val list: List[Item] = populatedList(3)
    assertTrue(list.containsAll(Arrays.asList()))
    assertTrue(list.containsAll(Arrays.asList(one)))
    assertTrue(list.containsAll(Arrays.asList(one, two)))
    assertFalse(list.containsAll(Arrays.asList(one, two, six)))
    assertFalse(list.containsAll(Arrays.asList(six)))

    try {
      list.containsAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** get returns the value at the given index
   */
  @Test def testGet(): Unit = {
    val list: List[Item] = populatedList(3)
    mustEqual(0, list.get(0))
  }

  /** indexOf(Object) returns the index of the first occurrence of the specified
   *  element in this list, or -1 if this list does not contain the element
   */
  @Test def testIndexOf(): Unit = {
    val list: List[Item] = populatedList(3)
    mustEqual(-1, list.indexOf(minusTen))
    val size = list.size()
    var i = 0
    while (i < size) {
      val I = itemFor(i)
      mustEqual(i, list.indexOf(I))
      mustEqual(i, list.subList(0, size).indexOf(I))
      mustEqual(i, list.subList(0, i + 1).indexOf(I))
      mustEqual(-1, list.subList(0, i).indexOf(I))
      mustEqual(0, list.subList(i, size).indexOf(I))
      mustEqual(-1, list.subList(i + 1, size).indexOf(I))
      i += 1
    }

    list.add(one)
    mustEqual(1, list.indexOf(one))
    mustEqual(1, list.subList(0, size + 1).indexOf(one))
    mustEqual(0, list.subList(1, size + 1).indexOf(one))
    mustEqual(size - 2, list.subList(2, size + 1).indexOf(one))
    mustEqual(0, list.subList(size, size + 1).indexOf(one))
    mustEqual(-1, list.subList(size + 1, size + 1).indexOf(one))
  }

  /** indexOf(E, int) returns the index of the first occurrence of the specified
   *  element in this list, searching forwards from index, or returns -1 if the
   *  element is not found
   */
  @Test def testIndexOf2(): Unit = {
    val list = populatedList(3)
    val size = list.size()
    mustEqual(-1, list.indexOf(minusTen, 0))

    // we might expect IOOBE, but spec says otherwise
    mustEqual(-1, list.indexOf(zero, size))
    mustEqual(-1, list.indexOf(zero, Integer.MAX_VALUE))

    assertThrows(
      classOf[IndexOutOfBoundsException],
      () => list.indexOf(zero, -1),
      () => list.indexOf(zero, Integer.MIN_VALUE)
    )

    var i = 0
    while (i < size) {
      val I = itemFor(i)
      mustEqual(i, list.indexOf(I, 0))
      mustEqual(i, list.indexOf(I, i))
      mustEqual(-1, list.indexOf(I, i + 1))
      i += 1
    }

    list.add(one)
    mustEqual(1, list.indexOf(one, 0))
    mustEqual(1, list.indexOf(one, 1))
    mustEqual(size, list.indexOf(one, 2))
    mustEqual(size, list.indexOf(one, size))
  }

  /** isEmpty returns true when empty, else false
   */
  @Test def testIsEmpty(): Unit = {
    val empty: List[Item] = new CopyOnWriteArrayList[Item]()
    assertTrue(empty.isEmpty())
    assertTrue(empty.subList(0, 0).isEmpty())

    val full: List[Item] = populatedList(SIZE)
    assertFalse(full.isEmpty())
    assertTrue(full.subList(0, 0).isEmpty())
    assertTrue(full.subList(SIZE, SIZE).isEmpty())
  }

  /** iterator() returns an iterator containing the elements of the list in
   *  insertion order
   */
  @Test def testIterator(): Unit = {
    val empty: Collection[Item] = new CopyOnWriteArrayList[Item]()
    assertFalse(empty.iterator().hasNext())
    try {
      empty.iterator().next()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full: Collection[Item] = populatedList(elements)

    val it: Iterator[_ <: Item] = full.iterator()
    var j = 0
    while (j < SIZE) {
      assertTrue(it.hasNext())
      mustEqual(elements(j), it.next())
      j += 1
    }
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    val c: Collection[Item] = new CopyOnWriteArrayList[Item]()
    assertIteratorExhausted(c.iterator())
  }

  /** iterator.remove throws UnsupportedOperationException
   */
  @Test def testIteratorRemove(): Unit = {
    val list = populatedList(SIZE)
    val it: Iterator[_ <: Item] = list.iterator()
    it.next()
    try {
      it.remove()
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
  }

  /** toString contains toString of elements
   */
  @Test def testToString(): Unit = {
    mustEqual("[]", new CopyOnWriteArrayList[Item]().toString())
    val list: List[Item] = populatedList(3)
    val s = list.toString()
    var i = 0
    while (i < 3) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
    mustEqual(new ArrayList[Item](list).toString(), list.toString())
  }

  /** lastIndexOf(Object) returns the index of the last occurrence of the
   *  specified element in this list, or -1 if this list does not contain the
   *  element
   */
  @Test def testLastIndexOf1(): Unit = {
    val list: List[Item] = populatedList(3)
    mustEqual(-1, list.lastIndexOf(itemFor(-42)))
    val size = list.size()
    var i = 0
    while (i < size) {
      val I = itemFor(i)
      mustEqual(i, list.lastIndexOf(I))
      mustEqual(i, list.subList(0, size).lastIndexOf(I))
      mustEqual(i, list.subList(0, i + 1).lastIndexOf(I))
      mustEqual(-1, list.subList(0, i).lastIndexOf(I))
      mustEqual(0, list.subList(i, size).lastIndexOf(I))
      mustEqual(-1, list.subList(i + 1, size).lastIndexOf(I))
      i += 1
    }

    list.add(one)
    mustEqual(size, list.lastIndexOf(one))
    mustEqual(size, list.subList(0, size + 1).lastIndexOf(one))
    mustEqual(1, list.subList(0, size).lastIndexOf(one))
    mustEqual(0, list.subList(1, 2).lastIndexOf(one))
    mustEqual(-1, list.subList(0, 1).indexOf(one))
  }

  /** lastIndexOf(E, int) returns the index of the last occurrence of the
   *  specified element in this list, searching backwards from index, or returns
   *  -1 if the element is not found
   */
  @Test def testLastIndexOf2(): Unit = {
    val list = populatedList(3)

    // we might expect IOOBE, but spec says otherwise
    mustEqual(-1, list.lastIndexOf(zero, -1))

    val size = list.size()
    assertThrows(
      classOf[IndexOutOfBoundsException],
      () => list.lastIndexOf(zero, size),
      () => list.lastIndexOf(zero, Integer.MAX_VALUE)
    )

    var i = 0
    while (i < size) {
      val I = itemFor(i)
      mustEqual(i, list.lastIndexOf(I, i))
      mustEqual(list.indexOf(I), list.lastIndexOf(I, i))
      if (i > 0)
        mustEqual(-1, list.lastIndexOf(I, i - 1))
      i += 1
    }
    list.add(one)
    list.add(three)
    mustEqual(1, list.lastIndexOf(one, 1))
    mustEqual(1, list.lastIndexOf(one, 2))
    mustEqual(3, list.lastIndexOf(one, 3))
    mustEqual(3, list.lastIndexOf(one, 4))
    mustEqual(-1, list.lastIndexOf(three, 3))
  }

  /** listIterator traverses all elements
   */
  @Test def testListIterator1(): Unit = {
    val list: List[Item] = populatedList(SIZE)
    val i: ListIterator[_ <: Item] = list.listIterator()
    var j = 0
    while (i.hasNext()) {
      mustEqual(j, i.next())
      j += 1
    }
    mustEqual(SIZE, j)
  }

  /** listIterator only returns those elements after the given index
   */
  @Test def testListIterator2(): Unit = {
    val list: List[Item] = populatedList(3)
    val i: ListIterator[_ <: Item] = list.listIterator(1)
    var j = 0
    while (i.hasNext()) {
      mustEqual(j + 1, i.next())
      j += 1
    }
    mustEqual(2, j)
  }

  /** remove(int) removes and returns the object at the given index
   */
  @Test def testRemove_int(): Unit = {
    val SIZE = 3
    var i = 0
    while (i < SIZE) {
      val list: List[Item] = populatedList(SIZE)
      mustEqual(i, list.remove(i))
      mustEqual(SIZE - 1, list.size())
      mustNotContain(list, i)
      i += 1
    }
  }

  /** remove(Object) removes the object if found and returns true
   */
  @Test def testRemove_Object(): Unit = {
    val SIZE = 3
    var i = 0
    while (i < SIZE) {
      val list: List[Item] = populatedList(SIZE)
      mustNotRemove(list, fortytwo)
      mustRemove(list, i)
      mustEqual(SIZE - 1, list.size())
      mustNotContain(list, i)
      i += 1
    }
    val x = new CopyOnWriteArrayList[Item](Arrays.asList(four, five, six))
    mustRemove(x, six)
    mustEqual(x, Arrays.asList(four, five))
    mustRemove(x, four)
    mustEqual(x, Arrays.asList(five))
    mustRemove(x, five)
    mustEqual(x, Arrays.asList())
    mustNotRemove(x, five)
  }

  /** removeAll removes all elements from the given collection
   */
  @Test def testRemoveAll(): Unit = {
    val list: List[Item] = populatedList(3)
    assertTrue(list.removeAll(Arrays.asList(one, two)))
    mustEqual(1, list.size())
    assertFalse(list.removeAll(Arrays.asList(one, two)))
    mustEqual(1, list.size())
  }

  /** set changes the element at the given index
   */
  @Test def testSet(): Unit = {
    val list: List[Item] = populatedList(3)
    mustEqual(2, list.set(2, four))
    mustEqual(4, list.get(2))
  }

  /** size returns the number of elements
   */
  @Test def testSize(): Unit = {
    val empty: List[Item] = new CopyOnWriteArrayList[Item]()
    mustEqual(0, empty.size())
    mustEqual(0, empty.subList(0, 0).size())

    val full: List[Item] = populatedList(SIZE)
    mustEqual(SIZE, full.size())
    mustEqual(0, full.subList(0, 0).size())
    mustEqual(0, full.subList(SIZE, SIZE).size())
  }

  /** toArray() returns an Object array containing all elements from the list in
   *  insertion order
   */
  @Test def testToArray(): Unit = {
    val a = new CopyOnWriteArrayList[Item]().toArray()
    assertTrue(Arrays.equals(new Array[Object](0), a))
    assertSame(classOf[Array[Object]], a.getClass())

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full: Collection[Item] = populatedList(elements)

    assertTrue(
      Arrays.equals(elements.asInstanceOf[Array[Object]], full.toArray())
    )
    assertSame(classOf[Array[Object]], full.toArray().getClass())
  }

  /** toArray(Item array) returns an Item array containing all elements from the
   *  list in insertion order
   */
  @Test def testToArray2(): Unit = {
    val empty: Collection[Item] = new CopyOnWriteArrayList[Item]()
    var a: Array[Item] = null

    a = new Array[Item](0)
    assertSame(a, empty.toArray(a))

    a = new Array[Item](SIZE / 2)
    Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertSame(a, empty.toArray(a))
    assertNull(a(0))
    var i = 1
    while (i < a.length) {
      mustEqual(42, a(i))
      i += 1
    }

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full: Collection[Item] = populatedList(elements)

    Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertTrue(
      Arrays.equals(
        elements.asInstanceOf[Array[Object]],
        full.toArray(a).asInstanceOf[Array[Object]]
      )
    )
    i = 0
    while (i < a.length) {
      mustEqual(42, a(i))
      i += 1
    }
    assertSame(classOf[Array[Item]], full.toArray(a).getClass())

    a = new Array[Item](SIZE)
    Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertSame(a, full.toArray(a))
    assertTrue(
      Arrays.equals(
        elements.asInstanceOf[Array[Object]],
        a.asInstanceOf[Array[Object]]
      )
    )

    a = new Array[Item](2 * SIZE)
    Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertSame(a, full.toArray(a))
    assertTrue(
      Arrays.equals(
        elements.asInstanceOf[Array[Object]],
        Arrays.copyOf(a.asInstanceOf[Array[Object]], SIZE)
      )
    )
    assertNull(a(SIZE))
    i = SIZE + 1
    while (i < a.length) {
      mustEqual(42, a(i))
      i += 1
    }
  }

  /** sublists contains elements at indexes offset from their base
   */
  @Test def testSubList(): Unit = {
    val a: List[Item] = populatedList(10)
    assertTrue(a.subList(1, 1).isEmpty())
    var j = 0
    while (j < 9) {
      var i = j
      while (i < 10) {
        val b: List[Item] = a.subList(j, i)
        var k = j
        while (k < i) {
          mustEqual(itemFor(k), b.get(k - j))
          k += 1
        }
        i += 1
      }
      j += 1
    }

    val s: List[Item] = a.subList(2, 5)
    mustEqual(3, s.size())
    s.set(2, minusOne)
    mustEqual(a.get(4), minusOne)
    s.clear()
    mustEqual(7, a.size())

    assertThrows(
      classOf[IndexOutOfBoundsException],
      () => s.get(0),
      () => s.set(0, fortytwo)
    )
  }

  /** toArray throws an ArrayStoreException when the given array can not store
   *  the objects inside the list
   */
  @Ignore("No array object exact element type information in SN runtime")
  @Test def testToArray_ArrayStoreException(): Unit = {
    val list: List[Item] = new CopyOnWriteArrayList[Item]()
    // Items are not auto-converted to Longs
    list.add(eightysix)
    list.add(ninetynine)
    assertThrows(
      classOf[ArrayStoreException],
      () => list.toArray(new Array[java.lang.Long](0)),
      () => list.toArray(new Array[java.lang.Long](5))
    )
  }

  def testIndexOutOfBoundsException(list: List[_]): Unit = {
    val size = list.size()
    val raw = list.asInstanceOf[List[AnyRef]]
    assertThrows(
      classOf[IndexOutOfBoundsException],
      () => raw.get(-1),
      () => raw.get(size),
      () => raw.set(-1, "qwerty"),
      () => raw.set(size, "qwerty"),
      () => raw.add(-1, "qwerty"),
      () => raw.add(size + 1, "qwerty"),
      () => raw.remove(-1),
      () => raw.remove(size),
      () => raw.addAll(-1, Collections.emptyList()),
      () => raw.addAll(size + 1, Collections.emptyList()),
      () => raw.listIterator(-1),
      () => raw.listIterator(size + 1),
      () => raw.subList(-1, size),
      () => raw.subList(0, size + 1)
    )

    // Conversely, operations that must not throw
    raw.addAll(0, Collections.emptyList())
    raw.addAll(size, Collections.emptyList())
    raw.add(0, "qwerty")
    raw.add(raw.size(), "qwerty")
    raw.get(0)
    raw.get(raw.size() - 1)
    raw.set(0, "azerty")
    raw.set(raw.size() - 1, "azerty")
    raw.listIterator(0)
    raw.listIterator(raw.size())
    raw.subList(0, raw.size())
    raw.remove(raw.size() - 1)
  }

  /** IndexOutOfBoundsException is thrown when specified
   */
  @Test def testIndexOutOfBoundsException(): Unit = {
    val rnd = ThreadLocalRandom.current()
    val x: List[Item] = populatedList(rnd.nextInt(5))
    testIndexOutOfBoundsException(x)

    val start = rnd.nextInt(x.size() + 1)
    val end = rnd.nextInt(start, x.size() + 1)
    assertThrows(
      classOf[IndexOutOfBoundsException],
      () => x.subList(start, start - 1)
    )
    val subList: List[Item] = x.subList(start, end)
    testIndexOutOfBoundsException(x)
  }

  // /**
  //  * a deserialized/reserialized list equals original
  //  * Unsupported: serialClone / ObjectInputStream unavailable on Scala Native
  //  */
  // @Test def testSerialization(): Unit = {
  //   val x: List[Item] = populatedList(SIZE)
  //   val y: List[Item] = serialClone(x)
  //
  //   assertNotSame(x, y)
  //   mustEqual(x.size(), y.size())
  //   mustEqual(x.toString(), y.toString())
  //   assertTrue(Arrays.equals(x.toArray(), y.toArray()))
  //   mustEqual(x, y)
  //   mustEqual(y, x)
  //   while (!x.isEmpty()) {
  //     assertFalse(y.isEmpty())
  //     mustEqual(x.remove(0), y.remove(0))
  //   }
  //   assertTrue(y.isEmpty())
  // }
}
