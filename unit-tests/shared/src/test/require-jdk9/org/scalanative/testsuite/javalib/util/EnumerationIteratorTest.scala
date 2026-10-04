package org.scalanative.testsuite.javalib.util

import java.util.{ArrayList, Collections, Enumeration, NoSuchElementException}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class EnumerationIteratorTest {
  @Test def iteratorConsumesRemainingElements(): Unit = {
    val values = new ArrayList[String]()
    values.add("first")
    values.add("second")
    values.add("third")
    val enumeration = Collections.enumeration(values)
    assertEquals("first", enumeration.nextElement())
    val iterator = enumeration.asIterator()
    assertTrue(iterator.hasNext())
    assertTrue(iterator.hasNext())
    assertEquals("second", iterator.next())
    assertEquals("third", iterator.next())
    assertFalse(iterator.hasNext())
    assertThrows(classOf[NoSuchElementException], iterator.next())
  }

  @Test def emptyIteratorIsExhausted(): Unit = {
    val enumeration = new Enumeration[String] {
      def hasMoreElements(): Boolean = false
      def nextElement(): String = throw new NoSuchElementException
    }
    val iterator = enumeration.asIterator()
    assertFalse(iterator.hasNext())
    assertThrows(classOf[NoSuchElementException], iterator.next())
    assertThrows(classOf[UnsupportedOperationException], iterator.remove())
  }

  @Test def collectionsEmptyEnumerationReturnsItsEmptyIterator(): Unit = {
    val iterator = Collections.emptyEnumeration[String]().asIterator()
    assertSame(Collections.emptyIterator[String](), iterator)
    assertFalse(iterator.hasNext())
    assertThrows(classOf[NoSuchElementException], iterator.next())
    assertThrows(classOf[IllegalStateException], iterator.remove())
  }

  @Test def removeIsUnsupportedWithoutConsumingElements(): Unit = {
    val iterator =
      Collections.enumeration(Collections.singletonList("value")).asIterator()
    assertThrows(classOf[UnsupportedOperationException], iterator.remove())
    assertEquals("value", iterator.next())
    assertThrows(classOf[UnsupportedOperationException], iterator.remove())
    assertFalse(iterator.hasNext())
  }

  @Test def forEachRemainingUsesEnumeration(): Unit = {
    val iterator =
      Collections.enumeration(Collections.singletonList("value")).asIterator()
    val consumed = new ArrayList[String]()
    iterator.forEachRemaining((value: String) => { consumed.add(value); () })
    assertEquals(Collections.singletonList("value"), consumed)
    assertFalse(iterator.hasNext())
  }

  @Test def customEnumerationMethodsAreDelegatedLazily(): Unit = {
    var hasNextCalls = 0
    var nextCalls = 0
    val failure = new IllegalStateException("sentinel")
    val enumeration = new Enumeration[String] {
      def hasMoreElements(): Boolean = { hasNextCalls += 1; false }
      def nextElement(): String = { nextCalls += 1; throw failure }
    }
    val iterator = enumeration.asIterator()
    assertEquals(0, hasNextCalls)
    assertEquals(0, nextCalls)
    assertFalse(iterator.hasNext())
    assertEquals(1, hasNextCalls)
    assertSame(
      failure,
      assertThrows(classOf[IllegalStateException], iterator.next())
    )
    assertEquals(1, nextCalls)
  }
}
