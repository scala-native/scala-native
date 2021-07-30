package javalib.util

import java.util._

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import scala.scalanative.junit.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.CollectionConverters._

class ArrayDequeTest {

  @Test def constructor(): Unit = {
    val ad = new ArrayDeque()

    assertTrue("Constructor returned null", ad != null)

    // There is no good way to check underlying capacity, which should
    // be 16.

    assertTrue("constructed ArrayDeque() is not empty", ad.isEmpty())

    val resultSize = ad.size
    val expectedSize = 0
    assertTrue(
      s"size: ${resultSize} != expected: ${expectedSize}",
      resultSize == expectedSize
    )
  }

  @Test def constructorInitialCapacityMinusCapacityGreaterThan0(): Unit = {
    val ad = new ArrayDeque(20)

    assertTrue("Constructor returned null", ad != null)

    // There is no good way to check underlying capacity, which should
    // be 20.

    assertTrue("constructed ArrayDeque() is not empty", ad.isEmpty())

    val resultSize = ad.size
    val expectedSize = 0
    assertTrue(
      s"size: ${resultSize} != expected: ${expectedSize}",
      resultSize == expectedSize
    )
  }

  @Test def constructorInitialCapacityMinuCapacityLessThanZero0(): Unit = {
    // This test basically tests that no exception is thrown
    // when the initialCapacity is negative, implementing JVM behavior.

    val ad = new ArrayDeque(-1)

    assertTrue("Constructor returned null", ad != null)

    // There is no good way to check underlying capacity, which should
    // be 20.

    assertTrue("constructed ArrayDeque() is not empty", ad.isEmpty())

    val resultSize = ad.size
    val expectedSize = 0
    assertTrue(
      s"size: ${resultSize} != expected: ${expectedSize}",
      resultSize == expectedSize
    )
  }

  @Test def constructorNull(): Unit = {
    assertThrows(classOf[NullPointerException], new ArrayDeque(null))
  }

  @Test def constructorCollectionInteger(): Unit = {
    // for AnyVal
    val is = Seq(1, 2, 3)
    val ad = new ArrayDeque(is.toJavaList)
    assertTrue("a1", ad.size() == 3)
    assertFalse("a2", ad.isEmpty())

    val result = ad.toArray
    val expected = is.toArray
    assertTrue(
      s"element: ${result} != expected: ${expected})",
      result.sameElements(expected)
    )
  }

  @Test def constructorCollectionString(): Unit = {
    // for AnyRef
    val is = Seq(1, 2, 3).map(_.toString)
    val ad = new ArrayDeque(is.toJavaList)
    assertTrue("a1", ad.size() == 3)
    assertFalse("a2", ad.isEmpty())

    val result = ad.toArray
    val expected = is.toArray

    assertTrue(
      s"element: ${result} != expected: ${expected})",
      result.sameElements(expected)
    )
  }

  @Test def addElementMinusTriggerCapacityChange(): Unit = {
    // Simple add()s are triggered by the addAll() in the previous
    // ArrayDesueue(constructor) test. Exercise a more complex code path.
    // Code should not fail when it resizes when adding the 17th element.

    val max = 20 // Must be > 16
    val is = 1 to 20
    val ad = new ArrayDeque[Int]()

    for (e <- is) {
      ad.add(e)
    }

    for (e <- is) {
      val result = ad.removeFirst()
      val expected = e
      assertTrue(
        s"element: ${result} != expected: ${expected}",
        result == expected
      )
    }
  }

  @Test def addFirstNull(): Unit = {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows(
        classOf[NullPointerException],
        ad.addFirst(null.asInstanceOf[E])
      )
    }

    locally {
      val is = Seq(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.add(is(0))
      ad.addFirst(is(1))

      val result = ad.toArray
      val expected = is.reverse.toArray

      assertTrue(
        s"result: ${ad.toString} != " +
          s"expected: ${expected.mkString("[", ", ", "]")}",
        result.sameElements(expected)
      )
    }
  }

  @Test def addLastNull(): Unit = {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows(
        classOf[NullPointerException],
        ad.addLast(null.asInstanceOf[E])
      )
    }

    locally {
      val expected = Array(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.add(expected(0))
      ad.addLast(expected(1))

      val result = ad.toArray

      assertTrue(
        s"result: ${ad.toString} != " +
          s"expected: ${expected.mkString("[", ", ", "]")}",
        result.sameElements(expected)
      )
    }
  }

  @Test def clear(): Unit = {
    val ad1 = new ArrayDeque(Seq(1, 2, 3, 2).toJavaList)
    ad1.clear()
    assertTrue(ad1.isEmpty())
    // makes sure that clear()ing an already empty list is safe.
    ad1.clear()
  }

  @Test def testClone(): Unit = {
    val ad1 = new ArrayDeque(Seq(1, 2, 3, 2).toJavaList)
    val ad2 = ad1.clone()

    val element = 1

    assertTrue("must be different objects", !ad1.eq(ad2))
    assertTrue("must have same contents", ad1.toString == ad2.toString)

    ad1.add(element)
    assertTrue("must have different contents", ad1.toString != ad2.toString)
    ad2.add(element)
    assertTrue("must have same contents", ad1.toString == ad2.toString)
  }

  @Test def containsAny(): Unit = {
    val needle = Math.PI
    val is = Seq(1.1, 2.2, 3.3, needle, 4.0)
    val ad = new ArrayDeque(is.toJavaList)

    val result = ad.contains(needle)
    assertTrue(s"'${ad.toString}' does not contain '${needle}'", result)
  }

  @Test def descendingIterator(): Unit = {
    // No good way on single threaded ScalaNative to test for
    // ConcurrentModificationException

    val is = Seq(1, 2, 3)
    val ad = new ArrayDeque(is.toJavaList)

    val result = ad.descendingIterator.toScalaSeq.toArray
    val expected = is.reverse.toArray

    assertTrue(
      s"element: result} != expected: ${expected})",
      result.sameElements(expected)
    )
  }

  @Test def element(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.getFirst())
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.element

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def getFirst(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.getFirst())
    }

    locally {
      val is = Seq("33", "22", "11")
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.getFirst

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def getLast(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.getFirst())
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.getLast

      val expected = is.last

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  // @Test def isEmpty()") exercised in ArrayDeque constructors

  @Test def iterator(): Unit = {
    // No good way on single threaded ScalaNative to test for
    // ConcurrentModificationException

    val is = Seq(-11, 0, 1)
    val ad = new ArrayDeque(is.toJavaList)

    val result = ad.iterator.toScalaSeq.toArray
    val expected = is.toArray

    assertTrue(
      s"element: ${result} != expected: ${expected})",
      result.sameElements(expected)
    )
  }

  @Test def offerNull(): Unit = {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows(
        classOf[NullPointerException],
        ad.offer(null.asInstanceOf[E])
      )
    }

    locally {
      val expected = Array(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.offer(expected(0))
      ad.offer(expected(1))

      val result = ad.toArray

      assertTrue(
        s"result: ${ad.toString} != " +
          s"expected: ${expected.mkString("[", ", ", "]")}",
        result.sameElements(expected)
      )
    }
  }

  @Test def offerFirstNull(): Unit = {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows(
        classOf[NullPointerException],
        ad.offerFirst(null.asInstanceOf[E])
      )
    }

    locally {
      val is = Seq(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.offer(is(0))
      ad.offerFirst(is(1))

      val result = ad.toArray
      val expected = is.reverse.toArray

      assertTrue(
        s"result: ${ad.toString} != " +
          s"expected: ${expected.mkString("[", ", ", "]")}",
        result.sameElements(expected)
      )
    }
  }

  @Test def offerLastNull(): Unit = {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows(
        classOf[NullPointerException],
        ad.offerLast(null.asInstanceOf[E])
      )
    }

    locally {
      val expected = Array(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.offerLast(expected(0))
      ad.offerLast(expected(1))

      val result = ad.toArray

      assertTrue(
        s"result: ${ad.toString} != " +
          s"expected: ${expected.mkString("[", ", ", "]")}",
        result.sameElements(expected)
      )
    }
  }

  @Test def peek(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertTrue(
        "expected null from peek() with empty ArrayDeque",
        ad.peek == null
      )
    }

    locally {
      val is = Seq("33", "22", "11")
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.peek

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def peekFirst(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertTrue(
        "expected null from peekFirst() with empty ArrayDeque",
        ad.peekFirst == null
      )
    }

    locally {
      val is = Seq("33", "22", "11")
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.peekFirst

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def peekLast(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertTrue(
        "expected null from peekFirst() with empty ArrayDeque",
        ad.peekLast == null
      )
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.peekLast

      val expected = is.last

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def poll(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertTrue(
        "expected null from poll() with empty ArrayDeque",
        ad.poll == null
      )
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.poll

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def pollFirst(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertTrue(
        "expected null from pollFirst() with empty ArrayDeque",
        ad.pollFirst == null
      )
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.pollFirst

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def pollLast(): Unit = {
    locally {
      val ad = new ArrayDeque()
      assertTrue(
        s"expected null from pollLast() with empty ArrayDeque",
        ad.pollLast == null
      )
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.pollLast

      val expected = is.last

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def pop(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.pop())
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.pop

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def pushNull(): Unit = {
    locally {
      type E = AnyRef
      val ad = new ArrayDeque[E]()

      assertThrows(classOf[NullPointerException], ad.push(null.asInstanceOf[E]))
    }

    locally {
      val is = Seq(-1, -2)
      val ad = new ArrayDeque[Int]()

      ad.add(is(0))
      ad.push(is(1))

      val result = ad.toArray
      val expected = is.reverse.toArray

      assertTrue(
        s"result: ${ad.toString} != " +
          s"expected: ${expected.mkString("[", ", ", "]")}",
        result.sameElements(expected)
      )
    }
  }

  @Test def remove(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.remove())
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.remove

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def removeAny(): Unit = {
    val haystack = "Looking for a needle in a haystack"
    val words = haystack.split(" ").toSeq
    val ad = new ArrayDeque(words.toJavaList)

    locally {
      val adClone = ad.clone()
      val adCloneStr = adClone.toString

      assertTrue(
        "deque and its clone must have same contents",
        ad.toString == adClone.toString
      )

      val beforeSize = ad.size
      val needle = "sharp"

      val result = ad.remove(needle)

      assertFalse(s"word '${needle}' found in string '${haystack}'", result)

      // Show deque has not changed

      val afterSize = ad.size
      val expectedSize = beforeSize

      assertTrue(
        s"size: ${afterSize} != expected: ${beforeSize}",
        afterSize == expectedSize
      )

      val adStr = ad.toString
      assertTrue(
        "deque: ${adStr} != expected: '${adCloneStr}'",
        ad.toString == adCloneStr
      )
    }

    locally {
      val needle = "needle"
      val beforeSize = ad.size

      val result = ad.remove(needle)

      assertTrue(s"word '${needle}' not found in string '${haystack}'", result)

      // Show deque changed as expected.

      val afterSize = ad.size
      val expectedSize = beforeSize - 1

      assertTrue(
        s"size: ${afterSize} != expected: ${beforeSize}",
        afterSize == expectedSize
      )

      val adStr = ad.toString

      assertFalse(
        "deque: ${adStr} must not contain '${needle}'",
        ad.toString.contains(needle)
      )
    }
  }

  @Test def removeFirst(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.removeFirst())
    }

    locally {
      val is = Seq(33, 22, 11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.removeFirst

      val expected = is.head

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def removeFirstOccurrenceAny(): Unit = {
    val haystack = "Square needle || round needle || shiny needle"
    val words = haystack.split(" ").toSeq
    val ad = new ArrayDeque(words.toJavaList)

    locally {
      val adClone = ad.clone()
      val adCloneStr = adClone.toString

      assertTrue(
        "deque and its clone must have same contents",
        ad.toString == adClone.toString
      )

      val beforeSize = ad.size
      val needle = "sharp"

      val result = ad.removeFirstOccurrence(needle)

      assertFalse(s"word '${needle}' found in string '${haystack}'", result)

      // Show deque has not changed

      val afterSize = ad.size
      val expectedSize = beforeSize

      assertTrue(
        s"size: ${afterSize} != expected: ${beforeSize}",
        afterSize == expectedSize
      )

      val adStr = ad.toString
      assertTrue(
        "deque: ${adStr} != expected: '${adCloneStr}'",
        ad.toString == adCloneStr
      )
    }

    locally {
      val needle = "needle"
      val beforeSize = ad.size

      val result = ad.removeFirstOccurrence(needle)

      assertTrue(s"word '${needle}' not found in string '${haystack}'", result)

      // Show deque changed as expected.

      val afterSize = ad.size
      val expectedSize = beforeSize - 1

      assertTrue(
        s"size: ${afterSize} != expected: ${beforeSize}",
        afterSize == expectedSize
      )

      for (i <- 0 until words.length if i != 1) {
        val result = ad.removeFirst
        val expected = words(i)
        assertTrue(
          "deque(${i}): ${result} != expected: '${expected}'",
          result == expected
        )
      }
    }
  }

  @Test def removeLast(): Unit = {
    locally {
      val ad = new ArrayDeque()

      assertThrows(classOf[NoSuchElementException], ad.removeLast())
    }

    locally {
      val is = Seq(-33, -22, -11)
      val ad = new ArrayDeque(is.toJavaList)

      val result = ad.removeLast

      val expected = is.last

      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )

      val afterSize = ad.size
      val expectedSize = is.size - 1
      assertTrue(
        s"after size: ${afterSize} != expected: ${expectedSize}",
        afterSize == expectedSize
      )
    }
  }

  @Test def removeLastOccurrenceAny(): Unit = {
    val haystack = "Square needle || round needle || shiny needle"
    val words = haystack.split(" ").toSeq
    val ad = new ArrayDeque(words.toJavaList)

    locally {
      val adClone = ad.clone()
      val adCloneStr = adClone.toString

      assertTrue(
        "deque and its clone must have same contents",
        ad.toString == adClone.toString
      )

      val beforeSize = ad.size
      val needle = "sharp"

      val result = ad.removeLastOccurrence(needle)

      assertFalse(s"word '${needle}' found in string '${haystack}'", result)

      // Show deque has not changed

      val afterSize = ad.size
      val expectedSize = beforeSize

      assertTrue(
        s"size: ${afterSize} != expected: ${beforeSize}",
        afterSize == expectedSize
      )

      val adStr = ad.toString
      assertTrue(
        "deque: ${adStr} != expected: '${adCloneStr}'",
        ad.toString == adCloneStr
      )
    }

    locally {
      val needle = "needle"
      val beforeSize = ad.size

      val result = ad.removeLastOccurrence(needle)

      assertTrue(s"word '${needle}' not found in string '${haystack}'", result)

      // Show deque changed as expected.

      val afterSize = ad.size
      val expectedSize = beforeSize - 1

      assertTrue(
        s"size: ${afterSize} != expected: ${beforeSize}",
        afterSize == expectedSize
      )

      for (i <- 0 until (words.length - 1)) {
        val result = ad.removeFirst
        val expected = words(i)
        assertTrue(
          "deque(${i}): ${result} != expected: '${expected}'",
          result == expected
        )
      }
    }
  }

  @Test def size(): Unit = {
    // exercised in ArrayDeque constructors
  }

  @Test def toArray(): Unit = {
    // exercised in ArrayDeque constructors
  }

  @Test def toArrayNullThrowsNullPointerException(): Unit = {
    val al1 =
      new ArrayDeque[String](Seq("apple", "banana", "cherry").toJavaList)
    assertThrows(classOf[NullPointerException], al1.toArray(null))
  }

  @Test def toArrayArrayMinusArrayIsShorter(): Unit = {
    val al1 =
      new ArrayDeque[String](Seq("apple", "banana", "cherry").toJavaList)
    val ain = Array.empty[String]
    val aout = al1.toArray(ain)
    assertTrue(ain ne aout)
    assertTrue(Array("apple", "banana", "cherry") sameElements aout)
  }

  @Test def toArrayArrayMinusArrayIsTheSameLengthOrLonger(): Unit = {
    val al1 =
      new ArrayDeque[String](Seq("apple", "banana", "cherry").toJavaList)
    val ain = Array.fill(4)("foo")
    val aout = al1.toArray(ain)
    assertTrue(ain eq aout)
    assertTrue(Array("apple", "banana", "cherry", null) sameElements aout)
  }

  @Test def toArrayArrayWhenSuperClass(): Unit = {
    class SuperClass
    class SubClass extends SuperClass
    val in = Seq.fill(2)(new SubClass)
    val al1 = new ArrayDeque[SubClass](in.toJavaList)
    val aout = al1.toArray(Array.empty[SuperClass])
    assertTrue(in.toArray sameElements aout)
  }

  @Ignore("#1694")
  @Test def toArrayArrayThrowsArrayStoreExceptionWhenNotSuperClass(): Unit = {
    class NotSuperClass
    class SubClass

    locally { // This passes on Scala JVM
      val ad = new ArrayList[SubClass]()

      ad.toArray(Array.empty[NotSuperClass])
    }

    locally { // This is the case which is failing on ScalaNative.
      // The difference is that this Deque is not Empty.
      val ad = new ArrayDeque(Seq(new SubClass).toJavaList)

      assertThrows(
        classOf[ArrayStoreException],
        ad.toArray(Array.empty[NotSuperClass])
      )
    }
  }
}
