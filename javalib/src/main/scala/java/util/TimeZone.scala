package java.util

class TimeZone extends Serializable with Cloneable {
  def getDisplayName(daylight: Boolean, style: Int, locale: Locale): String =
    ???

  def inDaylightTime(time: Date): Boolean = ???
}

object TimeZone {
  val SHORT: Int = 0

  def getTimeZone(ID: String): TimeZone = ???
}
