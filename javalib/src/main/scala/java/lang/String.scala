package java.lang

import java.io.Serializable

class _String private() extends CharSequence with Serializable with Comparable[_String] {
  def this(value: Array[scala.Char], offset: scala.Int, count: scala.Int) = this()

  def length(): scala.Int = ???
  def charAt(index: scala.Int): Char = ???
  def compareTo(another_String: _String): scala.Int = ???
  def subSequence(beginIndex: scala.Int, endIndex: scala.Int): CharSequence = ???
  def concat(str: _String): _String = ???
  def indexOf(ch: scala.Int): scala.Int = ???
  def indexOf(ch: scala.Int, fromIndex: scala.Int): scala.Int = ???
  def lastIndexOf(ch: scala.Int): scala.Int = ???
  def lastIndexOf(ch: scala.Int, fromIndex: scala.Int): scala.Int = ???
  def substring(beginIndex: scala.Int): _String = ???
  def substring(beginIndex: scala.Int, endIndex: scala.Int): _String = ???
  def startsWith(prefix: _String): scala.Boolean = ???
  def toLowerCase(): _String = ???
  def toCharArray(): Array[Char] = ???
  def equalsIgnoreCase(str: _String): scala.Boolean = ???
  def getChars(srcBegin: scala.Int, srcEnd: scala.Int, dst: Array[scala.Char],
      dstBegin: scala.Int): Unit = ???
  override def hashCode(): scala.Int = ???

  // TODO: rest of the api
}

object _String {
  def valueOf(obj: Object): _String = ???
  def valueOf(data: Array[Char]): _String = ???
  def valueOf(data: Array[Char], offset: scala.Int, count: scala.Int): _String = ???
  def valueOf(b: scala.Boolean): _String = ???
  def valueOf(c: scala.Char): _String = ???
  def valueOf(s: scala.Short): _String = ???
  def valueOf(i: scala.Int): _String = ???
  def valueOf(l: scala.Long): _String = ???
  def valueOf(f: scala.Float): _String = ???
  def valueOf(d: scala.Double): _String = ???
  def format(format: String, args: scala.Array[Object]): _String = ???
}
