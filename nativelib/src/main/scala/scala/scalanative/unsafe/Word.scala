package scala.scalanative
package unsafe

import scala.language.implicitConversions

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag

import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

import scalanative.unsigned._

final class Word(private[scalanative] val rawWord: RawWord) {
  @inline def toByte: Byte   = castRawWordToInt(rawWord).toByte
  @inline def toChar: Char   = castRawWordToInt(rawWord).toChar
  @inline def toShort: Short = castRawWordToInt(rawWord).toShort
  @inline def toInt: Int     = castRawWordToInt(rawWord)
  @inline def toLong: Long   = castRawWordToLong(rawWord)

  @inline def toUByte: UByte   = toByte.toUByte
  @inline def toUShort: UShort = toShort.toUShort
  @inline def toUInt: UInt     = toInt.toUInt
  @inline def toULong: ULong   = toLong.toULong
  @inline def toUWord: UWord   = new UWord(rawWord)

  @inline override def hashCode: Int = java.lang.Long.hashCode(toLong)

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: Word => this.rawWord == that.rawWord
    case _          => false
  }

  @inline override def toString(): String = toLong.toString

  /**
   * Returns the bitwise negation of this value.
   * @example {{{
   * ~5 == 4294967290
   * // in binary: ~00000101 ==
   * //             11111010
   * }}}
   */
  @inline def unary_~ : Word =
    (~toLong).toWord // TODO(shadaj): intrinsify

  /** Returns the negated version of this value. */
  @inline def unary_- : Word = 0 - this // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Int): Word =
    (toLong << x).toWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Long): Word =
    (toLong << x).toWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted right by the specified number of bits,
   *         filling the new left bits with zeroes.
   * @example {{{ 21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010 }}}
   * @example {{{
   * 4294967275 >>> 3 == 536870909
   * // in binary: 11111111 11111111 11111111 11101011 >>> 3 ==
   * //            00011111 11111111 11111111 11111101
   * }}}
   */
  @inline def >>>(x: Int): Word =
    (toLong >>> x).toWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted right by the specified number of bits,
   *         filling the new left bits with zeroes.
   * @example {{{ 21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010 }}}
   * @example {{{
   * 4294967275 >>> 3 == 536870909
   * // in binary: 11111111 11111111 11111111 11101011 >>> 3 ==
   * //            00011111 11111111 11111111 11111101
   * }}}
   */
  @inline def >>>(x: Long): Word =
    (toLong >>> x).toWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Int): Word = (toLong >> x).toWord

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Long): Word = (toLong >> x).toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Byte): Boolean = this == x.toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Short): Boolean = this == x.toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Int): Boolean = this == x.toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Long): Boolean = this.toLong == x

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(other: Word): Boolean =
    this.toLong == other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Byte): Boolean = this != x.toWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Short): Boolean = this != x.toWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Int): Boolean = this != x.toWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Long): Boolean = this.toLong != x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(other: Word): Boolean =
    this.toLong != other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Byte): Boolean = this < x.toWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Short): Boolean = this < x.toWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Int): Boolean = this < x.toWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Long): Boolean = this.toLong < x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(other: Word): Boolean =
    this.toLong < other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Byte): Boolean = this <= x.toWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Short): Boolean = this <= x.toWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Int): Boolean = this <= x.toWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Long): Boolean = this.toLong <= x

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(other: Word): Boolean =
    this.toLong <= other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Byte): Boolean = this > x.toWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Short): Boolean = this > x.toWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Int): Boolean = this > x.toWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Long): Boolean = this.toLong > x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(other: Word): Boolean =
    this.toLong > other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Byte): Boolean = this >= x.toWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Short): Boolean = this >= x.toWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Int): Boolean = this >= x.toWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Long): Boolean = this.toLong >= x

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(other: Word): Boolean =
    this.toLong >= other.toLong // TODO(shadaj): intrinsify

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Byte): Word = this & x.toWord

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Short): Word = this & x.toWord

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Int): Word = this & x.toWord

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Long): Long = this.toLong & x

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(other: Word): Word =
    new Word(andRawWords(rawWord, other.rawWord))

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Byte): Word = this | x.toWord

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Short): Word = this | x.toWord

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Int): Word = this | x.toWord

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Long): Long = this.toLong | x

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(other: Word): Word =
    new Word(orRawWords(rawWord, other.rawWord))

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Byte): Word = this ^ x.toWord

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Short): Word = this ^ x.toWord

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Int): Word = this ^ x.toWord

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Long): Long = this.toLong ^ x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(other: Word): Word =
    new Word(xorRawWords(rawWord, other.rawWord))

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Byte): Word = this + x.toWord

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Short): Word = this + x.toWord

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Int): Word = this + x.toWord

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Long): Long = this.toLong + x

  /** Returns the sum of this value and `x`. */
  @inline def +(other: Word): Word =
    new Word(addRawWords(rawWord, other.rawWord))

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Byte): Word = this - x.toWord

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Short): Word = this - x.toWord

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Int): Word = this - x.toWord

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Long): Long = this.toLong - x

  /** Returns the difference of this value and `x`. */
  @inline def -(other: Word): Word =
    new Word(subRawWords(rawWord, other.rawWord))

  /** Returns the product of this value and `x`. */
  @inline def *(x: Byte): Word = this * x.toWord

  /** Returns the product of this value and `x`. */
  @inline def *(x: Short): Word = this * x.toWord

  /** Returns the product of this value and `x`. */
  @inline def *(x: Int): Word = this * x.toWord

  /** Returns the product of this value and `x`. */
  @inline def *(x: Long): Long = this.toLong * x

  /** Returns the product of this value and `x`. */
  @inline def *(other: Word): Word =
    new Word(multRawWords(rawWord, other.rawWord))

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Byte): Word = this / x.toWord

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Short): Word = this / x.toWord

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Int): Word = this / x.toWord

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Long): Long = this.toLong / x

  /** Returns the quotient of this value and `x`. */
  @inline def /(other: Word): Word =
    new Word(divRawWords(rawWord, other.rawWord))

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Byte): Word = this % x.toWord

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Short): Word = this % x.toWord

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Int): Word = this % x.toWord

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Long): Long = this.toLong % x

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(other: Word): Word =
    new Word(modRawWords(rawWord, other.rawWord))

}

object Word {
  @inline implicit def byteToWord(x: Byte): Word =
    new Word(castIntToRawWord(x))
  @inline implicit def charToWord(x: Char): Word =
    new Word(castIntToRawWord(x))
  @inline implicit def shortToWord(x: Short): Word =
    new Word(castIntToRawWord(x))
  @inline implicit def intToWord(x: Int): Word =
    new Word(castIntToRawWord(x))
}
