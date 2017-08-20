package java.text

import java.util.Locale

class DateFormatSymbols(locale: Locale)
    extends java.io.Serializable
    with Cloneable {
  def this() = this {
    // should be Locale.getDefault(Locale.Category.FORMAT), but scala-native doesn't support it yet
    Locale.getDefault()
  }

  def getAmPmStrings(): Array[String] = ???

  def getEras(): Array[String] = ???

  def getMonths(): Array[String] = ???

  def getShortMonths(): Array[String] = ???

  def getShortWeekdays(): Array[String] = ???

  def getWeekdays(): Array[String] = ???
}

object DateFormatSymbols {
  def getInstance(locale: Locale): DateFormatSymbols = {
    // should check availability of the locale
    new DateFormatSymbols(locale)
  }
}

// vim: set tw=100:
