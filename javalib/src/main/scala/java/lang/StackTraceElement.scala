package java.lang

import scalanative.native.{CString, fromCString}
import scalanative.libc.string.strlen

final class StackTraceElement(val getClassName: String,
                              val getMethodName: String,
                              val getFileName: String,
                              val getLineNumber: Int) {

  if (getClassName == null) {
    throw new NullPointerException("Declaring class is null")
  }
  if (getMethodName == null) {
    throw new NullPointerException("Method name is null")
  }

  def isNativeMethod: scala.Boolean = false

  override def toString: String = {
    val (file, line) =
      if (getFileName == null) ("Unknown Source", "")
      else if (getLineNumber <= 0) (getFileName, "")
      else (getFileName, ":" + getLineNumber)
    s"$getClassName.$getMethodName($file$line)"
  }

  override def hashCode: scala.Int =
    toString.##

  override def equals(that: Any): scala.Boolean =
    that match {
      case that: StackTraceElement =>
        getClassName == that.getClassName &&
          getMethodName == that.getMethodName &&
          getFileName == that.getFileName &&
          getLineNumber == that.getLineNumber
      case _ =>
        false
    }
}

private[lang] object StackTraceElement {
  object Fail extends scala.util.control.NoStackTrace

  def fromSymbol(sym: CString): StackTraceElement = {
    val len        = strlen(sym)
    var pos        = 0
    var className  = ""
    var methodName = ""

    def readSymbol(): Boolean = {
      if (read != '_') {
        false
      } else if (read != 'S') {
        false
      } else {
        readGlobal()
      }
    }

    def readGlobal(): Boolean = read() match {
      case 'M' =>
        val id = readIdent()
        if (id.length == 0) {
          false
        } else {
          className = id
          readSig()
        }
      case _ =>
        false
    }

    def readSig(): Boolean = read() match {
      case 'R' =>
        methodName = "<init>"
        true
      case 'D' | 'P' | 'C' | 'G' =>
        val id = readIdent()
        if (id.length == 0) {
          false
        } else {
          methodName = id
          true
        }
      case 'K' =>
        readSig()
      case _ =>
        false
    }

    def readIdent(): String = {
      val n = readNumber()
      if (n <= 0) {
        ""
      } else if (!inBounds(pos) || !inBounds(pos + n)) {
        ""
      } else {
        val chars = new Array[Char](n)
        var i     = 0
        while (i < n) {
          chars(i) = sym(pos + i).toChar
          i += 1
        }
        pos += n
        new String(chars)
      }
    }

    def readNumber(): Int = {
      val start  = pos
      var number = 0
      while ('0' <= at(pos) && at(pos) <= '9') {
        number = number * 10 + (at(pos) - '0').toInt
        pos += 1
      }
      if (start == pos) {
        -1
      } else {
        number
      }
    }

    def read(): Char = {
      if (inBounds(pos)) {
        val res = sym(pos).toChar
        pos += 1
        res
      } else {
        -1.toChar
      }
    }

    def at(pos: Int): Char = {
      if (inBounds(pos)) {
        sym(pos).toChar
      } else {
        -1.toChar
      }
    }

    def inBounds(pos: Int) =
      pos >= 0 && pos < len

    if (!readSymbol()) {
      className = "<none>"
      methodName = fromCString(sym)
    }

    new StackTraceElement(className, methodName, null, 0)
  }
}
