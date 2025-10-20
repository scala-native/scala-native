package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class AssertEqualsTest {
  @Test def test(): Unit = {
    assertEquals(false, true)
  }
}

class AssertEqualsTestAssertions extends JUnitTest
