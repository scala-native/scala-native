package java.text

import scalanative.native.stub

import java.util.Locale

class DateFormatSymbols(locale: Locale)
    extends java.io.Serializable
    with Cloneable {
  def this() = this {
    // should be Locale.getDefault(Locale.Category.FORMAT), but scala-native doesn't support it yet
    Locale.getDefault()
  }

  @stub
  def getAmPmStrings(): Array[String] = ???

  @stub
  def getEras(): Array[String] = ???

  @stub
  def getMonths(): Array[String] = ???

  @stub
  def getShortMonths(): Array[String] = ???

  @stub
  def getShortWeekdays(): Array[String] = ???

  @stub
  def getWeekdays(): Array[String] = ???
}

object DateFormatSymbols {
  def getInstance(locale: Locale): DateFormatSymbols = {
    // should check availability of the locale
    new DateFormatSymbols(locale)
  }
}

// vim: set tw=100:
