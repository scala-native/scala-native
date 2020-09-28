package java.text

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.junit.utils.AssertThrows._

import java.lang.NullPointerException
import java.util.Locale
import java.util.MissingResourceException

class DecimalFormatSymbolsTest {

  // Test constructors

  @Test def zeroArgConstructor(): Unit = {
    val result = new DecimalFormatSymbols()
    assertNotNull(result)
  }

  @Test def oneArgConstructorLocaleUS(): Unit = {
    val onlySupportedLocale = Locale.US

    val result = new DecimalFormatSymbols(onlySupportedLocale)
    assertNotNull(result)
  }

  @Test def oneArgConstructorUnsupportedLocaleFrance(): Unit = {
    val unsupportedLocale = Locale.FRANCE
    assertThrows(classOf[MissingResourceException],
                 new DecimalFormatSymbols(unsupportedLocale))
  }

  // Overrides

  @Test def overrideClone(): Unit = {
    val dfs   = DecimalFormatSymbols.getInstance()
    val clone = dfs.clone()

    assert(!dfs.eq(clone),
           s"cloned object should not be reference equal to original.")

    assertEquals(dfs, clone)

    val newValue   = 'X'
    val startValue = dfs.getZeroDigit()

    // Do not set dfs value to what it already is, clone already has that.
    assertNotEquals(startValue, newValue)

    dfs.setZeroDigit(newValue)

    // There is no way to distinguish a shallow copy from deep copy.
    // The fields are either Char or immutable String. If
    // handling of the Currency class is added later, this test is
    // robust & correct, because Currency is immutable: no set methods.

    // Indeed, the copies are independent.
    val c = clone.asInstanceOf[DecimalFormatSymbols]
    assertNotEquals(dfs.getZeroDigit(), c.getZeroDigit())
  }

  @Test def equalsOverride(): Unit = {
    val dfs1 = new DecimalFormatSymbols()
    val dfs2 = new DecimalFormatSymbols()

    assertEquals(dfs1, dfs2)

    dfs1.setZeroDigit('X')
    assertNotEquals(dfs1, dfs2)
  }

  @Test def hashCodeOverride(): Unit = {
    val dfs1 = new DecimalFormatSymbols()
    val dfs2 = new DecimalFormatSymbols()

    assertEquals(dfs1.hashCode(), dfs2.hashCode())

    dfs1.setCurrencySymbol("\u20AC") // Unicode 'EURO SIGN'
    assertNotEquals(dfs1, dfs2)
  }

  // Object/Static methods

  @Test def getAvailableLocales(): Unit = {
    val result   = DecimalFormatSymbols.getAvailableLocales()
    val expected = Array(Locale.US)
    assertTrue(result.sameElements(expected))
  }

  @Test def getInstance(): Unit = {
    val dfs1 = DecimalFormatSymbols.getInstance()
    assertNotNull(dfs1)

    val dfs2 = new DecimalFormatSymbols(Locale.US)
    assertEquals(dfs1, dfs2)
  }

  @Test def getInstanceNullLocale: Unit = {
    try {
      val result = DecimalFormatSymbols.getInstance(null.asInstanceOf[Locale])
      fail("Expected Exception, should never get here");
    } catch {
      case ex: NullPointerException =>
        assertEquals(ex.getMessage, null)
      case _: Throwable => fail(s"Expected NullPointerException, got Thowable")
    }
  }

  @Test def getInstanceUnsupportedLocale: Unit = {
    val language    = "es" // Spanish
    val country     = "US"
    val unsupported = new Locale(language, country)

    try {
      val result = DecimalFormatSymbols.getInstance(unsupported)
      fail("Expected Exception, should never get here");
    } catch {
      case ex: MissingResourceException =>
        assertEquals(ex.getMessage, s"Locale not found: ${unsupported}")
      case _: Throwable =>
        fail(s"Expected NullPointerException, got Thowable")
    }
  }

  // instance methods

  @Test def getAndSetCurrencySymbol(): Unit = {
    val expectedGetResult = "$"
    val expectedSetResult = "\u20AC" // Unicode 'EURO SIGN'

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getCurrencySymbol, expectedGetResult)

    dfs.setCurrencySymbol(expectedSetResult)
    assertEquals(dfs.getCurrencySymbol, expectedSetResult)
  }

  @Test def getAndSetDecimalSeparator(): Unit = {
    val expectedGetResult = '.'
    val expectedSetResult = ',' // European usage

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getDecimalSeparator, expectedGetResult)

    dfs.setDecimalSeparator(expectedSetResult)
    assertEquals(dfs.getDecimalSeparator, expectedSetResult)
  }

  @Test def getAndSetDigit(): Unit = {
    val expectedGetResult = '#'
    val expectedSetResult = 'A' // arbitrary Junk

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getDigit, expectedGetResult)

    dfs.setDigit(expectedSetResult)
    assertEquals(dfs.getDigit, expectedSetResult)
  }

  @Test def getAndSetExponentSeparator(): Unit = {
    val expectedGetResult = "E"
    val expectedSetResult = "Garbage"

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getExponentSeparator, expectedGetResult)

    dfs.setExponentSeparator(expectedSetResult)
    assertEquals(dfs.getExponentSeparator, expectedSetResult)
  }

  @Test def getAndSetGroupingSeparator(): Unit = {
    val expectedGetResult = ','
    val expectedSetResult = '.' // European usage

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getGroupingSeparator, expectedGetResult)

    dfs.setGroupingSeparator(expectedSetResult)
    assertEquals(dfs.getGroupingSeparator, expectedSetResult)
  }

  @Test def getAndSetInfinity(): Unit = {
    val expectedGetResult = "\u221E" // Unicode 'INFINITY'
    val expectedSetResult = "Inf"    // plausible Junk

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getInfinity, expectedGetResult)

    dfs.setInfinity(expectedSetResult)
    assertEquals(dfs.getInfinity, expectedSetResult)
  }

  @Test def getAndSetInternationalCurrencySymbol(): Unit = {
    val expectedGetResult = "USD"
    val expectedSetResult = "EUR" // Euro

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getInternationalCurrencySymbol, expectedGetResult)

    dfs.setInternationalCurrencySymbol(expectedSetResult)
    assertEquals(dfs.getInternationalCurrencySymbol, expectedSetResult)
  }

  @Test def getAndSetMinusSign(): Unit = {
    val expectedGetResult = '-'
    val expectedSetResult = '\u2052' // Unicode COMMERCIAL MINUS SIGN, Germany

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getMinusSign, expectedGetResult)

    dfs.setMinusSign(expectedSetResult)
    assertEquals(dfs.getMinusSign, expectedSetResult)
  }

  @Test def getAndSetMonetaryDecimalSeparator(): Unit = {
    val expectedGetResult = '.'
    val expectedSetResult = ',' // France

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getMonetaryDecimalSeparator, expectedGetResult)

    dfs.setMonetaryDecimalSeparator(expectedSetResult)
    assertEquals(dfs.getMonetaryDecimalSeparator, expectedSetResult)

  }

  @Test def getAndSetNaN(): Unit = {
    val expectedGetResult = "\uFFFD" // Unicode 'REPLACEMENT CHARACTER'
    val expectedSetResult = "nAn"    // plausible Junk

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getNaN, expectedGetResult)

    dfs.setNaN(expectedSetResult)
    assertEquals(dfs.getNaN, expectedSetResult)
  }

  @Test def getAndSetPatternSeparator(): Unit = {
    val expectedGetResult = ';'
    val expectedSetResult = '/' // plausible Junk

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getPatternSeparator, expectedGetResult)

    dfs.setPatternSeparator(expectedSetResult)
    assertEquals(dfs.getPatternSeparator, expectedSetResult)
  }

  @Test def getAndSetPercent(): Unit = {
    val expectedGetResult = '%'
    val expectedSetResult = '\u2030' // plausible Junk, Unicode 'PER MILLE SIGN'

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getPercent, expectedGetResult)

    dfs.setPercent(expectedSetResult)
    assertEquals(dfs.getPercent, expectedSetResult)
  }

  @Test def getAndSetPerMill(): Unit = {
    val expectedGetResult = '\u2030' // Unicode 'PER MILLE SIGN'
    val expectedSetResult = '%'      // plausible Junk

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getPerMill, expectedGetResult)

    dfs.setPerMill(expectedSetResult)
    assertEquals(dfs.getPerMill, expectedSetResult)
  }

  @Test def getAndSetZeroDigit(): Unit = {
    val expectedGetResult = '0'
    val expectedSetResult = 'Z' // plausible Junk

    val dfs = DecimalFormatSymbols.getInstance()
    assertEquals(dfs.getZeroDigit, expectedGetResult)

    dfs.setZeroDigit(expectedSetResult)
    assertEquals(dfs.getZeroDigit, expectedSetResult)
  }
}
