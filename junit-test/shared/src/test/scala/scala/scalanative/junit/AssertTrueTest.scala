package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class AssertTrueTest {
  @Test def failTest(): Unit = {
    assertTrue(false)
  }

  @Test def successTest(): Unit = {
    assertTrue(true)
  }
}

class AssertTrueTestAssertions extends JUnitTest
