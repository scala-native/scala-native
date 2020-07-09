package junit

import org.junit.Assert._
import org.junit.Test

class JUnitExampleTest {
  @Test def exampleTest(): Unit = {
    assertTrue("This assertion should pass", true)
  }
}
