package java.text

import java.util.Locale

class DecimalFormatSymbols(locale: Locale) {
  // basic implementation for java.util.Formatter with Locale.US only
  def getDecimalSeparator(): Char  = '.'
  def getGroupingSeparator(): Char = ','
  def getMinusSign(): Char         = '-'
  def getZeroDigit(): Char         = '0'
}
