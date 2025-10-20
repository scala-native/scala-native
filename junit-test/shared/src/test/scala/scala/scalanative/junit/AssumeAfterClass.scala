package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Assume.*
import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object AssumeAfterClassTest {
  @AfterClass def afterClass(): Unit = {
    assumeTrue("This assume should not pass", false)
  }
}

class AssumeAfterClassTest {
  @Test def test(): Unit = ()
}

class AssumeAfterClassTestAssertions extends JUnitTest
