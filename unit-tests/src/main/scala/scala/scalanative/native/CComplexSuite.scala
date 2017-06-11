package scala.scalanative.native

import complex._
import complexOps._

import java.{lang => jl}

// Reference
// http://en.cppreference.com/w/c/numeric/complex
// Ran the complex functions in C to get the results
// in hex which are used here for the tests.
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
  // complex data for float
  val tf = stdlib
    .malloc(sizeof[CFloatComplex])
    .cast[Ptr[CFloatComplex]]
    .init(real.toFloat, imag.toFloat)
  val buff = stdlib.malloc(sizeof[CFloatComplex]).cast[Ptr[CFloatComplex]]

  test("cacosf") {
    assertEqualsComplexF(
      cacosf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f67910a), toFloat(0xbf87d7dc)))
  }
  test("casinf") {
    assertEqualsComplexF(
      casinf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f2a8eab), toFloat(0x3f87d7dc)))
  }
  test("catanf") {
    assertEqualsComplexF(
      catanf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f823454), toFloat(0x3ece0210)))
  }
  test("ccosf") {
    assertEqualsComplexF(
      ccosf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f556f55), toFloat(0xbf7d2866)))
  }
  test("csinf") {
    assertEqualsComplexF(
      csinf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3fa633dc), toFloat(0x3f228cff)))
  }
  test("ctanf") {
    assertEqualsComplexF(
      ctanf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3e8b2327), toFloat(0x3f8abe00)))

  }
  test("cacoshf") {
    assertEqualsComplexF(
      cacoshf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f87d7dc), toFloat(0x3f67910a)))
  }
  test("casinhf") {
    assertEqualsComplexF(
      casinhf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f87d7dc), toFloat(0x3f2a8eab)))
  }
  test("catanhf") {
    assertEqualsComplexF(
      catanhf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3ece0210), toFloat(0x3f823454)))
  }
  test("ccoshf") {
    assertEqualsComplexF(
      ccoshf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f556f55), toFloat(0x3f7d2866)))
  }
  test("csinhf") {
    assertEqualsComplexF(
      csinhf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f228cff), toFloat(0x3fa633dc)))
  }
  test("ctanhf") {
    assertEqualsComplexF(
      ctanhf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f8abe00), toFloat(0x3e8b2327)))
  }
  test("cexpf") {
    assertEqualsComplexF(
      cexpf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3fbbfe2a), toFloat(0x40126407)))
  }
  test("clogf") {
    assertEqualsComplexF(
      clogf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3eb17218), toFloat(0x3f490fdb)))
  }
  test("cabsf") {
    assertEquals(cabsf(tf), sqrt2.toFloat)
  }
  test("cpowf") {
    assertEqualsComplexF(
      cpowf(tf, tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3e8c441e), toFloat(0x3f156d6a)))
  }
  test("csqrtf") {
    assertEqualsComplexF(
      csqrtf(tf, buff),
      stackalloc[CFloatComplex].init(toFloat(0x3f8ca1af), toFloat(0x3ee90189)))
  }
  test("cargf") {
    assertEquals(cargf(tf), qtrPI.toFloat)
  }
  test("cimagf") {
    assertEquals(cimagf(tf), imag)
  }
  test("conjf") {
    assertEqualsComplexF(
      conjf(tf, buff),
      stackalloc[CFloatComplex].init(real.toFloat, -imag.toFloat))
  }
  test("cprojf") {
    assertEqualsComplexF(cprojf(tf, buff), tf)
  }
  test("crealf") {
    assertEquals(crealf(tf), real)
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

  // complex data for double
  val td = stdlib
    .malloc(sizeof[CDoubleComplex])
    .cast[Ptr[CDoubleComplex]]
    .init(real, imag)
  val buf = stdlib.malloc(sizeof[CDoubleComplex]).cast[Ptr[CDoubleComplex]]

  test("cacos") {
    assertEqualsComplexD(
      cacos(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fecf2214ccccd44"),
                                      toDouble("bff0fafb8f2f147f")))
  }
  test("cacos") {
    assertEqualsComplexD(
      cacos(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fecf2214ccccd44"),
                                      toDouble("bff0fafb8f2f147f")))
  }
  test("casin") {
    assertEqualsComplexD(
      casin(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fe551d55bbb8ced"),
                                      toDouble("3ff0fafb8f2f147f")))
  }
  test("ctan") {
    assertEqualsComplexD(
      ctan(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd16464f4a33f88"),
                                      toDouble("3ff157bffca4a8bc")))
  }
  test("ccos") {
    assertEqualsComplexD(
      ccos(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3feaadea96f4359b"),
                                      toDouble("bfefa50ccd2ae8f3")))
  }
  test("csin") {
    assertEqualsComplexD(
      csin(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff4c67b74f6cc4f"),
                                      toDouble("3fe4519fd8047f92")))
  }
  test("ctan") {
    assertEqualsComplexD(
      ctan(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd16464f4a33f88"),
                                      toDouble("3ff157bffca4a8bc")))
  }
  test("cacosh") {
    assertEqualsComplexD(
      cacosh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff0fafb8f2f147f"),
                                      toDouble("3fecf2214ccccd44")))
  }
  test("casinh") {
    assertEqualsComplexD(
      casinh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff0fafb8f2f147f"),
                                      toDouble("3fe551d55bbb8ced")))
  }
  test("catanh") {
    assertEqualsComplexD(
      catanh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd9c041f7ed8d33"),
                                      toDouble("3ff0468a8ace4df6")))
  }
  test("ccosh") {
    assertEqualsComplexD(
      ccosh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3feaadea96f4359b"),
                                      toDouble("3fefa50ccd2ae8f3")))
  }
  test("csinh") {
    assertEqualsComplexD(
      csinh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fe4519fd8047f92"),
                                      toDouble("3ff4c67b74f6cc4f")))
  }
  test("ctanh") {
    assertEqualsComplexD(
      ctanh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff157bffca4a8bc"),
                                      toDouble("3fd16464f4a33f88")))
  }
  test("cexp") {
    assertEqualsComplexD(
      cexp(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff77fc5377c5a96"),
                                      toDouble("40024c80edc62064")))
  }
  test("clog") {
    assertEqualsComplexD(
      clog(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd62e42fefa39ef"),
                                      toDouble("3fe921fb54442d18")))
  }
  test("cabs") {
    assertEquals(cabs(td), sqrt2)
  }
  test("cpow") {
    assertEqualsComplexD(
      cpow(td, td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd18884016cf327"),
                                      toDouble("3fe2adad36b098aa")))
  }
  test("csqrt") {
    assertEqualsComplexD(
      csqrt(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff19435caffa9f9"),
                                      toDouble("3fdd203138f6c828")))
  }
  test("carg") {
    assertEquals(carg(td), qtrPI)
  }
  test("cimag") {
    assertEquals(cimag(td), imag)
  }
  test("conj") {
    assertEqualsComplexD(conj(td, buf),
                         stackalloc[CDoubleComplex].init(real, -imag))
  }
  test("cproj") {
    assertEqualsComplexD(cproj(td, buf), td)
  }
  test("creal") {
    assertEquals(creal(td), real)
  }
  // Can't seem to free here - need after test hook
//  stdlib.free(tf.cast[Ptr[Byte]])
//  stdlib.free(buff.cast[Ptr[Byte]])
//  stdlib.free(td.cast[Ptr[Byte]])
//  stdlib.free(buf.cast[Ptr[Byte]])
}
