package scala.issues

import org.junit.Test
import org.junit.Assert._

class Scala3IssuesTest:

  // Test itself does not have a large value, it does however assert that
  // usage of macros in the code, does not break compiler plugin
  @Test def canUseMacros(): Unit = {
    val result = Macros.test("foo")
    assertEquals(List(1, 2, 3), result)
  }
