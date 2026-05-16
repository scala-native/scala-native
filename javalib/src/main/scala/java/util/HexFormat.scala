package java.util

import java.io.{IOException, UncheckedIOException}
import java.{lang => jl}

final class HexFormat private (
    private val delimiterString: String,
    private val prefixString: String,
    private val suffixString: String,
    private val uppercase: Boolean
) {

  private val digits: Array[Char] =
    if (uppercase) HexFormat.UpperCaseDigits else HexFormat.LowerCaseDigits

  def withDelimiter(delimiter: String): HexFormat =
    new HexFormat(
      Objects.requireNonNull(delimiter, "delimiter"),
      prefixString,
      suffixString,
      uppercase
    )

  def withPrefix(prefix: String): HexFormat =
    new HexFormat(
      delimiterString,
      Objects.requireNonNull(prefix, "prefix"),
      suffixString,
      uppercase
    )

  def withSuffix(suffix: String): HexFormat =
    new HexFormat(
      delimiterString,
      prefixString,
      Objects.requireNonNull(suffix, "suffix"),
      uppercase
    )

  def withUpperCase(): HexFormat =
    if (uppercase) this
    else new HexFormat(delimiterString, prefixString, suffixString, true)

  def withLowerCase(): HexFormat =
    if (uppercase)
      new HexFormat(delimiterString, prefixString, suffixString, false)
    else this

  def delimiter(): String = delimiterString

  def prefix(): String = prefixString

  def suffix(): String = suffixString

  def isUpperCase(): Boolean = uppercase

  def formatHex(bytes: Array[Byte]): String =
    formatHex(bytes, 0, bytes.length)

  def formatHex(bytes: Array[Byte], fromIndex: Int, toIndex: Int): String = {
    Objects.checkFromToIndex(fromIndex, toIndex, bytes.length)

    val length = toIndex - fromIndex
    if (length == 0) ""
    else if (hasNoMarkup) {
      val chars = new Array[Char](length << 1)
      var byteIndex = fromIndex
      var charIndex = 0
      while (byteIndex < toIndex) {
        val value = bytes(byteIndex) & 0xff
        chars(charIndex) = digits(value >>> 4)
        chars(charIndex + 1) = digits(value & 0x0f)
        byteIndex += 1
        charIndex += 2
      }
      new String(chars)
    } else {
      val chars = new Array[Char](formattedLength(length))
      var byteIndex = fromIndex
      var charIndex = 0
      while (byteIndex < toIndex) {
        if (byteIndex != fromIndex)
          charIndex = putString(chars, charIndex, delimiterString)
        charIndex = putString(chars, charIndex, prefixString)
        val value = bytes(byteIndex) & 0xff
        chars(charIndex) = digits(value >>> 4)
        chars(charIndex + 1) = digits(value & 0x0f)
        charIndex += 2
        charIndex = putString(chars, charIndex, suffixString)
        byteIndex += 1
      }
      new String(chars)
    }
  }

  def formatHex[A <: jl.Appendable](out: A, bytes: Array[Byte]): A =
    formatHex(out, bytes, 0, bytes.length)

  def formatHex[A <: jl.Appendable](
      out: A,
      bytes: Array[Byte],
      fromIndex: Int,
      toIndex: Int
  ): A = {
    Objects.requireNonNull(out, "out")
    Objects.checkFromToIndex(fromIndex, toIndex, bytes.length)

    try {
      var byteIndex = fromIndex
      while (byteIndex < toIndex) {
        if (byteIndex != fromIndex)
          appendString(out, delimiterString)
        appendString(out, prefixString)
        val value = bytes(byteIndex) & 0xff
        out.append(digits(value >>> 4))
        out.append(digits(value & 0x0f))
        appendString(out, suffixString)
        byteIndex += 1
      }
    } catch {
      case e: IOException => throw new UncheckedIOException(e.getMessage(), e)
    }

    out
  }

  def parseHex(string: jl.CharSequence): Array[Byte] =
    parseHex(string, 0, string.length())

  def parseHex(
      string: jl.CharSequence,
      fromIndex: Int,
      toIndex: Int
  ): Array[Byte] = {
    Objects.checkFromToIndex(fromIndex, toIndex, string.length())

    val length = toIndex - fromIndex
    if (hasNoMarkup)
      parseHexWithoutMarkup(string, fromIndex, length)
    else
      parseHexWithMarkup(string, fromIndex, length)
  }

  def parseHex(
      chars: Array[Char],
      fromIndex: Int,
      toIndex: Int
  ): Array[Byte] = {
    Objects.checkFromToIndex(fromIndex, toIndex, chars.length)

    val length = toIndex - fromIndex
    if (hasNoMarkup)
      parseHexWithoutMarkup(chars, fromIndex, length)
    else
      parseHexWithMarkup(chars, fromIndex, length)
  }

  def toLowHexDigit(value: Int): Char =
    digits(value & 0x0f)

  def toHighHexDigit(value: Int): Char =
    digits((value >>> 4) & 0x0f)

  def toHexDigits[A <: jl.Appendable](out: A, value: Byte): A = {
    Objects.requireNonNull(out, "out")
    try {
      val intValue = value & 0xff
      out.append(digits(intValue >>> 4))
      out.append(digits(intValue & 0x0f))
    } catch {
      case e: IOException => throw new UncheckedIOException(e.getMessage(), e)
    }
    out
  }

  def toHexDigits(value: Byte): String =
    twoDigits(value & 0xff)

  def toHexDigits(value: Char): String =
    toHexDigits(value.toInt.toLong, 4)

  def toHexDigits(value: Short): String =
    toHexDigits((value.toInt & 0xffff).toLong, 4)

  def toHexDigits(value: Int): String =
    toHexDigits(value.toLong, 8)

  def toHexDigits(value: Long): String =
    toHexDigits(value, 16)

  def toHexDigits(value: Long, digits: Int): String = {
    if (digits < 0 || digits > 16)
      throw new IllegalArgumentException("number of digits: " + digits)

    val chars = new Array[Char](digits)
    var i = 0
    var shift = (digits - 1) << 2
    while (i < digits) {
      chars(i) = this.digits(((value >>> shift) & 0x0fL).toInt)
      i += 1
      shift -= 4
    }
    new String(chars)
  }

  override def equals(other: Any): Boolean = {
    other match {
      case that: HexFormat =>
        uppercase == that.uppercase &&
          delimiterString == that.delimiterString &&
          prefixString == that.prefixString &&
          suffixString == that.suffixString
      case _ => false
    }
  }

  override def hashCode(): Int = {
    var hash = if (uppercase) 1231 else 1237
    hash = 31 * hash + delimiterString.hashCode()
    hash = 31 * hash + prefixString.hashCode()
    hash = 31 * hash + suffixString.hashCode()
    hash
  }

  override def toString(): String =
    "uppercase: " + uppercase +
      ", delimiter: \"" + delimiterString +
      "\", prefix: \"" + prefixString +
      "\", suffix: \"" + suffixString + "\""

  private def hasNoMarkup: Boolean =
    delimiterString.isEmpty() && prefixString.isEmpty() && suffixString
      .isEmpty()

  private def formattedLength(byteCount: Int): Int = {
    val valueLength = prefixString.length() + 2 + suffixString.length()
    val total =
      byteCount.toLong * valueLength.toLong +
        (byteCount.toLong - 1L) * delimiterString.length().toLong

    if (total > Integer.MAX_VALUE)
      throw new OutOfMemoryError("Required array size too large")
    total.toInt
  }

  private def putString(chars: Array[Char], index: Int, string: String): Int = {
    val length = string.length()
    string.getChars(0, length, chars, index)
    index + length
  }

  private def appendString(out: jl.Appendable, string: String): Unit = {
    if (!string.isEmpty())
      out.append(string)
  }

  private def twoDigits(value: Int): String = {
    val chars = new Array[Char](2)
    chars(0) = digits(value >>> 4)
    chars(1) = digits(value & 0x0f)
    new String(chars)
  }

  private def parseHexWithoutMarkup(
      string: jl.CharSequence,
      fromIndex: Int,
      length: Int
  ): Array[Byte] = {
    if ((length & 1) != 0)
      throw new IllegalArgumentException("string length not even: " + length)

    val bytes = new Array[Byte](length >>> 1)
    var sourceIndex = fromIndex
    var destIndex = 0
    while (destIndex < bytes.length) {
      bytes(destIndex) = HexFormat.fromHexDigits(
        string.charAt(sourceIndex),
        string.charAt(sourceIndex + 1)
      )
      sourceIndex += 2
      destIndex += 1
    }
    bytes
  }

  private def parseHexWithoutMarkup(
      chars: Array[Char],
      fromIndex: Int,
      length: Int
  ): Array[Byte] = {
    if ((length & 1) != 0)
      throw new IllegalArgumentException("string length not even: " + length)

    val bytes = new Array[Byte](length >>> 1)
    var sourceIndex = fromIndex
    var destIndex = 0
    while (destIndex < bytes.length) {
      bytes(destIndex) =
        HexFormat.fromHexDigits(chars(sourceIndex), chars(sourceIndex + 1))
      sourceIndex += 2
      destIndex += 1
    }
    bytes
  }

  private def parseHexWithMarkup(
      string: jl.CharSequence,
      fromIndex: Int,
      length: Int
  ): Array[Byte] = {
    val byteCount = parsedByteCount(length)
    val bytes = new Array[Byte](byteCount)
    var sourceIndex = fromIndex
    var destIndex = 0
    while (destIndex < byteCount) {
      sourceIndex = requireMatch(string, sourceIndex, prefixString)
      bytes(destIndex) = HexFormat.fromHexDigits(
        string.charAt(sourceIndex),
        string.charAt(sourceIndex + 1)
      )
      sourceIndex += 2
      sourceIndex = requireMatch(string, sourceIndex, suffixString)
      if (destIndex != byteCount - 1)
        sourceIndex = requireMatch(string, sourceIndex, delimiterString)
      destIndex += 1
    }
    bytes
  }

  private def parseHexWithMarkup(
      chars: Array[Char],
      fromIndex: Int,
      length: Int
  ): Array[Byte] = {
    val byteCount = parsedByteCount(length)
    val bytes = new Array[Byte](byteCount)
    var sourceIndex = fromIndex
    var destIndex = 0
    while (destIndex < byteCount) {
      sourceIndex = requireMatch(chars, sourceIndex, prefixString)
      bytes(destIndex) =
        HexFormat.fromHexDigits(chars(sourceIndex), chars(sourceIndex + 1))
      sourceIndex += 2
      sourceIndex = requireMatch(chars, sourceIndex, suffixString)
      if (destIndex != byteCount - 1)
        sourceIndex = requireMatch(chars, sourceIndex, delimiterString)
      destIndex += 1
    }
    bytes
  }

  private def parsedByteCount(length: Int): Int = {
    if (length == 0)
      0
    else {
      val valueLength = prefixString.length() + 2 + suffixString.length()
      val recordLength = valueLength + delimiterString.length()
      val adjustedLength = length.toLong + delimiterString.length().toLong

      if (adjustedLength % recordLength != 0L)
        throw HexFormat.invalidFormatException()

      val count = adjustedLength / recordLength
      if (count <= 0L || count > Integer.MAX_VALUE)
        throw HexFormat.invalidFormatException()
      count.toInt
    }
  }

  private def requireMatch(
      string: jl.CharSequence,
      index: Int,
      expected: String
  ): Int = {
    var i = 0
    while (i < expected.length()) {
      if (string.charAt(index + i) != expected.charAt(i))
        throw HexFormat.invalidFormatException()
      i += 1
    }
    index + expected.length()
  }

  private def requireMatch(
      chars: Array[Char],
      index: Int,
      expected: String
  ): Int = {
    var i = 0
    while (i < expected.length()) {
      if (chars(index + i) != expected.charAt(i))
        throw HexFormat.invalidFormatException()
      i += 1
    }
    index + expected.length()
  }
}

object HexFormat {
  private[util] val LowerCaseDigits: Array[Char] =
    "0123456789abcdef".toCharArray()

  private[util] val UpperCaseDigits: Array[Char] =
    "0123456789ABCDEF".toCharArray()

  private val DigitValues: Array[Byte] = {
    val values = Array.fill[Byte](128)(-1)
    var ch = '0'
    while (ch <= '9') {
      values(ch) = (ch - '0').toByte
      ch = (ch + 1).toChar
    }
    ch = 'A'
    while (ch <= 'F') {
      values(ch) = (ch - 'A' + 10).toByte
      ch = (ch + 1).toChar
    }
    ch = 'a'
    while (ch <= 'f') {
      values(ch) = (ch - 'a' + 10).toByte
      ch = (ch + 1).toChar
    }
    values
  }

  private val Default = new HexFormat("", "", "", false)

  def of(): HexFormat = Default

  def ofDelimiter(delimiter: String): HexFormat =
    new HexFormat(Objects.requireNonNull(delimiter, "delimiter"), "", "", false)

  def isHexDigit(ch: Int): Boolean =
    ch >= 0 && ch < DigitValues.length && DigitValues(ch) >= 0

  def fromHexDigit(ch: Int): Int = {
    if (ch >= 0 && ch < DigitValues.length) {
      val value = DigitValues(ch).toInt
      if (value >= 0) return value
    }

    {
      val value =
        if (ch >= 0 && ch <= Character.MAX_CODE_POINT)
          new String(Character.toChars(ch))
        else Integer.toString(ch)
      throw new NumberFormatException(
        "not a hexadecimal digit: \"" + value + "\" = " + ch
      )
    }
  }

  def fromHexDigits(string: jl.CharSequence): Int =
    fromHexDigits(string, 0, string.length())

  def fromHexDigits(
      string: jl.CharSequence,
      fromIndex: Int,
      toIndex: Int
  ): Int = {
    Objects.checkFromToIndex(fromIndex, toIndex, string.length())
    val length = toIndex - fromIndex
    if (length > 8)
      throw new IllegalArgumentException(
        "string length greater than 8: " + length
      )

    var value = 0
    var i = fromIndex
    while (i < toIndex) {
      value = (value << 4) | fromHexDigit(string.charAt(i))
      i += 1
    }
    value
  }

  def fromHexDigitsToLong(string: jl.CharSequence): Long =
    fromHexDigitsToLong(string, 0, string.length())

  def fromHexDigitsToLong(
      string: jl.CharSequence,
      fromIndex: Int,
      toIndex: Int
  ): Long = {
    Objects.checkFromToIndex(fromIndex, toIndex, string.length())
    val length = toIndex - fromIndex
    if (length > 16)
      throw new IllegalArgumentException(
        "string length greater than 16: " + length
      )

    var value = 0L
    var i = fromIndex
    while (i < toIndex) {
      value = (value << 4) | fromHexDigit(string.charAt(i)).toLong
      i += 1
    }
    value
  }

  private[util] def fromHexDigits(high: Char, low: Char): Byte =
    ((fromHexDigit(high) << 4) | fromHexDigit(low)).toByte

  private[util] def invalidFormatException(): IllegalArgumentException =
    new IllegalArgumentException(
      "extra or missing delimiters or values consisting of prefix, " +
        "two hexadecimal digits, and suffix"
    )
}
