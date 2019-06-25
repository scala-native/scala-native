package java.text

import java.lang.NullPointerException
import java.util.Locale
import java.util.MissingResourceException

object DateFormatSymbolsSuite extends tests.Suite {

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

  test("DateFormatSymbols()") {
    val result = new DateFormatSymbols()
    assert(result != null, s"DateFormatSymbols() returned null")
  }

  test("DateFormatSymbols(locale) - Locale.US") {

    // One would expect to see Locale.US here but this differs for a reason.
    // Create a directly equivalent locale instance to test that
    // the checks for content, not object, equality.

    val localeUS = new Locale("en", "US")

    // .eq tests for strict object equality.
    assert(!localeUS.eq(Locale.US),
           s"new Locale should not be object equal to Locale.US.")

    val result = new DateFormatSymbols(localeUS)

    assert(result != null, s"DateFormatSymbols(Locale.US) returned null.")
  }

  test("DateFormatSymbols(locale) - not Locale.US, unsupported") {
    val unsupportedLocale = Locale.FRANCE

    assertThrows[MissingResourceException] {
      new DateFormatSymbols(unsupportedLocale)
    }
  }

  // Test object DateFormatSymbols methods before subsequently using them.

  test("getAvailableLocales()") {
    val result   = DateFormatSymbols.getAvailableLocales()
    val expected = Array(Locale.US)

    assert(result.sameElements(expected),
           s"available locales: ${result} != expected: ${expected}")
  }

  test("getInstance()") {
    val result = DateFormatSymbols.getInstance()
    assert(result != null, s"getInstance() returned null")
  }

  test("getInstance(locale) - Null locale") {
    assertThrowsAnd[NullPointerException] {
      DateFormatSymbols.getInstance(null)
    } {
      _.getMessage == null
    } // match JVM, no message
  }

  test("getInstance(locale) - Unsupported locale") {

    assertThrowsAnd[MissingResourceException] {
      DateFormatSymbols.getInstance(new Locale("es", "US"))
    } { _.getMessage == "Locale not found: es_US" }
  }

  // Get methods

  test("getAmPmStrings()") {
    val dfs      = DateFormatSymbols.getInstance()
    val result   = dfs.getAmPmStrings()
    val expected = Array("AM", "PM")

    assert(result.sameElements(expected),
           s"result: ${result} != expected: ${expected}")
  }

  test("getEras()") {
    val dfs      = DateFormatSymbols.getInstance()
    val result   = dfs.getEras()
    val expected = Array("BC", "AD")

    assert(result.sameElements(expected),
           s"result: ${result} != expected: ${expected}")
  }

  test("getLocalPatternChars()") {
    val dfs      = DateFormatSymbols.getInstance()
    val result   = dfs.getLocalPatternChars()
    val expected = "GyMdkHmsSEDFwWahKzZ"

    assert(result.sameElements(expected),
           s"result: ${result} != expected: ${expected}")
  }

// format: off
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
// format: on

  test("getMonths()") {
    val dfs = DateFormatSymbols.getInstance()

    val result = dfs.getMonths()

    val expected = longMonths

    assert(result.sameElements(expected),
           s"result: ${result} != expected: ${expected}")
  }

// format: off
  private var shortMonths = Array("Jan",
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
// format: on

  test("getShortMonths()") {
    val dfs = DateFormatSymbols.getInstance()

    val result       = dfs.getShortMonths()
    val resultString = formatArrayAsString(result)

    val expected       = shortMonths
    val expectedString = formatArrayAsString(expected)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  test("getShortMonths() - memorization") {
    val dfs = DateFormatSymbols.getInstance()

    val result   = dfs.getShortMonths()
    val expected = dfs.getShortMonths()

    // Test object quality. If memoization worked. should be same object.
    assert(result.eq(expected),
           s"result: '${result}' != expected: '${expected}'")
  }

  // format: off
  private var shortWeekdays = Array("",
				   "Sun",
				   "Mon",
				   "Tue",
				   "Wed",
				   "Thu",
				   "Fri",
				   "Sat")
// format: on

  test("getShortWeekdays()") {
    val dfs = DateFormatSymbols.getInstance()

    val result       = dfs.getShortWeekdays()
    val resultString = formatArrayAsString(result)

    val expected       = shortWeekdays
    val expectedString = formatArrayAsString(expected)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  test("getShortWeekdays() - memorization") {
    val dfs = DateFormatSymbols.getInstance()

    val result   = dfs.getShortWeekdays()
    val expected = dfs.getShortWeekdays()

    // Test object quality. If memoization worked. should be same object.
    assert(result.eq(expected),
           s"result: '${result}' != expected: '${expected}'")
  }

// format: off
  private var longWeekdays = Array("",
				   "Sunday",
				   "Monday",
				   "Tuesday",
				   "Wednesday",
				   "Thursday",
				   "Friday",
				   "Saturday")
// format: on

  test("getWeekdays()") {
    val dfs = DateFormatSymbols.getInstance()

    val result       = dfs.getWeekdays()
    val resultString = formatArrayAsString(result)

    val expected       = longWeekdays
    val expectedString = formatArrayAsString(expected)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  // getZoneStrings() is documented by Java as discouraged.
  // This tests for the minimal set implemented in SN to keep binary
  // size down.
  // If that set is ever expanded, this test will need to change.

  test("getZoneStrings()") {

    val dfs = DateFormatSymbols.getInstance()

    val result = dfs.getZoneStrings()
    assert(result.length == 1,
           s"result length: '${result.length}' != expected: 1")
    val resultString = formatArrayAsString(result(0))

    val expected       = Array(Array("", "", "", "", ""))
    val expectedString = formatArrayAsString(expected(0))

    assert(result(0).sameElements(expected(0)),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  // Set methods

  test("setAmPmStrings(newAmpms)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = Array("a.m.", "p.m.") // Locale.CANADA*
    val expectedString = formatArrayAsString(expected)

    dfs.setEras(expected)

    val result       = dfs.getEras()
    val resultString = formatArrayAsString(result)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  test("setEras(newEras)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = Array("BCE", "CE")
    val expectedString = formatArrayAsString(expected)

    dfs.setEras(expected)

    val result       = dfs.getEras()
    val resultString = formatArrayAsString(result)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  test("setLocalPatternChars(newLocalPatternChars)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected = "QuothTheRaven"

    dfs.setLocalPatternChars(expected)

    val result = dfs.getLocalPatternChars()

    assert(result.sameElements(expected),
           s"result: '${result}' != expected: '${expected}'")
  }

// -----

// format: off

  private var longMonthsFR = Array("January",
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
// format: on

  test("setMonths(newMonths)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = longMonthsFR
    val expectedString = formatArrayAsString(expected)

    dfs.setMonths(expected)

    val result       = dfs.getMonths()
    val resultString = formatArrayAsString(result)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")

  }

  private var shortMonthsFR = Array("January",
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

  test("setShortMonths(newShortMonths)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = shortMonthsFR
    val expectedString = formatArrayAsString(expected)

    dfs.setShortMonths(expected)

    val result       = dfs.getShortMonths()
    val resultString = formatArrayAsString(result)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

// format: off

  private var shortWeekdaysFR = Array(".",
				   "dim.",
				   "lun.",
				   "mar.",
				   "mer.",
				   "jeu.",
				   "ven.",
				   "sam.")
// format: on

  test("setShortWeekdays(newShortWeekdays)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = shortWeekdaysFR
    val expectedString = formatArrayAsString(expected)

    dfs.setShortWeekdays(expected)

    val result       = dfs.getShortWeekdays()
    val resultString = formatArrayAsString(result)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  // Locale.FRANCE
  private var longWeekdaysFR = Array("",
                                     "dimanche",
                                     "lundi",
                                     "mardi",
                                     "mercredi",
                                     "jeudi",
                                     "vendredi",
                                     "samedi")
// format: on

  test("setWeekdays(newWeekdays)") {
    val dfs = DateFormatSymbols.getInstance()

    val expected       = longWeekdaysFR
    val expectedString = formatArrayAsString(expected)

    dfs.setWeekdays(expected)

    val result       = dfs.getWeekdays()
    val resultString = formatArrayAsString(result)

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

  // setZoneStrings() corresponds to getZoneStrings(). The latter is
  // documented by Java as discouraged. So the former method is likely
  // to be seldom used.
  // Skip the usual NullPointerException and IllegalArgument tests
  // to conserve implementation effort but do test the expected success case.

  test("setZoneStrings(newZoneStrings)") {

    val dfs = DateFormatSymbols.getInstance()

    val expected       = Array(Array("UTC", "A", "B", "C", "D"))
    val expectedString = formatArrayAsString(expected(0))

    dfs.setZoneStrings(expected)

    val result = dfs.getZoneStrings()
    assert(result.length == 1,
           s"result length: '${result.length}' != expected: 1")
    val resultString = formatArrayAsString(result(0))

    assert(result.sameElements(expected),
           s"result: '${resultString}' != expected: '${expectedString}'")
  }

}
