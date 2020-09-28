package java.text

import java.util.Locale
import java.util.MissingResourceException

// A mimimal implementation which supports only Locale.US.

class DecimalFormatSymbols(locale: Locale)
    extends java.io.Serializable
    with Cloneable {

  def this() = this {
    // Should be Locale.getDefault(Locale.Category.FORMAT),
    // but is implementation is defined as Locale.US only.
    Locale.US
  }

  if (!locale.equals(Locale.US)) {
    throw new MissingResourceException(s"Locale not found: ${locale.toString}",
                                       "",
                                       "")
  }

  private var _currencySymbol              = "$"
  private var _decimalSeparator            = '.'
  private var _digit                       = '#'
  private var _exponentSeparator           = "E"
  private var _groupingSeparator           = ','
  private var _infinity                    = "\u221E" // Unicode 'INFINITY'
  private var _internationalCurrencySymbol = "USD"
  private var _minusSign                   = '-'
  private var _monetaryDecimalSeparator    = '.'
  // _naN matches value returned by Scala JVM,
  // probably displayed as (raw underlying NaN).toString.
  private var _naN              = "\uFFFD" // Unicode 'REPLACEMENT CHARACTER'
  private var _patternSeparator = ';'
  private var _percent          = '%'
  private var _perMill          = '\u2030' // Unicode 'PER MILLE SIGN'
  private var _zeroDigit        = '0'

  override def clone(): Object = {
    // clone(), especially overridden clone(), usually does a deep copy.
    // The shallow clone of super/parent class (Object) is good enough here
    // because all the fields of this class are either primitive or
    // point to immutable objects.
    // When this class supports getCurrency() and setCurrency() this method
    // still works because the Currency class is immutable (no set methods).

    try {
      super.clone
    } catch {
      case e: CloneNotSupportedException => null
    }
  }

  override def equals(o: Any): Boolean = {
    o match {
      case that: DecimalFormatSymbols =>
        (_currencySymbol == that._currencySymbol) &&
          (_decimalSeparator == that._decimalSeparator) &&
          (_digit == that._digit) &&
          (_exponentSeparator == that._exponentSeparator) &&
          (_groupingSeparator == that._groupingSeparator) &&
          (_infinity == that._infinity) &&
          (_internationalCurrencySymbol == that._internationalCurrencySymbol) &&
          (_minusSign == that._minusSign) &&
          (_monetaryDecimalSeparator == that._monetaryDecimalSeparator) &&
          (_naN == that._naN) &&
          (_patternSeparator == that._patternSeparator) &&
          (_percent == that._percent) &&
          (_perMill == that._perMill) &&
          (_zeroDigit == that._zeroDigit)

      case _ => false
    }
  }

  //  def getCurrency()  Not Implemented No Currency class in Scala Native.
  def getCurrencySymbol(): String              = _currencySymbol
  def getDecimalSeparator(): Char              = _decimalSeparator
  def getDigit(): Char                         = _digit
  def getExponentSeparator(): String           = _exponentSeparator
  def getGroupingSeparator(): Char             = _groupingSeparator
  def getInfinity(): String                    = _infinity
  def getInternationalCurrencySymbol(): String = _internationalCurrencySymbol
  def getMinusSign(): Char                     = _minusSign
  def getMonetaryDecimalSeparator(): Char      = _monetaryDecimalSeparator
  def getNaN(): String                         = _naN
  def getPatternSeparator(): Char              = _patternSeparator
  def getPercent(): Char                       = _percent
  def getPerMill(): Char                       = _perMill
  def getZeroDigit(): Char                     = _zeroDigit

  override def hashCode(): Int = {
    _currencySymbol.hashCode() +
      _decimalSeparator.hashCode() +
      _digit.hashCode() +
      _exponentSeparator.hashCode() +
      _groupingSeparator.hashCode() +
      _infinity.hashCode() +
      _internationalCurrencySymbol.hashCode() +
      _minusSign.hashCode() +
      _monetaryDecimalSeparator.hashCode() +
      _naN.hashCode() +
      _patternSeparator.hashCode() +
      _percent.hashCode() +
      _perMill.hashCode() +
      _zeroDigit.hashCode()
  }

  //  def setCurrency()  Not Implemented No Currency class in Scala Native.

  def setCurrencySymbol(currency: String): Unit =
    _currencySymbol = currency

  def setDecimalSeparator(decimalSeparator: Char): Unit =
    _decimalSeparator = decimalSeparator

  def setDigit(digit: Char): Unit = _digit = digit

  def setExponentSeparator(exp: String): Unit = _exponentSeparator = exp

  def setGroupingSeparator(groupingSeparator: Char): Unit =
    _groupingSeparator = groupingSeparator

  def setInfinity(infinity: String): Unit = _infinity = infinity

  def setInternationalCurrencySymbol(currencyCode: String): Unit =
    _internationalCurrencySymbol = currencyCode

  def setMinusSign(minusSign: Char): Unit = _minusSign = minusSign

  def setMonetaryDecimalSeparator(sep: Char): Unit =
    _monetaryDecimalSeparator = sep

  def setNaN(NaN: String): Unit = _naN = NaN

  def setPatternSeparator(patternSeparator: Char): Unit =
    _patternSeparator = patternSeparator

  def setPercent(percent: Char): Unit = _percent = percent

  def setPerMill(perMill: Char): Unit = _perMill = perMill

  def setZeroDigit(zeroDigit: Char): Unit = _zeroDigit = zeroDigit
}

object DecimalFormatSymbols {

  def getAvailableLocales(): Array[Locale] = Array(Locale.US)

  def getInstance(): DecimalFormatSymbols = new DecimalFormatSymbols()

  def getInstance(locale: Locale): DecimalFormatSymbols = {
    if (locale == null) {
      throw new NullPointerException() // match JVM, which has no message.
    }
    new DecimalFormatSymbols(locale)
  }
}
