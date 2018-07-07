package scala.scalanative
package native

@extern
object math {

  // Basic operations

  def abs(x: CInt): CInt                                      = extern
  def labs(x: CLong): CLong                                   = extern
  def llabs(x: CLongLong): CLongLong                          = extern
  def fabsf(arg: CFloat): CFloat                              = extern
  def fabs(arg: CDouble): CDouble                             = extern
  def fmodf(x: CFloat, y: CFloat): CFloat                     = extern
  def fmod(x: CDouble, y: CDouble): CDouble                   = extern
  def remainderf(x: CFloat, y: CFloat): CFloat                = extern
  def remainder(x: CDouble, y: CDouble): CDouble              = extern
  def remquof(x: CFloat, y: CFloat, quo: Ptr[CInt]): CFloat   = extern
  def remquo(x: CDouble, y: CDouble, quo: Ptr[CInt]): CDouble = extern
  def fmaf(x: CFloat, y: CFloat, z: CFloat): CFloat           = extern
  def fma(x: CDouble, y: CDouble, z: CDouble): CDouble        = extern
  def fmaxf(x: CFloat, y: CFloat): CFloat                     = extern
  def fmax(x: CDouble, y: CDouble): CDouble                   = extern
  def fminf(x: CFloat, y: CFloat): CFloat                     = extern
  def fmin(x: CDouble, y: CDouble): CDouble                   = extern
  def fdimf(x: CFloat, y: CFloat): CFloat                     = extern
  def fdim(x: CDouble, y: CDouble): CDouble                   = extern
  def nanf(str: CString): CFloat                              = extern
  def nan(str: CString): CDouble                              = extern

  // Exponential functions

  def expf(x: CFloat): CFloat    = extern
  def exp(x: CDouble): CDouble   = extern
  def exp2f(x: CFloat): CFloat   = extern
  def exp2(x: CDouble): CDouble  = extern
  def expm1f(x: CFloat): CFloat  = extern
  def expm1(x: CDouble): CDouble = extern
  def logf(x: CFloat): CFloat    = extern
  def log(x: CDouble): CDouble   = extern
  def log10f(x: CFloat): CFloat  = extern
  def log10(x: CDouble): CDouble = extern
  def log2f(x: CFloat): CFloat   = extern
  def log2(x: CDouble): CDouble  = extern
  def log1pf(x: CFloat): CFloat  = extern
  def log1p(x: CDouble): CDouble = extern

  // Power functions

  def powf(base: CFloat, exponent: CFloat): CFloat   = extern
  def pow(base: CDouble, exponent: CDouble): CDouble = extern
  def sqrtf(x: CFloat): CFloat                       = extern
  def sqrt(x: CDouble): CDouble                      = extern
  def cbrtf(x: CFloat): CFloat                       = extern
  def cbrt(x: CDouble): CDouble                      = extern
  def hypotf(x: CFloat, y: CFloat): CFloat           = extern
  def hypot(x: CDouble, y: CDouble): CDouble         = extern

  // Trigonometric functions

  def sinf(x: CFloat): CFloat                = extern
  def sin(x: CDouble): CDouble               = extern
  def cosf(x: CFloat): CFloat                = extern
  def cos(x: CDouble): CDouble               = extern
  def tanf(x: CFloat): CFloat                = extern
  def tan(x: CDouble): CDouble               = extern
  def asinf(x: CFloat): CFloat               = extern
  def asin(x: CDouble): CDouble              = extern
  def acosf(x: CFloat): CFloat               = extern
  def acos(x: CDouble): CDouble              = extern
  def atanf(x: CFloat): CFloat               = extern
  def atan(x: CDouble): CDouble              = extern
  def atan2f(y: CFloat, x: CFloat): CFloat   = extern
  def atan2(y: CDouble, x: CDouble): CDouble = extern

  // Hyperbolic functions

  def sinhf(x: CFloat): CFloat   = extern
  def sinh(x: CDouble): CDouble  = extern
  def coshf(x: CFloat): CFloat   = extern
  def cosh(x: CDouble): CDouble  = extern
  def tanhf(x: CFloat): CFloat   = extern
  def tanh(x: CDouble): CDouble  = extern
  def asinhf(x: CFloat): CFloat  = extern
  def asinh(x: CDouble): CDouble = extern
  def atanhf(x: CFloat): CFloat  = extern
  def atanh(x: CDouble): CDouble = extern

  // Error and gamma functions

  def erff(x: CFloat): CFloat     = extern
  def erf(x: CDouble): CDouble    = extern
  def erfcf(x: CFloat): CFloat    = extern
  def erfc(x: CDouble): CDouble   = extern
  def tgammaf(x: CFloat): CFloat  = extern
  def tgamma(x: CDouble): CDouble = extern
  def lgammaf(x: CFloat): CFloat  = extern
  def lgamma(x: CDouble): CDouble = extern

  // Nearest integer floating-point operations

  def ceilf(x: CFloat): CFloat       = extern
  def ceil(x: CFloat): CFloat        = extern
  def floorf(x: CFloat): CFloat      = extern
  def floor(x: CDouble): CDouble     = extern
  def truncf(x: CFloat): CFloat      = extern
  def trunc(x: CDouble): CDouble     = extern
  def roundf(x: CFloat): CFloat      = extern
  def round(x: CDouble): CDouble     = extern
  def lroundf(x: CFloat): CLong      = extern
  def lround(x: CDouble): CLong      = extern
  def llroundf(x: CFloat): CLongLong = extern
  def llround(x: CDouble): CLongLong = extern
  def nearbyintf(x: CFloat): CFloat  = extern
  def nearbyint(x: CDouble): CDouble = extern
  def rintf(x: CFloat): CFloat       = extern
  def rint(x: CDouble): CDouble      = extern
  def lrintf(x: CFloat): CLong       = extern
  def lrint(x: CDouble): CLong       = extern
  def llrintf(x: CFloat): CLongLong  = extern
  def llrint(x: CDouble): CLongLong  = extern

  // Floating-point manipulation functions

  def frexpf(arg: CFloat, exp: Ptr[CInt]): CFloat     = extern
  def frexp(arg: CDouble, exp: Ptr[CInt]): CDouble    = extern
  def ldexpf(arg: CFloat, exp: CInt): CFloat          = extern
  def ldexp(arg: CDouble, exp: CInt): CDouble         = extern
  def modff(arg: CFloat, iptr: Ptr[CFloat]): CFloat   = extern
  def modf(arg: CDouble, iptr: Ptr[CDouble]): CDouble = extern
  def scalbnf(arg: CFloat, exp: CInt): CFloat         = extern
  def scalbn(arg: CDouble, exp: CInt): CDouble        = extern
  def scalblnf(arg: CFloat, exp: CLong): CFloat       = extern
  def scalbln(arg: CDouble, exp: CLong): CDouble      = extern
  def ilogbf(x: CFloat): CInt                         = extern
  def ilogb(x: CDouble): CInt                         = extern
  def logbf(x: CFloat): CFloat                        = extern
  def logb(x: CDouble): CDouble                       = extern
  def nextafterf(from: CFloat, to: CFloat): CFloat    = extern
  def nextafter(from: CDouble, to: CDouble): CDouble  = extern
  def copysignf(x: CFloat, y: CFloat): CFloat         = extern
  def copysign(x: CDouble, y: CDouble): CDouble       = extern

  // Macros

  @name("scalanative_libc_huge_valf")
  def HUGE_VALF: CFloat = extern
  @name("scalanative_libc_huge_val")
  def HUGE_VAL: CDouble = extern
  @name("scalanative_libc_infinity")
  def INFINITY: CFloat = extern
  @name("scalanative_libc_nan")
  def NAN: CFloat = extern
  @name("scalanative_libc_math_errhandling")
  def math_errhandling: CInt = extern
  @name("scalanative_libc_math_errno")
  def MATH_ERRNO: CInt = extern
  @name("scalanative_libc_math_errexcept")
  def MATH_ERREXCEPT: CInt = extern
}
