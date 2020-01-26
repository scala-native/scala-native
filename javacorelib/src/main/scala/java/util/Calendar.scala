package java.util

import scalanative.annotation.stub

import java.io.Serializable

abstract class Calendar
    extends Serializable
    with Cloneable
    with Comparable[Calendar] {

  @stub
  def get(field: Int): Int = ???

  @stub
  def set(field: Int, value: Int): Unit = ???

  @stub
  def set(year: Int,
          month: Int,
          date: Int,
          hourOfDay: Int,
          minute: Int,
          second: Int): Unit = ???

  @stub
  def compareTo(anotherCalendar: Calendar): Int = ???

  @stub
  def getFirstDayOfWeek(): Int = ???

  @stub
  def getMinimalDaysInFirstWeek(): Int = ???

  @stub
  def getTime(): Date = ???

  @stub
  def getTimeInMillis(): Long = ???

  @stub
  def getTimeZone(): TimeZone = ???

  @stub
  def setTime(date: Date): Unit = ???

  @stub
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
