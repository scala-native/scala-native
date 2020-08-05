/*
 * Ported from Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalanative.junit.utils

object AssertThrows {

  /** Backport implementation of Assert.assertThrows to be used until JUnit 4.13 is
   *  released. See org.junit.Assert.scala in jUnitRuntime.
   */
  private def assertThrowsBackport(expectedThrowable: Class[_ <: Throwable],
                                   runnable: ThrowingRunnable): Unit = {
    expectThrowsBackport(expectedThrowable, runnable)
  }

  /** Backport implementation of Assert.expectThrows to be used until JUnit 4.13 is
   *  released. See org.junit.Assert.scala in jUnitRuntime.
   */
  private def expectThrowsBackport[T <: Throwable](
      expectedThrowable: Class[T],
      runnable: ThrowingRunnable): T = {
    val result = {
      try {
        runnable.run()
        null.asInstanceOf[T]
      } catch {
        case actualThrown: Throwable =>
          if (expectedThrowable.isInstance(actualThrown)) {
            actualThrown.asInstanceOf[T]
          } else {
            val mismatchMessage = "unexpected exception type thrown;" +
              expectedThrowable.getSimpleName + " " + actualThrown.getClass.getSimpleName

            val assertionError = new AssertionError(mismatchMessage)
            assertionError.initCause(actualThrown)
            throw assertionError
          }
      }
    }
    if (result == null) {
      throw new AssertionError(
        "expected " + expectedThrowable.getSimpleName +
          " to be thrown, but nothing was thrown")
    } else {
      result
    }
  }

  /** Backport implementation of Assert.ThrowingRunnable to be used until
   *  JUnit 4.13 is released. See org.junit.Assert.scala in jUnitRuntime.
   */
  private trait ThrowingRunnable {
    def run(): Unit
  }

  private def throwingRunnable(code: => Unit): ThrowingRunnable = {
    new ThrowingRunnable {
      def run(): Unit = code
    }
  }

  def assertThrows[T <: Throwable, U](expectedThrowable: Class[T],
                                      code: => U): Unit = {
    assertThrowsBackport(expectedThrowable, throwingRunnable {
      code
    })
  }

  def expectThrows[T <: Throwable, U](expectedThrowable: Class[T],
                                      code: => U): T = {
    expectThrowsBackport(expectedThrowable, throwingRunnable {
      code
    })
  }
}
