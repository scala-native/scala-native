package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object MultiBeforeAssumeFailTest {
  @BeforeClass def beforeClass(): Unit = {
    assumeTrue("This assume should not pass", false)
  }
}

class MultiBeforeAssumeFailTest {
  @Test def multiTest1(): Unit = ()
  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
  @Test def multiTest4(): Unit = ()
  @Test def multiTest5(): Unit = ()
}

class MultiBeforeAssumeFailTestAssertions extends JUnitTest
