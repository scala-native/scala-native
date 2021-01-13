package java.text

import java.util.Locale

class DecimalFormatSymbols(locale: Locale) {
  def getZeroDigit(): Char = {
    val ext = locale.getExtension('u')
    if (ext != null && ext.contains("nu-deva"))
      '\u0966' // '०' DEVANAGARI DIGIT ZERO
    else
      '0'
  }

  def getGroupingSeparator(): Char = {
    locale.getLanguage() match {
      case "fr"             => '\u00A0' // NO-BREAK SPACE
      case "" | "en" | "hi" => ','
      case _                => unsupported()
    }
  }

  def getDecimalSeparator(): Char = {
    locale.getLanguage() match {
      case "fr"             => ','
      case "" | "en" | "hi" => '.'
      case _                => unsupported()
    }
  }

  def getMinusSign(): Char = '-'

  private def unsupported(): Nothing =
    throw new Error(s"Unsupported locale '$locale' in DecimalFormatSymbols")
}

object DecimalFormatSymbols {
  def getInstance(locale: Locale) =
    new DecimalFormatSymbols(locale)
}
