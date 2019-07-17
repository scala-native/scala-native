package scala.scalanative
package regex

/** Tests of RE2 API. */
object RE2Suite extends tests.Suite {
  test("FullMatch") {
    assert(new RE2("ab+c").match_("abbbbbc", 0, 7, RE2.ANCHOR_BOTH, null, 0))
    assert(!new RE2("ab+c").match_("xabbbbbc", 0, 8, RE2.ANCHOR_BOTH, null, 0))
  }

  test("FindEnd") {
    val r = new RE2("abc.*def")
    assert(r.match_("yyyabcxxxdefzzz", 0, 15, RE2.UNANCHORED, null, 0))
    assert(r.match_("yyyabcxxxdefzzz", 0, 12, RE2.UNANCHORED, null, 0))
    assert(r.match_("yyyabcxxxdefzzz", 3, 15, RE2.UNANCHORED, null, 0))
    assert(r.match_("yyyabcxxxdefzzz", 3, 12, RE2.UNANCHORED, null, 0))
    assert(!r.match_("yyyabcxxxdefzzz", 4, 12, RE2.UNANCHORED, null, 0))
    assert(!r.match_("yyyabcxxxdefzzz", 3, 11, RE2.UNANCHORED, null, 0))
  }
}
