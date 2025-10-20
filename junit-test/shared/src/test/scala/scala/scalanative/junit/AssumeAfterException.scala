package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class AssumeAfterException {
  @After def after(): Unit =
    assumeTrue("This assume should not pass", false)

  @Test def test(): Unit =
    throw new IllegalArgumentException("test throws")
}

class AssumeAfterExceptionAssertions extends JUnitTest
