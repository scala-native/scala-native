package java.lang

import java.nio.charset.Charset

class _String private() extends CharSequence with Serializable with Comparable[_String] {
  def this(bytes: Array[scala.Byte]) = this()
  def this(bytes: Array[scala.Byte], charset: Charset) = this()
  def this(bytes: Array[scala.Byte], offset: scala.Int, length: scala.Int) = this()
  def this(bytes: Array[scala.Byte], offset: scala.Int, length: scala.Int, charset: Charset) = this()
  def this(bytes: Array[scala.Byte], offset: scala.Int, length: scala.Int, charsetName: _String) = this()
  def this(bytes: Array[scala.Byte], charsetName: _String) = this()
  def this(value: Array[scala.Char]) = this()
  def this(value: Array[scala.Char], offset: scala.Int, count: scala.Int) = this()
  def this(codePoint: Array[scala.Int], offset: scala.Int, count: scala.Int) = this()
  def this(original: _String) = this()
  def this(buffer: StringBuffer) = this()
  def this(builder: StringBuilder) = this()

  def charAt(index: scala.Int): Char = ???
  def length(): scala.Int = ???
  def concat(str: _String): _String = ???
  def equalsIgnoreCase(another_String: _String): Boolean = ???
  def toUpperCase(): _String = ???
  def toUpperCase(locale: java.util.Locale): _String = ???
  def toLowerCase(): _String = ???
  def toLowerCase(locale: java.util.Locale): _String = ???
  def toCharArray(): Array[Char] = ???
  def subSequence(beginIndex: scala.Int, endIndex: scala.Int): CharSequence = ???
  def substring(beginIndex: scala.Int): _String = ???
  def substring(beginIndex: scala.Int, endIndex: scala.Int): _String = ???
  def compareTo(another_String: _String): scala.Int = ???
  def compareToIgnoreCase(another_String: _String): scala.Int = ???
}

object _String {
  def valueOf(obj: Object): _String = ???
  def valueOf(data: Array[Char]): _String = ???
  def valueOf(data: Array[Char], offset: scala.Int, count: scala.Int): _String = ???
  def valueOf(b: scala.Boolean): _String = ???
  def valueof(c: scala.Char): _String = ???
  def valueOf(i: scala.Int): _String = ???
  def valueOf(l: scala.Long): _String = ???
  def valueOf(f: scala.Float): _String = ???
  def valueOf(d: scala.Double): _String = ???
}
