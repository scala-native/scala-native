package java.util

import java.io._

class Formatter {
  def this(appendable: Appendable, locale: Locale) = this()
  def this(appendable: Appendable) = this()
  def this(file: File, charsetName: String, locale: Locale) = this()
  def this(file: File, charsetName: String) = this()
  def this(file: File) = this()
  def this(locale: Locale) = this()
  def this(out: OutputStream, charsetName: String, locale: Locale) = this()
  def this(out: OutputStream, charsetName: String) = this()
  def this(out: OutputStream) = this()
  def this(out: PrintStream) = this()
  def this(fileName: String, charsetName: String, locale: Locale) = this()
  def this(fileName: String, charsetName: String) = this()
  def this(fileName: String) = this()

  def close(): Unit = ???
  def flush(): Unit = ???
  def format(locale: Locale, format: String, args: Array[Object]): Formatter =
    ???
  def format(format: String, args: Array[Object]): Formatter = ???
  def ioException(): IOException                             = ???
  def locale(): Locale                                       = ???
  def out(): Appendable                                      = ???
  override def toString: String                              = ???
}

object Formatter
