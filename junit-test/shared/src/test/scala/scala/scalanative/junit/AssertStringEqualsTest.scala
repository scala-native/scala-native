package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class AssertStringEqualsTest {
  @Test def test(): Unit = {
    assertEquals("foobar", "foobbbr")
  }
}

class AssertStringEqualsTestAssertions extends JUnitTest
