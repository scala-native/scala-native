package scala

import org.junit.Test
import org.junit.Assert._

class HashCodeTest {
  case class MyData(string: String, num: Int)

  @Test def hashCodeOfStringMatchesScalaJVM(): Unit = {
    assertTrue("hello".hashCode == 99162322)
  }

  @Test def hashCodeOfCaseClassMatchesScalaJVM(): Unit = {
    assertTrue(MyData("hello", 12345).hashCode == -1824015247)
  }
}
