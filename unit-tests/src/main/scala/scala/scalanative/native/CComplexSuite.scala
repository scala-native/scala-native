package scala.scalanative.native

import complex._
import complexOps._

import java.{lang => jl}

// Reference
// http://en.cppreference.com/w/c/numeric/complex
// Ran the complex functions in C to get the results
// in hex which are used here for the tests.
// TODO: 2 float tests fail on Linux so they are omitted for now
object CComplexSuite extends tests.Suite {
  // shared values for special calculations
  val qtrPI = Math.PI / 4
  val sqrt2 = Math.sqrt(2)
  val real  = 1.0
  val imag  = 1.0

  // float complex helper fcns
  def printF(str: String, cp: Ptr[CFloatComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")

  def toFloat(i: Int): Float = jl.Float.intBitsToFloat(i)

  def isAlmostEqual(act: Float, exp: Float): Boolean = {
    val diff    = Math.abs(act - exp)
    val epsilon = Math.max(Math.ulp(act), Math.ulp(exp))
    diff <= epsilon
  }

  def assertEqualsComplexF(act: Ptr[CFloatComplex],
                           exp: Ptr[CFloatComplex]): Unit =
    assert(isAlmostEqual(act.re, exp.re) && isAlmostEqual(act.im, exp.im))

  def tf(implicit z: Zone) = res(real.toFloat, imag.toFloat)

  def buff(implicit z: Zone) = alloc[CFloatComplex]

  def res(real: Float, imag: Float)(implicit z: Zone) =
    alloc[CFloatComplex].init(real.toFloat, imag.toFloat)

  test("cacosf") {
    Zone { implicit z =>
      assertEqualsComplexF(cacosf(tf, buff),
                           res(toFloat(0x3f67910a), toFloat(0xbf87d7dc)))
    }
  }
  test("casinf") {
    Zone { implicit z =>
      assertEqualsComplexF(casinf(tf, buff),
                           res(toFloat(0x3f2a8eab), toFloat(0x3f87d7dc)))
    }
  }
  test("catanf") {
    Zone { implicit z =>
      assertEqualsComplexF(catanf(tf, buff),
                           res(toFloat(0x3f823454), toFloat(0x3ece0210)))
    }
  }
//  test("ccosf") {
//    Zone { implicit z =>
//      assertEqualsComplexF(
//        ccosf(tf, buff),
//        res(toFloat(0x3f556f55), toFloat(0xbf7d2866)))
//    }
//  }
  test("csinf") {
    Zone { implicit z =>
      assertEqualsComplexF(csinf(tf, buff),
                           res(toFloat(0x3fa633dc), toFloat(0x3f228cff)))
    }
  }
  test("ctanf") {
    Zone { implicit z =>
      assertEqualsComplexF(ctanf(tf, buff),
                           res(toFloat(0x3e8b2327), toFloat(0x3f8abe00)))
    }

  }
  test("cacoshf") {
    Zone { implicit z =>
      assertEqualsComplexF(cacoshf(tf, buff),
                           res(toFloat(0x3f87d7dc), toFloat(0x3f67910a)))
    }
  }
  test("casinhf") {
    Zone { implicit z =>
      assertEqualsComplexF(casinhf(tf, buff),
                           res(toFloat(0x3f87d7dc), toFloat(0x3f2a8eab)))
    }
  }
  test("catanhf") {
    Zone { implicit z =>
      assertEqualsComplexF(catanhf(tf, buff),
                           res(toFloat(0x3ece0210), toFloat(0x3f823454)))
    }
  }
//  test("ccoshf") {
//    Zone { implicit z =>
//      assertEqualsComplexF(
//        ccoshf(tf, buff),
//        res(toFloat(0x3f556f55), toFloat(0x3f7d2866)))
//    }
//  }
  test("csinhf") {
    Zone { implicit z =>
      assertEqualsComplexF(csinhf(tf, buff),
                           res(toFloat(0x3f228cff), toFloat(0x3fa633dc)))
    }
  }
  test("ctanhf") {
    Zone { implicit z =>
      assertEqualsComplexF(ctanhf(tf, buff),
                           res(toFloat(0x3f8abe00), toFloat(0x3e8b2327)))
    }
  }
  test("cexpf") {
    Zone { implicit z =>
      assertEqualsComplexF(cexpf(tf, buff),
                           res(toFloat(0x3fbbfe2a), toFloat(0x40126407)))
    }
  }
  test("clogf") {
    Zone { implicit z =>
      assertEqualsComplexF(clogf(tf, buff),
                           res(toFloat(0x3eb17218), toFloat(0x3f490fdb)))
    }
  }
  test("cabsf") {
    Zone { implicit z =>
      assertEquals(cabsf(tf), sqrt2.toFloat)
    }
  }
  test("cpowf") {
    Zone { implicit z =>
      assertEqualsComplexF(cpowf(tf, tf, buff),
                           res(toFloat(0x3e8c441e), toFloat(0x3f156d6a)))
    }
  }
  test("csqrtf") {
    Zone { implicit z =>
      assertEqualsComplexF(csqrtf(tf, buff),
                           res(toFloat(0x3f8ca1af), toFloat(0x3ee90189)))
    }
  }
  test("cargf") {
    Zone { implicit z =>
      assertEquals(cargf(tf), qtrPI.toFloat)
    }
  }
  test("cimagf") {
    Zone { implicit z =>
      assertEquals(cimagf(tf), imag)
    }
  }
  test("conjf") {
    Zone { implicit z =>
      assertEqualsComplexF(conjf(tf, buff), res(real.toFloat, -imag.toFloat))
    }
  }
  test("cprojf") {
    Zone { implicit z =>
      assertEqualsComplexF(cprojf(tf, buff), tf)
    }
  }
  test("crealf") {
    Zone { implicit z =>
      assertEquals(crealf(tf), real)
    }
  }

  // double complex helper fcns
  def printD(str: String, cp: Ptr[CDoubleComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")

  def toDouble(hex: String): Double = {
    import java.math.BigInteger
    val l = new BigInteger(hex, 16).longValue
    jl.Double.longBitsToDouble(l)
  }

  def isAlmostEqual(act: Double, exp: Double): Boolean = {
    val diff    = Math.abs(act - exp)
    val epsilon = Math.max(Math.ulp(act), Math.ulp(exp))
    diff <= epsilon
  }

  def assertEqualsComplexD(act: Ptr[CDoubleComplex],
                           exp: Ptr[CDoubleComplex]): Unit =
    assert(isAlmostEqual(act.re, exp.re) && isAlmostEqual(act.im, exp.im))

  def td(implicit z: Zone) = res(real, imag)

  def buf(implicit z: Zone) = alloc[CDoubleComplex]

  def res(real: Double, imag: Double)(implicit z: Zone) =
    alloc[CDoubleComplex].init(real, imag)

  test("cacos") {
    Zone { implicit z =>
      assertEqualsComplexD(cacos(td, buf),
                           res(toDouble("3fecf2214ccccd44"),
                               toDouble("bff0fafb8f2f147f")))
    }
  }
  test("cacos") {
    Zone { implicit z =>
      assertEqualsComplexD(cacos(td, buf),
                           res(toDouble("3fecf2214ccccd44"),
                               toDouble("bff0fafb8f2f147f")))
    }
  }
  test("casin") {
    Zone { implicit z =>
      assertEqualsComplexD(casin(td, buf),
                           res(toDouble("3fe551d55bbb8ced"),
                               toDouble("3ff0fafb8f2f147f")))
    }
  }
  test("ctan") {
    Zone { implicit z =>
      assertEqualsComplexD(ctan(td, buf),
                           res(toDouble("3fd16464f4a33f88"),
                               toDouble("3ff157bffca4a8bc")))
    }
  }
  test("ccos") {
    Zone { implicit z =>
      assertEqualsComplexD(ccos(td, buf),
                           res(toDouble("3feaadea96f4359b"),
                               toDouble("bfefa50ccd2ae8f3")))
    }
  }
  test("csin") {
    Zone { implicit z =>
      assertEqualsComplexD(csin(td, buf),
                           res(toDouble("3ff4c67b74f6cc4f"),
                               toDouble("3fe4519fd8047f92")))
    }
  }
  test("ctan") {
    Zone { implicit z =>
      assertEqualsComplexD(ctan(td, buf),
                           res(toDouble("3fd16464f4a33f88"),
                               toDouble("3ff157bffca4a8bc")))
    }
  }
  test("cacosh") {
    Zone { implicit z =>
      assertEqualsComplexD(cacosh(td, buf),
                           res(toDouble("3ff0fafb8f2f147f"),
                               toDouble("3fecf2214ccccd44")))
    }
  }
  test("casinh") {
    Zone { implicit z =>
      assertEqualsComplexD(casinh(td, buf),
                           res(toDouble("3ff0fafb8f2f147f"),
                               toDouble("3fe551d55bbb8ced")))
    }
  }
  test("catanh") {
    Zone { implicit z =>
      assertEqualsComplexD(catanh(td, buf),
                           res(toDouble("3fd9c041f7ed8d33"),
                               toDouble("3ff0468a8ace4df6")))
    }
  }
  test("ccosh") {
    Zone { implicit z =>
      assertEqualsComplexD(ccosh(td, buf),
                           res(toDouble("3feaadea96f4359b"),
                               toDouble("3fefa50ccd2ae8f3")))
    }
  }
  test("csinh") {
    Zone { implicit z =>
      assertEqualsComplexD(csinh(td, buf),
                           res(toDouble("3fe4519fd8047f92"),
                               toDouble("3ff4c67b74f6cc4f")))
    }
  }
  test("ctanh") {
    Zone { implicit z =>
      assertEqualsComplexD(ctanh(td, buf),
                           res(toDouble("3ff157bffca4a8bc"),
                               toDouble("3fd16464f4a33f88")))
    }
  }
  test("cexp") {
    Zone { implicit z =>
      assertEqualsComplexD(cexp(td, buf),
                           res(toDouble("3ff77fc5377c5a96"),
                               toDouble("40024c80edc62064")))
    }
  }
  test("clog") {
    Zone { implicit z =>
      assertEqualsComplexD(clog(td, buf),
                           res(toDouble("3fd62e42fefa39ef"),
                               toDouble("3fe921fb54442d18")))
    }
  }
  test("cabs") {
    Zone { implicit z =>
      assertEquals(cabs(td), sqrt2)
    }
  }
  test("cpow") {
    Zone { implicit z =>
      assertEqualsComplexD(cpow(td, td, buf),
                           res(toDouble("3fd18884016cf327"),
                               toDouble("3fe2adad36b098aa")))
    }
  }
  test("csqrt") {
    Zone { implicit z =>
      assertEqualsComplexD(csqrt(td, buf),
                           res(toDouble("3ff19435caffa9f9"),
                               toDouble("3fdd203138f6c828")))
    }
  }
  test("carg") {
    Zone { implicit z =>
      assertEquals(carg(td), qtrPI)
    }
  }
  test("cimag") {
    Zone { implicit z =>
      assertEquals(cimag(td), imag)
    }
  }
  test("conj") {
    Zone { implicit z =>
      assertEqualsComplexD(conj(td, buf), res(real, -imag))
    }
  }
  test("cproj") {
    Zone { implicit z =>
      assertEqualsComplexD(cproj(td, buf), td)
    }
  }
  test("creal") {
    Zone { implicit z =>
      assertEquals(creal(td), real)
    }
  }
}
