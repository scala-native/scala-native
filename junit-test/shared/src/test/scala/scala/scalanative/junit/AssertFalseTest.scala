package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class AssertFalseTest {
  @Test def test(): Unit = {
    assertFalse(true)
  }
}

class AssertFalseTestAssertions extends JUnitTest
