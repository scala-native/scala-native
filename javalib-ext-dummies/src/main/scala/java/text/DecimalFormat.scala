package java.text

import java.util.Locale

class DecimalFormat(locale: Locale) extends NumberFormat {
  def getGroupingSize(): Int = 3
}
