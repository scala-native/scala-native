package java.math

object BigDecimalSuite extends tests.Suite {

  // Editorial comment:
  //
  // You may think now that this file is excessively commented.
  // The author of those comments agrees. Any file requiring this
  // much commenting means that you are entering the land of deep
  // sneakers and exceedingly bad fortune!
  //
  // Wait until you have to chase accuracy issues, such as  "least
  // significant digits differing by one". You might want to consider again.

// sqrt

  test("Testing sqrt") {}

  test("* null MathContext should throw NPE") {
    assertThrows[NullPointerException] {
      val bd = BigDecimal.ONE.negate
      bd.sqrt(null: MathContext)
    }
  }

  test("* of negative value should throw") {
    assertThrows[ArithmeticException] {
      val bd = BigDecimal.ONE.negate
      bd.sqrt(MathContext.DECIMAL64)
    }
  }

  test("* of 0 should reduce scale to floor(original/2.0)") {

    val scale    = 9
    val expected = scale / 2 // expect 4, integer division.

    val bd = BigDecimal.ZERO
      .setScale(scale, MathContext.DECIMAL64.getRoundingMode)

    val result = bd.sqrt(MathContext.DECIMAL64).scale

    assert(
      result == expected,
      s"sqrt ${0} scale: ${scale} " +
        s"result: ${result} != expected: ${expected}"
    )
  }

  test("* of 2.0 should be correct to 16 decimal places") {

    // Why is precision 17, not 16 decimal places in test title"
    // A number comments in tests below refer to this comment for
    // the answer.
    //
    // By observation, not any deep understanding Scala JVM and
    // perhaps Java seem to count the inferred 0 or 1 in an IEEE754
    // float as part of the precision. Thus to get what the rest
    // of the world would call 16 digits of decimal precision, one must
    // add one. But Yes, you say, we are dealing with BigDecimal here,
    // not IEEE754 floats. Same behavior seems to rule, for consistency?

    val precision = 17

    val startWith = "2.0"

    val radicand: BigDecimal = new BigDecimal(
      startWith,
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    // URL: http://www.cs.utsa.edu/~wagner/CS3343/newton/sqrt.html
    // reference value:             1.41421356237309504880
    // Java 10:                     1.41421356237309505  // precision 18
    // Scala REPL Java8 math.sqrt   1.4142135623730951
    val expected = new BigDecimal(
      "1.4142135623730951", // Scala REPL/JVM
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    val root: BigDecimal =
      radicand.sqrt(new MathContext(precision, RoundingMode.HALF_UP))

    assert(
      root.compareTo(expected) == 0,
      s"root: |${root}| != expected: |${expected}|"
    )
  }

  test("* of e (2.7) should be correct to 100 decimal places, HALF_EVEN") {

    val precision = 100

    // format: off
    val startWith = "2.7182818284590452353602874713526624977572470936999" +
                      "59574966967627724076630353547594571382178525166427"
    // format: on

    val radicand: BigDecimal = new BigDecimal(
      startWith,
      new MathContext(precision, RoundingMode.HALF_EVEN)
    )

    // With thanks to http://www.wolframalpha.com for results of query:
    // ""square root of base of natural logarithm to 100 digits""
    //
    // "square root of  to 100 digits". 10 * sqrt(10) is:
    //
    // reference value:
    // 1.648721270700128146848650787814163571653776100710148
    //   011575079311640661021194215608632776520056366643
    // Java 10:                     Available but not calculated
    // Scala REPL Java8 math.sqrt   Not Available.

    // format: off
    val reference = "1.648721270700128146848650787814163571653776100710148" +
                      "011575079311640661021194215608632776520056366643"
    // format: on

    val expected = new BigDecimal(
      reference,
      new MathContext(precision, RoundingMode.HALF_EVEN)
    )

    val root: BigDecimal =
      radicand.sqrt(new MathContext(precision, RoundingMode.HALF_EVEN))

    assert(
      root.compareTo(expected) == 0,
      s"root: |${root}| != expected: |${expected}|"
    )
  }

  test("* of 3.0 should be correct to 16 decimal places") {

    // see comment in test of sqrt(2) for why this is 17, not 16.
    val precision = 17

    val startWith = "3.0"

    val radicand: BigDecimal = new BigDecimal(
      startWith,
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    // Digits of square root of three, April 1994  URL:
    //   https://apod.nasa.gov/htmltest/gifcity/sqrt3.1mil
    //
    // reference value:             1.7320508075688772935
    // Java 10:                     1.73205080756887729 // precision 18
    // Java 10:                     1.7320508075688773  // precision 17
    // Scala REPL Java8 math.sqrt   1.7320508075688772

    val expected = new BigDecimal(
      "1.7320508075688773",
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    val root: BigDecimal =
      radicand.sqrt(new MathContext(precision, RoundingMode.HALF_UP))

    assert(
      root.compareTo(expected) == 0,
      s"root: |${root}| != expected: |${expected}|"
    )

  }

  test("* of 1000.0 (negative scale) should be correct, HALF_UP") {
    // This tests both having digits to the left of the decimal point
    // (negative scale) and sqrt(5).
    // sqrt(100) * sqrt(10) == 100 * sqrt(2) * sqrt(5)

    // see comment in test of sqrt(2) for why this is 17, not 16.
    val precision = 17

    val startWith = "1000.0"

    val radicand: BigDecimal = new BigDecimal(
      startWith,
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    // With thanks to http://www.wolframalpha.com for results of query:
    // "square root of 1000 to 100 digits". Only a few of those digit are
    // used here.
    //
    // reference value:             31.62277660168379332
    // Java 10:                     31.622776601683793  // precision 17
    // Scala REPL Java8 math.sqrt   31.622776601683793

    val expected = new BigDecimal(
      "31.622776601683793",
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    val root: BigDecimal =
      radicand.sqrt(new MathContext(precision, RoundingMode.HALF_UP))

    assert(
      root.compareTo(expected) == 0,
      s"root: |${root}| != expected: |${expected}|"
    )

  }

  test("* of 1000.0 (negative scale) should be correct, HALF_EVEN") {

    // see comment in test of sqrt(2) for why this is 17, not 16.
    val precision = 17

    val startWith = "1000.0"
    val radicand: BigDecimal = new BigDecimal(
      startWith,
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    // reference value:             31.62277660168379332
    // Java 10:                     31.622776601683793  // precision 17
    // Scala REPL Java8 math.sqrt   31.622776601683793

    val expected = new BigDecimal(
      "31.622776601683793",
      new MathContext(precision, RoundingMode.HALF_EVEN)
    )

    val root: BigDecimal =
      radicand.sqrt(new MathContext(precision, RoundingMode.HALF_EVEN))

    assert(
      root.compareTo(expected) == 0,
      s"root: |${root}| != expected: |${expected}|"
    )

  }

  test("* of 1000000.0 (large number) should be correct") {

    // see comment in test of sqrt(2) for why this is 17, not 16.
    val precision = 17

    val startWith = "1000000.0"

    val radicand: BigDecimal = new BigDecimal(
      startWith,
      new MathContext(precision, RoundingMode.HALF_UP)
    )

    // reference value:             1000.00, by simple math.

    val expected =
      new BigDecimal("1000.0", new MathContext(precision, RoundingMode.HALF_UP))

    val root: BigDecimal =
      radicand.sqrt(new MathContext(precision, RoundingMode.HALF_UP))

    assert(
      root.compareTo(expected) == 0,
      s"root: |${root}| != expected: |${expected}|"
    )
  }

}
