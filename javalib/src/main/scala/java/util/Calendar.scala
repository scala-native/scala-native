package java.util

import java.io.Serializable

// TODO

abstract class Calendar
    extends Serializable
    with Cloneable
    with Comparable[Calendar] {
  def get(field: Int): Int = ???

  def set(field: Int, value: Int): Unit = ???

  def set(year: Int,
          month: Int,
          date: Int,
          hourOfDay: Int,
          minute: Int,
          second: Int): Unit = ???

  def compareTo(anotherCalendar: Calendar): Int = ???

  def getFirstDayOfWeek(): Int = ???

  def getMinimalDaysInFirstWeek(): Int = ???

  def getTime(): Date = ???

  def getTimeInMillis(): Long = ???

  def getTimeZone(): TimeZone = ???

  def setTime(date: Date): Unit = ???

  def setTimeZone(timezone: TimeZone): Unit = ???
}

object Calendar {
  def getInstance(locale: Locale): Calendar = ???

  def getInstance(zone: TimeZone, locale: Locale): Calendar = ???

  val YEAR: Int         = 0
  val MONTH: Int        = 0
  val DAY_OF_MONTH: Int = 0
  val DAY_OF_YEAR: Int  = 0
  val DAY_OF_WEEK: Int  = 0
  val AM_PM: Int        = 0
  val HOUR: Int         = 0
  val HOUR_OF_DAY: Int  = 0
  val MINUTE: Int       = 0
  val SECOND: Int       = 0
  val MILLISECOND: Int  = 0
  val ZONE_OFFSET: Int  = 0
}
