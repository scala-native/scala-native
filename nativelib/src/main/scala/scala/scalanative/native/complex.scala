package scala.scalanative
package native

/** All methods take complex but Scala Native does not
 * support pass by value so we pass a pointer to a
 * Array of length 2 and have a small wrapper in C
 * to do the conversion. Currently Scala Native
 * and JVM have no direct support for long double
 * so these methods are not implemented.
 *
 * Warning: Ptr[CComplexFloat] and Ptr[CComplex] values
 * passed in to the functions are mutated for
 * memory safety. Make copies on the stack or heap
 * for values you need after the functions are called.
 * Implicit classes are provided for convenience.
 *
 *
 * References:
 * https://en.wikipedia.org/wiki/C_data_types
 * C99 also added complex types: float _Complex, double _Complex, long double _Complex
 * https://en.wikipedia.org/wiki/Long_double
 * http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/complex.h.html
 *
 */
@extern
object complex {
  import Nat._2
  type CComplexFloat = CArray[CFloat, _2]
  type CComplex      = CArray[CDouble, _2]

  @name("scalanative_cacosf")
  def cacosf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_cacos")
  def cacos(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex cacosl(long double complex);

  @name("scalanative_casinf")
  def casinf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_casin")
  def casin(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex casinl(long double complex);

  @name("scalanative_catanf")
  def catanf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_catan")
  def catan(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex catanl(long double complex);

  @name("scalanative_ccosf")
  def ccosf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_ccos")
  def ccos(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex ccosl(long double complex);

  @name("scalanative_csinf")
  def csinf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_csin")
  def csin(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex csinl(long double complex);

  @name("scalanative_ctanf")
  def ctanf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_ctan")
  def ctan(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex ctanl(long double complex);

  @name("scalanative_cacoshf")
  def cacoshf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_cacosh")
  def cacosh(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex cacoshl(long double complex);

  @name("scalanative_casinhf")
  def casinhf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_casinh")
  def casinh(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex casinhl(long double complex);

  @name("scalanative_catanhf")
  def catanhf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_catanh")
  def catanh(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex catanhl(long double complex);

  @name("scalanative_ccoshf")
  def ccoshf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_ccosh")
  def ccosh(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex ccoshl(long double complex);

  @name("scalanative_csinhf")
  def csinhf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_csinh")
  def csinh(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex csinhl(long double complex);

  @name("scalanative_ctanhf")
  def ctanhf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_ctanh")
  def ctanh(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex ctanhl(long double complex);

  @name("scalanative_cexpf")
  def cexpf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_cexp")
  def cexp(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex cexpl(long double complex);

  @name("scalanative_clogf")
  def clogf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_clog")
  def clog(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex clogl(long double complex);

  @name("scalanative_cabsf")
  def cabsf(complex: Ptr[CComplexFloat]): CFloat = extern
  @name("scalanative_cabs")
  def cabs(complex: Ptr[CComplex]): CDouble = extern
  //  extern long double cabsl(long double complex);

  @name("scalanative_cpowf")
  def cpowf(x: Ptr[CComplexFloat], y: Ptr[CComplexFloat]): Ptr[CComplexFloat] =
    extern
  @name("scalanative_cpow")
  def cpow(x: Ptr[CComplex], y: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex cpowl(long double complex, long double complex);

  @name("scalanative_csqrtf")
  def csqrtf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_csqrt")
  def csqrt(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex csqrtl(long double complex);

  @name("scalanative_cargf")
  def cargf(complex: Ptr[CComplexFloat]): CFloat = extern
  @name("scalanative_carg")
  def carg(complex: Ptr[CComplex]): CDouble = extern
  //  extern long double cargl(long double complex);

  @name("scalanative_cimagf")
  def cimagf(complex: Ptr[CComplexFloat]): CFloat = extern
  @name("scalanative_cimag")
  def cimag(complex: Ptr[CComplex]): CDouble = extern
  //  extern long double cimagl(long double complex);

  @name("scalanative_conjf")
  def conjf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_conj")
  def conj(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex conjl(long double complex);

  @name("scalanative_cprojf")
  def cprojf(complex: Ptr[CComplexFloat]): Ptr[CComplexFloat] = extern
  @name("scalanative_cproj")
  def cproj(complex: Ptr[CComplex]): Ptr[CComplex] = extern
  //  extern long double complex cprojl(long double complex);

  @name("scalanative_crealf")
  def crealf(complex: Ptr[CComplexFloat]): CFloat = extern
  @name("scalanative_creal")
  def creal(complex: Ptr[CComplex]): CDouble = extern
  //  extern long double creall(long double complex);
}

object complexOps {
  import complex._

  implicit class complexOpsFloat(val ptr: Ptr[CComplexFloat]) extends AnyVal {
    def re: CFloat = !(ptr._1)
    def re_=(value: CFloat): Unit = {
      !ptr._1 = value
    }
    def im: CFloat = !(ptr._2)
    def im_=(value: CFloat): Unit = {
      !ptr._2 = value
    }
    def copy(to: Ptr[CComplexFloat]): Ptr[CComplexFloat] = {
      to.re = re
      to.im = im
      to
    }
    def init(re: CFloat, im: CFloat): Ptr[CComplexFloat] = {
      ptr.re = re
      ptr.im = im
      ptr
    }
  }

  implicit class complexOpsDouble(val ptr: Ptr[CComplex]) extends AnyVal {
    def re: CDouble = !(ptr._1)
    def re_=(value: CDouble): Unit = {
      !ptr._1 = value
    }
    def im: CDouble = !(ptr._2)
    def im_=(value: CDouble): Unit = {
      !ptr._2 = value
    }
    def copy(to: Ptr[CComplex]): Ptr[CComplex] = {
      to.re = re
      to.im = im
      to
    }
    def init(re: CDouble, im: CDouble): Ptr[CComplex] = {
      ptr.re = re
      ptr.im = im
      ptr
    }
  }
}
