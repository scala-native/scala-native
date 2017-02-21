package java.lang

import scalanative.native._
import java.io.Serializable
import java.util._
import java.util.regex._
import java.nio._
import java.nio.charset._

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
    value = charBuffer.array
    count = charBuffer.length
  }

  def this(data: Array[scala.Byte],
           start: Int,
           length: Int,
           encoding: _String) =
    this(data, start, length, Charset.forName(encoding))

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
      throw new IndexOutOfBoundsException()
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

  def this(sb: StringBuffer) {
    this()
    offset = 0
    value = sb.getValue
    count = sb.length
  }

  def this(codePoints: Array[Int], offset: Int, count: Int) {
    this()
    if (offset < 0 || count < 0 || offset > codePoints.length - count) {
      throw new IndexOutOfBoundsException()
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

  def this(sb: java.lang.StringBuilder) {
    this()
    offset = 0
    count = sb.length
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
    case s: _String if s eq this =>
      true
    case s: _String =>
      val thisHash = this.hashCode
      val thatHash = s.hashCode
      if (count != s.count ||
          (thisHash != thatHash && thisHash != 0 && thatHash != 0)) {
        false
      } else {
        var i = 0
        while (i < count) {
          if (value(offset + i) != s.value(s.offset + i)) {
            return false
          } else {
            i += 1
          }
        }
        true
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
    val charset = Charset.forName(encoding)
    val buffer  = charset.encode(CharBuffer.wrap(value, offset, count))
    val bytes   = new Array[scala.Byte](buffer.limit())
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
        var hash = 0
        var i    = offset
        while (i < count + offset) {
          hash = value(i) + ((hash << 5) - hash)
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
          while ({ o2 += 1; o2 } < end && value({ o1 += 1; o1 }) == target(o2)) ()
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

      if ("".equals(rs)) {
        val buffer =
          new java.lang.StringBuilder(count + (rs.length * (count + 1)))
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
      val tl     = target.length
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

  def toLowerCase(locale: Locale): _String = ???

  def toLowerCase(): _String =
    toLowerCase(Locale.getDefault)

  override def toString(): String = this

  def toUpperCase(locale: Locale): _String = ???

  def toUpperCase(): _String =
    toUpperCase(Locale.getDefault)

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
    val size = sb.length
    if (count != size) {
      false
    } else {
      regionMatches(0, new _String(0, size, sb.getValue), 0, size)
    }
  }

  def contentEquals(cs: CharSequence): scala.Boolean = {
    val len = cs.length
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

  def split(expr: _String): Array[String] =
    Pattern.compile(expr).split(this)

  def split(expr: _String, max: Int): Array[String] =
    Pattern.compile(expr).split(this, max)

  def subSequence(start: Int, end: Int): CharSequence =
    substring(start, end)

  def codePointAt(index: Int): Int =
    if (index < 0 || index >= count) {
      throw new IndexOutOfBoundsException()
    } else {
      Character.codePointAt(value, index + offset, offset + count)
    }

  def codePointBefore(index: Int): Int =
    if (index < 1 || index > count) {
      throw new IndexOutOfBoundsException()
    } else {
      Character.codePointBefore(value, index + offset)
    }

  def codePointCount(beginIndex: Int, endIndex: Int): Int =
    if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
      throw new IndexOutOfBoundsException()
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
    format(Locale.getDefault(), fmt, args)

  def format(loc: Locale, fmt: _String, args: Array[AnyRef]): _String = {
    if (fmt == null) {
      throw new NullPointerException("null format argument")
    } else {
      val bufferSize =
        if (args == null) fmt.length + 0
        else fmt.length + args.length * 10
      val f = new Formatter(new java.lang.StringBuilder(bufferSize), loc)
      f.format(fmt, args).toString
    }
  }

  import scala.language.implicitConversions
  @inline private implicit def _string2string(s: _String): String =
    s.asInstanceOf[String]
  @inline private implicit def string2_string(s: String): _String =
    s.asInstanceOf[_String]
}
