package scala.scalanative.windows.util

import scala.scalanative.unsigned._
import scala.scalanative.windows.{Word => WinWord}

private[windows] object Conversion {
  def wordToBytes(word: WinWord): (Byte, Byte) = {
    val lowByte = (word.toInt & 0xff).toByte
    val highByte = ((word.toInt >> 8) & 0xff).toByte
    (lowByte, highByte)
  }

  def wordFromBytes(low: Byte, high: Byte): WinWord = {
    ((low & 0xff) | ((high & 0xff) << 8)).toUShort
  }
}
