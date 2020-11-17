package java.text

import java.util.Locale
import java.util.MissingResourceException

// A mimimal implementation which supports only Locale.US.

class DateFormatSymbols(locale: Locale)
    extends java.io.Serializable
    with Cloneable {

  def this() = this {
    // should be Locale.getDefault(Locale.Category.FORMAT),
    // but scala-native doesn't support it yet
    Locale.getDefault()
  }

  if (!locale.equals(Locale.US)) {
    throw new MissingResourceException(s"Locale not found: ${locale.toString}",
                                       "",
                                       "")
  }

  private var amPmStrings = Array("AM", "PM")

  private var eras = Array("BC", "AD")

  private var localPatternChars = "GyMdkHmsSEDFwWahKzZ"

  private var longMonths = Array("January",
                                 "February",
                                 "March",
                                 "April",
                                 "May",
                                 "June",
                                 "July",
                                 "August",
                                 "September",
                                 "October",
                                 "November",
                                 "December",
                                 "")

  private var shortMonths: Option[Array[String]] = None

  private var longWeekdays = Array("",
                                   "Sunday",
                                   "Monday",
                                   "Tuesday",
                                   "Wednesday",
                                   "Thursday",
                                   "Friday",
                                   "Saturday")

  private var shortWeekdays: Option[Array[String]] = None

  // Rump implementation. Save binary size of discouraged method.
  // See comments just above getZoneStrings() for details.

  private var zoneStrings: Array[Array[String]] =
    Array(Array("", "", "", "", ""))

  def getAmPmStrings(): Array[String] = amPmStrings

  def getEras(): Array[String] = eras

  def getLocalPatternChars() = localPatternChars

  def getMonths(): Array[String] = longMonths

  def getShortMonths(): Array[String] = {
    shortMonths match {
      case Some(months) => months
      case None =>
        val months = for (m <- longMonths) yield (m.slice(0, 3))
        shortMonths = Some(months)
        months
    }
  }

  def getShortWeekdays(): Array[String] = {
    shortWeekdays match {
      case Some(days) => days
      case None =>
        val days = for (d <- longWeekdays) yield d.slice(0, 3)
        shortWeekdays = Some(days)
        days
    }
  }

  def getWeekdays(): Array[String] = longWeekdays

  def setAmPmStrings(newAmpms: Array[String]): Unit = {
    amPmStrings = newAmpms
  }

  def setEras(newEras: Array[String]): Unit = { eras = newEras }

  def setLocalPatternChars(newLocalPatternChars: String): Unit = {
    localPatternChars = newLocalPatternChars
  }

  def setMonths(newMonths: Array[String]): Unit = { longMonths = newMonths }

  def setShortMonths(newShortMonths: Array[String]): Unit = {
    shortMonths = Some(newShortMonths)
  }

  def setShortWeekdays(newShortWeekdays: Array[String]): Unit = {
    shortWeekdays = Some(newShortWeekdays)
  }

  def setWeekdays(newWeekdays: Array[String]): Unit = {
    longWeekdays = newWeekdays
  }

  // The Java 8 documentation says that use of this method is discourated.
  // It recomments TimeZone.getDisplayName().

  def getZoneStrings(): Array[Array[String]] = {
    zoneStrings
  }

  def setZoneStrings(newZoneStrings: Array[Array[String]]): Unit = {

    if (newZoneStrings == null) {
      throw new NullPointerException()
    }

    for (i <- 0 until newZoneStrings.length) {
      val elem       = newZoneStrings(i)
      val elemLength = elem.length
      if (elemLength < 5)
        throw new IllegalArgumentException
    }

    zoneStrings = newZoneStrings
  }
}

object DateFormatSymbols {

  def getAvailableLocales(): Array[Locale] = Array(Locale.US)

  def getInstance(): DateFormatSymbols = new DateFormatSymbols()

  def getInstance(locale: Locale): DateFormatSymbols = {

    if (locale == null) {
      throw new NullPointerException() // match JVM, which has no message.
    }

    new DateFormatSymbols(locale)
  }
}
