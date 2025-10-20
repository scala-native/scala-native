package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class ExceptionAfterAssume {
  @After def after(): Unit =
    throw new IllegalArgumentException("after() must be called")

  @Test def assumeFail(): Unit = {
    assumeTrue("This assume should not pass", false)
  }
}

class ExceptionAfterAssumeAssertions extends JUnitTest
