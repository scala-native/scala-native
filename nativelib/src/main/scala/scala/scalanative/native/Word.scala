package scala.scalanative
package native

import scalanative.runtime.intrinsic

/** Int on 32-bit architectures and Long on 64-bit ones. */
final abstract class Word {
  def toInt: Int   = intrinsic
  def toUInt: UInt = intrinsic

  def +(other: Word): Word = intrinsic
  def -(other: Word): Word = intrinsic
  def *(other: Word): Word = intrinsic
  def /(other: Word): Word = intrinsic

  def >>(shift: Word): Word = intrinsic
}

object Word {
  implicit def intToWord(int: Int): Word    = int.cast[Word]
  implicit def wordToLong(word: Word): Long = word.cast[Long]
}
