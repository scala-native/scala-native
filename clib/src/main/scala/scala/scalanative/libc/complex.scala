package scala.scalanative
package libc

import scalanative.unsafe._

/** All functions take complex but Scala Native does not support pass by value
 *  so we pass a pointer to an Array of length 2 and have a small wrapper in C
 *  doing the conversion to call the native function. Currently Scala Native and
 *  JVM have no direct support for long double so these methods are not
 *  implemented.
 *
 *  Since the user must manage the memory, we pass a buffer passed to each
 *  function for storing the result and is also is returned from the function so
 *  functions can be chained together. This adds one additional parameter to the
 *  function compared to the C API.
 *
 *  Implicit classes are provided for convenience.
 *
 *  References: https://en.wikipedia.org/wiki/C_data_types C99 also added
 *  complex types: float _Complex, double _Complex, long double _Complex
 *  https://en.wikipedia.org/wiki/Long_double
 *  http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/complex.h.html
 */
@extern object complex extends complex

@extern private[scalanative] trait complex {
  type CFloatComplex = CStruct2[CFloat, CFloat]
  type CDoubleComplex = CStruct2[CDouble, CDouble]

  @name("scalanative_cacosf")
  def cacosf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_cacos")
  def cacos(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_casinf")
  def casinf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_casin")
  def casin(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_catanf")
  def catanf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_catan")
  def catan(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_ccosf")
  def ccosf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_ccos")
  def ccos(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_csinf")
  def csinf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_csin")
  def csin(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_ctanf")
  def ctanf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_ctan")
  def ctan(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_cacoshf")
  def cacoshf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_cacosh")
  def cacosh(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_casinhf")
  def casinhf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_casinh")
  def casinh(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_catanhf")
  def catanhf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_catanh")
  def catanh(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_ccoshf")
  def ccoshf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_ccosh")
  def ccosh(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_csinhf")
  def csinhf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_csinh")
  def csinh(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_ctanhf")
  def ctanhf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_ctanh")
  def ctanh(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_cexpf")
  def cexpf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_cexp")
  def cexp(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_clogf")
  def clogf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_clog")
  def clog(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_cabsf")
  def cabsf(complex: Ptr[CFloatComplex]): CFloat = extern
  @name("scalanative_cabs")
  def cabs(complex: Ptr[CDoubleComplex]): CDouble = extern
  @name("scalanative_cpowf")
  def cpowf(
      x: Ptr[CFloatComplex],
      y: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_cpow")
  def cpow(
      x: Ptr[CDoubleComplex],
      y: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_csqrtf")
  def csqrtf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_csqrt")
  def csqrt(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_cargf")
  def cargf(complex: Ptr[CFloatComplex]): CFloat = extern
  @name("scalanative_carg")
  def carg(complex: Ptr[CDoubleComplex]): CDouble = extern
  @name("scalanative_cimagf")
  def cimagf(complex: Ptr[CFloatComplex]): CFloat = extern
  @name("scalanative_cimag")
  def cimag(complex: Ptr[CDoubleComplex]): CDouble = extern
  @name("scalanative_conjf")
  def conjf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_conj")
  def conj(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_cprojf")
  def cprojf(
      complex: Ptr[CFloatComplex],
      result: Ptr[CFloatComplex]
  ): Ptr[CFloatComplex] = extern
  @name("scalanative_cproj")
  def cproj(
      complex: Ptr[CDoubleComplex],
      result: Ptr[CDoubleComplex]
  ): Ptr[CDoubleComplex] = extern
  @name("scalanative_crealf")
  def crealf(complex: Ptr[CFloatComplex]): CFloat = extern
  @name("scalanative_creal")
  def creal(complex: Ptr[CDoubleComplex]): CDouble = extern

  // TODO: Add support for long double versions. Depends on #27
  //  extern long double complex cacosl(long double complex);
  //  extern long double complex casinl(long double complex);
  //  extern long double complex catanl(long double complex);
  //  extern long double complex ccosl(long double complex);
  //  extern long double complex csinl(long double complex);
  //  extern long double complex ctanl(long double complex);
  //  extern long double complex cacoshl(long double complex);
  //  extern long double complex casinhl(long double complex);
  //  extern long double complex catanhl(long double complex);
  //  extern long double complex ccoshl(long double complex);
  //  extern long double complex csinhl(long double complex);
  //  extern long double complex ctanhl(long double complex);
  //  extern long double complex cexpl(long double complex);
  //  extern long double complex clogl(long double complex);
  //  extern long double cabsl(long double complex);
  //  extern long double complex cpowl(long double complex, long double complex);
  //  extern long double complex csqrtl(long double complex);
  //  extern long double cargl(long double complex);
  //  extern long double cimagl(long double complex);
  //  extern long double complex conjl(long double complex);
  //  extern long double complex cprojl(long double complex);
  //  extern long double creall(long double complex);
}

object complexOps {
  import complex._

  implicit class complexOpsFloat(val ptr: Ptr[CFloatComplex]) extends AnyVal {
    def re: CFloat = ptr._1
    def re_=(value: CFloat): Unit = ptr._1 = value
    def im: CFloat = ptr._2
    def im_=(value: CFloat): Unit = ptr._2 = value
    def copy(to: Ptr[CFloatComplex]): Ptr[CFloatComplex] = {
      to.re = re
      to.im = im
      to
    }
    def init(re: CFloat, im: CFloat): Ptr[CFloatComplex] = {
      ptr.re = re
      ptr.im = im
      ptr
    }
  }

  implicit class complexOpsDouble(val ptr: Ptr[CDoubleComplex]) extends AnyVal {
    def re: CDouble = ptr._1
    def re_=(value: CDouble): Unit = ptr._1 = value
    def im: CDouble = ptr._2
    def im_=(value: CDouble): Unit = ptr._2 = value
    def copy(to: Ptr[CDoubleComplex]): Ptr[CDoubleComplex] = {
      to.re = re
      to.im = im
      to
    }
    def init(re: CDouble, im: CDouble): Ptr[CDoubleComplex] = {
      ptr.re = re
      ptr.im = im
      ptr
    }
  }
}
