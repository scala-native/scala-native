package java.lang

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.string.memcmp
import scalanative.runtime.CharArray
import java.io.Serializable
import java.util._
import java.util.regex._
import java.nio._
import java.nio.charset._
import java.util.Objects
import scala.annotation.{switch, tailrec}

final class _String()
    extends Serializable
    with Comparable[_String]
    with CharSequence {
  protected[_String] var value: Array[Char]  = new Array[Char](0)
  protected[_String] var offset: Int         = 0
  protected[_String] var count: Int          = 0
  protected[_String] var cachedHashCode: Int = _

  def this(data: Array[scala.Byte], high: Int, start: Int, length: Int) = {
    this()
    if (length <= data.length - start && start >= 0 && 0 <= length) {
      offset = 0
      value = {
        val value = new Array[Char](length)
        var i     = 0
        while (i < length) {
          value(i) = ((high & 0xff) << 8 | (data(start + i) & 0xff)).toChar
          i += 1
        }
        value
      }
    } else {
      throw new StringIndexOutOfBoundsException()
    }
  }

  def this(data: Array[scala.Byte],
           start: Int,
           length: Int,
           encoding: Charset) = {
    this()
    offset = 0
    val charBuffer = encoding.decode(ByteBuffer.wrap(data, start, length))
    value = charBuffer.array()
    count = charBuffer.length()
  }

  def this(data: Array[scala.Byte],
           start: Int,
           length: Int,
           encoding: _String) = {
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

  def this(start: Int, length: Int, data: Array[Char]) = {
    this()
    value = data
    offset = start
    count = length
  }

  def this(string: _String) = {
    this()
    value = string.value
    offset = string.offset
    count = string.length()
  }

  def this(sb: StringBuffer) = {
    this()
    offset = 0
    value = sb.getValue()
    count = sb.length()
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
    offset = 0
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
                .asInstanceOf[CharArray]
                .at(offset)
                .asInstanceOf[Ptr[scala.Byte]]
            val data2 =
              s.value
                .asInstanceOf[CharArray]
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
  def getBytes(start: Int,
               _end: Int,
               data: Array[scala.Byte],
               _index: Int): Unit = {
    var end = _end
    if (0 <= start && start <= end && end <= count) {
      end += offset

      try {
        var index = _index
        var i     = offset + start
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
    val bytes  = new Array[scala.Byte](buffer.limit())
    buffer.get(bytes)
    bytes
  }

  def getBytes(encoding: Charset): Array[scala.Byte] = {
    val buffer = encoding.encode(CharBuffer.wrap(value, offset, count))
    val bytes  = new Array[scala.Byte](buffer.limit())
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
        val data = value.asInstanceOf[CharArray].at(offset)
        var hash = 0
        var i    = 0
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
      val target    = subString.value
      val subOffset = subString.offset
      val firstChar = target(subOffset)
      val end       = subOffset + subCount
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
    var start    = _start
    val subCount = subString.count
    if (subCount <= count && start >= 0) {
      if (subCount > 0) {
        if (start > count - subCount) {
          start = count - subCount
        }
        val target    = subString.value
        val subOffset = subString.offset
        val firstChar = target(subOffset)
        val end       = subOffset + subCount
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

  def regionMatches(thisStart: Int,
                    string: _String,
                    start: Int,
                    length: Int): scala.Boolean = {
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

  def regionMatches(ignoreCase: scala.Boolean,
                    _thisStart: Int,
                    string: _String,
                    _start: Int,
                    length: Int): scala.Boolean = {
    var thisStart = _thisStart
    var start     = _start
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
        val end    = thisStart + length
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

  def replace(oldChar: Char, newChar: Char): _String = {
    var index = indexOf(oldChar, 0)
    if (index == -1) {
      this
    } else {
      val buffer = new Array[Char](count)
      System.arraycopy(value, offset, buffer, 0, count)

      do {
        buffer(index) = newChar
        index += 1
        index = indexOf(oldChar, index)
      } while (index != -1)

      new _String(0, count, buffer)
    }
  }

  def replace(target: CharSequence, replacement: CharSequence): _String = {
    if (target == null) {
      throw new NullPointerException("target should not be null")
    } else if (replacement == null) {
      throw new NullPointerException("replacement should not be null")
    } else {
      val ts    = target.toString
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
      val tl     = target.length()
      var tail   = 0
      do {
        buffer.append(value, offset + tail, index - tail)
        buffer.append(rs)
        tail = index + tl
        index = indexOf(ts, tail)
      } while (index != -1)
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
      (this.charAt(i): @switch) match {
        case '\u0049' if moreAbove(i) => "\u0069\u0307"
        case '\u004A' if moreAbove(i) => "\u006A\u0307"
        case '\u012E' if moreAbove(i) => "\u012F\u0307"
        case '\u00CC'                 => "\u0069\u0307\u0300"
        case '\u00CD'                 => "\u0069\u0307\u0301"
        case '\u0128'                 => "\u0069\u0307\u0303"
        case _                        => null
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
        case '\u0130'                  => "\u0069"
        case '\u0307' if afterI(i)     => ""
        case '\u0049' if !beforeDot(i) => "\u0131"
        case _                         => null
      }
    }

    preprocessed.toLowerCase()
  }

  @inline def toLowerCase(): _String = {
    replaceCharsAtIndex { i =>
      // Test if given character is last cased letter within given word context, that means
      // given char at index has at least 1 preceding cased letter within word
      // and is not followed by any cased letter
      def isFinalCased(idx: Int): Boolean = {

        /* Character is cased when its type matches lowercase, uppercase or title-case type.
         * Alternatively it can be non standard cased character, eq. modifier or circled letter or roman number
         */
        def isCased(c: Char): scala.Boolean = {
          val charType = Character.getType(c)
          charType == Character.LOWERCASE_LETTER ||
          charType == Character.UPPERCASE_LETTER ||
          charType == Character.TITLECASE_LETTER ||
          ((c >= 0x02B0) && (c <= 0x02B8)) || // Modifier small letter h..y
          ((c >= 0x02C0) && (c <= 0x02C1)) || // Modifier letter or reversed letter glottal stop
          ((c >= 0x02E0) && (c <= 0x02E4)) || // Modifier small or reversed small letter gamma
          c == '\u0345' ||                    // Combining greek ypogegrammeni
          c == '\u037A' ||                    // Greek ypogegrammeni
          ((c >= 0x1D2C) && (c <= 0x1D61)) || // Modifier letter capital A to CHI
          ((c >= 0x2160) && (c <= 0x217F)) || // Roman capital or small numeral one to one thousand
          ((c >= 0x24B6) && (c <= 0x24E9))    // Circled latin capital or small letters A to Z
        }

        @tailrec
        def followsCased(i: Int): scala.Boolean = {
          if (i < offset) false // Not found non cased in whole string
          else {
            val c = charAt(i)
            // Outside word boundary and not found cased
            if (c.isWhitespace) false
            else if (isCased(c)) true
            else followsCased(i - Character.charCount(c))
          }
        }

        @tailrec
        def precedesOnlyNonCased(i: Int): scala.Boolean = {
          if (i >= offset + count) true // At string boundary and no cased found
          else {
            val c = charAt(i)
            if (c.isWhitespace) true // At word boundary and no cased found
            else if (isCased(c)) false
            else precedesOnlyNonCased(i + Character.charCount(c))
          }
        }

        followsCased(idx - 1) && precedesOnlyNonCased(idx + 1)
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
        case '\u03A3' if isFinalCased(i) => "\u03C2"
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

  @inline
  def toUpperCase(): _String = {
    /* Generated based on unconditional mappings in [SpecialCasing.txt](https://unicode.org/Public/UNIDATA/SpecialCasing.txt)
     * Relevant excerpt from SpecialCasing.txt
     * # The German es-zed is special--the normal mapping is to SS.
     * # Note: the titlecase should never occur in practice. It is equal to titlecase(uppercase(<es-zed>))
     *
     * 00DF; 00DF; 0053 0073; 0053 0053; # LATIN SMALL LETTER SHARP S
     *
     * # Preserve canonical equivalence for I with dot. Turkic is handled below.
     *
     * 0130; 0069 0307; 0130; 0130; # LATIN CAPITAL LETTER I WITH DOT ABOVE
     *
     * # Ligatures
     *
     * FB00; FB00; 0046 0066; 0046 0046; # LATIN SMALL LIGATURE FF
     * FB01; FB01; 0046 0069; 0046 0049; # LATIN SMALL LIGATURE FI
     * FB02; FB02; 0046 006C; 0046 004C; # LATIN SMALL LIGATURE FL
     * FB03; FB03; 0046 0066 0069; 0046 0046 0049; # LATIN SMALL LIGATURE FFI
     * FB04; FB04; 0046 0066 006C; 0046 0046 004C; # LATIN SMALL LIGATURE FFL
     * FB05; FB05; 0053 0074; 0053 0054; # LATIN SMALL LIGATURE LONG S T
     * FB06; FB06; 0053 0074; 0053 0054; # LATIN SMALL LIGATURE ST
     *
     * 0587; 0587; 0535 0582; 0535 0552; # ARMENIAN SMALL LIGATURE ECH YIWN
     * FB13; FB13; 0544 0576; 0544 0546; # ARMENIAN SMALL LIGATURE MEN NOW
     * FB14; FB14; 0544 0565; 0544 0535; # ARMENIAN SMALL LIGATURE MEN ECH
     * FB15; FB15; 0544 056B; 0544 053B; # ARMENIAN SMALL LIGATURE MEN INI
     * FB16; FB16; 054E 0576; 054E 0546; # ARMENIAN SMALL LIGATURE VEW NOW
     * FB17; FB17; 0544 056D; 0544 053D; # ARMENIAN SMALL LIGATURE MEN XEH
     *
     * # No corresponding uppercase precomposed character
     *
     * 0149; 0149; 02BC 004E; 02BC 004E; # LATIN SMALL LETTER N PRECEDED BY APOSTROPHE
     * 0390; 0390; 0399 0308 0301; 0399 0308 0301; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
     * 03B0; 03B0; 03A5 0308 0301; 03A5 0308 0301; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
     * 01F0; 01F0; 004A 030C; 004A 030C; # LATIN SMALL LETTER J WITH CARON
     * 1E96; 1E96; 0048 0331; 0048 0331; # LATIN SMALL LETTER H WITH LINE BELOW
     * 1E97; 1E97; 0054 0308; 0054 0308; # LATIN SMALL LETTER T WITH DIAERESIS
     * 1E98; 1E98; 0057 030A; 0057 030A; # LATIN SMALL LETTER W WITH RING ABOVE
     * 1E99; 1E99; 0059 030A; 0059 030A; # LATIN SMALL LETTER Y WITH RING ABOVE
     * 1E9A; 1E9A; 0041 02BE; 0041 02BE; # LATIN SMALL LETTER A WITH RIGHT HALF RING
     * 1F50; 1F50; 03A5 0313; 03A5 0313; # GREEK SMALL LETTER UPSILON WITH PSILI
     * 1F52; 1F52; 03A5 0313 0300; 03A5 0313 0300; # GREEK SMALL LETTER UPSILON WITH PSILI AND VARIA
     * 1F54; 1F54; 03A5 0313 0301; 03A5 0313 0301; # GREEK SMALL LETTER UPSILON WITH PSILI AND OXIA
     * 1F56; 1F56; 03A5 0313 0342; 03A5 0313 0342; # GREEK SMALL LETTER UPSILON WITH PSILI AND PERISPOMENI
     * 1FB6; 1FB6; 0391 0342; 0391 0342; # GREEK SMALL LETTER ALPHA WITH PERISPOMENI
     * 1FC6; 1FC6; 0397 0342; 0397 0342; # GREEK SMALL LETTER ETA WITH PERISPOMENI
     * 1FD2; 1FD2; 0399 0308 0300; 0399 0308 0300; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND VARIA
     * 1FD3; 1FD3; 0399 0308 0301; 0399 0308 0301; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND OXIA
     * 1FD6; 1FD6; 0399 0342; 0399 0342; # GREEK SMALL LETTER IOTA WITH PERISPOMENI
     * 1FD7; 1FD7; 0399 0308 0342; 0399 0308 0342; # GREEK SMALL LETTER IOTA WITH DIALYTIKA AND PERISPOMENI
     * 1FE2; 1FE2; 03A5 0308 0300; 03A5 0308 0300; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND VARIA
     * 1FE3; 1FE3; 03A5 0308 0301; 03A5 0308 0301; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND OXIA
     * 1FE4; 1FE4; 03A1 0313; 03A1 0313; # GREEK SMALL LETTER RHO WITH PSILI
     * 1FE6; 1FE6; 03A5 0342; 03A5 0342; # GREEK SMALL LETTER UPSILON WITH PERISPOMENI
     * 1FE7; 1FE7; 03A5 0308 0342; 03A5 0308 0342; # GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND PERISPOMENI
     * 1FF6; 1FF6; 03A9 0342; 03A9 0342; # GREEK SMALL LETTER OMEGA WITH PERISPOMENI
     *
     * # IMPORTANT-when iota-subscript (0345) is uppercased or titlecased,
     * #  the result will be incorrect unless the iota-subscript is moved to the end
     * #  of any sequence of combining marks. Otherwise, the accents will go on the capital iota.
     * #  This process can be achieved by first transforming the text to NFC before casing.
     * #  E.g. <alpha><iota_subscript><acute> is uppercased to <ALPHA><acute><IOTA>
     *
     * # The following cases are already in the UnicodeData.txt file, so are only commented here.
     *
     * # 0345; 0345; 0399; 0399; # COMBINING GREEK YPOGEGRAMMENI
     *
     * # All letters with YPOGEGRAMMENI (iota-subscript) or PROSGEGRAMMENI (iota adscript)
     * # have special uppercases.
     * # Note: characters with PROSGEGRAMMENI are actually titlecase, not uppercase!
     *
     * 1F80; 1F80; 1F88; 1F08 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND YPOGEGRAMMENI
     * 1F81; 1F81; 1F89; 1F09 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND YPOGEGRAMMENI
     * 1F82; 1F82; 1F8A; 1F0A 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND VARIA AND YPOGEGRAMMENI
     * 1F83; 1F83; 1F8B; 1F0B 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND VARIA AND YPOGEGRAMMENI
     * 1F84; 1F84; 1F8C; 1F0C 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND OXIA AND YPOGEGRAMMENI
     * 1F85; 1F85; 1F8D; 1F0D 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND OXIA AND YPOGEGRAMMENI
     * 1F86; 1F86; 1F8E; 1F0E 0399; # GREEK SMALL LETTER ALPHA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
     * 1F87; 1F87; 1F8F; 1F0F 0399; # GREEK SMALL LETTER ALPHA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
     * 1F88; 1F80; 1F88; 1F08 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND PROSGEGRAMMENI
     * 1F89; 1F81; 1F89; 1F09 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND PROSGEGRAMMENI
     * 1F8A; 1F82; 1F8A; 1F0A 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA AND PROSGEGRAMMENI
     * 1F8B; 1F83; 1F8B; 1F0B 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA AND PROSGEGRAMMENI
     * 1F8C; 1F84; 1F8C; 1F0C 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA AND PROSGEGRAMMENI
     * 1F8D; 1F85; 1F8D; 1F0D 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA AND PROSGEGRAMMENI
     * 1F8E; 1F86; 1F8E; 1F0E 0399; # GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
     * 1F8F; 1F87; 1F8F; 1F0F 0399; # GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
     * 1F90; 1F90; 1F98; 1F28 0399; # GREEK SMALL LETTER ETA WITH PSILI AND YPOGEGRAMMENI
     * 1F91; 1F91; 1F99; 1F29 0399; # GREEK SMALL LETTER ETA WITH DASIA AND YPOGEGRAMMENI
     * 1F92; 1F92; 1F9A; 1F2A 0399; # GREEK SMALL LETTER ETA WITH PSILI AND VARIA AND YPOGEGRAMMENI
     * 1F93; 1F93; 1F9B; 1F2B 0399; # GREEK SMALL LETTER ETA WITH DASIA AND VARIA AND YPOGEGRAMMENI
     * 1F94; 1F94; 1F9C; 1F2C 0399; # GREEK SMALL LETTER ETA WITH PSILI AND OXIA AND YPOGEGRAMMENI
     * 1F95; 1F95; 1F9D; 1F2D 0399; # GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI
     * 1F96; 1F96; 1F9E; 1F2E 0399; # GREEK SMALL LETTER ETA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
     * 1F97; 1F97; 1F9F; 1F2F 0399; # GREEK SMALL LETTER ETA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
     * 1F98; 1F90; 1F98; 1F28 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND PROSGEGRAMMENI
     * 1F99; 1F91; 1F99; 1F29 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND PROSGEGRAMMENI
     * 1F9A; 1F92; 1F9A; 1F2A 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA AND PROSGEGRAMMENI
     * 1F9B; 1F93; 1F9B; 1F2B 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA AND PROSGEGRAMMENI
     * 1F9C; 1F94; 1F9C; 1F2C 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA AND PROSGEGRAMMENI
     * 1F9D; 1F95; 1F9D; 1F2D 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
     * 1F9E; 1F96; 1F9E; 1F2E 0399; # GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
     * 1F9F; 1F97; 1F9F; 1F2F 0399; # GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
     * 1FA0; 1FA0; 1FA8; 1F68 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND YPOGEGRAMMENI
     * 1FA1; 1FA1; 1FA9; 1F69 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND YPOGEGRAMMENI
     * 1FA2; 1FA2; 1FAA; 1F6A 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND VARIA AND YPOGEGRAMMENI
     * 1FA3; 1FA3; 1FAB; 1F6B 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND VARIA AND YPOGEGRAMMENI
     * 1FA4; 1FA4; 1FAC; 1F6C 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND OXIA AND YPOGEGRAMMENI
     * 1FA5; 1FA5; 1FAD; 1F6D 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND OXIA AND YPOGEGRAMMENI
     * 1FA6; 1FA6; 1FAE; 1F6E 0399; # GREEK SMALL LETTER OMEGA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
     * 1FA7; 1FA7; 1FAF; 1F6F 0399; # GREEK SMALL LETTER OMEGA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
     * 1FA8; 1FA0; 1FA8; 1F68 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND PROSGEGRAMMENI
     * 1FA9; 1FA1; 1FA9; 1F69 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND PROSGEGRAMMENI
     * 1FAA; 1FA2; 1FAA; 1F6A 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA AND PROSGEGRAMMENI
     * 1FAB; 1FA3; 1FAB; 1F6B 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA AND PROSGEGRAMMENI
     * 1FAC; 1FA4; 1FAC; 1F6C 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA AND PROSGEGRAMMENI
     * 1FAD; 1FA5; 1FAD; 1F6D 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA AND PROSGEGRAMMENI
     * 1FAE; 1FA6; 1FAE; 1F6E 0399; # GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
     * 1FAF; 1FA7; 1FAF; 1F6F 0399; # GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
     * 1FB3; 1FB3; 1FBC; 0391 0399; # GREEK SMALL LETTER ALPHA WITH YPOGEGRAMMENI
     * 1FBC; 1FB3; 1FBC; 0391 0399; # GREEK CAPITAL LETTER ALPHA WITH PROSGEGRAMMENI
     * 1FC3; 1FC3; 1FCC; 0397 0399; # GREEK SMALL LETTER ETA WITH YPOGEGRAMMENI
     * 1FCC; 1FC3; 1FCC; 0397 0399; # GREEK CAPITAL LETTER ETA WITH PROSGEGRAMMENI
     * 1FF3; 1FF3; 1FFC; 03A9 0399; # GREEK SMALL LETTER OMEGA WITH YPOGEGRAMMENI
     * 1FFC; 1FF3; 1FFC; 03A9 0399; # GREEK CAPITAL LETTER OMEGA WITH PROSGEGRAMMENI
     *
     * # Some characters with YPOGEGRAMMENI also have no corresponding titlecases
     *
     * 1FB2; 1FB2; 1FBA 0345; 1FBA 0399; # GREEK SMALL LETTER ALPHA WITH VARIA AND YPOGEGRAMMENI
     * 1FB4; 1FB4; 0386 0345; 0386 0399; # GREEK SMALL LETTER ALPHA WITH OXIA AND YPOGEGRAMMENI
     * 1FC2; 1FC2; 1FCA 0345; 1FCA 0399; # GREEK SMALL LETTER ETA WITH VARIA AND YPOGEGRAMMENI
     * 1FC4; 1FC4; 0389 0345; 0389 0399; # GREEK SMALL LETTER ETA WITH OXIA AND YPOGEGRAMMENI
     * 1FF2; 1FF2; 1FFA 0345; 1FFA 0399; # GREEK SMALL LETTER OMEGA WITH VARIA AND YPOGEGRAMMENI
     * 1FF4; 1FF4; 038F 0345; 038F 0399; # GREEK SMALL LETTER OMEGA WITH OXIA AND YPOGEGRAMMENI
     *
     * 1FB7; 1FB7; 0391 0342 0345; 0391 0342 0399; # GREEK SMALL LETTER ALPHA WITH PERISPOMENI AND YPOGEGRAMMENI
     * 1FC7; 1FC7; 0397 0342 0345; 0397 0342 0399; # GREEK SMALL LETTER ETA WITH PERISPOMENI AND YPOGEGRAMMENI
     * 1FF7; 1FF7; 03A9 0342 0345; 03A9 0342 0399; # GREEK SMALL LETTER OMEGA WITH PERISPOMENI AND YPOGEGRAMMENI
     */
    replaceCharsAtIndex { i =>
      val c = this.charAt(i)
      if (c < 0x80) null // fast-forward ASCII characters
      else if (c >= 0x1E96 && c <= 0x1E9A) (c: @switch) match {
        case '\u1E96' => "\u0048\u0331" // ẖ to H̱
        case '\u1E97' => "\u0054\u0308" // ẗ to T̈
        case '\u1E98' => "\u0057\u030A" // ẘ to W̊
        case '\u1E99' => "\u0059\u030A" // ẙ to Y̊
        case '\u1E9A' => "\u0041\u02BE" // ẚ to Aʾ
      }
      else if (c >= 0x1F80 && c <= 0x1FAF) (c: @switch) match {
        case '\u1F80' => "\u1F08\u0399" // ᾀ to ἈΙ
        case '\u1F81' => "\u1F09\u0399" // ᾁ to ἉΙ
        case '\u1F82' => "\u1F0A\u0399" // ᾂ to ἊΙ
        case '\u1F83' => "\u1F0B\u0399" // ᾃ to ἋΙ
        case '\u1F84' => "\u1F0C\u0399" // ᾄ to ἌΙ
        case '\u1F85' => "\u1F0D\u0399" // ᾅ to ἍΙ
        case '\u1F86' => "\u1F0E\u0399" // ᾆ to ἎΙ
        case '\u1F87' => "\u1F0F\u0399" // ᾇ to ἏΙ
        case '\u1F88' => "\u1F08\u0399" // ᾈ to ἈΙ
        case '\u1F89' => "\u1F09\u0399" // ᾉ to ἉΙ
        case '\u1F8A' => "\u1F0A\u0399" // ᾊ to ἊΙ
        case '\u1F8B' => "\u1F0B\u0399" // ᾋ to ἋΙ
        case '\u1F8C' => "\u1F0C\u0399" // ᾌ to ἌΙ
        case '\u1F8D' => "\u1F0D\u0399" // ᾍ to ἍΙ
        case '\u1F8E' => "\u1F0E\u0399" // ᾎ to ἎΙ
        case '\u1F8F' => "\u1F0F\u0399" // ᾏ to ἏΙ
        case '\u1F90' => "\u1F28\u0399" // ᾐ to ἨΙ
        case '\u1F91' => "\u1F29\u0399" // ᾑ to ἩΙ
        case '\u1F92' => "\u1F2A\u0399" // ᾒ to ἪΙ
        case '\u1F93' => "\u1F2B\u0399" // ᾓ to ἫΙ
        case '\u1F94' => "\u1F2C\u0399" // ᾔ to ἬΙ
        case '\u1F95' => "\u1F2D\u0399" // ᾕ to ἭΙ
        case '\u1F96' => "\u1F2E\u0399" // ᾖ to ἮΙ
        case '\u1F97' => "\u1F2F\u0399" // ᾗ to ἯΙ
        case '\u1F98' => "\u1F28\u0399" // ᾘ to ἨΙ
        case '\u1F99' => "\u1F29\u0399" // ᾙ to ἩΙ
        case '\u1F9A' => "\u1F2A\u0399" // ᾚ to ἪΙ
        case '\u1F9B' => "\u1F2B\u0399" // ᾛ to ἫΙ
        case '\u1F9C' => "\u1F2C\u0399" // ᾜ to ἬΙ
        case '\u1F9D' => "\u1F2D\u0399" // ᾝ to ἭΙ
        case '\u1F9E' => "\u1F2E\u0399" // ᾞ to ἮΙ
        case '\u1F9F' => "\u1F2F\u0399" // ᾟ to ἯΙ
        case '\u1FA0' => "\u1F68\u0399" // ᾠ to ὨΙ
        case '\u1FA1' => "\u1F69\u0399" // ᾡ to ὩΙ
        case '\u1FA2' => "\u1F6A\u0399" // ᾢ to ὪΙ
        case '\u1FA3' => "\u1F6B\u0399" // ᾣ to ὫΙ
        case '\u1FA4' => "\u1F6C\u0399" // ᾤ to ὬΙ
        case '\u1FA5' => "\u1F6D\u0399" // ᾥ to ὭΙ
        case '\u1FA6' => "\u1F6E\u0399" // ᾦ to ὮΙ
        case '\u1FA7' => "\u1F6F\u0399" // ᾧ to ὯΙ
        case '\u1FA8' => "\u1F68\u0399" // ᾨ to ὨΙ
        case '\u1FA9' => "\u1F69\u0399" // ᾩ to ὩΙ
        case '\u1FAA' => "\u1F6A\u0399" // ᾪ to ὪΙ
        case '\u1FAB' => "\u1F6B\u0399" // ᾫ to ὫΙ
        case '\u1FAC' => "\u1F6C\u0399" // ᾬ to ὬΙ
        case '\u1FAD' => "\u1F6D\u0399" // ᾭ to ὭΙ
        case '\u1FAE' => "\u1F6E\u0399" // ᾮ to ὮΙ
        case '\u1FAF' => "\u1F6F\u0399" // ᾯ to ὯΙ
      }
      else if (c >= 0x1FB2 && c <= 0x1FFC) (c: @switch) match {
        case '\u1FB2' => "\u1FBA\u0399"       // ᾲ to ᾺΙ
        case '\u1FB3' => "\u0391\u0399"       // ᾳ to ΑΙ
        case '\u1FB4' => "\u0386\u0399"       // ᾴ to ΆΙ
        case '\u1FB6' => "\u0391\u0342"       // ᾶ to Α͂
        case '\u1FB7' => "\u0391\u0342\u0399" // ᾷ to Α͂Ι
        case '\u1FBC' => "\u0391\u0399"       // ᾼ to ΑΙ
        case '\u1FC2' => "\u1FCA\u0399"       // ῂ to ῊΙ
        case '\u1FC3' => "\u0397\u0399"       // ῃ to ΗΙ
        case '\u1FC4' => "\u0389\u0399"       // ῄ to ΉΙ
        case '\u1FC6' => "\u0397\u0342"       // ῆ to Η͂
        case '\u1FC7' => "\u0397\u0342\u0399" // ῇ to Η͂Ι
        case '\u1FCC' => "\u0397\u0399"       // ῌ to ΗΙ
        case '\u1FD2' => "\u0399\u0308\u0300" // ῒ to Ϊ̀
        case '\u1FD3' => "\u0399\u0308\u0301" // ΐ to Ϊ́
        case '\u1FD6' => "\u0399\u0342"       // ῖ to Ι͂
        case '\u1FD7' => "\u0399\u0308\u0342" // ῗ to Ϊ͂
        case '\u1FE2' => "\u03A5\u0308\u0300" // ῢ to Ϋ̀
        case '\u1FE3' => "\u03A5\u0308\u0301" // ΰ to Ϋ́
        case '\u1FE4' => "\u03A1\u0313"       // ῤ to Ρ̓
        case '\u1FE6' => "\u03A5\u0342"       // ῦ to Υ͂
        case '\u1FE7' => "\u03A5\u0308\u0342" // ῧ to Ϋ͂
        case '\u1FF2' => "\u1FFA\u0399"       // ῲ to ῺΙ
        case '\u1FF3' => "\u03A9\u0399"       // ῳ to ΩΙ
        case '\u1FF4' => "\u038F\u0399"       // ῴ to ΏΙ
        case '\u1FF6' => "\u03A9\u0342"       // ῶ to Ω͂
        case '\u1FF7' => "\u03A9\u0342\u0399" // ῷ to Ω͂Ι
        case '\u1FFC' => "\u03A9\u0399"       // ῼ to ΩΙ
        case _        => null                 // no special handling
      }
      else if (c >= 0xFB00 && c <= 0xFB17) (c: @switch) match {
        case '\uFB00' => "\u0046\u0046"       // ﬀ to FF
        case '\uFB01' => "\u0046\u0049"       // ﬁ to FI
        case '\uFB02' => "\u0046\u004C"       // ﬂ to FL
        case '\uFB03' => "\u0046\u0046\u0049" // ﬃ to FFI
        case '\uFB04' => "\u0046\u0046\u004C" // ﬄ to FFL
        case '\uFB05' => "\u0053\u0054"       // ﬅ to ST
        case '\uFB06' => "\u0053\u0054"       // ﬆ to ST
        case '\uFB13' => "\u0544\u0546"       // ﬓ to ՄՆ
        case '\uFB14' => "\u0544\u0535"       // ﬔ to ՄԵ
        case '\uFB15' => "\u0544\u053B"       // ﬕ to ՄԻ
        case '\uFB16' => "\u054E\u0546"       // ﬖ to ՎՆ
        case '\uFB17' => "\u0544\u053D"       // ﬗ to ՄԽ
        case _        => null                 // no special handling
      }
      else {
        (c: @switch) match {
          case '\u00DF' => "\u0053\u0053"       // ß to SS
          case '\u0149' => "\u02BC\u004E"       // ŉ to ʼN
          case '\u01F0' => "\u004A\u030C"       // ǰ to J̌
          case '\u0390' => "\u0399\u0308\u0301" // ΐ to Ϊ́
          case '\u03B0' => "\u03A5\u0308\u0301" // ΰ to Ϋ́
          case '\u0587' => "\u0535\u0552"       // և to ԵՒ
          case '\u1F50' => "\u03A5\u0313"       // ὐ to Υ̓
          case '\u1F52' => "\u03A5\u0313\u0300" // ὒ to Υ̓̀
          case '\u1F54' => "\u03A5\u0313\u0301" // ὔ to Υ̓́
          case '\u1F56' => "\u03A5\u0313\u0342" // ὖ to Υ̓͂
          case _        => null                 // no special handling
        }
      }
    }.asInstanceOf[_String]
      .toCase(Character.toUpperCase)
  }

  private def toCase(convert: Int => Int): _String = {
    if (count == 0) return this
    val buf = new java.lang.StringBuilder(count)
    var i   = offset
    while (i < offset + count) {
      val high = value(i)
      i += 1
      if (Character.isHighSurrogate(high)) {
        if (i < offset + count) {
          val low = value(i)
          i += 1
          if (Character.isLowSurrogate(low)) {
            val cp    = Character.toCodePoint(high, low)
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
   *  - `toLowerCaseLithuanian()`
   *  - `toLowerCaseTurkishAndAzeri()`
   *  - `toUpperCaseLithuanian()`
   *  - `toUpperCaseTurkishAndAzeri()`
   *
   *  @param replacementAtIndex
   *    A function from index to `String | Null`, which should return a special
   *    replacement string for the character at the given index, or `null` if
   *    the character at the given index is not special.
   */
  @inline
  private def replaceCharsAtIndex(replacementAtIndex: Int => String): String = {
    var prep: java.lang.StringBuilder = null
    val len                           = this.length()
    var i                             = 0
    var startOfSegment                = 0

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

  private def skipCharsWithCombiningClassOtherThanNoneOrAboveForwards(
      i: Int): Int = {
    // scalastyle:off return
    import Character._
    val len = length()
    var j   = i
    while (j != len) {
      val cp = this.codePointAt(j)
      if (combiningClassNoneOrAboveOrOther(cp) != CombiningClassIsOther)
        return j
      j += Character.charCount(cp)
    }
    j
    // scalastyle:on return
  }

  private def skipCharsWithCombiningClassOtherThanNoneOrAboveBackwards(
      i: Int): Int = {
    // scalastyle:off return
    import Character._
    var j = i
    while (j > 0) {
      val cp = this.codePointBefore(j)
      if (combiningClassNoneOrAboveOrOther(cp) != CombiningClassIsOther)
        return j
      j -= Character.charCount(cp)
    }
    0
    // scalastyle:on return
  }

  def trim(): _String = {
    var start = offset
    val last  = offset + count - 1
    var end   = last

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
    var begin          = 0
    var end            = 0
    while (separatorCount + 1 != max && { end = indexOf(ch, begin); end != -1 }) {
      separatorCount += 1
      begin = end + 1
    }
    val lastPartEnd = if (max == 0 && begin == count) {
      if (separatorCount == count) {
        return Array.empty[String]
      }
      do {
        begin -= 1
      } while (charAt(begin - 1) == ch)
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

  def getValue(): Array[Char] = value
}

object _String {
  final val CASE_INSENSITIVE_ORDER: Comparator[_String] =
    new CaseInsensitiveComparator()
  private final val ascii = {
    val ascii = new Array[Char](128)
    var i     = 0
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

  def format(fmt: _String, args: Array[AnyRef]): _String =
    new Formatter().format(fmt, args).toString

  def format(loc: Locale, fmt: _String, args: Array[AnyRef]): _String =
    new Formatter(loc).format(fmt, args).toString()

  import scala.language.implicitConversions
  @inline private implicit def _string2string(s: _String): String =
    s.asInstanceOf[String]
  @inline private implicit def string2_string(s: String): _String =
    s.asInstanceOf[_String]
}
