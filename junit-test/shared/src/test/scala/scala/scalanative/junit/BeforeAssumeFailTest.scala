package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object BeforeAssumeFailTest {
  @BeforeClass def beforeClass(): Unit = {
    assumeTrue("This assume should not pass", false)
  }
}

class BeforeAssumeFailTest {
  @Test def test(): Unit = ()
}

class BeforeAssumeFailTestAssertions extends JUnitTest
