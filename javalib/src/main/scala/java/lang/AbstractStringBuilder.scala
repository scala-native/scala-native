package java.lang

import java.io.InvalidObjectException
import java.util.Arrays
import scala.util.control.Breaks._

abstract class AbstractStringBuilder private (unit: Unit) {
  import AbstractStringBuilder._

  protected var value: Array[Char]    = _
  protected var count: scala.Int      = _
  protected var shared: scala.Boolean = _

  final def getValue(): Array[scala.Char] = value
  final def shareValue(): Array[scala.Char] = {
    shared = true
    value
  }

  final def set(chars: Array[scala.Char], len: scala.Int): Unit = {
    val chars0 = if (chars != null) chars else new Array[scala.Char](0)
    if (chars0.length < len) {
      throw new InvalidObjectException("")
    }

    shared = false
    value = chars0
    count = len
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

  final def appendNull(): Unit = {
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

  final def append0(chars: Array[Char]): Unit = {
    val newSize = count + chars.length
    if (newSize > value.length) {
      enlargeBuffer(newSize)
    }
    System.arraycopy(chars, 0, value, count, chars.length)
    count = newSize
  }

  final def append0(chars: Array[Char],
                    offset: scala.Int,
                    length: scala.Int): Unit = {
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

  final def append0(ch: Char): Unit = {
    if (count == value.length) {
      enlargeBuffer(count + 1)
    }
    value(count) = ch
    count += 1
  }

  final def append0(string: String): Unit = {
    if (string == null) {
      appendNull()
      return
    }
    val adding  = string.length()
    val newSize = count + adding
    if (newSize > value.length) {
      enlargeBuffer(newSize)
    }
    string.getChars(0, adding, value, count)
    count = newSize
  }

  final def append0(chars: CharSequence,
                    start: scala.Int,
                    end: scala.Int): Unit = {
    val chars0 = if (chars != null) chars else "null"
    if (start < 0 || end < 0 || start > end || end > chars0.length()) {
      throw new IndexOutOfBoundsException()
    }
    append0(chars0.subSequence(start, end).toString)
  }

  def capacity(): scala.Int = value.length

  def charAt(index: scala.Int): scala.Char = {
    if (index < 0 || index >= count) {
      throw new StringIndexOutOfBoundsException(index)
    }
    return value(index)
  }

  final def delete0(start: scala.Int, _end: scala.Int): Unit = {
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

  final def deleteCharAt0(location: scala.Int): scala.Unit = {
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

  def getChars(start: scala.Int,
               end: scala.Int,
               dest: Array[Char],
               destStart: scala.Int): Unit = {
    if (start > count || end > count || start > end) {
      throw new StringIndexOutOfBoundsException()
    }
    System.arraycopy(value, start, dest, destStart, end - start)
  }

  final def insert0(index: scala.Int, chars: Array[Char]) {
    if (0 > index || index > count) {
      throw new StringIndexOutOfBoundsException(index)
    }
    if (chars.length != 0) {
      move(chars.length, index)
      System.arraycopy(chars, 0, value, index, chars.length)
      count += chars.length
    }
  }

  final def insert0(index: scala.Int,
                    chars: Array[Char],
                    start: scala.Int,
                    length: scala.Int): Unit = {
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
          chars.length)
    }
    throw new StringIndexOutOfBoundsException(index)
  }

  final def insert0(index: scala.Int, ch: scala.Char): Unit = {
    if (0 > index || index > count) {
      throw new ArrayIndexOutOfBoundsException(index)
    }
    move(1, index)
    value(index) = ch
    count += 1
  }

  final def insert0(index: scala.Int, string: String): Unit = {
    if (0 <= index && index <= count) {
      val string0 = if (string != null) string else "null"
      val min     = string0.length
      if (min != 0) {
        move(min, index)
        string0.getChars(0, min, value, index)
        count += min
      }
    } else {
      throw new StringIndexOutOfBoundsException(index)
    }
  }

  final def insert0(index: scala.Int,
                    chars: CharSequence,
                    start: scala.Int,
                    end: scala.Int): Unit = {
    val chars0 = if (chars != null) chars else "null"
    if (index < 0 || index > count || start < 0 || end < 0 || start > end ||
        end > chars0.length()) {
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

  final def replace0(start: scala.Int, _end: scala.Int, string: String): Unit = {
    var end = _end
    if (start >= 0) {
      if (end > count) {
        end = count
      }
      if (end > start) {
        val stringLength = string.length()
        val diff         = end - start - stringLength
        if (diff > 0) {
          if (!shared) {
            System
              .arraycopy(value, end, value, start + stringLength, count - end)
          } else {
            val newData = new Array[scala.Char](value.length)
            System.arraycopy(value, 0, newData, 0, start)
            System.arraycopy(value,
                             end,
                             newData,
                             start + stringLength,
                             count - end)
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

  final def reverse0(): Unit = {
    if (count < 2)
      return
    if (!shared) {
      var end           = count - 1
      var frontHigh     = value(0)
      var endLow        = value(end)
      var allowFrontSur = true
      var allowEndSur   = true
      var i             = 0
      var mid           = count / 2
      while (i < mid) {
        var frontLow = value(i + 1)
        val endHigh  = value(end - 1)
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
      var i       = 0
      var end     = count
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

  def indexOf(subString: String, _start: scala.Int): scala.Int = {
    var start = _start
    if (start < 0) {
      start = 0
    }
    val subCount = subString.length
    if (subCount > 0) {
      if (subCount + start > count) {
        return -1
      }
      val firstChar = subString.charAt(0)
      while (true) {
        var found = false
        var i     = start
        breakable {
          while (!found && i < count) {
            if (value(i) == firstChar) {
              found = true
              break
            }
            i += 1
          }
        }
        if (!found || subCount + i > count) {
          return -1
        }
        var o1 = i
        var o2 = 0
        breakable {
          while (true) {
            o2 += 1
            if (!(o2 < subCount)) break
            o1 += 1
            if (!(value(o1) == subString.charAt(o2))) break
          }
        }
        if (o2 == subCount) {
          return i
        }
        start = i + 1
      }
    }
    return if (start < count || start == 0) start else count
  }

  def lastIndexOf(string: String): scala.Int =
    lastIndexOf(string, count)

  def lastIndexOf(subString: String, _start: scala.Int): scala.Int = {
    var start    = _start
    val subCount = subString.length
    if (subCount <= count && start >= 0) {
      if (subCount > 0) {
        if (start > count - subCount) {
          start = count - subCount
        }
        val firstChar = subString.charAt(0)
        while (true) {
          var i     = start
          var found = false
          breakable {
            while (!found && i >= 0) {
              if (value(i) == firstChar) {
                found = true
                break
              }
              i -= 1
            }
          }
          if (!found) {
            return -1
          }
          var o1 = i
          var o2 = 0
          breakable {
            while (true) {
              o2 += 1
              if (!(o2 < subCount)) break
              o1 += 1
              if (!(value(o1) == subString.charAt(o2))) break
            }
          }
          if (o2 == subCount) {
            return i
          }
          start = i - 1
        }
      }
      return if (start < count) start else count
    }
    return -1
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

  def offsetByCodePoints(index: scala.Int,
                         codePointOffset: scala.Int): scala.Int = {
    return Character
      .offsetByCodePoints(value, 0, count, index, codePointOffset)
  }
}

object AbstractStringBuilder {
  final val INITIAL_CAPACITY = 16
}
