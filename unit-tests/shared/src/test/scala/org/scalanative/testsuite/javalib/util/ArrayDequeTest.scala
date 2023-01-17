package org.scalanative.testsuite.javalib.util

import java.util._

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
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

import java.util.concurrent.ThreadLocalRandom

/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Ported from JSR 166 revision 1.138
 * https://gee.cs.oswego.edu/dl/concurrency-interest/index.html
 *
 */
class ArrayDequeJSR166Test {

  final val SIZE = 32
  def mustEqual(x: Int, y: Int) = assertEquals(x, y)
  def mustAdd(d: ArrayDeque[Integer], t: Int) = assertTrue(d.add(t))
  def mustRemove(d: ArrayDeque[Integer], t: Int) = assertTrue(d.remove(t))
  def mustNotRemove(d: ArrayDeque[Integer], t: Int) = assertFalse(d.remove(t))
  def mustContain(d: ArrayDeque[Integer], t: Int) = assertTrue(d.contains(t))
  def mustNotContain(d: ArrayDeque[Integer], t: Int) =
    assertFalse(d.contains(t))
  def itemFor(x: Int): Int = x
  def assertIteratorExhausted[T](it: Iterator[T]) =
    assertThrows(classOf[NoSuchElementException], it.next())
  val defaultItems = Array.tabulate(SIZE)(i => i)

  /** Returns a new deque of given size containing consecutive Items 0 ... n -
   *  \1.
   */
  private def populatedDeque(n: Int): ArrayDeque[Integer] = {
    // Randomize various aspects of memory layout, including
    // capacity slop and wraparound.
    val rnd = ThreadLocalRandom.current();
    val q = rnd.nextInt(6) match {
      case 0 => new ArrayDeque[Integer]()
      case 1 => new ArrayDeque[Integer](0)
      case 2 => new ArrayDeque[Integer](1)
      case 3 => new ArrayDeque[Integer](Math.max(0, n - 1))
      case 4 => new ArrayDeque[Integer](n)
      case 5 => new ArrayDeque[Integer](n + 1)
      case _ => throw new AssertionError()
    }
    (rnd.nextInt(3)) match {
      case 0 =>
        q.addFirst(42)
        mustEqual(42, q.removeLast())
      case 1 =>
        q.addLast(42)
        mustEqual(42, q.removeFirst())
      case 2 => /* do nothing */
      case _ => throw new AssertionError()
    }
    assertTrue(q.isEmpty())
    if (rnd.nextBoolean())
      for (i <- 0 until n)
        assertTrue(q.offerLast(itemFor(i)))
    else
      for (i <- (n - 1) to 0 by -1)
        q.addFirst(itemFor(i))
    mustEqual(n, q.size())
    if (n > 0) {
      assertFalse(q.isEmpty())
      mustEqual(0, q.peekFirst())
      mustEqual((n - 1), q.peekLast())
    }
    return q
  }

  /** new deque is empty
   */
  @Test def testConstructor1(): Unit = {
    mustEqual(0, new ArrayDeque[Int]().size())
  }

  /** Initializing from null Collection throws NPE
   */
  @Test def testConstructor3(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new ArrayDeque[Object](null: Collection[Object])
    )
  }

  /** Initializing from Collection of null elements throws NPE
   */
  @Test def testConstructor4(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new ArrayDeque[Integer](Arrays.asList(new Array[Integer](SIZE): _*))
    )
  }

  /** Initializing from Collection with some null elements throws NPE
   */
  @Test def testConstructor5(): Unit = {
    val items = new Array[Integer](2)
    items(0) = 0
    assertThrows(
      classOf[NullPointerException],
      new ArrayDeque(Arrays.asList(items: _*))
    )
  }

  /** Deque contains all elements of collection used to initialize
   */
  @Test def testConstructor6(): Unit = {
    val items = defaultItems
    val q = new ArrayDeque(Arrays.asList(items: _*))
    for (i <- 0 until SIZE)
      mustEqual(items(i), q.pollFirst())
  }

  /** isEmpty is true before add, false after
   */
  @Test def testEmpty(): Unit = {
    val q = new ArrayDeque[Int]()
    assertTrue(q.isEmpty());
    q.add(1);
    assertFalse(q.isEmpty());
    q.add(2);
    q.removeFirst();
    q.removeFirst();
    assertTrue(q.isEmpty());
  }

  /** size changes when elements added and removed
   */
  @Test def testSize(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(SIZE - i, q.size())
      q.removeFirst()
    }
    for (i <- 0 until SIZE) {
      mustEqual(i, q.size())
      mustAdd(q, i)
    }
  }

  /** push(null) throws NPE
   */
  @Test def testPushNull(): Unit = {
    val q = new ArrayDeque[Integer](1)
    assertThrows(classOf[NullPointerException], q.push(null))
  }

  /** peekFirst() returns element inserted with push
   */
  @Test def testPush(): Unit = {
    val q = populatedDeque(3)
    q.pollLast()
    q.push(4)
    assertSame(4, q.peekFirst())
  }

  /** pop() removes next element, or throws NSEE if empty
   */
  @Test def testPop(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.pop())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.pop()
    )
  }

  /** offer(null) throws NPE
   */
  @Test def testOfferNull(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.offer(null)
    )
  }

  /** offerFirst(null) throws NPE
   */
  @Test def testOfferFirstNull(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.offerFirst(null)
    )
  }

  /** offerLast(null) throws NPE
   */
  @Test def testOfferLastNull(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.offerLast(null)
    )
  }

  /** offer(x) succeeds
   */
  @Test def testOffer(): Unit = {
    val q = new ArrayDeque[Int]()
    assertTrue(q.offer(0))
    assertTrue(q.offer(1))
    assertSame(0, q.peekFirst())
    assertSame(1, q.peekLast())
  }

  /** offerFirst(x) succeeds
   */
  @Test def testOfferFirst(): Unit = {
    val q = new ArrayDeque[Int]()
    assertTrue(q.offerFirst(0))
    assertTrue(q.offerFirst(1))
    assertSame(1, q.peekFirst())
    assertSame(0, q.peekLast())
  }

  /** offerLast(x) succeeds
   */
  @Test def testOfferLast(): Unit = {
    val q = new ArrayDeque[Int]()
    assertTrue(q.offerLast(0))
    assertTrue(q.offerLast(1))
    assertSame(0, q.peekFirst())
    assertSame(1, q.peekLast())
  }

  /** add(null) throws NPE
   */
  @Test def testAddNull(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.add(null)
    )
  }

  /** addFirst(null) throws NPE
   */
  @Test def testAddFirstNull(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.addFirst(null)
    )
  }

  /** addLast(null) throws NPE
   */
  @Test def testAddLastNull(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.addLast(null)
    )
  }

  /** add(x) succeeds
   */
  @Test def testAdd(): Unit = {
    val q = new ArrayDeque[Int]()
    assertTrue(q.add(0))
    assertTrue(q.add(1))
    assertSame(0, q.peekFirst())
    assertSame(1, q.peekLast())
  }

  /** addFirst(x) succeeds
   */
  @Test def testAddFirst(): Unit = {
    val q = new ArrayDeque[Int]()
    q.addFirst(0)
    q.addFirst(1)
    assertSame(1, q.peekFirst())
    assertSame(0, q.peekLast())
  }

  /** addLast(x) succeeds
   */
  @Test def testAddLast(): Unit = {
    val q = new ArrayDeque[Int]()
    q.addLast(0)
    q.addLast(1)
    assertSame(0, q.peekFirst())
    assertSame(1, q.peekLast())
  }

  /** addAll(null) throws NPE
   */
  @Test def testAddAll1(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.addAll(null)
    )
  }

  /** addAll of a collection with null elements throws NPE
   */
  @Test def testAddAll2(): Unit = {
    val q = new ArrayDeque[Integer]()
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(new Array[Integer](SIZE): _*))
    )
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testAddAll3(): Unit = {
    val q = new ArrayDeque[Integer]()
    val items = new Array[Integer](2)
    items(0) = 0
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(new Array[Integer](SIZE): _*))
    )
  }

  /** Deque contains all elements, in traversal order, of successful addAll
   */
  @Test def testAddAll5(): Unit = {
    val empty = new Array[Int](0)
    val items = defaultItems
    val q = new ArrayDeque[Int]()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    for (i <- 0 until SIZE)
      mustEqual(items(i), q.pollFirst())
  }

  /** pollFirst() succeeds unless empty
   */
  @Test def testPollFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.pollFirst())
    }
    assertNull(q.pollFirst())
  }

  /** pollLast() succeeds unless empty
   */
  @Test def testPollLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- (SIZE - 1) to 0 by -1) {
      mustEqual(i, q.pollLast())
    }
    assertNull(q.pollLast())
  }

  /** poll() succeeds unless empty
   */
  @Test def testPoll(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.poll())
    }
    assertNull(q.poll())
  }

  /** remove() removes next element, or throws NSEE if empty
   */
  @Test def testRemove(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.remove())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.remove()
    )
  }

  /** remove(x) removes x and returns true if present
   */
  @Test def testRemoveElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 1 until SIZE by 2) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      mustContain(q, i - 1)
    }
    for (i <- 0 until SIZE by 2) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      mustNotRemove(q, i + 1)
      mustNotContain(q, i + 1)
    }
    assertTrue(q.isEmpty())
  }

  /** peekFirst() returns next element, or null if empty
   */
  @Test def testPeekFirst(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.peekFirst())
      mustEqual(i, q.pollFirst())
      assertTrue(
        q.peekFirst() == null ||
        q.peekFirst() != i
      )
    }
    assertNull(q.peekFirst())
  }

  /** peek() returns next element, or null if empty
   */
  @Test def testPeek(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.peek())
      mustEqual(i, q.poll())
      assertTrue(
        q.peek() == null ||
        q.peek() != i
      )
    }
    assertNull(q.peek())
  }

  /** peekLast() returns next element, or null if empty
   */
  @Test def testPeekLast(): Unit = {
    val q = populatedDeque(SIZE);
    for (i <- (SIZE - 1) to 0 by -1) {
      mustEqual(i, q.peekLast())
      mustEqual(i, q.pollLast())
      assertTrue(
        q.peekLast() == null ||
        q.peekLast() != i
      )
    }
    assertNull(q.peekLast())
  }

  /** element() returns first element, or throws NSEE if empty
   */
  @Test def testElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.element())
      mustEqual(i, q.poll())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.element()
    )
  }

  /** getFirst() returns first element, or throws NSEE if empty
   */
  @Test def testFirstElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustEqual(i, q.getFirst())
      mustEqual(i, q.pollFirst())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.getFirst()
    )
  }

  /** getLast() returns last element, or throws NSEE if empty
   */
  @Test def testLastElement(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- (SIZE - 1) to 0 by -1) {
      mustEqual(i, q.getLast())
      mustEqual(i, q.pollLast())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.getLast()
    )
    assertNull(q.peekLast())
  }

  /** removeFirst() removes first element, or throws NSEE if empty
   */
  @Test def testRemoveFirst(): Unit = {
    val q = populatedDeque(SIZE);
    for (i <- 0 until SIZE) {
      mustEqual(i, q.removeFirst())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.removeFirst()
    )
    assertNull(q.peekFirst())
  }

  /** removeLast() removes last element, or throws NSEE if empty
   */
  @Test def testRemoveLast(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- (SIZE - 1) to 0 by -1) {
      mustEqual(i, q.removeLast())
    }
    assertThrows(
      classOf[NoSuchElementException],
      q.removeLast()
    )
    assertNull(q.peekLast())
  }

  /** removeFirstOccurrence(x) removes x and returns true if present
   */
  @Test def testRemoveFirstOccurrence(): Unit = {
    var q = populatedDeque(SIZE)
    assertFalse(q.removeFirstOccurrence(null))
    for (i <- 1 until SIZE by 2) {
      assertTrue(q.removeFirstOccurrence(itemFor(i)))
      mustNotContain(q, i)
    }
    for (i <- 0 until SIZE by 2) {
      assertTrue(q.removeFirstOccurrence(itemFor(i)))
      assertFalse(q.removeFirstOccurrence(itemFor(i + 1)))
      mustNotContain(q, i)
      mustNotContain(q, i + 1)
    }
    assertTrue(q.isEmpty())
    assertFalse(q.removeFirstOccurrence(null))
    assertFalse(q.removeFirstOccurrence(42))
    q = new ArrayDeque[Integer]();
    assertFalse(q.removeFirstOccurrence(null))
    assertFalse(q.removeFirstOccurrence(42))
  }

  /** removeLastOccurrence(x) removes x and returns true if present
   */
  @Test def testRemoveLastOccurrence(): Unit = {
    var q = populatedDeque(SIZE);
    assertFalse(q.removeLastOccurrence(null));
    for (i <- 1 until SIZE by 2) {
      assertTrue(q.removeLastOccurrence(itemFor(i)))
      mustNotContain(q, i)
    }
    for (i <- 0 until SIZE by 2) {
      assertTrue(q.removeLastOccurrence(itemFor(i)))
      assertFalse(q.removeLastOccurrence(itemFor(i + 1)))
      mustNotContain(q, i)
      mustNotContain(q, i + 1)
    }
    assertTrue(q.isEmpty())
    assertFalse(q.removeLastOccurrence(null))
    assertFalse(q.removeLastOccurrence(42))
    q = new ArrayDeque[Integer]()
    assertFalse(q.removeLastOccurrence(null))
    assertFalse(q.removeLastOccurrence(42))
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains(): Unit = {
    val q = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      mustContain(q, i)
      mustEqual(i, q.pollFirst())
      mustNotContain(q, i)
    }
  }

  /** clear removes all elements
   */
  @Test def testClear(): Unit = {
    val q = populatedDeque(SIZE)
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    mustAdd(q, 1)
    assertFalse(q.isEmpty())
    q.clear()
    assertTrue(q.isEmpty())
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val q = populatedDeque(SIZE)
    val p = new ArrayDeque[Integer]()
    for (i <- 0 until SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if changed
   */
  @Test def testRetainAll(): Unit = {
    val q = populatedDeque(SIZE)
    val p = populatedDeque(SIZE)
    for (i <- 0 until SIZE) {
      val changed = q.retainAll(p)
      assertEquals(changed, (i > 0))
      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size())
      p.removeFirst()
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testRemoveAll(): Unit = {
    for (i <- 1 until SIZE) {
      val q = populatedDeque(SIZE)
      val p = populatedDeque(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size())
      for (j <- 0 until i) {
        mustNotContain(q, p.removeFirst());
      }
    }
  }

  def checkToArray(q: ArrayDeque[Integer]): Unit = {
    val size = q.size()
    val a1 = q.toArray()
    mustEqual(size, a1.length)
    val a2 = q.toArray(new Array[Integer](0))
    mustEqual(size, a2.length)
    val a3 = q.toArray(new Array[Integer](Math.max(0, size - 1)))
    mustEqual(size, a3.length)
    val a4 = new Array[Integer](size)
    assertSame(a4, q.toArray(a4))
    val a5 = Array.fill(size + 1)(Integer.valueOf(42))
    assertSame(a5, q.toArray(a5))
    val a6 = Array.fill(size + 2)(Integer.valueOf(42))
    assertSame(a6, q.toArray(a6))
    val as = Array(
      a1,
      a2.asInstanceOf[Array[Object]],
      a3.asInstanceOf[Array[Object]],
      a4.asInstanceOf[Array[Object]],
      a5.asInstanceOf[Array[Object]],
      a6.asInstanceOf[Array[Object]]
    )
    as.foreach { a =>
      if (a.length > size) assertNull(a(size))
      if (a.length > size + 1) assertEquals(42, a(size + 1))
    }
    val it = q.iterator()
    val s = q.peekFirst()
    for (i <- 0 until size) {
      val x = it.next()
      mustEqual(s + i, x)
      as.foreach { a =>
        assertSame(a(i), x)
      }
    }
  }

  /** toArray() and toArray(a) contain all elements in FIFO order
   */
  @Test def testToArray(): Unit = {
    val size = ThreadLocalRandom.current().nextInt(10)
    val q = new ArrayDeque[Integer](size)
    for (i <- 0 until size) {
      checkToArray(q)
      q.addLast(itemFor(i))
    }
    // Provoke wraparound
    val added = size * 2
    for (i <- 0 until added) {
      checkToArray(q)
      mustEqual(i, q.poll())
      q.addLast(itemFor(size + i))
    }
    for (i <- 0 until size) {
      checkToArray(q)
      mustEqual((added + i), q.poll())
    }
  }

  /** toArray(null) throws NullPointerException
   */
  @Test def testToArray_NullArg(): Unit = {
    val l = new ArrayDeque[Integer]()
    l.add(0)
    assertThrows(
      classOf[NullPointerException],
      l.toArray(null: Array[Object])
    )
  }

  /** Iterator iterates through all elements
   */
  @Test def testIterator(): Unit = {
    val q = populatedDeque(SIZE)
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    val c = new ArrayDeque[Integer]()
    assertIteratorExhausted(c.iterator())
    assertIteratorExhausted(c.descendingIterator())
  }

  /** Iterator ordering is FIFO
   */
  @Test def testIteratorOrdering(): Unit = {
    val q = new ArrayDeque[Integer]();
    q.add(1);
    q.add(2);
    q.add(3);
    var k = 0;
    val it = q.iterator()
    while (it.hasNext()) {
      k += 1
      mustEqual(k, it.next())
    }
    mustEqual(3, k)
  }

  /** iterator.remove() removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q = new ArrayDeque[Integer]()
    val rng = new Random()
    for (iters <- 0 until 100) {
      val max = rng.nextInt(5) + 2
      val split = rng.nextInt(max - 1) + 1
      for (j <- 1 to max)
        mustAdd(q, j)
      var it = q.iterator()
      for (j <- 1 to split)
        mustEqual(it.next(), j)
      it.remove()
      mustEqual(it.next(), split + 1)
      for (j <- 1 to split)
        q.remove(itemFor(j))
      it = q.iterator();
      for (j <- (split + 1) to max) {
        mustEqual(it.next(), j)
        it.remove()
      }
      assertFalse(it.hasNext())
      assertTrue(q.isEmpty())
    }
  }

  /** Descending iterator iterates through all elements
   */
  @Test def testDescendingIterator(): Unit = {
    val q = populatedDeque(SIZE)
    var i = 0
    val it = q.descendingIterator()
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertFalse(it.hasNext())
    assertThrows(
      classOf[NoSuchElementException],
      it.next()
    )
  }

  /** Descending iterator ordering is reverse FIFO
   */
  @Test def testDescendingIteratorOrdering(): Unit = {
    val q = new ArrayDeque[Integer]()
    for (iters <- 0 until 100) {
      q.add(3);
      q.add(2);
      q.add(1);
      var k = 0;
      val it = q.descendingIterator()
      while (it.hasNext()) {
        k += 1
        mustEqual(k, it.next())
      }

      mustEqual(3, k)
      q.remove()
      q.remove()
      q.remove()
    }
  }

  /** descendingIterator.remove() removes current element
   */
  @Test def testDescendingIteratorRemove(): Unit = {
    val q = new ArrayDeque[Integer]()
    val rng = new Random()
    for (iter <- 0 until 100) {
      val max = rng.nextInt(5) + 2
      val split = rng.nextInt(max - 1) + 1
      for (j <- max to 1 by -1)
        q.add(itemFor(j))
      var it = q.descendingIterator()
      for (j <- 1 to split)
        mustEqual(it.next(), itemFor(j))
      it.remove()
      mustEqual(it.next(), itemFor(split + 1))
      for (j <- 1 to split)
        q.remove(itemFor(j))
      it = q.descendingIterator()
      for (j <- (split + 1) to max) {
        mustEqual(it.next(), j)
        it.remove()
      }
      assertFalse(it.hasNext())
      assertTrue(q.isEmpty())
    }
  }

  /** toString() contains toStrings of elements
   */
  @Test def testToString(): Unit = {
    val q = populatedDeque(SIZE)
    val s = q.toString()
    for (i <- 0 until SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
    }
  }

  /** A cloned deque has same elements in same order
   */
  @Test def testClone(): Unit = {
    val x = populatedDeque(SIZE)
    val y = x.clone()

    assertNotSame(y, x)
    mustEqual(x.size(), y.size())
    assertEquals(x.toString(), y.toString())
    assertTrue(Arrays.equals(x.toArray(), y.toArray()))
    while (!x.isEmpty()) {
      assertFalse(y.isEmpty())
      mustEqual(x.remove(), y.remove())
    }
    assertTrue(y.isEmpty())
  }

  /** remove(null), contains(null) always return false
   */
  @Test def testNeverContainsNull(): Unit = {
    val qs = Array(
      new ArrayDeque[Integer](),
      populatedDeque(2)
    )

    for (q <- qs) {
      assertFalse(q.contains(null))
      assertFalse(q.remove(null))
      assertFalse(q.removeFirstOccurrence(null))
      assertFalse(q.removeLastOccurrence(null))
    }
  }

  /** Spliterator.getComparator always throws IllegalStateException
   */
  @Test def testSpliterator_getComparator(): Unit = {
    assertThrows(
      classOf[IllegalStateException],
      new ArrayDeque[Integer]().spliterator().getComparator()
    )
  }

  /** Spliterator characteristics are as advertised
   */
  @Test def testSpliterator_characteristics(): Unit = {
    val q = new ArrayDeque[Integer]()
    val s = q.spliterator()
    val characteristics = s.characteristics()
    val required =
      Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED
    mustEqual(required, characteristics & required)
    assertTrue(s.hasCharacteristics(required))
    mustEqual(
      0,
      characteristics
        & (Spliterator.CONCURRENT
          | Spliterator.DISTINCT
          | Spliterator.IMMUTABLE
          | Spliterator.SORTED)
    );
  }

}
