package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class MultiIgnoreAllTest {
  @Ignore @Test def multiTest1(): Unit = ()
  @Ignore @Test def multiTest2(): Unit = ()
  @Ignore @Test def multiTest3(): Unit = ()
  @Ignore @Test def multiTest4(): Unit = ()
  @Ignore @Test def multiTest5(): Unit = ()
}

class MultiIgnoreAllTestAssertions extends JUnitTest
