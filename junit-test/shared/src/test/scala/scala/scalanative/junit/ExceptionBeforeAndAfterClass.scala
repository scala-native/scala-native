package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object ExceptionBeforeAndAfterClass {
  @BeforeClass def beforeClass(): Unit =
    throw new AssertionError("before")

  @AfterClass def afterClass(): Unit =
    throw new IllegalArgumentException("after")
}

class ExceptionBeforeAndAfterClass {
  @Test def test1(): Unit = ()
  @Test def test2(): Unit = ()
}

class ExceptionBeforeAndAfterClassAssertions extends JUnitTest
