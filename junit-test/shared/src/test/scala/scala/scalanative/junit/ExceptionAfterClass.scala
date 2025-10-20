package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object ExceptionAfterClass {
  @AfterClass def afterClass(): Unit =
    throw new IllegalArgumentException("foo")
}

class ExceptionAfterClass {
  @Test def test1(): Unit = ()
  @Test def test2(): Unit = ()
}

class ExceptionAfterClassAssertions extends JUnitTest
