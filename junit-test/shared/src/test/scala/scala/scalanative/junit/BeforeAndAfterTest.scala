package scala.scalanative.junit

// Ported from Scala.js

import org.junit.*

import scala.scalanative.junit.utils.JUnitTest

object BeforeAndAfterTest {
  @BeforeClass def beforeClass(): Unit = ()
  @AfterClass def afterClass(): Unit = ()
}

class BeforeAndAfterTest {
  @Before def before(): Unit = ()
  @After def after(): Unit = ()
  @Test def test(): Unit = ()
}

class BeforeAndAfterTestAssertions extends JUnitTest
