package scala.scalanative.junit

import org.junit.Assert._
import org.junit.Test
import org.junit.function.ThrowingRunnable

import scala.scalanative.junit.utils.JUnitTest

class AssertThrowsTest {
  @Test def test(): Unit = {
    assertThrows(
      classOf[UnsupportedOperationException],
      new ThrowingRunnable {
        def run(): Unit =
          throw new IllegalArgumentException("Exception message")
      }
    )
  }
}

class AssertThrowsTestAssertions extends JUnitTest
