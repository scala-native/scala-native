package scala.scalanative.native

import complex._
import complexOps._

import java.{lang => jl}

// Reference
// http://en.cppreference.com/w/c/numeric/complex
// Ran the complex functions in C to get the results
// in hex which are used here for the tests.
object CComplexSuite extends tests.Suite {
  // helpers to see results
  def printD(str: String, cp: Ptr[CDoubleComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")
  def printF(str: String, cp: Ptr[CFloatComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")

  def toFloat(i: Int): Float = jl.Float.intBitsToFloat(i)
  def toDouble(hex: String): Double = {
    import java.math.BigInteger
    val l = new BigInteger(hex, 16).longValue
    jl.Double.longBitsToDouble(l)
  }

  def assertEqualsComplexF(left: Ptr[CFloatComplex],
                           right: Ptr[CFloatComplex]): Unit =
    assert(left.re == right.re && left.im == right.im)
  def assertEqualsComplexD(left: Ptr[CDoubleComplex],
                           right: Ptr[CDoubleComplex]): Unit =
    assert(left.re == right.re && left.im == right.im)

  val qtrPI = Math.PI / 4
  val sqrt2 = Math.sqrt(2)

  test("float complex") {
    val real = 1.0f
    val imag = 1.0f

    val tf  = stackalloc[CFloatComplex].init(real, imag)
    val buf = stackalloc[CFloatComplex]

    assertEqualsComplexF(
      cacosf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f67910a), toFloat(0xbf87d7dc)))
    assertEqualsComplexF(
      casinf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f2a8eab), toFloat(0x3f87d7dc)))
    assertEqualsComplexF(
      catanf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f823454), toFloat(0x3ece0210)))
    assertEqualsComplexF(
      ccosf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f556f55), toFloat(0xbf7d2866)))
    assertEqualsComplexF(
      csinf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3fa633dc), toFloat(0x3f228cff)))
    assertEqualsComplexF(
      ctanf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3e8b2327), toFloat(0x3f8abe00)))

    assertEqualsComplexF(
      cacoshf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f87d7dc), toFloat(0x3f67910a)))
    assertEqualsComplexF(
      casinhf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f87d7dc), toFloat(0x3f2a8eab)))
    assertEqualsComplexF(
      catanhf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3ece0210), toFloat(0x3f823454)))
    assertEqualsComplexF(
      ccoshf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f556f55), toFloat(0x3f7d2866)))
    assertEqualsComplexF(
      csinhf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f228cff), toFloat(0x3fa633dc)))
    assertEqualsComplexF(
      ctanhf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f8abe00), toFloat(0x3e8b2327)))
    assertEqualsComplexF(
      cexpf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3fbbfe2a), toFloat(0x40126407)))
    assertEqualsComplexF(
      clogf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3eb17218), toFloat(0x3f490fdb)))
    assertEquals(cabsf(tf), sqrt2.toFloat)
    assertEqualsComplexF(
      cpowf(tf, tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3e8c441e), toFloat(0x3f156d6a)))
    assertEqualsComplexF(
      csqrtf(tf, buf),
      stackalloc[CFloatComplex].init(toFloat(0x3f8ca1af), toFloat(0x3ee90189)))
    assertEquals(cargf(tf), qtrPI.toFloat)
    assertEquals(cimagf(tf), imag)
    assertEqualsComplexF(conjf(tf, buf),
                         stackalloc[CFloatComplex].init(real, -imag))
    assertEqualsComplexF(cprojf(tf, buf), tf)
    assertEquals(crealf(tf), real)
  }

  test("double complex") {
    val real = 1.0
    val imag = 1.0

    val td  = stackalloc[CDoubleComplex].init(real, imag)
    val buf = stackalloc[CDoubleComplex]

    assertEqualsComplexD(
      cacos(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fecf2214ccccd44"),
                                      toDouble("bff0fafb8f2f147f")))
    assertEqualsComplexD(
      cacos(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fecf2214ccccd44"),
                                      toDouble("bff0fafb8f2f147f")))
    assertEqualsComplexD(
      casin(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fe551d55bbb8ced"),
                                      toDouble("3ff0fafb8f2f147f")))
    assertEqualsComplexD(
      ctan(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd16464f4a33f88"),
                                      toDouble("3ff157bffca4a8bc")))
    assertEqualsComplexD(
      ccos(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3feaadea96f4359b"),
                                      toDouble("bfefa50ccd2ae8f3")))
    assertEqualsComplexD(
      csin(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff4c67b74f6cc4f"),
                                      toDouble("3fe4519fd8047f92")))
    assertEqualsComplexD(
      ctan(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd16464f4a33f88"),
                                      toDouble("3ff157bffca4a8bc")))
    assertEqualsComplexD(
      cacosh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff0fafb8f2f147f"),
                                      toDouble("3fecf2214ccccd44")))
    assertEqualsComplexD(
      casinh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff0fafb8f2f147f"),
                                      toDouble("3fe551d55bbb8ced")))
    assertEqualsComplexD(
      catanh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd9c041f7ed8d33"),
                                      toDouble("3ff0468a8ace4df6")))
    assertEqualsComplexD(
      ccosh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3feaadea96f4359b"),
                                      toDouble("3fefa50ccd2ae8f3")))
    assertEqualsComplexD(
      csinh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fe4519fd8047f92"),
                                      toDouble("3ff4c67b74f6cc4f")))
    assertEqualsComplexD(
      ctanh(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff157bffca4a8bc"),
                                      toDouble("3fd16464f4a33f88")))
    assertEqualsComplexD(
      cexp(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff77fc5377c5a96"),
                                      toDouble("40024c80edc62064")))
    assertEqualsComplexD(
      clog(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd62e42fefa39ef"),
                                      toDouble("3fe921fb54442d18")))
    assertEquals(cabs(td), sqrt2)
    assertEqualsComplexD(
      cpow(td, td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3fd18884016cf327"),
                                      toDouble("3fe2adad36b098aa")))
    assertEqualsComplexD(
      csqrt(td, buf),
      stackalloc[CDoubleComplex].init(toDouble("3ff19435caffa9f9"),
                                      toDouble("3fdd203138f6c828")))
    assertEquals(carg(td), qtrPI)
    assertEquals(cimag(td), imag)
    assertEqualsComplexD(conj(td, buf),
                         stackalloc[CDoubleComplex].init(real, -imag))
    assertEqualsComplexD(cproj(td, buf), td)
    assertEquals(creal(td), real)
  }
}
