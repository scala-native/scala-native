package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class Multi1Test {
  @Test def multiTest1(): Unit = ()
  @Test def multiTest2(): Unit = ()
}

class Multi1TestAssertions extends JUnitTest
