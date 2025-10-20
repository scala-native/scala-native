package scala.scalanative.windows.util

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scala.scalanative.windows.{Word as WinWord, *}

private[windows] object Conversion {
  def wordToBytes(word: WinWord): (Byte, Byte) = {
    val lowByte = (word.toInt & 0xff).toByte
    val highByte = ((word.toInt >> 8) & 0xff).toByte
    (lowByte, highByte)
  }

  def wordFromBytes(low: Byte, high: Byte): WinWord = {
    ((low & 0xff) | ((high & 0xff) << 8)).toUShort
  }

  def dwordPairToULargeInteger(high: DWord, low: DWord): ULargeInteger = {
    if (high == 0) low
    else (high.toULong << 32) | low
  }

  def uLargeIntegerToDWordPair(
      v: ULargeInteger,
      high: Ptr[DWord],
      low: Ptr[DWord]
  ): Unit = {
    !high = (v >> 32).toUInt
    !low = v.toUInt
  }
}
