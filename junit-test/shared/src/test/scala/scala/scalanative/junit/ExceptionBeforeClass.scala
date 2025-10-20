package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object ExceptionBeforeClass {
  @BeforeClass def beforeClass(): Unit =
    throw new IllegalArgumentException("foo")
}

class ExceptionBeforeClass {
  @Test def test1(): Unit = ()
  @Test def test2(): Unit = ()
}

class ExceptionBeforeClassAssertions extends JUnitTest
