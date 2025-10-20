package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class AssertEquals2Test {
  @Test def test(): Unit = {
    assertEquals("This is the message", false, true)
  }
}

class AssertEquals2TestAssertions extends JUnitTest
