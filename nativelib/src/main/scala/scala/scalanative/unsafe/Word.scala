package scala.scalanative
package native

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag
import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

final class Word(private[scalanative] val rawWord: RawWord) {
  def toInt: Int   = castRawWordToInt(rawWord)
  def toUInt: UInt = castRawWordToInt(rawWord).toUInt

  def +(other: Word): Word = new Word(addRawWords(rawWord, other.rawWord))
  def -(other: Word): Word = new Word(subRawWords(rawWord, other.rawWord))
  def *(other: Word): Word = new Word(multRawWords(rawWord, other.rawWord))
  def /(other: Word): Word = new Word(divRawWords(rawWord, other.rawWord))

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
