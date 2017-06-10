package java.text

import java.util.Locale

class NumberFormat extends Format {
  // TODO
  def setGroupingUsed(value: Boolean): Unit = ???
}

object NumberFormat {
  def getInstance(locale: Locale): NumberFormat =
    getNumberInstance(locale)

  def getNumberInstance(locale: Locale): NumberFormat = ???
}
