package scala.scalanative
package native

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag
import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

final class Word(private[scalanative] val rawWord: RawWord) {
  @alwaysinline def toInt: Int     = castRawWordToInt(rawWord)
  @alwaysinline def toUInt: UInt   = castRawWordToInt(rawWord).toUInt
  @alwaysinline def toUWord: UWord = new UWord(rawWord)

  @alwaysinline def +(other: Word): Word =
    new Word(addRawWords(rawWord, other.rawWord))
  @alwaysinline def -(other: Word): Word =
    new Word(subRawWords(rawWord, other.rawWord))
  @alwaysinline def *(other: Word): Word =
    new Word(multRawWords(rawWord, other.rawWord))
  @alwaysinline def /(other: Word): Word =
    new Word(divRawWords(rawWord, other.rawWord))

  override def hashCode: Int =
    java.lang.Long.hashCode(Word.wordToLong(this))

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
  @alwaysinline implicit def wordToLong(word: Word): Long =
    castRawWordToLong(word.rawWord)
}
