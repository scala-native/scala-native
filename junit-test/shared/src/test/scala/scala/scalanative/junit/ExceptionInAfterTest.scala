package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class ExceptionInAfterTest {
  @After def after(): Unit =
    throw new UnsupportedOperationException("Exception in after()")

  /* Even if the test method declares expecting the exception thrown by the
   * after() method, it must result in an error, not a success.
   */
  @Test(expected = classOf[UnsupportedOperationException])
  def test(): Unit =
    throw new UnsupportedOperationException("Exception in test()")
}

class ExceptionInAfterTestAssertions extends JUnitTest
