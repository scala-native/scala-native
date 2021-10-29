// Ported from Scala.js commit: unk dated: unk

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Assert._

object Utils {
  def SIE[K, V](key: K, value: V): ju.Map.Entry[K, V] =
    new ju.AbstractMap.SimpleImmutableEntry(key, value)

  def iteratorIsEmpty(iter: ju.Iterator[_]): Boolean =
    !iter.hasNext()

  def iteratorSize(iter: ju.Iterator[_]): Int = {
    var result = 0
    while (iter.hasNext()) {
      iter.next()
      result += 1
    }
    result
  }

  def iteratorMap[A, B](iter: ju.Iterator[A])(f: A => B): ju.Iterator[B] = {
    new ju.Iterator[B] {
      def hasNext(): Boolean = iter.hasNext()

      def next(): B = f(iter.next())

      override def remove(): Unit =
        throw new UnsupportedOperationException("Iterator.remove()")
    }
  }

  def enumerationIsEmpty(enum: ju.Enumeration[_]): Boolean =
    !enum.hasMoreElements()

  def enumerationSize(enum: ju.Enumeration[_]): Int = {
    var result = 0
    while (enum.hasMoreElements()) {
      enum.nextElement()
      result += 1
    }
    result
  }

  def assertEnumSameElementsAsSet[A](
      expected: A*
  )(enum: ju.Enumeration[_ <: A]): Unit = {
    assertIteratorSameElementsAsSet(expected: _*)(new ju.Iterator[A] {
      def hasNext(): Boolean = enum.hasMoreElements()
      def next(): A = enum.nextElement()
      override def remove(): Unit =
        throw new UnsupportedOperationException("Iterator.remove()")
    })
  }

  def assertCollSameElementsAsSet[A](
      expected: A*
  )(coll: ju.Collection[A]): Unit = {
    assertIteratorSameElementsAsSet(expected: _*)(coll.iterator())
  }

  def assertIteratorSameElementsAsSet[A](
      expected: A*
  )(iter: ju.Iterator[A]): Unit = {
    val expectedSet = expected.toSet
    var size = 0
    while (iter.hasNext()) {
      val elem = iter.next()
      assertTrue(s"unexpected element $elem", expectedSet.contains(elem))
      size += 1
    }
    assertEquals(expectedSet.size, size)
  }

  def assertIteratorSameElementsAsSetDupesAllowed[A](
      expected: A*
  )(iter: ju.Iterator[A]): Unit = {
    val expectedSet = expected.toSet
    val notSeen = scala.collection.mutable.HashSet[A](expected: _*)
    while (iter.hasNext()) {
      val value = iter.next()
      assertTrue(
        s"iterator yieled unexpected value $value",
        expectedSet.contains(value)
      )
      notSeen -= value
    }
    assertTrue(
      s"iterator did not yield expected values $notSeen",
      notSeen.isEmpty
    )
  }
}
