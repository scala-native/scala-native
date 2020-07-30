package java.util

import java.util

import scala.collection.JavaConverters._

object ArrayDequeSuite extends tests.Suite {

  test("ArrayDeque()") {
    val ad = new ArrayDeque()

    assert(ad != null, s"Constructor returned null")

    // There is no good way to check underlying capacity, which should
    // be 16.

    assert(ad.isEmpty(), s"constructed ArrayDeque() is not empty")

    val resultSize   = ad.size
    val expectedSize = 0
    assert(resultSize == expectedSize,
           s"size: ${resultSize} != expected: ${expectedSize}")
  }

  test("ArrayDeque(initialCapacity) - capacity >= 0") {
    val ad = new ArrayDeque(20)

    assert(ad != null, s"Constructor returned null")

    // There is no good way to check underlying capacity, which should
    // be 20.

    assert(ad.isEmpty(), s"constructed ArrayDeque() is not empty")

    val resultSize   = ad.size
    val expectedSize = 0
    assert(resultSize == expectedSize,
           s"size: ${resultSize} != expected: ${expectedSize}")
  }

  test("ArrayDeque(initialCapacity) - capacity < 0") {
    // This test basically tests that no exception is thrown
    // when the initialCapacity is negative, implementing JVM behavior.

    val ad = new ArrayDeque(-1)

    assert(ad != null, s"Constructor returned null")

    // There is no good way to check underlying capacity, which should
    // be 20.

    assert(ad.isEmpty(), s"constructed ArrayDeque() is not empty")

    val resultSize   = ad.size
    val expectedSize = 0
    assert(resultSize == expectedSize,
           s"size: ${resultSize} != expected: ${expectedSize}")
  }

  test("ArrayDeque(null)") {
    assertThrows[NullPointerException] {
      new ArrayDeque(null)
    }
  }

  test("ArrayDeque(Collection[java.lang.Integer])") { // for AnyVal
    val is = Seq(1, 2, 3)
    val ad = new ArrayDeque(is.asJava)
    assert(ad.size() == 3, "a1")
    assert(!ad.isEmpty(), "a2")

    val result   = ad.toArray
    val expected = is.toArray
    assert(result.sameElements(expected),
           s"element: ${result} != expected: ${expected})")
  }

  test("ArrayDeque(Collection[String])") { // for AnyRef
    val is = Seq(1, 2, 3).map(_.toString)
    import scala.collection.JavaConverters._
    val ad = new ArrayDeque(is.asJava)
    assert(ad.size() == 3, "a1")
    assert(!ad.isEmpty(), "a2")

    val result   = ad.toArray
    val expected = is.toArray

    assert(result.sameElements(expected),
           s"element: ${result} != expected: ${expected})")
  }

  test(s"add(e) - trigger capacity change") {
    // Simple add()s are triggered by the addAll() in the previous
    // ArrayDesueue(constructor) test. Exercise a more complex code path.
    // Code should not fail when it resizes when adding the 17th element.

    val max = 20 // Must be > 16
    val is  = 1 to 20
    val ad  = new ArrayDeque[Int]()

    for (e <- is) {
      ad.add(e)
    }

    for (e <- is) {
      val result   = ad.removeFirst()
      val expected = e
      assert(result == expected, s"element: ${result} != expected: ${expected}")
    }
  }

  test(s"addFirst(e)") {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows[NullPointerException] {
        ad.addFirst(null.asInstanceOf[E])
      }
    }

    locally {
      val is = Seq(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.add(is(0))
      ad.addFirst(is(1))

      val result   = ad.toArray
      val expected = is.reverse.toArray

      assert(result.sameElements(expected),
             s"result: ${ad.toString} != " +
               s"expected: ${expected.mkString("[", ", ", "]")}")
    }
  }

  test(s"addLast(e)") {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows[NullPointerException] {
        ad.addLast(null.asInstanceOf[E])
      }
    }

    locally {
      val expected = Array(-1, -2)
      val ad       = new ArrayDeque[Int]()

      ad.add(expected(0))
      ad.addLast(expected(1))

      val result = ad.toArray

      assert(result.sameElements(expected),
             s"result: ${ad.toString} != " +
               s"expected: ${expected.mkString("[", ", ", "]")}")
    }
  }

  test(s"clear()") {
    val ad1 = new ArrayDeque(Seq(1, 2, 3, 2).asJava)
    ad1.clear()
    assert(ad1.isEmpty())
    // makes sure that clear()ing an already empty list is safe.
    ad1.clear()
  }

  test(s"clone()") {
    val ad1 = new ArrayDeque(Seq(1, 2, 3, 2).asJava)
    val ad2 = ad1.clone()

    val element = 1

    assert(!ad1.eq(ad2), "must be different objects")
    assert(ad1.toString == ad2.toString, "must have same contents")

    ad1.add(element)
    assert(ad1.toString != ad2.toString, "must have different contents")
    ad2.add(element)
    assert(ad1.toString == ad2.toString, "must have same contents")
  }

  test(s"contains(o: Any)") {
    val needle = Math.PI
    val is     = Seq(1.1, 2.2, 3.3, needle, 4.0)
    val ad     = new ArrayDeque(is.asJava)

    val result = ad.contains(needle)
    assert(result, s"'${ad.toString}' does not contain '${needle}'")
  }

  test(s"descendingIterator()") {
    // No good way on single threaded ScalaNative to test for
    // ConcurrentModificationException

    val is = Seq(1, 2, 3)
    val ad = new ArrayDeque(is.asJava)

    val result   = ad.descendingIterator.asScala.toArray
    val expected = is.reverse.toArray

    assert(result.sameElements(expected),
           s"element: result} != expected: ${expected})")
  }

  test(s"element()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.getFirst
      }
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.element

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"getFirst()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.getFirst
      }
    }

    locally {
      val is = Seq("33", "22", "11")
      val ad = new ArrayDeque(is.asJava)

      val result = ad.getFirst

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"getLast()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.getFirst
      }
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.getLast

      val expected = is.last

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  // test(s"isEmpty()") exercised in ArrayDeque constructors

  test(s"iterator()") {
    // No good way on single threaded ScalaNative to test for
    // ConcurrentModificationException

    val is = Seq(-11, 0, 1)
    val ad = new ArrayDeque(is.asJava)

    val result   = ad.iterator.asScala.toArray
    val expected = is.toArray

    assert(result.sameElements(expected),
           s"element: ${result} != expected: ${expected})")
  }

  test(s"offer(e: E)") {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows[NullPointerException] {
        ad.offer(null.asInstanceOf[E])
      }
    }

    locally {
      val expected = Array(-1, -2)
      val ad       = new ArrayDeque[Int]()

      ad.offer(expected(0))
      ad.offer(expected(1))

      val result = ad.toArray

      assert(result.sameElements(expected),
             s"result: ${ad.toString} != " +
               s"expected: ${expected.mkString("[", ", ", "]")}")
    }
  }

  test(s"offerFirst(e: E)") {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows[NullPointerException] {
        ad.offerFirst(null.asInstanceOf[E])
      }
    }

    locally {
      val is = Seq(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.offer(is(0))
      ad.offerFirst(is(1))

      val result   = ad.toArray
      val expected = is.reverse.toArray

      assert(result.sameElements(expected),
             s"result: ${ad.toString} != " +
               s"expected: ${expected.mkString("[", ", ", "]")}")
    }
  }

  test(s"offerLast(e: E)") {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows[NullPointerException] {
        ad.offerLast(null.asInstanceOf[E])
      }
    }

    locally {
      val expected = Array(-1, -2)
      val ad       = new ArrayDeque[Int]()

      ad.offerLast(expected(0))
      ad.offerLast(expected(1))

      val result = ad.toArray

      assert(result.sameElements(expected),
             s"result: ${ad.toString} != " +
               s"expected: ${expected.mkString("[", ", ", "]")}")
    }
  }

  test(s"peek()") {
    locally {
      val ad = new ArrayDeque()

      assert(ad.peek == null,
             s"expected null from peek() with empty ArrayDeque")
    }

    locally {
      val is = Seq("33", "22", "11")
      val ad = new ArrayDeque(is.asJava)

      val result = ad.peek

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"peekFirst()") {
    locally {
      val ad = new ArrayDeque()

      assert(ad.peekFirst == null,
             s"expected null from peekFirst() with empty ArrayDeque")
    }

    locally {
      val is = Seq("33", "22", "11")
      val ad = new ArrayDeque(is.asJava)

      val result = ad.peekFirst

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"peekLast()") {
    locally {
      val ad = new ArrayDeque()

      assert(ad.peekLast == null,
             s"expected null from peekFirst() with empty ArrayDeque")
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.peekLast

      val expected = is.last

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"poll()") {
    locally {
      val ad = new ArrayDeque()

      assert(ad.poll == null,
             s"expected null from poll() with empty ArrayDeque")
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.poll

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"pollFirst()") {
    locally {
      val ad = new ArrayDeque()

      assert(ad.pollFirst == null,
             s"expected null from pollFirst() with empty ArrayDeque")
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.pollFirst

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"pollLast()") {
    locally {
      val ad = new ArrayDeque()
      assert(ad.pollLast == null,
             s"expected null from pollLast() with empty ArrayDeque")
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.pollLast

      val expected = is.last

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"pop()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.pop
      }
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.pop

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"push(e: E)") {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows[NullPointerException] {
        ad.push(null.asInstanceOf[E])
      }
    }

    locally {
      val is = Seq(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.add(is(0))
      ad.push(is(1))

      val result   = ad.toArray
      val expected = is.reverse.toArray

      assert(result.sameElements(expected),
             s"result: ${ad.toString} != " +
               s"expected: ${expected.mkString("[", ", ", "]")}")
    }
  }

  test(s"remove()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.remove
      }
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.remove

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"remove(o: Any)") {
    val haystack = "Looking for a needle in a haystack"
    val words    = haystack.split(" ").toSeq
    val ad       = new ArrayDeque(words.asJava)

    locally {
      val adClone    = ad.clone()
      val adCloneStr = adClone.toString

      assert(ad.toString == adClone.toString,
             "deque and its clone must have same contents")

      val beforeSize = ad.size
      val needle     = "sharp"

      val result = ad.remove(needle)

      assert(!result, s"word '${needle}' found in string '${haystack}'")

      // Show deque has not changed

      val afterSize    = ad.size
      val expectedSize = beforeSize

      assert(afterSize == expectedSize,
             s"size: ${afterSize} != expected: ${beforeSize}")

      val adStr = ad.toString
      assert(ad.toString == adCloneStr,
             "deque: ${adStr} != expected: '${adCloneStr}'")
    }

    locally {
      val needle     = "needle"
      val beforeSize = ad.size

      val result = ad.remove(needle)

      assert(result, s"word '${needle}' not found in string '${haystack}'")

      // Show deque changed as expected.

      val afterSize    = ad.size
      val expectedSize = beforeSize - 1

      assert(afterSize == expectedSize,
             s"size: ${afterSize} != expected: ${beforeSize}")

      val adStr = ad.toString

      assert(!ad.toString.contains(needle),
             "deque: ${adStr} must not contain '${needle}'")
    }
  }

  test(s"removeFirst()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.removeFirst
      }
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.removeFirst

      val expected = is.head

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"removeFirstOccurrence(o: Any)") {
    val haystack = "Square needle || round needle || shiny needle"
    val words    = haystack.split(" ").toSeq
    val ad       = new ArrayDeque(words.asJava)

    locally {
      val adClone    = ad.clone()
      val adCloneStr = adClone.toString

      assert(ad.toString == adClone.toString,
             "deque and its clone must have same contents")

      val beforeSize = ad.size
      val needle     = "sharp"

      val result = ad.removeFirstOccurrence(needle)

      assert(!result, s"word '${needle}' found in string '${haystack}'")

      // Show deque has not changed

      val afterSize    = ad.size
      val expectedSize = beforeSize

      assert(afterSize == expectedSize,
             s"size: ${afterSize} != expected: ${beforeSize}")

      val adStr = ad.toString
      assert(ad.toString == adCloneStr,
             "deque: ${adStr} != expected: '${adCloneStr}'")
    }

    locally {
      val needle     = "needle"
      val beforeSize = ad.size

      val result = ad.removeFirstOccurrence(needle)

      assert(result, s"word '${needle}' not found in string '${haystack}'")

      // Show deque changed as expected.

      val afterSize    = ad.size
      val expectedSize = beforeSize - 1

      assert(afterSize == expectedSize,
             s"size: ${afterSize} != expected: ${beforeSize}")

      for (i <- 0 until words.length if i != 1) {
        val result   = ad.removeFirst
        val expected = words(i)
        assert(result == expected,
               "deque(${i}): ${result} != expected: '${expected}'")
      }
    }
  }

  test(s"removeLast()") {
    locally {
      val ad = new ArrayDeque()

      assertThrows[NoSuchElementException] {
        ad.removeLast
      }
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.asJava)

      val result = ad.removeLast

      val expected = is.last

      assert(result == expected, s"result: ${result} != expected: ${expected}")

      val afterSize    = ad.size
      val expectedSize = is.size - 1
      assert(afterSize == expectedSize,
             s"after size: ${afterSize} != expected: ${expectedSize}")
    }
  }

  test(s"removeLastOccurrence(o: Any)") {
    val haystack = "Square needle || round needle || shiny needle"
    val words    = haystack.split(" ").toSeq
    val ad       = new ArrayDeque(words.asJava)

    locally {
      val adClone    = ad.clone()
      val adCloneStr = adClone.toString

      assert(ad.toString == adClone.toString,
             "deque and its clone must have same contents")

      val beforeSize = ad.size
      val needle     = "sharp"

      val result = ad.removeLastOccurrence(needle)

      assert(!result, s"word '${needle}' found in string '${haystack}'")

      // Show deque has not changed

      val afterSize    = ad.size
      val expectedSize = beforeSize

      assert(afterSize == expectedSize,
             s"size: ${afterSize} != expected: ${beforeSize}")

      val adStr = ad.toString
      assert(ad.toString == adCloneStr,
             "deque: ${adStr} != expected: '${adCloneStr}'")
    }

    locally {
      val needle     = "needle"
      val beforeSize = ad.size

      val result = ad.removeLastOccurrence(needle)

      assert(result, s"word '${needle}' not found in string '${haystack}'")

      // Show deque changed as expected.

      val afterSize    = ad.size
      val expectedSize = beforeSize - 1

      assert(afterSize == expectedSize,
             s"size: ${afterSize} != expected: ${beforeSize}")

      for (i <- 0 until (words.length - 1)) {
        val result   = ad.removeFirst
        val expected = words(i)
        assert(result == expected,
               "deque(${i}): ${result} != expected: '${expected}'")
      }
    }
  }

  test("size()") {
    // exercised in ArrayDeque constructors
  }

  test("toArray()") {
    // exercised in ArrayDeque constructors
  }

  test("toArray(null) - throws NullPointerException") {
    val al1 = new ArrayDeque[String](Seq("apple", "banana", "cherry").asJava)
    assertThrows[NullPointerException] { al1.toArray(null) }
  }

  test("toArray(a: Array[T]) - arr is shorter") {
    val al1  = new ArrayDeque[String](Seq("apple", "banana", "cherry").asJava)
    val ain  = Array.empty[String]
    val aout = al1.toArray(ain)
    assert(ain ne aout)
    assert(Array("apple", "banana", "cherry") sameElements aout)
  }

  test("toArray(a: Array[T]) - arr is the same length or longer") {
    val al1  = new ArrayDeque[String](Seq("apple", "banana", "cherry").asJava)
    val ain  = Array.fill(4)("foo")
    val aout = al1.toArray(ain)
    assert(ain eq aout)
    assert(Array("apple", "banana", "cherry", null) sameElements aout)
  }

  test("toArray(Array[T]) - when T >: E") {
    class SuperClass
    class SubClass extends SuperClass
    val in   = Seq.fill(2)(new SubClass)
    val al1  = new ArrayDeque[SubClass](in.asJava)
    val aout = al1.toArray(Array.empty[SuperClass])
    assert(in.toArray sameElements aout)
  }

  testFails("toArray(a: Array[T]) - throws ArrayStoreException when not " +
              "T >: E",
            1694) {
    class NotSuperClass
    class SubClass

    locally { // This passes on Scala JVM
      val ad = new ArrayList[SubClass]()

      ad.toArray(Array.empty[NotSuperClass])
    }

    locally { // This is the case which is failing on ScalaNative.
      // The difference is that this Deque is not Empty.
      val ad = new ArrayDeque(Seq(new SubClass).asJava)

      assertThrows[ArrayStoreException] {
        ad.toArray(Array.empty[NotSuperClass])
      }
    }
  }
}
