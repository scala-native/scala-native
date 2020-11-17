package java.lang

import java.io._

import AbstractStringBuilder.INITIAL_CAPACITY

final class StringBuffer
    extends AbstractStringBuilder
    with Appendable
    with Serializable
    with CharSequence {

  def this(capacity: scala.Int) = {
    this()
    value = new Array[scala.Char](capacity)
  }

  def this(seq: CharSequence) = {
    this()
    val string = seq.toString
    count = string.length()
    shared = false
    value = new Array[scala.Char](count + INITIAL_CAPACITY)
    string.getChars(0, count, value, 0)
  }

  def this(string: String) = {
    this()
    count = string.length()
    shared = false
    value = new Array[scala.Char](count + INITIAL_CAPACITY)
    string.getChars(0, count, value, 0)
  }

  def append(b: scala.Boolean): StringBuffer = {
    append0(if (b) "true" else "false")
    this
  }

  def append(c: scala.Char): StringBuffer =
    synchronized {
      append0(c)
      this
    }

  def append(d: scala.Double): StringBuffer =
    append(Double.toString(d))

  def append(f: scala.Float): StringBuffer =
    append(Float.toString(f))

  def append(i: scala.Int): StringBuffer =
    append(Integer.toString(i))

  def append(l: scala.Long): StringBuffer =
    append(Long.toString(l))

  def append(obj: Object): StringBuffer =
    synchronized {
      if (obj == null) {
        appendNull()
      } else {
        append0(obj.toString())
      }
      this
    }

  def append(string: String): StringBuffer =
    synchronized {
      append0(string)
      this
    }

  def append(sb: StringBuffer): StringBuffer =
    synchronized {
      if (sb == null) {
        appendNull()
      } else {
        sb.synchronized {
          append0(sb.getValue(), 0, sb.length())
        }
      }
      this
    }

  def append(chars: Array[scala.Char]): StringBuffer = {
    append0(chars)
    this
  }

  def append(chars: Array[scala.Char],
             start: scala.Int,
             length: scala.Int): StringBuffer =
    synchronized {
      append0(chars, start, length)
      this
    }

  def append(s: CharSequence): StringBuffer =
    synchronized {
      if (s == null) {
        appendNull()
      } else {
        append0(s.toString())
      }
      this
    }

  def append(s: CharSequence, start: scala.Int, end: scala.Int): StringBuffer =
    synchronized {
      append0(s, start, end)
      this
    }

  def appendCodePoint(codePoint: scala.Int): StringBuffer =
    append(Character.toChars(codePoint))

  override def charAt(index: scala.Int): scala.Char =
    synchronized {
      super.charAt(index)
    }

  override def codePointAt(index: scala.Int) =
    synchronized {
      super.codePointAt(index)
    }

  override def codePointBefore(index: scala.Int): scala.Int =
    synchronized {
      super.codePointBefore(index)
    }

  override def codePointCount(beginIndex: scala.Int,
                              endIndex: scala.Int): scala.Int =
    synchronized {
      super.codePointCount(beginIndex, endIndex)
    }

  def delete(start: scala.Int, end: scala.Int): StringBuffer =
    synchronized {
      delete0(start, end)
      this
    }

  def deleteCharAt(location: scala.Int): StringBuffer =
    synchronized {
      deleteCharAt0(location)
      this
    }

  override def ensureCapacity(min: scala.Int): Unit =
    synchronized {
      super.ensureCapacity(min)
    }

  override def getChars(start: scala.Int,
                        end: scala.Int,
                        buffer: Array[scala.Char],
                        idx: scala.Int): Unit =
    synchronized {
      super.getChars(start, end, buffer, idx)
    }

  override def indexOf(subString: String, start: scala.Int): scala.Int =
    synchronized {
      super.indexOf(subString, start)
    }

  def insert(index: scala.Int, ch: scala.Char): StringBuffer =
    synchronized {
      insert0(index, ch)
      this
    }

  def insert(index: scala.Int, b: scala.Boolean): StringBuffer =
    insert(index, if (b) "true" else "false")

  def insert(index: scala.Int, i: scala.Int): StringBuffer =
    insert(index, Integer.toString(i))

  def insert(index: scala.Int, l: scala.Long): StringBuffer =
    insert(index, Long.toString(l))

  def insert(index: scala.Int, d: scala.Double): StringBuffer =
    insert(index, Double.toString(d))

  def insert(index: scala.Int, f: scala.Float): StringBuffer =
    insert(index, Float.toString(f))

  def insert(index: scala.Int, obj: Object): StringBuffer =
    insert(index, if (obj == null) "null" else obj.toString())

  def insert(index: scala.Int, string: String): StringBuffer =
    synchronized {
      insert0(index, string)
      return this
    }

  def insert(index: scala.Int, chars: Array[scala.Char]): StringBuffer =
    synchronized {
      insert0(index, chars)
      return this
    }

  def insert(index: scala.Int,
             chars: Array[scala.Char],
             start: scala.Int,
             length: scala.Int): StringBuffer =
    synchronized {
      insert0(index, chars, start, length)
      return this
    }

  def insert(index: scala.Int, s: CharSequence): StringBuffer =
    synchronized {
      insert0(index, if (s == null) "null" else s.toString())
      return this
    }

  def insert(index: scala.Int,
             seq: CharSequence,
             start: scala.Int,
             end: scala.Int): StringBuffer =
    synchronized {
      insert0(index, seq, start, end)
      return this
    }

  override def lastIndexOf(subString: String, start: scala.Int): scala.Int =
    synchronized {
      return super.lastIndexOf(subString, start)
    }

  override def offsetByCodePoints(index: scala.Int,
                                  codePointOffset: scala.Int): scala.Int =
    synchronized {
      return super.offsetByCodePoints(index, codePointOffset)
    }

  def replace(start: scala.Int, end: scala.Int, string: String): StringBuffer =
    synchronized {

      replace0(start, end, string)
      return this
    }

  def reverse(): StringBuffer =
    synchronized {
      reverse0()
      return this
    }

  override def setCharAt(index: scala.Int, ch: scala.Char): Unit =
    synchronized {
      super.setCharAt(index, ch)
    }

  override def setLength(length: scala.Int): Unit =
    synchronized {
      super.setLength(length)
    }

  override def subSequence(start: scala.Int, end: scala.Int): CharSequence =
    synchronized {
      return super.substring(start, end)
    }

  override def substring(start: scala.Int): String =
    synchronized {
      return super.substring(start)
    }

  override def substring(start: scala.Int, end: scala.Int): String =
    synchronized {
      return super.substring(start, end)
    }

  override def toString(): String =
    synchronized {
      return super.toString()
    }

  override def trimToSize(): Unit =
    synchronized {
      super.trimToSize()
    }
}
