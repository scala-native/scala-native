package java.util

/** Ported from Harmony, Scala.js and using Java API docs.
 *
 *  TODO: Commented out code needed to finish implementation.
 *
 *  Harmony defers much of the work to icu4j from
 *  [[http://site.icu-project.org/ ICU - International Components for Unicode]]
 */
final class Locale(
    languageRaw: String,
    countryRaw: String,
    variant: String,
    private val extensions: Map[Char, String]
) extends Serializable
    with Cloneable {

  private val language: String = languageRaw.toLowerCase()

  private val country: String = countryRaw.toUpperCase()

  if (language == null || country == null || variant == null)
    throw new NullPointerException()

  def this(languageRaw: String, countryRaw: String, variantRaw: String) =
    this(languageRaw, countryRaw, variantRaw, Collections.emptyMap())

  def this(language: String, country: String) = this(language, country, "")

  def this(language: String) = this(language, "", "")

  override def clone() =
    try {
      super.clone
    } catch {
      case e: CloneNotSupportedException => null
    }

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Locale =>
        getLanguage() == that.getLanguage() &&
          getCountry() == that.getCountry() &&
          getVariant() == that.getVariant() &&
          extensions == that.extensions
      case _ =>
        false
    }

  def getCountry(): String = country

  def hasExtensions(): Boolean = !extensions.isEmpty()

  def getExtension(key: Char): String = extensions.get(key)

  def getExtensionKeys(): Set[Char] = extensions.keySet()

  def getLanguage(): String = language

  def getVariant(): String = variant

  @inline override def hashCode(): Int =
    country.hashCode() +
      language.hashCode() +
      variant.hashCode() +
      extensions.hashCode()

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

  lazy val CANADA = new Locale("en", "CA")
  lazy val CANADA_FRENCH = new Locale("fr", "CA")
  lazy val CHINA = new Locale("zh", "CN")
  lazy val CHINESE = new Locale("zh", "")
  lazy val ENGLISH = new Locale("en", "")
  lazy val FRANCE = new Locale("fr", "FR")
  lazy val FRENCH = new Locale("fr", "")
  lazy val GERMAN = new Locale("de", "")
  lazy val GERMANY = new Locale("de", "DE")
  lazy val ITALIAN = new Locale("it", "")
  lazy val ITALY = new Locale("it", "IT")
  lazy val JAPAN = new Locale("ja", "JP")
  lazy val JAPANESE = new Locale("ja", "")
  lazy val KOREA = new Locale("ko", "KR")
  lazy val KOREAN = new Locale("ko", "")
  lazy val PRC = new Locale("zh", "CN")
  lazy val PRIVATE_USE_EXTENSION = 'x'
  lazy val ROOT = new Locale("", "", "")
  lazy val SIMPLIFIED_CHINESE = new Locale("zh", "CN")
  lazy val TAIWAN = new Locale("zh", "TW")
  lazy val TRADITIONAL_CHINESE = new Locale("zh", "TW")
  lazy val UK = new Locale("en", "GB")
  lazy val UNICODE_LOCALE_EXTENSION = 'u'
  lazy val US = new Locale("en", "US")

  def getDefault(): Locale = Locale.ROOT

  final class Builder {
    private var language: String = ""
    private var country: String = ""
    private var variant: String = ""
    private val extensions = new java.util.HashMap[Char, String]

    def setLanguage(language: String): Builder = {
      this.language = language.toLowerCase()
      this
    }

    def setCountry(country: String): Builder = {
      this.country = country.toUpperCase()
      this
    }

    def setVariant(variant: String): Builder = {
      this.variant = variant
      this
    }

    def setExtension(key: Char, value: String): Builder = {
      extensions.put(key, value)
      this
    }

    def build(): Locale = {
      new Locale(
        language,
        country,
        variant,
        extensions.clone().asInstanceOf[Map[Char, String]]
      )
    }
  }
}
