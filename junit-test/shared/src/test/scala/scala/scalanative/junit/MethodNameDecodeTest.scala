package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class MethodNameDecodeTest {
  @Test def `abcd ∆ƒ \uD83D\uDE00 * #&$`(): Unit = ()
}

class MethodNameDecodeTestAssertions extends JUnitTest
