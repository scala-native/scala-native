package scala.scalanative
package libc

import org.junit.Assert._
import org.junit.Test

import scalanative.libc._
import scalanative.runtime.{Platform, PlatformExt}
import scalanative.unsafe._

class TGMathTest {

  // basic math stuffs

  @Test def multiplyAndAdd(): Unit = {
    val ns = List.fill(50)(scala.util.Random.nextDouble() * 100)
    val ms = List.fill(50)(scala.util.Random.nextDouble() * 100)
    val os = List.fill(50)(scala.util.Random.nextDouble() * 100)
    for {
      n <- ns
      m <- ms
      o <- os
    } yield {
      val fma = tgmath.fma(n, m, o)
      val fmaf = tgmath.fma(n.toFloat, m.toFloat, o.toFloat)
      val fmaNaive = n * m + o
      val fmaNaiveF = n.toFloat * m.toFloat + o.toFloat
      assertEquals(
        fma,
        fmaNaive,
        TestUtils.eps(fma, fmaNaive)
      )
      assertEquals(
        fmaf,
        fmaNaiveF,
        TestUtils.eps(fmaf, fmaNaiveF)
      )
    }
  }
  // |n| >= 0, |n| = |-n|
  @Test def fabs(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextDouble() * 100)
    ns.foreach { n =>
      assertEquals(
        tgmath.fabs(-n),
        tgmath.fabs(n),
        0
      )
      assertEquals(
        tgmath.fabs(-n.toFloat),
        tgmath.fabs(n.toFloat),
        0
      )
      assertTrue(tgmath.fabs(n) >= 0)
      assertTrue(tgmath.fabs(n.toFloat) >= 0)
      assertTrue(tgmath.fabs(n) >= n)
      assertTrue(tgmath.fabs(n.toFloat) >= n.toFloat)
      assertTrue(tgmath.fabs(n) == n || tgmath.fabs(n) == -n)
      assertTrue(
        tgmath.fabs(n.toFloat) == n.toFloat || tgmath.fabs(
          n.toFloat
        ) == -n.toFloat
      )
    }
  }
  // max(a,b) == max(b,a), max(a,b) >= a && max(a,b) >= b
  // min(a,b) == min(b,a), min(a,b) <= a && min(a,b) <= b
  @Test def maxAndMin(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextDouble() * 100)
    val ms = List.fill(100)(scala.util.Random.nextDouble() * 100)
    for {
      n <- ns
      m <- ms
    } yield {
      val maxNM = tgmath.fmax(n, m)
      val maxMN = tgmath.fmax(m, n)
      assertEquals(
        maxNM,
        maxMN,
        TestUtils.eps(maxNM, maxMN)
      )
      val minNM = tgmath.fmin(n, m)
      val minMN = tgmath.fmin(m, n)
      assertEquals(
        minNM,
        minMN,
        TestUtils.eps(minNM, minMN)
      )
      val maxNMf = tgmath.fmax(n.toFloat, m.toFloat)
      val maxMNf = tgmath.fmax(m.toFloat, n.toFloat)
      assertEquals(
        maxNMf,
        maxMNf,
        TestUtils.eps(maxNMf, maxMNf)
      )
      val minNMf = tgmath.fmin(n.toFloat, m.toFloat)
      val minMNf = tgmath.fmin(m.toFloat, n.toFloat)
      assertEquals(
        minNMf,
        minMNf,
        TestUtils.eps(minNMf, minMNf)
      )

      assertTrue(
        tgmath.fmax(n, m) == n || tgmath.fmax(n, m) == m
      )
      assertTrue(
        tgmath.fmax(n, m) >= n && tgmath.fmax(n, m) >= m
      )
      assertTrue(
        tgmath.fmax(n.toFloat, m.toFloat) == n.toFloat || tgmath.fmax(
          n.toFloat,
          m.toFloat
        ) == m.toFloat
      )
      assertTrue(
        tgmath.fmax(n.toFloat, m.toFloat) >= n.toFloat && tgmath.fmax(
          n.toFloat,
          m.toFloat
        ) >= m.toFloat
      )
      assertTrue(
        tgmath.fmin(n, m) == n || tgmath.fmin(n, m) == m
      )
      assertTrue(
        tgmath.fmin(n, m) <= n && tgmath.fmin(n, m) <= m
      )
      assertTrue(
        tgmath.fmin(n.toFloat, m.toFloat) == n.toFloat || tgmath.fmin(
          n.toFloat,
          m.toFloat
        ) == m.toFloat
      )
      assertTrue(
        tgmath.fmin(n.toFloat, m.toFloat) <= n.toFloat && tgmath.fmin(
          n.toFloat,
          m.toFloat
        ) <= m.toFloat
      )
    }
  }

  @Test def fdim(): Unit = {
    val ns = List.fill(50)(scala.util.Random.nextDouble() * 100)
    val ms = List.fill(50)(scala.util.Random.nextDouble() * 100)
    for {
      n <- ns
      m <- ms
    } yield {
      assertEquals(
        tgmath.fdim(n, m),
        tgmath.fmax(n - m, 0d),
        0
      )
      assertEquals(
        tgmath.fdim(n.toFloat, m.toFloat),
        tgmath.fmax(n.toFloat - m.toFloat, 0f),
        0
      )

    }
  }

  @Test def copysign(): Unit = {
    val ns =
      List.fill(100)(scala.util.Random.nextDouble() * 100)
    ns.foreach { n =>
      assertEquals(
        tgmath.copysign(n, -(scala.util.Random.nextInt(100) + 1)),
        -tgmath.fabs(n),
        0
      )
      assertEquals(
        tgmath.copysign(n, scala.util.Random.nextInt(100) + 1),
        tgmath.fabs(n),
        0
      )
      assertEquals(
        tgmath.copysign(n, 0),
        tgmath.fabs(n),
        0
      )
    }
  }

  // n = √n^2
  @Test def sqrtOfNSquaredIsEqualsToN(): Unit = {
    val ns =
      List.fill(100)(scala.math.abs(scala.util.Random.nextDouble() * 100))
    ns.foreach { n =>
      assertTrue(
        TestUtils.isAlmostEqual(
          tgmath.sqrt(tgmath.pow(n, 2)).toDouble,
          n
        )
      )
      assertTrue(
        TestUtils.isAlmostEqual(
          tgmath.sqrt(tgmath.pow(n.toFloat, 2)),
          n.toFloat
        )
      )
    }
  }
  // n = ∛n^3
  @Test def cbrtOfNCubedIsEqualsToN(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextDouble() * 100)
    ns.foreach { n =>
      val nd = tgmath.cbrt(tgmath.pow(n, 3)).toDouble
      assertEquals(
        n,
        nd,
        1000 * TestUtils.eps(n, nd)
      )
      val nf = tgmath.cbrt(tgmath.pow(n, 3).toFloat).toFloat
      assertEquals(
        n.toFloat,
        nf,
        1000 * TestUtils.eps(n.toFloat, nf)
      )
    }
  }

  @Test def gamma(): Unit = {
    val ns = List.fill(50)(scala.math.abs(scala.util.Random.nextDouble() * 100))
    ns.foreach { n =>
      val lgamma = tgmath.lgamma(n)
      val lgammaFromTgamma = tgmath.log(tgmath.fabs(tgmath.tgamma(n)))
      assertEquals(
        lgamma,
        lgammaFromTgamma,
        0.00001
      )
    }
  }

  // trigonometric stuffs
  val half_Pi = scala.math.Pi / 2.0
  val half_Pif = scala.math.Pi / 2.0f
  val PI = scala.math.Pi

  @Test def wellKnownTrigonometricsValues(): Unit = {
    assertEquals(
      0,
      tgmath.sin(0),
      0.00001
    )
    assertEquals(
      0,
      tgmath.sin(0f),
      0.00001
    )
    assertEquals(
      1.0,
      tgmath.sin(half_Pi),
      TestUtils.eps(1, tgmath.sin(half_Pi))
    )
    assertEquals(
      1.0f,
      tgmath.sin(half_Pif),
      TestUtils.eps(1, tgmath.sin(half_Pif))
    )
    assertEquals(
      0,
      tgmath.sin(PI),
      0.00001
    )
    assertEquals(
      1,
      tgmath.cos(0),
      TestUtils.eps(tgmath.cos(0), 1.0)
    )
    assertEquals(
      1,
      tgmath.cos(0f),
      TestUtils.eps(tgmath.cos(0f), 1.0)
    )
    assertEquals(
      0,
      tgmath.cos(half_Pi),
      TestUtils.eps(tgmath.cos(half_Pi), 1.0)
    )
    assertEquals(
      -1,
      tgmath.cos(PI),
      TestUtils.eps(tgmath.cos(PI), -1)
    )
    val pi = tgmath.ldexp(tgmath.acos(0.0), 1)
    assertEquals(
      pi,
      PI,
      TestUtils.eps(pi, PI)
    )
  }

  // -1 <= sin theta <=1, -1 <= cos theta <= 1, sin theta^2 + cos theta^2 = 1
  @Test def trigonometrics(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextDouble() * 100)
    ns.foreach { n =>
      assertTrue(tgmath.sin(n) >= -1.0)
      assertTrue(tgmath.sin(n.toFloat) >= -1.0)
      assertTrue(tgmath.sin(n) <= 1.0)
      assertTrue(tgmath.sin(n.toFloat) <= 1.0)

      val sin2_cos2 =
        tgmath.pow(tgmath.sin(n), 2.0) + tgmath.pow(tgmath.cos(n), 2.0)
      assertEquals(
        1.0,
        sin2_cos2,
        TestUtils.eps(1.0, sin2_cos2)
      )

      val tanN0 = tgmath.sin(n) / tgmath.cos(n)
      val tanN1 = tgmath.tan(n)

      val tanNf0 = tgmath.sin(n.toFloat) / tgmath.cos(n.toFloat)
      val tanNf1 = tgmath.tan(n.toFloat)

      /* Scala Native Issue #2641
       *
       * The tan(double) function on Apple M1 is known, to some and
       * circa June 2022, as having poor numerical accuracy (> 3 eps).
       * tan(float) is accurate. See:
       *   https://members.loria.fr/PZimmermann/papers/accuracy.pdf
       *
       * Use wider acceptable bounds for Apple M1 tan(double).
       */
      // PlatformExt.isArm64 is currently broken/unreliable.
      // isMac() test here weakens the bounds on both Intel and Arm64
      // (i.e. M1). macOS on Intel hardware is known to work within 2 eps.
      //
      // The condition when a  truthful isArm64 is available should be:
      // (Platform.isMac() && PlatformExt.isArm64)
      val nEps = if (Platform.isMac()) 4 else 2

      assertEquals(
        tanN0,
        tanN1,
        TestUtils.eps(tanN0, tanN1, nEps)
      )

      assertEquals(
        tanNf0,
        tanNf1,
        TestUtils.eps(tanNf0, tanNf1)
      )
    }
  }

  @Test def hyperbolics(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextDouble() * 100)
    ns.foreach { n =>
      assertEquals(
        tgmath.cosh(n),
        tgmath.cosh(-n),
        0
      )
      assertEquals(
        tgmath.sinh(n),
        -tgmath.sinh(-n),
        0
      )
      val tnh0 = tgmath.sinh(n) / tgmath.cosh(n)
      val tnh1 = tgmath.tanh(n)
      assertEquals(
        tnh0,
        tnh1,
        0.00001
      )
    }

  }
  @Test def inversedTrigonometrics(): Unit = {
    val ns = List.fill(100)(scala.util.Random.nextDouble() * 100)
    ns.foreach { n =>
      val asn = tgmath.sin(tgmath.asin(n))
      val acs = tgmath.cos(tgmath.acos(n))
      val atn = tgmath.tan(tgmath.atan(n))
      assertEquals(
        n,
        asn,
        0.001
      )
      assertEquals(
        n,
        acs,
        0.001
      )
      assertEquals(
        n,
        atn,
        0.001
      )
      assertEquals(
        tgmath.atan(n),
        -tgmath.atan(-n),
        0
      )
    }
  }

  @Test def hypot(): Unit = {
    val ns = List.fill(50)(scala.math.abs(scala.util.Random.nextDouble() * 100))
    val ms = List.fill(50)(scala.math.abs(scala.util.Random.nextDouble() * 100))
    for {
      n <- ns
      m <- ms
    } yield {
      val n0 = tgmath.hypot(n, 0)
      val n0f = tgmath.hypot(n.toFloat, 0f)
      val zn = tgmath.hypot(0, n)
      val znf = tgmath.hypot(0f, n.toFloat)
      val nm = tgmath.hypot(n, m)
      val nmf = tgmath.hypot(n.toFloat, m.toFloat)
      val nMinusM = tgmath.hypot(n, -m)
      val nMinusMf = tgmath.hypot(n.toFloat, -m.toFloat)
      val mn = tgmath.hypot(m, n)
      val mnf = tgmath.hypot(m.toFloat, n.toFloat)

      assertEquals(
        nm,
        mn,
        TestUtils.eps(nm, mn)
      )
      assertEquals(
        nmf,
        mnf,
        TestUtils.eps(nmf, mnf)
      )

      assertEquals(
        nMinusM,
        nm,
        TestUtils.eps(nm, mn)
      )
      assertEquals(
        nMinusMf,
        nmf,
        TestUtils.eps(nMinusMf, mnf)
      )
      assertEquals(
        n0,
        tgmath.fabs(n),
        TestUtils.eps(n0, tgmath.fabs(n))
      )
      assertEquals(
        zn,
        tgmath.fabs(n),
        TestUtils.eps(zn, tgmath.fabs(n))
      )
    }
  }

  // log, exp stuffs

  // log 1 = 0, log e = 1
  @Test def wellKnownLogValues(): Unit = {
    // 0 = logk1
    assertEquals(
      tgmath.log10(1.0f),
      0f,
      0
    )
    assertEquals(
      tgmath.log10(1.0d),
      0d,
      0
    )
    assertEquals(
      tgmath.log10(1.0d),
      0d,
      0
    )
    assertEquals(
      tgmath.log2(1.0f),
      0f,
      0
    )
    assertEquals(
      tgmath.log2(1.0d),
      0d,
      0
    )
    assertEquals(
      tgmath.log(1d),
      0d,
      0
    )
    assertEquals(
      tgmath.log(1f),
      0f,
      0
    )
    assertEquals(
      tgmath.log(scala.math.E),
      1f,
      0
    )
    assertEquals(
      tgmath.log10(10),
      1d,
      0
    )
    assertEquals(
      tgmath.log2(2),
      1d,
      0
    )
    assertEquals(
      tgmath.log1p(scala.math.E - 1),
      1d,
      0
    )
  }
  // log_a N = log_b N/log_b a
  @Test def changeLogBaseFormula(): Unit = {
    val ns =
      List.fill(100)(scala.math.abs(scala.util.Random.nextDouble() * 100))
    ns.foreach { ns =>
      val log10d = tgmath.log10(ns)
      val log2d = tgmath.log2(ns) / tgmath.log2(10.0)
      assertEquals(
        log10d,
        log2d,
        1000 * TestUtils.eps(log10d, log2d)
      )
      val log10f = tgmath.log10(ns.toFloat)
      val log2f = tgmath.log2(ns.toFloat) / tgmath.log2(10.0f)
      assertEquals(
        log10f,
        log2f,
        1000 * TestUtils.eps(log10f, log2f)
      )
    }
  }

  @Test def scalbn(): Unit = {
    val ns = List.fill(50)(scala.util.Random.nextDouble() * 100)
    val ms = List.fill(50)(scala.util.Random.nextInt(100))
    for {
      n <- ns
      m <- ms
    } yield {
      val scalbnnmf = tgmath.scalbn(n, m).toFloat
      val scalbnnfm = tgmath.scalbn(n.toFloat, m)
      assertEquals(
        scalbnnmf,
        scalbnnfm,
        TestUtils.eps(scalbnnfm, scalbnnmf)
      )
      val scalblnnmf = tgmath.scalbln(n, m).toFloat
      val scalblnnfm = tgmath.scalbln(n.toFloat, m)
      assertEquals(
        scalblnnfm,
        scalblnnmf,
        TestUtils.eps(scalblnnfm, scalblnnmf)
      )
    }
  }
}
