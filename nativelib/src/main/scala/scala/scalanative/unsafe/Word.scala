package scala.scalanative
package unsafe

import scala.language.implicitConversions

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag

import scalanative.annotation.alwaysinline

import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

import scalanative.unsigned._

final class Word(private[scalanative] val rawWord: RawWord) {
  @alwaysinline def toByte: Byte   = castRawWordToInt(rawWord).toByte
  @alwaysinline def toChar: Char   = castRawWordToInt(rawWord).toChar
  @alwaysinline def toShort: Short = castRawWordToInt(rawWord).toShort
  @alwaysinline def toInt: Int     = castRawWordToInt(rawWord)
  @alwaysinline def toLong: Long   = castRawWordToLong(rawWord)

  @alwaysinline def toUByte: UByte   = toByte.toUByte
  @alwaysinline def toUShort: UShort = toShort.toUShort
  @alwaysinline def toUInt: UInt     = toInt.toUInt
  @alwaysinline def toULong: ULong   = toLong.toULong
  @alwaysinline def toUWord: UWord   = new UWord(rawWord)

  override def hashCode: Int = java.lang.Long.hashCode(toLong)

  override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Word =>
        other.rawWord == rawWord
      case _ =>
        false
    })

  // TODO(shadaj): equality with integers

  override def toString(): String = toLong.toString

  /**
   * Returns the bitwise negation of this value.
   * @example {{{
   * ~5 == 4294967290
   * // in binary: ~00000101 ==
   * //             11111010
   * }}}
   */
  @alwaysinline def unary_~ : Word =
    (~toLong).toWord // TODO(shadaj): intrinsify

  /** Returns the negated version of this value. */
  @alwaysinline def unary_- : Word = 0 - this // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @alwaysinline def <<(x: Int): Word =
    (toLong << x).toWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @alwaysinline def <<(x: Long): Word =
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
  @alwaysinline def >>>(x: Int): Word =
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
  @alwaysinline def >>>(x: Long): Word =
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
  @alwaysinline def ==(x: Byte): Boolean = this == x.toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @alwaysinline def ==(x: Short): Boolean = this == x.toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @alwaysinline def ==(x: Int): Boolean = this == x.toWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @alwaysinline def ==(x: Long): Boolean = this.toLong == x

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @alwaysinline def ==(other: Word): Boolean =
    this.toLong == other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @alwaysinline def !=(x: Byte): Boolean = this != x.toWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @alwaysinline def !=(x: Short): Boolean = this != x.toWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @alwaysinline def !=(x: Int): Boolean = this != x.toWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @alwaysinline def !=(x: Long): Boolean = this.toLong != x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @alwaysinline def !=(other: Word): Boolean =
    this.toLong != other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @alwaysinline def <(x: Byte): Boolean = this < x.toWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @alwaysinline def <(x: Short): Boolean = this < x.toWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @alwaysinline def <(x: Int): Boolean = this < x.toWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @alwaysinline def <(x: Long): Boolean = this.toLong < x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @alwaysinline def <(other: Word): Boolean =
    this.toLong < other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @alwaysinline def <=(x: Byte): Boolean = this <= x.toWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @alwaysinline def <=(x: Short): Boolean = this <= x.toWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @alwaysinline def <=(x: Int): Boolean = this <= x.toWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @alwaysinline def <=(x: Long): Boolean = this.toLong <= x

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @alwaysinline def <=(other: Word): Boolean =
    this.toLong <= other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @alwaysinline def >(x: Byte): Boolean = this > x.toWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @alwaysinline def >(x: Short): Boolean = this > x.toWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @alwaysinline def >(x: Int): Boolean = this > x.toWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @alwaysinline def >(x: Long): Boolean = this.toLong > x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @alwaysinline def >(other: Word): Boolean =
    this.toLong > other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @alwaysinline def >=(x: Byte): Boolean = this >= x.toWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @alwaysinline def >=(x: Short): Boolean = this >= x.toWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @alwaysinline def >=(x: Int): Boolean = this >= x.toWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @alwaysinline def >=(x: Long): Boolean = this.toLong >= x

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @alwaysinline def >=(other: Word): Boolean =
    this.toLong >= other.toLong // TODO(shadaj): intrinsify

  /** Returns the bitwise AND of this value and `x`. */
  @alwaysinline def &(x: Byte): Word = this & x.toWord

  /** Returns the bitwise AND of this value and `x`. */
  @alwaysinline def &(x: Short): Word = this & x.toWord

  /** Returns the bitwise AND of this value and `x`. */
  @alwaysinline def &(x: Int): Word = this & x.toWord

  /** Returns the bitwise AND of this value and `x`. */
  @alwaysinline def &(x: Long): Long = this.toLong & x

  /** Returns the bitwise AND of this value and `x`. */
  @alwaysinline def &(other: Word): Word =
    new Word(andRawWords(rawWord, other.rawWord))

  /** Returns the bitwise OR of this value and `x`. */
  @alwaysinline def |(x: Byte): Word = this | x.toWord

  /** Returns the bitwise OR of this value and `x`. */
  @alwaysinline def |(x: Short): Word = this | x.toWord

  /** Returns the bitwise OR of this value and `x`. */
  @alwaysinline def |(x: Int): Word = this | x.toWord

  /** Returns the bitwise OR of this value and `x`. */
  @alwaysinline def |(x: Long): Long = this.toLong | x

  /** Returns the bitwise OR of this value and `x`. */
  @alwaysinline def |(other: Word): Word =
    new Word(orRawWords(rawWord, other.rawWord))

  /** Returns the bitwise XOR of this value and `x`. */
  @alwaysinline def ^(x: Byte): Word = this ^ x.toWord

  /** Returns the bitwise XOR of this value and `x`. */
  @alwaysinline def ^(x: Short): Word = this ^ x.toWord

  /** Returns the bitwise XOR of this value and `x`. */
  @alwaysinline def ^(x: Int): Word = this ^ x.toWord

  /** Returns the bitwise XOR of this value and `x`. */
  @alwaysinline def ^(x: Long): Long = this.toLong ^ x

  /** Returns the bitwise XOR of this value and `x`. */
  @alwaysinline def ^(other: Word): Word =
    new Word(xorRawWords(rawWord, other.rawWord))

  /** Returns the sum of this value and `x`. */
  @alwaysinline def +(x: Byte): Word = this + x.toWord

  /** Returns the sum of this value and `x`. */
  @alwaysinline def +(x: Short): Word = this + x.toWord

  /** Returns the sum of this value and `x`. */
  @alwaysinline def +(x: Int): Word = this + x.toWord

  /** Returns the sum of this value and `x`. */
  @alwaysinline def +(x: Long): Long = this.toLong + x

  /** Returns the sum of this value and `x`. */
  @alwaysinline def +(other: Word): Word =
    new Word(addRawWords(rawWord, other.rawWord))

  /** Returns the difference of this value and `x`. */
  @alwaysinline def -(x: Byte): Word = this - x.toWord

  /** Returns the difference of this value and `x`. */
  @alwaysinline def -(x: Short): Word = this - x.toWord

  /** Returns the difference of this value and `x`. */
  @alwaysinline def -(x: Int): Word = this - x.toWord

  /** Returns the difference of this value and `x`. */
  @alwaysinline def -(x: Long): Long = this.toLong - x

  /** Returns the difference of this value and `x`. */
  @alwaysinline def -(other: Word): Word =
    new Word(subRawWords(rawWord, other.rawWord))

  /** Returns the product of this value and `x`. */
  @alwaysinline def *(x: Byte): Word = this * x.toWord

  /** Returns the product of this value and `x`. */
  @alwaysinline def *(x: Short): Word = this * x.toWord

  /** Returns the product of this value and `x`. */
  @alwaysinline def *(x: Int): Word = this * x.toWord

  /** Returns the product of this value and `x`. */
  @alwaysinline def *(x: Long): Long = this.toLong * x

  /** Returns the product of this value and `x`. */
  @alwaysinline def *(other: Word): Word =
    new Word(multRawWords(rawWord, other.rawWord))

  /** Returns the quotient of this value and `x`. */
  @alwaysinline def /(x: Byte): Word = this / x.toWord

  /** Returns the quotient of this value and `x`. */
  @alwaysinline def /(x: Short): Word = this / x.toWord

  /** Returns the quotient of this value and `x`. */
  @alwaysinline def /(x: Int): Word = this / x.toWord

  /** Returns the quotient of this value and `x`. */
  @alwaysinline def /(x: Long): Long = this.toLong / x

  /** Returns the quotient of this value and `x`. */
  @alwaysinline def /(other: Word): Word =
    new Word(divRawWords(rawWord, other.rawWord))

  /** Returns the remainder of the division of this value by `x`. */
  @alwaysinline def %(x: Byte): Word = this % x.toWord

  /** Returns the remainder of the division of this value by `x`. */
  @alwaysinline def %(x: Short): Word = this % x.toWord

  /** Returns the remainder of the division of this value by `x`. */
  @alwaysinline def %(x: Int): Word = this % x.toWord

  /** Returns the remainder of the division of this value by `x`. */
  @alwaysinline def %(x: Long): Long = this.toLong % x

  /** Returns the remainder of the division of this value by `x`. */
  @alwaysinline def %(other: Word): Word =
    new Word(modRawWords(rawWord, other.rawWord))

}

object Word {
  @alwaysinline implicit def byteToWord(x: Byte): Word =
    new Word(castIntToRawWord(x))
  @alwaysinline implicit def charToWord(x: Char): Word =
    new Word(castIntToRawWord(x))
  @alwaysinline implicit def shortToWord(x: Short): Word =
    new Word(castIntToRawWord(x))
  @alwaysinline implicit def intToWord(x: Int): Word =
    new Word(castIntToRawWord(x))
}
