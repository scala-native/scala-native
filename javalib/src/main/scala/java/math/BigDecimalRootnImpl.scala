package java.math

import java.util.Objects

private[math] object BigDecimalRootnImpl {

  /* Math facts to aid understanding this rootn() implementation:
   *
   *   + By definition, all irrational numbers have an infinitely
   *     repeating fraction. JDK rootn() will throw.
   *
   *   + all rational numbers which are not a perfect root at the requested
   *     degree, have an infinitely repeating fraction. JDK rootn() will throw.
   *
   *   + all rational numbers which are perfect root at requested degree have a
   *     fractional part which eventually terminates.
   */

  /* See additional developer notes in 'BigDecimal def sqrt(mc: MathContext)'.
   * In particular, the 'JDK-8370057' note.
   */

  final val RootNotExactMessage = "Computed root not exact."

  private def adjustFractionalScale(
      bd: BigDecimal,
      minimalScale: scala.Int,
      mc: MathContext
  ): BigDecimal = {
    /* Preconditions:
     *   + bd.scale() > 0. That is, bd has fractional part.
     *   + mc.getRoundingMode() != RoundingMode.UNNECESSARY
     *   + bd has been rounded to mc.scale().
     *   + minimalScale is at least 1.
     */

    val bdNoTZ = bd.stripTrailingZeros()
    val bdNoTZscale = bdNoTZ.scale()

    if (bdNoTZscale >= minimalScale) bdNoTZ
    else bdNoTZ.setScale(minimalScale, mc.getRoundingMode())
  }

  private def biRootAndRemainder(
      radicand: BigDecimal,
      degree: Int
  ): Array[BigInteger] = {
    val radicandNoTZ = radicand.stripTrailingZeros()
    val unsv = radicandNoTZ.unscaledValue()

    unsv.rootnAndRemainder(degree)
  }

  // Always use JDK 26+ practice. See 'JDK-8370057' above
  private def preferredScale(
      radicand: BigDecimal,
      degree: scala.Int
  ): scala.Int =
    Math.ceilDiv(radicand.scale(), degree)

  private def firstGuess(
      radicand: BigDecimal,
      degree: Int,
      mc: MathContext
  ): BigDecimal = {
    // Precondition - radicand is strictly positive, but perhaps < 1.0

    /* BigDecimal.TEN provides a good watershed. It avoids an allocation and
     * sends small values down a more efficient execution path.
     */

    if (radicand.compareTo(BigDecimal.TEN) >= 0) {
      new BigDecimal(
        biRootAndRemainder(radicand, degree)(0),
        preferredScale(radicand, degree)
      )
    } else {
      val radicandExponent = radicand.precision() - radicand.scale() - 1

      // Guess approaches root from one decimal place greater.
      val rootExponent = radicandExponent / degree // no -1

      BigDecimal.ONE.scaleByPowerOfTen(rootExponent)
    }
  }

  private def rootn_Exact(
      radicand: BigDecimal,
      degree: scala.Int
  ): BigDecimal = {
    /* Preconditions:
     *   + mc.precision == 0
     *
     *   + known ZERO and negative radicands have been filtered out,
     *      leaving only strictly positive candidates, possibly with
     *      trailing zeros.
     */

    val radicandNoTZ = radicand.stripTrailingZeros()
    val intRootnRandR = biRootAndRemainder(radicandNoTZ, degree)

    if (intRootnRandR(1).compareTo(BigInteger.ZERO) != 0)
      throw new ArithmeticException(RootNotExactMessage)

    // Careful, scale manipulation is sensitive to small changes.
    (new BigDecimal(intRootnRandR(0), preferredScale(radicandNoTZ, degree)))
      .setScale(preferredScale(radicand, degree))
  }

  private def rootn_Rounded(
      radicand: BigDecimal,
      degree: scala.Int,
      mc: MathContext
  ): BigDecimal = {
    /* Preconditions:
     *   + mc.precision > 0; recall that precision can never be < 0.
     *
     *   + RoundingMode is _never_ RoundingMode.UNNECESSARY. Guard digit
     *     handling depends upon this precondition.
     */

    /* Implement the the Newton-Raphson method.
     *
     * This is the Wikipedia "Square root algorithms"" example modified in
     * three ways. See the just-in-time comments at the usage sites.
     *
     *   + generalized to any positive integer degree
     *
     *   + fewer guard digits are used to save BigMath cycles.
     *
     *   + The loop stopping condition has been changed from using an
     *     epsilon to direct equality.
     */

    /* Choosing the number of extra digits to use as guard digits
     * is a playground for people who enjoy and are skilled in
     * numerical analysis.
     *
     * Conventionally, one is absolute minimum, more are usually recommended.
     * Here an arbitrary number is chosen which appears to be large
     * enough to suffice but which is small enough to avoid doing
     * expensive BigMath on digits which will be discarded and
     * add successively less useful discrimination.
     *
     * Using 0 guardDigits causes BigDecimalTestOnJDK9 unit test for
     * sqrt_Exceptions radicand 144.1 MathContext()precision=7
     * roundingMode=HALF_EVEN) to fail with an 'off-by-one' final digit
     * difference: JVM: 12.0041[7]  SN: 12.0041[6].
     *
     * Using 2 or fewer guard digits causes BigDecimalTestOnJDK27 unit test
     * for MctxDECIMAL32 radicand: 0.05, degree 3,
     * MathContext(precision=7 roundingMode=HALF_EVEN) to fail with an
     * 'off-by-one' final digit difference: JVM: 0.368403[1] SN: 0.368403[2].
     *
     * All current unit-test cases succeed using 3 guard digits.
     * Using 6 guard digits brings slightly runtime expensive peace of mind.
     */

    val nGuardDigits = 6

    val guardMC =
      new MathContext(mc.getPrecision() + nGuardDigits, mc.getRoundingMode())

    val bdDegree = new BigDecimal(degree.toString)

    val degreeMinus1 = degree - 1
    val bdDegreeMinus1 = new BigDecimal(degreeMinus1.toString)

    var guess = firstGuess(radicand, degree, guardMC)

    var done = false

    while (!done) {
      // Newton-Raphson refinement in a four line nutshell.
      val nextGuess = bdDegreeMinus1
        .multiply(guess)
        .add(radicand.divide(guess.pow(degreeMinus1), guardMC))
        .divide(bdDegree, guardMC)

      /* Rounding to the guard digits should keep the number of
       * iterations bounded.
       */

      if (guess.compareTo(nextGuess) == 0)
        done = true // break loop
      else
        guess = nextGuess
    }

    val candidate = guess.round(mc) // utilize & discard guard digits

    if (candidate.scale() <= 0) candidate
    else {
      val ps = preferredScale(radicand, degree)

      /* Preferred or minimal scale, but subject to increase as number of
       * non-zero trailing digits demands.
       */
      adjustFractionalScale(
        candidate,
        preferredScale(radicand, degree),
        mc
      )
    }
  }

  private def rootn_RoundingMode_UNNECESSARY(
      radicand: BigDecimal,
      degree: scala.Int,
      mc: MathContext
  ): BigDecimal = {
    /* Preconditions:
     *   + mc.precision > 0; recall that precision can never be < 0.
     *
     *   + mc.getRoundingMode() is RoundingMode.UNNECESSARY.
     */

    // Will be a perfect power or throw.
    val root = rootn_Exact(radicand, degree)

    if (root.scale() <= 0) { // no decimal point, no worries
      root
    } else if (root.precision() <= mc.getPrecision()) {
      root // can fit as-is into required precision.
    } else { // Try fitting with JVM quasi-cannonical most compact form.
      val rootNoTZ = root.stripTrailingZeros()

      if (rootNoTZ.precision() > mc.getPrecision())
        throw new ArithmeticException(RootNotExactMessage)
      else { // Now there are no trailing non-zero digits beyond mc.precison().
        /* round(mc) is mildly surprising but does a better job at matching JVM
         * handling of possible trailing zeros than other approaches tried.
         *
         * For example, the unit-test case radicand "2.000"
         * MathContext(2, RoundingMode.UNNECESSARY) where precision
         * limits the JVM result to "2.0" where preferredScale() alone
         * yields "2.00".
         */

        root.round(mc)
      }
    }
  }

  /** @since 27 */
  def rootn(
      radicand: BigDecimal,
      degree: scala.Int,
      mc: MathContext
  ): BigDecimal = {
    /* Return principal (largest) root for requested degree is returned or
     * throw an Exception.
     *
     * This is a first implementation, with plenty of room for improvement
     * as demand and resources allow.
     */

    // JDK 27 tests arguments in this order.

    if (degree == 0)
      throw new ArithmeticException("Zero root degree")

    if (degree == Integer.MIN_VALUE)
      throw new ArithmeticException(
        "Overflow to represent absolute value of Integer.MIN_VALUE"
      )

    val signum = radicand.signum()

    if (((degree & 1) == 0) && (signum < 0)) {
      /* sqrt() on Java 26 and probably back to 9 uses text
       * "Attempted square root of negative BigDecimal"
       *
       * SN always uses the Java 27 and counting text in both sqrt() and
       * rootn().
       */
      throw new ArithmeticException(
        "Negative radicand with even root degree"
      )
    }

    if (((degree < 0) && (signum == 0)))
      throw new ArithmeticException(
        "Zero radicand with negative root degree"
      )

    // SN always throws. JDK 25+ sqrt(mc) throws, but earlier version do not.
    Objects.requireNonNull(
      mc,
      """Cannot read field "roundingMode" because "mc" is null"""
    )

    if (signum == 0)
      BigDecimal.ZERO.setScale(preferredScale(radicand, degree))
    else if (mc.getPrecision() == 0)
      rootn_Exact(radicand, degree)
    else if (mc.getRoundingMode() != RoundingMode.UNNECESSARY)
      rootn_Rounded(radicand, degree, mc)
    else
      rootn_RoundingMode_UNNECESSARY(radicand, degree, mc)
  }
}
