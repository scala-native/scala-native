package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class MultiAssumeFail1Test {
  @Test def multiTest1(): Unit = {
    assumeTrue("This assume should not pass", false)
  }

  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
  @Test def multiTest4(): Unit = ()
  @Test def multiTest5(): Unit = ()
}

class MultiAssumeFail1TestAssertions extends JUnitTest
