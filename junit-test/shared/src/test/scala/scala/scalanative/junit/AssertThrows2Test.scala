package scala.scalanative.junit

import org.junit.Assert._
import org.junit.Test
import org.junit.function.ThrowingRunnable

import scala.scalanative.junit.utils.JUnitTest

class AssertThrows2Test {
  @Test def test(): Unit = {
    assertThrows(
      "This is the message",
      classOf[UnsupportedOperationException],
      new ThrowingRunnable {
        def run(): Unit =
          throw new IllegalAccessException("Exception message")
      }
    )
  }
}

class AssertThrows2TestAssertions extends JUnitTest
