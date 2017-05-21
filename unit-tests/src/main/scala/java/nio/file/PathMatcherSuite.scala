package java.nio.file

object PathMatcherSuite extends tests.Suite {

  test("supports `regex` syntax") {
    val matcher = getMatcher("regex:fo*")
    assert(matcher.matches(Paths.get("foo")))
  }

  test("throws UnsupportedOperationException when an unknown syntax is used") {
    assertThrows[UnsupportedOperationException] {
      getMatcher("foobar:blabla")
    }
  }

  test(
    "throws IllegalArgumentException if the parameter doesn't have the form: syntax:pattern") {
    assertThrows[IllegalArgumentException] {
      getMatcher("helloworld")
    }
  }

  private def getMatcher(syntaxAndPattern: String): PathMatcher =
    FileSystems.getDefault().getPathMatcher(syntaxAndPattern)
}
