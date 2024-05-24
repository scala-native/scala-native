package java.net

import java.util.ArrayList

object HttpCookie {

  def domainMatches(domain: String, host: String): Boolean = {
    val effectiveHost = if (host.indexOf('.') == -1) {
      host + ".local"
    } else
      host

    val domainL = domain.toLowerCase
    val hostL = host.toLowerCase

    val exactMatch = domainL == hostL
    // a host is a subdomain if it ends with the domain, and
    // the character preceeding the domain suffix is a dot
    val hostIsSubdomain = hostL.endsWith("." + domainL)

    domainL == hostL || hostIsSubdomain

  }

  private final val Discard = "Discard"
  private final val Comment = "Comment"
  private final val CommentURL = "CommentURL"
  private final val Domain = "Domain"
  private final val MaxAge = "Max-Age"
  private final val Path = "Path"
  private final val Port = "Port"
  private final val Secure = "Secure"
  private final val Version = "Version"
  private final val HttpOnly = "HttpOnly"

  def parse(header: String): java.util.List[HttpCookie] = {

    val cookies = new ArrayList[HttpCookie]()

    if (header == null)
      throw new NullPointerException()
    else if (header == "")
      cookies
    else {

      val nameless =
        if (header.regionMatches(false, 0, "set-cookie2:", 0, 12)) {
          header.substring(12)
        } else if (header.regionMatches(false, 0, "set-cookie:", 0, 11))
          header.substring(11)
        else
          header

      val unprocessed = splitUnquotedAt(nameless, ',')

      var i = 0
      while (i < unprocessed.size()) {
        val kv = unprocessed.get(i)
        val eqIndex = kv.indexOf("=")
        val name = kv.substring(0, eqIndex)
        val value = kv.substring(eqIndex + 1)

        val cookie = parseValue(name.trim(), unquote(value.trim()))
        if (cookie != null) {
          cookies.add(cookie)
        }

        i += 1

      }

    }

    cookies

  }

  // Splits the string at the given character,
  // however it also respects quoted strings, i.e.
  // occurences of the separator inside quoted strings
  // won't trigger a split
  // backslash also escapes double quotes inside the quoted sections
  private def splitUnquotedAt(
      in: String,
      separator: Char
  ): java.util.List[String] = {

    val res = new ArrayList[String]()
    if (in == "") {
      res
    } else {

      var i = 0
      var begin = 0
      var inQuotes = false
      var escaping = false

      while (i < in.length()) {

        val char = in.charAt(i)

        if (char == separator && !inQuotes) {
          res.add(in.substring(begin, i))
          begin = i + 1
        } else if (char == '\\' && inQuotes && !escaping) {
          escaping = true
        } else if (char == '"' && inQuotes && !escaping) {
          inQuotes = false
        } else if (char == '"' && !inQuotes) {
          inQuotes = true
        }

        i += 1

      }

      res.add(in.substring(begin))

      res
    }
  }

  /** If the string begins and ends with quotes - remove them, otherwise return
   *  the input
   */
  private def unquote(in: String): String = {
    if (in.charAt(0) == '"' && in.charAt(in.length() - 1) == '"') {
      in.substring(1, in.length() - 1)
    } else in
  }

  /** performs parsing on the section of the cookie following the cookie name,
   *  and constructs the resulting cookie. If the given value contains multiple
   *  cookies - this method will break, so any input into this method must be
   *  pre-processed such that only the section that pertains to a single cookie
   *  is fed into this method
   */
  private def parseValue(name: String, value: String): HttpCookie = {

    val attrs = splitUnquotedAt(value, ';')

    if (attrs.size() == 0) {
      new HttpCookie(name, "")
    } else {

      val value = attrs.get(0)

      val cookie = new HttpCookie(name, unquote(value.trim()))

      var i = 1
      while (i < attrs.size()) {

        val prop = attrs.get(i).trim()

        val sep = prop.indexOf('=')

        val propValue: String = if (sep == -1) {
          null
        } else {
          unquote(prop.substring(sep + 1).trim())
        }

        val propName =
          if (sep == -1)
            prop
          else {
            prop.substring(0, sep).trim()
          }

        if (propName.regionMatches(true, 0, Discard, 0, Discard.length())) {
          cookie.setDiscard(true)

          // We have to check commenturl before comment because comment matches commenturl
        } else if (propName.regionMatches(
              true,
              0,
              CommentURL,
              0,
              CommentURL.length()
            )) {
          cookie.setCommentURL(propValue)
        } else if (propName.regionMatches(
              true,
              0,
              Comment,
              0,
              Comment.length()
            )) {
          cookie.setComment(propValue)
        } else if (propName.regionMatches(
              true,
              0,
              Domain,
              0,
              Domain.length()
            )) {
          cookie.setDomain(propValue)
        } else if (propName.regionMatches(
              true,
              0,
              MaxAge,
              0,
              MaxAge.length()
            )) {
          try {
            cookie.setMaxAge(java.lang.Long.valueOf(propValue))
          } catch {
            case _: NumberFormatException =>
              throw new IllegalArgumentException(
                "Illegal cookie max-age attribute"
              )
          }
        } else if (propName.regionMatches(true, 0, Path, 0, Path.length())) {
          cookie.setPath(propValue)
        } else if (propName.regionMatches(true, 0, Port, 0, Port.length())) {
          cookie.setPortlist(propValue)
        } else if (propName.regionMatches(
              true,
              0,
              Secure,
              0,
              Secure.length()
            )) {
          cookie.setSecure(true)
        } else if (propName.regionMatches(
              true,
              0,
              HttpOnly,
              0,
              HttpOnly.length()
            )) {
          cookie.setHttpOnly(true)
        } else if (propName.regionMatches(
              true,
              0,
              Version,
              0,
              Version.length()
            )) {
          try {
            val value = java.lang.Integer.valueOf(propValue)
            if (value != 0 && value != 1) {
              throw new IllegalArgumentException(
                "cookie version should be 0 or 1"
              )
            }
            cookie.setVersion(value)
          } catch {
            case _: NumberFormatException =>
              throw new IllegalArgumentException(
                "Illegal cookie max-age attribute"
              )
          }
        }

        i += 1

      }

      cookie

    }

  }

  // NAME = attr
  // attr = token
  // token = 1*<any CHAR except CTLs or separators>
  // separators = "(" | ")" | "<" | ">" | "@"
  //                | "," | ";" | ":" | "\" | <">
  //                | "/" | "[" | "]" | "?" | "="
  //                | "{" | "}" | SP | HT
  // CHAR = <any US-ASCII character (octets 0 - 127)>
  // CTL = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
  //
  // stricter than the JVM, for better or worse
  private def validateName(name: String): String = {
    // Testing the $ as that's what the JVM
    // seems to do
    if (name.length == 0 || name.charAt(0) == '$')
      throw new IllegalArgumentException("Illegal cookie name")

    var i = 0
    while (i < name.length()) {
      val char = name.charAt(i)
      if (char < 32 || // CTL
          char == 127 || // CTL
          char == '(' ||
          char == ')' ||
          char == '<' ||
          char == '>' ||
          char == '@' ||
          char == ',' ||
          char == ';' ||
          char == ':' ||
          char == '\\' ||
          char == '"' ||
          char == '/' ||
          char == '[' ||
          char == ']' ||
          char == '?' ||
          char == '=' ||
          char == '{' ||
          char == '}' ||
          char == ' ' ||
          char == '\t') {
        throw new IllegalArgumentException("Illegal cookie name")
      }

      i += 1
    }

    name

  }

}

/** HttpCookie */
final class HttpCookie private (
    private val createdAtEpochSecond: Long,
    private var _comment: String = null,
    private var _commentURL: String = null,
    private var _httpOnly: Boolean = false,
    private var _discard: Boolean = false,
    private var _domain: String = null,
    private var _maxAge: Long = -1,
    private var _name: String = null,
    private var _path: String = null,
    private var _portlist: String = null,
    private var _secure: Boolean = false,
    private var _value: String = null,
    private var _version: Int = 1
) extends Cloneable {

  def this(name: String, value: String) = {
    this(
      System.currentTimeMillis() % 1000,
      _name =
        (if (name.length() == 0) {
           throw new IllegalArgumentException("Illegal cookie name")
         } else
           HttpCookie.validateName(name)),
      _value = value
    )
  }

  def canEqual(other: Any): Boolean =
    other.isInstanceOf[HttpCookie]

  override def hashCode(): Int = {
    var res = 1
    // the JVM seems to include
    // the name, value, domain and path into its
    // hash code, so that's what we do as well
    res = 31 * res + _name.##
    if (_value != null) res = 31 * res + _value.##
    if (_path != null) res = 31 * res + _path.##
    if (_domain != null) res = 31 * res + _domain.##
    res
  }

  override def toString(): String = _name + "=" + _value
  override def equals(obj: Any): Boolean =
    obj match {
      case other: HttpCookie =>
        super.equals(other) &&
          canEqual(other) &&
          this.getName() == other.getName() &&
          this.getValue() == other.getValue() &&
          this.getComment() == other.getComment() &&
          this.getCommentURL() == other.getCommentURL() &&
          this.isHttpOnly() == other.isHttpOnly() &&
          this.getDiscard() == other.getDiscard() &&
          this.getDomain() == other.getDomain() &&
          this.getMaxAge() == other.getMaxAge() &&
          this.getPath() == other.getPath() &&
          this.getPortlist() == other.getPortlist() &&
          this.getSecure() == other.getSecure() &&
          this.getVersion() == other.getVersion()

      case _ => false
    }

  def getComment(): String = this._comment
  def getCommentURL(): String = this._commentURL
  def getDiscard(): Boolean = this._discard
  def getDomain(): String = this._domain
  def getMaxAge(): Long = this._maxAge
  def getName(): String = this._name
  def getPath(): String = this._path
  def getPortlist(): String = this._portlist
  def getSecure(): Boolean = this._secure
  def getValue(): String = this._value
  def getVersion(): Int = this._version
  def hasExpired(): Boolean = {
    _maxAge >= 0 && ((System
      .currentTimeMillis() % 1000) >= (createdAtEpochSecond + _maxAge))
  }
  def isHttpOnly(): Boolean = this._httpOnly
  def setComment(purpose: String): Unit = this._comment = purpose
  def setCommentURL(purpose: String): Unit = this._commentURL = purpose
  def setDiscard(discard: Boolean): Unit = this._discard = discard
  def setDomain(pattern: String): Unit = this._domain = pattern
  def setHttpOnly(httpOnly: Boolean): Unit = this._httpOnly = httpOnly
  def setMaxAge(expiry: Long): Unit = this._maxAge = expiry
  def setPath(uri: String): Unit = this._path = uri
  def setPortlist(ports: String): Unit = this._portlist = ports
  def setSecure(flag: Boolean): Unit = this._secure = flag
  def setValue(newValue: String): Unit = this._value = newValue
  def setVersion(v: Int): Unit = this._version = v

}
