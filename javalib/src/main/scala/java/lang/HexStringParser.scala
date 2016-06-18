package java.lang

import java.util.regex.Pattern

private object HexStringParser {
  final private val DOUBLE_EXPONENT_WIDTH  = 11
  final private val DOUBLE_MANTISSA_WIDTH  = 52
  final private val FLOAT_EXPONENT_WIDTH   = 8
  final private val FLOAT_MANTISSA_WIDTH   = 23
  final private val HEX_RADIX              = 16
  final private val MAX_SIGNIFICANT_LENGTH = 15
  final private val HEX_SIGNIFICANT =
    "0[xX](\\p{XDigit}+\\.?|\\p{XDigit}*\\.\\p{XDigit}+)"
  final private val BINARY_EXPONENT   = "[pP]([+-]?\\d+)"
  final private val FLOAT_TYPE_SUFFIX = "[fFdD]?"
  final private val HEX_PATTERN =
    "[\\x00-\\x20]*([+-]?)" + HEX_SIGNIFICANT + BINARY_EXPONENT +
    FLOAT_TYPE_SUFFIX + "[\\x00-\\x20]*"
  final private val PATTERN = Pattern.compile(HEX_PATTERN)

  def parseDouble(hex_String: _String): scala.Double = {
    val parser = new HexStringParser(
        DOUBLE_EXPONENT_WIDTH, DOUBLE_MANTISSA_WIDTH)
    val result = parser.parse(hex_String, isDouble = true)
    Double.longBitsToDouble(result)
  }

  def parseFloat(hex_String: _String): scala.Float = {
    val parser = new HexStringParser(
        FLOAT_EXPONENT_WIDTH, FLOAT_MANTISSA_WIDTH)
    val result = parser.parse(hex_String, isDouble = false).toInt
    Float.intBitsToFloat(result)
  }
}

private class HexStringParser(
    exponentWidth: scala.Int, mantissaWidth: scala.Int) {
  private val EXPONENT_WIDTH = exponentWidth
  private val MANTISSA_WIDTH = mantissaWidth
  private val EXPONENT_BASE  = ~(-1L << (exponentWidth - 1))
  private val MAX_EXPONENT   = ~(-1L << exponentWidth)
  private val MIN_EXPONENT   = -(MANTISSA_WIDTH + 1)
  private val MANTISSA_MASK  = ~(-1L << mantissaWidth)
  private var sign: scala.Long         = _
  private var exponent: scala.Long     = _
  private var mantissa: scala.Long     = _
  private var abandonedNumber: _String = ""

  private def parse(hex_String: _String, isDouble: scala.Boolean): scala.Long = {
    val matcher = HexStringParser.PATTERN.matcher(hex_String)
    if (!matcher.matches()) {
      throw new NumberFormatException(
          "Invalid hex " + (if (isDouble) "double" else "float") + ":" +
          hex_String)
    }
    val signStr        = matcher.group(1)
    val significantStr = matcher.group(2)
    val exponentStr    = matcher.group(3)
    parseHexSign(signStr)
    parseExponent(exponentStr)
    parseMantissa(significantStr)
    sign <<= (MANTISSA_WIDTH + EXPONENT_WIDTH)
    exponent <<= MANTISSA_WIDTH
    sign | exponent | mantissa
  }

  private def parseHexSign(signStr: _String) {
    this.sign =
      if (signStr == "-") 1
      else 0
  }

  private def parseExponent(_exponentStr: _String) {
    var exponentStr = _exponentStr
    val leadingChar = exponentStr.charAt(0)
    val expSign =
      if (leadingChar == '-') -1
      else 1
    if (!Character.isDigit(leadingChar)) {
      exponentStr = exponentStr.substring(1)
    }
    try {
      exponent = expSign * Long.parseLong(exponentStr)
      checkedAddExponent(EXPONENT_BASE)
    } catch {
      case e: NumberFormatException =>
        exponent = expSign * Long.MAX_VALUE
    }
  }

  private def parseMantissa(significantStr: _String) {
    val str            = significantStr.split("\\.")
    val strIntegerPart = str(0)
    val strDecimalPart =
      if (str.length > 1) str(1)
      else ""
    var significand = getNormalizedSignificand(strIntegerPart, strDecimalPart)
    if (significand == "0") {
      setZero()
      return
    }
    val offset = getOffset(strIntegerPart, strDecimalPart)
    checkedAddExponent(offset)
    if (exponent >= MAX_EXPONENT) {
      setInfinite()
      return
    }
    if (exponent <= MIN_EXPONENT) {
      setZero()
      return
    }
    if (significand.length > HexStringParser.MAX_SIGNIFICANT_LENGTH) {
      abandonedNumber =
        significand.substring(HexStringParser.MAX_SIGNIFICANT_LENGTH)
      significand =
        significand.substring(0, HexStringParser.MAX_SIGNIFICANT_LENGTH)
    }
    mantissa = Long.parseLong(significand, HexStringParser.HEX_RADIX)
    if (exponent >= 1) {
      processNormalNumber()
    } else {
      processSubNormalNumber()
    }
  }

  private def setInfinite() {
    exponent = MAX_EXPONENT
    mantissa = 0
  }

  private def setZero() {
    exponent = 0
    mantissa = 0
  }

  private def checkedAddExponent(offset: scala.Long) {
    val result  = exponent + offset
    val expSign = Long.signum(exponent)
    exponent =
      if (expSign * Long.signum(offset) > 0 &&
          expSign * Long.signum(result) < 0)
        expSign * Long.MAX_VALUE
      else
        result
  }

  private def processNormalNumber() {
    val desiredWidth = MANTISSA_WIDTH + 2
    fitMantissaInDesiredWidth(desiredWidth)
    round()
    mantissa = mantissa & MANTISSA_MASK
  }

  private def processSubNormalNumber() {
    var desiredWidth = MANTISSA_WIDTH + 1
    desiredWidth += exponent.toInt
    exponent = 0
    fitMantissaInDesiredWidth(desiredWidth)
    round()
    mantissa = mantissa & MANTISSA_MASK
  }

  private def fitMantissaInDesiredWidth(desiredWidth: Int) {
    val bitLength = countBitsLength(mantissa)
    if (bitLength > desiredWidth) {
      discardTrailingBits(bitLength - desiredWidth)
    } else {
      mantissa <<= (desiredWidth - bitLength)
    }
  }

  private def discardTrailingBits(num: scala.Long) {
    val mask = ~(-1L << num)
    abandonedNumber += (mantissa & mask).toString
    mantissa >>= num
  }

  private def round() {
    val result           = abandonedNumber.replaceAll("0+", "")
    val moreThanZero     = result.length > 0
    val lastDiscardedBit = (mantissa & 1L).toInt
    mantissa >>= 1
    val tailBitInMantissa = (mantissa & 1L).toInt
    if (lastDiscardedBit == 1 && (moreThanZero || tailBitInMantissa == 1)) {
      val oldLength = countBitsLength(mantissa)
      mantissa += 1L
      val newLength = countBitsLength(mantissa)
      if (oldLength >= MANTISSA_WIDTH && newLength > oldLength) {
        checkedAddExponent(1)
      }
    }
  }

  private def getNormalizedSignificand(
      strIntegerPart: _String, strDecimalPart: _String): _String = {
    val significand = (strIntegerPart + strDecimalPart).replaceFirst("^0+", "")
    if (significand.length == 0) "0"
    else significand
  }

  private def getOffset(
      _strIntegerPart: _String, strDecimalPart: _String): Int = {
    var strIntegerPart = _strIntegerPart.replaceFirst("^0+", "")
    if (strIntegerPart.length != 0) {
      val leadingNumber = strIntegerPart.substring(0, 1)

      (strIntegerPart.length - 1) * 4 + countBitsLength(
          Long.parseLong(leadingNumber, HexStringParser.HEX_RADIX)) - 1
    } else {
      var i = 0
      while (i < strDecimalPart.length && strDecimalPart.charAt(i) == '0') {
        i += 1
      }

      if (i == strDecimalPart.length) {
        0
      } else {
        val leadingNumber = strDecimalPart.substring(i, i + 1)
        (-i - 1) * 4 + countBitsLength(
            Long.parseLong(leadingNumber, HexStringParser.HEX_RADIX)) - 1
      }
    }
  }

  private def countBitsLength(value: scala.Long): scala.Int = {
    val leadingZeros = Long.numberOfLeadingZeros(value)
    Long.SIZE - leadingZeros
  }
}
