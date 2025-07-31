package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.{lang => jl}
import java.{util => ju}

/* Guiding principle:
 *   If it is not tested somehow it is, by definition, broken.
 *   The goal is to asymptotically approach full coverage.
 *
 *   In the interim, some elementary methods do not have a formal test but are
 *   used in Tests for other methods, making failures evident, but harder
 *   to localize and/or problem solve.
 *
 *   SequencedCollection and descendent methods are directly tested but some
 *   non-JEP431 baseline Collections methods, such as 'contains()' are not.
 *   Someday coverage could be extended to those.
 */

private[util] object ReverseOrder {
  // Note carefully the scala.Char to AnyRef Character and back conversions.

  def stringToSequenced[T <: ju.SequencedCollection[Character]](
      string: String,
      receiver: T
  ): T = {
    val charArray = string.toCharArray()
    for (j <- 0 until string.length())
      receiver.add(Character.valueOf(charArray(j)))

    receiver
  }

  def charactersToString[T <: ju.SequencedCollection[Character]](
      characters: T
  ): String = {
    val iter = characters.iterator()
    val sb = new StringBuilder(characters.size())

    while (iter.hasNext())
      sb.append(iter.next())

    sb.toString()
  }
}

// ReverseOrderDeque ----------------------------------------------------

trait JEP431_ReverseOrderDequeTestTraitOnJDK21 {

  import ReverseOrder.{charactersToString, stringToSequenced}

  def dequeFactory[E](): ju.Deque[E] // Abstract
  def dequeFactory[E](initialCapacityHint: Int): ju.Deque[E]

  /* Given simple names, some Tests , such as add(), addFirst(), etc. would be
   * listed in both this trait and in ReverseOrderListTestTrait.
   * Give them a prefix to avoid conflict and ensure that both variants
   * are run, especially in LinkedList.
   */

  @Test def dequeAdd(): Unit = {
    val forward = dequeFactory[Int]()
    val reversed = forward.reversed()

    assertTrue("reversed add", reversed.add(41))

    assertEquals(
      "reversed size after add",
      1,
      reversed.size()
    )
  }

  @Test def dequeAddAll(): Unit = {
    val dequeData = "abcdefgh"
    val collectionData = "0123456789"
    val afterAddAllForwardData = "9876543210abcdefgh"

    val forward = stringToSequenced(dequeData, dequeFactory[Character]())
    val collection =
      ju.Arrays
        .stream(collectionData.toCharArray().map(c => Character.valueOf(c)))
        .toList()

    val startingSize = forward.size()
    val expectedChangedSize = startingSize + collection.size()

    val reversed = forward.reversed()

    assertTrue("addAll() insertion", reversed.addAll(collection))

    assertEquals("reversed size", expectedChangedSize, reversed.size())

    // forward size and content also changed.
    assertEquals("forward size", expectedChangedSize, forward.size())

    assertEquals(
      "forward content",
      afterAddAllForwardData,
      charactersToString(forward)
    )
  }

  @Test def dequeAddFirst(): Unit = {
    // Greek lowercase ζ (zeta), obviously visually different from ASCII
    val expected = Character.valueOf('\u03B6')

    val forwardData = "abcdefgh"
    val afterAddFirstForwardData = s"${forwardData}${expected}"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    reversed.addFirst(expected)

    assertEquals(
      "reversed size after addFirst",
      afterAddFirstForwardData.length(),
      reversed.size()
    )

    assertEquals(
      "forward content",
      afterAddFirstForwardData,
      charactersToString(forward)
    )
  }

  @Test def dequeAddLast(): Unit = {
    // Greek lowercase ζ (zeta), obviously visually different from ASCII
    val expected = Character.valueOf('\u03B6')

    val forwardData = "abcdefgh"
    val afterAddForwardData = s"${expected}${forwardData}"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    reversed.addLast(expected)

    assertEquals(
      "reversed size after addLast",
      afterAddForwardData.length(),
      reversed.size()
    )

    assertEquals(
      "forward content",
      afterAddForwardData,
      charactersToString(forward)
    )
  }

  @Test def descendingIterator(): Unit = {
    val expectedSize = 20

    val forward = dequeFactory[Int](expectedSize)

    for (j <- 1 to forward.size())
      forward.add(j)

    val revIter = forward.reversed().descendingIterator()

    var index = 0

    while (revIter.hasNext()) {
      assertEquals(
        s"iterator pos: ${index}",
        index,
        revIter.next()
      )
      index += 1
    }
  }

  @Test def element(): Unit = {
    val forwardData = "abcdefgH"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed element", expected, reversed.element())
  }

  @Test def dequeGetFirst(): Unit = {
    val expected = -2

    val forward = dequeFactory[Int]()

    forward.add(0)
    forward.add(2)
    forward.add(expected)

    val reversed = forward.reversed()
    val result = reversed.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def dequeGetLast(): Unit = {
    val expected = -9

    val forward = dequeFactory[Int]()

    forward.add(expected)
    forward.add(0)
    forward.add(2)

    val reversed = forward.reversed()
    val result = reversed.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def dequeIterator(): Unit = {
    val maxStep = 9

    val forward = dequeFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 0 to maxStep)
      forward.add(j)

    val reversedIter = reversed.iterator()

    for (j <- maxStep to 0 by -1)
      assertEquals(s"reversed iterator content: ${j}", j, reversedIter.next())
  }

  @Test def offer(): Unit = {
    // Greek lowercase ζ (zeta), obviously visually different from ASCII
    val expected = Character.valueOf('\u03B6')

    val forwardData = "abcdefgh"
    val afterOfferForwardData = s"${expected}${forwardData}"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertTrue("reversed offer", reversed.offer(expected))

    assertEquals(
      "reversed size after offer",
      afterOfferForwardData.length(),
      reversed.size()
    )

    assertEquals(
      "forward content",
      afterOfferForwardData,
      charactersToString(forward)
    )
  }

  @Test def offerFirst(): Unit = {
    // Greek lowercase ζ (zeta), obviously visually different from ASCII
    val expected = Character.valueOf('\u03B6')

    val forwardData = "abcdefgh"
    val afterOfferFirstForwardData = s"${forwardData}${expected}"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertTrue("reversed offerLast", reversed.offerFirst(expected))

    assertEquals(
      "reversed size after offerFirst",
      afterOfferFirstForwardData.length(),
      reversed.size()
    )

    assertEquals(
      "forward content",
      afterOfferFirstForwardData,
      charactersToString(forward)
    )
  }

  @Test def offerLast(): Unit = {
    // Greek lowercase ζ (zeta), obviously visually different from ASCII
    val expected = Character.valueOf('\u03B6')

    val forwardData = "abcdefgh"
    val afterOfferForwardData = s"${expected}${forwardData}"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertTrue("reversed offerLast", reversed.offerLast(expected))

    assertEquals(
      "reversed size after offerLast",
      afterOfferForwardData.length(),
      reversed.size()
    )

    assertEquals(
      "forward content",
      afterOfferForwardData,
      charactersToString(forward)
    )
  }

  @Test def peek(): Unit = {
    val forwardData = "abcdefg"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed peek", expected, reversed.peek())
    assertEquals(
      "reversed peek size",
      forwardData.length(),
      reversed.size()
    )
  }

  @Test def peekFirst(): Unit = {
    val forwardData = "abcdefg"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed peekFirst", expected, reversed.peekFirst())

    assertEquals(
      "reversed peekFirst size",
      forwardData.length(),
      reversed.size()
    )
  }

  @Test def peekLast(): Unit = {
    val forwardData = "abcdefg"
    val expected = forwardData(0)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed peekLast", expected, reversed.peekLast())

    assertEquals(
      "reversed peekLast size",
      forwardData.length(),
      reversed.size()
    )
  }

  @Test def poll(): Unit = {
    val forwardData = "abcdefghij"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed poll", expected, reversed.poll())

    assertEquals(
      "reversed poll size",
      forwardData.length() - 1,
      reversed.size()
    )
  }

  @Test def pollFirst(): Unit = {
    val forwardData = "abcdef"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed pollFirst", expected, reversed.pollFirst())

    assertEquals(
      "reversed pollFirst size",
      forwardData.length() - 1,
      reversed.size()
    )
  }

  @Test def pollLast(): Unit = {
    val forwardData = "abcdef"
    val expected = forwardData(0)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed pollLast", expected, reversed.pollLast())

    assertEquals(
      "reversed pollLast size",
      forwardData.length() - 1,
      reversed.size()
    )
  }

  @Test def pop(): Unit = {
    val forwardData = "abcdef"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed pop", expected, reversed.pop())

    assertEquals(
      "reversed pop size",
      forwardData.length() - 1,
      reversed.size()
    )
  }

  @Test def push(): Unit = {
    // Greek lowercase ζ (zeta), obviously visually different from ASCII
    val expected = Character.valueOf('\u03B6')

    val forwardData = "abcdefgh"
    val afterAddFirstForwardData = s"${forwardData}${expected}"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    reversed.push(expected)

    assertEquals(
      "reversed size after push",
      afterAddFirstForwardData.length(),
      reversed.size()
    )

    assertEquals(
      "forward content",
      afterAddFirstForwardData,
      charactersToString(forward)
    )
  }

  @Test def dequeRemove(): Unit = {
    // Same logic as dequeRemoveFirst
    val forwardData = "abcdef"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed remove", expected, reversed.remove())

    assertEquals(
      "reversed remove size",
      forwardData.length() - 1,
      reversed.size()
    )

    assertThrows(
      "reversed remove() empty deque",
      classOf[NoSuchElementException],
      dequeFactory[Character]().reversed().remove()
    )
  }

  @Test def dequeRemoveFirst(): Unit = {
    // Same logic as dequeRemove
    val forwardData = "abcdefQ"
    val expected = forwardData(forwardData.length() - 1)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed remove", expected, reversed.removeFirst())

    assertEquals(
      "reversed removeFirst size",
      forwardData.length() - 1,
      reversed.size()
    )

    assertThrows(
      "reversed removeFirst empty deque",
      classOf[NoSuchElementException],
      dequeFactory[Character]().reversed().removeFirst()
    )
  }

  @Test def remove_Object(): Unit = {
    val boat = 'B'

    val beforeRemoveFirstOccurrenceForwardData = "sssssBsssBssss"
    val afterRemoveFirstOccurrenceForwardData = "sssssBsssssss"

    val forward = stringToSequenced(
      beforeRemoveFirstOccurrenceForwardData,
      dequeFactory[Character]()
    )

    val reversed = forward.reversed()

    assertTrue(
      "remove(boat)",
      reversed.remove(boat)
    )

    assertEquals(
      "reversed remove size",
      afterRemoveFirstOccurrenceForwardData.length(),
      reversed.size()
    )

    // remove() made intended changes, but no other changes.
    assertEquals(
      "forward content",
      afterRemoveFirstOccurrenceForwardData,
      charactersToString(forward)
    )
  }

  @Test def removeFirstOccurrence(): Unit = {
    val goat = 'G'

    val beforeRemoveFirstOccurrenceForwardData = "sssssGsssGssss"
    val afterRemoveFirstOccurrenceForwardData = "sssssGsssssss"

    val forward = stringToSequenced(
      beforeRemoveFirstOccurrenceForwardData,
      dequeFactory[Character]()
    )

    val reversed = forward.reversed()

    assertTrue(
      "removeFirstOccurrence(goat)",
      reversed.removeFirstOccurrence(goat)
    )

    assertEquals(
      "reversed removeFirstOccurrence size",
      afterRemoveFirstOccurrenceForwardData.length(),
      reversed.size()
    )

    // removeFirstOccurance() made intended changes, but no other changes.
    assertEquals(
      "forward content",
      afterRemoveFirstOccurrenceForwardData,
      charactersToString(forward)
    )
  }

  @Test def dequeRemoveLast(): Unit = {
    val forwardData = "abcdef"
    val expected = forwardData(0)

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed removeLast", expected, reversed.removeLast())

    assertEquals(
      "reversed removeLast size",
      forwardData.length() - 1,
      reversed.size()
    )

    assertThrows(
      "reversed removeLast empty deque",
      classOf[NoSuchElementException],
      dequeFactory[Character]().reversed().removeLast()
    )
  }

  @Test def removeLastOccurrence(): Unit = {
    val goat = 'G'

    val beforeRemoveLastOccurrenceForwardData = "sssssGsssGssss"
    val afterRemoveLastOccurrenceForwardData = "ssssssssGssss"

    val forward = stringToSequenced(
      beforeRemoveLastOccurrenceForwardData,
      dequeFactory[Character]()
    )

    val reversed = forward.reversed()

    assertTrue(
      "removeLastOccurrence(goat)",
      reversed.removeLastOccurrence(goat)
    )

    assertEquals(
      "reversed removeLastOccurrence size",
      afterRemoveLastOccurrenceForwardData.length(),
      reversed.size()
    )

    // removeLastOccurance() made intended changes, but no other changes.
    assertEquals(
      "forward content",
      afterRemoveLastOccurrenceForwardData,
      charactersToString(forward)
    )
  }

  @Test def dequeSize(): Unit = {
    val forwardData = "abcdefghijklmn"

    val forward = stringToSequenced(forwardData, dequeFactory[Character]())
    val reversed = forward.reversed()

    assertEquals("reversed size", forwardData.length(), reversed.size())

    assertEquals(
      "reversed size, empty",
      0,
      dequeFactory[Character]().reversed().size()
    )
  }
}

class JEP431_ReverseOrderDequeTestOnJDK21
    extends JEP431_ReverseOrderDequeTestTraitOnJDK21 {

  // Use a Deque type other than LinkedList. LinkedList is complicated by List
  def dequeFactory[E](): ju.Deque[E] =
    dequeFactory[E](0)

  def dequeFactory[E](initialCapacityHint: Int): ju.Deque[E] =
    new ju.ArrayDeque[E](initialCapacityHint)
}

// ReverseOrderList -----------------------------------------------------

trait JEP431_ReverseOrderListTestTraitOnJDK21 {

  import ReverseOrder.{charactersToString, stringToSequenced}

  /* Use [E], not [E <: AnyRef].  Rely upon Scala boxing & unboxing,
   * where needed This allows Testing likely usage in the field.
   */

  def listFactory[E](initialCapacityHint: Int): ju.List[E] // Abstract

  def listFactory[E](): ju.List[E] = listFactory[E](0)

  def listFactory[E](c: java.util.Collection[E]): ju.List[E] = {
    val list = listFactory[E](c.size())
    val iter = c.iterator()
    while (iter.hasNext())
      list.addLast(iter.next())
    list
  }

  @Test def add_atIndex(): Unit = {

    val addAt = 3

    // Greek capital Ξ (Xi), obviously visually different from ASCII
    val expected = Character.valueOf('\u039E')

    val forwardData = "abcdefgh"
    val forward = stringToSequenced(
      forwardData,
      listFactory[Character](forwardData.length())
    )

    val startingSize = forward.size()
    val expectedChangedSize = startingSize + 1

    val reversed = forward.reversed()

    reversed.add(addAt, expected)

    assertEquals("reversed size", expectedChangedSize, reversed.size())
    assertEquals("reversed content", expected, reversed.get(addAt))

    // forward size and content also changed.
    assertEquals("forward size", expectedChangedSize, forward.size())
    assertEquals(
      "forward content",
      expected,
      forward.get((forward.size() - 1) - addAt)
    )
  }

  @Test def listAdd(): Unit = {
    val expectedSize = 12
    val expected = 8

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      forward.add(j)

    val reversed = forward.reversed()
    reversed.add(expected)

    assertEquals("expected size", expectedSize, reversed.size())
    assertEquals("last element", expected, reversed.get(expectedSize - 1))
    assertEquals("underlying first element", expected, forward.get(0))
  }

  @Test def addAll_Index(): Unit = {
    val addAt = 5

    val listData = "abcdefgh"
    val forward =
      stringToSequenced(listData, listFactory[Character](listData.length()))

    val collectionData = "0123456789"
    val collection = stringToSequenced(
      collectionData,
      listFactory[Character](collectionData.length())
    )

    val expectedForwardContent = "abc9876543210defgh"
    val expectedReversedContent = "hgfed0123456789cba"

    val reversed = forward.reversed()

    assertTrue("addAll insertion", reversed.addAll(addAt, collection))

    assertEquals(
      "reversed content",
      expectedReversedContent,
      charactersToString(reversed)
    )

    assertEquals(
      "forward content",
      expectedForwardContent,
      charactersToString(forward)
    )
  }

  @Test def listAddAll(): Unit = { // Add at end of reversed list.

    val listData = "abcdefgh"
    val collectionData = "0123456789"
    val afterAddAllForwardData = "9876543210abcdefgh"

    val forward =
      stringToSequenced(listData, listFactory[Character](listData.length()))

    val collection = stringToSequenced(
      collectionData,
      listFactory[Character](collectionData.length())
    )

    val startingSize = forward.size()
    val expectedChangedSize = startingSize + collection.size()

    val reversed = forward.reversed()

    assertTrue("addAll() insertion", reversed.addAll(collection))

    assertEquals("reversed size", expectedChangedSize, reversed.size())

    for (j <- startingSize until reversed.size())
      assertEquals(
        "reversed content j:",
        collection.get(j - startingSize),
        reversed.get(j)
      )

    // forward size and content also changed.
    assertEquals("forward size", expectedChangedSize, forward.size())

    assertEquals(
      "forward content",
      afterAddAllForwardData,
      charactersToString(forward)
    )
  }

  @Test def listAddFirst(): Unit = {
    val expectedSize = 4
    val expected = 6

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      forward.add(j)

    val reversed = forward.reversed()
    reversed.addFirst(expected)

    assertEquals("arraylist size", expectedSize, reversed.size())
    assertEquals("first element", expected, reversed.get(0))
    assertEquals(
      "underlying last element",
      expected,
      forward.get(expectedSize - 1)
    )
  }

  @Test def listAddLast(): Unit = {
    val expectedSize = 4
    val expected = 7

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      forward.add(j)

    val reversed = forward.reversed()
    reversed.addLast(expected)

    assertEquals("arraylist size", expectedSize, reversed.size())
    assertEquals("last element", expected, reversed.get(expectedSize - 1))
    assertEquals("underlying first element", expected, forward.get(0))
  }

  @Test def listGetFirst_EmptyList(): Unit = {
    val forward = listFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "getFirst() of empty List should throw",
      classOf[NoSuchElementException],
      reversed.getFirst()
    )
  }

  @Test def listGetFirst(): Unit = {
    val expected = -2

    val forward = listFactory[Int]()

    forward.add(0)
    forward.add(2)
    forward.add(expected)

    val reversed = forward.reversed()
    val result = reversed.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def listGetLast_EmptyList(): Unit = {
    val forward = listFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "getLast() of empty List should throw",
      classOf[NoSuchElementException],
      reversed.getLast()
    )
  }

  @Test def listGetLast(): Unit = {
    val expected = -9

    val forward = listFactory[Int]()

    forward.add(expected)
    forward.add(0)
    forward.add(2)

    val reversed = forward.reversed()
    val result = reversed.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def indexOf(): Unit = {
    val forwardData = "ssssGsssGsss"
    val forward = stringToSequenced(
      forwardData,
      listFactory[Character](forwardData.length())
    )

    val foundNeedle = 'G'
    val expectedFoundAt = 3 // index in reversed

    val notFoundNeedle = 'g'
    val expectedNotFoundAt = -1

    val reversed = forward.reversed()

    val foundAt = reversed.indexOf(foundNeedle)

    assertTrue("reversed found indexOf", foundAt >= 0)

    assertEquals(
      "reversed found indexOf index",
      expectedFoundAt,
      foundAt
    )

    val notFoundAt = reversed.indexOf(notFoundNeedle)

    assertTrue("reversed not found indexOf", notFoundAt < 0)

    assertEquals(
      "reversed not found indexOf index",
      expectedNotFoundAt,
      notFoundAt
    )
  }

  @Test def listTraitIterator(): Unit = {
    val expectedSize = 20

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize)
      forward.add(j)

    val revIter = forward.reversed().iterator()

    var index = 0

    while (revIter.hasNext()) {
      assertEquals(
        s"iterator pos: ${index}",
        expectedSize - index,
        revIter.next()
      )
      index += 1
    }
  }

  @Test def lastIndexOf(): Unit = {
    val forwardData = "ssssGsssGsss"
    val forward = stringToSequenced(
      forwardData,
      listFactory[Character](forwardData.length())
    )

    val foundNeedle = 'G'
    val expectedFoundAt = 7 // index in reversed

    val notFoundNeedle = 'g'
    val expectedNotFoundAt = -1

    val reversed = forward.reversed()

    val foundAt = reversed.lastIndexOf(foundNeedle)

    assertTrue("reversed found lastIndexOf", foundAt >= 0)

    assertEquals(
      "reversed not found lastIndexOf index",
      expectedFoundAt,
      foundAt
    )

    val notFoundAt = reversed.lastIndexOf(notFoundNeedle)

    assertTrue("reversed not found lastIndexOf", notFoundAt < 0)

    assertEquals(
      "reversed not found lastIndexOf index",
      expectedNotFoundAt,
      notFoundAt
    )
  }

  /* There is no direct "@Test def listIterator_Index(): Unit".
   * It immediately delegates to listIterator(index).
   * Test the latter more throughly, rather than duplicating code here.
   */

  @Test def listIterator_Index(): Unit = {
    /* A future Evolution could add stanzas for the other listIterator
     * methods. So far, this tests iteration and the methods likely
     * to cause disruption by changing the length of the list.
     */

    // Greek capital Ψ (Psi), obviously visually different from ASCII
    val psi = Character.valueOf('\u03A8')

    val startingForwardData = "lmnoqrstuv"
    val afterAddForwardData = "lmnoΨqrstuv"
    val afterRemoveForwardData = "lmnorstuv"

    locally { // Exercise listIterators descending (previous) iteration.

      val reversed =
        stringToSequenced(
          startingForwardData,
          listFactory[Character](startingForwardData.length())
        ).reversed()

      val lstIter = reversed.listIterator(startingForwardData.length)
      val sb1 = new StringBuilder(startingForwardData.length)

      while (lstIter.hasPrevious())
        sb1.append(lstIter.previous())

      assertEquals("starting data", startingForwardData, sb1.toString())
    }

    locally { // Exercise listInterator add()
      val addAt = 6

      val reversed =
        stringToSequenced(
          startingForwardData,
          listFactory[Character](startingForwardData.length())
        ).reversed()

      val iterForAdd = reversed.listIterator(addAt)
      val sb2 = new StringBuilder(startingForwardData.length)

      iterForAdd.add(psi)

      val iterForSb = reversed.listIterator(startingForwardData.length + 1)
      while (iterForSb.hasPrevious())
        sb2.append(iterForSb.previous())

      assertEquals("after add", afterAddForwardData, sb2.toString())
    }

    locally { // Exercise listInterator remove()
      val removeAt = 5

      val reversed =
        stringToSequenced(
          startingForwardData,
          listFactory[Character](startingForwardData.length())
        ).reversed()

      val iterForRemove = reversed.listIterator()
      val sb3 = new StringBuilder(startingForwardData.length)

      // Exercise forward iteration
      for (j <- 0 to removeAt)
        iterForRemove.next()

      iterForRemove.remove()

      val iterForSb = reversed.listIterator(startingForwardData.length - 1)
      while (iterForSb.hasPrevious())
        sb3.append(iterForSb.previous())

      assertEquals("after remove", afterRemoveForwardData, sb3.toString())
    }
  }

  @Test def remove_Index(): Unit = {
    val removeAt = 8 // index in reversed list
    val goat = 'G'

    val beforeRemoveForwardData = "abcdefGhijGlmno"
    //                                   876543210
    val afterRemoveForwardData = "abcdefhijGlmno"

    val forward = stringToSequenced(
      beforeRemoveForwardData,
      listFactory[Character](beforeRemoveForwardData.length())
    )

    val expected =
      stringToSequenced(
        afterRemoveForwardData,
        listFactory[Character](afterRemoveForwardData.length())
      ).reversed()

    val reversed = forward.reversed()

    val removed = reversed.remove(removeAt)
    assertEquals("removed element", goat, removed)

    // remove(index) made intended change to list, but no other changes
    assertEquals(
      "forward content",
      afterRemoveForwardData,
      charactersToString(forward)
    )
  }

  @Test def listRemove(): Unit = {
    val goat = 'G'
    val notGoat = "Γ" // Greek capital gamma

    val beforeRemoveForwardData = "sssssGsssGssss"
    //                                  876543210
    val afterRemoveForwardData = "sssssGsssssss"

    val forward = stringToSequenced(
      beforeRemoveForwardData,
      listFactory[Character](beforeRemoveForwardData.length())
    )

    val reversed = forward.reversed()

    assertTrue(
      "remove(goat)",
      reversed.remove(goat)
    )

    val expectedChangedLength = beforeRemoveForwardData.length - 1

    assertEquals("remove(goat) size ", expectedChangedLength, reversed.size())

    assertFalse(
      "remove(notGoat)",
      reversed.remove(notGoat)
    )

    assertEquals(
      "remove(notGoat) size ",
      expectedChangedLength,
      reversed.size()
    )

    // remove() made intended change to list, but no other changes
    assertEquals(
      "forward content",
      afterRemoveForwardData,
      charactersToString(forward)
    )

  }

  @Test def listRemoveFirst_EmptyList(): Unit = {
    val forward = listFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "removeFirst of empty List should throw",
      classOf[NoSuchElementException],
      reversed.removeFirst()
    )
  }

  @Test def listRemoveFirst(): Unit = {
    val expected = -2

    val forward = listFactory[Int]()

    forward.add(2)
    forward.add(0)
    forward.add(4)
    forward.add(expected)

    val reversed = forward.reversed()
    val result = reversed.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test def listRemoveLast_EmptyList(): Unit = {
    val forward = listFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "removeLast() of empty List should throw",
      classOf[NoSuchElementException],
      reversed.removeLast()
    )
  }

  @Test def listRemoveLast(): Unit = {
    val expected = -0.0

    val forward = listFactory[scala.Double]()

    forward.add(expected)
    forward.add(1.0)
    forward.add(+0.0)
    forward.add(-1.0)

    val reversed = forward.reversed()
    val result = reversed.removeLast()

    assertTrue(
      "List removeLast()",
      jl.Double.compare(expected, result) == 0 // compare primitive doubles.
    ) // if removeLast is broken & returned +0.0, compare will fail -1
  }

  @Test def reversed_TwiceReversed(): Unit = {
    // Test reversed() for a list that is itself reversed.
    val expectedSize = 9

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize)
      forward.add(j)

    val reversed = forward.reversed()

    assertEquals(
      "twice reversed list should be content equal to original",
      forward,
      reversed.reversed()
    )

    assertTrue(
      "twice reversed should be reference equal original",
      forward.eq(reversed.reversed())
    )
  }

  @Test def set_Index(): Unit = {
    val setAt = 8 // index in reversed list
    val newElement = 'Ω' // Greek capital letter Omega
    val expectedReturnedElement = 'G'

    val beforeSetForwardData = s"abcdef${expectedReturnedElement}hijGlmno"
    val afterSetForwardData = s"abcdef${newElement}hijGlmno"

    val forward = stringToSequenced(
      beforeSetForwardData,
      listFactory[Character](beforeSetForwardData.length())
    )

    val reversed = forward.reversed()

    val returnedElement = reversed.set(setAt, newElement)
    assertEquals("returned element", expectedReturnedElement, returnedElement)

    // set(index, E) made intended change to list, but no other changes
    assertEquals(
      "forward content",
      afterSetForwardData,
      charactersToString(forward)
    )
  }

  @Test def listSize(): Unit = {
    val expectedSize = 7

    val forward = listFactory[Integer]()
    for (j <- 1 to expectedSize)
      forward.add(j)

    val reversed = forward.reversed()
    assertEquals("expected size", expectedSize, reversed.size())
  }

  @Test def sort(): Unit = {
    /* 307 is prime and large enough to trigger bisection search.
     * Good enough for CI. Manual testing could use a much larger.
     */

    val expectedSize = 307

    val unsortedSrcList =
      (new ju.Random(1777).ints(expectedSize, 0, 100)).boxed.toList()

    val forward = listFactory[Integer](unsortedSrcList)

    val underlyingForReversed = listFactory[Integer](unsortedSrcList)
    val reversed = underlyingForReversed.reversed()

    /* Ensure same comparator is used for each sort.
     *
     * A separate Test yet-to-be-written, could test using comparators
     * which differ in each sort.
     */
    val cmp = ju.Comparator.naturalOrder[Integer]()
    forward.sort(cmp)
    reversed.sort(cmp)

    assertEquals("sorted List sizes 1", expectedSize, reversed.size())
    assertEquals("sorted List sizes 2", forward.size(), reversed.size())

    /* There is a tricky bit here.
     *
     * The forward and reversed lists sorted by the same comparator
     * now have the same size, elements, & order of elements.
     */
    val aLIter = forward.iterator()
    val revIter = reversed.iterator()

    for (j <- 0 until forward.size())
      assertEquals(
        s"reversed(${j}):",
        aLIter.next(),
        revIter.next()
      )

    /* Ensure we are not looking at two views of a single underlying list.
     *
     * The list underlying to sorted reversed list should differ in the
     * expected way from the list underlying the forward list; that is the
     * forward list itself.
     */
    val aLDescendingIter = forward.listIterator(expectedSize)
    val underlyingForReversedIter = underlyingForReversed.iterator()

    for (j <- 0 until expectedSize)
      assertEquals(
        s"reversed(${j}):",
        aLDescendingIter.previous(),
        underlyingForReversedIter.next()
      )
  }

  /* The Test of stream() exercises the List splitertor() method.
   * See comments above that test below.
   */

  @Test def subList(): Unit = {
    /* A future evolution could add a Test that altering the contents
     * of the reversed sublist writes through to the forward sublist
     * and thence to the underlying.
     */

    val expectedSize = 30

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      forward.add(j)

    val reversed = forward.reversed()

    val alFromIndex = 10
    val alToIndex = 20

    val revFromIndex = 9
    val revToIndex = 19

    val a1SubList = forward.subList(alFromIndex, alToIndex)
    val revSubList = reversed.subList(revFromIndex, revToIndex)
    val commonSubListSize = a1SubList.size() // commonality to be asserted.

    assertEquals("sublist sizes", commonSubListSize, revSubList.size())

    // Dodge Schlemiel the Painter here; use iterators rather than List.get().
    val slIter = a1SubList.listIterator(commonSubListSize)
    val revSlIter = revSubList.iterator()

    for (j <- 0 until commonSubListSize)
      assertEquals(
        s"revSubList(${j}):",
        slIter.previous(),
        revSlIter.next()
      )

  }

  @Test def toArray(): Unit = {
    val expectedSize = 16

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize)
      forward.add(j)

    val reversed = forward.reversed()

    val revArray = forward.reversed().toArray()

    assertEquals("revArray length", expectedSize, revArray.length)

    val alListIter = forward.listIterator(expectedSize)

    for (j <- 0 until expectedSize) {
      assertEquals(
        s"array(${j}):",
        alListIter.previous(),
        revArray(j)
      )
    }
  }

  @Test def toArray_ArrayT(): Unit = {
    val expectedSize = 26
    val expected = 'Y'
    val unexpected = 'N'

    val forward = listFactory[Character](expectedSize)

    for (j <- 'a' to 'z')
      forward.add(j)

    val reversed = forward.reversed()

    // destination array is larger than reversed list
    val dst = new Array[Character](reversed.size() + 2)
    dst(dst.length - 2) = unexpected
    dst(dst.length - 1) = expected

    val revArray = forward.reversed().toArray(dst)

    assertEquals("revArray length", dst.length, revArray.length)

    val alListIter = forward.listIterator(expectedSize)

    for (j <- 0 until expectedSize) {
      assertEquals(
        s"array(${j}):",
        alListIter.previous(),
        revArray(j)
      )
    }

    assertNull(
      s"destination(expectedSize), expected: null got: ${dst(expectedSize)}",
      dst(expectedSize)
    )

    /* Only one position getting overwritten and other contents preserved
     * may prove to be too strong a reading of the JDK description.
     */
    assertEquals(
      s"destination(expectedSize + 1)",
      expected,
      dst(expectedSize + 1)
    )

  }

  @Test def stream: Unit = { // Collection
    val expectedSize = 20

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize)
      forward.add(j)

    val revStream = forward.reversed().stream()

    var index = 0

    revStream.forEachOrdered(e => {
      assertEquals(s"stream pos: ${index}", expectedSize - index, e)
      index += 1
    })
  }

  @Test def forEach(): Unit = {
    val expectedSize = 17

    val forward = listFactory[Int](expectedSize)

    for (j <- 1 to expectedSize)
      forward.add(j)

    val reversed = forward.reversed()

    var index = 0

    reversed.forEach(e => {
      assertEquals(s"pos: ${index}", expectedSize - index, e)
      index += 1
    })
  }
}

class JEP431_ReverseOrderArrayListTestOnJDK21
    extends JEP431_ReverseOrderListTestTraitOnJDK21 {

  def listFactory[E](initialCapacityHint: Int): ju.List[E] = {
    new ju.ArrayList[E](initialCapacityHint)
  }
}

class JEP431_ReverseOrderLinkedListTestOnJDK21
    extends JEP431_ReverseOrderListTestTraitOnJDK21
    with JEP431_ReverseOrderDequeTestTraitOnJDK21 {

  def dequeFactory[E](): ju.Deque[E] =
    (new ju.LinkedList[E]()).asInstanceOf[ju.Deque[E]]

  def dequeFactory[E](initialCapacityHint: Int): ju.Deque[E] = {
    // initialCapacityHint is not used, LinkedList has no such constructor
    dequeFactory[E]()
  }

  def listFactory[E](initialCapacityHint: Int): ju.List[E] = {
    // initialCapacityHint is not used, LinkedList has no such constructor
    new ju.LinkedList[E]()
  }
}

// ReverseOrderNavigableSet  --------------------------------------------

trait JEP431_ReverseOrderNavigableSetTestTraitOnJDK21
    extends JEP431_ReverseOrderSortedSetTestTraitOnJDK21 {

  import ReverseOrder.{charactersToString, stringToSequenced}

  def navigableSetFactory[E](): ju.NavigableSet[E] // Abstract

  @Test def ceiling(): Unit = {
    val expectedPresent = 1
    val step = 3
    val maxStep = 12 * step

    val forward = navigableSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 1 to maxStep by step)
      forward.add(j)

    val lessThanRevFirst = jl.Integer.MAX_VALUE
    val moreThanRevLast = jl.Integer.MIN_VALUE

    assertEquals(
      s"ceiling, no such element < first()",
      reversed.first(),
      reversed.ceiling(lessThanRevFirst)
    )

    assertEquals(
      "ceiling, present",
      expectedPresent,
      reversed.ceiling(expectedPresent)
    )

    assertEquals(
      "ceiling, greater than first() & less than last() but not present",
      expectedPresent,
      reversed.ceiling(expectedPresent + 1)
    )

    assertEquals(
      "ceiling, greater than reversed.last()",
      null,
      reversed.ceiling(moreThanRevLast)
    )
  }

  @Test def descendingIterator(): Unit = {
    val maxStep = 12

    val forward = navigableSetFactory[Int]()

    for (j <- 1 to maxStep by 2)
      forward.add(j)

    val reversed = forward.reversed()

    val forwardIter = forward.iterator()
    val reversedDescendingIter = reversed.descendingIterator()

    // Expected contents, in expected order.
    while (forwardIter.hasNext()) {
      assertEquals(
        "content",
        forwardIter.next(),
        reversedDescendingIter.next()
      )
    }
  }

  @Test def descendingSet(): Unit = {
    val maxStep = 12

    val forward = navigableSetFactory[Int]()

    for (j <- 1 to maxStep by 2)
      forward.add(j)

    val reversed = forward.reversed()
    val reversedDescendingSet = reversed.descendingSet()

    assertEquals("size", forward.size(), reversedDescendingSet.size())

    val forwardIter = forward.iterator()
    val descendingIter = reversedDescendingSet.iterator()

    // Expected contents, in expected order.
    while (forwardIter.hasNext()) {
      assertEquals(
        "content",
        forwardIter.next(),
        descendingIter.next()
      )
    }
  }

  @Test def floor(): Unit = {
    val expected = 5
    val step = 2
    val maxStep = 12

    val forward = navigableSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 1 to maxStep by step)
      forward.add(j)

    val lessThanRevFirst = jl.Integer.MAX_VALUE
    val moreThanRevLast = jl.Integer.MIN_VALUE

    assertEquals(
      s"floor, no such element < first(): ${lessThanRevFirst}",
      null,
      reversed.floor(lessThanRevFirst)
    )

    assertEquals(
      "floor, present",
      expected,
      reversed.floor(expected)
    )

    assertEquals(
      "floor, in-range but not present",
      expected,
      reversed.floor(expected - 1)
    )

    assertEquals(
      "floor, greater than reversed.last()",
      reversed.last(),
      reversed.floor(moreThanRevLast)
    )
  }

  @Test def headSet_NavigableSet(): Unit = {
    /* Someday add a separate Test to do a headSet() of a headSet()
     * both in-range and out-of-range.
     */

    val forwardData = "abcdefghijk"
    val pivot = 'e'
    val pivotIndex = forwardData.indexOf(pivot)

    val forward = navigableSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()

    locally {
      val expectedReversedHeadSetInclusive =
        navigableSetFactory[Character]().reversed()

      for (j <- pivotIndex until forwardData.length())
        expectedReversedHeadSetInclusive.add(forwardData(j))

      assertEquals(
        s"reversed.headSet('${pivot}', inclusive)",
        expectedReversedHeadSetInclusive,
        reversed.headSet(pivot, true)
      )

      // Expected elements, in expected order
      assertEquals(
        s"reversed.headSet('${pivot}', inclusive)",
        charactersToString(expectedReversedHeadSetInclusive),
        charactersToString(reversed.headSet(pivot, true))
      )
    }

    locally {
      val expectedReversedHeadSetExclusive =
        navigableSetFactory[Character]().reversed()

      for (j <- pivotIndex + 1 until forwardData.length())
        expectedReversedHeadSetExclusive.add(forwardData(j))

      // Expected elements, in expected order
      assertEquals(
        s"reversed.headSet('${pivot}', exclusive)",
        charactersToString(expectedReversedHeadSetExclusive),
        charactersToString(reversed.headSet(pivot, false))
      )
    }
  }

  @Test def higher(): Unit = {
    val probe = 4

    val expected = 1
    val step = 3
    val maxStep = 12 * step

    val forward = navigableSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 1 to maxStep by step)
      forward.add(j)

    val moreThanRevLast = jl.Integer.MIN_VALUE

    assertEquals(
      "higher, present",
      expected,
      reversed.higher(probe)
    )

    assertEquals(
      "higher, in-range but not present",
      expected,
      reversed.higher(probe - 1)
    )

    assertEquals(
      s"higher, no such element > last()",
      null,
      reversed.higher(moreThanRevLast)
    )
  }

  @Test def iterator(): Unit = {
    val maxStep = 9

    val forward = navigableSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 0 to maxStep)
      forward.add(j)

    val reversedIter = reversed.iterator()

    for (j <- maxStep to 0 by -1)
      assertEquals(s"reversed iterator content: ${j}", j, reversedIter.next())
  }

  @Test def lower(): Unit = {
    val probe = 4
    val step = 3

    val expected = probe + step
    val maxStep = 12 * step

    val forward = navigableSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 1 to maxStep by step)
      forward.add(j)

    val lessThanRevFirst = jl.Integer.MAX_VALUE

    assertEquals(
      s"lower, no such element < first()",
      null,
      reversed.lower(lessThanRevFirst)
    )

    assertEquals(
      "lower, present",
      expected,
      reversed.lower(probe)
    )

    assertEquals(
      "lower, in-range but not present",
      expected,
      reversed.lower(probe + 1)
    )
  }

  @Test def pollFirst(): Unit = {
    val expected = 2

    val forward = navigableSetFactory[Int]()

    forward.add(-1)
    forward.add(0)
    forward.add(-3)
    forward.add(expected)

    val reversed = forward.reversed()
    val result = reversed.pollFirst()

    assertEquals("reversed content", expected, result)
  }

  @Test def pollLast(): Unit = {
    val expected = -0.0

    val forward = navigableSetFactory[scala.Double]()

    forward.add(expected)
    forward.add(1.0)
    forward.add(+0.0)
    forward.add(0.5)

    val reversed = forward.reversed()
    val result = reversed.pollLast()

    assertTrue(
      "reversed content",
      jl.Double.compare(expected, result) == 0 // compare primitive doubles.
    )
  }

  @Test def reversed_TwiceReversed_NavigableSet(): Unit = {
    // Test reversed() for a NavigableSet that is itself reversed.
    val maxStep = 9

    val forward = navigableSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 1 to maxStep)
      forward.add(j)

    /* JDK 24 Javadoc does not require reference equality and that library
     * does not implement it.
     */
    assertEquals(
      "twice reversed NavigableSet should be content equal to original",
      forward,
      reversed.reversed()
    )
  }

  @Test def subSet_NavigableSet_ArgumentsCheck(): Unit = {
    val forwardData = "abcd"
    val revFromElement = 'b'
    val revToElement = 'a'

    val forward = navigableSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()

    // JVM always checks fromKey, even if exclusive
    assertThrows(
      "fromKey (inclusive) <= toKey",
      classOf[IllegalArgumentException],
      reversed.subSet(revToElement, true, revFromElement, false)
    )

    assertThrows(
      "fromKey (exclusive) <= toKey",
      classOf[IllegalArgumentException],
      reversed.subSet(revToElement, false, revFromElement, false)
    )
  }

  @Test def subSet_NavigableSet_SubSet_Methods(): Unit = {
    // headSets, subSets, and tailSets of subSets

    val forwardData = "abcdefghijk"
    val revFromElement = 'j'
    val revToElement = 'c'
    val revLastInRange = 'd'

    val forward = navigableSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()
    val reversedSubSet =
      reversed.subSet(revFromElement, true, revToElement, false)

    val expectedReversedSubSet =
      navigableSetFactory[Character]().reversed()

    for (j <- forwardData.indexOf(revFromElement) until
          forwardData.indexOf(revToElement) by -1)
      expectedReversedSubSet.add(forwardData(j))

    assertEquals(
      s"reversed.subSet('${revFromElement}', true, '${revToElement}', false)",
      charactersToString(expectedReversedSubSet), // "jihgfed"
      charactersToString(reversedSubSet)
    )

    locally {
      // rvss prefix is short form of reversedSubSet
      val rvssToInclusive = 'e'
      val expectedHeadSetData = "jihgfe"

      assertEquals(
        s"subSet.headSet('${rvssToInclusive}', true)",
        expectedHeadSetData,
        charactersToString(
          reversedSubSet
            .headSet(rvssToInclusive, true)
        )
      )
    }

    locally {
      // rvss prefix is short form of reversedSubSet
      val rvssFromInclusive = 'h'
      val rvssToInclusive = 'f'
      val expectedSubSetData = "hgf"

      assertEquals(
        s"subSet('${rvssFromInclusive}', true, '${rvssToInclusive}', true)",
        expectedSubSetData,
        charactersToString(
          reversedSubSet
            .subSet(rvssFromInclusive, true, rvssToInclusive, true)
        )
      )
    }

    locally {
      // rvss prefix is short form of reversedSubSet
      val rvssFromInclusive = 'h'

      val expectedTailSetData = "hgfed"

      assertEquals(
        s"subSet.tailSet('${rvssFromInclusive}', true)",
        expectedTailSetData,
        charactersToString(
          reversedSubSet
            .tailSet(rvssFromInclusive, true)
        )
      )
    }
  }

  @Test def subSet_NavigableSet_NonSet_Methods(): Unit = {
    /* Do methods in a subSet respect the subSet bounds?
     * 
     * poll*() & remove*() methods modify the forward set, so
     * exercise headSet(), subSet(), and tailSet() of subSets in
     * a separate, know clean Test environment. Easier than trying to
     * re-establish a known environment.
     */

    val forwardData = "abcdefghijk"
    val revFromElement = 'j'
    val revToElement = 'c'
    val revLastInRange = 'd'

    val forward = navigableSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()
    val reversedSubSet =
      reversed.subSet(revFromElement, true, revToElement, false)

    val expectedReversedSubSet =
      navigableSetFactory[Character]().reversed()

    for (j <- forwardData.indexOf(revFromElement) until
          forwardData.indexOf(revToElement) by -1)
      expectedReversedSubSet.add(forwardData(j))

    assertEquals(
      s"reversed.subSet('${revFromElement}', true, '${revToElement}', false)",
      charactersToString(expectedReversedSubSet), // "jihgfed"
      charactersToString(reversedSubSet)
    )

    /* Pick probe elements which are within the range of the original set
     * but out of range in the subSet. Try to evoke off-by-one or
     * fencepost errors.
     */

    // assertEquals() gives a more informative message than assertNotNull()

    val outOfBoundsRevCeiling = revToElement
    assertEquals(
      s"reversedSubSet.ceiling('${outOfBoundsRevCeiling}')",
      null,
      reversedSubSet.ceiling(outOfBoundsRevCeiling)
    )

    val outOfBoundsRevFloor = forward.last()
    assertEquals(
      s"reversedSubSet.floor('${outOfBoundsRevFloor}')",
      null,
      reversedSubSet.floor(outOfBoundsRevFloor)
    )

    /* reversedSubSet.pollFirst() & reversedSubSet.pollLast() may be
     * untested at this point, do not rely upon them; be explicit & declarative.
     */

    val subSetFirst = revFromElement
    val subSetLast = revLastInRange

    assertEquals(
      s"reversedSubSet.higher('${subSetLast}')",
      null,
      reversedSubSet.higher(subSetLast)
    )

    assertEquals(
      s"reversedSubSet.lower('${subSetFirst}')",
      null,
      reversedSubSet.lower(subSetFirst)
    )

    /* Keep order dependent poll*() and remove*() tests together.
     * They require that the poll*() happen before the remove*().
     * Otherwise the Test fails because the methods-under-test return
     * unexpected elements,
     */
    locally {
      // first() & last() after the next two poll*() calls remove elements
      val subSetNewFirst = reversedSubSet.higher(subSetFirst)
      val subSetNewLast = reversedSubSet.lower(subSetLast)

      assertEquals(
        s"reversedSubSet.pollFirst()",
        subSetFirst,
        reversedSubSet.pollFirst()
      )

      assertEquals(
        s"reversedSubSet.pollLast()",
        subSetLast,
        reversedSubSet.pollLast()
      )

      assertEquals(
        s"reversedSubSet.removeFirst()",
        subSetNewFirst,
        reversedSubSet.removeFirst()
      )

      assertEquals(
        s"reversedSubSet.removeLast()",
        subSetNewLast,
        reversedSubSet.removeLast()
      )
    }
  }

  @Test def subSet_NavigableSet_Bounds(): Unit = {
    val forwardData = "abcdefghijk"
    val revFromElement = 'j'
    val revToElement = 'c'

    val forward = navigableSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()

    locally { // from: true, to: false
      val expectedReversedSubSet =
        navigableSetFactory[Character]().reversed()

      for (j <-
            forwardData.indexOf(revFromElement) until
              forwardData.indexOf(revToElement) by -1)
        expectedReversedSubSet.add(forwardData(j))

      assertEquals(
        s"reversed.subSet(${revFromElement}, true, ${revToElement}, false)",
        charactersToString(expectedReversedSubSet),
        charactersToString(
          reversed.subSet(revFromElement, true, revToElement, false)
        )
      )
    }

    locally { // from: true, to: true
      val expectedReversedSubSet =
        navigableSetFactory[Character]().reversed()

      for (j <-
            forwardData.indexOf(revFromElement) to
              forwardData.indexOf(revToElement) by -1)
        expectedReversedSubSet.add(forwardData(j))

      // Expected elements, in expected order
      assertEquals(
        s"reversed.subSet(${revFromElement}, true, ${revToElement}, true)",
        charactersToString(expectedReversedSubSet),
        charactersToString(
          reversed.subSet(revFromElement, true, revToElement, true)
        )
      )
    }

    locally { // from: false, to: true
      val expectedReversedSubSet =
        navigableSetFactory[Character]().reversed()

      for (j <- (forwardData.indexOf(revFromElement) - 1) to
            forwardData.indexOf(revToElement) by -1)
        expectedReversedSubSet.add(forwardData(j))

      // Expected elements, in expected order
      assertEquals(
        s"reversed.subSet(${revFromElement}, false, ${revToElement}, true)",
        charactersToString(expectedReversedSubSet),
        charactersToString(
          reversed.subSet(revFromElement, false, revToElement, true)
        )
      )
    }

    locally { // from: false, to: false
      val expectedReversedSubSet =
        navigableSetFactory[Character]().reversed()

      for (j <- (forwardData.indexOf(revFromElement) - 1) until
            forwardData.indexOf(revToElement) by -1)
        expectedReversedSubSet.add(forwardData(j))

      // Expected elements, in expected order
      assertEquals(
        s"reversed.subSet(${revFromElement}, false, ${revToElement}, true)",
        charactersToString(expectedReversedSubSet),
        charactersToString(
          reversed.subSet(revFromElement, false, revToElement, false)
        )
      )
    }
  }

  @Test def tailSet_NavigableSet(): Unit = {
    /* Someday add a separate Test to do a tailSet() of a tailSet()
     * both in-range and out-of-range.
     */

    val forwardData = "abcdefghijk"
    val pivot = 'e'
    val pivotIndex = forwardData.indexOf(pivot)

    val forward = navigableSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()

    locally {
      val expectedReversedTailSetInclusive =
        navigableSetFactory[Character]().reversed()

      for (j <- pivotIndex to 0 by -1)
        expectedReversedTailSetInclusive.add(forwardData(j))

      // Expected elements, in expected order
      assertEquals(
        s"reversed.tailSet('${pivot}', inclusive)",
        charactersToString(expectedReversedTailSetInclusive),
        charactersToString(reversed.tailSet(pivot, true))
      )
    }

    locally {
      val expectedReversedTailSetExclusive =
        navigableSetFactory[Character]().reversed()

      for (j <- (pivotIndex - 1) to 0 by -1)
        expectedReversedTailSetExclusive.add(forwardData(j))

      // Expected elements, in expected order
      assertEquals(
        s"reversed.tailSet('${pivot}', exclusive)",
        charactersToString(expectedReversedTailSetExclusive),
        charactersToString(reversed.tailSet(pivot, false))
      )
    }
  }
}

// ReverseOrderSequencedCollection --------------------------------------

trait JEP431_ReverseOrderSequencedCollectionTestTraitOnJDK21 {

  def sequencedCollectionFactory[E](): ju.SequencedCollection[E]

  @Test def addFirst(): Unit = {
    val sequencedCollection = sequencedCollectionFactory[Int]()
    val reversed = sequencedCollection.reversed()

    assertThrows(
      "SequencedCollection default method addFirst() should throw",
      classOf[UnsupportedOperationException],
      reversed.addFirst(6)
    )
  }

  @Test def addLast(): Unit = {
    val sequencedCollection = sequencedCollectionFactory[Int]()
    val reversed = sequencedCollection.reversed()

    assertThrows(
      "SequencedCollection default method addLast() should throw",
      classOf[UnsupportedOperationException],
      reversed.addLast(6)
    )
  }

  @Test def getFirst_EmptySequencedCollection(): Unit = {
    val forward = sequencedCollectionFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "getFirst() of empty SequencedCollection should throw",
      classOf[NoSuchElementException],
      reversed.getFirst()
    )
  }

  @Test def getFirst(): Unit = {
    val expected = 2

    val forward = sequencedCollectionFactory[Int]()
    val reversed = forward.reversed()

    forward.add(0)
    forward.add(-2)
    forward.add(expected)

    val result = reversed.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def getLast_EmptySequencedCollection(): Unit = {
    val forward = sequencedCollectionFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "getLast() of empty SequencedCollection should throw",
      classOf[NoSuchElementException],
      reversed.getLast()
    )
  }

  @Test def getLast(): Unit = {
    val expected = -9

    val forward = sequencedCollectionFactory[Int]()
    val reversed = forward.reversed()

    forward.add(expected)
    forward.add(0)
    forward.add(2)

    val result = reversed.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def removeFirst_EmptySequencedCollection(): Unit = {
    val forward = sequencedCollectionFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "removeFirst of empty SequencedCollection should throw",
      classOf[NoSuchElementException],
      reversed.removeFirst()
    )
  }

  @Test def removeFirst(): Unit = {
    val expected = -2

    val forward = sequencedCollectionFactory[Int]()

    forward.add(2)
    forward.add(0)
    forward.add(-3)
    forward.add(expected)

    val reversed = forward.reversed()
    val result = reversed.removeFirst()

    assertEquals("SequencedCollection removeFirst()", expected, result)
  }

  @Test def removeLast_EmptySequencedCollection(): Unit = {
    val forward = sequencedCollectionFactory[Int]()
    val reversed = forward.reversed()

    assertThrows(
      "removeLast() of empty SequencedCollection should throw",
      classOf[NoSuchElementException],
      reversed.removeLast()
    )
  }

  @Test def removeLast(): Unit = {
    val expected = -0.0

    val forward = sequencedCollectionFactory[scala.Double]()

    forward.add(expected)
    forward.add(1.0)
    forward.add(+0.0)
    forward.add(-1.0)

    val reversed = forward.reversed()
    val result = reversed.removeLast()

    assertTrue(
      "SequencedCollection removeLast()",
      jl.Double.compare(expected, result) == 0 // compare primitive doubles.
    ) // if removeLast is broken & returned +0.0, compare will fail -1
  }
}

// ReverseOrderSequencedSet ---------------------------------------------

trait JEP431_ReverseOrderSequencedSetTestTraitOnJDK21
    extends JEP431_ReverseOrderSequencedCollectionTestTraitOnJDK21 {

  def sequencedCollectionFactory[E](): ju.SequencedCollection[E]

  def sequencedSetFactory[E](): ju.SequencedSet[E]

  // abstract method reversed(): SequencedSet is exercised in concrete classes.
}

class JEP431_ReverseOrderSequencedSetTestOnJDK21
    extends JEP431_ReverseOrderSequencedSetTestTraitOnJDK21 {

  def sequencedCollectionFactory[E](): ju.SequencedCollection[E] =
    sequencedSetFactory[E]()

  // A LinkedHashSet is a SequencedSet but not a SortedSet
  def sequencedSetFactory[E](): ju.SequencedSet[E] =
    new ju.LinkedHashSet[E]()

  /* Punt a load of local complexity in favor of more extensive test coverage
   * of concrete implementations. ju.LinkedHashSet works well enough for
   * a Mock most places but not for addFirst() & addLast().
   * 
   * Java LinkedHashSet and LinkedHashSet#reversed override those two
   * methods to not throw the Exception expected by the inherited
   * SequencedCollection.
   * 
   * Scala Native should someday follow the JVM practice but is not
   * currently JVM compliant. No sense testing & defining some, hopefully,
   * transient misbehavior.
   */
  @Test override def addFirst(): Unit = { /* Do nothing */ }
  @Test override def addLast(): Unit = { /* Do nothing */ }
}

// ReverseOrderSortedSet ------------------------------------------------

trait JEP431_ReverseOrderSortedSetTestTraitOnJDK21
    extends JEP431_ReverseOrderSequencedSetTestTraitOnJDK21 {

  import ReverseOrder.{charactersToString, stringToSequenced}

  def sortedSetFactory[E](): ju.SortedSet[E] // Abstract

  @Test def comparator(): Unit = {
    val forwardData = "abcde"

    locally {
      val forward =
        stringToSequenced(forwardData, sortedSetFactory[Character]())

      assertNull(
        s"forwardNaturalOrderComparator",
        forward.comparator()
      )

      val reversed = forward.reversed()

      val reversedNaturalOrderComparator = reversed.comparator()
      assertNotNull(
        s"reversedNaturalOrderComparator",
        reversedNaturalOrderComparator
      )

      assertEquals(
        "reversedNaturalOrderComparator a > b",
        1,
        reversedNaturalOrderComparator.compare(forwardData(0), forwardData(1))
      )
    }

    locally {
      // Wart alert! Use of TreeSet rather a more generic SortedSet.
      val forward =
        stringToSequenced(
          forwardData,
          new ju.TreeSet[Character](ju.Comparator.reverseOrder[Character]())
        )

      assertNotNull(
        s"forwardCustomComparator",
        forward.comparator()
      )

      val reversed = forward.reversed()

      val reversedCustomComparator = reversed.comparator()
      assertNotNull(
        s"reversedCustomComparator",
        reversedCustomComparator
      )

      assertEquals(
        "reversedNaturalOrderComparator a > b",
        -1,
        reversedCustomComparator.compare(forwardData(0), forwardData(1))
      )
    }
  }

  @Test def first(): Unit = {
    val expected = 3

    val forward = sortedSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- 1 to expected)
      forward.add(j)

    assertEquals(
      "content",
      expected,
      reversed.first()
    )
  }

  @Test def headSet_SortedSet(): Unit = {

    assumeTrue("Not yet implemented on Scala Native", Platform.executingInJVM)

    val forwardData = "ABCDEFGHIJK"
    val pivot = 'G'
    val pivotIndex = forwardData.indexOf(pivot)

    val forward = sortedSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()

    val expectedReversedHeadSet =
      sortedSetFactory[Character]().reversed()

    for (j <- (pivotIndex + 1) until forwardData.length())
      expectedReversedHeadSet.add(forwardData(j))

    // Expected elements, in expected order
    assertEquals(
      s"reversed.headSet('${pivot}')",
      charactersToString(expectedReversedHeadSet),
      charactersToString(reversed.headSet(pivot))
    )
  }

  @Test def last(): Unit = {
    val expected = 1

    val forward = sortedSetFactory[Int]()
    val reversed = forward.reversed()

    for (j <- expected to 3)
      forward.add(j)

    assertEquals(
      "content",
      expected,
      reversed.last()
    )
  }

  @Test def reversed_TwiceReversed_SortedSet(): Unit = {
    // Test reversed() for a SortedSet that is itself reversed.
    val expectedSize = 9

    val forward = sortedSetFactory[Int]()

    val reversed = forward.reversed()

    for (j <- 1 to expectedSize)
      forward.add(j)

    /* JDK 24 Javadoc does not require reference equality and that library
     * does not implement it.
     */
    assertEquals(
      "twice reversed SortedSet should be content equal to original",
      forward,
      reversed.reversed()
    )
  }

  /* SortedSet has a different sense of "first" & "last" than SequencedSet
   * so Tests inherited from the latter must be overridden.
   */
  @Test override def removeFirst(): Unit = {
    val expected = 4

    val forward = sequencedCollectionFactory[Int]()

    forward.add(2)
    forward.add(0)
    forward.add(expected)
    forward.add(-3)

    val reversed = forward.reversed()
    val result = reversed.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test override def removeLast(): Unit = {
    val expected = -1.0

    val forward = sequencedCollectionFactory[scala.Double]()

    forward.add(1.0)
    forward.add(-0.0)
    forward.add(+0.0)
    forward.add(-1.0)
    forward.add(expected)

    val reversed = forward.reversed()
    val result = reversed.removeLast()

    assertTrue(
      "TreeSet removeLast()",
      jl.Double.compare(expected, result) == 0 // compare primitive doubles.
    ) // if removeLast is broken & returned +0.0, compare will fail -1
  }

  @Test def spliterator(): Unit = {
    /* This may _look_ like an out-of-place stream() test but it is
     * really a quicky Test for spliterator.  For the stream
     * to be right, the spliterator should be functioning correctly.
     */
    val expectedSize = 20

    val forward = sortedSetFactory[Int]()

    for (j <- 1 to expectedSize)
      forward.add(j)

    val revStream = forward.reversed().stream()

    var index = 0

    revStream.forEachOrdered(e => {
      assertEquals(s"stream pos: ${index}", expectedSize - index, e)
      index += 1
    })
  }

  @Test def subSet_SortedSet(): Unit = {

    assumeTrue("Not yet implemented on Scala Native", Platform.executingInJVM)

    val forwardData = "abcdefghijklmnop"

    val reversedSubsetFrom = 'm' // inclusive
    val reversedSubsetTo = 'f' // exclusive
    val expectedReversedSubSetData = "mlkjihg"

    val forward = stringToSequenced(forwardData, sortedSetFactory[Character]())

    locally {
      // Try to trip up off-by-one indicies in the Scala Native implementation.

      val reversedSubSet = forward
        .reversed()
        .subSet(reversedSubsetFrom, reversedSubsetFrom) // to & from are same.

      assertEquals(
        "reversed subSet size, zero span",
        0,
        reversedSubSet.size()
      )

      val reversedSubSetIterator = reversedSubSet.iterator()

      assertFalse(
        "reversed subSet iterator.hasNext()",
        reversedSubSetIterator.hasNext()
      )

    }

    locally {
      val reversedSubSet = forward
        .reversed()
        .subSet(reversedSubsetFrom, reversedSubsetTo)

      assertEquals(
        "reversed subSet size",
        expectedReversedSubSetData.length(),
        reversedSubSet.size()
      )

      // Expected elements, in expected order
      assertEquals(
        s"reversed.subSet('${reversedSubsetFrom}', '${reversedSubsetTo}')",
        expectedReversedSubSetData,
        charactersToString(reversedSubSet)
      )
    }
  }

  @Test def tailSet_SortedSet(): Unit = {

    assumeTrue("Not yet implemented on Scala Native", Platform.executingInJVM)

    val forwardData = "ABCDEFGHIJK"
    val pivot = 'G'
    val pivotIndex = forwardData.indexOf(pivot)

    val forward = sortedSetFactory[Character]()
    stringToSequenced(forwardData, forward)

    val reversed = forward.reversed()

    val expectedReversedTailSet =
      sortedSetFactory[Character]().reversed()

    for (j <- pivotIndex to 0 by -1)
      expectedReversedTailSet.add(forwardData(j))

    // Expected elements, in expected order
    assertEquals(
      s"reversed.tailSet('${pivot}')",
      charactersToString(expectedReversedTailSet),
      charactersToString(reversed.tailSet(pivot))
    )
  }
}

class MockSortedSet[E]() extends ju.AbstractSet[E] with ju.SortedSet[E] {
  // Members declared in java.util.Set

  val impl = new ju.TreeSet[E]()

  override def add(e: E): Boolean = impl.add(e)

  def iterator(): java.util.Iterator[E] = impl.iterator()

  def size(): Int = impl.size()

  // declared in java.util.SortedSet

  def comparator(): java.util.Comparator[? >: E] =
    impl.comparator()

  def first(): E = impl.first()

  def headSet(toElement: E): java.util.SortedSet[E] =
    impl.headSet(toElement)

  def last(): E =
    impl.last()

  def subSet(fromElement: E, toElement: E): java.util.SortedSet[E] =
    impl.subSet(fromElement, toElement)

  def tailSet(fromElement: E): java.util.SortedSet[E] =
    impl.tailSet(fromElement)
}

class JEP431_ReverseOrderSortedSetTestOnJDK21
    extends JEP431_ReverseOrderSortedSetTestTraitOnJDK21 {

  def sequencedCollectionFactory[E](): ju.SequencedCollection[E] =
    sortedSetFactory[E]()

  def sequencedSetFactory[E](): ju.SequencedSet[E] =
    sortedSetFactory[E]()

  def sortedSetFactory[E](): ju.SortedSet[E] =
    (new MockSortedSet[E]())
}

// ReverseOrderTreeSet  -------------------------------------------------

class JEP431_ReverseOrderTreeSetTestOnJDK21
    extends JEP431_ReverseOrderNavigableSetTestTraitOnJDK21 {

  def sequencedCollectionFactory[E](): java.util.SequencedCollection[E] =
    new ju.TreeSet[E]()

  def sequencedSetFactory[E](): java.util.SequencedSet[E] =
    new ju.TreeSet[E]()

  def sortedSetFactory[E](): ju.SortedSet[E] =
    (new ju.TreeSet[E]())

  def navigableSetFactory[E](): ju.NavigableSet[E] =
    (new ju.TreeSet[E]())

  def treeSetFactory[E](): ju.TreeSet[E] =
    new ju.TreeSet[E]()

  /* Most of the TreeSet reversed methods are tested by the inherited
   * trait hierarchy with the given factory methods using TreeSet.
   * 
   * There is no "@Test def clone_TreeSet()" because treeSet.reversed() returns
   * a NavigableSet, which does not declare clone().
   */
}
