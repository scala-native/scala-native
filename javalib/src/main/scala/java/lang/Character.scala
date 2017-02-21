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
  protected def unary_~ : scala.Int = ~ _value.toInt
  protected def unary_+ : scala.Int = _value.toInt
  protected def unary_- : scala.Int = - _value.toInt
  // scalastyle:on disallow.space.before.token

  protected def +(x: String): String = _value + x

  protected def <<(x: scala.Int): scala.Int   = _value << x
  protected def <<(x: scala.Long): scala.Int  = _value << x
  protected def >>>(x: scala.Int): scala.Int  = _value >>> x
  protected def >>>(x: scala.Long): scala.Int = _value >>> x
  protected def >>(x: scala.Int): scala.Int   = _value >> x
  protected def >>(x: scala.Long): scala.Int  = _value >> x

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
  final val TYPE      = classOf[scala.Char]
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

  def codePointAt(seq: Array[scala.Char],
                  _index: scala.Int,
                  limit: scala.Int): scala.Int = {
    var index = _index
    if (index < 0 || index >= limit || limit < 0 || limit > seq.length) {
      throw new ArrayIndexOutOfBoundsException()
    }

    val high = seq(index)
    index += 1
    if (index >= limit) {
      high
    } else {
      val low = seq(index)
      if (isSurrogatePair(high, low))
        toCodePoint(high, low)
      else
        high
    }
  }

  def codePointBefore(seq: Array[scala.Char], _index: scala.Int): scala.Int = {
    var index = _index
    val len   = seq.length
    if (index < 1 || index > len) {
      throw new ArrayIndexOutOfBoundsException(index)
    }

    index -= 1
    val low = seq.charAt(index)
    index -= 1
    if (index < 0) {
      low
    } else {
      val high = seq(index)
      if (isSurrogatePair(high, low))
        toCodePoint(high, low)
      else
        low
    }
  }

  def codePointCount(seq: Array[scala.Char],
                     offset: scala.Int,
                     count: scala.Int): scala.Int = {
    val len      = seq.length
    val endIndex = offset + count
    if (offset < 0 || count < 0 || endIndex > len) {
      throw new IndexOutOfBoundsException()
    }

    var result = 0
    var i      = offset
    while (i <= endIndex) {
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
    if (start < 0 || count < 0 || end > seq.length || index < start || index > end) {
      throw new IndexOutOfBoundsException()
    }

    if (codePointOffset == 0) {
      index
    } else if (codePointOffset > 0) {
      var codePoints = codePointOffset
      var i          = index
      while (codePoints > 0) {
        codePoints -= 1
        if (i >= end) {
          throw new IndexOutOfBoundsException()
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
          throw new IndexOutOfBoundsException()
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

  def valueOf(charValue: scala.Char): Character = new Character(charValue)

  def getType(ch: scala.Char): Int = getType(ch.toInt)

  def getType(codePoint: Int): Int = {
    if (codePoint < 0) UNASSIGNED.toInt
    else if (codePoint < 256) getTypeLT256(codePoint)
    else getTypeGE256(codePoint)
  }

  @inline
  private[this] def getTypeLT256(codePoint: Int): scala.Byte =
    charTypesFirst256(codePoint)

  private[this] def getTypeGE256(codePoint: Int): scala.Byte = {
    // the idx is increased by 1 due to the differences in indexing
    // between charTypeIndices and charType
    val idx = Arrays.binarySearch(charTypeIndices, codePoint) + 1
    // in the case where idx is negative (-insertionPoint - 1)
    charTypes(Math.abs(idx))
  }

  def digit(c: scala.Char, radix: Int): Int = {
    if (radix > MAX_RADIX || radix < MIN_RADIX)
      -1
    else if (c >= '0' && c <= '9' && c - '0' < radix)
      c - '0'
    else if (c >= 'A' && c <= 'Z' && c - 'A' < radix - 10)
      c - 'A' + 10
    else if (c >= 'a' && c <= 'z' && c - 'a' < radix - 10)
      c - 'a' + 10
    else if (c >= '\uFF21' && c <= '\uFF3A' &&
             c - '\uFF21' < radix - 10)
      c - '\uFF21' + 10
    else if (c >= '\uFF41' && c <= '\uFF5A' &&
             c - '\uFF41' < radix - 10)
      c - '\uFF21' + 10
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

  @inline private[this] def isJavaLetterOrDigitImpl(
      codePoint: Int,
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
    val idx = Arrays.binarySearch(isMirroredIndices, codePoint) + 1
    (Math.abs(idx) & 1) != 0
  }

  /* Conversions */
  def toUpperCase(c: scala.Char): scala.Char = {
    toUpperCase(c.toInt).toChar
  }

  def toUpperCase(codePoint: Int): Int = {
    import CaseFolding._
    toCase(codePoint,
           a,
           z,
           toUpper,
           lowerBeta,
           lowerRanges,
           lowerDeltas,
           lowerSteps)
  }

  def toLowerCase(c: scala.Char): scala.Char = {
    toLowerCase(c.toInt).toChar
  }

  def toLowerCase(codePoint: Int): Int = {
    import CaseFolding._
    toCase(codePoint,
           A,
           Z,
           toLower,
           upperMu,
           upperRanges,
           upperDeltas,
           upperSteps)
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
  private[this] lazy val charTypeIndices = {
    val deltas = Array[Int](257, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
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
    uncompressDeltas(deltas)
  }

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
  private[this] lazy val isMirroredIndices = {
    val deltas = Array[Int](40, 2, 18, 1, 1, 1, 28, 1, 1, 1, 29, 1, 1, 1,
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
    uncompressDeltas(deltas)
  }

  // format: on

  private[this] def uncompressDeltas(deltas: Array[Int]): Array[Int] = {
    for (i <- 1 until deltas.length)
      deltas(i) += deltas(i - 1)
    deltas
  }

  // Create tables to support toUpperCase and toLowerCase transformations
  // using the Unicode 7.0 database. This implementation uses the
  // CaseFolding.txt file referenced below.
  // Ranges: the codePoints with lower and upper bound pairs or
  //         individual codePoints.
  // Deltas: the difference between the upper and lower case codePoints
  //         for ranges and individual codePoints.
  // Steps:  values of O indicate the lower bound or individual codePoint.
  //         Steps of 1 or 2 indicate the spacing of the upper or lower case
  //         codePoints with the same index as the upper bound of a range.
  //
  // http://www.unicode.org/Public/7.0.0/ucd/CaseFolding.txt
  //
  //  import scala.io.Source
  //  import java.io.InputStream
  //  def toInt(hex: String): Int = Integer.parseInt(hex, 16)
  //  val filename = "/CaseFolding.txt"
  //  val stream : InputStream = getClass.getResourceAsStream(filename)
  //  val lines = scala.io.Source.fromInputStream(stream).getLines
  //  val records = lines.filterNot(line => line.startsWith("#") || line.isEmpty())
  //  val arrays = records.map { x => x.split(";")  }
  //  val tuples = arrays.map { c => (c(0), c(1).trim(), c(2).trim, c(3).trim) }
  //  // filter for 'simple case folding C + S'
  //  val fTuples = tuples.filter(t => (t._2 == "C" || t._2 == "S") ).toList
  //  val upperLower = fTuples.map(t => (toInt(t._1), toInt(t._3)))
  //  val pairs = upperLower.map { case (u, l) => (u, l, u - l) }
  //
  //  def printIndented(list: List[(Int,Int,Int,Int)]) = {
  //    list.foreach(f => if (f._4 == 0) print("\n" + f) else print(f)); println
  //  }
  //
  //  def addDiff(arr: List[(Int, Int, Int)]) = {
  //    arr.foldLeft[List[(Int, Int, Int, Int)]](Nil) {
  //      case (x :: xs, elem) if (x._3 == elem._3) => {
  //        val diff = elem._1 - x._1
  //        // only ranges with diff or 1 or 2 matter
  //        // some ranges with 2 are only 2 long
  //        if (diff <= 2) (elem._1, elem._2, elem._3, diff) :: x :: xs
  //        else (elem._1, elem._2, elem._3, 0) :: x :: xs
  //      }
  //      case (list, elem) => (elem._1, elem._2, elem._3, 0) :: list
  //    }.reverse
  //  }
  //
  //  def adjustStart(arr: List[(Int, Int, Int, Int)]) = {
  //    arr.tail.foldLeft[List[(Int, Int, Int, Int)]](List(arr.head)) {
  //      case (x :: xs, elem) if (x._4 == 0 || x._4 == elem._4) => elem :: x :: xs
  //      case (list, elem) => (elem._1, elem._2, elem._3, 0) :: list
  //    }.reverse
  //  }
  //
  //  def keepLast(arr: List[(Int, Int, Int, Int)]): List[(Int, Int, Int, Int)] = {
  //    import scala.annotation.tailrec
  //    val h = arr.head
  //    val tail = arr.tail
  //    @tailrec
  //    def process(list: List[(Int, Int, Int, Int)], acc: List[(Int, Int, Int, Int)],
  //        prev: (Int, Int, Int, Int)): List[(Int, Int, Int, Int)] = {
  //      list match {
  //        case Nil => acc
  //        case x :: xs =>
  //          if (x._4 == 0 && x._4 != prev._4) process(xs, x :: prev :: acc, x)
  //          else if (x._4 == 0 || xs == Nil) process(xs, x :: acc, x) // Nil to get last element
  //          else process(xs, acc, x)
  //      }
  //    }
  //    process(tail, List(h), h).reverse
  //  }
  //
  //  def addDiffLower(arr: List[(Int, Int, Int)]) = {
  //    arr.foldLeft[List[(Int, Int, Int, Int)]](Nil) {
  //      case (x :: xs, elem) if (x._3 == elem._3) => {
  //        val diff = elem._2 - x._2
  //        // only ranges with diff or 1 or 2 matter
  //        // some ranges with 2 are only 2 long
  //        if (diff <= 2) (elem._1, elem._2, elem._3, diff) :: x :: xs
  //        else (elem._1, elem._2, elem._3, 0) :: x :: xs
  //      }
  //      case (list, elem) => (elem._1, elem._2, elem._3, 0) :: list
  //    }.reverse
  //  }
  //
  //  def lowerDedup(arr: List[(Int, Int, Int)]) = {
  //    arr.tail.foldLeft[List[(Int, Int, Int)]](List(arr.head)) {
  //      case (x :: xs, elem) if (x._2 == elem._2) => x :: xs
  //      case (list, elem) => elem :: list
  //    }.reverse
  //  }
  //
  //  // process uppers
  //  val u2 = addDiff(pairs)
  //  val u3 = adjustStart(u2)
  //  //printIndented(u3)
  //  val u4 = keepLast(u3)
  //
  //  // Lower case manipulation
  //  val lPairs = pairs.sortBy{ case ((u,l,d))  => ((l,u)) }
  //  val l2 = lowerDedup(lPairs)
  //  // 1104 total, 1083 in lowers with dups removed
  //  // 244 in uppers because of upper -> lower can have more than one mapping
  //  // 230 in lowers
  //  // upper to lower is loss less but back is not reversible
  //  val l3 = addDiffLower(l2)
  //  val l4 = adjustStart(l3)
  //  //printIndented(l4)
  //  val l5 = keepLast(l4)
  //
  //  val uk = u4.unzip { case(_, _, c, d) => (c, d)}
  //  val ul = u4.unzip { case(a, b, _, _) => (a, b)}
  //  val lk = l5.unzip { case(_, _, c, d) => (c, d)}
  //  val ll = l5.unzip { case(a, b, _, _) => (a, b)}
  //  println(uk._1.mkString("private[this] lazy val upperDeltas = Array[scala.Int](", ", ", ")"))
  //  println(uk._2.mkString("private[this] lazy val upperSteps = Array[scala.Byte](", ", ", ")"))
  //  println(ul._1.mkString("private[this] lazy val upperRanges = Array[scala.Int](", ", ", ")"))
  //  println(lk._1.mkString("private[this] lazy val lowerDeltas = Array[scala.Int](", ", ", ")"))
  //  println(lk._2.mkString("private[this] lazy val lowerSteps = Array[scala.Byte](", ", ", ")"))
  //  println(ll._2.mkString("private[this] lazy val lowerRanges = Array[scala.Int](", ", ", ")"))

  private[this] lazy val upperDeltas = Array[scala.Int](32, 32, 775, 32, 32,
    32, 32, 1, 1, 1, 1, 1, 1, 1, 1, -121, 1, 1, -268, 210, 1, 1, 206, 1, 205,
    205, 1, 79, 202, 203, 1, 205, 207, 211, 209, 1, 211, 213, 214, 1, 1, 218,
    1, 218, 1, 218, 1, 217, 217, 1, 1, 219, 1, 1, 2, 1, 2, 1, 2, 1, 1, 1, 1, 2,
    1, 1, -97, -56, 1, 1, -130, 1, 1, 10795, 1, -163, 10792, 1, -195, 69, 71,
    1, 1, 116, 1, 1, 1, 116, 38, 37, 37, 64, 63, 63, 32, 32, 32, 32, 1, 8, -30,
    -25, -15, -22, 1, 1, -54, -48, -60, -64, 1, -7, 1, -130, -130, 80, 80, 32,
    32, 1, 1, 1, 1, 15, 1, 1, 1, 1, 48, 48, 7264, 7264, 7264, 7264, 1, 1, -58,
    -7615, 1, 1, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
    -8, -8, -8, -8, -8, -8, -8, -74, -74, -9, -7173, -86, -86, -9, -8, -8,
    -100, -100, -8, -8, -112, -112, -7, -128, -128, -126, -126, -9, -7517,
    -8383, -8262, 28, 16, 16, 1, 26, 26, 48, 48, 1, -10743, -3814, -10727, 1,
    1, -10780, -10749, -10783, -10782, 1, 1, -10815, -10815, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, -35332, 1, 1, 1, -42280, 1, 1, 1, 1, -42308,
    -42319, -42315, -42305, -42258, -42282, 32, 32, 40, 40, 32, 32)

  private[this] lazy val upperSteps = Array[scala.Byte](0, 1, 0, 0, 1, 0, 1, 0,
    2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
    0, 0, 2, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0,
    0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 2, 0, 2, 0, 0, 2, 0, 2, 0, 1, 0, 1, 0,
    0, 0, 2, 0, 0, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0, 1,
    0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0,
    0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1,
    0, 2, 0, 2, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 2, 0,
    0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1)

  private[this] lazy val upperRanges = Array[scala.Int](65, 90, 181, 192, 214,
    216, 222, 256, 302, 306, 310, 313, 327, 330, 374, 376, 377, 381, 383, 385,
    386, 388, 390, 391, 393, 394, 395, 398, 399, 400, 401, 403, 404, 406, 407,
    408, 412, 413, 415, 416, 420, 422, 423, 425, 428, 430, 431, 433, 434, 435,
    437, 439, 440, 444, 452, 453, 455, 456, 458, 459, 475, 478, 494, 497, 498,
    500, 502, 503, 504, 542, 544, 546, 562, 570, 571, 573, 574, 577, 579, 580,
    581, 582, 590, 837, 880, 882, 886, 895, 902, 904, 906, 908, 910, 911, 913,
    929, 931, 939, 962, 975, 976, 977, 981, 982, 984, 1006, 1008, 1009, 1012,
    1013, 1015, 1017, 1018, 1021, 1023, 1024, 1039, 1040, 1071, 1120, 1152,
    1162, 1214, 1216, 1217, 1229, 1232, 1326, 1329, 1366, 4256, 4293, 4295,
    4301, 7680, 7828, 7835, 7838, 7840, 7934, 7944, 7951, 7960, 7965, 7976,
    7983, 7992, 7999, 8008, 8013, 8025, 8031, 8040, 8047, 8072, 8079, 8088,
    8095, 8104, 8111, 8120, 8121, 8122, 8123, 8124, 8126, 8136, 8139, 8140,
    8152, 8153, 8154, 8155, 8168, 8169, 8170, 8171, 8172, 8184, 8185, 8186,
    8187, 8188, 8486, 8490, 8491, 8498, 8544, 8559, 8579, 9398, 9423, 11264,
    11310, 11360, 11362, 11363, 11364, 11367, 11371, 11373, 11374, 11375,
    11376, 11378, 11381, 11390, 11391, 11392, 11490, 11499, 11501, 11506,
    42560, 42604, 42624, 42650, 42786, 42798, 42802, 42862, 42873, 42875,
    42877, 42878, 42886, 42891, 42893, 42896, 42898, 42902, 42920, 42922,
    42923, 42924, 42925, 42928, 42929, 65313, 65338, 66560, 66599, 71840,
    71871)

  private[this] lazy val lowerDeltas = Array[scala.Int](32, 32, -7615, 32, 32,
    32, 32, -121, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -195, 1, 1, 1, 1, 1, -97, 1,
    -163, -130, 1, 1, 1, 1, 1, 1, 1, 1, 1, -56, 2, 2, 2, 1, 1, 79, 1, 1, 2, 1,
    1, 1, 1, 1, 1, -10815, -10815, 1, 1, 1, -10783, -10780, -10782, 210, 206,
    205, 205, 202, 203, -42319, 205, -42315, 207, -42280, -42308, 209, 211,
    -10743, -42305, 211, -10749, 213, 214, -10727, 218, 218, -42282, 218, 69,
    217, 217, 71, 219, -42258, 1, 1, 1, -130, -130, 38, 37, 37, 32, 32, 116,
    32, 32, 775, 32, 32, 32, 32, 64, 63, 63, 8, 1, 1, -7, 116, 1, 1, 32, 32,
    80, 80, 1, 1, 1, 1, 1, 1, 15, 1, 1, 48, 48, -35332, -3814, 1, 1, 1, 1, -8,
    -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -74, -74, -86, -86,
    -100, -100, -128, -128, -112, -112, -126, -126, -8, -8, -8, -8, -8, -8, -8,
    -8, -9, -9, -8, -8, -8, -8, -7, -9, 28, 16, 16, 1, 26, 26, 48, 48, 1,
    10795, 10792, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7264, 7264, 7264, 7264, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 32, 32, 40, 40, 32, 32)

  private[this] lazy val lowerSteps = Array[scala.Byte](0, 1, 0, 0, 1, 0, 1, 0,
    0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0,
    0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 2, 0, 2, 0, 0, 1, 0, 0, 2,
    0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1,
    0, 1, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 1, 0, 2, 0, 2, 0, 2, 0, 0, 2,
    0, 1, 0, 0, 0, 2, 0, 2, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0,
    1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0,
    0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 2, 0, 0, 1, 0, 0, 0,
    2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 0, 2, 0, 2, 0, 1, 0, 1, 0, 1)

  private[this] lazy val lowerRanges = Array[scala.Int](97, 122, 223, 224, 246,
    248, 254, 255, 257, 303, 307, 311, 314, 328, 331, 375, 378, 382, 384, 387,
    389, 392, 396, 402, 405, 409, 410, 414, 417, 421, 424, 429, 432, 436, 438,
    441, 445, 447, 454, 457, 460, 462, 476, 477, 479, 495, 499, 501, 505, 543,
    547, 563, 572, 575, 576, 578, 583, 591, 592, 593, 594, 595, 596, 598, 599,
    601, 603, 604, 608, 609, 611, 613, 614, 616, 617, 619, 620, 623, 625, 626,
    629, 637, 640, 643, 647, 648, 649, 650, 651, 652, 658, 670, 881, 883, 887,
    891, 893, 940, 941, 943, 945, 952, 953, 954, 955, 956, 957, 961, 963, 971,
    972, 973, 974, 983, 985, 1007, 1010, 1011, 1016, 1019, 1072, 1103, 1104,
    1119, 1121, 1153, 1163, 1215, 1218, 1230, 1231, 1233, 1327, 1377, 1414,
    7545, 7549, 7681, 7829, 7841, 7935, 7936, 7943, 7952, 7957, 7968, 7975,
    7984, 7991, 8000, 8005, 8017, 8023, 8032, 8039, 8048, 8049, 8050, 8053,
    8054, 8055, 8056, 8057, 8058, 8059, 8060, 8061, 8064, 8071, 8080, 8087,
    8096, 8103, 8112, 8113, 8115, 8131, 8144, 8145, 8160, 8161, 8165, 8179,
    8526, 8560, 8575, 8580, 9424, 9449, 11312, 11358, 11361, 11365, 11366,
    11368, 11372, 11379, 11382, 11393, 11491, 11500, 11502, 11507, 11520,
    11557, 11559, 11565, 42561, 42605, 42625, 42651, 42787, 42799, 42803,
    42863, 42874, 42876, 42879, 42887, 42892, 42897, 42899, 42903, 42921,
    65345, 65370, 66600, 66639, 71872, 71903)

  private[this] object CaseFolding {
    lazy val a     = lowerRanges(0)
    lazy val z     = lowerRanges(1)
    lazy val A     = upperRanges(0)
    lazy val Z     = upperRanges(1)
    lazy val delta = upperDeltas(0)
    // other low char optimization whitespace, punctuation, etc.
    lazy val upperMu                        = upperRanges(2)
    lazy val lowerBeta                      = lowerRanges(2)
    def insertionPoint(idx: Int)            = (-(idx) - 1)
    def toUpper(codePoint: Int, delta: Int) = codePoint - delta
    def toLower(codePoint: Int, delta: Int) = codePoint + delta
  }

  private[this] def toCase(codePoint: Int,
                           asciiLow: Int,
                           asciiHigh: Int,
                           convert: (Int, Int) => Int,
                           lowFilter: Int,
                           ranges: Array[scala.Int],
                           deltas: Array[scala.Int],
                           steps: Array[scala.Byte]): Int = {
    import CaseFolding._
    if (asciiLow <= codePoint && codePoint <= asciiHigh)
      convert(codePoint, delta) // ascii
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

  // TODO:
  // def getDirectionality(c: scala.Char): scala.Byte
  // def toTitleCase(c: scala.Char): scala.Char
  // def getNumericValue(c: scala.Char): Int
  // def reverseBytes(ch: scala.Char): scala.Char
  // ...
}
