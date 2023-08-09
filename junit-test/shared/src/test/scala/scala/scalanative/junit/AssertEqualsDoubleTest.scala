package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils._

class AssertEqualsDoubleTest {
  @Test def failsWithDouble(): Unit = {
    assertEquals(1.0, 1.0, 0.0)
  }

  @Test def failsWithDoubleMessage(): Unit = {
    assertEquals("Message", 1.0, 1.0, 0.0)
  }

  @Test def worksWithEpsilon(): Unit = {
    assertEquals(1.0, 1.0, 0.1)
    assertEquals("Message", 1.0, 1.0, 0.1)
  }

  @Test def worksWithByte(): Unit = {
    // This is supposed to take the (long, long) overload.
    assertEquals(1.toByte, 1.toByte)
  }

  @Test def worksWithShort(): Unit = {
    // This is supposed to take the (long, long) overload.
    assertEquals(2.toShort, 2.toShort)
  }

  @Test def worksWithInt(): Unit = {
    // This is supposed to take the (long, long) overload.
    assertEquals(1, 1)
  }
}

class AssertEqualsDoubleTestAssertions extends JUnitTest
