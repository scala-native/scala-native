package java.lang

private[lang] object NumberParser {
  private val FLOAT_MIN_EXP  = -46
  private val FLOAT_MAX_EXP  = 38
  private val DOUBLE_MIN_EXP = -324
  private val DOUBLE_MAX_EXP = 308

  private case class StringExponentPair(s: String,
                                        e: scala.Int,
                                        negative: scala.Boolean)

  // ToDo reimplement from c to scala
  @inline private def parseDblImpl(s: String, e: scala.Int): scala.Double =
    ???
  @inline private def parseFltImpl(s: String, e: scala.Int): scala.Float =
    ???

  @inline private def initialParse(
      _s: String, _length: scala.Int): StringExponentPair = {
    var s            = _s
    var length       = _length
    var negative     = false
    var c: Char      = 0
    var end: Int     = 0
    var decimal: Int = 0
    var shift: Int   = 0
    var e            = 0

    var start: Int = 0
    if (length == 0) throw new NumberFormatException(s)
    c = s.charAt(length - 1)

    if (c == 'D' || c == 'd' || c == 'F' || c == 'f') {
      length -= 1
      if (length == 0) throw new NumberFormatException(s)
    }

    end = Math.max(s.indexOf('E'), s.indexOf('e'))
    if (end > -1) {
      if (end + 1 == length) throw new NumberFormatException(s)

      var exponent_offset = end + 1
      if (s.charAt(exponent_offset) == '+') {
        if (s.charAt(exponent_offset + 1) == '-')
          throw new NumberFormatException(s)
        exponent_offset += 1
        if (exponent_offset == length) throw new NumberFormatException(s)
      }

      val strExp = s.substring(exponent_offset, length)
      try {
        e = Integer.parseInt(strExp)
      } catch {
        case ex: NumberFormatException =>
          var ch: Char = 0
          for (i <- 0 until strExp.length) {
            ch = strExp.charAt(i)
            if (ch < '0' || ch > '9') {
              if (i == 0 && ch == '-') {} else
                throw new NumberFormatException(s)
            }
          }
          e =
            if (strExp.charAt(0) == '-') Integer.MIN_VALUE
            else Integer.MAX_VALUE
      }
    } else {
      end = length
    }

    if (length == 0) throw new NumberFormatException(s)

    c = s.charAt(start)
    if (c == '-') {
      negative = true
    }

    if (length == 0) throw new NumberFormatException(s)

    decimal = s.indexOf('.')
    if (decimal > -1) {
      shift = end - decimal - 1

      if (e >= 0 || e - Integer.MIN_VALUE > shift) {
        e -= shift
      }

      s = s.substring(start, decimal) + s.substring(decimal + 1, end)
    } else {
      s = s.substring(start, end)
    }

    length = s.length()
    if (length == 0) throw new NumberFormatException()

    end = length
    while (end > 1 && s.charAt(end - 1) == '0') end

    start = 0
    while (start < end - 1 && s.charAt(start) == '0') start += 1
    if (end != length || start != 0) {
      shift = length - end
      if (e <= 0 || Integer.MAX_VALUE - e > shift) e += shift

      s = s.substring(start, end)
    }

    val APPROX_MIN_MAGNITUDE = -359
    val MAX_DIGITS           = 52
    length = s.length()
    if (length > MAX_DIGITS && e < APPROX_MIN_MAGNITUDE) {
      val d = Math.min(APPROX_MIN_MAGNITUDE - e, length - 1)
      s = s.substring(0, length - d)
      e += d
    }

    StringExponentPair(s, e, negative)
  }

  private def parseDblName(namedDouble: String, length: Int): scala.Double = {
    if ((length != 3) && (length != 4) && (length != 8) && (length != 9)) {
      throw new NumberFormatException()
    } else {
      var negative = false
      var cmpstart = 0
      namedDouble.charAt(0) match {
        case '-' => negative = true
        case '+' => cmpstart = 1
        case _   =>
      }

      if (namedDouble.regionMatches(false, cmpstart, "Infinity", 0, 8))
        if (negative)
          Double.NEGATIVE_INFINITY
        else
          Double.POSITIVE_INFINITY
      else if (namedDouble.regionMatches(false, cmpstart, "NaN", 0, 3))
        Double.NaN
      else
        throw new NumberFormatException()
    }
  }

  private def parseFltName(namedFloat: String, length: Int): scala.Float = {
    if ((length != 3) && (length != 4) && (length != 8) && (length != 9)) {
      throw new NumberFormatException()
    } else {
      var negative = false
      var cmpstart = 0

      namedFloat.charAt(0) match {
        case '-' => negative = true
        case '+' => cmpstart = 1
        case _   =>
      }

      if (namedFloat.regionMatches(false, cmpstart, "Infinity", 0, 8)) {
        if (negative)
          Float.NEGATIVE_INFINITY
        else
          Float.POSITIVE_INFINITY
      } else if (namedFloat.regionMatches(false, cmpstart, "NaN", 0, 3)) {
        Float.NaN
      } else {
        throw new NumberFormatException()
      }
    }
  }

  private def parseAsHex(s: String): scala.Boolean = {
    val length = s.length
    if (length < 2) {
      false
    } else {
      var first  = s.charAt(0)
      var second = s.charAt(1)

      if (first == '+' || first == '-') {
        if (length < 3) {
          return false
        }
        first = second
        second = s.charAt(2)
      }

      (first == '0') && (second == 'x' || second == 'X')
    }
  }

  def parseDouble(_s: String): scala.Double = {
    val s      = _s.trim()
    val length = s.length()
    if (length == 0) {
      throw new NumberFormatException(s)
    } else {
      val last = s.charAt(length - 1)

      if ((last == 'y') || (last == 'N')) {
        parseDblName(s, length)
      } else if (parseAsHex(s)) {
        HexStringParser.parseDouble(s)
      } else {
        val info = initialParse(s, length)
        if ("0" == info.s || (info.e + info.s.length - 1 < DOUBLE_MIN_EXP))
          if (info.negative) -0.0
          else 0.0
        else if ((info.e > DOUBLE_MAX_EXP) ||
                 (info.e + info.s.length - 1 > DOUBLE_MAX_EXP))
          if (info.negative) Double.NEGATIVE_INFINITY
          else Double.POSITIVE_INFINITY
        else {
          var result = parseDblImpl(info.s, info.e)
          if (info.negative) result = -result
          result
        }
      }
    }
  }

  def parseFloat(_s: String): scala.Float = {
    val s      = _s.trim()
    val length = s.length()
    if (length == 0) {
      throw new NumberFormatException(s)
    } else {
      val last = s.charAt(length - 1)
      if ((last == 'y') || (last == 'N')) {
        parseFltName(s, length)
      } else if (parseAsHex(s)) {
        HexStringParser.parseFloat(s)
      } else {
        val info = initialParse(s, length)
        if ("0" == info.s || (info.e + info.s.length - 1 < FLOAT_MIN_EXP))
          if (info.negative) -0.0f
          else 0.0f
        else if ((info.e > FLOAT_MAX_EXP) ||
                 (info.e + info.s.length - 1 > FLOAT_MAX_EXP))
          if (info.negative) Float.NEGATIVE_INFINITY
          else Float.POSITIVE_INFINITY
        else {
          var result = parseFltImpl(info.s, info.e)
          if (info.negative) result = -result
          result
        }
      }
    }
  }
}
