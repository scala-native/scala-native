package java.text

import java.util.Locale

abstract class NumberFormat protected () extends Format

object NumberFormat {
  def getNumberInstance(inLocale: Locale): NumberFormat =
    new DecimalFormat(inLocale)
}
