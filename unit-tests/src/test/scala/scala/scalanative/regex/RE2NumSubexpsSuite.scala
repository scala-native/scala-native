package scala.scalanative
package regex

object RE2NumSubexpsSuite extends tests.Suite {
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

  test("NumSubexp") {
    for (Array(input, _expected) <- NUM_SUBEXP_CASES) {
      val expected = _expected.toInt
      assert(expected == RE2.compile(input).numberOfCapturingGroups,
             "numberOfCapturingGroups(" + input + ")")
    }
  }
}
