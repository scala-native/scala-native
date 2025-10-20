package scala.scalanative.junit

// Ported from Scala.js

import java.io.IOException

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class ExpectTest {
  @Test(expected = classOf[IOException])
  def expectNormal(): Unit = throw new IOException

  @Test(expected = classOf[IOException])
  def failExpectDifferent(): Unit = throw new IllegalArgumentException

  @Test(expected = classOf[IOException])
  def failExpectNoThrow(): Unit = ()

  @Test(expected = classOf[AssertionError])
  def expectAssert(): Unit = throw new AssertionError

  @Test(expected = classOf[AssertionError])
  def failExpectAssert(): Unit = ()
}

class ExpectTestAssertions extends JUnitTest
