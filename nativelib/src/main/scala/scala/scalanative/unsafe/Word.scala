package scala.scalanative
package unsafe

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag

import scalanative.annotation.alwaysinline

import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

import scalanative.unsigned._

final class Word(private[scalanative] val rawWord: RawWord) {
  @alwaysinline def toInt: Int   = castRawWordToInt(rawWord)
  @alwaysinline def toLong: Long = castRawWordToLong(rawWord)

  @alwaysinline def toUInt: UInt   = castRawWordToInt(rawWord).toUInt
  @alwaysinline def toUWord: UWord = new UWord(rawWord)

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

  @alwaysinline def +(other: Word): Word =
    new Word(addRawWords(rawWord, other.rawWord))
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

  @alwaysinline def /(other: Word): Word =
    new Word(divRawWords(rawWord, other.rawWord))

  override def hashCode: Int = java.lang.Long.hashCode(toLong)

  override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Word =>
        other.rawWord == rawWord
      case _ =>
        false
    })

  override def toString(): String = castRawWordToLong(rawWord).toString
}

object Word {
  @alwaysinline implicit def intToWord(int: Int): Word =
    new Word(castIntToRawWord(int))
}
