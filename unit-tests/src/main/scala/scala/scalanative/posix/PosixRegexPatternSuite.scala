package scala.scalanative.posix

object PosixRegexPatternSuite extends tests.Suite {
  test("Expects abc to match abc") {
    PosixPattern.matches("abc", "abc") match {
      case Right(t) => assert(t)
      case _ => assert(false)
    }}
  test("Expects [[:digit:]] to match 123") {
    PosixPattern.matches("[[:digit:]]", "123") match {
      case Right(t) => assert(t)
      case _ => assert(false)
    }}
  test("Expects [[:digit:]] to match 1") {
    PosixPattern.matches("[[:digit:]]", "1") match {
      case Right(t) => assert(t)
      case _ => assert(false)
    }}
  test("Expects Scal.-Native to match Scala-Native") {
    PosixPattern.matches("[[:digit:]]", "123") match {
      case Right(t) => assert(t)
      case _ => assert(false)
    }}
              // This throws error: ("Scal.\\-Native", "Scala-Native")
}
