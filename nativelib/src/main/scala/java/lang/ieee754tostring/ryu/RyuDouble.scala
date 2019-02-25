// Copyright 2018 Ulf Adams
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Credits:
//
//    1) This work is a heavily modified derivation of the original work by
//       Ulf Adams at URL: https://github.com/ulfjack/ryu.
//       As such, it inherits the Apache license of the original work.
//       Thank you Ulf Adams.
//
//    2) The original java sources were converted to a rough draft of
//       scala code using the service at URL: javatoscala.com.
//
//       The raw conversion did not compile and contained bugs due
//       to the handling of break and return statements, but it saved
//       days, if not weeks, of effort.
//
//       Thank you javatoscala.com.
//
//    3) All additional work, including introduced bugs,  is an original
//       contribution to Scala Native development.

package java.lang.ieee754tostring.ryu

import java.math.BigInteger

import RyuRoundingMode._

object RyuDouble {

  var DEBUG: Boolean = false

  private val DOUBLE_MANTISSA_BITS: Int = 52

  private val DOUBLE_MANTISSA_MASK: Long = (1L << DOUBLE_MANTISSA_BITS) - 1

  private val DOUBLE_EXPONENT_BITS: Int = 11

  private val DOUBLE_EXPONENT_MASK: Int = (1 << DOUBLE_EXPONENT_BITS) - 1

  private val DOUBLE_EXPONENT_BIAS: Int = (1 << (DOUBLE_EXPONENT_BITS - 1)) - 1

  private val LOG10_2_DENOMINATOR: Long = 10000000L

  private val LOG10_2_NUMERATOR: Long =
    (LOG10_2_DENOMINATOR * Math.log10(2)).toLong

  private val LOG10_5_DENOMINATOR: Long = 10000000L

  private val LOG10_5_NUMERATOR: Long =
    (LOG10_5_DENOMINATOR * Math.log10(5)).toLong

  private val LOG2_5_DENOMINATOR: Long = 10000000L

  private val LOG2_5_NUMERATOR: Long =
    (LOG2_5_DENOMINATOR * (Math.log(5) / Math.log(2))).toLong

  private val POS_TABLE_SIZE: Int = 326

  private val NEG_TABLE_SIZE: Int = 291

  // Only for debugging.
  private val POW5: Array[BigInteger] = new Array[BigInteger](POS_TABLE_SIZE)

  private val POW5_INV: Array[BigInteger] =
    new Array[BigInteger](NEG_TABLE_SIZE)

  private val POW5_BITCOUNT: Int = 121 // max 3*31 = 124

  private val POW5_QUARTER_BITCOUNT: Int = 31

  private val POW5_SPLIT: Array[Array[Int]] =
    Array.ofDim[Int](POS_TABLE_SIZE, 4)

  private val POW5_INV_BITCOUNT: Int = 122 // max 3*31 = 124

  private val POW5_INV_QUARTER_BITCOUNT: Int = 31

  private val POW5_INV_SPLIT: Array[Array[Int]] =
    Array.ofDim[Int](NEG_TABLE_SIZE, 4)

  val mask: BigInteger = BigInteger
    .valueOf(1)
    .shiftLeft(POW5_QUARTER_BITCOUNT)
    .subtract(BigInteger.ONE)

  val invMask: BigInteger = BigInteger
    .valueOf(1)
    .shiftLeft(POW5_INV_QUARTER_BITCOUNT)
    .subtract(BigInteger.ONE)

  for (i <- 0 until Math.max(POW5.length, POW5_INV.length)) {
    val pow: BigInteger       = BigInteger.valueOf(5).pow(i)
    val pow5len: Int          = pow.bitLength()
    val expectedPow5Bits: Int = pow5bits(i)
    if (expectedPow5Bits != pow5len) {
      throw new IllegalStateException(pow5len + " != " + expectedPow5Bits)
    }
    if (i < POW5.length) {
      POW5(i) = pow
    }
    if (i < POW5_SPLIT.length) {
      for (j <- 0.until(4)) {
        POW5_SPLIT(i)(j) = pow
          .shiftRight(
            pow5len - POW5_BITCOUNT + (3 - j)
              * POW5_QUARTER_BITCOUNT)
          .and(mask)
          .intValue()
      }
    }
    if (i < POW5_INV_SPLIT.length) {
      // We want floor(log_2 5^q) here, which is pow5len - 1.
      val j: Int = pow5len - 1 + POW5_INV_BITCOUNT
      val inv: BigInteger =
        BigInteger.ONE.shiftLeft(j).divide(pow).add(BigInteger.ONE)
      POW5_INV(i) = inv
      for (k <- 0.until(4)) {
        POW5_INV_SPLIT(i)(k) =
          if (k == 0)
            inv.shiftRight((3 - k) * POW5_INV_QUARTER_BITCOUNT).intValue()
          else
            inv
              .shiftRight((3 - k) * POW5_INV_QUARTER_BITCOUNT)
              .and(invMask)
              .intValue()
      }
    }
  }

  def doubleToString(value: Double): String =
    doubleToString(value, RyuRoundingMode.ROUND_EVEN)

  def doubleToString(value: Double, roundingMode: RyuRoundingMode): String = {
    // Step 1: Decode the floating point number, and unify normalized and
    // subnormal cases.
    // First, handle all the trivial cases.
    if (java.lang.Double.isNaN(value)) return "NaN"
    if (value == java.lang.Double.POSITIVE_INFINITY) return "Infinity"
    if (value == java.lang.Double.NEGATIVE_INFINITY) return "-Infinity"
    val bits: Long = java.lang.Double.doubleToLongBits(value)
    if (bits == 0) return "0.0"
    if (bits == 0x8000000000000000L) return "-0.0"

    // Otherwise extract the mantissa and exponent bits and run the full
    // algorithm.
    val ieeeExponent: Int =
      ((bits >>> DOUBLE_MANTISSA_BITS) & DOUBLE_EXPONENT_MASK).toInt
    val ieeeMantissa: Long = bits & DOUBLE_MANTISSA_MASK
    var e2: Int            = 0
    var m2: Long           = 0l
    if (ieeeExponent == 0) {
      // Denormal number - no implicit leading 1, and the exponent is 1, not 0.
      e2 = 1 - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS
      m2 = ieeeMantissa
    } else {
      // Add implicit leading 1.
      e2 = ieeeExponent - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS
      m2 = ieeeMantissa | (1L << DOUBLE_MANTISSA_BITS)
    }
    val sign: Boolean = bits < 0
    if (DEBUG) {
      println("IN=" + java.lang.Long.toBinaryString(bits))
      println(
        "   S=" + (if (sign) "-" else "+") + " E=" + e2 + " M=" +
          m2)
    }
    // Step 2: Determine the interval of legal decimal representations.
    val even: Boolean = (m2 & 1) == 0
    val mv: Long      = 4 * m2
    val mp: Long      = 4 * m2 + 2
    val mmShift: Int =
      if (((m2 != (1L << DOUBLE_MANTISSA_BITS)) || (ieeeExponent <= 1))) 1
      else 0
    val mm: Long = 4 * m2 - 1 - mmShift
    e2 -= 2
    if (DEBUG) {
      var sv: String = null
      var sp: String = null
      var sm: String = null
      var e10: Int   = 0
      if (e2 >= 0) {
        sv = BigInteger.valueOf(mv).shiftLeft(e2).toString
        sp = BigInteger.valueOf(mp).shiftLeft(e2).toString
        sm = BigInteger.valueOf(mm).shiftLeft(e2).toString
        e10 = 0
      } else {
        val factor: BigInteger = BigInteger.valueOf(5).pow(-e2)
        sv = BigInteger.valueOf(mv).multiply(factor).toString
        sp = BigInteger.valueOf(mp).multiply(factor).toString
        sm = BigInteger.valueOf(mm).multiply(factor).toString
        e10 = e2
      }
      e10 += sp.length - 1
      println("E =" + e10)
      println("d+=" + sp)
      println("d =" + sv)
      println("d-=" + sm)
      println("e2=" + e2)
    }
    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    // -1077 = 1 - 1023 - 53 - 2 <= e_2 - 2 <= 2046 - 1023 - 53 - 2 = 968
    var dv: Long                   = 0l
    var dp: Long                   = 0l
    var dm: Long                   = 0l
    var e10: Int                   = 0
    var dmIsTrailingZeros: Boolean = false
    var dvIsTrailingZeros: Boolean = false
    if (e2 >= 0) {
      val q: Int = Math.max(
        0,
        (e2 * LOG10_2_NUMERATOR / LOG10_2_DENOMINATOR).toInt -
          1)
      // k = constant + floor(log_2(5^q))
      val k: Int = POW5_INV_BITCOUNT + pow5bits(q) - 1
      val i: Int = -e2 + q + k
      dv = mulPow5InvDivPow2(mv, q, i)
      dp = mulPow5InvDivPow2(mp, q, i)
      dm = mulPow5InvDivPow2(mm, q, i)
      e10 = q
      if (DEBUG) {
        println(mv + " * 2^" + e2)
        println("V+=" + dp)
        println("V =" + dv)
        println("V-=" + dm)
      }
      if (DEBUG) {
        val exact: Long = POW5_INV(q)
          .multiply(BigInteger.valueOf(mv))
          .shiftRight(-e2 + q + k)
          .longValue()
        println(exact + " " + POW5_INV(q).bitCount())
        if (dv != exact) {
          throw new IllegalStateException()
        }
      }
      if (q <= 21) {
        if (mv % 5 == 0) {
          dvIsTrailingZeros = multipleOfPowerOf5(mv, q)
        } else if (roundingMode.acceptUpperBound(even)) {
          dmIsTrailingZeros = multipleOfPowerOf5(mm, q)
        } else if (multipleOfPowerOf5(mp, q)) {
          dp -= 1
        }
      }
    } else {
      val q: Int = Math.max(
        0,
        (-e2 * LOG10_5_NUMERATOR / LOG10_5_DENOMINATOR).toInt -
          1)
      val i: Int = -e2 - q
      val k: Int = pow5bits(i) - POW5_BITCOUNT
      val j: Int = q - k
      dv = mulPow5divPow2(mv, i, j)
      dp = mulPow5divPow2(mp, i, j)
      dm = mulPow5divPow2(mm, i, j)
      e10 = q + e2
      if (DEBUG) {
        println(mv + " * 5^" + (-e2) + " / 10^" + q)
      }
      if (q <= 1) {
        dvIsTrailingZeros = true
        if (roundingMode.acceptUpperBound(even)) {
          dmIsTrailingZeros = mmShift == 1
        } else {
          dp -= 1
        }
      } else if (q < 63) {
        dvIsTrailingZeros = (mv & ((1L << (q - 1)) - 1)) == 0
      }
    }
    if (DEBUG) {
      println("d+=" + dp)
      println("d =" + dv)
      println("d-=" + dm)
      println("e10=" + e10)
      println("d-10=" + dmIsTrailingZeros)
      println("d   =" + dvIsTrailingZeros)
      println("Accept upper=" + roundingMode.acceptUpperBound(even))
      println("Accept lower=" + roundingMode.acceptLowerBound(even))
    }

    // Step 4: Find the shortest decimal representation in the interval of
    // legal representations.
    //
    // We do some extra work here in order to follow Float/Double.toString
    // semantics. In particular, that requires printing in scientific format
    // if and only if the exponent is between -3 and 7, and it requires
    // printing at least two decimal digits.
    //
    // Above, we moved the decimal dot all the way to the right, so now we
    // need to count digits to
    // figure out the correct exponent for scientific notation.
    val vplength: Int = decimalLength(dp)
    var exp: Int      = e10 + vplength - 1
    // Double.toString semantics requires using scientific notation if and
    // only if outside this range.
    val scientificNotation: Boolean = !((exp >= -3) && (exp < 7))
    var removed: Int                = 0
    var lastRemovedDigit: Int       = 0
    var output: Long                = 0l
    if (dmIsTrailingZeros || dvIsTrailingZeros) {
      var done = false // workaround break in .java source
      while ((dp / 10 > dm / 10) && !done) {
        if ((dp < 100) && scientificNotation) {
          // Double.toString semantics requires printing at least two digits.
          done = true
        } else {
          dmIsTrailingZeros &= dm % 10 == 0
          dvIsTrailingZeros &= lastRemovedDigit == 0
          lastRemovedDigit = (dv % 10).toInt
          dp /= 10
          dv /= 10
          dm /= 10; removed += 1
        }
      }
      if (dmIsTrailingZeros && roundingMode.acceptLowerBound(even)) {
        var done = false // workaround break in .java source
        while ((dm % 10 == 0) && !done) {
          if ((dp < 100) && scientificNotation) {
            // Double.toString semantics requires printing at least two digits.
            done = true
          } else {
            dvIsTrailingZeros &= lastRemovedDigit == 0
            lastRemovedDigit = (dv % 10).toInt
            dp /= 10
            dv /= 10
            dm /= 10; removed += 1
          }
        }
      }

      if (dvIsTrailingZeros && (lastRemovedDigit == 5) && (dv % 2 == 0)) {
        // Round even if the exact numbers is .....50..0.
        lastRemovedDigit = 4
      }
      output = dv +
        (if ((dv == dm &&
             !(dmIsTrailingZeros && roundingMode.acceptLowerBound(even))) ||
             (lastRemovedDigit >= 5)) 1
         else 0)
    } else {
      var done = false // workaround break in .java source
      while ((dp / 10 > dm / 10) && !done) {
        if ((dp < 100) && scientificNotation) {
          // Double.toString semantics requires printing at least two digits.
          done = true
        } else {
          lastRemovedDigit = (dv % 10).toInt
          dp /= 10
          dv /= 10
          dm /= 10; removed += 1
        }
      }
      output = dv +
        (if ((dv == dm || (lastRemovedDigit >= 5))) 1 else 0)
    }
    val olength: Int = vplength - removed
    if (DEBUG) {
      println("LAST_REMOVED_DIGIT=" + lastRemovedDigit)
      println("VP=" + dp)
      println("VR=" + dv)
      println("VM=" + dm)
      println("O=" + output)
      println("OLEN=" + olength)
      println("EXP=" + exp)
    }

    // Step 5: Print the decimal representation.
    // We follow Double.toString semantics here.
    val result: Array[Char] = Array.ofDim[Char](24)
    var index: Int          = 0
    if (sign) {
      result({ index += 1; index - 1 }) = '-'
    }

    // Values in the interval [1E-3, 1E7) are special.
    if (scientificNotation) {
      for (i <- 0 until olength - 1) {
        val c: Int = (output % 10).toInt
        output /= 10
        result(index + olength - i) = ('0' + c).toChar
      }
      result(index) = ('0' + output % 10).toChar
      result(index + 1) = '.'
      index += olength + 1
      if (olength == 1) {
        result({ index += 1; index - 1 }) = '0'
      }

      // Print 'E', the exponent sign, and the exponent, which has at most
      // three digits.
      result({ index += 1; index - 1 }) = 'E'
      if (exp < 0) {
        result({ index += 1; index - 1 }) = '-'
        exp = -exp
      }
      if (exp >= 100) {
        result({ index += 1; index - 1 }) = ('0' + exp / 100).toChar
        exp %= 100
        result({ index += 1; index - 1 }) = ('0' + exp / 10).toChar
      } else if (exp >= 10) {
        result({ index += 1; index - 1 }) = ('0' + exp / 10).toChar
      }
      result({ index += 1; index - 1 }) = ('0' + exp % 10).toChar
      new String(result, 0, index)
    } else {
      // Otherwise follow the Java spec for values in the interval [1E-3, 1E7).
      if (exp < 0) {
        // Decimal dot is before any of the digits.
        result({ index += 1; index - 1 }) = '0'
        result({ index += 1; index - 1 }) = '.'
        var i: Int = -1
        while (i > exp) {
          result({ index += 1; index - 1 }) = '0'; i -= 1
        }
        val current: Int = index
        for (i <- 0 until olength) {
          result(current + olength - i - 1) = ('0' + output % 10).toChar
          output /= 10; index += 1
        }
      } else if (exp + 1 >= olength) {
        for (i <- 0 until olength) {
          result(index + olength - i - 1) = ('0' + output % 10).toChar
          output /= 10
        }
        index += olength
        for (i <- olength until exp + 1) {
          result({ index += 1; index - 1 }) = '0'
        }
        result({ index += 1; index - 1 }) = '.'
        result({ index += 1; index - 1 }) = '0'
      } else {
        // Decimal dot is somewhere between the digits.
        var current: Int = index + 1
        for (i <- 0 until olength) {
          if (olength - i - 1 == exp) {
            result(current + olength - i - 1) = '.'; current -= 1
          }
          result(current + olength - i - 1) = ('0' + output % 10).toChar
          output /= 10
        }
        index += olength + 1
      }
      new String(result, 0, index)
    }
  }

  private def pow5bits(e: Int): Int =
    if (e == 0) 1
    else
      ((e * LOG2_5_NUMERATOR + LOG2_5_DENOMINATOR - 1)
        / LOG2_5_DENOMINATOR).toInt

  private def decimalLength(v: Long): Int = {
    if (v >= 1000000000000000000L) return 19
    if (v >= 100000000000000000L) return 18
    if (v >= 10000000000000000L) return 17
    if (v >= 1000000000000000L) return 16
    if (v >= 100000000000000L) return 15
    if (v >= 10000000000000L) return 14
    if (v >= 1000000000000L) return 13
    if (v >= 100000000000L) return 12
    if (v >= 10000000000L) return 11
    if (v >= 1000000000L) return 10
    if (v >= 100000000L) return 9
    if (v >= 10000000L) return 8
    if (v >= 1000000L) return 7
    if (v >= 100000L) return 6
    if (v >= 10000L) return 5
    if (v >= 1000L) return 4
    if (v >= 100L) return 3
    if (v >= 10L) return 2
    1
  }

  private def multipleOfPowerOf5(value: Long, q: Int): Boolean =
    pow5Factor(value) >= q

  private def pow5Factor(_value: Long): Int = {
    var value = _value
    // We want to find the largest power of 5 that divides value.
    if ((value % 5) != 0) return 0
    if ((value % 25) != 0) return 1
    if ((value % 125) != 0) return 2
    if ((value % 625) != 0) return 3
    var count: Int = 4
    value /= 625
    while (value > 0) {
      if (value % 5 != 0) {
        return count
      }
      value /= 5; count += 1
    }
    throw new IllegalArgumentException("" + value)
  }

  /**
   * Compute the high digits of
   * m * 5^p / 10^q = m * 5^(p - q) / 2^q = m * 5^i / 2^j, with q chosen
   * such that m * 5^i / 2^j has sufficiently many decimal digits to
   * represent the original floating point number.
   */
  private def mulPow5divPow2(m: Long, i: Int, j: Int): Long = {
    // m has at most 55 bits.
    val mHigh: Long      = m >>> 31
    val mLow: Long       = m & 0x7fffffff // 124
    val bits13: Long     = mHigh * POW5_SPLIT(i)(0) // 93
    val bits03: Long     = mLow * POW5_SPLIT(i)(0) // 93
    val bits12: Long     = mHigh * POW5_SPLIT(i)(1) // 62
    val bits02: Long     = mLow * POW5_SPLIT(i)(1) // 62
    val bits11: Long     = mHigh * POW5_SPLIT(i)(2) // 31
    val bits01: Long     = mLow * POW5_SPLIT(i)(2) // 31
    val bits10: Long     = mHigh * POW5_SPLIT(i)(3) // 0
    val bits00: Long     = mLow * POW5_SPLIT(i)(3)
    val actualShift: Int = j - 3 * 31 - 21
    if (actualShift < 0) {
      throw new IllegalArgumentException("" + actualShift)
    }
    ((((((((bits00 >>> 31) + bits01 + bits10) >>> 31) + bits02 +
      bits11) >>>
      31) +
      bits03 +
      bits12) >>>
      21) +
      (bits13 << 10)) >>>
      actualShift
  }

  /**
   * Compute the high digits of
   * m / 5^i / 2^j such that the result is accurate to at least 9
   * decimal digits. i and j are already chosen appropriately.
   */
  private def mulPow5InvDivPow2(m: Long, i: Int, j: Int): Long = {
    // m has at most 55 bits.
    val mHigh: Long      = m >>> 31
    val mLow: Long       = m & 0x7fffffff
    val bits13: Long     = mHigh * POW5_INV_SPLIT(i)(0)
    val bits03: Long     = mLow * POW5_INV_SPLIT(i)(0)
    val bits12: Long     = mHigh * POW5_INV_SPLIT(i)(1)
    val bits02: Long     = mLow * POW5_INV_SPLIT(i)(1)
    val bits11: Long     = mHigh * POW5_INV_SPLIT(i)(2)
    val bits01: Long     = mLow * POW5_INV_SPLIT(i)(2)
    val bits10: Long     = mHigh * POW5_INV_SPLIT(i)(3)
    val bits00: Long     = mLow * POW5_INV_SPLIT(i)(3)
    val actualShift: Int = j - 3 * 31 - 21
    if (actualShift < 0) {
      throw new IllegalArgumentException("" + actualShift)
    }
    ((((((((bits00 >>> 31) + bits01 + bits10) >>> 31) + bits02 +
      bits11) >>>
      31) +
      bits03 +
      bits12) >>>
      21) +
      (bits13 << 10)) >>>
      actualShift
  }

}
