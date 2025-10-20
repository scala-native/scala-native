package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.Test

import scala.scalanative.junit.utils.JUnitTest

class MultiAssumeFail2Test {
  @Test def multiTest1(): Unit = {
    assumeTrue("This assume should not pass", false)
  }
  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
  @Test def multiTest4(): Unit = {
    assumeTrue("This assume should not pass", false)
  }
  @Test def multiTest5(): Unit = ()
}

class MultiAssumeFail2TestAssertions extends JUnitTest
