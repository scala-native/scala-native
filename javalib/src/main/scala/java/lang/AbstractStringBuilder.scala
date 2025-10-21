// Contains parts ported from Android Luni
package java.lang

import java.util.{Arrays, Objects}

import scala.scalanative.libc
import scala.scalanative.runtime.ieee754tostring.ryu._
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.unsafe.UnsafeRichArray

/* Design Note:
 * The public methods indexOf(string) and lastIndexOf(string) and their
 * private indexOf(char) and lastIndexOf(char) are slightly modified
 * copies of the String.scala code. The methods in String.scala are
 * more likely to have been heavily exercised and to be correct.
 *
 * Textually duplicating code is regrettable but there was no easy
 * way to call into the String code or to have common code with reasonable
 * performance. String code is performance sensitive.
 *
 * The coding style in the routines here is a bit strange. It is designed
 * to minimize changes from the String code.  A "bridge" variable "offset"
 * is introduced, set to 0, and otherwise unused. That means that number
 * of other lines of code do not need to change.
 *
 * Most of the necessary changes from the String code, such as getting
 * lengths and substring contents are marked. This code will probably
 * be visited again.
 */

protected abstract class AbstractStringBuilder private (unit: Unit) {
  import AbstractStringBuilder._

  protected var value: Array[scala.Char] = _
  protected var count: scala.Int = _
  protected var shared: scala.Boolean = _

  private[lang] final def getValue(): Array[scala.Char] = value

  final def shareValue(): Array[scala.Char] = {
    shared = true
    value
  }

  def this() = {
    this(())
    value = new Array[scala.Char](INITIAL_CAPACITY)
  }

  def this(capacity: scala.Int) = {
    this(())
    value = new Array[scala.Char](capacity)
  }

  def this(string: String) = {
    this(())
    count = string.length
    shared = false
    value = new Array[scala.Char](count + INITIAL_CAPACITY)
    string.getChars(0, count, value, 0)
  }

  private def enlargeBuffer(min: scala.Int): Unit = {
    val newSize = ((value.length >> 1) + value.length) + 2
    val newData = new Array[scala.Char](if (min > newSize) min else newSize)
    System.arraycopy(value, 0, newData, 0, count)
    value = newData
    shared = false
  }

  protected final def appendNull(): Unit = {
    val newSize = count + 4
    if (newSize > value.length) {
      enlargeBuffer(newSize)
    }
    value(count) = 'n'
    count += 1
    value(count) = 'u'
    count += 1
    value(count) = 'l'
    count += 1
    value(count) = 'l'
    count += 1
  }

  protected final def append0(chars: Array[scala.Char]): Unit = {
    val newSize = count + chars.length
    if (newSize > value.length) {
      enlargeBuffer(newSize)
    }
    System.arraycopy(chars, 0, value, count, chars.length)
    count = newSize
  }

  protected final def append0(
      chars: Array[scala.Char],
      offset: scala.Int,
      length: scala.Int
  ): Unit = {
    if (offset > chars.length || offset < 0) {
      throw new ArrayIndexOutOfBoundsException("")
    }
    if (length < 0 || chars.length - offset < length) {
      throw new ArrayIndexOutOfBoundsException("")
    }

    val newSize = count + length
    if (newSize > value.length) {
      enlargeBuffer(newSize)
    }
    System.arraycopy(chars, offset, value, count, length)
    count = newSize
  }

  protected final def append0(ch: Char): Unit = {
    if (count == value.length) {
      enlargeBuffer(count + 1)
    }
    value(count) = ch
    count += 1
  }

  // Optimization: use `RyuFloat.floatToChars()` instead of `floatToString()`
  protected final def append0(f: scala.Float): Unit = {

    // We first ensure that we have enough space in the backing Array (`value`)
    this.ensureCapacity(this.count + RyuFloat.RESULT_STRING_MAX_LENGTH)

    // Then we call `RyuFloat.floatToChars()`, which will append chars to `value`
    this.count = RyuFloat.floatToChars(
      f,
      RyuRoundingMode.Conservative,
      value,
      this.count
    )
  }

  // Optimization: use `RyuFloat.doubleToChars()` instead of `doubleToString()`
  protected final def append0(d: scala.Double): Unit = {

    // We first ensure that we have enough space in the backing Array (`value`)
    this.ensureCapacity(this.count + RyuDouble.RESULT_STRING_MAX_LENGTH)

    // Then we call `RyuFloat.doubleToChars()`, which will append chars to `value`
    this.count = RyuDouble.doubleToChars(
      d,
      RyuRoundingMode.Conservative,
      value,
      this.count
    )
  }

  protected final def append0(string: String): Unit = {

    if (string == null) {
      appendNull()
      return
    }
    val adding = string.length()
    val newSize = count + adding
    if (newSize > value.length) {
      enlargeBuffer(newSize)
    }
    string.getChars(0, adding, value, count)
    count = newSize
  }

  protected final def append0(
      chars: CharSequence,
      start: scala.Int,
      end: scala.Int
  ): Unit = {
    val chars0 = if (chars != null) chars else "null"

    val nChars = chars0.length()
    if (nChars == 0) return

    if (start < 0 || end < 0 || start > end || end > nChars)
      throw new IndexOutOfBoundsException()

    val length = end - start
    val newCount = count + length
    if (newCount > value.length)
      enlargeBuffer(newCount)

    chars0 match {
      case str: String                => str.getChars(start, end, value, count)
      case asb: AbstractStringBuilder =>
        System.arraycopy(asb.value, start, value, count, length)
      case _ =>
        var i = start
        var j = count // Destination index.
        while (i < end) {
          value(j) = chars0.charAt(i)
          j += 1
          i += 1
        }
    }

    this.count = newCount
  }

  def capacity(): scala.Int = value.length

  def charAt(index: scala.Int): scala.Char = {
    if (index < 0 || index >= count) {
      throw new StringIndexOutOfBoundsException(index)
    }
    return value(index)
  }

  protected def compareTo0(that: AbstractStringBuilder): Int = {
    Objects.requireNonNull(
      that,
      """Cannot read field "value" because "another" is null"""
    )

    Arrays.compare(value, 0, this.count, that.value, 0, that.count)
  }

  protected final def delete0(start: scala.Int, _end: scala.Int): Unit = {
    var end = _end
    if (start >= 0) {
      if (end > count) {
        end = count
      }
      if (end == start) {
        return
      }
      if (end > start) {
        val length = count - end
        if (length >= 0) {
          if (!shared) {
            System.arraycopy(value, end, value, start, length)
          } else {
            val newData = new Array[scala.Char](value.length)
            System.arraycopy(value, 0, newData, 0, start)
            System.arraycopy(value, end, newData, start, length)
            value = newData
            shared = false
          }
        }
        count -= end - start
        return
      }
    }
    throw new StringIndexOutOfBoundsException()
  }

  protected final def deleteCharAt0(location: scala.Int): scala.Unit = {
    if (0 > location || location >= count) {
      throw new StringIndexOutOfBoundsException(location)
    }
    val length = count - location - 1
    if (length > 0) {
      if (!shared) {
        System.arraycopy(value, location + 1, value, location, length)
      } else {
        val newData = new Array[scala.Char](value.length)
        System.arraycopy(value, 0, newData, 0, location)
        System.arraycopy(value, location + 1, newData, location, length)
        value = newData
        shared = false
      }
    }
    count -= 1
  }

  def ensureCapacity(min: scala.Int): Unit = {
    if (min > value.length) {
      val twice = (value.length << 1) + 2
      enlargeBuffer(if (twice > min) twice else min)
    }
  }

  def getChars(
      start: scala.Int,
      end: scala.Int,
      dest: Array[scala.Char],
      destStart: scala.Int
  ): Unit = {
    if (start > count || end > count || start > end) {
      throw new StringIndexOutOfBoundsException()
    }
    System.arraycopy(value, start, dest, destStart, end - start)
  }

  protected final def insert0(
      index: scala.Int,
      chars: Array[scala.Char]
  ): Unit = {
    if (0 > index || index > count) {
      throw new StringIndexOutOfBoundsException(index)
    }
    if (chars.length != 0) {
      move(chars.length, index)
      System.arraycopy(chars, 0, value, index, chars.length)
      count += chars.length
    }
  }

  protected final def insert0(
      index: scala.Int,
      chars: Array[scala.Char],
      start: scala.Int,
      length: scala.Int
  ): Unit = {
    if (0 <= index && index <= count) {
      if (start >= 0 && 0 <= length && length <= chars.length - start) {
        if (length != 0) {
          move(length, index)
          System.arraycopy(chars, start, value, index, length)
          count += length
        }
        return
      }
      throw new StringIndexOutOfBoundsException(
        "offset " + start + ", length " + length + ", char[].length" +
          chars.length
      )
    }
    throw new StringIndexOutOfBoundsException(index)
  }

  protected final def insert0(index: scala.Int, ch: scala.Char): Unit = {
    if (0 > index || index > count) {
      throw new ArrayIndexOutOfBoundsException(index)
    }
    move(1, index)
    value(index) = ch
    count += 1
  }

  protected final def insert0(index: scala.Int, string: String): Unit = {
    if (0 <= index && index <= count) {
      val string0 = if (string != null) string else "null"
      val min = string0.length
      if (min != 0) {
        move(min, index)
        string0.getChars(0, min, value, index)
        count += min
      }
    } else {
      throw new StringIndexOutOfBoundsException(index)
    }
  }

  protected final def insert0(
      index: scala.Int,
      chars: CharSequence,
      start: scala.Int,
      end: scala.Int
  ): Unit = {
    val chars0 = if (chars != null) chars else "null"
    if (index < 0 || index > count || start < 0 || end < 0 ||
        start > end || end > chars0.length()) {
      throw new IndexOutOfBoundsException()
    }
    insert0(index, chars0.subSequence(start, end).toString)
  }

  def length(): scala.Int = count

  private def move(size: scala.Int, index: scala.Int): Unit = {
    val newSize = if (value.length - count >= size) {
      if (!shared) {
        System.arraycopy(value, index, value, index + size, count - index)
        return
      }
      value.length
    } else {
      val a = count + size
      val b = (value.length << 1) + 2
      if (a > b) a else b
    }

    val newData = new Array[scala.Char](newSize)
    System.arraycopy(value, 0, newData, 0, index)
    System.arraycopy(value, index, newData, index + size, count - index)
    value = newData
    shared = false
  }

  protected def repeat0(codePoint: Int, n: Int): Unit = {
    if (!Character.isValidCodePoint(codePoint)) {
      val hexString = Integer.toHexString(codePoint).toUpperCase()
      throw new IllegalArgumentException(
        s"Not a valid Unicode code point: 0x${hexString}"
      )
    }

    if (n < 0)
      throw new IllegalArgumentException(s"count is negative: ${count}")

    val cpChars = Character.toChars(codePoint)
    val cpCharsLength = cpChars.length
    ensureCapacity(count + (cpCharsLength * n))

    for (j <- 0 until n)
      append0(cpChars)
  }

  protected def repeat0(cs: CharSequence, n: Int): Unit = {
    if (n < 0)
      throw new IllegalArgumentException(s"count is negative: ${count}")

    val csLength = cs.length()
    ensureCapacity(count + (csLength * n))

    for (j <- 0 until n)
      append0(cs, 0, csLength)
  }

  protected final def replace0(
      start: scala.Int,
      _end: scala.Int,
      string: String
  ): Unit = {
    var end = _end
    if (start >= 0) {
      if (end > count) {
        end = count
      }
      if (end > start) {
        val stringLength = string.length()
        val diff = end - start - stringLength
        if (diff > 0) {
          if (!shared) {
            System
              .arraycopy(value, end, value, start + stringLength, count - end)
          } else {
            val newData = new Array[scala.Char](value.length)
            System.arraycopy(value, 0, newData, 0, start)
            System.arraycopy(
              value,
              end,
              newData,
              start + stringLength,
              count - end
            )
            value = newData
            shared = false
          }
        } else if (diff < 0) {
          move(-diff, end)
        } else if (shared) {
          value = value.clone()
          shared = false
        }
        string.getChars(0, stringLength, value, start)
        count -= diff
        return
      }
      if (start == end) {
        if (string == null) {
          throw new NullPointerException()
        }
        insert0(start, string)
        return
      }
    }
    throw new StringIndexOutOfBoundsException()
  }

  protected final def reverse0(): Unit = {
    if (count < 2)
      return
    if (!shared) {
      var end = count - 1
      var frontHigh = value(0)
      var endLow = value(end)
      var allowFrontSur = true
      var allowEndSur = true
      var i = 0
      var mid = count / 2
      while (i < mid) {
        var frontLow = value(i + 1)
        val endHigh = value(end - 1)
        val surAtFront =
          allowFrontSur && frontLow >= 0xdc00 && frontLow <= 0xdfff &&
            frontHigh >= 0xd800 && frontHigh <= 0xdbff
        if (surAtFront && (count < 3)) {
          return
        }
        val surAtEnd =
          allowEndSur && endHigh >= 0xd800 && endHigh <= 0xdbff &&
            endLow >= 0xdc00 && endLow <= 0xdfff
        allowFrontSur = true
        allowEndSur = true

        if (surAtFront == surAtEnd) {
          if (surAtFront) {
            value(end) = frontLow
            value(end - 1) = frontHigh
            value(i) = endHigh
            value(i + 1) = endLow
            frontHigh = value(i + 2)
            endLow = value(end - 2)
            i += 1
            end -= 1
          } else {
            value(end) = frontHigh
            value(i) = endLow
            frontHigh = frontLow
            endLow = endHigh
          }
        } else {
          if (surAtFront) {
            value(end) = frontLow
            value(i) = endLow
            endLow = endHigh
            allowFrontSur = false
          } else {
            value(end) = frontHigh
            value(i) = endHigh
            frontHigh = frontLow
            allowEndSur = false
          }
        }
        i += 1
        end -= 1
      }
      if ((count & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
        value(end) = if (allowFrontSur) endLow else frontHigh
      }
    } else {
      val newData = new Array[scala.Char](value.length)
      var i = 0
      var end = count
      while (i < count) {
        val high = value(i)
        if ((i + 1) < count && high >= 0xd800 && high <= 0xdbff) {
          val low = value(i + 1)
          if (low >= 0xdc00 && low <= 0xdfff) {
            end -= 1
            newData(end) = low
            i += 1
          }
        }
        end -= 1
        newData(end) = high
        i += 1
      }
      value = newData
      shared = false
    }
  }

  def setCharAt(index: scala.Int, ch: scala.Char): Unit = {
    if (0 > index || index >= count) {
      throw new StringIndexOutOfBoundsException(index)
    }
    if (shared) {
      value = value.clone()
      shared = false
    }
    value(index) = ch
  }

  def setLength(length: scala.Int): Unit = {
    if (length < 0) {
      throw new StringIndexOutOfBoundsException(length)
    }
    if (length > value.length) {
      enlargeBuffer(length)
    } else {
      if (shared) {
        val newData = new Array[scala.Char](value.length)
        System.arraycopy(value, 0, newData, 0, count)
        value = newData
        shared = false
      } else {
        if (count < length) {
          Arrays.fill(value, count, length, 0.toChar)
        }
      }
    }
    count = length
  }

  def substring(start: scala.Int): String = {
    if (0 <= start && start <= count) {
      if (start == count) {
        return ""
      }
      return new String(value, start, count - start)
    }
    throw new StringIndexOutOfBoundsException(start)
  }

  def substring(start: scala.Int, end: scala.Int): String = {
    if (0 <= start && start <= end && end <= count) {
      if (start == end) {
        return ""
      }
      return new String(value, start, end - start)
    }
    throw new StringIndexOutOfBoundsException()
  }

  override def toString(): String = {
    if (count == 0) {
      return ""
    }
    val wasted = value.length - count
    if (wasted >= 256 ||
        (wasted >= INITIAL_CAPACITY && wasted >= (count >> 1))) {
      return new String(value, 0, count)
    }
    shared = true
    return new String(value, 0, count)
  }

  def subSequence(start: scala.Int, end: scala.Int): CharSequence =
    substring(start, end)

  def indexOf(string: String): scala.Int =
    indexOf(string, 0)

  // See Design Note at top of this file.
  private def indexOf(c: Int, _start: Int): Int = {
    var offset = 0 // different than SN String.scala
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

  // See Design Note at top of this file.
  def indexOf(subString: String, _start: scala.Int): scala.Int = {
    var offset = 0 // different than SN String.scala
    var start = _start
    if (start < 0) {
      start = 0
    }
    val subCount = subString.length() // different than SN String.scala
    if (subCount > 0) {
      if (subCount + start > count) {
        return -1
      }
      val target = subString.toCharArray() // different than SN String.scala
      val subOffset = 0 // different than SN String.scala
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

  def lastIndexOf(string: String): scala.Int =
    lastIndexOf(string, count)

  // See Design Note at top of this file.
  private def lastIndexOf(c: Int, _start: Int): Int = {
    var offset = 0 // different than SN String.scala
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

  // See Design Note at top of this file.
  def lastIndexOf(subString: String, _start: scala.Int): scala.Int = {
    var offset = 0 // different than SN String.scala
    var start = _start
    val subCount = subString.length() // different than SN String.scala
    if (subCount <= count && start >= 0) {
      if (subCount > 0) {
        if (start > count - subCount) {
          start = count - subCount
        }
        val target = subString.toCharArray() // different than SN String.scala
        val subOffset = 0 // different than SN String.scala
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

  def trimToSize(): Unit = {
    if (count < value.length) {
      val newValue = new Array[scala.Char](count)
      System.arraycopy(value, 0, newValue, 0, count)
      value = newValue
      shared = false
    }
  }

  def codePointAt(index: scala.Int): scala.Int = {
    if (index < 0 || index >= count) {
      throw new StringIndexOutOfBoundsException(index)
    }
    return Character.codePointAt(value, index, count)
  }

  def codePointBefore(index: scala.Int): scala.Int = {
    if (index < 1 || index > count) {
      throw new StringIndexOutOfBoundsException()
    }
    return Character.codePointBefore(value, index)
  }

  def codePointCount(beginIndex: scala.Int, endIndex: scala.Int): scala.Int = {
    if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
      throw new StringIndexOutOfBoundsException()
    }
    return Character.codePointCount(value, beginIndex, endIndex - beginIndex)
  }

  def offsetByCodePoints(
      index: scala.Int,
      codePointOffset: scala.Int
  ): scala.Int = {
    return Character
      .offsetByCodePoints(value, 0, count, index, codePointOffset)
  }
}

object AbstractStringBuilder {
  final val INITIAL_CAPACITY = 16
}
