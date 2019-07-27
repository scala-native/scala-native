package java.util

import scalanative.annotation.stub

class TimeZone extends Serializable with Cloneable {

  @stub
  def getDisplayName(daylight: Boolean, style: Int, locale: Locale): String =
    ???

  @stub
  def inDaylightTime(time: Date): Boolean = ???
}

object TimeZone {

  final val SHORT = 0

  def getAvailableIDs(): Array[String] = Array.empty[String]

  @stub
  def getTimeZone(ID: String): TimeZone = ???

  @stub
  def getDefault(): TimeZone = ???

  @stub
  def setDefault(zone: TimeZone): Unit = ???
}
