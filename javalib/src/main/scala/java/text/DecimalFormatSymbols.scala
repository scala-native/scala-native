package java.text

import java.util.Locale

class DecimalFormatSymbols(locale: Locale) {
  // basic implementation for java.util.Formatter with Locale.US only
  // TODO: add missing methods

  // TODO: should consult the locale
  def getDecimalSeparator(): Char  = '.'
  def getGroupingSeparator(): Char = ','
  def getMinusSign(): Char         = '-'
  def getZeroDigit(): Char         = '0'
}
