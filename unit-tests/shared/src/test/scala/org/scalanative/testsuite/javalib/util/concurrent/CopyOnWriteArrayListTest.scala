// Ported from Scala.js commit: e7f1ff7 dated: 2022-06-01

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{CopyOnWriteArrayList, ThreadLocalRandom}
import java.util.{
  ArrayList, Arrays, Collection, Collections, List => JList,
  NoSuchElementException
}
import java.{util => ju}

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.{
  ListFactory, ListTest, TrivialImmutableCollection
}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CopyOnWriteArrayListTest extends JSR166Test with ListTest {
  import CopyOnWriteArrayListTest._
  import JSR166Test._

  def factory: CopyOnWriteArrayListFactory = new CopyOnWriteArrayListFactory

  @Test def addIfAbsent(): Unit = {
    val list = factory.empty[Int]

    assertTrue(list.addIfAbsent(0))
    assertEquals(1, list.size)
    assertEquals(0, list.get(0))

    assertFalse(list.addIfAbsent(0))
    assertEquals(1, list.size)
    assertEquals(0, list.get(0))

    assertTrue(list.addIfAbsent(1))
    assertEquals(2, list.size)
    assertEquals(0, list.get(0))
    assertEquals(1, list.get(1))
  }

  @Test def addAllAbsent(): Unit = {
    val list = factory.empty[Int]

    assertEquals(
      3,
      list.addAllAbsent(TrivialImmutableCollection((0 until 3): _*))
    )
    assertEquals(3, list.size)
    for (i <- 0 until 3)
      assertEquals(i, list.get(i))

    assertEquals(
      0,
      list.addAllAbsent(TrivialImmutableCollection((0 until 2): _*))
    )
    assertEquals(3, list.size)
    for (i <- 0 until 3)
      assertEquals(i, list.get(i))

    assertEquals(
      3,
      list.addAllAbsent(TrivialImmutableCollection((3 until 6): _*))
    )
    assertEquals(6, list.size)
    for (i <- 0 until 6)
      assertEquals(i, list.get(i))

    assertEquals(
      4,
      list.addAllAbsent(TrivialImmutableCollection((0 until 10): _*))
    )
    assertEquals(10, list.size)
    for (i <- 0 until 10)
      assertEquals(i, list.get(i))

    assertEquals(1, list.addAllAbsent(TrivialImmutableCollection(42, 42, 42)))
    assertEquals(11, list.size)
    for (i <- 0 until 10)
      assertEquals(i, list.get(i))
    assertEquals(42, list.get(10))
  }

  @Test def iteratorInt(): Unit = {
    val list = factory.empty[Int]
    list.addAll(TrivialImmutableCollection((0 to 10): _*))

    val iter = list.iterator()
    list.clear()
    val iter2 = list.iterator()
    list.addAll(TrivialImmutableCollection((0 to 5): _*))

    for (i <- 0 to 10) {
      assertTrue(iter.hasNext)
      if (iter.hasNext)
        assertEquals(i, iter.next())
    }
    assertFalse(iter2.hasNext)
  }

  @Test def newFromArray_Issue2023(): Unit = {
    def test[T <: AnyRef](arr: Array[T]): Unit = {
      val cowal1 = factory.newFrom(arr)
      assertEquals(arr.length, cowal1.size)
      for (i <- arr.indices)
        assertEquals(arr(i), cowal1.get(i))
    }

    test(Array("a", "", "da", "23"))
    test(Array[Integer](1, 7, 2, 5, 3))
    test(Array[Character]('a', '3', '5', 'g', 'a'))
  }

  @Test def testConstructor(): Unit = {
    val list = new CopyOnWriteArrayList[Item]()
    assertTrue(list.isEmpty())
  }

  @Test def testConstructor2(): Unit = {
    val elements = defaultItems
    val list = new CopyOnWriteArrayList[Item](elements)
    for (i <- 0 until SIZE) mustEqual(elements(i), list.get(i))
  }

  @Test def testConstructor3(): Unit = {
    val elements = defaultItems
    val list = new CopyOnWriteArrayList[Item](Arrays.asList(elements: _*))
    for (i <- 0 until SIZE) mustEqual(elements(i), list.get(i))
  }

  @Test def testAddAll(): Unit = {
    val list = populatedList(3)
    assertTrue(list.addAll(Arrays.asList(itemThree, itemFour, itemFive)))
    mustEqual(6, list.size())
    assertTrue(list.addAll(Arrays.asList(itemThree, itemFour, itemFive)))
    mustEqual(9, list.size())
  }

  @Test def testAddAllAbsent(): Unit = {
    val list = populatedList(3)
    mustEqual(2, list.addAllAbsent(Arrays.asList(itemThree, itemFour, itemOne)))
    mustEqual(5, list.size())
    mustEqual(0, list.addAllAbsent(Arrays.asList(itemThree, itemFour, itemOne)))
    mustEqual(5, list.size())
  }

  @Test def testAddIfAbsent(): Unit = {
    val list = populatedList(SIZE)
    list.addIfAbsent(itemOne)
    mustEqual(SIZE, list.size())
  }

  @Test def testAddIfAbsent2(): Unit = {
    val list = populatedList(SIZE)
    list.addIfAbsent(itemThree)
    mustContain(list, itemThree)
  }

  @Test def testClear(): Unit = {
    val list = populatedList(SIZE)
    list.clear()
    mustEqual(0, list.size())
  }

  @Test def testClone(): Unit = {
    val l1 = populatedList(SIZE)
    val l2 = l1.clone().asInstanceOf[CopyOnWriteArrayList[Item]]
    mustEqual(l1, l2)
    l1.clear()
    assertFalse(l1.equals(l2))
  }

  @Test def testContains(): Unit = {
    val list = populatedList(3)
    mustContain(list, itemOne)
    mustNotContain(list, itemFive)
  }

  @Test def testAddIndex(): Unit = {
    val list = populatedList(3)
    list.add(0, minusOne)
    mustEqual(4, list.size())
    mustEqual(minusOne, list.get(0))
    mustEqual(itemZero, list.get(1))

    list.add(2, itemFor(-2))
    mustEqual(5, list.size())
    mustEqual(itemFor(-2), list.get(2))
    mustEqual(itemTwo, list.get(4))
  }

  @Test def testEquals(): Unit = {
    val a = populatedList(3)
    val b = populatedList(3)
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

  @Test def testContainsAll(): Unit = {
    val list = populatedList(3)
    assertTrue(list.containsAll(Collections.emptyList[Item]()))
    assertTrue(list.containsAll(Arrays.asList(itemOne)))
    assertTrue(list.containsAll(Arrays.asList(itemOne, itemTwo)))
    assertFalse(list.containsAll(Arrays.asList(itemOne, itemTwo, itemSix)))
    assertFalse(list.containsAll(Arrays.asList(itemSix)))
    assertThrows(classOf[NullPointerException], list.containsAll(null))
  }

  @Test def testGet(): Unit = {
    val list = populatedList(3)
    mustEqual(0, list.get(0))
  }

  @Test def testIndexOf(): Unit = {
    val list = populatedList(3)
    mustEqual(-1, list.indexOf(itemFor(-10)))
    val size = list.size()
    for (i <- 0 until size) {
      val item = itemFor(i)
      mustEqual(i, list.indexOf(item))
      mustEqual(i, list.subList(0, size).indexOf(item))
      mustEqual(i, list.subList(0, i + 1).indexOf(item))
      mustEqual(-1, list.subList(0, i).indexOf(item))
      mustEqual(0, list.subList(i, size).indexOf(item))
      mustEqual(-1, list.subList(i + 1, size).indexOf(item))
    }

    list.add(itemOne)
    mustEqual(1, list.indexOf(itemOne))
    mustEqual(1, list.subList(0, size + 1).indexOf(itemOne))
    mustEqual(0, list.subList(1, size + 1).indexOf(itemOne))
    mustEqual(size - 2, list.subList(2, size + 1).indexOf(itemOne))
    mustEqual(0, list.subList(size, size + 1).indexOf(itemOne))
    mustEqual(-1, list.subList(size + 1, size + 1).indexOf(itemOne))
  }

  @Test def testIndexOf2(): Unit = {
    val list = populatedList(3)
    val size = list.size()
    mustEqual(-1, list.indexOf(itemFor(-10), 0))
    mustEqual(-1, list.indexOf(itemZero, size))
    mustEqual(-1, list.indexOf(itemZero, Integer.MAX_VALUE))
    assertThrows(classOf[IndexOutOfBoundsException], list.indexOf(itemZero, -1))
    assertThrows(
      classOf[IndexOutOfBoundsException],
      list.indexOf(itemZero, Integer.MIN_VALUE)
    )

    for (i <- 0 until size) {
      val item = itemFor(i)
      mustEqual(i, list.indexOf(item, 0))
      mustEqual(i, list.indexOf(item, i))
      mustEqual(-1, list.indexOf(item, i + 1))
    }

    list.add(itemOne)
    mustEqual(1, list.indexOf(itemOne, 0))
    mustEqual(1, list.indexOf(itemOne, 1))
    mustEqual(size, list.indexOf(itemOne, 2))
    mustEqual(size, list.indexOf(itemOne, size))
  }

  @Test def testIsEmpty(): Unit = {
    val empty = new CopyOnWriteArrayList[Item]()
    assertTrue(empty.isEmpty())
    assertTrue(empty.subList(0, 0).isEmpty())

    val full = populatedList(SIZE)
    assertFalse(full.isEmpty())
    assertTrue(full.subList(0, 0).isEmpty())
    assertTrue(full.subList(SIZE, SIZE).isEmpty())
  }

  @Test def testIterator(): Unit = {
    val empty = new CopyOnWriteArrayList[Item]()
    assertFalse(empty.iterator().hasNext())
    assertThrows(classOf[NoSuchElementException], empty.iterator().next())

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedList(elements)
    val it = full.iterator()
    for (j <- 0 until SIZE) {
      assertTrue(it.hasNext())
      mustEqual(elements(j), it.next())
    }
    assertIteratorExhausted(it)
  }

  @Test def testEmptyIterator(): Unit =
    assertIteratorExhausted(new CopyOnWriteArrayList[Item]().iterator())

  @Test def testIteratorRemove(): Unit = {
    val list = populatedList(SIZE)
    val it = list.iterator()
    it.next()
    assertThrows(classOf[UnsupportedOperationException], it.remove())
  }

  @Test def testToString(): Unit = {
    mustEqual("[]", new CopyOnWriteArrayList[Item]().toString())
    val list = populatedList(3)
    val s = list.toString()
    for (i <- 0 until 3) assertTrue(s.contains(String.valueOf(i)))
    mustEqual(new ArrayList[Item](list).toString(), list.toString())
  }

  @Test def testLastIndexOf1(): Unit = {
    val list = populatedList(3)
    mustEqual(-1, list.lastIndexOf(itemFor(-42)))
    val size = list.size()
    for (i <- 0 until size) {
      val item = itemFor(i)
      mustEqual(i, list.lastIndexOf(item))
      mustEqual(i, list.subList(0, size).lastIndexOf(item))
      mustEqual(i, list.subList(0, i + 1).lastIndexOf(item))
      mustEqual(-1, list.subList(0, i).lastIndexOf(item))
      mustEqual(0, list.subList(i, size).lastIndexOf(item))
      mustEqual(-1, list.subList(i + 1, size).lastIndexOf(item))
    }

    list.add(itemOne)
    mustEqual(size, list.lastIndexOf(itemOne))
    mustEqual(size, list.subList(0, size + 1).lastIndexOf(itemOne))
    mustEqual(1, list.subList(0, size).lastIndexOf(itemOne))
    mustEqual(0, list.subList(1, 2).lastIndexOf(itemOne))
    mustEqual(-1, list.subList(0, 1).indexOf(itemOne))
  }

  @Test def testLastIndexOf2(): Unit = {
    val list = populatedList(3)
    mustEqual(-1, list.lastIndexOf(itemZero, -1))

    val size = list.size()
    assertThrows(
      classOf[IndexOutOfBoundsException],
      list.lastIndexOf(itemZero, size)
    )
    assertThrows(
      classOf[IndexOutOfBoundsException],
      list.lastIndexOf(itemZero, Integer.MAX_VALUE)
    )

    for (i <- 0 until size) {
      val item = itemFor(i)
      mustEqual(i, list.lastIndexOf(item, i))
      mustEqual(list.indexOf(item), list.lastIndexOf(item, i))
      if (i > 0) mustEqual(-1, list.lastIndexOf(item, i - 1))
    }
    list.add(itemOne)
    list.add(itemThree)
    mustEqual(1, list.lastIndexOf(itemOne, 1))
    mustEqual(1, list.lastIndexOf(itemOne, 2))
    mustEqual(3, list.lastIndexOf(itemOne, 3))
    mustEqual(3, list.lastIndexOf(itemOne, 4))
    mustEqual(-1, list.lastIndexOf(itemThree, 3))
  }

  @Test def testListIterator1(): Unit = {
    val list = populatedList(SIZE)
    val it = list.listIterator()
    var j = 0
    while (it.hasNext()) {
      mustEqual(j, it.next())
      j += 1
    }
    mustEqual(SIZE, j)
  }

  @Test def testListIterator2(): Unit = {
    val list = populatedList(3)
    val it = list.listIterator(1)
    var j = 0
    while (it.hasNext()) {
      mustEqual(j + 1, it.next())
      j += 1
    }
    mustEqual(2, j)
  }

  @Test def testRemove_int(): Unit = {
    val size = 3
    for (i <- 0 until size) {
      val list = populatedList(size)
      mustEqual(i, list.remove(i))
      mustEqual(size - 1, list.size())
      mustNotContain(list, i)
    }
  }

  @Test def testRemove_Object(): Unit = {
    val size = 3
    for (i <- 0 until size) {
      val list = populatedList(size)
      mustNotRemove(list, fortytwo)
      mustRemove(list, i)
      mustEqual(size - 1, list.size())
      mustNotContain(list, i)
    }
    val x = new CopyOnWriteArrayList[Item](
      Arrays.asList(itemFour, itemFive, itemSix)
    )
    mustRemove(x, itemSix)
    mustEqual(x, Arrays.asList(itemFour, itemFive))
    mustRemove(x, itemFour)
    mustEqual(x, Arrays.asList(itemFive))
    mustRemove(x, itemFive)
    mustEqual(x, Collections.emptyList[Item]())
    mustNotRemove(x, itemFive)
  }

  @Test def testRemoveAll(): Unit = {
    val list = populatedList(3)
    assertTrue(list.removeAll(Arrays.asList(itemOne, itemTwo)))
    mustEqual(1, list.size())
    assertFalse(list.removeAll(Arrays.asList(itemOne, itemTwo)))
    mustEqual(1, list.size())
  }

  @Test def testSet(): Unit = {
    val list = populatedList(3)
    mustEqual(2, list.set(2, itemFour))
    mustEqual(4, list.get(2))
  }

  @Test def testSize(): Unit = {
    val empty = new CopyOnWriteArrayList[Item]()
    mustEqual(0, empty.size())
    mustEqual(0, empty.subList(0, 0).size())

    val full = populatedList(SIZE)
    mustEqual(SIZE, full.size())
    mustEqual(0, full.subList(0, 0).size())
    mustEqual(0, full.subList(SIZE, SIZE).size())
  }

  @Test def testToArray(): Unit = {
    val a = new CopyOnWriteArrayList[Item]().toArray()
    assertTrue(Arrays.equals(new Array[AnyRef](0), a))
    assertSame(classOf[Array[AnyRef]], a.getClass())

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedList(elements)

    assertTrue(
      Arrays.equals(elements.asInstanceOf[Array[AnyRef]], full.toArray())
    )
    assertSame(classOf[Array[AnyRef]], full.toArray().getClass())
  }

  @Test def testToArray2(): Unit = {
    def fillItems(a: Array[Item]): Unit =
      Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    def arraysEqual(a: Array[Item], b: Array[Item]): Boolean =
      Arrays.equals(
        a.asInstanceOf[Array[Object]],
        b.asInstanceOf[Array[Object]]
      )

    val empty = new CopyOnWriteArrayList[Item]()
    var a: Array[Item] = null

    a = new Array[Item](0)
    assertSame(a, empty.toArray(a))

    a = new Array[Item](SIZE / 2)
    fillItems(a)
    assertSame(a, empty.toArray(a))
    assertNull(a(0))
    for (i <- 1 until a.length) mustEqual(42, a(i))

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedList(elements)

    fillItems(a)
    assertTrue(arraysEqual(elements, full.toArray(a)))
    for (i <- 0 until a.length) mustEqual(42, a(i))
    assertSame(classOf[Array[Item]], full.toArray(a).getClass())

    a = new Array[Item](SIZE)
    fillItems(a)
    assertSame(a, full.toArray(a))
    assertTrue(arraysEqual(elements, a))

    a = new Array[Item](2 * SIZE)
    fillItems(a)
    assertSame(a, full.toArray(a))
    assertTrue(arraysEqual(elements, Arrays.copyOf(a, SIZE)))
    assertNull(a(SIZE))
    for (i <- SIZE + 1 until a.length) mustEqual(42, a(i))
  }

  @Test def testSubList(): Unit = {
    val a = populatedList(10)
    assertTrue(a.subList(1, 1).isEmpty())
    for (j <- 0 until 9) {
      for (i <- j until 10) {
        val b = a.subList(j, i)
        for (k <- j until i) mustEqual(itemFor(k), b.get(k - j))
      }
    }

    val s = a.subList(2, 5)
    mustEqual(3, s.size())
    s.set(2, minusOne)
    mustEqual(a.get(4), minusOne)
    s.clear()
    mustEqual(7, a.size())

    assertThrows(classOf[IndexOutOfBoundsException], s.get(0))
    assertThrows(classOf[IndexOutOfBoundsException], s.set(0, fortytwo))
  }

  @Ignore(
    "Scala Native reference arrays do not preserve runtime component types (#4845)"
  )
  @Test def testToArray_ArrayStoreException(): Unit = ()

  @Test def testIndexOutOfBoundsException(): Unit = {
    val x = populatedList(ThreadLocalRandom.current().nextInt(5))
    testIndexOutOfBoundsException(x)

    val start = ThreadLocalRandom.current().nextInt(x.size() + 1)
    val end = ThreadLocalRandom.current().nextInt(start, x.size() + 1)
    assertThrows(
      classOf[IndexOutOfBoundsException],
      x.subList(start, start - 1)
    )
    x.subList(start, end)
    testIndexOutOfBoundsException(x)
  }

  @Ignore("scala-native#4852: ObjectInputStream is unsupported")
  @Test def testSerialization(): Unit = ()

  private def testIndexOutOfBoundsException(list: JList[Item]): Unit = {
    val raw = list.asInstanceOf[JList[AnyRef]]
    val size = list.size()
    assertThrows(classOf[IndexOutOfBoundsException], raw.get(-1))
    assertThrows(classOf[IndexOutOfBoundsException], raw.get(size))
    assertThrows(classOf[IndexOutOfBoundsException], raw.set(-1, "qwerty"))
    assertThrows(classOf[IndexOutOfBoundsException], raw.set(size, "qwerty"))
    assertThrows(classOf[IndexOutOfBoundsException], raw.add(-1, "qwerty"))
    assertThrows(
      classOf[IndexOutOfBoundsException],
      raw.add(size + 1, "qwerty")
    )
    assertThrows(classOf[IndexOutOfBoundsException], raw.remove(-1))
    assertThrows(classOf[IndexOutOfBoundsException], raw.remove(size))
    assertThrows(
      classOf[IndexOutOfBoundsException],
      raw.addAll(-1, Collections.emptyList[AnyRef]())
    )
    assertThrows(
      classOf[IndexOutOfBoundsException],
      raw.addAll(size + 1, Collections.emptyList[AnyRef]())
    )
    assertThrows(classOf[IndexOutOfBoundsException], raw.listIterator(-1))
    assertThrows(classOf[IndexOutOfBoundsException], raw.listIterator(size + 1))
    assertThrows(classOf[IndexOutOfBoundsException], raw.subList(-1, size))
    assertThrows(classOf[IndexOutOfBoundsException], raw.subList(0, size + 1))

    raw.addAll(0, Collections.emptyList[AnyRef]())
    raw.addAll(size, Collections.emptyList[AnyRef]())
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
}

object CopyOnWriteArrayListTest {
  import JSR166Test._

  private val itemZero = itemFor(0)
  private val itemOne = itemFor(1)
  private val itemTwo = itemFor(2)
  private val itemThree = itemFor(3)
  private val itemFour = itemFor(4)
  private val itemFive = itemFor(5)
  private val itemSix = itemFor(6)

  def populatedList(n: Int): CopyOnWriteArrayList[Item] = {
    val list = new CopyOnWriteArrayList[Item]()
    assertTrue(list.isEmpty())
    for (i <- 0 until n) mustAdd(list, i)
    mustEqual(n <= 0, list.isEmpty())
    mustEqual(n, list.size())
    list
  }

  def populatedList(elements: Array[Item]): CopyOnWriteArrayList[Item] = {
    val list = new CopyOnWriteArrayList[Item]()
    assertTrue(list.isEmpty())
    for (element <- elements) list.add(element)
    assertFalse(list.isEmpty())
    mustEqual(elements.length, list.size())
    list
  }
}

class CopyOnWriteArrayListFactory extends ListFactory {

  override def allowsMutationThroughIterator: Boolean = false

  override def implementationName: String =
    "java.util.concurrent.CopyOnWriteArrayList"

  override def empty[E: ClassTag]: ju.concurrent.CopyOnWriteArrayList[E] =
    new ju.concurrent.CopyOnWriteArrayList[E]

  def newFrom[E <: AnyRef](
      arr: Array[E]
  ): ju.concurrent.CopyOnWriteArrayList[E] =
    new ju.concurrent.CopyOnWriteArrayList[E](arr)
}
