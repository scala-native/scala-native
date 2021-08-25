package scala.scalanative.junit

// Ported from Scala.js

import org.junit._

import scala.scalanative.junit.utils.JUnitTest

class MultiIgnore1Test {
  @Ignore @Test def multiTest1(): Unit = ()
  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
  @Test def multiTest4(): Unit = ()
  @Test def multiTest5(): Unit = ()
}

class MultiIgnore1TestAssertions extends JUnitTest
