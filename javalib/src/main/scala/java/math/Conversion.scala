/* Ported from Scala.js commit:  ??? dated: ???
 *   As prior SN code contained the change,
 *   commit was probably 3851c2d dated: 2020-06-18
 *
 * Scala Native changes:
 *
 * 2025-02-07 SN Issue 4176
 *    - Removed dead code method 'bigInteger2Double(BigInteger): Double'
 *      This aligns the start of SN Issue 4176 changes to:
 *        Scala.js commit: f3765f9  dated: 2022-09-06
 *
 *    - Modified code to use java.lang.StringBuilder rather than
 *      Scala String concatenation (string_1 + string_2). Former tends
 *      to be less expensive on Scala Native.
 *
 *      The original Google .java code used StringBuilder. The port to Scala.js
 *      introduced the concatenation.
 *
 *      Design Note:
 *
 *      Changing to use StringBuilder yields a  noticeable runtime improvement
 *      even though the algorithm from the Scala.js code does not favor
 *      StringBuilder. Changing this file to use a better algorithm from
 *      the literature is left an exercise for the Gentle Reader.
 *
 *      The Scala.js algorithm from the Scala.js code likes to insert
 *      characters at the beginning of strings. The idiom is widespread.
 *
 *      In order to do an insert(), StringBuilder internals must move all
 *      existing characters to the right, which is much more expensive than
 *      an append().
 *
 *      The Google .java code appears to have used an Array to build up
 *      the String in a more efficient manner.
 *
 *      A characteristic usage pessimistic case is where the digits of
 *      the resultant String are converted. The Scala.CSS, and current
 *      Scala Native, algorithm starts with the low order digits.
 *      Higher order digits are inserted _before_ the digits converted to
 *      date.
 *
 */

/* TL;DR -- This URL is no longer working. See discussion at top
 * of BigDecimal.scala.
 */
/*
 * Ported by Alistair Johnson from
 * https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/java/math/Conversion.java
 * Original license copied below:
 */

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.math

import java.{lang => jl}
import jl.Character

import scala.annotation.tailrec

/** Provides {@link BigInteger} base conversions.
 *
 *  Static library that provides {@link BigInteger} base conversion from/to any
 *  integer represented in a {@link java.lang.String} Object.
 */
private[math] object Conversion {

  /** Holds the maximal exponent for each radix.
   *
   *  Holds the maximal exponent for each radix, so that
   *  radix<sup>digitFitInInt[radix]</sup> fit in an {@code int} (32 bits).
   */
  final val DigitFitInInt =
    Array[Int](-1, -1, 31, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7,
      7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5)

  /** Precomputed maximal powers of radices.
   *
   *  BigRadices values are precomputed maximal powers of radices (integer
   *  numbers from 2 to 36) that fit into unsigned int (32 bits).
   *
   *  bigRadices[0] = 2 ^ 31, bigRadices[8] = 10 ^ 9, etc.
   */
  final val BigRadices = Array[Int](-2147483648, 1162261467, 1073741824,
    1220703125, 362797056, 1977326743, 1073741824, 387420489, 1000000000,
    214358881, 429981696, 815730721, 1475789056, 170859375, 268435456,
    410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904,
    148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321,
    729000000, 887503681, 1073741824, 1291467969, 1544804416, 1838265625,
    60466176)

  private def stripLeadingZeros(sb: jl.StringBuilder): jl.StringBuilder = {
    val leadingZeroCount = sb.chars().takeWhile(_ == '0').count()
    if (leadingZeroCount > 0L)
      sb.delete(0, leadingZeroCount.toInt)
    sb
  }

  /** @see BigInteger#toString(int) */
  def bigInteger2String(bi: BigInteger, radix: Int): String = {
    val sign = bi.sign
    val numberLength = bi.numberLength
    val digits = bi.digits
    val radixOutOfBounds =
      radix < Character.MIN_RADIX || radix > Character.MAX_RADIX

    if (sign == 0) {
      "0"
    } else if (numberLength == 1) {
      val highDigit = digits(numberLength - 1)
      var v = highDigit & 0xffffffffL
      if (sign < 0)
        v = -v
      java.lang.Long.toString(v, radix)
    } else if (radix == 10 || radixOutOfBounds) {
      bi.toString
    } else {
      var bitsForRadixDigit: Double = 0.0
      bitsForRadixDigit = Math.log(radix) / Math.log(2)
      val addForSign = if (sign < 0) 1 else 0
      val biAbsLen = bi.abs().bitLength()
      val resLenInChars = (biAbsLen / bitsForRadixDigit + addForSign).toInt + 1

      val result = new jl.StringBuilder(128) // a generous, growable guess

      var currentChar = resLenInChars
      var resDigit: Int = 0

      if (radix != 16) {
        val temp = new Array[Int](numberLength)
        System.arraycopy(digits, 0, temp, 0, numberLength)
        var tempLen = numberLength
        val charsPerInt = DigitFitInInt(radix)
        val bigRadix = BigRadices(radix - 2)

        @inline
        @tailrec
        def loop(): Unit = {
          resDigit = Division.divideArrayByInt(temp, temp, tempLen, bigRadix)
          val previous = currentChar

          @inline
          @tailrec
          def innerLoop(): Unit = {
            currentChar -= 1
            result.insert(0, jl.Character.forDigit(resDigit % radix, radix))
            resDigit /= radix
            if (resDigit != 0 && currentChar != 0)
              innerLoop()
          }
          innerLoop()

          val delta = charsPerInt - previous + currentChar
          var i: Int = 0
          while (i < delta && currentChar > 0) {
            currentChar -= 1
            result.insert(0, '0')
            i += 1
          }
          i = tempLen - 1
          while (i > 0 && temp(i) == 0) {
            i -= 1
          }
          tempLen = i + 1
          if (!(tempLen == 1 && temp(0) == 0))
            loop()
        }

        loop()
      } else {
        for (i <- 0 until numberLength) {
          var j = 0
          while (j < 8 && currentChar > 0) {
            resDigit = digits(i) >> (j << 2) & 0xf
            currentChar -= 1
            result.insert(0, jl.Character.forDigit(resDigit, 16))
            j += 1
          }
        }
      }

      if (result.charAt(0) == '0')
        stripLeadingZeros(result) // 1 found, so pay cost of counting rest.

      if (sign < 0)
        result.insert(0, '-')

      result.toString()
    }
  }

  /** The string representation scaled by zero.
   *
   *  Builds the correspondent {@code String} representation of {@code val}
   *  being scaled by 0.
   *
   *  @see
   *    BigInteger#toString()
   *  @see
   *    BigDecimal#toString()
   */
  def toDecimalScaledString(bi: BigInteger): String = {
    val sign: Int = bi.sign
    val numberLength: Int = bi.numberLength
    val digits: Array[Int] = bi.digits
    var resLengthInChars: Int = 0
    var currentChar: Int = 0

    if (sign == 0) {
      "0"
    } else {
      // one 32-bit unsigned value may contain 10 decimal digits
      // Explanation why +1+7:
      // +1 - one char for sign if needed.
      // +7 - For "special case 2" (see below) we have 7 free chars for inserting necessary scaled digits.
      resLengthInChars = numberLength * 10 + 1 + 7
      val result = new jl.StringBuilder(resLengthInChars)

      // a free latest character may be used for "special case 1" (see below)
      currentChar = resLengthInChars
      if (numberLength == 1) {
        val highDigit = digits(0)
        if (highDigit < 0) {
          var v: Long = highDigit & 0xffffffffL
          while ({
            val prev = v
            v /= 10
            currentChar -= 1
            result.insert(0, (prev - v * 10))
            v != 0
          }) ()
        } else {
          var v: Int = highDigit
          while ({
            val prev = v
            v /= 10
            currentChar -= 1
            result.insert(0, (prev - v * 10))
            v != 0
          }) ()
        }
      } else {
        val temp = new Array[Int](numberLength)
        var tempLen = numberLength
        System.arraycopy(digits, 0, temp, 0, tempLen)

        @inline
        @tailrec
        def loop(): Unit = {
          // divide the array of digits by bigRadix and convert
          // remainders
          // to characters collecting them in the char array
          var result11: Long = 0
          var i1: Int = tempLen - 1
          while (i1 >= 0) {
            val temp1: Long = (result11 << 32) + (temp(i1) & 0xffffffffL)
            val res: Long = divideLongByBillion(temp1)
            temp(i1) = res.toInt
            result11 = (res >> 32).toInt
            i1 -= 1
          }
          var resDigit = result11.toInt
          val previous = currentChar
          @inline
          @tailrec
          def innerLoop(): Unit = {
            currentChar -= 1
            result.insert(0, (resDigit % 10))
            resDigit /= 10
            if (resDigit != 0 && currentChar != 0)
              innerLoop()
          }

          innerLoop()

          val delta = 9 - previous + currentChar
          var i = 0
          while ((i < delta) && (currentChar > 0)) {
            currentChar -= 1
            result.insert(0, '0')
            i += 1
          }
          var j = tempLen - 1
          while ((temp(j) == 0) && (j != 0)) {
            j -= 1
          }
          tempLen = j + 1
          if (!(j == 0 && (temp(j) == 0))) loop()
        }

        loop()

        if (result.charAt(0) == '0')
          stripLeadingZeros(result) // 1 found, so pay cost of counting rest.
      }

      if (sign < 0)
        result.insert(0, '-')

      result.toString()
    }
  }

  /* can process only 32-bit numbers */
  def toDecimalScaledString(value: Long, scale: Int): String = {
    if (value == 0) {
      scale match {
        case 0 => "0"
        case 1 => "0.0"
        case 2 => "0.00"
        case 3 => "0.000"
        case 4 => "0.0000"
        case 5 => "0.00000"
        case 6 => "0.000000"
        case _ =>
          val scaleVal =
            if (scale == Int.MinValue) "2147483648"
            else java.lang.Integer.toString(-scale)

          val result = new jl.StringBuilder(128) // a generous, growable guess

          result.append("0E")
          if (scale < 0)
            result.append('+')
          result.append(scaleVal)
          result.toString()
      }
    } else {
      // one 32-bit unsigned value may contain 10 decimal digits
      // Explanation why 10+1+7:
      // +1 - one char for sign if needed.
      // +7 - For "special case 2" (see below) we have 7 free chars for inserting necessary scaled digits.
      val resLengthInChars = 18
      val negNumber = value < 0

      val result = new jl.StringBuilder(128) // a generous, growable guess

      //  Allocated [resLengthInChars+1] characters.
      // a free latest character may be used for "special case 1" (see below)
      var currentChar = resLengthInChars

      var v: Long = if (negNumber) -value else value
      while ({
        val prev = v
        v /= 10
        currentChar -= 1
        result.insert(0, (prev - v * 10))
        v != 0
      }) ()

      val exponent: Long = resLengthInChars - currentChar - scale.toLong - 1

      if (scale > 0 && exponent >= -6L) {
        val index = exponent.toInt + 1
        if (index > 0) {
          // special case 1
          result.insert(index, '.')
        } else {
          // special case 2
          for (j <- 0 until -index) {
            result.insert(0, '0')
          }
          result.insert(0, "0.")
        }
      } else if (scale != 0) {
        if (resLengthInChars - currentChar > 1)
          result.insert(1, '.')

        val exponentPrefix = if (exponent > 0) "E+" else "E"

        result.append(exponentPrefix).append(exponent)
      }

      if (negNumber)
        result.insert(0, '-')

      result.toString()
    }
  }

  def divideLongByBillion(a: Long): Long = {
    val (quot, rem) = if (a >= 0) {
      val bLong = 1000000000L
      (a / bLong, a % bLong)
    } else {
      /*
       * Make the dividend positive shifting it right by 1 bit then get
       * the quotient and remainder and correct them properly
       */
      val aPos: Long = a >>> 1
      val bPos: Long = 1000000000L >>> 1
      (aPos / bPos, (aPos % bPos << 1) + (a & 1))
    }
    (rem << 32) | (quot & 0xffffffffL)
  }

}
