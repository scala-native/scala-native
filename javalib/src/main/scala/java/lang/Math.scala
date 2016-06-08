package java.lang

import scalanative.runtime.Intrinsics._

object Math {
  final val E  = 2.718281828459045
  final val PI = 3.141592653589793

  @inline def abs(a: scala.Int): scala.Int       = if (a < 0) -a else a
  @inline def abs(a: scala.Long): scala.Long     = if (a < 0) -a else a
  @inline def abs(a: scala.Float): scala.Float   = `llvm.fabs.f32`(a)
  @inline def abs(a: scala.Double): scala.Double = `llvm.fabs.f64`(a)

  @inline def max(a: scala.Int, b: scala.Int): scala.Int = if (a > b) a else b
  @inline def max(a: scala.Long, b: scala.Long): scala.Long =
    if (a > b) a else b
  @inline def max(a: scala.Float, b: scala.Float): scala.Float =
    `llvm.maxnum.f32`(a, b)
  @inline def max(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.maxnum.f64`(a, b)

  @inline def min(a: scala.Int, b: scala.Int): scala.Int = if (a < b) a else b
  @inline def min(a: scala.Long, b: scala.Long): scala.Long =
    if (a < b) a else b
  @inline def min(a: scala.Float, b: scala.Float): scala.Float =
    `llvm.minnum.f32`(a, b)
  @inline def min(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.maxnum.f64`(a, b)

  @inline def ceil(a: scala.Double): scala.Double  = `llvm.ceil.f64`(a)
  @inline def floor(a: scala.Double): scala.Double = `llvm.floor.f64`(a)
  @inline def rint(a: scala.Double): scala.Double  = `llvm.rint.f64`(a)

  @inline def round(a: scala.Float): scala.Int   = `llvm.round.f32`(a).toInt
  @inline def round(a: scala.Double): scala.Long = `llvm.round.f64`(a).toLong

  @inline def sqrt(a: scala.Double): scala.Double = `llvm.sqrt.f64`(a)
  @inline def pow(a: scala.Double, b: scala.Double): scala.Double =
    `llvm.pow.f64`(a, b)

  @inline def exp(a: scala.Double): scala.Double   = `llvm.exp.f64`(a)
  @inline def log(a: scala.Double): scala.Double   = `llvm.log.f64`(a)
  @inline def log10(a: scala.Double): scala.Double = `llvm.log10.f64`(a)
  @inline def log1p(a: scala.Double): scala.Double = log(a + 1)

  @inline def sin(a: scala.Double): scala.Double                    = `llvm.sin.f64`(a)
  @inline def cos(a: scala.Double): scala.Double                    = `llvm.cos.f64`(a)
  @inline def tan(a: scala.Double): scala.Double                    = ???
  @inline def asin(a: scala.Double): scala.Double                   = ???
  @inline def acos(a: scala.Double): scala.Double                   = ???
  @inline def atan(a: scala.Double): scala.Double                   = ???
  @inline def atan2(y: scala.Double, x: scala.Double): scala.Double = ???

  @inline def random(): scala.Double = ???

  @inline def toDegrees(a: scala.Double): scala.Double = a * 180.0 / PI
  @inline def toRadians(a: scala.Double): scala.Double = a / 180.0 * PI

  @inline def signum(a: scala.Double): scala.Double = {
    if (a > 0) 1.0
    else if (a < 0) -1.0
    else a
  }

  @inline def signum(a: scala.Float): scala.Float = {
    if (a > 0) 1.0f
    else if (a < 0) -1.0f
    else a
  }

  def cbrt(a: scala.Double): scala.Double = ???

  def nextUp(a: scala.Double): scala.Double = {
    // js implementation of nextUp https://gist.github.com/Yaffle/4654250
    import scala.Double._
    if (a != a || a == PositiveInfinity)
      a
    else if (a == NegativeInfinity)
      MinValue
    else if (a == MaxValue)
      PositiveInfinity
    else if (a == 0)
      MinPositiveValue
    else {
      def iter(
          x: scala.Double, xi: scala.Double, n: scala.Double): scala.Double = {
        if (Math.abs(xi - x) >= 1E-16) {
          val c0 = (xi + x) / 2
          val c =
            if (c0 == NegativeInfinity || c0 == PositiveInfinity)
              x + (xi - x) / 2
            else
              c0
          if (n == c) xi
          else if (a < c) iter(x = x, xi = c, n = c)
          else iter(x = c, xi = xi, n = c)
        } else xi
      }
      val d  = Math.max(Math.abs(a) * 2E-16, MinPositiveValue)
      val ad = a + d
      val xi0 =
        if (ad == PositiveInfinity) MaxValue
        else ad
      iter(x = a, xi = xi0, n = a)
    }
  }

  def nextAfter(a: scala.Double, b: scala.Double): scala.Double = {
    if (b < a)
      -nextUp(-a)
    else if (a < b)
      nextUp(a)
    else if (a != a || b != b)
      scala.Double.NaN
    else
      b
  }

  def ulp(a: scala.Double): scala.Double = {
    if (abs(a) == scala.Double.PositiveInfinity)
      scala.Double.PositiveInfinity
    else if (abs(a) == scala.Double.MaxValue)
      pow(2, 971)
    else
      nextAfter(abs(a), scala.Double.MaxValue) - a
  }

  def hypot(a: scala.Double, b: scala.Double): scala.Double = {
    // http://en.wikipedia.org/wiki/Hypot#Implementation
    if (abs(a) == scala.Double.PositiveInfinity ||
        abs(b) == scala.Double.PositiveInfinity)
      scala.Double.PositiveInfinity
    else if (a.isNaN || b.isNaN)
      scala.Double.NaN
    else if (a == 0 && b == 0)
      0.0
    else {
      //To Avoid Overflow and UnderFlow
      // calculate |x| * sqrt(1 - (y/x)^2) instead of sqrt(x^2 + y^2)
      val x = abs(a)
      val y = abs(b)
      val m = max(x, y)
      val t = min(x, y) / m
      m * sqrt(1 + t * t)
    }
  }

  def expm1(a: scala.Double): scala.Double = {
    // https://github.com/ghewgill/picomath/blob/master/javascript/expm1.js
    if (a == 0 || a.isNaN)
      a
    // Power Series http://en.wikipedia.org/wiki/Power_series
    // for small values of a, exp(a) = 1 + a + (a*a)/2
    else if (abs(a) < 1E-5)
      a + 0.5 * a * a
    else
      exp(a) - 1.0
  }

  def sinh(a: scala.Double): scala.Double = {
    if (a.isNaN || a == 0.0 || abs(a) == scala.Double.PositiveInfinity)
      a
    else
      (exp(a) - exp(-a)) / 2.0
  }

  def cosh(a: scala.Double): scala.Double = {
    if (a.isNaN)
      a
    else if (a == 0.0)
      1.0
    else if (abs(a) == scala.Double.PositiveInfinity)
      scala.Double.PositiveInfinity
    else
      (exp(a) + exp(-a)) / 2.0
  }

  def tanh(a: scala.Double): scala.Double = {
    if (a.isNaN || a == 0.0)
      a
    else if (abs(a) == scala.Double.PositiveInfinity)
      signum(a)
    else {
      // sinh(a) / cosh(a) =
      // 1 - 2 * (exp(-a)/ (exp(-a) + exp (a)))
      val expma = exp(-a)
      if (expma == scala.Double.PositiveInfinity) //Infinity / Infinity
        -1.0
      else {
        val expa = exp(a)
        val ret  = expma / (expa + expma)
        1.0 - (2.0 * ret)
      }
    }
  }

  def addExact(a: scala.Int, b: scala.Int): scala.Int = {
    val res       = a + b
    val resSgnBit = res < 0
    if (resSgnBit == (a < 0) || resSgnBit == (b < 0)) res
    else throw new ArithmeticException("Integer overflow")
  }

  def addExact(a: scala.Long, b: scala.Long): scala.Long = {
    val res       = a + b
    val resSgnBit = res < 0
    if (resSgnBit == (a < 0) || resSgnBit == (b < 0)) res
    else throw new ArithmeticException("Long overflow")
  }

  def subtractExact(a: scala.Int, b: scala.Int): scala.Int = {
    val res       = a - b
    val resSgnBit = res < 0
    if (resSgnBit == (a < 0) || resSgnBit == (b > 0)) res
    else throw new ArithmeticException("Integer overflow")
  }

  def subtractExact(a: scala.Long, b: scala.Long): scala.Long = {
    val res       = a - b
    val resSgnBit = res < 0
    if (resSgnBit == (a < 0) || resSgnBit == (b > 0)) res
    else throw new ArithmeticException("Long overflow")
  }

  def multiplyExact(a: scala.Int, b: scala.Int): scala.Int = {
    val overflow = {
      if (b > 0)
        a > Integer.MAX_VALUE / b || a < Integer.MIN_VALUE / b
      else if (b < -1)
        a > Integer.MIN_VALUE / b || a < Integer.MAX_VALUE / b
      else if (b == -1)
        a == Integer.MIN_VALUE
      else
        false
    }
    if (!overflow) a * b
    else throw new ArithmeticException("Integer overflow")
  }

  def multiplyExact(a: scala.Long, b: scala.Long): scala.Long = {
    val overflow = {
      if (b > 0)
        a > Long.MAX_VALUE / b || a < Long.MIN_VALUE / b
      else if (b < -1)
        a > Long.MIN_VALUE / b || a < Long.MAX_VALUE / b
      else if (b == -1)
        a == Long.MIN_VALUE
      else
        false
    }
    if (!overflow) a * b
    else throw new ArithmeticException("Long overflow")
  }

  def incrementExact(a: scala.Int): scala.Int =
    if (a != Integer.MAX_VALUE) a + 1
    else throw new ArithmeticException("Integer overflow")

  def incrementExact(a: scala.Long): scala.Long =
    if (a != Long.MAX_VALUE) a + 1
    else throw new ArithmeticException("Long overflow")

  def decrementExact(a: scala.Int): scala.Int =
    if (a != Integer.MIN_VALUE) a - 1
    else throw new ArithmeticException("Integer overflow")

  def decrementExact(a: scala.Long): scala.Long =
    if (a != Long.MIN_VALUE) a - 1
    else throw new ArithmeticException("Long overflow")

  def negateExact(a: scala.Int): scala.Int =
    if (a != Integer.MIN_VALUE) -a
    else throw new ArithmeticException("Integer overflow")

  def negateExact(a: scala.Long): scala.Long =
    if (a != Long.MIN_VALUE) -a
    else throw new ArithmeticException("Long overflow")

  def toIntExact(a: scala.Long): scala.Int =
    if (a >= Integer.MIN_VALUE && a <= Integer.MAX_VALUE) a.toInt
    else throw new ArithmeticException("Integer overflow")

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

  def copySign(magnitude: scala.Float, sign: scala.Float): scala.Float =
    `llvm.copysign.f32`(magnitude, sign)

  def copySign(magnitude: scala.Double, sign: scala.Double): scala.Double =
    `llvm.copysign.f64`(magnitude, sign)

  // TODO
  // def IEEEremainder(f1: scala.Double, f2: scala.Double): Double
  // def ulp(a: scala.Float): scala.Float
  // def getExponent(a: scala.Float): scala.Int
  // def getExponent(a: scala.Double): scala.Int
  // def nextAfter(a: scala.Float, b: scala.Double): scala.Float
  // def nextUp(a: scala.Float): scala.Float
  // def nextDown(a: scala.Double): scala.Double
  // def nextDown(a: scala.Float): scala.Float
  // def scalb(a: scala.Double, scalaFactor: scala.Int): scala.Double
  // def scalb(a: scala.Float, scalaFactor: scala.Int): scala.Float
}
