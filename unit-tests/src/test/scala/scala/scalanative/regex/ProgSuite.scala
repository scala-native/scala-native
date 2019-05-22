package scala.scalanative
package regex

object ProgSuite extends tests.Suite {
  private val COMPILE_TESTS = Array(
    Array("a",
          "0       fail\n" + "1*      rune1 \"a\" -> 2\n" + "2       match\n"),
    Array(
      "[A-M][n-z]",
      "0       fail\n" + "1*      rune \"AM\" -> 2\n" + "2       rune \"nz\" -> 3\n" + "3       match\n"),
    Array("", "0       fail\n" + "1*      nop -> 2\n" + "2       match\n"),
    Array(
      "a?",
      "0       fail\n" + "1       rune1 \"a\" -> 3\n" + "2*      alt -> 1, 3\n" + "3       match\n"),
    Array(
      "a??",
      "0       fail\n" + "1       rune1 \"a\" -> 3\n" + "2*      alt -> 3, 1\n" + "3       match\n"),
    Array(
      "a+",
      "0       fail\n" + "1*      rune1 \"a\" -> 2\n" + "2       alt -> 1, 3\n" + "3       match\n"),
    Array(
      "a+?",
      "0       fail\n" + "1*      rune1 \"a\" -> 2\n" + "2       alt -> 3, 1\n" + "3       match\n"),
    Array(
      "a*",
      "0       fail\n" + "1       rune1 \"a\" -> 2\n" + "2*      alt -> 1, 3\n" + "3       match\n"),
    Array(
      "a*?",
      "0       fail\n" + "1       rune1 \"a\" -> 2\n" + "2*      alt -> 3, 1\n" + "3       match\n"),
    Array(
      "a+b+",
      "0       fail\n" + "1*      rune1 \"a\" -> 2\n" + "2       alt -> 1, 3\n" + "3       rune1 \"b\" -> 4\n" + "4       alt -> 3, 5\n" + "5       match\n"),
    Array(
      "(a+)(b+)",
      "0       fail\n" + "1*      cap 2 -> 2\n" + "2       rune1 \"a\" -> 3\n" + "3       alt -> 2, 4\n" + "4       cap 3 -> 5\n" + "5       cap 4 -> 6\n" + "6       rune1 \"b\" -> 7\n" + "7       alt -> 6, 8\n" + "8       cap 5 -> 9\n" + "9       match\n"
    ),
    Array(
      "a+|b+",
      "0       fail\n" + "1       rune1 \"a\" -> 2\n" + "2       alt -> 1, 6\n" + "3       rune1 \"b\" -> 4\n" + "4       alt -> 3, 6\n" + "5*      alt -> 1, 3\n" + "6       match\n"
    ),
    Array(
      "A[Aa]",
      "0       fail\n" + "1*      rune1 \"A\" -> 2\n" + "2       rune \"A\"/i -> 3\n" + "3       match\n"),
    Array(
      "(?:(?:^).)",
      "0       fail\n" + "1*      empty 4 -> 2\n" + "2       anynotnl -> 3\n" + "3       match\n")
  )

  test("compile") {
    for (Array(input, expected) <- COMPILE_TESTS) {
      val re = Parser.parse(input, RE2.PERL)
      val p  = Compiler.compileRegexp(re)
      val s  = p.toString
      assert(expected == s, "compiled: " + input)
    }
  }
}
