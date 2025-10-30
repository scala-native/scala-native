package java.net

import java.util.ArrayList
import java.util.Arrays
import scala.annotation.switch
import java.util.Collections

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
    def hostIsSubdomain = hostL.endsWith(domainL)

    exactMatch || hostIsSubdomain

  }

  def parse(header: String): java.util.List[HttpCookie] = {
    if (header == null)
      throw new NullPointerException()
    else if (header == "")
      Collections.emptyList()
    else {
      val cookies = new ArrayList[HttpCookie]()

      val nameless =
        if (header.regionMatches(false, 0, "set-cookie2:", 0, 12)) {
          header.substring(12)
        } else if (header.regionMatches(false, 0, "set-cookie:", 0, 11))
          header.substring(11)
        else
          header

      val unprocessed =
        nameless.split(",")
        // splitUnquotedAt(nameless, ',')

      Arrays.asList(unprocessed).forEach { kv =>

        val eqIndex = kv.indexOf("=")
        require(eqIndex >= 0, "Invalid cookie name-value pair")
        val name = kv.substring(0, eqIndex)
        val value = kv.substring(eqIndex + 1)

        val cookie = parseValue(name.trim(), unquote(value.trim()))
        if (cookie != null) {
          cookies.add(cookie)
        }
      }

      cookies
    }
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
        } else if (inQuotes) {
          if (!escaping) {
            char match {
              case '\\' => escaping = true
              case '"'  => inQuotes = false
              case _    => ()
            }
          } else {
            escaping = false
          }

        } else if (char == '"') {
          inQuotes = true
        }

        i += 1

      }

      if (begin < in.length())
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

    val attrs =
      Arrays.asList(value.split(";"))
      // splitUnquotedAt(value, ';')

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

        propName.toLowerCase() match {
          case "discard" =>
            cookie.setDiscard(true)
          case "comment" =>
            cookie.setComment(propValue)
          case "commenturl" =>
            cookie.setCommentURL(propValue)
          case "domain" =>
            cookie.setDomain(propValue)
          case "max-age" =>
            try {
              cookie.setMaxAge(java.lang.Long.valueOf(propValue))
            } catch {
              case _: NumberFormatException =>
                throw new IllegalArgumentException(
                  "Illegal cookie max-age attribute"
                )
            }
          case "path" =>
            cookie.setPath(propValue)
          case "port" =>
            cookie.setPortlist(propValue)
          case "secure" =>
            cookie.setSecure(true)
          case "version" =>
            try {
              val value = java.lang.Integer.valueOf(propValue)
              cookie.setVersion(value)
            } catch {
              case _: NumberFormatException => ()
            }
          case "httponly" =>
            cookie.setHttpOnly(true)
          case _ => ()
        }

        i += 1
      }
      cookie
    }
  }

  private def validateName(name: String): Unit = {
    // Testing the $ as that's what the JVM
    // seems to do
    if (name.length == 0 || name.charAt(0) == '$')
      throw new IllegalArgumentException("Illegal cookie name")

    name.foreach { char =>
      val isIllegal = (char: @switch) match {
        case 127 | ',' | ';' | ' ' | '\t' => true
        case char                         => char <= 32
      }
      if (isIllegal) throw new IllegalArgumentException("Illegal cookie name")
    }
  }

}

/** HttpCookie */
final class HttpCookie private (
    private val _name: String = null,
    private var _value: String = null
) extends Cloneable {

  if (_name.isEmpty())
    throw new IllegalArgumentException("Illegal cookie name")

  HttpCookie.validateName(_name)

  private val createdAtEpochMillis: Long = System.currentTimeMillis()
  private var _comment: String = null
  private var _commentURL: String = null
  private var _httpOnly: Boolean = false
  private var _discard: Boolean = false
  private var _domain: String = null
  private var _maxAge: Long = -1
  private var _path: String = null
  private var _portlist: String = null
  private var _secure: Boolean = false
  private var _version: Int = 1

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
        this.getName().equalsIgnoreCase(other.getName()) &&
          this.getDomain().equalsIgnoreCase(other.getDomain()) &&
          this.getPath().equalsIgnoreCase(other.getPath())

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
  def hasExpired(): Boolean =
    _maxAge >= 0 &&
      (_maxAge == 0 || System
        .currentTimeMillis() >= createdAtEpochMillis + _maxAge * 1000)

  def isHttpOnly(): Boolean = this._httpOnly
  def setComment(purpose: String): Unit = this._comment = purpose
  def setCommentURL(purpose: String): Unit = this._commentURL = purpose
  def setDiscard(discard: Boolean): Unit = this._discard = discard
  def setDomain(pattern: String): Unit = this._domain = pattern.toLowerCase()
  def setHttpOnly(httpOnly: Boolean): Unit = this._httpOnly = httpOnly
  def setMaxAge(expiry: Long): Unit = this._maxAge = expiry
  def setPath(uri: String): Unit = this._path = uri
  def setPortlist(ports: String): Unit = this._portlist = ports
  def setSecure(flag: Boolean): Unit = this._secure = flag
  def setValue(newValue: String): Unit = this._value = newValue
  def setVersion(v: Int): Unit = {
    if (v != 0 && v != 1) {
      throw new IllegalArgumentException(
        "cookie version should be 0 or 1"
      )
    }
    this._version = v
  }

}
