package org.scalanative.testsuite.javalib.math

import java.math.{BigDecimal, MathContext, RoundingMode}
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class BigDecimalTestOnJDK27 {

  private def composeAssertMsgSuffix(
      radicand: BigDecimal,
      degree: scala.Int,
      mc: MathContext,
      expected: BigDecimal,
      root: BigDecimal
  ): String = {
    val assertMsg_part1 = s"rootn(${radicand}, ${degree}, $mc)"
    val assertMsg_part2 = s"\texpected: ${expected}\n"
    val assertMsg_part3 = s"\troot    : ${root}\n"

    s"${assertMsg_part1}\n${assertMsg_part2}${assertMsg_part3}"
  }

  case class RootnTestCase(
      degree: Integer,
      radicand: BigDecimal,
      expected: BigDecimal
  )

  final val rootnTestCases_Mctx32 = Array(
    RootnTestCase(3, new BigDecimal("2.0"), new BigDecimal("1.259921")),
    RootnTestCase(3, new BigDecimal("0.04"), new BigDecimal("0.3419952")),
    RootnTestCase(3, new BigDecimal("0.05"), new BigDecimal("0.3684031")),
    RootnTestCase(3, new BigDecimal("35.937"), new BigDecimal("3.3")),
    RootnTestCase(
      3,
      new BigDecimal("1728000.00000"),
      new BigDecimal("120.00")
    ),
    RootnTestCase( // for music theorists, a useful higher order root
      12,
      new BigDecimal("2.0"),
      new BigDecimal("1.059463")
    )
  )

  final val rootnTestCases_Mctx64 = Array(
    RootnTestCase(
      3,
      new BigDecimal("2.0"),
      new BigDecimal("1.259921049894873")
    ),
    RootnTestCase(
      3,
      new BigDecimal("0.04"),
      new BigDecimal("0.3419951893353394")
    ),
    RootnTestCase(
      3,
      new BigDecimal("0.05"),
      new BigDecimal("0.3684031498640387")
    ),
    RootnTestCase(3, new BigDecimal("35.937"), new BigDecimal("3.3")),
    RootnTestCase(
      3,
      new BigDecimal("1728000.00000"),
      new BigDecimal("120.00")
    ),
    RootnTestCase( // for music theorists, a useful higher order root
      12,
      new BigDecimal("2.0"),
      new BigDecimal("1.059463094359295")
    )
  )

  final val rootnTestCases_Mctx128 = Array(
    RootnTestCase(
      3,
      new BigDecimal("2.0"),
      new BigDecimal("1.259921049894873164767210607278228")
    ),
    RootnTestCase(
      3,
      new BigDecimal("0.04"),
      new BigDecimal("0.341995189335339397870621774508772")
    ),
    RootnTestCase(
      3,
      new BigDecimal("0.05"),
      new BigDecimal("0.3684031498640386605779822833579807")
    ),
    RootnTestCase(
      3,
      new BigDecimal("8.8").pow(3), // 681.472
      new BigDecimal("8.8")
    ),
    RootnTestCase(3, new BigDecimal("35.937"), new BigDecimal("3.3")),
    RootnTestCase(
      3,
      new BigDecimal("1728000.00000"),
      new BigDecimal("120.00")
    ),
    RootnTestCase( // for music theorists, a useful higher order root
      12,
      new BigDecimal("2.0"),
      new BigDecimal("1.059463094359295264561825294946342")
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
  final val rootnTestCases_MctxUNLIMITED = Array(
    /* Note: This table is  similar to rootnTestCases_MctxCustom but one
     *       expected value differs. The 'why' of that difference is
     *       best left to JVM internal devos. Scala Native matches the JVM
     *       behavior, even if that seems strange.
     */
    RootnTestCase(
      3,
      new BigDecimal("8"),
      new BigDecimal("2") // used preferred scale
    ),
    RootnTestCase(3, new BigDecimal("8.0"), new BigDecimal("2.0")),
    RootnTestCase(
      3,
      new BigDecimal("8.00"),
      new BigDecimal("2.0") // only 1 trailing zero
    ),
    RootnTestCase(
      3,
      new BigDecimal("8.000"),
      /* Two trailing zeros, differs from mctxUNNECESSARY because
       * MathContet precisions differ.
       */
      new BigDecimal("2.0")
    ),
    RootnTestCase(3, new BigDecimal("27.0"), new BigDecimal("3.0")),
    RootnTestCase(3, new BigDecimal("35.937"), new BigDecimal("3.3")),
    RootnTestCase(3, new BigDecimal("0.000012167"), new BigDecimal("0.023")),
    RootnTestCase(
      3,
      new BigDecimal("1728000.00000"),
      new BigDecimal("120.00")
    )
  )

  /* Test cases for user-defined custom MathContext. Here
   *   val mc = new MathContext(2, RoundingMode.UNNECESSARY)
   *
   *  Using precision greater than zero and RoundingMode.UNNECESSARY,
   *  the radicands should all be perfect powers of root
   *  whose rootn of degree fits into the indicted precision.
   */
  final val rootnTestCases_MctxCustom = Array(
    RootnTestCase(
      3,
      new BigDecimal("8"),
      new BigDecimal("2") // used preferred scale
    ),
    RootnTestCase(3, new BigDecimal("8.0"), new BigDecimal("2.0")),
    RootnTestCase(
      3,
      /* Test: root exceeds mc.precision but root where all but
       * the first fractional zero, if any, have been removed does.
       */
      new BigDecimal("8.00"),
      new BigDecimal("2.0") // only 1 trailing zero, because mc precision=2
    ),
    RootnTestCase(
      3,
      // true root is 2.00 but expected is limited by mc precision=2 to 2.0
      new BigDecimal("8.000"),
      new BigDecimal("2.0")
    ),
    RootnTestCase(3, new BigDecimal("27.0"), new BigDecimal("3.0")),
    RootnTestCase(3, new BigDecimal("0.000027"), new BigDecimal("0.03")),
    RootnTestCase(3, new BigDecimal("35.937"), new BigDecimal("3.3")),
    RootnTestCase(
      3,
      new BigDecimal("1728000.00000"),
      new BigDecimal("1.2E+2") // MathContext precision 3 yields 120
    )
  )

  @Test def rootn_Exceptions(): Unit = {
    /* Exception of the given type is expected but exact message text can vary.
     * Better if they match but things change over time and people
     * sometimes use local customizations.
     */

    locally {
      val bd = new BigDecimal("-8.0")
      val mc = MathContext.DECIMAL64

      assertThrows(
        "rootn(0, mc)",
        classOf[ArithmeticException],
        bd.rootn(0, mc)
      )
    }

    locally {
      val bd = new BigDecimal("-8.0")
      val mc = MathContext.DECIMAL64

      assertThrows(
        "rootn(jl.Integer.MIN_VALUE, mc)",
        classOf[ArithmeticException],
        bd.rootn(jl.Integer.MIN_VALUE, mc)
      )
    }

    locally {
      /* Provoke JVM
       *  'ArithmeticException: Negative radicand with even root degree'
       */
      val bd = new BigDecimal("-8.0")
      val mc = MathContext.DECIMAL64

      assertEquals(
        "rootn(negative radicand, oddDegree)",
        -2.0,
        bd.rootn(3, mc).doubleValue,
        0.0
      )

      assertThrows(
        "rootn(negative radicand, evenDegree)",
        classOf[ArithmeticException],
        bd.rootn(4, mc)
      )
    }

    locally {
      /* Provoke JVM
       *  'ArithmeticException: Zero radicand with negative root degree'
       */
      val bd = BigDecimal.ZERO
      val mc = MathContext.DECIMAL64

      assertThrows(
        "rootn(zero radicand, negativeDegree)",
        classOf[ArithmeticException],
        bd.rootn(-1, mc)
      )
    }

    locally {
      val bd = new BigDecimal("2.0") // bd must be strictly positive here.
      val mc = MathContext.DECIMAL64

      assertThrows(
        "rootn(Integer.MIN_VALUE, mc)",
        // JVM 27: Overflow to represent absolute value of Integer.MIN_VALUE
        classOf[ArithmeticException],
        bd.rootn(Integer.MIN_VALUE, mc)
      )
    }

    locally {
      /* Provoke JVM
       *  'ArithmeticException: Negative radicand with even root degree'
       */
      val bd = new BigDecimal("-8.0")
      val mc = MathContext.DECIMAL64

      assertEquals(
        "rootn(negative radicand, oddDegree)",
        -2.0,
        bd.rootn(3, mc).doubleValue,
        0.0
      )

      assertThrows(
        "rootn(negative radicand, evenDegree)",
        classOf[ArithmeticException],
        bd.rootn(4, mc)
      )
    }

    assertThrows(
      "BigDecimal.TEN.rootn(20, null)",
      classOf[NullPointerException],
      BigDecimal.TEN.rootn(20, null)
    )

    // Provoke JVM 'ArithmeticException: Computed root not exact'
    locally { // mc.precision=0, radicand >= 1.0
      val bd = new BigDecimal("3.99")
      val mc = new MathContext(0, RoundingMode.FLOOR)
      assertThrows(
        "Computed root not exact, radicand: $bd mc(0, RoundingMode.FLOOR)",
        classOf[ArithmeticException],
        bd.rootn(3, mc)
      )
    }

    locally { // mc.precision=0, radicand < 1.0
      val bd = new BigDecimal("0.000012169")
      val mc = new MathContext(0, RoundingMode.FLOOR)
      assertThrows(
        "Computed root not exact, radicand: $bd mc(0, RoundingMode.FLOOR)",
        classOf[ArithmeticException],
        bd.rootn(3, mc)
      )
    }

    locally { // mc.precision=5
      val bd = new BigDecimal("3.99")
      val mc = new MathContext(5, RoundingMode.UNNECESSARY)
      assertThrows(
        "Computed root not exact, mc(5, RoundingMode.UNNECESSARY)",
        classOf[ArithmeticException],
        bd.rootn(3, mc)
      )
    }

    // Check RoundingModes with not-a-perfect root.
    locally {
      val bd = new BigDecimal("144.1")

      val mc = MathContext.UNLIMITED
      assertThrows(
        s"trailing 1 mc: ${mc}",
        classOf[ArithmeticException],
        bd.rootn(3, mc)
      )
    }
  }

  @Test def rootn_ZERO_JavaRequiredCase(): Unit = {
    // Explicitly test required 'Special case' in Java 27 documentation.

    val mathContexts = Array(
      MathContext.DECIMAL32,
      MathContext.DECIMAL64,
      MathContext.DECIMAL128,
      MathContext.UNLIMITED,
      new MathContext(99, RoundingMode.UNNECESSARY)
    )

    val degree = 3
    for (mc <- mathContexts)
      assertTrue(
        s"BigDecimal rootn($degree, $mc) does not .equals(BigDecimal.ZERO)",
        BigDecimal.ZERO.rootn(degree, mc).equals(BigDecimal.ZERO)
      )
  }

  @Test def rootn_Zero_preferred_scale(): Unit = {
    /* For radicands numerically equal, meaning "natural order" or
     * "clone equal" to zero, Java 27+ returns zero at the preferred
     * scale.
     */

    case class ZerosTestCase(
        degree: Integer,
        radicand: BigDecimal,
        expected: BigDecimal
    )

    val zerosTestCases = Array(
      ZerosTestCase(3, new BigDecimal("0"), new BigDecimal("0")),
      ZerosTestCase(3, new BigDecimal("0.0"), new BigDecimal("0.0")),
      ZerosTestCase(3, new BigDecimal("0.00"), new BigDecimal("0.0")),
      ZerosTestCase(3, new BigDecimal("0.000"), new BigDecimal("0.0")),
      ZerosTestCase(3, new BigDecimal("0.0000"), new BigDecimal("0.00")),
      ZerosTestCase(3, new BigDecimal("0.00000"), new BigDecimal("0.00")),
      ZerosTestCase(3, new BigDecimal("0.000000"), new BigDecimal("0.00")),
      ZerosTestCase(3, new BigDecimal("0.0000000"), new BigDecimal("0.000")),
      ZerosTestCase(3, new BigDecimal("-0.0"), new BigDecimal("0.0")),
      ZerosTestCase(3, new BigDecimal("-0"), new BigDecimal("0"))
    )

    for (zero <- zerosTestCases) {
      val degree = zero.degree

      // should be true with any MathContext, pick one.
      val rootn = zero.radicand.rootn(degree, MathContext.DECIMAL64)

      /* Use assertTrue() rather than assertEquals() to be belt & braces
       * __sure__ that .equals() is used for the comparison.
       */
      assertTrue(
        s"mismatch radicand: ${zero.radicand} expected: rootn: $rootn",
        rootn.equals(zero.expected)
      )
    }
  }

  /* Explicitly use both the BD 'clone membership' test '.compareTo()'
   * and the the BD content equality test ".equals()" in the rootn_Mctx*
   * tests below.
   *
   * The ".equals()" test ensures the expected "preferred scale"
   * has been returned by Scala Native. A fine point of JVM compliance.
   *
   * If the contents and scale match, then the corresponding Strings should
   * match. The check of '.toString()' appears redundant but is reassuring.
   */

  @Test def rootn_MctxDECIMAL32(): Unit = {
    val mc = MathContext.DECIMAL32

    for (j <- 0 until rootnTestCases_Mctx32.length) {
      val tc = rootnTestCases_Mctx32(j)
      val root = tc.radicand.rootn(tc.degree, mc)

      val assertMsgSuffix = composeAssertMsgSuffix(
        tc.radicand,
        tc.degree,
        mc,
        tc.expected,
        root
      )

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"compareTo: ${assertMsgSuffix}",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"equals: ${assertMsgSuffix}",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  @Test def rootn_MctxDECIMAL64(): Unit = {
    val mc = MathContext.DECIMAL64

    for (j <- 0 until rootnTestCases_Mctx64.length) {
      val tc = rootnTestCases_Mctx64(j)
      val root = tc.radicand.rootn(tc.degree, mc)

      val assertMsgSuffix = composeAssertMsgSuffix(
        tc.radicand,
        tc.degree,
        mc,
        tc.expected,
        root
      )

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"compareTo: ${assertMsgSuffix}",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"equals: ${assertMsgSuffix}",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  @Test def rootn_MctxDECIMAL128(): Unit = {
    val mc = MathContext.DECIMAL128

    for (j <- 0 until rootnTestCases_Mctx128.length) {
      val tc = rootnTestCases_Mctx128(j)
      val root = tc.radicand.rootn(tc.degree, mc)

      val assertMsgSuffix = composeAssertMsgSuffix(
        tc.radicand,
        tc.degree,
        mc,
        tc.expected,
        root
      )

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"compareTo: ${assertMsgSuffix}",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"equals: ${assertMsgSuffix}",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  @Test def rootn_MctxUNLIMITED(): Unit = {
    val mc = MathContext.UNLIMITED

    for (j <- 0 until rootnTestCases_MctxUNLIMITED.length) {
      val tc = rootnTestCases_MctxUNLIMITED(j)
      val root = tc.radicand.rootn(tc.degree, mc)

      val assertMsgSuffix = composeAssertMsgSuffix(
        tc.radicand,
        tc.degree,
        mc,
        tc.expected,
        root
      )

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"compareTo: ${assertMsgSuffix}",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"equals: ${assertMsgSuffix}",
        root.equals(tc.expected)
      )

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
        bd.rootn(3, mc)
      )
    }
  }

  /* Entries are perfect powers whose principal rootn of degree will fit into
   * MathContext(10, RoundingMode.UNNECESSARY)
   */
  val rootnTestCases_RmUNNECESSARY = Array(
    RootnTestCase(
      3,
      new BigDecimal("9.528128"),
      new BigDecimal("2.12")
    ),
    RootnTestCase(
      3,
      new BigDecimal("8"),
      new BigDecimal("2") // used preferred scale
    ),
    RootnTestCase(
      3,
      new BigDecimal("8.8").pow(3), // 681.472
      new BigDecimal("8.8")
    )
  )

  @Test def rootn_RoundingUNNECESSARY(): Unit = {
    /* This Test focuses on the features of RoundingMode.UNNECESSARY
     * and covers some edge cases.
     *
     * It is related to and may partially duplicate others, such as
     * rootn_Exceptions and rootn_MctxCustom, which may use
     * RoundingMode.UNNECESSARY tangentially.
     */

    // imperfect root
    locally {
      val bd = new BigDecimal("4.999")
      val mc = new MathContext(99, RoundingMode.UNNECESSARY)

      assertThrows(
        "imperfect root should throw",
        classOf[ArithmeticException],
        bd.rootn(3, mc)
      )
    }

    // Perfect root; fenceposts of fit to indicated precision, no rounding.
    locally {
      val expectedRoot = new BigDecimal("2.12")

      val degree = 3
      val radicand = expectedRoot.pow(degree) // 9.528128

      val expectedRootLength = expectedRoot.precision()
      val mcPrecisionLT = expectedRootLength - 1
      val mcLT = new MathContext(mcPrecisionLT, RoundingMode.UNNECESSARY)

      assertThrows(
        s"$radicand.rootn($degree) should not fit context precision: $mcPrecisionLT",
        classOf[ArithmeticException],
        radicand.rootn(degree, mcLT)
      )
    }

    /* perfect powers of root, which fit indicated precision.
     * Maintainers: when changing precision, check the associated data table.
     */
    val mc = new MathContext(10, RoundingMode.UNNECESSARY)

    val degree = 3

    for (j <- 0 until rootnTestCases_RmUNNECESSARY.length) {
      val tc = rootnTestCases_RmUNNECESSARY(j)
      val root = tc.radicand.rootn(degree, mc)

      val assertMsgSuffix = composeAssertMsgSuffix(
        tc.radicand,
        tc.degree,
        mc,
        tc.expected,
        root
      )

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"compareTo: ${assertMsgSuffix}",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"equals: ${assertMsgSuffix}",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }
  }

  @Test def rootn_MctxCustom(): Unit = {
    /* The MathContext is crafted so that radicand 1728000.00000
     * MathContext(2, RoundingMode.UNNECESSARY) will use
     * scientific notation 1.2E+2 on both JVM & SN.
     *
     * This exercises the logic where a root apparently taking more
     * places than the mc precision actually fits by manipulating its scale.
     *
     * MathContext(3, RoundingMode.UNNECESSARY) will yield 120.
     */

    val mc = new MathContext(2, RoundingMode.UNNECESSARY)

    for (j <- 0 until rootnTestCases_MctxCustom.length) {
      val tc = rootnTestCases_MctxCustom(j)
      val root = tc.radicand.rootn(tc.degree, mc)

      val assertMsgSuffix = composeAssertMsgSuffix(
        tc.radicand,
        tc.degree,
        mc,
        tc.expected,
        root
      )

      // Contents match, BigDecimal cohort equality idiom
      assertTrue(
        s"compareTo: ${assertMsgSuffix}",
        root.compareTo(tc.expected) == 0
      )

      // precision and scale match. LHS & RHS equals() but probably not eq().
      assertTrue(
        s"equals: ${assertMsgSuffix}",
        root.equals(tc.expected)
      )

      assertEquals(
        "SN String presentation does not match JVM reference",
        tc.expected.toString(),
        root.toString
      )
    }

    // Check tiny fraction is distinguished from near neighbor perfect power.
    locally {
      val bd = new BigDecimal("8.0000000000000000000000000000001")

      assertThrows(
        "tiny trailing 1",
        classOf[ArithmeticException],
        bd.rootn(3, mc)
      )
    }
  }

  @Test def testCustomMC_precision_99(): Unit = {
    val testAtPrecision = 99

    val customMcRmHE = new MathContext(testAtPrecision, RoundingMode.HALF_EVEN)

    val customMcRmDOWN = new MathContext(testAtPrecision, RoundingMode.DOWN)

    val radicand = new BigDecimal("3.0")

    val rootHE = radicand.rootn(3, customMcRmHE)
    val rootDown = radicand.rootn(3, customMcRmDOWN)

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

  @Test def rootn_BigDecimal_RadicandLargerThan_LongMAX_VALUE(): Unit = {
    val mcHE = new MathContext(110, RoundingMode.HALF_EVEN)

    val longMaxBD = new BigDecimal(jl.Long.MAX_VALUE)
    val longMaxPow3 = longMaxBD.pow(3)

    // verify that we are outside the range of a Java Long.
    assertThrows(
      "expected BigDecimal test point > jl.Long.MAX_VALUE",
      classOf[ArithmeticException],
      longMaxPow3.longValueExact()
    )

    val radicand = longMaxPow3.multiply(new BigDecimal(4.0))
    val degree = 3
    val root = radicand.rootn(degree, mcHE)

    // root still outside the range of a Java Long.
    assertThrows(
      "expected root BigDecimal > jl.Long.MAX_VALUE",
      classOf[ArithmeticException],
      root.longValueExact()
    )

    val expected_1 = "14641190473997345811.92353648093557671050214415886098770"
    val expected_2 = "4466889088355914614970819078650908257792155696899889313"
    val expected = new BigDecimal(s"${expected_1}${expected_2}")

    val assertMsgSuffix = composeAssertMsgSuffix(
      radicand,
      degree,
      mcHE,
      expected,
      root
    )

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(s"compareTo: ${assertMsgSuffix}", root.compareTo(expected) == 0)

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(s"equals: ${assertMsgSuffix}", root.equals(expected))

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString(),
      root.toString
    )
  }

  @Test def rootn_BigDecimal_RadicandLargerThan_DoubleMAX_VALUE(): Unit = {
    val mcHE = new MathContext(50, RoundingMode.HALF_EVEN)

    val doubleMaxBD = new BigDecimal(jl.Double.MAX_VALUE)
    val doubleMaxPow3 = doubleMaxBD.pow(3)

    // verify that we are outside the range of a Java Double.
    assertTrue(
      "expected BigDecimal test point > jl.Double.MAX_VALUE",
      doubleMaxPow3.doubleValue() == jl.Double.POSITIVE_INFINITY
    )

    val radicand = doubleMaxPow3.multiply(new BigDecimal(4.0))
    val degree = 3
    val root = radicand.rootn(degree, mcHE)

    // root still outside the range of a Java Double.
    assertTrue(
      "expected root BigDecimal > jl.Double.MAX_VALUE",
      root.doubleValue() == jl.Double.POSITIVE_INFINITY
    )

    val expected = new BigDecimal(
      "2.8536599733964502443420587832056073088646431637393E+308"
    )

    val assertMsgSuffix = composeAssertMsgSuffix(
      radicand,
      degree,
      mcHE,
      expected,
      root
    )

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(s"compareTo: ${assertMsgSuffix}", root.compareTo(expected) == 0)

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(s"equals: ${assertMsgSuffix}", root.equals(expected))

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString(),
      root.toString
    )
  }

  private def assertInIeeeSubnormalGap(
      msgPrefix: String,
      bd: BigDecimal
  ): Unit = {

    /* Validate that bd is in the IEEE subnormal range
     *  [jl.Double.MIN_NORMAL, jl.Double.MIN_VALUE)
     * but not exactly representable as an IEEE Double.
     */

    val predicate =
      (bd.compareTo(BigDecimal.valueOf(jl.Double.MIN_VALUE)) >= 0) &&
        (bd.compareTo(BigDecimal.valueOf(jl.Double.MIN_NORMAL)) < 0)

    assertTrue(
      s"${msgPrefix}: not a IEEE subnormal bd: ${bd}" +
        s" range: [${jl.Double.MIN_VALUE}, ${jl.Double.MIN_NORMAL}) ",
      predicate
    )

    val bdIeeeValue = new BigDecimal(bd.doubleValue())

    assertTrue(
      s"${msgPrefix}: Unexpected exact IEEE subnormal bd: $bd",
      bd.compareTo(bdIeeeValue) != 0
    )
  }

  @Test def rootn_BigDecimal_SubnormalRadicandNotIeeeRepresentable(): Unit = {
    /* Exercise a radicand in the IEEE 754 subnormal range:
     * [jl.Double.MIN_VALUE, jl.Double.MIN_NORMAL) which can not
     * be exactly represented as a scala.Double. That is, one less than
     * one full ulp from a representable subnormal.
     *
     * The root of given degree will be an IEEE 'normal'.
     */

    def genRadicand(degree: Int, mc: MathContext): BigDecimal = {
      /* The hard-coded radicand was determined experimentally to be
       * both not representable as an IEEE subnormal and a perfect
       * power of given degree.
       *
       * Changing to a different combination of degree and MathContext,
       * will require determining a new radicand.
       */

      assertEquals("genRadicand: unexpected degree", 3, degree)

      assertEquals(
        "genRadicand: unexpected MathContext",
        MathContext.UNLIMITED,
        mc
      )

      val radicand =
        new BigDecimal("5.400000000000003330619345256787158894083648E-324")

      assertInIeeeSubnormalGap("genRadicand radicand", radicand)

      radicand
    }

    val degree = 3

    val mc = MathContext.UNLIMITED

    val radicand = genRadicand(degree, mc)
    val root = radicand.rootn(degree, mc)

    // expected precision: 34 scale: 141
    assertTrue(
      s"unexpected root: ${root}" +
        s" root.precision: ${root.precision}" +
        s" root.scale: ${root.scale()}",
      // Match JVM value, precision, & scale exactly.
      root.equals(new BigDecimal("1.75441064292772E-108"))
    )

    val computedRadicand = root.pow(degree, mc)

    assertTrue(
      s"Incorrect radicand computed from rootn($degree).pow($degree)" +
        s"\n\texpected ${radicand}\n\tcomputed: $computedRadicand",
      // Match JVM value, precision, & scale exactly.
      computedRadicand.equals(radicand)
    )
  }

  @Test def rootn_BigDecimal_SubnormalRootNotIeeeRepresentable(): Unit = {
    /* Exercise an expected root value in the IEEE 754 subnormal range:
     * [jl.Double.MIN_VALUE, jl.Double.MIN_NORMAL) which can not
     * be exactly represented as a scala.Double. That is, one less than
     * one full ulp from a representable subnormal.
     *
     * The radicand based on that expected value will be closer-to-zero
     * than jl.Double.MIN_VALUE.
     */

    def genRadicand(degree: Int, mc: MathContext): BigDecimal = {
      /* Return a value value in the IEEE 754 non-representable range:
       * (0, jl.Double.MIN_VALUE)
       */

      /* 5E-325 is an arbitrary value chosen so that the root is not
       * representable as an IEEE subnormal.
       *
       * Could just return BigDecimal("5.4-324") but show & validate the math.
       */
      val root = new BigDecimal(jl.Double.MIN_VALUE.toString)
        .add(new BigDecimal("5E-325"))

      assertInIeeeSubnormalGap("genRadicand root", root)

      root.pow(degree) // radicand will be less than jl.Double.MIN_VALUE
    }

    val degree = 3

    /* MathContext.UNLIMITED and MathContext(300, RoundingMode.UNNECESSARY)
     * both work but exercises different paths.
     * To use those, one may need to tweak expected precision & scale in
     * radicand and root .equals() assertions below.
     */

    // radicand: 1.57464E-970 radicand.precision: 6 radicand.scale: 975
    val mc = MathContext.DECIMAL128

    val radicand = genRadicand(degree, mc)
    val root = radicand.rootn(degree, mc)

    // expected radicand 1.57464E-970 precision: 6 scale: 975
    assertTrue(
      s"unexpected radicand: ${radicand}" +
        s" radicand.precision: ${radicand.precision}" +
        s" radicand.scale: ${radicand.scale()}",
      // Match JVM value, precision, & scale exactly.
      radicand.equals(new BigDecimal("1.57464E-970"))
    )

    assertTrue(
      s"unexpected root: ${root}" +
        s" root.precision: ${root.precision} root.scale: ${root.scale()}",
      // Match JVM value, precision, & scale exactly.
      root.equals(new BigDecimal("5.4E-324"))
    )

    assertInIeeeSubnormalGap("root", root)

    val computedRadicand = root.pow(degree)

    assertTrue(
      s"Incorrect radicand computed from rootn($degree).pow($degree)" +
        s"\n\texpected ${radicand}\n\tcomputed: $computedRadicand",
      // Match JVM value, precision, & scale exactly.
      computedRadicand.equals(radicand)
    )
  }

  @Test def rootn_BigDecimal_RadicandCloserToZeroThan_DoubleMIN_VALUE()
      : Unit = {
    /* Exercise a radicand closer to zero than jl.Double.MIN_NORMAL,
     * chosen so that its expected rootn at given degree is also
     * closer to zero than MIN_NORMAL.
     *
     * BigDecimal allows such but IEEE scala.Double can not represent them,
     * even as a subnormal.
     */

    def assertPositiveLessThanMIN_VALUE(
        msgPrefix: String,
        bd: BigDecimal
    ): Unit = {
      /* Validate that bd is strictly positive and less than
       * jl.Double.MIN_VALUE. That is, bd is not representable as an IEEE
       * value.
       */

      assertTrue(
        s"${msgPrefix}: bd is <= zero:  ${bd}",
        bd.compareTo(BigDecimal.ZERO) == 1
      )

      assertTrue(
        s"${msgPrefix}: bd is <= jl.Double.MIN_VALUE  " +
          s"bd: ${bd} mv: ${jl.Double.MIN_VALUE}",
        bd.compareTo(BigDecimal.valueOf(jl.Double.MIN_VALUE)) == -1
      )

      val ieeeValue = bd.doubleValue()

      // Another way of determining if bd is too small to be an IEEE double.
      assertTrue(
        s"${msgPrefix}: IEEE value must be zero:  ${ieeeValue}",
        jl.Double.compare(ieeeValue, 0.0) == 0
      )
    }

    def genRadicand(degree: Int, mc: MathContext): BigDecimal = {
      /* Return a value which is strictly positive and less than
       * jl.Double.MIN_VALUE and whose root(degree) meets same conditions.
       */

      val r = (new BigDecimal(jl.Double.MIN_VALUE)).pow(degree + 1)
      assertPositiveLessThanMIN_VALUE("genRadicand", r)
      r
    }

    val degree = 3

    /* MathContext.UNLIMITED also works when radicand is a perfect power,
     * as it is here, but exercises a different path.
     */

    // 1001 is precision returned by JVM using RoundingMode.UNLIMITED
    val mc = new MathContext(1001, RoundingMode.HALF_EVEN)

    /* Both radicand and its roots will be closer-to-zero than smallest
     * representable IEEE value.
     */
    val radicand = genRadicand(degree, mc)
    val root = radicand.rootn(degree, mc)
    assertPositiveLessThanMIN_VALUE("root", root)

    val expected = new BigDecimal( // JVM 27
      "8.414846713278871165356190341157689935224811095077" +
        "04201837639213394755735930044787264399314705183943" +
        "45312071503298848061665302711549683639857784492308" +
        "01360646189420618471507939827511391497327199499505" +
        "79622535165613037678024465846154128162358471464255" +
        "08764023766429047443063895139532441388978245681847" +
        "00917257178325718786456353171785073807159776377527" +
        "13401964031131812782046158153914472848688751191703" +
        "16915957584399296634557593126676532268472900236510" +
        "78252327796917742615941949127452461064606406367315" +
        "65319550827087780092038171006034176235799045918867" +
        "68058959208365496020798685177383650905521438265474" +
        "28916221698559246568662019074304067875948644727891" +
        "28954876923701732285779662122131102442326492172403" +
        "23577520756248340498554687214848191086229916710890" +
        "73911182615550496948389252610973479372265324668424" +
        "83731314641359114862179181681445940520959372871627" +
        "00198421377013957123923421923731314611615374827099" +
        "22947295496018524495277446529967604706673656575807" +
        "0346497513904149290198120070272125303745269775390625E-432"
    )

    val assertMsgSuffix = composeAssertMsgSuffix(
      radicand,
      degree,
      mc,
      expected,
      root
    )

    // Contents match, BigDecimal cohort equality idiom
    assertTrue(
      s"compareTo: ${assertMsgSuffix}",
      root.compareTo(expected) == 0
    )

    // precision and scale match. LHS & RHS equals() but probably not eq().
    assertTrue(
      s"equals: ${assertMsgSuffix}",
      root.equals(expected)
    )

    assertEquals(
      "SN String presentation does not match JVM reference",
      expected.toString,
      root.toString
    )
  }
}
