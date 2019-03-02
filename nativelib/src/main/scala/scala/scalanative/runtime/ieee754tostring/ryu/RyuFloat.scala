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

package scala.scalanative
package runtime
package ieee754tostring.ryu

import java.math.BigInteger

import RyuRoundingMode._

object RyuFloat {

  var DEBUG: Boolean = false

  private val FLOAT_MANTISSA_BITS: Int = 23

  private val FLOAT_MANTISSA_MASK: Int = (1 << FLOAT_MANTISSA_BITS) - 1

  private val FLOAT_EXPONENT_BITS: Int = 8

  private val FLOAT_EXPONENT_MASK: Int = (1 << FLOAT_EXPONENT_BITS) - 1

  private val FLOAT_EXPONENT_BIAS: Int = (1 << (FLOAT_EXPONENT_BITS - 1)) - 1

  private val LOG10_2_DENOMINATOR: Long = 10000000L

  private val LOG10_2_NUMERATOR: Long =
    (LOG10_2_DENOMINATOR * Math.log10(2)).toLong

  private val LOG10_5_DENOMINATOR: Long = 10000000L

  private val LOG10_5_NUMERATOR: Long =
    (LOG10_5_DENOMINATOR * Math.log10(5)).toLong

  private val LOG2_5_DENOMINATOR: Long = 10000000L

  private val LOG2_5_NUMERATOR: Long =
    (LOG2_5_DENOMINATOR * (Math.log(5) / Math.log(2))).toLong

  private val POS_TABLE_SIZE: Int = 47

  private val INV_TABLE_SIZE: Int = 31

  // Only for debugging.
  private val POW5: scala.Array[BigInteger] =
    new scala.Array[BigInteger](POS_TABLE_SIZE)

  private val POW5_INV: scala.Array[BigInteger] =
    new scala.Array[BigInteger](INV_TABLE_SIZE)

  private val POW5_BITCOUNT: Int = 61

  private val POW5_HALF_BITCOUNT: Int = 31

  private val POW5_SPLIT: scala.Array[scala.Array[Int]] =
    scala.Array.ofDim[Int](POS_TABLE_SIZE, 2)

  private val POW5_INV_BITCOUNT: Int = 59

  private val POW5_INV_HALF_BITCOUNT: Int = 31

  private val POW5_INV_SPLIT: scala.Array[scala.Array[Int]] =
    scala.Array.ofDim[Int](INV_TABLE_SIZE, 2)

  val mask: BigInteger = BigInteger
    .valueOf(1)
    .shiftLeft(POW5_HALF_BITCOUNT)
    .subtract(BigInteger.ONE)

  val maskInv: BigInteger = BigInteger
    .valueOf(1)
    .shiftLeft(POW5_INV_HALF_BITCOUNT)
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
      POW5_SPLIT(i)(0) = pow
        .shiftRight(pow5len - POW5_BITCOUNT + POW5_HALF_BITCOUNT)
        .intValue()
      POW5_SPLIT(i)(1) =
        pow.shiftRight(pow5len - POW5_BITCOUNT).and(mask).intValue()
    }
    if (i < POW5_INV.length) {
      val j: Int = pow5len - 1 + POW5_INV_BITCOUNT
      val inv: BigInteger =
        BigInteger.ONE.shiftLeft(j).divide(pow).add(BigInteger.ONE)
      POW5_INV(i) = inv
      POW5_INV_SPLIT(i)(0) = inv.shiftRight(POW5_INV_HALF_BITCOUNT).intValue()
      POW5_INV_SPLIT(i)(1) = inv.and(maskInv).intValue()
    }
  }

  def floatToString(value: Float, roundingMode: RyuRoundingMode): String = {
    // Step 1: Decode the floating point number, and unify normalized and
    // subnormal cases.
    // First, handle all the trivial cases.
    if (java.lang.Float.isNaN(value)) return "NaN"
    if (value == java.lang.Float.POSITIVE_INFINITY) return "Infinity"
    if (value == java.lang.Float.NEGATIVE_INFINITY) return "-Infinity"
    val bits: Int = java.lang.Float.floatToIntBits(value)
    if (bits == 0) return "0.0"
    if (bits == 0x80000000) return "-0.0"
    // Otherwise extract the mantissa and exponent bits and run the full
    // algorithm.
    val ieeeExponent: Int = (bits >> FLOAT_MANTISSA_BITS) & FLOAT_EXPONENT_MASK
    val ieeeMantissa: Int = bits & FLOAT_MANTISSA_MASK
    // By default, the correct mantissa starts with a 1, except for
    // denormal numbers.
    var e2: Int = 0
    var m2: Int = 0
    if (ieeeExponent == 0) {
      e2 = 1 - FLOAT_EXPONENT_BIAS - FLOAT_MANTISSA_BITS
      m2 = ieeeMantissa
    } else {
      e2 = ieeeExponent - FLOAT_EXPONENT_BIAS - FLOAT_MANTISSA_BITS
      m2 = ieeeMantissa | (1 << FLOAT_MANTISSA_BITS)
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
    val mv: Int       = 4 * m2
    val mp: Int       = 4 * m2 + 2
    val mm: Int = 4 * m2 -
      (if ((m2 != (1L << FLOAT_MANTISSA_BITS)) || (ieeeExponent <= 1)) 2
       else 1)
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
      println("Exact values")
      println("  m =" + mv)
      println("  E =" + e10)
      println("  d+=" + sp)
      println("  d =" + sv)
      println("  d-=" + sm)
      println("  e2=" + e2)
    }

    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    // -151 = 1 - 127 - 23 - 2 <= e_2 - 2 <= 254 - 127 - 23 - 2 = 102
    var dp: Int                    = 0
    var dv: Int                    = 0
    var dm: Int                    = 0
    var e10: Int                   = 0
    var dpIsTrailingZeros: Boolean = false
    var dvIsTrailingZeros: Boolean = false
    var dmIsTrailingZeros: Boolean = false
    var lastRemovedDigit: Int      = 0
    if (e2 >= 0) {
      // Compute m * 2^e_2 / 10^q = m * 2^(e_2 - q) / 5^q
      val q: Int = (e2 * LOG10_2_NUMERATOR / LOG10_2_DENOMINATOR).toInt
      val k: Int = POW5_INV_BITCOUNT + pow5bits(q) - 1
      val i: Int = -e2 + q + k
      dv = mulPow5InvDivPow2(mv, q, i).toInt
      dp = mulPow5InvDivPow2(mp, q, i).toInt
      dm = mulPow5InvDivPow2(mm, q, i).toInt
      if (q != 0 && ((dp - 1) / 10 <= dm / 10)) {
        // 32-bit arithmetic is faster even on 64-bit machines.
        val l: Int = POW5_INV_BITCOUNT + pow5bits(q - 1) - 1
        lastRemovedDigit =
          (mulPow5InvDivPow2(mv, q - 1, -e2 + q - 1 + l) % 10).toInt
      }
      // We need to know one removed digit even if we are not going to loop
      // below. We could use
      // q = X - 1 above, except that would require 33 bits for the result,
      // and we've found that
      // 32-bit arithmetic is faster even on 64-bit machines
      e10 = q
      if (DEBUG) {
        println(mv + " * 2^" + e2 + " / 10^" + q)
      }
      dpIsTrailingZeros = pow5Factor(mp) >= q
      dvIsTrailingZeros = pow5Factor(mv) >= q
      dmIsTrailingZeros = pow5Factor(mm) >= q
    } else {
      // Compute m * 5^(-e_2) / 10^q = m * 5^(-e_2 - q) / 2^q
      val q: Int = (-e2 * LOG10_5_NUMERATOR / LOG10_5_DENOMINATOR).toInt
      val i: Int = -e2 - q
      val k: Int = pow5bits(i) - POW5_BITCOUNT
      var j: Int = q - k
      dv = mulPow5divPow2(mv, i, j).toInt
      dp = mulPow5divPow2(mp, i, j).toInt
      dm = mulPow5divPow2(mm, i, j).toInt
      if (q != 0 && ((dp - 1) / 10 <= dm / 10)) {
        j = q - 1 - (pow5bits(i + 1) - POW5_BITCOUNT)
        lastRemovedDigit = (mulPow5divPow2(mv, i + 1, j) % 10).toInt
      }
      e10 = q + e2 // Note: e2 and e10 are both negative here.
      if (DEBUG) {
        println(
          mv + " * 5^" + (-e2) + " / 10^" + q + " = " + mv + " * 5^" +
            (-e2 - q) +
            " / 2^" +
            q)
      }
      dpIsTrailingZeros = 1 >= q
      dvIsTrailingZeros = (q < FLOAT_MANTISSA_BITS) && (mv & ((1 << (q - 1)) - 1)) == 0
      dmIsTrailingZeros = (if (mm % 2 == 1) 0 else 1) >= q
    }
    if (DEBUG) {
      println("Actual values")
      println("  d+=" + dp)
      println("  d =" + dv)
      println("  d-=" + dm)
      println("  last removed=" + lastRemovedDigit)
      println("  e10=" + e10)
      println("  d+10=" + dpIsTrailingZeros)
      println("  d   =" + dvIsTrailingZeros)
      println("  d-10=" + dmIsTrailingZeros)
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

    val dplength: Int = decimalLength(dp)
    var exp: Int      = e10 + dplength - 1
    // Float.toString semantics requires using scientific notation if and
    // only if outside this range.
    val scientificNotation: Boolean = !((exp >= -3) && (exp < 7))
    var removed: Int                = 0
    if (dpIsTrailingZeros && !roundingMode.acceptUpperBound(even)) {
      dp -= 1
    }

    var done = false // workaround break in .java source

    while ((dp / 10 > dm / 10) && !done) {
      if ((dp < 100) && scientificNotation) {
        // We print at least two digits, so we might as well stop now.
        done = true
      } else {
        dmIsTrailingZeros &= dm % 10 == 0
        dp /= 10
        lastRemovedDigit = dv % 10
        dv /= 10
        dm /= 10; removed += 1
      }
    }
    if (dmIsTrailingZeros && roundingMode.acceptLowerBound(even)) {
      var done = false // workaround break in .java source
      while ((dm % 10 == 0) && !done) {
        if ((dp < 100) && scientificNotation) {
          // We print at least two digits, so we might as well stop now.
          done = true
        } else {
          dp /= 10
          lastRemovedDigit = dv % 10
          dv /= 10
          dm /= 10; removed += 1
        }
      }
    }
    if (dvIsTrailingZeros && (lastRemovedDigit == 5) && (dv % 2 == 0)) {
      // Round down not up if the number ends in X50000 and the number is even.
      lastRemovedDigit = 4
    }
    var output: Int = dv +
      (if ((dv == dm &&
           !(dmIsTrailingZeros && roundingMode.acceptLowerBound(even))) ||
           (lastRemovedDigit >= 5)) 1
       else 0)
    val olength: Int = dplength - removed
    if (DEBUG) {
      println("Actual values after loop")
      println("  d+=" + dp)
      println("  d =" + dv)
      println("  d-=" + dm)
      println("  last removed=" + lastRemovedDigit)
      println("  e10=" + e10)
      println("  d+10=" + dpIsTrailingZeros)
      println("  d-10=" + dmIsTrailingZeros)
      println("  output=" + output)
      println("  output_length=" + olength)
      println("  output_exponent=" + exp)
    }

    // Step 5: Print the decimal representation.
    // We follow Float.toString semantics here.

    val result: scala.Array[Char] = scala.Array.ofDim[Char](15)
    var index: Int                = 0
    if (sign) {
      result({ index += 1; index - 1 }) = '-'
    }
    if (scientificNotation) {
      for (i <- 0 until olength - 1) {
        val c: Int = output % 10
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
      // two digits.
      result({ index += 1; index - 1 }) = 'E'
      if (exp < 0) {
        result({ index += 1; index - 1 }) = '-'
        exp = -exp
      }
      if (exp >= 10) {
        result({ index += 1; index - 1 }) = ('0' + exp / 10).toChar
      }
      result({ index += 1; index - 1 }) = ('0' + exp % 10).toChar
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
            result(current + olength - i - 1) = '.';
            {
              current -= 1; current + 1
            }
          }
          result(current + olength - i - 1) = ('0' + output % 10).toChar
          output /= 10
        }
        index += olength + 1
      }
    }
    new String(result, 0, index)
  }

  private def pow5bits(e: Int): Int =
    if (e == 0) 1
    else
      ((e * LOG2_5_NUMERATOR + LOG2_5_DENOMINATOR - 1)
        / LOG2_5_DENOMINATOR).toInt

  /**
   * Returns the exponent of the largest power of 5 that divides the given
   * value, i.e., returns i such that value = 5^i * x, where x is an integer.
   */
  private def pow5Factor(_value: Int): Int = {
    var value      = _value
    var count: Int = 0
    while (value > 0) {
      if (value % 5 != 0) {
        return count
      }
      value /= 5; count += 1
    }
    throw new IllegalArgumentException("" + value)
  }

  /**
   * Compute the exact result of:
   *   [m * 5^(-e_2) / 10^q] = [m * 5^(-e_2 - q) / 2^q]
   *   = [m * [5^(p - q)/2^k] / 2^(q - k)] = [m * POW5[i] / 2^j].
   */
  private def mulPow5divPow2(m: Int, i: Int, j: Int): Long = {
    if (j - POW5_HALF_BITCOUNT < 0) {
      throw new IllegalArgumentException()
    }
    val bits0: Long = m * POW5_SPLIT(i)(0).toLong
    val bits1: Long = m * POW5_SPLIT(i)(1).toLong
    (bits0 + (bits1 >> POW5_HALF_BITCOUNT)) >> (j - POW5_HALF_BITCOUNT)
  }

  /**
   * Compute the exact result of:
   *   [m * 2^p / 10^q] = [m * 2^(p - q) / 5 ^ q]
   *   = [m * [2^k / 5^q] / 2^-(p - q - k)] = [m * POW5_INV[q] / 2^j].
   */
  private def mulPow5InvDivPow2(m: Int, q: Int, j: Int): Long = {
    if (j - POW5_INV_HALF_BITCOUNT < 0) {
      throw new IllegalArgumentException()
    }
    val bits0: Long = m * POW5_INV_SPLIT(q)(0).toLong
    val bits1: Long = m * POW5_INV_SPLIT(q)(1).toLong
    (bits0 + (bits1 >> POW5_INV_HALF_BITCOUNT)) >> (j - POW5_INV_HALF_BITCOUNT)
  }

  private def decimalLength(v: Int): Int = {
    var length: Int = 10
    var factor: Int = 1000000000
    var done        = false

    while ((length > 0) && !done) {
      if (v >= factor) {
        done = true
      } else {
        factor /= 10
        length -= 1
      }
    }
    length
  }

}
