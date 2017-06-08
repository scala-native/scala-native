package scala.scalanative.native

import complex._
import complexOps._

// Reference
// http://en.cppreference.com/w/c/numeric/complex
// TODO: commented out print statements need tests
object CComplexSuite extends tests.Suite {
  // helpers to see results
  def printD(str: String, cp: Ptr[CDoubleComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")
  def printF(str: String, cp: Ptr[CFloatComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")

  //assert(equalsEpsilon(cabsf(tf),  Math.sqrt(2.0).toFloat, eps)) // example
  def equalsEpsilon(a: Float, b: Float, epsilon: Float): Boolean =
    Math.abs(a - b) <= epsilon
  def equalsEpsilon(a: Double, b: Double, epsilon: Double): Boolean =
    Math.abs(a - b) <= epsilon

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
    val ulp  = Math.ulp(1.0f)
    val eps  = Math.nextAfter(ulp / 2, 1.0f)

    val tf  = stackalloc[CFloatComplex].init(real, imag)
    val buf = stackalloc[CFloatComplex]

//    printF("cacosf", cacosf(tf, buf))
//    printF("casinf", casinf(tf, buf))
//    printF("catanf", catanf(tf, buf))
//    printF("ccosf", ccosf(tf, buf))
//    printF("csinf", csinf(tf, buf))
//    printF("ctanf", ctanf(tf, buf))
//    printF("cacoshf", cacoshf(tf, buf))
//    printF("casinhf", casinhf(tf, buf))
//    printF("catanhf", catanhf(tf, buf))
//    printF("ccoshf", ccoshf(tf, buf))
//    printF("csinhf", csinhf(tf, buf))
//    printF("ctanhf", ctanhf(tf, buf))
//    printF("cexpf", cexpf(tf, buf))
//    printF("clogf", clogf(tf, buf))

    assertEquals(cabsf(tf), sqrt2.toFloat)

//    printF("cpowf", cpowf(tf, tf, buf))
//    printF("csqrtf", csqrtf(tf, buf))

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
    val ulp  = Math.ulp(1.0)
    val eps  = Math.nextAfter(ulp / 2, 1.0)

    val td  = stackalloc[CDoubleComplex].init(real, imag)
    val buf = stackalloc[CDoubleComplex]

//    printD("cacos", cacos(td, buf))
//    printD("casin", casin(td, buf))
//    printD("catan", catan(td, buf))
//    printD("ccos", ccos(td, buf))
//    printD("csin", csin(td, buf))
//    printD("ctan", ctan(td, buf))
//    printD("cacosh", cacosh(td, buf))
//    printD("casinh", casinh(td, buf))
//    printD("catanh", catanh(td, buf))
//    printD("ccosh", ccosh(td, buf))
//    printD("csinh", csinh(td, buf))
//    printD("ctanh", ctanh(td, buf))
//    printD("cexp", cexp(td, buf))
//    printD("clog", clog(td, buf))

    assertEquals(cabs(td), sqrt2)

//    printD("cpow", cpow(td, td, buf))
//    printD("csqrt", csqrt(td, buf))

    assertEquals(carg(td), qtrPI)
    assertEquals(cimag(td), imag)
    assertEqualsComplexD(conj(td, buf),
                         stackalloc[CDoubleComplex].init(real, -imag))
    assertEqualsComplexD(cproj(td, buf), td)
    assertEquals(creal(td), real)
  }
}
