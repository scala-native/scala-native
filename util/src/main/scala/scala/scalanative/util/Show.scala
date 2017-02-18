package scala.scalanative
package util

import scala.language.implicitConversions

final class ShowBuilder {
  private var sb          = new java.lang.StringBuilder
  private var indentation = 0

  def str(value: Any): Unit =
    sb.append(value.toString)

  def line(value: Any): Unit = {
    str(value)
    newline()
  }

  def rep[T](values: Seq[T], sep: String = "")(f: T => Unit): Unit =
    if (values.nonEmpty) {
      values.init.foreach { value =>
        f(value)
        str(sep)
      }
      f(values.last)
    }

  def indent(n: Int = 1): Unit =
    indentation += n

  def unindent(n: Int = 1): Unit =
    indentation -= n

  def newline(): Unit = {
    sb.append("\n")
    sb.append("  " * indentation)
  }

  def clear(): Unit = {
    indentation = 0
    sb = new java.lang.StringBuilder
  }

  override def toString = sb.toString
}
