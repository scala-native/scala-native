package scala.scalanative
package util

import scala.language.implicitConversions

sealed trait ShowBuilder {
  protected def out: Appendable
  private var indentation = 0

  def str(value: Any): Unit =
    out.append(value.toString)

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
    out.append("\n")
    out.append("  " * indentation)
  }

}

object ShowBuilder {
  final class InMemoryShowBuilder extends ShowBuilder {
    override protected val out: Appendable = new java.lang.StringBuilder
    override def toString: String          = out.toString
  }

  final class FileShowBuilder(protected val out: java.io.Writer)
      extends ShowBuilder
}
