package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

class MultiIgnore2Test {
  @Ignore @Test def multiTest1(): Unit = ()
  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
  @Ignore @Test def multiTest4(): Unit = ()
  @Test def multiTest5(): Unit = ()
}

class MultiIgnore2TestAssertions extends JUnitTest
