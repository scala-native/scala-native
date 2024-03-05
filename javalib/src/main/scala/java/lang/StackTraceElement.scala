package java.lang

import scalanative.unsafe.{CString, fromCString}
import scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.runtime.SymbolFormatter
import scala.scalanative.runtime.Backtrace
import scala.scalanative.meta.LinktimeInfo.isWindows

final class StackTraceElement(
    val getClassName: String,
    val getMethodName: String,
    val getFileName: String,
    val getLineNumber: Int
) {

  if (getClassName == null) {
    throw new NullPointerException("Declaring class is null")
  }
  if (getMethodName == null) {
    throw new NullPointerException("Method name is null")
  }

  def isNativeMethod: scala.Boolean = false

  override def toString: String = {
    val (file, line) =
      if (getFileName == null || getFileName.isEmpty()) ("Unknown Source", "")
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
