package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class AssertFalse2Test {
  @Test def test(): Unit = {
    assertFalse("This is the message", true)
  }
}

class AssertFalse2TestAssertions extends JUnitTest
