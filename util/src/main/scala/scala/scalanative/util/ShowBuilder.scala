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
      extends ShowBuilder {
    override def str(value: Any): Unit = {
      val body = value.toString
      out.append {
        /* Writer may have problems with strings containing surrogate pairs and throw exception,
         * but such cases may be observed only when dumping linked and optimized defns.
         * In lowered code strings are already transformed into array[byte] and effectively into safe string
         */
        if (!body.exists(_.isSurrogate)) body
        else body.filterNot(_.isSurrogate)
      }
    }
  }
}
