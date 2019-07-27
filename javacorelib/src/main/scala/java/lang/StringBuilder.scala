package java.lang

import AbstractStringBuilder.INITIAL_CAPACITY

final class StringBuilder
    extends AbstractStringBuilder
    with Appendable
    with CharSequence
    with Serializable {
  def this(capacity: Int) = {
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

  def append(b: scala.Boolean): StringBuilder = {
    append0(if (b) "true" else "false")
    this
  }

  def append(c: scala.Char): StringBuilder = {
    append0(c)
    this
  }

  def append(i: scala.Int): StringBuilder = {
    append0(Integer.toString(i))
    this
  }

  def append(l: scala.Long): StringBuilder = {
    append0(Long.toString(l))
    this
  }

  def append(f: scala.Float): StringBuilder = {
    append0(Float.toString(f))
    this
  }

  def append(d: scala.Double): StringBuilder = {
    append0(Double.toString(d))
    this
  }

  def append(obj: Object): StringBuilder = {
    if (obj == null) appendNull()
    else append0(obj.toString)
    this
  }

  def append(str: String): StringBuilder = {
    append0(str)
    this
  }

  def append(sb: StringBuffer): StringBuilder = {
    if (sb == null) {
      appendNull()
    } else {
      append0(sb.getValue(), 0, sb.length())
    }
    this
  }

  def append(str: Array[scala.Char]): StringBuilder = {
    append0(str)
    this
  }

  def append(str: Array[scala.Char],
             offset: scala.Int,
             len: scala.Int): StringBuilder = {
    append0(str, offset, len)
    this
  }

  def append(seq: CharSequence): StringBuilder = {
    if (seq == null) appendNull()
    else append0(seq.toString)
    this
  }

  def append(seq: CharSequence,
             start: scala.Int,
             end: scala.Int): StringBuilder = {
    append0(seq, start, end)
    this
  }

  def appendCodePoint(codePoint: scala.Int): StringBuilder = {
    append0(Character.toChars(codePoint))
    this
  }

  def delete(start: scala.Int, end: scala.Int): StringBuilder = {
    delete0(start, end)
    this
  }

  def deleteCharAt(index: scala.Int): StringBuilder = {
    deleteCharAt0(index)
    this
  }

  def insert(offset: scala.Int, b: scala.Boolean): StringBuilder = {
    insert0(offset, if (b) "true" else "false")
    this
  }

  def insert(offset: scala.Int, c: scala.Char): StringBuilder = {
    insert0(offset, c)
    this
  }

  def insert(offset: scala.Int, i: scala.Int): StringBuilder = {
    insert0(offset, Integer.toString(i))
    this
  }

  def insert(offset: scala.Int, l: scala.Long): StringBuilder = {
    insert0(offset, Long.toString(l))
    this
  }

  def insert(offset: scala.Int, f: scala.Float): StringBuilder = {
    insert0(offset, Float.toString(f))
    this
  }

  def insert(offset: scala.Int, d: scala.Double): StringBuilder = {
    insert0(offset, Double.toString(d))
    this
  }

  def insert(offset: scala.Int, obj: Object): StringBuilder = {
    insert0(offset, if (obj == null) "null" else obj.toString())
    this
  }

  def insert(offset: scala.Int, str: String): StringBuilder = {
    insert0(offset, str)
    this
  }

  def insert(offset: scala.Int,
             str: Array[scala.Char],
             strOffset: scala.Int,
             strLen: scala.Int): StringBuilder = {
    insert0(offset, str, strOffset, strLen)
    this
  }

  def insert(offset: scala.Int, seq: CharSequence): StringBuilder = {
    insert0(offset, if (seq == null) "null" else seq.toString)
    this
  }

  def insert(index: Int, chars: Array[scala.Char]): StringBuilder = {
    insert0(index, chars)
    this
  }

  def insert(offset: scala.Int,
             seq: CharSequence,
             start: scala.Int,
             end: scala.Int): StringBuilder = {
    insert0(offset, seq, start, end)
    this
  }

  def replace(start: scala.Int, end: scala.Int, str: String): StringBuilder = {
    replace0(start, end, str)
    this
  }

  def reverse(): StringBuilder = {
    reverse0()
    this
  }

  override def toString() = super.toString()
}
