package scala.scalanative
package libc

import java.lang as jl

import org.junit.Test
import org.junit.Assert.*

import scalanative.unsafe.*
import scalanative.libc.complex.*
import scalanative.libc.complexOps.*

/** Reference http://en.cppreference.com/w/c/numeric/complex
 *
 *  The complex functions were run in C on macOS to get the results in hex which
 *  are used here for the tests. The C code is below.
 *
 *  Since the code is run on macOS to get results and we need to test on other
 *  platforms we use ULP (Units of least precision) which is the spacing between
 *  floating point numbers to implement an "almost equals" function. We
 *  calculate epsilon, our error value, based on ULP. Currently the complex
 *  double functions use 1 times ulp and the float functions use 2 times ulp for
 *  ccosf and ccoshf tests to pass. Practically this has the effect of making
 *  the epsilon slightly larger for Float. Either way epsilon is a very small
 *  number.
 *
 *  Example printed in hex: 0x1.0p-23 (2*ulp) 0x1.0p-24 (ulp)
 */
class CComplexTest {
  // change for debug
  val debug = false
  // shared values for special calculations
  val qtrPI = Math.PI / 4
  val sqrt2 = Math.sqrt(2)
  val real = 1.0
  val imag = 1.0

  // float complex helper fcns
  def printF(str: String, cp: Ptr[CFloatComplex]): Unit =
    println(s"$str -> ${cp.re} ${cp.im}")

  def toFloat(i: Int): Float = jl.Float.intBitsToFloat(i)

  def isAlmostEqual(act: Float, exp: Float): Boolean = {
    val diff = Math.abs(act - exp)
    val epsilon = 2 * Math.max(Math.ulp(act), Math.ulp(exp))
    if (debug) {
      println(
        s"diff: ${jl.Float.toHexString(diff)} eps: ${jl.Float.toHexString(epsilon)}"
      )
    }
    diff <= epsilon
  }

  def assertEqualsComplexF(
      act: Ptr[CFloatComplex],
      exp: Ptr[CFloatComplex]
  ): Unit =
    assertTrue(isAlmostEqual(act.re, exp.re) && isAlmostEqual(act.im, exp.im))

  def tf(implicit z: Zone) = res(real.toFloat, imag.toFloat)

  def buff(implicit z: Zone) = alloc[CFloatComplex]()

  def res(real: Float, imag: Float)(implicit z: Zone) =
    alloc[CFloatComplex]().init(real.toFloat, imag.toFloat)

  @Test def testCacosf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        cacosf(tf, buff),
        res(toFloat(0x3f67910a), toFloat(0xbf87d7dc))
      )
    }
  }

  @Test def testCasinf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        casinf(tf, buff),
        res(toFloat(0x3f2a8eab), toFloat(0x3f87d7dc))
      )
    }
  }

  @Test def testCatanf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        catanf(tf, buff),
        res(toFloat(0x3f823454), toFloat(0x3ece0210))
      )
    }
  }

  @Test def testCcosf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        ccosf(tf, buff),
        res(toFloat(0x3f556f55), toFloat(0xbf7d2866))
      )
    }
  }

  @Test def testCsinf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        csinf(tf, buff),
        res(toFloat(0x3fa633dc), toFloat(0x3f228cff))
      )
    }
  }

  @Test def testCtanf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        ctanf(tf, buff),
        res(toFloat(0x3e8b2327), toFloat(0x3f8abe00))
      )
    }

  }

  @Test def testCacoshf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        cacoshf(tf, buff),
        res(toFloat(0x3f87d7dc), toFloat(0x3f67910a))
      )
    }
  }

  @Test def testCasinhf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        casinhf(tf, buff),
        res(toFloat(0x3f87d7dc), toFloat(0x3f2a8eab))
      )
    }
  }

  @Test def testCatanhf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        catanhf(tf, buff),
        res(toFloat(0x3ece0210), toFloat(0x3f823454))
      )
    }
  }

  @Test def testCcoshf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        ccoshf(tf, buff),
        res(toFloat(0x3f556f55), toFloat(0x3f7d2866))
      )
    }
  }

  @Test def testCsinhf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        csinhf(tf, buff),
        res(toFloat(0x3f228cff), toFloat(0x3fa633dc))
      )
    }
  }

  @Test def testCtanhf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        ctanhf(tf, buff),
        res(toFloat(0x3f8abe00), toFloat(0x3e8b2327))
      )
    }
  }

  @Test def testCexpf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        cexpf(tf, buff),
        res(toFloat(0x3fbbfe2a), toFloat(0x40126407))
      )
    }
  }

  @Test def testClogf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        clogf(tf, buff),
        res(toFloat(0x3eb17218), toFloat(0x3f490fdb))
      )
    }
  }

  @Test def testCabsf(): Unit = {
    Zone.acquire { implicit z => assertEquals(cabsf(tf), sqrt2.toFloat, 0.0f) }
  }

  @Test def testCpowf(): Unit = {
    Zone.acquire { implicit z =>
      // macOS values:   0x3e8c441e, 0x3f156d6a
      // FreeBSD values: 0x3e8c4421, 0x3f156d69
      assertEqualsComplexF(
        cpowf(tf, tf, buff),
        res(toFloat(0x3e8c4420), toFloat(0x3f156d6a))
      )
    }
  }

  @Test def testCsqrtf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(
        csqrtf(tf, buff),
        res(toFloat(0x3f8ca1af), toFloat(0x3ee90189))
      )
    }
  }

  @Test def testCargf(): Unit = {
    Zone.acquire { implicit z => assertEquals(cargf(tf), qtrPI.toFloat, 0.0f) }
  }

  @Test def testCimagf(): Unit = {
    Zone.acquire { implicit z => assertEquals(cimagf(tf), imag, 0.0f) }
  }

  @Test def testConjf(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexF(conjf(tf, buff), res(real.toFloat, -imag.toFloat))
    }
  }

  @Test def testCprojf(): Unit = {
    Zone.acquire { implicit z => assertEqualsComplexF(cprojf(tf, buff), tf) }
  }

  @Test def testCrealf(): Unit = {
    Zone.acquire { implicit z => assertEquals(crealf(tf), real, 0.0f) }
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
    val diff = Math.abs(act - exp)
    val epsilon = 2 * Math.max(Math.ulp(act), Math.ulp(exp))
    if (debug) {
      println(
        s"diff: ${jl.Double.toHexString(diff)} eps: ${jl.Double.toHexString(epsilon)}"
      )
    }
    diff <= epsilon
  }

  def assertEqualsComplexD(
      act: Ptr[CDoubleComplex],
      exp: Ptr[CDoubleComplex]
  ): Unit =
    assertTrue(isAlmostEqual(act.re, exp.re) && isAlmostEqual(act.im, exp.im))

  def td(implicit z: Zone) = res(real, imag)

  def buf(implicit z: Zone) = alloc[CDoubleComplex]()

  def res(real: Double, imag: Double)(implicit z: Zone) =
    alloc[CDoubleComplex]().init(real, imag)

  @Test def testCacos(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        cacos(td, buf),
        res(toDouble("3fecf2214ccccd44"), toDouble("bff0fafb8f2f147f"))
      )
    }
  }

  @Test def testCasin(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        casin(td, buf),
        res(toDouble("3fe551d55bbb8ced"), toDouble("3ff0fafb8f2f147f"))
      )
    }
  }

  @Test def testCcos(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        ccos(td, buf),
        res(toDouble("3feaadea96f4359b"), toDouble("bfefa50ccd2ae8f3"))
      )
    }
  }

  @Test def testCsin(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        csin(td, buf),
        res(toDouble("3ff4c67b74f6cc4f"), toDouble("3fe4519fd8047f92"))
      )
    }
  }

  @Test def testCtan(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        ctan(td, buf),
        res(toDouble("3fd16464f4a33f88"), toDouble("3ff157bffca4a8bc"))
      )
    }
  }

  @Test def testCacosh(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        cacosh(td, buf),
        res(toDouble("3ff0fafb8f2f147f"), toDouble("3fecf2214ccccd44"))
      )
    }
  }

  @Test def testCasinh(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        casinh(td, buf),
        res(toDouble("3ff0fafb8f2f147f"), toDouble("3fe551d55bbb8ced"))
      )
    }
  }

  @Test def testCatanh(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        catanh(td, buf),
        res(toDouble("3fd9c041f7ed8d33"), toDouble("3ff0468a8ace4df6"))
      )
    }
  }

  @Test def testCcosh(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        ccosh(td, buf),
        res(toDouble("3feaadea96f4359b"), toDouble("3fefa50ccd2ae8f3"))
      )
    }
  }

  @Test def testCsinh(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        csinh(td, buf),
        res(toDouble("3fe4519fd8047f92"), toDouble("3ff4c67b74f6cc4f"))
      )
    }
  }

  @Test def testCtanh(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        ctanh(td, buf),
        res(toDouble("3ff157bffca4a8bc"), toDouble("3fd16464f4a33f88"))
      )
    }
  }

  @Test def testCexp(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        cexp(td, buf),
        res(toDouble("3ff77fc5377c5a96"), toDouble("40024c80edc62064"))
      )
    }
  }

  @Test def testClog(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        clog(td, buf),
        res(toDouble("3fd62e42fefa39ef"), toDouble("3fe921fb54442d18"))
      )
    }
  }

  @Test def testCabs(): Unit = {
    Zone.acquire { implicit z => assertEquals(cabs(td), sqrt2, 0.0) }
  }

  @Test def testCpow(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        cpow(td, td, buf),
        res(toDouble("3fd18884016cf327"), toDouble("3fe2adad36b098aa"))
      )
    }
  }

  @Test def testCsqrt(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(
        csqrt(td, buf),
        res(toDouble("3ff19435caffa9f9"), toDouble("3fdd203138f6c828"))
      )
    }
  }

  @Test def testCarg(): Unit = {
    Zone.acquire { implicit z => assertEquals(carg(td), qtrPI, 0.0) }
  }

  @Test def testCimag(): Unit = {
    Zone.acquire { implicit z => assertEquals(cimag(td), imag, 0.0) }
  }

  @Test def testConj(): Unit = {
    Zone.acquire { implicit z =>
      assertEqualsComplexD(conj(td, buf), res(real, -imag))
    }
  }

  @Test def testCproj(): Unit = {
    Zone.acquire { implicit z => assertEqualsComplexD(cproj(td, buf), td) }
  }

  @Test def testCreal(): Unit = {
    Zone.acquire { implicit z => assertEquals(creal(td), real, 0.0) }
  }
}

//// C code to run and generate complex values
//#include <stdio.h>
//#include <complex.h>
//
//// 1 for view output, 0 for test output
//int view = 1;
//
//// recommend char[255]
//const char* toStrFloat(char* msg, float complex x, char* str) {
//    float re = crealf(x);
//    float im = cimagf(x);
//    if (view) {
//        sprintf(str, "%-7s -> %f, %f", msg, re, im); // dec
//    } else {
//        sprintf(str, "%-7s -> toFloat(0x%08x), toFloat(0x%08x)",
//                msg, *(unsigned int*)&re, *(unsigned int*)&im); //hex
//    }
//    return str;
//}
//
//const char* toStrDouble(char* msg, double complex x, char* str) {
//    double re = creal(x);
//    double im = cimag(x);
//    if (view) {
//        sprintf(str, "%-7s -> %f, %f", msg, re, im); // dec
//    } else {
//        sprintf(str, "%-7s -> toDouble(\"%lx\"), toDouble(\"%lx\")",
//                msg, *(unsigned long*)&re, *(unsigned long*)&im); // hex
//    }
//    return str;
//}
//
//char buf[255];
//
//void printF(char* msg, float complex f) {
//    printf("%s\n", toStrFloat(msg, f, buf));
//}
//
//void printD(char* msg, double complex d) {
//    printf("%s\n", toStrDouble(msg, d, buf));
//}
//
//int main(int argc, const char * argv[]) {
//    printf("Running complex functions...\n");
//
//    float complex f1 = 1.0 + 1.0 * I;
//
//    printf("\nRunning float...\n");
//    printF("f1", f1);
//    printF("cacosf", cacosf(f1));
//    printF("casinf", casinf(f1));
//    printF("catanf", catanf(f1));
//    printF("ccosf", ccosf(f1));
//    printF("csinf", csinf(f1));
//    printF("ctanf", ctanf(f1));
//    printF("cacoshf", cacoshf(f1));
//    printF("casinhf", casinhf(f1));
//    printF("catanhf", catanhf(f1));
//    printF("ccoshf", ccoshf(f1));
//    printF("csinhf", csinhf(f1));
//    printF("ctanhf", ctanhf(f1));
//    printF("cexpf", cexpf(f1));
//    printF("clogf", clogf(f1));
//    printF("cpowf", cpowf(f1, f1));
//    printF("csqrtf", csqrtf(f1));
//
//    double complex d1 = 1.0 + 1.0 * I;
//
//    printf("\nRunning double...\n");
//    printD("d1", d1);
//    printD("cacos", cacos(d1));
//    printD("casin", casin(d1));
//    printD("catan", catan(d1));
//    printD("ccos", ccos(d1));
//    printD("csin", csin(d1));
//    printD("ctan", ctan(d1));
//    printD("cacosh", cacosh(d1));
//    printD("casinh", casinh(d1));
//    printD("catanh", catanh(d1));
//    printD("ccosh", ccosh(d1));
//    printD("csinh", csinh(d1));
//    printD("ctanh", ctanh(d1));
//    printD("cexp", cexp(d1));
//    printD("clog", clog(d1));
//    printD("cpow", cpow(d1, d1));
//    printD("csqrt", csqrt(d1));
//    printf("Done.");
//
//    return 0;
//}
