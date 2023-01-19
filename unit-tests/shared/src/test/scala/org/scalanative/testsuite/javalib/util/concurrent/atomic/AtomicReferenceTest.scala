package org.scalanative.testsuite.javalib.util
package concurrent
package atomic

import java.util._
import java.util.concurrent.atomic._

import java.util.function.UnaryOperator

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

@Ignore
class AtomicReferenceTest {

  // This test suite is INCOMPLETE (obviously!).
  //
  // The get() method is used by getAndUpdate() and updateAndGet().
  // The test is only a shallow probe before use.
  //
  // getAndUpdate() and updateAndGet() test only that the expected
  // values are returned in the success case. It was not evident
  // how to test concurrent and contended access patterns within
  // the scope of unit-tests.

  @Test def get(): Unit = {

    val expected = -1
    val ar = new AtomicReference(expected)

    val result = ar.get()

    assertTrue(
      s"result: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def getAndUpdateUpdateFunction(): Unit = {

    val expectedValue = 100
    val expectedNewValue = expectedValue / 2

    val tax = new UnaryOperator[Int] {
      override def apply(t: Int): Int = t / 2
    }

    val ar = new AtomicReference[Int](expectedValue)

    val value = ar.getAndUpdate(tax)

    assertTrue(
      s"result before function: ${value} != expected: ${expectedValue}",
      value == expectedValue
    )

    val newValue = ar.get()

    assertTrue(
      s"newValue after function: ${newValue} != " +
        s"expected: ${expectedNewValue}",
      newValue == expectedNewValue
    )
  }

  @Test def updateAndGetUpdateFunction(): Unit = {

    val initialValue = 100
    val expected = initialValue * 3

    val reward = new UnaryOperator[Int] {
      override def apply(t: Int): Int = t * 3
    }

    val ar = new AtomicReference[Int](initialValue)

    val result = ar.updateAndGet(reward)

    assertTrue(
      s"result after function: ${result} != expected: ${expected}",
      result == expected
    )
  }

}
