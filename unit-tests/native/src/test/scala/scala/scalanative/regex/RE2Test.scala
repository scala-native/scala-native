package scala.scalanative
package regex

import org.junit.Assert._
import org.junit.Test

/** Tests of RE2 API. */
class RE2Test {
  @Test def fullMatch(): Unit = {
    assertTrue(
      new RE2("ab+c").match_("abbbbbc", 0, 7, RE2.ANCHOR_BOTH, null, 0)
    )
    assertFalse(
      new RE2("ab+c").match_("xabbbbbc", 0, 8, RE2.ANCHOR_BOTH, null, 0)
    )
  }

  @Test def findEnd(): Unit = {
    val r = new RE2("abc.*def")
    assertTrue(r.match_("yyyabcxxxdefzzz", 0, 15, RE2.UNANCHORED, null, 0))
    assertTrue(r.match_("yyyabcxxxdefzzz", 0, 12, RE2.UNANCHORED, null, 0))
    assertTrue(r.match_("yyyabcxxxdefzzz", 3, 15, RE2.UNANCHORED, null, 0))
    assertTrue(r.match_("yyyabcxxxdefzzz", 3, 12, RE2.UNANCHORED, null, 0))
    assertFalse(r.match_("yyyabcxxxdefzzz", 4, 12, RE2.UNANCHORED, null, 0))
    assertFalse(r.match_("yyyabcxxxdefzzz", 3, 11, RE2.UNANCHORED, null, 0))
  }
}
