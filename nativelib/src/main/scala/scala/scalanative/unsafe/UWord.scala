package scala.scalanative
package native

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag
import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

final class UWord(private[scalanative] val rawWord: RawWord) {
  @alwaysinline def toInt: Int   = castRawWordToInt(rawWord).toInt
  @alwaysinline def toUInt: UInt = castRawWordToInt(rawWord).toUInt

  @alwaysinline def +(other: UWord): UWord =
    new UWord(addRawWords(rawWord, other.rawWord))
  @alwaysinline def -(other: UWord): UWord =
    new UWord(subRawWords(rawWord, other.rawWord))
  @alwaysinline def *(other: UWord): UWord =
    new UWord(multRawWords(rawWord, other.rawWord))
  @alwaysinline def /(other: UWord): UWord =
    new UWord(divRawWordsUnsigned(rawWord, other.rawWord))

  override def hashCode: Int =
    java.lang.Long.hashCode(castRawWordToLong(rawWord))

  override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: UWord =>
        other.rawWord == rawWord
      case _ =>
        false
    })

  override def toString(): String = castRawWordToLong(rawWord).toString
}

object UWord {
  @alwaysinline implicit def uintToWord(uint: UInt): UWord =
    new UWord(castIntToRawWord(uint.toInt))
  @alwaysinline implicit def wordToULong(word: UWord): ULong =
    castRawWordToLong(word.rawWord).toULong
}
