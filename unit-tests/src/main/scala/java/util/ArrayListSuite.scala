package java.util

object ArrayListSuite extends tests.Suite {

  def assertEquals[T](a: T, b: T) = assert(a == b)

  test("shouldStoreStrings") {
    val lst = new ArrayList[String]

    assertEquals(0, lst.size())
    lst.add("one")
    assertEquals(1, lst.size())
    assertEquals("one", lst.get(0))
    lst.add("two")
    assertEquals(2, lst.size())
    assertEquals("one", lst.get(0))
    assertEquals("two", lst.get(1))

    assertThrows[IndexOutOfBoundsException](lst.get(-1))
    assertThrows[IndexOutOfBoundsException](lst.get(lst.size))
  }

  test("shouldStoreIntegers") {
    val lst = new ArrayList[Int]

    lst.add(1)
    assertEquals(1, lst.size())
    assertEquals(1, lst.get(0))
    lst.add(2)
    assertEquals(2, lst.size())
    assertEquals(1, lst.get(0))
    assertEquals(2, lst.get(1))

    assertThrows[IndexOutOfBoundsException](lst.get(-1))
    assertThrows[IndexOutOfBoundsException](lst.get(lst.size))
  }

  test("shouldStoreDoubles") {
    val lst = new ArrayList[Double]

    lst.add(1.234)
    assertEquals(1, lst.size())
    assertEquals(1.234, lst.get(0))
    lst.add(2.345)
    assertEquals(2, lst.size())
    assertEquals(1.234, lst.get(0))
    assertEquals(2.345, lst.get(1))
    lst.add(Double.NaN)
    lst.add(+0.0)
    lst.add(-0.0)
    assertEquals(5, lst.size())
    assertEquals(1.234, lst.get(0))
    assertEquals(2.345, lst.get(1))
    assert(lst.get(2).isNaN)
    assert(lst.get(3).equals(+0.0))
    assert(lst.get(4).equals(-0.0))

    assertThrows[IndexOutOfBoundsException](lst.get(-1))
    assertThrows[IndexOutOfBoundsException](lst.get(lst.size))
  }

  test("shouldStoreCustomObjects") {
    case class TestObj(num: Int)

    val lst = new ArrayList[TestObj]

    lst.add(TestObj(100))
    assertEquals(1, lst.size())
    assertEquals(TestObj(100), lst.get(0))

    assertThrows[IndexOutOfBoundsException](lst.get(-1))
    assertThrows[IndexOutOfBoundsException](lst.get(lst.size))
  }

  test("shouldRemoveStoredElements") {
    val lst = new ArrayList[String]

    lst.add("one")
    lst.add("two")
    lst.add("three")

    assertNot(lst.remove("four"))
    assertEquals(3, lst.size())
    assert(lst.remove("two"))
    assertEquals(2, lst.size())
    assertEquals("one", lst.remove(0))
    assertEquals(1, lst.size())
    assertEquals("three", lst.get(0))

    assertThrows[IndexOutOfBoundsException](lst.remove(-1))
    assertThrows[IndexOutOfBoundsException](lst.remove(lst.size))
  }

  test("shouldRemoveStoredElementsOnDoubleCornerCases") {
    val al = new ArrayList[Double]

    al.add(1.234)
    al.add(2.345)
    al.add(Double.NaN)
    al.add(+0.0)
    al.add(-0.0)

    // al == ArrayList(1.234, 2.345, NaN, +0.0, -0.0)
    assert(al.remove(Double.NaN))
    // al == ArrayList(1.234, 2.345, +0.0, -0.0)
    assertEquals(4, al.size())
    assert(al.remove(2.345))
    // al == ArrayList(1.234, +0.0, -0.0)
    assertEquals(3, al.size())
    assertEquals(1.234, al.remove(0))
    // al == ArrayList(+0.0, -0.0)
    assertEquals(2, al.size())
    assert(al.remove(-0.0))
    // al == ArrayList(NaN, +0.0)
    assertEquals(1, al.size())

    al.clear()

    assert(al.isEmpty)
  }

  test("shouldBeClearedWithOneOperation") {
    val al = new ArrayList[String]

    al.add("one")
    al.add("two")
    assertEquals(2, al.size)
    al.clear()
    assertEquals(0, al.size)
  }

  test("shouldCheckContainedPresence") {
    val al = new ArrayList[String]

    al.add("one")
    assert(al.contains("one"))
    assertNot(al.contains("two"))
    assertNot(al.contains(null))
  }

  test("shouldCheckContainedPresenceForDoubleCornerCases") {
    val al = new ArrayList[Double]

    al.add(-0.0)
    assert(al.contains(-0.0))
    assertNot(al.contains(+0.0))

    al.clear()

    al.add(+0.0)
    assertNot(al.contains(-0.0))
    assert(al.contains(+0.0))
  }

  test("shouldGiveAProperSetOperation") {
    val al = new ArrayList[String]
    al.add("one")
    al.add("two")
    al.add("three")

    al.set(1, "four")
    assertEquals("one", al.get(0))
    assertEquals("four", al.get(1))
    assertEquals("three", al.get(2))

    assertThrows[IndexOutOfBoundsException](al.set(-1, ""))
    assertThrows[IndexOutOfBoundsException](al.set(al.size, ""))
  }

  test("shouldGiveProperIteratorOverElements") {
    val al = new ArrayList[String]
    al.add("one")
    al.add("two")
    al.add("three")

    val elements = al.iterator
    assert(elements.hasNext)
    assertEquals("one", elements.next())
    assert(elements.hasNext)
    assertEquals("two", elements.next())
    assert(elements.hasNext)
    assertEquals("three", elements.next())
    assertNot(elements.hasNext)
  }

  test("shouldGiveProperListIteratorOverElements") {
    val lst = new ArrayList[String]
    lst.add("one")
    lst.add("two")
    lst.add("three")

    val elements = lst.listIterator
    assertNot(elements.hasPrevious)
    assert(elements.hasNext)
    assertEquals("one", elements.next())
    assert(elements.hasPrevious)
    assert(elements.hasNext)
    assertEquals("two", elements.next())
    assert(elements.hasPrevious)
    assert(elements.hasNext)
    assertEquals("three", elements.next())
    assert(elements.hasPrevious)
    assertNot(elements.hasNext)
    assertEquals("three", elements.previous())
    assertEquals("two", elements.previous())
    assertEquals("one", elements.previous())
  }

  test("shouldAddElementsAtAGivenIndex") {
    val al = new ArrayList[String]
    al.add(0, "one")   // ["one"]
    al.add(0, "two")   // ["two", "one"]
    al.add(1, "three") // ["two", "three", "one"]

    assertEquals("two", al.get(0))
    assertEquals("three", al.get(1))
    assertEquals("one", al.get(2))

    assertThrows[IndexOutOfBoundsException](al.add(-1, ""))
    assertThrows[IndexOutOfBoundsException](al.add(al.size + 1, ""))
  }

  test("shouldGiveTheFirstIndexOfAnElement") {
    val al = new ArrayList[String]
    al.add("one")
    al.add("two")
    al.add("three")
    al.add("one")
    al.add("two")
    al.add("three")

    assertEquals(0, al.indexOf("one"))
    assertEquals(1, al.indexOf("two"))
    assertEquals(2, al.indexOf("three"))
    assertEquals(-1, al.indexOf("four"))
  }

  test("shouldGiveTheLastIndexOfAnElement") {
    val al = new ArrayList[String]
    al.add("one")
    al.add("two")
    al.add("three")
    al.add("one")
    al.add("two")
    al.add("three")

    assertEquals(3, al.lastIndexOf("one"))
    assertEquals(4, al.lastIndexOf("two"))
    assertEquals(5, al.lastIndexOf("three"))
    assertEquals(-1, al.lastIndexOf("four"))
  }

  test("shouldGiveTheFirstOrLastIndexOfAnElementForDoubleCornerCases") {
    val al = new ArrayList[Double]

    al.add(-0.0)
    al.add(+0.0)
    al.add(Double.NaN)
    al.add(+0.0)
    al.add(-0.0)
    al.add(Double.NaN)

    assertEquals(0, al.indexOf(-0.0))
    assertEquals(1, al.indexOf(+0.0))
    assertEquals(2, al.indexOf(Double.NaN))

    assertEquals(3, al.lastIndexOf(+0.0))
    assertEquals(4, al.lastIndexOf(-0.0))
    assertEquals(5, al.lastIndexOf(Double.NaN))
  }

  test("shouldGiveASublistBackedUpByTheOriginalList") {
    def testListIterator(list: java.util.List[String],
                         expected: Seq[String]): Unit = {
      val iter = list.listIterator
      for (elem <- expected) {
        assert(iter.hasNext)
        assertEquals(elem, iter.next())
      }
      assertNot(iter.hasNext)

      for (elem <- expected.reverse) {
        assert(iter.hasPrevious)
        assertEquals(elem, iter.previous())
      }
      assertNot(iter.hasPrevious)
    }

    val al = new ArrayList[String]

    al.add("one")
    al.add("two")
    al.add("three")
    al.add("four")
    al.add("five")
    al.add("six")

    testListIterator(al, Seq("one", "two", "three", "four", "five", "six"))

    val al0 = al.subList(0, al.size)
    assertEquals(6, al0.size)
    assertEquals(al.size, al0.size)
    for (i <- 0 until al.size)
      assertEquals(al.get(i), al0.get(i))
    al0.set(3, "zero")
    assertEquals("zero", al0.get(3))
    for (i <- 0 until al.size)
      assertEquals(al.get(i), al0.get(i))
    testListIterator(al, Seq("one", "two", "three", "zero", "five", "six"))
    testListIterator(al0, Seq("one", "two", "three", "zero", "five", "six"))

    val al1 = al.subList(2, 5)
    assertEquals(3, al1.size)
    for (i <- 0 until 3)
      assertEquals(al.get(2 + i), al1.get(i))
    al1.set(0, "nine")
    assertEquals("nine", al1.get(0))
    for (i <- 0 until 3) {
      assertEquals(al.get(2 + i), al1.get(i))
      assertEquals(al0.get(2 + i), al1.get(i))
    }
    assertEquals("nine", al1.get(0))
    assertEquals("zero", al1.get(1))
    assertEquals("five", al1.get(2))

    testListIterator(al, Seq("one", "two", "nine", "zero", "five", "six"))
    testListIterator(al1, Seq("nine", "zero", "five"))

    al1.clear()

    assertEquals("one", al.get(0))
    assertEquals("two", al.get(1))
    assertEquals("six", al.get(2))
    assertEquals(3, al.size)
    assertEquals(0, al1.size)
    testListIterator(al, Seq("one", "two", "six"))
    testListIterator(al1, Seq.empty)

    assert(al1.add("ten"))
    testListIterator(al, Seq("one", "two", "ten", "six"))
    testListIterator(al1, Seq("ten"))

    val iter = al1.listIterator
    iter.add("three")
    iter.next()
    iter.add("zero")

    testListIterator(al, Seq("one", "two", "three", "ten", "zero", "six"))
    testListIterator(al1, Seq("three", "ten", "zero"))
  }

  test("shouldIterateAndModifyElementsWithAListIteratorIfAllowed") {
    val s  = Seq("one", "two", "three")
    val ll = new ArrayList[String]

    for (e <- s)
      ll.add(e)

    val iter = ll.listIterator(1)

    assert(iter.hasNext())
    assert(iter.hasPrevious())

    assertEquals("one", iter.previous())

    assert(iter.hasNext())
    assertNot(iter.hasPrevious())

    assertEquals("one", iter.next())

    assertEquals("two", iter.next())
    assertEquals("three", iter.next())

    assertNot(iter.hasNext())
    assert(iter.hasPrevious())

    iter.add("four")

    assertNot(iter.hasNext())
    assert(iter.hasPrevious())

    assertEquals("four", iter.previous())

    iter.remove()

    assertNot(iter.hasNext())
    assert(iter.hasPrevious())
    assertEquals("three", iter.previous())
    iter.set("THREE")
    assertEquals("two", iter.previous())
    iter.set("TWO")
    assertEquals("one", iter.previous())
    iter.set("ONE")
    assert(iter.hasNext())
    assertNot(iter.hasPrevious())

    assertEquals("ONE", iter.next())
    iter.remove()
    assertEquals("TWO", iter.next())
    iter.remove()
    assertEquals("THREE", iter.next())
    iter.remove()

    assertNot(iter.hasNext())
    assertNot(iter.hasPrevious())

    assert(ll.isEmpty())
  }

  test("toString") {
    val al = new ArrayList[Any]()
    assert(al.toString == "[]")
    al.add(0, "foo")
    assert(al.toString == "[foo]")
    al.add("bar")
    assert(al.toString == "[foo, bar]")
    al.set(1, "baz")
    assert(al.toString == "[foo, baz]")
    al.remove(1)
    assert(al.toString == "[foo]")
    al.add(1, 123)
    assert(al.toString == "[foo, 123]")
    al.add(456)
    assert(al.toString == "[foo, 123, 456]")
    al.clear()
    assert(al.toString == "[]")
  }
}
