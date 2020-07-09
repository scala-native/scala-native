package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class ExceptionTest {
  @Test def test(): Unit = {
    throw new IndexOutOfBoundsException("Exception message")
  }
}

class ExceptionTestAssertions extends JUnitTest
