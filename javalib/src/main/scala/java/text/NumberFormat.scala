package java.text

import java.util.Locale

abstract class NumberFormat extends Format {
  // basic implementation for java.util.Formatter with Locale.US only
  // TODO: add missing methods

  def format(obj: Object,
             toAppendTo: StringBuffer,
             pos: FieldPosition): StringBuffer =
    obj match {
      case num: Number =>
        val l = num.longValue
        val d = num.doubleValue
        if (l == d)
          format(l, toAppendTo, pos)
        else
          format(d, toAppendTo, pos)
      case _ => throw new IllegalArgumentException
    }

  def format(number: Double): String =
    format(number, new StringBuffer, new FieldPosition(0)).toString

  def format(number: Long): String =
    format(number, new StringBuffer, new FieldPosition(0)).toString

  def format(number: Double,
             toAppendTo: StringBuffer,
             pos: FieldPosition): StringBuffer

  def format(number: Long,
             toAppendTo: StringBuffer,
             pos: FieldPosition): StringBuffer

  protected[this] var groupingUsed: Boolean = true

  def isGroupingUsed(): Boolean             = groupingUsed
  def setGroupingUsed(value: Boolean): Unit = groupingUsed = value
}

object NumberFormat {
  def getInstance(locale: Locale): NumberFormat =
    getNumberInstance(locale)

  def getNumberInstance(locale: Locale): NumberFormat = new DecimalFormat
}
