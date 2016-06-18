package java.lang

private[lang] object NumberConverter {
  private val invLogOfTenBaseTwo: scala.Double = Math.log(2.0) / Math.log(10.0)
  private val TEN_TO_THE: Array[scala.Long] = {
    val TEN_TO_THE = Array[scala.Long](20)
    TEN_TO_THE(0) = 1L
    var i = 1
    while (i < TEN_TO_THE.length) {
      val previous = TEN_TO_THE(i - 1)
      TEN_TO_THE(i) = (previous << 1) + (previous << 3)
      i += 1
    }
    TEN_TO_THE
  }

  @inline private def getConverter(): NumberConverter =
    new NumberConverter()

  @inline def convert(d: scala.Double): _String =
    getConverter().convertD(d)

  @inline def convert(f: scala.Float): _String =
    getConverter().convertF(f)
}

private class NumberConverter {
  private var setCount: scala.Int = _
  private var getCount: scala.Int = _
  private val uArray: Array[scala.Int] = new Array[Int](64)
  private var firstK: scala.Int = _

  @inline def convertD(d: scala.Double): _String = {
    val p               = 1023 + 52
    val signMask        = 0x8000000000000000L
    val eMask           = 0x7FF0000000000000L
    val fMask           = 0x000FFFFFFFFFFFFFL
    val inputNumberBits = Double.doubleToLongBits(d)
    val signString =
      if ((inputNumberBits & signMask) == 0) ""
      else "-"
    val e = ((inputNumberBits & eMask) >> 52).toInt
    var f = inputNumberBits & fMask
    val mantissaIsZero = f == 0
    var pow     = 0
    var numBits = 52

    if (e == 2047) {
      if (mantissaIsZero) signString + "Infinity"
      else "NaN"
    } else {
      if (e == 0) {
        if (mantissaIsZero) return signString + "0.0"
        if (f == 1) return signString + "4.9E-324"

        pow = 1 - p
        var ff = f
        while ((ff & 0x0010000000000000L) == 0) {
          ff = ff << 1
          numBits -= 1
        }
      } else {
        f = f | 0x0010000000000000L
        pow = e - p
      }

      if (-59 < pow && pow < 6 || (pow == -59 && !mantissaIsZero))
        longDigitGenerator(f, pow, e == 0, mantissaIsZero, numBits)
      else
        bigIntDigitGeneratorInstImpl(f, pow, e == 0, mantissaIsZero, numBits)

      if (d >= 1e7D || d <= -1e7D || (d > -1e-3D && d < 1e-3D))
        signString + freeFormatExponential()
      else
        signString + freeFormat()
    }
  }

  @inline def convertF(f: scala.Float): _String = {
    val p               = 127 + 23
    val signMask        = 0x80000000
    val eMask           = 0x7F800000
    val fMask           = 0x007FFFFF
    val inputNumberBits = Float.floatToIntBits(f)
    val signString =
      if ((inputNumberBits & signMask) == 0) ""
      else "-"
    val e = (inputNumberBits & eMask) >> 23
    var i = inputNumberBits & fMask
    val mantissaIsZero = i == 0
    var pow     = 0
    var numBits = 23

    if (e == 255) {
      if (mantissaIsZero) signString + "Infinity"
      else "NaN"
    } else {
      if (e == 0) {
        if (mantissaIsZero) return signString + "0.0"

        pow = 1 - p
        if (i < 8) {
          i = i << 2
          pow -= 2
        }
        var ff = i
        while ((ff & 0x00800000) == 0) {
          ff = ff << 1
          numBits -= 1
        }
      } else {
        i = i | 0x00800000
        pow = e - p
      }

      if (-59 < pow && pow < 35 || (pow == -59 && !mantissaIsZero))
        longDigitGenerator(i, pow, e == 0, mantissaIsZero, numBits)
      else
        bigIntDigitGeneratorInstImpl(i, pow, e == 0, mantissaIsZero, numBits)

      if (f >= 1e7f || f <= -1e7f || (f > -1e-3f && f < 1e-3f))
        signString + freeFormatExponential()
      else
        signString + freeFormat()
    }
  }

  @inline private def freeFormatExponential(): _String = {
    val formattedDecimal = new Array[Char](25)
    getCount += 1
    formattedDecimal(0) = ('0' + uArray(getCount)).toChar
    formattedDecimal(1) = '.'
    var charPos = 2
    var k       = firstK
    val expt = k

    var i = true
    while (i) {
      k -= 1
      if (getCount >= setCount) i = false

      charPos += 1
      getCount += 1
      formattedDecimal(charPos) = ('0' + uArray(getCount)).toChar
    }

    if (k == expt - 1) {
      charPos += 1
      formattedDecimal(charPos) = '0'
    }

    charPos += 1
    formattedDecimal(charPos) = 'E'

    new _String(formattedDecimal, 0, charPos) + Integer.toString(expt)
  }

  @inline private def freeFormat(): _String = {
    val formattedDecimal = new Array[Char](25)
    var charPos = 0
    var k       = firstK
    if (k < 0) {
      formattedDecimal(0) = '0'
      formattedDecimal(1) = '.'
      charPos += 2

      var i = k + 1
      while (i < 0) {
        charPos += 1
        formattedDecimal(charPos) = '0'
        i += 1
      }
    }

    getCount += 1
    var U = uArray(getCount)
    do {
      if (U != -1) {
        charPos += 1
        formattedDecimal(charPos) = ('0' + U).toChar
      } else if (k >= -1) {
        charPos += 1
        formattedDecimal(charPos) = '0'
      }

      if (k == 0) {
        charPos += 1
        formattedDecimal(charPos) = '.'
      }

      k -= 1
      U =
        if (getCount < setCount) {
          getCount += 1
          uArray(getCount)
        } else -1
    } while (U != -1 || k >= -1)

    new _String(formattedDecimal, 0, charPos)
  }

  // ToDo reimplement
  def bigIntDigitGeneratorInstImpl(f: scala.Long,
                                   e: scala.Int,
                                   isDenormalized: scala.Boolean,
                                   mantissaIsZero: scala.Boolean,
                                   p: scala.Int): Unit = ???

  @inline private def longDigitGenerator(f: scala.Long,
                                         e: scala.Int,
                                         isDenormalized: scala.Boolean,
                                         mantissaIsZero: scala.Boolean,
                                         p: scala.Int) {
    var R = 0l
    var S = 0l
    var M = 0l

    if (e >= 0) {
      M = 1l << e
      if (!mantissaIsZero) {
        R = f << (e + 1)
        S = 2
      } else {
        R = f << (e + 2)
        S = 4
      }
    } else {
      M = 1
      if (isDenormalized || !mantissaIsZero) {
        R = f << 1
        S = 1l << (1 - e)
      } else {
        R = f << 2
        S = 1l << (2 - e)
      }
    }

    val k =
      Math.ceil((e + p - 1) * NumberConverter.invLogOfTenBaseTwo - 1e-10).toInt
    if (k > 0) {
      S = S * NumberConverter.TEN_TO_THE(k)
    } else if (k < 0) {
      val scale = NumberConverter.TEN_TO_THE(-k)
      R = R * scale
      M =
        if (M == 1) scale
        else M * scale
    }

    if (R + M > S) {
      firstK = k
    } else {
      firstK = k - 1
      R = R * 10
      M = M * 10
    }
    getCount = 0
    setCount = 0
    var low  = false
    var high = false
    var U    = 0
    var t    = true
    while (t) {
      U = 0
      var remainder = 0l
      var i         = 3
      val Si = Array(S, S << 1, S << 2, S << 3)
      while (i >= 0) {
        remainder = R - Si(i)
        if (remainder >= 0) {
          R = remainder
          U += 1 << i
        }
        i -= 1
      }
      low = R < M
      high = R + M > S
      if (low || high) {
        t = false
      } else {
        R = R * 10
        M = M * 10

        setCount += 1
        uArray(setCount) = U
      }
    }

    setCount += 1
    uArray(setCount) =
      if (low && !high) U
      else if (high && !low) U + 1
      else if ((R << 1) < S) U
      else U + 1
  }
}
