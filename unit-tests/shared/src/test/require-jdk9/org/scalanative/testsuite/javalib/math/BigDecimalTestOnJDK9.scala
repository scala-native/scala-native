package org.scalanative.testsuite.javalib.math

import java.math.{BigDecimal, MathContext, RoundingMode}
import java.util.Arrays
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class BigDecimalTestOnJDK9 {

// sqrt

  private def fixupExpectedByJdkVersion(
      expectedOnJdkGe26: String
  ): BigDecimal = {
    val fixedUp =
      if (Platform.executingInScalaNative ||
          Platform.executingInJVMWithJDKIn(26 to Integer.MAX_VALUE)) {
        expectedOnJdkGe26
      } else {
        // assume expectedOnJdkExpected26 is not null or empty.
        val s1 = expectedOnJdkGe26.substring(0, expectedOnJdkGe26.length - 1)
        if (s1.endsWith(".")) s1.substring(0, expectedOnJdkGe26.length - 1)
        else s1
      }

    new BigDecimal(fixedUp)
  }

//  final case class SqrtTestCase(
  case class SqrtTestCase(
      radicand: BigDecimal,
      expected: BigDecimal
  )

  final val sqrtTestCases_Mctx32 = Array(
    SqrtTestCase(new BigDecimal("2.0"), new BigDecimal("1.414214")),
    SqrtTestCase(new BigDecimal("0.04"), new BigDecimal("0.2")),
    SqrtTestCase(new BigDecimal("0.05"), new BigDecimal("0.2236068"))
  )

  final val sqrtTestCases_Mctx64 = Array(
    SqrtTestCase(new BigDecimal("2.0"), new BigDecimal("1.414213562373095")),
    SqrtTestCase(new BigDecimal("0.04"), new BigDecimal("0.2")),
    SqrtTestCase(new BigDecimal("0.05"), new BigDecimal("0.223606797749979"))
  )

  final val sqrtTestCases_Mctx128 = Array(
    SqrtTestCase(
      new BigDecimal("2.0"),
      new BigDecimal("1.414213562373095048801688724209698")
    ),
    SqrtTestCase(new BigDecimal("0.04"), new BigDecimal("0.2")),
    SqrtTestCase(
      new BigDecimal("0.05"),
      new BigDecimal("0.2236067977499789696409173668731276")
    )
  )

  /* Test cases for MathContext.UNLIMITED, which uses precision=0.
   * The mc.RoundingMode is documented as unused when precision=0.
   *
   * Here the RoundingMode is RoundingMode.HALF_EVEN. If there ever were a
   * sneak path which did use RoundingMode, this scenario would still
   * be distinguished from others which exercise special paths of
   * RoundingMode.UNNECESSARY which might have similar behavior.
   */
  final val sqrtTestCases_MctxUNLIMITED = Array(
    /* Note: This table is  similar to sqrtTestCases_MctxCustom but one
     *       expected value differs. The 'why' of that difference is
     *       best left to JVM internal devos. Scala Native matches the JVM
     *       behavior, even if that seems strange.
     */
    SqrtTestCase(
      new BigDecimal("4"),
      new BigDecimal("2") // used preferred scale
    ),
    SqrtTestCase(
      new BigDecimal("4.0"),
      fixupExpectedByJdkVersion("2.0")
    ),
    SqrtTestCase(
      new BigDecimal("4.00"),
      new BigDecimal("2.0") // only 1 trailing zero
    ),
    SqrtTestCase(
      new BigDecimal("4.000"),
      /* Two trailing zeros, differs from mctxUNNECESSARY because
       * MathContet precisions differ.
       */
      fixupExpectedByJdkVersion("2.00")
    ),
    SqrtTestCase(
      new BigDecimal("9.0"),
      fixupExpectedByJdkVersion("3.0")
    )
  )

  /* Test cases for user-defined custom MathContext. Here
   *   val mc = new MathContext(2, RoundingMode.UNNECESSARY)
   *
   *  Using precision greater than zero and RoundingMode.UNNECESSARY,
   *  the radicands should all be perfect
   *  squares whose sqrt() fits into the indicted precision.
   */
  final val sqrtTestCases_MctxCustom = Array(
    SqrtTestCase(
      new BigDecimal("4"),
      new BigDecimal("2") // used preferred scale
    ),
    SqrtTestCase(
      new BigDecimal("4.0"),
      fixupExpectedByJdkVersion("2.0")
    ),
    SqrtTestCase(
      /* Test: root exceeds mc.precision but root where all but
       * the first fractional zero, if any, have been removed does.
       */
      new BigDecimal("4.00"),
      new BigDecimal("2.0") // only 1 trailing zero, because mc precision=2
    ),
    SqrtTestCase(
      // true root is 2.00 but expected is limited by mc precision=2 to 2.0
      new BigDecimal("4.000"),
      new BigDecimal("2.0")
    ),
    SqrtTestCase(
      new BigDecimal("9.0"),
      fixupExpectedByJdkVersion("3.0")
    )
  )

  @Test def sqrt_Exceptions(): Unit = {
    /* Exception of expected type is expected but exact message text can vary.
     * Better if they match but things change over time and people
     * sometimes use local customizations.
     */

    if (Platform.executingInJVMWithJDKIn(25 to Integer.MAX_VALUE)) {
      val bd = new BigDecimal("1.0")
      assertThrows(
        "sqrt(null) on Scala Native or JDK >= 25",
        classOf[NullPointerException],
        bd.sqrt(null)
      )
    }

    locally {
      /* Provoke JVM
       *  'ArithmeticException: Attempted square root of negative BigDecimal'
       */
      val bd = new BigDecimal("-2.0")
      val mc = MathContext.DECIMAL64
      assertThrows(
        "sqrt(negative)",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )
    }

    locally { // mc.precision=0
      // Provoke JVM 'ArithmeticException: Computed square root not exact'
      val bd = new BigDecimal("3.99")
      val mc = new MathContext(0, RoundingMode.FLOOR)
      assertThrows(
        "sqrt() not exact, mc(0, RoundingMode.FLOOR)",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )
    }

    locally { // mc.precision=5
      // Provoke JVM 'ArithmeticException: Computed square root not exact'
      val bd = new BigDecimal("3.99")
      val mc = new MathContext(5, RoundingMode.UNNECESSARY)
      assertThrows(
        "sqrt() not exact, mc(5, RoundingMode.UNNECESSARY)",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )
    }

    // Check RoundingModes with not-a-perfect square.
    locally {
      val bd = new BigDecimal("144.1")

      val mc = MathContext.UNLIMITED
      assertThrows(
        s"trailing 1 mc: ${mc}",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )

      /* One would expect a more idiomtic Scala case class here.
       * Use parallel arrays instead to get the test up and running.
       *
       * For some time wasting reason case classes were slaying the
       * build optimizer. Problem for another time.
       */
      val mathContexts = Array(
        MathContext.DECIMAL32,
        MathContext.DECIMAL64,
        MathContext.DECIMAL128
      )

      val expected = Array(
        "12.00417",
        "12.0041659435381",
        "12.00416594353810155624530784259113"
      )

      for (j <- 0 until mathContexts.length) {
        val mc = mathContexts(j)
        assertEquals(
          s"mc: ${mc}",
          expected(j),
          bd.sqrt(mc).toPlainString()
        )
      }
    }
  }

  @Test def sqrt_Zero(): Unit = {
    // Intentionally test for reference equality, per the JVM description.

    val bdZero = new BigDecimal("0.0")

    locally {
      val mc = MathContext.DECIMAL32
      assertTrue(
        "DECIMAL32",
        bdZero.sqrt(mc).eq(BigDecimal.ZERO)
      )
    }

    locally {
      val mc = MathContext.DECIMAL64
      assertTrue(
        "DECIMAL64",
        bdZero.sqrt(mc).eq(BigDecimal.ZERO)
      )
    }

    locally {
      val mc = MathContext.DECIMAL128
      assertTrue(
        "DECIMAL128",
        bdZero.sqrt(mc).eq(BigDecimal.ZERO)
      )
    }

    locally {
      val mc = MathContext.UNLIMITED
      assertTrue(
        "UNLIMITED",
        bdZero.sqrt(mc).eq(BigDecimal.ZERO)
      )
    }

    locally {
      val mc = new MathContext(99, RoundingMode.UNNECESSARY)
      assertTrue(
        s"Custom mc ${mc}",
        bdZero.sqrt(mc).eq(BigDecimal.ZERO)
      )
    }
  }

  /* Alert! The road takes a sharp, unexpected turn here.
   *
   * Explicitly use both the BD 'clone membership' test '.compareTo()'
   * and the the BD content equality test ".equals()" in the sqrt_Mctx*
   * tests below.
   *
   * The ".equals()" test ensures the expected "preferred scale"
   * has been returned by Scala Native. A fine point of JVM compliance.
   *
   * If the contents and scale match, then the corresponding Strings should
   * match. The check of '.toString()' appears redundant but is reassuring.
   */

  @Test def sqrt_MctxDECIMAL32(): Unit = {
    val mc = MathContext.DECIMAL32

    for (j <- 0 until sqrtTestCases_Mctx32.length) {
      val tc = sqrtTestCases_Mctx32(j)

      val root = tc.radicand.sqrt(mc)

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"sqrt(${tc.radicand}) mc: ${mc} compareTo()",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"sqrt(${tc.radicand}) mc: ${mc} equals()",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  // @Ignore
  @Test def sqrt_MctxDECIMAL64(): Unit = {
    val mc = MathContext.DECIMAL64

    for (j <- 0 until sqrtTestCases_Mctx64.length) {
      val tc = sqrtTestCases_Mctx64(j)

      val root = tc.radicand.sqrt(mc)

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"sqrt(${tc.radicand}) mc: ${mc} compareTo()",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"sqrt(${tc.radicand}) mc: ${mc} equals()",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  // @Ignore
  @Test def sqrt_MctxDECIMAL128(): Unit = {
    val mc = MathContext.DECIMAL128

    for (j <- 0 until sqrtTestCases_Mctx128.length) {
      val tc = sqrtTestCases_Mctx128(j)

      val root = tc.radicand.sqrt(mc)

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"sqrt(${tc.radicand}) mc: ${mc} compareTo()",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"sqrt(${tc.radicand}) mc: ${mc} equals()",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  // @Ignore
  @Test def sqrt_MctxUNLIMITED(): Unit = {
    val mc = MathContext.UNLIMITED

    for (j <- 0 until sqrtTestCases_MctxUNLIMITED.length) {
      val tc = sqrtTestCases_MctxUNLIMITED(j)

      val root = tc.radicand.sqrt(mc)

      // Message is bulky to supply info needed to understand failures.
      val assertMsg_part1 = s"sqrt(${tc.radicand})"
      val assertMsg_part2 = s"\texpected: ${tc.expected}\n"
      val assertMsg_part3 = s"\troot    : ${root}\n"

      def composeAssertMsg(action: String): String =
        s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        composeAssertMsg("compareTo()"),
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(composeAssertMsg("equals()"), root.equals(tc.expected))

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }

    // Check tiny fraction is distinguished from near neighbor perfect square.
    locally {
      val bd = new BigDecimal("4.0000000000000000000000000000001")

      assertThrows(
        "tiny trailing 1",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )
    }
  }

  /* Entries are perfect squares whose principal root will fit into
   *    new MathContext(10, RoundingMode.UNNECESSARY)
   */
  val sqrtTestCases_RmUNNECESSARY = Array(
    SqrtTestCase(
      new BigDecimal("4.4944"),
      new BigDecimal("2.12")
    ),
    SqrtTestCase(
      new BigDecimal("4"),
      new BigDecimal("2") // used preferred scale
    )
  )

  // @Ignore
  @Test def sqrt_RoundingUNNECESSARY(): Unit = {
    /* This Test focuses on the features of RoundingMode.UNNECESSARY
     * and covers some edge cases.
     *
     * It is related to and may partially duplicate others, such as
     * sqrt_Exceptions and sqrt_MctxCustom, which may use
     * RoundingMode.UNNECESSARY tangentially.
     */

    // imperfect square
    locally {
      val bd = new BigDecimal("4.999")
      val mc = new MathContext(99, RoundingMode.UNNECESSARY)

      assertThrows(
        "imperfect square should throw",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )
    }

    // Perfect square; fenceposts of fit to indicated precision, no rounding.
    locally {
      val bdString = "4.4944"
      val bd = new BigDecimal(bdString)
      val expectedRoot = new BigDecimal("2.12")
      val expectedRootLength = expectedRoot.precision()

      val mcPrecisionLT = expectedRootLength - 1
      val mcLT = new MathContext(mcPrecisionLT, RoundingMode.UNNECESSARY)

      assertThrows(
        s"root of $bdString should not fit context precision: $mcPrecisionLT",
        classOf[ArithmeticException],
        bd.sqrt(mcLT)
      )
    }

    /* perfect squares, which fit indicated precision.
     * Maintainers: when changing precision, check the associated data table.
     */
    val mc = new MathContext(10, RoundingMode.UNNECESSARY)

    for (j <- 0 until sqrtTestCases_RmUNNECESSARY.length) {
      val tc = sqrtTestCases_RmUNNECESSARY(j)

      val root = tc.radicand.sqrt(mc)

      // Message is bulky to supply info needed to understand failures.
      val assertMsg_part1 = s"sqrt(${tc.radicand})"
      val assertMsg_part2 = s"\texpected: ${tc.expected}\n"
      val assertMsg_part3 = s"\troot    : ${root}\n"

      def composeAssertMsg(action: String): String =
        s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        composeAssertMsg("compareTo()"),
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(composeAssertMsg("equals()"), root.equals(tc.expected))

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  // @Ignore
  @Test def sqrt_MctxCustom(): Unit = {
    val mc = new MathContext(2, RoundingMode.UNNECESSARY)

    for (j <- 0 until sqrtTestCases_MctxCustom.length) {
      val tc = sqrtTestCases_MctxCustom(j)

      val root = tc.radicand.sqrt(mc)

      // Message is bulky to supply info needed to understand failures.
      val assertMsg_part1 = s"sqrt(${tc.radicand})"
      val assertMsg_part2 = s"\texpected: ${tc.expected}\n"
      val assertMsg_part3 = s"\troot    : ${root}\n"

      def composeAssertMsg(action: String): String =
        s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        composeAssertMsg("compareTo()"),
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(composeAssertMsg("equals()"), root.equals(tc.expected))

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }

    // Check tiny fraction is distinguished from near neighbor perfect square.
    locally {
      val bd = new BigDecimal("4.0000000000000000000000000000001")

      assertThrows(
        "tiny trailing 1",
        classOf[ArithmeticException],
        bd.sqrt(mc)
      )
    }
  }

  // @Ignore
  @Test def testCustomMC_precision_99(): Unit = {
    val testAtPrecision = 99

    val customMcRmHE = new MathContext(testAtPrecision, RoundingMode.HALF_EVEN)

    val customMcRmDOWN = new MathContext(testAtPrecision, RoundingMode.DOWN)

    val radicand = new BigDecimal("3.0")

    val rootHE = radicand.sqrt(customMcRmHE)
    val rootDown = radicand.sqrt(customMcRmDOWN)

    if (rootHE.compareTo(rootDown) == 0) {
      fail("failed - roots should not match")
    }

    assertTrue(
      "roots should not match rootHE: ${rootHE}\n rootDown: ${rootDown}",
      rootHE.compareTo(rootDown) != 0
    )

    val differenceBD = rootHE
      .subtract(rootDown)
      .scaleByPowerOfTen(rootHE.scale)

    val difference = differenceBD.unscaledValue.longValueExact()

    /* In this carefully crafted artificial but possible case,
     * rootHE should be 1 greater than rootDown in last place and only there.
     *
     * If one examines using precision 103, there is a succession of
     * place roundings which result in the off-by-one difference at
     * precision 99. This shows that different rounding modes were acutally
     * used and had a cumulative effect.
     *
     * With other radicands and combinations of RoundingModes, there might
     * be no difference or only a few leftmost places diference. The
     * JDK specification implies/states that the difference should only
     * be in the last place and be within plus or minus 1.
     *
     */
    assertEquals(
      s"unexpected difference: HE: ${rootHE}\n DOWN: ${rootDown}",
      1L,
      difference
    )
  }

  // @Ignore
  @Test def sqrt_BigDecimal_LargerThan_LongMAX_VALUE(): Unit = {
    val mcHE = new MathContext(110, RoundingMode.HALF_EVEN)

    val longMaxBD = new BigDecimal(jl.Long.MAX_VALUE)
    val longMaxPow2 = longMaxBD.pow(2)

    // verify that we are outside the range of a Java Long.
    assertThrows(
      "expected BigDecimal test point > jl.Long.MAX_VALUE",
      classOf[ArithmeticException],
      longMaxPow2.longValueExact()
    )

    val root =
      longMaxPow2.multiply(new BigDecimal(4.0)).sqrt(mcHE)

    // root still outside the range of a Java Long.
    assertThrows(
      "expected root BigDecimal > jl.Long.MAX_VALUE",
      classOf[ArithmeticException],
      root.longValueExact()
    )

    val expected = new BigDecimal("18446744073709551614")

    // Message is bulky to supply info needed to understand failures.
    val assertMsg_part1 = "usefully large Long-based BigDecimal root"
    val assertMsg_part2 = s"\texpected: ${expected}\n"
    val assertMsg_part3 = s"\troot    : ${root}\n"

    def composeAssertMsg(action: String): String =
      s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(composeAssertMsg("compareTo()"), root.compareTo(expected) == 0)

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(composeAssertMsg("equals()"), root.equals(expected))

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString(),
      root.toString
    )
  }

  // @Ignore
  @Test def sqrt_BigDecimal_LargerThan_DoubleMAX_VALUE(): Unit = {
    val mcHE = new MathContext(50, RoundingMode.HALF_EVEN)

    val doubleMaxBD = new BigDecimal(jl.Double.MAX_VALUE)
    val doubleMaxPow2 = doubleMaxBD.pow(2)

    // verify that we are outside the range of a Java Double.
    assertTrue(
      "expected BigDecimal test point > jl.Double.MAX_VALUE",
      doubleMaxPow2.doubleValue() == jl.Double.POSITIVE_INFINITY
    )

    val root =
      doubleMaxPow2.multiply(new BigDecimal(4.0)).sqrt(mcHE)

    // root still outside the range of a Java Double.
    assertTrue(
      "expected root BigDecimal > jl.Double.MAX_VALUE",
      root.doubleValue() == jl.Double.POSITIVE_INFINITY
    )

    val expected = new BigDecimal(
      "3.5953862697246314162905484746340871359614113505169E+308"
    )

    // Message is bulky to supply info needed to understand failures.
    val assertMsg_part1 = "usefully large Double-based BigDecimal root"
    val assertMsg_part2 = s"\texpected: ${expected}\n"
    val assertMsg_part3 = s"\troot    : ${root}\n"

    def composeAssertMsg(action: String): String =
      s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(composeAssertMsg("compareTo()"), root.compareTo(expected) == 0)

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(composeAssertMsg("equals()"), root.equals(expected))

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString(),
      root.toString
    )
  }

  @Test def sqrt_BigDecimal_CloserToZeroThan_DoubleMIN_NORMAL(): Unit = {
    /* Exercise an expected value in the IEEE 754 subnormal range:
     * [jl.Double.MIN_VALUE, jl.Double.MIN_NORMAL) which can not
     * be exactly represented as a scala.Double. That is, one less than
     * one full ulp from a representable subnormal.
     *
     * The radicand based on that expected value will be closer-to-zero
     * than jl.Double.MIN_VALUE.
     */

    def assertIsInSubnormalUlp(msgPrefix: String, bd: BigDecimal): Unit = {
      val ieeeValue = bd.doubleValue()

      assertTrue(
        s"${msgPrefix}: IEEE value is not finite:  ${ieeeValue}",
        jl.Double.isFinite(ieeeValue)
      )

      assertTrue(
        s"${msgPrefix}: IEEE value is a zero:  ${ieeeValue}",
        ieeeValue != 0.0 // or -0.0
      )

      assertTrue(
        s"${msgPrefix}: IEEE value is not less than Double.MIN_NORMAL:  ${ieeeValue}",
        jl.Double.compare(ieeeValue, jl.Double.MIN_NORMAL) == -1
      )

      assertTrue(
        s"${msgPrefix}: IEEE value is less than Double.MIN_VALUE:  ${ieeeValue}",
        jl.Double.compare(ieeeValue, jl.Double.MIN_VALUE) != -1
      )

      assertTrue(
        "bd is an exact IEEE subnormal",
        bd.compareTo(BigDecimal.valueOf(ieeeValue)) != 0
      )
    }

    val factor = BigDecimal
      .valueOf(4.0)
      .divide(
        BigDecimal.valueOf(3.0),
        MathContext.DECIMAL128 // arbitrary; large enough to yield gap.
      )

    val expected = BigDecimal.valueOf(jl.Double.MIN_VALUE).multiply(factor)

    assertIsInSubnormalUlp("expected", expected)

    // avoid possible power-of-two special cases in pow()
    val radicand = expected.multiply(expected)

    /* re: Magic number 256
     * 251 was determined empirically to give enough places so values match.
     * 256 is the next large power of two, and those have pizzazz, even if no
     * greater utility.
     */
    val mc = new MathContext(256, RoundingMode.HALF_EVEN)

    // radicand will be closer-to-zero than smallest representable IEEE value.
    val root = radicand.sqrt(mc)

    assertIsInSubnormalUlp("root", root)

    // Message is bulky to supply info needed to understand failures.
    val assertMsg_part1 = "unexpected sqrt(unrepresentable IEEE subnormal)"
    val assertMsg_part2 = s"\texpected: ${expected}\n)"
    val assertMsg_part3 = s"\troot    : ${root}\n)"

    def composeAssertMsg(action: String): String =
      s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(composeAssertMsg("compareTo()"), root.compareTo(expected) == 0)

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(composeAssertMsg("equals()"), root.equals(expected))

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString(),
      root.toString
    )
  }

  @Test def sqrt_BigDecimal_CloserToZeroThan_DoubleMIN_VALUE(): Unit = {
    /* Exercise an expected value less than jl.Double.MIN_NORMAL.
     * BigDecimal allows such but IEEE scala.Double can not represent them,
     * even as a subnormal.
     *
     * The radicand based on that expected value will be closer to zero
     * than jl.Double.MIN_VALUE.
     */

    def assertIsNotIEEE(msgPrefix: String, bd: BigDecimal): Unit = {
      assertTrue(
        s"${msgPrefix}: bd is <= zero:  ${bd}",
        bd.compareTo(BigDecimal.ZERO) == 1
      )

      assertTrue(
        s"${msgPrefix}: bd is >= jl.Double.MIN_VALUE:  ${bd}",
        bd.compareTo(BigDecimal.valueOf(jl.Double.MIN_VALUE)) == -1
      )

      val ieeeValue = bd.doubleValue()

      // Another way of determining if bd is too small to be an IEEE double.
      assertTrue(
        s"${msgPrefix}: IEEE value must be zero:  ${ieeeValue}",
        jl.Double.compare(ieeeValue, 0.0) == 0
      )
    }

    val factor = BigDecimal
      .valueOf(1.0)
      .divide(
        BigDecimal.valueOf(3.0),
        MathContext.DECIMAL128 // arbitrary; large enough to cause < MIN_VALUE
      )

    val expected = BigDecimal.valueOf(jl.Double.MIN_VALUE).multiply(factor)

    assertIsNotIEEE("expected", expected)

    // avoid possible power-of-two special cases in pow()
    val radicand = expected.multiply(expected)

    /* re: Magic number 64
     * 37 was determined empirically to give enough places so values match.
     * 64 is the next large power of two, and those have pizzazz, even if no
     * greater utility.
     */

    val mc = new MathContext(37, RoundingMode.HALF_EVEN)

    // radicand will be closer-to-zero than smallest representable IEEE value.
    val root = radicand.sqrt(mc)

    assertIsNotIEEE("root", root)

    // Message is bulky to supply info needed to understand failures.
    val assertMsg_part1 = "unexpected sqrt(unrepresentable IEEE)"
    val assertMsg_part2 = s"\texpected: ${expected}\n"
    val assertMsg_part3 = s"\troot    : ${root}\n"

    def composeAssertMsg(action: String): String =
      s"${assertMsg_part1} ${action} \n${assertMsg_part2}${assertMsg_part3}"

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(composeAssertMsg("compareTo()"), root.compareTo(expected) == 0)

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(composeAssertMsg("equals()"), root.equals(expected))

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString(),
      root.toString
    )
  }
}
