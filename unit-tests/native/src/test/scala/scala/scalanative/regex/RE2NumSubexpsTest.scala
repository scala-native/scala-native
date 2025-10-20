package scala.scalanative
package regex

import org.junit.Test
import org.junit.Assert.*

class RE2NumSubexpsTest {
  private val NUM_SUBEXP_CASES = Array(
    Array("", "0"),
    Array(".*", "0"),
    Array("abba", "0"),
    Array("ab(b)a", "1"),
    Array("ab(.*)a", "1"),
    Array("(.*)ab(.*)a", "2"),
    Array("(.*)(ab)(.*)a", "3"),
    Array("(.*)((a)b)(.*)a", "4"),
    Array("(.*)(\\(ab)(.*)a", "3"),
    Array("(.*)(\\(a\\)b)(.*)a", "3")
  )

  @Test def numSubexp(): Unit = {
    for (Array(input, _expected) <- NUM_SUBEXP_CASES) {
      val expected = _expected.toInt
      assertTrue(
        "numberOfCapturingGroups(" + input + ")",
        expected == RE2.compile(input).numberOfCapturingGroups()
      )
    }
  }
}
