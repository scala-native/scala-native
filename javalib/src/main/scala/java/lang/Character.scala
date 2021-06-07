package java.lang

import java.util.Arrays

class Character(val _value: scala.Char)
    extends _Object
    with java.io.Serializable
    with Comparable[Character] {
  def charValue(): scala.Char =
    _value

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Character =>
        _value == that._value
      case _ =>
        false
    }

  @inline override def compareTo(that: Character): Int =
    Character.compare(_value, that._value)

  @inline override def toString(): String =
    Character.toString(_value)

  @inline override def hashCode(): Int =
    Character.hashCode(_value)

  @inline override def __scala_==(other: _Object): scala.Boolean =
    other match {
      case other: java.lang.Character => _value == other._value
      case other: java.lang.Byte      => _value == other._value
      case other: java.lang.Short     => _value == other._value
      case other: java.lang.Integer   => _value == other._value
      case other: java.lang.Long      => _value == other._value
      case other: java.lang.Float     => _value == other._value
      case other: java.lang.Double    => _value == other._value
      case other: java.lang.Number    => other.__scala_==(this)
      case _                          => super.__scala_==(other)
    }

  /*
   * Ported from ScalaJS
   *
   * Methods on scala.Char
   * The following methods are only here to properly support reflective calls
   * on boxed primitive values. YOU WILL NOT BE ABLE TO USE THESE METHODS, since
   * we use the true javalib to lookup symbols, this file contains only
   * implementations.
   */
  protected def toByte: scala.Byte     = _value.toByte
  protected def toShort: scala.Short   = _value.toShort
  protected def toChar: scala.Char     = _value.toChar
  protected def toInt: scala.Int       = _value
  protected def toLong: scala.Long     = _value.toLong
  protected def toFloat: scala.Float   = _value.toFloat
  protected def toDouble: scala.Double = _value.toDouble

  // scalastyle:off disallow.space.before.token
  protected def unary_~ : scala.Int = ~_value.toInt
  protected def unary_+ : scala.Int = _value.toInt
  protected def unary_- : scala.Int = -_value.toInt
  // scalastyle:on disallow.space.before.token

  protected def +(x: String): String = "" + _value + x

  protected def <<(x: scala.Int): scala.Int   = _value << x
  protected def <<(x: scala.Long): scala.Int  = _value << x.toInt
  protected def >>>(x: scala.Int): scala.Int  = _value >>> x
  protected def >>>(x: scala.Long): scala.Int = _value >>> x.toInt
  protected def >>(x: scala.Int): scala.Int   = _value >> x
  protected def >>(x: scala.Long): scala.Int  = _value >> x.toInt

  protected def ==(x: scala.Byte): scala.Boolean   = _value == x
  protected def ==(x: scala.Short): scala.Boolean  = _value == x
  protected def ==(x: scala.Char): scala.Boolean   = _value == x
  protected def ==(x: scala.Int): scala.Boolean    = _value == x
  protected def ==(x: scala.Long): scala.Boolean   = _value == x
  protected def ==(x: scala.Float): scala.Boolean  = _value == x
  protected def ==(x: scala.Double): scala.Boolean = _value == x

  protected def !=(x: scala.Byte): scala.Boolean   = _value != x
  protected def !=(x: scala.Short): scala.Boolean  = _value != x
  protected def !=(x: scala.Char): scala.Boolean   = _value != x
  protected def !=(x: scala.Int): scala.Boolean    = _value != x
  protected def !=(x: scala.Long): scala.Boolean   = _value != x
  protected def !=(x: scala.Float): scala.Boolean  = _value != x
  protected def !=(x: scala.Double): scala.Boolean = _value != x

  protected def <(x: scala.Byte): scala.Boolean   = _value < x
  protected def <(x: scala.Short): scala.Boolean  = _value < x
  protected def <(x: scala.Char): scala.Boolean   = _value < x
  protected def <(x: scala.Int): scala.Boolean    = _value < x
  protected def <(x: scala.Long): scala.Boolean   = _value < x
  protected def <(x: scala.Float): scala.Boolean  = _value < x
  protected def <(x: scala.Double): scala.Boolean = _value < x

  protected def <=(x: scala.Byte): scala.Boolean   = _value <= x
  protected def <=(x: scala.Short): scala.Boolean  = _value <= x
  protected def <=(x: scala.Char): scala.Boolean   = _value <= x
  protected def <=(x: scala.Int): scala.Boolean    = _value <= x
  protected def <=(x: scala.Long): scala.Boolean   = _value <= x
  protected def <=(x: scala.Float): scala.Boolean  = _value <= x
  protected def <=(x: scala.Double): scala.Boolean = _value <= x

  protected def >(x: scala.Byte): scala.Boolean   = _value > x
  protected def >(x: scala.Short): scala.Boolean  = _value > x
  protected def >(x: scala.Char): scala.Boolean   = _value > x
  protected def >(x: scala.Int): scala.Boolean    = _value > x
  protected def >(x: scala.Long): scala.Boolean   = _value > x
  protected def >(x: scala.Float): scala.Boolean  = _value > x
  protected def >(x: scala.Double): scala.Boolean = _value > x

  protected def >=(x: scala.Byte): scala.Boolean   = _value >= x
  protected def >=(x: scala.Short): scala.Boolean  = _value >= x
  protected def >=(x: scala.Char): scala.Boolean   = _value >= x
  protected def >=(x: scala.Int): scala.Boolean    = _value >= x
  protected def >=(x: scala.Long): scala.Boolean   = _value >= x
  protected def >=(x: scala.Float): scala.Boolean  = _value >= x
  protected def >=(x: scala.Double): scala.Boolean = _value >= x

  protected def |(x: scala.Byte): scala.Int  = _value | x
  protected def |(x: scala.Short): scala.Int = _value | x
  protected def |(x: scala.Char): scala.Int  = _value | x
  protected def |(x: scala.Int): scala.Int   = _value | x
  protected def |(x: scala.Long): scala.Long = _value | x

  protected def &(x: scala.Byte): scala.Int  = _value & x
  protected def &(x: scala.Short): scala.Int = _value & x
  protected def &(x: scala.Char): scala.Int  = _value & x
  protected def &(x: scala.Int): scala.Int   = _value & x
  protected def &(x: scala.Long): scala.Long = _value & x

  protected def ^(x: scala.Byte): scala.Int  = _value ^ x
  protected def ^(x: scala.Short): scala.Int = _value ^ x
  protected def ^(x: scala.Char): scala.Int  = _value ^ x
  protected def ^(x: scala.Int): scala.Int   = _value ^ x
  protected def ^(x: scala.Long): scala.Long = _value ^ x

  protected def +(x: scala.Byte): scala.Int      = _value + x
  protected def +(x: scala.Short): scala.Int     = _value + x
  protected def +(x: scala.Char): scala.Int      = _value + x
  protected def +(x: scala.Int): scala.Int       = _value + x
  protected def +(x: scala.Long): scala.Long     = _value + x
  protected def +(x: scala.Float): scala.Float   = _value + x
  protected def +(x: scala.Double): scala.Double = _value + x

  protected def -(x: scala.Byte): scala.Int      = _value - x
  protected def -(x: scala.Short): scala.Int     = _value - x
  protected def -(x: scala.Char): scala.Int      = _value - x
  protected def -(x: scala.Int): scala.Int       = _value - x
  protected def -(x: scala.Long): scala.Long     = _value - x
  protected def -(x: scala.Float): scala.Float   = _value - x
  protected def -(x: scala.Double): scala.Double = _value - x

  protected def *(x: scala.Byte): scala.Int      = _value * x
  protected def *(x: scala.Short): scala.Int     = _value * x
  protected def *(x: scala.Char): scala.Int      = _value * x
  protected def *(x: scala.Int): scala.Int       = _value * x
  protected def *(x: scala.Long): scala.Long     = _value * x
  protected def *(x: scala.Float): scala.Float   = _value * x
  protected def *(x: scala.Double): scala.Double = _value * x

  protected def /(x: scala.Byte): scala.Int      = _value / x
  protected def /(x: scala.Short): scala.Int     = _value / x
  protected def /(x: scala.Char): scala.Int      = _value / x
  protected def /(x: scala.Int): scala.Int       = _value / x
  protected def /(x: scala.Long): scala.Long     = _value / x
  protected def /(x: scala.Float): scala.Float   = _value / x
  protected def /(x: scala.Double): scala.Double = _value / x

  protected def %(x: scala.Byte): scala.Int      = _value % x
  protected def %(x: scala.Short): scala.Int     = _value % x
  protected def %(x: scala.Char): scala.Int      = _value % x
  protected def %(x: scala.Int): scala.Int       = _value % x
  protected def %(x: scala.Long): scala.Long     = _value % x
  protected def %(x: scala.Float): scala.Float   = _value % x
  protected def %(x: scala.Double): scala.Double = _value % x
}

object Character {
  final val TYPE      = scala.Predef.classOf[scala.scalanative.runtime.PrimitiveChar]
  final val MIN_VALUE = '\u0000'
  final val MAX_VALUE = '\uffff'
  final val SIZE      = 16
  final val BYTES     = 2

  /* These are supposed to be final vals of type Byte, but that's not possible.
   * So we implement them as def's, which are binary compatible with final vals.
   */
  @inline def UNASSIGNED: scala.Byte                = 0
  @inline def UPPERCASE_LETTER: scala.Byte          = 1
  @inline def LOWERCASE_LETTER: scala.Byte          = 2
  @inline def TITLECASE_LETTER: scala.Byte          = 3
  @inline def MODIFIER_LETTER: scala.Byte           = 4
  @inline def OTHER_LETTER: scala.Byte              = 5
  @inline def NON_SPACING_MARK: scala.Byte          = 6
  @inline def ENCLOSING_MARK: scala.Byte            = 7
  @inline def COMBINING_SPACING_MARK: scala.Byte    = 8
  @inline def DECIMAL_DIGIT_NUMBER: scala.Byte      = 9
  @inline def LETTER_NUMBER: scala.Byte             = 10
  @inline def OTHER_NUMBER: scala.Byte              = 11
  @inline def SPACE_SEPARATOR: scala.Byte           = 12
  @inline def LINE_SEPARATOR: scala.Byte            = 13
  @inline def PARAGRAPH_SEPARATOR: scala.Byte       = 14
  @inline def CONTROL: scala.Byte                   = 15
  @inline def FORMAT: scala.Byte                    = 16
  @inline def PRIVATE_USE: scala.Byte               = 18
  @inline def SURROGATE: scala.Byte                 = 19
  @inline def DASH_PUNCTUATION: scala.Byte          = 20
  @inline def START_PUNCTUATION: scala.Byte         = 21
  @inline def END_PUNCTUATION: scala.Byte           = 22
  @inline def CONNECTOR_PUNCTUATION: scala.Byte     = 23
  @inline def OTHER_PUNCTUATION: scala.Byte         = 24
  @inline def MATH_SYMBOL: scala.Byte               = 25
  @inline def CURRENCY_SYMBOL: scala.Byte           = 26
  @inline def MODIFIER_SYMBOL: scala.Byte           = 27
  @inline def OTHER_SYMBOL: scala.Byte              = 28
  @inline def INITIAL_QUOTE_PUNCTUATION: scala.Byte = 29
  @inline def FINAL_QUOTE_PUNCTUATION: scala.Byte   = 30

  final val MIN_RADIX = 2
  final val MAX_RADIX = 36

  final val MIN_HIGH_SURROGATE = '\uD800'
  final val MAX_HIGH_SURROGATE = '\uDBFF'
  final val MIN_LOW_SURROGATE  = '\uDC00'
  final val MAX_LOW_SURROGATE  = '\uDFFF'
  final val MIN_SURROGATE      = MIN_HIGH_SURROGATE
  final val MAX_SURROGATE      = MAX_LOW_SURROGATE

  final val MIN_CODE_POINT               = 0
  final val MAX_CODE_POINT               = 0x10ffff
  final val MIN_SUPPLEMENTARY_CODE_POINT = 0x10000

  @inline def charCount(codePoint: Int): Int =
    if (codePoint >= MIN_SUPPLEMENTARY_CODE_POINT) 2 else 1

  def codePointAt(seq: Array[scala.Char], _index: scala.Int): scala.Int = {
    codePointAt(seq, _index, seq.length)
  }

  def codePointAt(seq: Array[scala.Char],
                  index: scala.Int,
                  limit: scala.Int): scala.Int = {

    if (index < 0 || index >= limit || limit < 0 || limit > seq.length) {
      throw new StringIndexOutOfBoundsException()
    }

    val high      = seq(index)
    val nextIndex = index + 1

    if (nextIndex >= limit) {
      high
    } else {
      val low = seq(nextIndex)
      if (isSurrogatePair(high, low))
        toCodePoint(high, low)
      else
        high
    }
  }

  def codePointAt(seq: CharSequence, _index: scala.Int): scala.Int = {
    codePointAt(seq.toString.toArray[scala.Char], _index)
  }

  def codePointBefore(seq: Array[scala.Char], index: scala.Int): scala.Int = {
    codePointBefore(seq, index, seq.length)
  }

  def codePointBefore(seq: Array[scala.Char],
                      index: scala.Int,
                      limit: scala.Int): scala.Int = {
    val len = limit
    if (index < 1 || index > len) {
      throw new StringIndexOutOfBoundsException(index)
    }

    val indexMinus1 = index - 1
    val indexMinus2 = index - 2

    val low = seq(indexMinus1)

    if (indexMinus2 < 0) {
      low
    } else {
      val high = seq(indexMinus2)
      if (isSurrogatePair(high, low))
        toCodePoint(high, low)
      else
        low
    }
  }

  def codePointBefore(seq: CharSequence, index: scala.Int): scala.Int = {
    codePointBefore(seq.toString.toArray[scala.Char], index)
  }

  def codePointCount(seq: Array[scala.Char],
                     offset: scala.Int,
                     count: scala.Int): scala.Int = {
    val len      = seq.length
    val endIndex = offset + count
    if (offset < 0 || count < 0 || endIndex > len) {
      throw new StringIndexOutOfBoundsException()
    }

    var result = 0
    var i      = offset
    while (i < endIndex) {
      var c = seq(i)
      if (isHighSurrogate(c)) {
        i += 1
        if (i < endIndex) {
          c = seq(i)
          if (!isLowSurrogate(c)) {
            result += 1
          }
        }
      }

      result += 1
      i += 1
    }

    result
  }

  def offsetByCodePoints(seq: Array[scala.Char],
                         start: scala.Int,
                         count: scala.Int,
                         index: scala.Int,
                         codePointOffset: scala.Int): scala.Int = {
    val end = start + count
    if (start < 0 || count < 0 || end > seq.length || index < start
        || index > end) {
      throw new StringIndexOutOfBoundsException()
    }

    if (codePointOffset == 0) {
      index
    } else if (codePointOffset > 0) {
      var codePoints = codePointOffset
      var i          = index
      while (codePoints > 0) {
        codePoints -= 1
        if (i >= end) {
          throw new StringIndexOutOfBoundsException()
        }
        if (isHighSurrogate(seq(i))) {
          val next = i + 1
          if (next <= end && isLowSurrogate(seq(next))) {
            i += 1
          }
        }
        i += 1
      }
      i
    } else {
      var codePoints = -codePointOffset
      var i          = index
      while (codePoints > 0) {
        codePoints -= 1
        i -= 1
        if (i < start) {
          throw new StringIndexOutOfBoundsException()
        }
        if (isLowSurrogate(seq(i))) {
          val prev = i - 1
          if (prev >= start && isHighSurrogate(seq(prev))) {
            i -= 1
          }
        }
      }
      i
    }
  }

  def hashCode(value: scala.Char): scala.Int = value

  import CharacterCache.cache

  def valueOf(charValue: scala.Char): Character = {
    if (charValue > 127) {
      new Character(charValue)
    } else {
      val idx    = charValue.toInt
      val cached = cache(idx)
      if (cached != null) {
        cached
      } else {
        val newchar = new Character(charValue)
        cache(idx) = newchar
        newchar
      }
    }
  }

  def getType(ch: scala.Char): Int = getType(ch.toInt)

  def getType(codePoint: Int): Int = {
    if (codePoint < 0) UNASSIGNED.toInt
    else if (codePoint < 256) getTypeLT256(codePoint)
    else getTypeGE256(codePoint)
  }

  @inline
  private[this] def getTypeLT256(codePoint: Int): scala.Byte =
    charTypesFirst256(codePoint)

  // Ported from Scala.js, commit: ac38a148, dated: 2020-09-25
  private[this] def getTypeGE256(codePoint: Int): scala.Byte = {
    charTypes(
      findIndexOfRange(charTypeIndices, codePoint, hasEmptyRanges = false))
  }

  // Two digit() constructors, digitWithValidRadix(), and
  // nonASCIIZeroDigitCodePoints ported, with gratitude from ScalaJS:
  // https://github.com/scala-js/scala-js/blob/master/javalanglib \
  //       /src/main/scala/java/lang/Character.scala

  @inline
  def digit(ch: scala.Char, radix: Int): Int =
    digit(ch.toInt, radix)

  @inline // because radix is probably constant at call site
  def digit(codePoint: Int, radix: Int): Int = {
    if (radix > MAX_RADIX || radix < MIN_RADIX)
      -1
    else
      digitWithValidRadix(codePoint, radix)
  }

  /** All the non-ASCII code points that map to the digit 0.
   *
   *  Each of them is directly followed by 9 other code points mapping to the
   *  digits 1 to 9, in order. Conversely, there are no other non-ASCII code
   *  point mapping to digits from 0 to 9.
   */
  private[this] lazy val nonASCIIZeroDigitCodePoints: Array[Int] = {
    Array[Int](0x660, 0x6f0, 0x7c0, 0x966, 0x9e6, 0xa66, 0xae6, 0xb66, 0xbe6,
      0xc66, 0xce6, 0xd66, 0xe50, 0xed0, 0xf20, 0x1040, 0x1090, 0x17e0, 0x1810,
      0x1946, 0x19d0, 0x1a80, 0x1a90, 0x1b50, 0x1bb0, 0x1c40, 0x1c50, 0xa620,
      0xa8d0, 0xa900, 0xa9d0, 0xaa50, 0xabf0, 0xff10, 0x104a0, 0x11066, 0x110f0,
      0x11136, 0x111d0, 0x116c0, 0x1d7ce, 0x1d7d8, 0x1d7e2, 0x1d7ec, 0x1d7f6)
  }

  private[lang] def digitWithValidRadix(codePoint: Int, radix: Int): Int = {
    val value = if (codePoint < 256) {
      // Fast-path for the ASCII repertoire
      if (codePoint >= '0' && codePoint <= '9')
        codePoint - '0'
      else if (codePoint >= 'A' && codePoint <= 'Z')
        codePoint - ('A' - 10)
      else if (codePoint >= 'a' && codePoint <= 'z')
        codePoint - ('a' - 10)
      else
        -1
    } else {
      if (codePoint >= 0xff21 && codePoint <= 0xff3a) {
        // Fullwidth uppercase Latin letter
        codePoint - (0xff21 - 10)
      } else if (codePoint >= 0xff41 && codePoint <= 0xff5a) {
        // Fullwidth lowercase Latin letter
        codePoint - (0xff41 - 10)
      } else {
        // Maybe it is a digit in a non-ASCII script

        // Find the position of the 0 digit corresponding to this code point
        val p                  = Arrays.binarySearch(nonASCIIZeroDigitCodePoints, codePoint)
        val zeroCodePointIndex = if (p < 0) -2 - p else p

        /* If the index is below 0, it cannot be a digit. Otherwise, the value
         * is the difference between the given codePoint and the code point of
         * its corresponding 0. We must ensure that it is not bigger than 9.
         */
        if (zeroCodePointIndex < 0) {
          -1
        } else {
          val v = codePoint - nonASCIIZeroDigitCodePoints(zeroCodePointIndex)
          if (v > 9) -1 else v
        }
      }
    }

    if (value < radix) value
    else -1
  }

  // ported from https://github.com/gwtproject/gwt/blob/master/user/super/com/google/gwt/emul/java/lang/Character.java
  def forDigit(digit: Int, radix: Int): Char = {
    if (radix < MIN_RADIX || radix > MAX_RADIX || digit < 0 || digit >= radix) {
      0
    } else {
      val overBaseTen = digit - 10
      val result      = if (overBaseTen < 0) '0' + digit else 'a' + overBaseTen
      result.toChar
    }
  }

  def isISOControl(c: scala.Char): scala.Boolean = isISOControl(c.toInt)

  def isISOControl(codePoint: Int): scala.Boolean = {
    (0x00 <= codePoint && codePoint <= 0x1F) || (0x7F <= codePoint && codePoint <= 0x9F)
  }

  @deprecated("Replaced by isWhitespace(char)", "")
  def isSpace(c: scala.Char): scala.Boolean =
    c == '\t' || c == '\n' || c == '\f' || c == '\r' || c == ' '

  def isWhitespace(c: scala.Char): scala.Boolean =
    isWhitespace(c.toInt)

  def isWhitespace(codePoint: scala.Int): scala.Boolean = {
    def isSeparator(tpe: Int): scala.Boolean =
      tpe == SPACE_SEPARATOR || tpe == LINE_SEPARATOR || tpe == PARAGRAPH_SEPARATOR
    if (codePoint < 256) {
      codePoint == '\t' || codePoint == '\n' || codePoint == '\u000B' ||
      codePoint == '\f' || codePoint == '\r' ||
      ('\u001C' <= codePoint && codePoint <= '\u001F') ||
      (codePoint != '\u00A0' && isSeparator(getTypeLT256(codePoint)))
    } else {
      (codePoint != '\u2007' && codePoint != '\u202F') &&
      isSeparator(getTypeGE256(codePoint))
    }
  }

  def isSpaceChar(ch: scala.Char): scala.Boolean =
    isSpaceChar(ch.toInt)

  def isSpaceChar(codePoint: Int): scala.Boolean =
    isSpaceCharImpl(getType(codePoint))

  @inline private[this] def isSpaceCharImpl(tpe: Int): scala.Boolean =
    tpe == SPACE_SEPARATOR || tpe == LINE_SEPARATOR || tpe == PARAGRAPH_SEPARATOR

  // --- UTF-16 surrogate pairs handling ---
  // See http://en.wikipedia.org/wiki/UTF-16

  private final val HighSurrogateMask       = 0xfc00 // 111111 00  00000000
  private final val HighSurrogateID         = 0xd800 // 110110 00  00000000
  private final val LowSurrogateMask        = 0xfc00 // 111111 00  00000000
  private final val LowSurrogateID          = 0xdc00 // 110111 00  00000000
  private final val SurrogateUsefulPartMask = 0x03ff // 000000 11  11111111

  @inline def isHighSurrogate(c: scala.Char): scala.Boolean =
    (c & HighSurrogateMask) == HighSurrogateID
  @inline def isLowSurrogate(c: scala.Char): scala.Boolean =
    (c & LowSurrogateMask) == LowSurrogateID
  @inline
  def isSurrogatePair(high: scala.Char, low: scala.Char): scala.Boolean =
    isHighSurrogate(high) && isLowSurrogate(low)
  @inline def isSurrogate(ch: scala.Char): scala.Boolean =
    ch >= MIN_SURROGATE && ch <= MAX_SURROGATE

  @inline def toCodePoint(high: scala.Char, low: scala.Char): Int =
    ((high & SurrogateUsefulPartMask) << 10) + (low & SurrogateUsefulPartMask) + 0x10000

  // --- End of UTF-16 surrogate pairs handling ---

  def isLowerCase(c: scala.Char): scala.Boolean =
    isLowerCase(c.toInt)

  def isLowerCase(c: Int): scala.Boolean = {
    if (c < 256)
      c == '\u00AA' || c == '\u00BA' || getTypeLT256(c) == LOWERCASE_LETTER
    else
      isLowerCaseGE256(c)
  }

  private[this] def isLowerCaseGE256(c: Int): scala.Boolean = {
    ('\u02B0' <= c && c <= '\u02B8') || ('\u02C0' <= c && c <= '\u02C1') ||
    ('\u02E0' <= c && c <= '\u02E4') || c == '\u0345' || c == '\u037A' ||
    ('\u1D2C' <= c && c <= '\u1D6A') || c == '\u1D78' ||
    ('\u1D9B' <= c && c <= '\u1DBF') || c == '\u2071' || c == '\u207F' ||
    ('\u2090' <= c && c <= '\u209C') || ('\u2170' <= c && c <= '\u217F') ||
    ('\u24D0' <= c && c <= '\u24E9') || ('\u2C7C' <= c && c <= '\u2C7D') ||
    c == '\uA770' || ('\uA7F8' <= c && c <= '\uA7F9') ||
    getTypeGE256(c) == LOWERCASE_LETTER
  }

  def isUpperCase(c: scala.Char): scala.Boolean =
    isUpperCase(c.toInt)

  def isUpperCase(c: Int): scala.Boolean = {
    ('\u2160' <= c && c <= '\u216F') || ('\u24B6' <= c && c <= '\u24CF') ||
    getType(c) == UPPERCASE_LETTER
  }

  @inline def isValidCodePoint(codePoint: Int): scala.Boolean =
    codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT

  @inline def isBmpCodePoint(codePoint: Int): scala.Boolean =
    codePoint >= MIN_VALUE && codePoint <= MAX_VALUE

  @inline def isSupplementaryCodePoint(codePoint: Int): scala.Boolean =
    codePoint >= MIN_SUPPLEMENTARY_CODE_POINT && codePoint <= MAX_CODE_POINT

  def isTitleCase(c: scala.Char): scala.Boolean =
    isTitleCase(c.toInt)

  def isTitleCase(cp: Int): scala.Boolean =
    if (cp < 256) false
    else isTitleCaseImpl(getTypeGE256(cp))

  @inline private[this] def isTitleCaseImpl(tpe: Int): scala.Boolean =
    tpe == TITLECASE_LETTER

  def isDigit(c: scala.Char): scala.Boolean =
    isDigit(c.toInt)

  def isDigit(cp: Int): scala.Boolean =
    if (cp < 256) '0' <= cp && cp <= '9'
    else isDigitImpl(getTypeGE256(cp))

  @inline private[this] def isDigitImpl(tpe: Int): scala.Boolean =
    tpe == DECIMAL_DIGIT_NUMBER

  def isDefined(c: scala.Char): scala.Boolean =
    isDefined(c.toInt)

  def isDefined(c: scala.Int): scala.Boolean = {
    if (c < 0) false
    else if (c < 888) true
    else getTypeGE256(c) != UNASSIGNED
  }

  def isLetter(c: scala.Char): scala.Boolean = isLetter(c.toInt)

  def isLetter(cp: Int): scala.Boolean = isLetterImpl(getType(cp))

  @inline private[this] def isLetterImpl(tpe: Int): scala.Boolean = {
    tpe == UPPERCASE_LETTER || tpe == LOWERCASE_LETTER ||
    tpe == TITLECASE_LETTER || tpe == MODIFIER_LETTER || tpe == OTHER_LETTER
  }

  def isLetterOrDigit(c: scala.Char): scala.Boolean =
    isLetterOrDigit(c.toInt)

  def isLetterOrDigit(cp: Int): scala.Boolean =
    isLetterOrDigitImpl(getType(cp))

  @inline private[this] def isLetterOrDigitImpl(tpe: Int): scala.Boolean =
    isDigitImpl(tpe) || isLetterImpl(tpe)

  def isJavaLetter(ch: scala.Char): scala.Boolean =
    isJavaLetterImpl(getType(ch))

  @inline private[this] def isJavaLetterImpl(tpe: Int): scala.Boolean = {
    isLetterImpl(tpe) || tpe == LETTER_NUMBER || tpe == CURRENCY_SYMBOL ||
    tpe == CONNECTOR_PUNCTUATION
  }

  def isJavaLetterOrDigit(ch: scala.Char): scala.Boolean =
    isJavaLetterOrDigitImpl(ch, getType(ch))

  @inline private[this] def isJavaLetterOrDigitImpl(codePoint: Int,
                                                    tpe: Int): scala.Boolean = {
    isJavaLetterImpl(tpe) || tpe == COMBINING_SPACING_MARK ||
    tpe == NON_SPACING_MARK || isIdentifierIgnorableImpl(codePoint, tpe)
  }

  def isAlphabetic(codePoint: Int): scala.Boolean = {
    val tpe = getType(codePoint)
    tpe == UPPERCASE_LETTER || tpe == LOWERCASE_LETTER ||
    tpe == TITLECASE_LETTER || tpe == MODIFIER_LETTER ||
    tpe == OTHER_LETTER || tpe == LETTER_NUMBER
  }

  def isIdeographic(c: Int): scala.Boolean = {
    (12294 <= c && c <= 12295) || (12321 <= c && c <= 12329) ||
    (12344 <= c && c <= 12346) || (13312 <= c && c <= 19893) ||
    (19968 <= c && c <= 40908) || (63744 <= c && c <= 64109) ||
    (64112 <= c && c <= 64217) || (131072 <= c && c <= 173782) ||
    (173824 <= c && c <= 177972) || (177984 <= c && c <= 178205) ||
    (194560 <= c && c <= 195101)
  }

  def isJavaIdentifierStart(ch: scala.Char): scala.Boolean =
    isJavaIdentifierStart(ch.toInt)

  def isJavaIdentifierStart(codePoint: Int): scala.Boolean =
    isJavaIdentifierStartImpl(getType(codePoint))

  @inline
  private[this] def isJavaIdentifierStartImpl(tpe: Int): scala.Boolean = {
    isLetterImpl(tpe) || tpe == LETTER_NUMBER || tpe == CURRENCY_SYMBOL ||
    tpe == CONNECTOR_PUNCTUATION
  }

  def isJavaIdentifierPart(ch: scala.Char): scala.Boolean =
    isJavaIdentifierPart(ch.toInt)

  def isJavaIdentifierPart(codePoint: Int): scala.Boolean =
    isJavaIdentifierPartImpl(codePoint, getType(codePoint))

  @inline private[this] def isJavaIdentifierPartImpl(
      codePoint: Int,
      tpe: Int): scala.Boolean = {
    isLetterImpl(tpe) || tpe == CURRENCY_SYMBOL ||
    tpe == CONNECTOR_PUNCTUATION || tpe == DECIMAL_DIGIT_NUMBER ||
    tpe == LETTER_NUMBER || tpe == COMBINING_SPACING_MARK ||
    tpe == NON_SPACING_MARK || isIdentifierIgnorableImpl(codePoint, tpe)
  }

  def isUnicodeIdentifierStart(ch: scala.Char): scala.Boolean =
    isUnicodeIdentifierStart(ch.toInt)

  def isUnicodeIdentifierStart(codePoint: Int): scala.Boolean =
    isUnicodeIdentifierStartImpl(getType(codePoint))

  @inline
  private[this] def isUnicodeIdentifierStartImpl(tpe: Int): scala.Boolean =
    isLetterImpl(tpe) || tpe == LETTER_NUMBER

  def isUnicodeIdentifierPart(ch: scala.Char): scala.Boolean =
    isUnicodeIdentifierPart(ch.toInt)

  def isUnicodeIdentifierPart(codePoint: Int): scala.Boolean =
    isUnicodeIdentifierPartImpl(codePoint, getType(codePoint))

  def isUnicodeIdentifierPartImpl(codePoint: Int, tpe: Int): scala.Boolean = {
    tpe == CONNECTOR_PUNCTUATION || tpe == DECIMAL_DIGIT_NUMBER ||
    tpe == COMBINING_SPACING_MARK || tpe == NON_SPACING_MARK ||
    isUnicodeIdentifierStartImpl(tpe) ||
    isIdentifierIgnorableImpl(codePoint, tpe)
  }

  def isIdentifierIgnorable(c: scala.Char): scala.Boolean =
    isIdentifierIgnorable(c.toInt)

  def isIdentifierIgnorable(codePoint: Int): scala.Boolean =
    isIdentifierIgnorableImpl(codePoint, getType(codePoint))

  @inline private[this] def isIdentifierIgnorableImpl(
      codePoint: Int,
      tpe: Int): scala.Boolean = {
    ('\u0000' <= codePoint && codePoint <= '\u0008') ||
    ('\u000E' <= codePoint && codePoint <= '\u001B') ||
    ('\u007F' <= codePoint && codePoint <= '\u009F') ||
    tpe == FORMAT
  }

  def isMirrored(c: scala.Char): scala.Boolean =
    isMirrored(c.toInt)

  def isMirrored(codePoint: Int): scala.Boolean = {
    val indexOfRange =
      findIndexOfRange(isMirroredIndices, codePoint, hasEmptyRanges = false)
    (indexOfRange & 1) != 0
  }

  /* Conversions */
  def toUpperCase(c: scala.Char): scala.Char = {
    toUpperCase(c.toInt).toChar
  }

  def toUpperCase(codePoint: Int): Int = {
    import CaseUtil._
    toCase(codePoint, a, z, lowerBeta, lowerRanges, lowerDeltas, lowerSteps)
  }

  def toLowerCase(c: scala.Char): scala.Char = {
    toLowerCase(c.toInt).toChar
  }

  def toLowerCase(codePoint: Int): Int = {
    import CaseUtil._
    toCase(codePoint, A, Z, upperMu, upperRanges, upperDeltas, upperSteps)
  }

  def toChars(codePoint: Int): Array[Char] = {
    if (!isValidCodePoint(codePoint))
      throw new IllegalArgumentException()

    if (isSupplementaryCodePoint(codePoint)) {
      val dst = new Array[Char](2)
      toSurrogate(codePoint, dst, 0)
      dst
    } else {
      Array(codePoint.toChar)
    }
  }

  def toChars(codePoint: Int, dst: Array[Char], dstIndex: Int): Int = {
    if (!isValidCodePoint(codePoint))
      throw new IllegalArgumentException()

    if (isSupplementaryCodePoint(codePoint)) {
      toSurrogate(codePoint, dst, dstIndex)
      2
    } else {
      dst(dstIndex) = codePoint.toChar
      1
    }
  }

  @inline private[this] def toSurrogate(codePoint: Int,
                                        dst: Array[Char],
                                        dstIndex: Int): Unit = {
    val cpPrime = codePoint - 0x10000
    val high    = 0xD800 | ((cpPrime >> 10) & 0x3FF)
    val low     = 0xDC00 | (cpPrime & 0x3FF)
    dst(dstIndex) = high.toChar
    dst(dstIndex + 1) = low.toChar
  }

  @inline def toString(c: scala.Char): String =
    String.valueOf(c)

  @inline def compare(x: scala.Char, y: scala.Char): Int =
    x - y

  // Based on Unicode 7.0.0

  // Scalafmt doesn't like long integer arrays, so we turn
  // it off for the arrays below.
  //
  // format: off

  // Types of characters from 0 to 255
  private[this] lazy val charTypesFirst256 = Array[scala.Byte](15, 15, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 12, 24, 24, 24, 26, 24, 24, 24,
    21, 22, 24, 25, 24, 20, 24, 24, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 24, 24, 25,
    25, 25, 24, 24, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 21, 24, 22, 27, 23, 27, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 21, 25, 22, 25, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
    15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 12, 24, 26, 26, 26,
    26, 28, 24, 27, 28, 5, 29, 25, 16, 28, 27, 28, 25, 11, 11, 27, 2, 24, 24,
    27, 11, 5, 30, 11, 11, 11, 24, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 25, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 25, 2, 2, 2, 2, 2, 2,
    2, 2)

  // Character type data by ranges of types
  // charTypeIndices: contains the index where the range ends
  // charType: contains the type of the carater in the range ends
  // note that charTypeIndices.length + 1 = charType.length and that the
  // range 0 to 255 is not included because it is contained in charTypesFirst256
  // They where generated with the following script:
  //
  //  val indicesAndTypes = (256 to Character.MAX_CODE_POINT)
  //    .map(i => (i, Character.getType(i)))
  //    .foldLeft[List[(Int, Int)]](Nil) {
  //      case (x :: xs, elem) if x._2 == elem._2 =>  x :: xs
  //      case (prevs, elem) => elem :: prevs
  //    }.reverse
  //  val charTypeIndices = indicesAndTypes.map(_._1).tail
  //  val charTypeIndicesDeltas = charTypeIndices.zip(0 :: charTypeIndices.init)
  //    .map(tup => tup._1 - tup._2)
  //  val charTypes = indicesAndTypes.map(_._2)
  //  println(charTypeIndicesDeltas.mkString(
  //    "charTypeIndices: val deltas = Array[Int](", ", ", ")"))
  //  println(charTypes.mkString("val charTypes = Array[scala.Byte](", ", ", ")"))
  //
  // format: off
  @noinline private def charTypeIndicesDeltas =
    Array[Int](257, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 3, 2, 1, 1, 1, 2, 1, 3,
      2, 4, 1, 2, 1, 3, 3, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1,
      3, 1, 1, 1, 2, 2, 1, 1, 3, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 7, 2, 1, 2, 2, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 69, 1, 27, 18,
      4, 12, 14, 5, 7, 1, 1, 1, 17, 112, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 3, 1,
      5, 2, 1, 1, 3, 1, 1, 1, 2, 1, 17, 1, 9, 35, 1, 2, 3, 3, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1, 1, 1, 1,
      1, 2, 2, 51, 48, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 2, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 38, 2, 1, 6, 1, 39, 1, 1,
      1, 4, 1, 1, 45, 1, 1, 1, 2, 1, 2, 1, 1, 8, 27, 5, 3, 2, 11, 5, 1, 3,
      2, 1, 2, 2, 11, 1, 2, 2, 32, 1, 10, 21, 10, 4, 2, 1, 99, 1, 1, 7, 1,
      1, 6, 2, 2, 1, 4, 2, 10, 3, 2, 1, 14, 1, 1, 1, 1, 30, 27, 2, 89, 11,
      1, 14, 10, 33, 9, 2, 1, 3, 1, 5, 22, 4, 1, 9, 1, 3, 1, 5, 2, 15, 1,
      25, 3, 2, 1, 65, 1, 1, 11, 55, 27, 1, 3, 1, 54, 1, 1, 1, 1, 3, 8, 4,
      1, 2, 1, 7, 10, 2, 2, 10, 1, 1, 6, 1, 7, 1, 1, 2, 1, 8, 2, 2, 2, 22,
      1, 7, 1, 1, 3, 4, 2, 1, 1, 3, 4, 2, 2, 2, 2, 1, 1, 8, 1, 4, 2, 1, 3,
      2, 2, 10, 2, 2, 6, 1, 1, 5, 2, 1, 1, 6, 4, 2, 2, 22, 1, 7, 1, 2, 1, 2,
      1, 2, 2, 1, 1, 3, 2, 4, 2, 2, 3, 3, 1, 7, 4, 1, 1, 7, 10, 2, 3, 1, 11,
      2, 1, 1, 9, 1, 3, 1, 22, 1, 7, 1, 2, 1, 5, 2, 1, 1, 3, 5, 1, 2, 1, 1,
      2, 1, 2, 1, 15, 2, 2, 2, 10, 1, 1, 15, 1, 2, 1, 8, 2, 2, 2, 22, 1, 7,
      1, 2, 1, 5, 2, 1, 1, 1, 1, 1, 4, 2, 2, 2, 2, 1, 8, 1, 1, 4, 2, 1, 3,
      2, 2, 10, 1, 1, 6, 10, 1, 1, 1, 6, 3, 3, 1, 4, 3, 2, 1, 1, 1, 2, 3, 2,
      3, 3, 3, 12, 4, 2, 1, 2, 3, 3, 1, 3, 1, 2, 1, 6, 1, 14, 10, 3, 6, 1,
      1, 6, 3, 1, 8, 1, 3, 1, 23, 1, 10, 1, 5, 3, 1, 3, 4, 1, 3, 1, 4, 7, 2,
      1, 2, 6, 2, 2, 2, 10, 8, 7, 1, 2, 2, 1, 8, 1, 3, 1, 23, 1, 10, 1, 5,
      2, 1, 1, 1, 1, 5, 1, 1, 2, 1, 2, 2, 7, 2, 7, 1, 1, 2, 2, 2, 10, 1, 2,
      15, 2, 1, 8, 1, 3, 1, 41, 2, 1, 3, 4, 1, 3, 1, 3, 1, 1, 8, 1, 8, 2, 2,
      2, 10, 6, 3, 1, 6, 2, 2, 1, 18, 3, 24, 1, 9, 1, 1, 2, 7, 3, 1, 4, 3,
      3, 1, 1, 1, 8, 18, 2, 1, 12, 48, 1, 2, 7, 4, 1, 6, 1, 8, 1, 10, 2, 37,
      2, 1, 1, 2, 2, 1, 1, 2, 1, 6, 4, 1, 7, 1, 3, 1, 1, 1, 1, 2, 2, 1, 4,
      1, 2, 6, 1, 2, 1, 2, 5, 1, 1, 1, 6, 2, 10, 2, 4, 32, 1, 3, 15, 1, 1,
      3, 2, 6, 10, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 8, 1, 36, 4, 14, 1,
      5, 1, 2, 5, 11, 1, 36, 1, 8, 1, 6, 1, 2, 5, 4, 2, 37, 43, 2, 4, 1, 6,
      1, 2, 2, 2, 1, 10, 6, 6, 2, 2, 4, 3, 1, 3, 2, 7, 3, 4, 13, 1, 2, 2, 6,
      1, 1, 1, 10, 3, 1, 2, 38, 1, 1, 5, 1, 2, 43, 1, 1, 332, 1, 4, 2, 7, 1,
      1, 1, 4, 2, 41, 1, 4, 2, 33, 1, 4, 2, 7, 1, 1, 1, 4, 2, 15, 1, 57, 1,
      4, 2, 67, 2, 3, 9, 20, 3, 16, 10, 6, 85, 11, 1, 620, 2, 17, 1, 26, 1,
      1, 3, 75, 3, 3, 15, 13, 1, 4, 3, 11, 18, 3, 2, 9, 18, 2, 12, 13, 1, 3,
      1, 2, 12, 52, 2, 1, 7, 8, 1, 2, 11, 3, 1, 3, 1, 1, 1, 2, 10, 6, 10, 6,
      6, 1, 4, 3, 1, 1, 10, 6, 35, 1, 52, 8, 41, 1, 1, 5, 70, 10, 29, 3, 3,
      4, 2, 3, 4, 2, 1, 6, 3, 4, 1, 3, 2, 10, 30, 2, 5, 11, 44, 4, 17, 7, 2,
      6, 10, 1, 3, 34, 23, 2, 3, 2, 2, 53, 1, 1, 1, 7, 1, 1, 1, 1, 2, 8, 6,
      10, 2, 1, 10, 6, 10, 6, 7, 1, 6, 82, 4, 1, 47, 1, 1, 5, 1, 1, 5, 1, 2,
      7, 4, 10, 7, 10, 9, 9, 3, 2, 1, 30, 1, 4, 2, 2, 1, 1, 2, 2, 10, 44, 1,
      1, 2, 3, 1, 1, 3, 2, 8, 4, 36, 8, 8, 2, 2, 3, 5, 10, 3, 3, 10, 30, 6,
      2, 64, 8, 8, 3, 1, 13, 1, 7, 4, 1, 4, 2, 1, 2, 9, 44, 63, 13, 1, 34,
      37, 39, 21, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9,
      8, 6, 2, 6, 2, 8, 8, 8, 8, 6, 2, 6, 2, 8, 1, 1, 1, 1, 1, 1, 1, 1, 8,
      8, 14, 2, 8, 8, 8, 8, 8, 8, 5, 1, 2, 4, 1, 1, 1, 3, 3, 1, 2, 4, 1, 3,
      4, 2, 2, 4, 1, 3, 8, 5, 3, 2, 3, 1, 2, 4, 1, 2, 1, 11, 5, 6, 2, 1, 1,
      1, 2, 1, 1, 1, 8, 1, 1, 5, 1, 9, 1, 1, 4, 2, 3, 1, 1, 1, 11, 1, 1, 1,
      10, 1, 5, 5, 6, 1, 1, 2, 6, 3, 1, 1, 1, 10, 3, 1, 1, 1, 13, 3, 27, 21,
      13, 4, 1, 3, 12, 15, 2, 1, 4, 1, 2, 1, 3, 2, 3, 1, 1, 1, 2, 1, 5, 6,
      1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 4, 1, 2, 2, 2, 5, 1, 4, 1, 1, 2, 1,
      1, 16, 35, 1, 1, 4, 1, 6, 5, 5, 2, 4, 1, 2, 1, 2, 1, 7, 1, 31, 2, 2,
      1, 1, 1, 31, 268, 8, 4, 20, 2, 7, 1, 1, 81, 1, 30, 25, 40, 6, 18, 12,
      39, 25, 11, 21, 60, 78, 22, 183, 1, 9, 1, 54, 8, 111, 1, 144, 1, 103,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 30, 44, 5, 1, 1, 31, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 16, 256, 131, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 63, 1, 1, 1, 1, 32, 1, 1, 258, 48,
      21, 2, 6, 3, 10, 166, 47, 1, 47, 1, 1, 1, 3, 2, 1, 1, 1, 1, 1, 1, 4,
      1, 1, 2, 1, 6, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 6, 1, 1, 1, 1, 3, 1, 1, 5,
      4, 1, 2, 38, 1, 1, 5, 1, 2, 56, 7, 1, 1, 14, 1, 23, 9, 7, 1, 7, 1, 7,
      1, 7, 1, 7, 1, 7, 1, 7, 1, 7, 1, 32, 2, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1,
      9, 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1, 10, 2, 68,
      26, 1, 89, 12, 214, 26, 12, 4, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 9, 4, 2, 1, 5, 2, 3,
      1, 1, 1, 2, 1, 86, 2, 2, 2, 2, 1, 1, 90, 1, 3, 1, 5, 41, 3, 94, 1, 2,
      4, 10, 27, 5, 36, 12, 16, 31, 1, 10, 30, 8, 1, 15, 32, 10, 39, 15, 63,
      1, 256, 6582, 10, 64, 20941, 51, 21, 1, 1143, 3, 55, 9, 40, 6, 2, 268,
      1, 3, 16, 10, 2, 20, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7, 1, 70, 10, 2, 6, 8,
      23, 9, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 77, 2, 1, 7, 1, 3, 1, 4, 1, 23, 2, 2, 1, 4, 4, 6, 2, 1, 1, 6,
      52, 4, 8, 2, 50, 16, 1, 9, 2, 10, 6, 18, 6, 3, 1, 4, 10, 28, 8, 2, 23,
      11, 2, 11, 1, 29, 3, 3, 1, 47, 1, 2, 4, 2, 1, 4, 13, 1, 1, 10, 4, 2,
      32, 41, 6, 2, 2, 2, 2, 9, 3, 1, 8, 1, 1, 2, 10, 2, 4, 16, 1, 6, 3, 1,
      1, 4, 48, 1, 1, 3, 2, 2, 5, 2, 1, 1, 1, 24, 2, 1, 2, 11, 1, 2, 2, 2,
      1, 2, 1, 1, 10, 6, 2, 6, 2, 6, 9, 7, 1, 7, 145, 35, 2, 1, 2, 1, 2, 1,
      1, 1, 2, 10, 6, 11172, 12, 23, 4, 49, 4, 2048, 6400, 366, 2, 106, 38,
      7, 12, 5, 5, 1, 1, 10, 1, 13, 1, 5, 1, 1, 1, 2, 1, 2, 1, 108, 16, 17,
      363, 1, 1, 16, 64, 2, 54, 40, 12, 1, 1, 2, 16, 7, 1, 1, 1, 6, 7, 9, 1,
      2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 4, 3,
      3, 1, 4, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 2, 4, 5, 1, 135, 2,
      1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 2, 10, 2, 3, 2, 26, 1, 1, 1, 1, 1, 1,
      26, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 10, 1, 45, 2, 31, 3, 6, 2, 6, 2, 6,
      2, 3, 3, 2, 1, 1, 1, 2, 1, 1, 4, 2, 10, 3, 2, 2, 12, 1, 26, 1, 19, 1,
      2, 1, 15, 2, 14, 34, 123, 5, 3, 4, 45, 3, 9, 53, 4, 17, 1, 5, 12, 52,
      45, 1, 130, 29, 3, 49, 47, 31, 1, 4, 12, 17, 1, 8, 1, 53, 30, 1, 1,
      36, 4, 8, 1, 5, 42, 40, 40, 78, 2, 10, 854, 6, 2, 1, 1, 44, 1, 2, 3,
      1, 2, 23, 1, 1, 8, 160, 22, 6, 3, 1, 26, 5, 1, 64, 56, 6, 2, 64, 1, 3,
      1, 2, 5, 4, 4, 1, 3, 1, 27, 4, 3, 4, 1, 8, 8, 9, 7, 29, 2, 1, 128, 54,
      3, 7, 22, 2, 8, 19, 5, 8, 128, 73, 535, 31, 385, 1, 1, 1, 53, 15, 7,
      4, 20, 10, 16, 2, 1, 45, 3, 4, 2, 2, 2, 1, 4, 14, 25, 7, 10, 6, 3, 36,
      5, 1, 8, 1, 10, 4, 60, 2, 1, 48, 3, 9, 2, 4, 4, 7, 10, 1190, 43, 1, 1,
      1, 2, 6, 1, 1, 8, 10, 2358, 879, 145, 99, 13, 4, 2956, 1071, 13265,
      569, 1223, 69, 11, 1, 46, 16, 4, 13, 16480, 2, 8190, 246, 10, 39, 2,
      60, 2, 3, 3, 6, 8, 8, 2, 7, 30, 4, 48, 34, 66, 3, 1, 186, 87, 9, 18,
      142, 26, 26, 26, 7, 1, 18, 26, 26, 1, 1, 2, 2, 1, 2, 2, 2, 4, 1, 8, 4,
      1, 1, 1, 7, 1, 11, 26, 26, 2, 1, 4, 2, 8, 1, 7, 1, 26, 2, 1, 4, 1, 5,
      1, 1, 3, 7, 1, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 28, 2,
      25, 1, 25, 1, 6, 25, 1, 25, 1, 6, 25, 1, 25, 1, 6, 25, 1, 25, 1, 6,
      25, 1, 25, 1, 6, 1, 1, 2, 50, 5632, 4, 1, 27, 1, 2, 1, 1, 2, 1, 1, 10,
      1, 4, 1, 1, 1, 1, 6, 1, 4, 1, 1, 1, 1, 1, 1, 3, 1, 2, 1, 1, 2, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 4, 1, 7, 1, 4, 1, 4, 1, 1, 1, 10,
      1, 17, 5, 3, 1, 5, 1, 17, 52, 2, 270, 44, 4, 100, 12, 15, 2, 14, 2,
      15, 1, 15, 32, 11, 5, 31, 1, 60, 4, 43, 75, 29, 13, 43, 5, 9, 7, 2,
      174, 33, 15, 6, 1, 70, 3, 20, 12, 37, 1, 5, 21, 17, 15, 63, 1, 1, 1,
      182, 1, 4, 3, 62, 2, 4, 12, 24, 147, 70, 4, 11, 48, 70, 58, 116, 2188,
      42711, 41, 4149, 11, 222, 16354, 542, 722403, 1, 30, 96, 128, 240,
      65040, 65534, 2, 65534)
  private[this] lazy val charTypeIndices =
    uncompressDeltas(charTypeIndicesDeltas)

  private[this] lazy val charTypes = Array[scala.Byte](1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 5, 1, 2, 5, 1, 3, 2,
    1, 3, 2, 1, 3, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 3, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 5, 2, 4, 27, 4, 27, 4, 27, 4, 27, 4, 27, 6, 1, 2, 1,
    2, 4, 27, 1, 2, 0, 4, 2, 24, 0, 27, 1, 24, 1, 0, 1, 0, 1, 2, 1, 0, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 25, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 28, 6, 7, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 0, 1, 0, 4, 24, 0, 2, 0, 24, 20, 0, 26, 0, 6, 20, 6, 24, 6, 24, 6,
    24, 6, 0, 5, 0, 5, 24, 0, 16, 0, 25, 24, 26, 24, 28, 6, 24, 0, 24, 5,
    4, 5, 6, 9, 24, 5, 6, 5, 24, 5, 6, 16, 28, 6, 4, 6, 28, 6, 5, 9, 5,
    28, 5, 24, 0, 16, 5, 6, 5, 6, 0, 5, 6, 5, 0, 9, 5, 6, 4, 28, 24, 4, 0,
    5, 6, 4, 6, 4, 6, 4, 6, 0, 24, 0, 5, 6, 0, 24, 0, 5, 0, 5, 0, 6, 0, 6,
    8, 5, 6, 8, 6, 5, 8, 6, 8, 6, 8, 5, 6, 5, 6, 24, 9, 24, 4, 5, 0, 5, 0,
    6, 8, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 6, 5, 8, 6, 0, 8, 0, 8,
    6, 5, 0, 8, 0, 5, 0, 5, 6, 0, 9, 5, 26, 11, 28, 26, 0, 6, 8, 0, 5, 0,
    5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 6, 0, 8, 6, 0, 6, 0, 6, 0, 6, 0,
    5, 0, 5, 0, 9, 6, 5, 6, 0, 6, 8, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 6, 5, 8, 6, 0, 6, 8, 0, 8, 6, 0, 5, 0, 5, 6, 0, 9, 24, 26, 0, 6, 8,
    0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 6, 5, 8, 6, 8, 6, 0, 8, 0, 8,
    6, 0, 6, 8, 0, 5, 0, 5, 6, 0, 9, 28, 5, 11, 0, 6, 5, 0, 5, 0, 5, 0, 5,
    0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 8, 6, 8, 0, 8, 0, 8, 6, 0, 5,
    0, 8, 0, 9, 11, 28, 26, 28, 0, 8, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5,
    6, 8, 0, 6, 0, 6, 0, 6, 0, 5, 0, 5, 6, 0, 9, 0, 11, 28, 0, 8, 0, 5, 0,
    5, 0, 5, 0, 5, 0, 5, 0, 6, 5, 8, 6, 8, 0, 6, 8, 0, 8, 6, 0, 8, 0, 5,
    0, 5, 6, 0, 9, 0, 5, 0, 8, 0, 5, 0, 5, 0, 5, 0, 5, 8, 6, 0, 8, 0, 8,
    6, 5, 0, 8, 0, 5, 6, 0, 9, 11, 0, 28, 5, 0, 8, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 5, 0, 6, 0, 8, 6, 0, 6, 0, 8, 0, 8, 24, 0, 5, 6, 5, 6, 0, 26, 5, 4,
    6, 24, 9, 24, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0,
    5, 0, 5, 0, 5, 6, 5, 6, 0, 6, 5, 0, 5, 0, 4, 0, 6, 0, 9, 0, 5, 0, 5,
    28, 24, 28, 24, 28, 6, 28, 9, 11, 28, 6, 28, 6, 28, 6, 21, 22, 21, 22,
    8, 5, 0, 5, 0, 6, 8, 6, 24, 6, 5, 6, 0, 6, 0, 28, 6, 28, 0, 28, 24,
    28, 24, 0, 5, 8, 6, 8, 6, 8, 6, 8, 6, 5, 9, 24, 5, 8, 6, 5, 6, 5, 8,
    5, 8, 5, 6, 5, 6, 8, 6, 8, 6, 5, 8, 9, 8, 6, 28, 1, 0, 1, 0, 1, 0, 5,
    24, 4, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 6, 24, 11, 0, 5, 28, 0, 5, 0, 20, 5,
    24, 5, 12, 5, 21, 22, 0, 5, 24, 10, 0, 5, 0, 5, 6, 0, 5, 6, 24, 0, 5,
    6, 0, 5, 0, 5, 0, 6, 0, 5, 6, 8, 6, 8, 6, 8, 6, 24, 4, 24, 26, 5, 6,
    0, 9, 0, 11, 0, 24, 20, 24, 6, 12, 0, 9, 0, 5, 4, 5, 0, 5, 6, 5, 0, 5,
    0, 5, 0, 6, 8, 6, 8, 0, 8, 6, 8, 6, 0, 28, 0, 24, 9, 5, 0, 5, 0, 5, 0,
    8, 5, 8, 0, 9, 11, 0, 28, 5, 6, 8, 0, 24, 5, 8, 6, 8, 6, 0, 6, 8, 6,
    8, 6, 8, 6, 0, 6, 9, 0, 9, 0, 24, 4, 24, 0, 6, 8, 5, 6, 8, 6, 8, 6, 8,
    6, 8, 5, 0, 9, 24, 28, 6, 28, 0, 6, 8, 5, 8, 6, 8, 6, 8, 6, 8, 5, 9,
    5, 6, 8, 6, 8, 6, 8, 6, 8, 0, 24, 5, 8, 6, 8, 6, 0, 24, 9, 0, 5, 9, 5,
    4, 24, 0, 24, 0, 6, 24, 6, 8, 6, 5, 6, 5, 8, 6, 5, 0, 2, 4, 2, 4, 2,
    4, 6, 0, 6, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 0, 1, 0, 2, 1, 2, 1, 2, 0, 1, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 2, 1,
    2, 0, 2, 3, 2, 3, 2, 3, 2, 0, 2, 1, 3, 27, 2, 27, 2, 0, 2, 1, 3, 27,
    2, 0, 2, 1, 0, 27, 2, 1, 27, 0, 2, 0, 2, 1, 3, 27, 0, 12, 16, 20, 24,
    29, 30, 21, 29, 30, 21, 29, 24, 13, 14, 16, 12, 24, 29, 30, 24, 23,
    24, 25, 21, 22, 24, 25, 24, 23, 24, 12, 16, 0, 16, 11, 4, 0, 11, 25,
    21, 22, 4, 11, 25, 21, 22, 0, 4, 0, 26, 0, 6, 7, 6, 7, 6, 0, 28, 1,
    28, 1, 28, 2, 1, 2, 1, 2, 28, 1, 28, 25, 1, 28, 1, 28, 1, 28, 1, 28,
    1, 28, 2, 1, 2, 5, 2, 28, 2, 1, 25, 1, 2, 28, 25, 28, 2, 28, 11, 10,
    1, 2, 10, 11, 0, 25, 28, 25, 28, 25, 28, 25, 28, 25, 28, 25, 28, 25,
    28, 25, 28, 25, 28, 25, 28, 25, 28, 25, 28, 21, 22, 28, 25, 28, 25,
    28, 25, 28, 0, 28, 0, 28, 0, 11, 28, 11, 28, 25, 28, 25, 28, 25, 28,
    25, 28, 0, 28, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22,
    11, 28, 25, 21, 22, 25, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 25,
    28, 25, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21,
    22, 21, 22, 21, 22, 21, 22, 25, 21, 22, 21, 22, 25, 21, 22, 25, 28,
    25, 28, 25, 0, 28, 0, 1, 0, 2, 0, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 4, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 28, 1, 2, 1, 2, 6, 1, 2, 0, 24,
    11, 24, 2, 0, 2, 0, 2, 0, 5, 0, 4, 24, 0, 6, 5, 0, 5, 0, 5, 0, 5, 0,
    5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 6, 24, 29, 30, 29, 30, 24, 29, 30, 24,
    29, 30, 24, 20, 24, 20, 24, 29, 30, 24, 29, 30, 21, 22, 21, 22, 21,
    22, 21, 22, 24, 4, 24, 20, 0, 28, 0, 28, 0, 28, 0, 28, 0, 12, 24, 28,
    4, 5, 10, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 28, 21, 22, 21, 22,
    21, 22, 21, 22, 20, 21, 22, 28, 10, 6, 8, 20, 4, 28, 10, 4, 5, 24, 28,
    0, 5, 0, 6, 27, 4, 5, 20, 5, 24, 4, 5, 0, 5, 0, 5, 0, 28, 11, 28, 5,
    0, 28, 0, 5, 28, 0, 11, 28, 11, 28, 11, 28, 11, 28, 11, 28, 0, 28, 5,
    0, 28, 5, 0, 5, 4, 5, 0, 28, 0, 5, 4, 24, 5, 4, 24, 5, 9, 5, 0, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 5, 6,
    7, 24, 6, 24, 4, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 0, 6, 5, 10, 6, 24, 0, 27, 4, 27, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1,
    2, 4, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 4, 27, 1, 2, 1, 2,
    0, 1, 2, 1, 2, 0, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 0, 4, 2, 5, 6, 5,
    6, 5, 6, 5, 8, 6, 8, 28, 0, 11, 28, 26, 28, 0, 5, 24, 0, 8, 5, 8, 6,
    0, 24, 9, 0, 6, 5, 24, 5, 0, 9, 5, 6, 24, 5, 6, 8, 0, 24, 5, 0, 6, 8,
    5, 6, 8, 6, 8, 6, 8, 24, 0, 4, 9, 0, 24, 0, 5, 6, 8, 6, 8, 6, 0, 5, 6,
    5, 6, 8, 0, 9, 0, 24, 5, 4, 5, 28, 5, 8, 0, 5, 6, 5, 6, 5, 6, 5, 6, 5,
    6, 5, 0, 5, 4, 24, 5, 8, 6, 8, 24, 5, 4, 8, 6, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 5, 0, 5, 8, 6, 8, 6, 8, 24, 8, 6, 0, 9, 0, 5, 0, 5, 0, 5, 0, 19,
    18, 5, 0, 5, 0, 2, 0, 2, 0, 5, 6, 5, 25, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0,
    5, 27, 0, 5, 21, 22, 0, 5, 0, 5, 0, 5, 26, 28, 0, 6, 24, 21, 22, 24,
    0, 6, 0, 24, 20, 23, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22,
    21, 22, 21, 22, 24, 21, 22, 24, 23, 24, 0, 24, 20, 21, 22, 21, 22, 21,
    22, 24, 25, 20, 25, 0, 24, 26, 24, 0, 5, 0, 5, 0, 16, 0, 24, 26, 24,
    21, 22, 24, 25, 24, 20, 24, 9, 24, 25, 24, 1, 21, 24, 22, 27, 23, 27,
    2, 21, 25, 22, 25, 21, 22, 24, 21, 22, 24, 5, 4, 5, 4, 5, 0, 5, 0, 5,
    0, 5, 0, 5, 0, 26, 25, 27, 28, 26, 0, 28, 25, 28, 0, 16, 28, 0, 5, 0,
    5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 24, 0, 11, 0, 28, 10, 11, 28, 11,
    0, 28, 0, 28, 6, 0, 5, 0, 5, 0, 5, 0, 11, 0, 5, 10, 5, 10, 0, 5, 0,
    24, 5, 0, 5, 24, 10, 0, 1, 2, 5, 0, 9, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 5, 0, 24, 11, 0, 5, 11, 0, 24, 5, 0, 24, 0, 5, 0, 5, 0, 5, 6, 0, 6,
    0, 6, 5, 0, 5, 0, 5, 0, 6, 0, 6, 11, 0, 24, 0, 5, 11, 24, 0, 5, 0, 24,
    5, 0, 11, 5, 0, 11, 0, 5, 0, 11, 0, 8, 6, 8, 5, 6, 24, 0, 11, 9, 0, 6,
    8, 5, 8, 6, 8, 6, 24, 16, 24, 0, 5, 0, 9, 0, 6, 5, 6, 8, 6, 0, 9, 24,
    0, 6, 8, 5, 8, 6, 8, 5, 24, 0, 9, 0, 5, 6, 8, 6, 8, 6, 8, 6, 0, 9, 0,
    5, 0, 10, 0, 24, 0, 5, 0, 5, 0, 5, 0, 5, 8, 0, 6, 4, 0, 5, 0, 28, 0,
    28, 0, 28, 8, 6, 28, 8, 16, 6, 28, 6, 28, 6, 28, 0, 28, 6, 28, 0, 28,
    0, 11, 0, 1, 2, 1, 2, 0, 2, 1, 2, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 2,
    0, 2, 0, 2, 0, 2, 1, 2, 1, 0, 1, 0, 1, 0, 1, 0, 2, 1, 0, 1, 0, 1, 0,
    1, 0, 1, 0, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0, 1, 25, 2, 25, 2,
    1, 25, 2, 25, 2, 1, 25, 2, 25, 2, 1, 25, 2, 25, 2, 1, 25, 2, 25, 2, 1,
    2, 0, 9, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0,
    5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5,
    0, 25, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 11, 0, 28, 0, 28,
    0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28,
    0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28, 0, 28,
    0, 28, 0, 28, 0, 28, 0, 5, 0, 5, 0, 5, 0, 5, 0, 16, 0, 16, 0, 6, 0,
    18, 0, 18, 0)

  // Indices representing the start of ranges of codePoint that have the same
  // `isMirrored` result. It is true for the first range
  // (i.e. isMirrored(40)==true, isMirrored(41)==true, isMirrored(42)==false)
  // They where generated with the following script:
  //
  //  val indicesAndRes = (0 to Character.MAX_CODE_POINT)
  //    .map(i => (i, Character.isMirrored(i))).foldLeft[List[(Int, Boolean)]](Nil) {
  //      case (x :: xs, elem) if x._2 == elem._2 =>  x :: xs
  //      case (prevs, elem) => elem :: prevs
  //    }.reverse
  //  val isMirroredIndices = indicesAndRes.map(_._1).tail
  //  val isMirroredIndicesDeltas = isMirroredIndices.zip(
  //       0 :: isMirroredIndices.init).map(tup => tup._1 - tup._2)
  //  println(isMirroredIndicesDeltas.mkString(
  //     "isMirroredIndices: val deltas = Array[Int](", ", ", ")"))
  @noinline private def isMirroredIndicesDeltas =
    Array[Int](40, 2, 18, 1, 1, 1, 28, 1, 1, 1, 29, 1, 1, 1,
      45, 1, 15, 1, 3710, 4, 1885, 2, 2460, 2, 10, 2, 54, 2, 14, 2, 177, 1,
      192, 4, 3, 6, 3, 1, 3, 2, 3, 4, 1, 4, 1, 1, 1, 1, 4, 9, 5, 1, 1, 18,
      5, 4, 9, 2, 1, 1, 1, 8, 2, 31, 2, 4, 5, 1, 9, 2, 2, 19, 5, 2, 9, 5, 2,
      2, 4, 24, 2, 16, 8, 4, 20, 2, 7, 2, 1085, 14, 74, 1, 2, 4, 1, 2, 1, 3,
      5, 4, 5, 3, 3, 14, 403, 22, 2, 21, 8, 1, 7, 6, 3, 1, 4, 5, 1, 2, 2, 5,
      4, 1, 1, 3, 2, 2, 10, 6, 2, 2, 12, 19, 1, 4, 2, 1, 1, 1, 2, 1, 1, 4,
      5, 2, 6, 3, 24, 2, 11, 2, 4, 4, 1, 2, 2, 2, 4, 43, 2, 8, 1, 40, 5, 1,
      1, 1, 3, 5, 5, 3, 4, 1, 3, 5, 1, 1, 772, 4, 3, 2, 1, 2, 14, 2, 2, 10,
      478, 10, 2, 8, 52797, 6, 5, 2, 162, 2, 18, 1, 1, 1, 28, 1, 1, 1, 29,
      1, 1, 1, 1, 2, 1, 2, 55159, 1, 57, 1, 57, 1, 57, 1, 57, 1)
  private[this] lazy val isMirroredIndices =
    uncompressDeltas(isMirroredIndicesDeltas)

  private[lang] final val CombiningClassIsNone = 0
  private[lang] final val CombiningClassIsAbove = 1
  private[lang] final val CombiningClassIsOther = 2
  
  /* Ported from Scala.js, commit: ac38a148, dated: 2020-09-25
   * Indices representing the start of ranges of codePoint that have the same
   * `combiningClassNoneOrAboveOrOther` result. The results cycle modulo 3 at
   * every range:
   *
   * - 0 for the range [0, array(0))
   * - 1 for the range [array(0), array(1))
   * - 2 for the range [array(1), array(2))
   * - 0 for the range [array(2), array(3))
   * - etc.
   *
   * In general, for a range ending at `array(i)` (excluded), the result is
   * `i % 3`.
   *
   * A range can be empty, i.e., it can happen that `array(i) == array(i + 1)`
   * (but then it is different from `array(i - 1)` and `array(i + 2)`).
   *
   * They where generated with the following script, which can be pasted into
   * a Scala REPL.

  val url = new java.net.URL("http://unicode.org/Public/UCD/latest/ucd/UnicodeData.txt")
  val cpToValue = scala.io.Source.fromURL(url, "UTF-8")
    .getLines()
    .filter(!_.startsWith("#"))
    .map(_.split(';'))
    .map { arr =>
      val cp = Integer.parseInt(arr(0), 16)
      val value = arr(3).toInt match {
        case 0   => 0
        case 230 => 1
        case _   => 2
      }
      cp -> value
    }
    .toMap
    .withDefault(_ => 0)

  var lastValue = 0
  val indicesBuilder = List.newBuilder[Int]
  for (cp <- 0 to Character.MAX_CODE_POINT) {
    val value = cpToValue(cp)
    while (lastValue != value) {
      indicesBuilder += cp
      lastValue = (lastValue + 1) % 3
    }
  }
  val indices = indicesBuilder.result()

  val indicesDeltas = indices
    .zip(0 :: indices.init)
    .map(tup => tup._1 - tup._2)
  println("combiningClassNoneOrAboveOrOtherIndices, deltas:")
  println("    Array(")
  println(formatLargeArray(indicesDeltas.toArray, "        "))
  println("    )")
   */
  private[this] lazy val combiningClassNoneOrAboveOrOtherIndices: Array[Int] = {
    val deltas = Array(
      768, 21, 40, 0, 8, 1, 0, 1, 3, 0, 3, 2, 1, 3, 4, 0, 1, 3, 0, 1, 7, 0,
      13, 0, 275, 5, 0, 265, 0, 1, 0, 4, 1, 0, 3, 2, 0, 6, 6, 0, 2, 1, 0, 2,
      2, 0, 1, 14, 1, 0, 1, 1, 0, 2, 1, 1, 1, 1, 0, 1, 72, 8, 3, 48, 0, 8, 0,
      2, 2, 0, 5, 1, 0, 2, 1, 16, 0, 1, 101, 7, 0, 2, 4, 1, 0, 1, 0, 2, 2, 0,
      1, 0, 1, 0, 2, 1, 35, 0, 1, 30, 1, 1, 0, 2, 1, 0, 2, 3, 0, 1, 2, 0, 1,
      1, 0, 3, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 2, 0, 160, 7, 1, 0, 1, 0, 9,
      0, 1, 24, 4, 0, 1, 9, 0, 1, 3, 0, 1, 5, 0, 43, 0, 3, 119, 0, 1, 0, 14,
      0, 1, 0, 1, 0, 2, 1, 0, 2, 1, 0, 3, 6, 0, 3, 1, 0, 2, 2, 0, 5, 0, 60,
      0, 1, 16, 0, 1, 3, 1, 1, 0, 2, 0, 103, 0, 1, 16, 0, 1, 48, 1, 0, 61, 0,
      1, 16, 0, 1, 110, 0, 1, 16, 0, 1, 110, 0, 1, 16, 0, 1, 127, 0, 1, 127,
      0, 1, 7, 0, 2, 101, 0, 1, 16, 0, 1, 109, 0, 2, 16, 0, 1, 124, 0, 1,
      109, 0, 3, 13, 0, 4, 108, 0, 3, 13, 0, 4, 76, 0, 2, 27, 0, 1, 1, 0, 1,
      1, 0, 1, 55, 0, 2, 1, 0, 1, 5, 0, 4, 2, 0, 1, 1, 2, 1, 1, 2, 0, 62, 0,
      1, 112, 0, 1, 1, 0, 2, 82, 0, 1, 719, 3, 0, 948, 0, 1, 31, 0, 1, 157,
      0, 1, 10, 1, 0, 203, 0, 1, 143, 0, 1, 0, 1, 1, 219, 1, 1, 71, 0, 1, 20,
      8, 0, 2, 0, 1, 48, 5, 6, 0, 2, 1, 1, 0, 2, 115, 0, 1, 15, 0, 1, 38, 1,
      1, 0, 7, 0, 54, 0, 2, 58, 0, 1, 11, 0, 2, 67, 0, 1, 152, 3, 0, 1, 0, 6,
      0, 2, 4, 0, 1, 0, 1, 0, 7, 4, 0, 1, 6, 1, 0, 3, 2, 0, 198, 2, 1, 0, 7,
      1, 0, 2, 4, 0, 37, 4, 1, 1, 2, 0, 1, 1, 720, 2, 2, 0, 4, 3, 0, 2, 0, 4,
      1, 0, 3, 0, 2, 0, 1, 1, 0, 1, 6, 0, 1, 0, 3070, 3, 0, 141, 0, 1, 96,
      32, 0, 554, 0, 6, 105, 0, 2, 30164, 1, 0, 4, 10, 0, 32, 2, 0, 80, 2, 0,
      276, 0, 1, 37, 0, 1, 151, 0, 1, 27, 18, 0, 57, 0, 3, 37, 0, 1, 95, 0,
      1, 12, 0, 1, 239, 1, 0, 1, 2, 1, 2, 2, 0, 5, 2, 0, 1, 1, 0, 52, 0, 1,
      246, 0, 1, 20272, 0, 1, 769, 7, 7, 0, 2, 0, 973, 0, 1, 226, 0, 1, 149,
      5, 0, 1682, 0, 1, 1, 1, 0, 40, 1, 2, 4, 0, 1, 165, 1, 1, 573, 4, 0,
      387, 2, 0, 153, 0, 2, 0, 3, 1, 0, 1, 4, 245, 0, 1, 56, 0, 1, 57, 0, 2,
      69, 3, 0, 48, 0, 2, 62, 0, 1, 76, 0, 1, 9, 0, 1, 106, 0, 2, 178, 0, 2,
      80, 0, 2, 16, 0, 1, 24, 7, 0, 3, 5, 0, 205, 0, 1, 3, 0, 1, 23, 1, 0,
      99, 0, 2, 251, 0, 2, 126, 0, 1, 118, 0, 2, 115, 0, 1, 269, 0, 2, 258,
      0, 2, 4, 0, 1, 156, 0, 1, 83, 0, 1, 18, 0, 1, 81, 0, 1, 421, 0, 1, 258,
      0, 1, 1, 0, 2, 81, 0, 1, 19800, 0, 5, 59, 7, 0, 1209, 0, 2, 19628, 0,
      1, 5318, 0, 5, 3, 0, 6, 8, 0, 8, 2, 5, 2, 30, 4, 0, 148, 3, 0, 3515, 7,
      0, 1, 17, 0, 2, 7, 0, 1, 2, 0, 1, 5, 0, 261, 7, 0, 437, 4, 0, 1504, 0,
      7, 109, 6, 1
    )
    uncompressDeltas(deltas)
  }

  /** Tests whether the given code point's combining class is 0 (None), 230
   *  (Above) or something else (Other).
   *
   *  This is a special-purpose method for use by `String.toLowerCase` and
   *  `String.toUpperCase`.
   */
  private[lang] def combiningClassNoneOrAboveOrOther(cp: Int): Int = {
    val indexOfRange = findIndexOfRange(
      combiningClassNoneOrAboveOrOtherIndices, cp, hasEmptyRanges = true)
    indexOfRange % 3
  }
  // format: on

  @noinline private[this] def uncompressDeltas(
      deltas: Array[Int]): Array[Int] = {
    for (i <- 1 until deltas.length)
      deltas(i) += deltas(i - 1)
    deltas
  }

  private[this] def findIndexOfRange(startOfRangesArray: Array[Int],
                                     value: Int,
                                     hasEmptyRanges: scala.Boolean): Int = {
    val i = Arrays.binarySearch(startOfRangesArray, value)
    if (i >= 0) {
      /* `value` is at the start of a range. Its range index is therefore
       * `i + 1`, since there is an implicit range starting at 0 in the
       * beginning.
       *
       * If the array has empty ranges, we may need to advance further than
       * `i + 1` until the first index `j > i` where
       * `startOfRangesArray(j) != value`.
       */
      if (hasEmptyRanges) {
        var j = i + 1
        while (j < startOfRangesArray.length && startOfRangesArray(j) == value)
          j += 1
        j
      } else {
        i + 1
      }
    } else {
      /* i is `-p - 1` where `p` is the insertion point. In that case the index
       * of the range is precisely `p`.
       */
      -i - 1
    }
  }

  // Tables to support toUpperCase and toLowerCase transformations
  // with Unicode 13.0.0 to match JDK15 and JDK16(LTS).
  // Refer to the following project for the transformation code.
  // https://github.com/ekrich/scala-unicode

  private[this] lazy val lowerRanges = Array[scala.Int](97, 122, 181, 224, 246,
    248, 254, 255, 257, 303, 305, 307, 311, 314, 328, 331, 375, 378, 382, 383,
    384, 387, 389, 392, 396, 402, 405, 409, 410, 414, 417, 421, 424, 429, 432,
    436, 438, 441, 445, 447, 453, 454, 456, 457, 459, 460, 462, 476, 477, 479,
    495, 498, 499, 501, 505, 543, 547, 563, 572, 575, 576, 578, 583, 591, 592,
    593, 594, 595, 596, 598, 599, 601, 603, 604, 608, 609, 611, 613, 614, 616,
    617, 618, 619, 620, 623, 625, 626, 629, 637, 640, 642, 643, 647, 648, 649,
    650, 651, 652, 658, 669, 670, 837, 881, 883, 887, 891, 893, 940, 941, 943,
    945, 961, 962, 963, 971, 972, 973, 974, 976, 977, 981, 982, 983, 985, 1007,
    1008, 1009, 1010, 1011, 1013, 1016, 1019, 1072, 1103, 1104, 1119, 1121,
    1153, 1163, 1215, 1218, 1230, 1231, 1233, 1327, 1377, 1414, 4304, 4346,
    4349, 4351, 5112, 5117, 7296, 7297, 7298, 7299, 7300, 7301, 7302, 7303,
    7304, 7545, 7549, 7566, 7681, 7829, 7835, 7841, 7935, 7936, 7943, 7952,
    7957, 7968, 7975, 7984, 7991, 8000, 8005, 8017, 8023, 8032, 8039, 8048,
    8049, 8050, 8053, 8054, 8055, 8056, 8057, 8058, 8059, 8060, 8061, 8064,
    8071, 8080, 8087, 8096, 8103, 8112, 8113, 8115, 8126, 8131, 8144, 8145,
    8160, 8161, 8165, 8179, 8526, 8560, 8575, 8580, 9424, 9449, 11312, 11358,
    11361, 11365, 11366, 11368, 11372, 11379, 11382, 11393, 11491, 11500, 11502,
    11507, 11520, 11557, 11559, 11565, 42561, 42605, 42625, 42651, 42787, 42799,
    42803, 42863, 42874, 42876, 42879, 42887, 42892, 42897, 42899, 42900, 42903,
    42921, 42933, 42943, 42947, 42952, 42954, 42998, 43859, 43888, 43967, 65345,
    65370, 66600, 66639, 66776, 66811, 68800, 68850, 71872, 71903, 93792, 93823,
    125218, 125251)

  private[this] lazy val lowerDeltas = Array[scala.Int](32, 32, -743, 32, 32,
    32, 32, -121, 1, 1, 232, 1, 1, 1, 1, 1, 1, 1, 1, 300, -195, 1, 1, 1, 1, 1,
    -97, 1, -163, -130, 1, 1, 1, 1, 1, 1, 1, 1, 1, -56, 1, 2, 1, 2, 1, 2, 1, 1,
    79, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, -10815, -10815, 1, 1, 1, -10783, -10780,
    -10782, 210, 206, 205, 205, 202, 203, -42319, 205, -42315, 207, -42280,
    -42308, 209, 211, -42308, -10743, -42305, 211, -10749, 213, 214, -10727,
    218, -42307, 218, -42282, 218, 69, 217, 217, 71, 219, -42261, -42258, -84,
    1, 1, 1, -130, -130, 38, 37, 37, 32, 32, 31, 32, 32, 64, 63, 63, 62, 57, 47,
    54, 8, 1, 1, 86, 80, -7, 116, 96, 1, 1, 32, 32, 80, 80, 1, 1, 1, 1, 1, 1,
    15, 1, 1, 48, 48, -3008, -3008, -3008, -3008, 8, 8, 6254, 6253, 6244, 6242,
    6242, 6243, 6236, 6181, -35266, -35332, -3814, -35384, 1, 1, 59, 1, 1, -8,
    -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -74, -74, -86, -86,
    -100, -100, -128, -128, -112, -112, -126, -126, -8, -8, -8, -8, -8, -8, -8,
    -8, -9, 7205, -9, -8, -8, -8, -8, -7, -9, 28, 16, 16, 1, 26, 26, 48, 48, 1,
    10795, 10792, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7264, 7264, 7264, 7264, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -48, 1, 1, 1, 1, 1, 1, 1, 1, 928, 38864,
    38864, 32, 32, 40, 40, 40, 40, 64, 64, 32, 32, 32, 32, 34, 34)

  private[this] lazy val lowerSteps = Array[scala.Byte](0, 1, 0, 0, 1, 0, 1, 0,
    0, 2, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,
    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 2, 0, 2,
    0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0,
    0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 0, 1, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1,
    0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0,
    1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0,
    1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 2, 0,
    0, 1, 0, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 2, 0, 2, 0,
    0, 2, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1)

  private[this] lazy val upperRanges = Array[scala.Int](65, 90, 192, 214, 216,
    222, 256, 302, 304, 306, 310, 313, 327, 330, 374, 376, 377, 381, 385, 386,
    388, 390, 391, 393, 394, 395, 398, 399, 400, 401, 403, 404, 406, 407, 408,
    412, 413, 415, 416, 420, 422, 423, 425, 428, 430, 431, 433, 434, 435, 437,
    439, 440, 444, 452, 453, 455, 456, 458, 459, 475, 478, 494, 497, 498, 500,
    502, 503, 504, 542, 544, 546, 562, 570, 571, 573, 574, 577, 579, 580, 581,
    582, 590, 880, 882, 886, 895, 902, 904, 906, 908, 910, 911, 913, 929, 931,
    939, 975, 984, 1006, 1012, 1015, 1017, 1018, 1021, 1023, 1024, 1039, 1040,
    1071, 1120, 1152, 1162, 1214, 1216, 1217, 1229, 1232, 1326, 1329, 1366,
    4256, 4293, 4295, 4301, 5024, 5103, 5104, 5109, 7312, 7354, 7357, 7359,
    7680, 7828, 7838, 7840, 7934, 7944, 7951, 7960, 7965, 7976, 7983, 7992,
    7999, 8008, 8013, 8025, 8031, 8040, 8047, 8072, 8079, 8088, 8095, 8104,
    8111, 8120, 8121, 8122, 8123, 8124, 8136, 8139, 8140, 8152, 8153, 8154,
    8155, 8168, 8169, 8170, 8171, 8172, 8184, 8185, 8186, 8187, 8188, 8486,
    8490, 8491, 8498, 8544, 8559, 8579, 9398, 9423, 11264, 11310, 11360, 11362,
    11363, 11364, 11367, 11371, 11373, 11374, 11375, 11376, 11378, 11381, 11390,
    11391, 11392, 11490, 11499, 11501, 11506, 42560, 42604, 42624, 42650, 42786,
    42798, 42802, 42862, 42873, 42875, 42877, 42878, 42886, 42891, 42893, 42896,
    42898, 42902, 42920, 42922, 42923, 42924, 42925, 42926, 42928, 42929, 42930,
    42931, 42932, 42942, 42946, 42948, 42949, 42950, 42951, 42953, 42997, 65313,
    65338, 66560, 66599, 66736, 66771, 68736, 68786, 71840, 71871, 93760, 93791,
    125184, 125217)

  private[this] lazy val upperDeltas = Array[scala.Int](-32, -32, -32, -32, -32,
    -32, -1, -1, 199, -1, -1, -1, -1, -1, -1, 121, -1, -1, -210, -1, -1, -206,
    -1, -205, -205, -1, -79, -202, -203, -1, -205, -207, -211, -209, -1, -211,
    -213, -214, -1, -1, -218, -1, -218, -1, -218, -1, -217, -217, -1, -1, -219,
    -1, -1, -2, -1, -2, -1, -2, -1, -1, -1, -1, -2, -1, -1, 97, 56, -1, -1, 130,
    -1, -1, -10795, -1, 163, -10792, -1, 195, -69, -71, -1, -1, -1, -1, -1,
    -116, -38, -37, -37, -64, -63, -63, -32, -32, -32, -32, -8, -1, -1, 60, -1,
    7, -1, 130, 130, -80, -80, -32, -32, -1, -1, -1, -1, -15, -1, -1, -1, -1,
    -48, -48, -7264, -7264, -7264, -7264, -38864, -38864, -8, -8, 3008, 3008,
    3008, 3008, -1, -1, 7615, -1, -1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 74, 74, 9, 86, 86, 9, 8, 8, 100, 100, 8, 8, 112,
    112, 7, 128, 128, 126, 126, 9, 7517, 8383, 8262, -28, -16, -16, -1, -26,
    -26, -48, -48, -1, 10743, 3814, 10727, -1, -1, 10780, 10749, 10783, 10782,
    -1, -1, 10815, 10815, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, 35332, -1, -1, -1, 42280, -1, -1, -1, -1, 42308, 42319, 42315,
    42305, 42308, 42258, 42282, 42261, -928, -1, -1, -1, 48, 42307, 35384, -1,
    -1, -1, -32, -32, -40, -40, -40, -40, -64, -64, -32, -32, -32, -32, -34,
    -34)

  private[this] lazy val upperSteps = Array[scala.Byte](0, 1, 0, 1, 0, 1, 0, 2,
    0, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,
    2, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 1, 0,
    1, 0, 2, 0, 2, 0, 0, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0,
    2, 0, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 0,
    1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0,
    0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 2,
    0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
    0, 1)

  private[this] object CaseUtil {
    lazy val a = lowerRanges(0)
    lazy val z = lowerRanges(1)
    lazy val A = upperRanges(0)
    lazy val Z = upperRanges(1)
    // other low char optimization whitespace, punctuation, etc.
    lazy val upperMu                        = upperRanges(2)
    lazy val lowerBeta                      = lowerRanges(2)
    def insertionPoint(idx: Int)            = (-(idx) - 1)
    def convert(codePoint: Int, delta: Int) = codePoint - delta
  }

  private[this] def toCase(codePoint: Int,
                           asciiLow: Int,
                           asciiHigh: Int,
                           lowFilter: Int,
                           ranges: Array[scala.Int],
                           deltas: Array[scala.Int],
                           steps: Array[scala.Byte]): Int = {
    import CaseUtil._
    if (asciiLow <= codePoint && codePoint <= asciiHigh)
      convert(codePoint, deltas(0))           // ascii
    else if (codePoint < lowFilter) codePoint // whitespace, punctuation, etc.
    else {
      val idx = Arrays.binarySearch(ranges, codePoint)
      if (idx >= 0) {
        convert(codePoint, deltas(idx))
      } else {
        val ip = insertionPoint(idx)
        // ip == 0 is below ranges but < lowFilter above covers that
        if (ip == ranges.size) codePoint // above ranges
        else {
          val step = steps(ip)
          if (step == 0) {
            // no range involved
            codePoint
          } else {
            val delta      = deltas(ip)
            val upperBound = ranges(ip)
            if (step == 1) {
              convert(codePoint, delta)
            } else {
              // step == 2 so check both odd or even
              if ((upperBound & 1) == (codePoint & 1)) {
                convert(codePoint, delta)
              } else {
                codePoint
              }
            }
          }
        }
      }
    }
  }

  /* Indices defining ranges of case-ignorable characters defined in Unicode reference chapter 3.13.
   * They are only used in String special casing method, eg. String.toLowerCase
   *
   * Definition of case-ignorable character form Unicode [reference document](https://www.unicode.org/versions/Unicode13.0.0/ch03.pdf#G33992):
   * A character C is defined to be case-ignorable if C has the value MidLetter (ML),
   * MidNumLet (MB), or Single_Quote (SQ) for the Word_Break property or its General_Category is one of Nonspacing_Mark (Mn),
   * Enclosing_Mark (Me), Format (Cf), Modifier_Letter (Lm), or Modifier_Symbol (Sk).
   * - The Word_Break property is defined in the data file WordBreakProperty.txt in the Unicode Character Database.
   * - The derived property Case_Ignorable is listed in the data file DerivedCoreProperties.txt in the Unicode Character Database.
   *
   * Deltas were generated based on DerivedCoreProperties.txt reference document using following code
   *  (it can also be used to generated deltas for cased characters)
   * ```
   * val codePoints = io.Source
   *   .fromURL("https://www.unicode.org/Public/13.0.0/ucd/DerivedCoreProperties.txt")
   *   .getLines().filterNot(_.isEmpty)
   *   .dropWhile(!_.startsWith("# Derived Property:   Cased (Cased)"))
   *   // .dropWhile(!_.startsWith("# Derived Property:   Case_Ignorable (CI)"))
   *   .takeWhile(!_.startsWith("# Total"))
   *   .filter(!_.startsWith("#"))
   *   .map { _
   *     .split(";").head
   *     .split("\\.\\.")
   *     .filterNot(_.isEmpty)
   *     .map(Integer.parseInt(_, 16))
   *    }
   *    .flatMap {
   *       case Array(single) => single :: Nil
   *       case Array(from, to) => from.to(to)
   *    }.toList
   *
   * val deltas: List[Int] = {
   *   val b = List.newBuilder[Int]
   *   b += 0
   *   (0 to Character.MAX_CODE_POINT).foldLeft((0, false)) {
   *     case ((rangeFrom, last), cp) if codePoints.contains(cp) != last =>
   *       b += cp - rangeFrom
   *       (cp, !last)
   *     case (notChanged, _) => notChanged
   *   }
   *   b.result()
   * }
   * ```
   */
  private[this] lazy val caseIgnorableIndices: Array[Int] = {
    val deltas: Array[Int] = Array(39, 1, 6, 1, 11, 1, 35, 1, 1, 1, 71, 1, 4, 1,
      1, 1, 4, 1, 2, 2, 503, 192, 4, 2, 4, 1, 9, 2, 1, 1, 251, 7, 207, 1, 5, 1,
      49, 45, 1, 1, 1, 2, 1, 2, 1, 1, 44, 1, 11, 6, 10, 11, 1, 1, 35, 1, 10, 21,
      16, 1, 101, 8, 1, 10, 1, 4, 33, 1, 1, 1, 30, 27, 91, 11, 58, 11, 4, 1, 2,
      1, 24, 24, 43, 3, 119, 48, 55, 1, 1, 1, 4, 8, 4, 1, 3, 7, 10, 2, 13, 1,
      15, 1, 58, 1, 4, 4, 8, 1, 20, 2, 26, 1, 2, 2, 57, 1, 4, 2, 4, 2, 2, 3, 3,
      1, 30, 2, 3, 1, 11, 2, 57, 1, 4, 5, 1, 2, 4, 1, 20, 2, 22, 6, 1, 1, 58, 1,
      2, 1, 1, 4, 8, 1, 7, 2, 11, 2, 30, 1, 61, 1, 12, 1, 50, 1, 3, 1, 57, 3, 5,
      3, 1, 4, 7, 2, 11, 2, 29, 1, 58, 1, 2, 1, 6, 1, 5, 2, 20, 2, 28, 2, 57, 2,
      4, 4, 8, 1, 20, 2, 29, 1, 72, 1, 7, 3, 1, 1, 90, 1, 2, 7, 11, 9, 98, 1, 2,
      9, 9, 1, 1, 6, 74, 2, 27, 1, 1, 1, 1, 1, 55, 14, 1, 5, 1, 2, 5, 11, 1, 36,
      9, 1, 102, 4, 1, 6, 1, 2, 2, 2, 25, 2, 4, 3, 16, 4, 13, 1, 2, 2, 6, 1, 15,
      1, 94, 1, 608, 3, 946, 3, 29, 3, 29, 2, 30, 2, 64, 2, 1, 7, 8, 1, 2, 11,
      3, 1, 5, 1, 45, 4, 52, 1, 65, 2, 34, 1, 118, 3, 4, 2, 9, 1, 6, 3, 219, 2,
      2, 1, 58, 1, 1, 7, 1, 1, 1, 1, 2, 8, 6, 10, 2, 1, 39, 1, 8, 17, 63, 4, 48,
      1, 1, 5, 1, 1, 5, 1, 40, 9, 12, 2, 32, 4, 2, 2, 1, 3, 56, 1, 1, 2, 3, 1,
      1, 3, 58, 8, 2, 2, 64, 6, 82, 3, 1, 13, 1, 7, 4, 1, 6, 1, 3, 2, 50, 63,
      13, 1, 34, 95, 1, 5, 445, 1, 1, 3, 11, 3, 13, 3, 13, 3, 13, 2, 12, 5, 8,
      2, 10, 1, 2, 1, 2, 5, 49, 5, 1, 10, 1, 1, 13, 1, 16, 13, 51, 33, 2955, 2,
      113, 3, 125, 1, 15, 1, 96, 32, 47, 1, 469, 1, 36, 4, 3, 5, 5, 1, 93, 6,
      93, 3, 28438, 1, 1250, 6, 270, 1, 98, 4, 1, 10, 1, 1, 28, 4, 80, 2, 14,
      34, 78, 1, 23, 3, 109, 2, 8, 1, 3, 1, 4, 1, 25, 2, 5, 1, 151, 2, 26, 18,
      13, 1, 38, 8, 25, 11, 46, 3, 48, 1, 2, 4, 2, 2, 17, 1, 21, 2, 66, 6, 2, 2,
      2, 2, 12, 1, 8, 1, 35, 1, 11, 1, 51, 1, 1, 3, 2, 2, 5, 2, 1, 1, 27, 1, 14,
      2, 5, 2, 1, 1, 100, 5, 9, 3, 121, 1, 2, 1, 4, 1, 20272, 1, 147, 16, 574,
      16, 3, 1, 12, 16, 34, 1, 2, 1, 169, 1, 7, 1, 6, 1, 11, 1, 35, 1, 1, 1, 47,
      1, 45, 2, 67, 1, 21, 3, 513, 1, 226, 1, 149, 5, 1670, 3, 1, 2, 5, 4, 40,
      3, 4, 1, 165, 2, 573, 4, 387, 2, 153, 11, 176, 1, 54, 15, 56, 3, 49, 4, 2,
      2, 2, 1, 15, 1, 50, 3, 36, 5, 1, 8, 62, 1, 12, 2, 52, 9, 10, 4, 2, 1, 95,
      3, 2, 1, 1, 2, 6, 1, 160, 1, 3, 8, 21, 2, 57, 2, 3, 1, 37, 7, 3, 5, 195,
      8, 2, 3, 1, 1, 23, 1, 84, 6, 1, 1, 4, 2, 1, 2, 238, 4, 6, 2, 1, 2, 27, 2,
      85, 8, 2, 1, 1, 2, 106, 1, 1, 1, 2, 6, 1, 1, 101, 3, 2, 4, 1, 5, 259, 9,
      1, 2, 256, 2, 1, 1, 4, 1, 144, 4, 2, 2, 4, 1, 32, 10, 40, 6, 2, 4, 8, 1,
      9, 6, 2, 3, 46, 13, 1, 2, 406, 7, 1, 6, 1, 1, 82, 22, 2, 7, 1, 2, 1, 2,
      122, 6, 3, 1, 1, 2, 1, 7, 1, 1, 72, 2, 3, 1, 1, 1, 347, 2, 5435, 9, 14007,
      5, 59, 7, 9, 4, 1035, 1, 63, 17, 64, 2, 1, 2, 19640, 2, 1, 4, 5315, 3, 9,
      16, 2, 7, 30, 4, 148, 3, 1979, 55, 4, 50, 8, 1, 14, 1, 22, 5, 1, 15, 1360,
      7, 1, 17, 2, 7, 1, 2, 1, 5, 261, 14, 430, 4, 1504, 7, 109, 8, 2735, 5,
      789505, 1, 30, 96, 128, 240)

    uncompressDeltas(deltas)
  }

  /* Indices defining ranges of case-ignorable characters defined in Unicode reference chapter 3.13.
   * They are only used in String special casing method, eg. String.toLowerCase.
   * Indices were generated based on [DerivedCodeProperties.txt](https://www.unicode.org/Public/13.0.0/ucd/DerivedCoreProperties.txt)
   *
   * Definition of cased character from Unicode [reference document](https://www.unicode.org/versions/Unicode13.0.0/ch03.pdf#G33992):
   * D135 A character C is defined to be cased if and only if
   * C has the Lowercase or Uppercase property or has a General_Category value of Titlecase_Letter.
   * - The Uppercase and Lowercase property values are specified in the data file DerivedCoreProperties.txt in the Unicode Character Database.
   * - The derived property Cased is also listed in DerivedCoreProperties.txt.
   *
   * What is worth to notice we currently cannot use isLowerCase || isUpperCase || isTitleCase (not implemented)
   * test methods instead, as they're not compatible with Unicode 13.0.0 specification used for special casing.
   * Usage of such methods would result in wrong results for following number of characters based on their type:
   * 135 Unassigned characters, 261 UppercaseLetters, 312 LowercaseLetters, 6 Modifier letters, 46 OtherLetters and 78 OtherSymbols.
   * Unicode 10.0 specification implemented in JDK 11 limits this numbers to all unmatched Unassigned and OtherLetter characters
   *
   * For code used to generate deltas see `caseIgnorableIndices` comment.
   */
  private[this] lazy val casedIndices: Array[Int] = {
    val deltas: Array[Int] = Array(65, 26, 6, 26, 47, 1, 10, 1, 4, 1, 5, 23, 1,
      31, 1, 195, 1, 4, 4, 208, 1, 36, 7, 2, 30, 5, 96, 1, 42, 4, 2, 2, 2, 4, 1,
      1, 6, 1, 1, 3, 1, 1, 1, 20, 1, 83, 1, 139, 8, 166, 1, 38, 9, 41, 2839, 38,
      1, 1, 5, 1, 2, 43, 2, 3, 672, 86, 2, 6, 2178, 9, 7, 43, 2, 3, 64, 192, 64,
      278, 2, 6, 2, 38, 2, 6, 2, 8, 1, 1, 1, 1, 1, 1, 1, 31, 2, 53, 1, 7, 1, 1,
      3, 3, 1, 7, 3, 4, 2, 6, 4, 13, 5, 3, 1, 7, 116, 1, 13, 1, 16, 13, 101, 1,
      4, 1, 2, 10, 1, 1, 3, 5, 6, 1, 1, 1, 1, 1, 1, 4, 1, 6, 4, 1, 2, 4, 5, 5,
      4, 1, 17, 32, 3, 2, 817, 52, 1814, 47, 1, 47, 1, 133, 6, 4, 3, 2, 12, 38,
      1, 1, 5, 1, 30994, 46, 18, 30, 132, 102, 3, 4, 1, 48, 2, 9, 42, 2, 1, 3,
      821, 43, 1, 13, 7, 80, 20288, 7, 12, 5, 1033, 26, 6, 26, 1189, 80, 96, 36,
      4, 36, 1924, 51, 13, 51, 2989, 64, 21856, 64, 25984, 85, 1, 71, 1, 2, 2,
      1, 2, 2, 2, 4, 1, 12, 1, 1, 1, 7, 1, 65, 1, 4, 2, 8, 1, 7, 1, 28, 1, 4, 1,
      5, 1, 1, 3, 7, 1, 340, 2, 25, 1, 25, 1, 31, 1, 25, 1, 31, 1, 25, 1, 31, 1,
      25, 1, 31, 1, 25, 1, 8, 4404, 68, 2028, 26, 6, 26, 6, 26)

    uncompressDeltas(deltas)
  }

  /*
   * This method is implementation specific. It's only used for support of String.toLowerCase special characters handling
   */
  private[lang] def isCaseIgnorable(codePoint: Int): scala.Boolean = {
    val idx =
      findIndexOfRange(caseIgnorableIndices, codePoint, hasEmptyRanges = false)
    idx % 2 == 1
  }

  /*
   * This method is implementation specific. It's only used for support of String.toLowerCase special characters handling
   */
  private[lang] def isCased(codePoint: Int): scala.Boolean = {
    val idx =
      findIndexOfRange(casedIndices, codePoint, hasEmptyRanges = false)
    idx % 2 == 1
  }

  // Ported from Harmony
  class Subset protected (var name: String) {
    if (name == null) {
      throw new NullPointerException()
    }
    override def equals(that: Any): scala.Boolean = super.equals(that)
    override def hashCode: scala.Int              = super.hashCode
    override def toString                         = name
  }

  final class UnicodeBlock protected (name: String) extends Subset(name) {
    private var start: Int = _
    private var end: Int   = _
    private def this(name: String, start: Int, end: Int) = {
      this(name)
      this.start = start
      this.end = end
    }
  }

  object UnicodeBlock {
    //////////////////
    // Begin Generated
    //////////////////

    // scalastyle:off line.size.limit
    val SURROGATES_AREA = new UnicodeBlock("SURROGATES_AREA", 0x0, 0x0)
    val BASIC_LATIN     = new UnicodeBlock("BASIC_LATIN", 0x0000, 0x007F)
    val LATIN_1_SUPPLEMENT =
      new UnicodeBlock("LATIN_1_SUPPLEMENT", 0x0080, 0x00FF)
    val LATIN_EXTENDED_A = new UnicodeBlock("LATIN_EXTENDED_A", 0x0100, 0x017F)
    val LATIN_EXTENDED_B = new UnicodeBlock("LATIN_EXTENDED_B", 0x0180, 0x024F)
    val IPA_EXTENSIONS   = new UnicodeBlock("IPA_EXTENSIONS", 0x0250, 0x02AF)
    val SPACING_MODIFIER_LETTERS =
      new UnicodeBlock("SPACING_MODIFIER_LETTERS", 0x02B0, 0x02FF)
    val COMBINING_DIACRITICAL_MARKS =
      new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", 0x0300, 0x036F)
    val GREEK    = new UnicodeBlock("GREEK", 0x0370, 0x03FF)
    val CYRILLIC = new UnicodeBlock("CYRILLIC", 0x0400, 0x04FF)
    val CYRILLIC_SUPPLEMENTARY =
      new UnicodeBlock("CYRILLIC_SUPPLEMENTARY", 0x0500, 0x052F)
    val ARMENIAN = new UnicodeBlock("ARMENIAN", 0x0530, 0x058F)
    val HEBREW   = new UnicodeBlock("HEBREW", 0x0590, 0x05FF)
    val ARABIC   = new UnicodeBlock("ARABIC", 0x0600, 0x06FF)
    val SYRIAC   = new UnicodeBlock("SYRIAC", 0x0700, 0x074F)
    val ARABIC_SUPPLEMENT =
      new UnicodeBlock("ARABIC_SUPPLEMENT", 0x0750, 0x077F)
    val THAANA    = new UnicodeBlock("THAANA", 0x0780, 0x07BF)
    val SAMARITAN = new UnicodeBlock("SAMARITAN", 0x0800, 0x083F)
    val MANDAIC   = new UnicodeBlock("MANDAIC", 0x0840, 0x085F)
    val SYRIAC_SUPPLEMENT =
      new UnicodeBlock("SYRIAC_SUPPLEMENT", 0x0860, 0x086F)
    val ARABIC_EXTENDED_A =
      new UnicodeBlock("ARABIC_EXTENDED_A", 0x08A0, 0x08FF)
    val DEVANAGARI  = new UnicodeBlock("DEVANAGARI", 0x0900, 0x097F)
    val BENGALI     = new UnicodeBlock("BENGALI", 0x0980, 0x09FF)
    val GURMUKHI    = new UnicodeBlock("GURMUKHI", 0x0A00, 0x0A7F)
    val GUJARATI    = new UnicodeBlock("GUJARATI", 0x0A80, 0x0AFF)
    val ORIYA       = new UnicodeBlock("ORIYA", 0x0B00, 0x0B7F)
    val TAMIL       = new UnicodeBlock("TAMIL", 0x0B80, 0x0BFF)
    val TELUGU      = new UnicodeBlock("TELUGU", 0x0C00, 0x0C7F)
    val KANNADA     = new UnicodeBlock("KANNADA", 0x0C80, 0x0CFF)
    val MALAYALAM   = new UnicodeBlock("MALAYALAM", 0x0D00, 0x0D7F)
    val SINHALA     = new UnicodeBlock("SINHALA", 0x0D80, 0x0DFF)
    val THAI        = new UnicodeBlock("THAI", 0x0E00, 0x0E7F)
    val LAO         = new UnicodeBlock("LAO", 0x0E80, 0x0EFF)
    val TIBETAN     = new UnicodeBlock("TIBETAN", 0x0F00, 0x0FFF)
    val MYANMAR     = new UnicodeBlock("MYANMAR", 0x1000, 0x109F)
    val GEORGIAN    = new UnicodeBlock("GEORGIAN", 0x10A0, 0x10FF)
    val HANGUL_JAMO = new UnicodeBlock("HANGUL_JAMO", 0x1100, 0x11FF)
    val ETHIOPIC    = new UnicodeBlock("ETHIOPIC", 0x1200, 0x137F)
    val ETHIOPIC_SUPPLEMENT =
      new UnicodeBlock("ETHIOPIC_SUPPLEMENT", 0x1380, 0x139F)
    val CHEROKEE = new UnicodeBlock("CHEROKEE", 0x13A0, 0x13FF)
    val UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS =
      new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 0x1400, 0x167F)
    val OGHAM     = new UnicodeBlock("OGHAM", 0x1680, 0x169F)
    val RUNIC     = new UnicodeBlock("RUNIC", 0x16A0, 0x16FF)
    val TAGALOG   = new UnicodeBlock("TAGALOG", 0x1700, 0x171F)
    val HANUNOO   = new UnicodeBlock("HANUNOO", 0x1720, 0x173F)
    val BUHID     = new UnicodeBlock("BUHID", 0x1740, 0x175F)
    val TAGBANWA  = new UnicodeBlock("TAGBANWA", 0x1760, 0x177F)
    val KHMER     = new UnicodeBlock("KHMER", 0x1780, 0x17FF)
    val MONGOLIAN = new UnicodeBlock("MONGOLIAN", 0x1800, 0x18AF)
    val UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = new UnicodeBlock(
      "UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED",
      0x18B0,
      0x18FF)
    val LIMBU         = new UnicodeBlock("LIMBU", 0x1900, 0x194F)
    val TAI_LE        = new UnicodeBlock("TAI_LE", 0x1950, 0x197F)
    val NEW_TAI_LUE   = new UnicodeBlock("NEW_TAI_LUE", 0x1980, 0x19DF)
    val KHMER_SYMBOLS = new UnicodeBlock("KHMER_SYMBOLS", 0x19E0, 0x19FF)
    val BUGINESE      = new UnicodeBlock("BUGINESE", 0x1A00, 0x1A1F)
    val TAI_THAM      = new UnicodeBlock("TAI_THAM", 0x1A20, 0x1AAF)
    val COMBINING_DIACRITICAL_MARKS_EXTENDED =
      new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_EXTENDED", 0x1AB0, 0x1AFF)
    val BALINESE  = new UnicodeBlock("BALINESE", 0x1B00, 0x1B7F)
    val SUNDANESE = new UnicodeBlock("SUNDANESE", 0x1B80, 0x1BBF)
    val BATAK     = new UnicodeBlock("BATAK", 0x1BC0, 0x1BFF)
    val LEPCHA    = new UnicodeBlock("LEPCHA", 0x1C00, 0x1C4F)
    val OL_CHIKI  = new UnicodeBlock("OL_CHIKI", 0x1C50, 0x1C7F)
    val CYRILLIC_EXTENDED_C =
      new UnicodeBlock("CYRILLIC_EXTENDED_C", 0x1C80, 0x1C8F)
    val GEORGIAN_EXTENDED =
      new UnicodeBlock("GEORGIAN_EXTENDED", 0x1C90, 0x1CBF)
    val SUNDANESE_SUPPLEMENT =
      new UnicodeBlock("SUNDANESE_SUPPLEMENT", 0x1CC0, 0x1CCF)
    val VEDIC_EXTENSIONS = new UnicodeBlock("VEDIC_EXTENSIONS", 0x1CD0, 0x1CFF)
    val PHONETIC_EXTENSIONS =
      new UnicodeBlock("PHONETIC_EXTENSIONS", 0x1D00, 0x1D7F)
    val PHONETIC_EXTENSIONS_SUPPLEMENT =
      new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT", 0x1D80, 0x1DBF)
    val COMBINING_DIACRITICAL_MARKS_SUPPLEMENT =
      new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT", 0x1DC0, 0x1DFF)
    val LATIN_EXTENDED_ADDITIONAL =
      new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", 0x1E00, 0x1EFF)
    val GREEK_EXTENDED = new UnicodeBlock("GREEK_EXTENDED", 0x1F00, 0x1FFF)
    val GENERAL_PUNCTUATION =
      new UnicodeBlock("GENERAL_PUNCTUATION", 0x2000, 0x206F)
    val SUPERSCRIPTS_AND_SUBSCRIPTS =
      new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", 0x2070, 0x209F)
    val CURRENCY_SYMBOLS = new UnicodeBlock("CURRENCY_SYMBOLS", 0x20A0, 0x20CF)
    val COMBINING_MARKS_FOR_SYMBOLS =
      new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", 0x20D0, 0x20FF)
    val LETTERLIKE_SYMBOLS =
      new UnicodeBlock("LETTERLIKE_SYMBOLS", 0x2100, 0x214F)
    val NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", 0x2150, 0x218F)
    val ARROWS       = new UnicodeBlock("ARROWS", 0x2190, 0x21FF)
    val MATHEMATICAL_OPERATORS =
      new UnicodeBlock("MATHEMATICAL_OPERATORS", 0x2200, 0x22FF)
    val MISCELLANEOUS_TECHNICAL =
      new UnicodeBlock("MISCELLANEOUS_TECHNICAL", 0x2300, 0x23FF)
    val CONTROL_PICTURES = new UnicodeBlock("CONTROL_PICTURES", 0x2400, 0x243F)
    val OPTICAL_CHARACTER_RECOGNITION =
      new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", 0x2440, 0x245F)
    val ENCLOSED_ALPHANUMERICS =
      new UnicodeBlock("ENCLOSED_ALPHANUMERICS", 0x2460, 0x24FF)
    val BOX_DRAWING      = new UnicodeBlock("BOX_DRAWING", 0x2500, 0x257F)
    val BLOCK_ELEMENTS   = new UnicodeBlock("BLOCK_ELEMENTS", 0x2580, 0x259F)
    val GEOMETRIC_SHAPES = new UnicodeBlock("GEOMETRIC_SHAPES", 0x25A0, 0x25FF)
    val MISCELLANEOUS_SYMBOLS =
      new UnicodeBlock("MISCELLANEOUS_SYMBOLS", 0x2600, 0x26FF)
    val DINGBATS = new UnicodeBlock("DINGBATS", 0x2700, 0x27BF)
    val MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A =
      new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", 0x27C0, 0x27EF)
    val SUPPLEMENTAL_ARROWS_A =
      new UnicodeBlock("SUPPLEMENTAL_ARROWS_A", 0x27F0, 0x27FF)
    val BRAILLE_PATTERNS = new UnicodeBlock("BRAILLE_PATTERNS", 0x2800, 0x28FF)
    val SUPPLEMENTAL_ARROWS_B =
      new UnicodeBlock("SUPPLEMENTAL_ARROWS_B", 0x2900, 0x297F)
    val MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B =
      new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", 0x2980, 0x29FF)
    val SUPPLEMENTAL_MATHEMATICAL_OPERATORS =
      new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", 0x2A00, 0x2AFF)
    val MISCELLANEOUS_SYMBOLS_AND_ARROWS =
      new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS", 0x2B00, 0x2BFF)
    val GLAGOLITIC       = new UnicodeBlock("GLAGOLITIC", 0x2C00, 0x2C5F)
    val LATIN_EXTENDED_C = new UnicodeBlock("LATIN_EXTENDED_C", 0x2C60, 0x2C7F)
    val COPTIC           = new UnicodeBlock("COPTIC", 0x2C80, 0x2CFF)
    val GEORGIAN_SUPPLEMENT =
      new UnicodeBlock("GEORGIAN_SUPPLEMENT", 0x2D00, 0x2D2F)
    val TIFINAGH = new UnicodeBlock("TIFINAGH", 0x2D30, 0x2D7F)
    val ETHIOPIC_EXTENDED =
      new UnicodeBlock("ETHIOPIC_EXTENDED", 0x2D80, 0x2DDF)
    val CYRILLIC_EXTENDED_A =
      new UnicodeBlock("CYRILLIC_EXTENDED_A", 0x2DE0, 0x2DFF)
    val SUPPLEMENTAL_PUNCTUATION =
      new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION", 0x2E00, 0x2E7F)
    val CJK_RADICALS_SUPPLEMENT =
      new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", 0x2E80, 0x2EFF)
    val KANGXI_RADICALS = new UnicodeBlock("KANGXI_RADICALS", 0x2F00, 0x2FDF)
    val IDEOGRAPHIC_DESCRIPTION_CHARACTERS =
      new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 0x2FF0, 0x2FFF)
    val CJK_SYMBOLS_AND_PUNCTUATION =
      new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", 0x3000, 0x303F)
    val HIRAGANA = new UnicodeBlock("HIRAGANA", 0x3040, 0x309F)
    val KATAKANA = new UnicodeBlock("KATAKANA", 0x30A0, 0x30FF)
    val BOPOMOFO = new UnicodeBlock("BOPOMOFO", 0x3100, 0x312F)
    val HANGUL_COMPATIBILITY_JAMO =
      new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", 0x3130, 0x318F)
    val KANBUN = new UnicodeBlock("KANBUN", 0x3190, 0x319F)
    val BOPOMOFO_EXTENDED =
      new UnicodeBlock("BOPOMOFO_EXTENDED", 0x31A0, 0x31BF)
    val CJK_STROKES = new UnicodeBlock("CJK_STROKES", 0x31C0, 0x31EF)
    val KATAKANA_PHONETIC_EXTENSIONS =
      new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS", 0x31F0, 0x31FF)
    val ENCLOSED_CJK_LETTERS_AND_MONTHS =
      new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", 0x3200, 0x32FF)
    val CJK_COMPATIBILITY =
      new UnicodeBlock("CJK_COMPATIBILITY", 0x3300, 0x33FF)
    val CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 0x3400, 0x4DBF)
    val YIJING_HEXAGRAM_SYMBOLS =
      new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS", 0x4DC0, 0x4DFF)
    val CJK_UNIFIED_IDEOGRAPHS =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", 0x4E00, 0x9FFF)
    val YI_SYLLABLES = new UnicodeBlock("YI_SYLLABLES", 0xA000, 0xA48F)
    val YI_RADICALS  = new UnicodeBlock("YI_RADICALS", 0xA490, 0xA4CF)
    val LISU         = new UnicodeBlock("LISU", 0xA4D0, 0xA4FF)
    val VAI          = new UnicodeBlock("VAI", 0xA500, 0xA63F)
    val CYRILLIC_EXTENDED_B =
      new UnicodeBlock("CYRILLIC_EXTENDED_B", 0xA640, 0xA69F)
    val BAMUM = new UnicodeBlock("BAMUM", 0xA6A0, 0xA6FF)
    val MODIFIER_TONE_LETTERS =
      new UnicodeBlock("MODIFIER_TONE_LETTERS", 0xA700, 0xA71F)
    val LATIN_EXTENDED_D = new UnicodeBlock("LATIN_EXTENDED_D", 0xA720, 0xA7FF)
    val SYLOTI_NAGRI     = new UnicodeBlock("SYLOTI_NAGRI", 0xA800, 0xA82F)
    val COMMON_INDIC_NUMBER_FORMS =
      new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS", 0xA830, 0xA83F)
    val PHAGS_PA   = new UnicodeBlock("PHAGS_PA", 0xA840, 0xA87F)
    val SAURASHTRA = new UnicodeBlock("SAURASHTRA", 0xA880, 0xA8DF)
    val DEVANAGARI_EXTENDED =
      new UnicodeBlock("DEVANAGARI_EXTENDED", 0xA8E0, 0xA8FF)
    val KAYAH_LI = new UnicodeBlock("KAYAH_LI", 0xA900, 0xA92F)
    val REJANG   = new UnicodeBlock("REJANG", 0xA930, 0xA95F)
    val HANGUL_JAMO_EXTENDED_A =
      new UnicodeBlock("HANGUL_JAMO_EXTENDED_A", 0xA960, 0xA97F)
    val JAVANESE = new UnicodeBlock("JAVANESE", 0xA980, 0xA9DF)
    val MYANMAR_EXTENDED_B =
      new UnicodeBlock("MYANMAR_EXTENDED_B", 0xA9E0, 0xA9FF)
    val CHAM = new UnicodeBlock("CHAM", 0xAA00, 0xAA5F)
    val MYANMAR_EXTENDED_A =
      new UnicodeBlock("MYANMAR_EXTENDED_A", 0xAA60, 0xAA7F)
    val TAI_VIET = new UnicodeBlock("TAI_VIET", 0xAA80, 0xAADF)
    val MEETEI_MAYEK_EXTENSIONS =
      new UnicodeBlock("MEETEI_MAYEK_EXTENSIONS", 0xAAE0, 0xAAFF)
    val ETHIOPIC_EXTENDED_A =
      new UnicodeBlock("ETHIOPIC_EXTENDED_A", 0xAB00, 0xAB2F)
    val LATIN_EXTENDED_E = new UnicodeBlock("LATIN_EXTENDED_E", 0xAB30, 0xAB6F)
    val CHEROKEE_SUPPLEMENT =
      new UnicodeBlock("CHEROKEE_SUPPLEMENT", 0xAB70, 0xABBF)
    val MEETEI_MAYEK     = new UnicodeBlock("MEETEI_MAYEK", 0xABC0, 0xABFF)
    val HANGUL_SYLLABLES = new UnicodeBlock("HANGUL_SYLLABLES", 0xAC00, 0xD7AF)
    val HANGUL_JAMO_EXTENDED_B =
      new UnicodeBlock("HANGUL_JAMO_EXTENDED_B", 0xD7B0, 0xD7FF)
    val HIGH_SURROGATES = new UnicodeBlock("HIGH_SURROGATES", 0xD800, 0xDB7F)
    val HIGH_PRIVATE_USE_SURROGATES =
      new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES", 0xDB80, 0xDBFF)
    val LOW_SURROGATES   = new UnicodeBlock("LOW_SURROGATES", 0xDC00, 0xDFFF)
    val PRIVATE_USE_AREA = new UnicodeBlock("PRIVATE_USE_AREA", 0xE000, 0xF8FF)
    val CJK_COMPATIBILITY_IDEOGRAPHS =
      new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", 0xF900, 0xFAFF)
    val ALPHABETIC_PRESENTATION_FORMS =
      new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", 0xFB00, 0xFB4F)
    val ARABIC_PRESENTATION_FORMS_A =
      new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", 0xFB50, 0xFDFF)
    val VARIATION_SELECTORS =
      new UnicodeBlock("VARIATION_SELECTORS", 0xFE00, 0xFE0F)
    val VERTICAL_FORMS = new UnicodeBlock("VERTICAL_FORMS", 0xFE10, 0xFE1F)
    val COMBINING_HALF_MARKS =
      new UnicodeBlock("COMBINING_HALF_MARKS", 0xFE20, 0xFE2F)
    val CJK_COMPATIBILITY_FORMS =
      new UnicodeBlock("CJK_COMPATIBILITY_FORMS", 0xFE30, 0xFE4F)
    val SMALL_FORM_VARIANTS =
      new UnicodeBlock("SMALL_FORM_VARIANTS", 0xFE50, 0xFE6F)
    val ARABIC_PRESENTATION_FORMS_B =
      new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", 0xFE70, 0xFEFF)
    val HALFWIDTH_AND_FULLWIDTH_FORMS =
      new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", 0xFF00, 0xFFEF)
    val SPECIALS = new UnicodeBlock("SPECIALS", 0xFFF0, 0xFFFF)
    val LINEAR_B_SYLLABARY =
      new UnicodeBlock("LINEAR_B_SYLLABARY", 0x10000, 0x1007F)
    val LINEAR_B_IDEOGRAMS =
      new UnicodeBlock("LINEAR_B_IDEOGRAMS", 0x10080, 0x100FF)
    val AEGEAN_NUMBERS = new UnicodeBlock("AEGEAN_NUMBERS", 0x10100, 0x1013F)
    val ANCIENT_GREEK_NUMBERS =
      new UnicodeBlock("ANCIENT_GREEK_NUMBERS", 0x10140, 0x1018F)
    val ANCIENT_SYMBOLS = new UnicodeBlock("ANCIENT_SYMBOLS", 0x10190, 0x101CF)
    val PHAISTOS_DISC   = new UnicodeBlock("PHAISTOS_DISC", 0x101D0, 0x101FF)
    val LYCIAN          = new UnicodeBlock("LYCIAN", 0x10280, 0x1029F)
    val CARIAN          = new UnicodeBlock("CARIAN", 0x102A0, 0x102DF)
    val COPTIC_EPACT_NUMBERS =
      new UnicodeBlock("COPTIC_EPACT_NUMBERS", 0x102E0, 0x102FF)
    val OLD_ITALIC  = new UnicodeBlock("OLD_ITALIC", 0x10300, 0x1032F)
    val GOTHIC      = new UnicodeBlock("GOTHIC", 0x10330, 0x1034F)
    val OLD_PERMIC  = new UnicodeBlock("OLD_PERMIC", 0x10350, 0x1037F)
    val UGARITIC    = new UnicodeBlock("UGARITIC", 0x10380, 0x1039F)
    val OLD_PERSIAN = new UnicodeBlock("OLD_PERSIAN", 0x103A0, 0x103DF)
    val DESERET     = new UnicodeBlock("DESERET", 0x10400, 0x1044F)
    val SHAVIAN     = new UnicodeBlock("SHAVIAN", 0x10450, 0x1047F)
    val OSMANYA     = new UnicodeBlock("OSMANYA", 0x10480, 0x104AF)
    val OSAGE       = new UnicodeBlock("OSAGE", 0x104B0, 0x104FF)
    val ELBASAN     = new UnicodeBlock("ELBASAN", 0x10500, 0x1052F)
    val CAUCASIAN_ALBANIAN =
      new UnicodeBlock("CAUCASIAN_ALBANIAN", 0x10530, 0x1056F)
    val LINEAR_A = new UnicodeBlock("LINEAR_A", 0x10600, 0x1077F)
    val CYPRIOT_SYLLABARY =
      new UnicodeBlock("CYPRIOT_SYLLABARY", 0x10800, 0x1083F)
    val IMPERIAL_ARAMAIC =
      new UnicodeBlock("IMPERIAL_ARAMAIC", 0x10840, 0x1085F)
    val PALMYRENE  = new UnicodeBlock("PALMYRENE", 0x10860, 0x1087F)
    val NABATAEAN  = new UnicodeBlock("NABATAEAN", 0x10880, 0x108AF)
    val HATRAN     = new UnicodeBlock("HATRAN", 0x108E0, 0x108FF)
    val PHOENICIAN = new UnicodeBlock("PHOENICIAN", 0x10900, 0x1091F)
    val LYDIAN     = new UnicodeBlock("LYDIAN", 0x10920, 0x1093F)
    val MEROITIC_HIEROGLYPHS =
      new UnicodeBlock("MEROITIC_HIEROGLYPHS", 0x10980, 0x1099F)
    val MEROITIC_CURSIVE =
      new UnicodeBlock("MEROITIC_CURSIVE", 0x109A0, 0x109FF)
    val KHAROSHTHI = new UnicodeBlock("KHAROSHTHI", 0x10A00, 0x10A5F)
    val OLD_SOUTH_ARABIAN =
      new UnicodeBlock("OLD_SOUTH_ARABIAN", 0x10A60, 0x10A7F)
    val OLD_NORTH_ARABIAN =
      new UnicodeBlock("OLD_NORTH_ARABIAN", 0x10A80, 0x10A9F)
    val MANICHAEAN = new UnicodeBlock("MANICHAEAN", 0x10AC0, 0x10AFF)
    val AVESTAN    = new UnicodeBlock("AVESTAN", 0x10B00, 0x10B3F)
    val INSCRIPTIONAL_PARTHIAN =
      new UnicodeBlock("INSCRIPTIONAL_PARTHIAN", 0x10B40, 0x10B5F)
    val INSCRIPTIONAL_PAHLAVI =
      new UnicodeBlock("INSCRIPTIONAL_PAHLAVI", 0x10B60, 0x10B7F)
    val PSALTER_PAHLAVI = new UnicodeBlock("PSALTER_PAHLAVI", 0x10B80, 0x10BAF)
    val OLD_TURKIC      = new UnicodeBlock("OLD_TURKIC", 0x10C00, 0x10C4F)
    val OLD_HUNGARIAN   = new UnicodeBlock("OLD_HUNGARIAN", 0x10C80, 0x10CFF)
    val HANIFI_ROHINGYA = new UnicodeBlock("HANIFI_ROHINGYA", 0x10D00, 0x10D3F)
    val RUMI_NUMERAL_SYMBOLS =
      new UnicodeBlock("RUMI_NUMERAL_SYMBOLS", 0x10E60, 0x10E7F)
    val OLD_SOGDIAN  = new UnicodeBlock("OLD_SOGDIAN", 0x10F00, 0x10F2F)
    val SOGDIAN      = new UnicodeBlock("SOGDIAN", 0x10F30, 0x10F6F)
    val ELYMAIC      = new UnicodeBlock("ELYMAIC", 0x10FE0, 0x10FFF)
    val BRAHMI       = new UnicodeBlock("BRAHMI", 0x11000, 0x1107F)
    val KAITHI       = new UnicodeBlock("KAITHI", 0x11080, 0x110CF)
    val SORA_SOMPENG = new UnicodeBlock("SORA_SOMPENG", 0x110D0, 0x110FF)
    val CHAKMA       = new UnicodeBlock("CHAKMA", 0x11100, 0x1114F)
    val MAHAJANI     = new UnicodeBlock("MAHAJANI", 0x11150, 0x1117F)
    val SHARADA      = new UnicodeBlock("SHARADA", 0x11180, 0x111DF)
    val SINHALA_ARCHAIC_NUMBERS =
      new UnicodeBlock("SINHALA_ARCHAIC_NUMBERS", 0x111E0, 0x111FF)
    val KHOJKI    = new UnicodeBlock("KHOJKI", 0x11200, 0x1124F)
    val MULTANI   = new UnicodeBlock("MULTANI", 0x11280, 0x112AF)
    val KHUDAWADI = new UnicodeBlock("KHUDAWADI", 0x112B0, 0x112FF)
    val GRANTHA   = new UnicodeBlock("GRANTHA", 0x11300, 0x1137F)
    val NEWA      = new UnicodeBlock("NEWA", 0x11400, 0x1147F)
    val TIRHUTA   = new UnicodeBlock("TIRHUTA", 0x11480, 0x114DF)
    val SIDDHAM   = new UnicodeBlock("SIDDHAM", 0x11580, 0x115FF)
    val MODI      = new UnicodeBlock("MODI", 0x11600, 0x1165F)
    val MONGOLIAN_SUPPLEMENT =
      new UnicodeBlock("MONGOLIAN_SUPPLEMENT", 0x11660, 0x1167F)
    val TAKRI       = new UnicodeBlock("TAKRI", 0x11680, 0x116CF)
    val AHOM        = new UnicodeBlock("AHOM", 0x11700, 0x1173F)
    val DOGRA       = new UnicodeBlock("DOGRA", 0x11800, 0x1184F)
    val WARANG_CITI = new UnicodeBlock("WARANG_CITI", 0x118A0, 0x118FF)
    val NANDINAGARI = new UnicodeBlock("NANDINAGARI", 0x119A0, 0x119FF)
    val ZANABAZAR_SQUARE =
      new UnicodeBlock("ZANABAZAR_SQUARE", 0x11A00, 0x11A4F)
    val SOYOMBO       = new UnicodeBlock("SOYOMBO", 0x11A50, 0x11AAF)
    val PAU_CIN_HAU   = new UnicodeBlock("PAU_CIN_HAU", 0x11AC0, 0x11AFF)
    val BHAIKSUKI     = new UnicodeBlock("BHAIKSUKI", 0x11C00, 0x11C6F)
    val MARCHEN       = new UnicodeBlock("MARCHEN", 0x11C70, 0x11CBF)
    val MASARAM_GONDI = new UnicodeBlock("MASARAM_GONDI", 0x11D00, 0x11D5F)
    val GUNJALA_GONDI = new UnicodeBlock("GUNJALA_GONDI", 0x11D60, 0x11DAF)
    val MAKASAR       = new UnicodeBlock("MAKASAR", 0x11EE0, 0x11EFF)
    val TAMIL_SUPPLEMENT =
      new UnicodeBlock("TAMIL_SUPPLEMENT", 0x11FC0, 0x11FFF)
    val CUNEIFORM = new UnicodeBlock("CUNEIFORM", 0x12000, 0x123FF)
    val CUNEIFORM_NUMBERS_AND_PUNCTUATION =
      new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION", 0x12400, 0x1247F)
    val EARLY_DYNASTIC_CUNEIFORM =
      new UnicodeBlock("EARLY_DYNASTIC_CUNEIFORM", 0x12480, 0x1254F)
    val EGYPTIAN_HIEROGLYPHS =
      new UnicodeBlock("EGYPTIAN_HIEROGLYPHS", 0x13000, 0x1342F)
    val EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS =
      new UnicodeBlock("EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS", 0x13430, 0x1343F)
    val BAMUM_SUPPLEMENT =
      new UnicodeBlock("BAMUM_SUPPLEMENT", 0x16800, 0x16A3F)
    val MRO          = new UnicodeBlock("MRO", 0x16A40, 0x16A6F)
    val BASSA_VAH    = new UnicodeBlock("BASSA_VAH", 0x16AD0, 0x16AFF)
    val PAHAWH_HMONG = new UnicodeBlock("PAHAWH_HMONG", 0x16B00, 0x16B8F)
    val MEDEFAIDRIN  = new UnicodeBlock("MEDEFAIDRIN", 0x16E40, 0x16E9F)
    val MIAO         = new UnicodeBlock("MIAO", 0x16F00, 0x16F9F)
    val TANGUT       = new UnicodeBlock("TANGUT", 0x17000, 0x187FF)
    val TANGUT_COMPONENTS =
      new UnicodeBlock("TANGUT_COMPONENTS", 0x18800, 0x18AFF)
    val KANA_SUPPLEMENT = new UnicodeBlock("KANA_SUPPLEMENT", 0x1B000, 0x1B0FF)
    val KANA_EXTENDED_A = new UnicodeBlock("KANA_EXTENDED_A", 0x1B100, 0x1B12F)
    val SMALL_KANA_EXTENSION =
      new UnicodeBlock("SMALL_KANA_EXTENSION", 0x1B130, 0x1B16F)
    val NUSHU    = new UnicodeBlock("NUSHU", 0x1B170, 0x1B2FF)
    val DUPLOYAN = new UnicodeBlock("DUPLOYAN", 0x1BC00, 0x1BC9F)
    val SHORTHAND_FORMAT_CONTROLS =
      new UnicodeBlock("SHORTHAND_FORMAT_CONTROLS", 0x1BCA0, 0x1BCAF)
    val BYZANTINE_MUSICAL_SYMBOLS =
      new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS", 0x1D000, 0x1D0FF)
    val MUSICAL_SYMBOLS = new UnicodeBlock("MUSICAL_SYMBOLS", 0x1D100, 0x1D1FF)
    val ANCIENT_GREEK_MUSICAL_NOTATION =
      new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION", 0x1D200, 0x1D24F)
    val MAYAN_NUMERALS = new UnicodeBlock("MAYAN_NUMERALS", 0x1D2E0, 0x1D2FF)
    val TAI_XUAN_JING_SYMBOLS =
      new UnicodeBlock("TAI_XUAN_JING_SYMBOLS", 0x1D300, 0x1D35F)
    val COUNTING_ROD_NUMERALS =
      new UnicodeBlock("COUNTING_ROD_NUMERALS", 0x1D360, 0x1D37F)
    val MATHEMATICAL_ALPHANUMERIC_SYMBOLS =
      new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 0x1D400, 0x1D7FF)
    val GLAGOLITIC_SUPPLEMENT =
      new UnicodeBlock("GLAGOLITIC_SUPPLEMENT", 0x1E000, 0x1E02F)
    val NYIAKENG_PUACHUE_HMONG =
      new UnicodeBlock("NYIAKENG_PUACHUE_HMONG", 0x1E100, 0x1E14F)
    val WANCHO        = new UnicodeBlock("WANCHO", 0x1E2C0, 0x1E2FF)
    val MENDE_KIKAKUI = new UnicodeBlock("MENDE_KIKAKUI", 0x1E800, 0x1E8DF)
    val ADLAM         = new UnicodeBlock("ADLAM", 0x1E900, 0x1E95F)
    val INDIC_SIYAQ_NUMBERS =
      new UnicodeBlock("INDIC_SIYAQ_NUMBERS", 0x1EC70, 0x1ECBF)
    val OTTOMAN_SIYAQ_NUMBERS =
      new UnicodeBlock("OTTOMAN_SIYAQ_NUMBERS", 0x1ED00, 0x1ED4F)
    val ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS = new UnicodeBlock(
      "ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS",
      0x1EE00,
      0x1EEFF)
    val MAHJONG_TILES = new UnicodeBlock("MAHJONG_TILES", 0x1F000, 0x1F02F)
    val DOMINO_TILES  = new UnicodeBlock("DOMINO_TILES", 0x1F030, 0x1F09F)
    val PLAYING_CARDS = new UnicodeBlock("PLAYING_CARDS", 0x1F0A0, 0x1F0FF)
    val ENCLOSED_ALPHANUMERIC_SUPPLEMENT =
      new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT", 0x1F100, 0x1F1FF)
    val ENCLOSED_IDEOGRAPHIC_SUPPLEMENT =
      new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT", 0x1F200, 0x1F2FF)
    val EMOTICONS = new UnicodeBlock("EMOTICONS", 0x1F600, 0x1F64F)
    val ORNAMENTAL_DINGBATS =
      new UnicodeBlock("ORNAMENTAL_DINGBATS", 0x1F650, 0x1F67F)
    val ANATOLIAN_HIEROGLYPHS =
      new UnicodeBlock("ANATOLIAN_HIEROGLYPHS", 0x1F700, 0x1F77F)
    val GEOMETRIC_SHAPES_EXTENDED =
      new UnicodeBlock("GEOMETRIC_SHAPES_EXTENDED", 0x1F780, 0x1F7FF)
    val SUPPLEMENTAL_ARROWS_C =
      new UnicodeBlock("SUPPLEMENTAL_ARROWS_C", 0x1F800, 0x1F8FF)
    val CHESS_SYMBOLS = new UnicodeBlock("CHESS_SYMBOLS", 0x1FA00, 0x1FA6F)
    val CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", 0x20000, 0x2A6DF)
    val CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C", 0x2A700, 0x2B73F)
    val CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D", 0x2B740, 0x2B81F)
    val CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E", 0x2B820, 0x2CEAF)
    val CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F =
      new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F", 0x2CEB0, 0x2EBEF)
    val CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = new UnicodeBlock(
      "CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT",
      0x2F800,
      0x2FA1F)
    val TAGS = new UnicodeBlock("TAGS", 0xE0000, 0xE007F)
    val VARIATION_SELECTORS_SUPPLEMENT =
      new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT", 0xE0100, 0xE01EF)
    val SUPPLEMENTARY_PRIVATE_USE_AREA_A =
      new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", 0xF0000, 0xFFFFF)
    val SUPPLEMENTARY_PRIVATE_USE_AREA_B =
      new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", 0x100000, 0x10FFFF)
    // scalastyle:on line.size.limit

    private val allBlocks: Array[UnicodeBlock] = Array(
      BASIC_LATIN,
      LATIN_1_SUPPLEMENT,
      LATIN_EXTENDED_A,
      LATIN_EXTENDED_B,
      IPA_EXTENSIONS,
      SPACING_MODIFIER_LETTERS,
      COMBINING_DIACRITICAL_MARKS,
      GREEK,
      CYRILLIC,
      CYRILLIC_SUPPLEMENTARY,
      ARMENIAN,
      HEBREW,
      ARABIC,
      SYRIAC,
      ARABIC_SUPPLEMENT,
      THAANA,
      SAMARITAN,
      MANDAIC,
      SYRIAC_SUPPLEMENT,
      ARABIC_EXTENDED_A,
      DEVANAGARI,
      BENGALI,
      GURMUKHI,
      GUJARATI,
      ORIYA,
      TAMIL,
      TELUGU,
      KANNADA,
      MALAYALAM,
      SINHALA,
      THAI,
      LAO,
      TIBETAN,
      MYANMAR,
      GEORGIAN,
      HANGUL_JAMO,
      ETHIOPIC,
      ETHIOPIC_SUPPLEMENT,
      CHEROKEE,
      UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,
      OGHAM,
      RUNIC,
      TAGALOG,
      HANUNOO,
      BUHID,
      TAGBANWA,
      KHMER,
      MONGOLIAN,
      UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED,
      LIMBU,
      TAI_LE,
      NEW_TAI_LUE,
      KHMER_SYMBOLS,
      BUGINESE,
      TAI_THAM,
      COMBINING_DIACRITICAL_MARKS_EXTENDED,
      BALINESE,
      SUNDANESE,
      BATAK,
      LEPCHA,
      OL_CHIKI,
      CYRILLIC_EXTENDED_C,
      GEORGIAN_EXTENDED,
      SUNDANESE_SUPPLEMENT,
      VEDIC_EXTENSIONS,
      PHONETIC_EXTENSIONS,
      PHONETIC_EXTENSIONS_SUPPLEMENT,
      COMBINING_DIACRITICAL_MARKS_SUPPLEMENT,
      LATIN_EXTENDED_ADDITIONAL,
      GREEK_EXTENDED,
      GENERAL_PUNCTUATION,
      SUPERSCRIPTS_AND_SUBSCRIPTS,
      CURRENCY_SYMBOLS,
      COMBINING_MARKS_FOR_SYMBOLS,
      LETTERLIKE_SYMBOLS,
      NUMBER_FORMS,
      ARROWS,
      MATHEMATICAL_OPERATORS,
      MISCELLANEOUS_TECHNICAL,
      CONTROL_PICTURES,
      OPTICAL_CHARACTER_RECOGNITION,
      ENCLOSED_ALPHANUMERICS,
      BOX_DRAWING,
      BLOCK_ELEMENTS,
      GEOMETRIC_SHAPES,
      MISCELLANEOUS_SYMBOLS,
      DINGBATS,
      MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A,
      SUPPLEMENTAL_ARROWS_A,
      BRAILLE_PATTERNS,
      SUPPLEMENTAL_ARROWS_B,
      MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,
      SUPPLEMENTAL_MATHEMATICAL_OPERATORS,
      MISCELLANEOUS_SYMBOLS_AND_ARROWS,
      GLAGOLITIC,
      LATIN_EXTENDED_C,
      COPTIC,
      GEORGIAN_SUPPLEMENT,
      TIFINAGH,
      ETHIOPIC_EXTENDED,
      CYRILLIC_EXTENDED_A,
      SUPPLEMENTAL_PUNCTUATION,
      CJK_RADICALS_SUPPLEMENT,
      KANGXI_RADICALS,
      IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
      CJK_SYMBOLS_AND_PUNCTUATION,
      HIRAGANA,
      KATAKANA,
      BOPOMOFO,
      HANGUL_COMPATIBILITY_JAMO,
      KANBUN,
      BOPOMOFO_EXTENDED,
      CJK_STROKES,
      KATAKANA_PHONETIC_EXTENSIONS,
      ENCLOSED_CJK_LETTERS_AND_MONTHS,
      CJK_COMPATIBILITY,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
      YIJING_HEXAGRAM_SYMBOLS,
      CJK_UNIFIED_IDEOGRAPHS,
      YI_SYLLABLES,
      YI_RADICALS,
      LISU,
      VAI,
      CYRILLIC_EXTENDED_B,
      BAMUM,
      MODIFIER_TONE_LETTERS,
      LATIN_EXTENDED_D,
      SYLOTI_NAGRI,
      COMMON_INDIC_NUMBER_FORMS,
      PHAGS_PA,
      SAURASHTRA,
      DEVANAGARI_EXTENDED,
      KAYAH_LI,
      REJANG,
      HANGUL_JAMO_EXTENDED_A,
      JAVANESE,
      MYANMAR_EXTENDED_B,
      CHAM,
      MYANMAR_EXTENDED_A,
      TAI_VIET,
      MEETEI_MAYEK_EXTENSIONS,
      ETHIOPIC_EXTENDED_A,
      LATIN_EXTENDED_E,
      CHEROKEE_SUPPLEMENT,
      MEETEI_MAYEK,
      HANGUL_SYLLABLES,
      HANGUL_JAMO_EXTENDED_B,
      HIGH_SURROGATES,
      HIGH_PRIVATE_USE_SURROGATES,
      LOW_SURROGATES,
      PRIVATE_USE_AREA,
      CJK_COMPATIBILITY_IDEOGRAPHS,
      ALPHABETIC_PRESENTATION_FORMS,
      ARABIC_PRESENTATION_FORMS_A,
      VARIATION_SELECTORS,
      VERTICAL_FORMS,
      COMBINING_HALF_MARKS,
      CJK_COMPATIBILITY_FORMS,
      SMALL_FORM_VARIANTS,
      ARABIC_PRESENTATION_FORMS_B,
      HALFWIDTH_AND_FULLWIDTH_FORMS,
      SPECIALS,
      LINEAR_B_SYLLABARY,
      LINEAR_B_IDEOGRAMS,
      AEGEAN_NUMBERS,
      ANCIENT_GREEK_NUMBERS,
      ANCIENT_SYMBOLS,
      PHAISTOS_DISC,
      LYCIAN,
      CARIAN,
      COPTIC_EPACT_NUMBERS,
      OLD_ITALIC,
      GOTHIC,
      OLD_PERMIC,
      UGARITIC,
      OLD_PERSIAN,
      DESERET,
      SHAVIAN,
      OSMANYA,
      OSAGE,
      ELBASAN,
      CAUCASIAN_ALBANIAN,
      LINEAR_A,
      CYPRIOT_SYLLABARY,
      IMPERIAL_ARAMAIC,
      PALMYRENE,
      NABATAEAN,
      HATRAN,
      PHOENICIAN,
      LYDIAN,
      MEROITIC_HIEROGLYPHS,
      MEROITIC_CURSIVE,
      KHAROSHTHI,
      OLD_SOUTH_ARABIAN,
      OLD_NORTH_ARABIAN,
      MANICHAEAN,
      AVESTAN,
      INSCRIPTIONAL_PARTHIAN,
      INSCRIPTIONAL_PAHLAVI,
      PSALTER_PAHLAVI,
      OLD_TURKIC,
      OLD_HUNGARIAN,
      HANIFI_ROHINGYA,
      RUMI_NUMERAL_SYMBOLS,
      OLD_SOGDIAN,
      SOGDIAN,
      ELYMAIC,
      BRAHMI,
      KAITHI,
      SORA_SOMPENG,
      CHAKMA,
      MAHAJANI,
      SHARADA,
      SINHALA_ARCHAIC_NUMBERS,
      KHOJKI,
      MULTANI,
      KHUDAWADI,
      GRANTHA,
      NEWA,
      TIRHUTA,
      SIDDHAM,
      MODI,
      MONGOLIAN_SUPPLEMENT,
      TAKRI,
      AHOM,
      DOGRA,
      WARANG_CITI,
      NANDINAGARI,
      ZANABAZAR_SQUARE,
      SOYOMBO,
      PAU_CIN_HAU,
      BHAIKSUKI,
      MARCHEN,
      MASARAM_GONDI,
      GUNJALA_GONDI,
      MAKASAR,
      TAMIL_SUPPLEMENT,
      CUNEIFORM,
      CUNEIFORM_NUMBERS_AND_PUNCTUATION,
      EARLY_DYNASTIC_CUNEIFORM,
      EGYPTIAN_HIEROGLYPHS,
      EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS,
      BAMUM_SUPPLEMENT,
      MRO,
      BASSA_VAH,
      PAHAWH_HMONG,
      MEDEFAIDRIN,
      MIAO,
      TANGUT,
      TANGUT_COMPONENTS,
      KANA_SUPPLEMENT,
      KANA_EXTENDED_A,
      SMALL_KANA_EXTENSION,
      NUSHU,
      DUPLOYAN,
      SHORTHAND_FORMAT_CONTROLS,
      BYZANTINE_MUSICAL_SYMBOLS,
      MUSICAL_SYMBOLS,
      ANCIENT_GREEK_MUSICAL_NOTATION,
      MAYAN_NUMERALS,
      TAI_XUAN_JING_SYMBOLS,
      COUNTING_ROD_NUMERALS,
      MATHEMATICAL_ALPHANUMERIC_SYMBOLS,
      GLAGOLITIC_SUPPLEMENT,
      NYIAKENG_PUACHUE_HMONG,
      WANCHO,
      MENDE_KIKAKUI,
      ADLAM,
      INDIC_SIYAQ_NUMBERS,
      OTTOMAN_SIYAQ_NUMBERS,
      ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS,
      MAHJONG_TILES,
      DOMINO_TILES,
      PLAYING_CARDS,
      ENCLOSED_ALPHANUMERIC_SUPPLEMENT,
      ENCLOSED_IDEOGRAPHIC_SUPPLEMENT,
      EMOTICONS,
      ORNAMENTAL_DINGBATS,
      ANATOLIAN_HIEROGLYPHS,
      GEOMETRIC_SHAPES_EXTENDED,
      SUPPLEMENTAL_ARROWS_C,
      CHESS_SYMBOLS,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E,
      CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F,
      CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
      TAGS,
      VARIATION_SELECTORS_SUPPLEMENT,
      SUPPLEMENTARY_PRIVATE_USE_AREA_A,
      SUPPLEMENTARY_PRIVATE_USE_AREA_B
    )

    import scala.Predef.ArrowAssoc
    private val blocksByNormalizedName
        : scala.collection.Map[String, UnicodeBlock] = scala.collection.Map(
      "basiclatin"                                 -> BASIC_LATIN,
      "latin1supplement"                           -> LATIN_1_SUPPLEMENT,
      "latinextendeda"                             -> LATIN_EXTENDED_A,
      "latinextendedb"                             -> LATIN_EXTENDED_B,
      "ipaextensions"                              -> IPA_EXTENSIONS,
      "spacingmodifierletters"                     -> SPACING_MODIFIER_LETTERS,
      "combiningdiacriticalmarks"                  -> COMBINING_DIACRITICAL_MARKS,
      "greekandcoptic"                             -> GREEK,
      "cyrillic"                                   -> CYRILLIC,
      "cyrillicsupplement"                         -> CYRILLIC_SUPPLEMENTARY,
      "armenian"                                   -> ARMENIAN,
      "hebrew"                                     -> HEBREW,
      "arabic"                                     -> ARABIC,
      "syriac"                                     -> SYRIAC,
      "arabicsupplement"                           -> ARABIC_SUPPLEMENT,
      "thaana"                                     -> THAANA,
      "samaritan"                                  -> SAMARITAN,
      "mandaic"                                    -> MANDAIC,
      "syriacsupplement"                           -> SYRIAC_SUPPLEMENT,
      "arabicextendeda"                            -> ARABIC_EXTENDED_A,
      "devanagari"                                 -> DEVANAGARI,
      "bengali"                                    -> BENGALI,
      "gurmukhi"                                   -> GURMUKHI,
      "gujarati"                                   -> GUJARATI,
      "oriya"                                      -> ORIYA,
      "tamil"                                      -> TAMIL,
      "telugu"                                     -> TELUGU,
      "kannada"                                    -> KANNADA,
      "malayalam"                                  -> MALAYALAM,
      "sinhala"                                    -> SINHALA,
      "thai"                                       -> THAI,
      "lao"                                        -> LAO,
      "tibetan"                                    -> TIBETAN,
      "myanmar"                                    -> MYANMAR,
      "georgian"                                   -> GEORGIAN,
      "hanguljamo"                                 -> HANGUL_JAMO,
      "ethiopic"                                   -> ETHIOPIC,
      "ethiopicsupplement"                         -> ETHIOPIC_SUPPLEMENT,
      "cherokee"                                   -> CHEROKEE,
      "unifiedcanadianaboriginalsyllabics"         -> UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,
      "ogham"                                      -> OGHAM,
      "runic"                                      -> RUNIC,
      "tagalog"                                    -> TAGALOG,
      "hanunoo"                                    -> HANUNOO,
      "buhid"                                      -> BUHID,
      "tagbanwa"                                   -> TAGBANWA,
      "khmer"                                      -> KHMER,
      "mongolian"                                  -> MONGOLIAN,
      "unifiedcanadianaboriginalsyllabicsextended" -> UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED,
      "limbu"                                      -> LIMBU,
      "taile"                                      -> TAI_LE,
      "newtailue"                                  -> NEW_TAI_LUE,
      "khmersymbols"                               -> KHMER_SYMBOLS,
      "buginese"                                   -> BUGINESE,
      "taitham"                                    -> TAI_THAM,
      "combiningdiacriticalmarksextended"          -> COMBINING_DIACRITICAL_MARKS_EXTENDED,
      "balinese"                                   -> BALINESE,
      "sundanese"                                  -> SUNDANESE,
      "batak"                                      -> BATAK,
      "lepcha"                                     -> LEPCHA,
      "olchiki"                                    -> OL_CHIKI,
      "cyrillicextendedc"                          -> CYRILLIC_EXTENDED_C,
      "georgianextended"                           -> GEORGIAN_EXTENDED,
      "sundanesesupplement"                        -> SUNDANESE_SUPPLEMENT,
      "vedicextensions"                            -> VEDIC_EXTENSIONS,
      "phoneticextensions"                         -> PHONETIC_EXTENSIONS,
      "phoneticextensionssupplement"               -> PHONETIC_EXTENSIONS_SUPPLEMENT,
      "combiningdiacriticalmarkssupplement"        -> COMBINING_DIACRITICAL_MARKS_SUPPLEMENT,
      "latinextendedadditional"                    -> LATIN_EXTENDED_ADDITIONAL,
      "greekextended"                              -> GREEK_EXTENDED,
      "generalpunctuation"                         -> GENERAL_PUNCTUATION,
      "superscriptsandsubscripts"                  -> SUPERSCRIPTS_AND_SUBSCRIPTS,
      "currencysymbols"                            -> CURRENCY_SYMBOLS,
      "combiningdiacriticalmarksforsymbols"        -> COMBINING_MARKS_FOR_SYMBOLS,
      "letterlikesymbols"                          -> LETTERLIKE_SYMBOLS,
      "numberforms"                                -> NUMBER_FORMS,
      "arrows"                                     -> ARROWS,
      "mathematicaloperators"                      -> MATHEMATICAL_OPERATORS,
      "miscellaneoustechnical"                     -> MISCELLANEOUS_TECHNICAL,
      "controlpictures"                            -> CONTROL_PICTURES,
      "opticalcharacterrecognition"                -> OPTICAL_CHARACTER_RECOGNITION,
      "enclosedalphanumerics"                      -> ENCLOSED_ALPHANUMERICS,
      "boxdrawing"                                 -> BOX_DRAWING,
      "blockelements"                              -> BLOCK_ELEMENTS,
      "geometricshapes"                            -> GEOMETRIC_SHAPES,
      "miscellaneoussymbols"                       -> MISCELLANEOUS_SYMBOLS,
      "dingbats"                                   -> DINGBATS,
      "miscellaneousmathematicalsymbolsa"          -> MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A,
      "supplementalarrowsa"                        -> SUPPLEMENTAL_ARROWS_A,
      "braillepatterns"                            -> BRAILLE_PATTERNS,
      "supplementalarrowsb"                        -> SUPPLEMENTAL_ARROWS_B,
      "miscellaneousmathematicalsymbolsb"          -> MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,
      "supplementalmathematicaloperators"          -> SUPPLEMENTAL_MATHEMATICAL_OPERATORS,
      "miscellaneoussymbolsandarrows"              -> MISCELLANEOUS_SYMBOLS_AND_ARROWS,
      "glagolitic"                                 -> GLAGOLITIC,
      "latinextendedc"                             -> LATIN_EXTENDED_C,
      "coptic"                                     -> COPTIC,
      "georgiansupplement"                         -> GEORGIAN_SUPPLEMENT,
      "tifinagh"                                   -> TIFINAGH,
      "ethiopicextended"                           -> ETHIOPIC_EXTENDED,
      "cyrillicextendeda"                          -> CYRILLIC_EXTENDED_A,
      "supplementalpunctuation"                    -> SUPPLEMENTAL_PUNCTUATION,
      "cjkradicalssupplement"                      -> CJK_RADICALS_SUPPLEMENT,
      "kangxiradicals"                             -> KANGXI_RADICALS,
      "ideographicdescriptioncharacters"           -> IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
      "cjksymbolsandpunctuation"                   -> CJK_SYMBOLS_AND_PUNCTUATION,
      "hiragana"                                   -> HIRAGANA,
      "katakana"                                   -> KATAKANA,
      "bopomofo"                                   -> BOPOMOFO,
      "hangulcompatibilityjamo"                    -> HANGUL_COMPATIBILITY_JAMO,
      "kanbun"                                     -> KANBUN,
      "bopomofoextended"                           -> BOPOMOFO_EXTENDED,
      "cjkstrokes"                                 -> CJK_STROKES,
      "katakanaphoneticextensions"                 -> KATAKANA_PHONETIC_EXTENSIONS,
      "enclosedcjklettersandmonths"                -> ENCLOSED_CJK_LETTERS_AND_MONTHS,
      "cjkcompatibility"                           -> CJK_COMPATIBILITY,
      "cjkunifiedideographsextensiona"             -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
      "yijinghexagramsymbols"                      -> YIJING_HEXAGRAM_SYMBOLS,
      "cjkunifiedideographs"                       -> CJK_UNIFIED_IDEOGRAPHS,
      "yisyllables"                                -> YI_SYLLABLES,
      "yiradicals"                                 -> YI_RADICALS,
      "lisu"                                       -> LISU,
      "vai"                                        -> VAI,
      "cyrillicextendedb"                          -> CYRILLIC_EXTENDED_B,
      "bamum"                                      -> BAMUM,
      "modifiertoneletters"                        -> MODIFIER_TONE_LETTERS,
      "latinextendedd"                             -> LATIN_EXTENDED_D,
      "sylotinagri"                                -> SYLOTI_NAGRI,
      "commonindicnumberforms"                     -> COMMON_INDIC_NUMBER_FORMS,
      "phagspa"                                    -> PHAGS_PA,
      "saurashtra"                                 -> SAURASHTRA,
      "devanagariextended"                         -> DEVANAGARI_EXTENDED,
      "kayahli"                                    -> KAYAH_LI,
      "rejang"                                     -> REJANG,
      "hanguljamoextendeda"                        -> HANGUL_JAMO_EXTENDED_A,
      "javanese"                                   -> JAVANESE,
      "myanmarextendedb"                           -> MYANMAR_EXTENDED_B,
      "cham"                                       -> CHAM,
      "myanmarextendeda"                           -> MYANMAR_EXTENDED_A,
      "taiviet"                                    -> TAI_VIET,
      "meeteimayekextensions"                      -> MEETEI_MAYEK_EXTENSIONS,
      "ethiopicextendeda"                          -> ETHIOPIC_EXTENDED_A,
      "latinextendede"                             -> LATIN_EXTENDED_E,
      "cherokeesupplement"                         -> CHEROKEE_SUPPLEMENT,
      "meeteimayek"                                -> MEETEI_MAYEK,
      "hangulsyllables"                            -> HANGUL_SYLLABLES,
      "hanguljamoextendedb"                        -> HANGUL_JAMO_EXTENDED_B,
      "highsurrogates"                             -> HIGH_SURROGATES,
      "highprivateusesurrogates"                   -> HIGH_PRIVATE_USE_SURROGATES,
      "lowsurrogates"                              -> LOW_SURROGATES,
      "privateusearea"                             -> PRIVATE_USE_AREA,
      "cjkcompatibilityideographs"                 -> CJK_COMPATIBILITY_IDEOGRAPHS,
      "alphabeticpresentationforms"                -> ALPHABETIC_PRESENTATION_FORMS,
      "arabicpresentationformsa"                   -> ARABIC_PRESENTATION_FORMS_A,
      "variationselectors"                         -> VARIATION_SELECTORS,
      "verticalforms"                              -> VERTICAL_FORMS,
      "combininghalfmarks"                         -> COMBINING_HALF_MARKS,
      "cjkcompatibilityforms"                      -> CJK_COMPATIBILITY_FORMS,
      "smallformvariants"                          -> SMALL_FORM_VARIANTS,
      "arabicpresentationformsb"                   -> ARABIC_PRESENTATION_FORMS_B,
      "halfwidthandfullwidthforms"                 -> HALFWIDTH_AND_FULLWIDTH_FORMS,
      "specials"                                   -> SPECIALS,
      "linearbsyllabary"                           -> LINEAR_B_SYLLABARY,
      "linearbideograms"                           -> LINEAR_B_IDEOGRAMS,
      "aegeannumbers"                              -> AEGEAN_NUMBERS,
      "ancientgreeknumbers"                        -> ANCIENT_GREEK_NUMBERS,
      "ancientsymbols"                             -> ANCIENT_SYMBOLS,
      "phaistosdisc"                               -> PHAISTOS_DISC,
      "lycian"                                     -> LYCIAN,
      "carian"                                     -> CARIAN,
      "copticepactnumbers"                         -> COPTIC_EPACT_NUMBERS,
      "olditalic"                                  -> OLD_ITALIC,
      "gothic"                                     -> GOTHIC,
      "oldpermic"                                  -> OLD_PERMIC,
      "ugaritic"                                   -> UGARITIC,
      "oldpersian"                                 -> OLD_PERSIAN,
      "deseret"                                    -> DESERET,
      "shavian"                                    -> SHAVIAN,
      "osmanya"                                    -> OSMANYA,
      "osage"                                      -> OSAGE,
      "elbasan"                                    -> ELBASAN,
      "caucasianalbanian"                          -> CAUCASIAN_ALBANIAN,
      "lineara"                                    -> LINEAR_A,
      "cypriotsyllabary"                           -> CYPRIOT_SYLLABARY,
      "imperialaramaic"                            -> IMPERIAL_ARAMAIC,
      "palmyrene"                                  -> PALMYRENE,
      "nabataean"                                  -> NABATAEAN,
      "hatran"                                     -> HATRAN,
      "phoenician"                                 -> PHOENICIAN,
      "lydian"                                     -> LYDIAN,
      "meroitichieroglyphs"                        -> MEROITIC_HIEROGLYPHS,
      "meroiticcursive"                            -> MEROITIC_CURSIVE,
      "kharoshthi"                                 -> KHAROSHTHI,
      "oldsoutharabian"                            -> OLD_SOUTH_ARABIAN,
      "oldnortharabian"                            -> OLD_NORTH_ARABIAN,
      "manichaean"                                 -> MANICHAEAN,
      "avestan"                                    -> AVESTAN,
      "inscriptionalparthian"                      -> INSCRIPTIONAL_PARTHIAN,
      "inscriptionalpahlavi"                       -> INSCRIPTIONAL_PAHLAVI,
      "psalterpahlavi"                             -> PSALTER_PAHLAVI,
      "oldturkic"                                  -> OLD_TURKIC,
      "oldhungarian"                               -> OLD_HUNGARIAN,
      "hanifirohingya"                             -> HANIFI_ROHINGYA,
      "ruminumeralsymbols"                         -> RUMI_NUMERAL_SYMBOLS,
      "oldsogdian"                                 -> OLD_SOGDIAN,
      "sogdian"                                    -> SOGDIAN,
      "elymaic"                                    -> ELYMAIC,
      "brahmi"                                     -> BRAHMI,
      "kaithi"                                     -> KAITHI,
      "sorasompeng"                                -> SORA_SOMPENG,
      "chakma"                                     -> CHAKMA,
      "mahajani"                                   -> MAHAJANI,
      "sharada"                                    -> SHARADA,
      "sinhalaarchaicnumbers"                      -> SINHALA_ARCHAIC_NUMBERS,
      "khojki"                                     -> KHOJKI,
      "multani"                                    -> MULTANI,
      "khudawadi"                                  -> KHUDAWADI,
      "grantha"                                    -> GRANTHA,
      "newa"                                       -> NEWA,
      "tirhuta"                                    -> TIRHUTA,
      "siddham"                                    -> SIDDHAM,
      "modi"                                       -> MODI,
      "mongoliansupplement"                        -> MONGOLIAN_SUPPLEMENT,
      "takri"                                      -> TAKRI,
      "ahom"                                       -> AHOM,
      "dogra"                                      -> DOGRA,
      "warangciti"                                 -> WARANG_CITI,
      "nandinagari"                                -> NANDINAGARI,
      "zanabazarsquare"                            -> ZANABAZAR_SQUARE,
      "soyombo"                                    -> SOYOMBO,
      "paucinhau"                                  -> PAU_CIN_HAU,
      "bhaiksuki"                                  -> BHAIKSUKI,
      "marchen"                                    -> MARCHEN,
      "masaramgondi"                               -> MASARAM_GONDI,
      "gunjalagondi"                               -> GUNJALA_GONDI,
      "makasar"                                    -> MAKASAR,
      "tamilsupplement"                            -> TAMIL_SUPPLEMENT,
      "cuneiform"                                  -> CUNEIFORM,
      "cuneiformnumbersandpunctuation"             -> CUNEIFORM_NUMBERS_AND_PUNCTUATION,
      "earlydynasticcuneiform"                     -> EARLY_DYNASTIC_CUNEIFORM,
      "egyptianhieroglyphs"                        -> EGYPTIAN_HIEROGLYPHS,
      "egyptianhieroglyphformatcontrols"           -> EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS,
      "bamumsupplement"                            -> BAMUM_SUPPLEMENT,
      "mro"                                        -> MRO,
      "bassavah"                                   -> BASSA_VAH,
      "pahawhhmong"                                -> PAHAWH_HMONG,
      "medefaidrin"                                -> MEDEFAIDRIN,
      "miao"                                       -> MIAO,
      "tangut"                                     -> TANGUT,
      "tangutcomponents"                           -> TANGUT_COMPONENTS,
      "kanasupplement"                             -> KANA_SUPPLEMENT,
      "kanaextendeda"                              -> KANA_EXTENDED_A,
      "smallkanaextension"                         -> SMALL_KANA_EXTENSION,
      "nushu"                                      -> NUSHU,
      "duployan"                                   -> DUPLOYAN,
      "shorthandformatcontrols"                    -> SHORTHAND_FORMAT_CONTROLS,
      "byzantinemusicalsymbols"                    -> BYZANTINE_MUSICAL_SYMBOLS,
      "musicalsymbols"                             -> MUSICAL_SYMBOLS,
      "ancientgreekmusicalnotation"                -> ANCIENT_GREEK_MUSICAL_NOTATION,
      "mayannumerals"                              -> MAYAN_NUMERALS,
      "taixuanjingsymbols"                         -> TAI_XUAN_JING_SYMBOLS,
      "countingrodnumerals"                        -> COUNTING_ROD_NUMERALS,
      "mathematicalalphanumericsymbols"            -> MATHEMATICAL_ALPHANUMERIC_SYMBOLS,
      "glagoliticsupplement"                       -> GLAGOLITIC_SUPPLEMENT,
      "nyiakengpuachuehmong"                       -> NYIAKENG_PUACHUE_HMONG,
      "wancho"                                     -> WANCHO,
      "mendekikakui"                               -> MENDE_KIKAKUI,
      "adlam"                                      -> ADLAM,
      "indicsiyaqnumbers"                          -> INDIC_SIYAQ_NUMBERS,
      "ottomansiyaqnumbers"                        -> OTTOMAN_SIYAQ_NUMBERS,
      "arabicmathematicalalphabeticsymbols"        -> ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS,
      "mahjongtiles"                               -> MAHJONG_TILES,
      "dominotiles"                                -> DOMINO_TILES,
      "playingcards"                               -> PLAYING_CARDS,
      "enclosedalphanumericsupplement"             -> ENCLOSED_ALPHANUMERIC_SUPPLEMENT,
      "enclosedideographicsupplement"              -> ENCLOSED_IDEOGRAPHIC_SUPPLEMENT,
      "emoticons"                                  -> EMOTICONS,
      "ornamentaldingbats"                         -> ORNAMENTAL_DINGBATS,
      "alchemicalsymbols"                          -> ANATOLIAN_HIEROGLYPHS,
      "geometricshapesextended"                    -> GEOMETRIC_SHAPES_EXTENDED,
      "supplementalarrowsc"                        -> SUPPLEMENTAL_ARROWS_C,
      "chesssymbols"                               -> CHESS_SYMBOLS,
      "cjkunifiedideographsextensionb"             -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
      "cjkunifiedideographsextensionc"             -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,
      "cjkunifiedideographsextensiond"             -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D,
      "cjkunifiedideographsextensione"             -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E,
      "cjkunifiedideographsextensionf"             -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F,
      "cjkcompatibilityideographssupplement"       -> CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
      "tags"                                       -> TAGS,
      "variationselectorssupplement"               -> VARIATION_SELECTORS_SUPPLEMENT,
      "supplementaryprivateuseareaa"               -> SUPPLEMENTARY_PRIVATE_USE_AREA_A,
      "supplementaryprivateuseareab"               -> SUPPLEMENTARY_PRIVATE_USE_AREA_B
    )

    // From: https://unicode.org/Public/UCD/latest/ucd/Blocks.txt
    // Note:   When comparing block names, casing, whitespace, hyphens,
    //         and underbars are ignored.
    //         For example, "Latin Extended-A" and "latin extended a" are equivalent.
    //         For more information on the comparison of property values,
    //           see UAX #44: http://www.unicode.org/reports/tr44/
    private def toNormalizedName(name: String): String = {
      name.toLowerCase.replaceAll(raw"[\s\-_]", "")
    }

    ////////////////
    // End Generated
    ////////////////

    def forName(blockName: String): UnicodeBlock = {
      blocksByNormalizedName
        .getOrElse(toNormalizedName(blockName),
                   throw new IllegalArgumentException())
    }

    def of(c: scala.Char): UnicodeBlock = of(c.toInt)

    def of(codePoint: scala.Int): UnicodeBlock = {
      // scalastyle:off return
      if (!Character.isValidCodePoint(codePoint)) {
        throw new IllegalArgumentException()
      }

      var low: Int  = 0
      var mid: Int  = -1
      var high: Int = allBlocks.length - 1

      while (low <= high) {
        mid = (low + high) >>> 1
        val block = allBlocks(mid)
        if (codePoint > block.end) {
          low = mid + 1
        } else if (codePoint >= block.start && codePoint <= block.end) {
          return block
        } else {
          high = mid - 1
        }
      }

      null

      // scalastyle:on return
    }
  }
  // TODO:
  // def getDirectionality(c: scala.Char): scala.Byte
  // def toTitleCase(c: scala.Char): scala.Char
  // def getNumericValue(c: scala.Char): Int
  // def reverseBytes(ch: scala.Char): scala.Char
  // ...
}

private[lang] object CharacterCache {
  private[lang] val cache = new Array[java.lang.Character](128)
}
