package java.util

//import java.{util => ju}

/** Ported from Harmony and using Java API docs.
 *
 * TODO: Commented out code needed to finish implementation.
 *
 * Harmony defers much of the work to icu4j from
 * [[http://site.icu-project.org/ ICU - International Components for Unicode]]
 */
final class Locale(val language: String,
                   val country: String,
                   val variant: String)
    extends Serializable
    with Cloneable {

  if (language == null || country == null || variant == null)
    throw new NullPointerException()

  def this(language: String) = this(language, "", "")

  def this(language: String, country: String) = this(language, country, "")

  override def clone() =
    try {
      super.clone
    } catch {
      case e: CloneNotSupportedException => null
    }

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Locale =>
        language == that.language &&
          country == that.country &&
          variant == that.variant
      case _ =>
        false
    }

  def getCountry(): String = country

  //def getDisplayCountry(): String = ???

  //def getDisplayCountry(locale: Locale): String = ???

  //def getDisplayLanguage(): String = ???

  //def getDisplayLanguage(locale: Locale): String = ???

  //def getDisplayName(): String = ???

  //def getDisplayName(locale: Locale): String = ???

  //def getDisplayScript(): String = ???

  //def getDisplayScript(locale: Locale): String = ???

  //def getDisplayVariant(): String = ???

  //def getDisplayVariant(locale: Locale): String = ???

  //def getExtension(key: Char): String = ???

  //def getExtensionKeys(): ju.Set[Character] = ???

  //def getISO3Country(): String = ???

  //def getISO3Language(): String = ???

  //def getLanguage(): String = language

  //def getScript(): String = ???

  //def getUnicodeLocaleAttributes(): ju.Set[String] = ???

  //def getUnicodeLocaleKeys(): ju.Set[String] = ???

  //def getUnicodeLocaleType(key: String) = ???

  //def getVariant(): String = variant

  @inline override def hashCode(): Int =
    country.hashCode() + language.hashCode() + variant.hashCode()

  //def toLanguageTag(): String = ???

  // Examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX"
  @inline override def toString(): String = {
    val buf = new StringBuilder
    buf.append(language)
    if (country.length > 0) {
      buf.append('_')
      buf.append(country)
    }
    if (variant.length > 0 && buf.nonEmpty) {
      if (0 == country.length) {
        buf.append("__")
      } else {
        buf.append('_')
      }
      buf.append(variant)
    }
    buf.toString
  }
}

object Locale {

  private[this] var defaultLocale = Locale.US

  lazy val CANADA                   = new Locale("en", "CA")
  lazy val CANADA_FRENCH            = new Locale("fr", "CA")
  lazy val CHINA                    = new Locale("zh", "CN")
  lazy val CHINESE                  = new Locale("zh", "")
  lazy val ENGLISH                  = new Locale("en", "")
  lazy val FRANCE                   = new Locale("fr", "FR")
  lazy val FRENCH                   = new Locale("fr", "")
  lazy val GERMAN                   = new Locale("de", "")
  lazy val GERMANY                  = new Locale("de", "DE")
  lazy val ITALIAN                  = new Locale("it", "")
  lazy val ITALY                    = new Locale("it", "IT")
  lazy val JAPAN                    = new Locale("ja", "JP")
  lazy val JAPANESE                 = new Locale("ja", "")
  lazy val KOREA                    = new Locale("ko", "KR")
  lazy val KOREAN                   = new Locale("ko", "")
  lazy val PRC                      = new Locale("zh", "CN")
  lazy val PRIVATE_USE_EXTENSION    = 'x'
  lazy val ROOT                     = new Locale("", "", "")
  lazy val SIMPLIFIED_CHINESE       = new Locale("zh", "CN")
  lazy val TAIWAN                   = new Locale("zh", "TW")
  lazy val TRADITIONAL_CHINESE      = new Locale("zh", "TW")
  lazy val UK                       = new Locale("en", "GB")
  lazy val UNICODE_LOCALE_EXTENSION = 'u'
  lazy val US                       = new Locale("en", "US")

  //def forLanguageTag(languageTag: String): String = ???

  // should have bundles for this list
  //def getAvailableLocales(): Array[Locale] = ???

  def getDefault(): Locale = defaultLocale

  //def getDefault(category: Locale.Category): Locale = ???

  //def getISOCountries(): Array[String] = ???

  //def getISOLanguages(): Array[String] = ???

  //def setDefault(category: Locale.Category, locale: Locale): Unit = ???

  def setDefault(locale: Locale): Unit =
    this.defaultLocale = locale

  //class Category
}
