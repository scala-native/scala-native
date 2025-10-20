package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object ExceptionEverywhere {
  @BeforeClass def beforeClass(): Unit =
    throw new AssertionError("before class")

  @AfterClass def afterClass(): Unit =
    throw new AssertionError("after class")
}

class ExceptionEverywhere {
  @Before def before(): Unit = throw new AssertionError("before")
  @After def after(): Unit = throw new AssertionError("after")

  @Test def test1(): Unit = throw new AssertionError("test 1")
  @Test def test2(): Unit = throw new AssertionError("test 2")
}

class ExceptionEverywhereAssertions extends JUnitTest
