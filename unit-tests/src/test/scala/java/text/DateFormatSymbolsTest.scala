package java.text

import java.lang.NullPointerException
import java.util.Locale
import java.util.MissingResourceException

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils._, AssertThrows._, ThrowsHelper._

class DateFormatSymbolsTest {

  // NOTE WELL:
  //
  //	Use a new DateFormatSymbols instance in each test.
  //	This keeps the environment being tested fresh and clean
  //	with each test.
  //
  //	It is obvious after the pain of the fact that doing a set() test
  //	a get() test which expects the default environment will fail.

  // By design & specification the arrays in in this file have
  // some empty strings (""). This means the display of first & last commas
  // may be a bit unusual. mkString() is not broken, a better
  // formatArrayAsString() is needed.

  private def formatArrayAsString(a: Array[String]): String = {
    a.mkString("Array(", ", ", ")")
  }

  // Test constructors

  @Test def dateFormatSymbols(): Unit = {
    val result = new DateFormatSymbols()
    assertTrue(s"DateFormatSymbols() returned null", result != null)
  }

  @Test def dateFormatSymbolsLocaleUS(): Unit = {

    // One would expect to see Locale.US here but this differs for a reason.
    // Create a directly equivalent locale instance to test that
    // the checks for content, not object, equality.

    val localeUS = new Locale("en", "US")

    // .eq tests for strict object equality.
    assertFalse(s"new Locale should not be object equal to Locale.US.",
                localeUS.eq(Locale.US))

    val result = new DateFormatSymbols(localeUS)

    assertTrue(s"DateFormatSymbols(Locale.US) returned null.", result != null)
  }

  @Test def dateFormatSymbolsLocaleUnsupported(): Unit = {
    val unsupportedLocale = Locale.FRANCE

    assertThrows(classOf[MissingResourceException],
                 new DateFormatSymbols(unsupportedLocale))
  }

  // Test object DateFormatSymbols methods before subsequently using them.

  @Test def getAvailableLocales(): Unit = {
    val result   = DateFormatSymbols.getAvailableLocales()
    val expected = Array(Locale.US)

    assertTrue(s"available locales: ${result} != expected: ${expected}",
               result.sameElements(expected))
  }

  @Test def getInstance(): Unit = {
    val result = DateFormatSymbols.getInstance()
    assertTrue(s"getInstance() returned null", result != null)
  }

  @Test def getInstanceLocaleNullLocale(): Unit = {
    // match JVM, no message
    assertThrowsAnd(classOf[NullPointerException],
                    DateFormatSymbols.getInstance(null))(_.getMessage == null)
  }

  @Test def getInstanceLocaleUnsupportedLocale(): Unit = {

    assertThrowsAnd(classOf[MissingResourceException],
                    DateFormatSymbols.getInstance(new Locale("es", "US")))(
      _.getMessage == "Locale not found: es_US")
  }

  // Get methods

  @Test def getAmPmStrings(): Unit = {
    val dfs      = DateFormatSymbols.getInstance()
    val result   = dfs.getAmPmStrings()
    val expected = Array("AM", "PM")

    assertTrue(s"result: ${result} != expected: ${expected}",
               result.sameElements(expected))
  }

  @Test def getEras(): Unit = {
    val dfs      = DateFormatSymbols.getInstance()
    val result   = dfs.getEras()
    val expected = Array("BC", "AD")

    assertTrue(s"result: ${result} != expected: ${expected}",
               result.sameElements(expected))
  }

  @Test def getLocalPatternChars(): Unit = {
    val dfs      = DateFormatSymbols.getInstance()
    val result   = dfs.getLocalPatternChars()
    val expected = "GyMdkHmsSEDFwWahKzZ"

    assertTrue(s"result: ${result} != expected: ${expected}",
               result.sameElements(expected))
  }

  private var longMonths =
    Array("January",
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

  @Test def getMonths(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val result = dfs.getMonths()

    val expected = longMonths

    assertTrue(s"result: ${result} != expected: ${expected}",
               result.sameElements(expected))
  }

  private var shortMonths =
    Array("Jan",
          "Feb",
          "Mar",
          "Apr",
          "May",
          "Jun",
          "Jul",
          "Aug",
          "Sep",
          "Oct",
          "Nov",
          "Dec",
          "")
  @Test def getShortMonths(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val result       = dfs.getShortMonths()
    val resultString = formatArrayAsString(result)

    val expected       = shortMonths
    val expectedString = formatArrayAsString(expected)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  @Test def getShortMonthsMemorization(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val result   = dfs.getShortMonths()
    val expected = dfs.getShortMonths()

    // Test object quality. If memoization worked. should be same object.
    assertTrue(s"result: '${result}' != expected: '${expected}'",
               result.eq(expected))
  }

  private var shortWeekdays =
    Array("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

  @Test def getShortWeekdays(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val result       = dfs.getShortWeekdays()
    val resultString = formatArrayAsString(result)

    val expected       = shortWeekdays
    val expectedString = formatArrayAsString(expected)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  @Test def getShortWeekdaysMemorization(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val result   = dfs.getShortWeekdays()
    val expected = dfs.getShortWeekdays()

    // Test object quality. If memoization worked. should be same object.
    assertTrue(s"result: '${result}' != expected: '${expected}'",
               result.eq(expected))
  }

  private var longWeekdays =
    Array("",
          "Sunday",
          "Monday",
          "Tuesday",
          "Wednesday",
          "Thursday",
          "Friday",
          "Saturday")

  @Test def getWeekdays(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val result       = dfs.getWeekdays()
    val resultString = formatArrayAsString(result)

    val expected       = longWeekdays
    val expectedString = formatArrayAsString(expected)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  // getZoneStrings() is documented by Java as discouraged.
  // This tests for the minimal set implemented in SN to keep binary
  // size down.
  // If that set is ever expanded, this test will need to change.

  @Test def getZoneStrings(): Unit = {

    val dfs = DateFormatSymbols.getInstance()

    val result = dfs.getZoneStrings()
    assertTrue(s"result length: '${result.length}' != expected: 1",
               result.length == 1)
    val resultString = formatArrayAsString(result(0))

    val expected       = Array(Array("", "", "", "", ""))
    val expectedString = formatArrayAsString(expected(0))

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result(0).sameElements(expected(0)))
  }

  // Set methods

  @Test def setAmPmStringsNewAmPm(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = Array("a.m.", "p.m.") // Locale.CANADA*
    val expectedString = formatArrayAsString(expected)

    dfs.setEras(expected)

    val result       = dfs.getEras()
    val resultString = formatArrayAsString(result)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  @Test def setErasNewEras(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = Array("BCE", "CE")
    val expectedString = formatArrayAsString(expected)

    dfs.setEras(expected)

    val result       = dfs.getEras()
    val resultString = formatArrayAsString(result)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  @Test def setLocalPatternCharsNewLocalPatternChars(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected = "QuothTheRaven"

    dfs.setLocalPatternChars(expected)

    val result = dfs.getLocalPatternChars()

    assertTrue(s"result: '${result}' != expected: '${expected}'",
               result.sameElements(expected))
  }

  private var longMonthsFR =
    Array("January",
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

  @Test def setMonthsNewMonths(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = longMonthsFR
    val expectedString = formatArrayAsString(expected)

    dfs.setMonths(expected)

    val result       = dfs.getMonths()
    val resultString = formatArrayAsString(result)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))

  }

  private var shortMonthsFR =
    Array("January",
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

  @Test def setShortMonthsNewShortMonths(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = shortMonthsFR
    val expectedString = formatArrayAsString(expected)

    dfs.setShortMonths(expected)

    val result       = dfs.getShortMonths()
    val resultString = formatArrayAsString(result)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  private var shortWeekdaysFR =
    Array(".", "dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam.")

  @Test def setShortWeekdaysNewShortWeekdays(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = shortWeekdaysFR
    val expectedString = formatArrayAsString(expected)

    dfs.setShortWeekdays(expected)

    val result       = dfs.getShortWeekdays()
    val resultString = formatArrayAsString(result)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  // Locale.FRANCE
  private var longWeekdaysFR =
    Array("",
          "dimanche",
          "lundi",
          "mardi",
          "mercredi",
          "jeudi",
          "vendredi",
          "samedi")

  @Test def setWeekdaysNewWeekdays(): Unit = {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = longWeekdaysFR
    val expectedString = formatArrayAsString(expected)

    dfs.setWeekdays(expected)

    val result       = dfs.getWeekdays()
    val resultString = formatArrayAsString(result)

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

  // setZoneStrings() corresponds to getZoneStrings(). The latter is
  // documented by Java as discouraged. So the former method is likely
  // to be seldom used.
  // Skip the usual NullPointerException and IllegalArgument tests
  // to conserve implementation effort but do test the expected success case.

  @Test def setZoneStringsNewZoneStrings(): Unit = {

    val dfs = DateFormatSymbols.getInstance()

    val expected       = Array(Array("UTC", "A", "B", "C", "D"))
    val expectedString = formatArrayAsString(expected(0))

    dfs.setZoneStrings(expected)

    val result = dfs.getZoneStrings()
    assertTrue(s"result length: '${result.length}' != expected: 1",
               result.length == 1)
    val resultString = formatArrayAsString(result(0))

    assertTrue(s"result: '${resultString}' != expected: '${expectedString}'",
               result.sameElements(expected))
  }

}
