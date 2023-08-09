package java.lang

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.string.memcmp
import java.io.Serializable
import java.util._
import java.util.regex._
import java.nio._
import java.nio.charset._
import java.util.Objects
import java.util.ScalaOps._
import java.lang.constant.{Constable, ConstantDesc}
import scala.annotation.{switch, tailrec}
import _String.{string2_string, _string2string}

final class _String()
    extends Serializable
    with Comparable[_String]
    with CharSequence
    with Constable
    with ConstantDesc {
  protected[_String] var value: Array[Char] = _
  protected[_String] var offset: Int = 0
  protected[_String] var count: Int = 0
  protected[_String] var cachedHashCode: Int = _

  @inline
  private def thisString: String =
    this.asInstanceOf[String]

  def this(data: Array[scala.Byte], high: Int, start: Int, length: Int) = {
    this()
    if (length <= data.length - start && start >= 0 && 0 <= length) {
      offset = 0
      count = length
      value = {
        val value = new Array[Char](length)
        val highByte = (high & 0xff) << 8
        var i = 0
        while (i < length) {
          value(i) = (highByte | (data(start + i) & 0xff)).toChar
          i += 1
        }
        value
      }
    } else {
      throw new StringIndexOutOfBoundsException()
    }
  }

  def this(
      data: Array[scala.Byte],
      start: Int,
      length: Int,
      encoding: Charset
  ) = {
    this()
    offset = 0
    val charBuffer = encoding.decode(ByteBuffer.wrap(data, start, length))
    value = charBuffer.array()
    count = charBuffer.length()
  }

  def this(
      data: Array[scala.Byte],
      start: Int,
      length: Int,
      encoding: _String
  ) = {
    this(
      data,
      start,
      length,
      try {
        Charset.forName(Objects.requireNonNull(encoding))
      } catch {
        case e: UnsupportedCharsetException =>
          throw new java.io.UnsupportedEncodingException(encoding)
      }
    )
  }

  def this(data: Array[scala.Byte], start: Int, length: Int) =
    this(data, start, length, Charset.defaultCharset())

  def this(data: Array[scala.Byte], high: Int) =
    this(data, high, 0, data.length)

  def this(data: Array[scala.Byte], encoding: _String) =
    this(data, 0, data.length, encoding)

  def this(data: Array[scala.Byte], encoding: Charset) =
    this(data, 0, data.length, encoding)

  def this(data: Array[scala.Byte]) =
    this(data, 0, data.length)

  def this(data: Array[Char], start: Int, length: Int) = {
    this()
    if (start >= 0 && 0 <= length && length <= data.length - start) {
      offset = 0
      value = new Array[Char](length)
      count = length
      System.arraycopy(data, start, value, 0, count)
    } else {
      throw new StringIndexOutOfBoundsException()
    }
  }

  def this(data: Array[Char]) =
    this(data, 0, data.length)

  /* Note Well:
   *   This constructor creates a MUTABLE String. That violates
   *   the immutable JVM specification, but is useful strictly within the
   *   confines of "java[lang]".
   *
   *   Any code with access to the "data" Array can change the content and
   *   that change will also be in a String created with this constructor.
   *   Use with knowledge, wisdom, and discretion.
   */
  private[lang] def this(start: Int, length: Int, data: Array[Char]) = {
    this()
    value = data
    offset = start
    count = length
  }

  def this(string: _String) = {
    this()
    value = string.value
    offset = string.offset
    count = string.count
  }

  def this(sb: StringBuffer) = {
    this()
    count = sb.length()
    value = new Array[Char](count)
    sb.getChars(0, count, value, 0)
  }

  def this(codePoints: Array[Int], offset: Int, count: Int) = {
    this()
    if (offset < 0 || count < 0 || offset > codePoints.length - count) {
      throw new StringIndexOutOfBoundsException()
    } else {
      this.offset = 0
      this.value = new Array[Char](count * 2)
      this.count = {
        var c = 0
        var i = offset
        while (i < offset + count) {
          c += Character.toChars(codePoints(i), this.value, c)
          i += 1
        }
        c
      }
    }
  }

  def this(sb: java.lang.StringBuilder) = {
    this()
    count = sb.length()
    value = new Array[Char](count)
    sb.getChars(0, count, value, 0)
  }

  def charAt(index: Int): Char = {
    if (0 <= index && index < count) {
      value(offset + index)
    } else {
      throw new StringIndexOutOfBoundsException()
    }
  }

  private def compareValue(ch: Char): Char =
    Character.toLowerCase(Character.toUpperCase(ch))

  private def toLowerCase(ch: Char): Char =
    Character.toLowerCase(ch)

  private def toUpperCase(ch: Char): Char =
    Character.toUpperCase(ch)

  def compareTo(string: _String): Int = {
    var o1 = offset
    var o2 = string.offset
    val end =
      if (count < string.count) offset + count
      else offset + string.count
    val target = string.value
    while (o1 < end) {
      val result = value(o1) - target(o2)
      o1 += 1
      o2 += 1
      if (result != 0) {
        return result
      }
    }
    count - string.count
  }

  def compareToIgnoreCase(string: _String): Int = {
    var o1 = offset
    var o2 = string.offset
    val end =
      if (count < string.count) offset + count
      else offset + string.count
    while (o1 < end) {
      val c1: Char = compareValue(value(o1))
      val c2: Char = compareValue(string.value(o2))
      o1 += 1
      o2 += 1
      val result: Int = c1 - c2
      if (result != 0) {
        return result
      }
    }
    count - string.count
  }

  def concat(string: _String): _String = {
    if (string.count == 0) {
      this
    } else {
      val buffer = new Array[Char](count + string.count)

      if (count > 0) {
        System.arraycopy(value, offset, buffer, 0, count)
      }

      System
        .arraycopy(string.value, string.offset, buffer, count, string.count)

      new _String(0, buffer.length, buffer)
    }
  }

  def endsWith(suffix: _String): scala.Boolean =
    regionMatches(count - suffix.count, suffix, 0, suffix.count)

  override def equals(obj: Any): scala.Boolean = obj match {
    case s: _String =>
      if (s eq this) {
        true
      } else {
        val thisCount = this.count
        val thatCount = s.count
        if (thisCount != thatCount) {
          false
        } else if (thisCount == 0 && thatCount == 0) {
          true
        } else {
          val thisHash = this.cachedHashCode
          val thatHash = s.cachedHashCode
          if (thisHash != thatHash && thisHash != 0 && thatHash != 0) {
            false
          } else {
            val data1 =
              value
                .at(offset)
                .asInstanceOf[Ptr[scala.Byte]]
            val data2 =
              s.value
                .at(s.offset)
                .asInstanceOf[Ptr[scala.Byte]]
            memcmp(data1, data2, (count * 2).toUInt) == 0
          }
        }
      }
    case _ =>
      false
  }

  def equalsIgnoreCase(string: _String): scala.Boolean = {
    if (string == this) {
      true
    } else if (string == null || count != string.count) {
      false
    } else {
      var o1 = offset
      var o2 = string.offset
      while (o1 < offset + count) {
        val c1 = value(o1)
        val c2 = string.value(o2)
        o1 += 1
        o2 += 1
        if (c1 != c2 && toUpperCase(c1) != toUpperCase(c2) &&
            toLowerCase(c1) != toLowerCase(c2)) {
          return false
        }
      }
      true
    }
  }

  def getBytes(): Array[scala.Byte] = {
    val buffer =
      Charset.defaultCharset().encode(CharBuffer.wrap(value, offset, count))
    val bytes = new Array[scala.Byte](buffer.limit())
    buffer.get(bytes)
    bytes
  }

  @Deprecated
  def getBytes(
      start: Int,
      _end: Int,
      data: Array[scala.Byte],
      _index: Int
  ): Unit = {
    var end = _end
    if (0 <= start && start <= end && end <= count) {
      end += offset

      try {
        var index = _index
        var i = offset + start
        while (i < end) {
          data(index) = value(i).toByte
          index += 1
          i += 1
        }
      } catch {
        case e: ArrayIndexOutOfBoundsException =>
          throw new StringIndexOutOfBoundsException()
      }
    } else {
      throw new StringIndexOutOfBoundsException()
    }
  }

  def getBytes(encoding: _String): Array[scala.Byte] = {
    val charset =
      try {
        Charset.forName(encoding)
      } catch {
        case e: UnsupportedCharsetException =>
          throw new java.io.UnsupportedEncodingException(encoding)
      }
    val buffer = charset.encode(CharBuffer.wrap(value, offset, count))
    val bytes = new Array[scala.Byte](buffer.limit())
    buffer.get(bytes)
    bytes
  }

  def getBytes(encoding: Charset): Array[scala.Byte] = {
    val buffer = encoding.encode(CharBuffer.wrap(value, offset, count))
    val bytes = new Array[scala.Byte](buffer.limit())
    buffer.get(bytes)
    bytes
  }

  def getChars(start: Int, end: Int, buffer: Array[Char], index: Int): Unit = {
    if (0 <= start && start <= end && end <= count) {
      System.arraycopy(value, start + offset, buffer, index, end - start)
    } else {
      throw new StringIndexOutOfBoundsException()
    }
  }

  // Update StringLowering::stringHashCode whenever you change this method.
  override def hashCode(): Int = {
    val currentHashCode = cachedHashCode
    if (currentHashCode == 0) {
      if (count == 0) {
        0
      } else {
        val data = value.at(offset)
        var hash = 0
        var i = 0
        while (i < count) {
          hash = data(i) + ((hash << 5) - hash)
          i += 1
        }
        cachedHashCode = hash
        hash
      }
    } else {
      currentHashCode
    }
  }

  def indexOf(c: Int, _start: Int): Int = {
    var start = _start
    if (start < count) {
      if (start < 0) {
        start = 0
      }
      if (c >= 0 && c <= Character.MAX_VALUE) {
        var i = offset + start
        while (i < offset + count) {
          if (value(i) == c) {
            return i - offset
          }
          i += 1
        }
      } else if (c > Character.MAX_VALUE && c <= Character.MAX_CODE_POINT) {
        var i = start
        while (i < count) {
          val codePoint = codePointAt(i)
          if (codePoint == c) {
            return i
          } else if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            i += 1
          }
          i += 1
        }
      }
    }
    -1
  }

  def indexOf(c: Int): Int =
    indexOf(c, 0)

  def indexOf(string: _String): Int =
    indexOf(string, 0)

  def indexOf(subString: _String, _start: Int): Int = {
    var start = _start
    if (start < 0) {
      start = 0
    }
    val subCount = subString.count
    if (subCount > 0) {
      if (subCount + start > count) {
        return -1
      }
      val target = subString.value
      val subOffset = subString.offset
      val firstChar = target(subOffset)
      val end = subOffset + subCount
      while (true) {
        val i = indexOf(firstChar, start)
        if (i == -1 || subCount + i > count) {
          return -1
        }
        var o1 = offset + i
        var o2 = subOffset
        while ({ o2 += 1; o2 } < end && value({ o1 += 1; o1 }) == target(o2)) ()
        if (o2 == end) {
          return i
        }
        start = i + 1
      }
    }
    if (start < count) start else count
  }

  // See https://github.com/scala-native/scala-native/issues/486
  def intern(): _String = this

  def lastIndexOf(c: Int): Int =
    lastIndexOf(c, count - 1)

  def lastIndexOf(c: Int, _start: Int): Int = {
    var start = _start
    if (start >= 0) {
      if (start >= count) {
        start = count - 1
      }
      if (c >= 0 && c <= Character.MAX_VALUE) {
        var i = offset + start
        while (i >= offset) {
          if (value(i) == c) {
            return i - offset
          } else {
            i -= 1
          }
        }
      } else if (c > Character.MAX_VALUE && c <= Character.MAX_CODE_POINT) {
        var i = start
        while (i >= 0) {
          val codePoint = codePointAt(i)
          if (codePoint == c) {
            return i
          } else if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            i -= 1
          }

          i -= 1
        }
      }
    }
    -1
  }

  def lastIndexOf(string: String): Int =
    lastIndexOf(string, count)

  def lastIndexOf(subString: _String, _start: Int): Int = {
    var start = _start
    val subCount = subString.count
    if (subCount <= count && start >= 0) {
      if (subCount > 0) {
        if (start > count - subCount) {
          start = count - subCount
        }
        val target = subString.value
        val subOffset = subString.offset
        val firstChar = target(subOffset)
        val end = subOffset + subCount
        while (true) {
          val i = lastIndexOf(firstChar, start)
          if (i == -1) {
            return -1
          }
          var o1 = offset + i
          var o2 = subOffset
          while ({ o2 += 1; o2 } < end && value({ o1 += 1; o1 }) == target(o2))
            ()
          if (o2 == end) {
            return i
          }
          start = i - 1
        }
      }

      if (start < count) start else count
    } else {
      -1
    }
  }

  def length(): Int = count

  def isEmpty(): scala.Boolean = 0 == count

  def regionMatches(
      thisStart: Int,
      string: _String,
      start: Int,
      length: Int
  ): scala.Boolean = {
    if (string.count - start < length || start < 0) {
      false
    } else if (thisStart < 0 || count - thisStart < length) {
      false
    } else if (length <= 0) {
      true
    } else {
      val o1 = offset + thisStart
      val o2 = string.offset + start

      var i = 0
      while (i < length) {
        if (value(o1 + i) != string.value(o2 + i)) {
          return false
        }
        i += 1
      }

      true
    }
  }

  def regionMatches(
      ignoreCase: scala.Boolean,
      _thisStart: Int,
      string: _String,
      _start: Int,
      length: Int
  ): scala.Boolean = {
    var thisStart = _thisStart
    var start = _start
    if (!ignoreCase) {
      regionMatches(thisStart, string, start, length)
    } else if (string != null) {
      if (thisStart < 0 || length > count - thisStart) {
        false
      } else if (start < 0 || length > string.count - start) {
        false
      } else {
        thisStart += offset
        start += string.offset
        val end = thisStart + length
        val target = string.value

        while (thisStart < end) {
          val c1 = value(thisStart)
          val c2 = target(start)
          thisStart += 1
          start += 1
          if (c1 != c2 && toUpperCase(c1) != toUpperCase(c2) &&
              toLowerCase(c1) != toLowerCase(c2)) {
            return false
          }
        }

        true
      }
    } else {
      throw new NullPointerException()
    }
  }

  def repeat(count: Int): String = {
    if (count < 0) {
      throw new IllegalArgumentException
    } else if (thisString == "" || count == 0) {
      ""
    } else if (thisString.length > (Int.MaxValue / count)) {
      throw new OutOfMemoryError
    } else {
      val resultLength = thisString.length * count
      val out = new StringBuilder(resultLength)
      out.append(thisString)
      var remainingIters = 31 - Integer.numberOfLeadingZeros(count)
      while (remainingIters > 0) {
        out.append(out.toString)
        remainingIters -= 1
      }
      val outLength = out.length()
      val remaining = resultLength - outLength
      if (remaining <= outLength) {
        out.append(out.substring(0, remaining))
      }
      out.toString
    }
  }

  def replace(oldChar: Char, newChar: Char): _String = {
    var index = indexOf(oldChar, 0)
    if (index == -1) {
      this
    } else {
      val buffer = new Array[Char](count)
      System.arraycopy(value, offset, buffer, 0, count)

      while ({
        buffer(index) = newChar
        index += 1
        index = indexOf(oldChar, index)
        index != -1
      }) ()

      new _String(0, count, buffer)
    }
  }

  def replace(target: CharSequence, replacement: CharSequence): _String = {
    if (target == null) {
      throw new NullPointerException("target should not be null")
    } else if (replacement == null) {
      throw new NullPointerException("replacement should not be null")
    } else {
      val ts = target.toString
      var index = indexOf(ts, 0)

      if (index == -1) return this

      val rs = replacement.toString

      if (ts.isEmpty()) {
        val buffer =
          new java.lang.StringBuilder(count + (rs.length() * (count + 1)))
        buffer.append(rs)

        var i = 0
        while (i < count) {
          buffer.append(value(offset + i))
          buffer.append(rs)
          i += 1
        }

        return buffer.toString
      }

      val buffer = new java.lang.StringBuilder(count + rs.length)
      val tl = target.length()
      var tail = 0
      while ({
        buffer.append(value, offset + tail, index - tail)
        buffer.append(rs)
        tail = index + tl
        index = indexOf(ts, tail)
        index != -1
      }) ()
      buffer.append(value, offset + tail, count - tail)

      buffer.toString
    }
  }

  def startsWith(prefix: _String, start: Int): scala.Boolean =
    regionMatches(start, prefix, 0, prefix.count)

  def startsWith(prefix: _String): scala.Boolean =
    startsWith(prefix, 0)

  def substring(start: Int): _String =
    if (start == 0) {
      this
    } else if (0 <= start && start <= count) {
      new _String(offset + start, count - start, value)
    } else {
      throw new StringIndexOutOfBoundsException(start)
    }

  def substring(start: Int, end: Int): _String =
    if (start == 0 && end == count) {
      this
    } else {
      if (start < 0) {
        throw new StringIndexOutOfBoundsException(start)
      } else if (start > end) {
        throw new StringIndexOutOfBoundsException(end - start)
      } else if (end > count) {
        throw new StringIndexOutOfBoundsException(end)
      }

      new _String(offset + start, end - start, value)
    }

  def toCharArray(): Array[Char] = {
    val buffer = new Array[Char](count)
    System.arraycopy(value, offset, buffer, 0, count)
    buffer
  }

  /* Ported from Scala.js, commit: ac38a148, dated: 2020-09-25
   *
   * The overloads without an explicit locale use the default locale, which is
   * the root locale by specification. They are implemented by direct
   * delegation to ECMAScript's `toLowerCase()` and `toUpperCase()`, which are
   * specified as locale-insensitive, therefore equivalent to the root locale.
   *
   * It turns out virtually every locale behaves in the same way as the root
   * locale for default case algorithms. Only Lithuanian (lt), Turkish (tr)
   * and Azeri (az) have different behaviors.
   *
   * The overloads with a `Locale` specifically test for those three languages
   * and delegate to dedicated methods to handle them. Those methods start by
   * handling their respective special cases, then delegate to the locale-
   * insensitive version. The special cases are specified in the Unicode
   * reference file at
   *
   *   https://unicode.org/Public/13.0.0/ucd/SpecialCasing.txt
   *
   * That file first contains a bunch of locale-insensitive special cases,
   * which we do not need to handle. Only the last two sections about locale-
   * sensitive special-cases are important for us.
   *
   * Some of the rules are further context-sensitive, using predicates that are
   * defined in Section 3.13 "Default Case Algorithms" of the Unicode Standard,
   * available at
   *
   *   http://www.unicode.org/versions/Unicode13.0.0/
   *
   * We based the implementations on Unicode 13.0.0. It is worth noting that
   * there has been no non-comment changes in the SpecialCasing.txt file
   * between Unicode 4.1.0 and 13.0.0 (perhaps even earlier; the version 4.1.0
   * is the earliest that is easily accessible).
   */
  def toLowerCase(locale: Locale): String = {
    locale.getLanguage() match {
      case "lt"        => toLowerCaseLithuanian()
      case "tr" | "az" => toLowerCaseTurkishAndAzeri()
      case _           => toLowerCase()
    }
  }

  private def toLowerCaseLithuanian(): String = {
    /* Relevant excerpt from SpecialCasing.txt
     *
     * # Lithuanian
     *
     * # Lithuanian retains the dot in a lowercase i when followed by accents.
     *
     * [...]
     *
     * # Introduce an explicit dot above when lowercasing capital I's and J's
     * # whenever there are more accents above.
     * # (of the accents used in Lithuanian: grave, acute, tilde above, and ogonek)
     *
     * 0049; 0069 0307; 0049; 0049; lt More_Above; # LATIN CAPITAL LETTER I
     * 004A; 006A 0307; 004A; 004A; lt More_Above; # LATIN CAPITAL LETTER J
     * 012E; 012F 0307; 012E; 012E; lt More_Above; # LATIN CAPITAL LETTER I WITH OGONEK
     * 00CC; 0069 0307 0300; 00CC; 00CC; lt; # LATIN CAPITAL LETTER I WITH GRAVE
     * 00CD; 0069 0307 0301; 00CD; 00CD; lt; # LATIN CAPITAL LETTER I WITH ACUTE
     * 0128; 0069 0307 0303; 0128; 0128; lt; # LATIN CAPITAL LETTER I WITH TILDE
     */

    /* Tests whether we are in an `More_Above` context.
     * From Table 3.17 in the Unicode standard:
     * - Description: C is followed by a character of combining class
     *   230 (Above) with no intervening character of combining class 0 or
     *   230 (Above).
     * - Regex, after C: [^\p{ccc=230}\p{ccc=0}]*[\p{ccc=230}]
     */
    def moreAbove(i: Int): scala.Boolean = {
      import Character._
      val len = length()

      @tailrec def loop(j: Int): scala.Boolean = {
        if (j == len) {
          false
        } else {
          val cp = this.codePointAt(j)
          combiningClassNoneOrAboveOrOther(cp) match {
            case CombiningClassIsNone  => false
            case CombiningClassIsAbove => true
            case _                     => loop(j + Character.charCount(cp))
          }
        }
      }

      loop(i + 1)
    }
    val preprocessed = replaceCharsAtIndex { i =>
      def ifMoreAboveOrNull(v: String) = if (moreAbove(i)) v else null
      (this.charAt(i): @switch) match {
        case '\u0049' => ifMoreAboveOrNull("\u0069\u0307")
        case '\u004A' => ifMoreAboveOrNull("\u006A\u0307")
        case '\u012E' => ifMoreAboveOrNull("\u012F\u0307")
        case '\u00CC' => "\u0069\u0307\u0300"
        case '\u00CD' => "\u0069\u0307\u0301"
        case '\u0128' => "\u0069\u0307\u0303"
        case _        => null
      }
    }

    preprocessed.toLowerCase()
  }

  private def toLowerCaseTurkishAndAzeri(): String = {
    /* Relevant excerpt from SpecialCasing.txt
     *
     * # Turkish and Azeri
     *
     * # I and i-dotless; I-dot and i are case pairs in Turkish and Azeri
     * # The following rules handle those cases.
     *
     * 0130; 0069; 0130; 0130; tr; # LATIN CAPITAL LETTER I WITH DOT ABOVE
     * 0130; 0069; 0130; 0130; az; # LATIN CAPITAL LETTER I WITH DOT ABOVE
     *
     * # When lowercasing, remove dot_above in the sequence I + dot_above, which will turn into i.
     * # This matches the behavior of the canonically equivalent I-dot_above
     *
     * 0307; ; 0307; 0307; tr After_I; # COMBINING DOT ABOVE
     * 0307; ; 0307; 0307; az After_I; # COMBINING DOT ABOVE
     *
     * # When lowercasing, unless an I is before a dot_above, it turns into a dotless i.
     *
     * 0049; 0131; 0049; 0049; tr Not_Before_Dot; # LATIN CAPITAL LETTER I
     * 0049; 0131; 0049; 0049; az Not_Before_Dot; # LATIN CAPITAL LETTER I
     */

    /* Tests whether we are in an `After_I` context.
     * From Table 3.17 in the Unicode standard:
     * - Description: There is an uppercase I before C, and there is no
     *   intervening combining character class 230 (Above) or 0.
     * - Regex, before C: [I]([^\p{ccc=230}\p{ccc=0}])*
     */
    def afterI(i: Int): scala.Boolean = {
      val j = skipCharsWithCombiningClassOtherThanNoneOrAboveBackwards(i)
      j > 0 && charAt(j - 1) == 'I'
    }

    /* Tests whether we are in an `Before_Dot` context.
     * From Table 3.17 in the Unicode standard:
     * - Description: C is followed by combining dot above (U+0307). Any
     *   sequence of characters with a combining class that is neither 0 nor
     *   230 may intervene between the current character and the combining dot
     *   above.
     * - Regex, after C: ([^\p{ccc=230}\p{ccc=0}])*[\u0307]
     */
    def beforeDot(i: Int): scala.Boolean = {
      val j = skipCharsWithCombiningClassOtherThanNoneOrAboveForwards(i + 1)
      j != length() && charAt(j) == '\u0307'
    }

    val preprocessed = replaceCharsAtIndex { i =>
      (this.charAt(i): @switch) match {
        case '\u0130' => "\u0069"
        case '\u0307' => if (afterI(i)) "" else null
        case '\u0049' => if (!beforeDot(i)) "\u0131" else null
        case _        => null
      }
    }

    preprocessed.toLowerCase()
  }

  def toLowerCase(): _String = {
    replaceCharsAtIndex { i =>
      /* Tests whether we are in an `Final_Sigma` context.
       * From Table 3.17 in the Unicode standard:
       * - Description: C is preceded by a sequence consisting of a cased letter and then zero or more case-ignorable characters,
       *     and C is not followed by a sequence consisting of zero or more case-ignorable characters and then a cased letter.
       * - Regex:
       *     before C: \p{cased}(\p{case-ignorable})*
       *     after C:  !((\p{case-ignorable})*\p{cased})
       */
      def isFinalSigma(idx: Int): scala.Boolean = {
        import Character._

        val hasCasedBefore = {
          val j = skipCaseIgnorableCharsBackwards(idx)
          j > 0 && isCased(this.codePointBefore(j))
        }

        val hasCasedAfter = {
          val j = skipCaseIgnorableCharsForwards(idx + 1)
          j < length() && isCased(charAt(j))
        }

        hasCasedBefore && !hasCasedAfter
      }

      /* Relevant excerpt from SpecialCasing.txt
       * # Preserve canonical equivalence for I with dot. Turkic is handled below.
       *
       * 0130; 0069 0307; 0130; 0130; # LATIN CAPITAL LETTER I WITH DOT ABOVE
       * ...
       * # Special case for final form of sigma
       *
       * 03A3; 03C2; 03A3; 03A3; Final_Sigma; # GREEK CAPITAL LETTER SIGMA
       *
       * # Note: the following cases for non-final are already in the UnicodeData.txt file.
       *
       * # 03A3; 03C3; 03A3; 03A3; # GREEK CAPITAL LETTER SIGMA
       * # 03C3; 03C3; 03A3; 03A3; # GREEK SMALL LETTER SIGMA
       * # 03C2; 03C2; 03A3; 03A3; # GREEK SMALL LETTER FINAL SIGMA
       *
       * # Note: the following cases are not included, since they would case-fold in lowercasing
       *
       * # 03C3; 03C2; 03A3; 03A3; Final_Sigma; # GREEK SMALL LETTER SIGMA
       * # 03C2; 03C3; 03A3; 03A3; Not_Final_Sigma; # GREEK SMALL LETTER FINAL SIGMA
       */
      (charAt(i): @switch) match {
        case '\u03A3' if isFinalSigma(i) => "\u03C2"
        case '\u0130'                    => "\u0069\u0307"
        case _                           => null
      }
    }.asInstanceOf[_String]
      .toCase(Character.toLowerCase)
  }

  override def toString(): String = this

  // Ported from Scala.js, commit: ac38a148, dated: 2020-09-25
  def toUpperCase(locale: Locale): String = {
    locale.getLanguage() match {
      case "lt"        => toUpperCaseLithuanian()
      case "tr" | "az" => toUpperCaseTurkishAndAzeri()
      case _           => toUpperCase()
    }
  }

  private def toUpperCaseLithuanian(): String = {
    /* Relevant excerpt from SpecialCasing.txt
     *
     * # Lithuanian
     *
     * # Lithuanian retains the dot in a lowercase i when followed by accents.
     *
     * # Remove DOT ABOVE after "i" with upper or titlecase
     *
     * 0307; 0307; ; ; lt After_Soft_Dotted; # COMBINING DOT ABOVE
     */

    /* Tests whether we are in an `After_Soft_Dotted` context.
     * From Table 3.17 in the Unicode standard:
     * - Description: There is a Soft_Dotted character before C, with no
     *   intervening character of combining class 0 or 230 (Above).
     * - Regex, before C: [\p{Soft_Dotted}]([^\p{ccc=230} \p{ccc=0}])*
     *
     * According to https://unicode.org/Public/13.0.0/ucd/PropList.txt, there
     * are 44 code points with the Soft_Dotted property. However,
     * experimentation on the JVM reveals that the JDK (8 and 14 were tested)
     * only recognizes 8 code points when deciding whether to remove the 0x0307
     * code points. The following script reproduces the list:

for (cp <- 0 to Character.MAX_CODE_POINT) {
  val input = new String(Array(cp, 0x0307, 0x0301), 0, 3)
  val output = input.toUpperCase(new java.util.Locale("lt"))
  if (!output.contains('\u0307'))
    println(cp.toHexString)
}

     */
    def afterSoftDotted(i: Int): scala.Boolean = {
      val j = skipCharsWithCombiningClassOtherThanNoneOrAboveBackwards(i)
      j > 0 && (codePointBefore(j) match {
        case 0x0069 | 0x006a | 0x012f | 0x0268 | 0x0456 | 0x0458 | 0x1e2d |
            0x1ecb =>
          true
        case _ => false
      })
    }

    val preprocessed = replaceCharsAtIndex { i =>
      (this.charAt(i): @switch) match {
        case '\u0307' if afterSoftDotted(i) => ""
        case _                              => null
      }
    }

    preprocessed.toUpperCase()
  }

  private def toUpperCaseTurkishAndAzeri(): String = {
    /* Relevant excerpt from SpecialCasing.txt
     *
     * # Turkish and Azeri
     *
     * # When uppercasing, i turns into a dotted capital I
     *
     * 0069; 0069; 0130; 0130; tr; # LATIN SMALL LETTER I
     * 0069; 0069; 0130; 0130; az; # LATIN SMALL LETTER I
     */

    val preprocessed = replaceCharsAtIndex { i =>
      (this.charAt(i): @switch) match {
        case '\u0069' => "\u0130"
        case _        => null
      }
    }

    preprocessed.toUpperCase()
  }

  def toUpperCase(): _String = {
    replaceCharsAtIndex { i =>
      val c = this.charAt(i)
      if (c < 0x80) null // fast-forward ASCII characters
      else StringSpecialCasing.toUpperCase.get(c)
    }.asInstanceOf[_String]
      .toCase(Character.toUpperCase)
  }

  private def toCase(convert: Int => Int): _String = {
    if (count == 0) return this
    val buf = new java.lang.StringBuilder(count)
    var i = offset
    while (i < offset + count) {
      val high = value(i)
      i += 1
      if (Character.isHighSurrogate(high)) {
        if (i < offset + count) {
          val low = value(i)
          i += 1
          if (Character.isLowSurrogate(low)) {
            val cp = Character.toCodePoint(high, low)
            val cased = convert(cp)
            buf.append(Character.toChars(cased))
          } else {
            buf.append(convert(high).toChar)
            buf.append(convert(low).toChar)
          }
        } else {
          // one high surrogate
          buf.append(convert(high).toChar)
        }
      } else {
        // normal case
        buf.append(convert(high).toChar)
      }
    }
    buf.toString
  }

  /** Replaces special characters in this string (possibly in special contexts)
   *  by dedicated strings.
   *
   *  This method encodes the general pattern of
   *
   *    - `toLowerCaseLithuanian()`
   *    - `toLowerCaseTurkishAndAzeri()`
   *    - `toUpperCaseLithuanian()`
   *    - `toUpperCaseTurkishAndAzeri()`
   *
   *  @param replacementAtIndex
   *    A function from index to `String | Null`, which should return a special
   *    replacement string for the character at the given index, or `null` if
   *    the character at the given index is not special.
   */
  @inline
  private def replaceCharsAtIndex(replacementAtIndex: Int => String): String = {
    var prep: java.lang.StringBuilder = null
    val len = this.length()
    var i = 0
    var startOfSegment = 0

    while (i != len) {
      val replacement = replacementAtIndex(i)
      if (replacement != null) {
        if (prep == null) {
          prep = new java.lang.StringBuilder(len * 2)
        }
        prep.append(this.substring(startOfSegment, i))
        prep.append(replacement)
        startOfSegment = i + 1
      }
      i += 1
    }

    if (startOfSegment == 0)
      this // opt: no character needed replacing, directly return the original string
    else
      prep.append(this.substring(startOfSegment, i)).toString
  }

  private def skipConditionalCharsForwards(
      i: Int
  )(shouldSkip: Int => scala.Boolean): Int = {
    // scalastyle:off return
    val len = length()
    var j = i
    while (j != len) {
      val cp = this.codePointAt(j)
      if (!shouldSkip(cp))
        return j
      j += Character.charCount(cp)
    }
    j
    // scalastyle:on return
  }

  private def skipConditionalCharsBackwards(
      i: Int
  )(shouldSkip: Int => scala.Boolean): Int = {
    // scalastyle:off return
    var j = i
    while (j > 0) {
      val cp = this.codePointBefore(j)
      if (!shouldSkip(cp))
        return j
      j -= Character.charCount(cp)
    }
    0
    // scalastyle:on return
  }

  private def skipCharsWithCombiningClassOtherThanNoneOrAboveForwards(
      i: Int
  ): Int = {
    skipConditionalCharsForwards(i) { cp =>
      import Character._
      combiningClassNoneOrAboveOrOther(cp) == CombiningClassIsOther
    }
  }

  private def skipCharsWithCombiningClassOtherThanNoneOrAboveBackwards(
      i: Int
  ): Int = {
    skipConditionalCharsBackwards(i) { cp =>
      import Character._
      combiningClassNoneOrAboveOrOther(cp) == CombiningClassIsOther
    }
  }

  private def skipCaseIgnorableCharsForwards(i: Int): Int = {
    skipConditionalCharsForwards(i) { cp =>
      import Character._
      isCaseIgnorable(cp)
    }
  }

  private def skipCaseIgnorableCharsBackwards(i: Int): Int = {
    skipConditionalCharsBackwards(i) { cp =>
      import Character._
      isCaseIgnorable(cp)
    }
  }

  def trim(): _String = {
    var start = offset
    val last = offset + count - 1
    var end = last

    while ((start <= end) && (value(start) <= ' ')) {
      start += 1
    }

    while ((end >= start) && (value(end) <= ' ')) {
      end -= 1
    }

    if (start == offset && end == last) {
      this
    } else {
      new _String(start, end - start + 1, value)
    }
  }

  def contentEquals(sb: StringBuffer): scala.Boolean = {
    val size = sb.length()
    if (count != size) {
      false
    } else {
      regionMatches(0, new _String(0, size, sb.getValue()), 0, size)
    }
  }

  def contentEquals(cs: CharSequence): scala.Boolean = {
    val len = cs.length()
    if (len != count) {
      false
    } else if (len == 0 && count == 0) {
      true
    } else {
      regionMatches(0, _String.valueOf(cs.toString), 0, len)
    }
  }

  def matches(expr: _String): scala.Boolean =
    Pattern.matches(expr, this)

  def replaceAll(expr: _String, substitute: _String): _String =
    Pattern.compile(expr).matcher(this).replaceAll(substitute)

  def replaceFirst(expr: _String, substitute: _String): _String =
    Pattern.compile(expr).matcher(this).replaceFirst(substitute)

  def fastSplit(ch: Char, max: Int): Array[String] = {
    var separatorCount = 0
    var begin = 0
    var end = 0
    while (separatorCount + 1 != max && {
          end = indexOf(ch, begin); end != -1
        }) {
      separatorCount += 1
      begin = end + 1
    }
    val lastPartEnd = if (max == 0 && begin == count) {
      if (separatorCount == count) {
        return Array.empty[String]
      }
      while ({
        begin -= 1
        charAt(begin - 1) == ch
      }) ()
      separatorCount -= count - begin
      begin
    } else {
      count
    }

    val result = new Array[String](separatorCount + 1)
    begin = 0
    var i = 0
    while (i < separatorCount) {
      end = indexOf(ch, begin)
      result(i) = substring(begin, end);
      begin = end + 1
      i += 1
    }
    result(separatorCount) = substring(begin, lastPartEnd)
    result
  }

  private[this] final val REGEX_METACHARACTERS = ".$()[{^?*+\\"
  @inline private def isRegexMeta(c: Char) =
    REGEX_METACHARACTERS.indexOf(c) >= 0

  def split(expr: _String): Array[String] =
    split(expr, 0)

  def split(expr: _String, max: Int): Array[String] =
    if (isEmpty()) {
      Array("")
    } else {
      expr.length() match {
        case 1 if !isRegexMeta(expr.charAt(0)) => fastSplit(expr.charAt(0), max)
        case 2 if expr.charAt(0) == '\\' && isRegexMeta(expr.charAt(1)) =>
          fastSplit(expr.charAt(1), max)
        case _ => Pattern.compile(expr).split(this, max)
      }
    }

  def subSequence(start: Int, end: Int): CharSequence =
    substring(start, end)

  def codePointAt(index: Int): Int =
    if (index < 0 || index >= count) {
      throw new StringIndexOutOfBoundsException()
    } else {
      Character.codePointAt(value, index + offset, offset + count)
    }

  def codePointBefore(index: Int): Int =
    if (index < 1 || index > count) {
      throw new StringIndexOutOfBoundsException()
    } else {
      Character.codePointBefore(value, index + offset)
    }

  def codePointCount(beginIndex: Int, endIndex: Int): Int =
    if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
      throw new StringIndexOutOfBoundsException()
    } else {
      Character
        .codePointCount(value, beginIndex + offset, endIndex - beginIndex)
    }

  def contains(cs: CharSequence): scala.Boolean =
    indexOf(_String.valueOf(cs.toString)) >= 0

  def offsetByCodePoints(index: Int, codePointOffset: Int): Int = {
    val s = index + offset
    val r =
      Character.offsetByCodePoints(value, offset, count, s, codePointOffset)
    r - offset
  }

  def stripLeading(): String = {
    val len = length()
    var idx = 0
    while (idx < len && Character.isWhitespace(charAt(idx)))
      idx += 1
    substring(idx)
  }

  def stripTrailing(): String = {
    val len = length()
    var idx = len - 1
    while (idx >= 0 && Character.isWhitespace(charAt(idx)))
      idx -= 1
    substring(0, idx + 1)
  }

  def strip(): String = {
    val len = length()
    var leading = 0
    while (leading < len && Character.isWhitespace(charAt(leading)))
      leading += 1
    if (leading == len) {
      ""
    } else {
      var trailing = len
      while (Character.isWhitespace(charAt(trailing - 1)))
        trailing -= 1
      if (leading == 0 && trailing == len) thisString
      else substring(leading, trailing)
    }
  }

  def isBlank(): scala.Boolean = {
    val len = length()
    var start = 0
    while (start != len && Character.isWhitespace(charAt(start)))
      start += 1
    start == len
  }

  private def splitLines(): java.util.LinkedList[String] = {
    // Scala.js uses js.Array here
    val xs = new java.util.LinkedList[String]()
    val len = length()
    var idx = 0
    var last = 0

    while (idx < len) {
      val c = charAt(idx)
      if (c == '\n' || c == '\r') {
        xs.add(substring(last, idx))
        if (c == '\r' && idx + 1 < len && charAt(idx + 1) == '\n')
          idx += 1
        last = idx + 1
      }
      idx += 1
    }
    // make sure we add the last segment, but not the last new line
    if (last != len)
      xs.add(substring(last))
    xs
  }

  def indent(n: Int): String = {

    def forEachLn(f: String => String): String = {
      val out = new StringBuilder("")
      val xs = splitLines()
      var line: String = null
      while ({
        line = xs.poll()
        line != null
      }) {
        out.append(f(line))
        out.append("\n")
      }
      out.toString()
    }

    if (n < 0) {
      forEachLn { l =>
        // n is negative here
        var idx = 0
        val lim = if (l.length() <= -n) l.length() else -n
        while (idx < lim && Character.isWhitespace(l.charAt(idx)))
          idx += 1
        l.substring(idx)
      }
    } else {
      val padding = " ".asInstanceOf[_String].repeat(n)
      forEachLn(padding + _)
    }
  }

  def stripIndent(): String = {
    if (isEmpty()) {
      ""
    } else {
      import Character.{isWhitespace => isWS}
      // splitLines discards the last NL if it's empty so we identify it here first
      val trailingNL = charAt(length() - 1) match {
        // this also covers the \r\n case via the last \n
        case '\r' | '\n' => true
        case _           => false
      }

      var minLeading = Int.MaxValue
      val xs = splitLines()
      val xi = xs.listIterator(0)
      while (xi.hasNext()) {
        val l = xi.next()
        // count the last line even if blank
        if (!xi.hasNext() || !l.asInstanceOf[_String].isBlank()) {
          var idx = 0
          while (idx < l.length() && isWS(l.charAt(idx)))
            idx += 1
          if (idx < minLeading)
            minLeading = idx
        }
      }
      // if trailingNL, then the last line is zero width
      if (trailingNL || minLeading == Int.MaxValue)
        minLeading = 0

      val out = new StringBuilder()
      var line: String = null
      while ({
        line = xs.poll()
        line != null
      }) {
        if (!line.asInstanceOf[_String].isBlank()) {
          // we strip the computed leading WS and also any *trailing* WS
          out.append(
            line.substring(minLeading).asInstanceOf[_String].stripTrailing()
          )
        }
        // different from indent, we don't add an LF at the end unless there's already one
        if (xs.peek() != null)
          out.append("\n")
      }
      if (trailingNL)
        out.append("\n")
      out.toString
    }
  }

  def translateEscapes(): String = {
    def isOctalDigit(c: Char): scala.Boolean = c >= '0' && c <= '7'
    def isValidIndex(n: Int): scala.Boolean = n < length()
    var i = 0
    val result = new StringBuilder()
    while (i < length()) {
      if (charAt(i) == '\\') {
        if (isValidIndex(i + 1)) {
          charAt(i + 1) match {
            // <line-terminator>, so CR(\r), LF(\n), or CRLF(\r\n)
            case '\r' if isValidIndex(i + 2) && charAt(i + 2) == '\n' =>
              i += 1 // skip \r and \n and discard, so 2+1 chars
            case '\r' | '\n' => // skip and discard
            // normal one char escapes
            case 'b'  => result.append("\b")
            case 't'  => result.append("\t")
            case 'n'  => result.append("\n")
            case 'f'  => result.append("\f")
            case 'r'  => result.append("\r")
            case 's'  => result.append(" ")
            case '"'  => result.append("\"")
            case '\'' => result.append("\'")
            case '\\' => result.append("\\")

            // we're parsing octal now, as per JLS-3, we got three cases:
            // 1) [0-3][0-7][0-7]
            case a @ ('0' | '1' | '2' | '3')
                if isValidIndex(i + 3) && isOctalDigit(
                  charAt(i + 2)
                ) && isOctalDigit(charAt(i + 3)) =>
              val codePoint =
                ((a - '0') * 64) + ((charAt(i + 2) - '0') * 8) + (charAt(
                  i + 3
                ) - '0')
              result.append(codePoint.toChar)
              i += 2 // skip two other numbers, so 2+2 chars
            // 2) [0-7][0-7]
            case a
                if isOctalDigit(a) && isValidIndex(i + 2) && isOctalDigit(
                  charAt(i + 2)
                ) =>
              val codePoint = ((a - '0') * 8) + (charAt(i + 2) - '0')
              result.append(codePoint.toChar)
              i += 1 // skip one other number, so 2+1 chars
            // 3) [0-7]
            case a if isOctalDigit(a) =>
              val codePoint = a - '0'
              result.append(codePoint.toChar)
            // bad escape otherwise, this catches everything else including the Unicode ones
            case bad =>
              throw new IllegalArgumentException(
                "Illegal escape: `\\" + bad + "`"
              )
          }
          // skip ahead 2 chars (\ and the escape char) at minimum, cases above can add more if needed
          i += 2
        } else {
          throw new IllegalArgumentException(
            "Illegal escape: `\\(end-of-string)`"
          )
        }
      } else {
        result.append(charAt(i))
        i += 1
      }
    }
    result.toString()
  }

  // Java 15 and above.
  def transform[R](f: java.util.function.Function[String, R]): R =
    f.apply(thisString)
}

object _String {
  final val CASE_INSENSITIVE_ORDER: Comparator[_String] =
    new CaseInsensitiveComparator()
  private final val ascii = {
    val ascii = new Array[Char](128)
    var i = 0
    while (i < ascii.length) {
      ascii(i) = i.toChar
      i += 1
    }
    ascii
  }

  private class CaseInsensitiveComparator
      extends Comparator[_String]
      with Serializable {
    def compare(o1: _String, o2: _String): Int =
      o1.compareToIgnoreCase(o2)
  }

  def copyValueOf(data: Array[Char], start: Int, length: Int): _String =
    new _String(data, start, length)

  def copyValueOf(data: Array[Char]): _String =
    new _String(data, 0, data.length)

  def format(fmt: _String, args: Array[AnyRef]): _String =
    new Formatter().format(fmt, args).toString

  def format(loc: Locale, fmt: _String, args: Array[AnyRef]): _String =
    new Formatter(loc).format(fmt, args).toString()

  def join(delimiter: CharSequence, elements: Array[CharSequence]): String = {
    val sj = new StringJoiner(delimiter)

    for (j <- 0 until elements.length)
      sj.add(elements(j))

    sj.toString()
  }

  def join(
      delimiter: CharSequence,
      elements: Iterable[CharSequence]
  ): String = {
    elements.scalaOps
      .foldLeft(new StringJoiner(delimiter))((j, e) => j.add(e))
      .toString()
  }

  def valueOf(data: Array[Char]): _String = new _String(data)

  def valueOf(data: Array[Char], start: Int, length: Int): _String =
    new _String(data, start, length)

  def valueOf(value: Char): _String = {
    val s =
      if (value < 128) new _String(value, 1, ascii)
      else new _String(0, 1, Array(value))
    s.cachedHashCode = value
    s
  }

  def valueOf(value: scala.Double): _String = Double.toString(value)

  def valueOf(value: scala.Float): _String = Float.toString(value)

  def valueOf(value: scala.Int): _String = Integer.toString(value)

  def valueOf(value: scala.Long): _String = Long.toString(value)

  def valueOf(value: scala.Boolean): _String = Boolean.toString(value)

  def valueOf(value: AnyRef): _String =
    if (value != null) value.toString else "null"

  import scala.language.implicitConversions
  @inline private[lang] implicit def _string2string(s: _String): String =
    s.asInstanceOf[String]
  @inline private[lang] implicit def string2_string(s: String): _String =
    s.asInstanceOf[_String]
}
