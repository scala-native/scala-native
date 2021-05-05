package scala.scalanative.windows.util

import scala.scalanative.unsigned._
import scala.scalanative.windows._

object Conversion {
  def wordToBytes(word: Word): (Byte, Byte) = {
    val lowByte  = (word & 0xFF.toUShort).toByte
    val highByte = (word >> 8 & 0xFF.toUShort).toByte
    (lowByte, highByte)
  }

  def wordFromBytes(low: Byte, high: Byte): Word = {
    ((low & 0xff) | ((high & 0xff) << 8)).toUShort
  }
}
