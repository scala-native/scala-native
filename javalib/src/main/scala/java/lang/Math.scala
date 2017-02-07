package java.lang

import scalanative.runtime.Intrinsics._
import scalanative.native.{math => cmath}

object Math {
  private lazy val rand = new java.util.Random

  final val E  = 2.718281828459045
  final val PI = 3.141592653589793

  @inline def abs(a: scala.Double): scala.Double =
    `llvm.fabs.f64`(a)

  @inline def abs(a: scala.Float): scala.Float =
    `llvm.fabs.f32`(a)

  @inline def abs(a: scala.Int): scala.Int =
    if (a < 0) -a else a

  @inline def abs(a: scala.Long): scala.Long =
    if (a < 0) -a else a

  @inline def acos(a: scala.Double): scala.Double =
    cmath.acos(a)

  @inline def addExact(a: scala.Int, b: scala.Int): scala.Int = {
    val overflow = `llvm.sadd.with.overflow.i32`(a, b)
    if (overflow.flag) throw new ArithmeticException("Integer overflow")
    else overflow.value
  }

  @inline def addExact(a: scala.Long, b: scala.Long): scala.Long = {
    val overflow = `llvm.sadd.with.overflow.i64`(a, b)
    if (overflow.flag) throw new ArithmeticException("Long overflow")
    else overflow.value
  }

  @inline def asin(a: scala.Double): scala.Double =
    cmath.asin(a)

  @inline def atan(a: scala.Double): scala.Double =
    cmath.atan(a)

  @inline def atan2(y: scala.Double, x: scala.Double): scala.Double =
    cmath.atan2(y, x)

  @inline def cbrt(a: scala.Double): scala.Double =
    cmath.cbrt(a)

  @inline def ceil(a: scala.Double): scala.Double =
    `llvm.ceil.f64`(a)

  @inline
  def copySign(magnitude: scala.Double, sign: scala.Double): scala.Double =
    `llvm.copysign.f64`(magnitude, sign)

  @inline
  def copySign(magnitude: scala.Float, sign: scala.Float): scala.Float =
    `llvm.copysign.f32`(magnitude, sign)

  @inline def cos(a: scala.Double): scala.Double =
    `llvm.cos.f64`(a)

  @inline def cosh(a: scala.Double): scala.Double =
    cmath.cosh(a)

  @inline def decrementExact(a: scala.Int): scala.Int =
    subtractExact(a, 1)

  @inline def decrementExact(a: scala.Long): scala.Long =
    subtractExact(a, 1L)

  @inline def exp(a: scala.Double): scala.Double =
    `llvm.exp.f64`(a)

  @inline def expm1(a: scala.Double): scala.Double =
    cmath.expm1(a)

  @inline def floor(a: scala.Double): scala.Double =
    `llvm.floor.f64`(a)

  def floorDiv(a: scala.Int, b: scala.Int): scala.Int = {
    val quot = a / b
    if ((a < 0) == (b < 0) || quot * b == a) quot
    else quot - 1
  }

  def floorDiv(a: scala.Long, b: scala.Long): scala.Long = {
    val quot = a / b
    if ((a < 0) == (b < 0) || quot * b == a) quot
    else quot - 1
  }

  def floorMod(a: scala.Int, b: scala.Int): scala.Int = {
    val rem = a % b
    if ((a < 0) == (b < 0) || rem == 0) rem
    else rem + b
  }

  def floorMod(a: scala.Long, b: scala.Long): scala.Long = {
    val rem = a % b
    if ((a < 0) == (b < 0) || rem == 0) rem
    else rem + b
  }

  @inline def getExponent(a: scala.Float): scala.Int =
    cmath.ilogbf(a)

  @inline def getExponent(a: scala.Double): scala.Long =
    cmath.ilogb(a)

  @inline def hypot(a: scala.Double, b: scala.Double): scala.Double =
    cmath.hypot(a, b)

  @inline def IEEEremainder(f1: scala.Double, f2: scala.Double): Double =
    cmath.remainder(f1, f2)

  @inline def incrementExact(a: scala.Int): scala.Int =
    addExact(a, 1)

  @inline def incrementExact(a: scala.Long): scala.Long =
    addExact(a, 1L)

  @inline def log(a: scala.Double): scala.Double =
    `llvm.log.f64`(a)

  @inline def log10(a: scala.Double): scala.Double =
    `llvm.log10.f64`(a)

  @inline def log1p(a: scala.Double): scala.Double =
    cmath.log1p(a)

  @inline def max(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.maxnum.f64`(a, b)

  @inline def max(a: scala.Float, b: scala.Float): scala.Float =
    `llvm.maxnum.f32`(a, b)

  @inline def max(a: scala.Int, b: scala.Int): scala.Int =
    if (a > b) a else b

  @inline def max(a: scala.Long, b: scala.Long): scala.Long =
    if (a > b) a else b

  @inline def min(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.minnum.f64`(a, b)

  @inline def min(a: scala.Float, b: scala.Float): scala.Float =
    `llvm.minnum.f32`(a, b)

  @inline def min(a: scala.Int, b: scala.Int): scala.Int =
    if (a < b) a else b

  @inline def min(a: scala.Long, b: scala.Long): scala.Long =
    if (a < b) a else b

  @inline def multiplyExact(a: scala.Int, b: scala.Int): scala.Int = {
    val overflow = `llvm.smul.with.overflow.i32`(a, b)
    if (overflow.flag) throw new ArithmeticException("Integer overflow")
    else overflow.value
  }

  @inline def multiplyExact(a: scala.Long, b: scala.Long): scala.Long = {
    val overflow = `llvm.smul.with.overflow.i64`(a, b)
    if (overflow.flag) throw new ArithmeticException("Long overflow")
    else overflow.value
  }

  @inline def negateExact(a: scala.Int): scala.Int =
    subtractExact(0, a)

  @inline def negateExact(a: scala.Long): scala.Long =
    subtractExact(0, a)

  def nextAfter(a: scala.Float, b: scala.Double): scala.Float = {
    val aabs = abs(a.toDouble)
    val babs = abs(b)

    if (Float.isNaN(a) || Double.isNaN(b)) {
      Float.NaN
    } else if (aabs == 0f && babs == 0d) {
      b.toFloat
    } else if (aabs == Float.MIN_VALUE && babs < aabs) {
      copySign(0, a)
    } else if (Float.isInfinite(a) && babs < aabs) {
      copySign(Float.MAX_VALUE, a)
    } else if (aabs == Float.MAX_VALUE && babs > aabs) {
      copySign(Float.POSITIVE_INFINITY, a)
    } else if (b > a) {
      cmath.nextafterf(a, Float.POSITIVE_INFINITY)
    } else {
      cmath.nextafterf(a, Float.NEGATIVE_INFINITY)
    }
  }

  def nextAfter(a: scala.Double, b: scala.Double): scala.Double = {
    val aabs = abs(a)
    val babs = abs(b)

    if (Double.isNaN(a) || Double.isNaN(b)) {
      Double.NaN
    } else if (aabs == 0f && babs == 0d) {
      b
    } else if (aabs == Double.MIN_VALUE && babs < aabs) {
      copySign(0, a)
    } else if (Double.isInfinite(a) && babs < aabs) {
      copySign(Double.MAX_VALUE, a)
    } else if (aabs == Double.MAX_VALUE && babs > aabs) {
      copySign(Double.POSITIVE_INFINITY, a)
    } else {
      cmath.nextafter(a, b)
    }
  }

  def nextDown(a: scala.Float): scala.Float =
    nextAfter(a, Double.NEGATIVE_INFINITY)

  def nextDown(a: scala.Double): scala.Double =
    nextAfter(a, Double.NEGATIVE_INFINITY)

  def nextUp(a: scala.Float): scala.Float =
    nextAfter(a, Double.POSITIVE_INFINITY)

  def nextUp(a: scala.Double): scala.Double =
    nextAfter(a, Double.POSITIVE_INFINITY)

  @inline def pow(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.pow.f64`(a, b)

  @inline def random(): scala.Double =
    rand.nextDouble()

  @inline def rint(a: scala.Double): scala.Double =
    `llvm.rint.f64`(a)

  @inline def round(a: scala.Float): scala.Int =
    `llvm.round.f32`(a).toInt

  @inline def round(a: scala.Double): scala.Long =
    `llvm.round.f64`(a).toLong

  @inline def scalb(a: scala.Float, scaleFactor: scala.Int): scala.Float =
    cmath.scalbnf(a, scaleFactor)

  @inline def scalb(a: scala.Double, scaleFactor: scala.Int): scala.Double =
    cmath.scalbn(a, scaleFactor)

  @inline def signum(a: scala.Float): scala.Float = {
    if (a > 0) 1.0f
    else if (a < 0) -1.0f
    else a
  }

  @inline def signum(a: scala.Double): scala.Double = {
    if (a > 0) 1.0
    else if (a < 0) -1.0
    else a
  }

  @inline def sin(a: scala.Double): scala.Double =
    `llvm.sin.f64`(a)

  @inline def sinh(a: scala.Double): scala.Double =
    cmath.sinh(a)

  @inline def sqrt(a: scala.Double): scala.Double =
    `llvm.sqrt.f64`(a)

  @inline def subtractExact(a: scala.Int, b: scala.Int): scala.Int = {
    val overflow = `llvm.ssub.with.overflow.i32`(a, b)
    if (overflow.flag) throw new ArithmeticException("Integer overflow")
    else overflow.value
  }

  @inline def subtractExact(a: scala.Long, b: scala.Long): scala.Long = {
    val overflow = `llvm.ssub.with.overflow.i64`(a, b)
    if (overflow.flag) throw new ArithmeticException("Long overflow")
    else overflow.value
  }

  @inline def tan(a: scala.Double): scala.Double =
    cmath.tan(a)

  @inline def tanh(a: scala.Double): scala.Double =
    cmath.tanh(a)

  @inline def toDegrees(a: scala.Double): scala.Double = a * 180.0 / PI

  def toIntExact(a: scala.Long): scala.Int =
    if (a >= Integer.MIN_VALUE && a <= Integer.MAX_VALUE) a.toInt
    else throw new ArithmeticException("Integer overflow")

  @inline def toRadians(a: scala.Double): scala.Double = a / 180.0 * PI

  def ulp(a: scala.Float): scala.Float = {
    if (Float.isNaN(a)) {
      Float.NaN
    } else if (Float.isInfinite(a)) {
      Float.POSITIVE_INFINITY
    } else if (abs(a) == Float.MAX_VALUE) {
      2.028241E31F // pow(2, 104).toFloat
    } else {
      val f = abs(a)
      cmath.nextafterf(f, Float.MAX_VALUE) - f
    }
  }

  def ulp(a: scala.Double): scala.Double = {
    if (Double.isInfinite(a)) {
      scala.Double.PositiveInfinity
    } else if (a == scala.Double.MaxValue || a == -Double.MAX_VALUE) {
      1.9958403095347198E292D // pow(2, 971)
    } else {
      val d = abs(a)
      cmath.nextafter(d, scala.Double.MaxValue) - d
    }
  }
}
