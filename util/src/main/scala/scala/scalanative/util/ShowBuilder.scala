package scala.scalanative
package util

import scala.language.implicitConversions

sealed trait ShowBuilder {
  protected def out: Appendable
  private var indentation = 0

  def str(v: Char): Unit = out.append(v)
  def str(v: CharSequence): Unit = out.append(v)
  def str(value: Int): Unit =
    out.append(java.lang.Integer.toString(value))
  def str(value: Long): Unit =
    out.append(java.lang.Long.toString(value))
  def str(value: Short): Unit =
    out.append(java.lang.Short.toString(value))
  def str(value: Byte): Unit =
    out.append(java.lang.Byte.toString(value))
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
    var i = 0
    while (i < indentation) {
      out.append("  ")
      i += 1
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
