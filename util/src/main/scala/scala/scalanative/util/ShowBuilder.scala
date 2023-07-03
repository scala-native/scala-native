package scala.scalanative
package util

import scala.language.implicitConversions

sealed trait ShowBuilder {
  protected def out: Appendable
  private var indentation = 0

  def str(v: Char): Unit = out.append(v)
  def str(v: CharSequence): Unit = out.append(v)
  def str(value: Any): Unit =
    out.append(value.toString)

  def quoted(v: CharSequence): Unit = {
    out.append('"')
    out.append(v)
    out.append('"')
  }

  def line(value: Any): Unit = {
    str(value)
    newline()
  }

  def rep[T](values: Iterable[T], sep: String = "")(f: T => Unit): Unit = {
    val it = values.iterator
    if (it.hasNext) {
      while ({
        f(it.next())
        it.hasNext
      }) str(sep)
    }
  }

  def indent(n: Int = 1): Unit =
    indentation += n

  def unindent(n: Int = 1): Unit =
    indentation -= n

  def newline(): Unit = {
    out.append("\n")
    for (_ <- 0.until(indentation)) {
      out.append("  ")
    }
  }

}

object ShowBuilder {
  final class InMemoryShowBuilder extends ShowBuilder {
    override protected val out: Appendable = new java.lang.StringBuilder
    override def toString: String = out.toString
  }

  final class FileShowBuilder(protected val out: java.io.Writer)
      extends ShowBuilder
}
