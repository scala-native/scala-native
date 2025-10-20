package scala.scalanative.junit

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

@Ignore
class IgnoreAllTest {
  throw new Error("unreachable")

  @Test def multiTest1(): Unit = ()
  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
}

class IgnoreAllTestAssertions extends JUnitTest
