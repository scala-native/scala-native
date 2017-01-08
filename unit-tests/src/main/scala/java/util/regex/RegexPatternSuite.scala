package java.util.regex

object RegexPatternSuite extends tests.Suite {
  test("Expects abc to match abc") {
    assert(Pattern.matches("abc", "abc"))
  }
  test("Expects [[:digit:]] to match 123") {
    assert(Pattern.matches("[[:digit:]]", "123"))
  }
  test("Expects [[:digit:]] to match 1") {
    assert(Pattern.matches("[[:digit:]]", "1")) // This is segfaulting!
  }
  test("Expects Scal.-Native to match Scala-Native") {
    assert(Pattern.matches("Scal.-Native", "Scala-Native"))
  }
}
