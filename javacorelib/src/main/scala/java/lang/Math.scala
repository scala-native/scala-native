package java.lang

import scalanative.runtime.LLVMIntrinsics._
import scalanative.libc.{math => cmath}
import scalanative.annotation.alwaysinline

private[lang] object MathRand {
  val rand = new java.util.Random
}

object Math {
  final val E  = 2.718281828459045
  final val PI = 3.141592653589793

  @alwaysinline def abs(a: scala.Double): scala.Double =
    `llvm.fabs.f64`(a)

  @alwaysinline def abs(a: scala.Float): scala.Float =
    `llvm.fabs.f32`(a)

  @alwaysinline def abs(a: scala.Int): scala.Int =
    if (a < 0) -a else a

  @alwaysinline def abs(a: scala.Long): scala.Long =
    if (a < 0) -a else a

  @alwaysinline def acos(a: scala.Double): scala.Double =
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

  @alwaysinline def asin(a: scala.Double): scala.Double =
    cmath.asin(a)

  @alwaysinline def atan(a: scala.Double): scala.Double =
    cmath.atan(a)

  @alwaysinline def atan2(y: scala.Double, x: scala.Double): scala.Double =
    cmath.atan2(y, x)

  @alwaysinline def cbrt(a: scala.Double): scala.Double =
    cmath.cbrt(a)

  @alwaysinline def ceil(a: scala.Double): scala.Double =
    `llvm.ceil.f64`(a)

  @alwaysinline
  def copySign(magnitude: scala.Double, sign: scala.Double): scala.Double =
    `llvm.copysign.f64`(magnitude, sign)

  @alwaysinline
  def copySign(magnitude: scala.Float, sign: scala.Float): scala.Float =
    `llvm.copysign.f32`(magnitude, sign)

  @alwaysinline def cos(a: scala.Double): scala.Double =
    `llvm.cos.f64`(a)

  @alwaysinline def cosh(a: scala.Double): scala.Double =
    cmath.cosh(a)

  @alwaysinline def decrementExact(a: scala.Int): scala.Int =
    subtractExact(a, 1)

  @alwaysinline def decrementExact(a: scala.Long): scala.Long =
    subtractExact(a, 1L)

  @alwaysinline def exp(a: scala.Double): scala.Double =
    `llvm.exp.f64`(a)

  @alwaysinline def expm1(a: scala.Double): scala.Double =
    cmath.expm1(a)

  @alwaysinline def floor(a: scala.Double): scala.Double =
    `llvm.floor.f64`(a)

  @inline def floorDiv(a: scala.Int, b: scala.Int): scala.Int = {
    val quot = a / b
    if ((a < 0) == (b < 0) || quot * b == a) quot
    else quot - 1
  }

  @inline def floorDiv(a: scala.Long, b: scala.Long): scala.Long = {
    val quot = a / b
    if ((a < 0) == (b < 0) || quot * b == a) quot
    else quot - 1
  }

  @inline def floorMod(a: scala.Int, b: scala.Int): scala.Int = {
    val rem = a % b
    if ((a < 0) == (b < 0) || rem == 0) rem
    else rem + b
  }

  @inline def floorMod(a: scala.Long, b: scala.Long): scala.Long = {
    val rem = a % b
    if ((a < 0) == (b < 0) || rem == 0) rem
    else rem + b
  }

  @alwaysinline def getExponent(a: scala.Float): scala.Int =
    cmath.ilogbf(a)

  @alwaysinline def getExponent(a: scala.Double): scala.Long =
    cmath.ilogb(a)

  @alwaysinline def hypot(a: scala.Double, b: scala.Double): scala.Double =
    cmath.hypot(a, b)

  @alwaysinline def IEEEremainder(f1: scala.Double, f2: scala.Double): Double =
    cmath.remainder(f1, f2)

  @alwaysinline def incrementExact(a: scala.Int): scala.Int =
    addExact(a, 1)

  @alwaysinline def incrementExact(a: scala.Long): scala.Long =
    addExact(a, 1L)

  @alwaysinline def log(a: scala.Double): scala.Double =
    `llvm.log.f64`(a)

  @alwaysinline def log10(a: scala.Double): scala.Double =
    `llvm.log10.f64`(a)

  @alwaysinline def log1p(a: scala.Double): scala.Double =
    cmath.log1p(a)

  @alwaysinline def max(a: scala.Double, b: scala.Double): scala.Double =
    if (a.isNaN() || b.isNaN()) Double.NaN else `llvm.maxnum.f64`(a, b)

  @alwaysinline def max(a: scala.Float, b: scala.Float): scala.Float =
    if (a.isNaN() || b.isNaN()) Float.NaN else `llvm.maxnum.f32`(a, b)

  @alwaysinline def max(a: scala.Int, b: scala.Int): scala.Int =
    if (a > b) a else b

  @alwaysinline def max(a: scala.Long, b: scala.Long): scala.Long =
    if (a > b) a else b

  @alwaysinline def min(a: scala.Double, b: scala.Double): scala.Double =
    if (a.isNaN() || b.isNaN()) Double.NaN else `llvm.minnum.f64`(a, b)

  @alwaysinline def min(a: scala.Float, b: scala.Float): scala.Float =
    if (a.isNaN() || b.isNaN()) Float.NaN else `llvm.minnum.f32`(a, b)

  @alwaysinline def min(a: scala.Int, b: scala.Int): scala.Int =
    if (a < b) a else b

  @alwaysinline def min(a: scala.Long, b: scala.Long): scala.Long =
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

  @alwaysinline def negateExact(a: scala.Int): scala.Int =
    subtractExact(0, a)

  @alwaysinline def negateExact(a: scala.Long): scala.Long =
    subtractExact(0L, a)

  def nextAfter(a: scala.Float, b: scala.Double): scala.Float = {
    val aabs = abs(a.toDouble)
    val babs = abs(b)

    if (Float.isNaN(a) || Double.isNaN(b)) {
      Float.NaN
    } else if (aabs == 0.0f && babs == 0.0d) {
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
    } else if (aabs == 0.0d && babs == 0.0d) {
      b
    } else if (aabs == Double.MIN_VALUE && babs < aabs) {
      copySign(0.0d, a)
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

  @alwaysinline def pow(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.pow.f64`(a, b)

  @alwaysinline def random(): scala.Double =
    MathRand.rand.nextDouble()

  @alwaysinline def rint(a: scala.Double): scala.Double =
    `llvm.rint.f64`(a)

  @inline def round(a: scala.Float): scala.Int = {
    if (a.isNaN()) {
      0
    } else if (a >= scala.Int.MaxValue.toFloat - 0.5f) {
      scala.Int.MaxValue
    } else if (a <= scala.Int.MinValue.toFloat) {
      scala.Int.MinValue
    } else {
      // Java rounds both +/- half to towards +Infinity.
      // In its default rounding mode, llvm.round.f32 rounds half away
      // from zero (+/- Infinity).
      math.floor(a + 0.5f).toInt
    }
  }

  @inline def round(a: scala.Double): scala.Long = {
    if (a.isNaN()) {
      0L
    } else if (a >= scala.Long.MaxValue.toDouble - 0.5d) {
      scala.Long.MaxValue
    } else if (a <= scala.Long.MinValue.toDouble) {
      scala.Long.MinValue
    } else {
      // Java rounds both +/- half towards +Infinity.
      // In its default rounding mode, llvm.round.f64 rounds half away
      // from zero (+/- Infinity).
      math.floor(a + 0.5d).toLong
    }
  }

  @alwaysinline def scalb(a: scala.Float, scaleFactor: scala.Int): scala.Float =
    cmath.scalbnf(a, scaleFactor)

  @alwaysinline def scalb(a: scala.Double,
                          scaleFactor: scala.Int): scala.Double =
    cmath.scalbn(a, scaleFactor)

  @alwaysinline def signum(a: scala.Float): scala.Float = {
    if (a > 0) 1.0f
    else if (a < 0) -1.0f
    else a
  }

  @alwaysinline def signum(a: scala.Double): scala.Double = {
    if (a > 0) 1.0
    else if (a < 0) -1.0
    else a
  }

  @alwaysinline def sin(a: scala.Double): scala.Double =
    `llvm.sin.f64`(a)

  @alwaysinline def sinh(a: scala.Double): scala.Double =
    cmath.sinh(a)

  @alwaysinline def sqrt(a: scala.Double): scala.Double =
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

  @alwaysinline def tan(a: scala.Double): scala.Double =
    cmath.tan(a)

  @alwaysinline def tanh(a: scala.Double): scala.Double =
    cmath.tanh(a)

  @alwaysinline def toDegrees(a: scala.Double): scala.Double = a * 180.0 / PI

  def toIntExact(a: scala.Long): scala.Int =
    if (a >= Integer.MIN_VALUE && a <= Integer.MAX_VALUE) a.toInt
    else throw new ArithmeticException("Integer overflow")

  @alwaysinline def toRadians(a: scala.Double): scala.Double = a / 180.0 * PI

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
