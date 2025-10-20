package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class ExceptionInConstructorTest {
  throw new UnsupportedOperationException(
    "Exception while constructing the test class"
  )

  @Before def before(): Unit =
    throw new IllegalStateException("before() must not be called")

  @After def after(): Unit =
    throw new IllegalStateException("after() must not be called")

  /* Even if the test method declares expecting the exception thrown by the
   * constructor, it must result in an error, not a success.
   */
  @Test(expected = classOf[UnsupportedOperationException])
  def test(): Unit =
    throw new IllegalStateException("test() must not be called")
}

class ExceptionInConstructorTestAssertions extends JUnitTest
