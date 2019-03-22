package scala.re2s


object SimplifySuite extends tests.Suite {

  private val SIMPLIFY_TESTS = Array(
    // Already-simple constructs
    Array("a", "a"),
    Array("ab", "ab"),
    Array("a|b", "[a-b]"),
    Array("ab|cd", "ab|cd"),
    Array("(ab)*", "(ab)*"),
    Array("(ab)+", "(ab)+"),
    Array("(ab)?", "(ab)?"),
    Array(".", "(?s:.)"),
    Array("^", "^"),
    Array("$", "$"),
    Array("[ac]", "[ac]"),
    Array("[^ac]", "[^ac]"),
    // Posix character classes
    Array("\\p{Alpha}", "[0-9A-Za-z]"),
    Array("\\p{Alnum}", "[A-Za-z]"),
    Array("\\p{Blank}", "[\\t ]"),
    Array("\\p{Cntrl}", "[\\x00-\\x1f\\x7f]"),
    Array("\\p{Digit}", "[0-9]"),
    Array("\\p{Graph}", "[!-~]"),
    Array("\\p{Lower}", "[a-z]"),
    Array("\\p{Print}", "[ -~]"),
    Array("\\p{Punct}", "[!-/:-@\\[-`\\{-~]"),
    Array("\\p{Space}", "[\\t-\\r ]"),
    Array("\\p{Upper}", "[A-Z]"),
    Array("\\p{XDigit}", "[0-9A-Fa-f]"),
    // Perl character classes
    Array("\\d", "[0-9]"),
    Array("\\s", "[\\t-\\n\\f-\\r ]"),
    Array("\\w", "[0-9A-Z_a-z]"),
    Array("\\D", "[^0-9]"),
    Array("\\S", "[^\\t-\\n\\f-\\r ]"),
    Array("\\W", "[^0-9A-Z_a-z]"),
    Array("[\\d]", "[0-9]"),
    Array("[\\s]", "[\\t-\\n\\f-\\r ]"),
    Array("[\\w]", "[0-9A-Z_a-z]"),
    Array("[\\D]", "[^0-9]"),
    Array("[\\S]", "[^\\t-\\n\\f-\\r ]"),
    Array("[\\W]", "[^0-9A-Z_a-z]"),
    // Posix repetitions
    Array("a{1}", "a"),
    Array("a{2}", "aa"),
    Array("a{5}", "aaaaa"),
    Array("a{0,1}", "a?"),
    // The next three are illegible because Simplify inserts (?:)
    // parens instead of () parens to avoid creating extra
    // captured subexpressions.  The comments show a version with fewer parens.
    Array("(a){0,2}", "(?:(a)(a)?)?"),
    //       (aa?)?
    Array("(a){0,4}", "(?:(a)(?:(a)(?:(a)(a)?)?)?)?"),
    //   (a(a(aa?)?)?)?
    Array("(a){2,6}", "(a)(a)(?:(a)(?:(a)(?:(a)(a)?)?)?)?"),
    // aa(a(a(aa?)?)?)?
    Array("a{0,2}", "(?:aa?)?"),
    Array("a{0,4}", "(?:a(?:a(?:aa?)?)?)?"),
    Array("a{2,6}", "aa(?:a(?:a(?:aa?)?)?)?"),
    Array("a{0,}", "a*"),
    Array("a{1,}", "a+"),
    Array("a{2,}", "aa+"),
    Array("a{5,}", "aaaaa+"),
    // Test that operators simplify their arguments.
    Array("(?:a{1,}){1,}", "a+"),
    Array("(a{1,}b{1,})", "(a+b+)"),
    Array("a{1,}|b{1,}", "a+|b+"),
    Array("(?:a{1,})*", "(?:a+)*"),
    Array("(?:a{1,})+", "a+"),
    Array("(?:a{1,})?", "(?:a+)?"),
    Array("", "(?:)"),
    Array("a{0}", "(?:)"),
    // Character class simplification
    Array("[ab]", "[a-b]"),
    Array("[a-za-za-z]", "[a-z]"),
    Array("[A-Za-zA-Za-z]", "[A-Za-z]"),
    Array("[ABCDEFGH]", "[A-H]"),
    Array("[AB-CD-EF-GH]", "[A-H]"),
    Array("[W-ZP-XE-R]", "[E-Z]"),
    Array("[a-ee-gg-m]", "[a-m]"),
    Array("[a-ea-ha-m]", "[a-m]"),
    Array("[a-ma-ha-e]", "[a-m]"),
    Array("[a-zA-Z0-9 -~]", "[ -~]"),
    // Unicode case folding.
    Array("(?i)A", "(?i:A)"),
    Array("(?i)a", "(?i:A)"),
    Array("(?i)[A]", "(?i:A)"),
    Array("(?i)[a]", "(?i:A)"),
    Array("(?i)K", "(?i:K)"),
    Array("(?i)k", "(?i:K)"),
    Array("(?i)\\x{212a}", "(?i:K)"),
    Array("(?i)[K]", "[Kk\u212A]"),
    Array("(?i)[k]", "[Kk\u212A]"),
    Array("(?i)[\\x{212a}]", "[Kk\u212A]"),
    Array("(?i)[a-z]", "[A-Za-z\u017F\u212A]"),
    Array("(?i)[\\x00-\\x{FFFD}]", "[\\x00-\uFFFD]"),
    Array("(?i)[\\x00-\\x{10FFFF}]", "(?s:.)"),
    // Empty string as a regular expression.
    // The empty string must be preserved inside parens in order
    // to make submatches work right, so these tests are less
    // interesting than they might otherwise be.  String inserts
    // explicit (?:) in place of non-parenthesized empty strings,
    // to make them easier to spot for other parsers.
    Array("(a|b|)", "([a-b]|(?:))"),
    Array("(|)", "()"),
    Array("a()", "a()"),
    Array("(()|())", "(()|())"),
    Array("(a|)", "(a|(?:))"),
    Array("ab()cd()", "ab()cd()"),
    Array("()", "()"),
    Array("()*", "()*"),
    Array("()+", "()+"),
    Array("()?", "()?"),
    Array("(){0}", "(?:)"),
    Array("(){1}", "()"),
    Array("(){1,}", "()+"),
    Array("(){0,2}", "(?:()()?)?"),
    Array("(?:(a){0})", "(?:)")
  )
  test("simplify") {
    SIMPLIFY_TESTS.foreach {
      case Array(input, expected) =>
        val re = Parser.parse(input, RE2.MATCH_NL | RE2.PERL & ~RE2.ONE_LINE)
        val s  = Simplify.simplify(re).toString
        assert(expected == s, String.format("simplify(%s)", input))
    }
  }
}
