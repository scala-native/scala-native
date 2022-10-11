package org.scalanative.testsuite.utils

import org.junit.Assert
import org.junit.function.ThrowingRunnable

// Port of AssertThrows from core unit-tests
object AssertThrows {
  def assertThrows[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U
  ): T = {
    Assert.assertThrows(
      expectedThrowable,
      new ThrowingRunnable {
        def run(): Unit = code
      }
    )
  }
}
